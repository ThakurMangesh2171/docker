package com.vassarlabs.gp.dao.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.vassarlabs.gp.dao.entity.ErrorMessage;
import com.vassarlabs.gp.pojo.ErrorMessageDetails;

import java.util.List;

public interface ErrorMessageDao {

    void saveErrorMessages(ErrorMessage errorMessage);

    void saveAllErrorMessages(List<ErrorMessage> errorMessageList);

    ErrorMessage getAllErrorMessagesOfResponseRfp(String responseRfpId);
}
