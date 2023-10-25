package com.vassarlabs.gp.repository;


import com.vassarlabs.gp.dao.entity.ErrorMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ErrorMessageRepository extends JpaRepository<ErrorMessage, String>{

    @Query(value = "select * from error_message where response_rfp_id = :responseRfpId limit 1;",nativeQuery = true)
    ErrorMessage getAllErrorMessagesOfResponseRfp(String responseRfpId);
}
