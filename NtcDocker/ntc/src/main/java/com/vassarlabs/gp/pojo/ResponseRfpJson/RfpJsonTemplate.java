package com.vassarlabs.gp.pojo.ResponseRfpJson;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class RfpJsonTemplate {

    private String supplier = "";

    private List<SupplierMills> supplier_mills = new ArrayList<>();

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public List<SupplierMills> getSupplier_mills() {
        return supplier_mills;
    }

    public void setSupplier_mills(List<SupplierMills> supplier_mills) {
        this.supplier_mills = supplier_mills;
    }
}
