package net.nasku.synco.model;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Seven on 24.08.2016.
 */
public class HttpPostVar {

    private static final String CHARSET = "UTF-8";

    private Map<String,Object> values = new HashMap<String, Object>();

    public void put(final String key, final Object value) {
        values.put(key, value);
    }

    @Override
    public String toString() {
        return toString(CHARSET);
    }

    public String toString(final String charset) {
        final StringBuilder queryStr = new StringBuilder();
        try {
            final Set<String> keys = values.keySet();
            for (final String key : keys) {
                appendQueryString(queryStr, key, values.get(key), charset);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return queryStr.toString();
    }


    private void appendQueryString(StringBuilder queryStr, String prefix, Object value, String charset) throws UnsupportedEncodingException {

        if(value instanceof Map) {
            Map<String, Object> map = (Map) value;
            for (final String mapKey : map.keySet()) {
                appendQueryString(queryStr, prefix + "[" + mapKey + "]", map.get(mapKey), charset);
            }
        } else if(value instanceof List) {
            List<Object> list = (List) value;
            for(int i=0;i<list.size();i++) {
                appendQueryString(queryStr, prefix + "[" + i + "]", list.get(i), charset);
            }
        } else if (null != value) {
            final String strValue = value.toString();
            if (null != strValue) {
                queryStr.append(prefix);
                queryStr.append("=");
                queryStr.append(URLEncoder.encode(strValue, charset));
                queryStr.append("&");
            }
        }
    }
}
