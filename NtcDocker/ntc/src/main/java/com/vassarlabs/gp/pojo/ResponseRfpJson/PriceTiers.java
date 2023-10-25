package com.vassarlabs.gp.pojo.ResponseRfpJson;

import lombok.Data;

@Data
public class PriceTiers {

    private Long tier_low;

    private Long tier_high;

    private String discount_type = "";

    private Long  discount_val = 0L;

    private Float discount_pct = 0f;

    private String tier_label = "";


    public Long getTier_low() {
        return tier_low;
    }

    public void setTier_low(Long tier_low) {
        this.tier_low = tier_low;
    }

    public Long getTier_high() {
        return tier_high;
    }

    public void setTier_high(Long tier_high) {
        this.tier_high = tier_high;
    }

    public String getDiscount_type() {
        return discount_type;
    }

    public void setDiscount_type(String discount_type) {
        this.discount_type = discount_type;
    }

    public Long getDiscount_val() {
        return discount_val;
    }

    public void setDiscount_val(Long discount_val) {
        this.discount_val = discount_val;
    }

    public Float getDiscount_pct() {
        return discount_pct;
    }

    public void setDiscount_pct(Float discount_pct) {
        this.discount_pct = discount_pct;
    }

    public String getTier_label() {
        return tier_label;
    }

    public void setTier_label(String tier_label) {
        this.tier_label = tier_label;
    }

}
