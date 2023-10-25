package com.vassarlabs.gp.pojo.ResponseRfpJson;

import com.vassarlabs.gp.constants.Constants;
import lombok.Data;

import java.util.concurrent.ConcurrentSkipListMap;

@Data
public class Location {

    private String addr_line1 = "";

    private String addr_line2 = "";

    private String city = "";

    private String state = "";

    private String country = "";

    private String pin_code = "";

    @Override
    public String toString() {
        return addr_line1 + Constants.COMMA + Constants.SPACE + addr_line2 + Constants.COMMA + Constants.SPACE + city + Constants.COMMA + Constants.SPACE + state + country + Constants.COMMA + Constants.SPACE + pin_code;
    }
}
