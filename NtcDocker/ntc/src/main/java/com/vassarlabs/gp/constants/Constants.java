package com.vassarlabs.gp.constants;

import com.vassarlabs.gp.pojo.TTOBMA.DowntimeReport;
import com.vassarlabs.gp.utils.Utils;

import java.util.*;

public class Constants {
    public enum BidType {
        MILL_SPECIFIC("Mill Specific"),
        LUMP_SUM("Lump Sum"),
        MILL_SPECIFIC_HYPHEN("Mill-Specific");


        public final String value;

        BidType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public static final String UNIT_OF_MEASURE = "USD/ADMT";
    public static final String PERIOD_TYPE = "Month OR Quarter OR Half Year OR Full Year";

    public enum MECHANISMS {
        INDEX("Index"),
        MOVEMENT("Movement"),
        HYBRID("Hybrid"),
        OTHER("Other");
        public final String value;
        MECHANISMS(String  value) {
            this.value = value;
        }
        public String getValue() {
            return value;
        }
    }

    public static final String PERCENT = "%";
    public static final String  COMMA =",";
    public static final String HYPHEN = "-";
    public static final String DOLLAR = "$";
    public static final String SPACE= " ";
    public static final String UOM_ADMT = "ADMT";
    public static final String UOM_USD = "USD";
    public static final String VALUE_STRING = "Value";
    public static final String PERCENT_STRING = "Percent";
    public static final String WEEK_OF_MONTH_STRING = "week of the Month";

    public static final String BID_QTY_UOM = "ADMT";
    public static final String DATE_FORMAT_FOR_EXCEL = "MM/dd/yyyy";

    public static final String COST_UOM = "USD/ADMT";

    public static final String ISTRUE = "true";
    public static final Character YES = 'Y';
    public static final Character NO = 'N';

    public static final String EMPTY_STRING = "";
    public static final String COLON = " : ";
    public static final String EXCEL = ".xlsx";
    public static final Integer END_COLUMN_FOR_BORDER = 6;

    public static final int ZERO = 0;
    public static final String SLASH = "/";
    public static final String MONTH = " month";
    public static final String FIRST_MONTH = " month";
    public static final String MONTHS = " months";
    public static final String MONTHS_PLURAL =" months";
    public static final String PERIOD_MONTH = "Month";
    public static final String PERIOD_HALF_YEAR = "Half Year";
    public static final String PERIOD_QUARTER_YEAR = "Quarter";
    public static final String PERIOD_FULL_YEAR ="Full Year";

    public static final String UNDER_SCORE = "_";

    public static final String DOT = ".";

    public static final String valueBeetweenParanthesisRegex = "\\((.*?)\\)";

    public static final String COMPANY_NAME = "Georgia Pacific";

    public static final String SEND_EXCEL_EMAIL_SUBJECT = "Request for Proposal (RFP) Response Template Attached";

    public static final String COMMA_SEPARATED_REGEX = "\\s*,\\s*";

    public static final String EXTRACT_DIGITS_REGEX = "[^0-9]";

    public static final String SUPPLIERS = "supplier's";

    public static final String YES_STRING = "Y";
    public static final String NO_STRING = "N";
    public static final String MINUS_STRING = "-";

    public static final long ONE_LONG = 1L;
    public static final String YEAR = "Y";

    public static final String WEEKDAY_OF_MONTH = "Weekday of month";
    public static final String GIVEN_DATE = "Given Date";

    public enum IndicesUpdatationTime {

        MINUTE("Every Minute"),
        HOURLY("Hourly"),
        DAILY("Daily"),
        WEEKLY("Weekly"),
        MONTHLY("Monthly");


        public final String value;

        IndicesUpdatationTime(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public static final String TTOIndicesNorthAmerica = "TTO Indices / Prices - North America, Europe -" + Constants.SPACE + Utils.getLastMonthName() + Constants.SPACE + Utils.getCurrentYear();
    public static final String TTOIndicesChina = "TTO Indices / Prices - China, Fluff Global, BCTMP Global, Dissolving Global, UKP Global -" + Constants.SPACE + Utils.getLastMonthName() + Constants.SPACE + Utils.getCurrentYear();



    public static final Map<String, Map<String,List<Integer>>> TIME_PERIOD_FAMILY_NAME_TO_SET_IDS_MAP = new HashMap<>();
    static{
        TIME_PERIOD_FAMILY_NAME_TO_SET_IDS_MAP.put(IndicesUpdatationTime.HOURLY.getValue(), new HashMap<>());
        TIME_PERIOD_FAMILY_NAME_TO_SET_IDS_MAP.put(IndicesUpdatationTime.DAILY.getValue(), new HashMap<>());
        TIME_PERIOD_FAMILY_NAME_TO_SET_IDS_MAP.put(IndicesUpdatationTime.WEEKLY.getValue(), new HashMap<>());
        TIME_PERIOD_FAMILY_NAME_TO_SET_IDS_MAP.put(IndicesUpdatationTime.MONTHLY.getValue(), new HashMap<>());
    }

    static {
        //Hourly
        TIME_PERIOD_FAMILY_NAME_TO_SET_IDS_MAP.get(IndicesUpdatationTime.HOURLY.getValue()).put("Shanghai Futures Latest Quotes (Near Real Time)", Arrays.asList(177, 178));
        TIME_PERIOD_FAMILY_NAME_TO_SET_IDS_MAP.get(IndicesUpdatationTime.HOURLY.getValue()).put("Stocks (Updated Hourly)", Arrays.asList(10, 11, 12, 13, 14, 15, 16, 17, 18, 20, 21, 22, 25, 26, 27, 28, 152, 153, 154, 155, 156, 157, 158, 159,160,161,162,164,181,201,231,232));

        //Daily
        TIME_PERIOD_FAMILY_NAME_TO_SET_IDS_MAP.get(IndicesUpdatationTime.DAILY.getValue()).put("Shanghai Futures Exchange Prior Day Close (Updated Daily)", Arrays.asList(165, 166, 167, 168, 173, 174, 175, 176, 233, 234));
        TIME_PERIOD_FAMILY_NAME_TO_SET_IDS_MAP.get(IndicesUpdatationTime.DAILY.getValue()).put("Exchange Rates (Updated Daily)", Arrays.asList(1, 2, 3, 4));
        TIME_PERIOD_FAMILY_NAME_TO_SET_IDS_MAP.get(IndicesUpdatationTime.DAILY.getValue()).put("Commodities (Updated Daily)", Arrays.asList(5, 6, 7, 8, 9, 182, 183, 200, 202, 203, 204, 212, 214, 235, 236));

        //Weekly
        TIME_PERIOD_FAMILY_NAME_TO_SET_IDS_MAP.get(IndicesUpdatationTime.WEEKLY.getValue()).put("Freight (Updated Weekly)", Arrays.asList(47, 48, 49, 237));
        TIME_PERIOD_FAMILY_NAME_TO_SET_IDS_MAP.get(IndicesUpdatationTime.WEEKLY.getValue()).put("Estimated Freight Costs - North America (Updated Weekly)", Arrays.asList(254, 255, 256, 257, 258, 259, 260, 261, 262, 263, 264, 265, 266, 267));

        //Monthly
        TIME_PERIOD_FAMILY_NAME_TO_SET_IDS_MAP.get(IndicesUpdatationTime.MONTHLY.getValue()).put(TTOIndicesNorthAmerica, Arrays.asList(75, 76, 77, 83));
        TIME_PERIOD_FAMILY_NAME_TO_SET_IDS_MAP.get(IndicesUpdatationTime.MONTHLY.getValue()).put(TTOIndicesChina, Arrays.asList(62, 63, 64, 75, 76, 77, 82, 83, 84, 85, 65, 78, 80, 81, 86, 87, 88, 89, 90, 91, 169, 170, 171, 172, 197, 198));
        TIME_PERIOD_FAMILY_NAME_TO_SET_IDS_MAP.get(IndicesUpdatationTime.MONTHLY.getValue()).put("BMA Pulp Forecasts(Updated monthly)", Arrays.asList(184, 185, 186, 187, 188, 189, 190, 191, 192, 193, 194, 195, 199));
        TIME_PERIOD_FAMILY_NAME_TO_SET_IDS_MAP.get(IndicesUpdatationTime.MONTHLY.getValue()).put("Pulp Imports / Inventory Levels (Updated Monthly)", Arrays.asList(51, 52, 73, 74, 180, 196, 216, 213));
        TIME_PERIOD_FAMILY_NAME_TO_SET_IDS_MAP.get(IndicesUpdatationTime.MONTHLY.getValue()).put("Other Prices (Updated Monthly)", Arrays.asList(40, 41, 268));
        TIME_PERIOD_FAMILY_NAME_TO_SET_IDS_MAP.get(IndicesUpdatationTime.MONTHLY.getValue()).put("Wood / Woodchips (Updated Monthly)", Arrays.asList(238, 239, 240, 241, 242, 243, 244, 245, 246, 247, 248, 249, 250, 251, 252, 253));
        TIME_PERIOD_FAMILY_NAME_TO_SET_IDS_MAP.get(IndicesUpdatationTime.MONTHLY.getValue()).put("Economics (Updated Monthly)", Arrays.asList(53, 54, 56, 57, 58, 59, 60, 61, 179));
        TIME_PERIOD_FAMILY_NAME_TO_SET_IDS_MAP.get(IndicesUpdatationTime.MONTHLY.getValue()).put("Manufacturing PMI", Arrays.asList(128, 129, 130, 131, 132));
        TIME_PERIOD_FAMILY_NAME_TO_SET_IDS_MAP.get(IndicesUpdatationTime.MONTHLY.getValue()).put("Imports / Inventory Levels", Arrays.asList(133, 134, 135, 136));
    }



    public static final String ABOVE = "above";

    public static final String OTHER_DATA = "If offer is different than the categories above, please explain in this area (please include as much details as possible)";

    public static final String APP_VERSION = "app-version";

    public static final List<Integer> SetIdListTTOIndices = new ArrayList<>(Arrays.asList(62, 63, 64, 75, 76, 77, 82, 83, 84, 85, 65, 78, 80, 81, 86, 87, 88, 89, 90, 91, 169, 170, 171, 172, 197, 198, 75, 76, 77, 83));

    public static final List<Integer> SetIdListValuesDoller = new ArrayList<>(Arrays.asList(62, 63, 64, 75, 76, 77, 82, 83, 84, 85, 65, 78, 80, 81, 86, 87, 88, 89, 90, 91, 169, 170, 171, 172, 197, 198, 75, 76, 77, 83, 184, 185, 186, 187, 188, 189, 190, 191, 192, 193, 194, 195, 199, 178, 165, 167, 173, 175, 233, 47, 48, 49, 237, 254, 255, 256, 257, 258, 259, 260, 261, 262, 263, 264, 265, 266, 267, 15, 16, 17, 18, 19, 20, 21, 22, 25, 26, 27, 28, 152, 154, 157, 159, 160,164, 181, 201, 5,6,7,8,9,214, 182, 183, 200, 40, 250, 251, 252, 253));

    public static final List<Integer> SetIdListBMAPulp = new ArrayList<>(Arrays.asList(184, 185, 186, 187, 188, 189, 190, 191, 192, 193, 194, 195, 199));

    public static final Map<String, Integer> CAPACITY_LISTS_EXCEL_HEADERS_MAP = new HashMap<>();
    static {
        CAPACITY_LISTS_EXCEL_HEADERS_MAP.put("Company Name", 0);
        CAPACITY_LISTS_EXCEL_HEADERS_MAP.put("Mill Location", 1);
        CAPACITY_LISTS_EXCEL_HEADERS_MAP.put("Grade", 2);
        CAPACITY_LISTS_EXCEL_HEADERS_MAP.put("Effective Date", 3);
        CAPACITY_LISTS_EXCEL_HEADERS_MAP.put("000's Tonnes", 4);
        CAPACITY_LISTS_EXCEL_HEADERS_MAP.put("Notes", 5);
        CAPACITY_LISTS_EXCEL_HEADERS_MAP.put("Status", 6);
    }

    public static final Map<String, Integer> DOWNTIME_REPORT_EXCEL_HEADERS_MAP = new HashMap<>();
    static {
        DOWNTIME_REPORT_EXCEL_HEADERS_MAP.put("Region", 0);
        DOWNTIME_REPORT_EXCEL_HEADERS_MAP.put("Country", 1);
        DOWNTIME_REPORT_EXCEL_HEADERS_MAP.put("Company", 2);
        DOWNTIME_REPORT_EXCEL_HEADERS_MAP.put("Mill", 3);
        DOWNTIME_REPORT_EXCEL_HEADERS_MAP.put("Grade", 4);
        DOWNTIME_REPORT_EXCEL_HEADERS_MAP.put("Month", 5);
        DOWNTIME_REPORT_EXCEL_HEADERS_MAP.put("Reason for Problem", 6);
        DOWNTIME_REPORT_EXCEL_HEADERS_MAP.put("Days of downtime", 7);
        DOWNTIME_REPORT_EXCEL_HEADERS_MAP.put("Lost tons", 8);
    }

    public enum CapacityExcelKeyName{
        PAPER_EXCEL("paper"),

        PULP_EXCEL("pulp");

        private String value;

        CapacityExcelKeyName(String value){
            this.value = value;
        }

        public String getValue(){
            return value;
        }
    }

    public enum CapcityExcelName {

        PAPER_CAPACITY_EXCEL("Paper_Capacity_Lists_" + Utils.getLastMonthName() + Constants.UNDER_SCORE + Utils.getCurrentYear()),
        PULP_CAPACITY_EXCEL("Pulp_Capacity_Lists_" + Utils.getLastMonthName() + Constants.UNDER_SCORE + Utils.getCurrentYear());

        public final String value;

        CapcityExcelName(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    //TODO : Compress this map and key name enum
    public static final Map<String, String> CAPACITY_LISTS_KEYNAME_TO_EXCEL_NAME_MAP = new HashMap<>();
    static {
        CAPACITY_LISTS_KEYNAME_TO_EXCEL_NAME_MAP.put(CapacityExcelKeyName.PAPER_EXCEL.getValue(), CapcityExcelName.PAPER_CAPACITY_EXCEL.getValue());
        CAPACITY_LISTS_KEYNAME_TO_EXCEL_NAME_MAP.put(CapacityExcelKeyName.PULP_EXCEL.getValue(), CapcityExcelName.PULP_CAPACITY_EXCEL.getValue());
    }

    public enum CapacityListsExcelHeaders {
        COMPANY_NAME("Company Name"),
        MILL_LOCATION("Mill Location"),
        GRADE("Grade"),
        EFF_DATE("Effective Date"),
        THOUSANDS("000's Tonnes"),
        NOTES("Notes"),

        STATUS("Status");

        public final String value;

        CapacityListsExcelHeaders(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public enum DowntimeReportExcelHeaders {
        REGION("Region"),
        COUNTRY("Country"),
        COMPANY("Company"),
        MILL("Mill"),
        GRADE("Grade"),
        MONTH("Month"),
        REASON("Reason for Problem"),
        DAYS_OF_DOWNTIME("Days of downtime"),
        LOST_TONS("Lost tons");

        public final String value;

        DowntimeReportExcelHeaders(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }


    public static List<String> paperCapacitySheetNameList  = new ArrayList<>(List.of("P&W - North America", "P&W - Europe", "P&W - Asia", "Tissue" + (Utils.getPreviousYear()), "Tissue" + Utils.getCurrentYear(), "Tissue" + (Utils.getNextYear()), "Folding Boxboard"));

    public static List<String> pulpCapacitySheetNameList = new ArrayList<>(List.of( "Softwood", "Hardwood", "Dissolving" ));

    public static String downtimeReportSheetName = "Worksheet";

    public static final String UPDATED_VERSION = "Updated Version";
    public static  String tissueExcel = "Tissue";
    public static final String TWO_SPACE = "  ";

    public static String SIMULATION_TYPE = "Simulation";

    //date Regex for MM/dd/yyyy
    public static final String VALID_DATE_REGEX = "^(0[1-9]|1[0-2])/(0[1-9]|[12][0-9]|3[01])/[0-9]{4}$";
}
