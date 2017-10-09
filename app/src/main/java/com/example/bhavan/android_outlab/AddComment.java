package com.example.bhavan.android_outlab;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class AddComment extends AppCompatActivity {
    SharedPreferences prefs;
    CookieManager msCookieManager = new java.net.CookieManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_comment);
        prefs = getSharedPreferences("MyPrefs",MODE_PRIVATE);
        final String cookies = prefs.getString("cookie","");
        if(cookies.equals("")){
            Intent intent = new Intent(this, MainActivity.class);
            this.startActivity(intent);
            this.finish();
        }

        Bundle bundle = getIntent().getExtras();
        final String postid = bundle.getString("postid");

        setContentView(R.layout.activity_add_comment);

        TextView addComment = (TextView)findViewById(R.id.addComment);

        addComment.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String content= ((EditText)findViewById(R.id.content)).getText().toString();
                Log.e("LOG","------------------"+content+"---");
                JSONObject postData = new JSONObject();
                try {
                    postData.put("postid", postid);
                    postData.put("content", content);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String data = null;
                try {
                    data = JsontoPost(postData);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                new connect().execute("http://"+MainActivity.IP+":"+MainActivity.port+"/app/NewComment",cookies,data);
            }
        });

    }

    private class connect extends AsyncTask<String, String,  String> {
        @Override
        protected String doInBackground(String... params) {
            String response=null;
            URL url= null;
            String COOKIES_HEADER = "Set-Cookie";
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
            conn.setRequestProperty("Cookie",params[1]);
            try {
                conn.connect();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                OutputStreamWriter writer= new OutputStreamWriter(conn.getOutputStream());
                Log.e("LOG", params[2]);
                writer.write(params[2]);
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
            Toast.makeText(AddComment.this, result, Toast.LENGTH_SHORT).show();
            Log.e("result...........",result);
            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(result);
                if(!jsonObject.getBoolean("status")){
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.clear();
                    editor.commit();

                }else{
                    AddComment.this.finish();
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
