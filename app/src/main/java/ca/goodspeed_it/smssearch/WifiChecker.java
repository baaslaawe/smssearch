package ca.goodspeed_it.smssearch;


import android.content.Context;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

/**
 * Created by bg on 2016-03-13.
 */
public class WifiChecker {
    public boolean wifiConnectedAndEnabled(Context context) {
        WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        SupplicantState supState = wifiInfo.getSupplicantState();


        return supState == SupplicantState.COMPLETED;
    }
}
