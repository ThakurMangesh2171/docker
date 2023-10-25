package com.vassarlabs.gp.repository;


import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

public class CustomRepositoryImpl implements CustomRepository {
    @PersistenceContext
    private EntityManager entityManager;


    @Override
    public List<Object[]> customSearch(String query) {
        return (List<Object[]>) entityManager.createNativeQuery(query).getResultList();
    }

    @Override
    public int updateEntity(String query) {
        return  entityManager.createNativeQuery(query).executeUpdate();
    }


}
