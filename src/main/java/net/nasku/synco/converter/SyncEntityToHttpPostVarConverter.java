package net.nasku.synco.converter;


import net.nasku.synco.converter.interf.Converter;
import net.nasku.synco.model.HttpPostVar;
import net.nasku.synco.model.SyncEntity;

import java.util.Set;

/**
 * Created by Seven on 23.08.2016.
 */
public class SyncEntityToHttpPostVarConverter implements Converter<SyncEntity, HttpPostVar> {

    @Override
    public HttpPostVar convert(final SyncEntity entity) {
        final HttpPostVar httpPostVar = new HttpPostVar();
        final Set<String> keys = entity.keySet();
        for (final String key : keys) {
            httpPostVar.put(key, entity.get(key));
        }
        return httpPostVar;
    }

//    @Override
//    public String toString(final List<SyncEntity> entities) {
//        final StringBuilder queryStr = new StringBuilder();
//        try {
//            for(SyncEntity entity : entities) {
//                final Set<String> keys = entity.keySet();
//                int i=0;
//                for (final String key : keys) {
//                    append(queryStr, key + "[" + i++ + "]", entity.get(key));
//                }
//            }
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//        return queryStr.toString();
//    }


}
