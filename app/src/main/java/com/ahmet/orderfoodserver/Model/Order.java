package com.ahmet.orderfoodserver.Model;

public class Order {

    private int id;

    private String productId;
    private String ProductName;
    private String quantity;
    private String price;
    private String discount;

    public Order() {}

    public Order(String productId, String productName, String quantity, String price, String discount) {
        this.productId = productId;
        ProductName = productName;
        this.quantity = quantity;
        this.price = price;
        this.discount = discount;
    }

    public Order(int id, String productId, String productName, String quantity, String price, String discount) {
        this.id = id;
        this.productId = productId;
        ProductName = productName;
        this.quantity = quantity;
        this.price = price;
        this.discount = discount;
    }

    public int getId() {
        return id;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return ProductName;
    }

    public void setProductName(String productName) {
        ProductName = productName;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getDiscount() {
        return discount;
    }

    public void setDiscount(String discount) {
        this.discount = discount;
    }
}
