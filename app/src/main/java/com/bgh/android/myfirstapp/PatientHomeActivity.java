package com.bgh.android.myfirstapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.bgh.android.puma.util.AppUtil;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;


public class PatientHomeActivity extends ActionBarActivity {

    private String patientInternId;
    private String username;

    ProgressDialog prgDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_home);

        prgDialog = new ProgressDialog(this);
        prgDialog.setMessage("Please wait...");
        prgDialog.setCancelable(false); // Set Cancelable as False

        Intent intent = getIntent();
        patientInternId = intent.getStringExtra(LoginActivity.PATIENT_INTERN_ID);
        username = intent.getStringExtra(LoginActivity.USERNAME);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_patient_home, menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void requestFeedback(View view){
        Intent homeIntent = null;
        homeIntent = new Intent(getApplicationContext(), FeedbackListActivity.class);
        homeIntent.putExtra(LoginActivity.PATIENT_INTERN_ID, patientInternId);
        homeIntent.putExtra(LoginActivity.USERNAME, username);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeIntent);
    }

    public void launchNewRecordPage(View view){
        navigateToCapture4PatientActivity();
    }


    public void navigateToCapture4PatientActivity(){
        Intent homeIntent = new Intent(getApplicationContext(),CaptureActivity4Patient.class);
        homeIntent.putExtra(LoginActivity.PATIENT_INTERN_ID, patientInternId);
        homeIntent.putExtra(LoginActivity.USERNAME, username);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeIntent);
    }

    public void requestFullMedicalRecord(View view){

        Toast.makeText(getApplicationContext(), "Full Medical Record requested. Please check your email shortly.", Toast.LENGTH_LONG).show();
        return;

        //String restServiceIP = getString(R.string.rest_service_ip);
        /*String restServiceIP = AppUtil.getRestServiceIP(getBaseContext());

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

        asyncHttpClient.get("http://"+ restServiceIP + ":8080/api/user/details", new AsyncHttpResponseHandler() {
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
                    if (obj.getBoolean("status")) {
                        Toast.makeText(getApplicationContext(), "Request successfully submitted to the server", Toast.LENGTH_LONG).show();
                        // Navigate to Home screen
                        //navigateToCapture4PatientActivity();
                    }
                    // Else display error message
                    else {
                        //errorMsg.setText(obj.getString("error_msg"));
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
            *//* public void onFailure(int statusCode, Throwable error, String content){}*//*
        });
*/
    }
}
