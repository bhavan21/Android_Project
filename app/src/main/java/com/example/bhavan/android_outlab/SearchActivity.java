package com.example.bhavan.android_outlab;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
import java.util.StringTokenizer;

import static android.R.attr.data;
import static android.R.attr.theme;

public class SearchActivity extends AppCompatActivity {

    SharedPreferences prefs;
    ArrayList<String> data = new ArrayList<>();
    ArrayAdapter<String> adapter;

    String uid;
    String name;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_serach);
        prefs = getSharedPreferences("MyPrefs",MODE_PRIVATE);
        final String cookies = prefs.getString("cookie","");

        uid=null;
        name=null;
//        data.add("Belgium");

        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, data);
        AutoCompleteTextView textView = (AutoCompleteTextView)
                findViewById(R.id.autocomplete);
        textView.setAdapter(adapter);

        String test = "[[{\"uid\":\"00128\",\"name\":\"Zhang\",\"email\":\"user1@gmail.com\"}]]";
        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(test);
            JSONArray jsonArray1=  jsonArray.getJSONArray(0);
            for(int i=0;i<jsonArray1.length();i++){
                JSONObject jsonObject = jsonArray1.getJSONObject(i);
                Log.e("&&&&&&&&&",jsonObject.getString("uid")+"&"+jsonObject.getString("name")+"&"+jsonObject.getString("email"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


        textView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick (AdapterView<?> parent, View view, int position, long id) {
                Object item = parent.getItemAtPosition(position);
                if (item instanceof String){
                    String s = (String) item;
                    Toast.makeText(SearchActivity.this,s,Toast.LENGTH_LONG).show();
                    uid = s.split("\n")[0];
                    name= s.split("\n")[1];

                    AlertDialog alertDialog = new AlertDialog.Builder(SearchActivity.this).create();

                    alertDialog.setTitle(uid);

                    alertDialog.setMessage(name);

                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "See Posts", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int id) {

                            //...

                        } });


                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Cancel", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int id) {

                            //...

                        }});

                    alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Follow User", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int id) {

                            //...

                        }});

                    alertDialog.show();
                }
            }
        });


        textView.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                JSONObject postData = new JSONObject();
                try {
                    postData.put("searchstring", s.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String data = null;
                try {
                    data = JsontoPost(postData);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                new searchServer().execute("http://"+MainActivity.IP+":"+MainActivity.port+"/"+MainActivity.projec_name+"/SearchUser",cookies,data);

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });





    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            // action with ID action_refresh was selected
            case R.id.action_home:

                if(this.getClass().getName() != HomeActivity.class.getClass().getName()){
                    intent = new Intent(this, HomeActivity.class);
                    this.startActivity(intent);
                }


                break;
            case R.id.action_search:
                if(this.getClass().getName() != SearchActivity.class.getClass().getName()){
                    intent = new Intent(this, SearchActivity.class);
                    this.startActivity(intent);
                }

                break;
            case R.id.action_logout:
                SharedPreferences.Editor editor= prefs.edit();
                editor.clear();
                editor.commit();
                intent = new Intent(this, MainActivity.class);
                this.startActivity(intent);
                break;
            // action with ID action_settings was selected
            default:
                break;
        }

        return true;
    }






    private static final String[] COUNTRIES = new String[] {
            "Belgium", "France", "Italy", "Germany", "Spain"
    };

    private class searchServer extends AsyncTask<String, String,  String> {
        @Override
        protected String doInBackground(String... params) {
            String response=null;
            URL url= null;
            String COOKIES_HEADER = "Set-Cookie";
//            CookieManager msCookieManager = new java.net.CookieManager();
            try {
                Log.e("^^^^^^^^^^^^^^^^^",params[0]);
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

//            if (cookiesHeader != null) {
//                for (String cookie : cookiesHeader) {
//                    Log.e("Cookie", cookie);
//
//                    msCookieManager.getCookieStore().add(null, HttpCookie.parse(cookie).get(0));
//                }
//            }

            return response;
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(SearchActivity.this, result, Toast.LENGTH_SHORT).show();
//            Log.e("result...........",result);
            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(result);

                Log.e(""+jsonObject.getJSONArray("data").length(),jsonObject.getJSONArray("data").toString()+"-------------------------------");

                if(jsonObject.getJSONArray("data").length()!=0){
                    JSONArray jsonArray = jsonObject.getJSONArray("data").getJSONArray(0);
                    adapter.clear();
                    for (int i=0;i<jsonArray.length();i++){
                        JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                        adapter.add(jsonObject1.getString("uid")+"\n"+jsonObject1.getString("name")+"\n"+jsonObject1.getString("email"));
//                        Log.e("**********",jsonArray.getJSONObject(i).toString());
                    }

                    adapter.notifyDataSetChanged();
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
