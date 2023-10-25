package com.vassarlabs.gp.dao.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vassarlabs.gp.pojo.UserPersonalisedDetails;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.Data;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.springframework.data.domain.Persistable;

import javax.persistence.*;

@Data
@Entity
@Table(name = "user_personalised_data")
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class UserPersonalisedData implements Persistable<String> {
    @Id
    @Column(name = "user_personalised_data_uuid")
    private String userPersonalisedDataId;

    @Column(name = "user_id")
    private String userId;

    @Type(type = "jsonb")
    @Column(name = "json_data")
    private UserPersonalisedDetails userPersonalisedDetails;

    @Column(name = "insert_ts")
    private Long insertTs;


    @Column(name = "updated_ts")
    private Long updatedTs;

    @Transient
    @JsonIgnore
    private transient Boolean isInsert;

    @Override
    public String getId() {
        return this.userPersonalisedDataId;
    }

    @Override
    public boolean isNew() {
        return isInsert;
    }

}
