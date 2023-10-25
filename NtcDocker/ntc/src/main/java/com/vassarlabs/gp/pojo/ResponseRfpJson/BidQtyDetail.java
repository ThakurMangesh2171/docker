package com.vassarlabs.gp.pojo.ResponseRfpJson;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BidQtyDetail {

    private String bid_type = "";

    private Long bid_vol = 0L;

    private Long bid_vol_variance_pct = 0L;

    private String qty_uom = "";

    private String period_start = "";

    private String period_end = "";

    private List<MillSpecBid> mill_spec_bid = new ArrayList<>();

    public BidQtyDetail(){}

    public BidQtyDetail(String bidType,String qtyUom, String periodStart, String periodEnd, Long bidVol, Long bidVolVariancePct, List<MillSpecBid> millSpecificBid) {
        this.bid_type = bidType;
        this.qty_uom = qtyUom;
        this.period_start = periodStart;
        this.period_end = periodEnd;
        this.bid_vol = bidVol;
        this.bid_vol_variance_pct = bidVolVariancePct;
        this.mill_spec_bid = millSpecificBid;
    }


    public BidQtyDetail(String bidType, String qtyUom, String periodStart, String periodEnd, Long bidVolVariancePct, List<MillSpecBid> millSpecificBid) {
        this.bid_type = bidType;
        this.qty_uom = qtyUom;
        this.period_start = periodStart;
        this.period_end = periodEnd;
        this.bid_vol_variance_pct = bidVolVariancePct;
        this.mill_spec_bid = millSpecificBid;
    }

    public String getBid_type() {
        return bid_type;
    }

    public void setBid_type(String bid_type) {
        this.bid_type = bid_type;
    }

    public Long getBid_vol() {
        return bid_vol;
    }

    public void setBid_vol(Long bid_vol) {
        this.bid_vol = bid_vol;
    }

    public Long getBid_vol_variance_pct() {
        return bid_vol_variance_pct;
    }

    public void setBid_vol_variance_pct(Long bid_vol_variance_pct) {
        this.bid_vol_variance_pct = bid_vol_variance_pct;
    }

    public String getQty_uom() {
        return qty_uom;
    }

    public void setQty_uom(String qty_uom) {
        this.qty_uom = qty_uom;
    }

    public String getPeriod_start() {
        return period_start;
    }

    public void setPeriod_start(String period_start) {
        this.period_start = period_start;
    }

    public String getPeriod_end() {
        return period_end;
    }

    public void setPeriod_end(String period_end) {
        this.period_end = period_end;
    }

    public List<MillSpecBid> getMill_spec_bid() {
        return mill_spec_bid;
    }

    public void setMill_spec_bid(List<MillSpecBid> mill_spec_bid) {
        this.mill_spec_bid = mill_spec_bid;
    }
}
