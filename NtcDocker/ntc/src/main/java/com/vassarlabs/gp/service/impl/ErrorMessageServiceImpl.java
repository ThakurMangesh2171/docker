package com.vassarlabs.gp.service.impl;

import com.vassarlabs.gp.constants.Constants;
import com.vassarlabs.gp.constants.ErrorMessages;
import com.vassarlabs.gp.constants.ExcelConstants;
import com.vassarlabs.gp.dao.api.ErrorMessageDao;
import com.vassarlabs.gp.dao.entity.ApplicationData;
import com.vassarlabs.gp.dao.entity.ErrorMessage;
import com.vassarlabs.gp.exception.FileParsingException;
import com.vassarlabs.gp.exception.ResourceNotFoundException;
import com.vassarlabs.gp.pojo.*;
import com.vassarlabs.gp.service.api.ErrorMessageService;
import com.vassarlabs.gp.utils.ExcelParsingUtils;
import com.vassarlabs.gp.utils.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ErrorMessageServiceImpl implements ErrorMessageService {
    private static final Logger LOGGER = LogManager.getLogger(ErrorMessageServiceImpl.class);

    @Autowired
    private ErrorMessageDao errorMessageDao;

    @Autowired
    private ResponseRfpServiceImpl responseRfpService;

    @Value("${responseRfp.excel.path}")
    private String responseRfpExcelPath;


    //Method to Fetch List of Error Messages for a Particular Response Rfp
    @Override
    public ResponseRfpExcelResponse getAllErrorMessagesOfResponseRfp(String responseRfpId, String supplierName) throws IOException,ResourceNotFoundException {
        LOGGER.info("In ErrorMessageServiceImpl :: getAllErrorMessagesOfResponseRfp");
        ResponseRfpExcelResponse responseRfpExcelResponse = new ResponseRfpExcelResponse();
        ErrorMessage errorMessageDetails = errorMessageDao.getAllErrorMessagesOfResponseRfp(responseRfpId);
        //The Size of List will be always 1 or 0
        //Check if Entry exists in Error Message table
        if (errorMessageDetails != null) {
            File file = new File(responseRfpExcelPath + responseRfpId + Constants.UNDER_SCORE + supplierName + Constants.EXCEL);
            //If errorMessageJson is Null return Error Messages
            //TODO : error Message
            if (errorMessageDetails.getResponseRfpErrorMessages() != null && errorMessageDetails.getResponseRfpErrorMessages().getErrorMessagesJson() != null && !errorMessageDetails.getResponseRfpErrorMessages().getErrorMessagesJson().isEmpty()) {
                responseRfpExcelResponse.setErrorMessageDetails(errorMessageDetails.getResponseRfpErrorMessages().getErrorMessagesJson());
                if (errorMessageDetails.getResponseRfpErrorMessages().getResponseRfpWarning() != null)
                    responseRfpExcelResponse.setResponseRfpWarning(errorMessageDetails.getResponseRfpErrorMessages().getResponseRfpWarning());
                if (file.exists() && file.canRead()) {
                    try (Workbook workbook = new XSSFWorkbook(new FileInputStream(file))) {
                        Sheet bidQtySheet = workbook.getSheet(ExcelConstants.ResponseRfpExcelSheetNames.BID_DETAILS.getSheetName());
                        String rfpNumber = ExcelParsingUtils.getRfpNumberFromBidQtyDetailsSheet(bidQtySheet);
                        if(rfpNumber!=null){
                            responseRfpExcelResponse.setRfpNumber(rfpNumber);
                        }
                    }
                }
            } else {
                    //else Parse the sheet and return JSON
                    if (file.exists() && file.canRead()) {
                        //3. Parse the Json
                        responseRfpExcelResponse = ExcelParsingUtils.populateResponseRfpJsonFromExcel(null, file, supplierName);
                    }else{
                        throw new ResourceNotFoundException(ErrorMessages.FILE_NOT_FOUND);
                    }
                }

        } else {
                //if Not then file is not yet Processed
                // 1. Fetch Uploaded Excel Using RestTemplate
                //2. Save this File In Our Local at Particular Path
                //Method to fetch the Uploaded Excel File  and Saving in localPath
                String filePath = responseRfpService.downloadRfpResponseSubmissionExcelByResponseRfpId(responseRfpId, supplierName);
                File file = new File(filePath);
                if (file.exists() && file.canRead()) {
                    //3. Parse the Json
                    //TODO :: added ErrorMessage type in Hybrid and other pricing mechanism basis
                    responseRfpExcelResponse = ExcelParsingUtils.populateResponseRfpJsonFromExcel(null, file,supplierName);
                    //4. create an entry in error messages table
                    //TODO :: modified Error Message thing
                    ErrorMessage errorMessage = Utils.buildErrorMessageEntity(responseRfpId, responseRfpExcelResponse.getErrorMessageDetails(),responseRfpExcelResponse.getResponseRfpWarning());
                    //5. If Errors save in Error Messages in above entry
                    errorMessageDao.saveErrorMessages(errorMessage);
                    //6. else need to return Valid Json
                }else{
                    throw new ResourceNotFoundException(ErrorMessages.FILE_NOT_FOUND);
                }
                //Set template and Submitted File Path
//                responseRfpExcelResponse.setTemplatePath(responseRfpExcelPath + responseRfpExcelResponse.getRfpNumber() + Constants.EXCEL);
                responseRfpExcelResponse.setSubmittedFilePath(responseRfpExcelPath + responseRfpId + Constants.UNDER_SCORE + supplierName + Constants.EXCEL);
                return responseRfpExcelResponse;
            }
            //Set template and Submitted File Path
//            responseRfpExcelResponse.setTemplatePath(responseRfpExcelPath + responseRfpExcelResponse.getRfpNumber() + Constants.EXCEL);
            responseRfpExcelResponse.setSubmittedFilePath(responseRfpExcelPath + responseRfpId + Constants.UNDER_SCORE + supplierName + Constants.EXCEL);
            return responseRfpExcelResponse;
    }


    //API for re-submitting Response Rfp
    @Override
    public ResponseRfpExcelResponse reSubmitResponseRfp(String responseRfpId, String supplierName, MultipartFile responseRfpFile) throws FileParsingException {
        LOGGER.info("In ErrorMessageServiceImpl :: reSubmitResponseRfp");
        //1. Parse the Sheet
        ResponseRfpExcelResponse responseRfpExcelResponse  = ExcelParsingUtils.populateResponseRfpJsonFromExcel(responseRfpFile,null,supplierName);
        //2. fetch the Entry From Error Messages table and Update error Message json
        ErrorMessage errorMessage = errorMessageDao.getAllErrorMessagesOfResponseRfp(responseRfpId);
        if(errorMessage==null){
            //TODO :: modified Error Message thing
            errorMessage = Utils.buildErrorMessageEntity(responseRfpId,responseRfpExcelResponse.getErrorMessageDetails(),responseRfpExcelResponse.getResponseRfpWarning());
        }else{
            //3. If Errors update error messages
            if(responseRfpExcelResponse.getErrorMessageDetails()!=null){
                //TODO :: modified for New ErrorMessage POJO
                if (errorMessage.getResponseRfpErrorMessages() != null){
                    errorMessage.getResponseRfpErrorMessages().setErrorMessagesJson(responseRfpExcelResponse.getErrorMessageDetails());
                    errorMessage.getResponseRfpErrorMessages().setResponseRfpWarning(responseRfpExcelResponse.getResponseRfpWarning());
                }else {
                    ResponseRfpErrorMessages responseRfpErrorMessages = new ResponseRfpErrorMessages();
                    responseRfpErrorMessages.setErrorMessagesJson(responseRfpExcelResponse.getErrorMessageDetails());
                    responseRfpErrorMessages.setResponseRfpWarning(responseRfpExcelResponse.getResponseRfpWarning());
                    errorMessage.setResponseRfpErrorMessages(responseRfpErrorMessages);
                }
//                errorMessage.setErrorMessagesJson(responseRfpExcelResponse.getErrorMessageDetails());
            }else{
                List<ErrorMessageDetails> errorMessageList = new ArrayList<>();
                if (errorMessage.getResponseRfpErrorMessages()!=null){
                    errorMessage.getResponseRfpErrorMessages().setErrorMessagesJson(errorMessageList);
                }else {
                    errorMessage.getResponseRfpErrorMessages().setErrorMessagesJson(errorMessageList);
                }
            }
            // checking for if responseRfpExcelResponse is NUll or NOt null
            if (responseRfpExcelResponse.getResponseRfpWarning() != null){
                if (errorMessage.getResponseRfpErrorMessages() != null){
                    errorMessage.getResponseRfpErrorMessages().setResponseRfpWarning(responseRfpExcelResponse.getResponseRfpWarning());
                }else {
                    ResponseRfpErrorMessages responseRfpErrorMessages = new ResponseRfpErrorMessages();
                    responseRfpErrorMessages.setResponseRfpWarning(responseRfpExcelResponse.getResponseRfpWarning());
                    errorMessage.setResponseRfpErrorMessages(responseRfpErrorMessages);
                }
            }else {
                List<ErrorMessageDetails> errorMessageList = new ArrayList<>();
                if (errorMessage.getResponseRfpErrorMessages()!=null){
                    errorMessage.getResponseRfpErrorMessages().setResponseRfpWarning(errorMessageList);
                }else {
                    errorMessage.getResponseRfpErrorMessages().setResponseRfpWarning(errorMessageList);
                }
            }
            errorMessage.setIsInsert(false);
        }
        errorMessage.setUpdatedTs(Utils.getCurrentTime());
        //4. Save updated Error Message entry
        errorMessageDao.saveErrorMessages(errorMessage);
        //5. Override the File that is in our local  with this new File
        try {
            // Create the directory if it doesn't exist
            File directory = new File(responseRfpExcelPath);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            //Delete file if exists
            Utils.deleteFile(responseRfpExcelPath+responseRfpId+Constants.UNDER_SCORE+supplierName+Constants.EXCEL);
            // Create the target file with the custom file name
            File file = new File(responseRfpExcelPath, responseRfpId+Constants.UNDER_SCORE+supplierName+Constants.EXCEL);
            // Transfer the multipartFile to the target file
            responseRfpFile.transferTo(file);
        } catch (IOException e) {
            throw  new FileParsingException(ErrorMessages.ERROR_SAVING_FILE);
        }
        //TODO need to trigger Update Response Rfp Status to Processing failed or Processed pr Processing (Based on errors)
        //TODO need to trigger Save Response Rfp if valid Json (successful validation)
        return responseRfpExcelResponse;
    }

    //Method For Saving Or updatingError Message
    @Override
    public String saveOrUpdateErrorMessage(ErrorMessageBasicData errorMessageBasicData) {
        LOGGER.info("In ErrorMessageServiceImpl :: saveOrUpdateErrorMessage");
        if (errorMessageBasicData.getResponseRfpId() !=null && !errorMessageBasicData.getResponseRfpId().trim().isEmpty()){
            updateErrorMessage(errorMessageBasicData);
            return errorMessageBasicData.getResponseRfpId();
        }else {
            return saveErrorMessageDetails();
        }

    }

    // Method to Adding ErrorMessage Details
    private String saveErrorMessageDetails() {
        LOGGER.info("In ErrorMessageServiceImpl :: saveErrorMessageDetails");
        ErrorMessage errorMessage = new ErrorMessage();

        errorMessage.setMsgUuid(Utils.generateRandomUUID());
        errorMessage.setResponseRfpId(Utils.generateRandomUUID());
        errorMessage.setResponseRfpErrorMessages(null);
        errorMessage.setInsertTs(Utils.getCurrentTime());
        errorMessage.setUpdatedTs(Utils.getCurrentTime());
        errorMessage.setInsert(Boolean.TRUE);

        errorMessageDao.saveErrorMessages(errorMessage);
        return errorMessage.getResponseRfpId();

    }


    // Methode to Update ErrorMessage Details Using ResponseRfpId
    private void updateErrorMessage(ErrorMessageBasicData errorMessageBasicData) throws ResourceNotFoundException{
        LOGGER.info("In ErrorMessageServiceImpl :: updateErrorMessage");

        // fetching ErrorMessage Details from DataBase
        ErrorMessage errorMessage = errorMessageDao.getAllErrorMessagesOfResponseRfp(errorMessageBasicData.getResponseRfpId());

        if (errorMessage == null){
            throw new ResourceNotFoundException(ErrorMessages.ERROR_MESSAGE_DATA_NOT_FOUNT);
        }


        if (errorMessageBasicData.getResponseRfpErrorMessages() !=null){
            errorMessage.setResponseRfpErrorMessages(errorMessageBasicData.getResponseRfpErrorMessages());
        }
        errorMessage.setUpdatedTs(Utils.getCurrentTime());
        errorMessage.setInsert(false);
        errorMessageDao.saveErrorMessages(errorMessage);
    }


}
