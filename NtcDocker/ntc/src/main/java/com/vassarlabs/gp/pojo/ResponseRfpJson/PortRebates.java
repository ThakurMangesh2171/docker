package com.vassarlabs.gp.pojo.ResponseRfpJson;


import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PortRebates  implements Cloneable{
//    private Long discount_val = 0L;

    private List<Ports> ports = new ArrayList<>();
    private String discount_uom = "";
    private String comments = "";

    public List<Ports> getPorts() {
        return ports;
    }

    public void setPorts(List<Ports> ports) {
        this.ports = ports;
    }

//    public Long getDiscount_val() {
//        return discount_val;
//    }
//
//    public void setDiscount_val(Long discount_val) {
//        this.discount_val = discount_val;
//    }

    public String getDiscount_uom() {
        return discount_uom;
    }

    public void setDiscount_uom(String discount_uom) {
        this.discount_uom = discount_uom;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Object clone() throws CloneNotSupportedException
    {
        PortRebates clonedObj = (PortRebates) super.clone();
        // Create a deep copy of the ports Map
        clonedObj.ports = new ArrayList<>(ports);
        return clonedObj;
    }
}
