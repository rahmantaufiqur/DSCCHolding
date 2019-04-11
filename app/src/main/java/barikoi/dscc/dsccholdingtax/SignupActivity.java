package barikoi.dscc.dsccholdingtax;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import barikoi.barikoilocation.RequestQueueSingleton;

import static barikoi.dscc.dsccholdingtax.Api.loginurl;
import static barikoi.dscc.dsccholdingtax.Api.signupurl;

/**
 * This is the sign up activity for a new user
 */
public class SignupActivity extends AppCompatActivity {

    protected  String USER_ID="user_id",PASSWORD="password",TOKEN="token",EMAIL="email",NAME="name";
    private String name,email,password,number, cpassword;
    private EditText nameText,emailText,passwordText,numberText, cpasswordText;
    private Button signupButton,skip;
    private TextView loginLink;
    ProgressDialog pd;
    private String player_id;

    private long starttime, endtime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        nameText=(EditText)findViewById(R.id.input_name);
        emailText=(EditText)findViewById(R.id.input_email);
        numberText=(EditText)findViewById(R.id.input_phone);
        passwordText=(EditText)findViewById(R.id.input_password);
        cpasswordText=(EditText)findViewById(R.id.confirm_password);

        signupButton = (Button)findViewById(R.id.btn_signup);
        loginLink=(TextView)findViewById(R.id.link_login);

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });

        loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the registration screen and return
                // to the Login activity
                Intent loginpage= new Intent(SignupActivity.this,LoginActivity.class);
                startActivity(loginpage);
                finish();
            }
        });
    }

    /**
     * Requests the server for a new sign up with the user given info
     */
    public void signup() {

        //Checks if fields are filled or not
        if (!validate()) {
            //onSignupFailed();
            return;
        }

        signupButton.setEnabled(false);

        pd = new ProgressDialog(this);
        pd.setMessage("Signing up...");
        pd.show();


        RequestQueue queue= RequestQueueSingleton.getInstance(getApplicationContext()).getRequestQueue();
        StringRequest request=new StringRequest(Request.Method.POST,signupurl,
                new Response.Listener<String>(){
                    @Override
                    public void onResponse(String response){
                        pd.dismiss();
                        onSignupSuccess();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        pd.dismiss();
                        if(error != null && error.networkResponse != null){
                            switch(error.networkResponse.statusCode){
                                default:
                                    try {
                                        onSignupFailed(new String(error.networkResponse.data,"UTF-8"));
                                    } catch (UnsupportedEncodingException e) {
                                        e.printStackTrace();
                                    }
                            }
                        }
                        else    onSignupFailed(getString(R.string.couldnot_connect));
                    }
                }
        ){
            @Override

            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parameters = new HashMap<String, String>();
                //parameters.put("id", id.getText().toString());
                parameters.put("name",name);
                parameters.put("email",email);
                parameters.put("password",password);
                parameters.put("phone",number);
                parameters.put("userType","2");
                return parameters;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(15 * 1000, 0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(request);
    }

    /**
     * Logs in the user after successful sign up
     */
    public void onSignupSuccess() {
        pd = new ProgressDialog(this);
        pd.setMessage("authenticating...");
        pd.show();
        endtime=System.currentTimeMillis();


        signupButton.setEnabled(true);
        RequestQueue queue= RequestQueueSingleton.getInstance(getApplicationContext()).getRequestQueue();
        StringRequest request=new StringRequest(Request.Method.POST,loginurl,
                new Response.Listener<String>(){
                    @Override
                    public void onResponse(String response){
                        try {
                            JSONObject responsedata= new JSONObject(response.toString());
                            JSONObject tokendata= responsedata.getJSONObject("data");
                            String token= tokendata.getString("token");
                            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString(EMAIL,email);
                            editor.putString(TOKEN,token);
                            editor.commit();
                            pd.dismiss();
                            onLoginDone();
                            // onLoginFailed();

                        } catch (JSONException e) {
                            pd.dismiss();
                            Toast.makeText(SignupActivity.this, "problem or change in server, please wait and try again.", Toast.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        pd.dismiss();
                        Toast.makeText(SignupActivity.this, "Problem connecting, please wait and try again.", Toast.LENGTH_LONG).show();

                    }
                }
        ){
            @Override

            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parameters = new HashMap<String, String>();
                //parameters.put("id", id.getText().toString());
                parameters.put("email",email);
                parameters.put("password",password);
                parameters.put("device_ID",player_id);
                return parameters;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(15 * 1000, 0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(request);

    }

    /**
     * This method is called when the sign up process is failed
     * @param s is either a Character set error or an network error from user end
     */
    public void onSignupFailed(String s) {
        if(!s.equals("")) {
            try {
                JSONObject data=new JSONObject(s).getJSONObject("messages");
                if(data.has("email")){
                    emailText.setError(data.getJSONArray("email").getString(0));
                }
                if(data.has("phone")){
                    numberText.setError(data.getJSONArray("phone").getString(0));
                }
                if(data.has("password")){
                    passwordText.setError(data.getJSONArray("password").getString(0));
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(getBaseContext(),  s, Toast.LENGTH_LONG).show();
            }
        }
        signupButton.setEnabled(true);
    }

    /**
     * If login is successful the user is redirected to WelcomeUser activity
     */
    public void onLoginDone() {
        Intent welcome=new Intent(this,MainActivity.class);
        welcome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(welcome);
    }
    @Override
    public void onBackPressed() {
        // disable going back to the MainActivity
        moveTaskToBack(true);
    }

    /**
     * This method validates the user name, email and password
     * @return true if the given info is valid or else returns false
     */
    public boolean validate() {
        boolean valid = true;
        name = nameText.getText().toString();
        email = emailText.getText().toString();
        password = passwordText.getText().toString();
        number = numberText.getText().toString();
        cpassword=cpasswordText.getText().toString();


        if (name.equals("")) {
            valid = false;
            nameText.setError(getString(R.string.this_field_is_required));
        }
        if (email.equals("")) {
            valid = false;
            emailText.setError(getString(R.string.this_field_is_required));
        }
        if (password.equals("")) {
            valid = false;
            passwordText.setError(getString(R.string.this_field_is_required));
        }
        if (number.equals("")) {
            valid = false;
            numberText.setError(getString(R.string.this_field_is_required));
        }
        else {

            if (name.length() < 3) {
                nameText.setError("at least 3 characters");
                valid = false;
            } else {
                nameText.setError(null);
            }

            if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailText.setError("enter a valid email address");
                valid = false;
            } else {
                emailText.setError(null);
            }
            if (number.length() < 11) {
                numberText.setError("enter a valid mobile number");
                valid = false;
            } else {
                numberText.setError(null);
            }

            if (password.length() < 6) {
                passwordText.setError("More than 4 alphanumeric characters");
                valid = false;
            } else {
                passwordText.setError(null);
            }

            if (!password.equals(cpassword)) {
                cpasswordText.setError("Password Does not Match");
                valid = false;
            } else {
                cpasswordText.setError(null);
            }
        }
        return valid;
    }
}
