package com.vassarlabs.gp.pojo;


import lombok.Data;

@Data
public class ErrorMessageDetails {
    private String sheetName;
    private String cellReference;
    private String expectedValue;
    private String foundValue;
    private String fieldName;
    private String message;

    public ErrorMessageDetails(String sheetName, String cellReference, String expectedValue, String foundValue, String fieldName, String message) {
        this.sheetName = sheetName;
        this.cellReference = cellReference;
        this.expectedValue = expectedValue;
        this.foundValue = foundValue;
        this.fieldName = fieldName;
        this.message = message;
    }
}
