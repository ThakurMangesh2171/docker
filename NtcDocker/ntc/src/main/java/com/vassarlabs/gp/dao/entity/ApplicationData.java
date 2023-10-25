package com.vassarlabs.gp.dao.entity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vassarlabs.gp.pojo.ApplicationDataJson;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.Data;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.springframework.data.domain.Persistable;

import javax.persistence.*;


@Data
@Entity
@Table(name = "application_data")
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class ApplicationData implements Persistable<String> {
    @Id
    @Column(name = "application_data_uuid")
    private String applicationDataId;

    //Foreign key
    @Column(name = "object_id")
    private String objectId;

    @Column(name = "type")
    private String type;

    @Column(name = "sub_type")
    private String subType;

    @Column(name = "status")
    private String status;


    @Type(type = "jsonb")
    @Column(name = "json_data")
    private ApplicationDataJson applicationJsonData;

    @Column(name = "insert_ts")
    private Long insertTs;

    @Column(name = "updated_ts")
    private Long updatedTs;

    @Transient
    @JsonIgnore
    private transient Boolean isInsert;

    @Override
    public String getId() {
        return this.applicationDataId;
    }

    @Override
    public boolean isNew() {
        return isInsert;
    }
}
