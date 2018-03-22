package com.natynki.bleair;

import com.google.gson.annotations.SerializedName;

class ApiResponse {
    @SerializedName("count")
    public boolean count;
    @SerializedName("previous")
    public String previous;
    @SerializedName("next")
    public String next;

    @SerializedName("results")
    ApiResults[] results;
}
