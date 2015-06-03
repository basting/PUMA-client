package com.bgh.android.myfirstapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
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


public class RegisterActivity extends ActionBarActivity {

    // Progress Dialog Object
    ProgressDialog prgDialog;

    EditText username;
    EditText password;
    EditText patientName;
    EditText email;
    EditText patientRegId;
    EditText motherMaidenName;
    EditText dateOfBirth;
    RadioGroup sexRadioGroup;
    EditText patientAddress;
    Spinner countryCode;
    EditText patientPhone;
    EditText primaryLang;
    EditText citizenship;

    private String nurseInternId;
    private String loggedInUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        prgDialog = new ProgressDialog(this);
        prgDialog.setMessage("Please wait...");
        prgDialog.setCancelable(false); // Set Cancelable as False

        Intent intent = getIntent();
        nurseInternId = intent.getStringExtra(LoginActivity.NURSE_INTERN_ID);
        loggedInUsername = intent.getStringExtra(LoginActivity.USERNAME);

        username = (EditText)findViewById(R.id.username);
        password = (EditText)findViewById(R.id.registerPassword);
        patientName = (EditText)findViewById(R.id.registerName);
        email = (EditText)findViewById(R.id.registerEmail);
        patientRegId = (EditText) findViewById(R.id.patientRegID);
        motherMaidenName = (EditText) findViewById(R.id.motherMaidenName);
        dateOfBirth = (EditText) findViewById(R.id.dob);
        sexRadioGroup = (RadioGroup) findViewById(R.id.sexRadioGrp);
        patientAddress = (EditText) findViewById(R.id.patientAddress);
        countryCode = (Spinner) findViewById(R.id.countryCodeSpinner);
        patientPhone = (EditText) findViewById(R.id.phone);
        primaryLang = (EditText) findViewById(R.id.patientLang);
        citizenship = (EditText) findViewById(R.id.patientcitizenShip);

        Spinner spinner = countryCode;
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.country_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        EditText editTextPhone = patientPhone;
        editTextPhone.addTextChangedListener(new PhoneNumberFormattingTextWatcher());

        EditText editTextDob = dateOfBirth;
        editTextDob.addTextChangedListener(new DateFormatTextWatcher(editTextDob));
    }

    public void navigateToLoginActivity(View view){
        Intent homeIntent = new Intent(getApplicationContext(),LoginActivity.class);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeIntent);
    }

    public void registerPatient(View view){
        // Get fields
        String userN = this.username.getText().toString();
        String pwd = this.password.getText().toString();
        String name = this.patientName.getText().toString();
        String email = this.email.getText().toString();
        String patientRegId = this.patientRegId.getText().toString();
        String motherMaidenName = this.motherMaidenName.getText().toString();
        String dateOfBirth = this.dateOfBirth.getText().toString();
        String patientAddress = this.patientAddress.getText().toString();
        String countryCode = this.countryCode.getSelectedItem().toString();
        String patientPhone = this.patientPhone.getText().toString();
        String primaryLang = this.primaryLang.getText().toString();
        String citizenship = this.citizenship.getText().toString();

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
                Utility.isNotNull(patientRegId) &&
                Utility.isNotNull(motherMaidenName) &&
                Utility.isNotNull(dateOfBirth) &&
                Utility.isNotNull(patientAddress) &&
                Utility.isNotNull(countryCode) &&
                Utility.isNotNull(patientPhone) &&
                Utility.isNotNull(primaryLang) &&
                Utility.isNotNull(citizenship)){

            if(Utility.isValidEmail(email)) {
                // When Email entered is Valid

                // Put Http parameter username with value of Email Edit View control
                params.put("username", userN);
                params.put("pwd", pwd);
                params.put("patientName", name);
                params.put("email", email);
                params.put("patientRegId", patientRegId);
                params.put("motherMName", motherMaidenName);
                params.put("dob", dateOfBirth);
                params.put("pAddress", patientAddress);
                params.put("countryCode", countryCode);
                params.put("phoneNo", patientPhone);
                params.put("primaryLang", primaryLang);
                params.put("citizenship", citizenship);
                params.put("sex", sexStr);
                params.put("isPatient", true);

                if (this.nurseInternId != null) {
                    params.put("nurseId", nurseInternId);
                }

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

        asyncHttpClient.post("http://"+ restServiceIP + ":8080/api/userinfopat", params, new AsyncHttpResponseHandler() {
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
                        Toast.makeText(getApplicationContext(), "Patient registration successfull!!", Toast.LENGTH_LONG).show();
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
        getMenuInflater().inflate(R.menu.menu_register, menu);
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
