package com.vassarlabs.gp.pojo;

import com.vassarlabs.gp.pojo.ResponseRfpJson.RfpJsonTemplate;
import lombok.Data;

import java.util.List;

@Data
public class ResponseRfpExcelResponse {
    private List<String> errorMessages;
    private RfpJsonTemplate rfpJsonTemplate;

    private List<ErrorMessageDetails> errorMessageDetails;
    private List<ErrorMessageDetails> responseRfpWarning;

    private String templatePath;
    private String submittedFilePath;
    private List<Mills> mills;
    private String rfpNumber;
    private String dueDate;

    public List<String> getErrorMessages() {
        return errorMessages;
    }

    public void setErrorMessages(List<String> errorMessages) {
        this.errorMessages = errorMessages;
    }

    public RfpJsonTemplate getRfpJsonTemplate() {
        return rfpJsonTemplate;
    }

    public void setRfpJsonTemplate(RfpJsonTemplate rfpJsonTemplate) {
        this.rfpJsonTemplate = rfpJsonTemplate;
    }

    public List<ErrorMessageDetails> getErrorMessageDetails() {
        return errorMessageDetails;
    }

    public void setErrorMessageDetails(List<ErrorMessageDetails> errorMessageDetails) {
        this.errorMessageDetails = errorMessageDetails;
    }

    public List<ErrorMessageDetails> getResponseRfpWarning() {
        return responseRfpWarning;
    }

    public void setResponseRfpWarning(List<ErrorMessageDetails> responseRfpWarning) {
        this.responseRfpWarning = responseRfpWarning;
    }

    public String getTemplatePath() {
        return templatePath;
    }

    public void setTemplatePath(String templatePath) {
        this.templatePath = templatePath;
    }

    public String getSubmittedFilePath() {
        return submittedFilePath;
    }

    public void setSubmittedFilePath(String submittedFilePath) {
        this.submittedFilePath = submittedFilePath;
    }

    public List<Mills> getMills() {
        return mills;
    }

    public void setMills(List<Mills> mills) {
        this.mills = mills;
    }

    public String getRfpNumber() {
        return rfpNumber;
    }

    public void setRfpNumber(String rfpNumber) {
        this.rfpNumber = rfpNumber;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }
}
