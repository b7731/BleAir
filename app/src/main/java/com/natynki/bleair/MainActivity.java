package com.natynki.bleair;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.idevicesinc.sweetblue.BleDevice;
import com.idevicesinc.sweetblue.BleDeviceState;
import com.idevicesinc.sweetblue.BleManager;
import com.idevicesinc.sweetblue.BleManagerConfig;
import com.idevicesinc.sweetblue.NotificationListener;
import com.idevicesinc.sweetblue.utils.BluetoothEnabler;
import com.idevicesinc.sweetblue.utils.Interval;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    public static final String apiUrl = "https://things.ubidots.com/api/v1.6/devices/ArduinoAir";  // This is the API base URL (GitHub API)
    private static final String device_name = "jnair";
    private static final String[][] sensorsNames = {{"Temperature", "C"}, {"Humitidy", "%"}};
    private static final String TAG = MainActivity.class.getSimpleName();
    private final List<Sensor> sensorList = new ArrayList<>();
    private final Handler timerHandler = new Handler();
    private final BleManagerConfig.ScanFilter scanFilter = new BleManagerConfig.ScanFilter() {
        @Override
        public BleManagerConfig.ScanFilter.Please onEvent(ScanEvent e) {
            return Please.acknowledgeIf(e.name_normalized().contains(device_name)).thenStopScan();
        }
    };
    private RecyclerView recyclerView;
    private sensorListAdapter mAdapter;
    private BleManager m_bleManager;
    private BleDevice m_device;
    private Button m_connect;
    private Button m_disconnect;
    private TextView m_name;
    private TextView m_state;
    private RequestQueue mRequestQueue;
    private TextView m_api_answer;
    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (m_disconnect.isEnabled() && (!sensorList.get(0).getData().equals(""))) {
                sendDataToApi();
            }
            timerHandler.postDelayed(this, 30 * 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        m_connect = findViewById(R.id.connect);
        m_disconnect = findViewById(R.id.disconnect);
        m_name = findViewById(R.id.name);
        m_state = findViewById(R.id.state);
        recyclerView = findViewById(R.id.recycler_view);
        mAdapter = new sensorListAdapter(sensorList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        m_api_answer = findViewById(R.id.api_answer);  // Link our repository list text output box.
        m_api_answer.setMovementMethod(new ScrollingMovementMethod());

        recyclerView.addOnItemTouchListener(new RecyclerViewTouchListener(getApplicationContext(), new RecyclerViewClickListener() {
            @Override
            public void onClick(int position) {
                Intent intent = new Intent(MainActivity.this, SensorActivity.class);
                intent.putExtra("Sensor", sensorList.get(position));
                startActivity(intent);
            }
        }));

        recyclerView.setAdapter(mAdapter);

        prepareSensorListData();

        setConnectButton();

        setDisconnectButton();

        startScan();
    }

    @Override
    public void onPause() {
        super.onPause();
        timerHandler.removeCallbacks(timerRunnable);
    }

    private RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }
        return mRequestQueue;
    }

    private <T> void addToRequestQueue(Request<T> req, String tag) {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    private void sendDataToApi() {
        Log.d(TAG, "API");
        JSONObject json = new JSONObject();
        try {
            for (final Sensor sens : sensorList) {
                json.put(sens.getName(), sens.getData());

            }
        } catch (JSONException e) {
            VolleyLog.d(TAG, "Error: " + e.getMessage());
        }
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                apiUrl, json,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, response.toString());
                        m_api_answer.setText("Connected to Ubidots-Api");
                        m_api_answer.setTextColor(R.color.name);
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

    private void setConnectButton() {
        m_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                m_connect.setEnabled(false);
                connectToDevice();
            }
        });
    }

    private void setDisconnectButton() {
        m_disconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                m_device.disconnect();
            }
        });
    }

    private void startScan() {
        BleManagerConfig config = new BleManagerConfig();
        config.loggingEnabled = BuildConfig.DEBUG;
        config.scanReportDelay = Interval.DISABLED;

        m_bleManager = BleManager.get(this, config);
        m_bleManager.setListener_Discovery(new SimpleDiscoveryListener());

        BluetoothEnabler.start(this, new BluetoothEnabler.DefaultBluetoothEnablerFilter() {
            @Override
            public Please onEvent(BluetoothEnablerEvent bluetoothEnablerEvent) {
                Please please = super.onEvent(bluetoothEnablerEvent);

                if (bluetoothEnablerEvent.isDone()) {
                    m_bleManager.startScan(scanFilter);
                }
                return please;
            }
        });
    }

    @SuppressWarnings("deprecation")
    private void connectToDevice() {
        m_device.connect(new BleDevice.StateListener() {
            @Override
            public void onEvent(final StateEvent stateEvent) {
                m_state.setText(stateEvent.device().printState());

                // Check if the device entered the INITIALIZED state (this is the "true" connected state where the device is ready to be operated upon).
                if (stateEvent.didEnter(BleDeviceState.INITIALIZED)) {
                    m_disconnect.setEnabled(true);
                    for (final Sensor sens : sensorList) {
                        readDataCharacteristic(stateEvent.device(), sens);
                        m_device.enableNotify(sens.getUuid());
                    }

                    m_device.setListener_Notification(new NotificationListener() {
                        @Override
                        public void onEvent(NotificationEvent e) {
                            if (e.type() == Type.NOTIFICATION) {
                                for (final Sensor sens : sensorList) {
                                    if (e.charUuid() == sens.getUuid()) {
                                        readDataCharacteristic(stateEvent.device(), sens);
                                        mAdapter.notifyDataSetChanged();
                                    }
                                }
                            }
                        }
                    });

                    timerHandler.postDelayed(timerRunnable, 10 * 1000);
                }
                if (stateEvent.didEnter(BleDeviceState.DISCONNECTED) && !m_device.is(BleDeviceState.RETRYING_BLE_CONNECTION)) {
                    // If the device got disconnected, and SweetBlue isn't retrying, then we disable the disconnect button, and enable the connect button.
                    m_connect.setEnabled(true);
                    m_disconnect.setEnabled(false);
                }
            }
        }, new BleDevice.DefaultConnectionFailListener() {
        });
    }

    private void prepareSensorListData() {
        for (int i = 0; i < sensorsNames.length; i++) {
            sensorList.add(new Sensor(sensorsNames[i][0], sensorsNames[i][1], i));
        }
    }

    @SuppressWarnings("deprecation")
    private void readDataCharacteristic(BleDevice device, final Sensor sens) {
        device.read(sens.getS_uuid(), sens.getUuid(), new BleDevice.ReadWriteListener() {
            @Override
            public void onEvent(ReadWriteEvent readWriteEvent) {
                if (readWriteEvent.wasSuccess()) {
                    String txt = String.format("%s", readWriteEvent.data_long(true));
                    sens.setData(txt);
                    recyclerView.setVisibility(View.VISIBLE);
                    mAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    private final class SimpleDiscoveryListener implements BleManager.DiscoveryListener {
        private boolean m_discovered = false;

        @Override
        public void onEvent(DiscoveryEvent discoveryEvent) {
            if (!m_discovered) {
                m_discovered = true;

                m_bleManager.stopScan();

                if (discoveryEvent.was(LifeCycle.DISCOVERED)) {
                    // Grab the device from the DiscoveryEvent instance.
                    m_device = discoveryEvent.device();
                    m_name.setText(m_device.getName_debug());
                    m_name.setTextColor(getResources().getColor(R.color.name));
                    m_state.setText(m_device.printState());
                    m_state.setTextColor(getResources().getColor(R.color.name));
                    connectToDevice();
                }
            }
        }
    }
}