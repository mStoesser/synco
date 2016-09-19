package net.nasku.synco.converter;

import net.nasku.synco.converter.interf.Converter;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by Seven on 26.08.2016.
 */
public class StringToJSONArrayConverter implements Converter<String, JSONArray> {

    @Override
    public JSONArray convert(String s) {
        try {
            return new JSONArray(s);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
