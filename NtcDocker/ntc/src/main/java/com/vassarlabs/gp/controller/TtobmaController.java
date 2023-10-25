package com.vassarlabs.gp.controller;

import com.vassarlabs.gp.constants.APIConstants;
import com.vassarlabs.gp.constants.Constants;
import com.vassarlabs.gp.constants.ErrorMessages;
import com.vassarlabs.gp.constants.ExcelConstants;
import com.vassarlabs.gp.exception.InvalidValueProvidedException;
import com.vassarlabs.gp.pojo.ApiResponse;
import com.vassarlabs.gp.pojo.ResponseRfpExcelResponse;
import com.vassarlabs.gp.service.api.TtobmaService;
import com.vassarlabs.gp.service.impl.ApiResponseService;
import com.vassarlabs.gp.utils.Utils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@Api(description = "List of APIs related to Ttobma")
public class TtobmaController {
    private static final Logger LOGGER = LogManager.getLogger(TtobmaController.class);

    @Autowired
    private TtobmaService ttobmaService;

    @Autowired
    private ApiResponseService apiResponseService;

    @ApiOperation(value = "API to download Capacity List excel",notes = "It is used to download Capacity List excel")
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server error"),
            @io.swagger.annotations.ApiResponse(code = 404, message = "Service not found"),
            @io.swagger.annotations.ApiResponse(code = 200,  message = "Ok",response = ResponseRfpExcelResponse.class) })
    @GetMapping(value = APIConstants.DOWNLOAD_CAPACITY_EXCEL)
    public ApiResponse downloadCapacityExcel(HttpServletResponse httpServletResponse, @RequestParam String excelName) throws IOException, InvalidValueProvidedException{
        LOGGER.info("In TtobmaController :: downloadCapacityExcel");

        if (!Stream.of(Constants.CapacityExcelKeyName.values()).map(Constants.CapacityExcelKeyName::getValue).collect(Collectors.toList()).contains(excelName)){
            throw new InvalidValueProvidedException(ErrorMessages.INVALID_EXCEL_NAME_CAPACITY_LISTS);
        }

        String filePath = ttobmaService.downloadCapacityExcel(excelName);
        downloadAsFile(httpServletResponse, Collections.singletonList(filePath));
        return apiResponseService.buildApiResponse(HttpStatus.OK, filePath);
    }

    @ApiOperation(value = "API to download Downtime report excel",notes = "It is used to download Downtime report excel")
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 500, message = "Internal Server error"),
            @io.swagger.annotations.ApiResponse(code = 404, message = "Service not found"),
            @io.swagger.annotations.ApiResponse(code = 200,  message = "Ok",response = ResponseRfpExcelResponse.class) })
    @GetMapping(value = APIConstants.DOWNLOAD_DOWNTIME_EXCEL)
    public ApiResponse downloadDowntimeReportExcel(HttpServletResponse httpServletResponse) throws IOException {
        LOGGER.info("In TtobmaController :: downloadDowntimeReportExcel");

        String filePath = ttobmaService.downloadDowntimeReportExcel();

        downloadAsFile(httpServletResponse,Collections.singletonList(filePath));
        return apiResponseService.buildApiResponse(HttpStatus.OK, filePath);
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

//        if (fileFormat.equals(Constants.EXCEL) || fileFormat.equals(ExcelConstants.EXCEL)){
//            response.setContentType("application/"+ExcelConstants.EXCEL_CONTENT_TYPE);
//        }else {
//            response.setContentType("application/"+fileFormat);
//        }
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


}



