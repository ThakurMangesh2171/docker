package com.vassarlabs.gp.pojo;

import lombok.Data;

@Data
public class SupplierExcelData {
    private String supplierName;
    private String email;
    private String rfpNumber;
    private String dueDate;
    private String commodity;
    private Integer contractTerm;
}
