package com.vassarlabs.gp.service.impl;

import com.vassarlabs.gp.dao.api.UserPersonalisedDataDao;
import com.vassarlabs.gp.dao.entity.UserPersonalisedData;
import com.vassarlabs.gp.pojo.UserPersonalisedDetails;
import com.vassarlabs.gp.service.api.UserPersonalisedService;
import com.vassarlabs.gp.utils.Utils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserPersonalisedServiceImpl implements UserPersonalisedService {

    private static final Logger LOGGER = LogManager.getLogger(UserPersonalisedServiceImpl.class);

    @Autowired
    private UserPersonalisedDataDao userPersonalisedDataDao;

    //Method to Save/Update Particular User Personalised Data
    @Override
    public void saveUserPersonalisedData(String userId, UserPersonalisedDetails userPersonalisedDetails) {
        LOGGER.info("In UserPersonalisedServiceImpl :: saveUserPersonalisedData ");
        //Check if User Details Exists (If yes Update else Save)
        UserPersonalisedData userPersonalisedData = userPersonalisedDataDao.getUserPersonalisedData(userId);
        if(userPersonalisedData==null){
            //save
            userPersonalisedData  = new UserPersonalisedData();
            userPersonalisedData.setUserPersonalisedDataId(Utils.generateRandomUUID());
            userPersonalisedData.setInsertTs(Utils.getCurrentTime());
            userPersonalisedData.setUserId(userId);
            userPersonalisedData.setIsInsert(true);

        }else{
            //Update
            userPersonalisedData.setIsInsert(false);
        }
        userPersonalisedData.setUpdatedTs(Utils.getCurrentTime());
        if(userPersonalisedDetails!=null){
            userPersonalisedData.setUserPersonalisedDetails(userPersonalisedDetails);
        }
        userPersonalisedDataDao.saveUserPersonalisedData(userPersonalisedData);
    }

    //Method to fetch Particular User Personalised Data
    @Override
    public UserPersonalisedDetails getUserPersonalisedData(String userId) {
        LOGGER.info("In UserPersonalisedServiceImpl :: getUserPersonalisedData");
        UserPersonalisedDetails userPersonalisedDetails = new UserPersonalisedDetails();
        UserPersonalisedData userPersonalisedData = userPersonalisedDataDao.getUserPersonalisedData(userId);
        if(userPersonalisedData!=null && userPersonalisedData.getUserPersonalisedDetails()!=null ){
            return userPersonalisedData.getUserPersonalisedDetails();
        }
        return userPersonalisedDetails;
    }
}
