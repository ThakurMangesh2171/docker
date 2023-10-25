package com.vassarlabs.gp.dao.impl;

import com.vassarlabs.gp.constants.ErrorMessages;
import com.vassarlabs.gp.dao.api.ApplicationDataDao;
import com.vassarlabs.gp.dao.entity.ApplicationData;
import com.vassarlabs.gp.exception.ResourceNotFoundException;
import com.vassarlabs.gp.repository.ApplicationDataRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ApplicationDataDaoImpl implements ApplicationDataDao {


    @Autowired
    private ApplicationDataRepository applicationDataRepository;

    private static final Logger LOGGER = LogManager.getLogger(ApplicationDataDaoImpl.class);


    //Method to fetch Particular object Details
    @Override
    public ApplicationData getObjectDataByTypeAndId(String type, String objectId) {
        LOGGER.info("In ApplicationDataDaoImpl :: getObjectDataByTypeAndId");
        return applicationDataRepository.findByTypeAndObjectId(type,objectId);
    }



    //Method to delete Particular Object data
    @Override
    public void deleteApplicationDataByTypeAndId(String type, String objectId){
        LOGGER.info("In ApplicationDataDaoImpl :: deleteApplicationDataByTypeAndId");
        ApplicationData applicationData = getObjectDataByTypeAndId(type,objectId);
        if (applicationData == null) {
            throw new ResourceNotFoundException(ErrorMessages.OBJECT_DETAILS_NOT_FOUND);
        }
        applicationDataRepository.deleteByTypeAndObjectId(type,objectId);
    }

    //Method to fetch application data list with Optional Filters on type and SubType and Status
    @Override
    public List<Object[]> getAllApplicationData(Optional<String> type, Optional<String> subType, Optional<String> status) {
        LOGGER.info("In ApplicationDataDaoImpl :: getAllApplicationData");
        StringBuilder query = new StringBuilder("select application_data_uuid , object_id , type , sub_type , status , CAST(json_data -> 'metaInfo' as TEXT) as meta from application_data ");
        boolean isAndRequired = false;
        if(type.isPresent() || subType.isPresent() || status.isPresent()){
            query.append(" where ");
        }
        if(type.isPresent()){
            query.append(" type = '"+type.get()+"' ");
            isAndRequired = true;
        }
        if(isAndRequired && subType.isPresent()){
            query.append(" AND ");
        }
        if(subType.isPresent()){
            query.append(" sub_type = '"+subType.get()+"' ");
            isAndRequired = true;
        }
        if(isAndRequired && status.isPresent()){
            query.append(" AND ");
        }
        if(status.isPresent()){
            query.append(" status = '"+status.get()+"' ");
        }
        query.append(" order by insert_ts ;");
        LOGGER.info("getAllApplicationData Query :: {}",query);
        return applicationDataRepository.customSearch(query.toString());
    }

    //Method to Add or Update Application data
    @Override
    public void addApplicationData(ApplicationData applicationData) {
        LOGGER.info("In ApplicationDataDaoImpl :: addApplicationData");
        applicationDataRepository.save(applicationData);
    }


    //Method to fetch Application Data Details by Application Data Id
    @Override
    public ApplicationData getDetailsByApplicationDataId(String applicationDataId) {
        LOGGER.info("In ApplicationDataDaoImpl :: getDetailsByApplicationDataId");
        return applicationDataRepository.findByApplicationDataId(applicationDataId);
    }

    //method to return Plan and Option Id of given Object Id for particular type and subType
    @Override
    public List<Object[]> getPlanAndOptionId(String objectId, String type, String subType) {
        LOGGER.info("In ApplicationDataDaoImpl :: getPlanAndOptionId");
        return applicationDataRepository.getPlanAndOptionId(objectId,type,subType);
    }


}
