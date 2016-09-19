package net.nasku.synco.converter;

import net.nasku.synco.converter.interf.Converter;

import org.json.JSONArray;

/**
 * Created by Seven on 26.08.2016.
 */
public class JSONArrayToStringConverter implements Converter<JSONArray, String> {
    @Override
    public String convert(JSONArray jsonArray) {
        return jsonArray.toString();
    }
}
