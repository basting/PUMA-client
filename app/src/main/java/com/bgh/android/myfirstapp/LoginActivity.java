package com.bgh.android.myfirstapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bgh.android.puma.util.AppUtil;
import com.bgh.android.puma.util.Utility;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.json.JSONException;
import org.json.JSONObject;


public class LoginActivity extends ActionBarActivity {

    // Progress Dialog Object
    ProgressDialog prgDialog;
    // Error Msg TextView Object
    TextView errorMsg;
    // Email Edit View Object
    EditText username;
    // Passwprd Edit View Object
    EditText pwd;

    public static final String NURSE_INTERN_ID = "com.bgh.android.myfirstapp.nurseInternId";
    public static final String PATIENT_INTERN_ID = "com.bgh.android.myfirstapp.patientInternId";
    public static final String USERNAME = "com.bgh.android.myfirstapp.username";
    public static final String USER_FULLNAME = "com.bgh.android.myfirstapp.userfullname";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        errorMsg = (TextView) findViewById(R.id.login_error);
        username = (EditText) findViewById(R.id.editTextUserNameToLogin);
        pwd = (EditText) findViewById(R.id.editTextPasswordToLogin);
        prgDialog = new ProgressDialog(this);
        prgDialog.setMessage("Please wait...");
        prgDialog.setCancelable(false); // Set Cancelable as False
    }


    /**
     * Method gets triggered when Register button is clicked
     *
     * @param view
     */
    public void openRegisterPatientScreen(View view){
        navigateToRegisterPatientActivity();
    }

    public void navigateToRegisterPatientActivity(){
        Intent homeIntent = new Intent(getApplicationContext(),RegisterActivity.class);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeIntent);
    }

    public void openRegisterNurseScreen(View view){
        navigateToRegisterNurseActivity();
    }

    public void navigateToRegisterNurseActivity(){
        Intent homeIntent = new Intent(getApplicationContext(),RegisterNurseActivity.class);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeIntent);
    }

    /**
     * Method gets triggered when Login button is clicked
     *
     * @param view
     */
    public void loginUser(View view){
        // Get Email Edit View Value
        String userN = username.getText().toString();
        // Instantiate Http Request Param Object
        RequestParams params = new RequestParams();
        // When Email Edit View and Password Edit View have values other than Null
        if(Utility.isNotNull(userN)){
            // When Email entered is Valid
            //if(Utility.validate(userN)){
                // Put Http parameter username with value of Email Edit View control
                params.put("username", userN);
                // Invoke RESTful Web Service with Http parameters
                invokeWS(params);
            //}
            // When username is invalid
            //else{
            //    Toast.makeText(getApplicationContext(), "Please enter valid username", Toast.LENGTH_LONG).show();
            //}
        } else{
            Toast.makeText(getApplicationContext(), "Please fill the form, don't leave any field blank", Toast.LENGTH_LONG).show();
        }

    }

    /**
     * Method that performs RESTful webservice invocations
     *
     * @param params
     */
    public void invokeWS(RequestParams params){

        //String restServiceIP = getString(R.string.rest_service_ip);
        String restServiceIP = AppUtil.getRestServiceIP(getBaseContext());

        if(restServiceIP == null){
            Toast.makeText(getApplicationContext(), "Please set the REST service IP in PUMA Settings", Toast.LENGTH_LONG).show();
            return;
        }

        if(restServiceIP.trim().length() == 0){
            Toast.makeText(getApplicationContext(), "Please set the REST service IP in PUMA Settings", Toast.LENGTH_LONG).show();
            return;
        }

        // Show Progress Dialog
        prgDialog.show();

        // Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient asyncHttpClient = new AsyncHttpClient();

        asyncHttpClient.get("http://" + restServiceIP + ":8080/api/user/" + username.getText().toString(), new AsyncHttpResponseHandler() {
            // When the response returned by REST has Http response code '200'

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                // Hide Progress Dialog
                prgDialog.dismiss();
                try {
                    // JSON Object
                    JSONObject obj = null;
                    String s = new String(responseBody);
                    obj = new JSONObject(s);

                    // When the JSON response has status boolean value assigned with true
                    String userNameLocal = obj.getString("username");
                    String userNameFromGUI = username.getText().toString();
                    if (userNameFromGUI.equals(userNameLocal)) {
                        String pwdLocal = obj.getString("pwd");
                        if (pwd.getText().toString().equals(pwdLocal)) {
                            Toast.makeText(getApplicationContext(), "You are successfully logged in!", Toast.LENGTH_LONG).show();
                            // Navigate to Home screen
                            navigateToHomePageActivity(userNameLocal, obj.getString("_id"),
                                    obj.getBoolean("isPatient"), null);
                        } else {
                            String errMsg = "Wrong password. Login failed";
                            errorMsg.setText(errMsg);
                            Toast.makeText(getApplicationContext(), errMsg, Toast.LENGTH_LONG).show();
                        }
                    }
                    // Else display error message
                    else {
                        errorMsg.setText(obj.getString("error_msg"));
                        Toast.makeText(getApplicationContext(), obj.getString("error_msg"), Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    Toast.makeText(getApplicationContext(), "Error Occured [Server's JSON response might be invalid]!", Toast.LENGTH_LONG).show();
                    e.printStackTrace();

                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                // Hide Progress Dialog
                prgDialog.dismiss();
                // When Http response code is '404'
                if (statusCode == 404) {
                    Toast.makeText(getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                }
                // When Http response code is '500'
                else if (statusCode == 500) {
                    Toast.makeText(getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
                }
                // When Http response code other than 404, 500
                else {
                    Toast.makeText(getApplicationContext(), "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                }
            }

            // When the response returned by REST has Http response code other than '200'

           /* public void onFailure(int statusCode, Throwable error, String content){}*/
        });
    }

    public void navigateToHomePageActivity(String username, String internalId, boolean isPatient, String fullName){

        Class c = null;

        Intent homeIntent = null;

        if(isPatient) {
            c = PatientHomeActivity.class;
            homeIntent = new Intent(getApplicationContext(), c);
            homeIntent.putExtra(PATIENT_INTERN_ID, internalId);
        }else{// Nurse
            c = NurseMasterHomeActivity.class;
            homeIntent = new Intent(getApplicationContext(), c);
            homeIntent.putExtra(NURSE_INTERN_ID, internalId);
        }

        homeIntent.putExtra(USERNAME, username);
        homeIntent.putExtra(USER_FULLNAME, fullName);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent();
            intent.setClass(LoginActivity.this, SettingsActivity.class);
            startActivityForResult(intent, 0);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
