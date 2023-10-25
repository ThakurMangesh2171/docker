package com.vassarlabs.gp.dao.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassarlabs.gp.constants.ErrorMessages;
import com.vassarlabs.gp.dao.api.ErrorMessageDao;
import com.vassarlabs.gp.dao.entity.ErrorMessage;
import com.vassarlabs.gp.pojo.ErrorMessageDetails;
import com.vassarlabs.gp.repository.ErrorMessageRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class ErrorMessageDaoImpl implements ErrorMessageDao {

    private static final Logger LOGGER = LogManager.getLogger(ErrorMessageDaoImpl.class);

    @Autowired
    private ErrorMessageRepository errorMessageRepository;



    //Method to save Error Messages
    @Override
    public void saveErrorMessages(ErrorMessage errorMessage) {
        LOGGER.info("In ErrorMessageDaoImpl :: saveErrorMessages");
        errorMessageRepository.save(errorMessage);
    }

    //Method to save List of Error Messages
    @Override
    public void saveAllErrorMessages(List<ErrorMessage> errorMessageList) {
        LOGGER.info("In ErrorMessageDaoImpl :: saveAllErrorMessages");
        errorMessageRepository.saveAll(errorMessageList);
    }

    //Method to fetch List of Error Messages for a particular Response Rfp
    @Override
    public ErrorMessage getAllErrorMessagesOfResponseRfp(String responseRfpId)  {
        LOGGER.info("In ErrorMessageDaoImpl :: getAllErrorMessagesOfResponseRfp");
        return errorMessageRepository.getAllErrorMessagesOfResponseRfp(responseRfpId);
    }


}
