package net.nasku.synco.converter;

import net.nasku.synco.converter.interf.ListConverterList;
import net.nasku.synco.model.SyncEntity;

import java.util.List;
import java.util.Vector;

/**
 * Created by Seven on 14.09.2016.
 */
public class SyncEntityToObjectListConverterList<T> implements ListConverterList<SyncEntity, T> {

    SyncEntityToObjectConverter syncEntityToObjectConverter;

    public SyncEntityToObjectListConverterList(SyncEntityToObjectConverter syncEntityToObjectConverter) {
        this.syncEntityToObjectConverter = syncEntityToObjectConverter;
    }

    @Override
    public List<T> convert(List<SyncEntity> entities) {
        final List data = new Vector<>();
        for(final SyncEntity entity : entities) {
            data.add(syncEntityToObjectConverter.convert(entity));
        }
        return data;
    }
}
