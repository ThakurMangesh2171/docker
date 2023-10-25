package com.vassarlabs.gp.pojo.ResponseRfpJson;

import lombok.Data;

@Data
public class Ports {

    private String port;

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public Long getDiscount_val() {
        return discount_val;
    }

    public void setDiscount_val(Long discount_val) {
        this.discount_val = discount_val;
    }

    private Long discount_val;
}
