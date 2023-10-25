package com.vassarlabs.gp.dao.api;
import com.vassarlabs.gp.dao.entity.ApplicationData;

import java.util.List;
import java.util.Optional;


public interface ApplicationDataDao {

    ApplicationData getObjectDataByTypeAndId(String type, String objectId);

    void deleteApplicationDataByTypeAndId(String type, String objectId);


    void addApplicationData(ApplicationData applicationData);
    List<Object[]> getAllApplicationData(Optional<String> type, Optional<String> subType, Optional<String> status);

    ApplicationData getDetailsByApplicationDataId(String applicationDataId);

    List<Object[]>  getPlanAndOptionId(String objectId, String type, String subType);
}
