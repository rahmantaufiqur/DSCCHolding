package barikoi.dscc.dsccholdingtax;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import barikoi.barikoilocation.JsonUtils;
import barikoi.barikoilocation.RequestQueueSingleton;

import static barikoi.dscc.dsccholdingtax.Api.loginurl;

/**
 * this activity checks the username and password the logs in the user
 */
public class LoginActivity extends AppCompatActivity {


    private static final int APP_REQUEST_CODE = 99;
    private static final int REQUEST_CHECK_PERMISSION = 11;
    protected  String USER_ID="user_id",PASSWORD="password",TOKEN="token",EMAIL="email",NAME="name",player_id="1";
    EditText emailText,passText;
    TextView signin_link,resetpass;
    Button login,skip;
    ProgressDialog pd;

    private long starttime,formtime,logintime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        init();

    }

    /**
     * Initializes the view Components
     */
    private void init(){
        starttime= System.currentTimeMillis();
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        emailText=(EditText)findViewById(R.id.input_email);
        passText=(EditText)findViewById(R.id.input_password);
        signin_link=(TextView)findViewById(R.id.link_signup);
        resetpass=(TextView)findViewById(R.id.textView_resetpass);
        resetpass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //reqReadSMS();
                ViewDialog resetdialog=new ViewDialog();
                resetdialog.showresetpassInputDialog(LoginActivity.this);
            }
        });

        signin_link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signup=new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(signup);
                finish();
            }
        });
        login=(Button)findViewById(R.id.btn_login);


        emailText.setText(prefs.getString(EMAIL,""));
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
    }

    /**
     * Check the login information with the server if the user is valid or not
     * if yes, redirects the user to Main activity
     *if no, throws Login failed
     */
    private void login(){
        if (!validate()) {
            onLoginFailed();
            return;
        }
        final String email = emailText.getText().toString();
        final String password = passText.getText().toString();
        login.setEnabled(false);

        pd = new ProgressDialog(this);
        pd.setMessage("Authenticating...");
        pd.show();
        formtime=System.currentTimeMillis();
        // Build and send timing.


        // TODO: Implement your own authentication logic here.

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
                            onLoginSuccess();
                            // onLoginFailed();

                        } catch (JSONException e) {
                            pd.dismiss();
                            Toast.makeText(LoginActivity.this, R.string.prob_change_server, Toast.LENGTH_LONG).show();
                            try{
                                JSONObject exception=new JSONObject(response);
                                JSONObject message=exception.getJSONObject("message");
                                if(message.toString().equals("invalid_credentials")){
                                    Log.d("Login","invalid");
                                }
                            }
                            catch(JSONException je){}
                        }
                    }

                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        pd.dismiss();
                        Toast.makeText(LoginActivity.this, JsonUtils.handleResponse(error), Toast.LENGTH_SHORT).show();
                        onLoginFailed();
                    }
                }
        )
        {
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
        queue.add(request);
    }

    @Override
    public void onBackPressed() {
        // disable going back to the MainActivity
        moveTaskToBack(true);
    }

    /**
     * Redirects the user to main Activity if login is successful
     */
    public void onLoginSuccess() {
        login.setEnabled(true);
        // Build and send timing.

        init();
       /* PlaceDbAdapter db=new PlaceDbAdapter(this);
        db.open();
        db.deleteAllPlaces();
        db.close();*/
        finish();
        Intent i=new Intent(this,MainActivity.class);
        startActivity(i);
    }

    /**
     * Throws a toast if the login fails
     */
    public void onLoginFailed() {
        Toast.makeText(getBaseContext(), R.string.check_email_password_correct, Toast.LENGTH_LONG).show();
/*
        PlaceDbAdapter db=new PlaceDbAdapter(this);
        db.open();
        db.deleteAllPlaces();
        db.close();*/
        login.setEnabled(true);
    }

    /**
     * validates the email nad password user has given
     * @return
     */
    private boolean validate(){
        boolean valid = true;

        String email = emailText.getText().toString();
        String password = passText.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailText.setError("enter a valid email address");
            valid = false;
        }
        else {
            emailText.setError(null);
        }
        if (password.isEmpty()) {
            passText.setError("Password required");
            valid = false;
        } else {
            passText.setError(null);
        }


        return valid;
    }

   /* public void phoneLogin() {
        final Intent intent = new Intent(this, AccountKitActivity.class);
        AccountKitConfiguration.AccountKitConfigurationBuilder configurationBuilder =
                new AccountKitConfiguration.AccountKitConfigurationBuilder(
                        LoginType.PHONE,
                        AccountKitActivity.ResponseType.CODE); // or .ResponseType.TOKEN
        // ... perform additional configuration ...
        intent.putExtra(
                AccountKitActivity.ACCOUNT_KIT_ACTIVITY_CONFIGURATION,
                configurationBuilder.setReceiveSMS(true).build());
        startActivityForResult(intent, APP_REQUEST_CODE);
    }

    private void reqReadSMS(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED  && ActivityCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED ){
            // TODO: Consider calling
            ActivityCompat.requestPermissions((Activity) this, new String[]{Manifest.permission.READ_SMS,Manifest.permission.RECEIVE_SMS}, REQUEST_CHECK_PERMISSION);

        } else {
            phoneLogin();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==REQUEST_CHECK_PERMISSION){
            phoneLogin();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == APP_REQUEST_CODE) { // confirm that this response matches your request
            AccountKitLoginResult loginResult = data.getParcelableExtra(AccountKitLoginResult.RESULT_KEY);
            String toastMessage;
            if (loginResult.getError() != null) {
                toastMessage = loginResult.getError().getErrorType().getMessage();
                //showErrorActivity(loginResult.getError());
            } else if (loginResult.wasCancelled()) {
                toastMessage = "Login Cancelled";
            } else {
                if (loginResult.getAccessToken() != null) {
                    toastMessage = "Success:" + loginResult.getAccessToken().getAccountId();
                } else {
                    toastMessage = String.format(
                            "Success:%s...",
                            loginResult.getAuthorizationCode().substring(0,10));
                }
                AccountKit.logOut();

                // If you have an authorization code, retrieve it from
                // loginResult.getAuthorizationCode()
                // and pass it to your server and exchange it for an access token.

                // Success! Start your next activity...

                //goToMyLoggedInActivity();
            }

            // Surface the result to your user in an appropriate way.
            Toast.makeText(
                    this,
                    toastMessage,
                    Toast.LENGTH_LONG)
                    .show();
        }
    }*/
}

