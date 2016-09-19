package net.nasku.synco.converter;

import net.nasku.synco.converter.interf.Converter;
import net.nasku.synco.model.SyncEntity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Seven on 23.08.2016.
 */
public class SyncEntityToJSONObjectConverter implements Converter<SyncEntity,JSONObject> {

    @Override
    public JSONObject convert(final SyncEntity entity) {
        final JSONObject jsonObject = new JSONObject();
        final Set<String> keys = entity.keySet();
        for (final String key : keys) {
            try {
                jsonObject.put(key, apply(entity.get(key)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return jsonObject;
    }

    private Object apply(final Object value) throws JSONException {
        if(value instanceof Map) {
            final Map<String, Object> map = (Map) value;
            final JSONObject obj = new JSONObject();
            for (final String key : map.keySet())
                obj.put(key, apply(map.get(key)));
            return obj;
        } else if(value instanceof List) {
            final List<Object> list = (List) value;
            final JSONArray jsonArray = new JSONArray();
            for(int i=0;i<list.size();i++)
                jsonArray.put(apply(list.get(i)));
            return jsonArray;
        } else if (null != value) {
            return value;
        } else {
            return JSONObject.NULL;
        }
    }
}
