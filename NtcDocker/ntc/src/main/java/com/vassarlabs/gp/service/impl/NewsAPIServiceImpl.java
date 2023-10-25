package com.vassarlabs.gp.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.Gson;
import com.vassarlabs.gp.constants.APIConstants;
import com.vassarlabs.gp.constants.Constants;
import com.vassarlabs.gp.exception.UpdateCacheFailureException;
import com.vassarlabs.gp.pojo.NewsApi.CachedData;
import com.vassarlabs.gp.pojo.NewsApi.NewsApiResponse;
import com.vassarlabs.gp.pojo.NewsApi.Series;
import com.vassarlabs.gp.pojo.NewsApi.Tile;
import com.vassarlabs.gp.service.api.NewsAPIService;
import com.vassarlabs.gp.utils.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;


@Service
public class NewsAPIServiceImpl implements NewsAPIService {

    private static final Logger LOGGER = LogManager.getLogger(NewsAPIServiceImpl.class);

    @Autowired
    private RestTemplate restTemplate;

    // Define a rate limiter
    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

    private static final Map<String, Map<String, List<Integer>>> familyNameToSetIdsMap = Constants.TIME_PERIOD_FAMILY_NAME_TO_SET_IDS_MAP;


    @Value("${semaphore.permits}")
    private int semaphorePermits;

    @Value("${ttobma.indicesApi.requestBody.appSecret}")
    private String appSecret;

    @Value("${ttobma.indicesApi.requestBody.appId}")
    private String appId;

    @Value("${ttobma.apiUrl}")
    private String ttobmaUrl;

    @Value("${ttobma.indicesApi.requestBody.appVersion}")
    private String appVersion;



    // Calling TTO-BMA Api get-dataset-app for specific setId
    @Override
    public CachedData getDataFromNewsApi(int setId) throws JsonProcessingException {

        String requestBody = "{" +
                "\"set_id\": \"" + setId + "\"," +
                "\"app_id\": \"" + appId + "\"," +
                "\"app_secret\": \"" + appSecret + "\"" +
                "}";


        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(Constants.APP_VERSION, appVersion);

        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

        String apiResponse =  restTemplate.postForObject(ttobmaUrl + APIConstants.TTOBMA_KEY_INDICES_URL, entity, String.class);

        Gson gson = new Gson();
        NewsApiResponse newsApiResponse = gson.fromJson(apiResponse, NewsApiResponse.class);

        Tile tile = newsApiResponse.getTile();

        Float lastValue = (float) 0;
        Float difference = (float) 0;
        Float percentageDifference = 0.0f; // Initialize to 0 to handle division by zero

        if(tile.getSeries() != null){
             Series series = tile.getSeries().get(0);
             List<Float> values = series.getValues();
             lastValue = values.get(values.size() - 1);
             Float secondLastValue = values.get(values.size() - 2);
             difference = lastValue - secondLastValue;
            if (secondLastValue != 0.0f) {
                percentageDifference = ((lastValue - secondLastValue) / secondLastValue) * 100.0f;
            }
        }

        CachedData cachedData = new CachedData();

        if(Objects.equals(Constants.SetIdListBMAPulp.contains(setId), Boolean.TRUE)){
            cachedData.setName(tile.getShort_desc());
        }else {
            cachedData.setName(Utils.removeTrailingHyphenAndDollar(tile.getName()));
        }

        cachedData.setShort_desc2(tile.getShort_desc2());


        // If the set ID belongs to the 'TTO Indices' family, we should calculate the absolute difference; otherwise, we calculate the percentage difference.
        if(Objects.equals(Constants.SetIdListTTOIndices.contains(setId), Boolean.TRUE)){

            String differenceWithSymbol = null;

            if(difference < 0){
//                cachedData.setDifferenceValue(Constants.HYPHEN + Constants.DOLLAR + Utils.getPositiveValueOf(Float.valueOf(Utils.TrimDecimalValue(difference))));
                differenceWithSymbol = Constants.HYPHEN + Constants.DOLLAR + Utils.getPositiveValueOf(Float.valueOf(Utils.TrimDecimalValue(difference)));
            } else {
//                cachedData.setDifferenceValue(Constants.DOLLAR + Float.valueOf(Utils.TrimDecimalValue(difference)));
                differenceWithSymbol = Constants.DOLLAR + Float.valueOf(Utils.TrimDecimalValue(difference));
            }

            cachedData.setDifference(differenceWithSymbol);
        }else{
            cachedData.setDifference(Float.valueOf(Utils.TrimDecimalValue(percentageDifference)) + Constants.PERCENT);
        }

        // If the set ID belongs to the Values Doller list values symbol should be Doller
        if(Objects.equals(Constants.SetIdListValuesDoller.contains(setId), Boolean.TRUE)){
            cachedData.setValue(Constants.DOLLAR + lastValue);

            if(difference < 0){
                cachedData.setDifferenceValue(Constants.HYPHEN + Constants.DOLLAR + Utils.getPositiveValueOf(Float.valueOf(Utils.TrimDecimalValue(difference))));
            } else {
                cachedData.setDifferenceValue(Constants.DOLLAR + Float.valueOf(Utils.TrimDecimalValue(difference)));
            }

        }else{

            if(difference < 0){
                cachedData.setDifferenceValue(Constants.HYPHEN + Utils.getPositiveValueOf(Float.valueOf(Utils.TrimDecimalValue(difference))));
            } else {
                cachedData.setDifferenceValue(Constants.EMPTY_STRING + Float.valueOf(Utils.TrimDecimalValue(difference)));
            }

            cachedData.setValue(String.valueOf(lastValue));
        }

        cachedData.setSetId(setId);

        cachedData.setDifferencePercentage(Float.valueOf(Utils.TrimDecimalValue(percentageDifference)));

        return cachedData;
    }


    // If data is present in cache it will directly return otherwise method will execute and put the result into cache
    @Cacheable(cacheNames = "setId", key ="#setId")
    public CachedData getCachedDataOfSetId(int setId, Boolean fromApi) throws JsonProcessingException {

        LOGGER.info("Inside NewsAPIServiceImpl :: getCachedDataOfSetId, set id {}", setId);

        // This will prevent hitting server many times if data is not present in cache.
        if(Objects.equals(fromApi, Boolean.TRUE)){
            return null;
        }

        CachedData cachedData = getDataFromNewsApi(setId);

        return cachedData;
    }

    // This method will execute the function every time and put the result into cache
    @CachePut(cacheNames = "setId", key ="#setId")
    @Override
    public CachedData updateCache(Integer setId) throws JsonProcessingException {

        LOGGER.info("Inside NewsAPIServiceImpl :: updateCache, set id {}", setId);

        CachedData cachedData = getDataFromNewsApi(setId);

        return cachedData;
    }
}