package com.vassarlabs.gp.pojo.ResponseRfpJson;

import lombok.Data;

@Data
public class DiscountsAllowances implements Cloneable {

    private PriceTierDiscounts price_tier_discounts;
    private VolumeTierDiscounts volume_tier_discounts;

//    private PortDiscounts port_discounts;

//    private InlandTransAllowances inland_trans_allowances;

//    private GoodwillDiscounts goodwill_discounts;

    private String alternate_rebate_criteria = "";

    private PortRebates port_rebates;

    public PriceTierDiscounts getPrice_tier_discounts() {
        return price_tier_discounts;
    }

    public void setPrice_tier_discounts(PriceTierDiscounts price_tier_discounts) {
        this.price_tier_discounts = price_tier_discounts;
    }

    public VolumeTierDiscounts getVolume_tier_discounts() {
        return volume_tier_discounts;
    }

    public void setVolume_tier_discounts(VolumeTierDiscounts volume_tier_discounts) {
        this.volume_tier_discounts = volume_tier_discounts;
    }

    public String getAlternate_rebate_criteria() {
        return alternate_rebate_criteria;
    }

    public void setAlternate_rebate_criteria(String alternate_rebate_criteria) {
        this.alternate_rebate_criteria = alternate_rebate_criteria;
    }

    public PortRebates getPort_rebates() {
        return port_rebates;
    }

    public void setPort_rebates(PortRebates port_rebates) {
        this.port_rebates = port_rebates;
    }

    public Object clone() throws CloneNotSupportedException
    {
        DiscountsAllowances clonedObj = (DiscountsAllowances) super.clone();
        // Create a deep copy of DiscountsAllowances
        clonedObj.port_rebates = (PortRebates) port_rebates.clone();
        return clonedObj;
    }
}
