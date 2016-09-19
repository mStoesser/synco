package net.nasku.synco.converter;


import net.nasku.synco.Sync;
import net.nasku.synco.converter.interf.Converter;
import net.nasku.synco.converter.interf.ConverterList;
import net.nasku.synco.model.SyncEntity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Vector;

/**
 * Created by Seven on 25.08.2016.
 */
public class JSONArrayToSyncEntityConverterList implements ConverterList<JSONArray, SyncEntity> {

    protected Converter<JSONObject, SyncEntity> jsonObjectToSyncEntityConverter;

    public JSONArrayToSyncEntityConverterList() {
        this.jsonObjectToSyncEntityConverter = Sync.getConverterByType(JSONObject.class, SyncEntity.class);
    }

    @Override
    public List<SyncEntity> convert(JSONArray jsonArray) {
        final List<SyncEntity> list = new Vector<SyncEntity>();
        for(int i=0;i<jsonArray.length();i++) {
            try {
                list.add(jsonObjectToSyncEntityConverter.convert(jsonArray.getJSONObject(i)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return list;
    }
}
