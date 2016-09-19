package net.nasku.synco.endpoint;



import net.nasku.synco.OverwriteMergeStrategy;
import net.nasku.synco.Sync;
import net.nasku.synco.SyncConfig;
import net.nasku.synco.converter.JSONArrayToSyncEntityConverterList;
import net.nasku.synco.converter.StringToJSONObjectConverter;
import net.nasku.synco.converter.interf.ConverterList;
import net.nasku.synco.endpoint.interf.Endpoint;
import net.nasku.synco.interf.MergeStrategy;
import net.nasku.synco.model.HttpPostVar;
import net.nasku.synco.model.SyncEntity;
import net.nasku.synco.converter.interf.Converter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Seven on 23.08.2016.
 */
public class HttpServerEndpoint implements Endpoint {

    public static final String POST = "POST";
    public static final String GET = "GET";
    private static final String CHARSET = "UTF-8";

    private static final String CONTENT_TYPE_TEXT_HTML = "text/html";
    private static final String CONTENT_TYPE_TEXT_CSV = "text/csv";
    private static final String CONTENT_TYPE_APPLICATION_JSON = "application/json";
    private static final String CONTENT_TYPE_APPLICATION_X_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded";


    String pushMethod;
    String pullMethod;
    String pushUrl;
    String pullUrl;

    MergeStrategy pushResponseMergeStrategy;

    String pushContentType;

    boolean pushSeparate;

    Map<String,Object> pushHeaders;
    Map<String,Object> pullHeaders;

    Converter<String, List<SyncEntity>> pullConverter;
    Converter<List<SyncEntity>, String> pushConverter;
    Converter<SyncEntity, String> pushSeperateConverter;
    Converter<String, List<SyncEntity>> pushResponseConverter;
    Converter<String, SyncEntity> pushSeperateResponseConverter;

    public HttpServerEndpoint(SyncConfig config) {

        pullMethod = config.getString("pullMethod", GET);
        pullUrl = config.getString("pullUrl", config.getString("url", null));
        pullConverter = Sync.getConverterByName(config.getString("pullConverter", "").split(","));
        pullHeaders = config.getMap("pullHeaders");

        pushMethod = config.getString("pushMethod", POST);
        pushUrl = config.getString("pushUrl", config.getString("url", null));
        pushContentType = config.getString("pushContentType", CONTENT_TYPE_APPLICATION_X_WWW_FORM_URLENCODED); ;
        pushSeparate = config.getBoolean("pushSeparate", false);
        if(pushSeparate) {
            pushSeperateConverter = Sync.getConverterByName(config.getString("pushConverter", "").split(","));
            pushSeperateResponseConverter = Sync.getConverterByName(config.getString("pushResponseConverter", "").split(","));
        } else {
            pushConverter = Sync.getConverterByName(config.getString("pushConverter", "").split(","));
            pushResponseConverter = Sync.getConverterByName(config.getString("pushResponseConverter", "").split(","));
        }
        pushHeaders = config.getMap("pushHeaders");
        pushResponseMergeStrategy = (MergeStrategy) Sync.load(config.getString("pushResponseMergeStrategy", OverwriteMergeStrategy.class.getName()), config);
    }

    @Override
    public void pull(List<SyncEntity> entities, int limit) {


        Map<String,List<String>> responseHeaders = new HashMap<String, List<String>>();

        String response = null;
        try {
            response = doRequest(pullMethod, pullUrl, null, pullHeaders, responseHeaders);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        String contentType = getContentType(responseHeaders);

        if(null != pullConverter) {

            entities.addAll(pullConverter.convert(response));

            //else contentType based
        } else if(CONTENT_TYPE_APPLICATION_JSON.equals(contentType)) {

            if (response.startsWith("{")) { //we expected a JSONArray but get an JSONObject so a pagination/meta-object is used..

                Converter<JSONArray, List<SyncEntity>> jsonArrayToSyncEntityConverter = Sync.getConverterListByType(JSONArray.class, SyncEntity.class);
                Converter<String, JSONObject> stringToJSONObjectConverter = Sync.getConverterByType(String.class, JSONObject.class);
                if(null != jsonArrayToSyncEntityConverter && null != stringToJSONObjectConverter) {
                    JSONObject jsonObject = stringToJSONObjectConverter.convert(response);
                    String[] keys = JSONObject.getNames(jsonObject);
                    for (final String key : keys) {
                        Object obj = jsonObject.opt(key);
                        if (obj instanceof JSONArray) {
                            entities.addAll(jsonArrayToSyncEntityConverter.convert((JSONArray) obj));
                            break;
                        }
                    }
                }
            } else {
                Converter<String, List<SyncEntity>> converter = Sync.<String, SyncEntity>getConverterListByType(String.class, JSONArray.class, SyncEntity.class);
                if (null != converter)
                    entities.addAll(converter.convert(response));
            }
        }
    }

    private String getContentType(Map<String,List<String>> responseHeaders) {
        String contentType = CONTENT_TYPE_TEXT_HTML;
        if(responseHeaders.containsKey("content-type")) { // Content-Type:text/html   , application/json, application/xhtml+xml,application/xml, text/csv
            List<String> fields = responseHeaders.get("content-type");
            if(null != fields && !fields.isEmpty()) {
                contentType = "";
                for (String field : fields)
                    contentType += field;
            }
        }
        return contentType;
    }

    @Override
    public void push(List<SyncEntity> entities) {

        Map<String,List<String>> responseHeaders = new HashMap<String, List<String>>();

        if(CONTENT_TYPE_APPLICATION_X_WWW_FORM_URLENCODED.equals(pushContentType)) {

            if(!pushSeparate) {

                Converter<List<SyncEntity>,String> converter = null != pushConverter ? pushConverter : Sync.<SyncEntity,String>getListConverterByType(SyncEntity.class, HttpPostVar.class, String.class);

                if(null != converter) {
                    String response = null;
                    try {
                        response = doRequest(POST, pushUrl, converter.convert(entities), pushHeaders, responseHeaders);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }
                    proccessResponse(response, getContentType(responseHeaders), entities);

                    return;
                } // else fallback to single-calls
            }

            Converter<SyncEntity,String> converter = null != pushSeperateConverter ? pushSeperateConverter : Sync.<SyncEntity,String>getConverterByType(SyncEntity.class, HttpPostVar.class, String.class);
            if(null != converter) {
                for (final SyncEntity entity : entities) {
                    String response = null;
                    try {
                        response = doRequest(pushMethod, pushUrl, converter.convert(entity), pushHeaders, responseHeaders);
                    } catch (Exception e) {
                        e.printStackTrace();
                        continue;
                    }

                    proccessResponse(response, getContentType(responseHeaders), entity);
                }
            }
        }
    }

    private void proccessResponse(String response, String contentType, List<SyncEntity> entities) {
        if(null == pushResponseMergeStrategy)
            return;

        if(CONTENT_TYPE_APPLICATION_JSON.equals(contentType)) {
            Converter<String,List<SyncEntity>> responseConverter = null != pushResponseConverter ? pushResponseConverter : Sync.<String,SyncEntity>getConverterListByType(String.class, JSONArray.class, SyncEntity.class);
            if(null != responseConverter) {
                List<SyncEntity> responseEntities = responseConverter.convert(response);
                for (int i = 0; i < entities.size(); i++) {
                    pushResponseMergeStrategy.merge(entities.get(i), responseEntities.get(i));
                }
            }
        }
    }

    private void proccessResponse(String response, String contentType, SyncEntity entity) {
        if(null == pushResponseMergeStrategy)
            return;

        if(CONTENT_TYPE_APPLICATION_JSON.equals(contentType)) {
            Converter<String,SyncEntity> responseConverter = null != pushSeperateResponseConverter ? pushSeperateResponseConverter : Sync.<String,SyncEntity>getConverterByType(String.class, JSONObject.class, SyncEntity.class);
            if(null != responseConverter) {
                pushResponseMergeStrategy.merge(entity, responseConverter.convert(response));
            }
        }
    }

    private static String doRequest(final String method, String url, final String postStr, final Map<String,Object> headers, final Map<String,List<String>> responseHeaders) throws IOException, IllegalAccessException {

        if(GET.equals(method)) {
            if(null != postStr && postStr.length() > 0) {
                url += (url.contains("?")?"&":"?")+postStr;
            }
        }

        HttpURLConnection urlConnection = (HttpURLConnection) new URL(url).openConnection();

        if(POST.equals(method)) {
            headers.put("Content-Type", "application/x-www-form-urlencoded;charset=" + CHARSET);
        }

        headers.put("Accept-Charset", CHARSET);
        for(Map.Entry<String,Object> header : headers.entrySet()) {
            urlConnection.addRequestProperty(header.getKey(), header.getValue().toString());
        }

        try {
            if(POST.equals(method)) {
                urlConnection.setDoOutput(true);
                OutputStream output = urlConnection.getOutputStream();
                output.write(postStr.getBytes(CHARSET));
            }

            System.out.println("HTTP: "+method+" "+url+" data:"+postStr);


            int responseCode = urlConnection.getResponseCode();
            if(responseCode != HttpURLConnection.HTTP_OK) {
                if(responseCode == 401)
                    throw new IllegalAccessException("Unauthorized");
                else
                    throw new IllegalStateException("register gcmToken failed responseCode:"+responseCode);
            }
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            final String ret = streamToString(in, CHARSET);
            System.out.println("HTTP response:" + ret);

            if(null != responseHeaders) {
                Map<String, List<String>> respHead = urlConnection.getHeaderFields();
                for (final String key : respHead.keySet()) {
                    responseHeaders.put(key, respHead.get(key));
                }
            }

            return ret;

        } finally{
            urlConnection.disconnect();
        }
    }

    public static String streamToString(InputStream in, String charset){
        BufferedReader br= new BufferedReader(new InputStreamReader(in));
        StringBuffer ret= new StringBuffer();
        String line;
        try {
            while((line = br.readLine()) != null)
                ret.append(line);
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret.toString();
    }
}
