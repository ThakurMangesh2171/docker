package com.vassarlabs.gp.pojo.ResponseRfpJson;

import lombok.Data;

@Data
public class CommercialPricingParsingRows {
    private  int pricingMechanismRows;
    private  int pricingTierDiscountRows;
    private  int volumeTierDiscountRows;
    private String periodType ;
    private String sheetName;
    private String readDate;
    private String readType;
    private String weekDay;
}
