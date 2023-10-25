package com.vassarlabs.gp.pojo.ResponseRfpJson;

import lombok.Data;

@Data
public class IndexDetails {

    private String index = "";

    private String read_type = "";

    private String read_date = "";

    private String read_day = "";

    private String read_week_criteria = "";

    private Float discount_pct = 0f;

    private Float additional_adjustment = 0f;

    private Long weightage_pct = 0L;

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }


    public String getRead_type() {
        return read_type;
    }

    public void setRead_type(String read_type) {
        this.read_type = read_type;
    }

    public String getRead_date() {
        return read_date;
    }

    public void setRead_date(String read_date) {
        this.read_date = read_date;
    }

    public String getRead_day() {
        return read_day;
    }

    public void setRead_day(String read_day) {
        this.read_day = read_day;
    }
    public String getRead_week_criteria() {
        return read_week_criteria;
    }

    public void setRead_week_criteria(String read_week_criteria) {
        this.read_week_criteria = read_week_criteria;
    }
    public Float getDiscount_pct() {
        return discount_pct;
    }

    public void setDiscount_pct(Float discount_pct) {
        this.discount_pct = discount_pct;
    }
    public Float getAdditional_adjustment() {
        return additional_adjustment;
    }

    public void setAdditional_adjustment(Float additional_adjustment) {
        this.additional_adjustment = additional_adjustment;
    }
    public Long getWeightage_pct() {
        return weightage_pct;
    }

    public void setWeightage_pct(Long weightage_pct) {
        this.weightage_pct = weightage_pct;
    }

}


