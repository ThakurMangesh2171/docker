package com.vassarlabs.gp.pojo;

import com.vassarlabs.gp.constants.ErrorMessages;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ApplicationDataDetails {
    private String applicationDataId;

    @NotBlank(message = ErrorMessages.OBJECT_ID_NULL_ERROR)
    private String objectId;
    @NotBlank(message = ErrorMessages.TYPE_NULL_ERROR)
    private String type;

//    @NotBlank(message = ErrorMessages.SUB_TYPE_NULL_ERROR)
    private String subType;

//    @NotBlank(message = ErrorMessages.STATUS_NULL_ERROR)
    private String status;
    private ApplicationDataJson applicationDataJson;
}
