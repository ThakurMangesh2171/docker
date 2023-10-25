package com.vassarlabs.gp.pojo.ResponseRfpJson;

import lombok.Data;

import java.util.List;

@Data
public class PortDiscounts {

    private String discount_uom;

    private List<Ports> ports;

}
