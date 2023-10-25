package com.vassarlabs.gp.repository;

import com.vassarlabs.gp.dao.entity.ApplicationData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationDataRepository extends JpaRepository<ApplicationData, String>, CustomRepository{

    @Query(value = "select * from application_data where type = :type and object_id = :objectId order by insert_ts desc limit 1",nativeQuery = true)
    ApplicationData findByTypeAndObjectId(String type, String objectId);

    void deleteByTypeAndObjectId(String type, String objectId);

    ApplicationData findByApplicationDataId(String applicationDataId);

    @Query(value = "select CAST(json_data->'jsonData'->'kpi'->>'selectedComparisonPlanPlanIdInKPI' as TEXT) as planId,  CAST(json_data->'jsonData'->'kpi'->>'selectedComparisonPlanOptionIdInKPI' as TEXT) as optionId, CAST(json_data->'jsonData'->'kpi'->>'selectedComparisonPlanInKPI' as TEXT) as comparisonPlanName from application_data  where  object_id = :objectId and type = :type and sub_type = :subType order by insert_ts desc limit 1",nativeQuery = true)
    List<Object[]> getPlanAndOptionId(String objectId, String type, String subType);
}
