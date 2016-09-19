package net.nasku.synco.converter;

import net.nasku.synco.converter.interf.Converter;
import net.nasku.synco.model.HttpPostVar;

/**
 * Created by Seven on 13.09.2016.
 */
public class HttpPostVarToStringConverter implements Converter<HttpPostVar, String> {
    @Override
    public String convert(final HttpPostVar httpPostVar) {
        return null==httpPostVar?null:httpPostVar.toString();
    }
}
