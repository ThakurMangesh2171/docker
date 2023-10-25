package com.vassarlabs.gp.dao.api;

import com.vassarlabs.gp.dao.entity.UserPersonalisedData;

public interface UserPersonalisedDataDao {
    UserPersonalisedData getUserPersonalisedData(String userId);

    void saveUserPersonalisedData(UserPersonalisedData userPersonalisedData);
}
