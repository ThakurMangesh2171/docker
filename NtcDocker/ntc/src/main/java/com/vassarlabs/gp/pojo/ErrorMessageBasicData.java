package com.vassarlabs.gp.pojo;

import lombok.Data;

@Data
public class ErrorMessageBasicData {

    private String responseRfpId;
    private ResponseRfpErrorMessages responseRfpErrorMessages;

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
}
