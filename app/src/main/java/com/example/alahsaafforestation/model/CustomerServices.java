package com.example.alahsaafforestation.model;

public class CustomerServices {

    private int customer_id;
    private String customerName;
    private VoluntaryService service;

    public CustomerServices(int id, String customerName, VoluntaryService service) {
        this.customer_id = id;
        this.customerName = customerName;
        this.service = service;
    }

    public CustomerServices(String customerName, VoluntaryService service) {
        this.customerName = customerName;
        this.service = service;
    }

    public int getCustomer_id() {
        return customer_id;
    }

    public void setCustomer_id(int customer_id) {
        this.customer_id = customer_id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public VoluntaryService getService() {
        return service;
    }

    public void setService(VoluntaryService service) {
        this.service = service;
    }

}
