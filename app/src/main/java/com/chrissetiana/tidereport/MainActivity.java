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

    private static final String REQUEST_URL = "http://earthquake.usgs.gov/fdsnws/event/1/query?format=geojson&starttime=2012-01-01&endtime=2012-12-01&minmagnitude=6";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TsunamiAsyncTask task = new TsunamiAsyncTask();
        task.execute(REQUEST_URL);
    }

    private void setViews(Tsunami tsunami) {
        TextView titleView = findViewById(R.id.tsunami_title);
        titleView.setText(tsunami.title);

        TextView dateView = findViewById(R.id.tsunami_date);
        dateView.setText(getDate(tsunami.time));

        TextView alertView = findViewById(R.id.tsunami_alert);
        alertView.setText(getAlert(tsunami.alert));
    }

    private class TsunamiAsyncTask extends AsyncTask<String, Void, Tsunami> {

        protected Tsunami doInBackground(String... urls) {
            if (urls.length < 1 || urls[0] == null) {
                return null;
            }

            Tsunami result = QueryUtils.fetchEarthquakeData(urls[0]);
            return result;
        }

        protected void onPostExecute(Tsunami result) {
            if (result == null) {
                return;
            }

            setViews(result);
        }
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
}
