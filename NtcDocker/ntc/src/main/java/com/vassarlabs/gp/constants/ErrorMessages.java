package com.vassarlabs.gp.constants;

public class ErrorMessages {
    public static final String RESOURCE_NOT_FOUND = "Requested resource could not be found.";
    public static final String METHOD_NOT_ALLOWED = "Method Not Allowed";

    //Excel Error Messages
    public static final String DATA_TYPE_MISMATCH = "In sheet : {0} at Cell : {1}, for field {2} Expected Data Type {3} but found {4}";

    public static final String MANDATORY_FIELD_MISSING_ERROR = "In sheet : {1} at Cell {2}, Mandatory Field {0} is missing";

    public static final String CONDITIONAL_MANDATORY_FIELD_ERROR = "In sheet {0} at Cell : {1} field {2} is mandatory when the {3} is set to {4}";
    public static final String CONDITIONAL_MANDATORY_OTHER_THAN_FILED_ERROR = "In sheet {0} at Cell : {1} field {2} is mandatory when the {3} is set to other than {4}";
    public static final String CONDITIONAL_FIELD_ERROR = "In sheet {0} at Cell {1} When {2} is set as {3} Expected value of {4} is {5} but received {6}";
    public static final String REPEATED_SUPPLIER_METADATA_ERROR = "In sheet {0} at cell {1} for same supplier mill name Values of column {2} should be same";
    public static final String REPEATED_PORT_ENTRY_METADATA_ERROR = "In sheet {0} at cell {1} for same supplier mill name and port of entry values of column {2} should be same";
    public static final String REPEATED_INLAND_FREIGHT_METADATA_ERROR = "In sheet {0} at cell {1} for same supplier mill name and port of entry values of column {2} should be same";
    public static final String DATE_MISMATCHED_ERROR = "In Sheet {0} At {1} {2}  Ending Supply Period should be more than Beginning Supply Period";

    public static final String CONDITIONAL_INVALID_FIELD_ERROR = "In sheet {0} at Cell : {1} field {2} value should not be there when the {3} is set to {4}";
    public static final String INVALID_HEADERS_ERROR = "In Sheet {0}  At cell {1}  header Invalid provided = {2} but Expected = {3}";

    public static final String INVALID_FORMAT_ERROR = "In Sheet {0} At cell {1} Invalid format provided {2} but expected in this format {3}";

    public static final String DATE_FORMAT_MISMATCHED_ERROR = "In Sheet : {0} At cell {1} Expected Date in {2} format but provided in {3} format ";

    public static final String SHEET_NOT_FOUND_ERROR = "Sheet {0} not Found";

    public static final String INVALID_VALUE = "In Sheet {0} At cell {1} Invalid value provided ";

    public static final String DATE_BEGGINING_SUPPLY_PERIOD_INVALID_MONTH_ERROR = "In Sheet {0} At Cell {1} Beginning Supply Period should be greater than current month";

    public static final String DATE_ENDING_SUPPLY_PERIOD_INVALID_MONTH_ERROR = "In Sheet {0} At Cell {1} Ending Supply Period should be greater than current month";

    public static final String INVALID_PERIOD_ERROR = "In sheet {0} At Cell {1} The value entered {2} overlaps with previous entries";


    public static final String OVERLAPPING_TIERS_NOT_ALLOWED = "In Sheet :: Commercial pricing At Cell {0} Overlapping Tiers are not allowed at Field :: {1}";
    public static final String INDEX_WEIGHTAGE_OVER_FLOW = "In Sheet :: Commercial pricing At Cell {0} Weightage is Crossed 100% in Period {1} at Field :: {2}";

    public static final String MIN_MAX_DATA_ERROR = "In Sheet {0} At Cell {1} Field :: {2} MIN Value shouldn't be greater or equal to MAX";

    public static final String RESPONSE_RFP_NULL_ERROR = "ResponseRfp Id cannot be null";
    public static final String RFP_NUMBER_NULL_ERROR = "Rfp Number cannot be null";
    public static final String SUPPLIER_NAME_NULL_ERROR = "Supplier Name cannot be null";
    public static final String RESPONSE_RFP_ID_NULL_ERROR = "ResponseRfp Id can't be Null or empty";
    public static final String FILE_NOT_FOUND = "File Not Found";

    public static final String PERCENTAGE_OVER_100 = "In Sheet {0} At Cell {1} Field :: {2}  percentage cant be more than 100% , but given {3}";

    //generate rfp for specific supplier
    public static final String CONTRACT_TERM_NULL_ERROR = "contractTerm can't be NULL";

    public static final String FIBER_TYPE_NULL_ERROR = "fiberType can't be NULL";

    public static final String DUE_DATE_NULL_ERROR = "Due Date Can't be NULL";

    public static final String MAIL_ID_NULL_ERROR = "Email Id can't be NULL";

    public static final String MILL_NAME_NULL_ERROR = "MillName Can't be Null";
    public static final String STATE_NULL_ERROR = "State Can't be Null";
    public static final String EXCEPTED_ANNUAL_VOLUME_NULL_ERROR = "ExpectedAnnualVolume Can't be Null";
    public static final String MILLS_LIST_NULL_ERROR = "MillList Can't be NULL";

    public static final String SUPPLIER_LIST_NULL_ERROR = "Supplier List Can't be NULL";
    public static final String File_NULL_ERROR = "file Not found !!!!";

    public static final String EMAIL_SENT_FAIL = "Some error occured while sending emails";

    public static final String INVALID_EXCEL_NAME_CAPACITY_LISTS = "Excel name should be either paper or pulp";

    // Error Messages Containing only message part

    public enum Messages {
        MANDATORY_FIELD_MISSING_ERROR("Mandatory field {0} is missing"),
        DATA_TYPE_MISMATCH("Expected data type {0} but found {1}"),
        CONDITIONAL_MANDATORY_FIELD_ERROR("Field {0} is mandatory when the {1} is set to {2}"),
        CONDITIONAL_MANDATORY_OTHER_THAN_FILED_ERROR("Field {0} is mandatory when the {1} is set to other than {2}"),

        CONDITIONAL_FIELD_ERROR("When {0} is set as {1} expected value of {2} is {3} but received {4}"),

        CONDITIONAL_FIELD_OTHER_THAN_ERROR("When {0} is set as {1} expected value of {2} is Other than {3} but received {4}"),
        INVALID_FIELD_VALUE_ERROR("Expected field {0}, but found {1}"),

        REPEATED_PORT_ENTRY_METADATA_ERROR("For same Port of Entry, values of column {0} should be same"),

        REPEATED_SUPPLIER_MILL_PORT_ENTRY_HEADER_ERROR("For same Supplier Mill and Port of Entry values of column {0} should be same"),

        REPEATED_INLAND_FREIGHT_METADATA_ERROR("For same Supplier Mill name, Port of Entry and GP mill values of column {0} should be same"),

        REPEATED_SUPPLIER_MILL_PORT_ENTRY_METADATA("For same Supplier Mill name, Port of Entry and GP mill values of column {0} should be same"),

        REPEATED_SUPPLIER_METADATA_ERROR("For same Supplier Mill name, values of column {0} should be same"),
        REPEATED_PERIOD_DETAILS_ERROR("For same period, values of column {0} should be same"),
        DATE_BEGINNING_SUPPLY_PERIOD_INVALID_MONTH_ERROR("Beginning Supply Period should be greater than current month"),
        DATE_BEGINNING_SUPPLY_PERIOD_INVALID_BASED_ON_CONTRACT_TERM_ERROR("Beginning Supply Period should be Between {0} - {1} but Provided {2}"),

        DATE_ENDING_SUPPLY_PERIOD_INVALID_BASED_ON_CONTRACT_TERM_ERROR("Ending Supply Period should be Between {0} - {1} but Provided {2}"),

        DATE_ENDING_SUPPLY_PERIOD_INVALID_MONTH_ERROR("Ending Supply Period should be greater than current month"),
        DATE_MISMATCHED_ERROR("Ending Supply Period should be more than Beginning Supply Period"),
        INVALID_FORMAT_ERROR("Invalid format provided {0} but expected in this format {1}"),
        HEADER_INVALID("Header provided {0}, is Invalid, expected {1}"),
        ADDITIONAL_DISCOUNT_OVER_FLOW("Additional Discount has crossed 100% but given {0} "),
        TIME_WINDOW_ERROR("Invalid value provided, it should contain values as 1 Month or 2 Months and should not be greater than 12, but given Value is {0}"),
        PERCENTAGE_OVER_100(" percentage cant be more than 100%, but given {0}"),
        INDEX_WEIGHTAGE_OVER_FLOW(" Weightage is Crossed 100% in Period {0}"),
        MIN_MAX_DATA_ERROR("MIN Value shouldn't be greater or equal to MAX, but given {0}"),
        OVERLAPPING_TIERS_NOT_ALLOWED("Overlapping Tiers are not allowed"),
        PERIOD_MANDATORY_VALUES("Values for Period should be given for whole year, Ex: M1-M12 or Q1-Q4 or H1-H2 or Year 1, but given value is {0}"),

        BID_QTY_DETAILS_MISSING_ERROR("Bid Quantity Details Missing for Supplier Mill - {0}"),
        LOGISTIC_SHEET_DETAILS_MISSING_ERROR("Logistic sheet Details Missing for Supplier Mill {0}"),
        INVALID_INCOTERMS_ERROR("For Same US Port Entry And Incoterm values of column {0} should be same"),
        INVALID_PERIOD_ERROR("The value entered {0} overlaps with previous entries"),
        PORT_OF_ENTRY_MISMATCH("Port of Entries not matching with Port Of Entry details given in Freight details"),

        DATA_FORMAT_MISMATCH("Value should be in {0} format but found {1}"),
        HEADERS_NOT_FOUND("Headers {0} not found"),

        PERIOD_MISSING_ERROR("Period values of Column {0} should cover whole year"),

        MECHANISM_BASIS_IS_MANDATORY("Pricing Mechanism Basis is mandatory, need to fill details in any one Mechanism Basis"),

        MECHANISM_BASIS_ONLY_ONE("Pricing Mechanism Basis to be filled for only one Mechanism, where in it is filled for more than one mechanism"),
        NON_MANDATORY_FIELD("{0} should be empty when {0} is selected as N"),
        EXPECTED_NULL_VALUE("Do not enter data in Cell because BidType is {1} but Found {0}"),

        MISSING_HEADER("Expected Header {0} not found"),

        GP_MILL_NOT_REQUIRED_IF_LUMP_SUM("Don't select GP Mill if BidType is Lump Sum"),

        REPEATED_INCOTERMS_ERROR("For same Supplier Mill and Port of Entry value, Incoterms list should be same"),
        SUPPLIER_MILL_VOLUME_SHOULD_BE_EMPTY_OR_ZERO("If Supplier Mill is Empty or Zero then volume should be empty or 0, but provided {0}"),

        PORT_ENTRY_REQUIRE_ERROR("Expected Port of Entry : Port Name or Domestic"),
        PRICE_EFFECTIVE_PERIOD_MANDATORY("Price Ceiling/Floor Effective Period is mandatory once Price Ceil or Price Floor has been entered."),

        INVALID_US_PORT("US port of entry {0} is invalid when Supplier is {1}"),

        INCOTERMS_ALREADY_PROVIDED("Incoterm values for {0} have been already provided"),
        BID_VOLUME_MISMATCHED_FOR_LUMP_SUM("Bid Volume Is Mis Matched It Should be {0} but provided {1}"),
        BID_VOLUME_MISMATCHED_FOR_MILL_SPECIFIC_FOR_TWO_SHEET("Volume Mismatched For Supplier Mill Name = {0}, GpMillName = {1}, Value from BidQtySheet = {2}, Value from Logistic Sheet = {3}"),
        BID_VOLUME_MISMATCHED_FOR_LUMP_SUM_MILL_FOR_TWO_SHEET("Volume Mismatched For Supplier Mill Name = {0}, Value from BidQtySheet = {1},  Value from Logistic Sheet = {2}"),

        SECOND_PORT_SAME_ERROR("Second Port of entry should be different from first port of entry");


        public final String message;

        Messages(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    public static final String PERIOD_MANDATORY_VALUES = "In Sheet {0}  Field :: {1} Values for Period should be given for whole Year , Ex: M1-M12 or Q1-Q4 or H1-H2 or Year 1, but given only {2} ";
    public static final String TIME_WINDOW_ERROR = "In Sheet {0} At cell {1} Invalid value provided , it should contain Ex : 1 month or 2 Months and should not be greater than 12 ,but given Value is {2} ";

    public static final String ERROR_SAVING_FILE = "Error While Saving File" ;

    public static final String FILE_NAMES_NULL_ERROR = " File Names Cannot be NULL";

    public static final String FILE_PERMISSION_ERROR = "File don't have Permissions";

    public static final String FILE_FORMAT_NOT_FOUND = "File Format Not Found";

    public static final String FILE_READING_ERROR = "Error While reading the file" ;

    public static final String ADDITIONAL_DISCOUNT_OVER_FLOW = "In Sheet :: Commercial pricing At Cell {0} Additional Discount is Crossed 100% but given  {1} at Field :: {2}";

    public static final String OBJECT_DETAILS_NOT_FOUND = "Object Details does not exist" ;

    public static final String TYPE_NULL_ERROR = "Type cant be Null";

    public static final String OBJECT_ID_NULL_ERROR = "Object id cant be Null";

    public static final String STATUS_NULL_ERROR = "Status cant be Null";

    public static final String SUB_TYPE_NULL_ERROR = "SubType cant be Null";

    public static final String LOGISTIC_MISSING_GP_MILLS = "Logistic Pricing details are missing for these GP mills : ";

    public static final String LOGISTIC_NOT_REQUIRED_GP_MILLS = "Logistics Pricing details not required for {0} as Supplier volume is 0 for {1} - {0} in Bid Details. ";

    public static final String UNABLE_TO_FETCH_FILE_FROM_AIRO = "Unable to fetch file from Airo";

    public static final String INTERNAL_SERVER_ERROR = "Something went wrong. Please try again later.";

    public static final String USER_ID_NULL_ERROR = "User Id Can't be Null";

    public static final String RFP_JSON_TEMPLATE_NULL_ERROR = "RfpJsonTemplate  Can't be Null";

    public static final String SUPPLIER_EXCEL_META_DATA_NULL_ERROR = "Supplier Excel Meta Data Can't be Null";

    public static final String RESPONSE_RFP_EXCEL_DATA_NULL = "Response Rfp Excel Data Can't be NULL";

    public static final String DATA_DECODING_ERROR = "An error occurred while applying colors in the Excel file";

    public static final String COMPARISON_PLAN_DETAILS_NOT_FOUND = "Comparison Plan Details Not found";

    public static final String ERROR_MESSAGE_DATA_NOT_FOUNT= "Error Message Data Does Not Exit";




}
