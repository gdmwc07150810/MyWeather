package cn.edu.gdmec.a07150810.myweather;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class GetWeatherInfoTask extends AsyncTask<String ,Void,List<Map<String,Object>>>{
    private Activity context;
    private ProgressDialog progressDialog;
    private String errorMsg="网络错误";
    private ListView weather_info;
    private static String BASE_URL = "http://v.juhe.cn/weather/index?format=2&cityname=";
    private static String key="&key=2f5519af859fc627f22e243bfeedb7ad";
    public GetWeatherInfoTask(Activity context){
        this.context=context;
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("正在获取天气，请稍后......");
        progressDialog.setCancelable(false);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog.show();
    }

    @Override
    protected void onPostExecute(List<Map<String, Object>> maps) {
        super.onPostExecute(maps);
        progressDialog.dismiss();
        if(maps.size()>0){
            weather_info= (ListView) context.findViewById(R.id.weather_info);
            SimpleAdapter simpleAdapter = new SimpleAdapter(context,maps,
                    R.layout.weather_item,new String []{"temperature",
                    "weather","date","week","weather_icon"},new int[]{
                    R.id.temperature,R.id.wheather,R.id.date,R.id.week,
                    R.id.imageView});
            weather_info.setAdapter(simpleAdapter);
        }else{
            Toast.makeText(context,errorMsg,Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected List<Map<String, Object>> doInBackground(String... params) {
        List<Map<String,Object>> list = new ArrayList<Map<String, Object>>();
        try {
            HttpClient httpClient = new DefaultHttpClient();
            String url = BASE_URL+ URLEncoder.encode(params[0],"UTF-8")+key;
            HttpGet httpget = new HttpGet(url);
            HttpResponse response  = httpClient.execute(httpget);
            if(response.getStatusLine().getStatusCode()==200){
                //response.setEntity(new GzipDecompressingEntity(response.getEntity()));
                String jsonString  = EntityUtils.toString(response.getEntity(),"UTF-8");
                JSONObject jsondata = new JSONObject(jsonString);
                if(jsondata.getInt("resultcode")==200){
                    JSONObject result = jsondata.getJSONObject("result");
                    JSONArray weatherList = result.getJSONArray("future");
                    for(int i=0;i<7;i++){
                        Map<String,Object> item = new HashMap<String,Object>();
                        JSONObject wathObject = weatherList.getJSONObject(i);
                        item.put("temperature",wathObject.getString("temperature"));
                        item.put("weather",wathObject.getString("weather"));
                        item.put("date",wathObject.getString("date"));
                        item.put("week",wathObject.getString("week"));
                        item.put("wind",wathObject.getString("wind"));
                        JSONObject wid = wathObject.getJSONObject("weather_id");
                        int weather_icon =wid.getInt("fa");
                        item.put("weather_icon",WeathIcon.weather_icons[weather_icon]);
                        list.add(item);
                    }
                }else{
                    errorMsg="不支持的城市";
                }
            }else{
                errorMsg="请检查网络";
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return list;
    }
}