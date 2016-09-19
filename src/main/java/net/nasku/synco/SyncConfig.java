package net.nasku.synco;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Created by Seven on 25.08.2016.
 */
public class SyncConfig {

    Map<String,Object> values = new HashMap<String, Object>();

    public static SyncConfig loadConfigFromPropertiesResourceFile(final File file) {
        try (InputStream inputStream = new FileInputStream(file)) {
            return loadConfigFromPropertiesInputStream(inputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static SyncConfig loadConfigFromPropertiesResourceFile(final String fileName) {
        try (InputStream inputStream = SyncConfig.class.getResourceAsStream(fileName)) {
            return loadConfigFromPropertiesInputStream(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static SyncConfig loadConfigFromPropertiesInputStream(InputStream inputStream){
        final Properties properties = new Properties();
        try {
            properties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return loadConfig(properties);
    }

    public static SyncConfig loadConfig(final Properties properties) {
        SyncConfig syncConfig = new SyncConfig();
        for(String name : properties.stringPropertyNames()) {
            syncConfig.put(name.split("\\."), properties.getProperty(name));
        }
        return syncConfig;
    }

    public SyncConfig() {
        this.values = new HashMap<String, Object>();
    }

    private SyncConfig(Map<String,Object> values) {
        this.values = values;
    }

    private void put(String[] keys, Object value) {
        for(int i=keys.length-1;i>0;i--) {
            Map<String, Object> values = new HashMap<String, Object>();
            values.put(keys[i], value);
            value = values;
        }
        values.put(keys[0], value);
    }
    // Well not sure if put should be automatically add an recursive structure (like a.b=x a.c=y)
    public SyncConfig put(String key, Object value){
        values.put(key, value);
        return this;
    }

    public Set<String> keySet() {
        return values.keySet();
    }

    public String getString(final String key, final String defaultValue) {
        if(values.containsKey(key)) {
            Object obj = values.get(key);
            if(obj instanceof String) {
                return (String) obj;
            } else if(obj instanceof Number) {
                return ((Number)obj).toString();
            } else if(obj instanceof Boolean) {
                return obj.toString();
            }
        }
        return defaultValue;
    }
    public Float getFloat(final String key, final Float defaultValue) {
        if(values.containsKey(key)) {
            Object obj = values.get(key);
            if(obj instanceof Float) {
                return (Float) obj;
            } else if(obj instanceof Number) {
                return ((Number)obj).floatValue();
            } else if(obj instanceof String) {
                try {
                    return Float.parseFloat((String) obj);
                } catch (NumberFormatException e) {}
            }
        }
        return defaultValue;
    }
    public Double getDouble(final String key, final Double defaultValue) {
        if(values.containsKey(key)) {
            Object obj = values.get(key);
            if(obj instanceof Double) {
                return (Double) obj;
            } else if(obj instanceof Number) {
                return ((Number)obj).doubleValue();
            } else if(obj instanceof String) {
                try {
                    return Double.parseDouble((String) obj);
                } catch (NumberFormatException e) {}
            }
        }
        return defaultValue;
    }
    public Long getLong(final String key, final Long defaultValue) {
        if(values.containsKey(key)) {
            Object obj = values.get(key);
            if(obj instanceof Long) {
                return (Long) obj;
            } else if(obj instanceof Number) {
                return ((Number)obj).longValue();
            } else if(obj instanceof String) {
                try {
                    return Long.parseLong((String) obj);
                } catch (NumberFormatException e) {}
            }
        }
        return defaultValue;
    }
    public Integer getInteger(final String key, final Integer defaultValue) {
        if(values.containsKey(key)) {
            Object obj = values.get(key);
            if(obj instanceof Integer) {
                return (Integer) obj;
            } else if(obj instanceof Number) {
                return ((Number)obj).intValue();
            } else if(obj instanceof String) {
                try {
                    return Integer.parseInt((String) obj);
                } catch (NumberFormatException e) {}
            }
        }
        return defaultValue;
    }
    public Boolean getBoolean(final String key, final Boolean defaultValue) {
        if(values.containsKey(key)) {
            Object obj = values.get(key);
            if(obj instanceof Boolean) {
                return (Boolean) obj;
            } else if(obj instanceof Number) {
                return ((Number)obj).longValue() == 0;
            } else if(obj instanceof String) {
                final String str = ((String) obj).trim();
                if(str.equalsIgnoreCase("true"))
                    return true;
                else if(str.equalsIgnoreCase("false"))
                    return false;
                //for other strings we want the default value
            }
        }
        return defaultValue;
    }

    public Map<String,Object> getMap(final String key) {
        return getMap(key, new HashMap<String, Object>());
    }

    public Map<String,Object> getMap(final String key, final Map<String,Object> defaultValue) {
        if(values.containsKey(key)) {
            Object obj = values.get(key);
            if(obj instanceof Map) {
                return (Map<String,Object>) obj;
            }
        }
        return defaultValue;
    }

    public SyncConfig getSyncConfig(final String key) {
        final Map<String,Object> map = getMap(key);
        if(map.containsKey("parent")) {
            final Object obj = map.get("parent");
            if( obj instanceof String) {
                return new SyncConfig(merge(getMap((String)obj), map));
            }
        }

        return new SyncConfig(map);
    }

    //TODO: put this elsewhere
    public static Map<String,Object> merge(Map<String,Object> a, Map<String,Object> b) {
        final Map<String,Object> out = new HashMap<>(a);
        for(final String key : b.keySet()) {
            final Object valueB = b.get(key);
            if(a.containsKey(key)) {
                final Object valueA = a.get(key);
                if (valueA instanceof Map && valueB instanceof Map) {
                    out.put(key, merge((Map) valueA, (Map) valueB));
                    continue;
                }
            }
            out.put(key, valueB);
        }
        return out;
    }
}
