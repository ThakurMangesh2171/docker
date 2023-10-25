package com.vassarlabs.gp.pojo;

import java.util.List;

public class ResponseRfpErrorMessages {
    private List<ErrorMessageDetails> errorMessagesJson;



    private List<ErrorMessageDetails> responseRfpWarning;


    public List<ErrorMessageDetails> getErrorMessagesJson() {
        return errorMessagesJson;
    }

    public void setErrorMessagesJson(List<ErrorMessageDetails> errorMessagesJson) {
        this.errorMessagesJson = errorMessagesJson;
    }

    public List<ErrorMessageDetails> getResponseRfpWarning() {
        return responseRfpWarning;
    }

    public void setResponseRfpWarning(List<ErrorMessageDetails> responseRfpWarning) {
        this.responseRfpWarning = responseRfpWarning;
    }
}
