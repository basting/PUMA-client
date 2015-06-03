package com.bgh.android.myfirstapp;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bgh.android.puma.util.AppUtil;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class CaptureActivity4Nurse extends ActionBarActivity {
    private String nurseInternId;
    private String patientInternId;
    private String username;

    Spinner siteOfUlcer;
    EditText addnlInfo;
    EditText length;
    EditText width;
    Spinner exudateAmt;
    Spinner tissueType;
    TextView patientNameLabelTvw;

    ProgressDialog prgDialog;

    Button btnCapture;
    ImageView imageView4Photo;
    int REQUEST_TAKE_PHOTO = 567;
    String mCurrentPhotoPath;

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "PUMA_" + timeStamp + "_";
        File storageDir = Environment.getExternalStorageDirectory();
        storageDir = new File(storageDir.getAbsolutePath()+"/temp/");
        if(!storageDir.exists())
        {
            storageDir.mkdir();
        }

        File f = new File(storageDir.getAbsolutePath(), imageFileName+ ".jpg");

        FileOutputStream fos = null;
        fos = new FileOutputStream(f);

        //bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        fos.flush();
        fos.close();

        //bitmap.recycle();
        //

        mCurrentPhotoPath = f.getAbsolutePath();
        return f;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture_activity_nurse);

        prgDialog = new ProgressDialog(this);
        prgDialog.setMessage("Please wait...");
        prgDialog.setCancelable(false); // Set Cancelable as False

        siteOfUlcer = (Spinner)findViewById(R.id.siteSpinner);
        addnlInfo = (EditText)findViewById(R.id.addnlInfo);
        length = (EditText)findViewById(R.id.length);
        width = (EditText)findViewById(R.id.width);
        exudateAmt = (Spinner)findViewById(R.id.exudateAmtSpinner);
        tissueType = (Spinner)findViewById(R.id.tissueTypeSpinner);

        patientNameLabelTvw = (TextView)findViewById(R.id.patientNameLabel);

        Intent intent = getIntent();
        nurseInternId = intent.getStringExtra(LoginActivity.NURSE_INTERN_ID);
        patientInternId = intent.getStringExtra(LoginActivity.PATIENT_INTERN_ID);
        username = intent.getStringExtra(LoginActivity.USERNAME);
        String patientName = intent.getStringExtra(NurseHomeActivity.TAG_FULL_NAME);

        patientNameLabelTvw.setText("Data for '"+patientName+"'");

        Spinner spinner = siteOfUlcer;
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.pressure_ulcer_site_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);


        Spinner spinner2 = exudateAmt;
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this,R.array.exudate_array, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner2.setAdapter(adapter2);

        Spinner spinner3 = tissueType;
        ArrayAdapter<CharSequence> adapter3 = ArrayAdapter.createFromResource(this,R.array.tissue_type_array, android.R.layout.simple_spinner_item);
        adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner3.setAdapter(adapter3);

        btnCapture = (Button)findViewById(R.id.btnCapture);
        imageView4Photo = (ImageView) findViewById(R.id.imageView4Photo);
        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    // Create the File where the photo should go
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException ex) {
                       ex.printStackTrace();
                    }
                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                        startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                    }
                }
            }
        });

    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    public void grabImageThumbnail(ImageView imageView)
    {
        Uri uri = Uri.fromFile(new File(mCurrentPhotoPath));
        this.getContentResolver().notifyChange(uri, null);
        ContentResolver cr = this.getContentResolver();
        Bitmap bitmap;
        try
        {
            bitmap = android.provider.MediaStore.Images.Media.getBitmap(cr, uri);
            imageView.setImageBitmap(bitmap);
        }
        catch (Exception e)
        {
            Toast.makeText(this, "Failed to load", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_TAKE_PHOTO) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(getApplication(), "Image saved", Toast.LENGTH_LONG).show();
                galleryAddPic();
                grabImageThumbnail(imageView4Photo);
                    /* Below code is useful when only Bitmap is required.
                    *  Intent data becomes null when EXTRA_OUTPUT is passed as Extra to the Camera Intent
                    Bundle b = data.getExtras();
                    Bitmap img = (Bitmap) b.get("data");
                    imageView4Photo.setImageBitmap(img);*/
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(getApplication(), "Image capture cancelled", Toast.LENGTH_LONG).show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_my, menu);
        //return true;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_actions,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch(id){
            //case R.id.action_search:
                //openSearch();
            //    return true;
            case R.id.action_settings:
                //openSettings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void sendToServerFromNurse(View view){

        if(mCurrentPhotoPath == null){
            Toast.makeText(getApplicationContext(), "Please capture a photograph before sending", Toast.LENGTH_LONG).show();
            return;
        }

        String siteOfUlcer = this.siteOfUlcer.getSelectedItem().toString();
        String addnlInfo = this.addnlInfo.getText().toString();
        String length = this.length.getText().toString();
        String width = this.width.getText().toString();
        String exudateAmt = this.exudateAmt.getSelectedItem().toString();
        String tissueType = this.tissueType.getSelectedItem().toString();

        File myFile = new File(mCurrentPhotoPath);
        RequestParams params = new RequestParams();
        try {
            params.put("userPhoto", myFile);
            params.put("patientId",patientInternId);
            params.put("puSite",siteOfUlcer);
            params.put("info",addnlInfo);
            params.put("nurseId", nurseInternId);
            params.put("length",length);
            params.put("width",width);
            params.put("exuAmount",exudateAmt);
            params.put("tissueType",tissueType);

        } catch(FileNotFoundException e) {e.printStackTrace();}

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

        prgDialog.show();

        AsyncHttpClient asyncHttpClient = new AsyncHttpClient();

        asyncHttpClient.post("http://"+ restServiceIP + ":8080/api/photo", params, new AsyncHttpResponseHandler() {
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
            /* public void onFailure(int statusCode, Throwable error, String content){}*/
        });

    }
}
