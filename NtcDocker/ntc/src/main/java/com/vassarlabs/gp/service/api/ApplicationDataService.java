package com.vassarlabs.gp.service.api;

import java.util.List;
import java.util.Optional;
import com.vassarlabs.gp.pojo.ApplicationDataDetails;
import com.vassarlabs.gp.pojo.PlanDetails;

public interface ApplicationDataService {

    ApplicationDataDetails getObjectDataByTypeAndId(String type, String objectId);

    void deleteApplicationDataByTypeAndId(String type, String objectId);

    List<ApplicationDataDetails> getAllApplicationData(Optional<String> type, Optional<String> subType, Optional<String> status);
    String saveApplicationData(ApplicationDataDetails applicationDataDetails);

    PlanDetails getPlanAndOptionId(String objectId, String type, String subType);
}
