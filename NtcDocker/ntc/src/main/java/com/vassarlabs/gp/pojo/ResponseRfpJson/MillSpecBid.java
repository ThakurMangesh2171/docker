package com.vassarlabs.gp.pojo.ResponseRfpJson;

import lombok.Data;

@Data
public class MillSpecBid {

    private String mill = "";

    private Long bid_vol = 0L;

    public String getMill() {
        return mill;
    }

    public void setMill(String mill) {
        this.mill = mill;
    }

    public Long getBid_vol() {
        return bid_vol;
    }

    public void setBid_vol(Long bid_vol) {
        this.bid_vol = bid_vol;
    }
}
