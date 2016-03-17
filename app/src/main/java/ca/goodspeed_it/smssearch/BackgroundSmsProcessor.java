package ca.goodspeed_it.smssearch;

import android.os.AsyncTask;

/**
 * Created by bg on 2016-03-16.
 */
public class BackgroundSmsProcessor extends AsyncTask <String, Void, String > {

    public QueryResponder queryResponder = new QueryResponder();
    public SmsResponder smsResponder = new SmsResponder();
    public CustomLogger logger = new CustomLogger();

    @Override
    protected String doInBackground(String... args) {
        String body = args[0];
        String address = args[1];

        String response = queryResponder.responseFor(body);
        logger.log("Response: " + response);
        smsResponder.replyToQuery(address, response);

        return response;
    }



}
