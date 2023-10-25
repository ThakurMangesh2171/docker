package com.vassarlabs.gp.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.vassarlabs.gp.constants.Constants;
import com.vassarlabs.gp.constants.ErrorMessages;
import com.vassarlabs.gp.exception.InvalidValueProvidedException;
import com.vassarlabs.gp.exception.ResourceNotFoundException;
import com.vassarlabs.gp.exception.UpdateCacheFailureException;
import com.vassarlabs.gp.pojo.*;
import com.vassarlabs.gp.pojo.NewsApi.CachedData;
import com.vassarlabs.gp.pojo.ResponseRfpJson.RfpJsonTemplate;
import com.vassarlabs.gp.service.api.IEmailService;
import com.vassarlabs.gp.service.api.NewsAPIService;
import com.vassarlabs.gp.service.api.ResponseRfpService;
import com.vassarlabs.gp.utils.ExcelGenerateUtils;
import com.vassarlabs.gp.utils.ExcelParsingUtils;
import com.vassarlabs.gp.utils.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@Service
public class ResponseRfpServiceImpl implements ResponseRfpService {

    private static final Logger LOGGER = LogManager.getLogger(ResponseRfpServiceImpl.class);

    private static final Map<String, Map<String, List<Integer>>> familyNameToSetIdsMap = Constants.TIME_PERIOD_FAMILY_NAME_TO_SET_IDS_MAP;

    // Define a rate limiter
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

    @Value("${semaphore.permits}")
    private int semaphorePermits;


    @Autowired
    private NewsAPIService newsAPIService;


    @Value("${rfp.response.excel.url}")
    private String restTemplateUrl;
    @Value("${rfp.template.name}")
    private String excelTemplateName;

    @Value("${responseRfp.excel.path}")
    private String responseRfpExcelPath;

    @Autowired
    private IEmailService emailService;

    @Value("${spring.mail.emailBodyComplianceDocumentsApproved}")
    private String emailBody;



    //Method to Submit response to Rfp using Excel
    @Override
    public ResponseRfpExcelResponse submitRfpResponseToRfpUsingExcel(MultipartFile responseRfpExcel, String supplierName) {
        LOGGER.info("In ResponseRfpServiceImpl :: submitRfpResponseToRfpUsingExcel");
        return ExcelParsingUtils.populateResponseRfpJsonFromExcel(responseRfpExcel,null,supplierName);
    }

    //Method to fetch and Download Excel Response Rfp Submission by Rest template using ResponseRfpId
    @Override
    public String downloadRfpResponseSubmissionExcelByResponseRfpId(String responseRfpId, String supplierName) throws IOException,InvalidValueProvidedException {
        LOGGER.info("In ResponseRfpServiceImpl :: downloadRfpResponseSubmissionExcelByResponseRfpId : responseRfpId {}",responseRfpId);
        if(Boolean.TRUE.equals(Utils.checkIfStringIsNullOrEmpty(responseRfpId))){
            throw new InvalidValueProvidedException(ErrorMessages.RESPONSE_RFP_ID_NULL_ERROR);
        }
        RestTemplate restTemplate = new RestTemplate();

//        HttpHeaders headers = new HttpHeaders();
////        headers.add("rfp_response_id",responseRfpId);
//        HttpEntity<String> entity = new HttpEntity<>("parameters",headers);
        // Build the URL with query parameters
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(restTemplateUrl)
                .queryParam("rfp_response_id", responseRfpId);

        // Make the HTTP GET request with query parameters
        ResponseEntity<ByteArrayResource> response = restTemplate.exchange(
                builder.toUriString(),
                HttpMethod.GET,
                null,
                ByteArrayResource.class);

//        ResponseEntity<ByteArrayResource> response = restTemplate.exchange(restTemplateUrl+"?rfp_response_id="+responseRfpId, HttpMethod.GET, entity, ByteArrayResource.class);
        // Check the response status code
        if (response.getStatusCode().is2xxSuccessful()) {
            // Extract the response body (byte array)
            ByteArrayResource resource = response.getBody();
            if (resource != null) {
                // Specify the local path to save the file
                StringBuilder filePath = new StringBuilder();
                filePath.append(responseRfpExcelPath).append(responseRfpId).append(Constants.UNDER_SCORE).append(supplierName).append(Constants.EXCEL);
                // Save the file to the specified path
                FileOutputStream fileOutputStream = new FileOutputStream(new File(String.valueOf(filePath)));
                FileCopyUtils.copy(resource.getByteArray(), fileOutputStream);
                fileOutputStream.close();
                return String.valueOf(filePath);
            }else{
                throw new ResourceNotFoundException(ErrorMessages.UNABLE_TO_FETCH_FILE_FROM_AIRO);
            }
        } else{
            throw new ResourceNotFoundException(ErrorMessages.UNABLE_TO_FETCH_FILE_FROM_AIRO);
        }
    }

    //Method for generating Response Rfp Excel for Specific rfp
    @Override
    public List<String> generateResponseRfpExcelTemplate(String rfpNumber, Integer contractTerm, String fiberType, SupplierAndMillListPOJO supplierAndMillListPOJO) throws IOException {
        LOGGER.info("In ResponseRfpServiceImpl :: generateResponseRfpExcelTemplate");
        List<String> filePathList = new ArrayList<>();
        for (SupplierInfo supplierInfo: supplierAndMillListPOJO.getSupplierInfoList()) {
            String filepath =  ExcelParsingUtils.generateResponseRfpExcelTemplate(rfpNumber, contractTerm, fiberType, supplierAndMillListPOJO.getMIllsList(), excelTemplateName, responseRfpExcelPath,supplierInfo.getName(),supplierInfo.getEmail(),supplierAndMillListPOJO.getDueDate());
            filePathList.add(filepath);
        }
        return filePathList;
    }

    @Override
    public Boolean sendResponseRfpExcelTemplate(String rfpNumber, Integer contractTerm, String fiberType, SupplierAndMillListPOJO supplierAndMillListPOJO) throws ParseException {
        LOGGER.info("In ResponseRfpServiceImpl :: sendResponseRfpExcelTemplate");
        List<String> filePathList = new ArrayList<>();

        Boolean sendMailSuccess = true;

        for (SupplierInfo supplierInfo: supplierAndMillListPOJO.getSupplierInfoList()) {
            String filepath =  ExcelParsingUtils.generateOutPathForResponseRfpExcelTemplate(responseRfpExcelPath, rfpNumber, contractTerm, supplierInfo.getName(), fiberType);

            if(sendMailToSupplier(supplierInfo.getEmail().trim().split(Constants.COMMA_SEPARATED_REGEX), filepath, supplierInfo.getName(), fiberType, supplierAndMillListPOJO.getDueDate()) == Boolean.FALSE){
                sendMailSuccess = false;
                break;
            }
            filePathList.add(filepath);
        }

        return sendMailSuccess;
    }

    @Override
    public Map<String, List<CachedData>> fillCache(Map<String, List<CachedData>> indicesMetadata) throws InterruptedException {

        LOGGER.info("Inside NewsAPIServiceImpl :: fillCache");

        final Semaphore semaphore = new Semaphore(semaphorePermits);

        for (Map.Entry<String, Map<String, List<Integer>>> entry : familyNameToSetIdsMap.entrySet()) {
            Map<String, List<Integer>> familyMap = entry.getValue();

            for (Map.Entry<String, List<Integer>> familyMapEntry : familyMap.entrySet()) {

                String familyName = familyMapEntry.getKey();
                indicesMetadata.computeIfAbsent(familyName, k -> new ArrayList<>());

                List<Integer> setIds = familyMapEntry.getValue();

                for (Integer setId : setIds) {
                    // Acquire a permit (allows 50 concurrent API calls)
                    semaphore.acquire();

                    // Schedule the API call to release the permit in 1 minute
                    executorService.schedule(() -> {
                        try {
                            CachedData cachedData = newsAPIService.updateCache(setId);
                            indicesMetadata.get(familyName).add(cachedData);
                        } catch (JsonProcessingException e) {
                            throw new UpdateCacheFailureException(e.getOriginalMessage());
                        } finally {
                            // Release the permit
                            semaphore.release();
                        }
                    }, 1, TimeUnit.MINUTES);
                }

            }
        }

        return indicesMetadata;
    }


    Boolean sendMailToSupplier(String[] recipients, String filePath, String supplierName, String fiberType, String dueDate) throws ParseException {

        LOGGER.info("In ResponseRfpServiceImpl :: sendMailToSupplier");

        String message = MessageFormat.format(emailBody, supplierName, fiberType, dueDate ,Constants.COMPANY_NAME, Constants.COMPANY_NAME, Constants.SUPPLIERS);

        String subject = Constants.SEND_EXCEL_EMAIL_SUBJECT;


       return emailService.sendMessageWithAttachment(recipients, subject, message,filePath);
    }

    //Method to run scheduler to scrape Data from Datasets based on timePeriod (Weekly,Monthly,Daily,Hourly)
    void runScheduler(String timePeriod) throws InterruptedException {

        final Semaphore semaphore = new Semaphore(semaphorePermits);

        //Fetching Datasets and Associated SetIds for given TimePeriod
        for (Map.Entry<String, List<Integer>> entry : familyNameToSetIdsMap.get(timePeriod).entrySet()) {
            List<Integer> setIds = entry.getValue();

            for (Integer setId : setIds) {
                // Acquire a permit (allows 50 concurrent API calls)
                semaphore.acquire();

                // Schedule the API call to release the permit in 1 minute
                executorService.schedule(() -> {
                    try {
                        newsAPIService.updateCache(setId);
                    } catch (JsonProcessingException e) {
                        throw new UpdateCacheFailureException(e.getOriginalMessage());
                    } finally {
                        // Release the permit
                        semaphore.release();
                    }
                }, 1, TimeUnit.MINUTES);
            }
        }

//        // Shutdown the executor service when you're done with it
//        executorService.shutdown();

//        // Optionally, wait for the tasks to complete
//        try {
//            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//        }
    }

    @Scheduled(cron = "${cron.expression.newsApiHourlyScheduler}")
    public void fetchAllIndicesHourlyData() throws InterruptedException {
        LOGGER.info("In ResponseRfpServiceImpl :: fetchAllIndicesHourlyData");
        runScheduler(Constants.IndicesUpdatationTime.HOURLY.getValue());
    }

    @Scheduled(cron = "${cron.expression.newsApiDailyScheduler}")
    public void fetchAllIndicesDailyData() throws InterruptedException {
        LOGGER.info("In ResponseRfpServiceImpl :: fetchAllIndicesDailyData");
        runScheduler(Constants.IndicesUpdatationTime.DAILY.getValue());
    }

    @Scheduled(cron = "${cron.expression.newsApiWeeklyScheduler}")
    public void fetchAllIndicesWeeklyData() throws InterruptedException {
        LOGGER.info("In ResponseRfpServiceImpl :: fetchAllIndicesWeeklyData");
        runScheduler(Constants.IndicesUpdatationTime.WEEKLY.getValue());
    }

    @Scheduled(cron = "${cron.expression.newsApiMonthlyScheduler}")
    public void fetchAllIndicesMonthlyData() throws InterruptedException {
        LOGGER.info("In ResponseRfpServiceImpl :: fetchAllIndicesMonthlyData");
        runScheduler(Constants.IndicesUpdatationTime.MONTHLY.getValue());
    }

    //Methode to Generate Excel From Json
    @Override
    public String generateExcelFromResponseRfpJson(String responseRfpId, RfpJsonTemplate rfpJsonTemplate, SupplierExcelData supplierExcelMetaData, List<Mills> millsList) throws FileNotFoundException {
        LOGGER.info("In ResponseRfpServiceImpl :: generateExcelFromJson");
        return ExcelGenerateUtils.generateExcelFromResponseRfpJson(responseRfpId,rfpJsonTemplate,responseRfpExcelPath,excelTemplateName,supplierExcelMetaData,millsList);
    }

}
