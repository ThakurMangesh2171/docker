package com.vassarlabs.gp.service.api;

import com.vassarlabs.gp.pojo.Mills;
import com.vassarlabs.gp.pojo.NewsApi.CachedData;
import com.vassarlabs.gp.pojo.ResponseRfpExcelResponse;
import com.vassarlabs.gp.pojo.ResponseRfpJson.RfpJsonTemplate;
import com.vassarlabs.gp.pojo.SupplierAndMillListPOJO;
import com.vassarlabs.gp.pojo.SupplierExcelData;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

public interface ResponseRfpService {

    ResponseRfpExcelResponse submitRfpResponseToRfpUsingExcel(MultipartFile responseRfpExcel, String supplierName);

    String downloadRfpResponseSubmissionExcelByResponseRfpId(String responseRfpId, String supplierName) throws IOException;
    List<String> generateResponseRfpExcelTemplate(String rfpNumber, Integer contractTerm, String fiberType, SupplierAndMillListPOJO supplierAndMillListPOJO) throws IOException;

    Boolean sendResponseRfpExcelTemplate(String rfpNumber, Integer contractTerm, String fiberType, SupplierAndMillListPOJO supplierAndMillListPOJO) throws ParseException;

    Map<String, List<CachedData>> fillCache(Map<String, List<CachedData>> indicesMetadata) throws InterruptedException;

    String generateExcelFromResponseRfpJson(String responseRfpId, RfpJsonTemplate rfpJsonTemplate, SupplierExcelData supplierExcelMetaData, List<Mills> millsList) throws FileNotFoundException;

//    void fetchAllIndicesData() throws InterruptedException;
}
