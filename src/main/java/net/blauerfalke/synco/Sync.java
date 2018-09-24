package net.blauerfalke.synco;

import lombok.extern.slf4j.Slf4j;
import net.blauerfalke.synco.conf.SyncConfiguration;
import net.blauerfalke.synco.merge.DefaultFieldMergeStrategy;
import net.blauerfalke.synco.merge.MergeStrategy;
import net.blauerfalke.synco.merge.conflict.MergeConflictStrategy;
import net.blauerfalke.synco.merge.conflict.TakeNewerOnMergeConflictStrategy;
import net.blauerfalke.synco.merge.field.FieldMergeStrategy;
import net.blauerfalke.synco.merge.field.ListFieldMergeStrategy;
import net.blauerfalke.synco.merge.field.SimpleFieldMergeStrategy;
import net.blauerfalke.synco.model.Diff;
import net.blauerfalke.synco.model.SyncTriple;
import net.blauerfalke.synco.model.Syncable;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

@Slf4j
public class Sync {

    private static Map<Class<?>,Object> singletons = new HashMap<Class<?>, Object>();
    static {
        singletons.put(DefaultFieldMergeStrategy.class, new DefaultFieldMergeStrategy());
        singletons.put(ListFieldMergeStrategy.class, new ListFieldMergeStrategy());
        singletons.put(SimpleFieldMergeStrategy.class, new SimpleFieldMergeStrategy());
        singletons.put(TakeNewerOnMergeConflictStrategy.class, new TakeNewerOnMergeConflictStrategy());
    }

    SyncableProvider provider;
    SyncableProvider remoteAdapter;
    SyncConfiguration syncConfiguration;

    public Sync(SyncableProvider provider, SyncableProvider remoteAdapter) {
        this(provider, remoteAdapter, new SyncConfiguration());
    }
    public Sync(SyncableProvider provider, SyncableProvider remoteAdapter, SyncConfiguration syncConfiguration) {
        this.provider = provider;
        this.remoteAdapter = remoteAdapter;
        this.syncConfiguration = syncConfiguration;
    }

    public void syncSyncable(String id) {

        Syncable local = provider.load(id);
        Syncable base = provider.load(local.getId()+"-base");
        Syncable remote = remoteAdapter.load(local.getId());

        if(local.getUpdated() == base.getUpdated()) {
            // no local changes -> check for remote changes

            if(base.getUpdated() == remote.getUpdated()) {
                // no local changes, no remote -> nothing to do

            } else if(base.getUpdated() < remote.getUpdated()) {
                // not local changes, remote changes -> pull
                provider.save(id, remote);
                provider.save(id+"-base", remote);
            } else {
                // base is newer than remote -> should never happen -> (but fixed with push)
                push(local);
            }

        } else if(local.getUpdated() > base.getUpdated()) {
            // local changes

            if(base.getUpdated() == remote.getUpdated()) {
                // local changes, no remote changes -> push
                push(local);

            } else if(base.getUpdated() < remote.getUpdated()) {
                // local changes, remote changes -> merge

                MergeStrategy mergeStrategy = getMergeStrategy(findType(base, local, remote));
                Syncable merged = mergeStrategy.merge(this, new SyncTriple(base, local, remote));
                push(merged);

            } else {
                // remote is older than base -> should never happen -> (but fixed with push)
//                Log.error(TAG, "remote-version is older than base-version from syncable:"+id);
                push(local);
            }
        } else {
            // local is older than base -> should never happen -> (but fixed with save base-version)
            log.error("local-version is older than base-version from syncable:"+id);
            provider.save(id+"-base", local);
        }
    }

    public static Map<String,Diff> calculateChanges(Syncable base, Syncable syncable) {
        Map<String, Diff> diffs = new TreeMap<String, Diff>();
        Map<String,Object> baseFields = getSyncableFields(base);
        Map<String,Object> syncableFields = getSyncableFields(syncable);
        for(Map.Entry<String,Object> entry : baseFields.entrySet()) {
            diffs.put(entry.getKey(), new Diff(entry.getValue(), syncableFields.get(entry.getKey())));
        }
        return diffs;
    }

    public void push(Syncable syncable) {
        Syncable lastRemote = remoteAdapter.save(syncable.getId(), syncable);
        provider.save(syncable.getId()+"-base", lastRemote);
    }


    public static Class<?> findType(Object ... objects) {
        for(Object o : objects) if(o != null) return o.getClass();
        return null;
    }

    public MergeStrategy getMergeStrategy(Class<?> type) {
        MergeStrategy mergeStrategy = syncConfiguration.findMergeStrategyForType(type);
        if(null != mergeStrategy)
            return mergeStrategy;
        return (MergeStrategy) singletons.get(DefaultFieldMergeStrategy.class);
    }

    public FieldMergeStrategy getFieldMergeStrategy(Class<?> syncableType, String fieldName, Class<?> fieldType) {
        FieldMergeStrategy fieldMergeStrategy = syncConfiguration.findFieldMergeStrategyForSyncableTypeAndFieldName(syncableType, fieldName);
        if(null != fieldMergeStrategy)
            return fieldMergeStrategy;

        if(fieldType.equals(List.class)) {
            return (FieldMergeStrategy) singletons.get(ListFieldMergeStrategy.class);
        }
        return (FieldMergeStrategy) singletons.get(SimpleFieldMergeStrategy.class);
    }

    public MergeConflictStrategy getMergeConflictStrategy(Class<?> syncableType, String fieldName, Class<?> fieldType) {
        MergeConflictStrategy fieldMergeStrategy = syncConfiguration.findMergeConflictStrategyForSyncableTypeAndFieldName(syncableType, fieldName);
        if(null != fieldMergeStrategy)
            return fieldMergeStrategy;

        fieldMergeStrategy = syncConfiguration.findMergeConflictStrategyForSyncableTypeAndFieldType(syncableType, fieldType);
        if(null != fieldMergeStrategy)
            return fieldMergeStrategy;

        return (MergeConflictStrategy) singletons.get(TakeNewerOnMergeConflictStrategy.class);
    }



    public Syncable cloneSyncable(Syncable syncable) {
        return syncable;
    }

//
//    public static void main(String[] args) {
//        ClimbingRegion region = new ClimbingRegion();
//        region.setId("someId");
//        region.setName("aName");
//
//        System.out.println("fieldName:"+extractFieldNameFromMethodName("getBlub"));
//        System.out.println("fieldName:"+extractFieldNameFromMethodName("getblub"));
//        System.out.println("fieldName:"+extractFieldNameFromMethodName("blub"));
//
//        Map<String,Object> fields = getSyncableFields(region);
//
//        System.out.println("data:"+StringHelper.outputMap(fields));
//    }

    public static boolean applyDiff(Syncable syncable, String fieldName, Diff diff) {

        try {
            Field field = syncable.getClass().getField(fieldName);
            Class<?> fieldType = field.getType();
            if (Modifier.isPublic(field.getModifiers()) && !Modifier.isStatic(field.getModifiers()) && isSupportedType(fieldType)) {
                try {
                    field.set(syncable, diff.to);
                    return true;
                } catch (IllegalAccessException e) {
                    log.warn("could not access field '"+fieldName+"' in applyDiff for syncable:"+syncable.getId()+" ("+syncable.getClass().getSimpleName()+")");
                }
            }
        } catch (NoSuchFieldException e) {
            // some kind of expected behavior
            log.debug("could not find field '"+fieldName+"' in applyDiff for syncable:"+syncable.getId()+" ("+syncable.getClass().getSimpleName()+")");
        }

        try {
            Method method = findSetterMethodForFieldName(fieldName, syncable);
            if (Modifier.isPublic(method.getModifiers()) && !Modifier.isStatic(method.getModifiers()) && !Modifier.isAbstract(method.getModifiers()) && method.getParameterTypes().length == 1 && isSupportedType(method.getParameterTypes()[0])) {
                try {
                    method.invoke(syncable, diff.to);
                    return true;
                } catch (IllegalAccessException e) {
                    log.warn("could not access method '"+method.getName()+"' in applyDiff for syncable:"+syncable.getId()+" ("+syncable.getClass().getSimpleName()+")");
                } catch (InvocationTargetException e) {
                    log.warn("wrong invocation for method '"+method.getName()+"' in applyDiff for syncable:"+syncable.getId()+" ("+syncable.getClass().getSimpleName()+")");
                }
            }
        } catch (NoSuchMethodException e) {
            log.debug("could not find method for field '"+fieldName+"' in applyDiff for syncable:"+syncable.getId()+" ("+syncable.getClass().getSimpleName()+")");
        }
        log.error("could not applyDiff for field '"+fieldName+"' to syncable:"+syncable.getId()+" ("+syncable.getClass().getSimpleName()+")");
        return false;
    }

    private static Map<String,Object> getSyncableFields(Syncable syncable) {
        SortedMap<String,Object> result = new TreeMap<String, Object>();

        List<String> syncFields = getSyncableFieldConfiguration(syncable);
        if(!syncFields.isEmpty()) {
            for (String fieldName : syncFields) {
                try {
                    if (fromField(fieldName, syncable.getClass().getField(fieldName), syncable, result))
                        continue;
                } catch (NoSuchFieldException e) { //expected behavior
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    fromMethod(fieldName, findGetterMethodForFieldName(fieldName, syncable),syncable, result);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return result;
        }

        Field[] fields = syncable.getClass().getDeclaredFields();
        for(Field field : fields) {
           fromField(field.getName(), field, syncable, result);
        }

        Method[] methods = syncable.getClass().getDeclaredMethods();
        for(Method method : methods) {
            fromMethod(extractFieldNameFromMethodName(method.getName()), method, syncable, result);
        }
        return result;
    }

    private static Method findSetterMethodForFieldName(String fieldName, Object syncable) throws NoSuchMethodException {
        String[] methodNames = new String[] {
                "set"+fieldName.substring(0,1).toUpperCase()+fieldName.substring(1),
                "set"+fieldName,
                fieldName
        };
        for(String methodName : methodNames) {
            try {
                return syncable.getClass().getMethod(methodName);
            } catch (NoSuchMethodException e) {}
        }
        throw new NoSuchMethodException("No method for field '"+fieldName+"' found.");
    }
    private static Method findGetterMethodForFieldName(String fieldName, Object syncable) throws NoSuchMethodException {
        String[] methodNames = new String[] {
                "get"+fieldName.substring(0,1).toUpperCase()+fieldName.substring(1),
                fieldName,
                "get"+fieldName
        };
        for(String methodName : methodNames) {
            try {
                return syncable.getClass().getMethod(methodName);
            } catch (NoSuchMethodException e) {}
        }
        throw new NoSuchMethodException("No method for field '"+fieldName+"' found.");
    }

    private static boolean fromField(String fieldName, Field field, Object syncable, Map<String,Object> result) {
        Class<?> fieldType = field.getType();
        if(Modifier.isPublic(field.getModifiers()) && !Modifier.isStatic(field.getModifiers()) && isSupportedType(fieldType)) {
            try {
                result.put(fieldName, getFieldValue(field, syncable));
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private static boolean fromMethod(String fieldName, Method method, Object syncable, Map<String,Object> result) {
        String methodName = method.getName();
        if(methodName.startsWith("get")) {
            if(!result.containsKey(fieldName)) {
                Class<?> returnType = method.getReturnType();
                if (Modifier.isPublic(method.getModifiers()) && !Modifier.isStatic(method.getModifiers()) && !Modifier.isAbstract(method.getModifiers()) && isSupportedType(returnType) && method.getParameterTypes().length == 0) {
                    try {
                        result.put(fieldName, method.invoke(syncable));
                        return true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return false;
    }

    private static List<String> getSyncableFieldConfiguration(Syncable syncable) {
        return Collections.emptyList();
    }


    private static String extractFieldNameFromMethodName(String methodName) {
        if(methodName.startsWith("get")) {
            return methodName.substring(3,4).toLowerCase()+methodName.substring(4);
        }
        return methodName.substring(0,1).toLowerCase()+methodName.substring(1);
    }

    private static Object getFieldValue(Field field, Object object) throws IllegalAccessException {
        return field.get(object);
       /* if(field.getType().equals(Integer.TYPE)) {
            return field.getInt(object);
        } else if(field.getType() == Float.class) {
            return field.getFloat(object);
        } else if(field.getType() == Double.class) {
            return field.getDouble(object);
        } else if(field.getType() == Double.class) {
            return field.getDouble(object);
        } else if(field.getType() == Boolean.class) {
            return field.getDouble(object);
        } else if(field.getType() == String.class) {
            return (String) field.get(object);
        } else if(field.getType() == List.class) {
            return field.getDouble(object);
        }
        return null;*/
    }

    private static boolean isSupportedType(Class<?> fieldType) {
        return fieldType.equals(Integer.TYPE) || fieldType.equals(Double.TYPE) || fieldType.equals(Float.TYPE) || fieldType.equals(Boolean.TYPE)|| fieldType.equals(String.class)|| fieldType.equals(List.class);
    }

    private static String hash(SortedMap<String,Object> map) {

        return "";
    }
}
