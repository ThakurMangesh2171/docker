package com.vassarlabs.gp.service.impl;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.vassarlabs.gp.constants.APIConstants;
import com.vassarlabs.gp.constants.Constants;
import com.vassarlabs.gp.constants.ErrorMessages;
import com.vassarlabs.gp.pojo.TTOBMA.CapacityLists;
import com.vassarlabs.gp.pojo.TTOBMA.DowntimeReport;
import com.vassarlabs.gp.pojo.TTOBMA.Entries;
import com.vassarlabs.gp.service.api.TtobmaService;
import com.vassarlabs.gp.utils.Utils;
import org.apache.commons.codec.DecoderException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFPicture;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class TtobmaServiceImpl implements TtobmaService {

    @Value("${ttobma.apiUrl}")
    private String ttobmaUrl;

    @Value("${ttobma.indicesApi.requestBody.appSecret}")
    private String appSecret;

    @Value("${ttobma.indicesApi.requestBody.appId}")
    private String appId;

    @Value("${ttobma.indicesApi.requestBody.appVersion}")
    private String appVersion;

    @Value("${ttobma.documents.excel.path}")
    private String ttobmaExcelPath;

    @Value("${ttobma.documents.paperCapacity}")
    private String paperCapacityTemplateExcelName;

    @Value("${ttobma.documents.pulpCapacity}")
    private String pulpCapacityTemplateExcelName;

    @Value("${ttobma.documents.downtimeReport}")
    private String downtimeReportTemplateExcelName;






    @Override
    public String downloadCapacityExcel(String excelName) throws IOException {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(Constants.APP_VERSION, String.valueOf(appVersion));

        String requestBody = "{" +
                "\"app_id\": \"" + appId + "\"," +
                "\"app_secret\": \"" + appSecret + "\"" +
                "}";


        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(ttobmaUrl + APIConstants.TTOBMA_CAPACITY_LISTS_URL, HttpMethod.POST, entity, String.class);

        String responseBody = response.getBody();
        Gson gson = new Gson();


        List<CapacityLists> capacityListsList = gson.fromJson(responseBody, new TypeToken<List<CapacityLists>>(){}.getType());


        if(Objects.equals(excelName, Constants.CapacityExcelKeyName.PAPER_EXCEL.getValue())){
            return appendEntriesToExcelCapacityList(capacityListsList, Constants.paperCapacitySheetNameList, Constants.CAPACITY_LISTS_KEYNAME_TO_EXCEL_NAME_MAP.get(Constants.CapacityExcelKeyName.PAPER_EXCEL.getValue()), paperCapacityTemplateExcelName);
        }else{
            return appendEntriesToExcelCapacityList(capacityListsList, Constants.pulpCapacitySheetNameList,Constants.CAPACITY_LISTS_KEYNAME_TO_EXCEL_NAME_MAP.get(Constants.CapacityExcelKeyName.PULP_EXCEL.getValue()), pulpCapacityTemplateExcelName);
        }

    }

    @Override
    public String downloadDowntimeReportExcel() throws IOException {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(Constants.APP_VERSION, String.valueOf(appVersion));

        String requestBody = "{" +
                "\"app_id\": \"" + appId + "\"," +
                "\"app_secret\": \"" + appSecret + "\"" +
                "}";


        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.exchange(ttobmaUrl + APIConstants.TTOBMA_DOWNTiME_REPORT_URL, HttpMethod.POST, entity, String.class);

        String responseBody = response.getBody();
        Gson gson = new Gson();


        List<DowntimeReport> downtimeReportList = gson.fromJson(responseBody, new TypeToken<List<DowntimeReport>>(){}.getType());

        return appendEntriesToExcelDowntimeReport(downtimeReportList, Utils.getLastMonthName() + Constants.SPACE + Utils.getCurrentYear());
    }


    public String appendEntriesToExcelCapacityList(List<CapacityLists> entriesList, List<String> excelSheetNameList, String excelName, String excelTemplateName) throws IOException {
        try (InputStream inputStream = new FileInputStream(new File(ttobmaExcelPath + excelTemplateName + Constants.EXCEL));
             Workbook workbook = WorkbookFactory.create(inputStream)) {

            for(String sheetName : excelSheetNameList) {
                Sheet sheet = workbook.getSheet(sheetName);

                int rowNum = 2; //TODO : Constants
                for (CapacityLists capacityLists : entriesList) {
                    if (sheetName.equals(capacityLists.getName())) {
                        List<Entries> entries = capacityLists.getEntries();
                        for (Entries entry : entries) {
                            Row row = sheet.createRow(rowNum++);
                            row.createCell(Constants.CAPACITY_LISTS_EXCEL_HEADERS_MAP.get(Constants.CapacityListsExcelHeaders.COMPANY_NAME.getValue())).setCellValue(entry.getCompany());
                            row.createCell(Constants.CAPACITY_LISTS_EXCEL_HEADERS_MAP.get(Constants.CapacityListsExcelHeaders.MILL_LOCATION.getValue())).setCellValue(entry.getMillLocation());
                            row.createCell(Constants.CAPACITY_LISTS_EXCEL_HEADERS_MAP.get(Constants.CapacityListsExcelHeaders.GRADE.getValue())).setCellValue(entry.getGrade());
                            row.createCell(Constants.CAPACITY_LISTS_EXCEL_HEADERS_MAP.get(Constants.CapacityListsExcelHeaders.EFF_DATE.getValue())).setCellValue(entry.getEffectiveDate());
                            row.createCell(Constants.CAPACITY_LISTS_EXCEL_HEADERS_MAP.get(Constants.CapacityListsExcelHeaders.THOUSANDS.getValue())).setCellValue(entry.getTonnes());
                            row.createCell(Constants.CAPACITY_LISTS_EXCEL_HEADERS_MAP.get(Constants.CapacityListsExcelHeaders.NOTES.getValue())).setCellValue(entry.getNote());
                            row.createCell(Constants.CAPACITY_LISTS_EXCEL_HEADERS_MAP.get(Constants.CapacityListsExcelHeaders.STATUS.getValue())).setCellValue(entry.getStatus());

                        }
                    }else if(sheetName.contains(Constants.tissueExcel)){
                        // Extract the year from sheetName
                        String yearStr = sheetName.replaceAll(Constants.EXTRACT_DIGITS_REGEX, Constants.EMPTY_STRING);

                        // Filter and set only entries with the matching year
                        List<Entries> tissueEntries = capacityLists.getEntries().stream().filter(entry -> entry.getEffectiveDate().contains(yearStr)).collect(Collectors.toList());

                        // Create rows for the filtered entries
                        for (Entries entry : tissueEntries) {
                            Row row = sheet.createRow(rowNum++);
                            row.createCell(Constants.CAPACITY_LISTS_EXCEL_HEADERS_MAP.get(Constants.CapacityListsExcelHeaders.COMPANY_NAME.getValue())).setCellValue(entry.getCompany());
                            row.createCell(Constants.CAPACITY_LISTS_EXCEL_HEADERS_MAP.get(Constants.CapacityListsExcelHeaders.MILL_LOCATION.getValue())).setCellValue(entry.getMillLocation());
                            row.createCell(Constants.CAPACITY_LISTS_EXCEL_HEADERS_MAP.get(Constants.CapacityListsExcelHeaders.GRADE.getValue())).setCellValue(entry.getGrade());
                            row.createCell(Constants.CAPACITY_LISTS_EXCEL_HEADERS_MAP.get(Constants.CapacityListsExcelHeaders.EFF_DATE.getValue())).setCellValue(entry.getEffectiveDate());
                            row.createCell(Constants.CAPACITY_LISTS_EXCEL_HEADERS_MAP.get(Constants.CapacityListsExcelHeaders.THOUSANDS.getValue())).setCellValue(entry.getTonnes());
                            row.createCell(Constants.CAPACITY_LISTS_EXCEL_HEADERS_MAP.get(Constants.CapacityListsExcelHeaders.NOTES.getValue())).setCellValue(entry.getNote());
                        }

                    }
                }


                // Auto-size columns
                for (int i = 0; i < 6; i++) {
                    sheet.autoSizeColumn(i);
                }

            }

            FileOutputStream outputStream = new FileOutputStream(ttobmaExcelPath + excelName + Constants.EXCEL);
            workbook.write(outputStream);

            return ttobmaExcelPath + excelName + Constants.EXCEL;

        }catch (IOException e) {
            throw new FileNotFoundException(ErrorMessages.FILE_NOT_FOUND);
        }
    }


    public String appendEntriesToExcelDowntimeReport(List<DowntimeReport> downtimeReportList, String excelName) throws IOException {
        try (InputStream inputStream = new FileInputStream(new File(ttobmaExcelPath + downtimeReportTemplateExcelName + Constants.EXCEL));
             Workbook workbook = WorkbookFactory.create(inputStream)) {

            Sheet sheet = workbook.getSheet(Constants.downtimeReportSheetName);

            int rowNum = 2;
            for (DowntimeReport downtimeReport : downtimeReportList) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(Constants.DOWNTIME_REPORT_EXCEL_HEADERS_MAP.get(Constants.DowntimeReportExcelHeaders.REGION.getValue())).setCellValue(downtimeReport.getRegion());
                row.createCell(Constants.DOWNTIME_REPORT_EXCEL_HEADERS_MAP.get(Constants.DowntimeReportExcelHeaders.COUNTRY.getValue())).setCellValue(downtimeReport.getCountry());
                row.createCell(Constants.DOWNTIME_REPORT_EXCEL_HEADERS_MAP.get(Constants.DowntimeReportExcelHeaders.COMPANY.getValue())).setCellValue(downtimeReport.getCompany());
                row.createCell(Constants.DOWNTIME_REPORT_EXCEL_HEADERS_MAP.get(Constants.DowntimeReportExcelHeaders.MILL.getValue())).setCellValue(downtimeReport.getMill());
                row.createCell(Constants.DOWNTIME_REPORT_EXCEL_HEADERS_MAP.get(Constants.DowntimeReportExcelHeaders.MONTH.getValue())).setCellValue(downtimeReport.getMonth());
                row.createCell(Constants.DOWNTIME_REPORT_EXCEL_HEADERS_MAP.get(Constants.DowntimeReportExcelHeaders.REASON.getValue())).setCellValue(downtimeReport.getReason());
                row.createCell(Constants.DOWNTIME_REPORT_EXCEL_HEADERS_MAP.get(Constants.DowntimeReportExcelHeaders.DAYS_OF_DOWNTIME.getValue())).setCellValue(downtimeReport.getDaysOfDowntime());
                row.createCell(Constants.DOWNTIME_REPORT_EXCEL_HEADERS_MAP.get(Constants.DowntimeReportExcelHeaders.LOST_TONS.getValue())).setCellValue(downtimeReport.getLostTonnes());
            }

            // Auto-size columns
            for (int i = 0; i < 8; i++) {
                sheet.autoSizeColumn(i);
            }

            FileOutputStream outputStream = new FileOutputStream(ttobmaExcelPath + excelName + Constants.EXCEL);
            workbook.write(outputStream);

            return (ttobmaExcelPath + excelName + Constants.EXCEL);
        }catch (IOException e) {
            throw new FileNotFoundException(ErrorMessages.FILE_NOT_FOUND);
        }
    }


}
