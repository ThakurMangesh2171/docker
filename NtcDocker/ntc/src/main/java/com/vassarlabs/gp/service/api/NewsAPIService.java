package com.vassarlabs.gp.service.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.vassarlabs.gp.pojo.NewsApi.CachedData;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


@Service
public interface NewsAPIService {

    CachedData getDataFromNewsApi(int setId) throws JsonProcessingException;
    @Cacheable(value = "setId", key ="#setId")
    CachedData getCachedDataOfSetId(int setId, Boolean fromApi) throws JsonProcessingException;

    @CachePut(cacheNames = "setId", key ="#setId")
    CachedData updateCache(Integer setId) throws JsonProcessingException;
}