package net.nasku.synco.converter;

import net.nasku.synco.converter.interf.Converter;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Seven on 25.08.2016.
 */
public class StringToJSONObjectConverter implements Converter<String, JSONObject> {
    @Override
    public JSONObject convert(String s) {
        try {
            return new JSONObject(s);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
