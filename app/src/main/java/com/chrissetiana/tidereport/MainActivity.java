package com.chrissetiana.tidereport;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;

public class MainActivity extends AppCompatActivity {

    public static final String LOG_TAG = MainActivity.class.getSimpleName();

    private static final String REQUEST_URL = "http://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson&starttime=2012-01-01&endtime=2012-12-01&minmagnitude=6";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TsunamiAsyncTask task = new TsunamiAsyncTask();
        task.execute();
    }

    private void setViews(Tsunami tsunami) {
        TextView titleView = findViewById(R.id.tsunami_title);
        titleView.setText(tsunami.title);

        TextView dateView = findViewById(R.id.tsunami_date);
        dateView.setText(getDate(tsunami.time));

        TextView alertView = findViewById(R.id.tsunami_alert);
        alertView.setText(getAlert(tsunami.alert));
    }

    private String getDate(long time) {
        return new SimpleDateFormat("EEE, d MMM yyyy 'at' HH:mm:ss z").format(time);
    }

    private String getAlert(int tsunamiAlert) {
        switch (tsunamiAlert) {
            case 0:
                return getString(R.string.alert_no);
            case 1:
                return getString(R.string.alert_yes);
            default:
                return getString(R.string.alert_not_available);
        }
    }

    private class TsunamiAsyncTask extends AsyncTask<URL, Void, Tsunami> {

        @Override
        protected Tsunami doInBackground(URL... urls) {
            String jsonResponse = "";
            URL url = createUrl(REQUEST_URL);

            try {
                jsonResponse = makeHttpRequest(url);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Problem making the HTTP request.", e);
            }

            Tsunami tsunami = getJSONData(jsonResponse);

            return tsunami;
        }

        @Override
        protected void onPostExecute(Tsunami tsunami) {
            if (tsunami == null) {
                return;
            }

            setViews(tsunami);
        }

        private URL createUrl(String stringUrl) {
            URL url = null;
            try {
                url = new URL(stringUrl);
            } catch (MalformedURLException exception) {
                Log.e(LOG_TAG, "Error with creating URL", exception);
                return null;
            }
            return url;
        }

        private String makeHttpRequest(URL url) throws IOException {
            String jsonResponse = "";

            if (url == null) {
                return jsonResponse;
            }

            HttpURLConnection urlConnection = null;
            InputStream inputStream = null;
            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setReadTimeout(10000 /* milliseconds */);
                urlConnection.setConnectTimeout(15000 /* milliseconds */);
                urlConnection.connect();

                if (urlConnection.getResponseCode() == 200) {
                    inputStream = urlConnection.getInputStream();
                    jsonResponse = readFromStream(inputStream);
                } else {
                    Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
                }
            } catch (IOException e) {
                Log.e(LOG_TAG, "Problem retrieving the earthquake JSON results.", e);
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (inputStream != null) {
                    // function must handle java.io.IOException here
                    inputStream.close();
                }
            }
            return jsonResponse;
        }

        private String readFromStream(InputStream inputStream) throws IOException {
            StringBuilder output = new StringBuilder();
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
                BufferedReader reader = new BufferedReader(inputStreamReader);
                String line = reader.readLine();
                while (line != null) {
                    output.append(line);
                    line = reader.readLine();
                }
            }
            return output.toString();
        }

        private Tsunami getJSONData(String tsunamiJSON) {
            if (TextUtils.isEmpty(tsunamiJSON)) {
                return null;
            }

            try {
                JSONObject jsonObject = new JSONObject(tsunamiJSON);
                JSONArray jsonArray = jsonObject.getJSONArray("features");

                if (jsonArray.length() > 0) {
                    JSONObject element = jsonArray.getJSONObject(0);
                    JSONObject properties = element.getJSONObject("properties");

                    String title = properties.getString("title");
                    long time = properties.getLong("time");
                    int alert = properties.getInt("tsunami");

                    return new Tsunami(title, time, alert);
                }
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Problem parsing the earthquake JSON results", e);
            }
            return null;
        }
    }
}
