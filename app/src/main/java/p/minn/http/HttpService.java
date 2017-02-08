package p.minn.http;

import android.os.StrictMode;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * @author minn
 * @QQ:3942986006
 */
public abstract class HttpService {

    private final static String HTTP_STR="http://192.168.1.104:8080/admin/";

    public HttpService(){
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads().detectDiskWrites().detectNetwork()
                .penaltyLog().build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects().detectLeakedClosableObjects()
                .penaltyLog().penaltyDeath().build());
    }

    public void get(String req) {
        JSONObject rsobj=null;
        try {
            HttpClient hc=new DefaultHttpClient();
            HttpGet hg=new HttpGet(req);
            HttpResponse response=hc.execute(hg);
            String rs=null;
            if (response.getStatusLine().getStatusCode() == 200){
                rs = EntityUtils.toString(response.getEntity());
                if(rs!=null){
                    rsobj=new JSONObject(rs);
                }
                onSuccess(rsobj);
            }else{
                JSONObject fail=new JSONObject();
                fail.put("status","0");
                fail.put("msg","fail");
                onFail(fail);
            }

        } catch (Exception e) {
            e.printStackTrace();
            try {
                JSONObject fail = new JSONObject();
                fail.put("status", "0");
                fail.put("msg", e.getMessage());
                onFail(fail);
            }catch (Exception ie){
                ie.printStackTrace();
            }
        }

    }

    public void post(String method,List <NameValuePair> params) {
        JSONObject rsobj=null;
        try {
            HttpPost httpRequest = new HttpPost(HTTP_STR+method);

            httpRequest.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
            HttpResponse httpResponse = new DefaultHttpClient().execute(httpRequest);
            String rs=null;
            if(httpResponse.getStatusLine().getStatusCode() == 200){
                rs = EntityUtils.toString(httpResponse.getEntity());
                if(rs!=null){
                    rsobj=new JSONObject(rs);
                }
                onSuccess(rsobj);
            }else{
                JSONObject fail=new JSONObject();
                fail.put("status","0");
                fail.put("msg","fail");
                onFail(fail);
            }
        } catch (Exception e) {
                e.printStackTrace();
            try {
                JSONObject fail = new JSONObject();
                fail.put("status", "0");
                fail.put("msg", e.getMessage());
                onFail(fail);
            }catch (Exception ie){
                 ie.printStackTrace();
            }
        }

    }


    protected abstract void onSuccess(JSONObject result);
    protected abstract void onCancelled();

    protected  void onFail(JSONObject fail){
        try {
            System.out.println("http fail********************:"+fail.getString("msg"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

    };

}
