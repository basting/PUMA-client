package com.bgh.android.myfirstapp;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


public class NurseMasterHomeActivity extends ActionBarActivity {
    private String nurseInternId;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nurse_master_home);

        Intent intent = getIntent();
        nurseInternId = intent.getStringExtra(LoginActivity.NURSE_INTERN_ID);
        username = intent.getStringExtra(LoginActivity.USERNAME);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_nurse_master_home, menu);
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

    public void launchNurseHome(View view){
        Intent homeIntent = null;
        homeIntent = new Intent(getApplicationContext(), NurseHomeActivity.class);
        homeIntent.putExtra(LoginActivity.NURSE_INTERN_ID, nurseInternId);
        homeIntent.putExtra(LoginActivity.USERNAME, username);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeIntent);
    }

    public void launchRegisterPatientPage(View view){
        Intent homeIntent = null;
        homeIntent = new Intent(getApplicationContext(), RegisterActivity.class);
        homeIntent.putExtra(LoginActivity.NURSE_INTERN_ID, nurseInternId);
        homeIntent.putExtra(LoginActivity.USERNAME, username);
        homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeIntent);
    }
}
