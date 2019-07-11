package com.ahmet.orderfoodserver.Model;


import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {

    private String name;
    private String password;
    private String phone;
    private String isStaff;

    public User() {
    }

    public User(String name, String password){
        this.name = name;
        this.password = password;
    }

    protected User(Parcel in) {
        phone = in.readString();
        name = in.readString();
        password = in.readString();
        isStaff = in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getIsStaff() {
        return isStaff;
    }

    public void setIsStaff(String isStaff) {
        this.isStaff = isStaff;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(phone);
        dest.writeString(name);
        dest.writeString(password);
        dest.writeString(isStaff);
    }


    @Override
    public int describeContents() {
        return 0;
    }
}
