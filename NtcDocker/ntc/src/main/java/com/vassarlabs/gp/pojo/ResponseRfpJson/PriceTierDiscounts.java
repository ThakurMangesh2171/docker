package com.vassarlabs.gp.pojo.ResponseRfpJson;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
@Data
public class PriceTierDiscounts {

    private boolean is_tier_based_discount;

    private String tier_uom = "";

    private List<PriceTiers> price_tiers = new ArrayList<>();

    private String comments = "";


    public boolean isIs_tier_based_discount() {
        return is_tier_based_discount;
    }

    public void setIs_tier_based_discount(boolean is_tier_based_discount) {
        this.is_tier_based_discount = is_tier_based_discount;
    }

    public String getTier_uom() {
        return tier_uom;
    }

    public void setTier_uom(String tier_uom) {
        this.tier_uom = tier_uom;
    }

    public List<PriceTiers> getPrice_tiers() {
        return price_tiers;
    }

    public void setPrice_tiers(List<PriceTiers> price_tiers) {
        this.price_tiers = price_tiers;
    }
}
