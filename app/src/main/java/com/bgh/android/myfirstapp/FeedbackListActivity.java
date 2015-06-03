package com.bgh.android.myfirstapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
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

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;


public class FeedbackListActivity extends ActionBarActivity {

    private String patientInternId;
    private String username;

    ProgressDialog prgDialog;

    ListView listViewFeedbacks;

    private static final String TAG_MODIFIED_DT = "modified";
    public static final String TAG_INFO_FB = "info";
    private static final String TAG_PATIENT_INTERN_ID = "patient";
    private static final String STR_FEEDBACK_CNT = "feedbackCnt";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback_list);

        prgDialog = new ProgressDialog(this);
        prgDialog.setMessage("Please wait...");
        prgDialog.setCancelable(false); // Set Cancelable as False

        listViewFeedbacks = (ListView)findViewById(R.id.listViewFeedbacks);

        Intent intent = getIntent();
        patientInternId = intent.getStringExtra(LoginActivity.PATIENT_INTERN_ID);
        username = intent.getStringExtra(LoginActivity.USERNAME);

        getAllFeedbackForPatientInternId();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_feedback_list, menu);
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

    public void navigateToFeedbackDetails(String feedbackInfo){
        Intent homeIntent = new Intent(getApplicationContext(),FeedbackDetailsActivity.class);
        homeIntent.putExtra(LoginActivity.PATIENT_INTERN_ID, patientInternId);
        homeIntent.putExtra(LoginActivity.USERNAME, username);
        homeIntent.putExtra(TAG_INFO_FB, feedbackInfo);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeIntent);
    }

    public void getAllFeedbackForPatientInternId(){
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

        asyncHttpClient.get("http://"+ restServiceIP + ":8080/api/feedback/"+patientInternId, new AsyncHttpResponseHandler() {
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

                    SimpleDateFormat dateFormatInput = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                    SimpleDateFormat dateFormatOutput = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");

                    for(int i = obj.length()-1; i >= 0; i--) {
                        JSONObject c = obj.getJSONObject(i);
                        String info = c.getString(TAG_INFO_FB);
                        String modifiedDt = c.getString(TAG_MODIFIED_DT);
                        String patientInternId = c.getString(TAG_PATIENT_INTERN_ID);
                        HashMap<String, String> map = new HashMap<>();
                        map.put(STR_FEEDBACK_CNT,"Feedback "+i);
                        map.put(TAG_INFO_FB, info);

                        Date convertedDate = null;
                        try {
                            convertedDate = dateFormatInput.parse(modifiedDt);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        map.put(TAG_MODIFIED_DT, dateFormatOutput.format(convertedDate));
                        map.put(TAG_PATIENT_INTERN_ID,patientInternId);
                        oslist.add(map);
                    }

                    ListAdapter adapter = new SimpleAdapter(FeedbackListActivity.this, oslist,
                            R.layout.list_v_feedback, new String[] { STR_FEEDBACK_CNT,TAG_MODIFIED_DT}, new int[] {
                            R.id.feedbackHeader,R.id.feedbackDate});

                    listViewFeedbacks.setAdapter(adapter);
                    listViewFeedbacks.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view,
                                                int position, long id) {
                            navigateToFeedbackDetails(oslist.get(position).get(TAG_INFO_FB));
                        }
                    });

                    int lstSize = oslist.size();
                    if(lstSize == 1) {
                        setTitle(getTitle() + " (" +lstSize +" feedback)");
                    }else{
                        setTitle(getTitle() + " ("+lstSize  +" feedbacks)");
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
}
