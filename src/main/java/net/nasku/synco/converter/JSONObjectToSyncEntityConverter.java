package net.nasku.synco.converter;


import net.nasku.synco.converter.interf.Converter;
import net.nasku.synco.model.SyncEntity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Seven on 24.08.2016.
 */
public class JSONObjectToSyncEntityConverter implements Converter<JSONObject, SyncEntity> {
    @Override
    public SyncEntity convert(JSONObject jsonObject) {
        SyncEntity syncEntity = new SyncEntity();
        JSONArray keys = jsonObject.names();
        for(int i=0;i<keys.length();i++) {
            try {
                String key = keys.getString(i);
                if (jsonObject.isNull(key)) {
                    syncEntity.put(key, null);
                } else {
                    syncEntity.put(key, jsonObject.get(key));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return syncEntity;
    }
}
