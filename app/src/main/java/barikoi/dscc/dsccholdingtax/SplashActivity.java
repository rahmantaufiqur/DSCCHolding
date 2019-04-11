package barikoi.dscc.dsccholdingtax;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import barikoi.barikoilocation.RequestQueueSingleton;

public class SplashActivity extends AppCompatActivity {
    ImageView dncc;
    ProgressBar progressBar;
    private String token="";
    private boolean isFirst=true;
    private RequestQueue queue;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        dncc=findViewById(R.id.dncc);
        progressBar=findViewById(R.id.pd);
        progressBar.setVisibility(View.VISIBLE);
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
              Intent intent=new Intent(SplashActivity.this,MainActivity.class);
              startActivity(intent);
              finish();
            }
        }, 1000);
    }
    @Override
    protected void onStart() {
        super.onStart();
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        token = prefs.getString(Api.TOKEN, "");
        isFirst = prefs.getBoolean("isFirst", true);

        proceedusercheck();
    }
    /**
     * This method checks the user validity and proceeds to the appropriate page
     */
    private void proceedusercheck() {
        //Toast.makeText(SplashActivity.this, "checking user", Toast.LENGTH_LONG).show();
        progressBar.setVisibility(View.VISIBLE);
        if (!token.equals("")) {
            queue = RequestQueueSingleton.getInstance(getApplicationContext()).getRequestQueue();
            StringRequest request = new StringRequest(Request.Method.GET, Api.usercheckurl,
                    response -> {
                        try {
                            JSONObject data = new JSONObject(response.toString());
                            JSONObject user = data.getJSONObject("data");
                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString(Api.TOKEN, token);
                            editor.putString(Api.USER_ID, user.getString("id"));
                            editor.putString(Api.EMAIL, user.getString("email"));
                            editor.putString(Api.NAME, user.getString("name"));
                            editor.putString(Api.PHONE, user.getString("phone"));
                            editor.putString(Api.POINTS, user.getString("total_points"));
                            editor.putString(Api.SPENT_POINTS, user.getString("redeemed_points"));
                            editor.putString(Api.ISREFFERED, user.getString(Api.ISREFFERED));
                            editor.putString(Api.REFCODE, user.getString(Api.REFCODE));
                            if(!user.getString(Api.HOME_ID).equals("null")){
                                editor.putString(Api.HOME_ID, user.getString(Api.HOME_ID));
                            }
                            if(! user.getString(Api.WORK_ID).equals("null")){
                                editor.putString(Api.WORK_ID, user.getString(Api.WORK_ID));
                            }
                            editor.commit();
                            routeToAppropriatePage(2);
                        } catch (JSONException e){
                            routeToAppropriatePage(2);
                        }
                    },
                    error -> {
                        NetworkResponse response = error.networkResponse;
                        if (response != null && response.data != null) {
                            switch(response.statusCode) {
                                case 401:{
                                    routeToAppropriatePage(1);
                                    break;
                                }
                                default:
                                    routeToAppropriatePage(2);
                            }
                        } else {
                            Toast.makeText(SplashActivity.this, getString(R.string.could_not_con_acc), Toast.LENGTH_LONG).show();
                            routeToAppropriatePage(2);
                        }
                    }
            ) {

                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("Authorization", "bearer " + token);
                    return params;
                }

            };
            queue.add(request);
        } else {
            if (isFirst) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("isFirst", false);
                editor.commit();
                routeToAppropriatePage(1);

            } else {
                routeToAppropriatePage(1);
            }

        }
    }
    /**
     * @param nameOrCode is the place searching for in the app
     *  requests the server to get info about the current position
     */

    /**
     * redirects the user to appropriate page
     * @param routeopt
     */
    private void routeToAppropriatePage(int routeopt) {
        // Example routing

        switch (routeopt){
            case 1: {
                Intent i = new Intent(this, LoginActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                finish();
                break;
            }
            case 2: {
                Intent i = new Intent(this, MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                finish();
                break;
            }
        }
    }
}
