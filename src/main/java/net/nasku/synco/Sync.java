package net.nasku.synco;


import net.nasku.synco.converter.HttpPostVarToStringConverter;
import net.nasku.synco.converter.HttpPostVarToStringListConverter;
import net.nasku.synco.converter.ObjectToSyncEntityConverter;
import net.nasku.synco.converter.ObjectToSyncEntityListConverterList;
import net.nasku.synco.converter.interf.Converter;
import net.nasku.synco.converter.ConverterChain;
import net.nasku.synco.converter.interf.ConverterList;
import net.nasku.synco.converter.JSONArrayToStringConverter;
import net.nasku.synco.converter.JSONArrayToSyncEntityConverterList;
import net.nasku.synco.converter.JSONObjectToStringConverter;
import net.nasku.synco.converter.JSONObjectToSyncEntityConverter;
import net.nasku.synco.converter.interf.ListConverter;
import net.nasku.synco.converter.interf.ListConverterList;
import net.nasku.synco.converter.StringToJSONArrayConverter;
import net.nasku.synco.converter.StringToJSONObjectConverter;
import net.nasku.synco.converter.SyncEntityToHttpPostVarConverter;
import net.nasku.synco.converter.SyncEntityToJSONObjectConverter;
import net.nasku.synco.endpoint.interf.Endpoint;
import net.nasku.synco.interf.SyncStrategy;
import net.nasku.synco.model.HttpPostVar;
import net.nasku.synco.model.SyncEntity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Seven on 23.08.2016.
 */
public class Sync {

    SyncConfig syncConfig;

    SyncStrategy syncStrategy;

    Endpoint srcEndpoint;
    Endpoint destEndpoint;

    public Sync(SyncConfig config) {
        this.syncConfig = config;
        if(!loaded)
            load();
        final SyncConfig srcEnpointConfig = config.getSyncConfig("srcEndpoint");
        final SyncConfig destEnpointConfig = config.getSyncConfig("destEndpoint");
        final String srcEndpointClassname = srcEnpointConfig.getString("class", "");
        final String destEndpointClassname = destEnpointConfig.getString("class", "");
        if(srcEndpointClassname.length()>0)
            this.srcEndpoint = (Endpoint) Sync.load(srcEndpointClassname, srcEnpointConfig);
        if(destEndpointClassname.length()>0)
            this.destEndpoint = (Endpoint) Sync.load(destEndpointClassname, destEnpointConfig);
        this.syncStrategy = (SyncStrategy) Sync.load(config.getString("syncStrategy", ServerMergeSyncStrategy.class.getName()), config);
    }

    public static void main(String[] args) {
        String test = "";
        String[] arr = test.split("\\.");
        for(String a : arr)
            System.out.println("t:"+a);
    }

    public void sync() {
        sync(this.srcEndpoint, this.destEndpoint);
    }

    public void sync(final Endpoint src) {
        sync(src, this.destEndpoint);
    }

    public void sync(Endpoint src, Endpoint dest) {
        this.srcEndpoint = src;
        this.destEndpoint = dest;
        syncStrategy.sync(this);
    }

    public void pushDest(List<SyncEntity> entities) {
        if(null == destEndpoint)
            throw new RuntimeException("No destEnpoint found");
        destEndpoint.push(entities);
    }

    public void pullDest(List<SyncEntity> entities) {
        pullDest(entities, -1);
    }
    public void pullDest(List<SyncEntity> entities, int limit) {
        if(null == destEndpoint)
            throw new RuntimeException("No destEnpoint found");
        destEndpoint.pull(entities, limit);
    }

    public void pullSrc(List<SyncEntity> entities) {
        pullSrc(entities, -1);
    }

    public void pullSrc(List<SyncEntity> entities, int limit) {
        if(null == srcEndpoint)
            throw new RuntimeException("No srcEnpoint found");
        srcEndpoint.pull(entities, limit);
    }

    public void pushSrc(List<SyncEntity> entities) {
        if(null == srcEndpoint)
            throw new RuntimeException("No srcEnpoint found");
        srcEndpoint.push(entities);
//        for(SyncEntity entity : entities) {
//            pushSrc(entity);
//        }
    }

    private static boolean loaded = false;

    private static void load() {
        convertersByName = new HashMap<>();
        convertersByType = new HashMap<>();
        listConvertersByType = new HashMap<>();
        convertersListByType = new HashMap<>();
        listConvertersListByType = new HashMap<>();

        registerConverter(JSONObject.class, String.class, new JSONObjectToStringConverter());
        registerConverter(JSONArray.class, String.class, new JSONArrayToStringConverter());
        registerConverter(String.class, JSONObject.class, new StringToJSONObjectConverter());
        registerConverter(String.class, JSONArray.class, new StringToJSONArrayConverter());
        registerConverter(JSONObject.class, SyncEntity.class, new JSONObjectToSyncEntityConverter());
        registerConverter(JSONArray.class, SyncEntity.class, new JSONArrayToSyncEntityConverterList());
        registerConverter(SyncEntity.class, JSONObject.class, new SyncEntityToJSONObjectConverter());

        registerConverter(SyncEntity.class, HttpPostVar.class, new SyncEntityToHttpPostVarConverter());
        registerConverter(Object.class, SyncEntity.class, new ObjectToSyncEntityConverter());
        registerConverter(Object.class, SyncEntity.class, new ObjectToSyncEntityListConverterList());
        registerConverter(HttpPostVar.class, String.class, new HttpPostVarToStringConverter());
//        registerConverter(HttpPostVar.class, String.class, new HttpPostVarToStringListConverter()); //not ready yet
        loaded = true;
    }

    public static void registerConverter(Class<?> from, Class<?> to, Converter<?,?> converter) {
        registerConverter(from, to, converter, null);
    }

    public static void registerConverter(Class<?> from, Class<?> to, Converter<?,?> converter, String name) {
        if(converter instanceof ListConverter) {
            if(!listConvertersByType.containsKey(from))
                listConvertersByType.put(from, new HashMap<Class<?>, ListConverter<?, ?>>());
            listConvertersByType.get(from).put(to, (ListConverter)converter);
        } else if(converter instanceof ConverterList) {
            if(!convertersListByType.containsKey(from))
                convertersListByType.put(from, new HashMap<Class<?>, ConverterList<?, ?>>());
            convertersListByType.get(from).put(to, (ConverterList)converter);
        } else if(converter instanceof ListConverterList) {
            if(!listConvertersListByType.containsKey(from))
                listConvertersListByType.put(from, new HashMap<Class<?>, ListConverterList<?, ?>>());
            listConvertersListByType.get(from).put(to, (ListConverterList)converter);
        } else {
            if(!convertersByType.containsKey(from))
                convertersByType.put(from, new HashMap<Class<?>, Converter<?, ?>>());
            convertersByType.get(from).put(to, converter);
        }

        convertersByName.put(null == name ? converter.getClass().getName() : name , converter);
    }
    public static Object load(String className, SyncConfig syncConfig) {
        try {
            Class<?> c = Class.forName(className);
            try {
                return c.getConstructor(SyncConfig.class).newInstance(syncConfig);
            } catch (NoSuchMethodException e) {
            } catch (SecurityException e) {}

            return c.newInstance();

        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

//    public static String[] trim(String[] strings) {
//        String[] ret = new String[strings.length];
//        for(int i=0;i<strings.length;i++) {
//            ret[i] = strings[i].trim();
//        }
//        return ret;
//    }

    private static Map<String,Converter<?,?>> convertersByName;
    private static Map<Class<?>,Map<Class<?>,Converter<?,?>>> convertersByType;
    private static Map<Class<?>,Map<Class<?>,ListConverter<?,?>>> listConvertersByType;
    private static Map<Class<?>,Map<Class<?>,ConverterList<?,?>>> convertersListByType;
    private static Map<Class<?>,Map<Class<?>,ListConverterList<?,?>>> listConvertersListByType;

    private static <F,T> Converter<F,T> converterByType(final Class<F> fromClass, final Class<T> toClass) {
        if(convertersByType.containsKey(fromClass)) {
            final Map<Class<?>,Converter<?,?>> conv = convertersByType.get(fromClass);
            if(conv.containsKey(toClass))
                return (Converter<F, T>) conv.get(toClass);
        }
        return null;
    }

    public static <F,T> Converter<F,T> getConverterByType(final Class<?>...classes) {
        if(classes.length < 2) {
            throw new IllegalArgumentException("Need at least two Types");
        } else if(classes.length == 2) {
            return (Converter<F,T>) converterByType(classes[0], classes[1]);
        } else {
            final ConverterChain<F, T> converterChain = new ConverterChain();
            for (int i = 1; i < classes.length; i++) {
                Converter converter = converterByType(classes[i - 1], classes[i]);
                if (null == converter)
                    return null;
                converterChain.add(converter);
            }
            return converterChain;
        }
    }

    private static <F,T> ConverterList<F,T> converterListByType(final Class<F> fromClass, final Class<T> toClass) {
        if(convertersListByType.containsKey(fromClass)) {
            final Map<Class<?>,ConverterList<?,?>> conv = convertersListByType.get(fromClass);
            if(conv.containsKey(toClass))
                return (ConverterList<F, T>) conv.get(toClass);
        }
        return null;
    }

    public static <F,T> Converter<F,List<T>> getConverterListByType(final Class<?>...classes) {
        if(classes.length < 2) {
            throw new IllegalArgumentException("Need at least two Types");
        } else if(classes.length == 2) {
            return (ConverterList<F,T>) converterListByType(classes[0], classes[1]);
        } else {
            final ConverterChain<F, T> converterChain = new ConverterChain();
            for (int i = 1; i < classes.length; i++) {
                Converter converter = i+1==classes.length ? converterListByType(classes[i - 1], classes[i]) : converterByType(classes[i - 1], classes[i]);
                if (null == converter)
                    return null;
                converterChain.add(converter);
            }
            return (Converter<F,List<T>>) converterChain;
        }
    }

    private static <F,T> ListConverter<F,T> listConverterByType(final Class<F> fromClass, final Class<T> toClass) {
        if(listConvertersByType.containsKey(fromClass)) {
            final Map<Class<?>,ListConverter<?,?>> conv = listConvertersByType.get(fromClass);
            if(conv.containsKey(toClass))
                return (ListConverter<F, T>) conv.get(toClass);
        }
        return null;
    }

    public static <F,T> Converter<List<F>,T> getListConverterByType(final Class<?>...classes) {
        if(classes.length < 2) {
            throw new IllegalArgumentException("Need at least two Types");
        } else if(classes.length == 2) {
            return (ListConverter<F,T>) listConverterByType(classes[0], classes[1]);
        } else {
            final ConverterChain<F, T> converterChain = new ConverterChain();
            boolean lastIsList = true;
            for (int i = 1; i < classes.length; i++) {
                if(lastIsList && i+1 != classes.length) { //first try to find ListToList
                    Converter converter = listConverterListByType(classes[i-1], classes[i]);
                    if (null == converter) { //fallback: try to find list to none list
                        converter = listConverterByType(classes[i - 1], classes[i]);
                        if(null == converter)
                            return null;
                        lastIsList = false;
                    }
                    converterChain.add(converter);
                } else {
                    Converter converter = listConverterByType(classes[i - 1], classes[i]);
                    if (null == converter)
                        return null;
                    converterChain.add(converter);
                }
            }
            return (Converter<List<F>,T>) converterChain;
        }
    }

    private static <F,T> ListConverterList<F,T> listConverterListByType(final Class<F> fromClass, final Class<T> toClass) {
        if(listConvertersListByType.containsKey(fromClass)) {
            final Map<Class<?>,ListConverterList<?,?>> conv = listConvertersListByType.get(fromClass);
            if(conv.containsKey(toClass))
                return (ListConverterList<F,T>) conv.get(toClass);
        }
        return null;
    }

    public static <F,T> Converter<List<F>,List<T>> getListConverterListByType(final Class<?>...classes) {
        if(classes.length < 2) {
            throw new IllegalArgumentException("Need at least two Types");
        } else if(classes.length == 2) {
            return (ListConverterList<F,T>) listConverterListByType(classes[0], classes[1]);
        } else {
            final ConverterChain<F, T> converterChain = new ConverterChain();
            for (int i = 1; i < classes.length; i++) {
                Converter converter = listConverterListByType(classes[i - 1], classes[i]);
                if (null == converter)
                    return null;
                converterChain.add(converter);
            }
            return (Converter<List<F>,List<T>>) converterChain;
        }
    }


    private static <F,T> Converter<F,T> converterByName(final String name) {
        return (Converter<F,T>) convertersByName.get(name);
    }

    public static <F,T> Converter<F,T> getConverterByName(final String...names) {
        if(names.length < 1) {
            throw new IllegalArgumentException("Need at least one Converter-Name");
        } else if(names.length == 1) {
            return (Converter<F,T>) converterByName(names[0].trim());
        } else {
            final ConverterChain<F, T> converterChain = new ConverterChain();
            for (int i = 0; i < names.length; i++) {
                Converter converter = converterByName(names[i].trim());
                if (null == converter)
                    return null;
                converterChain.add(converter);
            }
            return converterChain;
        }
    }


//    public static List<String> propertyKeys(Properties properties) {
//        List<String> keys = new Vector<String>();
//        for(Object k : properties.keySet()) {
//            if(k instanceof String) {
//                final String key = (String) k;
//                final int idx = key.indexOf(".");
//                final String kk = idx<1 ? key : key.substring(0, idx);
//                if(!keys.contains(kk))
//                    keys.add(kk);
//            }
//        }
//        return keys;
//    }
//
//    public static Properties shrink(Properties input, final String key) {
//        final Properties output = new Properties();
//        final String prefix = key+".";
//        for(final Object k : input.keySet()) {
//            if(k instanceof String) {
//                final String strKey = (String) k;
//                final int idx = strKey.indexOf(prefix);
//                if(0 == idx) {
//                    output.setProperty(strKey.substring(prefix.length()), input.getProperty(strKey));
//                }
//            }
//        }
//        return output;
//    }
//    public void pushSrc(SyncEntity entity) {
//
//    }
}
