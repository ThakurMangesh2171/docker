package com.vassarlabs.gp.pojo.ResponseRfpJson;

import lombok.Data;

@Data
public class Incoterms {

    private String incoterm = "";

    private Float stevedoring_cost = 0f;

    private Float handling_cost = 0f;

    private Float wharfage_cost = 0f;

    private Float security_cost = 0f;

    private Float warehouse_cost_per_month = 0f;

    private Float customs_fee = 0f;

    public String getIncoterm() {
        return incoterm;
    }

    public void setIncoterm(String incoterm) {
        this.incoterm = incoterm;
    }

    public Float getStevedoring_cost() {
        return stevedoring_cost;
    }

    public void setStevedoring_cost(Float stevedoring_cost) {
        this.stevedoring_cost = stevedoring_cost;
    }

    public Float getHandling_cost() {
        return handling_cost;
    }

    public void setHandling_cost(Float handling_cost) {
        this.handling_cost = handling_cost;
    }

    public Float getWharfage_cost() {
        return wharfage_cost;
    }

    public void setWharfage_cost(Float wharfage_cost) {
        this.wharfage_cost = wharfage_cost;
    }

    public Float getSecurity_cost() {
        return security_cost;
    }

    public void setSecurity_cost(Float security_cost) {
        this.security_cost = security_cost;
    }

    public Float getWarehouse_cost_per_month() {
        return warehouse_cost_per_month;
    }

    public void setWarehouse_cost_per_month(Float warehouse_cost_per_month) {
        this.warehouse_cost_per_month = warehouse_cost_per_month;
    }

    public Float getCustoms_fee() {
        return customs_fee;
    }

    public void setCustoms_fee(Float customs_fee) {
        this.customs_fee = customs_fee;
    }
}
