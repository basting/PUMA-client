package com.bgh.android.myfirstapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
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
import java.util.ArrayList;
import java.util.HashMap;


public class NurseHomeActivity extends ActionBarActivity {
    private String nurseInternId;
    private String username;

    ProgressDialog prgDialog;

    ListView listViewPatients;

    //JSON tag names
    public static final String TAG_FULL_NAME = "PatientName";
    private static final String TAG_PATIENT_ID = "PatientID";
    private static final String TAG_PATIENT_INTERN_ID = "_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nurse_home);

        prgDialog = new ProgressDialog(this);
        prgDialog.setMessage("Please wait...");
        prgDialog.setCancelable(false); // Set Cancelable as False

        listViewPatients = (ListView)findViewById(R.id.listViewPatients);

        Intent intent = getIntent();
        nurseInternId = intent.getStringExtra(LoginActivity.NURSE_INTERN_ID);
        username = intent.getStringExtra(LoginActivity.USERNAME);

        getAllPatientsForNurseInternId();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_nurse_home, menu);
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

    public void getAllPatientsForNurseInternId(){
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

        asyncHttpClient.get("http://" + restServiceIP + ":8080/api/userinfopat/" + nurseInternId, new AsyncHttpResponseHandler() {
            // When the response returned by REST has Http response code '200'

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                // Hide Progress Dialog
                prgDialog.dismiss();
                try {
                    // JSON Object
                    JSONArray obj = null;
                    String s = new String(responseBody);
                    obj = new JSONArray(s);

                    final ArrayList<HashMap<String, String>> oslist = new ArrayList<HashMap<String, String>>();

                    for(int i = 0; i < obj.length(); i++) {
                        JSONObject c = obj.getJSONObject(i);
                        String fName = c.getString(TAG_FULL_NAME);
                        String patientID = c.getString(TAG_PATIENT_ID);
                        String patientInternId = c.getString(TAG_PATIENT_INTERN_ID);
                        HashMap<String, String> map = new HashMap<>();
                        map.put(TAG_FULL_NAME, fName);
                        map.put(TAG_PATIENT_ID, patientID);
                        map.put(TAG_PATIENT_INTERN_ID,patientInternId);
                        oslist.add(map);
                    }

                    ListAdapter adapter = new SimpleAdapter(NurseHomeActivity.this, oslist,
                          R.layout.list_v, new String[] { TAG_FULL_NAME,TAG_PATIENT_ID}, new int[] {
                          R.id.patientFullNameInList,R.id.patientIDInList});

                    listViewPatients.setAdapter(adapter);
                    listViewPatients.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view,
                                                int position, long id) {
                            String fullName = oslist.get(position).get(TAG_FULL_NAME);
                            Toast.makeText(getApplicationContext(), "Selected patient: " + fullName, Toast.LENGTH_LONG).show();
                            navigateToCapture4NurseActivity(oslist.get(position).get(TAG_PATIENT_INTERN_ID), fullName);
                        }
                    });

                    int lstSize = oslist.size();
                    if(lstSize == 1) {
                        setTitle(getTitle() + " (" +lstSize +" patient)");
                    }else{
                        setTitle(getTitle() + " ("+lstSize  +" patients)");
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

        registerForContextMenu(listViewPatients);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        AdapterView.AdapterContextMenuInfo aInfo = (AdapterView.AdapterContextMenuInfo) menuInfo;

        // We know that each row in the adapter is a Map
        ListAdapter simpleAdpt = listViewPatients.getAdapter();
        HashMap map =  (HashMap) simpleAdpt.getItem(aInfo.position);
        menu.setHeaderTitle("Options for " + map.get(TAG_FULL_NAME));
        menu.add(1, 1, 1, "Capture Photo and Details");
        menu.add(1, 2, 2, "Feedback");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
       int itemId = item.getItemId();
       AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
       ListAdapter simpleAdpt = listViewPatients.getAdapter();
       HashMap<String, String> map =  (HashMap) simpleAdpt.getItem(info.position);
       String fullName = map.get(TAG_FULL_NAME);
       String patientInternId = map.get(TAG_PATIENT_INTERN_ID);
       if(itemId == 1){
           navigateToCapture4NurseActivity(patientInternId,fullName);
       }else if(itemId == 2){
           requestFeedback(patientInternId);
       }
       return true;
    }

    public void navigateToCapture4NurseActivity(String patientInternId, String fullName){
        Intent homeIntent = new Intent(getApplicationContext(),CaptureActivity4Nurse.class);
        homeIntent.putExtra(LoginActivity.PATIENT_INTERN_ID, patientInternId);
        homeIntent.putExtra(LoginActivity.NURSE_INTERN_ID, nurseInternId);
        homeIntent.putExtra(LoginActivity.USERNAME, username);
        homeIntent.putExtra(TAG_FULL_NAME, fullName);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeIntent);
    }

    public void requestFeedback(String patientInternId){
        Intent homeIntent = null;
        homeIntent = new Intent(getApplicationContext(), FeedbackListActivity.class);
        homeIntent.putExtra(LoginActivity.PATIENT_INTERN_ID, patientInternId);
        homeIntent.putExtra(LoginActivity.USERNAME, username);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeIntent);
    }
}
