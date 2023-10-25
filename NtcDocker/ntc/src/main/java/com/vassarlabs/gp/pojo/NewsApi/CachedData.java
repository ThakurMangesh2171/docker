package com.vassarlabs.gp.pojo.NewsApi;

import lombok.Data;

@Data
public class CachedData {

    private Integer setId;
    private String name;
    private String short_desc2;

    public Integer getSetId() {
        return setId;
    }

    public void setSetId(Integer setId) {
        this.setId = setId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShort_desc2() {
        return short_desc2;
    }

    public void setShort_desc2(String short_desc2) {
        this.short_desc2 = short_desc2;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDifference() {
        return difference;
    }

    public void setDifference(String difference) {
        this.difference = difference;
    }

    public Float getDifferencePercentage() {
        return differencePercentage;
    }

    public void setDifferencePercentage(Float differencePercentage) {
        this.differencePercentage = differencePercentage;
    }

    public String getDifferenceValue() {
        return differenceValue;
    }

    public void setDifferenceValue(String differenceValue) {
        this.differenceValue = differenceValue;
    }

    private String value;
    private String difference;
    private Float differencePercentage;
    private String differenceValue;
}
