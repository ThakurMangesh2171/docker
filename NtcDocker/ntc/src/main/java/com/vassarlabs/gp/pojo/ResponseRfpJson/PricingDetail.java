package com.vassarlabs.gp.pojo.ResponseRfpJson;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PricingDetail implements Cloneable{

    private String mechanism_basis = "";


    private Boolean is_movement_based;

    private Float initial_price = 0f;

    private Long time_window = 0L;

    private String time_window_period = "";

    private String volume_based_period = "";

    private Float additional_discount = 0f;

    private String discount_uom = "";

    private String price_uom = "";

    private List<PeriodDetail> period_detail = new ArrayList<>();

    private String pricing_alternate_mechanism = "";


    private DiscountsAllowances discounts_allowances;

    private String payment_term ="";

    private Long price_ceil;

    private Long price_floor;

    private String ceil_floor_uom="";

    private String ceil_floor_period_start;

    private String ceil_floor_period_end;

    private String comments="";

    private String movement_change_type="";

    //TODO :default vale for boolean
    private boolean monthly_negotiation;

    public String getMechanism_basis() {
        return mechanism_basis;
    }

    public void setMechanism_basis(String mechanism_basis) {
        this.mechanism_basis = mechanism_basis;
    }

    public Boolean getIs_movement_based() {
        return is_movement_based;
    }

    public void setIs_movement_based(Boolean is_movement_based) {
        this.is_movement_based = is_movement_based;
    }

    public Float getInitial_price() {
        return initial_price;
    }

    public void setInitial_price(Float initial_price) {
        this.initial_price = initial_price;
    }

    public Long getTime_window() {
        return time_window;
    }

    public void setTime_window(Long time_window) {
        this.time_window = time_window;
    }

    public String getTime_window_period() {
        return time_window_period;
    }

    public void setTime_window_period(String time_window_period) {
        this.time_window_period = time_window_period;
    }

    public String getVolume_based_period() {
        return volume_based_period;
    }

    public void setVolume_based_period(String volume_based_period) {
        this.volume_based_period = volume_based_period;
    }

    public Float getAdditional_discount() {
        return additional_discount;
    }

    public void setAdditional_discount(Float additional_discount) {
        this.additional_discount = additional_discount;
    }

    public String getDiscount_uom() {
        return discount_uom;
    }

    public void setDiscount_uom(String discount_uom) {
        this.discount_uom = discount_uom;
    }

    public String getPrice_uom() {
        return price_uom;
    }

    public void setPrice_uom(String price_uom) {
        this.price_uom = price_uom;
    }

    public List<PeriodDetail> getPeriod_detail() {
        return period_detail;
    }

    public void setPeriod_detail(List<PeriodDetail> period_detail) {
        this.period_detail = period_detail;
    }

    public String getPricing_alternate_mechanism() {
        return pricing_alternate_mechanism;
    }

    public void setPricing_alternate_mechanism(String pricing_alternate_mechanism) {
        this.pricing_alternate_mechanism = pricing_alternate_mechanism;
    }

    public DiscountsAllowances getDiscounts_allowances() {
        return discounts_allowances;
    }

    public void setDiscounts_allowances(DiscountsAllowances discounts_allowances) {
        this.discounts_allowances = discounts_allowances;
    }

    public String getPayment_term() {
        return payment_term;
    }

    public void setPayment_term(String payment_term) {
        this.payment_term = payment_term;
    }

    public Long getPrice_ceil() {
        return price_ceil;
    }

    public void setPrice_ceil(Long price_ceil) {
        this.price_ceil = price_ceil;
    }

    public Long getPrice_floor() {
        return price_floor;
    }

    public void setPrice_floor(Long price_floor) {
        this.price_floor = price_floor;
    }

    public String getCeil_floor_uom() {
        return ceil_floor_uom;
    }

    public void setCeil_floor_uom(String ceil_floor_uom) {
        this.ceil_floor_uom = ceil_floor_uom;
    }

    public String getCeil_floor_period_start() {
        return ceil_floor_period_start;
    }

    public void setCeil_floor_period_start(String ceil_floor_period_start) {
        this.ceil_floor_period_start = ceil_floor_period_start;
    }

    public String getCeil_floor_period_end() {
        return ceil_floor_period_end;
    }

    public void setCeil_floor_period_end(String ceil_floor_period_end) {
        this.ceil_floor_period_end = ceil_floor_period_end;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getMovement_change_type() {
        return movement_change_type;
    }

    public void setMovement_change_type(String movement_change_type) {
        this.movement_change_type = movement_change_type;
    }

    public boolean isMonthly_negotiation() {
        return monthly_negotiation;
    }

    public void setMonthly_negotiation(boolean monthly_negotiation) {
        this.monthly_negotiation = monthly_negotiation;
    }

    public Object clone() throws CloneNotSupportedException
    {
        PricingDetail clonedObj = (PricingDetail) super.clone();
        // Create a deep copy of DiscountsAllowances
        clonedObj.discounts_allowances = (DiscountsAllowances) discounts_allowances.clone();
        return clonedObj;
    }
}
