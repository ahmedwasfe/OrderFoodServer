package com.ahmet.orderfoodserver.Model;

import java.util.List;

public class Request {

    private String phone;
    private String name;
    private String address;
    private String total;
    private String status;
    // List of food order
    private List<Order> mListOrder;

    public Request() {
    }

    public Request(String phone, String name, String address, String total, List<Order> mListOrder) {
        this.phone = phone;
        this.name = name;
        this.address = address;
        this.total = total;
        this.mListOrder = mListOrder;
        // Default is 0, 0: Placed, 1: Shipping, 2: Shipped
        this.status = "0";
    }

    public String getPhone() {
        return phone;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTotal() {
        return total;
    }

    public List<Order> getmListOrder() {
        return mListOrder;
    }

    public void setmListOrder(List<Order> mListOrder) {
        this.mListOrder = mListOrder;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
