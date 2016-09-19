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
public class ObjectToSyncEntityConverter implements Converter<Object,SyncEntity> {

    private String getterPrefix;

    private String[] blackList;
    private String[] whiteList;

    public ObjectToSyncEntityConverter() {
        this.getterPrefix = "get";
    }
    public ObjectToSyncEntityConverter(SyncConfig config) {
        this.getterPrefix = config.getString("getterPrefix", "get");
    }

    @Override
    public SyncEntity convert(Object obj) {

        final SyncEntity syncEntity = new SyncEntity();

        final Class<?> clazz = obj.getClass();

        final Field[] fields = clazz.getFields(); //c.getDeclaredFields();

        for(final Field field : fields) {
            final int modifiers = field.getModifiers();
            if(Modifier.isPublic(modifiers) && !Modifier.isTransient(modifiers) && !Modifier.isStatic(modifiers)) {
                try {
                    syncEntity.put(field.getName(), field.get(obj));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        final Method[] methods = clazz.getMethods();
        for(final Method method : methods) {
            final int modifiers = method.getModifiers();
            if(!Modifier.isStatic(modifiers) && !Modifier.isFinal(modifiers) && Modifier.isPublic(modifiers) && !method.getReturnType().equals(Void.TYPE) && method.getParameterTypes().length == 0 && method.getName().startsWith(getterPrefix)) {
                try {
                    syncEntity.put(method.getName().substring(getterPrefix.length()),method.invoke(obj));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }

        return syncEntity;
    }
}
