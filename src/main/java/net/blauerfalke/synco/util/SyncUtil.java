/*
 * Copyright 2018 Michael Stößer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.blauerfalke.synco.util;

import lombok.extern.slf4j.Slf4j;
import net.blauerfalke.synco.conf.SyncConfiguration;
import net.blauerfalke.synco.model.Diff;
import net.blauerfalke.synco.model.Syncable;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Stream;


@Slf4j
public class SyncUtil {

    private static final Class<?>[] INTEGER_TYPES = new Class[]{Integer.TYPE, Integer.class};
    private static final Class<?>[] LONG_TYPES = new Class[]{Long.TYPE, Long.class};
    private static final Class<?>[] FLOAT_TYPES = new Class[]{Float.TYPE, Float.class};
    private static final Class<?>[] DOUBLE_TYPES = new Class[]{Double.TYPE, Double.class};
    private static final Class<?>[] BOOLEAN_TYPES = new Class[]{Boolean.TYPE, Boolean.class};

    private static Class<?>[] SUPPORTED_TYPES = new Class[] { Long.TYPE, Integer.TYPE, Double.TYPE, Float.TYPE, Boolean.TYPE,
            Long.class, Integer.class, Double.class, Float.class, Boolean.class, String.class, List.class };

    public static Map<String,Diff> calculateChanges(Syncable base, Syncable syncable, SyncConfiguration syncConfiguration) {
        Map<String, Diff> diffs = new TreeMap<>();
        Map<String,Object> baseFields = getSyncableFields(base, syncConfiguration);
        Map<String,Object> syncableFields = getSyncableFields(syncable, syncConfiguration);
        for(Map.Entry<String,Object> entry : baseFields.entrySet()) {
            final String key = entry.getKey();
            final Object from = entry.getValue();
            final Object to = syncableFields.get(key);
            if(!equals(from, to)) {
                diffs.put(key, new Diff<>(from, to));
            }
        }
        return diffs;
    }

    public static boolean equals(Object a, Object b) {
        return (a != null && a.equals(b)) || (a == null && b == null);
    }

    public static void applyDiff(Syncable syncable, String fieldName, Diff diff) {

        try {
            Field field = syncable.getClass().getDeclaredField(fieldName);
            Class<?> fieldType = field.getType();
            if (Modifier.isPublic(field.getModifiers()) && !Modifier.isStatic(field.getModifiers()) && isSupportedType(fieldType)) {
                try {
                    field.set(syncable, diff.to);
                    return;
                } catch (IllegalArgumentException e) {
                    if(diff.to == null && isPrimitiveType(fieldType) ) {
                        log.warn("could not set null value to primitive-type '{}' for syncable: '{}' ('{}')", fieldName, syncable.getId(), syncable.getClass().getSimpleName());
                    } else {
                        log.warn("could not set field '{}' in applyDiff for syncable: '{}' ('{}')", fieldName, syncable.getId(), syncable.getClass().getSimpleName());
                    }
                } catch (IllegalAccessException e) {
                    log.warn("could not access field '{}' in applyDiff for syncable: '{}' ('{}')", fieldName, syncable.getId(), syncable.getClass().getSimpleName());
                }
            }
        } catch (NoSuchFieldException e) {
            // some kind of expected behavior
            log.debug("could not find field '{}' in applyDiff for syncable: '{}' ('{}')", fieldName, syncable.getId(), syncable.getClass().getSimpleName());
        }

        try {
            Class<?> setterType = findType(diff.to, diff.from);
            Method method = setterType != null ?
                    findSetterMethodForFieldName(fieldName, syncable, mapTypes(setterType))
                    : findSetterMethodForFieldName(fieldName, syncable, SUPPORTED_TYPES);
            if (Modifier.isPublic(method.getModifiers()) && !Modifier.isStatic(method.getModifiers()) && !Modifier.isAbstract(method.getModifiers()) && method.getParameterTypes().length == 1 && isSupportedType(method.getParameterTypes()[0])) {
                try {
                    method.invoke(syncable, diff.to);
                    return;
                } catch (IllegalArgumentException e) {
                    if(diff.to == null && isPrimitiveType(method.getParameterTypes()[0]) ) {
                        log.warn("could not set null value to primitive-type '{}' for syncable: '{}' ('{}')", fieldName, syncable.getId(), syncable.getClass().getSimpleName());
                    } else {
                        log.warn("could not invoke method '{}' in applyDiff for syncable: '{}' ('{}') ", method.getName(), syncable.getId(), syncable.getClass().getSimpleName());
                    }
                } catch (IllegalAccessException e) {
                    log.warn("could not access method '{}' in applyDiff for syncable: '{}' ('{}')", method.getName(), syncable.getId(), syncable.getClass().getSimpleName());
                } catch (InvocationTargetException e) {
                    log.warn("wrong invocation for method '{}' in applyDiff for syncable: '{}' ('{}')", method.getName(), syncable.getId(), syncable.getClass().getSimpleName());
                }
            }
        } catch (NoSuchMethodException e) {
            log.debug("could not find method for field '{}' in applyDiff for syncable: '{}' ('{}')", fieldName, syncable.getId(), syncable.getClass().getSimpleName());
        }
        log.error("could not applyDiff for field '{}' to syncable: '{}' ('{}')", fieldName, syncable.getId(), syncable.getClass().getSimpleName());
    }

    private static boolean isPrimitiveType(Class<?> type) {
        return Stream.of(Integer.TYPE, Long.TYPE, Float.TYPE, Double.TYPE).anyMatch((t)->t.equals(type));
    }
    private static Class<?>[] mapTypes(Class<?> type) {
        if (type.equals(Integer.class)) {
            return INTEGER_TYPES;
        } else if (type.equals(Long.class)) {
            return LONG_TYPES;
        } else if (type.equals(Float.class)) {
            return FLOAT_TYPES;
        } else if (type.equals(Double.class)) {
            return DOUBLE_TYPES;
        } else if (type.equals(Boolean.class)) {
            return BOOLEAN_TYPES;
        } else if(List.class.isAssignableFrom(type)) {
            return new Class[] {List.class};
        }
        return new Class[] {type};
    }

    private static Map<String,Object> getSyncableFields(Syncable syncable, SyncConfiguration syncConfiguration) {
        SortedMap<String,Object> result = new TreeMap<>();

        List<String> syncFields = syncConfiguration.getSyncableFieldsForType(syncable.getClass());
        if(!syncFields.isEmpty()) {
            for (String fieldName : syncFields) {
                try {
                    if (fromField(fieldName, syncable.getClass().getDeclaredField(fieldName), syncable, result))
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

    private static Method findSetterMethodForFieldName(String fieldName, Object syncable, Class<?> ... types) throws NoSuchMethodException {
        String[] methodNames = new String[] {
                "set"+fieldName.substring(0,1).toUpperCase()+fieldName.substring(1),
                "set"+fieldName,
                fieldName
        };
        for(Class<?> setterType : types) {
            for (String methodName : methodNames) {
                try {
                    return syncable.getClass().getDeclaredMethod(methodName, setterType);
                } catch (NoSuchMethodException e) {
                    // expected behavior: cause we search for more than one method name
                }
            }
        }
        throw new NoSuchMethodException("No method for field '"+fieldName+"' found.");
    }

    private static Method findGetterMethodForFieldName(String fieldName, Object syncable) throws NoSuchMethodException {
        String[] methodNames = new String[] {
                "get"+fieldName.substring(0,1).toUpperCase()+fieldName.substring(1),
                fieldName,
                "get"+fieldName,
                "is"+fieldName.substring(0,1).toUpperCase()+fieldName.substring(1),
                "is"+fieldName
        };
        for(String methodName : methodNames) {
            try {
                return syncable.getClass().getDeclaredMethod(methodName);
            } catch (NoSuchMethodException e) {
                // expected behavior: cause we search for more than one method name
            }
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

    private static void fromMethod(String fieldName, Method method, Object syncable, Map<String,Object> result) {
        String methodName = method.getName();
        if(methodName.startsWith("get") || methodName.startsWith("is")) {
            if(!result.containsKey(fieldName)) {
                Class<?> returnType = method.getReturnType();
                if (Modifier.isPublic(method.getModifiers()) && !Modifier.isStatic(method.getModifiers()) && !Modifier.isAbstract(method.getModifiers()) && isSupportedType(returnType) && method.getParameterTypes().length == 0) {
                    try {
                        result.put(fieldName, method.invoke(syncable));
                    } catch (Exception e) {
                        log.error("error invoking method '{}' for field '{}' on '{}' ", method.getName(), fieldName, syncable.toString());
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private static String extractFieldNameFromMethodName(String methodName) {
        if(methodName.startsWith("get")) {
            return methodName.substring(3,4).toLowerCase()+methodName.substring(4);
        }
        if(methodName.startsWith("is")) {
            return methodName.substring(2,3).toLowerCase()+methodName.substring(3);
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
        return Stream.of(SUPPORTED_TYPES).anyMatch(
                (t) -> t.equals(fieldType)
        );
    }

    public static Class<?> findType(Object ... objects) {
        for(Object o : objects) if(o != null) return o.getClass();
        return null;
    }
}
