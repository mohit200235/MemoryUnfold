package com.example.memoriesunfold.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Arrays;

public class DataMemoryModel implements Parcelable {

    int id;
    int memory_id;
    byte[] image;
    String Date;
    String Description;

    public DataMemoryModel(int id,int memory_id,byte[] image, String date, String description) {
        this.id =id;
        this.memory_id = memory_id;
        this.image = image;
        Date = date;
        Description = description;
    }

    public DataMemoryModel(int memory_id, byte[] image, String date, String description) {
        this.memory_id = memory_id;
        this.image = image;
        Date = date;
        Description = description;
    }

    protected DataMemoryModel(Parcel in) {
        image = in.createByteArray();
        Date = in.readString();
        Description = in.readString();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMemory_id() {
        return memory_id;
    }

    public void setMemory_id(int memory_id) {
        this.memory_id = memory_id;
    }

    public static final Creator<DataMemoryModel> CREATOR = new Creator<DataMemoryModel>() {
        @Override
        public DataMemoryModel createFromParcel(Parcel in) {
            return new DataMemoryModel(in);
        }

        @Override
        public DataMemoryModel[] newArray(int size) {
            return new DataMemoryModel[size];
        }
    };

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeByteArray(image);
        parcel.writeString(Date);
        parcel.writeString(Description);
    }

    @Override
    public String toString() {
        return "DataMemoryModel{" +
                "id=" + id +
                ", memory_id=" + memory_id +
                ", image=" + image +
                ", Date='" + Date + '\'' +
                ", Description='" + Description + '\'' +
                '}';
    }
}
