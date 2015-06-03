package com.bgh.android.puma.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by BastinGomez on 2015-04-10.
 */
public class AppUtil {

    public static String getRestServiceIP(Context context){
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(context);
        String restIPAddress = SP.getString("rest_ip_address_key","");
        return restIPAddress;
    }
}
