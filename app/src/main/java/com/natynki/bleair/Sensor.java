package com.natynki.bleair;

import android.os.Parcel;
import android.os.ParcelUuid;
import android.os.Parcelable;

import com.idevicesinc.sweetblue.utils.Uuids;

import java.util.UUID;

public class Sensor implements Parcelable {
    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Sensor> CREATOR = new Parcelable.Creator<Sensor>() {
        @Override
        public Sensor createFromParcel(Parcel in) {
            return new Sensor(in);
        }

        @Override
        public Sensor[] newArray(int size) {
            return new Sensor[size];
        }
    };
    private final String name;
    private final String suure;
    private final ParcelUuid uuid;
    private final ParcelUuid s_uuid;
    private String data;

    Sensor(String name, String suure, int position) {
        String t = "00000000-0000-1000-8000-00805f9b34fb";
        this.name = name;
        this.suure = suure;
        this.data = "";
        this.s_uuid = ParcelUuid.fromString(Uuids.fromShort("aaa"+Integer.toString(position), t).toString());
        this.uuid = ParcelUuid.fromString(Uuids.fromShort("bbb"+Integer.toString(position), t).toString());
    }

    private Sensor(Parcel in) {
        name = in.readString();
        suure = in.readString();
        data = in.readString();
        uuid = (ParcelUuid) in.readValue(ParcelUuid.class.getClassLoader());
        s_uuid = (ParcelUuid) in.readValue(ParcelUuid.class.getClassLoader());
    }

    public String getName() {
        return name;
    }

    String getSuure() {
        return suure;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    UUID getUuid() {
        return uuid.getUuid();
    }

    UUID getS_uuid() {
        return s_uuid.getUuid();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(suure);
        dest.writeString(data);
        dest.writeValue(uuid);
        dest.writeValue(s_uuid);
    }
}