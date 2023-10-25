package com.vassarlabs.gp.service.api;

import com.vassarlabs.gp.pojo.UserPersonalisedDetails;

public interface UserPersonalisedService {
    void saveUserPersonalisedData(String userId, UserPersonalisedDetails userPersonalisedDetails);

    UserPersonalisedDetails getUserPersonalisedData(String userId);
}
