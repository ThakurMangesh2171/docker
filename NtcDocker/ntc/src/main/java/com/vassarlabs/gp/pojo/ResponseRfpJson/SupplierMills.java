package com.vassarlabs.gp.pojo.ResponseRfpJson;

import lombok.Data;

@Data
public class SupplierMills {


    private String supplier_mill = "";

    private String origin_port = "";

    private String origin_cntry = "";

    private String fiber_type = "";

    private String environmental_certification = "";

    private String bale_packaging = "";

    private String bale_type = "";

    private boolean is_supplier_mill_domestic;

    private BidQtyDetail bid_qty_detail;

    private FreightDetail freight_detail;

    private PricingDetail pricing_detail;

    public boolean isIs_supplier_mill_domestic() {
        return is_supplier_mill_domestic;
    }

    public void setIs_supplier_mill_domestic(boolean is_supplier_mill_domestic) {
        this.is_supplier_mill_domestic = is_supplier_mill_domestic;
    }

    public String getSupplier_mill() {
        return supplier_mill;
    }

    public void setSupplier_mill(String supplier_mill) {
        this.supplier_mill = supplier_mill;
    }

    public String getOrigin_port() {
        return origin_port;
    }

    public void setOrigin_port(String origin_port) {
        this.origin_port = origin_port;
    }

    public String getOrigin_cntry() {
        return origin_cntry;
    }

    public void setOrigin_cntry(String origin_cntry) {
        this.origin_cntry = origin_cntry;
    }

    public String getFiber_type() {
        return fiber_type;
    }

    public void setFiber_type(String fiber_type) {
        this.fiber_type = fiber_type;
    }

    public String getEnvironmental_certification() {
        return environmental_certification;
    }

    public void setEnvironmental_certification(String environmental_certification) {
        this.environmental_certification = environmental_certification;
    }

    public String getBale_packaging() {
        return bale_packaging;
    }

    public void setBale_packaging(String bale_packaging) {
        this.bale_packaging = bale_packaging;
    }

    public String getBale_type() {
        return bale_type;
    }

    public void setBale_type(String bale_type) {
        this.bale_type = bale_type;
    }

    public BidQtyDetail getBid_qty_detail() {
        return bid_qty_detail;
    }

    public void setBid_qty_detail(BidQtyDetail bid_qty_detail) {
        this.bid_qty_detail = bid_qty_detail;
    }

    public FreightDetail getFreight_detail() {
        return freight_detail;
    }

    public void setFreight_detail(FreightDetail freight_detail) {
        this.freight_detail = freight_detail;
    }

    public PricingDetail getPricing_detail() {
        return pricing_detail;
    }

    public void setPricing_detail(PricingDetail pricing_detail) {
        this.pricing_detail = pricing_detail;
    }
}
