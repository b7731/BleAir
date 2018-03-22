package com.natynki.bleair;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SensorActivity extends AppCompatActivity {
    private static final String TAG = SensorActivity.class.getSimpleName();
    private RequestQueue sRequestQueue;
    private TextView m_name;
    private ApiResults[] aDetails;
    private LineChart chart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);
        m_name = findViewById(R.id.name);

        Intent intent = getIntent();
        Sensor sensor = intent.getParcelableExtra("Sensor");
        m_name.setText(sensor.getName());

        getValues(sensor.getName());
        chart = findViewById(R.id.chart);
    }

    private RequestQueue getRequestQueue() {
        if (sRequestQueue == null) {
            sRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }
        return sRequestQueue;
    }

    private <T> void addToRequestQueue(Request<T> req, String tag) {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    private void getValues(String name) {
        String url = MainActivity.apiUrl + "/" + name + "/values";
        JSONObject json = new JSONObject();
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                url, json,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        //   Log.d(TAG, response.toString());
                        parseJson(response.toString());
                        List<Entry> entries = new ArrayList<>();

                        for (int i = 0; i < aDetails.length; i++) {
                            entries.add(new Entry(i, aDetails[i].value));
                        }

                        LineDataSet dataSet = new LineDataSet(entries, (String) m_name.getText()); // add entries to dataset
                        dataSet.setColor(R.color.name);
                        dataSet.setValueTextColor(R.color.colorAccent);

                        LineData lineData = new LineData(dataSet);
                        chart.setData(lineData);
                        chart.getDescription().setText("API data for sensor");
                        chart.invalidate(); // refresh
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("X-Auth-Token", BuildConfig.API_KEY);
                return headers;
            }
        };
        String tag_json_obj = "json_obj_req";
        addToRequestQueue(jsonObjReq, tag_json_obj);
    }

    private void parseJson(String mJsonString) {
        if (mJsonString != null) {
            Gson gson = new Gson();
            ApiResponse aresponse = gson.fromJson(mJsonString, ApiResponse.class);
            aDetails = aresponse.results;
            float start = aDetails[aDetails.length - 1].timestamp;

            for (ApiResults aDetail : aDetails) {
                aDetail.time = aDetail.timestamp - start;
            }

            Arrays.sort(aDetails);
        }
    }
}
