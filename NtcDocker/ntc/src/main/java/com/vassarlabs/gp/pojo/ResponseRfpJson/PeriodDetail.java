package com.vassarlabs.gp.pojo.ResponseRfpJson;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PeriodDetail {

    private Long period_num = 0L;

    private String period = "";

    private String period_type ="";

    private FixedPriceDetails fixed_price_details;

    private List<IndexDetails> index_details = new ArrayList<>();

    public Long getPeriod_num() {
        return period_num;
    }

    public void setPeriod_num(Long period_num) {
        this.period_num = period_num;
    }
    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public String getPeriod_type() {
        return period_type;
    }

    public void setPeriod_type(String period_type) {
        this.period_type = period_type;
    }

    public FixedPriceDetails getFixed_price_details() {
        return fixed_price_details;
    }

    public void setFixed_price_details(FixedPriceDetails fixed_price_details) {
        this.fixed_price_details = fixed_price_details;
    }

    public List<IndexDetails> getIndex_details() {
        return index_details;
    }

    public void setIndex_details(List<IndexDetails> index_details) {
        this.index_details = index_details;
    }
}
