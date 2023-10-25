package com.vassarlabs.gp.pojo;

import lombok.Data;

import java.util.List;

@Data
public class SupplierAndMillListPOJO {
    private List<SupplierInfo> supplierInfoList;

    private List<Mills> mIllsList;

    private String dueDate;
}
