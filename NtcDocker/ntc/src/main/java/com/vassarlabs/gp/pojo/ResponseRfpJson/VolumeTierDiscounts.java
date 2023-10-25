package com.vassarlabs.gp.pojo.ResponseRfpJson;

import lombok.Data;

import java.util.List;

@Data
public class VolumeTierDiscounts {

    private boolean is_volume_based_discount;

    private String tier_uom = "";

    private List<VolumeTiers> volume_tiers;

    private String comments = "";


    public boolean isIs_volume_based_discount() {
        return is_volume_based_discount;
    }

    public void setIs_volume_based_discount(boolean is_volume_based_discount) {
        this.is_volume_based_discount = is_volume_based_discount;
    }

    public String getTier_uom() {
        return tier_uom;
    }

    public void setTier_uom(String tier_uom) {
        this.tier_uom = tier_uom;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public List<VolumeTiers> getVolume_tiers() {
        return volume_tiers;
    }

    public void setVolume_tiers(List<VolumeTiers> volume_tiers) {
        this.volume_tiers = volume_tiers;
    }
}
