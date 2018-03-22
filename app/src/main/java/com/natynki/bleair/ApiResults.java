package com.natynki.bleair;

import android.support.annotation.NonNull;

import org.json.JSONObject;

public class ApiResults implements Comparable<ApiResults> {
    public long created_at;
    public JSONObject context;
    public float time;
    float timestamp;
    float value;

    public int compareTo(@NonNull ApiResults compareResult) {
        return Float.compare(this.value, compareResult.value);
    }
}
