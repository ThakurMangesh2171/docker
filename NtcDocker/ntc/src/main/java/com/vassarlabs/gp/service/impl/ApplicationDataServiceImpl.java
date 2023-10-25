package com.vassarlabs.gp.service.impl;

import com.google.gson.Gson;
import com.vassarlabs.gp.constants.Constants;
import com.vassarlabs.gp.constants.ErrorMessages;
import com.vassarlabs.gp.constants.ExcelConstants;
import com.vassarlabs.gp.dao.api.ApplicationDataDao;
import com.vassarlabs.gp.dao.entity.ApplicationData;
import com.vassarlabs.gp.exception.ResourceNotFoundException;

import com.vassarlabs.gp.pojo.ApplicationDataDetails;
import com.vassarlabs.gp.pojo.ApplicationDataJson;
import com.vassarlabs.gp.pojo.PlanDetails;
import com.vassarlabs.gp.service.api.ApplicationDataService;
import com.vassarlabs.gp.utils.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ApplicationDataServiceImpl implements ApplicationDataService {
    private static final Logger LOGGER = LogManager.getLogger(ApplicationDataServiceImpl.class);

    @Autowired
    private ApplicationDataDao applicationDataDao;

    @Autowired
    private Gson gson;


    //Method to fetch Particular object Details
    @Override
    public ApplicationDataDetails getObjectDataByTypeAndId(String type, String objectId) throws ResourceNotFoundException {
        LOGGER.info("In ApplicationDataServiceImpl :: getObjectDataByTypeAndId");
        ApplicationData applicationData = applicationDataDao.getObjectDataByTypeAndId(type,objectId);
        if (applicationData == null) {
            throw new ResourceNotFoundException(ErrorMessages.OBJECT_DETAILS_NOT_FOUND);
        }
        return mapApplicationDataEntityToPojo(applicationData);
    }

    //Method to fetch Map Application data Entity to POJO
    private ApplicationDataDetails mapApplicationDataEntityToPojo(ApplicationData applicationData) {
        ApplicationDataDetails applicationDataDetails = new ApplicationDataDetails();
        applicationDataDetails.setApplicationDataId(applicationData.getApplicationDataId());
        applicationDataDetails.setObjectId(applicationData.getObjectId());
        if(applicationData.getStatus()!=null){
            applicationDataDetails.setStatus(applicationData.getStatus());
        }
        applicationDataDetails.setType(applicationData.getType());
        if(applicationData.getSubType()!=null){
            applicationDataDetails.setSubType(applicationData.getSubType());
        }
        //Non Mandatory Fields null check
        if (applicationData.getApplicationJsonData() != null) {
            applicationDataDetails.setApplicationDataJson(applicationData.getApplicationJsonData());
        }
        return applicationDataDetails;
    }


    //Method to delete Particular Object data
    @Transactional
    @Override
    public void deleteApplicationDataByTypeAndId(String type, String objectId) {
        LOGGER.info("In ApplicationDataServiceImpl :: deleteApplicationDataByTypeAndId");
        applicationDataDao.deleteApplicationDataByTypeAndId(type,objectId);

    }


    //Method to fetch application data list with Optional Filters on type and SubType and Status
    public List<ApplicationDataDetails> getAllApplicationData(Optional<String> type, Optional<String> subType, Optional<String> status) {
        LOGGER.info("In ApplicationDataServiceImpl :: getAllApplicationData");
        List<ApplicationDataDetails> applicationDataDetailsList = new ArrayList<>();
        List<Object[]> applicationDataObj =  applicationDataDao.getAllApplicationData(type,subType,status);
        if(applicationDataObj!=null && !applicationDataObj.isEmpty()){
            for(Object[] obj : applicationDataObj){
                ApplicationDataDetails applicationDataDetails = new ApplicationDataDetails();
                applicationDataDetails.setApplicationDataId((String) obj[0]);
                applicationDataDetails.setObjectId((String) obj[1]);
                applicationDataDetails.setType((String) obj[2]);
                if(obj[3]!=null){
                    applicationDataDetails.setSubType((String) obj[3]);
                }
                if(obj[4]!=null){
                    applicationDataDetails.setStatus((String) obj[4]);
                }
                if(obj[5]!=null){
                    ApplicationDataJson applicationDataJson = new ApplicationDataJson();
                    Object metaObject = gson.fromJson((String) obj[5],Object.class);
                    applicationDataJson.setMetaInfo(metaObject);
                    applicationDataDetails.setApplicationDataJson(applicationDataJson);
                }
                applicationDataDetailsList.add(applicationDataDetails);
            }
        }
        return applicationDataDetailsList;
    }

    //Method to Add or Update Application data
    @Override
    public String saveApplicationData(ApplicationDataDetails applicationDataDetails) throws ResourceNotFoundException{
        LOGGER.info("In ApplicationDataServiceImpl :: saveApplicationData");
        if (applicationDataDetails.getApplicationDataId() !=null && !applicationDataDetails.getApplicationDataId().trim().isEmpty()){
            updateApplicationData(applicationDataDetails);
            return applicationDataDetails.getApplicationDataId();
        }else {
            return addApplicationData(applicationDataDetails);
        }
    }

    //Method to add application data
    private String addApplicationData(ApplicationDataDetails applicationDataDetails) {
        LOGGER.info("In ApplicationDataServiceImpl :: addApplicationData");
        ApplicationData applicationData = new ApplicationData();
        applicationData.setApplicationDataId(Utils.generateRandomUUID());
        applicationData.setObjectId(applicationDataDetails.getObjectId());
        if(applicationDataDetails.getStatus()!=null){
            applicationData.setStatus(applicationDataDetails.getStatus());
        }
        applicationData.setType(applicationDataDetails.getType());
        if(applicationDataDetails.getSubType()!=null) {
            applicationData.setSubType(applicationDataDetails.getSubType());
        }
        if(applicationDataDetails.getApplicationDataJson()!=null){
            applicationData.setApplicationJsonData(applicationDataDetails.getApplicationDataJson());
        }
        applicationData.setInsertTs(Utils.getCurrentTime());
        applicationData.setUpdatedTs(Utils.getCurrentTime());
        applicationData.setIsInsert(true);
        applicationDataDao.addApplicationData(applicationData);
        return applicationData.getApplicationDataId();
    }

    //Method to update application data
    private void updateApplicationData(ApplicationDataDetails applicationDataDetails) throws ResourceNotFoundException{
        LOGGER.info("In ApplicationDataServiceImpl :: updateApplicationData");

        // fetching simulation Details from DB
        ApplicationData applicationData;
        if(applicationDataDetails.getType().equals(Constants.SIMULATION_TYPE)){
            applicationData = applicationDataDao.getDetailsByApplicationDataId(applicationDataDetails.getApplicationDataId());
        }else{
            applicationData = applicationDataDao.getObjectDataByTypeAndId(applicationDataDetails.getType(), applicationDataDetails.getObjectId());
        }

        if (applicationData == null){
            throw new ResourceNotFoundException(ErrorMessages.OBJECT_DETAILS_NOT_FOUND);
        }
        applicationData.setObjectId(applicationDataDetails.getObjectId());
        if(applicationDataDetails.getStatus()!=null){
            applicationData.setStatus(applicationDataDetails.getStatus());
        }else{
            applicationData.setStatus(null);
        }
        applicationData.setType(applicationDataDetails.getType());
        if(applicationDataDetails.getSubType()!=null){
            applicationData.setSubType(applicationDataDetails.getSubType());
        }else{
            applicationData.setSubType(null);
        }
        if(applicationDataDetails.getApplicationDataJson()!=null){
            applicationData.setApplicationJsonData(applicationDataDetails.getApplicationDataJson());
        }else{
            applicationData.setApplicationJsonData(null);
        }
        applicationData.setUpdatedTs(Utils.getCurrentTime());
        applicationData.setIsInsert(false);
        applicationDataDao.addApplicationData(applicationData);
    }


    //Method to fetch Plan and Option Id of given ObjectId  for Particular type and sub type
    @Override
    public PlanDetails getPlanAndOptionId(String objectId, String type, String subType) throws ResourceNotFoundException {
        LOGGER.info("In ApplicationDataServiceImpl :: getPlanAndOptionId");
        PlanDetails planDetails = new PlanDetails();
        List<Object[]> planAndObjectId = applicationDataDao.getPlanAndOptionId(objectId,type,subType);
        if(planAndObjectId!=null && !planAndObjectId.isEmpty()) {
            for (Object[] obj : planAndObjectId) {
                if (obj[0] != null) {
                    planDetails.setPlanId((String) obj[0]);
                }
                if (obj[1] != null) {
                    planDetails.setOptionId((String) obj[1]);
                }
                if (obj[2] != null) {
                    planDetails.setComparisonPlanName(obj[2].toString());
                }
            }
        }
        //If Details does not exist throw error
        if(planDetails.getPlanId()==null){
            throw new ResourceNotFoundException(ErrorMessages.COMPARISON_PLAN_DETAILS_NOT_FOUND);
        }
        return planDetails;
    }



}
