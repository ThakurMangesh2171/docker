package com.vassarlabs.gp.repository;

import com.vassarlabs.gp.dao.entity.ApplicationData;
import com.vassarlabs.gp.dao.entity.UserPersonalisedData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


@Repository
public interface UserPersonalisedDataRepository extends JpaRepository<UserPersonalisedData, String> {

    //For one user only one record will be there
    @Query(value = "select * from user_personalised_data where user_id = :userId order by insert_ts desc limit 1",nativeQuery = true)
    UserPersonalisedData findByUserid(String userId);
}
