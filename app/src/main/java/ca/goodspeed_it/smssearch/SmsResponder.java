package ca.goodspeed_it.smssearch;

import android.telephony.SmsManager;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by bg on 2016-03-13.
 */
public class SmsResponder {
    public SmsManager getResponder() {
        return SmsManager.getDefault();
    }
    public final int MAX_MSG_LEN = 135;
    public void replyToQuery(String address, String response) {
        String encoded = response;
        encoded = SmsResponder.decompose(response);
        String shortened = encoded.substring(0, MAX_MSG_LEN);
        getResponder().sendTextMessage(address, null, shortened, null, null);
    }

    public static String decompose(String s) {
        return java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+","");
    }

}
