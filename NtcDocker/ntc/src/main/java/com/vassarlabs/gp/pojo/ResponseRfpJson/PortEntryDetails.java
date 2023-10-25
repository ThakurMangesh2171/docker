package com.vassarlabs.gp.pojo.ResponseRfpJson;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PortEntryDetails {

    private String port_entry = "";

    private List<Incoterms> incoterms = new ArrayList<>();

    private String cost_uom = "";

    private Long port_free_time_in_days = 0L;

    private Long transit_leadtime_in_days_origin_port_port_entry = 0L;

    private String steamship_line = "";

    private Float ocean_freight = 0f;

    private Long safety_stock_nominated_in_days = 0L;

    private SafetyStockLocation safety_stock_location;

    private Float transit_cost_from_port_entry_to_safety_stock_loc = 0f;

    private String gp_mill = "";

    private String handled_by = "";

    public String getPort_entry() {
        return port_entry;
    }

    public void setPort_entry(String port_entry) {
        this.port_entry = port_entry;
    }

    public List<Incoterms> getIncoterms() {
        return incoterms;
    }

    public void setIncoterms(List<Incoterms> incoterms) {
        this.incoterms = incoterms;
    }

    public String getCost_uom() {
        return cost_uom;
    }

    public void setCost_uom(String cost_uom) {
        this.cost_uom = cost_uom;
    }

    public Long getPort_free_time_in_days() {
        return port_free_time_in_days;
    }

    public void setPort_free_time_in_days(Long port_free_time_in_days) {
        this.port_free_time_in_days = port_free_time_in_days;
    }

    public Long getTransit_leadtime_in_days_origin_port_port_entry() {
        return transit_leadtime_in_days_origin_port_port_entry;
    }

    public void setTransit_leadtime_in_days_origin_port_port_entry(Long transit_leadtime_in_days_origin_port_port_entry) {
        this.transit_leadtime_in_days_origin_port_port_entry = transit_leadtime_in_days_origin_port_port_entry;
    }

    public String getSteamship_line() {
        return steamship_line;
    }

    public void setSteamship_line(String steamship_line) {
        this.steamship_line = steamship_line;
    }

    public Float getOcean_freight() {
        return ocean_freight;
    }

    public void setOcean_freight(Float ocean_freight) {
        this.ocean_freight = ocean_freight;
    }

    public Long getSafety_stock_nominated_in_days() {
        return safety_stock_nominated_in_days;
    }

    public void setSafety_stock_nominated_in_days(Long safety_stock_nominated_in_days) {
        this.safety_stock_nominated_in_days = safety_stock_nominated_in_days;
    }

    public SafetyStockLocation getSafety_stock_location() {
        return safety_stock_location;
    }

    public void setSafety_stock_location(SafetyStockLocation safety_stock_location) {
        this.safety_stock_location = safety_stock_location;
    }

    public Float getTransit_cost_from_port_entry_to_safety_stock_loc() {
        return transit_cost_from_port_entry_to_safety_stock_loc;
    }

    public void setTransit_cost_from_port_entry_to_safety_stock_loc(Float transit_cost_from_port_entry_to_safety_stock_loc) {
        this.transit_cost_from_port_entry_to_safety_stock_loc = transit_cost_from_port_entry_to_safety_stock_loc;
    }

    public String getGp_mill() {
        return gp_mill;
    }

    public void setGp_mill(String gp_mill) {
        this.gp_mill = gp_mill;
    }

    public String getHandled_by() {
        return handled_by;
    }

    public void setHandled_by(String handled_by) {
        this.handled_by = handled_by;
    }
}
