package com.vassarlabs.gp.pojo.ResponseRfpJson;

import com.vassarlabs.gp.pojo.Mills;
import com.vassarlabs.gp.pojo.ResponseRfpJson.MetadataPojo.SupplierMetadata;
import com.vassarlabs.gp.pojo.SupplierExcelData;
import lombok.Data;

import java.util.List;

@Data
public class ResponseRfpExcelData {

    private RfpJsonTemplate rfpJsonTemplate;
    private SupplierExcelData supplierExcelData;

    private List<Mills> mills;

}
