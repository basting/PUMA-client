package com.bgh.android.myfirstapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.bgh.android.puma.util.AppUtil;
import com.bgh.android.puma.util.DateFormatTextWatcher;
import com.bgh.android.puma.util.Utility;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;


public class RegisterNurseActivity extends ActionBarActivity {

    // Progress Dialog Object
    ProgressDialog prgDialog;

    EditText username;
    EditText password;
    EditText fullName;
    EditText email;
    EditText nurseRegId;
    EditText dateOfBirth;
    RadioGroup sexRadioGroup;
    EditText phone;
    EditText affilHospital;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_nurse);

        prgDialog = new ProgressDialog(this);
        prgDialog.setMessage("Please wait...");
        prgDialog.setCancelable(false); // Set Cancelable as False

        username = (EditText)findViewById(R.id.usernameNurse);
        password = (EditText)findViewById(R.id.registerPasswordNurse);
        fullName = (EditText)findViewById(R.id.registerNameNurse);
        email = (EditText)findViewById(R.id.registerEmailNurse);
        nurseRegId = (EditText) findViewById(R.id.nurseRegID);
        dateOfBirth = (EditText) findViewById(R.id.dobNurse);
        sexRadioGroup = (RadioGroup) findViewById(R.id.sexRadioGrpN);
        phone = (EditText) findViewById(R.id.phoneNurse);
        affilHospital = (EditText) findViewById(R.id.affilHospital);

        phone.addTextChangedListener(new PhoneNumberFormattingTextWatcher());

        dateOfBirth.addTextChangedListener(new DateFormatTextWatcher(dateOfBirth));
    }

    public void navigateToLoginActivity(View view){
        Intent homeIntent = new Intent(getApplicationContext(),LoginActivity.class);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeIntent);
    }

    public void registerNurse(View view){
        // Get fields
        String userN = this.username.getText().toString();
        String pwd = this.password.getText().toString();
        String name = this.fullName.getText().toString();
        String email = this.email.getText().toString();
        String nurseRegId = this.nurseRegId.getText().toString();
        String dateOfBirth = this.dateOfBirth.getText().toString();
        String phone = this.phone.getText().toString();
        String affilHospital = this.affilHospital.getText().toString();

        int radioButtonID = sexRadioGroup.getCheckedRadioButtonId();
        RadioButton sexRadio = (RadioButton)sexRadioGroup.findViewById(radioButtonID);
        String sexStr = sexRadio.getText().toString();
        // Instantiate Http Request Param Object
        RequestParams params = new RequestParams();
        // When Email Edit View and Password Edit View have values other than Null
        if(Utility.isNotNull(userN) &&
                Utility.isNotNull(pwd) &&
                Utility.isNotNull(name) &&
                Utility.isNotNull(email) &&
                Utility.isNotNull(nurseRegId) &&
                Utility.isNotNull(dateOfBirth) &&
                Utility.isNotNull(phone) &&
                Utility.isNotNull(affilHospital)){

            if(Utility.isValidEmail(email)) {
                // When Email entered is Valid

                // Put Http parameter username with value of Email Edit View control
                params.put("username", userN);
                params.put("pwd", pwd);
                params.put("fullName", name);
                params.put("email", email);
                params.put("nurseRegId", nurseRegId);
                params.put("dob", dateOfBirth);
                params.put("phoneNo", phone);
                params.put("sex", sexStr);
                params.put("affilHospital", affilHospital);
                params.put("isPatient", false);
                // Invoke RESTful Web Service with Http parameters
                invokeWS(params);
            }else{
                Toast.makeText(getApplicationContext(), "Invalid email address", Toast.LENGTH_LONG).show();
            }
        } else{
            Toast.makeText(getApplicationContext(), "Please fill the form, don't leave any field blank", Toast.LENGTH_LONG).show();
        }
    }

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

        asyncHttpClient.post("http://"+ restServiceIP + ":8080/api/userinfonurse", params, new AsyncHttpResponseHandler() {
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
                        Toast.makeText(getApplicationContext(), "Nurse registration successfull!!", Toast.LENGTH_LONG).show();
                    }
                    // Else display error message
                    else {
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_register_nurse, menu);
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
}
