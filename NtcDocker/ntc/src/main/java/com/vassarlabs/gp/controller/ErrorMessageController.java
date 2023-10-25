package com.vassarlabs.gp.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.vassarlabs.gp.constants.APIConstants;
import com.vassarlabs.gp.constants.ErrorMessages;
import com.vassarlabs.gp.constants.SuccessMessages;
import com.vassarlabs.gp.exception.InvalidValueProvidedException;
import com.vassarlabs.gp.pojo.*;
import com.vassarlabs.gp.service.api.ErrorMessageService;
import com.vassarlabs.gp.service.impl.ApiResponseService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@RestController
@Api(description = "List of APIs related to Error Messages")
public class ErrorMessageController {

    private static final Logger LOGGER = LogManager.getLogger(ErrorMessageController.class);

    @Autowired
    private ErrorMessageService errorMessageService;

    @Autowired
    private ApiResponseService apiResponseService;


    //API to fetch List Of ErrorMessages For A Particular Response Rfp
    @ApiOperation(value = "API for fetching list of Error Messages For a Particular Response Rfp",notes = "It is used for fetching list of Error Messages For a Particular Response Rfp")
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server error"),
            @io.swagger.annotations.ApiResponse(code = 404, message = "Service not found"),
            @io.swagger.annotations.ApiResponse(code = 200,  message = "Ok",response = ResponseRfpExcelResponse.class) })
    @GetMapping(value = APIConstants.GET_ALL_ERROR_MESSAGES_OF_RFP)
    public ApiResponse getAllErrorMessagesOfResponseRfp(@PathVariable String responseRfpId , @RequestParam String supplierName) throws IOException {
        LOGGER.info("In ErrorMessageController :: getAllErrorMessagesOfResponseRfp");
        if(responseRfpId==null || responseRfpId.trim().isEmpty()){
            throw new InvalidValueProvidedException(ErrorMessages.RESPONSE_RFP_NULL_ERROR);
        }
        if(supplierName==null || supplierName.trim().isEmpty()){
            throw new InvalidValueProvidedException(ErrorMessages.SUPPLIER_NAME_NULL_ERROR);
        }
        ResponseRfpExcelResponse responseRfpExcelResponse = errorMessageService.getAllErrorMessagesOfResponseRfp(responseRfpId,supplierName);
        return apiResponseService.buildApiResponse(HttpStatus.OK, responseRfpExcelResponse);
    }


    //API to Re-submit the Response Rfp

    //API to fetch List Of ErrorMessages For A Particular Response Rfp
    @ApiOperation(value = "API for resubmitting Response Rfp",notes = "It is used for resubmitting Response Rfp")
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server error"),
            @io.swagger.annotations.ApiResponse(code = 404, message = "Service not found"),
            @io.swagger.annotations.ApiResponse(code = 200,  message = "Ok",response = ResponseRfpExcelResponse.class)})
    @PostMapping(value = APIConstants.RE_SUBMIT_RESPONSE_RFP)
    public ApiResponse reSubmitResponseRfp(@PathVariable String responseRfpId , @RequestParam String supplierName, @RequestPart MultipartFile responseRfpFile) throws  JsonProcessingException {
        LOGGER.info("In ErrorMessageController :: reSubmitResponseRfp");
        if(responseRfpId==null || responseRfpId.trim().isEmpty()){
            throw new InvalidValueProvidedException(ErrorMessages.RESPONSE_RFP_NULL_ERROR);
        }
        if(supplierName==null || supplierName.trim().isEmpty()){
            throw new InvalidValueProvidedException(ErrorMessages.SUPPLIER_NAME_NULL_ERROR);
        }
        ResponseRfpExcelResponse responseRfpExcelResponse = errorMessageService.reSubmitResponseRfp(responseRfpId,supplierName, responseRfpFile);
        return apiResponseService.buildApiResponse(HttpStatus.OK,responseRfpExcelResponse);
    }

    //API to fetch List Of ErrorMessages For A Particular Response Rfp
    @ApiOperation(value = "API for Saving or Updating Data in ErrorMessages",notes = "It is used for saving Data in ErrorMessages")
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server error"),
            @io.swagger.annotations.ApiResponse(code = 404, message = "Service not found"),
            @io.swagger.annotations.ApiResponse(code = 200,  message = "Ok",response = ErrorMessageBasicData.class)})
    @PostMapping(value = APIConstants.SAVE_UPDATE_ERROR_MESSAGE)
    public ApiResponse saveOrUpdateErrorMessage(@RequestBody ErrorMessageBasicData errorMessageBasicData){
        LOGGER.info("In ErrorMessageController :: saveOrUpdateErrorMessage");

        String responseRfpId = errorMessageService.saveOrUpdateErrorMessage(errorMessageBasicData);

        return apiResponseService.buildApiResponse(HttpStatus.OK, SuccessMessages.ERROR_MESSAGE_SAVED_SUCCESS,responseRfpId);
    }

}
