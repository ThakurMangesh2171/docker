package com.vassarlabs.gp.controller;

import com.google.gson.Gson;
import com.vassarlabs.gp.constants.*;
import com.vassarlabs.gp.exception.FileTemplateMismatchException;
import com.vassarlabs.gp.exception.InvalidValueProvidedException;
import com.vassarlabs.gp.exception.ResourceNotFoundException;
import com.vassarlabs.gp.pojo.*;
import com.vassarlabs.gp.pojo.ResponseRfpJson.ResponseRfpExcelData;
import com.vassarlabs.gp.pojo.ResponseRfpJson.RfpJsonTemplate;
import com.vassarlabs.gp.service.api.ResponseRfpService;
import com.vassarlabs.gp.service.impl.ApiResponseService;
import com.vassarlabs.gp.utils.Utils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.engine.jdbc.StreamUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@Api(description = "Response Rfp related operations")
public class ResponseRfpController {

    private static final Logger LOGGER = LogManager.getLogger(ResponseRfpController.class);

    @Autowired
    private ResponseRfpService responseRfpService;

    @Autowired
    private ApiResponseService apiResponseService;

    @Value("${responseRfp.excel.path}")
    private String responseRfpExcelPath;


    //API to submit Response Rfp from Excel for submitting RfpResponse
    @ApiOperation(value = "API for Submitting RFP Response",notes = "It is used For Submitting Response to Rfp")
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server error"),
            @io.swagger.annotations.ApiResponse(code = 404, message = "Service not found"),
            @io.swagger.annotations.ApiResponse(code = 200, message = "OK") })
    @PostMapping(value = APIConstants.SUBMIT_RFP_RESPONSE_TO_RFP_USING_EXCEL)
    public ApiResponse submitRfpResponseToRfpUsingExcel(@RequestPart MultipartFile responseRfpExcel, @RequestParam String supplierName) throws InvalidValueProvidedException , ResourceNotFoundException, FileTemplateMismatchException {
        LOGGER.info("In ResponseRfpController :: submitRfpResponseToRfpUsingExcel");
        if(responseRfpExcel==null){
            throw new InvalidValueProvidedException(MessageFormat.format(ErrorMessages.SHEET_NOT_FOUND_ERROR,"responseRfpExcel"));
        }
        if(supplierName==null || supplierName.trim().isEmpty()){
            throw new InvalidValueProvidedException(ErrorMessages.SUPPLIER_NAME_NULL_ERROR);
        }
        ResponseRfpExcelResponse responseRfpExcelResponse = responseRfpService.submitRfpResponseToRfpUsingExcel(responseRfpExcel, supplierName);
        return apiResponseService.buildApiResponse(HttpStatus.OK,responseRfpExcelResponse);
    }

//    //API to submit Response Rfp from Excel for submitting RfpResponse
//    @ApiOperation(value = "API for Downloading ResponseRfp Submission Excel ",notes = "It is used For download Response to Rfp submission ")
//    @io.swagger.annotations.ApiResponses(value = {
//            @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server error"),
//            @io.swagger.annotations.ApiResponse(code = 404, message = "Service not found"),
//            @io.swagger.annotations.ApiResponse(code = 200, message = "OK") })
//    @GetMapping(value = APIConstants.REST_TEMPLATE_EXCEL)
//    public ApiResponse downloadRfpResponseExcelByResponseRfpId(@RequestParam String responseRfpId) throws IOException {
//            LOGGER.info("In ResponseRfpController :: downloadRfpResponseExcelByResponseRfpId :: responseRfpId {}",responseRfpId);
//            responseRfpService.downloadRfpResponseSubmissionExcelByResponseRfpId(responseRfpId, rfpNumber, supplierName);
//        return apiResponseService.buildApiResponse(HttpStatus.OK,"Downloaded Successfully");
//    }

    //API for generating Response Rfp Excel template for specific Rfp
    @ApiOperation(value = "API for generating Response Rfp template for Specific Rfp",notes = "It is used for generating Response Rfp template for Specific Rfp")
    @PostMapping(value = APIConstants.GENERATE_EXCEL_TEMPLATE_FOR_RFP)
    public ApiResponse generateResponseRfpExcelTemplate(@RequestParam String rfpNumber, @RequestParam Integer contractTerm, @RequestParam String fiberType,@RequestBody SupplierAndMillListPOJO supplierAndMillListPOJO) throws InvalidValueProvidedException, IOException {
            LOGGER.info("In ResponseRfpController :: generateResponseRfpExcelTemplate");
            if (rfpNumber == null || rfpNumber.trim().isEmpty()) {
                throw new InvalidValueProvidedException(ErrorMessages.RFP_NUMBER_NULL_ERROR);
            }
            if (contractTerm == null) {
                throw new InvalidValueProvidedException(ErrorMessages.CONTRACT_TERM_NULL_ERROR);
            }
            if (fiberType == null || fiberType.trim().isEmpty()) {
                throw new InvalidValueProvidedException(ErrorMessages.FIBER_TYPE_NULL_ERROR);
            }
            if (supplierAndMillListPOJO.getDueDate() == null || supplierAndMillListPOJO.getDueDate().trim().isEmpty()) {
                throw new InvalidValueProvidedException(ErrorMessages.DUE_DATE_NULL_ERROR);
            }
            //validating SupplierNames and Email Id
            Utils.validateSupplierInfo(supplierAndMillListPOJO.getSupplierInfoList());

            // validating MillList is empty and validating Mandatory fields
            Utils.validateMillDetails(supplierAndMillListPOJO.getMIllsList());

            List<String> filePath = responseRfpService.generateResponseRfpExcelTemplate(rfpNumber, contractTerm, fiberType, supplierAndMillListPOJO);

            return apiResponseService.buildApiResponse(HttpStatus.OK, filePath);
        }
        


    //    Method to download files as zip or single file
    @ApiOperation(value = "API for downloading Documents",notes = "It is Used For Downloading documents")
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server error"),
            @io.swagger.annotations.ApiResponse(code = 404, message = "Service not found"),
            @io.swagger.annotations.ApiResponse(code = 200, message = "OK") })
    @PostMapping(value = APIConstants.DOWNLOAD_DOCUMENTS)
    public void downloadFiles(HttpServletResponse response , @RequestBody @Valid DownloadDocumentRequestBody filesDetails) throws IOException, SQLException {
        LOGGER.info("In ResponseRfpController :: downloadFiles");
        //check for filenames size
        if(filesDetails.getFileNames().isEmpty()){
            throw new InvalidValueProvidedException(ErrorMessages.FILE_NAMES_NULL_ERROR);
        }

        //If file is single we need to return as file
        if(filesDetails.getFileNames().size()==1){
            downloadAsFile(response,filesDetails.getFileNames());
        }else{
            //If file is zip then we need to return as zip
            downloadAzZip(response,filesDetails.getFileNames());
        }
    }

    private void downloadAsFile(HttpServletResponse response, List<String> fileNames) throws IOException {
        //check if file exists
        File file = new File(fileNames.get(0));
        if(!file.exists()){
            throw new FileNotFoundException(ErrorMessages.FILE_NOT_FOUND);
        }
        //check if file has permissions
        if(!file.canRead()){
            throw new FileNotFoundException(ErrorMessages.FILE_PERMISSION_ERROR);
        }
        String  fileFormat;
        Path path = Paths.get(fileNames.get(0));
        if (fileNames.get(0).contains(Constants.DOT))
            fileFormat = fileNames.get(0).substring(fileNames.get(0).lastIndexOf(Constants.DOT));
        else
            throw new FileNotFoundException(ErrorMessages.FILE_FORMAT_NOT_FOUND);

        if (fileFormat.equals(Constants.EXCEL) || fileFormat.equals(ExcelConstants.EXCEL)){
            response.setContentType("application/"+ExcelConstants.EXCEL_CONTENT_TYPE);
        }else {
            response.setContentType("application/"+fileFormat);
        }
        response.addHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + path.getFileName().toString().replaceAll(Constants.SPACE,Constants.UNDER_SCORE));
        response.setContentLength((int) file.length());
        //try with resource (https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html)
        try(FileInputStream fileInputStream = new FileInputStream(file);
            OutputStream responseOutputStream = response.getOutputStream();){
            int bytes;
            while ((bytes = fileInputStream.read()) != -1) {
                responseOutputStream.write(bytes);
            }
        }
    }

    private void downloadAzZip(HttpServletResponse response, List<String> fileNames) throws IOException {
        //try with resourcse and check file and permissions
        try (ZipOutputStream zipOut = new ZipOutputStream(response.getOutputStream());){
            for (String filePath: fileNames) {
                FileSystemResource resource = new FileSystemResource(filePath);
                //check if file exists or not if not skip that
                if(resource.exists() && resource.isReadable()){
                    try {
                        //TODO need to recheck
                        ZipEntry zipEntry = new ZipEntry(Objects.requireNonNull(resource.getFilename()));
                        zipEntry.setSize(resource.contentLength());
                        zipOut.putNextEntry(zipEntry);
                        StreamUtils.copy(resource.getInputStream(), zipOut);
                    }finally {
                        zipOut.closeEntry();
                    }

                }
            }
            zipOut.finish();
        }
        response.setStatus(HttpServletResponse.SC_OK);
        response.addHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename= Documents.zip");
    }


    //API to download Rfp Generated template
    @ApiOperation(value = "API for downloading Rfp template",notes = "It is Used For Downloading Rfp template")
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server error"),
            @io.swagger.annotations.ApiResponse(code = 404, message = "Service not found"),
            @io.swagger.annotations.ApiResponse(code = 200, message = "OK") })
    @PostMapping(value = APIConstants.DOWNLOAD_RFP_TEMPLATE)
    public void downloadRfpTemplate(HttpServletResponse response , @PathVariable String rfpNumber,@RequestParam String supplierName, @RequestParam Integer contractTerm,@RequestParam String fiberType) throws InvalidValueProvidedException, IOException {
        LOGGER.info("In ResponseRfpController :: downloadRfpTemplate");
        //check for filenames size
        if(rfpNumber==null || rfpNumber.trim().isEmpty()){
            throw new InvalidValueProvidedException(ErrorMessages.RFP_NUMBER_NULL_ERROR);
        }
        if (contractTerm == null) {
            throw new InvalidValueProvidedException(ErrorMessages.CONTRACT_TERM_NULL_ERROR);
        }
        if (fiberType == null || fiberType.trim().isEmpty()) {
            throw new InvalidValueProvidedException(ErrorMessages.FIBER_TYPE_NULL_ERROR);
        }
        if (supplierName == null || supplierName.trim().isEmpty()) {
            throw new InvalidValueProvidedException(ErrorMessages.SUPPLIER_NAME_NULL_ERROR);
        }
        downloadAsFile(response, Collections.singletonList(responseRfpExcelPath+rfpNumber+Constants.UNDER_SCORE+contractTerm+Constants.UNDER_SCORE+supplierName+Constants.UNDER_SCORE+fiberType+Constants.EXCEL));
    }

    //API for sending response rfp template excel template to list of suppliers
    @ApiOperation(value = "API for sending response rfp template excel template to list of suppliers",notes = "API for sending response rfp template excel template to list of suppliers")
    @PostMapping(value = APIConstants.SEND_EXCEL_TEMPLATE_FOR_RFP)
    public ApiResponse sendResponseRfpExcelTemplate(@RequestParam String rfpNumber, @RequestParam Integer contractTerm, @RequestParam String fiberType,@RequestBody SupplierAndMillListPOJO supplierAndMillListPOJO) throws InvalidValueProvidedException, IOException, ParseException {
        LOGGER.info("In ResponseRfpController :: generateResponseRfpExcelTemplate");
        LOGGER.info("Request Params :: rfpNumber : "+rfpNumber+"  contractTerm : "+contractTerm+"  fiberType : "+fiberType);
        LOGGER.info("Payload : "+new Gson().toJson(supplierAndMillListPOJO));
        if (rfpNumber == null || rfpNumber.trim().isEmpty()) {
            throw new InvalidValueProvidedException(ErrorMessages.RFP_NUMBER_NULL_ERROR);
        }
        if (contractTerm == null) {
            throw new InvalidValueProvidedException(ErrorMessages.CONTRACT_TERM_NULL_ERROR);
        }
        if (fiberType == null || fiberType.trim().isEmpty()) {
            throw new InvalidValueProvidedException(ErrorMessages.FIBER_TYPE_NULL_ERROR);
        }

        if (supplierAndMillListPOJO.getDueDate() == null || supplierAndMillListPOJO.getDueDate().trim().isEmpty()) {
            throw new InvalidValueProvidedException(ErrorMessages.DUE_DATE_NULL_ERROR);
        }
        //validating SupplierNames and Email Id
        Utils.validateSupplierInfo(supplierAndMillListPOJO.getSupplierInfoList());

        // validating MillList is empty and validating Mandatory fields
        Utils.validateMillDetails(supplierAndMillListPOJO.getMIllsList());

       Boolean sendMailSuccess = responseRfpService.sendResponseRfpExcelTemplate(rfpNumber, contractTerm, fiberType, supplierAndMillListPOJO);

       if(Boolean.TRUE.equals(sendMailSuccess)){
           return apiResponseService.buildApiResponse(HttpStatus.OK, SuccessMessages.EMAIL_SENT_SUCCESSFULLY);
       }else{
           return apiResponseService.buildApiResponse(HttpStatus.INTERNAL_SERVER_ERROR,  ErrorMessages.EMAIL_SENT_FAIL);
       }
    }

    //API for generating Response Rfp Excel From Json
    @ApiOperation(value = "API for generating Response Rfp Excel From Json",notes = "API for generating Response Rfp Excel From Json")
    @PostMapping(value = APIConstants.GENERATE_EXCEL)
    public ApiResponse generateExcelFromResponseRfpJson(HttpServletResponse httpServletResponse, @RequestParam String responseRfpId, @RequestBody ResponseRfpExcelData responseRfpExcelData) throws IOException, InvalidValueProvidedException{
        LOGGER.info("In ResponseRfpController :: generateExcelFromJson");
        if(responseRfpId==null){
            throw new InvalidValueProvidedException(ErrorMessages.RESPONSE_RFP_ID_NULL_ERROR);
        }
        if(responseRfpExcelData==null){
            throw new InvalidValueProvidedException(ErrorMessages.RESPONSE_RFP_EXCEL_DATA_NULL);
        }
        if (responseRfpExcelData.getRfpJsonTemplate() == null) {
            throw new InvalidValueProvidedException(ErrorMessages.RFP_JSON_TEMPLATE_NULL_ERROR);
        }
        if(responseRfpExcelData.getSupplierExcelData() == null){
            throw new InvalidValueProvidedException(ErrorMessages.SUPPLIER_EXCEL_META_DATA_NULL_ERROR);
        }
        if(responseRfpExcelData.getSupplierExcelData().getSupplierName()==null){
            throw new InvalidValueProvidedException(ErrorMessages.SUPPLIER_NAME_NULL_ERROR);
        }
        if(responseRfpExcelData.getMills()==null || responseRfpExcelData.getMills().isEmpty()){
            throw new InvalidValueProvidedException(ErrorMessages.MILLS_LIST_NULL_ERROR);
        }
        if(responseRfpExcelData.getSupplierExcelData()!=null && responseRfpExcelData.getSupplierExcelData().getCommodity()==null || responseRfpExcelData.getSupplierExcelData().getCommodity().trim().isEmpty()){
            throw new InvalidValueProvidedException(ErrorMessages.FIBER_TYPE_NULL_ERROR);
        }
        String filePath = responseRfpService.generateExcelFromResponseRfpJson(responseRfpId,responseRfpExcelData.getRfpJsonTemplate(),responseRfpExcelData.getSupplierExcelData(),responseRfpExcelData.getMills());
        //downloading Excel
        downloadAsFile(httpServletResponse,Collections.singletonList(filePath));
        return apiResponseService.buildApiResponse(HttpStatus.OK, filePath);
    }



}
