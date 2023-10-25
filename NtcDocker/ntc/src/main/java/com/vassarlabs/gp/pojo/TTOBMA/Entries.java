package com.vassarlabs.gp.pojo.TTOBMA;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class Entries {
    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getMillLocation() {
        return millLocation;
    }

    public void setMillLocation(String millLocation) {
        this.millLocation = millLocation;
    }

    public String getGradeCategory() {
        return gradeCategory;
    }

    public void setGradeCategory(String gradeCategory) {
        this.gradeCategory = gradeCategory;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(String effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public int getTonnes() {
        return tonnes;
    }

    public void setTonnes(int tonnes) {
        this.tonnes = tonnes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    @SerializedName("Company")
    private String company;

    @SerializedName("Mill Location")
    private String millLocation;

    @SerializedName("Grade Category")
    private String gradeCategory;

    @SerializedName("Grade")
    private String grade;

    @SerializedName("Effective Date")
    private String effectiveDate;

    @SerializedName("Tonnes (000s)")
    private int tonnes;

    @SerializedName("Status")
    private String status;

    @SerializedName("Note")
    private String note;

}
