package ca.goodspeed_it.smssearch;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.internal.matchers.Null;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class SmsSearchUnitTest {

    class JSONQueryResponder extends QueryResponder {
        private final JSONObject mockJSON;
        private String httpResponseBody;

        JSONQueryResponder(JSONObject json, String httpResponseBody) {
            this.mockJSON = json;
            this.httpResponseBody = httpResponseBody;
        }
        public JSONObject forJsonText(String jsonResponse) throws JSONException {
            return this.mockJSON;
        }


        @Override
        public String performHTTPRequest(URL url) throws IOException {
            return this.httpResponseBody;
        }
    }
    class FakeTextReceiver extends TextReceiver {
        private SmsMessage fakeSmsMessage;
        public FakeTextReceiver(SmsMessage msg) {
            this.fakeSmsMessage = msg;
        }

    }

    class EmptySmsExtractor extends SmsExtractor {
        public List<SmsMessage> extractSmss(Intent intent) {
            return Collections.emptyList();
        }

    }
    class FakeSmsExtractor extends SmsExtractor {
        private SmsMessage fakeSmsMessage;
        public FakeSmsExtractor(SmsMessage msg) {
            this.fakeSmsMessage = msg;
        }


        public List<SmsMessage> extractSmss(Intent intent) {
            return Collections.singletonList(fakeSmsMessage);
        }

        public SmsMessage buildSMSMsgFromPDU(byte[] data) {
            return this.fakeSmsMessage;
        }

    }

    class FakeQueryResponder extends QueryResponder {
        private final boolean responds;
        private final String response;

        public FakeQueryResponder(boolean responds, String response) {
            this.responds = responds;
            this.response = response;
        }

        public boolean respondsTo(String body) {
            return this.responds;
        }

        public String responseFor(String body) {
            return this.response;
        }


    }

    class FakeWifiChecker extends WifiChecker {
        public boolean wifiUp = false;
        public FakeWifiChecker(boolean rv) {
            this.wifiUp = rv;
        }

        @Override
        public boolean wifiConnectedAndEnabled(Context txt){
            return this.wifiUp;
        }
    }
    class DeliberateTestException extends RuntimeException {

    }

    class ExplodingSmsExtractor extends SmsExtractor {
        @Override
        public List<SmsMessage> extractSmss(Intent intent) {
            throw new DeliberateTestException();
        }
    }

    class ZeroMessageSmsExtractor extends  SmsExtractor {
        @Override
        public List<SmsMessage> extractSmss(Intent intent) {
            return Collections.emptyList();
        }

    }

    class LoggingSmsResponder extends SmsResponder {
        public List<String> sentMessages = new ArrayList<String>();;
        public List<String> sentAddresses = new ArrayList<String>();;

        public void replyToQuery(String address, String response) {
            sentAddresses.add(address);
            sentMessages.add(response);
        }
    }

    class FakeManagerSmsResponder extends SmsResponder {
        private final SmsManager fakeManager;

        FakeManagerSmsResponder(SmsManager fakeManager){
            this.fakeManager = fakeManager;
        }
        public SmsManager getResponder() {
            return this.fakeManager;

        }
    }

    class NullLogger extends CustomLogger {
        @Override
        public void log(String msg) {
            // NOOP
        }
    }

    @Mock
    Context mockContext = mock(Context.class);

    @Mock
    Intent mockIntent = mock(Intent.class);

    @Mock
    Bundle mockBundle = mock(Bundle.class);

    @Mock
    SmsMessage mockMessage = mock(SmsMessage.class);


    @Test
    public void smsExtractorPullsOutText() {
        when(mockIntent.getExtras()).thenReturn(mockBundle);
        when(mockBundle.get(SmsExtractor.SMS_EXTRA_NAME)).thenReturn(createSMSRawData());
        when(mockMessage.getMessageBody()).thenReturn("msg_body");
        when(mockMessage.getOriginatingAddress()).thenReturn("msg_address");

        FakeSmsExtractor extractor = new FakeSmsExtractor(mockMessage);

        List<SmsMessage> msgs = extractor.extractSmss(mockIntent);
        assertEquals(1, msgs.size());
        assertEquals("msg_body", msgs.get(0).getMessageBody());
    }

    @Test
    public void smsExtractorPullsOutText_one() {
        when(mockIntent.getExtras()).thenReturn(null);

        FakeSmsExtractor extractor = new FakeSmsExtractor(mockMessage);

        List<SmsMessage> msgs = extractor.extractSmss(mockIntent);
        assertEquals(1, msgs.size());
    }

    @Test
    public void queryResponderIsTrueForGoogle() {
        QueryResponder qr = new QueryResponder();
        assertEquals(true,  qr.respondsTo("search:"));
        assertEquals(true,  qr.respondsTo("Search:"));
        assertEquals(false, qr.respondsTo("Sreach:"));
        assertEquals(false, qr.respondsTo("Search is"));
        assertEquals(false, qr.respondsTo(null));
    }


    @Mock
    JSONObject mockJSON = mock(JSONObject.class);

    @Mock
    JSONArray mockJSONArray = mock(JSONArray.class);


    @Test
    public void queryResponderCallsOutForResponse_fakeJSON() throws Exception {

        when(mockJSON.getJSONArray(QueryResponder.API_TOPLEVEL)).thenReturn(mockJSONArray);
        when(mockJSONArray.getJSONObject(0)).thenReturn(mockJSON);
        when(mockJSON.getString(QueryResponder.API_TEXTKEY)).thenReturn("idano");
        JSONQueryResponder qr = new JSONQueryResponder(mockJSON, "unused");

        assertEquals("idano",  qr.responseFor("search: cheese"));

    }


    @Test
    public void smsExtractorPullsOutText_empty() {
        when(mockIntent.getExtras()).thenReturn(null);

        EmptySmsExtractor extractor = new EmptySmsExtractor();

        List<SmsMessage> msgs = extractor.extractSmss(mockIntent);
        assertEquals(0, msgs.size());
    }

    @Test
    public void textReceiver_processesWithWifi() {
        TextReceiver r = new TextReceiver();
        r.wifiChecker = new FakeWifiChecker(true);
        r.smsExtractor = new ExplodingSmsExtractor();
        r.logger = new NullLogger();
        try {
            r.onReceive(null, null);
            assertTrue("should have reached extraction", false);
        } catch (DeliberateTestException e) {
        }
    }

    @Test
    public void textReceiver_stopsWithoutWifi() {
        TextReceiver r = new TextReceiver();
        r.wifiChecker = new FakeWifiChecker(false);
        r.smsExtractor = new ZeroMessageSmsExtractor();
        r.logger = new NullLogger();
        r.onReceive(null, null);
    }


    @Test
    public void textReceiver_processesMsgUnknown() {
        TextReceiver r = new TextReceiver();
        r.logger = new NullLogger();
        r.queryResponder = new FakeQueryResponder(false, "");
        LoggingSmsResponder resp = new LoggingSmsResponder();
        r.smsResponder = resp;


        assertEquals("unknown", r.processMessage(mockMessage, null));
        assertEquals(0, resp.sentAddresses.size());
    }

    class NonBGTextReceiver extends TextReceiver {

        public BackgroundSmsProcessor bgProcessor;
        NonBGTextReceiver(){
            this.bgProcessor = new BackgroundSmsProcessor();
        }
        @Override
        public void execBackgroundProcessor(String body, String address) {
            String args[] = new String[2];
            args[0] = body;
            args[1] = address;

            bgProcessor.doInBackground(args);
        }
    }

    @Test
    public void textReceiver_processesMsg() {
        NonBGTextReceiver r = new NonBGTextReceiver();
        r.logger = new NullLogger();
        r.queryResponder = new FakeQueryResponder(true, "fake_reply");
        LoggingSmsResponder resp = new LoggingSmsResponder();
        r.smsResponder = resp;
        r.wifiChecker = new FakeWifiChecker(true);
        r.bgProcessor.queryResponder = r.queryResponder;
        r.bgProcessor.smsResponder = resp;
        r.bgProcessor.logger = r.logger;
        when(mockMessage.getOriginatingAddress()).thenReturn("msg_address");


        r.processMessage(mockMessage, null);
        assertEquals("fake_reply", resp.sentMessages.get(0));
        assertEquals("msg_address", resp.sentAddresses.get(0));
    }

    @Mock()
    WifiManager mockWifiManager = mock(WifiManager.class);
    @Mock()
    WifiInfo mockConnectionInfo = mock(WifiInfo.class);

    @Test
    public void wifiCheckerDeterminesIfUp() {
        when(mockContext.getSystemService(Context.WIFI_SERVICE)).thenReturn(mockWifiManager);
        when(mockWifiManager.getConnectionInfo()).thenReturn(mockConnectionInfo);
        when(mockConnectionInfo.getSupplicantState()).thenReturn(SupplicantState.COMPLETED);
        WifiChecker wc = new WifiChecker();

        assertEquals(true, wc.wifiConnectedAndEnabled(mockContext));
    }

    @Test
    public void wifiCheckerDeterminesIfUp_downWhenNotCompleted() {
        when(mockContext.getSystemService(Context.WIFI_SERVICE)).thenReturn(mockWifiManager);
        when(mockWifiManager.getConnectionInfo()).thenReturn(mockConnectionInfo);
        when(mockConnectionInfo.getSupplicantState()).thenReturn(SupplicantState.INACTIVE);
        WifiChecker wc = new WifiChecker();

        assertEquals(false, wc.wifiConnectedAndEnabled(mockContext));
    }



    @Mock
    SmsManager mockSmsManager = mock(SmsManager.class);
    @Test
    public void sendsResponseSMS() {
        FakeManagerSmsResponder lr = new FakeManagerSmsResponder(mockSmsManager);

        lr.replyToQuery("src", "msg");

    }

    @Test
    public void sendsResponseSMSUsingMgrBlowsUp() {

        FakeManagerSmsResponder lr = new FakeManagerSmsResponder(null);

        try {
            lr.replyToQuery("src", "msg");
            assertEquals(true, false);
        } catch(NullPointerException npe) {
            // NOOP
        }
    }

    @Test
    public void serviceThingDoesStuff() {


        NonBGTextReceiver r = new NonBGTextReceiver();
        r.logger = new NullLogger();
        r.queryResponder = new FakeQueryResponder(true, "fake_reply");
        LoggingSmsResponder resp = new LoggingSmsResponder();
        r.smsResponder = resp;
        r.wifiChecker = new FakeWifiChecker(true);
        r.smsExtractor = new FakeSmsExtractor(mockMessage);

        r.bgProcessor.queryResponder = r.queryResponder;
        r.bgProcessor.smsResponder = resp;
        r.bgProcessor.logger = r.logger;

        when(mockMessage.getOriginatingAddress()).thenReturn("msg_address");


        r.onReceive(mockContext, mockIntent);
        assertEquals(1, resp.sentMessages.size());
    }

    Object [] createSMSRawData() {
        Object[] objs = new Object [1];
        objs[0] = "TESTTEXTPDUBLAHBLAH".getBytes();
        return objs;
    }

}