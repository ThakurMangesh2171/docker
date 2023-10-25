package com.vassarlabs.gp.dao.impl;

import com.vassarlabs.gp.dao.api.UserPersonalisedDataDao;
import com.vassarlabs.gp.dao.entity.UserPersonalisedData;
import com.vassarlabs.gp.repository.UserPersonalisedDataRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserPersonalisedDataDaoImpl implements UserPersonalisedDataDao {
    private static final Logger LOGGER = LogManager.getLogger(UserPersonalisedDataDaoImpl.class);

    @Autowired
    private UserPersonalisedDataRepository userPersonalisedDataRepository;


    //Method to fetch user Personalised Details of Particular user
    @Override
    public UserPersonalisedData getUserPersonalisedData(String userId) {
        LOGGER.info("In UserPersonalisedDataDaoImpl :: getUserPersonalisedData");
        return userPersonalisedDataRepository.findByUserid(userId);
    }

    @Override
    public void saveUserPersonalisedData(UserPersonalisedData userPersonalisedData) {
        LOGGER.info("In UserPersonalisedDataDaoImpl :: saveUserPersonalisedData");
        userPersonalisedDataRepository.save(userPersonalisedData);
    }
}
