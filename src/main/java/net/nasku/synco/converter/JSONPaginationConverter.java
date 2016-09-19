package net.nasku.synco.converter;

import net.nasku.synco.SyncConfig;
import net.nasku.synco.converter.interf.Converter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Properties;

/**
 * Created by Seven on 25.08.2016.
 */
public class JSONPaginationConverter implements Converter<JSONObject, JSONArray> {

    String dataKey;

    public JSONPaginationConverter(SyncConfig config) {
        this.dataKey = config.getString("dataKey", "_embedded");
    }

    @Override
    public JSONArray convert(JSONObject jsonObject) {
        try {
            return jsonObject.getJSONArray(dataKey);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
