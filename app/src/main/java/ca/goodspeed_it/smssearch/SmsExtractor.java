package ca.goodspeed_it.smssearch;

import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by bg on 2016-03-13.
 */
public class SmsExtractor {
    public static final String SMS_EXTRA_NAME = "pdus";

    public List<SmsMessage> extractSmss(Intent intent) {
        Bundle extras = intent.getExtras();

        if (extras == null) return Collections.emptyList();


        Object[] smsExtra = (Object[]) extras.get(SMS_EXTRA_NAME);
        List<SmsMessage> smss = new ArrayList<>();

        for (Object smsData : smsExtra) {
            smss.add(buildSMSMsgFromPDU((byte[]) smsData));
        }

        return smss;
    }

    public SmsMessage buildSMSMsgFromPDU(byte[] data) {
        return SmsMessage.createFromPdu(data);
    }

}
