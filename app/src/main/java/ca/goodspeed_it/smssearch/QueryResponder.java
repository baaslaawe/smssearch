package ca.goodspeed_it.smssearch;


import java.net.*;
import java.io.*;
import org.json.*;
import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;
/**
 * Created by bg on 2016-03-11.
 */

public class QueryResponder {
    public static final String SEARCH = "search:";
    public static final String QUERY_BASE = "https://www.googleapis.com/customsearch/v1?";
    public static final String CSE_ID = "FILL IN WITH REAL VALUE FROM CREATED ENGINE";
    public static final String CSE_API_KEY = "FILL IN WITH REAL VALUE FROM DEVELOPER CONSOLE";
    public static final String API_TOPLEVEL = "items";
    public static final String API_TEXTKEY = "snippet";

    public boolean respondsTo(String body) {
        return body != null && body.toLowerCase().startsWith(SEARCH);

    }

    public String performHTTPRequest(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        InputStream in = new BufferedInputStream(urlConnection.getInputStream());

        return convertStreamToString(in);

    }
    // https://developers.google.com/api-client-library/java/google-api-java-client/setup#eclipse
    // https://cse.google.com/cse/setup/basic?cx=018435981732196364506%3A3ilugu2hbgo
    public String responseFor(String body) {
        URL url = null;
        HttpURLConnection urlConnection = null;
        String queryResponse = "Could not find any info";
        try {
            String baseQuery = body.split(":")[1];
            String queryTerm = URLEncoder.encode(baseQuery.trim(), "UTF-8");
            url = new URL(QUERY_BASE + "&cx=" + CSE_ID + "&key=" + CSE_API_KEY + "&q=" + queryTerm);
            String jsonResponse = performHTTPRequest(url);
            queryResponse = extractResponseFromJson(jsonResponse);

        } catch (MalformedURLException mue) {
            queryResponse = "Error: bad url" + mue.getMessage();
        } catch (IOException e) {
            queryResponse = "Error: io issue:" + e.getMessage();
        } catch (JSONException e) {
            queryResponse = "Error: json issue:" + e.getMessage();
        } finally {
            if (urlConnection != null) urlConnection.disconnect();
        }

        return queryResponse;

    }

    public JSONObject forJsonText(String jsonResponse) throws JSONException {
        return new JSONObject(jsonResponse);
    }
    public String extractResponseFromJson(String jsonResponse) throws JSONException {
        JSONObject obj = forJsonText(jsonResponse);

        JSONArray arr = obj.getJSONArray(API_TOPLEVEL);
        String firstResult = arr.getJSONObject(0).getString(API_TEXTKEY);
        return firstResult;

    }

    static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

}
