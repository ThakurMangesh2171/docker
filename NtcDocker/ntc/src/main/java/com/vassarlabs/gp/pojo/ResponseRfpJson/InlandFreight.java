package com.vassarlabs.gp.pojo.ResponseRfpJson;

import lombok.Data;

@Data
public class InlandFreight {

    private String gp_mill = "";
    private String source_type = "";

    private String source_name = "";

    private String inland_trans_route = "";

    private String dest_type = "";

    private String dest_name = "";

    private Location dest_location;

    private String transit_mode = "";

    private Float transit_cost = 0f;

    private String cost_uom = "";

    private Long transit_leadtime_in_days_port_entry_gp_mill = 0L;

    private String port_entry = "";

    public String getGp_mill() {
        return gp_mill;
    }

    public void setGp_mill(String gp_mill) {
        this.gp_mill = gp_mill;
    }

    public String getSource_type() {
        return source_type;
    }

    public void setSource_type(String source_type) {
        this.source_type = source_type;
    }

    public String getSource_name() {
        return source_name;
    }

    public void setSource_name(String source_name) {
        this.source_name = source_name;
    }

    public String getInland_trans_route() {
        return inland_trans_route;
    }

    public void setInland_trans_route(String inland_trans_route) {
        this.inland_trans_route = inland_trans_route;
    }

    public String getDest_type() {
        return dest_type;
    }

    public void setDest_type(String dest_type) {
        this.dest_type = dest_type;
    }

    public String getDest_name() {
        return dest_name;
    }

    public void setDest_name(String dest_name) {
        this.dest_name = dest_name;
    }

    public Location getDest_location() {
        return dest_location;
    }

    public void setDest_location(Location dest_location) {
        this.dest_location = dest_location;
    }

    public String getTransit_mode() {
        return transit_mode;
    }

    public void setTransit_mode(String transit_mode) {
        this.transit_mode = transit_mode;
    }

    public Float getTransit_cost() {
        return transit_cost;
    }

    public void setTransit_cost(Float transit_cost) {
        this.transit_cost = transit_cost;
    }

    public String getCost_uom() {
        return cost_uom;
    }

    public void setCost_uom(String cost_uom) {
        this.cost_uom = cost_uom;
    }

    public Long getTransit_leadtime_in_days_port_entry_gp_mill() {
        return transit_leadtime_in_days_port_entry_gp_mill;
    }

    public void setTransit_leadtime_in_days_port_entry_gp_mill(Long transit_leadtime_in_days_port_entry_gp_mill) {
        this.transit_leadtime_in_days_port_entry_gp_mill = transit_leadtime_in_days_port_entry_gp_mill;
    }

    public String getPort_entry() {
        return port_entry;
    }

    public void setPort_entry(String port_entry) {
        this.port_entry = port_entry;
    }

}
