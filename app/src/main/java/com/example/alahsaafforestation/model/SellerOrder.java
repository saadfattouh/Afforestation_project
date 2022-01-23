package com.example.alahsaafforestation.model;

public class SellerOrder {

    private int id;
    private String customerName;
    private int price;
    private int quantity;

    public SellerOrder(int id, String customerName, int price, int quantity) {
        this.id = id;
        this.customerName = customerName;
        this.price = price;
        this.quantity = quantity;
    }



    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public int getPrice() {
            return price;
        }

    public void setPrice(int price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

}
