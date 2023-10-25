package com.vassarlabs.gp.service.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.vassarlabs.gp.pojo.ErrorMessageBasicData;
import com.vassarlabs.gp.pojo.ResponseRfpExcelResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ErrorMessageService {
    ResponseRfpExcelResponse getAllErrorMessagesOfResponseRfp(String responseRfpId, String supplierName) throws IOException;

    ResponseRfpExcelResponse reSubmitResponseRfp(String responseRfpId, String supplierName, MultipartFile responseRfpFile);

    String saveOrUpdateErrorMessage(ErrorMessageBasicData errorMessageBasicData);
}
