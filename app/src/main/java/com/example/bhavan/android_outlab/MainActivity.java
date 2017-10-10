package com.example.bhavan.android_outlab;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.StringBuilderPrinter;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    public static final String IP = "192.168.31.240";
    public static final String port = "8080";
    public static final String projec_name = "app";

    String COOKIES_HEADER = "Set-Cookie";
    CookieManager msCookieManager = new java.net.CookieManager();
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        editor  = getSharedPreferences("MyPrefs", MODE_PRIVATE).edit();

        setContentView(R.layout.activity_main);


        Log.e("Login starting","fllfdldllf............................."+TextUtils.join(";", msCookieManager.getCookieStore().getCookies()));

        Button button= (Button)findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String username= ((EditText)findViewById(R.id.username)).getText().toString();
                String pass= ((EditText)findViewById(R.id.pass)).getText().toString();
                Log.e("LOG","------------------"+username+"---"+pass);
                JSONObject postData = new JSONObject();
                try {
                    postData.put("id", username);
                    postData.put("password",pass);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String data = null;
                try {
                    data = JsontoPost(postData);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                new connect().execute("http://"+IP+":"+port+"/"+projec_name+"/Login",data );
            }
        });
    }

    private class connect extends AsyncTask<String, String,  String> {
        @Override
        protected String doInBackground(String... params) {
            String response=null;
            URL url= null;

            try {
                url = new URL(params[0]);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            HttpURLConnection conn = null;
            try {
                conn = (HttpURLConnection)url.openConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }
            conn.setDoInput(true);
            conn.setDoOutput(true);
            try {
                conn.setRequestMethod("POST");
            } catch (ProtocolException e) {
                e.printStackTrace();
            }
            if (msCookieManager.getCookieStore().getCookies().size() > 0) {
                // While joining the Cookies, use ',' or ';' as needed. Most of the servers are using ';'
                conn.setRequestProperty("Cookie",
                        TextUtils.join(";", msCookieManager.getCookieStore().getCookies()));
            }
            try {
                conn.connect();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
            OutputStreamWriter writer= new OutputStreamWriter(conn.getOutputStream());
            Log.e("LOG", params[1]);
            writer.write(params[1]);
            writer.flush();
            writer.close();

            } catch (IOException e) {
                e.printStackTrace();
            }

            InputStream inputStream = null;
            try {
                inputStream = new BufferedInputStream(conn.getInputStream());

            BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder total = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) {
                total.append(line).append('\n');
            }
                response = total.toString();
            } catch (IOException e) {
                e.printStackTrace();
            }



            Map<String, List<String>> headerFields = conn.getHeaderFields();
            List<String> cookiesHeader = headerFields.get(COOKIES_HEADER);

            if (cookiesHeader != null) {
                for (String cookie : cookiesHeader) {
                    Log.e("Cookie", cookie);
                    msCookieManager.getCookieStore().add(null, HttpCookie.parse(cookie).get(0));
                }
            }

            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(MainActivity.this, result, Toast.LENGTH_SHORT).show();
            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(result);
                if(jsonObject.getBoolean("status")){
                    if(msCookieManager.getCookieStore().getCookies().size() > 0){
                        Log.e("Log",msCookieManager.toString());
                        editor.putString("cookie",TextUtils.join(";",msCookieManager.getCookieStore().getCookies()));
                        editor.apply();
                    }
                    Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                    MainActivity.this.startActivity(intent);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    public  String JsontoPost(JSONObject jsonObject) throws Exception{
        StringBuilder result = new StringBuilder();
        Boolean first =true;
        Iterator<String> itr = jsonObject.keys();
        while(itr.hasNext()){
            String key = itr.next();
            Object value = jsonObject.get(key);
            if(first){
                first=false;
            }else {
                result.append("&");
            }

            result.append(URLEncoder.encode(key,"UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(value.toString(),"UTF-8"));
        }
        return  result.toString();
    }

}
