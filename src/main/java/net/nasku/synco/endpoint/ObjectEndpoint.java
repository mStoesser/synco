package net.nasku.synco.endpoint;


import net.nasku.synco.OverwriteMergeStrategy;
import net.nasku.synco.converter.ObjectToSyncEntityConverter;
import net.nasku.synco.converter.SyncEntityToObjectConverter;
import net.nasku.synco.converter.SyncEntityToObjectListConverterList;
import net.nasku.synco.endpoint.interf.Endpoint;
import net.nasku.synco.interf.MergeStrategy;
import net.nasku.synco.Sync;
import net.nasku.synco.SyncConfig;
import net.nasku.synco.model.SyncEntity;
import net.nasku.synco.converter.interf.Converter;

import java.util.List;

/**
 * Created by Seven on 23.08.2016.
 */
public class ObjectEndpoint<E> implements Endpoint {


    interface LoadObjectsCallback<F> {
        List<F> loadObjects(int limit);
    }

    interface SaveObjectsCallback<F> {
        void saveObjects(List<F> objects);
    }

    LoadObjectsCallback<E> loadObjectsCallback;
    SaveObjectsCallback<E> saveObjectsCallback;

    Converter<List<E>, List<SyncEntity>> pullConverter;
    Converter<List<SyncEntity>, List<E>> pushConverter;
    Converter<List<E>, List<SyncEntity>> pushResponseConverter;

    MergeStrategy pushResponseMergeStrategy;

    public ObjectEndpoint(SyncConfig config) throws ClassNotFoundException {
        Class<?> typeClass = Class.forName(config.getString("type", ""));
        pullConverter = Sync.<E,SyncEntity>getListConverterListByType(typeClass, SyncEntity.class);
        pushConverter = Sync.<SyncEntity,E>getListConverterListByType(SyncEntity.class, typeClass);
        pushResponseConverter = Sync.<E,SyncEntity>getListConverterListByType(typeClass, SyncEntity.class);
        pushResponseMergeStrategy = (MergeStrategy) Sync.load(config.getString("pushResponseMergeStrategy", OverwriteMergeStrategy.class.getName()), config);
    }

    public ObjectEndpoint(final List<E> data) {
        loadObjectsCallback = new LoadObjectsCallback<E>() {
            @Override
            public List<E> loadObjects(int limit) {
                return data;
            }
        };
        pullConverter = Sync.getListConverterListByType(Object.class, SyncEntity.class);
        if(!data.isEmpty()) {
            pushConverter = Sync.getListConverterListByType(SyncEntity.class, data.get(0).getClass());
            if(null == pushConverter)
                pushConverter = new SyncEntityToObjectListConverterList(new SyncEntityToObjectConverter(data.get(0).getClass()));
        }
        pushResponseConverter = Sync.getListConverterListByType(Object.class, SyncEntity.class);
    }

    @Override
    public void pull(List<SyncEntity> entities, int limit) {

        List<E> objects = loadObjectsCallback.loadObjects(limit);

        if(!objects.isEmpty()) {

            if(null == pushConverter) { // some little magic
                pushConverter = Sync.getListConverterListByType(SyncEntity.class, objects.get(0).getClass());
                if(null == pushConverter)
                    pushConverter = new SyncEntityToObjectListConverterList(new SyncEntityToObjectConverter(objects.get(0).getClass()));
            }

            entities.addAll(pullConverter.convert(objects));
        }

    }

    @Override
    public void push(List<SyncEntity> entities) {

        if(null != pullConverter) {

            final List<E> objects = pushConverter.convert(entities);

            if(null != saveObjectsCallback) {

                saveObjectsCallback.saveObjects(objects);

                if (null != pushResponseMergeStrategy && null != pushResponseConverter) {
                    List<SyncEntity> responseEntities = pushResponseConverter.convert(objects);
                    for (int i = 0; i < entities.size(); i++) {
                        pushResponseMergeStrategy.merge(entities.get(i), responseEntities.get(i));
                    }
                }
            }
        }
    }

    public ObjectEndpoint<E> setLoadObjectsCallback(LoadObjectsCallback<E> loadObjectsCallback) {
        this.loadObjectsCallback = loadObjectsCallback;
        return this;
    }

    public ObjectEndpoint<E> setSaveObjectsCallback(SaveObjectsCallback<E> saveObjectsCallback) {
        this.saveObjectsCallback = saveObjectsCallback;
        return this;
    }
}
