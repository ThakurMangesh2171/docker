package com.vassarlabs.gp.constants;

public class APIConstants {
    public static final String REST_TEMPLATE_EXCEL ="responseRfp/downloadExcelSubmission";

    public static final String SUBMIT_RFP_RESPONSE_TO_RFP_USING_EXCEL = "responseRfp/submitResponseToRfpUsingExcel";


    public static final String GET_ALL_ERROR_MESSAGES_OF_RFP = "responseRfp/getAllErrorMessages/{responseRfpId}";
    public static final String RE_SUBMIT_RESPONSE_RFP = "responseRfp/reSubmit/{responseRfpId}" ;
    public static final String GENERATE_EXCEL_TEMPLATE_FOR_RFP = "responseRfp/generateExcelTemplate";

    public static final String DOWNLOAD_DOCUMENTS = "/downloadDocuments";

    public static final String GENERATE_EXCEL = "responseRfp/generateExcelFromResponseRfpJson";

    public static final String SAVE_UPDATE_ERROR_MESSAGE = "responseRfp/saveOrUpdateErrorMessage" ;



    //simulation
    public static final String FETCH_APPLICATION_DATA_BY_TYPE_AND_ID = "/applicationData";
    public static final String DELETE_APPLICATION_DATA = "/applicationData/delete/{type}/{objectId}";
    public static final String SAVE_APPLICATION_DATA= "/applicationData/save";

    public static final String GET_ALL_APPLICATION_DATA = "/applicationData/FetchAll";
    public static final String DOWNLOAD_RFP_TEMPLATE = "rfp/downloadTemplate/{rfpNumber}";

    public static final String SEND_EXCEL_TEMPLATE_FOR_RFP = "responseRfp/sendExcelTemplate";

    public static final String SAVE_USER_PERSONALISED_DATA ="/userPersonalisation/save/{userId}";
    public static final String FETCH_USER_PERSONALISED_DATA = "/userPersonalisation/get";

    public static final String GET_ALL_INDIXES_METADATA = "/getAllIndicesMetadata";

    public static final String INDICES_SCHEDULER = "/runIndicesScheduler";

    public static final String UPDATE_INDICES = "/updateIndicesData";

    public static final String DOWNLOAD_CAPACITY_EXCEL = "/downloadExcelCapacityLists";

    //TTOBMA Urls

    public static final String TTOBMA_KEY_INDICES_URL = "/get-dataset-app";

    public static final String TTOBMA_CAPACITY_LISTS_URL = "/capacity-lists";

    public static final String TTOBMA_DOWNTiME_REPORT_URL = "/downtime-report";

    public static final String DOWNLOAD_DOWNTIME_EXCEL = "/downloadExcelDowntimeReport";

    public static final String GET_PLAN_AND_OPTION_ID = "/applicationData/getPlanAndOptionId" ;
}
