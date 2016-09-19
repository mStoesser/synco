package net.nasku.synco.converter;

import net.nasku.synco.Sync;
import net.nasku.synco.converter.interf.Converter;
import net.nasku.synco.converter.interf.ListConverterList;
import net.nasku.synco.model.SyncEntity;

import java.util.List;
import java.util.Vector;

/**
 * Created by Seven on 09.09.2016.
 */
public class ObjectToSyncEntityListConverterList implements ListConverterList<Object,SyncEntity> {

    private Converter<Object,SyncEntity> objectToSyncEntityConverter;

    public ObjectToSyncEntityListConverterList() {
        this.objectToSyncEntityConverter = Sync.getConverterByType(Object.class, SyncEntity.class);
    }

    @Override
    public List<SyncEntity> convert(List<Object> objects) {
        final List<SyncEntity> syncEntities = new Vector<>();
        for(Object obj : objects) {
            syncEntities.add(objectToSyncEntityConverter.convert(obj));
        }
        return syncEntities;
    }
}
