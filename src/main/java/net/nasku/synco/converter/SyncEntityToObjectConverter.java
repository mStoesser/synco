package net.nasku.synco.converter;

import net.nasku.synco.SyncConfig;
import net.nasku.synco.converter.interf.Converter;
import net.nasku.synco.model.SyncEntity;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Created by Seven on 07.09.2016.
 */
public class SyncEntityToObjectConverter<T> implements Converter<SyncEntity, T> {

    private Class<T> clazz;
    private String setterPrefix;

    public SyncEntityToObjectConverter(Class<T> clazz) {
        this.clazz = clazz;
    }

    public SyncEntityToObjectConverter(final SyncConfig config) {
        try {
            this.clazz = (Class<T>) Class.forName(config.getString("clazz", "java.lang.Object"));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        this.setterPrefix = config.getString("setterPrefix", "set");
    }

    @Override
    public T convert(final SyncEntity syncEntity) {

        Object obj = null;
        try {
            obj = clazz.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        if(null != obj) {
            for (final String key : syncEntity.keySet()) {

                try {
                    final Field field = clazz.getField(key);
                    final int modifiers = field.getModifiers();
                    if (field.isAccessible() && Modifier.isPublic(modifiers) && !Modifier.isTransient(modifiers)) {
                        try {
                            field.set(obj, syncEntity.get(field.getName()));
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                }

                String methodName = setterPrefix+key.substring(0,1).toUpperCase()+key.substring(1);
                try {
                    final Method method = clazz.getMethod(methodName);
                    if(method.isAccessible() && Modifier.isPublic(method.getModifiers()) && method.getParameterTypes().length == 1 && method.getName().startsWith(setterPrefix)) {
                        try {
                            method.invoke(obj, syncEntity.get(key));
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }

            }
        }
        return (T) obj;
    }
}
