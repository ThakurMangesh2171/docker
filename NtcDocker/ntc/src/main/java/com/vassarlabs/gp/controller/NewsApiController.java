package com.vassarlabs.gp.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.vassarlabs.gp.constants.APIConstants;
import com.vassarlabs.gp.constants.Constants;
import com.vassarlabs.gp.constants.ErrorMessages;
import com.vassarlabs.gp.exception.InvalidValueProvidedException;
import com.vassarlabs.gp.pojo.ApiResponse;
import com.vassarlabs.gp.pojo.NewsApi.CachedData;
import com.vassarlabs.gp.pojo.SupplierAndMillListPOJO;
import com.vassarlabs.gp.service.api.NewsAPIService;
import com.vassarlabs.gp.service.api.ResponseRfpService;
import com.vassarlabs.gp.service.impl.ApiResponseService;
import com.vassarlabs.gp.service.impl.NewsAPIServiceImpl;
import com.vassarlabs.gp.utils.Utils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@RestController
@Api(description = "News API related Controller")
public class NewsApiController {

    @Autowired
    private ApiResponseService apiResponseService;

    @Autowired
    private NewsAPIService newsAPIService;

    @Autowired
    private ResponseRfpService responseRfpService;

    private static final Logger LOGGER = LogManager.getLogger(NewsApiController.class);

    private Map<String, Map<String, List<Integer>>> familyNameToSetIdsMap = Constants.TIME_PERIOD_FAMILY_NAME_TO_SET_IDS_MAP;

    // Define a rate limiter
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
    private final Semaphore semaphore = new Semaphore(50);


    @ApiOperation(value = "API for getting all indices metadata",notes = "It is used for generating Response Rfp template for Specific Rfp")
    @GetMapping(value = APIConstants.GET_ALL_INDIXES_METADATA)
    public ApiResponse getAllIndicesMetaData() throws JsonProcessingException, InterruptedException {
        LOGGER.info("In NewsApiContoller :: getAllIndicesMetaData");

        Map<String, List<CachedData>> indicesMetadata = new HashMap<>();

        Boolean isCacheEmpty = false;

        for (Map.Entry<String, Map<String, List<Integer>>> entry : familyNameToSetIdsMap.entrySet()) {
            Map<String, List<Integer>> familyMap = entry.getValue();

            for(Map.Entry<String , List<Integer>> familyMapEntry : familyMap.entrySet()){
                String familyName = familyMapEntry.getKey();
                indicesMetadata.put(familyName, new ArrayList<>());

                for (Integer setId : familyMapEntry.getValue()) {
                    CachedData cachedData = newsAPIService.getCachedDataOfSetId(setId, Boolean.TRUE);
                    if(cachedData == null) {
                        isCacheEmpty = true;
                        break;
                    }
                    indicesMetadata.get(familyName).add(cachedData);
                }
            }
        }

        if(Objects.equals(isCacheEmpty, Boolean.TRUE)){
//            indicesMetadata = responseRfpService.fillCache(indicesMetadata);
            return null;
        }


        return apiResponseService.buildApiResponse(HttpStatus.OK, indicesMetadata);
    }

    @ApiOperation(value = "API for scheduler",notes = "It is used to call scheduler method for news Api")
    @PostMapping(value = APIConstants.INDICES_SCHEDULER)
    public ApiResponse runIndicesScheduler() throws InterruptedException {
        LOGGER.info("In NewsApiContoller :: runIndicesScheduler");

        Map<String, List<CachedData>> indicesMetadata = new HashMap<>();


        responseRfpService.fillCache(indicesMetadata);

        return null;
    }

    @ApiOperation(value = "API for updating indices cached data in cache",notes = "It is used to update indices data in cache")
    @PostMapping(value = APIConstants.UPDATE_INDICES)
    public ApiResponse updateIndicesData(@RequestParam Integer setId) throws JsonProcessingException {
        LOGGER.info("In NewsApiContoller :: updateIndicesData");

        CachedData cachedData = newsAPIService.updateCache(setId);

        return apiResponseService.buildApiResponse(HttpStatus.OK, cachedData);
    }

}