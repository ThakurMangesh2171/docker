package com.vassarlabs.gp.pojo.NewsApi;

import lombok.Data;

import java.util.List;

@Data
public class Series {
    private List<String> keys;
    private String name;
    private List<Float> values;
}
