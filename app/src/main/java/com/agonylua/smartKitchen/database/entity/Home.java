package com.agonylua.smartKitchen.database.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.agonylua.smartKitchen.database.DataConverter;

import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "home")
@TypeConverters(DataConverter.class)
public class Home implements Parcelable {
    public static final Creator<Device> CREATOR = new Creator<Device>() {
        @Override
        public Device createFromParcel(Parcel in) {
            return new Device(in);
        }

        @Override
        public Device[] newArray(int size) {
            return new Device[size];
        }
    };
    @PrimaryKey(autoGenerate = false)
    @NonNull
    @ColumnInfo(name = "homeId")
    private String homeId = "";
    @ColumnInfo(name = "homeName")
    private String homeName;
    @ColumnInfo(name = "ownerId")
    private String ownerId;
    @ColumnInfo(name = "memberIds")
    private List<String> memberIds = new ArrayList<>();

    public Home() {
    }

    @Ignore
    public Home(@NonNull String homeId, String homeName, String ownerId, List<String> memberIds) {
        this.homeId = homeId;
        this.homeName = homeName;
        this.ownerId = ownerId;
        this.memberIds = memberIds;
    }

    protected Home(Parcel in) {
        homeId = in.readString();
        homeName = in.readString();
        ownerId = in.readString();
        memberIds = in.createStringArrayList();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(homeId);
        parcel.writeString(homeName);
        parcel.writeString(ownerId);
        parcel.writeStringList(memberIds);
    }

    @NonNull
    public String getHomeId() {
        return homeId;
    }

    public void setHomeId(@NonNull String homeId) {
        this.homeId = homeId;
    }

    public String getHomeName() {
        return homeName;
    }

    public void setHomeName(String homeName) {
        this.homeName = homeName;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public List<String> getMemberIds() {
        return memberIds;
    }

    public void setMemberIds(List<String> memberIds) {
        this.memberIds = memberIds;
    }

}
