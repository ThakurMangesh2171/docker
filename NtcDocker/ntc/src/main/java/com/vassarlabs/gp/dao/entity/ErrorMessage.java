package com.vassarlabs.gp.dao.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vassarlabs.gp.pojo.ResponseRfpErrorMessages;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.Data;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.springframework.data.domain.Persistable;

import javax.persistence.*;


@Data
@Entity
@Table(name = "error_message")
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class ErrorMessage implements Persistable<String> {
    @Id
    @Column(name = "error_message_uuid")
    private String msgUuid;

    @Column(name = "response_rfp_id")
    private String responseRfpId;

    @Type(type = "jsonb")
    @Column(name = "error_messages_json")
    private ResponseRfpErrorMessages responseRfpErrorMessages;


    @Column(name = "insert_ts")
    private Long insertTs;

    @Column(name = "updated_ts")
    private Long updatedTs;


    @Transient
    @JsonIgnore
    private transient Boolean isInsert ;

    public String getMsgUuid() {
        return msgUuid;
    }

    public void setMsgUuid(String msgUuid) {
        this.msgUuid = msgUuid;
    }

    public String getResponseRfpId() {
        return responseRfpId;
    }

    public void setResponseRfpId(String responseRfpId) {
        this.responseRfpId = responseRfpId;
    }
    public ResponseRfpErrorMessages getResponseRfpErrorMessages() {
        return responseRfpErrorMessages;
    }

    public void setResponseRfpErrorMessages(ResponseRfpErrorMessages responseRfpErrorMessages) {
        this.responseRfpErrorMessages = responseRfpErrorMessages;
    }

    public Long getInsertTs() {
        return insertTs;
    }

    public void setInsertTs(Long insertTs) {
        this.insertTs = insertTs;
    }

    public Long getUpdatedTs() {
        return updatedTs;
    }

    public void setUpdatedTs(Long updatedTs) {
        this.updatedTs = updatedTs;
    }

    public Boolean getInsert() {
        return isInsert;
    }

    public void setInsert(Boolean insert) {
        isInsert = insert;
    }

    @Override
    public String getId() {
        return this.msgUuid;
    }

    @Override
    public boolean isNew() {
        return isInsert;
    }
}
