package com.vassarlabs.gp.constants;

import io.swagger.models.auth.In;

import java.util.*;

public class ExcelConstants {


    //Response Rfp Excel Sheet Related Constants
    public enum ResponseRfpExcelSheetNames {
        BID_DETAILS("Bid Details"),
        LOGISTIC_PRICING("Logistics Pricing"),
        COMMERCIAL_PRICING("Commercial Pricing");
        public final String sheetName;

        ResponseRfpExcelSheetNames(String sheetName) {
            this.sheetName = sheetName;
        }

        public String getSheetName() {
            return sheetName;
        }
    }

    public enum InlandTransitOriginType {
        PORT_OF_ENTRY("Port of Entry"),

        WAREHOUSE("Warehouse"),

        SUPPLIER_MILL("Supplier Mill");
        public final String sourceType;

        InlandTransitOriginType(String sourceType) {
            this.sourceType = sourceType;
        }

        public String getValue() {
            return sourceType;
        }
    }

    public enum InlandTransitDestinationType {
        GP_MILL("GP Mill");
        public final String destType;

        InlandTransitDestinationType(String destType) {
            this.destType = destType;
        }

        public String getValue() {
            return destType;
        }
    }

    public enum ResponseRfpExcelHeaders {

        //Bid Qty Sheet Headers
        BID_TYPE("Mill-Specific/Lump Sum *"),

        VOLUME_BID_FOR_LUMP_SUM("Volume Bid (if Lump Sum)"),
        //        SHIP_FROM_MILL_LOCATION_FOR_LUMP_SUM("Ship From Mill Location (if Lump Sum)"),
        BEGINNING_SUPPLIER_PERIOD("Beginning Supply Period *"),
        ENDING_SUPPLIER_PERIOD("Ending Supply Period *"),
        BID_VOLUME_VARIANCE("Bid Volume Variance (%) *"),
        NAME_OF_SUPPLIER_MILL_NAME("Name of Supplier Mill 1, ADMT (D)"),
        GP_MILL_START_ROW("GP MILL START ROW"),

        GP_MILL_EXAMPLE_ROW("Gp Mill Example Row"),
        GP_MILL_COL_NUMBER("Gp mill Column num"),
        TOTAL_BID_VOLUME_COL_NUM("Total"),
        LUMP_SUM_SUPPLIER_NAME("Supplier Mill Name for LumpSum"),

        SUPPLIER_MILL_A("Supplier Mill [A]"),
        SUPPLIER_MILL_A_VOLUME("Supplier Mill [A] Volume (ADMT)"),
        SUPPLIER_MILL_B("Supplier Mill [B] (if applicable or bidding from multiple Mills to one GP location)"),
        SUPPLIER_MILL_B_VOLUME("Supplier Mill [B] Volume (ADMT)"),

        SUPPLIER_MILL_LAST_COL_NUM("Supplier Mill Name Last column Number"),
        SUPPLIER_MILL_START_ROW_NUM("Supplier Mill Name Start Row Number"),
        TOTAL_BID_VOLUME_VALUE_COL("Total value of Supplier Bid Vol Column Num"),
        GP_MILL_STATE_COL("Gp mill State Col Num"),
        GP_MILL_EXPECTED_ANNUAL_VOLUME_COL_NUM("Gp mill Expected Annual Volume Col Num"),

        GP_MILL_CELL_NUM("GP Mill"),
        GP_MILL_STATE_CELL_NUM("State"),
        GP_MILL_EXPECTED_ANNUAL_VOLUME_CELL_NUM("Expected Annual Volume (ADMT)"),
        GP_MILL_SUPPLIER_VOLUME_BID_CELL_NUM("Supplier's Volume Bid, ADMT "),

        RFP_NUMBER("RFP Number: "),
        CONTRACT_TERM("Contract Term: "),
        FIBER_TYPE("Commodity: "),
        SUPPLIER_NAME("Supplier Name: "),
        CONTACT_EMAIL("Contact Email: "),
        DUE_DATE("Due Date: "),


        //Logistic sheet Headers
        MILL("Mill *"),
        SUPPLIER_BID_VOLUME("Supplier Bid Volume *"),
        SUPPLIER_MILL("Supplier Mill  *"),

        ORIGIN_PORT("Origin Port *"),
        ORIGIN_COUNTRY("Origin Country *"),
        ENVIRONMENTAL_CERTIFICATION("Environmental Certification *"),

        BALE_PACKAGING("Bale Packaging *"),
        BALE_TYPE("Bale Type *"),
        US_PORT_OF_ENTRY("US Port(s) of Entry *"),

        INCOTERMS("Incoterms *"),

        INCOTERMS_1("Incoterms 1*"),

        INCOTERMS_2("Incoterms 2 "),
        STEVEDORING_COST("Stevedoring Cost ($/ADMT)"),
        HANDLING_COST("Handling Cost ($/ADMT)"),

        WHARFAGE_COST("Wharfage Cost ($/ADMT)"),
        SECURITY_COST("Security Cost ($/ADMT)"),
        WAREHOUSING_FEE_MONTHLY("Warehousing Fee Monthly ($/ADMT)"),

        IMPORT_CUSTOMS_TARIFF_FEE("Import Customs & Tariff Fee ($/ADMT)"),
        PORT_TIME_IN_DAYS("Port Free Time in Days *"),
        TRANSIT_LEAD_TIME_ORIGIN_PORT_TO_US_PORT("Transit Lead Time in Days (Origin Port to US Port of Entry) *"),
        STEAMSHIP_LINE("Steamship Line *"),
        OCEAN_FREIGHT("Ocean Freight ($/ADMT)"),
        NOMINATED_SAFETY_STOCK("Nominated Safety Stock (Days) *"),
        SAFETY_STOCK_LOCATION_TYPE("Safety Stock Location Type *"),
        SAFETY_STOCK_LOCATION_NAME("Safety Stock Location Name *"),
        ADDRESS("Address"),
        TRANSIT_COST_FROM_US_PORT_TO_SAFETY_STOCK_LOC("Transit Cost ($/ADMT) from US Port to Safety Stock Location "),
        INLAND_TRANSIT_ORIGIN_TYPE("Inland Transit Origin Type *"),
        INLAND_TRANSIT_ORIGIN_NAME("Inland Transit Origin Name *"),
        INLAND_TRANSPORTATION_ROUTE("Inland Transportation Route *"),
        INLAND_TRANSIT_DESTINATION_TYPE("Inland Transit Destination Type *"),
        INLAND_TRANSIT_DESTINATION_NAME("Inland Transit Destination Name  *"),

        //destination address
        DESTINATION_ADDRESS("Address"),
        TRANSIT_MODE("Transit Mode  *"),
        TRANSIT_COST("Transit Cost ($/ADMT)"),
        TRANSIT_LEAD_TIME_IN_DAYS_US_PORT_TO_GP_MILL("Transit Lead Time in Days (US Port to GP Mill)  *"),
        SUPPLIER_MILL_START_COL("Supplier Mill Start col num"),


        //Commercial Pricing Sheet Headers
        PAYMENT_TERMS("Payment Terms *"),
        PRICE_FLOOR("Price Floor ($/ADMT)"),
        PRICE_CEILING("Price Ceiling ($/ADMT)"),
        PRICE_CEILING_EFFECTIVE_PERIOD("Price Ceiling/Floor Effective Period"),
        MECHANISM_BASIS("Mechanism Basis *"),
        INITIAL_PRICE("Initial price"),
        ADDITIONAL_DISCOUNT("Additional Discount ($/ADMT)"),
        TIME_WINDOW("Time Window *"),
        TIME_WINDOW_PERIOD("Period *"),
        VOLUME_BASED_PERIOD(""),
        PERIOD_PRICE_UOM(""),
        IS_MOVEMENT("Index vs Movement *"),
        INDEX_NAME("Index Name *"),
        INDEX_READ_TYPE("Index Read Type "),
        WEEK_DAY_OF_MONTH("Weekday of month"),
        GIVEN_DATE("Given Date"),
        INDEX_PERCENTAGE_DISCOUNT_APPLIED("Percentage Discount Applied *"),
        ADDITIONAL_ADJUSTMENT("Additional Adjustment ($/ADMT)"),
        INDEX_WEIGHTAGE("Weightage% *"),
        FIXED_PRICE("Fixed Price *"),
        FIXED_PRICE_WEIGHTAGE("Weightage% *"),
        PRICING_ALTERNATE_MECHANISM("Pricing Alternate Mechanism"),
        IS_TIER_BASED_PRICING("Tier Based Pricing discount(Y/N) "),
        PRICE_TIER_CATEGORY("Price Tier Category *"),
        PRICE_TIER_MIN_AND_MAX("Price Tier ($/ADMT) *"),
        TIER_ADDITIONAL_DISCOUNT("Additional Discount *"),
        IS_VOLUME_BASED_PRICING("Volume Based Pricing discount(Y/N) *"),
        VOLUME_BASED_PRICING_PERIOD("Volume Based Pricing Period *"),
        VOLUME_TIER_CATEGORY("Volume Tier Category *"),
        VOLUME_TIER_MIN_AND_MAX("Volume Tier (ADMT) *"),
        VOLUME_TIER_ADDITIONAL_DISCOUNT("Additional Discount *"),
        VOLUME_DISCOUNT_APPLIED_TO_ORDERS_FROM_NEXT("Volume Discount Applied to orders from next  *"),
        PORT_OF_ENTRY("Port(s) of Entry"),
        PORT_REBATE("Port Rebate"),
        INLAND_TRANSPORTATION_ALLOWANCES("Inland Transportation Allowances ($/ADMT)"),
        GOODWILL_DISCOUNT("Goodwill Discount ($/ADMT)"),
        ALTERNATE_REBATE_CRITERIA("Alternate Rebate Criteria"),
        COMMENTS("Comments"),


        //commercialSheet Headers according to New Sheet
        PAYMENT_TERM("Payment Terms"),
        MONTHLY_NEGOTIATION("Monthly Negotiation"),
        PRICING_MECHANISM_INDEX_LESS_DISCOUNT("Pricing Mechanism (Index less discount)"),
        PRICING_MECHANISM_STARTING_PRICE_WITH_MOVEMENT("Pricing Mechanism (Starting Price with Movement)"),
        PRICING_MECHANISM_HYBRID_MODEL("Pricing Mechanism (Hybrid Model)"),
        OTHER("Other (Please Explain)"),
        COLLARS("Collars"),
        TIER_BASED_PRICING_DISCOUNT(" Tier Based Pricing Discount"),
        VOLUME_BASED_REBATE("Volume Based Rebate"),
        PORT_REBATES("Port Rebate");


        public final String value;

        ResponseRfpExcelHeaders(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }


    public static final Map<String, Integer> BID_DETAILS_MAP = new HashMap<>();

    static {
        //BidQty
        BID_DETAILS_MAP.put(ResponseRfpExcelHeaders.TOTAL_BID_VOLUME_COL_NUM.getValue(), 0);
        BID_DETAILS_MAP.put(ResponseRfpExcelHeaders.GP_MILL_COL_NUMBER.getValue(), 0);
        BID_DETAILS_MAP.put(ResponseRfpExcelHeaders.GP_MILL_STATE_COL.getValue(), 1);
        BID_DETAILS_MAP.put(ResponseRfpExcelHeaders.GP_MILL_EXPECTED_ANNUAL_VOLUME_COL_NUM.getValue(), 2);
        BID_DETAILS_MAP.put(ResponseRfpExcelHeaders.SUPPLIER_MILL_START_COL.getValue(), 3);
        BID_DETAILS_MAP.put(ResponseRfpExcelHeaders.TOTAL_BID_VOLUME_VALUE_COL.getValue(), 3);
        BID_DETAILS_MAP.put(ResponseRfpExcelHeaders.SUPPLIER_MILL_A.getValue(), 3);
        BID_DETAILS_MAP.put(ResponseRfpExcelHeaders.SUPPLIER_MILL_A_VOLUME.getValue(), 4);
        BID_DETAILS_MAP.put(ResponseRfpExcelHeaders.SUPPLIER_MILL_B.getValue(), 5);
        BID_DETAILS_MAP.put(ResponseRfpExcelHeaders.SUPPLIER_MILL_B_VOLUME.getValue(), 6);
        BID_DETAILS_MAP.put(ResponseRfpExcelHeaders.SUPPLIER_MILL_LAST_COL_NUM.getValue(), 34);
        BID_DETAILS_MAP.put(ResponseRfpExcelHeaders.NAME_OF_SUPPLIER_MILL_NAME.getValue(), 35);
        BID_DETAILS_MAP.put(ResponseRfpExcelHeaders.GP_MILL_START_ROW.getValue(), 37);
        BID_DETAILS_MAP.put(ResponseRfpExcelHeaders.GP_MILL_EXAMPLE_ROW.getValue(), 36);


    }

    public static final Map<String, Integer> LOGISTIC_PRICING_MAP = new HashMap<>();

    static {
        LOGISTIC_PRICING_MAP.put(ResponseRfpExcelHeaders.MILL.getValue(), 0);
        LOGISTIC_PRICING_MAP.put(ResponseRfpExcelHeaders.SUPPLIER_BID_VOLUME.getValue(), 1);
        LOGISTIC_PRICING_MAP.put(ResponseRfpExcelHeaders.SUPPLIER_MILL.getValue(), 2);
        LOGISTIC_PRICING_MAP.put(ResponseRfpExcelHeaders.ORIGIN_PORT.getValue(), 3);
        LOGISTIC_PRICING_MAP.put(ResponseRfpExcelHeaders.ORIGIN_COUNTRY.getValue(), 4);

        LOGISTIC_PRICING_MAP.put(ResponseRfpExcelHeaders.ENVIRONMENTAL_CERTIFICATION.getValue(), 5);
        LOGISTIC_PRICING_MAP.put(ResponseRfpExcelHeaders.BALE_PACKAGING.getValue(), 6);
        LOGISTIC_PRICING_MAP.put(ResponseRfpExcelHeaders.BALE_TYPE.getValue(), 7);
        LOGISTIC_PRICING_MAP.put(ResponseRfpExcelHeaders.US_PORT_OF_ENTRY.getValue(), 8);
        LOGISTIC_PRICING_MAP.put(ResponseRfpExcelHeaders.INCOTERMS.getValue(), 9);

        LOGISTIC_PRICING_MAP.put(ResponseRfpExcelHeaders.STEVEDORING_COST.getValue(), 10);
        LOGISTIC_PRICING_MAP.put(ResponseRfpExcelHeaders.HANDLING_COST.getValue(), 11);
        LOGISTIC_PRICING_MAP.put(ResponseRfpExcelHeaders.WHARFAGE_COST.getValue(), 12);
        LOGISTIC_PRICING_MAP.put(ResponseRfpExcelHeaders.SECURITY_COST.getValue(), 13);
        LOGISTIC_PRICING_MAP.put(ResponseRfpExcelHeaders.WAREHOUSING_FEE_MONTHLY.getValue(), 14);

        LOGISTIC_PRICING_MAP.put(ResponseRfpExcelHeaders.IMPORT_CUSTOMS_TARIFF_FEE.getValue(), 15);
        LOGISTIC_PRICING_MAP.put(ResponseRfpExcelHeaders.PORT_TIME_IN_DAYS.getValue(), 16);
        LOGISTIC_PRICING_MAP.put(ResponseRfpExcelHeaders.TRANSIT_LEAD_TIME_ORIGIN_PORT_TO_US_PORT.getValue(), 17);
        LOGISTIC_PRICING_MAP.put(ResponseRfpExcelHeaders.STEAMSHIP_LINE.getValue(), 18);
        LOGISTIC_PRICING_MAP.put(ResponseRfpExcelHeaders.OCEAN_FREIGHT.getValue(), 19);

        LOGISTIC_PRICING_MAP.put(ResponseRfpExcelHeaders.NOMINATED_SAFETY_STOCK.getValue(), 20);
        LOGISTIC_PRICING_MAP.put(ResponseRfpExcelHeaders.SAFETY_STOCK_LOCATION_TYPE.getValue(), 21);
        LOGISTIC_PRICING_MAP.put(ResponseRfpExcelHeaders.SAFETY_STOCK_LOCATION_NAME.getValue(), 22);
        LOGISTIC_PRICING_MAP.put(ResponseRfpExcelHeaders.ADDRESS.getValue(), 23);
        LOGISTIC_PRICING_MAP.put(ResponseRfpExcelHeaders.TRANSIT_COST_FROM_US_PORT_TO_SAFETY_STOCK_LOC.getValue(), 24);

        LOGISTIC_PRICING_MAP.put(ResponseRfpExcelHeaders.INLAND_TRANSIT_ORIGIN_TYPE.getValue(), 25);
        LOGISTIC_PRICING_MAP.put(ResponseRfpExcelHeaders.INLAND_TRANSIT_ORIGIN_NAME.getValue(), 26);
        LOGISTIC_PRICING_MAP.put(ResponseRfpExcelHeaders.INLAND_TRANSPORTATION_ROUTE.getValue(), 27);
        LOGISTIC_PRICING_MAP.put(ResponseRfpExcelHeaders.INLAND_TRANSIT_DESTINATION_TYPE.getValue(), 28);
        LOGISTIC_PRICING_MAP.put(ResponseRfpExcelHeaders.INLAND_TRANSIT_DESTINATION_NAME.getValue(), 29);

        LOGISTIC_PRICING_MAP.put(ResponseRfpExcelHeaders.TRANSIT_MODE.getValue(), 31);
        LOGISTIC_PRICING_MAP.put(ResponseRfpExcelHeaders.TRANSIT_COST.getValue(), 32);
        LOGISTIC_PRICING_MAP.put(ResponseRfpExcelHeaders.TRANSIT_LEAD_TIME_IN_DAYS_US_PORT_TO_GP_MILL.getValue(), 33);
    }

    public static final Map<String, Integer> LOGISTIC_PRICING_MISMATCH_MAP = new HashMap<>();

    static {
        LOGISTIC_PRICING_MISMATCH_MAP.put(ResponseRfpExcelHeaders.DESTINATION_ADDRESS.getValue(), 30);
    }


    public static final Map<String, String> COLUMN_NAME_TO_CELL_REFERENCE = new HashMap<>();

    static {
        COLUMN_NAME_TO_CELL_REFERENCE.put(ResponseRfpExcelHeaders.SUPPLIER_NAME.getValue(), "A6");
        COLUMN_NAME_TO_CELL_REFERENCE.put(ResponseRfpExcelHeaders.CONTACT_EMAIL.getValue(), "A7");
        COLUMN_NAME_TO_CELL_REFERENCE.put(ResponseRfpExcelHeaders.RFP_NUMBER.getValue(), "A8");
        COLUMN_NAME_TO_CELL_REFERENCE.put(ResponseRfpExcelHeaders.DUE_DATE.getValue(), "A9");
        COLUMN_NAME_TO_CELL_REFERENCE.put(ResponseRfpExcelHeaders.FIBER_TYPE.getValue(), "A10");
        COLUMN_NAME_TO_CELL_REFERENCE.put(ResponseRfpExcelHeaders.CONTRACT_TERM.getValue(), "A11");

        COLUMN_NAME_TO_CELL_REFERENCE.put(ResponseRfpExcelHeaders.BID_TYPE.getValue(), "B27");
        COLUMN_NAME_TO_CELL_REFERENCE.put(ResponseRfpExcelHeaders.VOLUME_BID_FOR_LUMP_SUM.getValue(), "B28");
//        COLUMN_NAME_TO_CELL_REFERENCE.put(ResponseRfpExcelHeaders.SHIP_FROM_MILL_LOCATION_FOR_LUMP_SUM.getValue(), "B29");
        COLUMN_NAME_TO_CELL_REFERENCE.put(ResponseRfpExcelHeaders.BEGINNING_SUPPLIER_PERIOD.getValue(), "B29");
        COLUMN_NAME_TO_CELL_REFERENCE.put(ResponseRfpExcelHeaders.ENDING_SUPPLIER_PERIOD.getValue(), "B30");
        COLUMN_NAME_TO_CELL_REFERENCE.put(ResponseRfpExcelHeaders.BID_VOLUME_VARIANCE.getValue(), "B31");
        //TODO :: recheck
        COLUMN_NAME_TO_CELL_REFERENCE.put(ResponseRfpExcelHeaders.LUMP_SUM_SUPPLIER_NAME.getValue(), "D27");


    }

    // Field Name for BidQtyDetails
    public static final Map<String, String> BID_QTY_DETAILS_COLUMN_HEADER_TO_COLUMN_NUMBER = new HashMap<>();

    static {

        BID_QTY_DETAILS_COLUMN_HEADER_TO_COLUMN_NUMBER.put(ResponseRfpExcelHeaders.FIBER_TYPE.getValue(), "A10");
        BID_QTY_DETAILS_COLUMN_HEADER_TO_COLUMN_NUMBER.put(ResponseRfpExcelHeaders.RFP_NUMBER.getValue(), "A8");
        BID_QTY_DETAILS_COLUMN_HEADER_TO_COLUMN_NUMBER.put(ResponseRfpExcelHeaders.DUE_DATE.getValue(), "A9");

        BID_QTY_DETAILS_COLUMN_HEADER_TO_COLUMN_NUMBER.put(ResponseRfpExcelHeaders.SUPPLIER_NAME.getValue(), "A6");
        BID_QTY_DETAILS_COLUMN_HEADER_TO_COLUMN_NUMBER.put(ResponseRfpExcelHeaders.CONTACT_EMAIL.getValue(), "A7");
        BID_QTY_DETAILS_COLUMN_HEADER_TO_COLUMN_NUMBER.put(ResponseRfpExcelHeaders.CONTRACT_TERM.getValue(), "A11");


        BID_QTY_DETAILS_COLUMN_HEADER_TO_COLUMN_NUMBER.put(ResponseRfpExcelHeaders.BID_TYPE.getValue(), "A27");
        BID_QTY_DETAILS_COLUMN_HEADER_TO_COLUMN_NUMBER.put(ResponseRfpExcelHeaders.VOLUME_BID_FOR_LUMP_SUM.getValue(), "A28");
//        BID_QTY_DETAILS_COLUMN_HEADER_TO_COLUMN_NUMBER.put(ResponseRfpExcelHeaders.SHIP_FROM_MILL_LOCATION_FOR_LUMP_SUM.getValue(), "A29");
        BID_QTY_DETAILS_COLUMN_HEADER_TO_COLUMN_NUMBER.put(ResponseRfpExcelHeaders.BEGINNING_SUPPLIER_PERIOD.getValue(), "A29");
        BID_QTY_DETAILS_COLUMN_HEADER_TO_COLUMN_NUMBER.put(ResponseRfpExcelHeaders.ENDING_SUPPLIER_PERIOD.getValue(), "A30");
        BID_QTY_DETAILS_COLUMN_HEADER_TO_COLUMN_NUMBER.put(ResponseRfpExcelHeaders.BID_VOLUME_VARIANCE.getValue(), "A31");

        BID_QTY_DETAILS_COLUMN_HEADER_TO_COLUMN_NUMBER.put(ResponseRfpExcelHeaders.GP_MILL_CELL_NUM.getValue(), "A35");
        BID_QTY_DETAILS_COLUMN_HEADER_TO_COLUMN_NUMBER.put(ResponseRfpExcelHeaders.GP_MILL_STATE_CELL_NUM.getValue(), "B35");
        BID_QTY_DETAILS_COLUMN_HEADER_TO_COLUMN_NUMBER.put(ResponseRfpExcelHeaders.GP_MILL_EXPECTED_ANNUAL_VOLUME_CELL_NUM.getValue(), "C35");
        BID_QTY_DETAILS_COLUMN_HEADER_TO_COLUMN_NUMBER.put(ResponseRfpExcelHeaders.GP_MILL_SUPPLIER_VOLUME_BID_CELL_NUM.getValue(), "D35");

        BID_QTY_DETAILS_COLUMN_HEADER_TO_COLUMN_NUMBER.put(ResponseRfpExcelHeaders.SUPPLIER_MILL_A.getValue(), "D36");
        BID_QTY_DETAILS_COLUMN_HEADER_TO_COLUMN_NUMBER.put(ResponseRfpExcelHeaders.SUPPLIER_MILL_A_VOLUME.getValue(), "E36");
        BID_QTY_DETAILS_COLUMN_HEADER_TO_COLUMN_NUMBER.put(ResponseRfpExcelHeaders.SUPPLIER_MILL_B.getValue(), "F36");
        BID_QTY_DETAILS_COLUMN_HEADER_TO_COLUMN_NUMBER.put(ResponseRfpExcelHeaders.SUPPLIER_MILL_B_VOLUME.getValue(), "G36");

    }

    public static final String TOTAL = "Total";

    public static final String SUPPLIER_MILL = "SupplierMill";


    public static final int EXCEL_COMMERCIAL_RESPONSE_START_ROW = 6;
    public static final int LOGISTIC_PRICING_SHEET_STARTING_ROW = 5;
    public static final int LOGISTIC_PRICING_SHEET_HEADERS_ROW = 1;


    public static Map<String, Integer> COMMERICIAL_PRICING_MAP = new HashMap<>();

    static {
        COMMERICIAL_PRICING_MAP.put(ResponseRfpExcelHeaders.PAYMENT_TERMS.getValue(), 0);
        COMMERICIAL_PRICING_MAP.put(ResponseRfpExcelHeaders.PRICE_FLOOR.getValue(), 1);
        COMMERICIAL_PRICING_MAP.put(ResponseRfpExcelHeaders.PRICE_CEILING.getValue(), 2);
        COMMERICIAL_PRICING_MAP.put(ResponseRfpExcelHeaders.PRICE_CEILING_EFFECTIVE_PERIOD.getValue(), 3);
        COMMERICIAL_PRICING_MAP.put(ResponseRfpExcelHeaders.MECHANISM_BASIS.getValue(), 4);
        COMMERICIAL_PRICING_MAP.put(ResponseRfpExcelHeaders.IS_MOVEMENT.getValue(), 5);
        COMMERICIAL_PRICING_MAP.put(ResponseRfpExcelHeaders.INITIAL_PRICE.getValue(), 6);
        COMMERICIAL_PRICING_MAP.put(ResponseRfpExcelHeaders.ADDITIONAL_DISCOUNT.getValue(), 7);
        COMMERICIAL_PRICING_MAP.put(ResponseRfpExcelHeaders.TIME_WINDOW.getValue(), 8);
        COMMERICIAL_PRICING_MAP.put(ResponseRfpExcelHeaders.TIME_WINDOW_PERIOD.getValue(), 9);
        COMMERICIAL_PRICING_MAP.put(ResponseRfpExcelHeaders.INDEX_NAME.getValue(), 10);
        COMMERICIAL_PRICING_MAP.put(ResponseRfpExcelHeaders.INDEX_READ_TYPE.getValue(), 11);
        COMMERICIAL_PRICING_MAP.put(ResponseRfpExcelHeaders.WEEK_DAY_OF_MONTH.getValue(), 12);
        COMMERICIAL_PRICING_MAP.put(ResponseRfpExcelHeaders.GIVEN_DATE.getValue(), 13);
        COMMERICIAL_PRICING_MAP.put(ResponseRfpExcelHeaders.INDEX_PERCENTAGE_DISCOUNT_APPLIED.getValue(), 14);
        COMMERICIAL_PRICING_MAP.put(ResponseRfpExcelHeaders.ADDITIONAL_ADJUSTMENT.getValue(), 15);
        COMMERICIAL_PRICING_MAP.put(ResponseRfpExcelHeaders.FIXED_PRICE.getValue(), 17);
        COMMERICIAL_PRICING_MAP.put(ResponseRfpExcelHeaders.FIXED_PRICE_WEIGHTAGE.getValue(), 18);
        COMMERICIAL_PRICING_MAP.put(ResponseRfpExcelHeaders.PRICING_ALTERNATE_MECHANISM.getValue(), 19);
        COMMERICIAL_PRICING_MAP.put(ResponseRfpExcelHeaders.IS_TIER_BASED_PRICING.getValue(), 20);
        COMMERICIAL_PRICING_MAP.put(ResponseRfpExcelHeaders.PRICE_TIER_CATEGORY.getValue(), 21);
        COMMERICIAL_PRICING_MAP.put(ResponseRfpExcelHeaders.PRICE_TIER_MIN_AND_MAX.getValue(), 22);
        COMMERICIAL_PRICING_MAP.put(ResponseRfpExcelHeaders.TIER_ADDITIONAL_DISCOUNT.getValue(), 23);
        COMMERICIAL_PRICING_MAP.put(ResponseRfpExcelHeaders.IS_VOLUME_BASED_PRICING.getValue(), 24);
        COMMERICIAL_PRICING_MAP.put(ResponseRfpExcelHeaders.VOLUME_BASED_PRICING_PERIOD.getValue(), 25);
        COMMERICIAL_PRICING_MAP.put(ResponseRfpExcelHeaders.VOLUME_TIER_CATEGORY.getValue(), 26);
        COMMERICIAL_PRICING_MAP.put(ResponseRfpExcelHeaders.VOLUME_TIER_MIN_AND_MAX.getValue(), 27);
        COMMERICIAL_PRICING_MAP.put(ResponseRfpExcelHeaders.VOLUME_DISCOUNT_APPLIED_TO_ORDERS_FROM_NEXT.getValue(), 29);
        COMMERICIAL_PRICING_MAP.put(ResponseRfpExcelHeaders.PORT_OF_ENTRY.getValue(), 30);
        COMMERICIAL_PRICING_MAP.put(ResponseRfpExcelHeaders.PORT_REBATE.getValue(), 31);
        COMMERICIAL_PRICING_MAP.put(ResponseRfpExcelHeaders.INLAND_TRANSPORTATION_ALLOWANCES.getValue(), 32);
        COMMERICIAL_PRICING_MAP.put(ResponseRfpExcelHeaders.GOODWILL_DISCOUNT.getValue(), 33);
        COMMERICIAL_PRICING_MAP.put(ResponseRfpExcelHeaders.ALTERNATE_REBATE_CRITERIA.getValue(), 34);
        COMMERICIAL_PRICING_MAP.put(ResponseRfpExcelHeaders.COMMENTS.getValue(), 35);
    }

    public static Map<String, int[]> PERIOD_INDEX_MAP = new HashMap<>();

    static {
        for (int i = 1; i <= 12; i++) {
            PERIOD_INDEX_MAP.put("M" + i, new int[]{i - 1});
        }
        for (int i = 1; i <= 4; i++) {
            PERIOD_INDEX_MAP.put("Q" + i, new int[]{(i - 1) * 3, (i - 1) * 3 + 1, (i - 1) * 3 + 2});
        }
        for (int i = 1; i <= 2; i++) {
            PERIOD_INDEX_MAP.put("H" + i, new int[]{(i - 1) * 6, (i - 1) * 6 + 1, (i - 1) * 6 + 2, (i - 1) * 6 + 3, (i - 1) * 6 + 4, (i - 1) * 6 + 5});
        }
        PERIOD_INDEX_MAP.put("Year 1", new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11});
    }


    public static Map<String, Integer> COMMERCIAL_PRICING_MISMATCH_MAP = new HashMap<>();

    static {
        COMMERCIAL_PRICING_MISMATCH_MAP.put(ResponseRfpExcelHeaders.INDEX_WEIGHTAGE.getValue(), 16);
        COMMERCIAL_PRICING_MISMATCH_MAP.put(ResponseRfpExcelHeaders.VOLUME_TIER_ADDITIONAL_DISCOUNT.getValue(), 28);
    }

    public enum USPortOfEntry {
        DOMESTIC("Domestic"),
        PORT_OF_VANCOUVER_CA("Port of Vancouver (CA)"),
        PORT_OF_VANCOUVER("Port of Vancouver"),
        PORT_OF_SEATTLE("Port of Seattle"),
        PORT_OF_TACOMA("Port of Tacoma"),
        PORT_OF_GREEN_BAY("Port of Green Bay"),
        PORT_OF_LAKE_CHARLES("Port of Lake Charles"),
        PORT_OF_HOUSTON("Port of Houston"),
        PORT_OF_BEAUMONT("Port of Beaumont"),
        PORT_OF_PORT_ARTHUR("Port of Arthur"),
        PORT_OF_NEW_ORLEANS("Port of New Orleans"),
        PORT_OF_MOBILE("Port of Mobile"),
        PORT_OF_JACKSONVILLE("Port of Jacksonville"),
        PORT_OF_FERNANDINA("Port of Fernandina"),
        PORT_OF_CANAVERAL("Port of Canaveral"),
        PORT_OF_CHARLESTON("Port of Charleston"),
        PORT_OF_SAVANNAH("Port of Savannah"),
        PORT_OF_BRUNSWICK("Port of Brunswick"),
        PORT_OF_PHILADELPHIA("Port of Philadelphia"),
        PORT_OF_BALTIMORE("Port of Baltimore"),
        PORT_OF_NEWARK("Port of Newark"),
        PORT_OF_ALBANY("Port of Albany"),
        PORT_OF_LONGVIEW("Port of Longview"),
        PORT_OF_MANATEE("Port of Manatee");

        public final String value;

        USPortOfEntry(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public enum AddressFields {
        CITY("City"),
        COUNTRY("Country"),

        STATE("State"),
        PINCODE("Pin Code");

        public final String addressField;

        AddressFields(String addressField) {
            this.addressField = addressField;
        }

        public String getValue() {
            return addressField;
        }
    }


    public static final int COMMERCIAL_TOTAL_COLUMNS_COUNT = 36;
    public static final int COMMERCIAL_HEADERS_ROW = 2;

    public static final String PERCENTAGE_REGEX = "^(?:100(?:\\.0{1,2})?|\\d{1,2}(?:\\.\\d{1,2})?)%$";

    //public static final String DECIMAL_REGEX = "^[-+]?\\$\\d*[.]?\\d+|^[-+]?\\d+[.]?\\d*"
    public static final String DECIMAL_REGEX = "^[+-]?\\$?\\d{1,3}(?:,\\d{3})*(?:\\.\\d{2})?$";
//    public static final String DECIMAL_REGEX =  "^[-+]?\\$\\d+(\\.\\d{1,2})?";

    public static final String INTEGER_REGEX = "^\\d{1,3}$";

    public static final String NUMBER_REGEX = "^[0-9]+$";

    public static final String YEAR_REGEX = "^[0-9]{4}";

    public static final String ALPHA_NUMERIC_REGEX = "^[a-z A-Z0-9]+$";

    public static final String ALPHA_NUMERIC_WITH_HYPHEN = "^[a-z A-Z0-9\\-]+$";

    public static final String THOUSAND_SAPARATOR_REGEX = "#,###0";

    public static final String COMMA_WITH_SPACE = ", ";
    public static final String DOLLER_REGEX = "\\$\\d+(\\.\\d{1,2})?";

    public static final String DOLLER_WITH_NA_REGEX= "(\\\\$[\\\\d]+(\\\\.\\\\d{1,2})?|(?i:N/A))";

    public static final String DOLLER_REGEX_WITH_MINUS_SIGN = "^-?\\$\\d+(\\.\\d{1,2})?$";
    public static final String EFFECTIVE_DATE_REGEX = "\\d{2}/\\d{2}/\\d{4}\\s*-\\s*\\d{2}/\\d{2}/\\d{4}";

    public static final String EFFECTIVE_DATE_WITH_NA_REGEX = "(\\\\d{1,2}/\\\\d{1,2}/\\\\d{2,4}\\\\s*-\\\\s*\\\\d{1,2}/\\\\d{1,2}/\\\\d{2,4}|(?i:N/A))";

    public static final String DUE_DATE_REGEX = "^(0[1-9]|1[0-2])/(0[1-9]|1\\d|2\\d|3[0-1])/\\d{4}$";
    public static final String MIN_MAX_REGEX_DOLLER = "\\$(\\d+)\\s*-\\s*\\$(\\d+)\\d+";

    public static final String MIN_MAX_REGEX_DOLLER_OR_NUMBER_WITH_DOLLER = "\\$(\\d+)\\s*-\\s*\\$(\\d+)|\\$(\\d+)\n";

    public static final String MIN_MAX_REGEX_WITHOUT_DOLLAR = "(\\d+)\\s*-\\s*(\\d+)|(\\d+)";
    public static final String MIN_MAX_REGEX_NUMERIC = "^\\d+\\s*-\\s*\\d+$";
    public static final String PERCENT_OR_DOLLAR_REGEX = "(\\$\\d+(\\.\\d{2})?|\\d+(\\.\\d{1,2})?%)";

    public static final String ALPHABETS_REGEX = "^[a-z A-Z]+$";
    public static final String ALPHABETS_WITH_NA_REGEX = "^(?:[a-zA-Z ]+|N/A)$";
    public static final String ALPHABETS_OR_NA_REGEX = "^(?:[a-zA-Z ]+|N/A|n/a|N/a|n/A)$";

    public static final String ALPHABETS_OR_ZERO_REGEX = "^[A-Z a-z0/]+$";

    public static final String ALPHABETS = "Alphabets";

    public static final String NUMBER = "Number";

    public static final String DOLLER = "Dollar";

    public static final String PERCENTAGE = "Percentage";

    public static final String DECIMAL = "Decimal Number";

    public static final String MIN_MAX = "Min - Max";

    public static final String MIN_MAX_EXAMPLE = "EXAMPLE = $100 - $200 OR $100 ";

    public static final String MIN_MAX_EXAMPLE_WITHOUT_DOLLER = "EXAMPLE = 10,000 - 20,000 OR 1,000 ";

    public static final String PERCENT_OR_DOLLAR = "10% 0r 10$";

    public static final String ALPHA_NUMERIC = "Alpha Numeric";

    public static final String EFFECTIVE_DATE = "mm/dd/yyyy - mm/dd/yyyy";

    public static final String WEEK_CRITERIA_AND_DAY_FORMAT = "First/Second/Third/Fourth/Last Tuesday of the Month";

    public static final String VOLUME_TIER_RANGE_FORMAT = "10,000-20,000";

    public static final int ADDRESS_STRING_REQUIRED_FIELDS_COUNT = 6;

    public static final String ADDRESS_FORMAT = "address Line1 , address Line2, city, state, country, zip code";

    public static final String MOVEMENT = "Movement";

    public static final String NUMERIC_CHARACTERS_REGEX = "^[1-9]\\d*$";

    public static final String NUMERIC_CHARACTERS_INCLUDE_ZERO_REGEX = "^[0-9]\\d*$";
    public static final String ZERO_OR_NULL_REGEX = "^$|0";
    public static final Map<Character, String> PERIOD_MAP_COMMERCIAL_PRICING = new HashMap<>();

    static {
        PERIOD_MAP_COMMERCIAL_PRICING.put('M', Constants.PERIOD_MONTH);
        PERIOD_MAP_COMMERCIAL_PRICING.put('Q', Constants.PERIOD_QUARTER_YEAR);
        PERIOD_MAP_COMMERCIAL_PRICING.put('H', Constants.PERIOD_HALF_YEAR);
        PERIOD_MAP_COMMERCIAL_PRICING.put('Y', Constants.PERIOD_FULL_YEAR);
    }

    public static final Map<String, List<String>> PERIOD_MAP_FOR_COMMERCIAL_PRICING = new HashMap<>();

    static {
        PERIOD_MAP_FOR_COMMERCIAL_PRICING.put("quarterly", new ArrayList<>(Arrays.asList("Q1", "Q2", "Q3", "Q4")));
        PERIOD_MAP_FOR_COMMERCIAL_PRICING.put("monthly", new ArrayList<>(Arrays.asList("M1", "M2", "M3", "M4", "M5", "M6", "M7", "M8", "M9", "M10", "M11", "M12")));
        PERIOD_MAP_FOR_COMMERCIAL_PRICING.put("halfYearly", new ArrayList<>(Arrays.asList("H1", "H2")));

    }

    public enum ExpectedValuesList {
        MANDATORY("Mandatory"),
        PORT_OF_ENTRY("Valid Port of Entry");

        public final String value;

        ExpectedValuesList(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public enum OriginPort {
        DOMESTIC("Domestic");

        public final String value;

        OriginPort(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public enum OriginCountry {
        USA("USA");

        public final String value;

        OriginCountry(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public static final String EXCEL = "xlsx";
    public static final String EXCEL_CONTENT_TYPE = "vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    public static final String COLON = ":";
    public static final String RFP_NUMBER_FORMAT = "RFP Number: 1234";
    public static final String DUE_DATE_FORMAT = "Due Date: MM/DD/YYYY";
    public static final String FIBER_TYPE_FORMAT = "Commodity: Fiber Name";
    public static final String STRING = "String";

    // EXCEL FONTS
    public static final String FONT_ARIAL = "Arial";

    public static final String HANDLED_BY_SUPPLIER = "supplier";

    public static final String NOT_APPLICABLE = "NA";

    public static final String ZERO = "0";

    // ENUM FOR FILED OF COMMERCIAL SHEET
    public enum CommercialSheetFieldNames {
        //payment terms
        PAYMENT_TERM("Payment Terms *"),

        // monthly negotiation
        MONTHLY_NEGOTIATION("Monthly Negotiation (Y/N)"),

        //Pricing Mechanism (Index less discount)
        DISCOUNT("Discount %"),
        INDEX_NAME("Index Name *"),
        INDEX_PUBLISHED_DATE("Index Published Date"),
        ADDITIONAL_ADJUSTMENT_ADMT("Additional Adjustment ($/ADMT)"),
        COMMENTS("Comments"),

        //Pricing Mechanism (Starting Price with Movement)
        STARTING_PRICE_POINT("Starting Price Point"),
        INDEX_NAME_WITH_MOVEMENT("Index Name *"),
        MONTH_OVER_MONTH_CHANGE("Month Over Month Change"),
        INDEX_PUBLISHED_DATE_2("Index Published Date"),
        ADDITIONAL_ADJUSTMENT_ADMT_WITH_MOVEMENT("Additional Adjustment ($/ADMT)"),
        COMMENTS_WITH_MOVEMENT("Comments"),

        //Pricing Mechanism (Hybrid Model)
        PART_1("Part 1"),
        PART_2("Part 2"),
        PART_3("Part 3 (If necessary)"),
        COMMENTS_FOR_HYBRID("Comments"),

        //Collars
        PRICE_FLOOR_ADMT("Price Floor ($/ADMT)"),
        PRICE_CEILING_ADMT("Price Ceiling ($/ADMT)"),
        PRICE_CEILING_FLOOR_PERIOD("Price Ceiling/Floor Effective Period"),

        // Tier Based Pricing Discount
        TIER_BASED_PRICING_DISCOUNT("Tier Based Pricing discount(Y/N) "),
        PRICE_TIER_CATEGORY("Price Tier Category *"),
        PRICE_RANGE_ADMT("Price Range ($/ADMT)"),
        DISCOUNT_ADMT("Discount ($/ADMT)"),
        PRICE_TIER_CATEGORY_2("Price Tier Category *"),
        PRICE_TIER_ADMT("Price Tier ($/ADMT) *"),
        DISCOUNT_ADMT_2("Discount ($/ADMT)"),
        PRICE_TIER_CATEGORY_3("Price Tier Category *"),
        PRICE_TIER_ADMT_2("Price Tier ($/ADMT) *"),
        DISCOUNT_ADMT_3("Discount ($/ADMT)"),
        TIER_BASED_PRICING_DISCOUNT_COMMENTS("Comments"),

        //Volume Based Rebate
        VOLUME_BASED_PRICING_DISCOUNT("Volume Based Pricing discount(Y/N) *"),
        VOLUME_BASED_PRICING_PERIOD("Volume Based Pricing Period *"),
        VOLUME_TIER_CATEGORY("Volume Tier Category *"),
        VOLUME_TIER_ADMT("Volume Tier (ADMT) *"),
        VOLUME_DISCOUNT_REBATE("Volume Discount Rebate (% or $)"),
        VOLUME_BASED_REBATE_COMMENTS("Comments"),

        //Port Rebate
        PORT_REBATE("Port Rebate"),
        PORT_REBATE_COMMENTS("Comments");

        private final String fieldName;

        CommercialSheetFieldNames(String fieldName) {
            this.fieldName = fieldName;
        }

        public String getFieldName() {
            return fieldName;
        }
    }


    // commercial Pricing sheets Headers to cellReference Map
    public static final Map<String, String> COMMERCIAL_PRICING_HEADER_TO_CELLREFERENCE_MAP = new HashMap<>();

    static {
        COMMERCIAL_PRICING_HEADER_TO_CELLREFERENCE_MAP.put(ResponseRfpExcelHeaders.PAYMENT_TERM.getValue(), "A2");
        COMMERCIAL_PRICING_HEADER_TO_CELLREFERENCE_MAP.put(ResponseRfpExcelHeaders.MONTHLY_NEGOTIATION.getValue(), "A5");
        COMMERCIAL_PRICING_HEADER_TO_CELLREFERENCE_MAP.put(ResponseRfpExcelHeaders.PRICING_MECHANISM_INDEX_LESS_DISCOUNT.getValue(), "A9");
        COMMERCIAL_PRICING_HEADER_TO_CELLREFERENCE_MAP.put(ResponseRfpExcelHeaders.PRICING_MECHANISM_STARTING_PRICE_WITH_MOVEMENT.getValue(), "A16");
        COMMERCIAL_PRICING_HEADER_TO_CELLREFERENCE_MAP.put(ResponseRfpExcelHeaders.PRICING_MECHANISM_HYBRID_MODEL.getValue(), "A24");
        COMMERCIAL_PRICING_HEADER_TO_CELLREFERENCE_MAP.put(ResponseRfpExcelHeaders.OTHER.getValue(), "A42");
        COMMERCIAL_PRICING_HEADER_TO_CELLREFERENCE_MAP.put(ResponseRfpExcelHeaders.COLLARS.getValue(), "A55");
        COMMERCIAL_PRICING_HEADER_TO_CELLREFERENCE_MAP.put(ResponseRfpExcelHeaders.TIER_BASED_PRICING_DISCOUNT.getValue(), "A59");
        COMMERCIAL_PRICING_HEADER_TO_CELLREFERENCE_MAP.put(ResponseRfpExcelHeaders.VOLUME_BASED_REBATE.getValue(), "A71");
        COMMERCIAL_PRICING_HEADER_TO_CELLREFERENCE_MAP.put(ResponseRfpExcelHeaders.PORT_REBATES.getValue(), "A80");
    }


    // commercial Pricing sheets filedNames to cellReference Map
    public static final Map<String, String> COMMERCIAL_PRICING_FIELDS_NAME_TO_CELLREFERENCE_MAP = new HashMap<>();

    static {
        COMMERCIAL_PRICING_FIELDS_NAME_TO_CELLREFERENCE_MAP.put(CommercialSheetFieldNames.PAYMENT_TERM.getFieldName(), "B2");
        COMMERCIAL_PRICING_FIELDS_NAME_TO_CELLREFERENCE_MAP.put(CommercialSheetFieldNames.MONTHLY_NEGOTIATION.getFieldName(), "B5");
        COMMERCIAL_PRICING_FIELDS_NAME_TO_CELLREFERENCE_MAP.put(CommercialSheetFieldNames.PRICE_FLOOR_ADMT.getFieldName(), "B55");
        COMMERCIAL_PRICING_FIELDS_NAME_TO_CELLREFERENCE_MAP.put(CommercialSheetFieldNames.PRICE_CEILING_ADMT.getFieldName(), "B56");
        COMMERCIAL_PRICING_FIELDS_NAME_TO_CELLREFERENCE_MAP.put(CommercialSheetFieldNames.PRICE_CEILING_FLOOR_PERIOD.getFieldName(), "B57");
        COMMERCIAL_PRICING_FIELDS_NAME_TO_CELLREFERENCE_MAP.put(CommercialSheetFieldNames.TIER_BASED_PRICING_DISCOUNT.getFieldName(), "B59");
        COMMERCIAL_PRICING_FIELDS_NAME_TO_CELLREFERENCE_MAP.put(CommercialSheetFieldNames.PORT_REBATE.getFieldName(), "B80");
        COMMERCIAL_PRICING_FIELDS_NAME_TO_CELLREFERENCE_MAP.put(CommercialSheetFieldNames.PORT_REBATE_COMMENTS.getFieldName(), "B81");
    }

    public static String CELL_VALUE_COLOUR_CODE = "ffffcc";

    public static String CELL_HEADER_COLOUR_CODE = "e7e6e6";

    public static Integer commercialSheetValueColumnNumber = 4;

//    public static final  Map<String, String> COMMERCIAL_PRICING_FIELDS_VALUE_TO_CELL_REFERENCE_MAP = new HashMap<>();
//
//    static {
//        COMMERCIAL_PRICING_FIELDS_VALUE_TO_CELL_REFERENCE_MAP.put(CommercialSheetFieldNames.DISCOUNT.getFieldName(), "E10");
//        COMMERCIAL_PRICING_FIELDS_VALUE_TO_CELL_REFERENCE_MAP.put(CommercialSheetFieldNames.INDEX_NAME.getFieldName(), "E11");
//        COMMERCIAL_PRICING_FIELDS_VALUE_TO_CELL_REFERENCE_MAP.put(CommercialSheetFieldNames.INDEX_PUBLISHED_DATE.getFieldName(), "E12");
//        COMMERCIAL_PRICING_FIELDS_VALUE_TO_CELL_REFERENCE_MAP.put(CommercialSheetFieldNames.ADDITIONAL_ADJUSTMENT_ADMT.getFieldName(), "E13");
//        COMMERCIAL_PRICING_FIELDS_VALUE_TO_CELL_REFERENCE_MAP.put(CommercialSheetFieldNames.COMMENTS.getFieldName(), "E14");
//
//    }

    // Constant for getting Mechanism basis
    public static String INDEX_CELL_VALUE_FOR_INDEX_BASIS = "F10";
    public static String INDEX_CELL_VALUE_FOR_MOVEMENT_BASIS = "F17";
    public static String INDEX_CELL_VALUE_FOR_HYBRID_BASIS = "F24";
    public static String INDEX_CELL_VALUE_FOR_OTHER_BASIS = "B42";


    public static final Map<String, String> INDEX_BASED_CELL_REFERENCE_FOR_COMMERCIAL_PRICING = new HashMap<>();

    static {
        INDEX_BASED_CELL_REFERENCE_FOR_COMMERCIAL_PRICING.put(CommercialSheetFieldNames.DISCOUNT.getFieldName(), "F9");
        INDEX_BASED_CELL_REFERENCE_FOR_COMMERCIAL_PRICING.put(CommercialSheetFieldNames.INDEX_NAME.getFieldName(), "F10");
        INDEX_BASED_CELL_REFERENCE_FOR_COMMERCIAL_PRICING.put(CommercialSheetFieldNames.INDEX_PUBLISHED_DATE.getFieldName(), "F11");
        INDEX_BASED_CELL_REFERENCE_FOR_COMMERCIAL_PRICING.put(CommercialSheetFieldNames.ADDITIONAL_ADJUSTMENT_ADMT.getFieldName(), "F12");
        INDEX_BASED_CELL_REFERENCE_FOR_COMMERCIAL_PRICING.put(CommercialSheetFieldNames.COMMENTS.getFieldName(), "C13");
    }


    public static final Map<String, String> MOVEMENT_BASED_CELL_REFERENCE_FOR_COMMERCIAL_PRICING = new HashMap<>();

    static {
        MOVEMENT_BASED_CELL_REFERENCE_FOR_COMMERCIAL_PRICING.put(CommercialSheetFieldNames.STARTING_PRICE_POINT.getFieldName(), "F16");
        MOVEMENT_BASED_CELL_REFERENCE_FOR_COMMERCIAL_PRICING.put(CommercialSheetFieldNames.INDEX_NAME.getFieldName(), "F17");
        MOVEMENT_BASED_CELL_REFERENCE_FOR_COMMERCIAL_PRICING.put(CommercialSheetFieldNames.MONTH_OVER_MONTH_CHANGE.getFieldName(), "F18");
        MOVEMENT_BASED_CELL_REFERENCE_FOR_COMMERCIAL_PRICING.put(CommercialSheetFieldNames.INDEX_PUBLISHED_DATE.getFieldName(), "F19");
        MOVEMENT_BASED_CELL_REFERENCE_FOR_COMMERCIAL_PRICING.put(CommercialSheetFieldNames.ADDITIONAL_ADJUSTMENT_ADMT.getFieldName(), "F20");
        MOVEMENT_BASED_CELL_REFERENCE_FOR_COMMERCIAL_PRICING.put(CommercialSheetFieldNames.COMMENTS.getFieldName(), "C21");

    }

    public static final Map<String, String> HYBRID_BASED_CELL_REFERENCE_FOR_COMMERCIAL_PRICING = new HashMap<>();

    static {
        HYBRID_BASED_CELL_REFERENCE_FOR_COMMERCIAL_PRICING.put(CommercialSheetFieldNames.PART_1.getFieldName(), "F24");
        HYBRID_BASED_CELL_REFERENCE_FOR_COMMERCIAL_PRICING.put(CommercialSheetFieldNames.PART_2.getFieldName(), "F29");
        HYBRID_BASED_CELL_REFERENCE_FOR_COMMERCIAL_PRICING.put(CommercialSheetFieldNames.PART_3.getFieldName(), "F34");
        HYBRID_BASED_CELL_REFERENCE_FOR_COMMERCIAL_PRICING.put(CommercialSheetFieldNames.COMMENTS.getFieldName(), "C39");

    }

    public static final Map<String, String> OTHER_BASED_CELL_REFERENCE_FOR_COMMERCIAL_PRICING = new HashMap<>();

    static {
        OTHER_BASED_CELL_REFERENCE_FOR_COMMERCIAL_PRICING.put(ResponseRfpExcelHeaders.OTHER.getValue(), "B42");
    }


    public static final Map<String, String> PRICING_DETAILS_CELL_REFERENCE_FOR_COMMERCIAL_PRICING = new HashMap<>();

    static {
        PRICING_DETAILS_CELL_REFERENCE_FOR_COMMERCIAL_PRICING.put(CommercialSheetFieldNames.PAYMENT_TERM.getFieldName(), "F2");

        PRICING_DETAILS_CELL_REFERENCE_FOR_COMMERCIAL_PRICING.put(CommercialSheetFieldNames.MONTHLY_NEGOTIATION.getFieldName(), "F5");
        PRICING_DETAILS_CELL_REFERENCE_FOR_COMMERCIAL_PRICING.put(CommercialSheetFieldNames.PRICE_FLOOR_ADMT.getFieldName(), "F55");
        PRICING_DETAILS_CELL_REFERENCE_FOR_COMMERCIAL_PRICING.put(CommercialSheetFieldNames.PRICE_CEILING_ADMT.getFieldName(), "F56");
        PRICING_DETAILS_CELL_REFERENCE_FOR_COMMERCIAL_PRICING.put(CommercialSheetFieldNames.PRICE_CEILING_FLOOR_PERIOD.getFieldName(), "F57");

    }

    public static final Map<String, String> PRICE_TIER_1_FIELDS_NAME_TO_CELLREFERENCE_MAP = new HashMap<>();

    static {


        PRICE_TIER_1_FIELDS_NAME_TO_CELLREFERENCE_MAP.put(CommercialSheetFieldNames.PRICE_TIER_CATEGORY.getFieldName(), "B60");
        PRICE_TIER_1_FIELDS_NAME_TO_CELLREFERENCE_MAP.put(CommercialSheetFieldNames.PRICE_RANGE_ADMT.getFieldName(), "B61");
        PRICE_TIER_1_FIELDS_NAME_TO_CELLREFERENCE_MAP.put(CommercialSheetFieldNames.DISCOUNT_ADMT.getFieldName(), "B62");

    }


    public static final Map<String, String> PRICE_TIER_2_FIELDS_NAME_TO_CELLREFERENCE_MAP = new HashMap<>();

    static {

        PRICE_TIER_2_FIELDS_NAME_TO_CELLREFERENCE_MAP.put(CommercialSheetFieldNames.PRICE_TIER_CATEGORY.getFieldName(), "B63");
        PRICE_TIER_2_FIELDS_NAME_TO_CELLREFERENCE_MAP.put(CommercialSheetFieldNames.PRICE_RANGE_ADMT.getFieldName(), "B64");
        PRICE_TIER_2_FIELDS_NAME_TO_CELLREFERENCE_MAP.put(CommercialSheetFieldNames.DISCOUNT_ADMT.getFieldName(), "B65");
    }

    public static final Map<String, String> PRICE_TIER_3_FIELDS_NAME_TO_CELLREFERENCE_MAP = new HashMap<>();

    static {
        PRICE_TIER_3_FIELDS_NAME_TO_CELLREFERENCE_MAP.put(CommercialSheetFieldNames.PRICE_TIER_CATEGORY.getFieldName(), "B66");
        PRICE_TIER_3_FIELDS_NAME_TO_CELLREFERENCE_MAP.put(CommercialSheetFieldNames.PRICE_RANGE_ADMT.getFieldName(), "B67");
        PRICE_TIER_3_FIELDS_NAME_TO_CELLREFERENCE_MAP.put(CommercialSheetFieldNames.DISCOUNT_ADMT.getFieldName(), "B68");
    }


    public static final Map<String, String> PRICE_TIER_1_FIELDS_NAME_VALUES_TO_CELLREFERENCE_MAP = new HashMap<>();

    static {


        PRICE_TIER_1_FIELDS_NAME_VALUES_TO_CELLREFERENCE_MAP.put(CommercialSheetFieldNames.PRICE_TIER_CATEGORY.getFieldName(), "F60");
        PRICE_TIER_1_FIELDS_NAME_VALUES_TO_CELLREFERENCE_MAP.put(CommercialSheetFieldNames.PRICE_RANGE_ADMT.getFieldName(), "F61");
        PRICE_TIER_1_FIELDS_NAME_VALUES_TO_CELLREFERENCE_MAP.put(CommercialSheetFieldNames.DISCOUNT_ADMT.getFieldName(), "F62");

    }


    public static final Map<String, String> PRICE_TIER_2_FIELDS_NAME_VALUES_TO_CELLREFERENCE_MAP = new HashMap<>();

    static {

        PRICE_TIER_2_FIELDS_NAME_VALUES_TO_CELLREFERENCE_MAP.put(CommercialSheetFieldNames.PRICE_TIER_CATEGORY.getFieldName(), "F63");
        PRICE_TIER_2_FIELDS_NAME_VALUES_TO_CELLREFERENCE_MAP.put(CommercialSheetFieldNames.PRICE_TIER_ADMT.getFieldName(), "F64");
        PRICE_TIER_2_FIELDS_NAME_VALUES_TO_CELLREFERENCE_MAP.put(CommercialSheetFieldNames.DISCOUNT_ADMT.getFieldName(), "F65");
    }

    public static final Map<String, String> PRICE_TIER_3_FIELDS_NAME_VALUES_TO_CELLREFERENCE_MAP = new HashMap<>();

    static {
        PRICE_TIER_3_FIELDS_NAME_VALUES_TO_CELLREFERENCE_MAP.put(CommercialSheetFieldNames.PRICE_TIER_CATEGORY.getFieldName(), "F66");
        PRICE_TIER_3_FIELDS_NAME_VALUES_TO_CELLREFERENCE_MAP.put(CommercialSheetFieldNames.PRICE_TIER_ADMT_2.getFieldName(), "F67");
        PRICE_TIER_3_FIELDS_NAME_VALUES_TO_CELLREFERENCE_MAP.put(CommercialSheetFieldNames.DISCOUNT_ADMT.getFieldName(), "F68");
    }


    public static Map<String, String> PRICE_TIER_FIELDS_NAME_VALUES_TO_CELLREFERENCE_MAP = new HashMap<>();

    static {
        PRICE_TIER_FIELDS_NAME_VALUES_TO_CELLREFERENCE_MAP.put(CommercialSheetFieldNames.TIER_BASED_PRICING_DISCOUNT.getFieldName(), "F59");
        PRICE_TIER_FIELDS_NAME_VALUES_TO_CELLREFERENCE_MAP.put(CommercialSheetFieldNames.TIER_BASED_PRICING_DISCOUNT_COMMENTS.getFieldName(), "C69");
    }


    public static Map<String, String> VOLUME_TIER_FIELDS_NAME_VALUES_TO_CELLREFERENCE_MAP = new HashMap<>();

    static {
        VOLUME_TIER_FIELDS_NAME_VALUES_TO_CELLREFERENCE_MAP.put(CommercialSheetFieldNames.VOLUME_BASED_PRICING_DISCOUNT.getFieldName(), "F71");
        VOLUME_TIER_FIELDS_NAME_VALUES_TO_CELLREFERENCE_MAP.put(CommercialSheetFieldNames.VOLUME_BASED_PRICING_PERIOD.getFieldName(), "F72");
        VOLUME_TIER_FIELDS_NAME_VALUES_TO_CELLREFERENCE_MAP.put(CommercialSheetFieldNames.VOLUME_TIER_CATEGORY.getFieldName(), "F73");
        VOLUME_TIER_FIELDS_NAME_VALUES_TO_CELLREFERENCE_MAP.put(CommercialSheetFieldNames.VOLUME_TIER_ADMT.getFieldName(), "F74");
        VOLUME_TIER_FIELDS_NAME_VALUES_TO_CELLREFERENCE_MAP.put(CommercialSheetFieldNames.VOLUME_DISCOUNT_REBATE.getFieldName(), "F75");
        VOLUME_TIER_FIELDS_NAME_VALUES_TO_CELLREFERENCE_MAP.put(CommercialSheetFieldNames.VOLUME_BASED_REBATE_COMMENTS.getFieldName(), "C76");
    }


    public static Map<String, String> PORT_REBATE_FIELDS_NAME_VALUES_TO_CELLREFERENCE_MAP = new HashMap<>();

    static {
        PORT_REBATE_FIELDS_NAME_VALUES_TO_CELLREFERENCE_MAP.put(CommercialSheetFieldNames.PORT_REBATE.getFieldName(), "F80");
        PORT_REBATE_FIELDS_NAME_VALUES_TO_CELLREFERENCE_MAP.put(CommercialSheetFieldNames.PORT_REBATE_COMMENTS.getFieldName(), "C81");
    }

    public static final String DROP_DOWN_VALUE = "Value From DropDown";

    public static final String CURRENCY_DOLLAR = "Currency (USD)";

    public static final String YES_OR_NO_REGEX = "^[YN]$";

    public static final String YES_OR_NO = "Y or N";

    public static final String PRICE_UOM = "USD/ADMT";


    // validating Filed Name which (bz it came double)
    public static Map<String, String> INDEX_FIELD_TO_CELL_REFERENCE = new HashMap<>();

    static {
        INDEX_FIELD_TO_CELL_REFERENCE.put(CommercialSheetFieldNames.DISCOUNT.getFieldName(), "B9");
        INDEX_FIELD_TO_CELL_REFERENCE.put(CommercialSheetFieldNames.INDEX_NAME.getFieldName(), "B10");
        INDEX_FIELD_TO_CELL_REFERENCE.put(CommercialSheetFieldNames.INDEX_PUBLISHED_DATE.getFieldName(), "B11");
        INDEX_FIELD_TO_CELL_REFERENCE.put(CommercialSheetFieldNames.ADDITIONAL_ADJUSTMENT_ADMT.getFieldName(), "B12");
        INDEX_FIELD_TO_CELL_REFERENCE.put(CommercialSheetFieldNames.COMMENTS.getFieldName(), "B13");
    }


    public static Map<String, String> MOVEMENT_FIELD_TO_CELL_REFERENCE = new HashMap<>();

    static {
        MOVEMENT_FIELD_TO_CELL_REFERENCE.put(CommercialSheetFieldNames.STARTING_PRICE_POINT.getFieldName(), "B16");
        MOVEMENT_FIELD_TO_CELL_REFERENCE.put(CommercialSheetFieldNames.INDEX_NAME_WITH_MOVEMENT.getFieldName(), "B17");
        MOVEMENT_FIELD_TO_CELL_REFERENCE.put(CommercialSheetFieldNames.MONTH_OVER_MONTH_CHANGE.getFieldName(), "B18");
        MOVEMENT_FIELD_TO_CELL_REFERENCE.put(CommercialSheetFieldNames.INDEX_PUBLISHED_DATE.getFieldName(), "B19");
        MOVEMENT_FIELD_TO_CELL_REFERENCE.put(CommercialSheetFieldNames.ADDITIONAL_ADJUSTMENT_ADMT_WITH_MOVEMENT.getFieldName(), "B20");
        MOVEMENT_FIELD_TO_CELL_REFERENCE.put(CommercialSheetFieldNames.COMMENTS.getFieldName(), "B21");
    }

    public static Map<String, String> HYBRID_FIELD_TO_CELL_REFERENCE = new HashMap<>();

    static {
        HYBRID_FIELD_TO_CELL_REFERENCE.put(CommercialSheetFieldNames.PART_1.getFieldName(), "B24");
        HYBRID_FIELD_TO_CELL_REFERENCE.put(CommercialSheetFieldNames.PART_2.getFieldName(), "B29");
        HYBRID_FIELD_TO_CELL_REFERENCE.put(CommercialSheetFieldNames.PART_3.getFieldName(), "B34");
        HYBRID_FIELD_TO_CELL_REFERENCE.put(CommercialSheetFieldNames.COMMENTS.getFieldName(), "B39");
    }

    public static Map<String, String> PRICE_TIER_1_FIELD_TO_CELL_REFERENCE = new HashMap<>();

    static {
        PRICE_TIER_1_FIELD_TO_CELL_REFERENCE.put(CommercialSheetFieldNames.PRICE_TIER_CATEGORY.getFieldName(), "B60");
        PRICE_TIER_1_FIELD_TO_CELL_REFERENCE.put(CommercialSheetFieldNames.PRICE_RANGE_ADMT.getFieldName(), "B61");
        PRICE_TIER_1_FIELD_TO_CELL_REFERENCE.put(CommercialSheetFieldNames.DISCOUNT_ADMT.getFieldName(), "B62");
    }


    public static Map<String, String> PRICE_TIER_2_FIELD_TO_CELL_REFERENCE = new HashMap<>();

    static {
        PRICE_TIER_2_FIELD_TO_CELL_REFERENCE.put(CommercialSheetFieldNames.PRICE_TIER_CATEGORY.getFieldName(), "B63");
        PRICE_TIER_2_FIELD_TO_CELL_REFERENCE.put(CommercialSheetFieldNames.PRICE_TIER_ADMT.getFieldName(), "B64");
        PRICE_TIER_2_FIELD_TO_CELL_REFERENCE.put(CommercialSheetFieldNames.DISCOUNT_ADMT.getFieldName(), "B65");
    }


    public static Map<String, String> PRICE_TIER_3_FIELD_TO_CELL_REFERENCE = new HashMap<>();

    static {
        PRICE_TIER_3_FIELD_TO_CELL_REFERENCE.put(CommercialSheetFieldNames.PRICE_TIER_CATEGORY.getFieldName(), "B66");
        PRICE_TIER_3_FIELD_TO_CELL_REFERENCE.put(CommercialSheetFieldNames.PRICE_TIER_ADMT_2.getFieldName(), "B67");
        PRICE_TIER_3_FIELD_TO_CELL_REFERENCE.put(CommercialSheetFieldNames.DISCOUNT_ADMT.getFieldName(), "B68");
        PRICE_TIER_3_FIELD_TO_CELL_REFERENCE.put(CommercialSheetFieldNames.COMMENTS.getFieldName(), "B69");
    }

    public static Map<String, String> VOLUME_BASED_REBATE_FIELD_TO_CELL_REFERENCE = new HashMap<>();

    static {
        VOLUME_BASED_REBATE_FIELD_TO_CELL_REFERENCE.put(CommercialSheetFieldNames.VOLUME_BASED_PRICING_DISCOUNT.getFieldName(), "B71");
        VOLUME_BASED_REBATE_FIELD_TO_CELL_REFERENCE.put(CommercialSheetFieldNames.VOLUME_BASED_PRICING_PERIOD.getFieldName(), "B72");
        VOLUME_BASED_REBATE_FIELD_TO_CELL_REFERENCE.put(CommercialSheetFieldNames.VOLUME_TIER_CATEGORY.getFieldName(), "B73");
        VOLUME_BASED_REBATE_FIELD_TO_CELL_REFERENCE.put(CommercialSheetFieldNames.VOLUME_TIER_ADMT.getFieldName(), "B74");
        VOLUME_BASED_REBATE_FIELD_TO_CELL_REFERENCE.put(CommercialSheetFieldNames.VOLUME_DISCOUNT_REBATE.getFieldName(), "B75");
        VOLUME_BASED_REBATE_FIELD_TO_CELL_REFERENCE.put(CommercialSheetFieldNames.COMMENTS.getFieldName(), "B76");
    }


    public static final Map<String, String> BID_QTY_DETAILS_COLUMN_VALUE_TO_COLUMN_NUMBER = new HashMap<>();

    static {

        BID_QTY_DETAILS_COLUMN_VALUE_TO_COLUMN_NUMBER.put(ResponseRfpExcelHeaders.RFP_NUMBER.getValue(), "B8");
        BID_QTY_DETAILS_COLUMN_VALUE_TO_COLUMN_NUMBER.put(ResponseRfpExcelHeaders.DUE_DATE.getValue(), "B9");
        BID_QTY_DETAILS_COLUMN_VALUE_TO_COLUMN_NUMBER.put(ResponseRfpExcelHeaders.FIBER_TYPE.getValue(), "B10");

    }

    public static final Map<String, Integer> LUMP_SUM_HEADER_TO_COLUMN_NUMBER = new HashMap<>();

    static {


        LUMP_SUM_HEADER_TO_COLUMN_NUMBER.put(ResponseRfpExcelHeaders.GP_MILL_EXPECTED_ANNUAL_VOLUME_CELL_NUM.getValue(), 1);
        LUMP_SUM_HEADER_TO_COLUMN_NUMBER.put(ResponseRfpExcelHeaders.SUPPLIER_BID_VOLUME.getValue(), 2);
        LUMP_SUM_HEADER_TO_COLUMN_NUMBER.put(ResponseRfpExcelHeaders.SUPPLIER_MILL_A.getValue(), 2);
        LUMP_SUM_HEADER_TO_COLUMN_NUMBER.put(ResponseRfpExcelHeaders.SUPPLIER_MILL_A_VOLUME.getValue(), 3);
        LUMP_SUM_HEADER_TO_COLUMN_NUMBER.put(ResponseRfpExcelHeaders.SUPPLIER_MILL_B.getValue(), 4);
        LUMP_SUM_HEADER_TO_COLUMN_NUMBER.put(ResponseRfpExcelHeaders.SUPPLIER_MILL_B_VOLUME.getValue(), 5);


    }


    public static final String NA = "N/A";

    //Logistic Pricing Headers enum
    public enum LogisticPricingHeaders {

        SUPPLIER_MILL("Supplier Mill "),

        FIRST_PORT_ENTRY_DETAILS("First Port of Entry Details"),

        INCOTERMS_AND_ASSOCIATED_COST_DETAILS("Incoterms and Associated Cost Details"),

        OCEAN_FREIGHT_DETAILS("Ocean Freight Details"),

        SAFETY_STOCK_DETAILS("Safety Stock Details"),

        INLAND_FREIGHT_DETAILS("Inland Freight Details"),

        SECOND_PORT_ENTRY_DETAILS("Secondary Port of Entry Details"),

        COMMENTS("Comments");

        private final String headerName;

        LogisticPricingHeaders(String headerName) {
            this.headerName = headerName;
        }

        public String getHeaderName() {
            return headerName;
        }

    }

    //Logistic Pricing Fields enum
    public enum LogisticPricingFields {

        GP_MILL("GP Mill *"),
        SUPPLIER_BID_VOLUME("Supplier Bid Volume *"),
        SUPPLIER_MILL("Supplier Mill  *"),
        ORIGIN_PORT("Origin Port *"),
        ORIGIN_COUNTRY("Origin Country *"),
        ENVIRONMENTAL_CERTIFICATION("Environmental Certification *"),
        BALE_PACKAGING("Bale Packaging"),
        BALE_TYPE("Bale Type *"),
        US_PORT_OF_ENTRY("US Port of Entry *"),
        INCOTERMS_1("Incoterms 1*"),
        STEVEDORING_COST("Stevedoring Cost ($/ADMT)"),
        HANDLING_COST("Handling Cost ($/ADMT)"),
        WHARFAGE_COST("Wharfage Cost ($/ADMT)"),
        SECURITY_COST("Security Cost ($/ADMT)"),
        WAREHOUSING_FEE_MONTHLY("Warehousing Fee Monthly ($/ADMT)"),
        IMPORT_CUSTOMS_TARIFF_FEE("Import Customs & Tariff Fee ($/ADMT)"),
        INCOTERMS_2("Incoterms 2 "),
        PORT_FREE_TIME_IN_DAYS("Port Free Time in Days *"),
        TRANSIT_LEAD_TIME_IN_DAYS("Transit Lead Time in Days (Origin Port to US Port of Entry) *"),
        STEAMSHIP_LINE("Steamship Line *"),
        OCEAN_FREIGHT("Ocean Freight ($/ADMT)"),
        NOMINATED_SAFETY_STOCK("Nominated Safety Stock (Days) *"),
        SAFETY_STOCK_LOCATION_TYPE("Safety Stock Location Type *"),
        SAFETY_STOCK_LOCATION_NAME("Safety Stock Location Name *"),
        ADDRESS("Address"),
        INLAND_TRANSIT_ORIGIN_TYPE("Inland Transit Origin Type *"),
        INLAND_TRANSIT_ORIGIN_NAME("Inland Transit Origin Name *"),
        INLAND_TRANSPORTATION_ROUTE("Inland Transportation Route *"),
        INLAND_TRANSIT_DESTINATION_TYPE("Inland Transit Destination Type *"),
        INLAND_TRANSIT_DESTINATION_NAME("Inland Transit Destination Name *"),
        TRANSIT_MODE("Transit Mode *"),
        TRANSIT_COST("Transit Cost ($/ADMT)"),
        TRANSIT_LEAD_TIME_TO_GP_MILL("Transit Lead Time in Days (Ship from Origin to GP Mill) *"),

        TRANSIT_COST_US_PORT_TO_SAFETY_STOCK("Transit Cost ($/ADMT) from US Port to Safety Stock Location ");

        private final String fieldName;

        LogisticPricingFields(String fieldName) {
            this.fieldName = fieldName;
        }

        public String getFieldName() {
            return fieldName;
        }
    }


    // Logistic Pricing Maps
    public static Map<String, String> LOGISTIC_PRICING_HEADER_TO_CELLREFERENCE_MAP = new HashMap<>();

    static {
        LOGISTIC_PRICING_HEADER_TO_CELLREFERENCE_MAP.put(LogisticPricingHeaders.SUPPLIER_MILL.getHeaderName(), "A2");
        LOGISTIC_PRICING_HEADER_TO_CELLREFERENCE_MAP.put(LogisticPricingHeaders.FIRST_PORT_ENTRY_DETAILS.getHeaderName(), "A12");
        LOGISTIC_PRICING_HEADER_TO_CELLREFERENCE_MAP.put(LogisticPricingHeaders.INCOTERMS_AND_ASSOCIATED_COST_DETAILS.getHeaderName(), "A15");
        LOGISTIC_PRICING_HEADER_TO_CELLREFERENCE_MAP.put(LogisticPricingHeaders.OCEAN_FREIGHT_DETAILS.getHeaderName(), "A31");
        LOGISTIC_PRICING_HEADER_TO_CELLREFERENCE_MAP.put(LogisticPricingHeaders.SAFETY_STOCK_DETAILS.getHeaderName(), "A37");
        LOGISTIC_PRICING_HEADER_TO_CELLREFERENCE_MAP.put(LogisticPricingHeaders.INLAND_FREIGHT_DETAILS.getHeaderName(), "A44");
        LOGISTIC_PRICING_HEADER_TO_CELLREFERENCE_MAP.put(LogisticPricingHeaders.SECOND_PORT_ENTRY_DETAILS.getHeaderName(), "A56");
        LOGISTIC_PRICING_HEADER_TO_CELLREFERENCE_MAP.put(LogisticPricingHeaders.COMMENTS.getHeaderName(), "A99");
    }

    public static Map<String, String> LOGISTIC_PRICING_HEADER_TO_CELLREFERENCE_MISMATCH_MAP = new HashMap<>();

    static {
        LOGISTIC_PRICING_HEADER_TO_CELLREFERENCE_MAP.put(LogisticPricingHeaders.INCOTERMS_AND_ASSOCIATED_COST_DETAILS.getHeaderName(), "A59");
        LOGISTIC_PRICING_HEADER_TO_CELLREFERENCE_MAP.put(LogisticPricingHeaders.OCEAN_FREIGHT_DETAILS.getHeaderName(), "A75");
        LOGISTIC_PRICING_HEADER_TO_CELLREFERENCE_MAP.put(LogisticPricingHeaders.SAFETY_STOCK_DETAILS.getHeaderName(), "A81");
        LOGISTIC_PRICING_HEADER_TO_CELLREFERENCE_MAP.put(LogisticPricingHeaders.INLAND_FREIGHT_DETAILS.getHeaderName(), "A88");
    }


    public static Map<String, Integer> LOGISTIC_PRICING_FIELDNAME_TO_ROW_MAP = new HashMap<>();

    static {
        LOGISTIC_PRICING_FIELDNAME_TO_ROW_MAP.put(LogisticPricingFields.GP_MILL.getFieldName(), 1);
        LOGISTIC_PRICING_FIELDNAME_TO_ROW_MAP.put(LogisticPricingFields.SUPPLIER_BID_VOLUME.getFieldName(), 2);
        LOGISTIC_PRICING_FIELDNAME_TO_ROW_MAP.put(LogisticPricingFields.SUPPLIER_MILL.getFieldName(), 3);
        LOGISTIC_PRICING_FIELDNAME_TO_ROW_MAP.put(LogisticPricingFields.ORIGIN_PORT.getFieldName(), 4);
        LOGISTIC_PRICING_FIELDNAME_TO_ROW_MAP.put(LogisticPricingFields.ORIGIN_COUNTRY.getFieldName(), 5);
        LOGISTIC_PRICING_FIELDNAME_TO_ROW_MAP.put(LogisticPricingFields.ENVIRONMENTAL_CERTIFICATION.getFieldName(), 6);
        LOGISTIC_PRICING_FIELDNAME_TO_ROW_MAP.put(LogisticPricingFields.BALE_PACKAGING.getFieldName(), 7);
        LOGISTIC_PRICING_FIELDNAME_TO_ROW_MAP.put(LogisticPricingFields.BALE_TYPE.getFieldName(), 8);
    }


    public static Map<String, Integer> LOGISTIC_PRICING_FIRST_PORT_ENTRY_FIELDNAME_TO_ROW_MAP = new HashMap<>();

    static {
        LOGISTIC_PRICING_FIRST_PORT_ENTRY_FIELDNAME_TO_ROW_MAP.put(LogisticPricingFields.US_PORT_OF_ENTRY.getFieldName(), 11);

        LOGISTIC_PRICING_FIRST_PORT_ENTRY_FIELDNAME_TO_ROW_MAP.put(LogisticPricingFields.PORT_FREE_TIME_IN_DAYS.getFieldName(), 30);
        LOGISTIC_PRICING_FIRST_PORT_ENTRY_FIELDNAME_TO_ROW_MAP.put(LogisticPricingFields.TRANSIT_LEAD_TIME_IN_DAYS.getFieldName(), 31);
        LOGISTIC_PRICING_FIRST_PORT_ENTRY_FIELDNAME_TO_ROW_MAP.put(LogisticPricingFields.STEAMSHIP_LINE.getFieldName(), 32);
        LOGISTIC_PRICING_FIRST_PORT_ENTRY_FIELDNAME_TO_ROW_MAP.put(LogisticPricingFields.OCEAN_FREIGHT.getFieldName(), 33);

        LOGISTIC_PRICING_FIRST_PORT_ENTRY_FIELDNAME_TO_ROW_MAP.put(LogisticPricingFields.NOMINATED_SAFETY_STOCK.getFieldName(), 36);
        LOGISTIC_PRICING_FIRST_PORT_ENTRY_FIELDNAME_TO_ROW_MAP.put(LogisticPricingFields.SAFETY_STOCK_LOCATION_TYPE.getFieldName(), 37);
        LOGISTIC_PRICING_FIRST_PORT_ENTRY_FIELDNAME_TO_ROW_MAP.put(LogisticPricingFields.SAFETY_STOCK_LOCATION_NAME.getFieldName(), 38);
        LOGISTIC_PRICING_FIRST_PORT_ENTRY_FIELDNAME_TO_ROW_MAP.put(LogisticPricingFields.ADDRESS.getFieldName(), 39);
        LOGISTIC_PRICING_FIRST_PORT_ENTRY_FIELDNAME_TO_ROW_MAP.put(LogisticPricingFields.TRANSIT_COST_US_PORT_TO_SAFETY_STOCK.getFieldName(), 40);

        LOGISTIC_PRICING_FIRST_PORT_ENTRY_FIELDNAME_TO_ROW_MAP.put(LogisticPricingFields.INLAND_TRANSIT_ORIGIN_TYPE.getFieldName(), 43);
        LOGISTIC_PRICING_FIRST_PORT_ENTRY_FIELDNAME_TO_ROW_MAP.put(LogisticPricingFields.INLAND_TRANSIT_ORIGIN_NAME.getFieldName(), 44);
        LOGISTIC_PRICING_FIRST_PORT_ENTRY_FIELDNAME_TO_ROW_MAP.put(LogisticPricingFields.INLAND_TRANSPORTATION_ROUTE.getFieldName(), 45);
        LOGISTIC_PRICING_FIRST_PORT_ENTRY_FIELDNAME_TO_ROW_MAP.put(LogisticPricingFields.INLAND_TRANSIT_DESTINATION_TYPE.getFieldName(), 46);
        LOGISTIC_PRICING_FIRST_PORT_ENTRY_FIELDNAME_TO_ROW_MAP.put(LogisticPricingFields.INLAND_TRANSIT_DESTINATION_NAME.getFieldName(), 47);
        LOGISTIC_PRICING_FIRST_PORT_ENTRY_FIELDNAME_TO_ROW_MAP.put(LogisticPricingFields.TRANSIT_MODE.getFieldName(), 49);
        LOGISTIC_PRICING_FIRST_PORT_ENTRY_FIELDNAME_TO_ROW_MAP.put(LogisticPricingFields.TRANSIT_COST.getFieldName(), 50);
        LOGISTIC_PRICING_FIRST_PORT_ENTRY_FIELDNAME_TO_ROW_MAP.put(LogisticPricingFields.TRANSIT_LEAD_TIME_TO_GP_MILL.getFieldName(), 51);
    }


    public static Map<String, Integer> LOGISTIC_PRICING_FIRST_PORT_ENTRY_FIELDNAME_TO_ROW_MISMATCH_MAP = new HashMap<>();

    static {
        LOGISTIC_PRICING_FIRST_PORT_ENTRY_FIELDNAME_TO_ROW_MISMATCH_MAP.put(LogisticPricingFields.ADDRESS.getFieldName(), 48);
    }

    public static Map<String, Integer> LOGISTIC_PRICING_FIRST_PORT_ENTRY_INCOTERM_1_FIELDNAME_TO_ROW_MAP = new HashMap<>();

    static {
        LOGISTIC_PRICING_FIRST_PORT_ENTRY_INCOTERM_1_FIELDNAME_TO_ROW_MAP.put(LogisticPricingFields.INCOTERMS_1.getFieldName(), 14);

        LOGISTIC_PRICING_FIRST_PORT_ENTRY_INCOTERM_1_FIELDNAME_TO_ROW_MAP.put(LogisticPricingFields.STEVEDORING_COST.getFieldName(), 15);
        LOGISTIC_PRICING_FIRST_PORT_ENTRY_INCOTERM_1_FIELDNAME_TO_ROW_MAP.put(LogisticPricingFields.HANDLING_COST.getFieldName(), 16);
        LOGISTIC_PRICING_FIRST_PORT_ENTRY_INCOTERM_1_FIELDNAME_TO_ROW_MAP.put(LogisticPricingFields.WHARFAGE_COST.getFieldName(), 17);
        LOGISTIC_PRICING_FIRST_PORT_ENTRY_INCOTERM_1_FIELDNAME_TO_ROW_MAP.put(LogisticPricingFields.SECURITY_COST.getFieldName(), 18);
        LOGISTIC_PRICING_FIRST_PORT_ENTRY_INCOTERM_1_FIELDNAME_TO_ROW_MAP.put(LogisticPricingFields.WAREHOUSING_FEE_MONTHLY.getFieldName(), 19);
        LOGISTIC_PRICING_FIRST_PORT_ENTRY_INCOTERM_1_FIELDNAME_TO_ROW_MAP.put(LogisticPricingFields.IMPORT_CUSTOMS_TARIFF_FEE.getFieldName(), 20);
    }


    public static Map<String, Integer> LOGISTIC_PRICING_FIRST_PORT_ENTRY_INCOTERM_2_FIELDNAME_TO_ROW_MAP = new HashMap<>();

    static {
        LOGISTIC_PRICING_FIRST_PORT_ENTRY_INCOTERM_2_FIELDNAME_TO_ROW_MAP.put(LogisticPricingFields.INCOTERMS_2.getFieldName(), 21);

        LOGISTIC_PRICING_FIRST_PORT_ENTRY_INCOTERM_2_FIELDNAME_TO_ROW_MAP.put(LogisticPricingFields.STEVEDORING_COST.getFieldName(), 22);
        LOGISTIC_PRICING_FIRST_PORT_ENTRY_INCOTERM_2_FIELDNAME_TO_ROW_MAP.put(LogisticPricingFields.HANDLING_COST.getFieldName(), 23);
        LOGISTIC_PRICING_FIRST_PORT_ENTRY_INCOTERM_2_FIELDNAME_TO_ROW_MAP.put(LogisticPricingFields.WHARFAGE_COST.getFieldName(), 24);
        LOGISTIC_PRICING_FIRST_PORT_ENTRY_INCOTERM_2_FIELDNAME_TO_ROW_MAP.put(LogisticPricingFields.SECURITY_COST.getFieldName(), 25);
        LOGISTIC_PRICING_FIRST_PORT_ENTRY_INCOTERM_2_FIELDNAME_TO_ROW_MAP.put(LogisticPricingFields.WAREHOUSING_FEE_MONTHLY.getFieldName(), 26);
        LOGISTIC_PRICING_FIRST_PORT_ENTRY_INCOTERM_2_FIELDNAME_TO_ROW_MAP.put(LogisticPricingFields.IMPORT_CUSTOMS_TARIFF_FEE.getFieldName(), 27);
    }


    public static Map<String, Integer> LOGISTIC_PRICING_SECOND_PORT_ENTRY_FIELDNAME_TO_ROW_MAP = new HashMap<>();

    static {
        LOGISTIC_PRICING_SECOND_PORT_ENTRY_FIELDNAME_TO_ROW_MAP.put(LogisticPricingFields.US_PORT_OF_ENTRY.getFieldName(), 55);

        LOGISTIC_PRICING_SECOND_PORT_ENTRY_FIELDNAME_TO_ROW_MAP.put(LogisticPricingFields.PORT_FREE_TIME_IN_DAYS.getFieldName(), 74);
        LOGISTIC_PRICING_SECOND_PORT_ENTRY_FIELDNAME_TO_ROW_MAP.put(LogisticPricingFields.TRANSIT_LEAD_TIME_IN_DAYS.getFieldName(), 75);
        LOGISTIC_PRICING_SECOND_PORT_ENTRY_FIELDNAME_TO_ROW_MAP.put(LogisticPricingFields.STEAMSHIP_LINE.getFieldName(), 76);
        LOGISTIC_PRICING_SECOND_PORT_ENTRY_FIELDNAME_TO_ROW_MAP.put(LogisticPricingFields.OCEAN_FREIGHT.getFieldName(), 77);

        LOGISTIC_PRICING_SECOND_PORT_ENTRY_FIELDNAME_TO_ROW_MAP.put(LogisticPricingFields.NOMINATED_SAFETY_STOCK.getFieldName(), 80);
        LOGISTIC_PRICING_SECOND_PORT_ENTRY_FIELDNAME_TO_ROW_MAP.put(LogisticPricingFields.SAFETY_STOCK_LOCATION_TYPE.getFieldName(), 81);
        LOGISTIC_PRICING_SECOND_PORT_ENTRY_FIELDNAME_TO_ROW_MAP.put(LogisticPricingFields.SAFETY_STOCK_LOCATION_NAME.getFieldName(), 82);
        LOGISTIC_PRICING_SECOND_PORT_ENTRY_FIELDNAME_TO_ROW_MAP.put(LogisticPricingFields.ADDRESS.getFieldName(), 83);
        LOGISTIC_PRICING_SECOND_PORT_ENTRY_FIELDNAME_TO_ROW_MAP.put(LogisticPricingFields.TRANSIT_COST_US_PORT_TO_SAFETY_STOCK.getFieldName(), 84);

        LOGISTIC_PRICING_SECOND_PORT_ENTRY_FIELDNAME_TO_ROW_MAP.put(LogisticPricingFields.INLAND_TRANSIT_ORIGIN_TYPE.getFieldName(), 87);
        LOGISTIC_PRICING_SECOND_PORT_ENTRY_FIELDNAME_TO_ROW_MAP.put(LogisticPricingFields.INLAND_TRANSIT_ORIGIN_NAME.getFieldName(), 88);
        LOGISTIC_PRICING_SECOND_PORT_ENTRY_FIELDNAME_TO_ROW_MAP.put(LogisticPricingFields.INLAND_TRANSPORTATION_ROUTE.getFieldName(), 89);
        LOGISTIC_PRICING_SECOND_PORT_ENTRY_FIELDNAME_TO_ROW_MAP.put(LogisticPricingFields.INLAND_TRANSIT_DESTINATION_TYPE.getFieldName(), 90);
        LOGISTIC_PRICING_SECOND_PORT_ENTRY_FIELDNAME_TO_ROW_MAP.put(LogisticPricingFields.INLAND_TRANSIT_DESTINATION_NAME.getFieldName(), 91);
        LOGISTIC_PRICING_SECOND_PORT_ENTRY_FIELDNAME_TO_ROW_MAP.put(LogisticPricingFields.TRANSIT_MODE.getFieldName(), 93);
        LOGISTIC_PRICING_SECOND_PORT_ENTRY_FIELDNAME_TO_ROW_MAP.put(LogisticPricingFields.TRANSIT_COST.getFieldName(), 94);
        LOGISTIC_PRICING_SECOND_PORT_ENTRY_FIELDNAME_TO_ROW_MAP.put(LogisticPricingFields.TRANSIT_LEAD_TIME_TO_GP_MILL.getFieldName(), 95);
    }


    public static Map<String, Integer> LOGISTIC_PRICING_SECOND_PORT_ENTRY_FIELDNAME_TO_ROW_MISMATCH_MAP = new HashMap<>();

    static {
        LOGISTIC_PRICING_SECOND_PORT_ENTRY_FIELDNAME_TO_ROW_MISMATCH_MAP.put(LogisticPricingFields.ADDRESS.getFieldName(), 92);
    }


    public static Map<String, Integer> LOGISTIC_PRICING_SECOND_PORT_ENTRY_INCOTERM_1_FIELDNAME_TO_ROW_MAP = new HashMap<>();

    static {
        LOGISTIC_PRICING_SECOND_PORT_ENTRY_INCOTERM_1_FIELDNAME_TO_ROW_MAP.put(LogisticPricingFields.INCOTERMS_1.getFieldName(), 58);

        LOGISTIC_PRICING_SECOND_PORT_ENTRY_INCOTERM_1_FIELDNAME_TO_ROW_MAP.put(LogisticPricingFields.STEVEDORING_COST.getFieldName(), 59);
        LOGISTIC_PRICING_SECOND_PORT_ENTRY_INCOTERM_1_FIELDNAME_TO_ROW_MAP.put(LogisticPricingFields.HANDLING_COST.getFieldName(), 60);
        LOGISTIC_PRICING_SECOND_PORT_ENTRY_INCOTERM_1_FIELDNAME_TO_ROW_MAP.put(LogisticPricingFields.WHARFAGE_COST.getFieldName(), 61);
        LOGISTIC_PRICING_SECOND_PORT_ENTRY_INCOTERM_1_FIELDNAME_TO_ROW_MAP.put(LogisticPricingFields.SECURITY_COST.getFieldName(), 62);
        LOGISTIC_PRICING_SECOND_PORT_ENTRY_INCOTERM_1_FIELDNAME_TO_ROW_MAP.put(LogisticPricingFields.WAREHOUSING_FEE_MONTHLY.getFieldName(), 63);
        LOGISTIC_PRICING_SECOND_PORT_ENTRY_INCOTERM_1_FIELDNAME_TO_ROW_MAP.put(LogisticPricingFields.IMPORT_CUSTOMS_TARIFF_FEE.getFieldName(), 64);
    }


    public static Map<String, Integer> LOGISTIC_PRICING_SECOND_PORT_ENTRY_INCOTERM_2_FIELDNAME_TO_ROW_MAP = new HashMap<>();

    static {
        LOGISTIC_PRICING_SECOND_PORT_ENTRY_INCOTERM_2_FIELDNAME_TO_ROW_MAP.put(ResponseRfpExcelHeaders.INCOTERMS_2.getValue(), 65);

        LOGISTIC_PRICING_SECOND_PORT_ENTRY_INCOTERM_2_FIELDNAME_TO_ROW_MAP.put(ResponseRfpExcelHeaders.STEVEDORING_COST.getValue(), 66);
        LOGISTIC_PRICING_SECOND_PORT_ENTRY_INCOTERM_2_FIELDNAME_TO_ROW_MAP.put(ResponseRfpExcelHeaders.HANDLING_COST.getValue(), 67);
        LOGISTIC_PRICING_SECOND_PORT_ENTRY_INCOTERM_2_FIELDNAME_TO_ROW_MAP.put(ResponseRfpExcelHeaders.WHARFAGE_COST.getValue(), 68);
        LOGISTIC_PRICING_SECOND_PORT_ENTRY_INCOTERM_2_FIELDNAME_TO_ROW_MAP.put(ResponseRfpExcelHeaders.SECURITY_COST.getValue(), 69);
        LOGISTIC_PRICING_SECOND_PORT_ENTRY_INCOTERM_2_FIELDNAME_TO_ROW_MAP.put(ResponseRfpExcelHeaders.WAREHOUSING_FEE_MONTHLY.getValue(), 70);
        LOGISTIC_PRICING_SECOND_PORT_ENTRY_INCOTERM_2_FIELDNAME_TO_ROW_MAP.put(ResponseRfpExcelHeaders.IMPORT_CUSTOMS_TARIFF_FEE.getValue(), 71);
    }

    public static Integer SECOND_US_PORT_OF_ENTRY_ROW = 55;

    public static Integer LOGISTIC_PRICING_SHEET_STARTING_COLUMN = 5;

    public static Integer SUPPLIER_BID_VOL_ROW = 2;

    public static String GP_MILL_LUMP_SUM = "LUMP_SUM_MILL";

    public static String INDEX_BASIS_INDEX_PUBLISHED_DATE_EXAMPLES = "First Tuesday of the Month/Second Wednesday of the Month/Third Tuesday of the Month/Last Tuesday of the Month/28";

    public static String MECHANISM_BASIS_INDEX_PUBLISHED_DATE_EXAMPLES = "1 Month Last Tuesday of the Month/2 Quarter First Tuesday of the Month/1 Full Year Second Wednesday of the Month/2 Half Year Third Tuesday of the Month/1 Month 28";

    // weekday and given day regex for Index and movement mechanism basis
    public static final String WEEK_DAY_REGEX_FOR_INDEX = "^(First|Second|Third|Fourth|Last) (Monday|Tuesday|Wednesday|Thursday|Friday|Saturday|Sunday) of the Month$";

    public static final String GIVEN_DATE_REGEX_FOR_INDEX = "^(0?[1-9]|[12][0-9]|3[01])$";
    public static final String WEEK_DAY_REGEX_FOR_MOVEMENT = "^(\\d{1,2}) (Month|Quarter|Half Year|Full Year) (First|Second|Third|Fourth|Last) (Monday|Tuesday|Wednesday|Thursday|Friday|Saturday|Sunday) of the Month$";
    public static final String GIVEN_DATE_REGEX_FOR_MOVEMENT = "^(\\d+) (Month|Quarter|Half Year|Full Year) (\\d+)$";

    public static final String IF_LUMP_SUM_CONDITION = "If Lump Sum";


    public static final Integer IF_LUMP_SUM_TO_TOTAL_ROW_NUM_DIFF = 3;

    public static final  Integer LUMP_SUM_TO_TOTAL_ROW_NUM_DIFF = 6;
    public static final Integer TOTAL_ROW_NUL_TO_LUMP_SUM_DIFF = 4;

    public static final Integer LUMP_SUM_ABOVE_ROW_TO_SUPPLIER_DETAILS_ROW = 3;
    public static final Integer MILL_SPECIFIC_TOTAL_TO_LUMP_SUM_MILL_DETAILS_ROW_DIFF = 7;


    public static final Map<String, String> BID_QTY_SHEET_HEADERS_VALUES_CELL_REFERENCE = new HashMap<>();

    static {
        BID_QTY_SHEET_HEADERS_VALUES_CELL_REFERENCE.put(ResponseRfpExcelHeaders.SUPPLIER_NAME.getValue(), "B6");
        BID_QTY_SHEET_HEADERS_VALUES_CELL_REFERENCE.put(ResponseRfpExcelHeaders.CONTACT_EMAIL.getValue(), "B7");
        BID_QTY_SHEET_HEADERS_VALUES_CELL_REFERENCE.put(ResponseRfpExcelHeaders.RFP_NUMBER.getValue(), "B8");
        BID_QTY_SHEET_HEADERS_VALUES_CELL_REFERENCE.put(ResponseRfpExcelHeaders.DUE_DATE.getValue(), "B9");
        BID_QTY_SHEET_HEADERS_VALUES_CELL_REFERENCE.put(ResponseRfpExcelHeaders.FIBER_TYPE.getValue(), "B10");
        BID_QTY_SHEET_HEADERS_VALUES_CELL_REFERENCE.put(ResponseRfpExcelHeaders.CONTRACT_TERM.getValue(), "B11");
    }

    public static final Integer LUMP_SUM_DETAILS_ROW_NUMBER = 43;

    public static final String OF_THE_MONTH = "of the Month";
    public static final String LOGISTIC_PRICING_COMMENTS_CELL = "B99";

    public static final String DEFAULT_LOGISTIC_COMMENT = "(If offer is different than categories above, please explain in this area (please include as much details as possible)";

    public static final List<String> OceanFreightDetailsList = new ArrayList<>(Arrays.asList(LogisticPricingFields.PORT_FREE_TIME_IN_DAYS.getFieldName(), LogisticPricingFields.TRANSIT_LEAD_TIME_IN_DAYS.getFieldName(), LogisticPricingFields.STEAMSHIP_LINE.getFieldName()));

    public static final String OTHER_THAN = "Other than";

    public enum SupplierType {
        DOMESTIC("Domestic"),

        INTERNATIONAL("International");

        private final String value;

        SupplierType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public static final String BID_TYPE = "Bid Type";

    public static final String supplierMillA = "SupplierMillA";

    public static final String supplierMillB = "SupplierMillB";

    public static final String CONTRACT_TERM_REGEX = "^(20[0-9]{2})$";

    public static final String YEAR = "YEAR";

    public static final String START_MONTH_DATE = "01/01/";

    public static final String END_MONTH_DATE = "12/31/";
}