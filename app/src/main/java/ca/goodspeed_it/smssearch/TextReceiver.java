package ca.goodspeed_it.smssearch;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

import java.util.List;


public class TextReceiver extends BroadcastReceiver {


    public QueryResponder queryResponder = new QueryResponder();
    public WifiChecker wifiChecker = new WifiChecker();
    public SmsExtractor smsExtractor = new SmsExtractor();
    public SmsResponder smsResponder = new SmsResponder();
    public CustomLogger logger = new CustomLogger();


        @Override
    public void onReceive(Context context, Intent intent) {
        logger.log("Received broadcast");
        for (SmsMessage sms : smsExtractor.extractSmss(intent)) {
            processMessage(sms, context);
        }
        logger.log("Processed broadcast");
    }

    public String processMessage(SmsMessage sms, Context context) {
        String body = sms.getMessageBody();
        String address = sms.getOriginatingAddress();
        String response = "unknown";
        logger.log("Processing message: (" + body + ")");
        boolean matches = queryResponder.respondsTo(body);
        if (matches && wifiChecker.wifiConnectedAndEnabled(context)) {
            logger.log("Connected to wifi, responding");


            execBackgroundProcessor(body, address);

            logger.log("Replied");
        } else if (matches) {
            logger.log("Matches but no wifi");
            smsResponder.replyToQuery(address, response);
        }
        logger.log("Done processing msg");
        return response;
    }

    public void execBackgroundProcessor(String body, String address) {

        new BackgroundSmsProcessor().execute(body, address);
    }

}
