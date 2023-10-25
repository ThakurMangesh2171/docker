package com.vassarlabs.gp.pojo.NewsApi;

import lombok.Data;

import java.util.List;

@Data
public class Tile {
    private String name;
    private List<Series> series;
    private String long_desc;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Series> getSeries() {
        return series;
    }

    public void setSeries(List<Series> series) {
        this.series = series;
    }

    public String getLong_desc() {
        return long_desc;
    }

    public void setLong_desc(String long_desc) {
        this.long_desc = long_desc;
    }

    public String getExtra_desc() {
        return extra_desc;
    }

    public void setExtra_desc(String extra_desc) {
        this.extra_desc = extra_desc;
    }

    public String getShort_desc() {
        return short_desc;
    }

    public void setShort_desc(String short_desc) {
        this.short_desc = short_desc;
    }

    public String getShort_desc2() {
        return short_desc2;
    }

    public void setShort_desc2(String short_desc2) {
        this.short_desc2 = short_desc2;
    }

    private String extra_desc;
    private String short_desc;
    private String short_desc2;
}
