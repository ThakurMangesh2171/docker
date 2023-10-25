package com.vassarlabs.gp.repository;

import java.util.List;

public interface CustomRepository {
    List<Object[]> customSearch(String query);

    int updateEntity(String query);
}
