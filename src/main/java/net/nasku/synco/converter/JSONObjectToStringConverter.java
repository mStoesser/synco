package net.nasku.synco.converter;

import net.nasku.synco.converter.interf.Converter;

import org.json.JSONObject;

/**
 * Created by Seven on 26.08.2016.
 */
public class JSONObjectToStringConverter implements Converter<JSONObject, String> {
    @Override
    public String convert(JSONObject jsonObject) {
        return jsonObject.toString();
    }
}
