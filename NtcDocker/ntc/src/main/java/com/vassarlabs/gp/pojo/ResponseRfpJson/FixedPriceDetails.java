package com.vassarlabs.gp.pojo.ResponseRfpJson;

import lombok.Data;

@Data
public class FixedPriceDetails {

    private Float fixed_price_value = 0f;

    private Long weightage_pct = 0L;
}
