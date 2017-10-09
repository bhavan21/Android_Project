package com.example.bhavan.android_outlab;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
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

public class HomeActivity extends AppCompatActivity {
    SharedPreferences prefs;
    CookieManager msCookieManager = new java.net.CookieManager();
    ArrayAdapter<JSONObject> adapter;
    ArrayList<JSONObject> data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String COOKIES_HEADER = "Set-Cookie";
        prefs = getSharedPreferences("MyPrefs",MODE_PRIVATE);
        String cookies = prefs.getString("cookie","");
        if(cookies.equals("")){
            Intent intent = new Intent(this, MainActivity.class);
            this.startActivity(intent);
            this.finish();
        }


        setContentView(R.layout.activity_home);

         data = new ArrayList();



        adapter = new UsersAdapter(HomeActivity.this, data);
        // Attach the adapter to a ListView
        ListView listView = (ListView)findViewById(R.id.posts);
        listView.setAdapter(adapter);


        new connect().execute("http://"+MainActivity.IP+":"+MainActivity.port+"/app/SeePosts",cookies);




    }


    public class UsersAdapter extends ArrayAdapter<JSONObject> {
        public UsersAdapter(Context context, ArrayList<JSONObject> users) {
            super(context, 0, users);
        }

        JSONObject post;

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            post = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
//            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.single_post, parent, false);
//            }
            try {
                ((TextView)convertView.findViewById(R.id.uid)).setText(post.getString("uid"));
                ((TextView)convertView.findViewById(R.id.time)).setText(post.getString("timestamp"));
                ((TextView)convertView.findViewById(R.id.text)).setText(post.getString("text"));
                Log.e("===============",post.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                JSONArray comments = post.getJSONArray("Comment");

            LinearLayout linearLayout = (LinearLayout)convertView.findViewById(R.id.comments);
                if(comments.length()>=1) {
                    for (int i = 0; i < 1; i++) {
                        View comment_view = LayoutInflater.from(getContext()).inflate(R.layout.single_comment, parent, false);
                        JSONObject jsonObject = comments.getJSONObject(i);
                        ((TextView) comment_view.findViewById(R.id.uid)).setText(jsonObject.getString("uid"));
                        ((TextView) comment_view.findViewById(R.id.text)).setText(jsonObject.getString("text"));
                        linearLayout.addView(comment_view);
                    }

                    TextView loadmore = (TextView) convertView.findViewById(R.id.loadmore);
                    loadmore.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {

                        }
                    });

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            // Return the completed view to render on screen

            return convertView;
    }
}







    private class connect extends AsyncTask<String, String,  String> {
        @Override
        protected String doInBackground(String... params) {
            String response=null;
            URL url= null;
            String COOKIES_HEADER = "Set-Cookie";
//            CookieManager msCookieManager = new java.net.CookieManager();
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
            Toast.makeText(HomeActivity.this, result, Toast.LENGTH_SHORT).show();
            Log.e("result...........",result);
            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(result);
                if(!jsonObject.getBoolean("status")){
                   SharedPreferences.Editor editor = prefs.edit();
                    editor.clear();
                    editor.commit();
                    Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                    HomeActivity.this.startActivity(intent);
                    HomeActivity.this.finish();


                }else{
                    JSONArray jsonArray = jsonObject.getJSONArray("data");
                    data.clear();
                    for (int i=0;i<jsonArray.length();i++){
                        data.add(jsonArray.getJSONObject(i));
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
