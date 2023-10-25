package com.vassarlabs.gp.pojo.TTOBMA;

import com.google.gson.annotations.SerializedName;

public class DowntimeReport {
    @SerializedName("Region")
    private String region;

    @SerializedName("Country")
    private String country;

    @SerializedName("Company")
    private String company;

    @SerializedName("Mill")
    private String mill;

    @SerializedName("Pulp Category")
    private String pulpCategory;

    @SerializedName("Pulp Grade")
    private String pulpGrade;

    @SerializedName("Month")
    private String month;

    @SerializedName("Reason")
    private String reason;

    @SerializedName("Status")
    private String status;

    @SerializedName("Days of Downtime")
    private int daysOfDowntime;

    @SerializedName("Lost Tonnes")
    private int lostTonnes;

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getMill() {
        return mill;
    }

    public void setMill(String mill) {
        this.mill = mill;
    }

    public String getPulpCategory() {
        return pulpCategory;
    }

    public void setPulpCategory(String pulpCategory) {
        this.pulpCategory = pulpCategory;
    }

    public String getPulpGrade() {
        return pulpGrade;
    }

    public void setPulpGrade(String pulpGrade) {
        this.pulpGrade = pulpGrade;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getDaysOfDowntime() {
        return daysOfDowntime;
    }

    public void setDaysOfDowntime(int daysOfDowntime) {
        this.daysOfDowntime = daysOfDowntime;
    }

    public int getLostTonnes() {
        return lostTonnes;
    }

    public void setLostTonnes(int lostTonnes) {
        this.lostTonnes = lostTonnes;
    }
}