package com.vassarlabs.gp.pojo.ResponseRfpJson;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class FreightDetail {

    private List<PortEntryDetails> port_entry_details = new ArrayList<>();

    private List<InlandFreight> inland_freight = new ArrayList<>();
    private String comments = "";

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public List<PortEntryDetails> getPort_entry_details() {
        return port_entry_details;
    }

    public void setPort_entry_details(List<PortEntryDetails> port_entry_details) {
        this.port_entry_details = port_entry_details;
    }

    public List<InlandFreight> getInland_freight() {
        return inland_freight;
    }

    public void setInland_freight(List<InlandFreight> inland_freight) {
        this.inland_freight = inland_freight;
    }
}
