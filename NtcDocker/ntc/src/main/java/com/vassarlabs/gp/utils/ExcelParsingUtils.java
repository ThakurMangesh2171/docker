package com.vassarlabs.gp.utils;

import com.google.common.collect.Sets;
import com.vassarlabs.gp.constants.Constants;
import com.vassarlabs.gp.constants.ErrorMessages;
import com.vassarlabs.gp.constants.ExcelConstants;
import com.vassarlabs.gp.constants.ExcelConstants.ResponseRfpExcelHeaders;
import com.vassarlabs.gp.exception.FileParsingException;
import com.vassarlabs.gp.pojo.ErrorMessageDetails;
import com.vassarlabs.gp.pojo.Mills;
import com.vassarlabs.gp.pojo.ResponseRfpExcelResponse;
import com.vassarlabs.gp.pojo.ResponseRfpJson.*;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ExcelParsingUtils {


    public static List<ErrorMessageDetails> errorMessageDetailsList = new ArrayList<>();

    public static List<ErrorMessageDetails> responseRfpWarningList = new ArrayList<>();


    private ExcelParsingUtils() {
    }

    public static final DataFormatter df = new DataFormatter();

    private static final Logger LOGGER = LogManager.getLogger(ExcelParsingUtils.class);

    public static Map<String, Integer> commercialPricingMap = ExcelConstants.COMMERICIAL_PRICING_MAP;
    public static Map<String, Integer> commercialPricingMismatchMap = ExcelConstants.COMMERCIAL_PRICING_MISMATCH_MAP;
    public static Map<String, Integer> bidDetailsMap = ExcelConstants.BID_DETAILS_MAP;
    public static Integer[] periodArray = new Integer[12];

    public static List<String> periodList = new ArrayList<>();
    public static Map<String, int[]> periodIndexMap = ExcelConstants.PERIOD_INDEX_MAP;

    public static Map<Character, String> periodMapCommercial = ExcelConstants.PERIOD_MAP_COMMERCIAL_PRICING;

    //Logistic Pricing Maps
    public static Map<String, String> logisticPricingHeaderToCellRefrenceMap = ExcelConstants.LOGISTIC_PRICING_HEADER_TO_CELLREFERENCE_MAP;
    public static Map<String, String> logisticPricingHeaderToCellRefrenceMisMatchMap = ExcelConstants.LOGISTIC_PRICING_HEADER_TO_CELLREFERENCE_MISMATCH_MAP;
    public static Map<String, Integer> logisticPricingFieldNameToRowMap = ExcelConstants.LOGISTIC_PRICING_FIELDNAME_TO_ROW_MAP;
    public static Map<String, Integer> logisticPricingFirstPortEntryFieldNameToRowMap = ExcelConstants.LOGISTIC_PRICING_FIRST_PORT_ENTRY_FIELDNAME_TO_ROW_MAP;

    public static Map<String, Integer> logisticPricingFirstPortEntryFieldNameToRowMismatchMap = ExcelConstants.LOGISTIC_PRICING_FIRST_PORT_ENTRY_FIELDNAME_TO_ROW_MISMATCH_MAP;

    public static Map<String, Integer> logisticPricingFirstPortEntryIncoterm1FieldNameToRowMap = ExcelConstants.LOGISTIC_PRICING_FIRST_PORT_ENTRY_INCOTERM_1_FIELDNAME_TO_ROW_MAP;

    public static Map<String, Integer> logisticPricingFirstPortEntryIncoterm2FieldNameToRowMap = ExcelConstants.LOGISTIC_PRICING_FIRST_PORT_ENTRY_INCOTERM_2_FIELDNAME_TO_ROW_MAP;

    public static Map<String, Integer> logisticPricingSecondPortEntryFieldNameToRowMap = ExcelConstants.LOGISTIC_PRICING_SECOND_PORT_ENTRY_FIELDNAME_TO_ROW_MAP;

    public static Map<String, Integer> logisticPricingSecondPortEntryFieldNameToRowMismatchMap = ExcelConstants.LOGISTIC_PRICING_SECOND_PORT_ENTRY_FIELDNAME_TO_ROW_MISMATCH_MAP;

    public static Map<String, Integer> logisticPricingSecondPortEntryIncoterm1FieldNameToRowMap = ExcelConstants.LOGISTIC_PRICING_SECOND_PORT_ENTRY_INCOTERM_1_FIELDNAME_TO_ROW_MAP;

    public static Map<String, Integer> logisticPricingSecondPortEntryIncoterm2FieldNameToRowMap = ExcelConstants.LOGISTIC_PRICING_SECOND_PORT_ENTRY_INCOTERM_2_FIELDNAME_TO_ROW_MAP;

    //Map of Supplier mill to Gp mill to Supplier Bid Volume
    public static Map<String, Map<String, Long>> supplierMillToGpMillToSupplierBidVolume = new HashMap<>();
    public static Map<String, Long> supplierMillToSupplierBidVolume = new HashMap<>();

    // BidQty Details related variables
    public static Integer startColumnNumber;

    public static Integer endColumnNumber;

    public static Integer endRowOfMills;

    public static Boolean isOneTime = Boolean.FALSE;

    public static String period = null;
    public static List<Ports> portRebateList = new ArrayList<>();


    public static boolean isRowEmpty(Row row) {
        if (row != null) {
            for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
                Cell cell = row.getCell(c);
                if (cell != null && cell.getCellType() != CellType.BLANK)
                    return false;
            }
        }
        return true;
    }


    //Method to Populate Response Rfp Json Template From Submitted Excel sheet/File
    public static ResponseRfpExcelResponse populateResponseRfpJsonFromExcel(MultipartFile responseRfpExcel, File file, String supplierName) {
        LOGGER.info("In ExcelParsingUtils :: populateResponseRfpJsonFromExcel");
        ResponseRfpExcelResponse responseRfpExcelResponse = new ResponseRfpExcelResponse();
        RfpJsonTemplate rfpJsonTemplate = new RfpJsonTemplate();
        errorMessageDetailsList = new ArrayList<>();
        responseRfpWarningList = new ArrayList<>();
        Set<String> portOfEntries = new HashSet<>();
        period = "";

        //PeriodArray is used for Validating Periods in Pricing Tier to cover entire year without overlapping Months
        //Filling Period array of pricing details with values 0
        Arrays.fill(periodArray, 0);
        periodList = new ArrayList<>();

        //Initializing isOneTime everytime we populate excel ( Logistic Sheet )
        //This is used to determine whether a second port entry has been made when certain fields of the second port entry have been filled in.
         isOneTime = Boolean.FALSE;

        //Initializing Logistic Pricing Supplier Bid volume maps again again
        supplierMillToGpMillToSupplierBidVolume = new HashMap<>();
        supplierMillToSupplierBidVolume = new HashMap<>();


        //If Excel is  null read from file
        try (Workbook workbook = (responseRfpExcel == null) ? new XSSFWorkbook(new FileInputStream(file)) : new XSSFWorkbook(responseRfpExcel.getInputStream())) {
            Sheet logisticSheet = workbook.getSheet(ExcelConstants.ResponseRfpExcelSheetNames.LOGISTIC_PRICING.getSheetName());
            Sheet bidQtySheet = workbook.getSheet(ExcelConstants.ResponseRfpExcelSheetNames.BID_DETAILS.getSheetName());
            Sheet commercialPricingSheet = workbook.getSheet(ExcelConstants.ResponseRfpExcelSheetNames.COMMERCIAL_PRICING.getSheetName());

            //Fetch Rfp Number from Bid Qty Details Sheet
            String rfpNumber = getRfpNumberFromBidQtyDetailsSheet(bidQtySheet);

            //Fetching DueDate value from bidQtyDetailsSheet and setting values
            String dueDate = getDueDate(bidQtySheet);
            if (rfpNumber != null) {
                responseRfpExcelResponse.setRfpNumber(rfpNumber);
            }
            if (dueDate != null) {
                responseRfpExcelResponse.setDueDate(dueDate);
            }


            //Check if any sheet is missing , if yes return
            if (!checkIfResponseRfpSheetsPresent(workbook)) {
                responseRfpExcelResponse.setErrorMessageDetails(errorMessageDetailsList);
                if (responseRfpExcelResponse.getRfpJsonTemplate() != null && supplierName != null) {
                    responseRfpExcelResponse.getRfpJsonTemplate().setSupplier(supplierName);
                }
                return responseRfpExcelResponse;
            }

            //Fetching fiberType value from bidQtyDetails sheet and setting values
            String fiberType = getFiberType(bidQtySheet);

            // Set of logistic pricing mills
            Set<String> logisticPricingMills = new HashSet<>();

            Map<String, Set<String>> mapOfSupplierMillsToGpMills = new HashMap<>();

            Map<String,List<Ports>> supplierMillToPorts = new HashMap<>();

            // Setting ErrorCount
            Integer errorCount = errorMessageDetailsList.size();


            //Prepare Supplier Mills List with SupplierMills Meta Data and  Freight Details
            //TODO check in the below method are they doing null check for fiberType
            List<SupplierMills> supplierMillsList = populateSupplierMillsAndFreightDetails(logisticSheet, portOfEntries, fiberType, logisticPricingMills, mapOfSupplierMillsToGpMills,supplierMillToPorts);
            //Populate Commercial Pricing Details by Parsing Commercial Pricing Sheet
            //Old Template Code
//            PricingDetail pricingDetail = new PricingDetail();
//            PricingDetail pricingDetail = parseAndPopulateCommercialPricingDetails(commercialPricingSheet, portOfEntries);
            //new Template
            //Map of Supplier Mill Name to pricing Details
            Map<String,PricingDetail> supplierMillToPricingDetails = populateCommercialPricingDetails(commercialPricingSheet,responseRfpExcelResponse,supplierMillsList,supplierMillToPorts);

            //TODO :: need to uncomment if need whole year validation
//            //validating if whole year is covered or not
//            if(Arrays.asList(periodArray).contains(Constants.ZERO) == Boolean.TRUE.equals(Boolean.TRUE)){
//                String errorMessage = MessageFormat.format(ErrorMessages.Messages.PERIOD_MISSING_ERROR.getMessage(), ResponseRfpExcelHeaders.TIME_WINDOW_PERIOD.getValue());
//                errorMessageDetailsList.add(new ErrorMessageDetails(ExcelConstants.ResponseRfpExcelSheetNames.COMMERCIAL_PRICING.getSheetName(),null , null , null, ResponseRfpExcelHeaders.TIME_WINDOW_PERIOD.getValue(), errorMessage));
//            }


            //Parsing Bid Qty Details Sheet and setting values
            if (supplierMillsList != null) {
                rfpJsonTemplate.setSupplier_mills(supplierMillsList);
                List<Mills> millsList = new ArrayList<>();
                Map<String, BidQtyDetail> millNameToBidQtyDetails = populateBidQtyDetails(bidQtySheet, millsList);
                //TODO : review below 2 Methods
                setBidQtyDetailsAndPricingDetailsInJson(millNameToBidQtyDetails, supplierMillsList, supplierMillToPricingDetails, errorCount);
                validateGpMillsForSpecificSupplier(mapOfSupplierMillsToGpMills, millNameToBidQtyDetails,errorCount);
                //validating BitVolume From BidQtySheet To LogisticSheet
                validateBidVolumeFromBidQtySheetToLogisticSheet(millNameToBidQtyDetails,supplierMillToGpMillToSupplierBidVolume,supplierMillToSupplierBidVolume,errorCount);
                //setting GP Mills details
                responseRfpExcelResponse.setMills(millsList);
            }

            responseRfpExcelResponse.setRfpJsonTemplate(rfpJsonTemplate);

            responseRfpExcelResponse.setErrorMessageDetails(errorMessageDetailsList);

            responseRfpExcelResponse.setResponseRfpWarning(responseRfpWarningList);

        } catch (Exception e) {
            e.printStackTrace();
            throw new FileParsingException(ErrorMessages.FILE_READING_ERROR);
        }

        if (responseRfpExcelResponse.getErrorMessageDetails() != null && !responseRfpExcelResponse.getErrorMessageDetails().isEmpty()) {
            responseRfpExcelResponse.setRfpJsonTemplate(null);
            responseRfpExcelResponse.setMills(null);
        }
        if (responseRfpExcelResponse.getRfpJsonTemplate() != null && supplierName != null) {
            responseRfpExcelResponse.getRfpJsonTemplate().setSupplier(supplierName);
        }
        //TODO : Handle this In PopulateLogistic Details
//            setUsPortEntryToNullForDomestic(rfpJsonTemplate);


        return responseRfpExcelResponse;

    }

    //method to populate Pricing Details(Suppliername to pricing Details map) From Commercial Sheet
    private static Map<String,PricingDetail> populateCommercialPricingDetails(Sheet sheet, ResponseRfpExcelResponse responseRfpExcelResponse, List<SupplierMills> supplierMillsList, Map<String, List<Ports>> supplierMillToPorts) throws CloneNotSupportedException {
        LOGGER.info("In ExcelParsingUtils :: populateCommercialPricingDetails ");

        Map<String,PricingDetail> supplierMillNameToPricingDetails = new HashMap<>();
        PricingDetail pricingDetail = new PricingDetail();


        //Validating if all headers are present or not
        verifyAllCommercialHeadersPresent(sheet, ExcelConstants.COMMERCIAL_PRICING_HEADER_TO_CELLREFERENCE_MAP);

        // validating The Filed Names of CommercialSheet
        verifyAllFieldsNameForCommercialSheet(sheet);


        // TODO :: setting all these Details
        //setting time_window, time_window_period,
        // volume_based_period, discount_uom, price_uom, period_detail,
        // discounts_allowances,  comments


        //setting  mechanism_basis
        setMechanismBasisValue(sheet, pricingDetail);


        //setting is_movement_basis (if the Mechanism is Movement then is_movement_based == true else false)
        if (pricingDetail.getMechanism_basis() != null && pricingDetail.getMechanism_basis().equals(Constants.MECHANISMS.MOVEMENT.getValue())) {
            pricingDetail.setIs_movement_based(Boolean.TRUE);
        } else {
            pricingDetail.setIs_movement_based(Boolean.FALSE);
        }


        //setting Period Details
        List<PeriodDetail> periodDetailsList = parsePeriodDetails(sheet,pricingDetail,responseRfpExcelResponse);


        pricingDetail.setPeriod_detail(periodDetailsList);


        //setting Collars like payment_term, monthly negotiation , price_ceil, price_floor, ceil_floor_uom, ceil_floor_period_start, ceil_floor_period_end,
        parsingPricingDetailsBasicValues(sheet, pricingDetail);

        pricingDetail.setDiscounts_allowances(new DiscountsAllowances());
        //Price Tier
        PriceTierDiscounts priceTierDiscounts = populatePriceTierDiscountsFromExcel(sheet);
        if (priceTierDiscounts != null) {
            pricingDetail.getDiscounts_allowances().setPrice_tier_discounts(priceTierDiscounts);
        }
        // Volume Tier
        VolumeTierDiscounts volumeTierDiscounts = populateVolumeTierDiscountsFromExcel(sheet,pricingDetail);
        if(volumeTierDiscounts!=null){
            pricingDetail.getDiscounts_allowances().setVolume_tier_discounts(volumeTierDiscounts);
        }


        //Port Rebate
        PortRebates portRebates = populatePortRebateFromExcel(sheet);
        if (portRebates != null) {
            pricingDetail.getDiscounts_allowances().setPort_rebates(portRebates);
        }

        for(SupplierMills supplierMills : supplierMillsList){
            if (pricingDetail.getDiscounts_allowances()!=null && pricingDetail.getDiscounts_allowances().getPort_rebates()!=null) {
                PricingDetail supplierPricingDetail = (PricingDetail) pricingDetail.clone();
                supplierPricingDetail.getDiscounts_allowances().getPort_rebates().setPorts(supplierMillToPorts.get(supplierMills.getSupplier_mill()));
                supplierMillNameToPricingDetails.put(supplierMills.getSupplier_mill(), supplierPricingDetail);
            }
        }

        return supplierMillNameToPricingDetails;
    }

    //Method to verify Commercial Headers are present
    private static void verifyAllCommercialHeadersPresent(Sheet sheet, Map<String, String> commercialPricingHeaderToCellreferenceMap) {
        String errorMessage;
        //Iterating header to Cell reference map and verifying Header Names
        for (Map.Entry<String, String> headerToCellRefMap : commercialPricingHeaderToCellreferenceMap.entrySet()) {
            String cellReference = headerToCellRefMap.getValue();

            CellReference cellRef = new CellReference(cellReference);
            Cell cell = sheet.getRow(cellRef.getRow()).getCell(cellRef.getCol());
            String givenHeaderValue = df.formatCellValue(cell);

            if (givenHeaderValue != null && !givenHeaderValue.isEmpty()) {
                // if the Wrong Header Value is Given then adding to ErrorList
                if (!givenHeaderValue.equals(headerToCellRefMap.getKey())) {
                    errorMessage = MessageFormat.format(ErrorMessages.Messages.HEADER_INVALID.getMessage(), givenHeaderValue, headerToCellRefMap.getKey());
                    errorMessageDetailsList.add(new ErrorMessageDetails(sheet.getSheetName(), cellReference, null, null, null, errorMessage));
                }

            } else {
                //Building Error Message with Comma separated Header Names which are missing
                errorMessage = MessageFormat.format(ErrorMessages.Messages.HEADERS_NOT_FOUND.getMessage(), headerToCellRefMap.getKey());
                errorMessageDetailsList.add(new ErrorMessageDetails(sheet.getSheetName(), cellReference, null, null, null, errorMessage));
            }
        }

    }

    //Method to fetch Due Date From Bid Qty Details Sheet
    private static String getDueDate(Sheet bidQtySheet) {
        // fetch value
        CellReference cellReference = new CellReference(ExcelConstants.BID_QTY_DETAILS_COLUMN_HEADER_TO_COLUMN_NUMBER.get(ResponseRfpExcelHeaders.DUE_DATE.getValue()));
        //Validate Header
        if (Boolean.TRUE.equals(validateHeadersForBidQtyDetails(bidQtySheet.getRow(cellReference.getRow()), cellReference.getCol(), ResponseRfpExcelHeaders.DUE_DATE.getValue()))) {
            //Validate value (Regex and Mandatory )
            if (Boolean.TRUE.equals(isCellEmptyAndValidateRegex(new CellReference(ExcelConstants.BID_QTY_DETAILS_COLUMN_VALUE_TO_COLUMN_NUMBER.get(ResponseRfpExcelHeaders.DUE_DATE.getValue())), bidQtySheet, ResponseRfpExcelHeaders.DUE_DATE.getValue(), Optional.of(ExcelConstants.DUE_DATE_REGEX), ExcelConstants.DUE_DATE_FORMAT))) {
                return df.formatCellValue(getCellValueFromCellReference(new CellReference(ExcelConstants.BID_QTY_DETAILS_COLUMN_VALUE_TO_COLUMN_NUMBER.get(ResponseRfpExcelHeaders.DUE_DATE.getValue())), bidQtySheet));
            }
        }
        return null;

    }

    //Method to validate GpMills same for BidQtySheet and Logistic sheet
    private static void validateGpMillsForSpecificSupplier(Map<String, Set<String>> mapOfSupplierMIllsToGpMills, Map<String, BidQtyDetail> millNameToBidQtyDetails,Integer errorCount) {
        LOGGER.info("In ExcelParsingUtils :: validateGpMillsForSpecificSupplier");


        // Check if the keys of the two maps are the same (i.e., SupplierMillsName keys)
        if (mapOfSupplierMIllsToGpMills.keySet().equals(millNameToBidQtyDetails.keySet())) {
            // Both maps have the same keys (SupplierMillsName keys)
            for (String supplierMillName : mapOfSupplierMIllsToGpMills.keySet()) {
                // Get the Set of GpMills for the current SupplierMillsName key
                Set<String> setOfGpMillsOfLogisticDetaials = mapOfSupplierMIllsToGpMills.get(supplierMillName);
                Set<String> setOfGpMillsOfBidQtySheet = new HashSet<>();


                // Get the BidQtyDetail for the current SupplierMillsName key
                BidQtyDetail bidQtyDetails = millNameToBidQtyDetails.get(supplierMillName);

                if (bidQtyDetails.getBid_type() != null && Constants.BidType.MILL_SPECIFIC.getValue().equals(bidQtyDetails.getBid_type())) {

                    // Your code here
                    for (MillSpecBid millSpecBid : bidQtyDetails.getMill_spec_bid()) {
                        if (millSpecBid.getBid_vol() != null && millSpecBid.getBid_vol() != 0)
                            setOfGpMillsOfBidQtySheet.add(millSpecBid.getMill());
                    }
                    //Comparing mills of bid qty details and logistic pricing
                    Set<String> millsDifferenceBidQtyAndLogisticSheet = Sets.difference(setOfGpMillsOfBidQtySheet, setOfGpMillsOfLogisticDetaials);

                    StringBuilder errorMessage = new StringBuilder();
                    for (String mills : millsDifferenceBidQtyAndLogisticSheet) {
                        errorMessage.append(mills).append(ExcelConstants.COMMA_WITH_SPACE);
                    }


                    if (!millsDifferenceBidQtyAndLogisticSheet.isEmpty() && errorCount == errorMessageDetailsList.size()) {
                        errorMessageDetailsList.add(new ErrorMessageDetails(ExcelConstants.ResponseRfpExcelSheetNames.LOGISTIC_PRICING.getSheetName(), null, null, null, ResponseRfpExcelHeaders.SUPPLIER_MILL.getValue(), (ErrorMessages.LOGISTIC_MISSING_GP_MILLS + errorMessage + "For SupplierMill :" + supplierMillName).toString()));
                    }

                    //Comparing mills of logistic pricing and bid qty details
                    Set<String> millsDifferenceLogisticToBidQtySheet = Sets.difference(setOfGpMillsOfLogisticDetaials, setOfGpMillsOfBidQtySheet);


                    StringBuilder millList = new StringBuilder();
                    StringBuilder errorMessages = new StringBuilder();
                    for (String mills : millsDifferenceLogisticToBidQtySheet) {
                        millList.append(mills).append(ExcelConstants.COMMA_WITH_SPACE);
                    }


                    if (!millsDifferenceLogisticToBidQtySheet.isEmpty() && errorCount == errorMessageDetailsList.size()) {
                        errorMessageDetailsList.add(new ErrorMessageDetails(ExcelConstants.ResponseRfpExcelSheetNames.LOGISTIC_PRICING.getSheetName(), null, null, null, ResponseRfpExcelHeaders.SUPPLIER_MILL.getValue(), MessageFormat.format(ErrorMessages.LOGISTIC_NOT_REQUIRED_GP_MILLS + errorMessages, millList, supplierMillName).toString()));
                    }
                }

                // if bidType is LumpSum then check for Gp millName in Logisitc sheet if it is Null or constst set to null or thow error
                if (bidQtyDetails.getBid_type() != null && Constants.BidType.LUMP_SUM.getValue().equals(bidQtyDetails.getBid_type())){
                    for (String gpMillNameFromLogisticSheet :setOfGpMillsOfLogisticDetaials) {
                        // thow error if the Enter GpMill
                        if (gpMillNameFromLogisticSheet != null && !gpMillNameFromLogisticSheet.equals(ExcelConstants.GP_MILL_LUMP_SUM) && errorCount == errorMessageDetailsList.size()){
                            errorMessageDetailsList.add(new ErrorMessageDetails(ExcelConstants.ResponseRfpExcelSheetNames.LOGISTIC_PRICING.getSheetName(), null, null, gpMillNameFromLogisticSheet, null, ErrorMessages.Messages.GP_MILL_NOT_REQUIRED_IF_LUMP_SUM.getMessage()));
                        }
                    }
                }
            }
        }

    }

    //Method to fetch Rfp Number From Bid Details Sheet
    public static String getRfpNumberFromBidQtyDetailsSheet(Sheet bidQtySheet) {
        if (bidQtySheet != null) {
            // fetch value
            CellReference cellReference = new CellReference(ExcelConstants.BID_QTY_DETAILS_COLUMN_HEADER_TO_COLUMN_NUMBER.get(ResponseRfpExcelHeaders.RFP_NUMBER.getValue()));
            //checking for Valid RFPNumber Header
            if (Boolean.TRUE.equals(validateHeadersForBidQtyDetails(bidQtySheet.getRow(cellReference.getRow()), cellReference.getCol(), ResponseRfpExcelHeaders.RFP_NUMBER.getValue()))) {
                //Mandatory check for Value
                if (Boolean.TRUE.equals(isCellEmptyAndValidateRegex(new CellReference(ExcelConstants.BID_QTY_DETAILS_COLUMN_VALUE_TO_COLUMN_NUMBER.get(ResponseRfpExcelHeaders.RFP_NUMBER.getValue())), bidQtySheet, ResponseRfpExcelHeaders.RFP_NUMBER.getValue(), Optional.of(ExcelConstants.ALPHA_NUMERIC_WITH_HYPHEN), ExcelConstants.ALPHA_NUMERIC))) {
                    return df.formatCellValue(getCellValueFromCellReference(new CellReference(ExcelConstants.BID_QTY_DETAILS_COLUMN_VALUE_TO_COLUMN_NUMBER.get(ResponseRfpExcelHeaders.RFP_NUMBER.getValue())), bidQtySheet));
                }
            }
        }
        return null;
    }

    //Method to get FiberType from bidQtySheet
    private static String getFiberType(Sheet bidQtySheet) {
        CellReference cellReference = new CellReference(ExcelConstants.BID_QTY_DETAILS_COLUMN_HEADER_TO_COLUMN_NUMBER.get(ResponseRfpExcelHeaders.FIBER_TYPE.getValue()));
        //Validating Header
        if (Boolean.TRUE.equals(validateHeadersForBidQtyDetails(bidQtySheet.getRow(cellReference.getRow()), cellReference.getCol(), ResponseRfpExcelHeaders.FIBER_TYPE.getValue()))) {
            //Validate value (Not Null and Regex)
            if (Boolean.TRUE.equals(isCellEmptyAndValidateRegex(new CellReference(ExcelConstants.BID_QTY_DETAILS_COLUMN_VALUE_TO_COLUMN_NUMBER.get(ResponseRfpExcelHeaders.FIBER_TYPE.getValue())), bidQtySheet, ResponseRfpExcelHeaders.FIBER_TYPE.getValue(), Optional.of(ExcelConstants.ALPHABETS_REGEX), ExcelConstants.ALPHABETS))) {
                return df.formatCellValue(getCellValueFromCellReference(new CellReference(ExcelConstants.BID_QTY_DETAILS_COLUMN_VALUE_TO_COLUMN_NUMBER.get(ResponseRfpExcelHeaders.FIBER_TYPE.getValue())), bidQtySheet));
            }
        }
        return null;
    }


    //Method to check if all the required Response Rfp Sheets are present are there not
    private static boolean checkIfResponseRfpSheetsPresent(Workbook workbook) {
        Sheet logisticSheet = workbook.getSheet(ExcelConstants.ResponseRfpExcelSheetNames.LOGISTIC_PRICING.getSheetName());
        Sheet bidQtySheet = workbook.getSheet(ExcelConstants.ResponseRfpExcelSheetNames.BID_DETAILS.getSheetName());
        Sheet commercialPricingSheet = workbook.getSheet(ExcelConstants.ResponseRfpExcelSheetNames.COMMERCIAL_PRICING.getSheetName());
        boolean sheetMissing = false;
        String error;
        if (logisticSheet == null) {
            error = MessageFormat.format(ErrorMessages.SHEET_NOT_FOUND_ERROR, ExcelConstants.ResponseRfpExcelSheetNames.LOGISTIC_PRICING.getSheetName());
            errorMessageDetailsList.add(new ErrorMessageDetails(ExcelConstants.ResponseRfpExcelSheetNames.LOGISTIC_PRICING.getSheetName(), null, null, null, null, error));
            sheetMissing = true;
        }
        if (bidQtySheet == null) {
            error = MessageFormat.format(ErrorMessages.SHEET_NOT_FOUND_ERROR, ExcelConstants.ResponseRfpExcelSheetNames.BID_DETAILS.getSheetName());
            errorMessageDetailsList.add(new ErrorMessageDetails(ExcelConstants.ResponseRfpExcelSheetNames.BID_DETAILS.getSheetName(), null, null, null, null, error));
            sheetMissing = true;
        }
        if (commercialPricingSheet == null) {
            error = MessageFormat.format(ErrorMessages.SHEET_NOT_FOUND_ERROR, ExcelConstants.ResponseRfpExcelSheetNames.COMMERCIAL_PRICING.getSheetName());
            errorMessageDetailsList.add(new ErrorMessageDetails(ExcelConstants.ResponseRfpExcelSheetNames.COMMERCIAL_PRICING.getSheetName(), null, null, null, null, error));
            sheetMissing = true;
        }
        return !sheetMissing;
    }

    //Method to set Bid Qty Details in Rfp json Template
    private static void setBidQtyDetailsAndPricingDetailsInJson(Map<String, BidQtyDetail> millNameToBidQtyDetails, List<SupplierMills> supplierMillsList,  Map<String,PricingDetail> supplierMillToPricingDetail, Integer errorCount) throws CloneNotSupportedException {
        LOGGER.info("In ExcelParsingUtils :: setBidQtyDetailsAndPricingDetailsInJson");

        Set<String> logisticSheetSupplierMillsSet = new HashSet<>();
        Set<String> bidQtySheetSupplierMillsSet = millNameToBidQtyDetails.keySet();
        String supplierMillName;
        for(SupplierMills supplierMills : supplierMillsList){
            if (supplierMillToPricingDetail!=null && supplierMillToPricingDetail.get(supplierMills.getSupplier_mill()) != null) {
                PricingDetail pricingDetail = (PricingDetail) supplierMillToPricingDetail.get(supplierMills.getSupplier_mill()).clone();
                if (pricingDetail.getPeriod_detail()!=null && period!=null) {
                    List<PeriodDetail> periodDetailList = pricingDetail.getPeriod_detail();
                    for (PeriodDetail periodDetail : periodDetailList) {
                        periodDetail.setPeriod(period);
                    }
                    pricingDetail.setPeriod_detail(periodDetailList);
                }
                supplierMills.setPricing_detail(pricingDetail);
            }
            supplierMillName = supplierMills.getSupplier_mill();
            logisticSheetSupplierMillsSet.add(supplierMillName);
            if (millNameToBidQtyDetails.containsKey(supplierMillName)) {
                supplierMills.setBid_qty_detail(millNameToBidQtyDetails.get(supplierMillName));
            } else {
                if (errorCount == errorMessageDetailsList.size()) {
                    //Bid Qty Details are Mandatory
                    errorMessageDetailsList.add(new ErrorMessageDetails(ExcelConstants.ResponseRfpExcelSheetNames.BID_DETAILS.getSheetName(), null, null, null, null, MessageFormat.format(ErrorMessages.Messages.BID_QTY_DETAILS_MISSING_ERROR.getMessage(), supplierMillName)));
                }
            }
        }

        //Comparing Supplier Mills of bid qty details and logistic pricing
        Set<String> millsDifferenceBidQtyAndLogisticSheet = Sets.difference(bidQtySheetSupplierMillsSet, logisticSheetSupplierMillsSet);


        StringBuilder errorMessage = new StringBuilder();
        int numberOfMills = millsDifferenceBidQtyAndLogisticSheet.size();
        int millCount = 0; // Counter to keep track of the current mill

        for (String mills : millsDifferenceBidQtyAndLogisticSheet) {
            millCount++;

            // Append the mill name
            errorMessage.append(mills);

            // If there are multiple mills, and it's not the last one, add a comma and space
            if (numberOfMills > 1 && millCount < numberOfMills) {
                errorMessage.append(ExcelConstants.COMMA_WITH_SPACE);
            }
        }

        if (errorCount == errorMessageDetailsList.size() && !millsDifferenceBidQtyAndLogisticSheet.isEmpty())
            errorMessageDetailsList.add(new ErrorMessageDetails(ExcelConstants.ResponseRfpExcelSheetNames.LOGISTIC_PRICING.getSheetName(), null, null, null, null, MessageFormat.format(ErrorMessages.Messages.LOGISTIC_SHEET_DETAILS_MISSING_ERROR.getMessage(), errorMessage)));

    }
    //Method to Populate Supplier Mills Data with Freight Details From Submitted Response Rfp Excel Sheet
    private static List<SupplierMills> populateSupplierMillsAndFreightDetails(Sheet logisticSheet, Set<String> portOfEntries, String fiberType, Set<String> logisticPricingMills, Map<String, Set<String>> mapOfSupplierMillsToGpMills, Map<String, List<Ports>> supplierMillToPorts) {
        LOGGER.info("In ExcelParsingUtils :: populateSupplierMillsAndFreightDetails");

        int errorsBeforeVerifyingHeaders = errorMessageDetailsList.size();


        //Method to validate Headers And Filed Names of Logistic Sheet for new Template
        validateHeadersForLogisticSheet(logisticSheet);

        //If any Headers are not found , then not parsing the sheet
        if (errorMessageDetailsList.size() != errorsBeforeVerifyingHeaders) {
            return Collections.emptyList();
        }

        String supplierMillName;
        String gpMillName;
        String portEntry;
        String comments;

        //Map of supplier MillName to Supplier Mills Data
        Map<String, SupplierMills> supplierMillsToSupplierMillsMetadataMap = new HashMap<>();
        //Map of supplier mill , port of entry, Gp Mill Port Entry Details
        Map<String, Map<String, Map<String, PortEntryDetails>>> supplierMillPortEntryAndDetailsMap = new HashMap<>();
        //Map of supplier mill , Port of entry, Gp Mill ,Inland Freight Details
        Map<String, Map<String, Map<String, InlandFreight>>> supplierMillPortEntryInlandDetailsMap = new HashMap<>();

        //Map of Supplier Mill,  port of entry and port entry details
        Map<String, Map<String, PortEntryDetails>> portEntryAndDetailsMap = new HashMap<>();

        //Map of port entry and port entry Headers
        Map<String, Map<String, PortEntryHeaderFields>> portEntryAndHeaderFieldsMap = new HashMap<>();

        //Map of UsPortEntry to Incoterms
        Map<String, Map<String, Incoterms>> usPortEntryToIncotermMap = new HashMap<>();

        Map<String, Map<String,List<String>>> supplierMillAndPortEntryToIncotermListMap = new HashMap<>();

        Map<String, String> supplierMilltoPortEntryMap = new HashMap<>();

        //start logistic vertical
        int col = ExcelConstants.LOGISTIC_PRICING_SHEET_STARTING_COLUMN;
        Row supplierBidVolRow = logisticSheet.getRow(ExcelConstants.SUPPLIER_BID_VOL_ROW);

        // Fetching comments
       comments = df.formatCellValue(getCellValueFromCellReference(new CellReference(ExcelConstants.LOGISTIC_PRICING_COMMENTS_CELL), logisticSheet));


        while (!isLogisticColumnEmpty(logisticSheet, col)) {

            int errorsBeforeLogisticValidations = errorMessageDetailsList.size();

            //Validating Second Port of entry ( should be different from first Port entry )
            if(Objects.equals(df.formatCellValue(logisticSheet.getRow(logisticPricingFirstPortEntryFieldNameToRowMap.get(ExcelConstants.LogisticPricingFields.US_PORT_OF_ENTRY.getFieldName())).getCell(col)), df.formatCellValue(logisticSheet.getRow(logisticPricingSecondPortEntryFieldNameToRowMap.get(ExcelConstants.LogisticPricingFields.US_PORT_OF_ENTRY.getFieldName())).getCell(col)))){
                String errorMessage = ErrorMessages.Messages.SECOND_PORT_SAME_ERROR.getMessage();
                errorMessageDetailsList.add(new ErrorMessageDetails(logisticSheet.getSheetName(), new CellReference(logisticSheet.getRow(logisticPricingSecondPortEntryFieldNameToRowMap.get(ExcelConstants.LogisticPricingFields.US_PORT_OF_ENTRY.getFieldName())).getCell(col)).formatAsString(false), ExcelConstants.OTHER_THAN + Constants.SPACE + df.formatCellValue(logisticSheet.getRow(logisticPricingFirstPortEntryFieldNameToRowMap.get(ExcelConstants.LogisticPricingFields.US_PORT_OF_ENTRY.getFieldName())).getCell(col)) , df.formatCellValue(logisticSheet.getRow(logisticPricingFirstPortEntryFieldNameToRowMap.get(ExcelConstants.LogisticPricingFields.US_PORT_OF_ENTRY.getFieldName())).getCell(col)), ExcelConstants.LogisticPricingFields.US_PORT_OF_ENTRY.getFieldName(), errorMessage));
            }



            supplierMillName = df.formatCellValue(logisticSheet.getRow(logisticPricingFieldNameToRowMap.get(ExcelConstants.LogisticPricingFields.SUPPLIER_MILL.getFieldName())).getCell(col));
            gpMillName = df.formatCellValue(logisticSheet.getRow(logisticPricingFieldNameToRowMap.get(ExcelConstants.LogisticPricingFields.GP_MILL.getFieldName())).getCell(col));

            mandatoryFieldsValidationLogisticSheet(col, logisticSheet);
            dataFormatValidationLogisticSheet(col, logisticSheet);
            conditionValidationLogisticSheet(col, logisticSheet, gpMillName);

            if (errorMessageDetailsList.size() > errorsBeforeLogisticValidations) {
                col++;
                continue;
            }



           if(gpMillName == null || gpMillName.trim().isEmpty()){
               gpMillName = ExcelConstants.GP_MILL_LUMP_SUM;
           }else{
               supplierMillToGpMillToSupplierBidVolume.computeIfAbsent(supplierMillName, k-> new HashMap<>());
           }
            mapOfSupplierMillsToGpMills.computeIfAbsent(supplierMillName, k -> new HashSet<>()).add(gpMillName);

            //Filling map
            if(Objects.equals(gpMillName, ExcelConstants.GP_MILL_LUMP_SUM)){
                supplierMillToSupplierBidVolume.put(supplierMillName, Utils.getLongDoller(df.formatCellValue(logisticSheet.getRow(logisticPricingFieldNameToRowMap.get(ExcelConstants.LogisticPricingFields.SUPPLIER_BID_VOLUME.getFieldName())).getCell(col))));
            }else {
                supplierMillToGpMillToSupplierBidVolume.get(supplierMillName).put(gpMillName, Utils.getLongDoller(df.formatCellValue(logisticSheet.getRow(logisticPricingFieldNameToRowMap.get(ExcelConstants.LogisticPricingFields.SUPPLIER_BID_VOLUME.getFieldName())).getCell(col))));
            }

            if (!supplierMillsToSupplierMillsMetadataMap.containsKey(supplierMillName)) {
                SupplierMills supplierMills = new SupplierMills();
                supplierMills.setSupplier_mill(supplierMillName);
                supplierMills.setOrigin_port(df.formatCellValue(logisticSheet.getRow(logisticPricingFieldNameToRowMap.get(ExcelConstants.LogisticPricingFields.ORIGIN_PORT.getFieldName())).getCell(col)));
                supplierMills.setOrigin_cntry(df.formatCellValue(logisticSheet.getRow(logisticPricingFieldNameToRowMap.get(ExcelConstants.LogisticPricingFields.ORIGIN_COUNTRY.getFieldName())).getCell(col)));
                supplierMills.setEnvironmental_certification(df.formatCellValue(logisticSheet.getRow(logisticPricingFieldNameToRowMap.get(ExcelConstants.LogisticPricingFields.ENVIRONMENTAL_CERTIFICATION.getFieldName())).getCell(col)));
                supplierMills.setBale_packaging(df.formatCellValue(logisticSheet.getRow(logisticPricingFieldNameToRowMap.get(ExcelConstants.LogisticPricingFields.BALE_PACKAGING.getFieldName())).getCell(col)));
                supplierMills.setBale_type(df.formatCellValue(logisticSheet.getRow(logisticPricingFieldNameToRowMap.get(ExcelConstants.LogisticPricingFields.BALE_TYPE.getFieldName())).getCell(col)));
                supplierMills.setFiber_type(fiberType);
                supplierMills.setIs_supplier_mill_domestic(Objects.equals((df.formatCellValue(logisticSheet.getRow(logisticPricingFirstPortEntryFieldNameToRowMap.get(ExcelConstants.LogisticPricingFields.US_PORT_OF_ENTRY.getFieldName())).getCell(col))), ExcelConstants.OriginPort.DOMESTIC.getValue()));
                supplierMillsToSupplierMillsMetadataMap.put(supplierMillName, supplierMills);
            } else {
//                If SupplierMillName is already there in Map , Data Should Match else throw error
                validateRepeatedSupplierMetadataForNewTemplate(logisticSheet, col, supplierMillName, supplierMillsToSupplierMillsMetadataMap, logisticPricingFieldNameToRowMap);
            }

            supplierMillPortEntryAndDetailsMap.computeIfAbsent(supplierMillName, k -> new HashMap<>());
            supplierMillPortEntryInlandDetailsMap.computeIfAbsent(supplierMillName, k -> new HashMap<>());
            portEntryAndHeaderFieldsMap.computeIfAbsent(supplierMillName, k -> new HashMap<>());
            supplierMillToPorts.computeIfAbsent(supplierMillName, k->new ArrayList<>());
            portEntryAndDetailsMap.computeIfAbsent(supplierMillName, k-> new HashMap<>());

            portEntry = df.formatCellValue(logisticSheet.getRow(logisticPricingFirstPortEntryFieldNameToRowMap.get(ExcelConstants.LogisticPricingFields.US_PORT_OF_ENTRY.getFieldName())).getCell(col));

            addUsPortEntryToPortRebateList(supplierMillName, portEntry,supplierMillToPorts);


            // This function will set logistic pricing value based on specific port entry and provided map for respective port entries
            populateAndSetLogisticSheetDetails(logisticSheet, col, portEntry, gpMillName, supplierMillName, portEntryAndDetailsMap, portEntryAndHeaderFieldsMap, usPortEntryToIncotermMap, supplierMillPortEntryAndDetailsMap, supplierMillPortEntryInlandDetailsMap, supplierMillAndPortEntryToIncotermListMap, supplierMilltoPortEntryMap, logisticPricingFieldNameToRowMap, logisticPricingFirstPortEntryFieldNameToRowMap, logisticPricingFirstPortEntryFieldNameToRowMismatchMap, logisticPricingFirstPortEntryIncoterm1FieldNameToRowMap, logisticPricingFirstPortEntryIncoterm2FieldNameToRowMap);

            //Second Port Of Entry
            portEntry = df.formatCellValue(logisticSheet.getRow(logisticPricingSecondPortEntryFieldNameToRowMap.get(ExcelConstants.LogisticPricingFields.US_PORT_OF_ENTRY.getFieldName())).getCell(col));

            if (portEntry != null && !portEntry.trim().isEmpty()) {
                addUsPortEntryToPortRebateList(supplierMillName, portEntry,supplierMillToPorts);
                populateAndSetLogisticSheetDetails(logisticSheet, col, portEntry, gpMillName, supplierMillName, portEntryAndDetailsMap, portEntryAndHeaderFieldsMap, usPortEntryToIncotermMap, supplierMillPortEntryAndDetailsMap, supplierMillPortEntryInlandDetailsMap, supplierMillAndPortEntryToIncotermListMap, supplierMilltoPortEntryMap, logisticPricingFieldNameToRowMap, logisticPricingSecondPortEntryFieldNameToRowMap, logisticPricingSecondPortEntryFieldNameToRowMismatchMap, logisticPricingSecondPortEntryIncoterm1FieldNameToRowMap, logisticPricingSecondPortEntryIncoterm2FieldNameToRowMap);
            }

            //Iterating to next column
            col++;
        }

        //end logistic vertical


//
//        int i = ExcelConstants.LOGISTIC_PRICING_SHEET_STARTING_ROW;
//        Row row = logisticSheet.getRow(i);
//        //if row is empty skip
//            while (!isRowEmpty(row)) {
//
//                int errorCount = errorMessageDetailsList.size();
//
//                //Validating Mandatory Fields
//                validateMandatoryFieldsLogisticSheet(row, logisticHeadersMap, logisticSheet.getSheetName());
//                //Validating Data Types
//                validateDataTypesInLogisticSheet(row, logisticHeadersMap, logisticSheet.getSheetName());
//                //Validating Numeric Values
//                validateDataFormatsLogisticSheet(row, logisticHeadersMap);
//                //Logical Validation
//                validateConditionsForLogisticPricing(row, logisticHeadersMap, logisticHeadersMismatchMap, logisticSheet.getSheetName());
//                //If there are any errors, No need to set Json Skip the row
//                if (errorMessageDetailsList.size() > errorCount) {
//                    row = logisticSheet.getRow(++i);
//                    continue;
//                }
//
//
//            //read Supplier MillName, Gp MillName and Port of entry
//            supplierMillName = df.formatCellValue(row.getCell(logisticHeadersMap.get(ExcelConstants.ResponseRfpExcelHeaders.SUPPLIER_MILL.getValue())));
//            gpMillName = df.formatCellValue(row.getCell(logisticHeadersMap.get(ExcelConstants.ResponseRfpExcelHeaders.MILL.getValue())));
//            portEntry = df.formatCellValue(row.getCell(logisticHeadersMap.get(ExcelConstants.ResponseRfpExcelHeaders.US_PORT_OF_ENTRY.getValue())));
//            portOfEntries.add(portEntry);
//            mapOfSupplierMillsToGpMills.computeIfAbsent(supplierMillName, k -> new HashSet<>()).add(gpMillName);
//            if (!supplierMillsToSupplierMillsMetadataMap.containsKey(supplierMillName)) {
//                SupplierMills supplierMills = new SupplierMills();
//                supplierMills.setSupplier_mill(df.formatCellValue(row.getCell(logisticHeadersMap.get(ExcelConstants.ResponseRfpExcelHeaders.SUPPLIER_MILL.getValue()))));
//                supplierMills.setOrigin_port(df.formatCellValue(row.getCell(logisticHeadersMap.get(ExcelConstants.ResponseRfpExcelHeaders.ORIGIN_PORT.getValue()))));
//                supplierMills.setOrigin_cntry(df.formatCellValue(row.getCell(logisticHeadersMap.get(ExcelConstants.ResponseRfpExcelHeaders.ORIGIN_COUNTRY.getValue()))));
//                supplierMills.setEnvironmental_certification(df.formatCellValue(row.getCell(logisticHeadersMap.get(ExcelConstants.ResponseRfpExcelHeaders.ENVIRONMENTAL_CERTIFICATION.getValue()))));
//                supplierMills.setBale_packaging(df.formatCellValue(row.getCell(logisticHeadersMap.get(ExcelConstants.ResponseRfpExcelHeaders.BALE_PACKAGING.getValue()))));
//                supplierMills.setBale_type(df.formatCellValue(row.getCell(logisticHeadersMap.get(ExcelConstants.ResponseRfpExcelHeaders.BALE_TYPE.getValue()))));
//                supplierMills.setFiber_type(fiberType);
//                if(Objects.equals(df.formatCellValue(row.getCell(logisticHeadersMap.get(ResponseRfpExcelHeaders.ORIGIN_PORT.getValue()))), ExcelConstants.OriginPort.DOMESTIC.getValue())){
//                    supplierMills.setIs_supplier_mill_domestic(true);
//                }else {
//                    supplierMills.setIs_supplier_mill_domestic(false);
//                }
//                supplierMillsToSupplierMillsMetadataMap.put(supplierMillName, supplierMills);
//            } else {
//                //If SupplierMillName is already there in Map , Data Should Match else throw error
//                validateRepeatedSupplierMetadata(supplierMillName, supplierMillsToSupplierMillsMetadataMap, row, logisticHeadersMap, logisticSheet.getSheetName());
//            }
//
//            supplierMillPortEntryAndDetailsMap.computeIfAbsent(supplierMillName, k -> new HashMap<>());
//            supplierMillPortEntryInlandDetailsMap.computeIfAbsent(supplierMillName, k -> new HashMap<>());
//            portEntryAndHeaderFieldsMap.computeIfAbsent(supplierMillName, k -> new HashMap<>());
//
//            supplierMillPortEntryAndDetailsMap.get(supplierMillName).computeIfAbsent(portEntry, k -> new HashMap<>());
//            supplierMillPortEntryInlandDetailsMap.get(supplierMillName).computeIfAbsent(portEntry, k -> new HashMap<>());
//
//
//            if (!portEntryAndDetailsMap.containsKey(portEntry)) {
//                //Populate Port Entry Details and set in the Map
//                PortEntryDetails portEntryDetails = populatePortEntryDetails(row, logisticHeadersMap, portEntry, gpMillName);
//                portEntryAndDetailsMap.put(portEntry, portEntryDetails);
//            } else {
//                //For Same Port of entry Details Should be Same else add in errors list
//                validatePortEntryToPortEntryMapDetails(portEntry, row, logisticHeadersMap, logisticSheet.getSheetName(), portEntryAndDetailsMap);
//            }
//
//            if(!portEntryAndHeaderFieldsMap.get(supplierMillName).containsKey(portEntry)){
//                PortEntryHeaderFields portEntryHeaderFields = populatePortEntryAndHeaderDetails(row, logisticHeadersMap, portEntry, gpMillName);
//                portEntryAndHeaderFieldsMap.get(supplierMillName).put(portEntry, portEntryHeaderFields);
//            } else {
//                validatePortEntryToPortEntryHeaderFields(portEntry, row, logisticHeadersMap, logisticSheet.getSheetName(), portEntryAndHeaderFieldsMap,supplierMillName);
//            }
//
//            if (!supplierMillPortEntryAndDetailsMap.get(supplierMillName).get(portEntry).containsKey(gpMillName)) {
//                //Populate Port Entry Details and set in the Map
//                PortEntryDetails portEntryDetails = populatePortEntryDetails(row, logisticHeadersMap, portEntry, gpMillName);
//                supplierMillPortEntryAndDetailsMap.get(supplierMillName).get(portEntry).put(gpMillName, portEntryDetails);
//            }
//            //Populate and Set Incoterms
//            Incoterms incoterms = populateIncoterms(row, logisticHeadersMap, usPortEntryToIncotermMap, portEntry);
//            //Adding Incoterms to list (For particular SupplierMill,portEntry and Gp mill combination)
//            if (supplierMillPortEntryAndDetailsMap.get(supplierMillName) != null && supplierMillPortEntryAndDetailsMap.get(supplierMillName).get(portEntry) != null) {
//                supplierMillPortEntryAndDetailsMap.get(supplierMillName).get(portEntry).get(gpMillName).getIncoterms().add(incoterms);
//            }
//            //Populate and Set Freight Details
//            if (!supplierMillPortEntryInlandDetailsMap.get(supplierMillName).get(portEntry).containsKey(gpMillName)) {
//                InlandFreight inlandFreight = populateInlandFreightDetails(row, logisticHeadersMap, gpMillName, portEntry);
//                supplierMillPortEntryInlandDetailsMap.get(supplierMillName).get(portEntry).put(gpMillName, inlandFreight);
//            } else {
//                //If SupplierMillName is already there in Map , Data Should Match else throw error
//                validateRepeatedInlandFreightMetadata(supplierMillName, portEntry, supplierMillPortEntryInlandDetailsMap, row, logisticHeadersMap, logisticSheet.getSheetName(), gpMillName);
//            }
//            //Move to next row
//            row = logisticSheet.getRow(++i);
//        }

        //Prepare supplierMill TO FreightDetailsMap from Above Prepared Maps
        return prepareSupplierMillsListWithFreightDetails(supplierMillPortEntryAndDetailsMap, supplierMillPortEntryInlandDetailsMap, supplierMillsToSupplierMillsMetadataMap, comments);


    }


    private static void populateAndSetLogisticSheetDetails(Sheet logisticSheet, int col, String portEntry, String gpMillName, String supplierMillName, Map<String, Map<String, PortEntryDetails>> portEntryAndDetailsMap, Map<String, Map<String, PortEntryHeaderFields>> portEntryAndHeaderFieldsMap, Map<String, Map<String, Incoterms>> usPortEntryToIncotermMap, Map<String, Map<String, Map<String, PortEntryDetails>>> supplierMillPortEntryAndDetailsMap, Map<String, Map<String, Map<String, InlandFreight>>> supplierMillPortEntryInlandDetailsMap, Map<String, Map<String,List<String>>> supplierMillAndPortEntryToIncotermListMap , Map<String, String> supplierMilltoPortEntryMap, Map<String, Integer> rowMap, Map<String, Integer> rowPortEntryMap, Map<String, Integer> rowPortEntryMismatchMap, Map<String, Integer> incoterms1Map, Map<String, Integer> incoterms2Map) {
        supplierMillPortEntryInlandDetailsMap.get(supplierMillName).computeIfAbsent(portEntry, k -> new HashMap<>());
        supplierMillPortEntryAndDetailsMap.get(supplierMillName).computeIfAbsent(portEntry, k -> new HashMap<>());
        supplierMillAndPortEntryToIncotermListMap.computeIfAbsent(supplierMillName, k -> new HashMap<>());

        validateUsPortEntry(logisticSheet, supplierMillName, col, supplierMilltoPortEntryMap, portEntry, rowPortEntryMap);
        validateRepeatativeFieldsLogisticPricingForNewTemplate(logisticSheet, col, portEntry, gpMillName, supplierMillName, portEntryAndDetailsMap, portEntryAndHeaderFieldsMap, rowMap, rowPortEntryMap);
        populateAndSetPortEntryDetailsForNewTemplate(logisticSheet, col, supplierMillName, usPortEntryToIncotermMap, portEntry, gpMillName, supplierMillPortEntryAndDetailsMap, rowPortEntryMap);
        populateAndSetIncotermsForNewTemplate(logisticSheet, col, supplierMillName, usPortEntryToIncotermMap, portEntry, gpMillName, supplierMillPortEntryAndDetailsMap, supplierMillAndPortEntryToIncotermListMap ,incoterms1Map, incoterms2Map);
        populateAndSetFreightDetailsForNewTemplate(logisticSheet, col, supplierMillName, portEntry, supplierMillPortEntryInlandDetailsMap, gpMillName, rowMap, rowPortEntryMap, rowPortEntryMismatchMap);
    }


    private static void validatePortEntryToPortEntryMapDetails(String portEntry, Row row, Map<String, Integer> columnsMap, String sheetName, Map<String, PortEntryDetails> portEntryAndDetailsMap) {
        PortEntryDetails portEntryDetails = portEntryAndDetailsMap.get(portEntry);

        if (!Objects.equals(portEntryDetails.getPort_free_time_in_days(), Long.valueOf(Utils.getTrimmedNumber(df.formatCellValue(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.PORT_TIME_IN_DAYS.getValue()))))))) {
            String errorMessage = MessageFormat.format(ErrorMessages.Messages.REPEATED_PORT_ENTRY_METADATA_ERROR.getMessage(), ExcelConstants.ResponseRfpExcelHeaders.PORT_TIME_IN_DAYS.getValue());
            errorMessageDetailsList.add(new ErrorMessageDetails(sheetName, new CellReference(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.PORT_TIME_IN_DAYS.getValue()))).formatAsString(false), portEntryDetails.getPort_free_time_in_days().toString(), Utils.getTrimmedNumber(df.formatCellValue(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.PORT_TIME_IN_DAYS.getValue())))), ExcelConstants.ResponseRfpExcelHeaders.PORT_TIME_IN_DAYS.getValue(), errorMessage));
        }

        if (!Objects.equals(portEntryDetails.getTransit_leadtime_in_days_origin_port_port_entry(), Long.valueOf(Utils.getTrimmedNumber(df.formatCellValue(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.TRANSIT_LEAD_TIME_ORIGIN_PORT_TO_US_PORT.getValue()))))))) {
            String errorMessage = MessageFormat.format(ErrorMessages.Messages.REPEATED_PORT_ENTRY_METADATA_ERROR.getMessage(), ExcelConstants.ResponseRfpExcelHeaders.TRANSIT_LEAD_TIME_ORIGIN_PORT_TO_US_PORT.getValue());
            errorMessageDetailsList.add(new ErrorMessageDetails(sheetName, new CellReference(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.TRANSIT_LEAD_TIME_ORIGIN_PORT_TO_US_PORT.getValue()))).formatAsString(false), portEntryDetails.getTransit_leadtime_in_days_origin_port_port_entry().toString(), Utils.getTrimmedNumber(df.formatCellValue(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.TRANSIT_LEAD_TIME_ORIGIN_PORT_TO_US_PORT.getValue())))), ExcelConstants.ResponseRfpExcelHeaders.TRANSIT_LEAD_TIME_ORIGIN_PORT_TO_US_PORT.getValue(), errorMessage));
        }

        if (!Objects.equals(portEntryDetails.getSteamship_line(), df.formatCellValue(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.STEAMSHIP_LINE.getValue()))))) {
            String errorMessage = MessageFormat.format(ErrorMessages.Messages.REPEATED_PORT_ENTRY_METADATA_ERROR.getMessage(), ExcelConstants.ResponseRfpExcelHeaders.STEAMSHIP_LINE.getValue());
            errorMessageDetailsList.add(new ErrorMessageDetails(sheetName, new CellReference(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.STEAMSHIP_LINE.getValue()))).formatAsString(false), portEntryDetails.getSteamship_line(), df.formatCellValue(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.STEAMSHIP_LINE.getValue()))), ExcelConstants.ResponseRfpExcelHeaders.STEAMSHIP_LINE.getValue(), errorMessage));
        }

        if (!Objects.equals(portEntryDetails.getOcean_freight(), Utils.getFloatDoller(df.formatCellValue(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.OCEAN_FREIGHT.getValue())))))) {
            String errorMessage = MessageFormat.format(ErrorMessages.Messages.REPEATED_PORT_ENTRY_METADATA_ERROR.getMessage(), ExcelConstants.ResponseRfpExcelHeaders.OCEAN_FREIGHT.getValue());
            errorMessageDetailsList.add(new ErrorMessageDetails(sheetName, new CellReference(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.OCEAN_FREIGHT.getValue()))).formatAsString(false), portEntryDetails.getOcean_freight().toString(), Utils.getTrimmedNumber(df.formatCellValue(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.OCEAN_FREIGHT.getValue())))), ExcelConstants.ResponseRfpExcelHeaders.OCEAN_FREIGHT.getValue(), errorMessage));
        }

        if (!Objects.equals(portEntryDetails.getSafety_stock_nominated_in_days(), Long.valueOf(Utils.getTrimmedNumber(df.formatCellValue(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.NOMINATED_SAFETY_STOCK.getValue()))))))) {
            String errorMessage = MessageFormat.format(ErrorMessages.Messages.REPEATED_PORT_ENTRY_METADATA_ERROR.getMessage(), ExcelConstants.ResponseRfpExcelHeaders.NOMINATED_SAFETY_STOCK.getValue());
            errorMessageDetailsList.add(new ErrorMessageDetails(sheetName, new CellReference(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.NOMINATED_SAFETY_STOCK.getValue()))).formatAsString(false), portEntryDetails.getSafety_stock_nominated_in_days().toString(), Utils.getTrimmedNumber(df.formatCellValue(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.NOMINATED_SAFETY_STOCK.getValue())))), ExcelConstants.ResponseRfpExcelHeaders.NOMINATED_SAFETY_STOCK.getValue(), errorMessage));
        }


        if (!Objects.equals(portEntryDetails.getSafety_stock_location().getType(), df.formatCellValue(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.SAFETY_STOCK_LOCATION_TYPE.getValue()))).trim())) {
            String errorMessage = MessageFormat.format(ErrorMessages.Messages.REPEATED_PORT_ENTRY_METADATA_ERROR.getMessage(), ExcelConstants.ResponseRfpExcelHeaders.SAFETY_STOCK_LOCATION_TYPE.getValue());
            errorMessageDetailsList.add(new ErrorMessageDetails(sheetName, new CellReference(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.SAFETY_STOCK_LOCATION_TYPE.getValue()))).formatAsString(false), portEntryDetails.getSafety_stock_location().getType(), df.formatCellValue(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.SAFETY_STOCK_LOCATION_TYPE.getValue()))), ExcelConstants.ResponseRfpExcelHeaders.SAFETY_STOCK_LOCATION_TYPE.getValue(), errorMessage));
        }

        if (!Objects.equals(portEntryDetails.getSafety_stock_location().getName(), df.formatCellValue(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.SAFETY_STOCK_LOCATION_NAME.getValue()))))) {
            String errorMessage = MessageFormat.format(ErrorMessages.Messages.REPEATED_PORT_ENTRY_METADATA_ERROR.getMessage(), ExcelConstants.ResponseRfpExcelHeaders.SAFETY_STOCK_LOCATION_NAME.getValue());
            errorMessageDetailsList.add(new ErrorMessageDetails(sheetName, new CellReference(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.SAFETY_STOCK_LOCATION_NAME.getValue()))).formatAsString(false), portEntryDetails.getSafety_stock_location().getName(), df.formatCellValue(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.SAFETY_STOCK_LOCATION_NAME.getValue()))), ExcelConstants.ResponseRfpExcelHeaders.SAFETY_STOCK_LOCATION_NAME.getValue(), errorMessage));
        }

        if (!Objects.equals(portEntryDetails.getTransit_cost_from_port_entry_to_safety_stock_loc(), Utils.getFloatDoller(df.formatCellValue(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.TRANSIT_COST_FROM_US_PORT_TO_SAFETY_STOCK_LOC.getValue())))))) {
            String errorMessage = MessageFormat.format(ErrorMessages.Messages.REPEATED_PORT_ENTRY_METADATA_ERROR.getMessage(), ExcelConstants.ResponseRfpExcelHeaders.TRANSIT_COST_FROM_US_PORT_TO_SAFETY_STOCK_LOC.getValue());
            errorMessageDetailsList.add(new ErrorMessageDetails(sheetName, new CellReference(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.TRANSIT_COST_FROM_US_PORT_TO_SAFETY_STOCK_LOC.getValue()))).formatAsString(false), portEntryDetails.getTransit_cost_from_port_entry_to_safety_stock_loc().toString(), Utils.getTrimmedNumber(df.formatCellValue(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.TRANSIT_COST_FROM_US_PORT_TO_SAFETY_STOCK_LOC.getValue())))), ExcelConstants.ResponseRfpExcelHeaders.SAFETY_STOCK_LOCATION_NAME.getValue(), errorMessage));
        }

    }


    //Method to Validate Data Format In Logistic Sheet
    private static void validateDataFormatsLogisticSheet(Row row, Map<String, Integer> logisticHeadersMap) {
        checkIsEmptyStringAndDataFormat(row, ResponseRfpExcelHeaders.SUPPLIER_BID_VOLUME.getValue(), ExcelConstants.NUMBER_REGEX, ExcelConstants.ResponseRfpExcelSheetNames.LOGISTIC_PRICING.getSheetName(), ExcelConstants.NUMBER, logisticHeadersMap, Boolean.TRUE, Boolean.FALSE);
        checkIsEmptyStringAndDataFormat(row, ResponseRfpExcelHeaders.STEVEDORING_COST.getValue(), ExcelConstants.DOLLER_REGEX, ExcelConstants.ResponseRfpExcelSheetNames.LOGISTIC_PRICING.getSheetName(), ExcelConstants.CURRENCY_DOLLAR, logisticHeadersMap, Boolean.FALSE, Boolean.TRUE);
        checkIsEmptyStringAndDataFormat(row, ResponseRfpExcelHeaders.HANDLING_COST.getValue(), ExcelConstants.DOLLER_REGEX, ExcelConstants.ResponseRfpExcelSheetNames.LOGISTIC_PRICING.getSheetName(), ExcelConstants.CURRENCY_DOLLAR, logisticHeadersMap, Boolean.FALSE, Boolean.TRUE);
        checkIsEmptyStringAndDataFormat(row, ResponseRfpExcelHeaders.WHARFAGE_COST.getValue(), ExcelConstants.DOLLER_REGEX, ExcelConstants.ResponseRfpExcelSheetNames.LOGISTIC_PRICING.getSheetName(), ExcelConstants.CURRENCY_DOLLAR, logisticHeadersMap, Boolean.FALSE, Boolean.TRUE);
        checkIsEmptyStringAndDataFormat(row, ResponseRfpExcelHeaders.SECURITY_COST.getValue(), ExcelConstants.DOLLER_REGEX, ExcelConstants.ResponseRfpExcelSheetNames.LOGISTIC_PRICING.getSheetName(), ExcelConstants.CURRENCY_DOLLAR, logisticHeadersMap, Boolean.FALSE, Boolean.TRUE);
        checkIsEmptyStringAndDataFormat(row, ResponseRfpExcelHeaders.WAREHOUSING_FEE_MONTHLY.getValue(), ExcelConstants.DOLLER_REGEX, ExcelConstants.ResponseRfpExcelSheetNames.LOGISTIC_PRICING.getSheetName(), ExcelConstants.CURRENCY_DOLLAR, logisticHeadersMap, Boolean.FALSE, Boolean.TRUE);
        checkIsEmptyStringAndDataFormat(row, ResponseRfpExcelHeaders.IMPORT_CUSTOMS_TARIFF_FEE.getValue(), ExcelConstants.DOLLER_REGEX, ExcelConstants.ResponseRfpExcelSheetNames.LOGISTIC_PRICING.getSheetName(), ExcelConstants.CURRENCY_DOLLAR, logisticHeadersMap, Boolean.FALSE, Boolean.TRUE);
        checkIsEmptyStringAndDataFormat(row, ResponseRfpExcelHeaders.PORT_TIME_IN_DAYS.getValue(), ExcelConstants.NUMBER_REGEX, ExcelConstants.ResponseRfpExcelSheetNames.LOGISTIC_PRICING.getSheetName(), ExcelConstants.NUMBER, logisticHeadersMap, Boolean.TRUE, Boolean.TRUE);
        checkIsEmptyStringAndDataFormat(row, ResponseRfpExcelHeaders.OCEAN_FREIGHT.getValue(), ExcelConstants.DOLLER_REGEX, ExcelConstants.ResponseRfpExcelSheetNames.LOGISTIC_PRICING.getSheetName(), ExcelConstants.CURRENCY_DOLLAR, logisticHeadersMap, Boolean.FALSE, Boolean.TRUE);
        checkIsEmptyStringAndDataFormat(row, ResponseRfpExcelHeaders.NOMINATED_SAFETY_STOCK.getValue(), ExcelConstants.NUMBER_REGEX, ExcelConstants.ResponseRfpExcelSheetNames.LOGISTIC_PRICING.getSheetName(), ExcelConstants.NUMBER, logisticHeadersMap, Boolean.TRUE, Boolean.FALSE);
        checkIsEmptyStringAndDataFormat(row, ResponseRfpExcelHeaders.TRANSIT_COST.getValue(), ExcelConstants.DOLLER_REGEX, ExcelConstants.ResponseRfpExcelSheetNames.LOGISTIC_PRICING.getSheetName(), ExcelConstants.CURRENCY_DOLLAR, logisticHeadersMap, Boolean.FALSE, Boolean.FALSE);
        checkIsEmptyStringAndDataFormat(row, ResponseRfpExcelHeaders.TRANSIT_LEAD_TIME_IN_DAYS_US_PORT_TO_GP_MILL.getValue(), ExcelConstants.NUMBER_REGEX, ExcelConstants.ResponseRfpExcelSheetNames.LOGISTIC_PRICING.getSheetName(), ExcelConstants.NUMBER, logisticHeadersMap, Boolean.TRUE, Boolean.TRUE);
        checkIsEmptyStringAndDataFormat(row, ResponseRfpExcelHeaders.SUPPLIER_MILL.getValue(), ExcelConstants.ALPHABETS_REGEX, ExcelConstants.ResponseRfpExcelSheetNames.LOGISTIC_PRICING.getSheetName(), ExcelConstants.ALPHABETS, logisticHeadersMap, Boolean.TRUE, Boolean.FALSE);
        checkIsEmptyStringAndDataFormat(row, ResponseRfpExcelHeaders.STEAMSHIP_LINE.getValue(), ExcelConstants.ALPHA_NUMERIC_REGEX, ExcelConstants.ResponseRfpExcelSheetNames.LOGISTIC_PRICING.getSheetName(), ExcelConstants.ALPHA_NUMERIC, logisticHeadersMap, Boolean.TRUE, Boolean.TRUE);
        checkIsEmptyStringAndDataFormat(row, ResponseRfpExcelHeaders.ORIGIN_COUNTRY.getValue(), ExcelConstants.ALPHABETS_REGEX, ExcelConstants.ResponseRfpExcelSheetNames.LOGISTIC_PRICING.getSheetName(), ExcelConstants.ALPHABETS, logisticHeadersMap, Boolean.TRUE, Boolean.FALSE);

    }


    private static List<SupplierMills> prepareSupplierMillsListWithFreightDetails(Map<String, Map<String, Map<String, PortEntryDetails>>> supplierMillPortEntryAndDetailsMap, Map<String, Map<String, Map<String, InlandFreight>>> supplierMillPortEntryInlandDetailsMap, Map<String, SupplierMills> supplierMillsToSupplierMillsMetadataMap, String comments) {
        //List os Supplier Mills
        List<SupplierMills> supplierMillsList = new ArrayList<>();
        //Prepare Map of Supplier Mill to FreightDetails using the supplierMillPortEntryAndDetailsMap and supplierMillPortEntryInlandDetailsMap
        for (Map.Entry<String, Map<String, Map<String,PortEntryDetails>>> millPortAndDetails : supplierMillPortEntryAndDetailsMap.entrySet()) {
            FreightDetail freightDetail = new FreightDetail();
            freightDetail.setInland_freight(new ArrayList<>());
            freightDetail.setPort_entry_details(new ArrayList<>());

            if(!comments.toLowerCase().contains(ExcelConstants.DEFAULT_LOGISTIC_COMMENT.toLowerCase())) {
                freightDetail.setComments(comments);
            }

            for (Map.Entry<String, Map<String, PortEntryDetails>> gpMillAndPortAndDetailsMap : millPortAndDetails.getValue().entrySet()) {
                for(Map.Entry<String, PortEntryDetails> portAndDetailsMap : gpMillAndPortAndDetailsMap.getValue().entrySet()) {
                    if (supplierMillPortEntryInlandDetailsMap.containsKey(millPortAndDetails.getKey()) && supplierMillPortEntryInlandDetailsMap.get(millPortAndDetails.getKey()).containsKey(gpMillAndPortAndDetailsMap.getKey()) && supplierMillPortEntryInlandDetailsMap.get(millPortAndDetails.getKey()).get(gpMillAndPortAndDetailsMap.getKey()).containsKey(portAndDetailsMap.getKey())) {
                        freightDetail.getInland_freight().add(supplierMillPortEntryInlandDetailsMap.get(millPortAndDetails.getKey()).get(gpMillAndPortAndDetailsMap.getKey()).get(portAndDetailsMap.getKey()));
                    }
                    freightDetail.getPort_entry_details().add(portAndDetailsMap.getValue());
                }
            }
            if (supplierMillsToSupplierMillsMetadataMap.containsKey(millPortAndDetails.getKey())) {
                //Set Freight Details for that Mill and Add in the List
                supplierMillsToSupplierMillsMetadataMap.get(millPortAndDetails.getKey()).setFreight_detail(freightDetail);
                supplierMillsList.add(supplierMillsToSupplierMillsMetadataMap.get(millPortAndDetails.getKey()));
            }
        }
        return supplierMillsList;
    }

    //Method to Populate PortEntry Details From particular row
    private static PortEntryDetails populatePortEntryDetails(Row row, Map<String, Integer> columnsMap, String portEntry, String gpMillName) {
        PortEntryDetails portEntryDetails = new PortEntryDetails();
        portEntryDetails.setPort_entry(portEntry);
        portEntryDetails.setCost_uom(Constants.COST_UOM);
        portEntryDetails.setIncoterms(new ArrayList<>());

        if (!Objects.equals(Utils.getTrimmedNumber(getCellValueForNAFields(row, columnsMap, ResponseRfpExcelHeaders.PORT_TIME_IN_DAYS.getValue())), ExcelConstants.NA)) {
            portEntryDetails.setPort_free_time_in_days(Long.valueOf(Utils.getTrimmedNumber(getCellValueForNAFields(row, columnsMap, ResponseRfpExcelHeaders.PORT_TIME_IN_DAYS.getValue()))));
        }


        if (!Objects.equals(Utils.getTrimmedNumber(getCellValueForNAFields(row, columnsMap, ResponseRfpExcelHeaders.TRANSIT_LEAD_TIME_ORIGIN_PORT_TO_US_PORT.getValue())), ExcelConstants.NA)) {
            portEntryDetails.setTransit_leadtime_in_days_origin_port_port_entry(Long.valueOf(Utils.getTrimmedNumber(getCellValueForNAFields(row, columnsMap, ResponseRfpExcelHeaders.TRANSIT_LEAD_TIME_ORIGIN_PORT_TO_US_PORT.getValue()))));
        }


        portEntryDetails.setSteamship_line(getCellValueForNAFields(row, columnsMap, ExcelConstants.ResponseRfpExcelHeaders.STEAMSHIP_LINE.getValue()));
        portEntryDetails.setOcean_freight(Utils.getFloatDoller(getCellValueForNAFields(row, columnsMap, ResponseRfpExcelHeaders.OCEAN_FREIGHT.getValue())));

        if (df.formatCellValue(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.NOMINATED_SAFETY_STOCK.getValue()))) == null || df.formatCellValue(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.NOMINATED_SAFETY_STOCK.getValue()))).trim().isEmpty()) {
            String errorMessage = MessageFormat.format(ErrorMessages.Messages.MANDATORY_FIELD_MISSING_ERROR.getMessage(), ExcelConstants.ResponseRfpExcelHeaders.NOMINATED_SAFETY_STOCK.getValue());
            errorMessageDetailsList.add(new ErrorMessageDetails(ExcelConstants.ResponseRfpExcelSheetNames.LOGISTIC_PRICING.getSheetName(), new CellReference(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.NOMINATED_SAFETY_STOCK.getValue()))).formatAsString(false), ExcelConstants.ExpectedValuesList.MANDATORY.getValue(), df.formatCellValue(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.NOMINATED_SAFETY_STOCK.getValue()))), ExcelConstants.ResponseRfpExcelHeaders.NOMINATED_SAFETY_STOCK.getValue(), errorMessage));
        } else {
            portEntryDetails.setSafety_stock_nominated_in_days(Long.valueOf(df.formatCellValue(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.NOMINATED_SAFETY_STOCK.getValue())))));
        }

        portEntryDetails.setTransit_cost_from_port_entry_to_safety_stock_loc(Utils.getFloatDoller(getCellValueForNAFields(row, columnsMap, ExcelConstants.ResponseRfpExcelHeaders.TRANSIT_COST_FROM_US_PORT_TO_SAFETY_STOCK_LOC.getValue())));
        portEntryDetails.setGp_mill(gpMillName);
        SafetyStockLocation safetyStockLocation = new SafetyStockLocation();
        safetyStockLocation.setName(df.formatCellValue(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.SAFETY_STOCK_LOCATION_NAME.getValue()))));
        safetyStockLocation.setType(df.formatCellValue(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.SAFETY_STOCK_LOCATION_TYPE.getValue()))).trim());
        safetyStockLocation.setLocation(getLocationFromAddressString(ExcelConstants.ResponseRfpExcelSheetNames.LOGISTIC_PRICING.getSheetName(), row.getCell(columnsMap.get(ResponseRfpExcelHeaders.ADDRESS.getValue()))));
        portEntryDetails.setSafety_stock_location(safetyStockLocation);
        //TODO Confirm when it will be GP
        portEntryDetails.setHandled_by(ExcelConstants.HANDLED_BY_SUPPLIER);
        return portEntryDetails;
    }

    //Method to return  Location Object By Splitting Address in Format(AddressLine1 , AddressLine2, city, state, country, zip)
    private static Location getLocationFromAddressString(String sheetName, Cell cell) {
        Location location = new Location();
        String addressString = df.formatCellValue(cell);
        if (addressString == null || addressString.trim().isEmpty()) {
            return location;
        }
        String[] addressArray = addressString.split(Constants.COMMA);
        if(addressArray==null || addressArray.length>ExcelConstants.ADDRESS_STRING_REQUIRED_FIELDS_COUNT){
            return location;
        }
        if(addressArray.length >= 1 && !addressArray[0].trim().equals(ExcelConstants.NA)){
            location.setAddr_line1(addressArray[0]);
        }
        if(addressArray.length >= 2 && !addressArray[0].trim().equals(ExcelConstants.NA)){
            location.setAddr_line2(addressArray[1]);
        }
        if(addressArray.length >= 3 && !addressArray[0].trim().equals(ExcelConstants.NA)){
            location.setCity(addressArray[2]);
        }
        if(addressArray.length >= 4 && !addressArray[0].trim().equals(ExcelConstants.NA)){
            location.setState(addressArray[3]);
        }
        if(addressArray.length >= 5 && !addressArray[0].trim().equals(ExcelConstants.NA)){
            location.setCountry(addressArray[4]);
        }
        if(addressArray.length==6 && !addressArray[0].trim().equals(ExcelConstants.NA)){
            location.setPin_code(Utils.getTrimmedNumber(addressArray[5]));
        }
        return location;
    }

    //Method to Populate Inland Freight details For Particular Row
    private static InlandFreight populateInlandFreightDetails(Row row, Map<String, Integer> columnsMap, String gpMillName, String portEntry) {
        InlandFreight inlandFreight = new InlandFreight();
        inlandFreight.setGp_mill(gpMillName);
        inlandFreight.setSource_type(df.formatCellValue(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.INLAND_TRANSIT_ORIGIN_TYPE.getValue()))));
        inlandFreight.setSource_name(df.formatCellValue(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.INLAND_TRANSIT_ORIGIN_NAME.getValue()))));
        inlandFreight.setInland_trans_route(df.formatCellValue(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.INLAND_TRANSPORTATION_ROUTE.getValue()))));
        inlandFreight.setDest_type(df.formatCellValue(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.INLAND_TRANSIT_DESTINATION_TYPE.getValue()))));
        inlandFreight.setDest_name(df.formatCellValue(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.INLAND_TRANSIT_DESTINATION_NAME.getValue()))));
        inlandFreight.setDest_location(getLocationFromAddressString(ExcelConstants.ResponseRfpExcelSheetNames.LOGISTIC_PRICING.getSheetName(), row.getCell(ExcelConstants.LOGISTIC_PRICING_MISMATCH_MAP.get(ResponseRfpExcelHeaders.DESTINATION_ADDRESS.getValue()))));
        inlandFreight.setTransit_mode(df.formatCellValue(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.TRANSIT_MODE.getValue()))));
        inlandFreight.setTransit_cost(Utils.getFloatDoller(df.formatCellValue(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.TRANSIT_COST.getValue())))));
        inlandFreight.setCost_uom(Constants.COST_UOM);
        inlandFreight.setTransit_leadtime_in_days_port_entry_gp_mill(Long.valueOf(Utils.getTrimmedNumber(df.formatCellValue(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.TRANSIT_LEAD_TIME_IN_DAYS_US_PORT_TO_GP_MILL.getValue()))))));
        inlandFreight.setPort_entry(portEntry);
        return inlandFreight;
    }

    //Populate Incoterms For Particular Row
    private static Incoterms populateIncoterms(Row row, Map<String, Integer> columnsMap, Map<String, Map<String, Incoterms>> usPortEntryToIncotermMap, String portEntry) {
        Incoterms incoterms = new Incoterms();
        incoterms.setIncoterm(df.formatCellValue(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.INCOTERMS.getValue()))));
        incoterms.setCustoms_fee(Utils.getFloatDoller(getCellValueForNAFields(row, columnsMap, ResponseRfpExcelHeaders.IMPORT_CUSTOMS_TARIFF_FEE.getValue())));

        incoterms.setHandling_cost(Utils.getFloatDoller(getCellValueForNAFields(row, columnsMap, ExcelConstants.ResponseRfpExcelHeaders.HANDLING_COST.getValue())));
        incoterms.setSecurity_cost(Utils.getFloatDoller(getCellValueForNAFields(row, columnsMap, ExcelConstants.ResponseRfpExcelHeaders.SECURITY_COST.getValue())));
        incoterms.setStevedoring_cost(Utils.getFloatDoller(getCellValueForNAFields(row, columnsMap, ExcelConstants.ResponseRfpExcelHeaders.STEVEDORING_COST.getValue())));
        incoterms.setWharfage_cost(Utils.getFloatDoller(getCellValueForNAFields(row, columnsMap, ResponseRfpExcelHeaders.WHARFAGE_COST.getValue())));
        incoterms.setWarehouse_cost_per_month(Utils.getFloatDoller(getCellValueForNAFields(row, columnsMap, ResponseRfpExcelHeaders.WAREHOUSING_FEE_MONTHLY.getValue())));


        return validateIncoterm(incoterms, usPortEntryToIncotermMap, portEntry, row);
    }


    //Method to Populate and validate Incoterms
    private static Incoterms validateIncoterm(Incoterms incoterms, Map<String, Map<String, Incoterms>> usPortEntryToIncotermMap, String portEntry, Row row) {
        Map<String, Incoterms> incotermMap = new HashMap<>();

        String errorMessage;
        String foundValue = null;

        //If portEntry key is not there , add and return
        if (!usPortEntryToIncotermMap.containsKey(portEntry)) {
            incotermMap.put(incoterms.getIncoterm(), incoterms);
            usPortEntryToIncotermMap.put(portEntry, incotermMap);
            return incoterms;
        }
        //If portEntry key is  there , validate for same data
        incotermMap = usPortEntryToIncotermMap.get(portEntry);
        // check if the incoterm is same then incoterm details also be same
        if (incotermMap.containsKey(incoterms.getIncoterm())) {
            Incoterms incotermFromMap = incotermMap.get(incoterms.getIncoterm());

//            if (!Objects.equals(incotermFromMap.getStevedoring_cost(), incoterms.getStevedoring_cost())) {
//                CellReference cellReference = new CellReference(row.getRowNum(), ExcelConstants.LOGISTIC_PRICING_MAP.get(ExcelConstants.ResponseRfpExcelHeaders.STEVEDORING_COST.getValue()));
//                errorMessage = MessageFormat.format(ErrorMessages.Messages.INVALID_INCOTERMS_ERROR.getMessage(), cellReference.formatAsString(false));
//                errorMessageDetailsList.add(new ErrorMessageDetails(ExcelConstants.ResponseRfpExcelSheetNames.LOGISTIC_PRICING.getSheetName(), cellReference.formatAsString(false), incotermFromMap.getStevedoring_cost().toString(), incoterms.getStevedoring_cost().toString(), ResponseRfpExcelHeaders.STEVEDORING_COST.getValue(), errorMessage));
//            }
//            if (!Objects.equals(incotermFromMap.getHandling_cost(), incoterms.getHandling_cost())) {
//                CellReference cellReference = new CellReference(row.getRowNum(), ExcelConstants.LOGISTIC_PRICING_MAP.get(ResponseRfpExcelHeaders.HANDLING_COST.getValue()));
//                errorMessage = MessageFormat.format(ErrorMessages.Messages.INVALID_INCOTERMS_ERROR.getMessage(), cellReference.formatAsString(false));
//                if(incotermFromMap.getHandling_cost() != null){
//                    foundValue = incotermFromMap.getHandling_cost().toString();
//                }
//                errorMessageDetailsList.add(new ErrorMessageDetails(ExcelConstants.ResponseRfpExcelSheetNames.LOGISTIC_PRICING.getSheetName(), cellReference.formatAsString(false), incotermFromMap.getHandling_cost().toString(), foundValue , ResponseRfpExcelHeaders.HANDLING_COST.getValue(), errorMessage));
//            }
//            if (!Objects.equals(incotermFromMap.getWharfage_cost(), incoterms.getWharfage_cost())) {
//                CellReference cellReference = new CellReference(row.getRowNum(), ExcelConstants.LOGISTIC_PRICING_MAP.get(ResponseRfpExcelHeaders.WHARFAGE_COST.getValue()));
//                errorMessage = MessageFormat.format(ErrorMessages.Messages.INVALID_INCOTERMS_ERROR.getMessage(), cellReference.formatAsString(false));
//                if(incoterms.getWharfage_cost() != null){
//                    foundValue = incoterms.getWharfage_cost().toString();
//                }
//                errorMessageDetailsList.add(new ErrorMessageDetails(ExcelConstants.ResponseRfpExcelSheetNames.LOGISTIC_PRICING.getSheetName(), cellReference.formatAsString(false), incotermFromMap.getWharfage_cost().toString(), foundValue, ResponseRfpExcelHeaders.WHARFAGE_COST.getValue(), errorMessage));
//            }
//            if (!Objects.equals(incotermFromMap.getSecurity_cost(), incoterms.getSecurity_cost())) {
//                CellReference cellReference = new CellReference(row.getRowNum(), ExcelConstants.LOGISTIC_PRICING_MAP.get(ResponseRfpExcelHeaders.SECURITY_COST.getValue()));
//                errorMessage = MessageFormat.format(ErrorMessages.Messages.INVALID_INCOTERMS_ERROR.getMessage(), cellReference.formatAsString(false));
//                if(incotermFromMap.getSecurity_cost() != null){
//                    foundValue = incotermFromMap.getSecurity_cost().toString();
//                }
//                errorMessageDetailsList.add(new ErrorMessageDetails(ExcelConstants.ResponseRfpExcelSheetNames.LOGISTIC_PRICING.getSheetName(), cellReference.formatAsString(false), foundValue, incoterms.getSecurity_cost().toString(), ResponseRfpExcelHeaders.SECURITY_COST.getValue(), errorMessage));
//            }
//            if (!Objects.equals(incotermFromMap.getWarehouse_cost_per_month(), incoterms.getWarehouse_cost_per_month())) {
//                CellReference cellReference = new CellReference(row.getRowNum(), ExcelConstants.LOGISTIC_PRICING_MAP.get(ResponseRfpExcelHeaders.WAREHOUSING_FEE_MONTHLY.getValue()));
//                errorMessage = MessageFormat.format(ErrorMessages.Messages.INVALID_INCOTERMS_ERROR.getMessage(), cellReference.formatAsString(false));
//                if(incotermFromMap.getWarehouse_cost_per_month()!= null){
//                    foundValue = incotermFromMap.getWarehouse_cost_per_month().toString();
//                }
//                errorMessageDetailsList.add(new ErrorMessageDetails(ExcelConstants.ResponseRfpExcelSheetNames.LOGISTIC_PRICING.getSheetName(), cellReference.formatAsString(false), foundValue, incoterms.getWarehouse_cost_per_month().toString(), ResponseRfpExcelHeaders.WAREHOUSING_FEE_MONTHLY.getValue(), errorMessage));
//            }
//            if (!Objects.equals(incotermFromMap.getCustoms_fee(), incoterms.getCustoms_fee())) {
//                CellReference cellReference = new CellReference(row.getRowNum(), ExcelConstants.LOGISTIC_PRICING_MAP.get(ResponseRfpExcelHeaders.IMPORT_CUSTOMS_TARIFF_FEE.getValue()));
//                errorMessage = MessageFormat.format(ErrorMessages.Messages.INVALID_INCOTERMS_ERROR.getMessage(), cellReference.formatAsString(false));
//                if(incotermFromMap.getCustoms_fee()!= null){
//                    foundValue = incotermFromMap.getCustoms_fee().toString();
//                }
//                errorMessageDetailsList.add(new ErrorMessageDetails(ExcelConstants.ResponseRfpExcelSheetNames.LOGISTIC_PRICING.getSheetName(), cellReference.formatAsString(false), foundValue, incoterms.getCustoms_fee().toString(), ResponseRfpExcelHeaders.IMPORT_CUSTOMS_TARIFF_FEE.getValue(), errorMessage));
//            }
            return incoterms;
        } else {
            incotermMap.put(incoterms.getIncoterm(), incoterms);
            usPortEntryToIncotermMap.put(portEntry, incotermMap);
            return incoterms;
        }
    }


    //Method to Populate Map of SupplierMill Name to Bid Quantity Details
    private static Map<String, BidQtyDetail> populateBidQtyDetails(Sheet sheet, List<Mills> millsList) throws ParseException {
        Map<String, BidQtyDetail> millNameToBidQtyDetails = new HashMap<>();
        // validate Headers of BidQtyDetails Sheet
        Boolean isValidSheet = validateHeaderOfBidQtyDetails(sheet);
        BidQtyDetail bidQtyDetail = new BidQtyDetail();
        Boolean isTotalFound = validateMandatoryFieldsOfBidQtyDetails(sheet,bidQtyDetail);

        //setting Period value for CommercialPricing Details
        settingPeriodValue(bidQtyDetail.getPeriod_start());

        //If total is not found then End Of Mills Row will not be able to find so return
        if(Boolean.FALSE.equals(isTotalFound)){
            return millNameToBidQtyDetails;
        }
        //validating Mill Specific and Lump Sum Section Details Based on Bid type
        validateBidTypeBasedConditions(sheet,bidQtyDetail);
        // if the sheet has Invalid headers or Missing mandatory fields then return
        if(Boolean.FALSE.equals(isValidSheet)){
            return millNameToBidQtyDetails;
        }

        //If Bid Type is Mill Specific We need to store Mill Wise Volumes
        if (Objects.equals(bidQtyDetail.getBid_type(), Constants.BidType.MILL_SPECIFIC.getValue())) {
            //setting GpMills Details  like GpMill,State and ExpectedAnnualVolume only if it is Mill_specific
            setGpMillDetails(sheet, millsList);
            Map<String,Long> mapOfSupplierMillToTotalBidVol = new HashMap<>();
            Map<String, List<MillSpecBid>> mapOfSupplierMillsToBidQty = populateSupplierMillToMillSpecificBidDetails(sheet,mapOfSupplierMillToTotalBidVol);
            //Preparing map of supplier Mill name to Bid Qty Details From  mapOfSupplierMillsToBidQty
            for (Map.Entry<String, List<MillSpecBid>> supplierMillToMillSpecBid : mapOfSupplierMillsToBidQty.entrySet()) {
                // If it is Mill_specific then setting Bid_vol = sum of all vol for a supplier
                bidQtyDetail.setBid_vol(mapOfSupplierMillToTotalBidVol.get(supplierMillToMillSpecBid.getKey()));
                BidQtyDetail millSpecificBidQtyDetails = new BidQtyDetail(bidQtyDetail.getBid_type(), bidQtyDetail.getQty_uom(), bidQtyDetail.getPeriod_start(), bidQtyDetail.getPeriod_end(), bidQtyDetail.getBid_vol(),bidQtyDetail.getBid_vol_variance_pct(), supplierMillToMillSpecBid.getValue());
                millNameToBidQtyDetails.put(supplierMillToMillSpecBid.getKey(), millSpecificBidQtyDetails);
            }
        }


        //If Lump Sum no need to store Mill Specific we have to store only for one SupplierMill
        if (Objects.equals(bidQtyDetail.getBid_type(), Constants.BidType.LUMP_SUM.getValue())) {
            //Uncomment for old sheet
             //millNameToBidQtyDetails = getLumpSumSpecificValues(sheet, bidQtyDetail);
            //New sheet
            millNameToBidQtyDetails = populateSupplierNameToBidDetailsForLumpSum(sheet,bidQtyDetail);
        }
        return millNameToBidQtyDetails;

    }

    //Method to setting GpMills Details  like GpMill,State and ExpectedAnnualVolume for Mill Specific Section
    private static void setGpMillDetails(Sheet sheet, List<Mills> millsList) {
        //Starting Row of Gp Mills in Mill Specific Section
        int firstRowOfGpMills = bidDetailsMap.get(ExcelConstants.ResponseRfpExcelHeaders.GP_MILL_START_ROW.getValue());
        // getting end of row where supplier mills ends
        if(endRowOfMills==null){
            return;
        }
        int endRow = endRowOfMills;
        for (int rowNum = firstRowOfGpMills; rowNum < endRow; rowNum++) {
            Row row = sheet.getRow(rowNum);
            if (!isRowEmpty(row)) {
                Mills mill = new Mills();
                //Setting Mill Name
                if (checkIsCellEmptyAndValidateRegex(new CellReference(row.getCell(bidDetailsMap.get(ExcelConstants.ResponseRfpExcelHeaders.GP_MILL_COL_NUMBER.getValue()))), sheet, Optional.of(ExcelConstants.ALPHABETS_REGEX))) {
                    mill.setMillName(df.formatCellValue(row.getCell(bidDetailsMap.get(ExcelConstants.ResponseRfpExcelHeaders.GP_MILL_COL_NUMBER.getValue()))));
                }
                //Setting state
                if (checkIsCellEmptyAndValidateRegex(new CellReference(row.getCell(bidDetailsMap.get(ExcelConstants.ResponseRfpExcelHeaders.GP_MILL_STATE_COL.getValue()))), sheet, Optional.of(ExcelConstants.ALPHABETS_REGEX))) {
                    mill.setState(df.formatCellValue(row.getCell(bidDetailsMap.get(ExcelConstants.ResponseRfpExcelHeaders.GP_MILL_STATE_COL.getValue()))));
                }
                //Setting Expected Volume
                if (checkIsCellEmptyAndValidateRegex(new CellReference(row.getCell(bidDetailsMap.get(ExcelConstants.ResponseRfpExcelHeaders.GP_MILL_EXPECTED_ANNUAL_VOLUME_COL_NUM.getValue()))), sheet, Optional.of(ExcelConstants.NUMERIC_CHARACTERS_REGEX))) {
                    mill.setExpectedAnnualVolume(Math.round(Double.parseDouble(df.formatCellValue(row.getCell(ExcelConstants.BID_DETAILS_MAP.get(ResponseRfpExcelHeaders.GP_MILL_EXPECTED_ANNUAL_VOLUME_COL_NUM.getValue()))).replaceAll(Constants.COMMA, Constants.EMPTY_STRING))));
                }
                millsList.add(mill);
            }
        }

    }

    //Method to validate Mandatory fields of Bid qty details Sheet
    private static Boolean validateMandatoryFieldsOfBidQtyDetails(Sheet sheet, BidQtyDetail bidQtyDetail) throws ParseException {

        // getting starting and ending of column number of SupplierMills
        CellRangeAddress mergedRegionOfSupplierMillNames = getSupplierMillNamesMergedRegion(sheet);
        startColumnNumber = mergedRegionOfSupplierMillNames.getFirstColumn();
        endColumnNumber = mergedRegionOfSupplierMillNames.getLastColumn();
        // getting end of row where supplier mills ends
        endRowOfMills = getEndOfMillsRow(sheet);
        if (endRowOfMills == null) {
            return false;
        }

        //BidType
        Cell bidType = isCellEmptyForBidQtyDetails(new CellReference(ExcelConstants.COLUMN_NAME_TO_CELL_REFERENCE.get(ExcelConstants.ResponseRfpExcelHeaders.BID_TYPE.getValue())), sheet, ResponseRfpExcelHeaders.BID_TYPE.getValue(), Optional.of(CellType.STRING));
        if ((bidType != null && df.formatCellValue(bidType) != null && !df.formatCellValue(bidType).trim().isEmpty()) && Utils.validateBidType(df.formatCellValue(bidType))) {
            if(df.formatCellValue(bidType).equals(Constants.BidType.MILL_SPECIFIC_HYPHEN.getValue())){
                bidQtyDetail.setBid_type(Constants.BidType.MILL_SPECIFIC.getValue());
            }else{
                bidQtyDetail.setBid_type(df.formatCellValue(bidType));
            }
        }
        //set Qty Uom
        bidQtyDetail.setQty_uom(Constants.BID_QTY_UOM);
        //Start period
        Cell startPeriod = isCellEmptyForBidQtyDetails(new CellReference(ExcelConstants.COLUMN_NAME_TO_CELL_REFERENCE.get(ExcelConstants.ResponseRfpExcelHeaders.BEGINNING_SUPPLIER_PERIOD.getValue())), sheet, ResponseRfpExcelHeaders.BEGINNING_SUPPLIER_PERIOD.getValue(), Optional.empty());

        //endPeriod
        Cell endPeriod = isCellEmptyForBidQtyDetails(new CellReference(ExcelConstants.COLUMN_NAME_TO_CELL_REFERENCE.get(ExcelConstants.ResponseRfpExcelHeaders.ENDING_SUPPLIER_PERIOD.getValue())), sheet, ResponseRfpExcelHeaders.ENDING_SUPPLIER_PERIOD.getValue(), Optional.empty());
        //validate startPeriod and endPeriod
        validateBeginningAndEndingSupplyPeriod(sheet,startPeriod,endPeriod,bidQtyDetail);

        //volVariancePct
        CellReference volVariancePct = new CellReference(ExcelConstants.COLUMN_NAME_TO_CELL_REFERENCE.get(ExcelConstants.ResponseRfpExcelHeaders.BID_VOLUME_VARIANCE.getValue()));
        Cell bidVolVariance = isCellEmptyForBidQtyDetails(volVariancePct, sheet, ResponseRfpExcelHeaders.BID_VOLUME_VARIANCE.getValue(), Optional.empty());

        if (bidVolVariance != null && df.formatCellValue(bidVolVariance)!=null && !df.formatCellValue(bidVolVariance).trim().isEmpty() && validateVolVarianceData(bidVolVariance, sheet.getSheetName())) {
            bidQtyDetail.setBid_vol_variance_pct(Math.round(Double.parseDouble(df.formatCellValue(bidVolVariance).replaceAll(Constants.PERCENT, Constants.EMPTY_STRING))));
        }


        //check for BidValue is Mill-specific then validate bid-volume
        if (bidQtyDetail.getBid_type() != null && Objects.equals(bidQtyDetail.getBid_type(), Constants.BidType.MILL_SPECIFIC.getValue())) {
            validateMillSpecificConditions(sheet,endRowOfMills);
         }
        //if bid Type is Lump Sum then Validate Lump Specific conditions
        if (bidType != null && Objects.equals(bidType.toString(), Constants.BidType.LUMP_SUM.getValue())) {
            validateLumpSumSpecificConditions(sheet,endRowOfMills);
        }
        return true;
    }

    //method to validate begining and Ending Supplier Period
    private static void validateBeginningAndEndingSupplyPeriod(Sheet sheet, Cell startPeriod, Cell endPeriod, BidQtyDetail bidQtyDetail) throws ParseException {
        if (startPeriod != null && df.formatCellValue(startPeriod) != null && !df.formatCellValue(startPeriod).trim().isEmpty() && endPeriod != null && df.formatCellValue(endPeriod) != null && !df.formatCellValue(endPeriod).trim().isEmpty()) {
            String periodStart =  Utils.parseAndExtractDateString(df.formatCellValue(startPeriod));
            String periodEnd = Utils.parseAndExtractDateString(df.formatCellValue(endPeriod));
            Boolean isValidDates = Utils.validateDateForExcel(periodStart, periodEnd, new CellReference(startPeriod.getRowIndex(), startPeriod.getColumnIndex()), new CellReference(endPeriod.getRowIndex(), endPeriod.getColumnIndex()));
            // Validating Start Date And End Date Based On contract Term
            int contractTerm = 0;

            if (Boolean.TRUE.equals(isCellEmptyAndValidateRegex(new CellReference(ExcelConstants.BID_QTY_SHEET_HEADERS_VALUES_CELL_REFERENCE.get(ResponseRfpExcelHeaders.CONTRACT_TERM.getValue())),sheet,ResponseRfpExcelHeaders.CONTRACT_TERM.getValue(), Optional.of(ExcelConstants.CONTRACT_TERM_REGEX),ExcelConstants.YEAR))){
                contractTerm = Integer.parseInt(df.formatCellValue(getCellValueFromCellReference(new CellReference(ExcelConstants.BID_QTY_SHEET_HEADERS_VALUES_CELL_REFERENCE.get(ResponseRfpExcelHeaders.CONTRACT_TERM.getValue())),sheet)));
            }

            if (contractTerm!=0) {
                //validate startPeriod
                if (Boolean.TRUE.equals(isValidDates) && Boolean.FALSE.equals(Utils.isValidStartPeriod(periodStart, contractTerm))) {
                    String errorMessage = MessageFormat.format(ErrorMessages.Messages.DATE_BEGINNING_SUPPLY_PERIOD_INVALID_BASED_ON_CONTRACT_TERM_ERROR.getMessage(), ExcelConstants.START_MONTH_DATE + (contractTerm - 1), ExcelConstants.END_MONTH_DATE + contractTerm, periodStart);
                    errorMessageDetailsList.add(new ErrorMessageDetails(sheet.getSheetName(), ExcelConstants.COLUMN_NAME_TO_CELL_REFERENCE.get(ResponseRfpExcelHeaders.BEGINNING_SUPPLIER_PERIOD.getValue()), Constants.DATE_FORMAT_FOR_EXCEL, periodStart, ResponseRfpExcelHeaders.BEGINNING_SUPPLIER_PERIOD.getValue(), errorMessage));
                }
                //validate endPeriod
                if (Boolean.TRUE.equals(isValidDates) && Boolean.FALSE.equals(Utils.isValidEndPeriod(periodEnd, contractTerm))) {
                    String errorMessage = MessageFormat.format(ErrorMessages.Messages.DATE_ENDING_SUPPLY_PERIOD_INVALID_BASED_ON_CONTRACT_TERM_ERROR.getMessage(), ExcelConstants.START_MONTH_DATE + contractTerm, ExcelConstants.END_MONTH_DATE + contractTerm, periodEnd);
                    errorMessageDetailsList.add(new ErrorMessageDetails(sheet.getSheetName(), ExcelConstants.COLUMN_NAME_TO_CELL_REFERENCE.get(ResponseRfpExcelHeaders.ENDING_SUPPLIER_PERIOD.getValue()), Constants.DATE_FORMAT_FOR_EXCEL, periodEnd, ResponseRfpExcelHeaders.ENDING_SUPPLIER_PERIOD.getValue(), errorMessage));
                }
            }
            bidQtyDetail.setPeriod_start(periodStart);
            bidQtyDetail.setPeriod_end(periodEnd);

        }
    }

    //Method to validate GP Mills details like Mill,State, And ExpectedAnnualVolume
    private static void validateMandatoryFieldOfGpMills(Sheet sheet, Integer endRowOfMills) {

        int firstRowOfGpMills = bidDetailsMap.get(ExcelConstants.ResponseRfpExcelHeaders.GP_MILL_START_ROW.getValue());
        // getting end of row where supplier mills ends
        int endRow = endRowOfMills;
        for (int rowNum = firstRowOfGpMills; rowNum < endRow; rowNum++) {
            Row row = sheet.getRow(rowNum);
            if (!isRowEmpty(row)) {
                isCellEmptyAndValidateRegex(new CellReference(row.getRowNum(), ExcelConstants.BID_DETAILS_MAP.get(ResponseRfpExcelHeaders.GP_MILL_COL_NUMBER.getValue())), sheet, ResponseRfpExcelHeaders.GP_MILL_CELL_NUM.getValue(), Optional.of(ExcelConstants.ALPHABETS_REGEX), ExcelConstants.ALPHABETS);
                isCellEmptyAndValidateRegex(new CellReference(row.getRowNum(), ExcelConstants.BID_DETAILS_MAP.get(ResponseRfpExcelHeaders.GP_MILL_STATE_COL.getValue())), sheet, ResponseRfpExcelHeaders.GP_MILL_STATE_CELL_NUM.getValue(), Optional.of(ExcelConstants.ALPHABETS_REGEX), ExcelConstants.ALPHABETS);
                isCellEmptyAndValidateRegex(new CellReference(row.getRowNum(), ExcelConstants.BID_DETAILS_MAP.get(ResponseRfpExcelHeaders.GP_MILL_EXPECTED_ANNUAL_VOLUME_COL_NUM.getValue())), sheet, ResponseRfpExcelHeaders.GP_MILL_EXPECTED_ANNUAL_VOLUME_CELL_NUM.getValue(), Optional.of(ExcelConstants.NUMERIC_CHARACTERS_REGEX), ExcelConstants.NUMBER);
            }
        }

    }


    //Methode to validating empty Cell and cell value regex
    private static Boolean isCellEmptyAndValidateRegex(CellReference cellReference, Sheet sheet, String filedName, Optional<String> regex, String exceptedDataType) {

        Row row = sheet.getRow(cellReference.getRow());
        if (row == null) {
            String errorMessage = MessageFormat.format(ErrorMessages.Messages.MANDATORY_FIELD_MISSING_ERROR.getMessage(), filedName);
            errorMessageDetailsList.add(new ErrorMessageDetails(sheet.getSheetName(), cellReference.formatAsString(false), ExcelConstants.ExpectedValuesList.MANDATORY.getValue(), null, filedName, errorMessage));
            return false;
        } else {
            Cell cell = row.getCell(cellReference.getCol());
            if (cell == null || df.formatCellValue(cell) == null || df.formatCellValue(cell).trim().isEmpty()) {
                String errorMessage = MessageFormat.format(ErrorMessages.Messages.MANDATORY_FIELD_MISSING_ERROR.getMessage(), filedName);
                errorMessageDetailsList.add(new ErrorMessageDetails(sheet.getSheetName(), cellReference.formatAsString(false), ExcelConstants.ExpectedValuesList.MANDATORY.getValue(), df.formatCellValue(cell), filedName, errorMessage));
                return false;
            } else {
                if (regex.isPresent() && !validateRegex((df.formatCellValue(cell).replaceAll(Constants.SPACE, Constants.EMPTY_STRING).replaceAll(Constants.COMMA, Constants.EMPTY_STRING)), regex.get())) {
                    String errorMessage = MessageFormat.format(ErrorMessages.Messages.DATA_FORMAT_MISMATCH.getMessage(), exceptedDataType, df.formatCellValue(cell));
                    errorMessageDetailsList.add(new ErrorMessageDetails(sheet.getSheetName(), new CellReference(row.getRowNum(), cell.getColumnIndex()).formatAsString(false), ExcelConstants.ExpectedValuesList.MANDATORY.getValue(), df.formatCellValue(cell), filedName, errorMessage));
                    return false;
                }
            }
        }
        return true;
    }


    //Methode to validating empty Cell and cell value regex (Return true if string is not empty and valid regex)
    private static boolean checkIsCellEmptyAndValidateRegex(CellReference cellReference, Sheet sheet, Optional<String> regex) {

        Row row = sheet.getRow(cellReference.getRow());
        if (row == null) {
            return false;
        } else {
            Cell cell = row.getCell(cellReference.getCol());
            if (cell == null || df.formatCellValue(cell) == null || df.formatCellValue(cell).trim().isEmpty()) {
                return false;
            } else {
                if (regex.isPresent() && !validateRegex((df.formatCellValue(cell).replaceAll(Constants.SPACE, Constants.EMPTY_STRING).replaceAll(Constants.COMMA, Constants.EMPTY_STRING)), regex.get())) {
                    return false;
                }
            }
        }
        return true;
    }

    private static Cell isCellEmptyForBidQtyDetails(CellReference cellReference, Sheet sheet, String filedName, Optional<CellType> cellType) {
        Row row = sheet.getRow(cellReference.getRow());
        if (row == null) {
            String errorMessage = MessageFormat.format(ErrorMessages.Messages.MANDATORY_FIELD_MISSING_ERROR.getMessage(), filedName);
            errorMessageDetailsList.add(new ErrorMessageDetails(sheet.getSheetName(), cellReference.formatAsString(false), ExcelConstants.ExpectedValuesList.MANDATORY.getValue(), null, filedName, errorMessage));
            return null;
        } else {
            Cell cell = row.getCell(cellReference.getCol());
            if (cell == null || df.formatCellValue(cell)==null || df.formatCellValue(cell).trim().isEmpty()) {
                String errorMessage = MessageFormat.format(ErrorMessages.Messages.MANDATORY_FIELD_MISSING_ERROR.getMessage(), filedName);
                errorMessageDetailsList.add(new ErrorMessageDetails(sheet.getSheetName(), cellReference.formatAsString(false), ExcelConstants.ExpectedValuesList.MANDATORY.getValue(), df.formatCellValue(cell), filedName, errorMessage));
            } else {
                if (cellType.isPresent()) {
                    if (cell.getCellType() != cellType.get()) {
                        String errorMessage = MessageFormat.format(ErrorMessages.Messages.DATA_TYPE_MISMATCH.getMessage(), cellType.get(), cell.getCellType());
                        errorMessageDetailsList.add(new ErrorMessageDetails(sheet.getSheetName(), new CellReference(row.getRowNum(), cell.getColumnIndex()).formatAsString(false), ExcelConstants.ExpectedValuesList.MANDATORY.getValue(), df.formatCellValue(cell), filedName, errorMessage));
                    }
                }
            }
            return cell;
        }

    }

    private static Boolean validateHeaderOfBidQtyDetails(Sheet sheet) {
        CellReference cellReference;

        int size = errorMessageDetailsList.size();

        // validate Headers for SupplierName
        cellReference = new CellReference(ExcelConstants.BID_QTY_DETAILS_COLUMN_HEADER_TO_COLUMN_NUMBER.get(ResponseRfpExcelHeaders.SUPPLIER_NAME.getValue()));
        validateHeadersForBidQtyDetails(sheet.getRow(cellReference.getRow()), cellReference.getCol(), ResponseRfpExcelHeaders.SUPPLIER_NAME.getValue());

        // validate Headers for ContactEmail
        cellReference = new CellReference(ExcelConstants.BID_QTY_DETAILS_COLUMN_HEADER_TO_COLUMN_NUMBER.get(ResponseRfpExcelHeaders.CONTACT_EMAIL.getValue()));
        validateHeadersForBidQtyDetails(sheet.getRow(cellReference.getRow()), cellReference.getCol(), ResponseRfpExcelHeaders.CONTACT_EMAIL.getValue());

        //Rfp Number and Due Date, Commodity(fiberType) we are doing before this function (so here skipping)

        // validate Headers for ContactTerm
        cellReference = new CellReference(ExcelConstants.BID_QTY_DETAILS_COLUMN_HEADER_TO_COLUMN_NUMBER.get(ResponseRfpExcelHeaders.CONTRACT_TERM.getValue()));
        validateHeadersForBidQtyDetails(sheet.getRow(cellReference.getRow()), cellReference.getCol(), ResponseRfpExcelHeaders.CONTRACT_TERM.getValue());


        // for BidType
        cellReference = new CellReference(ExcelConstants.BID_QTY_DETAILS_COLUMN_HEADER_TO_COLUMN_NUMBER.get(ExcelConstants.ResponseRfpExcelHeaders.BID_TYPE.getValue()));
        validateHeadersForBidQtyDetails(sheet.getRow(cellReference.getRow()), cellReference.getCol(), ResponseRfpExcelHeaders.BID_TYPE.getValue());

        //Volume Bid (if Lump Sum)
        cellReference = new CellReference(ExcelConstants.BID_QTY_DETAILS_COLUMN_HEADER_TO_COLUMN_NUMBER.get(ResponseRfpExcelHeaders.VOLUME_BID_FOR_LUMP_SUM.getValue()));
        validateHeadersForBidQtyDetails(sheet.getRow(cellReference.getRow()), cellReference.getCol(), ResponseRfpExcelHeaders.VOLUME_BID_FOR_LUMP_SUM.getValue());

        //Beginning Supply period
        cellReference = new CellReference(ExcelConstants.BID_QTY_DETAILS_COLUMN_HEADER_TO_COLUMN_NUMBER.get(ExcelConstants.ResponseRfpExcelHeaders.BEGINNING_SUPPLIER_PERIOD.getValue()));
        validateHeadersForBidQtyDetails(sheet.getRow(cellReference.getRow()), cellReference.getCol(), ResponseRfpExcelHeaders.BEGINNING_SUPPLIER_PERIOD.getValue());

        //Ending Supply Period *
        cellReference = new CellReference(ExcelConstants.BID_QTY_DETAILS_COLUMN_HEADER_TO_COLUMN_NUMBER.get(ExcelConstants.ResponseRfpExcelHeaders.ENDING_SUPPLIER_PERIOD.getValue()));
        validateHeadersForBidQtyDetails(sheet.getRow(cellReference.getRow()), cellReference.getCol(), ResponseRfpExcelHeaders.ENDING_SUPPLIER_PERIOD.getValue());

        //Bid Volume Variance (%) *
        cellReference = new CellReference(ExcelConstants.BID_QTY_DETAILS_COLUMN_HEADER_TO_COLUMN_NUMBER.get(ExcelConstants.ResponseRfpExcelHeaders.BID_VOLUME_VARIANCE.getValue()));
        validateHeadersForBidQtyDetails(sheet.getRow(cellReference.getRow()), cellReference.getCol(), ResponseRfpExcelHeaders.BID_VOLUME_VARIANCE.getValue());

        //validate Mill Table headers
        //validate GpMill
        cellReference = new CellReference(ExcelConstants.BID_QTY_DETAILS_COLUMN_HEADER_TO_COLUMN_NUMBER.get(ExcelConstants.ResponseRfpExcelHeaders.GP_MILL_CELL_NUM.getValue()));
        validateHeadersForBidQtyDetails(sheet.getRow(cellReference.getRow()), cellReference.getCol(), ExcelConstants.ResponseRfpExcelHeaders.GP_MILL_CELL_NUM.getValue());

        //validate State Header
        cellReference = new CellReference(ExcelConstants.BID_QTY_DETAILS_COLUMN_HEADER_TO_COLUMN_NUMBER.get(ResponseRfpExcelHeaders.GP_MILL_STATE_CELL_NUM.getValue()));
        validateHeadersForBidQtyDetails(sheet.getRow(cellReference.getRow()), cellReference.getCol(), ResponseRfpExcelHeaders.GP_MILL_STATE_CELL_NUM.getValue());

        //validate Expected Annual Volume (ADMT)
        cellReference = new CellReference(ExcelConstants.BID_QTY_DETAILS_COLUMN_HEADER_TO_COLUMN_NUMBER.get(ResponseRfpExcelHeaders.GP_MILL_EXPECTED_ANNUAL_VOLUME_CELL_NUM.getValue()));
        validateHeadersForBidQtyDetails(sheet.getRow(cellReference.getRow()), cellReference.getCol(), ResponseRfpExcelHeaders.GP_MILL_EXPECTED_ANNUAL_VOLUME_CELL_NUM.getValue());

        //validate Supplier's Volume Bid, ADMT
        cellReference = new CellReference(ExcelConstants.BID_QTY_DETAILS_COLUMN_HEADER_TO_COLUMN_NUMBER.get(ResponseRfpExcelHeaders.GP_MILL_SUPPLIER_VOLUME_BID_CELL_NUM.getValue()));
        validateHeadersForBidQtyDetails(sheet.getRow(cellReference.getRow()), cellReference.getCol(), ResponseRfpExcelHeaders.GP_MILL_SUPPLIER_VOLUME_BID_CELL_NUM.getValue());


        // Header validating for supplier Mill A
        cellReference = new CellReference(ExcelConstants.BID_QTY_DETAILS_COLUMN_HEADER_TO_COLUMN_NUMBER.get(ResponseRfpExcelHeaders.SUPPLIER_MILL_A.getValue()));
        validateHeadersForBidQtyDetails(sheet.getRow(cellReference.getRow()), cellReference.getCol(), ResponseRfpExcelHeaders.SUPPLIER_MILL_A.getValue());

        // Header validating for supplier Mill A Volume
        cellReference = new CellReference(ExcelConstants.BID_QTY_DETAILS_COLUMN_HEADER_TO_COLUMN_NUMBER.get(ResponseRfpExcelHeaders.SUPPLIER_MILL_A_VOLUME.getValue()));
        validateHeadersForBidQtyDetails(sheet.getRow(cellReference.getRow()), cellReference.getCol(), ResponseRfpExcelHeaders.SUPPLIER_MILL_A_VOLUME.getValue());

        // Header validating for supplier Mill B
        cellReference = new CellReference(ExcelConstants.BID_QTY_DETAILS_COLUMN_HEADER_TO_COLUMN_NUMBER.get(ResponseRfpExcelHeaders.SUPPLIER_MILL_B.getValue()));
        validateHeadersForBidQtyDetails(sheet.getRow(cellReference.getRow()), cellReference.getCol(), ResponseRfpExcelHeaders.SUPPLIER_MILL_B.getValue());

        // Header validating for supplier Mill B Volume
        cellReference = new CellReference(ExcelConstants.BID_QTY_DETAILS_COLUMN_HEADER_TO_COLUMN_NUMBER.get(ResponseRfpExcelHeaders.SUPPLIER_MILL_B_VOLUME.getValue()));
        validateHeadersForBidQtyDetails(sheet.getRow(cellReference.getRow()), cellReference.getCol(), ResponseRfpExcelHeaders.SUPPLIER_MILL_B_VOLUME.getValue());

        //If any errors in Header return false
        if (size < errorMessageDetailsList.size()) {
            return Boolean.FALSE;
        } else {
            return Boolean.TRUE;
        }

    }

    private static Boolean validateHeadersForBidQtyDetails(Row row, int colIndex, String expectedValue) {
        if (row == null) {
            String errorMessage = MessageFormat.format(ErrorMessages.Messages.HEADER_INVALID.getMessage(), null);
            errorMessageDetailsList.add(new ErrorMessageDetails(ExcelConstants.ResponseRfpExcelSheetNames.BID_DETAILS.getSheetName(), null, expectedValue, Constants.EMPTY_STRING, expectedValue, errorMessage));
            return false;
        } else {
            Cell cell = row.getCell(colIndex);
            CellReference cellReference = new CellReference(row.getRowNum(), colIndex);
            if (cell == null || cell.toString().trim().isEmpty()) {
                String errorMessage = MessageFormat.format(ErrorMessages.Messages.MISSING_HEADER.getMessage(),expectedValue);
                errorMessageDetailsList.add(new ErrorMessageDetails(ExcelConstants.ResponseRfpExcelSheetNames.BID_DETAILS.getSheetName(), cellReference.formatAsString(false), expectedValue, Constants.EMPTY_STRING, expectedValue, errorMessage));
                return false;
            } else {
                if (!Objects.equals(df.formatCellValue(cell), expectedValue)) {
                    String errorMessage = MessageFormat.format(ErrorMessages.Messages.HEADER_INVALID.getMessage(), df.formatCellValue(cell), expectedValue);
                    errorMessageDetailsList.add(new ErrorMessageDetails(ExcelConstants.ResponseRfpExcelSheetNames.BID_DETAILS.getSheetName(), cellReference.formatAsString(false), expectedValue, df.formatCellValue(cell), expectedValue, errorMessage));
                    return false;
                }
            }
        }
        return true;
    }


    //Method to get Row Number of last (That is Total bid Volume Row)
    private static Integer getEndOfMillsRow(Sheet sheet) {
        String errorMessage = MessageFormat.format(ErrorMessages.Messages.MANDATORY_FIELD_MISSING_ERROR.getMessage(), ResponseRfpExcelHeaders.TOTAL_BID_VOLUME_COL_NUM.getValue());
        //Last row will be the Row Where there is Total
        Integer lastGpMillNameRow = null;
        for (int i = bidDetailsMap.get(ExcelConstants.ResponseRfpExcelHeaders.GP_MILL_START_ROW.getValue()); i < sheet.getPhysicalNumberOfRows(); i++) {
            Row row = sheet.getRow(i);
            //check for Total Row
            if (row != null && row.getCell(bidDetailsMap.get(ExcelConstants.ResponseRfpExcelHeaders.TOTAL_BID_VOLUME_COL_NUM.getValue()))!=null && df.formatCellValue(row.getCell(bidDetailsMap.get(ExcelConstants.ResponseRfpExcelHeaders.TOTAL_BID_VOLUME_COL_NUM.getValue())))!=null && Objects.equals(df.formatCellValue(row.getCell(bidDetailsMap.get(ExcelConstants.ResponseRfpExcelHeaders.TOTAL_BID_VOLUME_COL_NUM.getValue()))), ExcelConstants.TOTAL)) {
                lastGpMillNameRow = i;
                return lastGpMillNameRow;
            }
            //If total row not found check for If Lump Sum and Throw Error
            if (row != null && row.getCell(bidDetailsMap.get(ExcelConstants.ResponseRfpExcelHeaders.TOTAL_BID_VOLUME_COL_NUM.getValue()))!=null &&  df.formatCellValue(row.getCell(bidDetailsMap.get(ExcelConstants.ResponseRfpExcelHeaders.TOTAL_BID_VOLUME_COL_NUM.getValue())))!=null && df.formatCellValue(row.getCell(bidDetailsMap.get(ExcelConstants.ResponseRfpExcelHeaders.TOTAL_BID_VOLUME_COL_NUM.getValue()))).contains(ExcelConstants.IF_LUMP_SUM_CONDITION)) {
                errorMessageDetailsList.add(new ErrorMessageDetails(ExcelConstants.ResponseRfpExcelSheetNames.BID_DETAILS.getSheetName(), null, null, null, ResponseRfpExcelHeaders.TOTAL_BID_VOLUME_COL_NUM.getValue(), errorMessage));
                lastGpMillNameRow = i-ExcelConstants.IF_LUMP_SUM_TO_TOTAL_ROW_NUM_DIFF;
                return lastGpMillNameRow;
            }

            //If above two not found check Lump Sum
            if (row != null && row.getCell(bidDetailsMap.get(ExcelConstants.ResponseRfpExcelHeaders.TOTAL_BID_VOLUME_COL_NUM.getValue()))!=null && df.formatCellValue(row.getCell(bidDetailsMap.get(ExcelConstants.ResponseRfpExcelHeaders.TOTAL_BID_VOLUME_COL_NUM.getValue())))!=null  && Objects.equals(df.formatCellValue(row.getCell(bidDetailsMap.get(ExcelConstants.ResponseRfpExcelHeaders.TOTAL_BID_VOLUME_COL_NUM.getValue()))), Constants.BidType.LUMP_SUM.getValue())) {
                errorMessageDetailsList.add(new ErrorMessageDetails(ExcelConstants.ResponseRfpExcelSheetNames.BID_DETAILS.getSheetName(), null, null, null, ResponseRfpExcelHeaders.TOTAL_BID_VOLUME_COL_NUM.getValue(), errorMessage));
                lastGpMillNameRow = i-ExcelConstants.LUMP_SUM_TO_TOTAL_ROW_NUM_DIFF;
                return lastGpMillNameRow;
            }
        }
        errorMessageDetailsList.add(new ErrorMessageDetails(ExcelConstants.ResponseRfpExcelSheetNames.BID_DETAILS.getSheetName(), null, null, null, ResponseRfpExcelHeaders.TOTAL_BID_VOLUME_COL_NUM.getValue(), errorMessage));
        return lastGpMillNameRow;
    }

    private static Map<String, BidQtyDetail> getLumpSumSpecificValues(Sheet sheet, BidQtyDetail bidQtyDetail) {
        Map<String, BidQtyDetail> millNameToBidQtyDetails = new HashMap<>();

        String supplierMillName = null;

        CellReference cellReference = new CellReference(ExcelConstants.COLUMN_NAME_TO_CELL_REFERENCE.get(ResponseRfpExcelHeaders.VOLUME_BID_FOR_LUMP_SUM.getValue()));
        if (checkIsCellEmptyAndValidateRegex(new CellReference(ExcelConstants.COLUMN_NAME_TO_CELL_REFERENCE.get(ResponseRfpExcelHeaders.VOLUME_BID_FOR_LUMP_SUM.getValue())), sheet, Optional.of(ExcelConstants.NUMERIC_CHARACTERS_REGEX))) {
            bidQtyDetail.setBid_vol(Math.round(Double.parseDouble(df.formatCellValue(getCellValueFromCellReference(cellReference, sheet)).replaceAll(Constants.COMMA, Constants.EMPTY_STRING))));
        }

        //supplierMillName
//        CellReference supplierMillNameCellReference = new CellReference(ExcelConstants.COLUMN_NAME_TO_CELL_REFERENCE.get(ResponseRfpExcelHeaders.SHIP_FROM_MILL_LOCATION_FOR_LUMP_SUM.getValue()));
//        if (checkIsCellEmptyAndValidateRegex(supplierMillNameCellReference,sheet,Optional.of(ExcelConstants.ALPHABETS_REGEX))){
//            supplierMillName = df.formatCellValue(getCellValueFromCellReference(supplierMillNameCellReference,sheet));
//        }

        Map<String, List<MillSpecBid>> mapOfSupplierMillsToBidQty = new HashMap<>();

        int startColumn = startColumnNumber;
        int endColumn = endColumnNumber;
        int firstRowOfGpMills = bidDetailsMap.get(ExcelConstants.ResponseRfpExcelHeaders.GP_MILL_START_ROW.getValue());
        // getting end of row where supplier mills ends
        Integer endRow = endRowOfMills;
        for (int colNum = startColumn; colNum <= endColumn; colNum++) {
            List<MillSpecBid> millSpecBidsList = new ArrayList<>();
            for (int rowNum = firstRowOfGpMills; rowNum < endRow; rowNum++) {
                Row row = sheet.getRow(rowNum);
                if (!isRowEmpty(row)) {
                    MillSpecBid millSpecbid = new MillSpecBid();
                    if (checkIsCellEmptyAndValidateRegex(new CellReference(row.getCell(bidDetailsMap.get(ExcelConstants.ResponseRfpExcelHeaders.GP_MILL_COL_NUMBER.getValue()))), sheet, Optional.of(ExcelConstants.ALPHABETS_REGEX))) {
                        millSpecbid.setMill(df.formatCellValue(row.getCell(bidDetailsMap.get(ExcelConstants.ResponseRfpExcelHeaders.GP_MILL_COL_NUMBER.getValue()))));
                    }
                    // need to add supplierName
                    millSpecBidsList.add(millSpecbid);
                    mapOfSupplierMillsToBidQty.put(supplierMillName, millSpecBidsList);
                }
            }
        }

        for (Map.Entry<String, List<MillSpecBid>> supplierMillToMillSpecBid : mapOfSupplierMillsToBidQty.entrySet()) {
            BidQtyDetail millSpecificBidQtyDetails = new BidQtyDetail(bidQtyDetail.getBid_type(), bidQtyDetail.getQty_uom(), bidQtyDetail.getPeriod_start(), bidQtyDetail.getPeriod_end(), bidQtyDetail.getBid_vol(), bidQtyDetail.getBid_vol_variance_pct(), supplierMillToMillSpecBid.getValue());
            millNameToBidQtyDetails.put(supplierMillToMillSpecBid.getKey(), millSpecificBidQtyDetails);
        }
        return millNameToBidQtyDetails;
    }

    private static Cell getCellValueFromCellReference(CellReference cellReference, Sheet sheet) {
        Row row = sheet.getRow(cellReference.getRow());
        if (row != null) {
            return row.getCell(cellReference.getCol());
        } else {
            //TODO need to check if it is ok
            row = sheet.createRow(cellReference.getRow());
            return row.createCell(cellReference.getCol());
        }
    }


    //Method to Populate Mill Specific values if Bid Type Is Mill Specific
    private static Map<String, List<MillSpecBid>> populateSupplierMillToMillSpecificBidDetails(Sheet sheet, Map<String, Long> mapOfSupplierMillToTotalBidVol) {
        //Map of Supplier Mill Name to Mill Specific Details
        Map<String, List<MillSpecBid>> mapOfSupplierMillsToBidQty = new HashMap<>();
        int firstRowOfGpMills = bidDetailsMap.get(ExcelConstants.ResponseRfpExcelHeaders.GP_MILL_START_ROW.getValue());
        // getting end of row where supplier mills ends
        Integer endRow = endRowOfMills;
        for (int rowNum = firstRowOfGpMills; rowNum < endRow; rowNum++) {
            Row row = sheet.getRow(rowNum);
            if(isRowEmpty(row)){
                continue;
            }
            //Supplier Mill 1 Details
            populateSupplierMillSpecificDetails(sheet,row,new CellReference(row.getCell(bidDetailsMap.get(ResponseRfpExcelHeaders.SUPPLIER_MILL_A.getValue()))),new CellReference(row.getCell(bidDetailsMap.get(ResponseRfpExcelHeaders.SUPPLIER_MILL_A_VOLUME.getValue()))),mapOfSupplierMillsToBidQty,mapOfSupplierMillToTotalBidVol);

            //Supplier Mill 2 Details
            populateSupplierMillSpecificDetails(sheet,row,new CellReference(row.getCell(bidDetailsMap.get(ResponseRfpExcelHeaders.SUPPLIER_MILL_B.getValue()))),new CellReference(row.getCell(bidDetailsMap.get(ResponseRfpExcelHeaders.SUPPLIER_MILL_B_VOLUME.getValue()))),mapOfSupplierMillsToBidQty,mapOfSupplierMillToTotalBidVol);
        }
        return mapOfSupplierMillsToBidQty;
    }

    private static void populateSupplierMillSpecificDetails(Sheet sheet,Row row, CellReference supplierMillNameRef, CellReference supplierMillVolumeRef, Map<String,List<MillSpecBid>> mapOfSupplierMillsToBidQty , Map<String,Long> mapOfSupplierMillToTotalBidVol) {
        MillSpecBid millSpecbid = new MillSpecBid();
        List<MillSpecBid> millSpecBidsList = new ArrayList<>();
        String supplierMillName=null;
        Long bidVol = 0L;
        //Setting Gp Mill Name
        if(checkIsCellEmptyAndValidateRegex(new CellReference(row.getCell(bidDetailsMap.get(ExcelConstants.ResponseRfpExcelHeaders.GP_MILL_COL_NUMBER.getValue()))),sheet,Optional.of(ExcelConstants.ALPHABETS_REGEX))) {
            millSpecbid.setMill(df.formatCellValue(row.getCell(bidDetailsMap.get(ExcelConstants.ResponseRfpExcelHeaders.GP_MILL_COL_NUMBER.getValue()))));
        }
        //Setting Supplier Mill  Volume
        if(checkIsCellEmptyAndValidateRegex(supplierMillVolumeRef,sheet,Optional.of(ExcelConstants.NUMERIC_CHARACTERS_REGEX))) {
            //check for null if it is empty then throw error
            bidVol = Math.round(Double.parseDouble(df.formatCellValue(row.getCell(supplierMillVolumeRef.getCol())).replaceAll(Constants.SPACE, Constants.EMPTY_STRING).replaceAll(Constants.COMMA, Constants.EMPTY_STRING)));
            millSpecbid.setBid_vol(bidVol);
        }
        //Getting Supplier Mill Name
        if (checkIsCellEmptyAndValidateRegex(supplierMillNameRef,sheet,Optional.empty())){
            supplierMillName = df.formatCellValue(getCellValueFromCellReference(supplierMillNameRef,sheet));
        }

        //Adding In Mill Specific List of Supplier Mill
        millSpecBidsList.add(millSpecbid);

        //Checking if Map contains Key if yes appending else adding new Key
        if(supplierMillName != null && !ExcelConstants.NOT_APPLICABLE.equalsIgnoreCase(supplierMillName) && mapOfSupplierMillsToBidQty.containsKey(supplierMillName)){
            List<MillSpecBid> millSpecBidsListForSupplier = mapOfSupplierMillsToBidQty.get(supplierMillName);
            millSpecBidsListForSupplier.addAll(millSpecBidsList);
            mapOfSupplierMillsToBidQty.put(supplierMillName, millSpecBidsListForSupplier);
            //Updating Total bid Vol
            mapOfSupplierMillToTotalBidVol.put(supplierMillName,mapOfSupplierMillToTotalBidVol.get(supplierMillName)+bidVol);
        }else if (supplierMillName != null && !ExcelConstants.NOT_APPLICABLE.equalsIgnoreCase(supplierMillName)) {
            mapOfSupplierMillsToBidQty.put(supplierMillName,millSpecBidsList);
            //Adding Total Bid Bol
            mapOfSupplierMillToTotalBidVol.put(supplierMillName,bidVol);
        }
    }


    //Method to return Starting and Ending Column (Merged Region) of Supplier Mills Details
    private static CellRangeAddress getSupplierMillNamesMergedRegion(Sheet sheet) {
        CellRangeAddress supplierMillNamesMergedRegion;
        for (int j = 0; j < sheet.getNumMergedRegions(); j++) {
            supplierMillNamesMergedRegion = sheet.getMergedRegion(j);
            //Check if row is same as MillNames break and return
            if (supplierMillNamesMergedRegion.getFirstRow() == bidDetailsMap.get(ExcelConstants.ResponseRfpExcelHeaders.SUPPLIER_MILL_LAST_COL_NUM.getValue()) && supplierMillNamesMergedRegion.getFirstColumn() == bidDetailsMap.get(ExcelConstants.ResponseRfpExcelHeaders.SUPPLIER_MILL_START_COL.getValue())) {
                return supplierMillNamesMergedRegion;
            }
        }
        //If no Merged region is Found then only 1 Supplier Mill
        int millNameRowNum = bidDetailsMap.get(ExcelConstants.ResponseRfpExcelHeaders.GP_MILL_START_ROW.getValue());
        int millNameColNum = bidDetailsMap.get(ExcelConstants.ResponseRfpExcelHeaders.SUPPLIER_MILL_START_COL.getValue());
        int millNameColEndNum = 6;
        return new CellRangeAddress(millNameRowNum, millNameRowNum, millNameColNum, millNameColEndNum);
    }

    //Method to remove comma from string and return prefix of string if comma is not there it will return the same string
    private static String getSupplierMillNameFromExcelCell(String millName) {
        //check if string contains comma if yes return first value
        if (millName.contains(Constants.COMMA)) {
            // Remove the commas using the replace() method
            String values[] = millName.split(Constants.COMMA);
//            return the first value (Mill Name)
            return values[0];
        }
        // If no comma is found, return the original string
        return millName;
    }


    //Method To parse Commercial Pricing Sheet and populate the details
    public static PricingDetail parseAndPopulateCommercialPricingDetails(Sheet sheet, Set<String> portOfEntries) {

        int errorsBeforeVerifyingHeaders = errorMessageDetailsList.size();

        Map<String, PeriodDetail> PeriodToPeriodDetailsMap = new HashMap<>();


        //Validating if all headers are present or not
        verifyAllHeadersPresent(sheet.getRow(ExcelConstants.COMMERCIAL_HEADERS_ROW), commercialPricingMap, commercialPricingMismatchMap, sheet.getSheetName());

        if (errorMessageDetailsList.size() != errorsBeforeVerifyingHeaders) {
            return null;
        }


        //Validating Headers for Commercial Pricing
        validateHeaders(sheet.getRow(ExcelConstants.COMMERCIAL_HEADERS_ROW), commercialPricingMap, commercialPricingMismatchMap, sheet.getSheetName());
        PricingDetail pricingDetail = new PricingDetail();
        PortDiscounts portDiscounts = new PortDiscounts();
        PriceTierDiscounts priceTierDiscounts = new PriceTierDiscounts();
        VolumeTierDiscounts volumeTierDiscounts = new VolumeTierDiscounts();
        List<Ports> portsList = new ArrayList<>();
        List<PeriodDetail> periodDetailList = new ArrayList<>();
        boolean isPriceTierDiscounts = true;
        boolean isVolumeTierDiscounts = true;
        String weekCriteria = Constants.EMPTY_STRING;
        int i = ExcelConstants.EXCEL_COMMERCIAL_RESPONSE_START_ROW;
        Row row = sheet.getRow(i);
        validateConditionsForCommercialPricingDetails(row, commercialPricingMap, commercialPricingMismatchMap, sheet.getSheetName());
        CommercialPricingParsingRows parsingRows = new CommercialPricingParsingRows();
        //Parsing rows object used for storing row numbers for volume tier, price tier etc..
        parsingRows.setSheetName(sheet.getSheetName());
        parsingRows.setReadType(Constants.EMPTY_STRING);
        parsingRows.setReadDate(Constants.EMPTY_STRING);
        parsingRows.setWeekDay(Constants.EMPTY_STRING);
        // Iterate over each row in the sheet
        while (!isRowEmpty(row)) {
            row = sheet.getRow(i);
            String period = df.formatCellValue(row.getCell(commercialPricingMap.get(ResponseRfpExcelHeaders.TIME_WINDOW_PERIOD.getValue())));

            //validating Period details for same period
            if (!PeriodToPeriodDetailsMap.containsKey(period)) {
                PeriodDetail periodDetail = populatePeriodDetails(sheet.getSheetName(), row, commercialPricingMap, period);
                PeriodToPeriodDetailsMap.put(period, periodDetail);
            } else {
                validateRepeatedPeriodDetails(PeriodToPeriodDetailsMap, sheet.getSheetName(), row, period, commercialPricingMap);
            }

            //Throughout the response in commercial pricing,only same data will be considered for all rows (As default considering initial row of response)
            if (row.getRowNum() == ExcelConstants.EXCEL_COMMERCIAL_RESPONSE_START_ROW) {
                //checking price-Tier and Volume-Tier Discount is Opted or Not
                isPriceTierDiscounts = checkPriceTierDiscountOptedOrNot(row, isPriceTierDiscounts, priceTierDiscounts);
                isVolumeTierDiscounts = checkVolumeTierDiscountOptedOrNot(row, isVolumeTierDiscounts, volumeTierDiscounts);
                //parsing and populating basic Commercial pricing Details
                parseBasicPricingDetails(pricingDetail, row, sheet, parsingRows, isVolumeTierDiscounts);
                if (Boolean.FALSE.equals(Utils.checkIfStringIsNullOrEmpty(df.formatCellValue(row.getCell(commercialPricingMap.get(ResponseRfpExcelHeaders.WEEK_DAY_OF_MONTH.getValue()))))) && validateCellFormat(sheet.getSheetName(), row.getCell(commercialPricingMap.get(ResponseRfpExcelHeaders.WEEK_DAY_OF_MONTH.getValue())), Constants.SPACE, ExcelConstants.WEEK_CRITERIA_AND_DAY_FORMAT, 2)) {
                    weekCriteria = df.formatCellValue(row.getCell(commercialPricingMap.get(ResponseRfpExcelHeaders.WEEK_DAY_OF_MONTH.getValue()))).split(Constants.SPACE)[0] + Constants.SPACE + Constants.WEEK_OF_MONTH_STRING;
                    parsingRows.setWeekDay(df.formatCellValue(row.getCell(commercialPricingMap.get(ResponseRfpExcelHeaders.WEEK_DAY_OF_MONTH.getValue()))).split(Constants.SPACE)[1]);
                }
                //parsing and populating Other Discount Allowances
                parseOtherDiscountAllowances(pricingDetail, row);
            }
            //Parsing All rows start from commercial sheet response start row
            //To parse Port Discounts(Port Rebate)
            if (Boolean.FALSE.equals(Utils.checkIfStringIsNullOrEmpty(df.formatCellValue(row.getCell(commercialPricingMap.get(ResponseRfpExcelHeaders.PORT_OF_ENTRY.getValue())))))) {
                portsList.add(parsePortNameAndPortRebateDiscountDetails(row, portOfEntries));
            }
            if (pricingDetail.getMechanism_basis() != null && pricingDetail.getMechanism_basis().equals(Constants.MECHANISMS.OTHER.getValue())) {
                // TODO:: custom type of mechanism ::
                // Parsing details of Tier Based Pricing Discount
                populatePriceAndVolumeTierDiscounts(row, isPriceTierDiscounts, priceTierDiscounts, isVolumeTierDiscounts, volumeTierDiscounts, sheet.getSheetName(), parsingRows);
            } else {
                // Parsing details of Mechanism based Details
                populateMechanismBasedDetails(periodDetailList, row, pricingDetail.getMechanism_basis(), weekCriteria, parsingRows);
                // Parsing details of Tier Based Pricing Discount
                populatePriceAndVolumeTierDiscounts(row, isPriceTierDiscounts, priceTierDiscounts, isVolumeTierDiscounts, volumeTierDiscounts, sheet.getSheetName(), parsingRows);
            }
            i++;
        }
        List<String> ports = portsList.stream().map(Ports::getPort).collect(Collectors.toList());
        if (portOfEntries != null && !portOfEntries.isEmpty() && !portsList.isEmpty() && portOfEntries.size() > (new HashSet<>(ports)).size()) {
            errorMessageDetailsList.add(new ErrorMessageDetails(ExcelConstants.ResponseRfpExcelSheetNames.COMMERCIAL_PRICING.getSheetName(), null, portOfEntries.toString(), ports.toString(), ResponseRfpExcelHeaders.PORT_OF_ENTRY.getValue(), ErrorMessages.Messages.PORT_OF_ENTRY_MISMATCH.getMessage()));
        }
        portDiscounts.setPorts(portsList);
        portDiscounts.setDiscount_uom(Constants.UNIT_OF_MEASURE);
        pricingDetail.setDiscount_uom(Constants.UNIT_OF_MEASURE);
        pricingDetail.setCeil_floor_uom(Constants.UNIT_OF_MEASURE);
        /* if (pricingDetail.getDiscounts_allowances() != null) {
            pricingDetail.getDiscounts_allowances().setPort_discounts(portDiscounts);
            pricingDetail.getDiscounts_allowances().setPrice_tier_discounts(priceTierDiscounts);
            pricingDetail.getDiscounts_allowances().setVolume_tier_discounts(volumeTierDiscounts);
            pricingDetail.getDiscounts_allowances().setPort_discounts(portDiscounts);
        } */
        pricingDetail.setPeriod_detail(periodDetailList);
        return pricingDetail;
    }

    private static boolean validateCellFormat(String sheetName, Cell cell, String separator, String expectedFormat, Integer expectedLength) {
        String s = df.formatCellValue(cell);
        String[] splittedArray = s.split(separator);
        if (splittedArray == null || splittedArray.length < expectedLength) {
            String errorMessage = MessageFormat.format(ErrorMessages.Messages.INVALID_FORMAT_ERROR.getMessage(), s, expectedFormat);
            errorMessageDetailsList.add(new ErrorMessageDetails(sheetName, new CellReference(cell.getRowIndex(), cell.getColumnIndex()).formatAsString(false), expectedFormat, s, null, errorMessage));
            return false;
        }
        return true;
    }

    //Method to parse and populate Price and Volume Tier Discounts :: Commercial Pricing Sheet
    private static void populatePriceAndVolumeTierDiscounts(Row row, boolean isPriceTierDiscounts, PriceTierDiscounts priceTierDiscounts, boolean isVolumeTierDiscounts, VolumeTierDiscounts volumeTierDiscounts, String sheetName, CommercialPricingParsingRows parsingRows) {
        if (isPriceTierDiscounts && parsingRows.getPricingTierDiscountRows() > 0) {
            validatePriceTierMandatoryFields(row, sheetName);
            //if Price Tier Discount is Yes
            parsePriceTierDiscounts(priceTierDiscounts, row);
            parsingRows.setPricingTierDiscountRows(parsingRows.getPricingTierDiscountRows());
        } else if (priceTierDiscounts.getPrice_tiers() == null) {
            priceTierDiscounts.setPrice_tiers(new ArrayList<>());
        }
        if (isVolumeTierDiscounts && parsingRows.getVolumeTierDiscountRows() > 0) {
            validateVolumeTierMandatoryFields(row, sheetName);
            //if Volume Tier Discount is Yes
            parseVolumeTierDiscounts(volumeTierDiscounts, row);
            parsingRows.setVolumeTierDiscountRows(parsingRows.getVolumeTierDiscountRows());
        } else if (volumeTierDiscounts.getVolume_tiers() == null) {
            volumeTierDiscounts.setVolume_tiers(new ArrayList<>());
        }
    }

    //Method to parse and populate Mechanism Based Details of Pricing :: :: Commercial Pricing Sheet
    private static void populateMechanismBasedDetails(List<PeriodDetail> periodDetailList, Row row, String mechanismBasisType, String weekCriteria, CommercialPricingParsingRows parsingRows) {
        //Parsing index Details and Fixed-price Details
        String sheetName = parsingRows.getSheetName();

        // Validating Period
        validatePeriodPricingDetails(row, commercialPricingMap.get(ResponseRfpExcelHeaders.TIME_WINDOW_PERIOD.getValue()), sheetName);

        if (parsingRows.getPricingMechanismRows() > 0) {
            FixedPriceDetails fixedPriceDetails = new FixedPriceDetails();
            validateMechanismBasisMandatoryFields(mechanismBasisType, row, sheetName);
            isNonEmptyString(row, commercialPricingMap.get(ResponseRfpExcelHeaders.TIME_WINDOW_PERIOD.getValue()), sheetName, ResponseRfpExcelHeaders.TIME_WINDOW_PERIOD.getValue());
            //parse and populating Fixed price Details
            if (mechanismBasisType.equalsIgnoreCase(Constants.MECHANISMS.MOVEMENT.getValue()) || mechanismBasisType.equalsIgnoreCase(Constants.MECHANISMS.HYBRID.getValue())) {
                fixedPriceDetails = parseFixedPriceDetails(row, periodDetailList, mechanismBasisType);
            }
            //Period details if Mechanism is MOVEMENT Based
            if (mechanismBasisType.equalsIgnoreCase(Constants.MECHANISMS.MOVEMENT.getValue())) {
                parseAndPopulateMovementBasedPriceDetails(periodDetailList, fixedPriceDetails, row, parsingRows);
            }
            //Period details if Mechanism is Hybrid or Index Based :: Commercial Pricing Sheet
            if (mechanismBasisType.equalsIgnoreCase(Constants.MECHANISMS.INDEX.getValue()) || mechanismBasisType.equalsIgnoreCase(Constants.MECHANISMS.HYBRID.getValue())) {
                IndexDetails indexDetails = parseMechanismBasedIndexDetailsParsing(row, weekCriteria, parsingRows);
                parseAndPopulateIndexOrHybridPricingDetails(mechanismBasisType, periodDetailList, indexDetails, fixedPriceDetails, row, parsingRows);
            }
            parsingRows.setPricingMechanismRows(parsingRows.getPricingMechanismRows());
        }
    }

    private static void validateMechanismBasisMandatoryFields(String mechanismBasisType, Row row, String sheetName) {
        if (mechanismBasisType.equals(Constants.MECHANISMS.MOVEMENT.getValue())) {
            isNonEmptyString(row, commercialPricingMap.get(ResponseRfpExcelHeaders.FIXED_PRICE.getValue()), ExcelConstants.ResponseRfpExcelSheetNames.COMMERCIAL_PRICING.getSheetName(), ResponseRfpExcelHeaders.FIXED_PRICE.getValue());
            isNonEmptyString(row, commercialPricingMap.get(ResponseRfpExcelHeaders.FIXED_PRICE_WEIGHTAGE.getValue()), ExcelConstants.ResponseRfpExcelSheetNames.COMMERCIAL_PRICING.getSheetName(), ResponseRfpExcelHeaders.FIXED_PRICE_WEIGHTAGE.getValue());
        }
        if (mechanismBasisType.equals(Constants.MECHANISMS.HYBRID.getValue())) {
            isNonEmptyString(row, commercialPricingMap.get(ResponseRfpExcelHeaders.INDEX_NAME.getValue()), sheetName, ResponseRfpExcelHeaders.INDEX_NAME.getValue());
            isNonEmptyStringForCommercialSheet(row, commercialPricingMap.get(ResponseRfpExcelHeaders.INDEX_PERCENTAGE_DISCOUNT_APPLIED.getValue()), sheetName, ResponseRfpExcelHeaders.INDEX_PERCENTAGE_DISCOUNT_APPLIED.getValue());
            isNonEmptyStringForCommercialSheet(row, commercialPricingMismatchMap.get(ResponseRfpExcelHeaders.INDEX_WEIGHTAGE.getValue()), sheetName, ResponseRfpExcelHeaders.INDEX_WEIGHTAGE.getValue());
//            isNonEmptyString(row, commercialPricingMap.get(ResponseRfpExcelHeaders.FIXED_PRICE.getValue()), sheetName, ResponseRfpExcelHeaders.FIXED_PRICE.getValue());
//            isNonEmptyString(row, commercialPricingMap.get(ResponseRfpExcelHeaders.FIXED_PRICE_WEIGHTAGE.getValue()), sheetName, ResponseRfpExcelHeaders.FIXED_PRICE_WEIGHTAGE.getValue());
        }
        if (mechanismBasisType.equals(Constants.MECHANISMS.INDEX.getValue())) {
            isNonEmptyString(row, commercialPricingMap.get(ResponseRfpExcelHeaders.INDEX_NAME.getValue()), sheetName, ResponseRfpExcelHeaders.INDEX_NAME.getValue());
            isNonEmptyStringForCommercialSheet(row, commercialPricingMap.get(ResponseRfpExcelHeaders.INDEX_PERCENTAGE_DISCOUNT_APPLIED.getValue()), sheetName, ResponseRfpExcelHeaders.INDEX_PERCENTAGE_DISCOUNT_APPLIED.getValue());
            isNonEmptyStringForCommercialSheet(row, commercialPricingMismatchMap.get(ResponseRfpExcelHeaders.INDEX_WEIGHTAGE.getValue()), sheetName, ResponseRfpExcelHeaders.INDEX_WEIGHTAGE.getValue());
        }
    }

    //Parsing and Populating Price Tier Discount Details :: Commercial Pricing Sheet
    private static void parsePriceTierDiscounts(PriceTierDiscounts priceTierDiscounts, Row row) {
        PriceTiers priceTiers = null;

        Cell cell = row.getCell(commercialPricingMap.get(ResponseRfpExcelHeaders.PRICE_TIER_MIN_AND_MAX.getValue()));
        String priceTiersRangeValue = df.formatCellValue(cell);

        if (checkIsEmptyStringAndDataFormat(row, ResponseRfpExcelHeaders.PRICE_TIER_MIN_AND_MAX.getValue(), priceTiersRangeValue.contains(Constants.HYPHEN) ? ExcelConstants.MIN_MAX_REGEX_DOLLER_OR_NUMBER_WITH_DOLLER : ExcelConstants.DOLLER_REGEX, ExcelConstants.ResponseRfpExcelSheetNames.COMMERCIAL_PRICING.getSheetName(), ExcelConstants.MIN_MAX_EXAMPLE, commercialPricingMap, Boolean.TRUE, Boolean.FALSE)) {
            String priceTierRange = df.formatCellValue(row.getCell(commercialPricingMap.get(ResponseRfpExcelHeaders.PRICE_TIER_MIN_AND_MAX.getValue())));
            priceTiers = new PriceTiers();
//            priceTiers.setPriceTierType(df.formatCellValue(row.getCell(commercialPricingMap.get(ResponseRfpExcelHeaders.PRICE_TIER_CATEGORY.getValue()))));
            if (!priceTierRange.contains(Constants.HYPHEN)) {
                priceTiers.setTier_low((long) (Double.parseDouble(getNumericString(priceTierRange, Constants.DOLLAR))));
            } else {
                priceTiers.setTier_low((long) (Double.parseDouble(getNumericString(priceTierRange.split(Constants.HYPHEN)[0], Constants.DOLLAR).trim())));
                priceTiers.setTier_high((long) (Double.parseDouble(getNumericString(priceTierRange.split(Constants.HYPHEN)[1], Constants.DOLLAR).trim())));
            }
            if (priceTiers.getTier_high() != null) {
                //TODO : Uncomment for old sheet
                //check if min is less than Max
//                validateMinMaxPrice(priceTiers.getTier_low(), priceTiers.getTier_high(), row, ResponseRfpExcelHeaders.PRICE_TIER_MIN_AND_MAX.getValue());
            }
            if (checkIsEmptyStringAndDataFormat(row, ResponseRfpExcelHeaders.TIER_ADDITIONAL_DISCOUNT.getValue(), ExcelConstants.PERCENT_OR_DOLLAR_REGEX, ExcelConstants.ResponseRfpExcelSheetNames.COMMERCIAL_PRICING.getSheetName(), ExcelConstants.PERCENT_OR_DOLLAR, commercialPricingMap, Boolean.FALSE, Boolean.FALSE)) {
                String additionalDiscount = df.formatCellValue(row.getCell(commercialPricingMap.get(ResponseRfpExcelHeaders.TIER_ADDITIONAL_DISCOUNT.getValue())));
                if (additionalDiscount.contains(Constants.PERCENT)) {
                    priceTiers.setDiscount_type(Constants.PERCENT_STRING);
                    //TODO : Uncomment for Old sheet
//                    parsePriceTierDiscountPercentValue(additionalDiscount, row, priceTiers);
                } else if (additionalDiscount.contains(Constants.DOLLAR)) {
                    priceTiers.setDiscount_val((long) Double.parseDouble(getNumericString(additionalDiscount, Constants.DOLLAR)));
                    priceTiers.setDiscount_type(Constants.VALUE_STRING);
                }
            }
        }
        if (priceTiers != null) {
            if (priceTierDiscounts.getPrice_tiers() != null) {
                List<PriceTiers> priceTiersList = priceTierDiscounts.getPrice_tiers();
                //validating min-max values inclusion range is not acceptable
                //TODO : Uncomment for Old sheet
//                validateMinMaxInclusionRangeForPriceTiers(priceTiersList, priceTiers, row);
                priceTierDiscounts.getPrice_tiers().add(priceTiers);
                priceTierDiscounts.setIs_tier_based_discount(true);
            } else {
                priceTierDiscounts.setIs_tier_based_discount(true);
                priceTierDiscounts.setTier_uom(Constants.UOM_ADMT);
                priceTierDiscounts.setPrice_tiers(new ArrayList<>());
                priceTierDiscounts.getPrice_tiers().add(priceTiers);
            }

        }

//        else {
//            priceTierDiscounts.setPrice_tiers((List<PriceTiers>) new PriceTiers());
//        }
    }

    private static void validatePriceTierMandatoryFields(Row row, String sheetName) {
        isNonEmptyString(row, commercialPricingMap.get(ResponseRfpExcelHeaders.PRICE_TIER_CATEGORY.getValue()), sheetName, ResponseRfpExcelHeaders.PRICE_TIER_CATEGORY.getValue());
        isNonEmptyString(row, commercialPricingMap.get(ResponseRfpExcelHeaders.PRICE_TIER_MIN_AND_MAX.getValue()), sheetName, ResponseRfpExcelHeaders.PRICE_TIER_MIN_AND_MAX.getValue());
        isNonEmptyString(row, commercialPricingMap.get(ResponseRfpExcelHeaders.TIER_ADDITIONAL_DISCOUNT.getValue()), sheetName, ResponseRfpExcelHeaders.TIER_ADDITIONAL_DISCOUNT.getValue());
    }

    //Method to parse and populate Ports and Rebate discount Details :: Commercial Pricing Sheet
    private static Ports parsePortNameAndPortRebateDiscountDetails(Row row, Set<String> portOfEntries) {
        Ports port = new Ports();
        if (portOfEntries != null && !portOfEntries.isEmpty()) {
            if (Boolean.FALSE.equals(Utils.checkIfStringIsNullOrEmpty(df.formatCellValue(row.getCell(commercialPricingMap.get(ResponseRfpExcelHeaders.PORT_OF_ENTRY.getValue())))))) {
                if (portOfEntries.contains(df.formatCellValue(row.getCell(commercialPricingMap.get(ResponseRfpExcelHeaders.PORT_OF_ENTRY.getValue()))))) {
                    port.setPort(df.formatCellValue(row.getCell(commercialPricingMap.get(ResponseRfpExcelHeaders.PORT_OF_ENTRY.getValue()))));
                    if (Boolean.FALSE.equals(Utils.checkIfStringIsNullOrEmpty(df.formatCellValue(row.getCell(commercialPricingMap.get(ResponseRfpExcelHeaders.PORT_REBATE.getValue())))))) {
                        if (checkIsEmptyStringAndDataFormat(row, ResponseRfpExcelHeaders.PORT_REBATE.getValue(), ExcelConstants.DOLLER_REGEX, ExcelConstants.ResponseRfpExcelSheetNames.COMMERCIAL_PRICING.getSheetName(), ExcelConstants.DOLLER, commercialPricingMap, Boolean.FALSE, Boolean.FALSE)) {
                            port.setDiscount_val(Utils.getLongDoller(df.formatCellValue(row.getCell(commercialPricingMap.get(ResponseRfpExcelHeaders.PORT_REBATE.getValue())))));
                        }
                    } else {
                        errorMessageDetailsList.add(new ErrorMessageDetails(ExcelConstants.ResponseRfpExcelSheetNames.COMMERCIAL_PRICING.getSheetName(), new CellReference(row.getRowNum(), commercialPricingMap.get(ResponseRfpExcelHeaders.PORT_REBATE.getValue())).formatAsString(false), ExcelConstants.DECIMAL, null, ResponseRfpExcelHeaders.PORT_REBATE.getValue(), MessageFormat.format(ErrorMessages.Messages.MANDATORY_FIELD_MISSING_ERROR.getMessage(), ResponseRfpExcelHeaders.PORT_REBATE.getValue())));
                    }
                } else {
                    errorMessageDetailsList.add(new ErrorMessageDetails(ExcelConstants.ResponseRfpExcelSheetNames.COMMERCIAL_PRICING.getSheetName(), new CellReference(row.getRowNum(), commercialPricingMap.get(ResponseRfpExcelHeaders.PORT_OF_ENTRY.getValue())).formatAsString(false), portOfEntries.toString(), df.formatCellValue(row.getCell(commercialPricingMap.get(ResponseRfpExcelHeaders.PORT_OF_ENTRY.getValue()))), ResponseRfpExcelHeaders.PORT_OF_ENTRY.getValue(), ErrorMessages.Messages.PORT_OF_ENTRY_MISMATCH.getMessage()));
                }
            }
        }
        return port;
    }

    //method to parse and populate Period details if Mechanism is Hybrid or Index Based :: Commercial Pricing Sheet
    private static void parseAndPopulateIndexOrHybridPricingDetails(String mechanismType, List<PeriodDetail> periodDetailList, IndexDetails indexDetails, FixedPriceDetails fixedPriceDetails, Row row, CommercialPricingParsingRows parsingRows) {
        int f = 0;
        if (!periodDetailList.isEmpty()) {
            for (PeriodDetail periodDetail1 : periodDetailList) {
                // if period already exists
                Cell timeWindowPeriod = row.getCell(commercialPricingMap.get(ResponseRfpExcelHeaders.TIME_WINDOW_PERIOD.getValue()));
                if (Boolean.FALSE.equals(Utils.checkIfStringIsNullOrEmpty(df.formatCellValue(timeWindowPeriod)))) {
                    if (checkIsEmptyStringAndDataFormat(row, ResponseRfpExcelHeaders.TIME_WINDOW_PERIOD.getValue(), ExcelConstants.ALPHA_NUMERIC_REGEX, ExcelConstants.ResponseRfpExcelSheetNames.COMMERCIAL_PRICING.getSheetName(), ExcelConstants.ALPHA_NUMERIC, commercialPricingMap, Boolean.TRUE, Boolean.FALSE)) {
                        if (periodDetail1.getPeriod().equalsIgnoreCase(df.formatCellValue(row.getCell(commercialPricingMap.get(ResponseRfpExcelHeaders.TIME_WINDOW_PERIOD.getValue()))))) {
                            checkWeightageSumValidation(periodDetail1.getIndex_details(), indexDetails, row, periodDetail1.getPeriod());
                            periodDetail1.getIndex_details().add(indexDetails);
                            f = 1;
                            break;
                        }
                    }
                }
            }
        }
        //if Period is new
        if (f == 0) {
            parseNewPeriodDetailsForCommercialSheet(indexDetails, periodDetailList, parsingRows, row, mechanismType, fixedPriceDetails);
        }
    }

    //Method to parse and populate Movement Mechanism Pricing Details :: Commercial pricing Sheet
    private static void parseAndPopulateMovementBasedPriceDetails(List<PeriodDetail> periodDetailList, FixedPriceDetails fixedPriceDetails, Row row, CommercialPricingParsingRows parsingRows) {
        PeriodDetail periodDetail1 = new PeriodDetail();
        if (fixedPriceDetails != null) {
            periodDetail1.setFixed_price_details(fixedPriceDetails);
            Cell timeWindowPeriod = row.getCell(commercialPricingMap.get(ResponseRfpExcelHeaders.TIME_WINDOW_PERIOD.getValue()));
            if (Boolean.FALSE.equals(Utils.checkIfStringIsNullOrEmpty(df.formatCellValue(timeWindowPeriod)))) {
                if (checkIsEmptyStringAndDataFormat(row, ResponseRfpExcelHeaders.TIME_WINDOW_PERIOD.getValue(), ExcelConstants.ALPHA_NUMERIC_REGEX, ExcelConstants.ResponseRfpExcelSheetNames.COMMERCIAL_PRICING.getSheetName(), ExcelConstants.ALPHA_NUMERIC, commercialPricingMap, Boolean.TRUE, Boolean.FALSE)) {
                    periodDetail1.setPeriod(df.formatCellValue(row.getCell(commercialPricingMap.get(ResponseRfpExcelHeaders.TIME_WINDOW_PERIOD.getValue()))));
                    int lengthOfPeriod = periodDetail1.getPeriod().length();
//                String periodNum = (lengthOfPeriod <= 3) ? periodDetail1.getPeriod().substring(1) : periodDetail1.getPeriod().substring(5);
                    //setting period_num in ascending number order
                    periodDetail1.setPeriod_num((long) periodDetailList.size() + 1);
                    periodDetailList.add(periodDetail1);
                }
            }
        } else {
            periodDetail1.setFixed_price_details(new FixedPriceDetails());
        }
    }

    //method to parse and populate Fixed-Price details of Hybrid and Movement based Mechanisms :: Commercial Pricing Sheet
    private static FixedPriceDetails parseFixedPriceDetails(Row row, List<PeriodDetail> periodDetailList, String mechanismBasisType) {
        FixedPriceDetails fixedPriceDetails = new FixedPriceDetails();
        int f = 0;
        if (Constants.MECHANISMS.MOVEMENT.getValue().equals(mechanismBasisType)) {
            if (checkIsEmptyStringAndDataFormat(row, ResponseRfpExcelHeaders.FIXED_PRICE.getValue(), ExcelConstants.DECIMAL_REGEX, ExcelConstants.ResponseRfpExcelSheetNames.COMMERCIAL_PRICING.getSheetName(), ExcelConstants.DECIMAL, commercialPricingMap, Boolean.TRUE, Boolean.FALSE)) {
                fixedPriceDetails.setFixed_price_value((float) row.getCell(commercialPricingMap.get(ResponseRfpExcelHeaders.FIXED_PRICE.getValue())).getNumericCellValue());
            }
            if (checkIsEmptyStringAndDataFormat(row, ResponseRfpExcelHeaders.FIXED_PRICE_WEIGHTAGE.getValue(), ExcelConstants.PERCENT_OR_DOLLAR_REGEX, ExcelConstants.ResponseRfpExcelSheetNames.COMMERCIAL_PRICING.getSheetName(), ExcelConstants.PERCENTAGE, commercialPricingMap, Boolean.TRUE, Boolean.FALSE)) {
                if (validateRegex(df.formatCellValue(row.getCell(commercialPricingMap.get(ResponseRfpExcelHeaders.FIXED_PRICE_WEIGHTAGE.getValue()))), ExcelConstants.PERCENTAGE_REGEX)) {
                    fixedPriceDetails.setWeightage_pct(Long.valueOf(df.formatCellValue(row.getCell(commercialPricingMap.get(ResponseRfpExcelHeaders.FIXED_PRICE_WEIGHTAGE.getValue()))).replace(Constants.PERCENT, Constants.EMPTY_STRING)));
                } else {
                    String errorMessage = MessageFormat.format(ErrorMessages.Messages.PERCENTAGE_OVER_100.getMessage(), df.formatCellValue(row.getCell(commercialPricingMap.get(ResponseRfpExcelHeaders.FIXED_PRICE_WEIGHTAGE.getValue()))));
                    errorMessageDetailsList.add(new ErrorMessageDetails(ExcelConstants.ResponseRfpExcelSheetNames.COMMERCIAL_PRICING.getSheetName(), new CellReference(row.getRowNum(), commercialPricingMap.get(ResponseRfpExcelHeaders.FIXED_PRICE_WEIGHTAGE.getValue())).formatAsString(false), null, null, ResponseRfpExcelHeaders.FIXED_PRICE_WEIGHTAGE.getValue(), errorMessage));
                }
            }
        } else {
            parseFixedPriceDetailsForHybridMechanism(fixedPriceDetails, periodDetailList, row, f);
        }
        return fixedPriceDetails;
    }

    //Method to parse and populate Mechanism Based Index Details :: Commercial Pricing Sheet
    private static IndexDetails parseMechanismBasedIndexDetailsParsing(Row row, String weekCriteria, CommercialPricingParsingRows parsingRows) {
        IndexDetails indexDetails = new IndexDetails();
        if (Boolean.FALSE.equals(Utils.checkIfStringIsNullOrEmpty(df.formatCellValue(row.getCell(commercialPricingMap.get(ResponseRfpExcelHeaders.INDEX_NAME.getValue())))))) {
            indexDetails.setIndex(df.formatCellValue(row.getCell(commercialPricingMap.get(ResponseRfpExcelHeaders.INDEX_NAME.getValue()))));
        }
        indexDetails.setRead_type(parsingRows.getReadType());
        indexDetails.setRead_date(parsingRows.getReadDate());
        if (weekCriteria != null || !weekCriteria.trim().isEmpty()) {
            indexDetails.setRead_week_criteria(weekCriteria.split(Constants.SPACE)[0]);
        }
        indexDetails.setRead_day(parsingRows.getWeekDay());
        if (checkIsEmptyStringAndDataFormat(row, ResponseRfpExcelHeaders.INDEX_PERCENTAGE_DISCOUNT_APPLIED.getValue(), ExcelConstants.PERCENT_OR_DOLLAR_REGEX, ExcelConstants.ResponseRfpExcelSheetNames.COMMERCIAL_PRICING.getSheetName(), ExcelConstants.PERCENTAGE, commercialPricingMap, Boolean.TRUE, Boolean.FALSE)) {
            if (validateRegex(df.formatCellValue(row.getCell(commercialPricingMap.get(ResponseRfpExcelHeaders.INDEX_PERCENTAGE_DISCOUNT_APPLIED.getValue()))).replaceAll(Constants.SPACE, Constants.EMPTY_STRING), ExcelConstants.PERCENTAGE_REGEX)) {
                indexDetails.setDiscount_pct(Float.valueOf(df.formatCellValue(row.getCell(commercialPricingMap.get(ResponseRfpExcelHeaders.INDEX_PERCENTAGE_DISCOUNT_APPLIED.getValue()))).replace(Constants.PERCENT, Constants.EMPTY_STRING).replaceAll(Constants.SPACE, Constants.EMPTY_STRING)));
            } else {
                indexDetails.setDiscount_pct(Float.valueOf(df.formatCellValue(row.getCell(commercialPricingMap.get(ResponseRfpExcelHeaders.INDEX_PERCENTAGE_DISCOUNT_APPLIED.getValue()))).replace(Constants.PERCENT, Constants.EMPTY_STRING).replaceAll(Constants.SPACE, Constants.EMPTY_STRING)));
                String errorMessage = MessageFormat.format(ErrorMessages.Messages.PERCENTAGE_OVER_100.getMessage(), df.formatCellValue(row.getCell(commercialPricingMap.get(ResponseRfpExcelHeaders.INDEX_PERCENTAGE_DISCOUNT_APPLIED.getValue()))));
                errorMessageDetailsList.add(new ErrorMessageDetails(ExcelConstants.ResponseRfpExcelSheetNames.COMMERCIAL_PRICING.getSheetName(), new CellReference(row.getRowNum(), commercialPricingMap.get(ResponseRfpExcelHeaders.INDEX_PERCENTAGE_DISCOUNT_APPLIED.getValue())).formatAsString(false), null, null, ResponseRfpExcelHeaders.INDEX_PERCENTAGE_DISCOUNT_APPLIED.getValue(), errorMessage));
            }
        }

        //checking NotNull for additional_adjustment
        Cell additionalAdjustmentCell = row.getCell(commercialPricingMap.get(ResponseRfpExcelHeaders.ADDITIONAL_ADJUSTMENT.getValue()));
        if (Boolean.FALSE.equals(Utils.checkIfStringIsNullOrEmpty(df.formatCellValue(additionalAdjustmentCell)))) {
            if (checkIsEmptyStringAndDataFormat(row, ResponseRfpExcelHeaders.ADDITIONAL_ADJUSTMENT.getValue(), ExcelConstants.DOLLER_REGEX, ExcelConstants.ResponseRfpExcelSheetNames.COMMERCIAL_PRICING.getSheetName(), ExcelConstants.DOLLER, commercialPricingMap, Boolean.FALSE, Boolean.FALSE)) {
                indexDetails.setAdditional_adjustment(Utils.getFloatDoller(df.formatCellValue(row.getCell(commercialPricingMap.get(ResponseRfpExcelHeaders.ADDITIONAL_ADJUSTMENT.getValue())))));
            }
        }
        if (checkIsEmptyStringAndDataFormat(row, ResponseRfpExcelHeaders.INDEX_WEIGHTAGE.getValue(), ExcelConstants.PERCENT_OR_DOLLAR_REGEX, ExcelConstants.ResponseRfpExcelSheetNames.COMMERCIAL_PRICING.getSheetName(), ExcelConstants.PERCENTAGE, commercialPricingMismatchMap, Boolean.TRUE, Boolean.FALSE)) {
            if (validateRegex(df.formatCellValue(row.getCell(commercialPricingMismatchMap.get(ResponseRfpExcelHeaders.INDEX_WEIGHTAGE.getValue()))), ExcelConstants.PERCENTAGE_REGEX)) {
                indexDetails.setWeightage_pct(Long.valueOf(df.formatCellValue(row.getCell(ExcelConstants.COMMERCIAL_PRICING_MISMATCH_MAP.get(ResponseRfpExcelHeaders.INDEX_WEIGHTAGE.getValue()))).replace(Constants.PERCENT, Constants.EMPTY_STRING)));
            } else {
                indexDetails.setWeightage_pct(Long.valueOf(df.formatCellValue(row.getCell(ExcelConstants.COMMERCIAL_PRICING_MISMATCH_MAP.get(ResponseRfpExcelHeaders.INDEX_WEIGHTAGE.getValue()))).replace(Constants.PERCENT, Constants.EMPTY_STRING)));
                String errorMessage = MessageFormat.format(ErrorMessages.Messages.PERCENTAGE_OVER_100.getMessage(), df.formatCellValue(row.getCell(commercialPricingMismatchMap.get(ResponseRfpExcelHeaders.INDEX_WEIGHTAGE.getValue()))));
                errorMessageDetailsList.add(new ErrorMessageDetails(ExcelConstants.ResponseRfpExcelSheetNames.COMMERCIAL_PRICING.getSheetName(), new CellReference(row.getRowNum(), commercialPricingMismatchMap.get(ResponseRfpExcelHeaders.INDEX_WEIGHTAGE.getValue())).formatAsString(false), null, null, ResponseRfpExcelHeaders.INDEX_WEIGHTAGE.getValue(), errorMessage));
            }
        }
//        else{
//            String error = MessageFormat.format(ErrorMessages.MANDATORY_FIELD_MISSING_ERROR,ResponseRfpExcelHeaders.INDEX_WEIGHTAGE.getValue(),ExcelConstants.ResponseRfpExcelSheetNames.COMMERCIAL_PRICING.getSheetName(),new CellReference(row.getRowNum(),commercialPricingMismatchMap.get(ResponseRfpExcelHeaders.INDEX_WEIGHTAGE.getValue())).formatAsString(false));
//            errorMessagesListInExcel.add(error);
//            errorMessageDetailsList.add(new ErrorMessageDetails(ExcelConstants.ResponseRfpExcelSheetNames.COMMERCIAL_PRICING.getSheetName(), new CellReference(row.getRowNum(), commercialPricingMismatchMap.get(ResponseRfpExcelHeaders.INDEX_WEIGHTAGE.getValue())).formatAsString(false), null, null, ResponseRfpExcelHeaders.INDEX_WEIGHTAGE.getValue(), error));
//        }
        return indexDetails;
    }

    //Parsing and Populating Volume Tier Discount Details :: Commercial Pricing Sheet
    private static void parseVolumeTierDiscounts(VolumeTierDiscounts volumeTierDiscounts, Row row) {
        VolumeTiers volumeTiers = null;
        Cell cell = row.getCell(commercialPricingMap.get(ResponseRfpExcelHeaders.VOLUME_TIER_MIN_AND_MAX.getValue()));
        String volumeTiersRangeValue = df.formatCellValue(cell);

        if (checkIsEmptyStringAndDataFormat(row, ResponseRfpExcelHeaders.VOLUME_TIER_MIN_AND_MAX.getValue(), volumeTiersRangeValue.contains(Constants.HYPHEN) ? ExcelConstants.MIN_MAX_REGEX_NUMERIC : ExcelConstants.NUMERIC_CHARACTERS_REGEX, ExcelConstants.ResponseRfpExcelSheetNames.COMMERCIAL_PRICING.getSheetName(), ExcelConstants.MIN_MAX, commercialPricingMap, Boolean.TRUE, Boolean.FALSE)) {
//            if (Boolean.FALSE.equals(Utils.checkIfStringIsNullOrEmpty(df.formatCellValue(row.getCell(commercialPricingMap.get(ResponseRfpExcelHeaders.VOLUME_TIER_MIN_AND_MAX.getValue()))))) && validateCellFormat(ExcelConstants.ResponseRfpExcelSheetNames.COMMERCIAL_PRICING.getSheetName(), row.getCell(commercialPricingMap.get(ResponseRfpExcelHeaders.VOLUME_TIER_MIN_AND_MAX.getValue())), Constants.HYPHEN, ExcelConstants.VOLUME_TIER_RANGE_FORMAT, 2)) {
            volumeTiers = new VolumeTiers();
            String volumeTierRange = df.formatCellValue(row.getCell(commercialPricingMap.get(ResponseRfpExcelHeaders.VOLUME_TIER_MIN_AND_MAX.getValue())));

            if (!volumeTierRange.contains(Constants.HYPHEN)) {
                volumeTiers.setTier_low((long) (Double.parseDouble(getNumericString(volumeTierRange, Constants.COMMA))));
            } else {
                volumeTiers.setTier_low((long) Double.parseDouble(getNumericString(volumeTierRange, Constants.COMMA).split(Constants.HYPHEN)[0].trim()));
                volumeTiers.setTier_high((long) Double.parseDouble(getNumericString(volumeTierRange, Constants.COMMA).split(Constants.HYPHEN)[1].trim()));
            }

            if (volumeTiers.getTier_high() != null)
                //TODO : Uncomment for old sheet
//                    validateMinMaxPrice(volumeTiers.getTier_low(), volumeTiers.getTier_high(), row, ResponseRfpExcelHeaders.VOLUME_TIER_MIN_AND_MAX.getValue());
                if (Boolean.FALSE.equals(Utils.checkIfStringIsNullOrEmpty(df.formatCellValue(row.getCell(commercialPricingMap.get(ResponseRfpExcelHeaders.VOLUME_DISCOUNT_APPLIED_TO_ORDERS_FROM_NEXT.getValue())))))) {
                    volumeTiers.setDiscount_appl_next_period(df.formatCellValue(row.getCell(commercialPricingMap.get(ResponseRfpExcelHeaders.VOLUME_DISCOUNT_APPLIED_TO_ORDERS_FROM_NEXT.getValue()))));
                }
            parseVolumeTierDiscountBasedOnType(volumeTiers, row);
            if (volumeTierDiscounts.getVolume_tiers() != null) {
                List<VolumeTiers> volumeTiersList = volumeTierDiscounts.getVolume_tiers();
                //TODO : Uncomment for Old sheet
//                    validateMinMaxInclusionRangeForVolumeTiers(volumeTiersList, volumeTiers, row);
                volumeTierDiscounts.getVolume_tiers().add(volumeTiers);
                volumeTierDiscounts.setIs_volume_based_discount(Boolean.TRUE);

            } else {
                volumeTierDiscounts.setTier_uom(Constants.UOM_ADMT);
                volumeTierDiscounts.setIs_volume_based_discount(Boolean.TRUE);
                volumeTierDiscounts.setVolume_tiers(new ArrayList<>());
                volumeTierDiscounts.getVolume_tiers().add(volumeTiers);
            }
        }
//        }
    }

    private static void validateVolumeTierMandatoryFields(Row row, String sheetName) {
        isNonEmptyString(row, commercialPricingMap.get(ResponseRfpExcelHeaders.VOLUME_TIER_MIN_AND_MAX.getValue()), sheetName, ResponseRfpExcelHeaders.VOLUME_TIER_MIN_AND_MAX.getValue());
        isNonEmptyString(row, commercialPricingMismatchMap.get(ResponseRfpExcelHeaders.VOLUME_TIER_ADDITIONAL_DISCOUNT.getValue()), sheetName, ResponseRfpExcelHeaders.VOLUME_TIER_ADDITIONAL_DISCOUNT.getValue());
        isNonEmptyString(row, commercialPricingMap.get(ResponseRfpExcelHeaders.VOLUME_DISCOUNT_APPLIED_TO_ORDERS_FROM_NEXT.getValue()), sheetName, ResponseRfpExcelHeaders.VOLUME_DISCOUNT_APPLIED_TO_ORDERS_FROM_NEXT.getValue());
    }

    //Method to populate Other Discount Allowances :: Commercial Pricing Sheet
    private static void parseOtherDiscountAllowances(PricingDetail pricingDetail, Row row) {
        InlandTransAllowances inlandTransAllowances = new InlandTransAllowances();
        GoodwillDiscounts goodwillDiscounts = new GoodwillDiscounts();
        DiscountsAllowances discountsAllowances = new DiscountsAllowances();
       /* //parsing Other Discount Allowances
        Cell cell = row.getCell(commercialPricingMap.get(ResponseRfpExcelHeaders.INLAND_TRANSPORTATION_ALLOWANCES.getValue()));
        if (Boolean.FALSE.equals(Utils.checkIfStringIsNullOrEmpty(df.formatCellValue(cell)))) {
            if (checkIsEmptyStringAndDataFormat(row, ResponseRfpExcelHeaders.INLAND_TRANSPORTATION_ALLOWANCES.getValue(), ExcelConstants.DOLLER_REGEX, ExcelConstants.ResponseRfpExcelSheetNames.COMMERCIAL_PRICING.getSheetName(), ExcelConstants.DOLLER, commercialPricingMap, Boolean.FALSE, Boolean.FALSE)) {
                inlandTransAllowances.setDiscount_val(Utils.getFloatDoller(df.formatCellValue(row.getCell(commercialPricingMap.get(ResponseRfpExcelHeaders.INLAND_TRANSPORTATION_ALLOWANCES.getValue())))));
            }
        }
        inlandTransAllowances.setDiscount_uom(Constants.UNIT_OF_MEASURE);
        goodwillDiscounts.setDiscount_uom(Constants.UNIT_OF_MEASURE);

        Cell goodWillDiscountCell = row.getCell(commercialPricingMap.get(ResponseRfpExcelHeaders.GOODWILL_DISCOUNT.getValue()));
        if (Boolean.FALSE.equals(Utils.checkIfStringIsNullOrEmpty(df.formatCellValue(goodWillDiscountCell)))) {
            if (checkIsEmptyStringAndDataFormat(row, ResponseRfpExcelHeaders.GOODWILL_DISCOUNT.getValue(), ExcelConstants.DOLLER_REGEX, ExcelConstants.ResponseRfpExcelSheetNames.COMMERCIAL_PRICING.getSheetName(), ExcelConstants.DOLLER, commercialPricingMap, Boolean.FALSE, Boolean.FALSE)) {
                goodwillDiscounts.setDiscount_val(Utils.getFloatDoller(df.formatCellValue(row.getCell(commercialPricingMap.get(ResponseRfpExcelHeaders.GOODWILL_DISCOUNT.getValue())))));
            }
        }
        if (Boolean.FALSE.equals(Utils.checkIfStringIsNullOrEmpty(df.formatCellValue(row.getCell(commercialPricingMap.get(ResponseRfpExcelHeaders.ALTERNATE_REBATE_CRITERIA.getValue())))))) {
            discountsAllowances.setAlternate_rebate_criteria(df.formatCellValue(row.getCell(commercialPricingMap.get(ResponseRfpExcelHeaders.ALTERNATE_REBATE_CRITERIA.getValue()))));
        }
        discountsAllowances.setGoodwill_discounts(goodwillDiscounts);
        discountsAllowances.setInland_trans_allowances(inlandTransAllowances);
        pricingDetail.setDiscounts_allowances(discountsAllowances);

        */
    }

    //Method to parse and populate Basic or Default Commercial Pricing Details :: Commercial Pricing Sheet
    private static void parseBasicPricingDetails(PricingDetail pricingDetail, Row row, Sheet sheet, CommercialPricingParsingRows parsingRows, boolean isVolumeTierDiscounts) {

        if (isNonEmptyString(row, commercialPricingMap.get(ResponseRfpExcelHeaders.INDEX_READ_TYPE.getValue()), sheet.getSheetName(), ResponseRfpExcelHeaders.INDEX_READ_TYPE.getValue())) {
            parsingRows.setReadType(df.formatCellValue(row.getCell(commercialPricingMap.get(ResponseRfpExcelHeaders.INDEX_READ_TYPE.getValue()))));
        }
        isNonEmptyString(row, commercialPricingMap.get(ResponseRfpExcelHeaders.WEEK_DAY_OF_MONTH.getValue()), sheet.getSheetName(), ResponseRfpExcelHeaders.WEEK_DAY_OF_MONTH.getValue());
//        isNonEmptyString(row, commercialPricingMap.get(ResponseRfpExcelHeaders.GIVEN_DATE.getValue()), sheet.getSheetName(), ResponseRfpExcelHeaders.GIVEN_DATE.getValue());
        //payment Terms in mandatory
        if (Boolean.TRUE.equals(isNonEmptyString(row, commercialPricingMap.get(ExcelConstants.ResponseRfpExcelHeaders.PAYMENT_TERMS.getValue()), sheet.getSheetName(), ResponseRfpExcelHeaders.PAYMENT_TERMS.getValue()))) {
            pricingDetail.setPayment_term(df.formatCellValue(row.getCell(commercialPricingMap.get(ExcelConstants.ResponseRfpExcelHeaders.PAYMENT_TERMS.getValue()))));
        }

        Cell priceFloorCell = row.getCell(commercialPricingMap.get(ResponseRfpExcelHeaders.PRICE_FLOOR.getValue()));
        if (Boolean.FALSE.equals(Utils.checkIfStringIsNullOrEmpty(df.formatCellValue(priceFloorCell)))) {
            if (checkIsEmptyStringAndDataFormat(row, ResponseRfpExcelHeaders.PRICE_FLOOR.getValue(), ExcelConstants.DOLLER_REGEX, ExcelConstants.ResponseRfpExcelSheetNames.COMMERCIAL_PRICING.getSheetName(), ExcelConstants.DOLLER, commercialPricingMap, Boolean.FALSE, Boolean.FALSE)) {
                pricingDetail.setPrice_floor(Utils.getLongDoller(df.formatCellValue(row.getCell(commercialPricingMap.get(ResponseRfpExcelHeaders.PRICE_FLOOR.getValue())))));
            }
        }

        Cell priceCeilCell = row.getCell(commercialPricingMap.get(ResponseRfpExcelHeaders.PRICE_CEILING.getValue()));
        if (Boolean.FALSE.equals(Utils.checkIfStringIsNullOrEmpty(df.formatCellValue(priceCeilCell)))) {
            if (checkIsEmptyStringAndDataFormat(row, ResponseRfpExcelHeaders.PRICE_CEILING.getValue(), ExcelConstants.DOLLER_REGEX, ExcelConstants.ResponseRfpExcelSheetNames.COMMERCIAL_PRICING.getSheetName(), ExcelConstants.DOLLER, commercialPricingMap, Boolean.FALSE, Boolean.FALSE)) {
                pricingDetail.setPrice_ceil(Utils.getLongDoller(df.formatCellValue(row.getCell(commercialPricingMap.get(ResponseRfpExcelHeaders.PRICE_CEILING.getValue())))));
            }
        }

        Cell priceCeilingEffectiveCell = row.getCell(commercialPricingMap.get(ResponseRfpExcelHeaders.PRICE_CEILING_EFFECTIVE_PERIOD.getValue()));
        if (Boolean.FALSE.equals(Utils.checkIfStringIsNullOrEmpty(df.formatCellValue(priceCeilingEffectiveCell)))) {
            if (checkIsEmptyStringAndDataFormat(row, ResponseRfpExcelHeaders.PRICE_CEILING_EFFECTIVE_PERIOD.getValue(), ExcelConstants.EFFECTIVE_DATE_REGEX, ExcelConstants.ResponseRfpExcelSheetNames.COMMERCIAL_PRICING.getSheetName(), ExcelConstants.EFFECTIVE_DATE, commercialPricingMap, Boolean.FALSE, Boolean.FALSE)) {
                String startAndEnDate = df.formatCellValue(row.getCell(commercialPricingMap.get(ResponseRfpExcelHeaders.PRICE_CEILING_EFFECTIVE_PERIOD.getValue())));
                //Getting Period Start and Period End (Separated by - )
                //TODO need to validate Start Period and End period
                pricingDetail.setCeil_floor_period_start(startAndEnDate.split(Constants.HYPHEN)[0].trim());
                pricingDetail.setCeil_floor_period_end(startAndEnDate.split(Constants.HYPHEN)[1].trim());
            }
        }


        if (Boolean.FALSE.equals(Utils.checkIfStringIsNullOrEmpty(df.formatCellValue(row.getCell(commercialPricingMap.get(ResponseRfpExcelHeaders.COMMENTS.getValue())))))) {
            pricingDetail.setComments(df.formatCellValue(row.getCell(commercialPricingMap.get(ResponseRfpExcelHeaders.COMMENTS.getValue()))));
        }

        // Parsing Pricing Mechanism Details
        if (Boolean.TRUE.equals(isNonEmptyString(row, commercialPricingMap.get(ResponseRfpExcelHeaders.MECHANISM_BASIS.getValue()), sheet.getSheetName(), ResponseRfpExcelHeaders.MECHANISM_BASIS.getValue()))) {
            pricingDetail.setMechanism_basis(df.formatCellValue(row.getCell(commercialPricingMap.get(ResponseRfpExcelHeaders.MECHANISM_BASIS.getValue()))));
        }

        if (pricingDetail.getMechanism_basis() != null && !pricingDetail.getMechanism_basis().equals(Constants.MECHANISMS.OTHER.getValue())) {
            getRowsCountForCommercialSheetParsing(row, parsingRows, sheet);
        }

        if (Boolean.TRUE.equals(isNonEmptyString(row, commercialPricingMap.get(ResponseRfpExcelHeaders.IS_MOVEMENT.getValue()), sheet.getSheetName(), ResponseRfpExcelHeaders.IS_MOVEMENT.getValue()))) {
            pricingDetail.setIs_movement_based(df.formatCellValue(row.getCell(commercialPricingMap.get(ResponseRfpExcelHeaders.IS_MOVEMENT.getValue()))).equalsIgnoreCase(Constants.MECHANISMS.MOVEMENT.getValue()));
        }


        //Initial Price
        Cell initialPriceCell = row.getCell(commercialPricingMap.get(ResponseRfpExcelHeaders.INITIAL_PRICE.getValue()));
        if (Boolean.FALSE.equals(Utils.checkIfStringIsNullOrEmpty(df.formatCellValue(initialPriceCell)))) {
            if (checkIsEmptyStringAndDataFormat(row, ResponseRfpExcelHeaders.INITIAL_PRICE.getValue(), ExcelConstants.DOLLER_REGEX, ExcelConstants.ResponseRfpExcelSheetNames.COMMERCIAL_PRICING.getSheetName(), ExcelConstants.DOLLER, commercialPricingMap, Boolean.FALSE, Boolean.FALSE)) {
                pricingDetail.setInitial_price(Utils.getFloatDoller(df.formatCellValue(row.getCell(commercialPricingMap.get(ResponseRfpExcelHeaders.INITIAL_PRICE.getValue())))));
            }
        }


        //Additional Discount
        Cell additionalDiscount = row.getCell(commercialPricingMap.get(ResponseRfpExcelHeaders.ADDITIONAL_DISCOUNT.getValue()));
        if (Boolean.FALSE.equals(Utils.checkIfStringIsNullOrEmpty(df.formatCellValue(additionalDiscount)))) {
            if (checkIsEmptyStringAndDataFormat(row, ResponseRfpExcelHeaders.ADDITIONAL_DISCOUNT.getValue(), ExcelConstants.DOLLER_REGEX, ExcelConstants.ResponseRfpExcelSheetNames.COMMERCIAL_PRICING.getSheetName(), ExcelConstants.DOLLER, commercialPricingMap, Boolean.FALSE, Boolean.FALSE)) {
                pricingDetail.setAdditional_discount(Utils.getFloatDoller(df.formatCellValue(row.getCell(commercialPricingMap.get(ResponseRfpExcelHeaders.ADDITIONAL_DISCOUNT.getValue())))));
            }
        }

        if (Boolean.TRUE.equals(isNonEmptyString(row, commercialPricingMap.get(ResponseRfpExcelHeaders.TIME_WINDOW.getValue()), sheet.getSheetName(), ResponseRfpExcelHeaders.TIME_WINDOW.getValue())) && checkIsEmptyStringAndDataFormat(row, ResponseRfpExcelHeaders.TIME_WINDOW.getValue(), ExcelConstants.ALPHA_NUMERIC_REGEX, ExcelConstants.ResponseRfpExcelSheetNames.COMMERCIAL_PRICING.getSheetName(), ExcelConstants.ALPHA_NUMERIC, commercialPricingMap, Boolean.TRUE, Boolean.FALSE)) {
            String timeWindow = df.formatCellValue(row.getCell(commercialPricingMap.get(ResponseRfpExcelHeaders.TIME_WINDOW.getValue())));
            parseTimeWindowAndTimeWindowPeriodForCommercialSheet(timeWindow, row, pricingDetail);
        }


        //TODO Confirm We are not getting below fields from Excel
//        pricingDetail.setTime_window_period(Constants.PERIOD_TYPE);
//        pricingDetail.setVolume_based_period(Constants.PERIOD_TYPE);
        pricingDetail.setPrice_uom(Constants.UNIT_OF_MEASURE);
        if (checkIsEmptyStringAndDataFormat(row, ResponseRfpExcelHeaders.GIVEN_DATE.getValue(), ExcelConstants.NUMBER_REGEX, ExcelConstants.ResponseRfpExcelSheetNames.COMMERCIAL_PRICING.getSheetName(), ExcelConstants.NUMBER, commercialPricingMap, Boolean.FALSE, Boolean.FALSE)) {
            parsingRows.setReadDate(df.formatCellValue(row.getCell(commercialPricingMap.get(ResponseRfpExcelHeaders.GIVEN_DATE.getValue()))));
        }
    }

    //Fetching No of rows present for that pricing method , so that we iterate that no of times to validate
    private static void getRowsCountForCommercialSheetParsing(Row row, CommercialPricingParsingRows parsingRows, Sheet sheet) {
        parsingRows.setPricingMechanismRows(getNonEmptyRowCount(sheet, commercialPricingMap.get(ResponseRfpExcelHeaders.TIME_WINDOW_PERIOD.getValue())));
        parsingRows.setPricingTierDiscountRows(getNonEmptyRowCount(sheet, commercialPricingMap.get(ResponseRfpExcelHeaders.PRICE_TIER_CATEGORY.getValue())));
        parsingRows.setVolumeTierDiscountRows(getNonEmptyRowCount(sheet, commercialPricingMap.get(ResponseRfpExcelHeaders.VOLUME_TIER_CATEGORY.getValue())));
    }

    public static String getNumericString(String string, String limitString) {
        //replace Commas in between numbers (1,000 to 1000) if any
        string = string.replaceAll(Constants.COMMA, Constants.EMPTY_STRING);
        //replace desired character ($ or %)
        return string.replace(limitString, Constants.EMPTY_STRING);
    }

    public static boolean validateRegex(String data, String regex) {
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(data).matches();
    }

    //    Non empty String check (It will throw error when string is  empty)
    public static Boolean isNonEmptyString(Row row, int colNumber, String sheetName, String columnName) {
        if (row.getCell(colNumber) == null || df.formatCellValue(row.getCell(colNumber)) == null || df.formatCellValue(row.getCell(colNumber)).trim().isEmpty()) {
            String errorMessage = MessageFormat.format(ErrorMessages.Messages.MANDATORY_FIELD_MISSING_ERROR.getMessage(), columnName);
            errorMessageDetailsList.add(new ErrorMessageDetails(sheetName, new CellReference(row.getRowNum(), colNumber).formatAsString(false), ExcelConstants.ExpectedValuesList.MANDATORY.getValue(), df.formatCellValue(row.getCell(colNumber)), columnName, errorMessage));
            return false;
        }
        return true;
    }

    //    Non empty String check (It will throw error when string is  empty)
    public static Boolean isNonEmptyStringForCommercialSheet(Row row, int colNumber, String sheetName, String columnName) {
        if (row.getCell(colNumber) == null || df.formatCellValue(row.getCell(colNumber)) == null || df.formatCellValue(row.getCell(colNumber)).trim().isEmpty()) {
            return false;
        }
        return true;
    }


    //Validates NonEmpty String and With Regex
//    public static void validateWithRegex(Row row,int colNumber,String regex,String columnName){
//        String s = df.formatCellValue(row.getCell(colNumber));
//        isNonEmptyString(row,colNumber);
//        if(!validateRegex(s,regex)){
//            throw new FileTemplateMismatchException(ErrorMessages.FILE_TEMPLATE_MISMATCH_ERROR+ (new CellReference(row.getCell(colNumber))).formatAsString(true));
//        }
//    }

    public static int getNonEmptyRowCount(Sheet sheet, int columnIdx) {
        int rowCount = 0;
        for (Row row : sheet) {
            if (row.getRowNum() >= 6) {
                Cell cell = row.getCell(columnIdx, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                if (cell != null && cell.getCellType() != CellType.BLANK) {
                    rowCount++;
                }
            }
        }
        return rowCount;
    }

    public static boolean checkIsEmptyStringAndDataFormat(Row row, String headerName, String regex, String sheetName, String regexType, Map<String, Integer> columnsMap, Boolean isMandatory, Boolean NA) {
        Cell cell = row.getCell(columnsMap.get(headerName));
        if (Boolean.FALSE.equals(Utils.checkIfStringIsNullOrEmpty(df.formatCellValue(cell).trim()))) {
            String value = df.formatCellValue(cell);


            if (NA == Boolean.TRUE && (Objects.equals(Utils.getTrimmedNumber(value), ExcelConstants.NA))) {
                return true;
            }

            if (regex.equals(ExcelConstants.NUMBER_REGEX) || regex.equals(ExcelConstants.DOLLER_REGEX) || regex.equals(ExcelConstants.MIN_MAX_REGEX_NUMERIC) || regex.equals(ExcelConstants.MIN_MAX_REGEX_DOLLER) || regex.equals(ExcelConstants.PERCENT_OR_DOLLAR_REGEX) || regex.equals(ExcelConstants.MIN_MAX_REGEX_DOLLER_OR_NUMBER_WITH_DOLLER)) {
                value = Utils.getTrimmedNumber(value);
            }
            if (regex.equals(ExcelConstants.PERCENTAGE_REGEX)) {
                value = value.replaceAll(Constants.SPACE, Constants.EMPTY_STRING);
            }
            if (regex.equals(ExcelConstants.DOLLER_REGEX)) {
                value = value.replace(Constants.HYPHEN, Constants.EMPTY_STRING);
            }
            if (validateRegex(value, regex)) {
                return true;
            } else {
                String errorMessage = MessageFormat.format(ErrorMessages.Messages.DATA_FORMAT_MISMATCH.getMessage(), regexType, df.formatCellValue(cell));
                errorMessageDetailsList.add(new ErrorMessageDetails(sheetName, new CellReference(row.getRowNum(), columnsMap.get(headerName)).formatAsString(false), regexType, df.formatCellValue(cell), headerName, errorMessage));
            }
        } else if (isMandatory && cell != null) {
            String errorMessage = MessageFormat.format(ErrorMessages.Messages.MANDATORY_FIELD_MISSING_ERROR.getMessage(), headerName);
            errorMessageDetailsList.add(new ErrorMessageDetails(sheetName, new CellReference(cell).formatAsString(false), ExcelConstants.ExpectedValuesList.MANDATORY.getValue(), df.formatCellValue(cell), headerName, errorMessage));
        }
        return false;
    }

    //SHIVA END

    //Validating Mandatory fields in Logistic Sheet
    //Validations :: Logistic Details ::
//    private static void validateMandatoryFieldsLogisticSheet(Row row, Map<String, Integer> columnsMap, String sheetName) {
//        isNonEmptyString(row, columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.MILL.getValue()), sheetName, ExcelConstants.ResponseRfpExcelHeaders.MILL.getValue());
//        isNonEmptyString(row, columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.ORIGIN_PORT.getValue()), sheetName, ExcelConstants.ResponseRfpExcelHeaders.ORIGIN_PORT.getValue());
//        isNonEmptyString(row, columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.ENVIRONMENTAL_CERTIFICATION.getValue()), sheetName, ExcelConstants.ResponseRfpExcelHeaders.ENVIRONMENTAL_CERTIFICATION.getValue());
//        isNonEmptyString(row, columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.BALE_TYPE.getValue()), sheetName, ExcelConstants.ResponseRfpExcelHeaders.BALE_TYPE.getValue());
//        isNonEmptyString(row, columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.BALE_PACKAGING.getValue()), sheetName, ExcelConstants.ResponseRfpExcelHeaders.BALE_PACKAGING.getValue());
//        isNonEmptyString(row, columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.INCOTERMS.getValue()), sheetName, ExcelConstants.ResponseRfpExcelHeaders.INCOTERMS.getValue());
//        isNonEmptyString(row, columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.TRANSIT_LEAD_TIME_ORIGIN_PORT_TO_US_PORT.getValue()), sheetName, ExcelConstants.ResponseRfpExcelHeaders.TRANSIT_LEAD_TIME_ORIGIN_PORT_TO_US_PORT.getValue());
//        isNonEmptyString(row, columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.SAFETY_STOCK_LOCATION_TYPE.getValue()), sheetName, ExcelConstants.ResponseRfpExcelHeaders.SAFETY_STOCK_LOCATION_TYPE.getValue());
//        isNonEmptyString(row, columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.SAFETY_STOCK_LOCATION_NAME.getValue()), sheetName, ExcelConstants.ResponseRfpExcelHeaders.SAFETY_STOCK_LOCATION_NAME.getValue());
//        isNonEmptyString(row, columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.INLAND_TRANSIT_ORIGIN_TYPE.getValue()), sheetName, ExcelConstants.ResponseRfpExcelHeaders.INLAND_TRANSIT_ORIGIN_TYPE.getValue());
//        isNonEmptyString(row, columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.INLAND_TRANSIT_ORIGIN_NAME.getValue()), sheetName, ExcelConstants.ResponseRfpExcelHeaders.INLAND_TRANSIT_ORIGIN_NAME.getValue());
//        isNonEmptyString(row, columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.INLAND_TRANSPORTATION_ROUTE.getValue()), sheetName, ExcelConstants.ResponseRfpExcelHeaders.INLAND_TRANSPORTATION_ROUTE.getValue());
//        isNonEmptyString(row, columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.INLAND_TRANSIT_DESTINATION_TYPE.getValue()), sheetName, ExcelConstants.ResponseRfpExcelHeaders.INLAND_TRANSIT_DESTINATION_TYPE.getValue());
//
//        //If Inland Transit Destination Type is other than GP Mill then Inland Transit Destination Name is Mandatory, Else it should be same as GP mill Name
//        if (!(Objects.equals(df.formatCellValue(row.getCell(columnsMap.get(ResponseRfpExcelHeaders.INLAND_TRANSIT_DESTINATION_TYPE.getValue()))), ExcelConstants.InlandTransitDestinationType.GP_MILL.getValue()))) {
//            if (row.getCell(columnsMap.get(ResponseRfpExcelHeaders.INLAND_TRANSIT_DESTINATION_NAME.getValue())) == null || df.formatCellValue(row.getCell(columnsMap.get(ResponseRfpExcelHeaders.INLAND_TRANSIT_DESTINATION_NAME.getValue()))) == null || df.formatCellValue(row.getCell(columnsMap.get(ResponseRfpExcelHeaders.INLAND_TRANSIT_DESTINATION_NAME.getValue()))).trim().isEmpty()) {
//                String errorMessage = MessageFormat.format(ErrorMessages.Messages.CONDITIONAL_MANDATORY_OTHER_THAN_FILED_ERROR.getMessage(), ResponseRfpExcelHeaders.INLAND_TRANSIT_DESTINATION_NAME.getValue(), ResponseRfpExcelHeaders.INLAND_TRANSIT_DESTINATION_TYPE.getValue(), ExcelConstants.InlandTransitDestinationType.GP_MILL.getValue());
//                errorMessageDetailsList.add(new ErrorMessageDetails(sheetName, new CellReference(row.getRowNum(), columnsMap.get(ResponseRfpExcelHeaders.INLAND_TRANSIT_DESTINATION_NAME.getValue())).formatAsString(false), ExcelConstants.ExpectedValuesList.MANDATORY.getValue(), df.formatCellValue(row.getCell(columnsMap.get(ResponseRfpExcelHeaders.INLAND_TRANSIT_DESTINATION_NAME.getValue()))), ResponseRfpExcelHeaders.INLAND_TRANSIT_DESTINATION_NAME.getValue(), errorMessage));
//            }
//        } else {
//            if (!Objects.equals(df.formatCellValue(row.getCell(columnsMap.get(ResponseRfpExcelHeaders.MILL.getValue()))), df.formatCellValue(row.getCell(columnsMap.get(ResponseRfpExcelHeaders.INLAND_TRANSIT_DESTINATION_NAME.getValue()))))) {
//                String errorMessage = MessageFormat.format(ErrorMessages.Messages.CONDITIONAL_FIELD_ERROR.getMessage(), ResponseRfpExcelHeaders.INLAND_TRANSIT_DESTINATION_TYPE.getValue(), ExcelConstants.InlandTransitDestinationType.GP_MILL.getValue(), ResponseRfpExcelHeaders.INLAND_TRANSIT_DESTINATION_NAME.getValue(), df.formatCellValue(row.getCell(columnsMap.get(ResponseRfpExcelHeaders.MILL.getValue()))), df.formatCellValue(row.getCell(columnsMap.get(ResponseRfpExcelHeaders.INLAND_TRANSIT_DESTINATION_NAME.getValue()))));
//                errorMessageDetailsList.add(new ErrorMessageDetails(sheetName, new CellReference(row.getRowNum(), columnsMap.get(ResponseRfpExcelHeaders.INLAND_TRANSIT_DESTINATION_NAME.getValue())).formatAsString(false), df.formatCellValue(row.getCell(columnsMap.get(ResponseRfpExcelHeaders.MILL.getValue()))), df.formatCellValue(row.getCell(columnsMap.get(ResponseRfpExcelHeaders.INLAND_TRANSIT_DESTINATION_NAME.getValue()))), ResponseRfpExcelHeaders.INLAND_TRANSIT_DESTINATION_NAME.getValue(), errorMessage));
//            }
//        }
//
//        isNonEmptyString(row, columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.TRANSIT_MODE.getValue()), sheetName, ExcelConstants.ResponseRfpExcelHeaders.TRANSIT_MODE.getValue());
//        isNonEmptyString(row, columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.US_PORT_OF_ENTRY.getValue()), sheetName, ResponseRfpExcelHeaders.US_PORT_OF_ENTRY.getValue());
//
//    }

    public static void validateCellDataType(Row row, int columnIndex, String headerName, CellType expectedDataType, String sheet) {
        Cell cell = row.getCell(columnIndex);
        if (df.formatCellValue(cell) != null && !df.formatCellValue(cell).trim().isEmpty()) {
            CellType actualDataType = cell.getCellType();

            if (actualDataType != expectedDataType) {
                String errorMessage = MessageFormat.format(ErrorMessages.Messages.DATA_TYPE_MISMATCH.getMessage(), expectedDataType, actualDataType);
                errorMessageDetailsList.add(new ErrorMessageDetails(sheet, new CellReference(row.getRowNum(), columnIndex).formatAsString(false), expectedDataType.toString(), actualDataType.toString(), headerName, errorMessage));
            }
        }
    }

    //Method to Validate Cell Data types in Logistic Sheet
    private static void validateDataTypesInLogisticSheet(Row row, Map<String, Integer> columnsMap, String sheetName) {
        validateCellDataType(row, columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.MILL.getValue()), ExcelConstants.ResponseRfpExcelHeaders.MILL.getValue(), CellType.STRING, sheetName);
        validateCellDataType(row, columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.SUPPLIER_MILL.getValue()), ExcelConstants.ResponseRfpExcelHeaders.SUPPLIER_MILL.getValue(), CellType.STRING, sheetName);
        validateCellDataType(row, columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.ORIGIN_PORT.getValue()), ExcelConstants.ResponseRfpExcelHeaders.ORIGIN_PORT.getValue(), CellType.STRING, sheetName);
        validateCellDataType(row, columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.ORIGIN_COUNTRY.getValue()), ExcelConstants.ResponseRfpExcelHeaders.ORIGIN_COUNTRY.getValue(), CellType.STRING, sheetName);
        validateCellDataType(row, columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.ENVIRONMENTAL_CERTIFICATION.getValue()), ExcelConstants.ResponseRfpExcelHeaders.ENVIRONMENTAL_CERTIFICATION.getValue(), CellType.STRING, sheetName);
        validateCellDataType(row, columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.BALE_PACKAGING.getValue()), ExcelConstants.ResponseRfpExcelHeaders.BALE_PACKAGING.getValue(), CellType.STRING, sheetName);
        validateCellDataType(row, columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.BALE_TYPE.getValue()), ExcelConstants.ResponseRfpExcelHeaders.BALE_TYPE.getValue(), CellType.STRING, sheetName);
        validateCellDataType(row, columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.US_PORT_OF_ENTRY.getValue()), ExcelConstants.ResponseRfpExcelHeaders.US_PORT_OF_ENTRY.getValue(), CellType.STRING, sheetName);
        validateCellDataType(row, columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.INCOTERMS.getValue()), ExcelConstants.ResponseRfpExcelHeaders.INCOTERMS.getValue(), CellType.STRING, sheetName);
        validateCellDataType(row, columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.STEAMSHIP_LINE.getValue()), ExcelConstants.ResponseRfpExcelHeaders.STEAMSHIP_LINE.getValue(), CellType.STRING, sheetName);
        validateCellDataType(row, columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.SAFETY_STOCK_LOCATION_TYPE.getValue()), ExcelConstants.ResponseRfpExcelHeaders.SAFETY_STOCK_LOCATION_TYPE.getValue(), CellType.STRING, sheetName);
        validateCellDataType(row, columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.SAFETY_STOCK_LOCATION_NAME.getValue()), ExcelConstants.ResponseRfpExcelHeaders.SAFETY_STOCK_LOCATION_NAME.getValue(), CellType.STRING, sheetName);
        validateCellDataType(row, columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.ADDRESS.getValue()), ExcelConstants.ResponseRfpExcelHeaders.ADDRESS.getValue(), CellType.STRING, sheetName);
        validateCellDataType(row, columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.INLAND_TRANSIT_ORIGIN_TYPE.getValue()), ExcelConstants.ResponseRfpExcelHeaders.INLAND_TRANSIT_ORIGIN_TYPE.getValue(), CellType.STRING, sheetName);
        validateCellDataType(row, columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.INLAND_TRANSIT_ORIGIN_NAME.getValue()), ExcelConstants.ResponseRfpExcelHeaders.INLAND_TRANSIT_ORIGIN_NAME.getValue(), CellType.STRING, sheetName);
        validateCellDataType(row, columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.INLAND_TRANSPORTATION_ROUTE.getValue()), ExcelConstants.ResponseRfpExcelHeaders.INLAND_TRANSPORTATION_ROUTE.getValue(), CellType.STRING, sheetName);
        validateCellDataType(row, columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.INLAND_TRANSIT_DESTINATION_TYPE.getValue()), ExcelConstants.ResponseRfpExcelHeaders.INLAND_TRANSIT_DESTINATION_TYPE.getValue(), CellType.STRING, sheetName);
        validateCellDataType(row, columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.INLAND_TRANSIT_DESTINATION_NAME.getValue()), ExcelConstants.ResponseRfpExcelHeaders.INLAND_TRANSIT_DESTINATION_NAME.getValue(), CellType.STRING, sheetName);
        validateCellDataType(row, columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.DESTINATION_ADDRESS.getValue()), ExcelConstants.ResponseRfpExcelHeaders.DESTINATION_ADDRESS.getValue(), CellType.STRING, sheetName);
        validateCellDataType(row, columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.TRANSIT_MODE.getValue()), ExcelConstants.ResponseRfpExcelHeaders.TRANSIT_MODE.getValue(), CellType.STRING, sheetName);
    }

    //Method to validate  Repeated Data (Data should be Same)
    private static void validateRepeatedSupplierMetadata(String supplierMillName, Map<String, SupplierMills> supplierMillsToSupplierMillsMetadataMap, Row row, Map<String, Integer> columnsMap, String sheetName) {

        SupplierMills supplierMills = supplierMillsToSupplierMillsMetadataMap.get(supplierMillName);

        if (!Objects.equals(supplierMills.getOrigin_port(), df.formatCellValue(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.ORIGIN_PORT.getValue()))))) {
            String errorMessage = MessageFormat.format(ErrorMessages.Messages.REPEATED_SUPPLIER_METADATA_ERROR.getMessage(), ExcelConstants.ResponseRfpExcelHeaders.ORIGIN_PORT.getValue());
            errorMessageDetailsList.add(new ErrorMessageDetails(sheetName, new CellReference(row.getRowNum(), columnsMap.get(ResponseRfpExcelHeaders.ORIGIN_PORT.getValue())).formatAsString(false), supplierMills.getOrigin_port(), df.formatCellValue(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.ORIGIN_PORT.getValue()))), ExcelConstants.ResponseRfpExcelHeaders.ORIGIN_PORT.getValue(), errorMessage));
        }

        if (!Objects.equals(supplierMills.getOrigin_cntry(), df.formatCellValue(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.ORIGIN_COUNTRY.getValue()))))) {
            String errorMessage = MessageFormat.format(ErrorMessages.Messages.REPEATED_SUPPLIER_METADATA_ERROR.getMessage(), ExcelConstants.ResponseRfpExcelHeaders.ORIGIN_COUNTRY.getValue());
            errorMessageDetailsList.add(new ErrorMessageDetails(sheetName, new CellReference(row.getRowNum(), columnsMap.get(ResponseRfpExcelHeaders.ORIGIN_COUNTRY.getValue())).formatAsString(false), supplierMills.getOrigin_cntry(), df.formatCellValue(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.ORIGIN_COUNTRY.getValue()))), ExcelConstants.ResponseRfpExcelHeaders.ORIGIN_COUNTRY.getValue(), errorMessage));

        }

        if (!Objects.equals(supplierMills.getEnvironmental_certification(), df.formatCellValue(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.ENVIRONMENTAL_CERTIFICATION.getValue()))))) {
            String errorMessage = MessageFormat.format(ErrorMessages.Messages.REPEATED_SUPPLIER_METADATA_ERROR.getMessage(), ExcelConstants.ResponseRfpExcelHeaders.ENVIRONMENTAL_CERTIFICATION.getValue());
            errorMessageDetailsList.add(new ErrorMessageDetails(sheetName, new CellReference(row.getRowNum(), columnsMap.get(ResponseRfpExcelHeaders.ENVIRONMENTAL_CERTIFICATION.getValue())).formatAsString(false), supplierMills.getEnvironmental_certification(), df.formatCellValue(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.ENVIRONMENTAL_CERTIFICATION.getValue()))), ExcelConstants.ResponseRfpExcelHeaders.ENVIRONMENTAL_CERTIFICATION.getValue(), errorMessage));

        }

        if (!Objects.equals(supplierMills.getBale_packaging(), df.formatCellValue(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.BALE_PACKAGING.getValue()))))) {
            String errorMessage = MessageFormat.format(ErrorMessages.Messages.REPEATED_SUPPLIER_METADATA_ERROR.getMessage(), ExcelConstants.ResponseRfpExcelHeaders.BALE_PACKAGING.getValue());
            errorMessageDetailsList.add(new ErrorMessageDetails(sheetName, new CellReference(row.getRowNum(), columnsMap.get(ResponseRfpExcelHeaders.BALE_PACKAGING.getValue())).formatAsString(false), supplierMills.getBale_packaging(), df.formatCellValue(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.BALE_PACKAGING.getValue()))), ExcelConstants.ResponseRfpExcelHeaders.BALE_PACKAGING.getValue(), errorMessage));

        }

        if (!Objects.equals(supplierMills.getBale_type(), df.formatCellValue(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.BALE_TYPE.getValue()))))) {
            String errorMessage = MessageFormat.format(ErrorMessages.Messages.REPEATED_SUPPLIER_METADATA_ERROR.getMessage(), ExcelConstants.ResponseRfpExcelHeaders.BALE_TYPE.getValue());
            errorMessageDetailsList.add(new ErrorMessageDetails(sheetName, new CellReference(row.getRowNum(), columnsMap.get(ResponseRfpExcelHeaders.BALE_TYPE.getValue())).formatAsString(false), supplierMills.getBale_type(), df.formatCellValue(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.BALE_TYPE.getValue()))), ExcelConstants.ResponseRfpExcelHeaders.BALE_TYPE.getValue(), errorMessage));

        }
    }


    private static void validateRepeatedPortEntryMetadata(String supplierMillName, String portEntry, Map<String, Map<String, Map<String, PortEntryDetails>>> supplierMillPortEntryInlandDetailsMap, Row row, Map<String, Integer> columnsMap, String sheetName, String gpMillName) {

        PortEntryDetails portEntryDetails = supplierMillPortEntryInlandDetailsMap.get(supplierMillName).get(portEntry).get(gpMillName);

        if (!Objects.equals(portEntryDetails.getPort_free_time_in_days(), Long.valueOf(Utils.getTrimmedNumber(df.formatCellValue(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.PORT_TIME_IN_DAYS.getValue()))))))) {
            String errorMessage = MessageFormat.format(ErrorMessages.Messages.REPEATED_SUPPLIER_MILL_PORT_ENTRY_METADATA.getMessage(), ExcelConstants.ResponseRfpExcelHeaders.PORT_TIME_IN_DAYS.getValue());
            errorMessageDetailsList.add(new ErrorMessageDetails(sheetName, new CellReference(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.PORT_TIME_IN_DAYS.getValue()))).formatAsString(false), portEntryDetails.getPort_free_time_in_days().toString(), Utils.getTrimmedNumber(df.formatCellValue(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.PORT_TIME_IN_DAYS.getValue())))), ExcelConstants.ResponseRfpExcelHeaders.PORT_TIME_IN_DAYS.getValue(), errorMessage));
        }

        if (!Objects.equals(portEntryDetails.getTransit_leadtime_in_days_origin_port_port_entry(), Long.valueOf(Utils.getTrimmedNumber(df.formatCellValue(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.TRANSIT_LEAD_TIME_ORIGIN_PORT_TO_US_PORT.getValue()))))))) {
            String errorMessage = MessageFormat.format(ErrorMessages.Messages.REPEATED_SUPPLIER_MILL_PORT_ENTRY_METADATA.getMessage(), ExcelConstants.ResponseRfpExcelHeaders.TRANSIT_LEAD_TIME_ORIGIN_PORT_TO_US_PORT.getValue());
            errorMessageDetailsList.add(new ErrorMessageDetails(sheetName, new CellReference(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.TRANSIT_LEAD_TIME_ORIGIN_PORT_TO_US_PORT.getValue()))).formatAsString(false), portEntryDetails.getTransit_leadtime_in_days_origin_port_port_entry().toString(), Utils.getTrimmedNumber(df.formatCellValue(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.TRANSIT_LEAD_TIME_ORIGIN_PORT_TO_US_PORT.getValue())))), ExcelConstants.ResponseRfpExcelHeaders.TRANSIT_LEAD_TIME_ORIGIN_PORT_TO_US_PORT.getValue(), errorMessage));
        }

        if (!Objects.equals(portEntryDetails.getSteamship_line(), df.formatCellValue(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.STEAMSHIP_LINE.getValue()))))) {
            String errorMessage = MessageFormat.format(ErrorMessages.Messages.REPEATED_SUPPLIER_MILL_PORT_ENTRY_METADATA.getMessage(), ExcelConstants.ResponseRfpExcelHeaders.STEAMSHIP_LINE.getValue());
            errorMessageDetailsList.add(new ErrorMessageDetails(sheetName, new CellReference(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.STEAMSHIP_LINE.getValue()))).formatAsString(false), portEntryDetails.getSteamship_line(), df.formatCellValue(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.STEAMSHIP_LINE.getValue()))), ExcelConstants.ResponseRfpExcelHeaders.STEAMSHIP_LINE.getValue(), errorMessage));
        }

        if (!Objects.equals(portEntryDetails.getOcean_freight(), Utils.getFloatDoller(String.valueOf(row.getCell(columnsMap.get(ResponseRfpExcelHeaders.OCEAN_FREIGHT.getValue())))))) {
            String errorMessage = MessageFormat.format(ErrorMessages.Messages.REPEATED_SUPPLIER_MILL_PORT_ENTRY_METADATA.getMessage(), ExcelConstants.ResponseRfpExcelHeaders.OCEAN_FREIGHT.getValue());
            errorMessageDetailsList.add(new ErrorMessageDetails(sheetName, new CellReference(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.OCEAN_FREIGHT.getValue()))).formatAsString(false), portEntryDetails.getOcean_freight().toString(), Utils.getTrimmedNumber(df.formatCellValue(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.OCEAN_FREIGHT.getValue())))), ExcelConstants.ResponseRfpExcelHeaders.OCEAN_FREIGHT.getValue(), errorMessage));
        }

        if (!Objects.equals(portEntryDetails.getSafety_stock_nominated_in_days(), Long.valueOf(Utils.getTrimmedNumber(df.formatCellValue(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.NOMINATED_SAFETY_STOCK.getValue()))))))) {
            String errorMessage = MessageFormat.format(ErrorMessages.Messages.REPEATED_SUPPLIER_MILL_PORT_ENTRY_METADATA.getMessage(), ExcelConstants.ResponseRfpExcelHeaders.NOMINATED_SAFETY_STOCK.getValue());
            errorMessageDetailsList.add(new ErrorMessageDetails(sheetName, new CellReference(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.NOMINATED_SAFETY_STOCK.getValue()))).formatAsString(false), portEntryDetails.getSafety_stock_nominated_in_days().toString(), Utils.getTrimmedNumber(df.formatCellValue(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.NOMINATED_SAFETY_STOCK.getValue())))), ExcelConstants.ResponseRfpExcelHeaders.NOMINATED_SAFETY_STOCK.getValue(), errorMessage));
        }


        if (!Objects.equals(portEntryDetails.getSafety_stock_location().getType(), df.formatCellValue(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.SAFETY_STOCK_LOCATION_TYPE.getValue()))).trim())) {
            String errorMessage = MessageFormat.format(ErrorMessages.Messages.REPEATED_SUPPLIER_MILL_PORT_ENTRY_METADATA.getMessage(), ExcelConstants.ResponseRfpExcelHeaders.SAFETY_STOCK_LOCATION_TYPE.getValue());
            errorMessageDetailsList.add(new ErrorMessageDetails(sheetName, new CellReference(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.SAFETY_STOCK_LOCATION_TYPE.getValue()))).formatAsString(false), portEntryDetails.getSafety_stock_location().getType(), df.formatCellValue(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.SAFETY_STOCK_LOCATION_TYPE.getValue()))), ExcelConstants.ResponseRfpExcelHeaders.SAFETY_STOCK_LOCATION_TYPE.getValue(), errorMessage));
        }

        if (!Objects.equals(portEntryDetails.getSafety_stock_location().getName(), df.formatCellValue(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.SAFETY_STOCK_LOCATION_NAME.getValue()))))) {
            String errorMessage = MessageFormat.format(ErrorMessages.Messages.REPEATED_SUPPLIER_MILL_PORT_ENTRY_METADATA.getMessage(), ExcelConstants.ResponseRfpExcelHeaders.SAFETY_STOCK_LOCATION_NAME.getValue());
            errorMessageDetailsList.add(new ErrorMessageDetails(sheetName, new CellReference(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.SAFETY_STOCK_LOCATION_NAME.getValue()))).formatAsString(false), portEntryDetails.getSafety_stock_location().getName(), df.formatCellValue(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.SAFETY_STOCK_LOCATION_NAME.getValue()))), ExcelConstants.ResponseRfpExcelHeaders.SAFETY_STOCK_LOCATION_NAME.getValue(), errorMessage));
        }

        if (!Objects.equals(portEntryDetails.getTransit_cost_from_port_entry_to_safety_stock_loc(), Utils.getFloatDoller(df.formatCellValue(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.TRANSIT_COST_FROM_US_PORT_TO_SAFETY_STOCK_LOC.getValue())))))) {
            String errorMessage = MessageFormat.format(ErrorMessages.Messages.REPEATED_SUPPLIER_MILL_PORT_ENTRY_METADATA.getMessage(), ExcelConstants.ResponseRfpExcelHeaders.TRANSIT_COST_FROM_US_PORT_TO_SAFETY_STOCK_LOC.getValue());
            errorMessageDetailsList.add(new ErrorMessageDetails(sheetName, new CellReference(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.TRANSIT_COST_FROM_US_PORT_TO_SAFETY_STOCK_LOC.getValue()))).formatAsString(false), portEntryDetails.getTransit_cost_from_port_entry_to_safety_stock_loc().toString(), Utils.getTrimmedNumber(df.formatCellValue(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.TRANSIT_COST_FROM_US_PORT_TO_SAFETY_STOCK_LOC.getValue())))), ExcelConstants.ResponseRfpExcelHeaders.SAFETY_STOCK_LOCATION_NAME.getValue(), errorMessage));
        }

    }

    private static void validateRepeatedInlandFreightMetadata(String supplierMillName, String portEntry, Map<String, Map<String, Map<String, InlandFreight>>> supplierMillPortEntryInlandDetailsMap, Row row, Map<String, Integer> columnsMap, String sheetName, String gpMillName) {
        InlandFreight inlandFreight = supplierMillPortEntryInlandDetailsMap.get(supplierMillName).get(portEntry).get(gpMillName);

        if (!Objects.equals(inlandFreight.getGp_mill(), df.formatCellValue(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.MILL.getValue()))))) {
            String errorMessage = MessageFormat.format(ErrorMessages.Messages.REPEATED_INLAND_FREIGHT_METADATA_ERROR.getMessage(), ExcelConstants.ResponseRfpExcelHeaders.MILL.getValue());
            errorMessageDetailsList.add(new ErrorMessageDetails(sheetName, new CellReference(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.MILL.getValue()))).formatAsString(false), inlandFreight.getGp_mill(), df.formatCellValue(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.MILL.getValue()))), ExcelConstants.ResponseRfpExcelHeaders.MILL.getValue(), errorMessage));
        }

        if (!Objects.equals(inlandFreight.getSource_type(), df.formatCellValue(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.INLAND_TRANSIT_ORIGIN_TYPE.getValue()))))) {
            String errorMessage = MessageFormat.format(ErrorMessages.Messages.REPEATED_INLAND_FREIGHT_METADATA_ERROR.getMessage(), ExcelConstants.ResponseRfpExcelHeaders.INLAND_TRANSIT_ORIGIN_TYPE.getValue());
            errorMessageDetailsList.add(new ErrorMessageDetails(sheetName, new CellReference(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.INLAND_TRANSIT_ORIGIN_TYPE.getValue()))).formatAsString(false), inlandFreight.getSource_type(), df.formatCellValue(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.INLAND_TRANSIT_ORIGIN_TYPE.getValue()))), ExcelConstants.ResponseRfpExcelHeaders.INLAND_TRANSIT_ORIGIN_TYPE.getValue(), errorMessage));
        }

        if (!Objects.equals(inlandFreight.getSource_name(), df.formatCellValue(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.INLAND_TRANSIT_ORIGIN_NAME.getValue()))))) {
            String errorMessage = MessageFormat.format(ErrorMessages.Messages.REPEATED_INLAND_FREIGHT_METADATA_ERROR.getMessage(), ExcelConstants.ResponseRfpExcelHeaders.INLAND_TRANSIT_ORIGIN_NAME.getValue());
            errorMessageDetailsList.add(new ErrorMessageDetails(sheetName, new CellReference(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.INLAND_TRANSIT_ORIGIN_NAME.getValue()))).formatAsString(false), inlandFreight.getSource_name(), df.formatCellValue(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.INLAND_TRANSIT_ORIGIN_NAME.getValue()))), ExcelConstants.ResponseRfpExcelHeaders.INLAND_TRANSIT_ORIGIN_NAME.getValue(), errorMessage));
        }

        if (!Objects.equals(inlandFreight.getInland_trans_route(), df.formatCellValue(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.INLAND_TRANSPORTATION_ROUTE.getValue()))))) {
            String errorMessage = MessageFormat.format(ErrorMessages.Messages.REPEATED_INLAND_FREIGHT_METADATA_ERROR.getMessage(), ExcelConstants.ResponseRfpExcelHeaders.INLAND_TRANSPORTATION_ROUTE.getValue());
            errorMessageDetailsList.add(new ErrorMessageDetails(sheetName, new CellReference(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.INLAND_TRANSPORTATION_ROUTE.getValue()))).formatAsString(false), inlandFreight.getInland_trans_route(), df.formatCellValue(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.INLAND_TRANSPORTATION_ROUTE.getValue()))), ExcelConstants.ResponseRfpExcelHeaders.INLAND_TRANSPORTATION_ROUTE.getValue(), errorMessage));

        }

        if (!Objects.equals(inlandFreight.getDest_type(), df.formatCellValue(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.INLAND_TRANSIT_DESTINATION_TYPE.getValue()))))) {
            String errorMessage = MessageFormat.format(ErrorMessages.Messages.REPEATED_INLAND_FREIGHT_METADATA_ERROR.getMessage(), ExcelConstants.ResponseRfpExcelHeaders.INLAND_TRANSIT_DESTINATION_TYPE.getValue());
            errorMessageDetailsList.add(new ErrorMessageDetails(sheetName, new CellReference(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.INLAND_TRANSIT_DESTINATION_TYPE.getValue()))).formatAsString(false), inlandFreight.getDest_type(), df.formatCellValue(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.INLAND_TRANSIT_DESTINATION_TYPE.getValue()))), ExcelConstants.ResponseRfpExcelHeaders.INLAND_TRANSIT_DESTINATION_TYPE.getValue(), errorMessage));
        }

        if (!Objects.equals(inlandFreight.getDest_name(), df.formatCellValue(row.getCell(columnsMap.get(ResponseRfpExcelHeaders.INLAND_TRANSIT_DESTINATION_NAME.getValue()))))) {
            String errorMessage = MessageFormat.format(ErrorMessages.Messages.REPEATED_INLAND_FREIGHT_METADATA_ERROR.getMessage(), ExcelConstants.ResponseRfpExcelHeaders.INLAND_TRANSIT_DESTINATION_NAME.getValue());
            errorMessageDetailsList.add(new ErrorMessageDetails(sheetName, new CellReference(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.INLAND_TRANSIT_DESTINATION_NAME.getValue()))).formatAsString(false), inlandFreight.getDest_type(), df.formatCellValue(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.INLAND_TRANSIT_DESTINATION_NAME.getValue()))), ExcelConstants.ResponseRfpExcelHeaders.INLAND_TRANSIT_DESTINATION_NAME.getValue(), errorMessage));
        }


        if (!Objects.equals(inlandFreight.getTransit_mode(), df.formatCellValue(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.TRANSIT_MODE.getValue()))))) {
            String errorMessage = MessageFormat.format(ErrorMessages.Messages.REPEATED_INLAND_FREIGHT_METADATA_ERROR.getMessage(), ExcelConstants.ResponseRfpExcelHeaders.TRANSIT_MODE.getValue());
            errorMessageDetailsList.add(new ErrorMessageDetails(sheetName, new CellReference(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.TRANSIT_MODE.getValue()))).formatAsString(false), inlandFreight.getTransit_mode(), df.formatCellValue(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.TRANSIT_MODE.getValue()))), ExcelConstants.ResponseRfpExcelHeaders.TRANSIT_MODE.getValue(), errorMessage));
        }

        if (!Objects.equals(inlandFreight.getTransit_cost(), Utils.getFloatDoller(df.formatCellValue(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.TRANSIT_COST.getValue())))))) {
            String errorMessage = MessageFormat.format(ErrorMessages.Messages.REPEATED_INLAND_FREIGHT_METADATA_ERROR.getMessage(), ExcelConstants.ResponseRfpExcelHeaders.TRANSIT_COST.getValue());
            errorMessageDetailsList.add(new ErrorMessageDetails(sheetName, new CellReference(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.TRANSIT_COST.getValue()))).formatAsString(false), inlandFreight.getTransit_cost().toString(), Utils.getTrimmedNumber(df.formatCellValue(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.TRANSIT_COST.getValue())))), ExcelConstants.ResponseRfpExcelHeaders.TRANSIT_COST.getValue(), errorMessage));
        }

        // TODO : Any Validations for cutom_uom


        if (!Objects.equals(inlandFreight.getTransit_leadtime_in_days_port_entry_gp_mill(), Long.valueOf(Utils.getTrimmedNumber(df.formatCellValue(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.TRANSIT_LEAD_TIME_IN_DAYS_US_PORT_TO_GP_MILL.getValue()))))))) {
            String errorMessage = MessageFormat.format(ErrorMessages.Messages.REPEATED_INLAND_FREIGHT_METADATA_ERROR.getMessage(), ExcelConstants.ResponseRfpExcelHeaders.TRANSIT_LEAD_TIME_IN_DAYS_US_PORT_TO_GP_MILL.getValue());
            errorMessageDetailsList.add(new ErrorMessageDetails(sheetName, new CellReference(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.TRANSIT_LEAD_TIME_IN_DAYS_US_PORT_TO_GP_MILL.getValue()))).formatAsString(false), inlandFreight.getTransit_leadtime_in_days_port_entry_gp_mill().toString(), Utils.getTrimmedNumber(df.formatCellValue(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.TRANSIT_LEAD_TIME_IN_DAYS_US_PORT_TO_GP_MILL.getValue())))), ExcelConstants.ResponseRfpExcelHeaders.TRANSIT_LEAD_TIME_IN_DAYS_US_PORT_TO_GP_MILL.getValue(), errorMessage));
        }

    }

    //Method to Validate Fields Based On Some Condition in Logistic Pricing
    private static void validateConditionsForLogisticPricing(Row row, Map<String, Integer> columnsMap, Map<String, Integer> columnsMapMisMatch, String sheetName) {
        validateWarehouseAddress(row, columnsMap, sheetName);
        validateInlandFreightSourceName(row, columnsMap, sheetName);
        validatePortEntrySourceName(row, columnsMap, sheetName);
        validateDestinationAddress(row, columnsMap, columnsMapMisMatch, sheetName);

        //validation for Domestic Origin Port
        validateCountryAndUsPortOfEntry(row, columnsMap, sheetName);
    }


    //Method to validate WareHouse Address based on Conditions and Regex of Address
    private static void validateWarehouseAddress(Row row, Map<String, Integer> columnsMap, String sheetName) {
        String errorMessage;
        //If Safety Stock Location Type is Not Warehouse return
        if (!Objects.equals(df.formatCellValue(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.SAFETY_STOCK_LOCATION_TYPE.getValue()))).trim(), ExcelConstants.InlandTransitOriginType.WAREHOUSE.getValue())) {
            return;
        }
        Cell addressCell = row.getCell(columnsMap.get(ResponseRfpExcelHeaders.ADDRESS.getValue()));
        //Checking for Empty Address
        if (addressCell == null || df.formatCellValue(addressCell) == null || df.formatCellValue(addressCell).trim().isEmpty()) {
            errorMessage = MessageFormat.format(ErrorMessages.Messages.CONDITIONAL_MANDATORY_FIELD_ERROR.getMessage(), ResponseRfpExcelHeaders.ADDRESS.getValue(), ResponseRfpExcelHeaders.SAFETY_STOCK_LOCATION_TYPE.getValue(), ExcelConstants.InlandTransitOriginType.WAREHOUSE.getValue());
            errorMessageDetailsList.add(new ErrorMessageDetails(sheetName, new CellReference(row.getRowNum(), columnsMap.get(ResponseRfpExcelHeaders.ADDRESS.getValue())).formatAsString(false), ExcelConstants.ExpectedValuesList.MANDATORY.getValue(), df.formatCellValue(addressCell), ResponseRfpExcelHeaders.ADDRESS.getValue(), errorMessage));
        } else {
//            validateAddress(addressCell, sheetName);
        }
    }

    //Method to validate Destination Address with Regex if Inland Transit Destination type is GP Mill
    private static void validateDestinationAddress(Row row, Map<String, Integer> columnsMap, Map<String, Integer> columnsMapMisMatch, String sheetName) {
        String errorMessage;


        if (df.formatCellValue(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.INLAND_TRANSIT_DESTINATION_TYPE.getValue()))) == null || df.formatCellValue(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.INLAND_TRANSIT_DESTINATION_TYPE.getValue()))).trim().isEmpty()) {
            return;
        }

        if (!Objects.equals(df.formatCellValue(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.INLAND_TRANSIT_DESTINATION_TYPE.getValue()))), ExcelConstants.InlandTransitDestinationType.GP_MILL.getValue())) {
            Cell addressCell = row.getCell(columnsMapMisMatch.get(ResponseRfpExcelHeaders.ADDRESS.getValue()));
            String s = df.formatCellValue(addressCell);
            if (s == null || s.trim().isEmpty()) {
                errorMessage = MessageFormat.format(ErrorMessages.Messages.CONDITIONAL_MANDATORY_OTHER_THAN_FILED_ERROR.getMessage(), ResponseRfpExcelHeaders.ADDRESS.getValue(), ResponseRfpExcelHeaders.INLAND_TRANSIT_DESTINATION_TYPE.getValue(), ExcelConstants.InlandTransitDestinationType.GP_MILL.getValue());
                errorMessageDetailsList.add(new ErrorMessageDetails(sheetName, new CellReference(row.getRowNum(), columnsMapMisMatch.get(ResponseRfpExcelHeaders.ADDRESS.getValue())).formatAsString(false), ExcelConstants.ExpectedValuesList.MANDATORY.getValue(), s, ResponseRfpExcelHeaders.ADDRESS.getValue(), errorMessage));
            } else {
//                validateAddress(addressCell, sheetName);
            }
        }
    }

    //Method to Validate Headers (Headers name to Column Number validation)
    private static void validateHeaders(Row row, Map<String, Integer> headersMap, Map<String, Integer> headersMismatchMap, String sheetName) {
        String errorMessage;
        //Iterating Headers Map and Validating Name and Column Numbers
        for (Map.Entry<String, Integer> entry : headersMap.entrySet()) {
            String columnName = entry.getKey();
            Integer columnIndex = entry.getValue();

            if (!Objects.equals(row.getCell(columnIndex).toString(), columnName)) {
                errorMessage = MessageFormat.format(ErrorMessages.Messages.HEADER_INVALID.getMessage(), row.getCell(columnIndex).toString(), columnName);
                errorMessageDetailsList.add(new ErrorMessageDetails(sheetName, new CellReference(row.getRowNum(), columnIndex).formatAsString(false), columnName, row.getCell(columnIndex).toString(), null, errorMessage));
            }
        }

        //Iterating Headers Map and Validating Name and Column Numbers
        for (Map.Entry<String, Integer> entry : headersMismatchMap.entrySet()) {
            String columnName = entry.getKey();
            Integer columnIndex = entry.getValue();

            if (!Objects.equals(row.getCell(columnIndex).toString(), columnName)) {
                errorMessage = MessageFormat.format(ErrorMessages.Messages.HEADER_INVALID.getMessage(), row.getCell(columnIndex).toString(), columnName);
                errorMessageDetailsList.add(new ErrorMessageDetails(sheetName, new CellReference(row.getRowNum(), columnIndex).formatAsString(false), columnName, row.getCell(columnIndex).toString(), null, errorMessage));
            }
        }
    }

    //Method to verify If all headers are present in any sheet
    private static void verifyAllHeadersPresent(Row row, Map<String, Integer> headersMap, Map<String, Integer> headersMismatchMap, String sheetName) {
        Set<String> headersList = new HashSet<>();
        Set<String> headersListExcel = new HashSet<>();

        //Preparing the Headers Set by Iterating headers row in Excel Sheet
        for (int columnIndex = 0; columnIndex <= headersMap.size() + headersMismatchMap.size(); columnIndex++) {
            if (row.getCell(columnIndex) != null && !row.getCell(columnIndex).toString().trim().isEmpty()) {
                headersListExcel.add(row.getCell(columnIndex).toString());
            }
        }
        //Preparing the Headers set which needs to be present in Excel sheet
        headersList.addAll(headersMap.keySet());
        headersList.addAll(headersMismatchMap.keySet());

        //Finding the Set difference to check if any Header is Missing
        Set<String> missingHeaders = Sets.difference(headersList, headersListExcel);

        if (!missingHeaders.isEmpty()) {
            //Building Error Message with Comma separated Header Names which are missing
            String errorMessage = String.join(Constants.COMMA, missingHeaders);
            errorMessageDetailsList.add(new ErrorMessageDetails(sheetName, null, null, null, null, MessageFormat.format(ErrorMessages.Messages.HEADERS_NOT_FOUND.getMessage(), errorMessage)));
        }

    }

    private static boolean validateVolVarianceData(Cell bidVolVariance, String sheetName) {
        if (!validateRegex(df.formatCellValue(bidVolVariance), ExcelConstants.PERCENTAGE_REGEX)) {
            String errorMessage = MessageFormat.format(ErrorMessages.Messages.DATA_TYPE_MISMATCH.getMessage(), ExcelConstants.PERCENTAGE, df.formatCellValue(bidVolVariance));
            errorMessageDetailsList.add(new ErrorMessageDetails(sheetName, new CellReference(bidVolVariance.getRowIndex(), bidVolVariance.getColumnIndex()).formatAsString(false), ExcelConstants.PERCENTAGE, df.formatCellValue(bidVolVariance), ResponseRfpExcelHeaders.BID_VOLUME_VARIANCE.getValue(), errorMessage));
            return false;
        }
        return true;
    }

    //Method to validate if Safety Stock Location name with Port of Entry if Safety location type us Port of entry
    private static void validatePortEntrySourceName(Row row, Map<String, Integer> columnsMap, String sheetName) {

        if (df.formatCellValue(row.getCell(columnsMap.get(ResponseRfpExcelHeaders.SAFETY_STOCK_LOCATION_NAME.getValue()))) == null || df.formatCellValue(row.getCell(columnsMap.get(ResponseRfpExcelHeaders.SAFETY_STOCK_LOCATION_NAME.getValue()))).trim().isEmpty()) {
            return;
        }


        if (df.formatCellValue(row.getCell(columnsMap.get(ResponseRfpExcelHeaders.US_PORT_OF_ENTRY.getValue()))) == null || df.formatCellValue(row.getCell(columnsMap.get(ResponseRfpExcelHeaders.US_PORT_OF_ENTRY.getValue()))).trim().isEmpty()) {
            return;
        }

        if (Objects.equals(df.formatCellValue(row.getCell(columnsMap.get(ResponseRfpExcelHeaders.SAFETY_STOCK_LOCATION_TYPE.getValue()))).trim(), ExcelConstants.InlandTransitOriginType.PORT_OF_ENTRY.getValue()) && (!Objects.equals(df.formatCellValue(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.US_PORT_OF_ENTRY.getValue()))), df.formatCellValue(row.getCell(columnsMap.get(ResponseRfpExcelHeaders.SAFETY_STOCK_LOCATION_NAME.getValue())))))) {
            String errorMessage = MessageFormat.format(ErrorMessages.Messages.CONDITIONAL_FIELD_ERROR.getMessage(), ResponseRfpExcelHeaders.SAFETY_STOCK_LOCATION_TYPE.getValue(), ExcelConstants.InlandTransitOriginType.PORT_OF_ENTRY.getValue(), ResponseRfpExcelHeaders.SAFETY_STOCK_LOCATION_NAME.getValue(), df.formatCellValue(row.getCell(columnsMap.get(ResponseRfpExcelHeaders.US_PORT_OF_ENTRY.getValue()))), df.formatCellValue(row.getCell(columnsMap.get(ResponseRfpExcelHeaders.SAFETY_STOCK_LOCATION_NAME.getValue()))));
            errorMessageDetailsList.add(new ErrorMessageDetails(sheetName, new CellReference(row.getRowNum(), columnsMap.get(ResponseRfpExcelHeaders.SAFETY_STOCK_LOCATION_NAME.getValue())).formatAsString(false), df.formatCellValue(row.getCell(columnsMap.get(ResponseRfpExcelHeaders.US_PORT_OF_ENTRY.getValue()))), df.formatCellValue(row.getCell(columnsMap.get(ResponseRfpExcelHeaders.SAFETY_STOCK_LOCATION_NAME.getValue()))), ResponseRfpExcelHeaders.SAFETY_STOCK_LOCATION_NAME.getValue(), errorMessage));
        }
    }

    private static void validateConditionsForCommercialPricingDetails(Row row, Map<String, Integer> commercialPricingMap, Map<String, Integer> commercialPricingMismatchMap, String sheetName) {
        validateInitialPriceCommercialPricing(row, commercialPricingMap, sheetName);
    }

    //Method for Mandatory check for InitialPrice when Index vs Movement is Selected as Movement
    private static void validateInitialPriceCommercialPricing(Row row, Map<String, Integer> commercialPricingMap, String sheetName) {
        if (Objects.equals(df.formatCellValue(row.getCell(commercialPricingMap.get(ResponseRfpExcelHeaders.IS_MOVEMENT.getValue()))), ExcelConstants.MOVEMENT)) {
            if (row.getCell(commercialPricingMap.get(ResponseRfpExcelHeaders.INITIAL_PRICE.getValue())) == null || df.formatCellValue(row.getCell(commercialPricingMap.get(ResponseRfpExcelHeaders.INITIAL_PRICE.getValue()))) == null || df.formatCellValue(row.getCell(commercialPricingMap.get(ResponseRfpExcelHeaders.INITIAL_PRICE.getValue()))).trim().isEmpty()) {
                String errorMessage = MessageFormat.format(ErrorMessages.Messages.CONDITIONAL_MANDATORY_FIELD_ERROR.getMessage(), ResponseRfpExcelHeaders.INITIAL_PRICE.getValue(), ResponseRfpExcelHeaders.IS_MOVEMENT.getValue(), ExcelConstants.MOVEMENT);
                errorMessageDetailsList.add(new ErrorMessageDetails(sheetName, new CellReference(row.getRowNum(), commercialPricingMap.get(ResponseRfpExcelHeaders.INITIAL_PRICE.getValue())).formatAsString(false), null, null, ResponseRfpExcelHeaders.INITIAL_PRICE.getValue(), errorMessage));
            }
        }
    }

    //Method to validate  Min-Max values,to not accept the inclusion ranges
    private static void validateMinMaxInclusionRangeForVolumeTiers(List<VolumeTiers> volumeTiersList, VolumeTiers targetVolumeTier, CellReference cellReference) {
        for (VolumeTiers volumeTiers : volumeTiersList) {

            if (volumeTiers.getTier_high() != null && targetVolumeTier.getTier_high() != null) {
                if (isInclusionRange(volumeTiers.getTier_low(), volumeTiers.getTier_high(), targetVolumeTier.getTier_low()) ||
                        isInclusionRange(volumeTiers.getTier_low(), volumeTiers.getTier_high(), targetVolumeTier.getTier_high())) {
                    String errorMessage = ErrorMessages.Messages.OVERLAPPING_TIERS_NOT_ALLOWED.getMessage();
                    errorMessageDetailsList.add(new ErrorMessageDetails(ExcelConstants.ResponseRfpExcelSheetNames.COMMERCIAL_PRICING.getSheetName(), cellReference.formatAsString(false), null, null, ResponseRfpExcelHeaders.PRICE_TIER_MIN_AND_MAX.getValue(), errorMessage));
                }
            }
            if (volumeTiers.getTier_high() == null && targetVolumeTier.getTier_high() == null) {
                if (volumeTiers.getTier_low() == targetVolumeTier.getTier_low()) {
                    String errorMessage = ErrorMessages.Messages.OVERLAPPING_TIERS_NOT_ALLOWED.getMessage();
                    errorMessageDetailsList.add(new ErrorMessageDetails(ExcelConstants.ResponseRfpExcelSheetNames.COMMERCIAL_PRICING.getSheetName(), cellReference.formatAsString(false), null, null, ResponseRfpExcelHeaders.PRICE_TIER_MIN_AND_MAX.getValue(), errorMessage));
                }
            }
            if (volumeTiers.getTier_high() == null && targetVolumeTier.getTier_high() != null) {
                if (targetVolumeTier.getTier_low() <= volumeTiers.getTier_low() && volumeTiers.getTier_low() <= targetVolumeTier.getTier_high()) {
                    String errorMessage = ErrorMessages.Messages.OVERLAPPING_TIERS_NOT_ALLOWED.getMessage();
                    errorMessageDetailsList.add(new ErrorMessageDetails(ExcelConstants.ResponseRfpExcelSheetNames.COMMERCIAL_PRICING.getSheetName(), cellReference.formatAsString(false), null, null, ResponseRfpExcelHeaders.PRICE_TIER_MIN_AND_MAX.getValue(), errorMessage));
                }
            }
            if (volumeTiers.getTier_high() != null && targetVolumeTier.getTier_high() == null) {
                if (volumeTiers.getTier_low() <= targetVolumeTier.getTier_low() && targetVolumeTier.getTier_low() <= volumeTiers.getTier_high()) {
                    String errorMessage = ErrorMessages.Messages.OVERLAPPING_TIERS_NOT_ALLOWED.getMessage();
                    errorMessageDetailsList.add(new ErrorMessageDetails(ExcelConstants.ResponseRfpExcelSheetNames.COMMERCIAL_PRICING.getSheetName(), cellReference.formatAsString(false), null, null, ResponseRfpExcelHeaders.PRICE_TIER_MIN_AND_MAX.getValue(), errorMessage));
                }
            }
        }
    }

    private static void validateMinMaxInclusionRangeForPriceTiers(List<PriceTiers> priceTiersList, PriceTiers targetPriceTiers, CellReference cellReference) {

        //TODO :: clarity for zero thing for price tier high(),low()
        for (PriceTiers priceTiers : priceTiersList) {
            if (priceTiers.getTier_high() != null && targetPriceTiers.getTier_high() != null) {
                if (isInclusionRange(priceTiers.getTier_low(), priceTiers.getTier_high(), targetPriceTiers.getTier_low()) ||
                        isInclusionRange(priceTiers.getTier_low(), priceTiers.getTier_high(), targetPriceTiers.getTier_high())) {
                    String errorMessage = ErrorMessages.Messages.OVERLAPPING_TIERS_NOT_ALLOWED.getMessage();
                    errorMessageDetailsList.add(new ErrorMessageDetails(ExcelConstants.ResponseRfpExcelSheetNames.COMMERCIAL_PRICING.getSheetName(), cellReference.formatAsString(false), null, null, ResponseRfpExcelHeaders.PRICE_TIER_MIN_AND_MAX.getValue(), errorMessage));
                }
            }
            if (priceTiers.getTier_high() == null && targetPriceTiers.getTier_high() == null) {
                if (priceTiers.getTier_low() == targetPriceTiers.getTier_low()) {
                    String errorMessage = ErrorMessages.Messages.OVERLAPPING_TIERS_NOT_ALLOWED.getMessage();
                    errorMessageDetailsList.add(new ErrorMessageDetails(ExcelConstants.ResponseRfpExcelSheetNames.COMMERCIAL_PRICING.getSheetName(), cellReference.formatAsString(false), null, null, ResponseRfpExcelHeaders.PRICE_TIER_MIN_AND_MAX.getValue(), errorMessage));
                }
            }
            if (priceTiers.getTier_high() == null && targetPriceTiers.getTier_high() != null) {
                if (targetPriceTiers.getTier_low() <= priceTiers.getTier_low() && priceTiers.getTier_low() <= targetPriceTiers.getTier_high()) {
                    String errorMessage = ErrorMessages.Messages.OVERLAPPING_TIERS_NOT_ALLOWED.getMessage();
                    errorMessageDetailsList.add(new ErrorMessageDetails(ExcelConstants.ResponseRfpExcelSheetNames.COMMERCIAL_PRICING.getSheetName(), cellReference.formatAsString(false), null, null, ResponseRfpExcelHeaders.PRICE_TIER_MIN_AND_MAX.getValue(), errorMessage));
                }
            }
            if (priceTiers.getTier_high() != null && targetPriceTiers.getTier_high() == null) {
                if (priceTiers.getTier_low() <= targetPriceTiers.getTier_low() && targetPriceTiers.getTier_low() <= priceTiers.getTier_high()) {
                    String errorMessage = ErrorMessages.Messages.OVERLAPPING_TIERS_NOT_ALLOWED.getMessage();
                    errorMessageDetailsList.add(new ErrorMessageDetails(ExcelConstants.ResponseRfpExcelSheetNames.COMMERCIAL_PRICING.getSheetName(), cellReference.formatAsString(false), null, null, ResponseRfpExcelHeaders.PRICE_TIER_MIN_AND_MAX.getValue(), errorMessage));
                }
            }

        }
    }

    //Method to check the inclusionRange is present or not
    private static boolean isInclusionRange(Long rangeStart, Long rangeEnd, Long value) {
        return value >= rangeStart && value <= rangeEnd;
    }

    //Method to check total weightage in each period , should be less than 100%
    private static void checkWeightageSumValidation(List<IndexDetails> indexDetailsList, IndexDetails
            targetIndexDetail, Row row, String period) {
        if (targetIndexDetail.getWeightage_pct() != null) {
            Long weightageSum = indexDetailsList.stream().mapToLong(IndexDetails::getWeightage_pct).sum();
            weightageSum = weightageSum + targetIndexDetail.getWeightage_pct();
            if (weightageSum > 100) {
                String errorMessage = MessageFormat.format(ErrorMessages.Messages.INDEX_WEIGHTAGE_OVER_FLOW.getMessage(), weightageSum);
                errorMessageDetailsList.add(new ErrorMessageDetails(ExcelConstants.ResponseRfpExcelSheetNames.COMMERCIAL_PRICING.getSheetName(), new CellReference(row.getRowNum(), commercialPricingMismatchMap.get(ResponseRfpExcelHeaders.INDEX_WEIGHTAGE.getValue())).formatAsString(false), null, null, ResponseRfpExcelHeaders.INDEX_WEIGHTAGE.getValue(), errorMessage));
            }
        } else {
            String errorMessage = MessageFormat.format(ErrorMessages.Messages.MANDATORY_FIELD_MISSING_ERROR.getMessage(), ResponseRfpExcelHeaders.INDEX_WEIGHTAGE.getValue());
            errorMessageDetailsList.add(new ErrorMessageDetails(ExcelConstants.ResponseRfpExcelSheetNames.COMMERCIAL_PRICING.getSheetName(), new CellReference(row.getRowNum(), commercialPricingMismatchMap.get(ResponseRfpExcelHeaders.INDEX_WEIGHTAGE.getValue())).formatAsString(false), null, null, ResponseRfpExcelHeaders.INDEX_WEIGHTAGE.getValue(), errorMessage));
        }
    }


    // Method to generate Response Rfp Excel for Specific rfp using rfp Details
    public static String generateResponseRfpExcelTemplate(String rfpNumber, Integer contractTerm, String fiberType, List<Mills> millList, String excelTemplateName, String excelSavingPath, String supplierName, String mailId, String dueDate) throws IOException {
        String outPutPath = generateOutPathForResponseRfpExcelTemplate(excelSavingPath, rfpNumber, contractTerm, supplierName, fiberType);
        String fiberSpecificFilePath = excelSavingPath + excelTemplateName + Constants.UNDER_SCORE + fiberType + Constants.EXCEL;
        try (InputStream inputStream = new FileInputStream(new File(fiberSpecificFilePath));
             Workbook workbook = WorkbookFactory.create(inputStream)) {
            //Populates the given data in Template
            populateDataInTemplate(workbook, rfpNumber, contractTerm, fiberType, millList, supplierName, mailId, dueDate);
            // Save the file to outPutPath
            Utils.saveWorkbookToExcel(workbook, outPutPath);
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException(ErrorMessages.FILE_NOT_FOUND);
        } catch (DecoderException e) {
            //TODO : Throw Specific Exception and add in GlobalExceptionHandlerController
            throw new RuntimeException(e);
        }
        return outPutPath;
    }

    public static String generateOutPathForResponseRfpExcelTemplate(String excelSavingPath, String rfpNumber, Integer contractTerm, String supplierName, String fiberType) {
        return excelSavingPath + rfpNumber + Constants.UNDER_SCORE + contractTerm + Constants.UNDER_SCORE + supplierName + Constants.UNDER_SCORE + fiberType + Constants.EXCEL;
    }


    //method to set Rfp details in the Bid Qty Details Sheet
    private static void populateDataInTemplate(Workbook workbook, String rfpNumber, Integer contractTerm, String fiberType, List<Mills> millList, String supplierName, String mailId, String dueDate) throws FileNotFoundException, DecoderException {
        Sheet bidQtySheet = workbook.getSheet(ExcelConstants.ResponseRfpExcelSheetNames.BID_DETAILS.getSheetName());
        if (bidQtySheet == null) {
            throw new FileNotFoundException(MessageFormat.format(ErrorMessages.SHEET_NOT_FOUND_ERROR, ExcelConstants.ResponseRfpExcelSheetNames.BID_DETAILS.getSheetName()));
        }

        //Setting fiberType and Contract Term , Rfp Number, supplier name, contact email and Due Date
        Utils.setHeaderValueInExcelCell(workbook, bidQtySheet, new CellReference(ExcelConstants.COLUMN_NAME_TO_CELL_REFERENCE.get(ResponseRfpExcelHeaders.FIBER_TYPE.getValue())), ResponseRfpExcelHeaders.FIBER_TYPE.getValue(), fiberType);
        Utils.setHeaderValueInExcelCell(workbook, bidQtySheet, new CellReference(ExcelConstants.COLUMN_NAME_TO_CELL_REFERENCE.get(ResponseRfpExcelHeaders.CONTRACT_TERM.getValue())), ResponseRfpExcelHeaders.CONTRACT_TERM.getValue(), String.valueOf(contractTerm));
        Utils.setHeaderValueInExcelCell(workbook, bidQtySheet, new CellReference(ExcelConstants.COLUMN_NAME_TO_CELL_REFERENCE.get(ResponseRfpExcelHeaders.RFP_NUMBER.getValue())), ResponseRfpExcelHeaders.RFP_NUMBER.getValue(), rfpNumber);
        Utils.setHeaderValueInExcelCell(workbook, bidQtySheet, new CellReference(ExcelConstants.COLUMN_NAME_TO_CELL_REFERENCE.get(ResponseRfpExcelHeaders.SUPPLIER_NAME.getValue())), ResponseRfpExcelHeaders.SUPPLIER_NAME.getValue(), supplierName);
        Utils.setHeaderValueInExcelCell(workbook, bidQtySheet, new CellReference(ExcelConstants.COLUMN_NAME_TO_CELL_REFERENCE.get(ResponseRfpExcelHeaders.CONTACT_EMAIL.getValue())), ResponseRfpExcelHeaders.CONTACT_EMAIL.getValue(), mailId.replaceAll(Constants.COMMA, ExcelConstants.COMMA_WITH_SPACE));
        Utils.setHeaderValueInExcelCell(workbook, bidQtySheet, new CellReference(ExcelConstants.COLUMN_NAME_TO_CELL_REFERENCE.get(ResponseRfpExcelHeaders.DUE_DATE.getValue())), ResponseRfpExcelHeaders.DUE_DATE.getValue(), dueDate);


        Integer millNameColumnIndex = ExcelConstants.BID_DETAILS_MAP.get(ExcelConstants.ResponseRfpExcelHeaders.GP_MILL_COL_NUMBER.getValue());
        Integer stateColumnIndex = ExcelConstants.BID_DETAILS_MAP.get(ResponseRfpExcelHeaders.GP_MILL_STATE_COL.getValue());
        Integer expectedAnnualVolColIndex = ExcelConstants.BID_DETAILS_MAP.get(ResponseRfpExcelHeaders.GP_MILL_EXPECTED_ANNUAL_VOLUME_COL_NUM.getValue());
        Integer rowIndex = ExcelConstants.BID_DETAILS_MAP.get(ResponseRfpExcelHeaders.GP_MILL_START_ROW.getValue());


        Integer startColumnIndex = ExcelConstants.BID_DETAILS_MAP.get(ExcelConstants.ResponseRfpExcelHeaders.GP_MILL_COL_NUMBER.getValue());
        Integer endColumnIndex = Constants.END_COLUMN_FOR_BORDER;


        Long totalExpectedAnnualVolume = 0L;
        //Inserting rows (number of Mills) after Example row (Todo it will adjust next 70 rows)
        //This function will insert specified number of rows in between the rows that specified(i.e it will insert no.of mills +1 rows in between Gp mill start row and 70)
        bidQtySheet.shiftRows(ExcelConstants.BID_DETAILS_MAP.get(ResponseRfpExcelHeaders.GP_MILL_START_ROW.getValue()), 70, millList.size() + 1, false, true);

        //setting Mills Details List
        for (Mills millDetails : millList) {
            setBorderToCell(workbook, rowIndex, startColumnIndex, endColumnIndex, ExcelConstants.CELL_VALUE_COLOUR_CODE);
            setCellValueInExcel(millDetails.getMillName(), workbook, bidQtySheet, rowIndex, millNameColumnIndex, Boolean.FALSE, ExcelConstants.CELL_HEADER_COLOUR_CODE);
            setCellValueInExcel(millDetails.getState(), workbook, bidQtySheet, rowIndex, stateColumnIndex, Boolean.FALSE, ExcelConstants.CELL_HEADER_COLOUR_CODE);
            setCellValueInExcel(Utils.formatWithCommas(millDetails.getExpectedAnnualVolume()), workbook, bidQtySheet, rowIndex, expectedAnnualVolColIndex, Boolean.TRUE, ExcelConstants.CELL_HEADER_COLOUR_CODE);
            totalExpectedAnnualVolume += millDetails.getExpectedAnnualVolume();
            rowIndex++;
        }

        //Adding Total At end of GpMills And Total Volume
        setBorderToCell(workbook, rowIndex, startColumnIndex, endColumnIndex, ExcelConstants.CELL_VALUE_COLOUR_CODE);
        Utils.setCellValueWithBorderInExcel(workbook, bidQtySheet, new CellReference(bidQtySheet.getRow(rowIndex).getCell(millNameColumnIndex)), ResponseRfpExcelHeaders.TOTAL_BID_VOLUME_COL_NUM.getValue(), Constants.EMPTY_STRING, Boolean.FALSE, ExcelConstants.CELL_HEADER_COLOUR_CODE);
        Utils.setCellValueWithBorderInExcel(workbook, bidQtySheet, new CellReference(bidQtySheet.getRow(rowIndex).getCell(stateColumnIndex)), Constants.EMPTY_STRING, Constants.EMPTY_STRING, Boolean.FALSE, ExcelConstants.CELL_HEADER_COLOUR_CODE);
        Utils.setCellValueWithBorderInExcel(workbook, bidQtySheet, new CellReference(bidQtySheet.getRow(rowIndex).getCell(expectedAnnualVolColIndex)), Utils.formatWithCommas(totalExpectedAnnualVolume), Constants.EMPTY_STRING, Boolean.TRUE, ExcelConstants.CELL_HEADER_COLOUR_CODE);
    }


    //Setting cell Value In Excel
    private static void setCellValueInExcel(String cellValue, Workbook workbook, Sheet sheet, Integer rowIndex, Integer columnIndex, Boolean alignRigh, String cellColourCode) throws DecoderException {

        int rowNumber = rowIndex;
        int columnNumber = columnIndex;

        Row row = sheet.getRow(rowNumber);
        if (row == null) {
            row = sheet.createRow(rowNumber);
        }

        Cell cell = row.getCell(columnNumber);
        if (cell == null) {
            cell = row.createCell(columnNumber);
        }

        // Create a CellStyle with bold font
        XSSFCellStyle cellStyle = (XSSFCellStyle) workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontName(ExcelConstants.FONT_ARIAL);
        cellStyle.setFont(font);
        if (Boolean.TRUE.equals(alignRigh))
            cellStyle.setAlignment(HorizontalAlignment.RIGHT);


        //Setting color
        String rgbs = cellColourCode;
        byte[] rgbB = Hex.decodeHex(rgbs); // get byte array from hex string
        XSSFColor color = new XSSFColor(rgbB, null); //IndexedColorMap has no usage until now. So it can be set null.

        cellStyle.setFillForegroundColor(color);
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);


        // Set border styles
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);


        // Apply the CellStyle to the cell
        cell.setCellStyle(cellStyle);

        //setting cellValue
        cell.setCellValue(cellValue);
    }

    private static void setBorderToCell(Workbook workbook, Integer rowIndex, Integer startColumnIndex, Integer endColumnIndex, String cellColourCode) throws DecoderException {
        Sheet bidQtySheet = workbook.getSheet(ExcelConstants.ResponseRfpExcelSheetNames.BID_DETAILS.getSheetName());
        Row row = bidQtySheet.createRow(rowIndex);
        //Set Row Size Same as Example value
        row.setHeightInPoints(bidQtySheet.getRow(ExcelConstants.BID_DETAILS_MAP.get(ResponseRfpExcelHeaders.GP_MILL_EXAMPLE_ROW.getValue())).getHeightInPoints());


        // Create a CellStyle with borders
        XSSFCellStyle cellStyle = (XSSFCellStyle) workbook.createCellStyle();
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);


        //Setting color
        String rgbs = cellColourCode;
        byte[] rgbB = Hex.decodeHex(rgbs); // get byte array from hex string
        XSSFColor color = new XSSFColor(rgbB, null); //IndexedColorMap has no usage until now. So it can be set null.

        cellStyle.setFillForegroundColor(color);
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);


        // Apply the CellStyle to cells up to column index F
        int lastColumnIndex = endColumnIndex; // Column index F (0-based index)
        for (int column = startColumnIndex; column <= lastColumnIndex; column++) {
            Cell cell = row.getCell(column);
            if (cell == null) {
                cell = row.createCell(column);
                if (column > 2)
                    cellStyle.setDataFormat(workbook.createDataFormat().getFormat(ExcelConstants.THOUSAND_SAPARATOR_REGEX)); // Set the format with thousand separators
            }
            cell.setCellStyle(cellStyle);
        }
    }


    //Method to check whether Volume-Tier-Discount is Opted or Not (Y/N)
    private static boolean checkVolumeTierDiscountOptedOrNot(Row row, boolean isVolumeTierDiscounts, VolumeTierDiscounts volumeTierDiscounts) {
        if (Boolean.TRUE.equals(Utils.checkIfStringIsNullOrEmpty(df.formatCellValue(row.getCell(commercialPricingMap.get(ResponseRfpExcelHeaders.IS_VOLUME_BASED_PRICING.getValue())))))) {
            String errorMessage = MessageFormat.format(ErrorMessages.Messages.MANDATORY_FIELD_MISSING_ERROR.getMessage(), ResponseRfpExcelHeaders.IS_VOLUME_BASED_PRICING.getValue());
            errorMessageDetailsList.add(new ErrorMessageDetails(ExcelConstants.ResponseRfpExcelSheetNames.COMMERCIAL_PRICING.getSheetName(), new CellReference(row.getRowNum(), commercialPricingMap.get(ResponseRfpExcelHeaders.IS_VOLUME_BASED_PRICING.getValue())).formatAsString(false), null, df.formatCellValue(row.getCell(commercialPricingMap.get(ResponseRfpExcelHeaders.IS_VOLUME_BASED_PRICING.getValue()))), ResponseRfpExcelHeaders.IS_VOLUME_BASED_PRICING.getValue(), errorMessage));
            isVolumeTierDiscounts = false;
            volumeTierDiscounts = null;
        }
        //if Volume Tier Discount is No
        if (df.formatCellValue(row.getCell(commercialPricingMap.get(ResponseRfpExcelHeaders.IS_VOLUME_BASED_PRICING.getValue()))).equalsIgnoreCase(Constants.NO.toString())) {
            isVolumeTierDiscounts = false;
            volumeTierDiscounts = null;
        }
        return isVolumeTierDiscounts;
    }

    //Method to check whether Price-Tier-Discount is Opted or Not (Y/N)
    private static boolean checkPriceTierDiscountOptedOrNot(Row row, Boolean isPriceTierDiscounts, PriceTierDiscounts priceTierDiscounts) {
        if (Boolean.TRUE.equals(Utils.checkIfStringIsNullOrEmpty(df.formatCellValue(row.getCell(commercialPricingMap.get(ResponseRfpExcelHeaders.IS_TIER_BASED_PRICING.getValue())))))) {
            String errorMessage = MessageFormat.format(ErrorMessages.Messages.MANDATORY_FIELD_MISSING_ERROR.getMessage(), ResponseRfpExcelHeaders.IS_TIER_BASED_PRICING.getValue());
            errorMessageDetailsList.add(new ErrorMessageDetails(ExcelConstants.ResponseRfpExcelSheetNames.COMMERCIAL_PRICING.getSheetName(), new CellReference(row.getRowNum(), commercialPricingMap.get(ResponseRfpExcelHeaders.IS_TIER_BASED_PRICING.getValue())).formatAsString(false), null, df.formatCellValue(row.getCell(commercialPricingMap.get(ResponseRfpExcelHeaders.IS_TIER_BASED_PRICING.getValue()))), ResponseRfpExcelHeaders.IS_TIER_BASED_PRICING.getValue(), errorMessage));
            isPriceTierDiscounts = false;
        }
        //if Price Tier Discount is No
        if (df.formatCellValue(row.getCell(commercialPricingMap.get(ResponseRfpExcelHeaders.IS_TIER_BASED_PRICING.getValue()))).equalsIgnoreCase(Constants.NO.toString())) {
            isPriceTierDiscounts = false;
        }
        return isPriceTierDiscounts;
    }


//    //Method to set PeriodType For Commercial Sheet
//    private static void setPeriodType(CommercialPricingParsingRows parsingRows, List<String> periodList, String initialPeriod) {
//        Set<String> periodSet = new HashSet<>(periodList);
//        if ((initialPeriod.equals(Constants.QUARTER_1) && periodSet.containsAll(ExcelConstants.PERIOD_MAP_FOR_COMMERCIAL_PRICING.get(Constants.QUARTERLY)) && periodList.size() >= Constants.NO_OF_QUARTERS)) {
//            parsingRows.setPeriodType(Constants.PERIOD_QUARTER_YEAR);
//        }
//        else if (initialPeriod.equals(Constants.MONTH_1) && periodSet.containsAll(ExcelConstants.PERIOD_MAP_FOR_COMMERCIAL_PRICING.get(Constants.MONTHLY)) && periodList.size() >= Constants.NO_OF_MONTHS) {
//            parsingRows.setPeriodType(Constants.PERIOD_MONTH);
//        }
//        else if (initialPeriod.equals(Constants.HALF_YEARLY_1) && periodSet.containsAll(ExcelConstants.PERIOD_MAP_FOR_COMMERCIAL_PRICING.get(Constants.HALF_YEARLY)) && periodList.size() >= Constants.NO_OF_HALF_YEARS) {
//            parsingRows.setPeriodType(Constants.PERIOD_HALF_YEAR);
//        }
//        else if(initialPeriod.equals(Constants.YEARLY) && periodList.size() == Constants.NO_OF_YEAR){
//            parsingRows.setPeriodType(Constants.PERIOD_FULL_YEAR);
//        }
//    }

    //method to fetch periods in commercial Sheet and prepare a list
    private static List<String> getPeriodListForCommercialSheet(Sheet sheet, int columnIdx) {
        List<String> periodList = new ArrayList<>();
        for (Row row : sheet) {
            if (row.getRowNum() >= ExcelConstants.EXCEL_COMMERCIAL_RESPONSE_START_ROW) {
                Cell cell = row.getCell(columnIdx, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                if (cell != null && cell.getCellType() != CellType.BLANK) {
                    periodList.add(df.formatCellValue(row.getCell(commercialPricingMap.get(ResponseRfpExcelHeaders.TIME_WINDOW_PERIOD.getValue()))));
                }
            }
        }
        return periodList;
    }

    //Method to set Time Window for Commercial Sheet
    private static void parseTimeWindowForCommercialSheet(String timeWindow, Row row, PricingDetail pricingDetail) {
        if (timeWindow.contains(Constants.MONTH)) {
            timeWindow = timeWindow.trim().replace(Constants.MONTH, Constants.EMPTY_STRING);
        } else if (timeWindow.contains(Constants.MONTHS)) {
            timeWindow = timeWindow.trim().replace(Constants.MONTHS_PLURAL, Constants.EMPTY_STRING);
        }
        if (validateRegex(timeWindow, ExcelConstants.ALPHA_NUMERIC_REGEX) && Long.valueOf(timeWindow) <= 12) {
            pricingDetail.setTime_window(Long.valueOf(timeWindow));
        } else {
            String errorMessage = MessageFormat.format(ErrorMessages.Messages.TIME_WINDOW_ERROR.getMessage(), df.formatCellValue(row.getCell(commercialPricingMap.get(ResponseRfpExcelHeaders.TIME_WINDOW.getValue()))));
            errorMessageDetailsList.add(new ErrorMessageDetails(ExcelConstants.ResponseRfpExcelSheetNames.COMMERCIAL_PRICING.getSheetName(), new CellReference(row.getRowNum(), commercialPricingMap.get(ResponseRfpExcelHeaders.TIME_WINDOW.getValue())).formatAsString(false), null, null, ResponseRfpExcelHeaders.TIME_WINDOW.getValue(), errorMessage));
        }
    }

    //Method to set Volume Tier Discount Values based on Discount Type
    private static void parseVolumeTierDiscountBasedOnType(VolumeTiers volumeTiers, Row row) {
        if (checkIsEmptyStringAndDataFormat(row, ResponseRfpExcelHeaders.VOLUME_TIER_ADDITIONAL_DISCOUNT.getValue(), ExcelConstants.PERCENT_OR_DOLLAR_REGEX, ExcelConstants.ResponseRfpExcelSheetNames.COMMERCIAL_PRICING.getSheetName(), ExcelConstants.PERCENT_OR_DOLLAR, commercialPricingMismatchMap, Boolean.FALSE, Boolean.FALSE)) {
            String volumeTierDiscount = df.formatCellValue(row.getCell(commercialPricingMismatchMap.get(ResponseRfpExcelHeaders.VOLUME_TIER_ADDITIONAL_DISCOUNT.getValue())));
            if (volumeTierDiscount.contains(Constants.PERCENT)) {
                volumeTiers.setDiscount_type(Constants.PERCENT_STRING);
                //TODO : Uncomment for Old sheet
//                parseVolumeTierDiscountPercentValue(volumeTierDiscount, row, volumeTiers);
            } else if (volumeTierDiscount.contains(Constants.DOLLAR)) {
                volumeTiers.setDiscount_val(Utils.getLongDoller(volumeTierDiscount));
                volumeTiers.setDiscount_type(Constants.VALUE_STRING);
            }
        }
    }

    //Method to set Volume Tier Discount Value if its percentage type
    private static void parseVolumeTierDiscountPercentValue(String volumeTierDiscount, CellReference cellReference, VolumeTiers volumeTiers) {
        if (Float.valueOf(getNumericString(volumeTierDiscount, Constants.PERCENT).trim()) <= 100) {
            volumeTiers.setDiscount_pct(Float.valueOf(getNumericString(volumeTierDiscount, Constants.PERCENT).trim()));
        } else {
            String errorMessage = MessageFormat.format(ErrorMessages.Messages.ADDITIONAL_DISCOUNT_OVER_FLOW.getMessage(), volumeTierDiscount);
            errorMessageDetailsList.add(new ErrorMessageDetails(ExcelConstants.ResponseRfpExcelSheetNames.COMMERCIAL_PRICING.getSheetName(), cellReference.formatAsString(false), null, null, ResponseRfpExcelHeaders.VOLUME_TIER_ADDITIONAL_DISCOUNT.getValue(), errorMessage));
        }
    }

    //Method to set the Price Tier Discount Value if its Percent Type
    private static void parsePriceTierDiscountPercentValue(String additionalDiscount, CellReference cellReference, PriceTiers priceTiers) {
        if (Float.parseFloat(getNumericString(additionalDiscount, Constants.PERCENT)) <= 100) {
            priceTiers.setDiscount_pct(Float.valueOf(getNumericString(additionalDiscount, Constants.PERCENT)));
        } else {
            String errorMessage = MessageFormat.format(ErrorMessages.Messages.ADDITIONAL_DISCOUNT_OVER_FLOW.getMessage(), additionalDiscount);
            errorMessageDetailsList.add(new ErrorMessageDetails(ExcelConstants.ResponseRfpExcelSheetNames.COMMERCIAL_PRICING.getSheetName(), cellReference.formatAsString(false), null, null, ResponseRfpExcelHeaders.TIER_ADDITIONAL_DISCOUNT.getValue(), errorMessage));
        }
    }

    //Method to check the Lower Range value is greater than or equal to Higher range
    private static void validateMinMaxPrice(Long tierLow, Long tierHigh, CellReference cellReference, String columnName) {
        if (tierLow >= tierHigh) {
            String errorMessage = MessageFormat.format(ErrorMessages.Messages.MIN_MAX_DATA_ERROR.getMessage(), tierLow + " - " + tierHigh);
            errorMessageDetailsList.add(new ErrorMessageDetails(ExcelConstants.ResponseRfpExcelSheetNames.COMMERCIAL_PRICING.getSheetName(), cellReference.formatAsString(false), null, null, columnName, errorMessage));
        }
    }

    //method to Parse the Fixed Price Details for Hybrid Mechanism
    private static void parseFixedPriceDetailsForHybridMechanism(FixedPriceDetails fixedPriceDetails, List<PeriodDetail> periodDetailList, Row row, int f) {
        for (PeriodDetail periodDetail1 : periodDetailList) {
            if (checkIsEmptyStringAndDataFormat(row, ResponseRfpExcelHeaders.TIME_WINDOW_PERIOD.getValue(), ExcelConstants.ALPHA_NUMERIC_REGEX, ExcelConstants.ResponseRfpExcelSheetNames.COMMERCIAL_PRICING.getSheetName(), ExcelConstants.ALPHA_NUMERIC, commercialPricingMap, Boolean.TRUE, Boolean.FALSE)) {
                if (periodDetail1.getPeriod().equalsIgnoreCase(df.formatCellValue(row.getCell(commercialPricingMap.get(ResponseRfpExcelHeaders.TIME_WINDOW_PERIOD.getValue()))))) {
                    f = 1;
                    break;
                }
            }
        }
        if (f == 0) {
            if (checkIsEmptyStringAndDataFormat(row, ResponseRfpExcelHeaders.FIXED_PRICE.getValue(), ExcelConstants.DOLLER_REGEX, ExcelConstants.ResponseRfpExcelSheetNames.COMMERCIAL_PRICING.getSheetName(), ExcelConstants.DOLLER, commercialPricingMap, Boolean.TRUE, Boolean.FALSE)) {
                fixedPriceDetails.setFixed_price_value(Utils.getFloatDoller(df.formatCellValue(row.getCell(commercialPricingMap.get(ResponseRfpExcelHeaders.FIXED_PRICE.getValue())))));
            }
            if (checkIsEmptyStringAndDataFormat(row, ResponseRfpExcelHeaders.FIXED_PRICE_WEIGHTAGE.getValue(), ExcelConstants.PERCENT_OR_DOLLAR_REGEX, ExcelConstants.ResponseRfpExcelSheetNames.COMMERCIAL_PRICING.getSheetName(), ExcelConstants.PERCENTAGE, commercialPricingMap, Boolean.TRUE, Boolean.FALSE)) {
                if (validateRegex(df.formatCellValue(row.getCell(commercialPricingMap.get(ResponseRfpExcelHeaders.FIXED_PRICE_WEIGHTAGE.getValue()))), ExcelConstants.PERCENTAGE_REGEX)) {
                    fixedPriceDetails.setWeightage_pct(Long.valueOf(df.formatCellValue(row.getCell(commercialPricingMap.get(ResponseRfpExcelHeaders.FIXED_PRICE_WEIGHTAGE.getValue()))).replace(Constants.PERCENT, Constants.EMPTY_STRING)));
                } else {
                    String errorMessage = MessageFormat.format(ErrorMessages.Messages.PERCENTAGE_OVER_100.getMessage(), df.formatCellValue(row.getCell(commercialPricingMap.get(ResponseRfpExcelHeaders.FIXED_PRICE_WEIGHTAGE.getValue()))));
                    errorMessageDetailsList.add(new ErrorMessageDetails(ExcelConstants.ResponseRfpExcelSheetNames.COMMERCIAL_PRICING.getSheetName(), new CellReference(row.getRowNum(), commercialPricingMap.get(ResponseRfpExcelHeaders.FIXED_PRICE_WEIGHTAGE.getValue())).formatAsString(false), null, null, ResponseRfpExcelHeaders.FIXED_PRICE_WEIGHTAGE.getValue(), errorMessage));
                }
            }
        }
    }

    //Method to parse the Period Details  for Commercial Sheet based on Mechanism is Hybrid or Index Based
    private static void parseNewPeriodDetailsForCommercialSheet(IndexDetails indexDetails, List<PeriodDetail> periodDetailList, CommercialPricingParsingRows parsingRows, Row row, String mechanismType, FixedPriceDetails fixedPriceDetails) {
        PeriodDetail periodDetail = new PeriodDetail();
        if (checkIsEmptyStringAndDataFormat(row, ResponseRfpExcelHeaders.TIME_WINDOW_PERIOD.getValue(), ExcelConstants.ALPHA_NUMERIC_REGEX, ExcelConstants.ResponseRfpExcelSheetNames.COMMERCIAL_PRICING.getSheetName(), ExcelConstants.ALPHA_NUMERIC, commercialPricingMap, Boolean.TRUE, Boolean.FALSE)) {
            periodDetail.setPeriod(df.formatCellValue(row.getCell(commercialPricingMap.get(ResponseRfpExcelHeaders.TIME_WINDOW_PERIOD.getValue()))));
            periodDetail.setPeriod_type(periodMapCommercial.get(periodDetail.getPeriod().charAt(Constants.ZERO)));
            int lengthOfPeriod = periodDetail.getPeriod().length();
//            String periodNum = (lengthOfPeriod <= 3) ? periodDetail.getPeriod().substring(1) : periodDetail.getPeriod().substring(5);
            //setting period_num in ascending number order
            periodDetail.setPeriod_num((long) periodDetailList.size() + 1);
            //Period details if Mechanism is HYBRID Based, we will add fixed_Price_details also
            if (mechanismType.equalsIgnoreCase(Constants.MECHANISMS.HYBRID.getValue())) {
                periodDetail.setFixed_price_details(fixedPriceDetails);
            } else {
                periodDetail.setFixed_price_details(new FixedPriceDetails());
            }
            List<IndexDetails> indexDetailsList = new ArrayList<>();
            if (indexDetails.getWeightage_pct() != null) {
                indexDetailsList.add(indexDetails);
                periodDetail.setIndex_details(indexDetailsList);
                periodDetailList.add(periodDetail);
            }
//                else{
//                    String error = MessageFormat.format(ErrorMessages.MANDATORY_FIELD_MISSING_ERROR,ResponseRfpExcelHeaders.INDEX_WEIGHTAGE.getValue(),ExcelConstants.ResponseRfpExcelSheetNames.COMMERCIAL_PRICING.getSheetName(),new CellReference(row.getRowNum(),commercialPricingMismatchMap.get(ResponseRfpExcelHeaders.INDEX_WEIGHTAGE.getValue())).formatAsString(false));
//                    errorMessagesListInExcel.add(error);
//                    errorMessageDetailsList.add(new ErrorMessageDetails(ExcelConstants.ResponseRfpExcelSheetNames.COMMERCIAL_PRICING.getSheetName(), new CellReference(row.getRowNum(), commercialPricingMismatchMap.get(ResponseRfpExcelHeaders.INDEX_WEIGHTAGE.getValue())).formatAsString(false), null, null, ResponseRfpExcelHeaders.INDEX_WEIGHTAGE.getValue(), error));
//                }
        }
    }

    //Method to set TIme Window and TIme Window Period
    private static void parseTimeWindowAndTimeWindowPeriodForCommercialSheet(String timeWindow, Row row, PricingDetail pricingDetail) {
        if (timeWindow == null || timeWindow.trim().isEmpty()) {
            String errorMessage = MessageFormat.format(ErrorMessages.Messages.MANDATORY_FIELD_MISSING_ERROR.getMessage(), ResponseRfpExcelHeaders.TIME_WINDOW.getValue());
            errorMessageDetailsList.add(new ErrorMessageDetails(ExcelConstants.ResponseRfpExcelSheetNames.COMMERCIAL_PRICING.getSheetName(), new CellReference(row.getRowNum(), commercialPricingMap.get(ResponseRfpExcelHeaders.TIME_WINDOW.getValue())).formatAsString(false), null, null, ResponseRfpExcelHeaders.TIME_WINDOW.getValue(), errorMessage));
        } else {
            String[] tokens = timeWindow.trim().split(Constants.SPACE);
            if (tokens.length != 2 || !validateRegex(tokens[0], ExcelConstants.NUMBER_REGEX)) {
                String errorMessage = MessageFormat.format(ErrorMessages.Messages.TIME_WINDOW_ERROR.getMessage(), df.formatCellValue(row.getCell(commercialPricingMap.get(ResponseRfpExcelHeaders.TIME_WINDOW.getValue()))));
                errorMessageDetailsList.add(new ErrorMessageDetails(ExcelConstants.ResponseRfpExcelSheetNames.COMMERCIAL_PRICING.getSheetName(), new CellReference(row.getRowNum(), commercialPricingMap.get(ResponseRfpExcelHeaders.TIME_WINDOW.getValue())).formatAsString(false), null, null, ResponseRfpExcelHeaders.TIME_WINDOW.getValue(), errorMessage));
            } else {
                int timeWindowNumber = Integer.parseInt(tokens[0]);
                String timeWindowPeriod = tokens[1];

                pricingDetail.setTime_window((long) timeWindowNumber);
                if (timeWindowPeriod != null && !timeWindowPeriod.trim().isEmpty()) {
                    pricingDetail.setTime_window_period(timeWindowPeriod);
                }
            }
        }
    }


    private static void validatePeriodPricingDetails(Row row, Integer columnIndex, String sheetName) {
        String period = df.formatCellValue(row.getCell(columnIndex));

        if (periodIndexMap.get(period) == null || periodList.contains(period)) return;

        periodList.add(period);

        Boolean overlapping = Boolean.FALSE;

        for (Integer i : periodIndexMap.get(period)) {
            if (periodArray[i] == 1) {
                overlapping = true;
                break;
            }
        }

        if (!overlapping) {
            for (Integer i : periodIndexMap.get(period)) {
                periodArray[i] = 1;
            }
        } else {
            String errorMessage = MessageFormat.format(ErrorMessages.Messages.INVALID_PERIOD_ERROR.getMessage(), period);
            errorMessageDetailsList.add(new ErrorMessageDetails(ExcelConstants.ResponseRfpExcelSheetNames.COMMERCIAL_PRICING.getSheetName(), new CellReference(row.getRowNum(), columnIndex).formatAsString(false), null, period, ResponseRfpExcelHeaders.TIME_WINDOW_PERIOD.getValue(), errorMessage));
        }

    }

    private static void validateAddress(Cell addressCell, String sheetName) {
        String addressString = df.formatCellValue(addressCell);
        String[] addressArray = addressString.split(Constants.COMMA);
        String errorMessage;
        //Checking For length
        if (addressArray.length > ExcelConstants.ADDRESS_STRING_REQUIRED_FIELDS_COUNT) {
            errorMessage = MessageFormat.format(ErrorMessages.Messages.DATA_FORMAT_MISMATCH.getMessage(), ExcelConstants.ADDRESS_FORMAT, addressString);
            errorMessageDetailsList.add(new ErrorMessageDetails(sheetName, new CellReference(addressCell.getRowIndex(), addressCell.getColumnIndex()).formatAsString(false), ExcelConstants.ADDRESS_FORMAT, addressString, ResponseRfpExcelHeaders.ADDRESS.getValue(), errorMessage));
            return;
        }
//        if(addressArray.length!=ExcelConstants.ADDRESS_STRING_REQUIRED_FIELDS_COUNT){
//            int i = 0;
//        }
//        //validate city
//         if (addressArray.length>=3 && !validateRegex(addressArray[2], ExcelConstants.ALPHABETS_REGEX)) {
//             errorMessage = MessageFormat.format(ErrorMessages.Messages.DATA_TYPE_MISMATCH.getMessage(), ExcelConstants.ALPHABETS, addressArray[2]);
//             errorMessageDetailsList.add(new ErrorMessageDetails(sheetName, new CellReference(addressCell.getRowIndex(), addressCell.getColumnIndex()).formatAsString(false), ExcelConstants.ALPHABETS, addressArray[2], ResponseRfpExcelHeaders.ADDRESS.getValue() + Constants.SPACE + ExcelConstants.AddressFields.CITY.getValue(), errorMessage));
//         }
//         //validate state
//         if (addressArray.length>=4 && !validateRegex(addressArray[3], ExcelConstants.ALPHABETS_REGEX)) {
//             errorMessage = MessageFormat.format(ErrorMessages.Messages.DATA_TYPE_MISMATCH.getMessage(), ExcelConstants.ALPHABETS, addressArray[3]);
//             errorMessageDetailsList.add(new ErrorMessageDetails(sheetName, new CellReference(addressCell.getRowIndex(), addressCell.getColumnIndex()).formatAsString(false), ExcelConstants.ALPHABETS, addressArray[3], ResponseRfpExcelHeaders.ADDRESS.getValue() + Constants.SPACE + ExcelConstants.AddressFields.STATE.getValue(), errorMessage));
//         }
//         //Validate country
//         if (addressArray.length>=5 && !validateRegex(addressArray[4], ExcelConstants.ALPHABETS_REGEX)) {
//             errorMessage = MessageFormat.format(ErrorMessages.Messages.DATA_TYPE_MISMATCH.getMessage(), ExcelConstants.ALPHABETS, addressArray[4]);
//             errorMessageDetailsList.add(new ErrorMessageDetails(sheetName, new CellReference(addressCell.getRowIndex(), addressCell.getColumnIndex()).formatAsString(false), ExcelConstants.ALPHABETS, addressArray[4], ResponseRfpExcelHeaders.ADDRESS.getValue() + Constants.SPACE + ExcelConstants.AddressFields.COUNTRY.getValue(), errorMessage));
//         }
//         //validate PIN
//         if (addressArray.length == 6 && !validateRegex(Utils.getTrimmedNumber(addressArray[5]), ExcelConstants.NUMERIC_CHARACTERS_REGEX)) {
//             errorMessage = MessageFormat.format(ErrorMessages.Messages.DATA_TYPE_MISMATCH.getMessage(), ExcelConstants.NUMBER, addressArray[5]);
//             errorMessageDetailsList.add(new ErrorMessageDetails(sheetName, new CellReference(addressCell.getRowIndex(), addressCell.getColumnIndex()).formatAsString(false), ExcelConstants.NUMBER, addressArray[5], ResponseRfpExcelHeaders.ADDRESS.getValue() + Constants.SPACE + ExcelConstants.AddressFields.PINCODE.getValue(), errorMessage));
//         }


    }


    // If (in Inland Freight) source type is selected as "Port of Entry" Source name should be same as selected Us Port of entry
    public static void validateInlandFreightSourceName(Row row, Map<String, Integer> columnsMap, String sheetName) {

        if (df.formatCellValue(row.getCell(columnsMap.get(ResponseRfpExcelHeaders.INLAND_TRANSIT_ORIGIN_NAME.getValue()))) == null || df.formatCellValue(row.getCell(columnsMap.get(ResponseRfpExcelHeaders.INLAND_TRANSIT_ORIGIN_NAME.getValue()))).trim().isEmpty()) {
            return;
        }

        if (df.formatCellValue(row.getCell(columnsMap.get(ResponseRfpExcelHeaders.US_PORT_OF_ENTRY.getValue()))) == null || df.formatCellValue(row.getCell(columnsMap.get(ResponseRfpExcelHeaders.US_PORT_OF_ENTRY.getValue()))).trim().isEmpty()) {
            return;
        }

        if (row.getCell(columnsMap.get(ResponseRfpExcelHeaders.INLAND_TRANSIT_ORIGIN_TYPE.getValue())) != null && Objects.equals(df.formatCellValue(row.getCell(columnsMap.get(ResponseRfpExcelHeaders.INLAND_TRANSIT_ORIGIN_TYPE.getValue()))), ExcelConstants.InlandTransitOriginType.PORT_OF_ENTRY.getValue()) && (!Objects.equals(df.formatCellValue(row.getCell(columnsMap.get(ExcelConstants.ResponseRfpExcelHeaders.US_PORT_OF_ENTRY.getValue()))), df.formatCellValue(row.getCell(columnsMap.get(ResponseRfpExcelHeaders.INLAND_TRANSIT_ORIGIN_NAME.getValue())))))) {
            String errorMessage = MessageFormat.format(ErrorMessages.Messages.CONDITIONAL_FIELD_ERROR.getMessage(), ResponseRfpExcelHeaders.INLAND_TRANSIT_ORIGIN_TYPE.getValue(), ExcelConstants.InlandTransitOriginType.PORT_OF_ENTRY.getValue(), ResponseRfpExcelHeaders.INLAND_TRANSIT_ORIGIN_NAME.getValue(), df.formatCellValue(row.getCell(columnsMap.get(ResponseRfpExcelHeaders.US_PORT_OF_ENTRY.getValue()))), df.formatCellValue(row.getCell(columnsMap.get(ResponseRfpExcelHeaders.INLAND_TRANSIT_ORIGIN_NAME.getValue()))));
            errorMessageDetailsList.add(new ErrorMessageDetails(sheetName, new CellReference(row.getRowNum(), columnsMap.get(ResponseRfpExcelHeaders.INLAND_TRANSIT_ORIGIN_NAME.getValue())).formatAsString(false), df.formatCellValue(row.getCell(columnsMap.get(ResponseRfpExcelHeaders.US_PORT_OF_ENTRY.getValue()))), df.formatCellValue(row.getCell(columnsMap.get(ResponseRfpExcelHeaders.INLAND_TRANSIT_ORIGIN_NAME.getValue()))), ResponseRfpExcelHeaders.INLAND_TRANSIT_ORIGIN_NAME.getValue(), errorMessage));
        }
    }

    private static void validateCountryAndUsPortOfEntry(Row row, Map<String, Integer> columnsMap, String sheetName) {
        // IF Origin port is set as Domestic, Origin country and Us Port of entry should be Domestic
        if (Objects.equals(df.formatCellValue(row.getCell(columnsMap.get(ResponseRfpExcelHeaders.ORIGIN_PORT.getValue()))), ExcelConstants.OriginPort.DOMESTIC.getValue())) {

            if (!Objects.equals(df.formatCellValue(row.getCell(columnsMap.get(ResponseRfpExcelHeaders.ORIGIN_COUNTRY.getValue()))), ExcelConstants.OriginCountry.USA.getValue())) {
                String errorMessage = MessageFormat.format(ErrorMessages.Messages.CONDITIONAL_FIELD_ERROR.getMessage(), ResponseRfpExcelHeaders.ORIGIN_PORT.getValue(), ExcelConstants.OriginPort.DOMESTIC.getValue(), ResponseRfpExcelHeaders.ORIGIN_COUNTRY.getValue(), ExcelConstants.OriginCountry.USA.getValue(), df.formatCellValue(row.getCell(columnsMap.get(ResponseRfpExcelHeaders.ORIGIN_COUNTRY.getValue()))));
                errorMessageDetailsList.add(new ErrorMessageDetails(sheetName, new CellReference(row.getRowNum(), columnsMap.get(ResponseRfpExcelHeaders.ORIGIN_COUNTRY.getValue())).formatAsString(false), ExcelConstants.OriginCountry.USA.getValue(), df.formatCellValue(row.getCell(columnsMap.get(ResponseRfpExcelHeaders.ORIGIN_COUNTRY.getValue()))), ResponseRfpExcelHeaders.ORIGIN_COUNTRY.getValue(), errorMessage));
            }

            if (!Objects.equals(df.formatCellValue(row.getCell(columnsMap.get(ResponseRfpExcelHeaders.US_PORT_OF_ENTRY.getValue()))), ExcelConstants.OriginPort.DOMESTIC.getValue())) {
                String errorMessage = MessageFormat.format(ErrorMessages.Messages.CONDITIONAL_FIELD_ERROR.getMessage(), ResponseRfpExcelHeaders.ORIGIN_PORT.getValue(), ExcelConstants.OriginPort.DOMESTIC.getValue(), ResponseRfpExcelHeaders.US_PORT_OF_ENTRY.getValue(), ExcelConstants.OriginPort.DOMESTIC.getValue(), df.formatCellValue(row.getCell(columnsMap.get(ResponseRfpExcelHeaders.US_PORT_OF_ENTRY.getValue()))));
                errorMessageDetailsList.add(new ErrorMessageDetails(sheetName, new CellReference(row.getRowNum(), columnsMap.get(ResponseRfpExcelHeaders.ORIGIN_COUNTRY.getValue())).formatAsString(false), ExcelConstants.OriginPort.DOMESTIC.getValue(), df.formatCellValue(row.getCell(columnsMap.get(ResponseRfpExcelHeaders.US_PORT_OF_ENTRY.getValue()))), ResponseRfpExcelHeaders.US_PORT_OF_ENTRY.getValue(), errorMessage));
            }

        }
    }

    private static PortEntryHeaderFields populatePortEntryAndHeaderDetails(Row row, Map<String, Integer> logisticHeadersMap, String portEntry, String gpMillName) {
        PortEntryHeaderFields portEntryHeaderFields = new PortEntryHeaderFields();

        portEntryHeaderFields.setOrigin_port(df.formatCellValue(row.getCell(logisticHeadersMap.get(ExcelConstants.ResponseRfpExcelHeaders.ORIGIN_PORT.getValue()))));
        portEntryHeaderFields.setOrigin_cntry(df.formatCellValue(row.getCell(logisticHeadersMap.get(ExcelConstants.ResponseRfpExcelHeaders.ORIGIN_COUNTRY.getValue()))));
        portEntryHeaderFields.setEnvironmental_certification(df.formatCellValue(row.getCell(logisticHeadersMap.get(ExcelConstants.ResponseRfpExcelHeaders.ENVIRONMENTAL_CERTIFICATION.getValue()))));
        portEntryHeaderFields.setBale_packaging(df.formatCellValue(row.getCell(logisticHeadersMap.get(ExcelConstants.ResponseRfpExcelHeaders.BALE_PACKAGING.getValue()))));
        portEntryHeaderFields.setBale_type(df.formatCellValue(row.getCell(logisticHeadersMap.get(ExcelConstants.ResponseRfpExcelHeaders.BALE_TYPE.getValue()))));

        return portEntryHeaderFields;
    }

    private static void validatePortEntryToPortEntryHeaderFields(String portEntry, Row row, Map<String, Integer> logisticHeadersMap, String sheetName, Map<String, Map<String, PortEntryHeaderFields>> portEntryAndHeaderFieldsMap, String supplierMillName) {
        PortEntryHeaderFields portEntryHeaderFields = portEntryAndHeaderFieldsMap.get(supplierMillName).get((portEntry));

        if (!Objects.equals(portEntryHeaderFields.getOrigin_port(), df.formatCellValue(row.getCell(logisticHeadersMap.get(ExcelConstants.ResponseRfpExcelHeaders.ORIGIN_PORT.getValue()))))) {
            String errorMessage = MessageFormat.format(ErrorMessages.Messages.REPEATED_SUPPLIER_MILL_PORT_ENTRY_HEADER_ERROR.getMessage(), ExcelConstants.ResponseRfpExcelHeaders.ORIGIN_PORT.getValue());
            errorMessageDetailsList.add(new ErrorMessageDetails(sheetName, new CellReference(row.getRowNum(), logisticHeadersMap.get(ResponseRfpExcelHeaders.ORIGIN_PORT.getValue())).formatAsString(false), portEntryHeaderFields.getOrigin_port(), df.formatCellValue(row.getCell(logisticHeadersMap.get(ExcelConstants.ResponseRfpExcelHeaders.ORIGIN_PORT.getValue()))), ExcelConstants.ResponseRfpExcelHeaders.ORIGIN_PORT.getValue(), errorMessage));
        }

        if (!Objects.equals(portEntryHeaderFields.getOrigin_cntry(), df.formatCellValue(row.getCell(logisticHeadersMap.get(ExcelConstants.ResponseRfpExcelHeaders.ORIGIN_COUNTRY.getValue()))))) {
            String errorMessage = MessageFormat.format(ErrorMessages.Messages.REPEATED_SUPPLIER_MILL_PORT_ENTRY_HEADER_ERROR.getMessage(), ExcelConstants.ResponseRfpExcelHeaders.ORIGIN_COUNTRY.getValue());
            errorMessageDetailsList.add(new ErrorMessageDetails(sheetName, new CellReference(row.getRowNum(), logisticHeadersMap.get(ResponseRfpExcelHeaders.ORIGIN_COUNTRY.getValue())).formatAsString(false), portEntryHeaderFields.getOrigin_cntry(), df.formatCellValue(row.getCell(logisticHeadersMap.get(ExcelConstants.ResponseRfpExcelHeaders.ORIGIN_COUNTRY.getValue()))), ExcelConstants.ResponseRfpExcelHeaders.ORIGIN_COUNTRY.getValue(), errorMessage));

        }

        if (!Objects.equals(portEntryHeaderFields.getEnvironmental_certification(), df.formatCellValue(row.getCell(logisticHeadersMap.get(ExcelConstants.ResponseRfpExcelHeaders.ENVIRONMENTAL_CERTIFICATION.getValue()))))) {
            String errorMessage = MessageFormat.format(ErrorMessages.Messages.REPEATED_SUPPLIER_MILL_PORT_ENTRY_HEADER_ERROR.getMessage(), ExcelConstants.ResponseRfpExcelHeaders.ENVIRONMENTAL_CERTIFICATION.getValue());
            errorMessageDetailsList.add(new ErrorMessageDetails(sheetName, new CellReference(row.getRowNum(), logisticHeadersMap.get(ResponseRfpExcelHeaders.ENVIRONMENTAL_CERTIFICATION.getValue())).formatAsString(false), portEntryHeaderFields.getEnvironmental_certification(), df.formatCellValue(row.getCell(logisticHeadersMap.get(ExcelConstants.ResponseRfpExcelHeaders.ENVIRONMENTAL_CERTIFICATION.getValue()))), ExcelConstants.ResponseRfpExcelHeaders.ENVIRONMENTAL_CERTIFICATION.getValue(), errorMessage));

        }

        if (!Objects.equals(portEntryHeaderFields.getBale_packaging(), df.formatCellValue(row.getCell(logisticHeadersMap.get(ExcelConstants.ResponseRfpExcelHeaders.BALE_PACKAGING.getValue()))))) {
            String errorMessage = MessageFormat.format(ErrorMessages.Messages.REPEATED_SUPPLIER_MILL_PORT_ENTRY_HEADER_ERROR.getMessage(), ExcelConstants.ResponseRfpExcelHeaders.BALE_PACKAGING.getValue());
            errorMessageDetailsList.add(new ErrorMessageDetails(sheetName, new CellReference(row.getRowNum(), logisticHeadersMap.get(ResponseRfpExcelHeaders.BALE_PACKAGING.getValue())).formatAsString(false), portEntryHeaderFields.getBale_packaging(), df.formatCellValue(row.getCell(logisticHeadersMap.get(ExcelConstants.ResponseRfpExcelHeaders.BALE_PACKAGING.getValue()))), ExcelConstants.ResponseRfpExcelHeaders.BALE_PACKAGING.getValue(), errorMessage));
        }

        if (!Objects.equals(portEntryHeaderFields.getBale_type(), df.formatCellValue(row.getCell(logisticHeadersMap.get(ExcelConstants.ResponseRfpExcelHeaders.BALE_TYPE.getValue()))))) {
            String errorMessage = MessageFormat.format(ErrorMessages.Messages.REPEATED_SUPPLIER_MILL_PORT_ENTRY_HEADER_ERROR.getMessage(), ExcelConstants.ResponseRfpExcelHeaders.BALE_TYPE.getValue());
            errorMessageDetailsList.add(new ErrorMessageDetails(sheetName, new CellReference(row.getRowNum(), logisticHeadersMap.get(ResponseRfpExcelHeaders.BALE_TYPE.getValue())).formatAsString(false), portEntryHeaderFields.getBale_type(), df.formatCellValue(row.getCell(logisticHeadersMap.get(ExcelConstants.ResponseRfpExcelHeaders.BALE_TYPE.getValue()))), ExcelConstants.ResponseRfpExcelHeaders.BALE_TYPE.getValue(), errorMessage));
        }
    }

    private static void validateRepeatedPeriodDetails(Map<String, PeriodDetail> periodToPeriodDetailsMap, String sheetName, Row row, String period, Map<String, Integer> commercialPricingMap) {
        PeriodDetail periodDetail = periodToPeriodDetailsMap.get(period);

        if (df.formatCellValue(row.getCell(commercialPricingMap.get(ResponseRfpExcelHeaders.FIXED_PRICE_WEIGHTAGE.getValue()))) == null || df.formatCellValue(row.getCell(commercialPricingMap.get(ResponseRfpExcelHeaders.FIXED_PRICE_WEIGHTAGE.getValue()))).trim().isEmpty()) {
            String errorMessage = MessageFormat.format(ErrorMessages.Messages.REPEATED_PERIOD_DETAILS_ERROR.getMessage(), ExcelConstants.ResponseRfpExcelHeaders.FIXED_PRICE_WEIGHTAGE.getValue());
            String expectedValue = null;
//            if (periodDetail.getFixed_price_details() != null && periodDetail.getFixed_price_details().getWeightage_pct() != null){
//                expectedValue =  periodDetail.getFixed_price_details().getWeightage_pct().toString();
//            }
            errorMessageDetailsList.add(new ErrorMessageDetails(sheetName, new CellReference(row.getRowNum(), commercialPricingMap.get(ResponseRfpExcelHeaders.FIXED_PRICE_WEIGHTAGE.getValue())).formatAsString(false), null, df.formatCellValue(row.getCell(commercialPricingMap.get(ExcelConstants.ResponseRfpExcelHeaders.FIXED_PRICE_WEIGHTAGE.getValue()))), ExcelConstants.ResponseRfpExcelHeaders.FIXED_PRICE_WEIGHTAGE.getValue(), errorMessage));
        } else if (!Objects.equals(periodDetail.getFixed_price_details().getWeightage_pct(), Long.valueOf(df.formatCellValue(row.getCell(commercialPricingMap.get(ResponseRfpExcelHeaders.FIXED_PRICE_WEIGHTAGE.getValue()))).replace(Constants.PERCENT, Constants.EMPTY_STRING)))) {
            String errorMessage = MessageFormat.format(ErrorMessages.Messages.REPEATED_PERIOD_DETAILS_ERROR.getMessage(), ExcelConstants.ResponseRfpExcelHeaders.FIXED_PRICE_WEIGHTAGE.getValue());
            String expectedValue = periodDetail.getFixed_price_details().getWeightage_pct() == null ? null : periodDetail.getFixed_price_details().toString();
            errorMessageDetailsList.add(new ErrorMessageDetails(sheetName, new CellReference(row.getRowNum(), commercialPricingMap.get(ResponseRfpExcelHeaders.FIXED_PRICE_WEIGHTAGE.getValue())).formatAsString(false), expectedValue, df.formatCellValue(row.getCell(commercialPricingMap.get(ExcelConstants.ResponseRfpExcelHeaders.FIXED_PRICE_WEIGHTAGE.getValue()))), ExcelConstants.ResponseRfpExcelHeaders.FIXED_PRICE_WEIGHTAGE.getValue(), errorMessage));
        }

        if (!Objects.equals(periodDetail.getFixed_price_details().getFixed_price_value(), Utils.getFloatDoller(df.formatCellValue(row.getCell(commercialPricingMap.get(ResponseRfpExcelHeaders.FIXED_PRICE.getValue())))))) {
            String errorMessage = MessageFormat.format(ErrorMessages.Messages.REPEATED_PERIOD_DETAILS_ERROR.getMessage(), ExcelConstants.ResponseRfpExcelHeaders.FIXED_PRICE.getValue());
            errorMessageDetailsList.add(new ErrorMessageDetails(sheetName, new CellReference(row.getRowNum(), commercialPricingMap.get(ResponseRfpExcelHeaders.FIXED_PRICE.getValue())).formatAsString(false), null, df.formatCellValue(row.getCell(commercialPricingMap.get(ExcelConstants.ResponseRfpExcelHeaders.FIXED_PRICE.getValue()))), ExcelConstants.ResponseRfpExcelHeaders.FIXED_PRICE.getValue(), errorMessage));
        }
    }

    private static PeriodDetail populatePeriodDetails(String sheetName, Row row, Map<String, Integer> commercialPricingMap, String period) {
        PeriodDetail periodDetail = new PeriodDetail();

        FixedPriceDetails fixedPriceDetails = new FixedPriceDetails();
        if (df.formatCellValue(row.getCell(commercialPricingMap.get(ResponseRfpExcelHeaders.FIXED_PRICE_WEIGHTAGE.getValue()))).replace(Constants.PERCENT, Constants.EMPTY_STRING) != null && !df.formatCellValue(row.getCell(commercialPricingMap.get(ResponseRfpExcelHeaders.FIXED_PRICE_WEIGHTAGE.getValue()))).replace(Constants.PERCENT, Constants.EMPTY_STRING).trim().isEmpty()) {
            fixedPriceDetails.setWeightage_pct((long) Double.parseDouble(df.formatCellValue(row.getCell(commercialPricingMap.get(ResponseRfpExcelHeaders.FIXED_PRICE_WEIGHTAGE.getValue()))).replace(Constants.PERCENT, Constants.EMPTY_STRING)));
        }
        if (df.formatCellValue(row.getCell(commercialPricingMap.get(ResponseRfpExcelHeaders.FIXED_PRICE.getValue()))) != null && !df.formatCellValue(row.getCell(commercialPricingMap.get(ResponseRfpExcelHeaders.FIXED_PRICE.getValue()))).trim().isEmpty()) {
            fixedPriceDetails.setFixed_price_value(Utils.getFloatDoller(df.formatCellValue(row.getCell(commercialPricingMap.get(ResponseRfpExcelHeaders.FIXED_PRICE.getValue())))));
        }
        periodDetail.setFixed_price_details(fixedPriceDetails);
        return periodDetail;
    }

    public static String getCellValueForNAFields(Row row, Map<String, Integer> columnsMap, String headerName) {

        if(Objects.equals(Utils.getTrimmedNumber(df.formatCellValue(row.getCell(columnsMap.get(headerName)))).toLowerCase(), ExcelConstants.NA.toLowerCase())) {
            return null;
        } else {
            return df.formatCellValue(row.getCell(columnsMap.get(headerName)));
        }
    }


    //Code Related to New Commercial Sheet

    // Method to set the Mechanism Basis value by parsing commercial sheet
    private static void setMechanismBasisValue(Sheet commercialSheet, PricingDetail pricingDetail) {

        // setting the mechanism basis (Index/Movement/Hybrid/Other)
        //First check which Mechanism basis is entered (User need to select only one Mechanism Basis else throw error)

        String indexBasis = null;
        String movementBasis = null;
        String hybridBasis = null;
        String otherBasis = null;

        //for maintaining count of number of index basis details present in Excel
        int countOfIndexBasis = 0;
        //Check if Index basis is entered
        if (checkIsCellEmptyAndValidateRegex(new CellReference(ExcelConstants.INDEX_CELL_VALUE_FOR_INDEX_BASIS), commercialSheet, Optional.empty())) {
            indexBasis = df.formatCellValue(getCellValueFromCellReference(new CellReference(ExcelConstants.INDEX_CELL_VALUE_FOR_INDEX_BASIS), commercialSheet));
            if (!Objects.equals(indexBasis.toLowerCase().trim(), ExcelConstants.NA.toLowerCase()))
                countOfIndexBasis++;
        }
        //Check if Movement basis is entered
        if (checkIsCellEmptyAndValidateRegex(new CellReference(ExcelConstants.INDEX_CELL_VALUE_FOR_MOVEMENT_BASIS), commercialSheet, Optional.empty())) {
            movementBasis = df.formatCellValue(getCellValueFromCellReference(new CellReference(ExcelConstants.INDEX_CELL_VALUE_FOR_MOVEMENT_BASIS), commercialSheet));
            if (!Objects.equals(movementBasis.toLowerCase().trim(), ExcelConstants.NA.toLowerCase()))
                countOfIndexBasis++;
        }
        //Check if Hybrid basis is entered
        if (checkIsCellEmptyAndValidateRegex(new CellReference(ExcelConstants.INDEX_CELL_VALUE_FOR_HYBRID_BASIS), commercialSheet, Optional.empty())) {
            hybridBasis = df.formatCellValue(getCellValueFromCellReference(new CellReference(ExcelConstants.INDEX_CELL_VALUE_FOR_HYBRID_BASIS), commercialSheet));
            if (!Objects.equals(hybridBasis.toLowerCase().trim(), ExcelConstants.NA.toLowerCase()))
                countOfIndexBasis++;
        }
        //Check if Other basis is entered
        if (checkIsCellEmptyAndValidateRegex(new CellReference(ExcelConstants.INDEX_CELL_VALUE_FOR_OTHER_BASIS),commercialSheet,Optional.empty())) {
            otherBasis = df.formatCellValue(getCellValueFromCellReference(new CellReference(ExcelConstants.INDEX_CELL_VALUE_FOR_OTHER_BASIS), commercialSheet));
            if (!otherBasis.toLowerCase().contains(Constants.OTHER_DATA.toLowerCase()) && !otherBasis.toLowerCase().trim().equals(ExcelConstants.NA.toLowerCase()))
                countOfIndexBasis++;
        }

        //Atleast one Index details is mandatory
        if (countOfIndexBasis == 0) {
            errorMessageDetailsList.add(new ErrorMessageDetails(commercialSheet.getSheetName(), null, null, null, null, ErrorMessages.Messages.MECHANISM_BASIS_IS_MANDATORY.getMessage()));
            return;
        }
        //only one Index details should be there
        if (countOfIndexBasis > 1) {
            errorMessageDetailsList.add(new ErrorMessageDetails(commercialSheet.getSheetName(), null, null, null, null, ErrorMessages.Messages.MECHANISM_BASIS_ONLY_ONE.getMessage()));
            return;
        }
        //setting Mechanism basis
        if (indexBasis != null) {
            pricingDetail.setMechanism_basis(Constants.MECHANISMS.INDEX.getValue());
        } else if (movementBasis != null) {
            pricingDetail.setMechanism_basis(Constants.MECHANISMS.MOVEMENT.getValue());
        } else if (hybridBasis != null) {
            pricingDetail.setMechanism_basis(Constants.MECHANISMS.HYBRID.getValue());
        } else if (otherBasis != null) {
            pricingDetail.setMechanism_basis(Constants.MECHANISMS.OTHER.getValue());
        }

    }




    private static List<PeriodDetail> parsePeriodDetails(Sheet sheet, PricingDetail pricingDetail, ResponseRfpExcelResponse responseRfpExcelResponse) {
        List<PeriodDetail> periodDetailList = new ArrayList<>();

        PeriodDetail periodDetail = new PeriodDetail();
        periodDetail.setPeriod_num(Constants.ONE_LONG);
        periodDetail.setPeriod(Constants.YEAR);
        periodDetail.setPeriod_type(Constants.PERIOD_FULL_YEAR);

        //Setting Fixed Pricing Details Empty Object
        periodDetail.setFixed_price_details(new FixedPriceDetails());

        // Parsing index details
        List<IndexDetails> indexDetailsList = parseIndexDetails(sheet,pricingDetail,responseRfpExcelResponse);
        periodDetail.setIndex_details(indexDetailsList);

        periodDetailList.add(periodDetail);
        return periodDetailList;

    }


    //Method to Populate List of IndexDetails Based on Mechanism Basis
    private static List<IndexDetails> parseIndexDetails(Sheet sheet, PricingDetail pricingDetail, ResponseRfpExcelResponse responseRfpExcelResponse) {
        // adding index details based on constant excel
        List<IndexDetails> indexDetailsList = new ArrayList<>();
        IndexDetails indexDetails = new IndexDetails();

        // check for the mechanism basis if it is index then set indexValues
        if (pricingDetail.getMechanism_basis() !=null && Objects.equals(pricingDetail.getMechanism_basis(), Constants.MECHANISMS.INDEX.getValue()) ){
            parseIndexDetailsForIndexBasis(sheet,indexDetails,pricingDetail);
        }
        if (pricingDetail.getMechanism_basis() !=null && Objects.equals(pricingDetail.getMechanism_basis(), Constants.MECHANISMS.MOVEMENT.getValue())){
            //For Index and movement , mechanism is Index  but isMovement basis will be true for movement
            pricingDetail.setMechanism_basis(Constants.MECHANISMS.INDEX.getValue());
            parseIndexDetailsForMovementBasis(sheet,indexDetails,pricingDetail);
        }
        if (pricingDetail.getMechanism_basis() !=null && Objects.equals(pricingDetail.getMechanism_basis(), Constants.MECHANISMS.OTHER.getValue())){
            parseIndexDetailsForOtherBasis(sheet,pricingDetail,responseRfpExcelResponse);
        }
        if (pricingDetail.getMechanism_basis() !=null && Objects.equals(pricingDetail.getMechanism_basis(), Constants.MECHANISMS.HYBRID.getValue())){
            //For Hybrid , mechanism is Other Only
            pricingDetail.setMechanism_basis(Constants.MECHANISMS.OTHER.getValue());
            parseIndexDetailsForHybridBasis(sheet,pricingDetail,responseRfpExcelResponse);
        }
        indexDetailsList.add(indexDetails);
        return indexDetailsList;

    }

    //Setting Index Details is Mechanism Basis is Others
    private static void parseIndexDetailsForOtherBasis(Sheet sheet, PricingDetail pricingDetail, ResponseRfpExcelResponse responseRfpExcelResponse) {
        // if mechanism basis is other.
        // 1, adding to pricing_alternate_mechanism value
        if (Boolean.TRUE.equals(isCellEmptyAndValidateRegex(new CellReference(ExcelConstants.OTHER_BASED_CELL_REFERENCE_FOR_COMMERCIAL_PRICING.get(ResponseRfpExcelHeaders.OTHER.getValue())), sheet, ExcelConstants.ResponseRfpExcelHeaders.OTHER.getValue(), Optional.of(ExcelConstants.ALPHABETS_REGEX), ExcelConstants.ALPHABETS))) {
            pricingDetail.setPricing_alternate_mechanism(df.formatCellValue(getCellValueFromCellReference(new CellReference(ExcelConstants.OTHER_BASED_CELL_REFERENCE_FOR_COMMERCIAL_PRICING.get(ResponseRfpExcelHeaders.OTHER.getValue())), sheet)));

            String errorMessage = df.formatCellValue(getCellValueFromCellReference(new CellReference(ExcelConstants.OTHER_BASED_CELL_REFERENCE_FOR_COMMERCIAL_PRICING.get(ResponseRfpExcelHeaders.OTHER.getValue())), sheet));
            responseRfpWarningList.add(new ErrorMessageDetails(sheet.getSheetName(), new CellReference(ExcelConstants.OTHER_BASED_CELL_REFERENCE_FOR_COMMERCIAL_PRICING.get(ResponseRfpExcelHeaders.OTHER.getValue())).formatAsString(false), null, null, Constants.MECHANISMS.OTHER.getValue(), errorMessage));

        }


    }

    //Setting Hybrid Basis  Details in Alternate Pricing Mechanism
    private static void parseIndexDetailsForHybridBasis(Sheet sheet, PricingDetail pricingDetail, ResponseRfpExcelResponse responseRfpExcelResponse) {
        // if mechanism basis is Hybrid
        //  pricing alternate_mechanism = part1 + part 2 + part 3
        String part1;
        String part2;
        String part3;
        StringBuilder alternatePricingMechanism = new StringBuilder();

        //Part 1 (mandatory field)
        if (Boolean.TRUE.equals(isCellEmptyAndValidateRegex(new CellReference(ExcelConstants.HYBRID_BASED_CELL_REFERENCE_FOR_COMMERCIAL_PRICING.get(ExcelConstants.CommercialSheetFieldNames.PART_1.getFieldName())), sheet, ExcelConstants.CommercialSheetFieldNames.PART_1.getFieldName(), Optional.empty(), ExcelConstants.ALPHABETS))) {
            part1 = (df.formatCellValue(getCellValueFromCellReference(new CellReference(ExcelConstants.HYBRID_BASED_CELL_REFERENCE_FOR_COMMERCIAL_PRICING.get(ExcelConstants.CommercialSheetFieldNames.PART_1.getFieldName())), sheet)));
            alternatePricingMechanism.append(part1);
        }
        //Part 2 (mandatory field)
        if (Boolean.TRUE.equals(isCellEmptyAndValidateRegex(new CellReference(ExcelConstants.HYBRID_BASED_CELL_REFERENCE_FOR_COMMERCIAL_PRICING.get(ExcelConstants.CommercialSheetFieldNames.PART_2.getFieldName())), sheet, ExcelConstants.CommercialSheetFieldNames.PART_2.getFieldName(), Optional.empty(), ExcelConstants.ALPHABETS))) {
            part2 = (df.formatCellValue(getCellValueFromCellReference(new CellReference(ExcelConstants.HYBRID_BASED_CELL_REFERENCE_FOR_COMMERCIAL_PRICING.get(ExcelConstants.CommercialSheetFieldNames.PART_2.getFieldName())), sheet)));
            alternatePricingMechanism.append(Constants.COMMA).append(part2);
        }

        //Part 3 (Optional Field field)
        if (checkIsCellEmpty(new CellReference(ExcelConstants.HYBRID_BASED_CELL_REFERENCE_FOR_COMMERCIAL_PRICING.get(ExcelConstants.CommercialSheetFieldNames.PART_3.getFieldName())), sheet)) {
            part3 = (df.formatCellValue(getCellValueFromCellReference(new CellReference(ExcelConstants.HYBRID_BASED_CELL_REFERENCE_FOR_COMMERCIAL_PRICING.get(ExcelConstants.CommercialSheetFieldNames.PART_3.getFieldName())), sheet)));
            alternatePricingMechanism.append(Constants.COMMA).append(part3);
        }

        //setting 3 parts in pricing alternate mechanism
        pricingDetail.setPricing_alternate_mechanism(alternatePricingMechanism.toString());
        //Adding it to show in Warnings Section
        String errorMessage = alternatePricingMechanism.toString();
        String cellReference = new CellReference(ExcelConstants.HYBRID_BASED_CELL_REFERENCE_FOR_COMMERCIAL_PRICING.get(ExcelConstants.CommercialSheetFieldNames.PART_1.getFieldName())).formatAsString(false) +Constants.COMMA+ new CellReference(ExcelConstants.HYBRID_BASED_CELL_REFERENCE_FOR_COMMERCIAL_PRICING.get(ExcelConstants.CommercialSheetFieldNames.PART_2.getFieldName())).formatAsString(false) +Constants.COMMA+ new CellReference(ExcelConstants.HYBRID_BASED_CELL_REFERENCE_FOR_COMMERCIAL_PRICING.get(ExcelConstants.CommercialSheetFieldNames.PART_3.getFieldName())).formatAsString(false);
        responseRfpWarningList.add(new ErrorMessageDetails(sheet.getSheetName(), cellReference, null, null, Constants.MECHANISMS.HYBRID.getValue(), errorMessage));

        //Comments (Optional field)
        if(checkIsCellEmpty(new CellReference(ExcelConstants.HYBRID_BASED_CELL_REFERENCE_FOR_COMMERCIAL_PRICING.get(ExcelConstants.CommercialSheetFieldNames.COMMENTS.getFieldName())),sheet)){
            pricingDetail.setComments(df.formatCellValue(getCellValueFromCellReference(new CellReference(ExcelConstants.HYBRID_BASED_CELL_REFERENCE_FOR_COMMERCIAL_PRICING.get(ExcelConstants.CommercialSheetFieldNames.COMMENTS.getFieldName())),sheet)));
        }

    }

    //Method to set Index Details if mechanism basis is movement
    private static void parseIndexDetailsForMovementBasis(Sheet sheet, IndexDetails indexDetails, PricingDetail pricingDetail) {
        indexDetails.setRead_type(Constants.WEEKDAY_OF_MONTH);
        indexDetails.setWeightage_pct(100L);

        //Setting index (mandatory Field)
        if (Boolean.TRUE.equals(isCellEmptyAndValidateRegex(new CellReference(ExcelConstants.MOVEMENT_BASED_CELL_REFERENCE_FOR_COMMERCIAL_PRICING.get(ExcelConstants.CommercialSheetFieldNames.INDEX_NAME.getFieldName())), sheet, ExcelConstants.CommercialSheetFieldNames.INDEX_NAME.getFieldName(), Optional.empty(), ExcelConstants.ALPHABETS))) {
            if (!Objects.equals(df.formatCellValue(getCellValueFromCellReference(new CellReference(ExcelConstants.MOVEMENT_BASED_CELL_REFERENCE_FOR_COMMERCIAL_PRICING.get(ExcelConstants.CommercialSheetFieldNames.INDEX_NAME.getFieldName())), sheet)), ExcelConstants.NA))
                indexDetails.setIndex(df.formatCellValue(getCellValueFromCellReference(new CellReference(ExcelConstants.MOVEMENT_BASED_CELL_REFERENCE_FOR_COMMERCIAL_PRICING.get(ExcelConstants.CommercialSheetFieldNames.INDEX_NAME.getFieldName())), sheet)));
        }

        //Setting Initial price  if any (It is Optional field)
        CellReference cellReference = new CellReference(ExcelConstants.MOVEMENT_BASED_CELL_REFERENCE_FOR_COMMERCIAL_PRICING.get(ExcelConstants.CommercialSheetFieldNames.STARTING_PRICE_POINT.getFieldName()));
        if (checkIsEmptyStringAndRegex(sheet.getRow(cellReference.getRow()).getCell(cellReference.getCol()), ExcelConstants.CommercialSheetFieldNames.STARTING_PRICE_POINT.getFieldName(), ExcelConstants.DOLLER_REGEX, sheet.getSheetName(), ExcelConstants.CURRENCY_DOLLAR, false)) {
            pricingDetail.setInitial_price(Float.parseFloat(df.formatCellValue(getCellValueFromCellReference(cellReference, sheet)).replace(Constants.DOLLAR, Constants.EMPTY_STRING)));
        }

        //Setting Movement Change type  if any (It is Optional field)
        cellReference = new CellReference(ExcelConstants.MOVEMENT_BASED_CELL_REFERENCE_FOR_COMMERCIAL_PRICING.get(ExcelConstants.CommercialSheetFieldNames.MONTH_OVER_MONTH_CHANGE.getFieldName()));
        if (checkIsEmptyStringAndRegex(sheet.getRow(cellReference.getRow()).getCell(cellReference.getCol()), ExcelConstants.CommercialSheetFieldNames.MONTH_OVER_MONTH_CHANGE.getFieldName(), ExcelConstants.ALPHABETS_REGEX, sheet.getSheetName(), ExcelConstants.ALPHABETS, false)) {
            pricingDetail.setMovement_change_type(df.formatCellValue(getCellValueFromCellReference(cellReference, sheet)));
        }

        //Setting Values by Parsing Index Published Date
        cellReference = new CellReference(ExcelConstants.MOVEMENT_BASED_CELL_REFERENCE_FOR_COMMERCIAL_PRICING.get(ExcelConstants.CommercialSheetFieldNames.INDEX_PUBLISHED_DATE.getFieldName()));
        parseIndexPublishedDateInMovementBasis(sheet,cellReference,indexDetails,pricingDetail);

        //Setting Additional Adjustment if any (It is Optional field)
        cellReference = new CellReference(ExcelConstants.MOVEMENT_BASED_CELL_REFERENCE_FOR_COMMERCIAL_PRICING.get(ExcelConstants.CommercialSheetFieldNames.ADDITIONAL_ADJUSTMENT_ADMT.getFieldName()));
        if (checkIsEmptyStringAndRegex(sheet.getRow(cellReference.getRow()).getCell(cellReference.getCol()),ExcelConstants.CommercialSheetFieldNames.ADDITIONAL_ADJUSTMENT_ADMT.getFieldName(),ExcelConstants.DOLLER_REGEX_WITH_MINUS_SIGN, sheet.getSheetName(), ExcelConstants.CURRENCY_DOLLAR,false)) {
            indexDetails.setAdditional_adjustment(Float.parseFloat(df.formatCellValue(getCellValueFromCellReference(cellReference,sheet)).replace(Constants.DOLLAR,Constants.EMPTY_STRING)));
        }

        //Setting comments if any (Optional field)
        if (checkIsCellEmpty(new CellReference(ExcelConstants.MOVEMENT_BASED_CELL_REFERENCE_FOR_COMMERCIAL_PRICING.get(ExcelConstants.CommercialSheetFieldNames.COMMENTS.getFieldName())), sheet)){
            pricingDetail.setComments(df.formatCellValue(getCellValueFromCellReference(new CellReference(ExcelConstants.MOVEMENT_BASED_CELL_REFERENCE_FOR_COMMERCIAL_PRICING.get(ExcelConstants.CommercialSheetFieldNames.COMMENTS.getFieldName())), sheet)));
        }
    }

    //Method to Parse Index Published Date String and Set time_window , time_window_period,, read_date, read_type, read_week_criteria, read_day
    private static void parseIndexPublishedDateInMovementBasis(Sheet sheet,CellReference cellReference, IndexDetails indexDetails,PricingDetail pricingDetail) {
        if (checkIsCellEmpty(cellReference, sheet)) {
            String indexPublishedDate = df.formatCellValue(getCellValueFromCellReference(cellReference,sheet)).trim();
            String values[] = indexPublishedDate.split(Constants.SPACE);
            if (Utils.validateRegex(indexPublishedDate, ExcelConstants.WEEK_DAY_REGEX_FOR_MOVEMENT)) {
                indexDetails.setRead_type(Constants.WEEKDAY_OF_MONTH);
                if (values.length == 7) {
                    pricingDetail.setTime_window(Long.valueOf(values[0]));
                    pricingDetail.setTime_window_period(values[1]);
                    indexDetails.setRead_week_criteria(values[2]);
                    indexDetails.setRead_day(values[3]);
                } else {
                    pricingDetail.setTime_window(Long.valueOf(values[0]));
                    pricingDetail.setTime_window_period(values[1] + Constants.SPACE + values[2]);
                    indexDetails.setRead_week_criteria(values[3]);
                    indexDetails.setRead_day(values[4]);
                }
            } else if (Utils.validateRegex(indexPublishedDate, ExcelConstants.GIVEN_DATE_REGEX_FOR_MOVEMENT)) {
                indexDetails.setRead_type(Constants.GIVEN_DATE);
                if (values.length == 3) {
                    pricingDetail.setTime_window(Long.valueOf(values[0]));
                    pricingDetail.setTime_window_period(values[1]);
                    indexDetails.setRead_date(values[2]);
                } else if (values.length == 4) {
                    pricingDetail.setTime_window(Long.valueOf(values[0]));
                    pricingDetail.setTime_window_period(values[1] + Constants.SPACE + values[2]);
                    indexDetails.setRead_date(values[3]);
                }
            } else {
                //Need to throw Error
                String errorMessage = MessageFormat.format(ErrorMessages.Messages.DATA_FORMAT_MISMATCH.getMessage(), ExcelConstants.MECHANISM_BASIS_INDEX_PUBLISHED_DATE_EXAMPLES, indexPublishedDate);
                errorMessageDetailsList.add(new ErrorMessageDetails(sheet.getSheetName(), cellReference.formatAsString(false), ExcelConstants.ExpectedValuesList.MANDATORY.getValue(), indexPublishedDate, ExcelConstants.CommercialSheetFieldNames.INDEX_PUBLISHED_DATE.getFieldName(), errorMessage));

            }
        }
    }


    // parsing indexDetails if the mechanism basis is Index
    private static void parseIndexDetailsForIndexBasis(Sheet sheet, IndexDetails indexDetails, PricingDetail pricingDetail) {

        //static setting window time and period
        pricingDetail.setTime_window(Constants.ONE_LONG);
        pricingDetail.setTime_window_period(Constants.PERIOD_MONTH);
        //Setting Index (mandatory field)
        if (Boolean.TRUE.equals(isCellEmptyAndValidateRegex(new CellReference(ExcelConstants.INDEX_BASED_CELL_REFERENCE_FOR_COMMERCIAL_PRICING.get(ExcelConstants.CommercialSheetFieldNames.INDEX_NAME.getFieldName())), sheet, ExcelConstants.CommercialSheetFieldNames.INDEX_NAME.getFieldName(), Optional.empty(), ExcelConstants.ALPHABETS))) {
            if (!Objects.equals(df.formatCellValue(getCellValueFromCellReference(new CellReference(ExcelConstants.INDEX_BASED_CELL_REFERENCE_FOR_COMMERCIAL_PRICING.get(ExcelConstants.CommercialSheetFieldNames.INDEX_NAME.getFieldName())), sheet)), ExcelConstants.NA))
                indexDetails.setIndex(df.formatCellValue(getCellValueFromCellReference(new CellReference(ExcelConstants.INDEX_BASED_CELL_REFERENCE_FOR_COMMERCIAL_PRICING.get(ExcelConstants.CommercialSheetFieldNames.INDEX_NAME.getFieldName())), sheet)));
        }

        indexDetails.setWeightage_pct(100L);

        CellReference cellReference = new CellReference(ExcelConstants.INDEX_BASED_CELL_REFERENCE_FOR_COMMERCIAL_PRICING.get(ExcelConstants.CommercialSheetFieldNames.INDEX_PUBLISHED_DATE.getFieldName()));
        if (checkIsCellEmpty(cellReference, sheet)) {
            String indexPublishedDate = df.formatCellValue(getCellValueFromCellReference(cellReference, sheet)).trim();

            // NEED TO check for givenDate or WeekDay regex if then we will say that it is givenDate or WeekDay
            // After this split the index PublishedDate and set based on things

            String values[] = indexPublishedDate.split(Constants.SPACE);
            if (Utils.validateRegex(indexPublishedDate, ExcelConstants.WEEK_DAY_REGEX_FOR_INDEX)) {
                indexDetails.setRead_type(Constants.WEEKDAY_OF_MONTH);
                indexDetails.setRead_week_criteria(values[0]);
                indexDetails.setRead_day(values[1]);
            } else if (Utils.validateRegex(indexPublishedDate, ExcelConstants.GIVEN_DATE_REGEX_FOR_INDEX)) {
                indexDetails.setRead_type(Constants.GIVEN_DATE);
                indexDetails.setRead_date(values[0]);
            }else {

                //Need to throw Error
                String errorMessage = MessageFormat.format(ErrorMessages.Messages.DATA_FORMAT_MISMATCH.getMessage(), ExcelConstants.INDEX_BASIS_INDEX_PUBLISHED_DATE_EXAMPLES, indexPublishedDate);
                errorMessageDetailsList.add(new ErrorMessageDetails(sheet.getSheetName(), cellReference.formatAsString(false), ExcelConstants.ExpectedValuesList.MANDATORY.getValue(), indexPublishedDate, ExcelConstants.CommercialSheetFieldNames.INDEX_PUBLISHED_DATE.getFieldName(), errorMessage));
            }
        }


        //Setting discount if any (It is Optional field)
        cellReference = new CellReference(ExcelConstants.INDEX_BASED_CELL_REFERENCE_FOR_COMMERCIAL_PRICING.get(ExcelConstants.CommercialSheetFieldNames.DISCOUNT.getFieldName()));
        if (checkIsEmptyStringAndRegex(sheet.getRow(cellReference.getRow()).getCell(cellReference.getCol()), ExcelConstants.CommercialSheetFieldNames.DISCOUNT.getFieldName(), ExcelConstants.PERCENTAGE_REGEX, sheet.getSheetName(), ExcelConstants.PERCENTAGE, false)) {
            indexDetails.setDiscount_pct(Float.parseFloat(df.formatCellValue(getCellValueFromCellReference(cellReference, sheet)).replace(Constants.PERCENT, Constants.EMPTY_STRING)));
        }

        //Setting additional adjustment if any (It is Optional field)
        cellReference = new CellReference(ExcelConstants.INDEX_BASED_CELL_REFERENCE_FOR_COMMERCIAL_PRICING.get(ExcelConstants.CommercialSheetFieldNames.ADDITIONAL_ADJUSTMENT_ADMT.getFieldName()));
        if (checkIsEmptyStringAndRegex(sheet.getRow(cellReference.getRow()).getCell(cellReference.getCol()),ExcelConstants.CommercialSheetFieldNames.ADDITIONAL_ADJUSTMENT_ADMT.getFieldName(),ExcelConstants.DOLLER_REGEX_WITH_MINUS_SIGN, sheet.getSheetName(), ExcelConstants.CURRENCY_DOLLAR,false)) {
            indexDetails.setAdditional_adjustment(Float.parseFloat(df.formatCellValue(getCellValueFromCellReference(cellReference, sheet)).replace(Constants.DOLLAR,Constants.EMPTY_STRING)));
        }
        //Setting comments if any (Optional field)
        if (checkIsCellEmpty(new CellReference(ExcelConstants.INDEX_BASED_CELL_REFERENCE_FOR_COMMERCIAL_PRICING.get(ExcelConstants.CommercialSheetFieldNames.COMMENTS.getFieldName())), sheet)) {
            pricingDetail.setComments(df.formatCellValue(getCellValueFromCellReference(new CellReference(ExcelConstants.INDEX_BASED_CELL_REFERENCE_FOR_COMMERCIAL_PRICING.get(ExcelConstants.CommercialSheetFieldNames.COMMENTS.getFieldName())), sheet)));
        }

    }

    // check cell is empty or not
    private static boolean checkIsCellEmpty(CellReference cellReference, Sheet sheet) {

        Row row = sheet.getRow(cellReference.getRow());
        if (row == null) {
            return false;
        } else {
            Cell cell = row.getCell(cellReference.getCol());
            if (cell == null || df.formatCellValue(cell) == null || df.formatCellValue(cell).trim().isEmpty()) {
                return false;
            } else {
                return true;
            }
        }
    }

    //Method to set payment_term,Monthly Negotiation, price_ceil, price_floor,  ceil_floor_period_start, ceil_floor_period_end,(Collars Section)
    private static void parsingPricingDetailsBasicValues(Sheet sheet, PricingDetail pricingDetail) {
        //Setting price Uom
        pricingDetail.setPrice_uom(ExcelConstants.PRICE_UOM);
        //Setting payment term (Mandatory field)
        if (Boolean.TRUE.equals(isCellEmptyAndValidateRegex(new CellReference(ExcelConstants.PRICING_DETAILS_CELL_REFERENCE_FOR_COMMERCIAL_PRICING.get(ExcelConstants.CommercialSheetFieldNames.PAYMENT_TERM.getFieldName())), sheet, ExcelConstants.CommercialSheetFieldNames.PAYMENT_TERM.getFieldName(), Optional.empty(), ExcelConstants.ALPHABETS))) {
            pricingDetail.setPayment_term(df.formatCellValue(getCellValueFromCellReference(new CellReference(ExcelConstants.PRICING_DETAILS_CELL_REFERENCE_FOR_COMMERCIAL_PRICING.get(ExcelConstants.CommercialSheetFieldNames.PAYMENT_TERM.getFieldName())), sheet)));
        }

        //Setting Monthly negotiation (Optional Field)
        CellReference cellReference = new CellReference(ExcelConstants.PRICING_DETAILS_CELL_REFERENCE_FOR_COMMERCIAL_PRICING.get(ExcelConstants.CommercialSheetFieldNames.MONTHLY_NEGOTIATION.getFieldName()));
        if (checkIsEmptyStringAndRegex(sheet.getRow(cellReference.getRow()).getCell(cellReference.getCol()), ExcelConstants.CommercialSheetFieldNames.MONTHLY_NEGOTIATION.getFieldName(), ExcelConstants.YES_OR_NO_REGEX, sheet.getSheetName(), ExcelConstants.YES_OR_NO, false)) {
            //If Y then set to true else false
            if (df.formatCellValue(getCellValueFromCellReference(cellReference, sheet)).equals(Constants.YES_STRING)) {
                pricingDetail.setMonthly_negotiation(true);
            } else {
                pricingDetail.setMonthly_negotiation(false);
            }
//            pricingDetail.setMonthly_negotiation(df.formatCellValue(getCellValueFromCellReference(cellReference, sheet)));
        }

        int errorCount = errorMessageDetailsList.size();
        //Setting Price Floor (Optional field)
        cellReference = new CellReference(ExcelConstants.PRICING_DETAILS_CELL_REFERENCE_FOR_COMMERCIAL_PRICING.get(ExcelConstants.CommercialSheetFieldNames.PRICE_FLOOR_ADMT.getFieldName()));
        //
        if(!isCellValueNA(sheet,sheet.getRow(cellReference.getRow()).getCell(cellReference.getCol()))) {
            if (checkIsEmptyStringAndRegex(sheet.getRow(cellReference.getRow()).getCell(cellReference.getCol()), ExcelConstants.CommercialSheetFieldNames.PRICE_FLOOR_ADMT.getFieldName(), ExcelConstants.DOLLER_REGEX, sheet.getSheetName(), ExcelConstants.CURRENCY_DOLLAR, false)) {
                if (!df.formatCellValue(getCellValueFromCellReference(cellReference, sheet)).replace(Constants.DOLLAR, Constants.EMPTY_STRING).toLowerCase().trim().equals(ExcelConstants.NA.toLowerCase()))
                    pricingDetail.setPrice_floor(Math.round(Double.parseDouble(df.formatCellValue(getCellValueFromCellReference(cellReference, sheet)).replace(Constants.DOLLAR, Constants.EMPTY_STRING))));
            }
        }
        //Setting Price Ceil (Optional field)
        cellReference = new CellReference(ExcelConstants.PRICING_DETAILS_CELL_REFERENCE_FOR_COMMERCIAL_PRICING.get(ExcelConstants.CommercialSheetFieldNames.PRICE_CEILING_ADMT.getFieldName()));
        if(!isCellValueNA(sheet,sheet.getRow(cellReference.getRow()).getCell(cellReference.getCol()))) {
            if (checkIsEmptyStringAndRegex(sheet.getRow(cellReference.getRow()).getCell(cellReference.getCol()), ExcelConstants.CommercialSheetFieldNames.PRICE_CEILING_ADMT.getFieldName(), ExcelConstants.DOLLER_REGEX, sheet.getSheetName(), ExcelConstants.CURRENCY_DOLLAR, false)) {
                if (!df.formatCellValue(getCellValueFromCellReference(cellReference, sheet)).replace(Constants.DOLLAR, Constants.EMPTY_STRING).toLowerCase().trim().equals(ExcelConstants.NA.toLowerCase()))
                    pricingDetail.setPrice_ceil(Math.round(Double.parseDouble(df.formatCellValue(getCellValueFromCellReference(cellReference, sheet)).replace(Constants.DOLLAR, Constants.EMPTY_STRING))));
            }
        }
        //Setting Price Floor Period start and End (Optional field)
        cellReference = new CellReference(ExcelConstants.PRICING_DETAILS_CELL_REFERENCE_FOR_COMMERCIAL_PRICING.get(ExcelConstants.CommercialSheetFieldNames.PRICE_CEILING_FLOOR_PERIOD.getFieldName()));
        if(!isCellValueNA(sheet,sheet.getRow(cellReference.getRow()).getCell(cellReference.getCol()))) {
            if (checkIsEmptyStringAndRegex(sheet.getRow(cellReference.getRow()).getCell(cellReference.getCol()), ExcelConstants.CommercialSheetFieldNames.PRICE_CEILING_FLOOR_PERIOD.getFieldName(), ExcelConstants.EFFECTIVE_DATE_REGEX, sheet.getSheetName(), ExcelConstants.EFFECTIVE_DATE, false)) {
                String effectivePeriodString = df.formatCellValue(getCellValueFromCellReference(cellReference, sheet));
                if (!Objects.equals(effectivePeriodString.toLowerCase().trim(), ExcelConstants.NA.toLowerCase())) {
                    //Split the String by Hyphen and set start and end
                    String[] parts = effectivePeriodString.split(Constants.HYPHEN);
                    pricingDetail.setCeil_floor_period_start(parts[0].trim());
                    pricingDetail.setCeil_floor_period_end(parts[1].trim());
                }
            }
        }

        //Validation For Price Ceil,Price Flore,And Period
        if (errorCount == errorMessageDetailsList.size())
            validationForCeilFloorPeriod(pricingDetail.getPrice_ceil(),pricingDetail.getPrice_floor(),pricingDetail.getCeil_floor_period_start(),pricingDetail.getCeil_floor_period_end());

        pricingDetail.setDiscount_uom(Constants.UNIT_OF_MEASURE);
        pricingDetail.setCeil_floor_uom(Constants.UNIT_OF_MEASURE);

    }


    //Method to Populate Price Tier Discounts From Commercial Excel sheet
    private static PriceTierDiscounts populatePriceTierDiscountsFromExcel(Sheet sheet) {
        LOGGER.info("In ExcelParsingUtils :: populatePriceTierDiscountsFromExcel");
        PriceTierDiscounts priceTierDiscounts = new PriceTierDiscounts();
        CellReference cellReference = new CellReference(ExcelConstants.PRICE_TIER_FIELDS_NAME_VALUES_TO_CELLREFERENCE_MAP.get(ExcelConstants.CommercialSheetFieldNames.TIER_BASED_PRICING_DISCOUNT.getFieldName()));
        //Checking if is Tier Based Discount (Y or N), if Blank then assume it as N
        if (Utils.isCellEmpty(sheet, cellReference.getRow(), cellReference.getCol())) {
            priceTierDiscounts.setIs_tier_based_discount(false);
        }else{
            String isTierBasedDiscount = df.formatCellValue(sheet.getRow(cellReference.getRow()).getCell(cellReference.getCol()));
            priceTierDiscounts.setIs_tier_based_discount(isTierBasedDiscount.equals(Constants.YES_STRING));
        }

        //Starting rowIndex and colIndex
        int rowIndex = new CellReference(ExcelConstants.PRICE_TIER_1_FIELDS_NAME_VALUES_TO_CELLREFERENCE_MAP.get(ExcelConstants.CommercialSheetFieldNames.PRICE_TIER_CATEGORY.getFieldName())).getRow();
        int colIndex = new CellReference(ExcelConstants.PRICE_TIER_1_FIELDS_NAME_VALUES_TO_CELLREFERENCE_MAP.get(ExcelConstants.CommercialSheetFieldNames.PRICE_TIER_CATEGORY.getFieldName())).getCol();
        //If is Price Tier Discount is Y need to validate for mandatory fields
        if (priceTierDiscounts.isIs_tier_based_discount()) {
            List<PriceTiers> priceTiersList = new ArrayList<>();
            //read 3 tiers
            //3 Price Tiers so 3 times iterating
            for (int i = 0; i < 3; i++) {
                PriceTiers priceTiers = new PriceTiers();
                //Tier label
                if (Utils.isCellEmpty(sheet, rowIndex, colIndex)) {
                    errorMessageDetailsList.add(new ErrorMessageDetails(sheet.getSheetName(), new CellReference(rowIndex, colIndex).formatAsString(), ExcelConstants.DROP_DOWN_VALUE, null, ExcelConstants.CommercialSheetFieldNames.PRICE_TIER_CATEGORY.getFieldName(), MessageFormat.format(ErrorMessages.Messages.MANDATORY_FIELD_MISSING_ERROR.getMessage(), ExcelConstants.CommercialSheetFieldNames.PRICE_TIER_CATEGORY.getFieldName())));
                } else {
                    priceTiers.setTier_label(df.formatCellValue(sheet.getRow(rowIndex).getCell(colIndex)));
                }
                rowIndex++;
                //Price range : Split by -
                parseAndValidatePriceTierRange(sheet, priceTierDiscounts, priceTiers, new CellReference(rowIndex, colIndex), new CellReference(rowIndex + 1, colIndex));
                rowIndex++;
                rowIndex++;
                priceTiersList.add(priceTiers);
            }
            cellReference = new CellReference(ExcelConstants.PRICE_TIER_FIELDS_NAME_VALUES_TO_CELLREFERENCE_MAP.get(ExcelConstants.CommercialSheetFieldNames.TIER_BASED_PRICING_DISCOUNT_COMMENTS.getFieldName()));
            if (!Utils.isCellEmpty(sheet, cellReference.getRow(), cellReference.getCol())) {
                priceTierDiscounts.setComments(df.formatCellValue(sheet.getRow(cellReference.getRow()).getCell(cellReference.getCol())));
            }
            priceTierDiscounts.setPrice_tiers(priceTiersList);

        } else {
            //setting PriceTier to empty List
            priceTierDiscounts.setPrice_tiers(new ArrayList<>());
            //No need to read Values
            //If Price Tier is N (Then they should not enter Values) to read Values else throw error
            validateForEmptyFieldsForPriceTier(sheet,rowIndex,colIndex);
        }
        return priceTierDiscounts;

    }

    //Method to Validate If users enters any Data in Price Tiers after Selecting Is Price Tier as N
    private static void validateForEmptyFieldsForPriceTier(Sheet sheet, int rowIndex, int colIndex) {
        for (int i = 0; i < 3; i++) {
            //Tier label
            if (!Utils.isCellEmpty(sheet, rowIndex, colIndex)) {
                errorMessageDetailsList.add(new ErrorMessageDetails(sheet.getSheetName(), new CellReference(rowIndex, colIndex).formatAsString(), null, null, ExcelConstants.CommercialSheetFieldNames.PRICE_TIER_CATEGORY.getFieldName(), MessageFormat.format(ErrorMessages.Messages.NON_MANDATORY_FIELD.getMessage(), ExcelConstants.CommercialSheetFieldNames.PRICE_TIER_CATEGORY.getFieldName(),ExcelConstants.CommercialSheetFieldNames.TIER_BASED_PRICING_DISCOUNT.getFieldName())));
            }
            //Range
            rowIndex++;
            if (!Utils.isCellEmpty(sheet, rowIndex, colIndex)) {
                errorMessageDetailsList.add(new ErrorMessageDetails(sheet.getSheetName(), new CellReference(rowIndex, colIndex).formatAsString(),null, null, ExcelConstants.CommercialSheetFieldNames.PRICE_TIER_ADMT.getFieldName(), MessageFormat.format(ErrorMessages.Messages.NON_MANDATORY_FIELD.getMessage(), ExcelConstants.CommercialSheetFieldNames.PRICE_TIER_ADMT.getFieldName(),ExcelConstants.CommercialSheetFieldNames.TIER_BASED_PRICING_DISCOUNT.getFieldName())));
            }
            rowIndex++;
            //Discount
            if (!Utils.isCellEmpty(sheet, rowIndex, colIndex)) {
                errorMessageDetailsList.add(new ErrorMessageDetails(sheet.getSheetName(), new CellReference(rowIndex, colIndex).formatAsString(), null, null, ExcelConstants.CommercialSheetFieldNames.DISCOUNT_ADMT.getFieldName(), MessageFormat.format(ErrorMessages.Messages.NON_MANDATORY_FIELD.getMessage(), ExcelConstants.CommercialSheetFieldNames.DISCOUNT_ADMT.getFieldName(),ExcelConstants.CommercialSheetFieldNames.TIER_BASED_PRICING_DISCOUNT.getFieldName())));
            }
            rowIndex++;
        }
        CellReference cellReference = new CellReference(ExcelConstants.PRICE_TIER_FIELDS_NAME_VALUES_TO_CELLREFERENCE_MAP.get(ExcelConstants.CommercialSheetFieldNames.TIER_BASED_PRICING_DISCOUNT_COMMENTS.getFieldName()));
        if (!Utils.isCellEmpty(sheet, cellReference.getRow(), cellReference.getCol())) {
            errorMessageDetailsList.add(new ErrorMessageDetails(sheet.getSheetName(),cellReference.formatAsString(), null, null, ExcelConstants.CommercialSheetFieldNames.COMMENTS.getFieldName(), MessageFormat.format(ErrorMessages.Messages.NON_MANDATORY_FIELD.getMessage(), ExcelConstants.CommercialSheetFieldNames.COMMENTS.getFieldName(),ExcelConstants.CommercialSheetFieldNames.TIER_BASED_PRICING_DISCOUNT.getFieldName())));
        }
    }

    //Parsing and Populating Price Tier Discount Details :: Commercial Pricing Sheet
    private static void parseAndValidatePriceTierRange(Sheet sheet, PriceTierDiscounts priceTierDiscounts, PriceTiers priceTiers, CellReference priceTierCellRef, CellReference discountCellRef) {
        Cell priceTierCell = sheet.getRow(priceTierCellRef.getRow()).getCell(priceTierCellRef.getCol());
        Cell discountCell = sheet.getRow(discountCellRef.getRow()).getCell(discountCellRef.getCol());
        String priceTiersRangeValue = df.formatCellValue(priceTierCell);
        //Removing Above if they enter
        priceTiersRangeValue = priceTiersRangeValue.toLowerCase().replace(Constants.ABOVE,Constants.EMPTY_STRING).trim();
        //setting tierlow and tier_high If Price tier range is Empty or Not a valid throw error
        if (!checkIsEmptyStringAndRegex(priceTierCell, ExcelConstants.CommercialSheetFieldNames.PRICE_RANGE_ADMT.getFieldName(), priceTiersRangeValue.contains(Constants.HYPHEN) ? ExcelConstants.MIN_MAX_REGEX_DOLLER_OR_NUMBER_WITH_DOLLER : ExcelConstants.DOLLER_REGEX, ExcelConstants.ResponseRfpExcelSheetNames.COMMERCIAL_PRICING.getSheetName(), ExcelConstants.MIN_MAX_EXAMPLE, Boolean.TRUE)) {
            return ;
        }
        String priceTierRange = df.formatCellValue(priceTierCell);
        if (!priceTierRange.contains(Constants.HYPHEN)) {
            priceTiers.setTier_low((long) (Double.parseDouble(getNumericString(priceTierRange, Constants.DOLLAR))));
        } else {
            priceTiers.setTier_low((long) (Double.parseDouble(getNumericString(priceTierRange.split(Constants.HYPHEN)[0], Constants.DOLLAR).trim())));
            priceTiers.setTier_high((long) (Double.parseDouble(getNumericString(priceTierRange.split(Constants.HYPHEN)[1], Constants.DOLLAR).trim())));
        }
        if (priceTiers.getTier_high() != null) {
            //check if min is less than Max
            validateMinMaxPrice(priceTiers.getTier_low(), priceTiers.getTier_high(), priceTierCellRef, ResponseRfpExcelHeaders.PRICE_TIER_MIN_AND_MAX.getValue());
        }
        //Setting Discount type and val and pct
        if (checkIsEmptyStringAndRegex(discountCell, ExcelConstants.CommercialSheetFieldNames.DISCOUNT_ADMT.getFieldName(), ExcelConstants.PERCENT_OR_DOLLAR_REGEX, ExcelConstants.ResponseRfpExcelSheetNames.COMMERCIAL_PRICING.getSheetName(), ExcelConstants.PERCENT_OR_DOLLAR, Boolean.TRUE)) {
            String additionalDiscount = df.formatCellValue(discountCell);
            if (additionalDiscount.contains(Constants.PERCENT)) {
                priceTiers.setDiscount_type(Constants.PERCENT_STRING);
                parsePriceTierDiscountPercentValue(additionalDiscount, discountCellRef, priceTiers);
            } else if (additionalDiscount.contains(Constants.DOLLAR)) {
                priceTiers.setDiscount_val((long) Double.parseDouble(getNumericString(additionalDiscount, Constants.DOLLAR)));
                priceTiers.setDiscount_type(Constants.VALUE_STRING);
            }
        }
        if (priceTiers == null || priceTiers.getTier_low() == null) {
            return;
        }
        //Checking if this Price Tier Range is Overlapping with Previous Price Tiers
        if (priceTierDiscounts.getPrice_tiers() != null && !priceTierDiscounts.getPrice_tiers().isEmpty()) {
            List<PriceTiers> priceTiersList = priceTierDiscounts.getPrice_tiers();
            //validating min-max values Overlapping range is not acceptable
            validateMinMaxInclusionRangeForPriceTiers(priceTiersList, priceTiers, priceTierCellRef);
            priceTierDiscounts.getPrice_tiers().add(priceTiers);
            priceTierDiscounts.setTier_uom(Constants.UOM_ADMT);
            priceTierDiscounts.setIs_tier_based_discount(true);
        } else {
            priceTierDiscounts.setIs_tier_based_discount(true);
            priceTierDiscounts.setTier_uom(Constants.UOM_ADMT);
            priceTierDiscounts.setPrice_tiers(new ArrayList<>());
            priceTierDiscounts.getPrice_tiers().add(priceTiers);
        }


    }

    //Method to check if string is not empty and will satisfy regex (returns true if string is not empty and follows regex
    public static boolean checkIsEmptyStringAndRegex(Cell cell, String headerName, String regex, String sheetName, String regexType, Boolean isMandatory) {
        if (Boolean.FALSE.equals(Utils.checkIfStringIsNullOrEmpty(df.formatCellValue(cell).trim()))) {
            String value = df.formatCellValue(cell);
            if (regex.equals(ExcelConstants.NUMBER_REGEX) || regex.equals(ExcelConstants.DOLLER_REGEX) || regex.equals(ExcelConstants.MIN_MAX_REGEX_NUMERIC) || regex.equals(ExcelConstants.MIN_MAX_REGEX_DOLLER) || regex.equals(ExcelConstants.PERCENT_OR_DOLLAR_REGEX) || regex.equals(ExcelConstants.MIN_MAX_REGEX_DOLLER_OR_NUMBER_WITH_DOLLER) || regex.equals(ExcelConstants.MIN_MAX_REGEX_WITHOUT_DOLLAR) || regex.equals(ExcelConstants.DOLLER_REGEX_WITH_MINUS_SIGN)) {
                value = Utils.getTrimmedNumber(value);
            }
            if (regex.equals(ExcelConstants.PERCENTAGE_REGEX)) {
                value = value.replaceAll(Constants.SPACE, Constants.EMPTY_STRING);
            }
            if (regex.equals(ExcelConstants.DOLLER_REGEX)) {
                value = value.replace(Constants.HYPHEN, Constants.EMPTY_STRING);
            }
            if (validateRegex(value, regex)) {
                return true;
            } else {
                String errorMessage = MessageFormat.format(ErrorMessages.Messages.DATA_FORMAT_MISMATCH.getMessage(), regexType, df.formatCellValue(cell));
                errorMessageDetailsList.add(new ErrorMessageDetails(sheetName, new CellReference(cell).formatAsString(false), regexType, df.formatCellValue(cell), headerName, errorMessage));
            }
        } else if (Boolean.TRUE.equals(isMandatory)) {
            String errorMessage = MessageFormat.format(ErrorMessages.Messages.MANDATORY_FIELD_MISSING_ERROR.getMessage(), headerName);
            errorMessageDetailsList.add(new ErrorMessageDetails(sheetName, new CellReference(cell.getRowIndex(), cell.getColumnIndex()).formatAsString(false), ExcelConstants.ExpectedValuesList.MANDATORY.getValue(), df.formatCellValue(cell), headerName, errorMessage));
        }
        return false;
    }


    //Parsing and Populating Volume Tier Discount Details :: Commercial Pricing Sheet
    private static VolumeTierDiscounts populateVolumeTierDiscountsFromExcel(Sheet sheet, PricingDetail pricingDetail) {
        VolumeTierDiscounts volumeTierDiscounts = new VolumeTierDiscounts();
        CellReference cellReference = new CellReference(ExcelConstants.VOLUME_TIER_FIELDS_NAME_VALUES_TO_CELLREFERENCE_MAP.get(ExcelConstants.CommercialSheetFieldNames.VOLUME_BASED_PRICING_DISCOUNT.getFieldName()));
        //Checking for Y/N
        if (Utils.isCellEmpty(sheet, cellReference.getRow(), cellReference.getCol())) {
            volumeTierDiscounts.setIs_volume_based_discount(false);
        }else{
            String isVolumeBasedDiscount = df.formatCellValue(sheet.getRow(cellReference.getRow()).getCell(cellReference.getCol()));
            volumeTierDiscounts.setIs_volume_based_discount(isVolumeBasedDiscount.equals(Constants.YES_STRING));
        }


        //Starting rowIndex and colIndex
        int rowIndex = new CellReference(ExcelConstants.VOLUME_TIER_FIELDS_NAME_VALUES_TO_CELLREFERENCE_MAP.get(ExcelConstants.CommercialSheetFieldNames.VOLUME_TIER_CATEGORY.getFieldName())).getRow();
        int colIndex =new CellReference(ExcelConstants.VOLUME_TIER_FIELDS_NAME_VALUES_TO_CELLREFERENCE_MAP.get(ExcelConstants.CommercialSheetFieldNames.VOLUME_TIER_CATEGORY.getFieldName())).getCol();

        //If is Price Tier Discount is Y need to validate for mandatory fields
        if (volumeTierDiscounts.isIs_volume_based_discount()) {
            List<VolumeTiers> volumeTiersList = new ArrayList<>();
            VolumeTiers volumeTiers = new VolumeTiers();
            //Tier label
            if (Utils.isCellEmpty(sheet, rowIndex, colIndex)) {
                errorMessageDetailsList.add(new ErrorMessageDetails(sheet.getSheetName(), new CellReference(rowIndex, colIndex).formatAsString(), ExcelConstants.DROP_DOWN_VALUE, null, ExcelConstants.CommercialSheetFieldNames.VOLUME_TIER_CATEGORY.getFieldName(), MessageFormat.format(ErrorMessages.Messages.MANDATORY_FIELD_MISSING_ERROR.getMessage(), ExcelConstants.CommercialSheetFieldNames.VOLUME_TIER_CATEGORY.getFieldName())));
            } else {
                volumeTiers.setTier_label(df.formatCellValue(sheet.getRow(rowIndex).getCell(colIndex)));
            }
            //Volume Based Pricing  label (Optional Field)
            if (!Utils.isCellEmpty(sheet, rowIndex - 1, colIndex)) {
                pricingDetail.setVolume_based_period(df.formatCellValue(sheet.getRow(rowIndex - 1).getCell(colIndex)));
//                errorMessageDetailsList.add(new ErrorMessageDetails(sheet.getSheetName(), new CellReference(rowIndex - 1, colIndex).formatAsString(), ExcelConstants.DROP_DOWN_VALUE, null, ExcelConstants.CommercialSheetFieldNames.VOLUME_BASED_PRICING_PERIOD.getFieldName(), MessageFormat.format(ErrorMessages.Messages.MANDATORY_FIELD_MISSING_ERROR.getMessage(), ExcelConstants.CommercialSheetFieldNames.VOLUME_BASED_PRICING_PERIOD.getFieldName())));
            }
            rowIndex++;
            //Price range : Split by -
            parseAndValidateVolumeTierRange(sheet, volumeTierDiscounts, volumeTiers, new CellReference(rowIndex  , colIndex), new CellReference(rowIndex+1, colIndex ));
            cellReference = new CellReference(ExcelConstants.VOLUME_TIER_FIELDS_NAME_VALUES_TO_CELLREFERENCE_MAP.get(ExcelConstants.CommercialSheetFieldNames.VOLUME_BASED_REBATE_COMMENTS.getFieldName()));
            if (!Utils.isCellEmpty(sheet, cellReference.getRow(), cellReference.getCol())) {
                volumeTierDiscounts.setComments(df.formatCellValue(sheet.getRow(cellReference.getRow()).getCell(cellReference.getCol())));
            }
            volumeTiersList.add(volumeTiers);
            volumeTierDiscounts.setVolume_tiers(volumeTiersList);

        } else {
            // setting volume Tier_volume to emptyList
            volumeTierDiscounts.setVolume_tiers(new ArrayList<>());
            //No need to read Values
            //If Volume Tier is N (Then they should not enter Values) else throw error
            validateForEmptyFieldsForVolumeTier(sheet,rowIndex,colIndex);
        }
        return volumeTierDiscounts;
    }

    //If Is Volume Priced is Selected as N : Then this fields should be empty else throw error
    private static void validateForEmptyFieldsForVolumeTier(Sheet sheet, int rowIndex, int colIndex) {
        //Tier label
        if (!Utils.isCellEmpty(sheet, rowIndex, colIndex)) {
            errorMessageDetailsList.add(new ErrorMessageDetails(sheet.getSheetName(), new CellReference(rowIndex, colIndex).formatAsString(), null, null, ExcelConstants.CommercialSheetFieldNames.VOLUME_TIER_CATEGORY.getFieldName(), MessageFormat.format(ErrorMessages.Messages.NON_MANDATORY_FIELD.getMessage(), ExcelConstants.CommercialSheetFieldNames.VOLUME_TIER_CATEGORY.getFieldName(),ExcelConstants.CommercialSheetFieldNames.VOLUME_BASED_PRICING_DISCOUNT.getFieldName())));
        }
        //Pricing Period
        if (!Utils.isCellEmpty(sheet, rowIndex-1, colIndex)) {
            errorMessageDetailsList.add(new ErrorMessageDetails(sheet.getSheetName(), new CellReference(rowIndex-1, colIndex).formatAsString(), null, null, ExcelConstants.CommercialSheetFieldNames.VOLUME_BASED_PRICING_PERIOD.getFieldName(), MessageFormat.format(ErrorMessages.Messages.NON_MANDATORY_FIELD.getMessage(), ExcelConstants.CommercialSheetFieldNames.VOLUME_BASED_PRICING_PERIOD.getFieldName(),ExcelConstants.CommercialSheetFieldNames.VOLUME_BASED_PRICING_DISCOUNT.getFieldName())));
        }
        //Volume Tier Period
        rowIndex++;
        if (!Utils.isCellEmpty(sheet, rowIndex, colIndex)) {
            errorMessageDetailsList.add(new ErrorMessageDetails(sheet.getSheetName(), new CellReference(rowIndex, colIndex).formatAsString(),null, null, ExcelConstants.CommercialSheetFieldNames.VOLUME_TIER_ADMT.getFieldName(), MessageFormat.format(ErrorMessages.Messages.NON_MANDATORY_FIELD.getMessage(), ExcelConstants.CommercialSheetFieldNames.VOLUME_TIER_ADMT.getFieldName(),ExcelConstants.CommercialSheetFieldNames.VOLUME_BASED_PRICING_DISCOUNT.getFieldName())));
        }
        rowIndex++;
        //Discount
        if (!Utils.isCellEmpty(sheet, rowIndex, colIndex)) {
            errorMessageDetailsList.add(new ErrorMessageDetails(sheet.getSheetName(), new CellReference(rowIndex, colIndex).formatAsString(), null, null, ExcelConstants.CommercialSheetFieldNames.VOLUME_DISCOUNT_REBATE.getFieldName(), MessageFormat.format(ErrorMessages.Messages.NON_MANDATORY_FIELD.getMessage(), ExcelConstants.CommercialSheetFieldNames.VOLUME_DISCOUNT_REBATE.getFieldName(),ExcelConstants.CommercialSheetFieldNames.VOLUME_BASED_PRICING_DISCOUNT.getFieldName())));
        }
        //Comments
        CellReference cellReference = new CellReference(ExcelConstants.VOLUME_TIER_FIELDS_NAME_VALUES_TO_CELLREFERENCE_MAP.get(ExcelConstants.CommercialSheetFieldNames.VOLUME_BASED_REBATE_COMMENTS.getFieldName()));
        if (!Utils.isCellEmpty(sheet, cellReference.getRow(), cellReference.getCol())) {
            errorMessageDetailsList.add(new ErrorMessageDetails(sheet.getSheetName(), cellReference.formatAsString(), null, null, ExcelConstants.CommercialSheetFieldNames.COMMENTS.getFieldName(), MessageFormat.format(ErrorMessages.Messages.NON_MANDATORY_FIELD.getMessage(), ExcelConstants.CommercialSheetFieldNames.COMMENTS.getFieldName(),ExcelConstants.CommercialSheetFieldNames.VOLUME_BASED_PRICING_PERIOD.getFieldName())));
        }
    }

    private static void parseAndValidateVolumeTierRange(Sheet sheet, VolumeTierDiscounts volumeTierDiscounts, VolumeTiers volumeTiers, CellReference volumeTierRangeCellRef, CellReference volumeTierDiscCellRef) {
        Cell volumeTierCell = sheet.getRow(volumeTierRangeCellRef.getRow()).getCell(volumeTierRangeCellRef.getCol());
        Cell discountCell = sheet.getRow(volumeTierDiscCellRef.getRow()).getCell(volumeTierDiscCellRef.getCol());
        String volumeTiersRangeValue = df.formatCellValue(volumeTierCell).replace(Constants.COMMA,Constants.EMPTY_STRING);
        volumeTiersRangeValue = volumeTiersRangeValue.toLowerCase().replace(Constants.ABOVE,Constants.EMPTY_STRING).trim();
        //If Price tier range is Empty or Not a valid value return
        if (!checkIsEmptyStringAndRegex(volumeTierCell, ExcelConstants.CommercialSheetFieldNames.VOLUME_TIER_ADMT.getFieldName(), volumeTiersRangeValue.contains(Constants.HYPHEN) ? ExcelConstants.MIN_MAX_REGEX_WITHOUT_DOLLAR : ExcelConstants.NUMBER_REGEX, ExcelConstants.ResponseRfpExcelSheetNames.COMMERCIAL_PRICING.getSheetName(), ExcelConstants.MIN_MAX_EXAMPLE_WITHOUT_DOLLER, Boolean.TRUE)) {
            return;
        }
        String priceTierRange = df.formatCellValue(volumeTierCell);
        if (!priceTierRange.contains(Constants.HYPHEN)) {
            volumeTiers.setTier_low((long) (Double.parseDouble(getNumericString(priceTierRange, Constants.COMMA))));
        } else {
            volumeTiers.setTier_low((long) (Double.parseDouble(getNumericString(priceTierRange.split(Constants.HYPHEN)[0], Constants.COMMA).trim())));
            volumeTiers.setTier_high((long) (Double.parseDouble(getNumericString(priceTierRange.split(Constants.HYPHEN)[1], Constants.COMMA).trim())));
        }
        if (volumeTiers.getTier_high() != null) {
            //check if min is less than Max
            validateMinMaxPrice(volumeTiers.getTier_low(), volumeTiers.getTier_high(), volumeTierDiscCellRef, ResponseRfpExcelHeaders.PRICE_TIER_MIN_AND_MAX.getValue());
        }
        if (checkIsEmptyStringAndRegex(discountCell, ExcelConstants.CommercialSheetFieldNames.VOLUME_DISCOUNT_REBATE.getFieldName(), ExcelConstants.PERCENT_OR_DOLLAR_REGEX, ExcelConstants.ResponseRfpExcelSheetNames.COMMERCIAL_PRICING.getSheetName(), ExcelConstants.PERCENT_OR_DOLLAR, Boolean.TRUE)) {
            String additionalDiscount = df.formatCellValue(discountCell);
            if (additionalDiscount.contains(Constants.PERCENT)) {
                volumeTiers.setDiscount_type(Constants.PERCENT_STRING);
                parseVolumeTierDiscountPercentValue(additionalDiscount, new CellReference(discountCell), volumeTiers);
            } else if (additionalDiscount.contains(Constants.DOLLAR)) {
                volumeTiers.setDiscount_val((long) Double.parseDouble(getNumericString(additionalDiscount, Constants.DOLLAR)));
                volumeTiers.setDiscount_type(Constants.VALUE_STRING);
            }
        }

        if (volumeTiers == null || volumeTiers.getTier_low()==null) {
            return;
        }
        //Checking if this Price Tier Range is Overlapping with Previous Price Tiers
        if (volumeTierDiscounts.getVolume_tiers() != null) {
            List<VolumeTiers> volumeTiersList = volumeTierDiscounts.getVolume_tiers();
            //validating min-max values inclusion range is not acceptable
            validateMinMaxInclusionRangeForVolumeTiers(volumeTiersList, volumeTiers, volumeTierRangeCellRef);
            volumeTierDiscounts.getVolume_tiers().add(volumeTiers);
            volumeTierDiscounts.setTier_uom(Constants.UOM_ADMT);
            volumeTierDiscounts.setIs_volume_based_discount(true);
        } else {
            volumeTierDiscounts.setIs_volume_based_discount(true);
            volumeTierDiscounts.setTier_uom(Constants.UOM_ADMT);
            volumeTierDiscounts.setVolume_tiers(new ArrayList<>());
            volumeTierDiscounts.getVolume_tiers().add(volumeTiers);
        }
    }

    //Method to populate Port Rebate Details
    private static PortRebates populatePortRebateFromExcel(Sheet sheet) {
        PortRebates portRebates = new PortRebates();
        CellReference portRebateCellRef = new CellReference(ExcelConstants.PORT_REBATE_FIELDS_NAME_VALUES_TO_CELLREFERENCE_MAP.get(ExcelConstants.CommercialSheetFieldNames.PORT_REBATE.getFieldName()));
        CellReference portRebateCommentsCellRef = new CellReference(ExcelConstants.PORT_REBATE_FIELDS_NAME_VALUES_TO_CELLREFERENCE_MAP.get(ExcelConstants.CommercialSheetFieldNames.PORT_REBATE_COMMENTS.getFieldName()));
//        //setting Discount (Optional field)
//        if(!isCellValueNA(sheet,sheet.getRow(portRebateCellRef.getRow()).getCell(portRebateCellRef.getCol()))) {
//            if (checkIsEmptyStringAndRegex(sheet.getRow(portRebateCellRef.getRow()).getCell(portRebateCellRef.getCol()), ExcelConstants.CommercialSheetFieldNames.PORT_REBATE.getFieldName(), ExcelConstants.DOLLER_REGEX, sheet.getSheetName(), ExcelConstants.CURRENCY_DOLLAR, false)) {
////                if (!Objects.equals(df.formatCellValue(sheet.getRow(portRebateCellRef.getRow()).getCell(portRebateCellRef.getCol())).toLowerCase().trim(), ExcelConstants.NA.toLowerCase())) {
//                    String value = df.formatCellValue(sheet.getRow(portRebateCellRef.getRow()).getCell(portRebateCellRef.getCol()));
//                    value = Utils.getTrimmedNumber(value);
//                    portRebates.setDiscount_val(Utils.getLongDoller(value));
////                }
//            }
//        }
        //setting Comments (Optional field)
        if (!Utils.isCellEmpty(sheet, portRebateCommentsCellRef.getRow(), portRebateCommentsCellRef.getCol())) {
            if(!Objects.equals(df.formatCellValue(sheet.getRow(portRebateCommentsCellRef.getRow()).getCell(portRebateCommentsCellRef.getCol())).toLowerCase().trim(), ExcelConstants.NA.toLowerCase()))
                portRebates.setComments(df.formatCellValue(sheet.getRow(portRebateCommentsCellRef.getRow()).getCell(portRebateCommentsCellRef.getCol())));
        }
        portRebates.setDiscount_uom(Constants.UNIT_OF_MEASURE);
        return portRebates;
    }


    private static void verifyLogisticFields(Sheet logisticSheet, Map<String, Integer> logisticMap) {

        for (String key : logisticMap.keySet()) {

            Integer updatedNum = (logisticMap.get(key)) + 1;
            String cellReference = "B" + updatedNum.toString(); //TODO (New) : Move to constant

            CellReference cellRef = new CellReference(cellReference);
            Cell cell = logisticSheet.getRow(cellRef.getRow()).getCell(cellRef.getCol());
            String givenHeaderValue = df.formatCellValue(cell);

            if (givenHeaderValue != null && !givenHeaderValue.isEmpty()) {
                // if the Wrong Header Value is Given then adding to ErrorList
                if (!givenHeaderValue.equals(key)) {
                    String errorMessage = MessageFormat.format(ErrorMessages.Messages.HEADER_INVALID.getMessage(), givenHeaderValue, key);
                    errorMessageDetailsList.add(new ErrorMessageDetails(logisticSheet.getSheetName(), cellReference, null, null, null, errorMessage));
                }

            } else {
                //Building Error Message with Comma separated Header Names which are missing
                String errorMessage = MessageFormat.format(ErrorMessages.Messages.HEADERS_NOT_FOUND.getMessage(), key);
                errorMessageDetailsList.add(new ErrorMessageDetails(logisticSheet.getSheetName(), cellReference, null, null, null, errorMessage));
            }
        }


    }

    public static void mandatoryFieldsValidationLogisticSheet(Integer col, Sheet logisticSheet) {

        String firstUsPortOfEntry = df.formatCellValue(logisticSheet.getRow(logisticPricingSecondPortEntryFieldNameToRowMap.get(ExcelConstants.LogisticPricingFields.US_PORT_OF_ENTRY.getFieldName())).getCell(col));

        validateMandatoryFieldsLogisticSheet(col, logisticSheet, logisticPricingFieldNameToRowMap, Boolean.TRUE, firstUsPortOfEntry);
        validateMandatoryFieldsLogisticSheet(col, logisticSheet, logisticPricingFirstPortEntryFieldNameToRowMap, Boolean.TRUE, firstUsPortOfEntry);
        validateMandatoryFieldsLogisticSheet(col, logisticSheet, logisticPricingFirstPortEntryIncoterm1FieldNameToRowMap, Boolean.TRUE, firstUsPortOfEntry);

        //If second port of entry is present need to validate those value also
        Row secondPortEntryRow = logisticSheet.getRow(logisticPricingSecondPortEntryFieldNameToRowMap.get(ExcelConstants.LogisticPricingFields.US_PORT_OF_ENTRY.getFieldName()));

        if (df.formatCellValue(secondPortEntryRow.getCell(col)) != null && !df.formatCellValue(secondPortEntryRow.getCell(col)).isEmpty()) {
            validateMandatoryFieldsLogisticSheet(col, logisticSheet, logisticPricingSecondPortEntryFieldNameToRowMap, Boolean.TRUE, df.formatCellValue(secondPortEntryRow.getCell(col)));
            validateMandatoryFieldsLogisticSheet(col, logisticSheet, logisticPricingSecondPortEntryIncoterm1FieldNameToRowMap, Boolean.TRUE, df.formatCellValue(secondPortEntryRow.getCell(col)));
        }else {
            validateMandatoryFieldsLogisticSheet(col, logisticSheet, logisticPricingSecondPortEntryFieldNameToRowMap, Boolean.FALSE, df.formatCellValue(secondPortEntryRow.getCell(col)));
            validateMandatoryFieldsLogisticSheet(col, logisticSheet, logisticPricingSecondPortEntryIncoterm1FieldNameToRowMap, Boolean.FALSE, df.formatCellValue(secondPortEntryRow.getCell(col)));
        }

    }

    // Logistic Pricing functinos
    private static void validateMandatoryFieldsLogisticSheet(int col, Sheet logisticSheet, Map<String, Integer> logisticMap, Boolean isUsPortPresent, String usPortEntry) {
        for (Map.Entry<String, Integer> entry : logisticMap.entrySet()) {
            String fieldName = entry.getKey();

            // Removing mandatory validations for specific fields
            if(Objects.equals(fieldName, ExcelConstants.LogisticPricingFields.INLAND_TRANSPORTATION_ROUTE.getFieldName()) || Objects.equals(fieldName, ExcelConstants.LogisticPricingFields.STEAMSHIP_LINE.getFieldName()) || Objects.equals(fieldName, ExcelConstants.LogisticPricingFields.ORIGIN_PORT.getFieldName())){
                continue;
            }

            if(ExcelConstants.OceanFreightDetailsList.contains(fieldName) && Objects.equals(usPortEntry, ExcelConstants.USPortOfEntry.DOMESTIC.getValue())){
                continue;
            }


            if(isUsPortPresent == Boolean.FALSE) {
                Cell cell = logisticSheet.getRow(entry.getValue()).getCell(col);
                if((df.formatCellValue(cell) != null && !df.formatCellValue(cell).isEmpty())  && isOneTime == Boolean.FALSE){
                    String errorMessage = ErrorMessages.Messages.PORT_ENTRY_REQUIRE_ERROR.getMessage();
                    errorMessageDetailsList.add(new ErrorMessageDetails(logisticSheet.getSheetName(), new CellReference(logisticPricingSecondPortEntryFieldNameToRowMap.get(ExcelConstants.LogisticPricingFields.US_PORT_OF_ENTRY.getFieldName()), col).formatAsString(false), ExcelConstants.ExpectedValuesList.MANDATORY.getValue(), df.formatCellValue(logisticSheet.getRow(logisticPricingSecondPortEntryFieldNameToRowMap.get(ExcelConstants.LogisticPricingFields.US_PORT_OF_ENTRY.getFieldName())).getCell(col)), ExcelConstants.LogisticPricingFields.US_PORT_OF_ENTRY.getFieldName(), errorMessage));
                    isOneTime = Boolean.TRUE;
                }
            }


            if (((isUsPortPresent == Boolean.TRUE && !Objects.equals(usPortEntry, ExcelConstants.USPortOfEntry.DOMESTIC.getValue())) && (Objects.equals(fieldName, ExcelConstants.LogisticPricingFields.OCEAN_FREIGHT.getFieldName()))) || (isUsPortPresent == Boolean.TRUE &&  Objects.equals(fieldName, ExcelConstants.LogisticPricingFields.TRANSIT_COST_US_PORT_TO_SAFETY_STOCK.getFieldName())) || (!Objects.equals(fieldName, ExcelConstants.LogisticPricingFields.GP_MILL.getFieldName()) && fieldName.contains("*") && isUsPortPresent == Boolean.TRUE)) {
                Cell cell = logisticSheet.getRow(entry.getValue()).getCell(col);
                if (df.formatCellValue(cell) == null || df.formatCellValue(cell).isEmpty()) {
                    String errorMessage = MessageFormat.format(ErrorMessages.Messages.MANDATORY_FIELD_MISSING_ERROR.getMessage(), fieldName);
                    errorMessageDetailsList.add(new ErrorMessageDetails(logisticSheet.getSheetName(), new CellReference(entry.getValue(), col).formatAsString(false), ExcelConstants.ExpectedValuesList.MANDATORY.getValue(), df.formatCellValue(cell), fieldName, errorMessage));
                }
            }
        }
    }


    private static void dataFormatValidationLogisticSheet(int col, Sheet logisticSheet) {
        //First US Port
        validateDataFormatLogisticSheet(logisticSheet, logisticPricingFieldNameToRowMap.get(ExcelConstants.LogisticPricingFields.SUPPLIER_BID_VOLUME.getFieldName()), col, ExcelConstants.LogisticPricingFields.SUPPLIER_BID_VOLUME.getFieldName(), ExcelConstants.NUMBER_REGEX, ExcelConstants.ResponseRfpExcelSheetNames.LOGISTIC_PRICING.getSheetName(), ExcelConstants.NUMBER, Boolean.TRUE, Boolean.FALSE);
        validateDataFormatLogisticSheet(logisticSheet, logisticPricingFirstPortEntryFieldNameToRowMap.get(ExcelConstants.LogisticPricingFields.PORT_FREE_TIME_IN_DAYS.getFieldName()), col, ExcelConstants.LogisticPricingFields.PORT_FREE_TIME_IN_DAYS.getFieldName(), ExcelConstants.NUMBER_REGEX, ExcelConstants.ResponseRfpExcelSheetNames.LOGISTIC_PRICING.getSheetName(), ExcelConstants.NUMBER, Boolean.TRUE, Boolean.TRUE);
        validateDataFormatLogisticSheet(logisticSheet, logisticPricingFirstPortEntryFieldNameToRowMap.get(ExcelConstants.LogisticPricingFields.OCEAN_FREIGHT.getFieldName()), col, ExcelConstants.LogisticPricingFields.OCEAN_FREIGHT.getFieldName(), ExcelConstants.DOLLER_REGEX, ExcelConstants.ResponseRfpExcelSheetNames.LOGISTIC_PRICING.getSheetName(), ExcelConstants.CURRENCY_DOLLAR, Boolean.FALSE, Boolean.TRUE);
        validateDataFormatLogisticSheet(logisticSheet, logisticPricingFirstPortEntryFieldNameToRowMap.get(ExcelConstants.LogisticPricingFields.NOMINATED_SAFETY_STOCK.getFieldName()), col, ExcelConstants.LogisticPricingFields.NOMINATED_SAFETY_STOCK.getFieldName(), ExcelConstants.NUMBER_REGEX, ExcelConstants.ResponseRfpExcelSheetNames.LOGISTIC_PRICING.getSheetName(), ExcelConstants.NUMBER, Boolean.TRUE, Boolean.FALSE);
        validateDataFormatLogisticSheet(logisticSheet, logisticPricingFirstPortEntryFieldNameToRowMap.get(ExcelConstants.LogisticPricingFields.TRANSIT_COST.getFieldName()), col, ExcelConstants.LogisticPricingFields.TRANSIT_COST.getFieldName(), ExcelConstants.DOLLER_REGEX, ExcelConstants.ResponseRfpExcelSheetNames.LOGISTIC_PRICING.getSheetName(), ExcelConstants.CURRENCY_DOLLAR, Boolean.FALSE, Boolean.TRUE);
        validateDataFormatLogisticSheet(logisticSheet, logisticPricingFirstPortEntryFieldNameToRowMap.get(ExcelConstants.LogisticPricingFields.TRANSIT_LEAD_TIME_IN_DAYS.getFieldName()), col, ExcelConstants.LogisticPricingFields.TRANSIT_LEAD_TIME_IN_DAYS.getFieldName(), ExcelConstants.NUMBER_REGEX, ExcelConstants.ResponseRfpExcelSheetNames.LOGISTIC_PRICING.getSheetName(), ExcelConstants.NUMBER, Boolean.TRUE, Boolean.TRUE);
//        validateDataFormatLogisticSheet(logisticSheet, logisticPricingFieldNameToRowMap.get(ExcelConstants.LogisticPricingFields.SUPPLIER_MILL.getFieldName()), col, ExcelConstants.LogisticPricingFields.SUPPLIER_MILL.getFieldName(), ExcelConstants.ALPHABETS_REGEX, ExcelConstants.ResponseRfpExcelSheetNames.LOGISTIC_PRICING.getSheetName(), ExcelConstants.ALPHABETS, Boolean.TRUE, Boolean.FALSE);
        validateDataFormatLogisticSheet(logisticSheet, logisticPricingFirstPortEntryFieldNameToRowMap.get(ExcelConstants.LogisticPricingFields.STEAMSHIP_LINE.getFieldName()), col, ExcelConstants.LogisticPricingFields.STEAMSHIP_LINE.getFieldName(), ExcelConstants.ALPHA_NUMERIC_REGEX, ExcelConstants.ResponseRfpExcelSheetNames.LOGISTIC_PRICING.getSheetName(), ExcelConstants.ALPHA_NUMERIC, Boolean.TRUE, Boolean.TRUE);
        validateDataFormatLogisticSheet(logisticSheet, logisticPricingFieldNameToRowMap.get(ExcelConstants.LogisticPricingFields.ORIGIN_PORT.getFieldName()), col, ExcelConstants.LogisticPricingFields.ORIGIN_PORT.getFieldName(), ExcelConstants.ALPHABETS_REGEX, ExcelConstants.ResponseRfpExcelSheetNames.LOGISTIC_PRICING.getSheetName(), ExcelConstants.ALPHABETS, Boolean.TRUE, Boolean.FALSE);
        validateDataFormatLogisticSheet(logisticSheet, logisticPricingFieldNameToRowMap.get(ExcelConstants.LogisticPricingFields.ORIGIN_COUNTRY.getFieldName()), col, ExcelConstants.LogisticPricingFields.ORIGIN_COUNTRY.getFieldName(), ExcelConstants.ALPHABETS_REGEX, ExcelConstants.ResponseRfpExcelSheetNames.LOGISTIC_PRICING.getSheetName(), ExcelConstants.ALPHABETS, Boolean.TRUE, Boolean.FALSE);
        validateDataFormatLogisticSheet(logisticSheet, logisticPricingFirstPortEntryFieldNameToRowMap.get(ExcelConstants.LogisticPricingFields.TRANSIT_LEAD_TIME_TO_GP_MILL.getFieldName()), col, ExcelConstants.LogisticPricingFields.TRANSIT_LEAD_TIME_TO_GP_MILL.getFieldName(), ExcelConstants.NUMBER_REGEX, ExcelConstants.ResponseRfpExcelSheetNames.LOGISTIC_PRICING.getSheetName(), ExcelConstants.NUMBER, Boolean.TRUE, Boolean.FALSE);
        validateDataFormatLogisticSheet(logisticSheet, logisticPricingFirstPortEntryFieldNameToRowMap.get(ExcelConstants.LogisticPricingFields.TRANSIT_COST_US_PORT_TO_SAFETY_STOCK.getFieldName()), col, ExcelConstants.LogisticPricingFields.TRANSIT_COST_US_PORT_TO_SAFETY_STOCK.getFieldName(), ExcelConstants.DOLLER_REGEX, ExcelConstants.ResponseRfpExcelSheetNames.LOGISTIC_PRICING.getSheetName(), ExcelConstants.CURRENCY_DOLLAR, Boolean.FALSE, Boolean.TRUE);



        // First US Port Incoterms 1
        validateDataFormatLogisticSheet(logisticSheet, logisticPricingFirstPortEntryIncoterm1FieldNameToRowMap.get(ExcelConstants.LogisticPricingFields.STEVEDORING_COST.getFieldName()), col, ExcelConstants.LogisticPricingFields.STEVEDORING_COST.getFieldName(), ExcelConstants.DOLLER_REGEX, ExcelConstants.ResponseRfpExcelSheetNames.LOGISTIC_PRICING.getSheetName(), ExcelConstants.CURRENCY_DOLLAR, Boolean.FALSE, Boolean.TRUE);
        validateDataFormatLogisticSheet(logisticSheet, logisticPricingFirstPortEntryIncoterm1FieldNameToRowMap.get(ExcelConstants.LogisticPricingFields.HANDLING_COST.getFieldName()), col, ExcelConstants.LogisticPricingFields.HANDLING_COST.getFieldName(), ExcelConstants.DOLLER_REGEX, ExcelConstants.ResponseRfpExcelSheetNames.LOGISTIC_PRICING.getSheetName(), ExcelConstants.CURRENCY_DOLLAR, Boolean.FALSE, Boolean.TRUE);
        validateDataFormatLogisticSheet(logisticSheet, logisticPricingFirstPortEntryIncoterm1FieldNameToRowMap.get(ExcelConstants.LogisticPricingFields.WHARFAGE_COST.getFieldName()), col, ExcelConstants.LogisticPricingFields.WHARFAGE_COST.getFieldName(), ExcelConstants.DOLLER_REGEX, ExcelConstants.ResponseRfpExcelSheetNames.LOGISTIC_PRICING.getSheetName(), ExcelConstants.CURRENCY_DOLLAR, Boolean.FALSE, Boolean.TRUE);
        validateDataFormatLogisticSheet(logisticSheet, logisticPricingFirstPortEntryIncoterm1FieldNameToRowMap.get(ExcelConstants.LogisticPricingFields.SECURITY_COST.getFieldName()), col, ExcelConstants.LogisticPricingFields.SECURITY_COST.getFieldName(), ExcelConstants.DOLLER_REGEX, ExcelConstants.ResponseRfpExcelSheetNames.LOGISTIC_PRICING.getSheetName(), ExcelConstants.CURRENCY_DOLLAR, Boolean.FALSE, Boolean.TRUE);
        validateDataFormatLogisticSheet(logisticSheet, logisticPricingFirstPortEntryIncoterm1FieldNameToRowMap.get(ExcelConstants.LogisticPricingFields.WAREHOUSING_FEE_MONTHLY.getFieldName()), col, ExcelConstants.LogisticPricingFields.WAREHOUSING_FEE_MONTHLY.getFieldName(), ExcelConstants.DOLLER_REGEX, ExcelConstants.ResponseRfpExcelSheetNames.LOGISTIC_PRICING.getSheetName(), ExcelConstants.CURRENCY_DOLLAR, Boolean.FALSE, Boolean.TRUE);
        validateDataFormatLogisticSheet(logisticSheet, logisticPricingFirstPortEntryIncoterm1FieldNameToRowMap.get(ExcelConstants.LogisticPricingFields.IMPORT_CUSTOMS_TARIFF_FEE.getFieldName()), col, ExcelConstants.LogisticPricingFields.IMPORT_CUSTOMS_TARIFF_FEE.getFieldName(), ExcelConstants.DOLLER_REGEX, ExcelConstants.ResponseRfpExcelSheetNames.LOGISTIC_PRICING.getSheetName(), ExcelConstants.CURRENCY_DOLLAR, Boolean.FALSE, Boolean.TRUE);

        // First US Port Incoterms 2
        validateDataFormatLogisticSheet(logisticSheet, logisticPricingFirstPortEntryIncoterm2FieldNameToRowMap.get(ExcelConstants.LogisticPricingFields.STEVEDORING_COST.getFieldName()), col, ExcelConstants.LogisticPricingFields.STEVEDORING_COST.getFieldName(), ExcelConstants.DOLLER_REGEX, ExcelConstants.ResponseRfpExcelSheetNames.LOGISTIC_PRICING.getSheetName(), ExcelConstants.CURRENCY_DOLLAR, Boolean.FALSE, Boolean.TRUE);
        validateDataFormatLogisticSheet(logisticSheet, logisticPricingFirstPortEntryIncoterm2FieldNameToRowMap.get(ExcelConstants.LogisticPricingFields.HANDLING_COST.getFieldName()), col, ExcelConstants.LogisticPricingFields.HANDLING_COST.getFieldName(), ExcelConstants.DOLLER_REGEX, ExcelConstants.ResponseRfpExcelSheetNames.LOGISTIC_PRICING.getSheetName(), ExcelConstants.CURRENCY_DOLLAR, Boolean.FALSE, Boolean.TRUE);
        validateDataFormatLogisticSheet(logisticSheet, logisticPricingFirstPortEntryIncoterm2FieldNameToRowMap.get(ExcelConstants.LogisticPricingFields.WHARFAGE_COST.getFieldName()), col, ExcelConstants.LogisticPricingFields.WHARFAGE_COST.getFieldName(), ExcelConstants.DOLLER_REGEX, ExcelConstants.ResponseRfpExcelSheetNames.LOGISTIC_PRICING.getSheetName(), ExcelConstants.CURRENCY_DOLLAR, Boolean.FALSE, Boolean.TRUE);
        validateDataFormatLogisticSheet(logisticSheet, logisticPricingFirstPortEntryIncoterm2FieldNameToRowMap.get(ExcelConstants.LogisticPricingFields.SECURITY_COST.getFieldName()), col, ExcelConstants.LogisticPricingFields.SECURITY_COST.getFieldName(), ExcelConstants.DOLLER_REGEX, ExcelConstants.ResponseRfpExcelSheetNames.LOGISTIC_PRICING.getSheetName(), ExcelConstants.CURRENCY_DOLLAR, Boolean.FALSE, Boolean.TRUE);
        validateDataFormatLogisticSheet(logisticSheet, logisticPricingFirstPortEntryIncoterm2FieldNameToRowMap.get(ExcelConstants.LogisticPricingFields.WAREHOUSING_FEE_MONTHLY.getFieldName()), col, ExcelConstants.LogisticPricingFields.WAREHOUSING_FEE_MONTHLY.getFieldName(), ExcelConstants.DOLLER_REGEX, ExcelConstants.ResponseRfpExcelSheetNames.LOGISTIC_PRICING.getSheetName(), ExcelConstants.CURRENCY_DOLLAR, Boolean.FALSE, Boolean.TRUE);
        validateDataFormatLogisticSheet(logisticSheet, logisticPricingFirstPortEntryIncoterm2FieldNameToRowMap.get(ExcelConstants.LogisticPricingFields.IMPORT_CUSTOMS_TARIFF_FEE.getFieldName()), col, ExcelConstants.LogisticPricingFields.IMPORT_CUSTOMS_TARIFF_FEE.getFieldName(), ExcelConstants.DOLLER_REGEX, ExcelConstants.ResponseRfpExcelSheetNames.LOGISTIC_PRICING.getSheetName(), ExcelConstants.CURRENCY_DOLLAR, Boolean.FALSE, Boolean.TRUE);

        Row secondPortEntryRow = logisticSheet.getRow(ExcelConstants.SECOND_US_PORT_OF_ENTRY_ROW);
        if (df.formatCellValue(secondPortEntryRow.getCell(col)) != null && !df.formatCellValue(secondPortEntryRow.getCell(col)).isEmpty()) {

            //Second US Port
            validateDataFormatLogisticSheet(logisticSheet, logisticPricingSecondPortEntryFieldNameToRowMap.get(ExcelConstants.LogisticPricingFields.PORT_FREE_TIME_IN_DAYS.getFieldName()), col, ExcelConstants.LogisticPricingFields.PORT_FREE_TIME_IN_DAYS.getFieldName(), ExcelConstants.NUMBER_REGEX, ExcelConstants.ResponseRfpExcelSheetNames.LOGISTIC_PRICING.getSheetName(), ExcelConstants.NUMBER, Boolean.TRUE, Boolean.TRUE);
            validateDataFormatLogisticSheet(logisticSheet, logisticPricingSecondPortEntryFieldNameToRowMap.get(ExcelConstants.LogisticPricingFields.OCEAN_FREIGHT.getFieldName()), col, ExcelConstants.LogisticPricingFields.OCEAN_FREIGHT.getFieldName(), ExcelConstants.DOLLER_REGEX, ExcelConstants.ResponseRfpExcelSheetNames.LOGISTIC_PRICING.getSheetName(), ExcelConstants.CURRENCY_DOLLAR, Boolean.FALSE, Boolean.TRUE);
            validateDataFormatLogisticSheet(logisticSheet, logisticPricingSecondPortEntryFieldNameToRowMap.get(ExcelConstants.LogisticPricingFields.NOMINATED_SAFETY_STOCK.getFieldName()), col, ExcelConstants.LogisticPricingFields.NOMINATED_SAFETY_STOCK.getFieldName(), ExcelConstants.NUMBER_REGEX, ExcelConstants.ResponseRfpExcelSheetNames.LOGISTIC_PRICING.getSheetName(), ExcelConstants.NUMBER, Boolean.TRUE, Boolean.FALSE);
            validateDataFormatLogisticSheet(logisticSheet, logisticPricingSecondPortEntryFieldNameToRowMap.get(ExcelConstants.LogisticPricingFields.TRANSIT_COST.getFieldName()), col, ExcelConstants.LogisticPricingFields.TRANSIT_COST.getFieldName(), ExcelConstants.DOLLER_REGEX, ExcelConstants.ResponseRfpExcelSheetNames.LOGISTIC_PRICING.getSheetName(), ExcelConstants.CURRENCY_DOLLAR, Boolean.FALSE, Boolean.TRUE);
            validateDataFormatLogisticSheet(logisticSheet, logisticPricingSecondPortEntryFieldNameToRowMap.get(ExcelConstants.LogisticPricingFields.TRANSIT_LEAD_TIME_IN_DAYS.getFieldName()), col, ExcelConstants.LogisticPricingFields.TRANSIT_LEAD_TIME_IN_DAYS.getFieldName(), ExcelConstants.NUMBER_REGEX, ExcelConstants.ResponseRfpExcelSheetNames.LOGISTIC_PRICING.getSheetName(), ExcelConstants.NUMBER, Boolean.TRUE, Boolean.TRUE);
            validateDataFormatLogisticSheet(logisticSheet, logisticPricingSecondPortEntryFieldNameToRowMap.get(ExcelConstants.LogisticPricingFields.STEAMSHIP_LINE.getFieldName()), col, ExcelConstants.LogisticPricingFields.STEAMSHIP_LINE.getFieldName(), ExcelConstants.ALPHA_NUMERIC_REGEX, ExcelConstants.ResponseRfpExcelSheetNames.LOGISTIC_PRICING.getSheetName(), ExcelConstants.ALPHA_NUMERIC, Boolean.TRUE, Boolean.TRUE);
            validateDataFormatLogisticSheet(logisticSheet, logisticPricingSecondPortEntryFieldNameToRowMap.get(ExcelConstants.LogisticPricingFields.TRANSIT_COST_US_PORT_TO_SAFETY_STOCK.getFieldName()), col, ExcelConstants.LogisticPricingFields.TRANSIT_COST_US_PORT_TO_SAFETY_STOCK.getFieldName(), ExcelConstants.DOLLER_REGEX, ExcelConstants.ResponseRfpExcelSheetNames.LOGISTIC_PRICING.getSheetName(), ExcelConstants.CURRENCY_DOLLAR, Boolean.FALSE, Boolean.TRUE);


            // Second US Port Incoterms 1
            validateDataFormatLogisticSheet(logisticSheet, logisticPricingSecondPortEntryIncoterm1FieldNameToRowMap.get(ExcelConstants.LogisticPricingFields.STEVEDORING_COST.getFieldName()), col, ExcelConstants.LogisticPricingFields.STEVEDORING_COST.getFieldName(), ExcelConstants.DOLLER_REGEX, ExcelConstants.ResponseRfpExcelSheetNames.LOGISTIC_PRICING.getSheetName(), ExcelConstants.CURRENCY_DOLLAR, Boolean.FALSE, Boolean.TRUE);
            validateDataFormatLogisticSheet(logisticSheet, logisticPricingSecondPortEntryIncoterm1FieldNameToRowMap.get(ExcelConstants.LogisticPricingFields.HANDLING_COST.getFieldName()), col, ExcelConstants.LogisticPricingFields.HANDLING_COST.getFieldName(), ExcelConstants.DOLLER_REGEX, ExcelConstants.ResponseRfpExcelSheetNames.LOGISTIC_PRICING.getSheetName(), ExcelConstants.CURRENCY_DOLLAR, Boolean.FALSE, Boolean.TRUE);
            validateDataFormatLogisticSheet(logisticSheet, logisticPricingSecondPortEntryIncoterm1FieldNameToRowMap.get(ExcelConstants.LogisticPricingFields.WHARFAGE_COST.getFieldName()), col, ExcelConstants.LogisticPricingFields.WHARFAGE_COST.getFieldName(), ExcelConstants.DOLLER_REGEX, ExcelConstants.ResponseRfpExcelSheetNames.LOGISTIC_PRICING.getSheetName(), ExcelConstants.CURRENCY_DOLLAR, Boolean.FALSE, Boolean.TRUE);
            validateDataFormatLogisticSheet(logisticSheet, logisticPricingSecondPortEntryIncoterm1FieldNameToRowMap.get(ExcelConstants.LogisticPricingFields.SECURITY_COST.getFieldName()), col, ExcelConstants.LogisticPricingFields.SECURITY_COST.getFieldName(), ExcelConstants.DOLLER_REGEX, ExcelConstants.ResponseRfpExcelSheetNames.LOGISTIC_PRICING.getSheetName(), ExcelConstants.CURRENCY_DOLLAR, Boolean.FALSE, Boolean.TRUE);
            validateDataFormatLogisticSheet(logisticSheet, logisticPricingSecondPortEntryIncoterm1FieldNameToRowMap.get(ExcelConstants.LogisticPricingFields.WAREHOUSING_FEE_MONTHLY.getFieldName()), col, ExcelConstants.LogisticPricingFields.WAREHOUSING_FEE_MONTHLY.getFieldName(), ExcelConstants.DOLLER_REGEX, ExcelConstants.ResponseRfpExcelSheetNames.LOGISTIC_PRICING.getSheetName(), ExcelConstants.CURRENCY_DOLLAR, Boolean.FALSE, Boolean.TRUE);
            validateDataFormatLogisticSheet(logisticSheet, logisticPricingSecondPortEntryIncoterm1FieldNameToRowMap.get(ExcelConstants.LogisticPricingFields.IMPORT_CUSTOMS_TARIFF_FEE.getFieldName()), col, ExcelConstants.LogisticPricingFields.IMPORT_CUSTOMS_TARIFF_FEE.getFieldName(), ExcelConstants.DOLLER_REGEX, ExcelConstants.ResponseRfpExcelSheetNames.LOGISTIC_PRICING.getSheetName(), ExcelConstants.CURRENCY_DOLLAR, Boolean.FALSE, Boolean.TRUE);

            // Second US Port Incoterms 2
            validateDataFormatLogisticSheet(logisticSheet, logisticPricingSecondPortEntryIncoterm2FieldNameToRowMap.get(ExcelConstants.LogisticPricingFields.STEVEDORING_COST.getFieldName()), col, ExcelConstants.LogisticPricingFields.STEVEDORING_COST.getFieldName(), ExcelConstants.DOLLER_REGEX, ExcelConstants.ResponseRfpExcelSheetNames.LOGISTIC_PRICING.getSheetName(), ExcelConstants.CURRENCY_DOLLAR, Boolean.FALSE, Boolean.TRUE);
            validateDataFormatLogisticSheet(logisticSheet, logisticPricingSecondPortEntryIncoterm2FieldNameToRowMap.get(ExcelConstants.LogisticPricingFields.HANDLING_COST.getFieldName()), col, ExcelConstants.LogisticPricingFields.HANDLING_COST.getFieldName(), ExcelConstants.DOLLER_REGEX, ExcelConstants.ResponseRfpExcelSheetNames.LOGISTIC_PRICING.getSheetName(), ExcelConstants.CURRENCY_DOLLAR, Boolean.FALSE, Boolean.TRUE);
            validateDataFormatLogisticSheet(logisticSheet, logisticPricingSecondPortEntryIncoterm2FieldNameToRowMap.get(ExcelConstants.LogisticPricingFields.WHARFAGE_COST.getFieldName()), col, ExcelConstants.LogisticPricingFields.WHARFAGE_COST.getFieldName(), ExcelConstants.DOLLER_REGEX, ExcelConstants.ResponseRfpExcelSheetNames.LOGISTIC_PRICING.getSheetName(), ExcelConstants.CURRENCY_DOLLAR, Boolean.FALSE, Boolean.TRUE);
            validateDataFormatLogisticSheet(logisticSheet, logisticPricingSecondPortEntryIncoterm2FieldNameToRowMap.get(ExcelConstants.LogisticPricingFields.SECURITY_COST.getFieldName()), col, ExcelConstants.LogisticPricingFields.SECURITY_COST.getFieldName(), ExcelConstants.DOLLER_REGEX, ExcelConstants.ResponseRfpExcelSheetNames.LOGISTIC_PRICING.getSheetName(), ExcelConstants.CURRENCY_DOLLAR, Boolean.FALSE, Boolean.TRUE);
            validateDataFormatLogisticSheet(logisticSheet, logisticPricingSecondPortEntryIncoterm2FieldNameToRowMap.get(ExcelConstants.LogisticPricingFields.WAREHOUSING_FEE_MONTHLY.getFieldName()), col, ExcelConstants.LogisticPricingFields.WAREHOUSING_FEE_MONTHLY.getFieldName(), ExcelConstants.DOLLER_REGEX, ExcelConstants.ResponseRfpExcelSheetNames.LOGISTIC_PRICING.getSheetName(), ExcelConstants.CURRENCY_DOLLAR, Boolean.FALSE, Boolean.TRUE);
            validateDataFormatLogisticSheet(logisticSheet, logisticPricingSecondPortEntryIncoterm2FieldNameToRowMap.get(ExcelConstants.LogisticPricingFields.IMPORT_CUSTOMS_TARIFF_FEE.getFieldName()), col, ExcelConstants.LogisticPricingFields.IMPORT_CUSTOMS_TARIFF_FEE.getFieldName(), ExcelConstants.DOLLER_REGEX, ExcelConstants.ResponseRfpExcelSheetNames.LOGISTIC_PRICING.getSheetName(), ExcelConstants.CURRENCY_DOLLAR, Boolean.FALSE, Boolean.TRUE);

        }


    }

    private static boolean validateDataFormatLogisticSheet(Sheet logisticSheet, Integer row, Integer col, String headerName, String regex, String sheetName, String regexType, Boolean isMandatory, Boolean NA) {

        Cell cell = logisticSheet.getRow(row).getCell(col);

        if (Boolean.FALSE.equals(Utils.checkIfStringIsNullOrEmpty(df.formatCellValue(cell).trim()))) {
            String value = df.formatCellValue(cell);

            if (NA == Boolean.TRUE && (Objects.equals(Utils.getTrimmedNumber(value).toLowerCase(), ExcelConstants.NA.toLowerCase()))) {
                return true;
            }

            if (regex.equals(ExcelConstants.NUMBER_REGEX) || regex.equals(ExcelConstants.DOLLER_REGEX) || regex.equals(ExcelConstants.MIN_MAX_REGEX_NUMERIC) || regex.equals(ExcelConstants.MIN_MAX_REGEX_DOLLER) || regex.equals(ExcelConstants.PERCENT_OR_DOLLAR_REGEX) || regex.equals(ExcelConstants.MIN_MAX_REGEX_DOLLER_OR_NUMBER_WITH_DOLLER)) {
                value = Utils.getTrimmedNumber(value);
            }
            if (regex.equals(ExcelConstants.PERCENTAGE_REGEX)) {
                value = value.replaceAll(Constants.SPACE, Constants.EMPTY_STRING);
            }
            if (regex.equals(ExcelConstants.DOLLER_REGEX)) {
                value = value.replace(Constants.HYPHEN, Constants.EMPTY_STRING);
            }
            if (validateRegex(value, regex)) {
                return true;
            } else {
                String errorMessage = MessageFormat.format(ErrorMessages.Messages.DATA_TYPE_MISMATCH.getMessage(), regexType, df.formatCellValue(cell));
                errorMessageDetailsList.add(new ErrorMessageDetails(logisticSheet.getSheetName(), new CellReference(row, col).formatAsString(false), regexType, df.formatCellValue(cell), headerName, errorMessage));
            }
        }
//        } else if (Boolean.TRUE.equals(isMandatory) && cell != null) {
//            String errorMessage = MessageFormat.format(ErrorMessages.Messages.MANDATORY_FIELD_MISSING_ERROR.getMessage(), headerName);
//            errorMessageDetailsList.add(new ErrorMessageDetails(logisticSheet.getSheetName(), new CellReference(cell).formatAsString(false), ExcelConstants.ExpectedValuesList.MANDATORY.getValue(), df.formatCellValue(cell), headerName, errorMessage));
//        }
        return false;

    }


    private static void conditionValidationLogisticSheet(int col, Sheet logisticSheet, String gpMill) {
        validateWarehouseAddressLogistic(col, logisticSheet, logisticPricingFirstPortEntryFieldNameToRowMap);
        validateInlandFreightSourceNameForNewTemplate(logisticSheet, col, logisticPricingFirstPortEntryFieldNameToRowMap);
        validatePortEntrySourceNameForNewTemplate(logisticSheet, col, logisticPricingFirstPortEntryFieldNameToRowMap);
        validateDestinationAddressForNewTemplate(logisticSheet, col, logisticPricingFirstPortEntryFieldNameToRowMap, logisticPricingFirstPortEntryFieldNameToRowMismatchMap, gpMill);
        validateCountryAndUsPortOfEntryForNewTemplate(logisticSheet, col, logisticPricingFieldNameToRowMap, logisticPricingFirstPortEntryFieldNameToRowMap);
        validateDomesticPortEntryDetails(logisticSheet, col, logisticPricingFirstPortEntryFieldNameToRowMap);

        Row secondPortEntryRow = logisticSheet.getRow(ExcelConstants.SECOND_US_PORT_OF_ENTRY_ROW);
        if (df.formatCellValue(secondPortEntryRow.getCell(col)) != null && !df.formatCellValue(secondPortEntryRow.getCell(col)).isEmpty()) {

            validateWarehouseAddressLogistic(col, logisticSheet, logisticPricingSecondPortEntryFieldNameToRowMap);
            validateInlandFreightSourceNameForNewTemplate(logisticSheet, col, logisticPricingSecondPortEntryFieldNameToRowMap);
            validatePortEntrySourceNameForNewTemplate(logisticSheet, col, logisticPricingSecondPortEntryFieldNameToRowMap);
            validateDestinationAddressForNewTemplate(logisticSheet, col, logisticPricingSecondPortEntryFieldNameToRowMap, logisticPricingSecondPortEntryFieldNameToRowMismatchMap, gpMill);
            validateCountryAndUsPortOfEntryForNewTemplate(logisticSheet, col, logisticPricingFieldNameToRowMap, logisticPricingSecondPortEntryFieldNameToRowMap);
            validateDomesticPortEntryDetails(logisticSheet, col, logisticPricingSecondPortEntryFieldNameToRowMap);
        }
    }

    private static void validateWarehouseAddressLogistic(int col, Sheet logisticSheet, Map<String, Integer> rowMap) {
        String errorMessage;

        if (Objects.equals(df.formatCellValue(logisticSheet.getRow(logisticPricingFirstPortEntryFieldNameToRowMap.get(ExcelConstants.LogisticPricingFields.SAFETY_STOCK_LOCATION_TYPE.getFieldName())).getCell(col)).trim()
                , ExcelConstants.InlandTransitOriginType.WAREHOUSE.getValue())) {
            Cell addressCell = logisticSheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.ADDRESS.getFieldName())).getCell(col);

            //Checking for Empty Address
            if (addressCell == null || df.formatCellValue(addressCell) == null || df.formatCellValue(addressCell).trim().isEmpty()) {
                errorMessage = MessageFormat.format(ErrorMessages.Messages.CONDITIONAL_MANDATORY_FIELD_ERROR.getMessage(), ResponseRfpExcelHeaders.ADDRESS.getValue(), ResponseRfpExcelHeaders.SAFETY_STOCK_LOCATION_TYPE.getValue(), ExcelConstants.InlandTransitOriginType.WAREHOUSE.getValue());
                errorMessageDetailsList.add(new ErrorMessageDetails(logisticSheet.getSheetName(), new CellReference(addressCell).formatAsString(false), ExcelConstants.ExpectedValuesList.MANDATORY.getValue(), df.formatCellValue(addressCell), ResponseRfpExcelHeaders.ADDRESS.getValue(), errorMessage));
            } else {
                validateAddress(addressCell, logisticSheet.getSheetName());
            }
        }
    }

    private static PortEntryDetails populatePortEntryDetailsForNewTemplate(Sheet logisticSheet, int col, String firstPortEntry, String gpMillName, Map<String, Integer> rowMap) {

        PortEntryDetails portEntryDetails = new PortEntryDetails();
        portEntryDetails.setPort_entry(firstPortEntry);
        portEntryDetails.setCost_uom(Constants.COST_UOM);
        portEntryDetails.setIncoterms(new ArrayList<>());

        if (!Objects.equals(df.formatCellValue(logisticSheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.PORT_FREE_TIME_IN_DAYS.getFieldName())).getCell(col)), ExcelConstants.NA)) {
            portEntryDetails.setPort_free_time_in_days(Long.valueOf(Utils.getTrimmedNumber(getCellValueForNAFieldsForNewTemplate(logisticSheet, col, rowMap, ExcelConstants.LogisticPricingFields.PORT_FREE_TIME_IN_DAYS.getFieldName()))));
        }

        if (!Objects.equals(df.formatCellValue(logisticSheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.TRANSIT_LEAD_TIME_IN_DAYS.getFieldName())).getCell(col)), ExcelConstants.NA)) {
            portEntryDetails.setTransit_leadtime_in_days_origin_port_port_entry(Long.valueOf(Utils.getTrimmedNumber(getCellValueForNAFieldsForNewTemplate(logisticSheet, col, rowMap, ExcelConstants.LogisticPricingFields.TRANSIT_LEAD_TIME_IN_DAYS.getFieldName()))));
        }

        portEntryDetails.setSteamship_line(getCellValueForNAFieldsForNewTemplate(logisticSheet, col, rowMap, ExcelConstants.LogisticPricingFields.STEAMSHIP_LINE.getFieldName()));
        portEntryDetails.setOcean_freight(Utils.getFloatDoller(getCellValueForNAFieldsForNewTemplate(logisticSheet, col, rowMap, ExcelConstants.LogisticPricingFields.OCEAN_FREIGHT.getFieldName())));
        portEntryDetails.setSafety_stock_nominated_in_days(Long.valueOf(Utils.getTrimmedNumber(df.formatCellValue(logisticSheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.NOMINATED_SAFETY_STOCK.getFieldName())).getCell(col)))));

        portEntryDetails.setTransit_cost_from_port_entry_to_safety_stock_loc(Utils.getFloatDoller(getCellValueForNAFieldsForNewTemplate(logisticSheet, col, rowMap, ExcelConstants.LogisticPricingFields.TRANSIT_COST_US_PORT_TO_SAFETY_STOCK.getFieldName())));

        portEntryDetails.setGp_mill(gpMillName);
        SafetyStockLocation safetyStockLocation = new SafetyStockLocation();


        safetyStockLocation.setName(df.formatCellValue(logisticSheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.SAFETY_STOCK_LOCATION_NAME.getFieldName())).getCell(col)));
        safetyStockLocation.setType(df.formatCellValue(logisticSheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.SAFETY_STOCK_LOCATION_TYPE.getFieldName())).getCell(col)));


        safetyStockLocation.setLocation(getLocationFromAddressString(ExcelConstants.ResponseRfpExcelSheetNames.LOGISTIC_PRICING.getSheetName(), logisticSheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.ADDRESS.getFieldName())).getCell(col)));
        portEntryDetails.setSafety_stock_location(safetyStockLocation);
        //TODO Confirm when it will be GP
        portEntryDetails.setHandled_by(ExcelConstants.HANDLED_BY_SUPPLIER);
        return portEntryDetails;
    }

    public static String getCellValueForNAFieldsForNewTemplate(Sheet sheet, Integer col, Map<String, Integer> rowMap, String headerName) {
        if (Objects.equals(Utils.getTrimmedNumber(df.formatCellValue(sheet.getRow(rowMap.get(headerName)).getCell(col))).toLowerCase(), ExcelConstants.NA.toLowerCase())) {
            return null;
        } else {
            return df.formatCellValue(sheet.getRow(rowMap.get(headerName)).getCell(col));
        }
    }


    // methode to validate All FiledNames For commercialSheet
    private static void verifyAllFieldsNameForCommercialSheet(Sheet sheet) {

        verifyAllCommercialHeadersPresent(sheet, ExcelConstants.COMMERCIAL_PRICING_FIELDS_NAME_TO_CELLREFERENCE_MAP);
        verifyAllCommercialHeadersPresent(sheet, ExcelConstants.INDEX_FIELD_TO_CELL_REFERENCE);
        verifyAllCommercialHeadersPresent(sheet, ExcelConstants.MOVEMENT_FIELD_TO_CELL_REFERENCE);
        verifyAllCommercialHeadersPresent(sheet, ExcelConstants.HYBRID_FIELD_TO_CELL_REFERENCE);
        verifyAllCommercialHeadersPresent(sheet, ExcelConstants.PRICE_TIER_1_FIELD_TO_CELL_REFERENCE);
        verifyAllCommercialHeadersPresent(sheet, ExcelConstants.PRICE_TIER_2_FIELD_TO_CELL_REFERENCE);
        verifyAllCommercialHeadersPresent(sheet, ExcelConstants.PRICE_TIER_3_FIELD_TO_CELL_REFERENCE);
        verifyAllCommercialHeadersPresent(sheet, ExcelConstants.VOLUME_BASED_REBATE_FIELD_TO_CELL_REFERENCE);


    }


    //Method to Populate Bid Details if Bid type is Lump Sum
    private static Map<String, BidQtyDetail> populateSupplierNameToBidDetailsForLumpSum(Sheet sheet, BidQtyDetail bidQtyDetail) {
        //Map of Supplier Name to Bid Details Map
        Map<String, BidQtyDetail> millNameToBidQtyDetails = new HashMap<>();
        BidQtyDetail bidQtyDetail1 = Utils.copyBidDetailsToBidQtyDetail(bidQtyDetail);
        BidQtyDetail bidQtyDetail2 = Utils.copyBidDetailsToBidQtyDetail(bidQtyDetail);
        
        // getting TotalBidVolume if it is LumpSum
        long bidVolumeForLumpSum = 0L;

        if(Boolean.TRUE.equals(isCellEmptyAndValidateRegex(new CellReference(ExcelConstants.COLUMN_NAME_TO_CELL_REFERENCE.get(ResponseRfpExcelHeaders.VOLUME_BID_FOR_LUMP_SUM.getValue())),sheet, ResponseRfpExcelHeaders.VOLUME_BID_FOR_LUMP_SUM.getValue(), Optional.of(ExcelConstants.NUMBER_REGEX),ExcelConstants.NUMBER))){
            bidVolumeForLumpSum = Long.parseLong(df.formatCellValue(getCellValueFromCellReference(new CellReference(ExcelConstants.COLUMN_NAME_TO_CELL_REFERENCE.get(ResponseRfpExcelHeaders.VOLUME_BID_FOR_LUMP_SUM.getValue())),sheet)).replace(Constants.COMMA,Constants.EMPTY_STRING));
        }

        //first need to store the supplierName and volume
        if (endRowOfMills != null) {

            //Getting the row
            Row row = sheet.getRow(endRowOfMills + ExcelConstants.MILL_SPECIFIC_TOTAL_TO_LUMP_SUM_MILL_DETAILS_ROW_DIFF);

            //For Supplier Mill 1
            populateSupplierLumpSumDetails(sheet,new CellReference(row.getCell(ExcelConstants.LUMP_SUM_HEADER_TO_COLUMN_NUMBER.get(ResponseRfpExcelHeaders.SUPPLIER_MILL_A.getValue()))),new CellReference(row.getCell( ExcelConstants.LUMP_SUM_HEADER_TO_COLUMN_NUMBER.get(ResponseRfpExcelHeaders.SUPPLIER_MILL_A_VOLUME.getValue()))),bidQtyDetail1,millNameToBidQtyDetails);

            //For Supplier Mill 2
            populateSupplierLumpSumDetails(sheet,new CellReference(row.getCell(ExcelConstants.LUMP_SUM_HEADER_TO_COLUMN_NUMBER.get(ResponseRfpExcelHeaders.SUPPLIER_MILL_B.getValue()))),new CellReference(row.getCell(ExcelConstants.LUMP_SUM_HEADER_TO_COLUMN_NUMBER.get(ResponseRfpExcelHeaders.SUPPLIER_MILL_B_VOLUME.getValue()))),bidQtyDetail2,millNameToBidQtyDetails);

            //setting Total BidVolume For LumpSum
            long totalBidVolume = bidQtyDetail1.getBid_vol() + bidQtyDetail2.getBid_vol();
            if (bidVolumeForLumpSum != 0 && totalBidVolume!=bidVolumeForLumpSum){
                String errorMessage = MessageFormat.format(ErrorMessages.Messages.BID_VOLUME_MISMATCHED_FOR_LUMP_SUM.getMessage(), totalBidVolume,bidVolumeForLumpSum);
                errorMessageDetailsList.add(new ErrorMessageDetails(sheet.getSheetName(),new CellReference(ExcelConstants.COLUMN_NAME_TO_CELL_REFERENCE.get(ResponseRfpExcelHeaders.VOLUME_BID_FOR_LUMP_SUM.getValue())).formatAsString(), ExcelConstants.ExpectedValuesList.MANDATORY.getValue(), null, ResponseRfpExcelHeaders.VOLUME_BID_FOR_LUMP_SUM.getValue(), errorMessage));
            }
        }
        return millNameToBidQtyDetails;

    }

    //Method to populate Supplier Lump Sum Details
    private static void populateSupplierLumpSumDetails(Sheet sheet, CellReference supplierMillNameCellRef, CellReference supplierMillVolumeCellRef, BidQtyDetail bidQtyDetail, Map<String, BidQtyDetail> millNameToBidQtyDetails) {
        String supplierMillName = null;
        Long supplierVolume = 0L;
        //add for supplierMillName
        if (checkIsCellEmptyAndValidateRegex(supplierMillNameCellRef, sheet, Optional.empty())) {
            supplierMillName = df.formatCellValue(getCellValueFromCellReference(supplierMillNameCellRef, sheet));
        }
        //Volume
        if (checkIsCellEmptyAndValidateRegex(supplierMillVolumeCellRef, sheet, Optional.of(ExcelConstants.NUMERIC_CHARACTERS_REGEX))) {
            supplierVolume = (Math.round(Double.parseDouble(df.formatCellValue(getCellValueFromCellReference(supplierMillVolumeCellRef, sheet)).replaceAll(Constants.COMMA, Constants.EMPTY_STRING))));
        }
        if (supplierMillName != null && !ExcelConstants.ZERO.equalsIgnoreCase(supplierMillName) && supplierVolume != Constants.ZERO) {
            bidQtyDetail.setBid_vol(supplierVolume);
            bidQtyDetail.setMill_spec_bid(new ArrayList<>());
            millNameToBidQtyDetails.put(supplierMillName, bidQtyDetail);
        }

    }


    // Methode to Validate Headers For Logistic Sheet Of new Template
    private static void validateHeadersForLogisticSheet(Sheet logisticSheet) {

        verifyAllCommercialHeadersPresent(logisticSheet, logisticPricingHeaderToCellRefrenceMap);
        verifyAllCommercialHeadersPresent(logisticSheet, logisticPricingHeaderToCellRefrenceMisMatchMap);

        //Validate Fields
        verifyLogisticFields(logisticSheet, logisticPricingFieldNameToRowMap);

        verifyLogisticFields(logisticSheet, logisticPricingFirstPortEntryFieldNameToRowMap);
        verifyLogisticFields(logisticSheet, logisticPricingFirstPortEntryFieldNameToRowMismatchMap);
        verifyLogisticFields(logisticSheet, logisticPricingFirstPortEntryIncoterm1FieldNameToRowMap);
        verifyLogisticFields(logisticSheet, logisticPricingFirstPortEntryIncoterm2FieldNameToRowMap);

        verifyLogisticFields(logisticSheet, logisticPricingSecondPortEntryFieldNameToRowMap);
        verifyLogisticFields(logisticSheet, logisticPricingSecondPortEntryFieldNameToRowMismatchMap);
        verifyLogisticFields(logisticSheet, logisticPricingSecondPortEntryIncoterm1FieldNameToRowMap);
        verifyLogisticFields(logisticSheet, logisticPricingSecondPortEntryIncoterm2FieldNameToRowMap);


    }


    // validatePortEntry to Port Entry Map Details
    private static void validatePortEntryToPortEntryMapDetailsForNewTemplate(String supplierMill, String portEntry, int col, Map<String, Integer> rowMap, Sheet sheet, Map<String, Map<String, PortEntryDetails>> portEntryAndDetailsMap) {

        if(Objects.equals(portEntry, ExcelConstants.USPortOfEntry.DOMESTIC.getValue())){
            return;
        }

        PortEntryDetails portEntryDetails = portEntryAndDetailsMap.get(supplierMill).get(portEntry);

        //TODO :: move to constant
        if (Objects.equals(Utils.getTrimmedNumber(df.formatCellValue(sheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.PORT_FREE_TIME_IN_DAYS.getFieldName())).getCell(col))), ExcelConstants.NA)) {
            if (portEntryDetails.getPort_free_time_in_days() != null && portEntryDetails.getPort_free_time_in_days() != 0L) {
                String errorMessage = MessageFormat.format(ErrorMessages.Messages.REPEATED_SUPPLIER_MILL_PORT_ENTRY_HEADER_ERROR.getMessage(), ExcelConstants.LogisticPricingFields.PORT_FREE_TIME_IN_DAYS.getFieldName());
                errorMessageDetailsList.add(new ErrorMessageDetails(sheet.getSheetName(), new CellReference(sheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.PORT_FREE_TIME_IN_DAYS.getFieldName())).getCell(col)).formatAsString(false), portEntryDetails.getPort_free_time_in_days().toString(), Utils.getTrimmedNumber(df.formatCellValue(sheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.PORT_FREE_TIME_IN_DAYS.getFieldName())).getCell(col))), ExcelConstants.LogisticPricingFields.PORT_FREE_TIME_IN_DAYS.getFieldName(), errorMessage));
            }
        } else {
            if (!Objects.equals(portEntryDetails.getPort_free_time_in_days(), Long.valueOf(Utils.getTrimmedNumber(df.formatCellValue(sheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.PORT_FREE_TIME_IN_DAYS.getFieldName())).getCell(col)))))) {
                String errorMessage = MessageFormat.format(ErrorMessages.Messages.REPEATED_SUPPLIER_MILL_PORT_ENTRY_HEADER_ERROR.getMessage(), ExcelConstants.LogisticPricingFields.PORT_FREE_TIME_IN_DAYS.getFieldName());
                errorMessageDetailsList.add(new ErrorMessageDetails(sheet.getSheetName(), new CellReference(sheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.PORT_FREE_TIME_IN_DAYS.getFieldName())).getCell(col)).formatAsString(false), (portEntryDetails.getPort_free_time_in_days() != null) ? portEntryDetails.getPort_free_time_in_days().toString() : ExcelConstants.NA, Utils.getTrimmedNumber(df.formatCellValue(sheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.PORT_FREE_TIME_IN_DAYS.getFieldName())).getCell(col))), ExcelConstants.LogisticPricingFields.PORT_FREE_TIME_IN_DAYS.getFieldName(), errorMessage));
            }
        }

        if (Objects.equals(Utils.getTrimmedNumber(df.formatCellValue(sheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.TRANSIT_LEAD_TIME_IN_DAYS.getFieldName())).getCell(col))), ExcelConstants.NA)) {
            if (portEntryDetails.getTransit_leadtime_in_days_origin_port_port_entry() != null && portEntryDetails.getTransit_leadtime_in_days_origin_port_port_entry() != 0L) {
                String errorMessage = MessageFormat.format(ErrorMessages.Messages.REPEATED_SUPPLIER_MILL_PORT_ENTRY_HEADER_ERROR.getMessage(), ExcelConstants.LogisticPricingFields.TRANSIT_LEAD_TIME_IN_DAYS.getFieldName());
                errorMessageDetailsList.add(new ErrorMessageDetails(sheet.getSheetName(), new CellReference(sheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.TRANSIT_LEAD_TIME_IN_DAYS.getFieldName())).getCell(col)).formatAsString(false), portEntryDetails.getTransit_leadtime_in_days_origin_port_port_entry().toString(), Utils.getTrimmedNumber(df.formatCellValue(sheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.TRANSIT_LEAD_TIME_IN_DAYS.getFieldName())).getCell(col))), ExcelConstants.LogisticPricingFields.TRANSIT_LEAD_TIME_IN_DAYS.getFieldName(), errorMessage));
            }
        } else {
            if (!Objects.equals(portEntryDetails.getTransit_leadtime_in_days_origin_port_port_entry(), Long.valueOf(Utils.getTrimmedNumber(df.formatCellValue(sheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.TRANSIT_LEAD_TIME_IN_DAYS.getFieldName())).getCell(col)))))) {
                String errorMessage = MessageFormat.format(ErrorMessages.Messages.REPEATED_SUPPLIER_MILL_PORT_ENTRY_HEADER_ERROR.getMessage(), ExcelConstants.LogisticPricingFields.TRANSIT_LEAD_TIME_IN_DAYS.getFieldName());
                errorMessageDetailsList.add(new ErrorMessageDetails(sheet.getSheetName(), new CellReference(sheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.TRANSIT_LEAD_TIME_IN_DAYS.getFieldName())).getCell(col)).formatAsString(false), (portEntryDetails.getTransit_leadtime_in_days_origin_port_port_entry() != null) ? portEntryDetails.getTransit_leadtime_in_days_origin_port_port_entry().toString() : ExcelConstants.NA, Utils.getTrimmedNumber(df.formatCellValue(sheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.TRANSIT_LEAD_TIME_IN_DAYS.getFieldName())).getCell(col))), ExcelConstants.LogisticPricingFields.TRANSIT_LEAD_TIME_IN_DAYS.getFieldName(), errorMessage));
            }
        }

        if (Objects.equals(Utils.getTrimmedNumber(df.formatCellValue(sheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.STEAMSHIP_LINE.getFieldName())).getCell(col))), ExcelConstants.NA)) {
            if (portEntryDetails.getSteamship_line() != null && portEntryDetails.getSteamship_line() != Constants.EMPTY_STRING) {
                String errorMessage = MessageFormat.format(ErrorMessages.Messages.REPEATED_SUPPLIER_MILL_PORT_ENTRY_HEADER_ERROR.getMessage(), ExcelConstants.LogisticPricingFields.STEAMSHIP_LINE.getFieldName());
                errorMessageDetailsList.add(new ErrorMessageDetails(sheet.getSheetName(), new CellReference(sheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.STEAMSHIP_LINE.getFieldName())).getCell(col)).formatAsString(false), portEntryDetails.getSteamship_line(), df.formatCellValue(sheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.STEAMSHIP_LINE.getFieldName())).getCell(col)), ExcelConstants.LogisticPricingFields.STEAMSHIP_LINE.getFieldName(), errorMessage));
            }
        } else {
            if (!Objects.equals(portEntryDetails.getSteamship_line(), df.formatCellValue(sheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.STEAMSHIP_LINE.getFieldName())).getCell(col)))) {
                String errorMessage = MessageFormat.format(ErrorMessages.Messages.REPEATED_SUPPLIER_MILL_PORT_ENTRY_HEADER_ERROR.getMessage(), ExcelConstants.LogisticPricingFields.STEAMSHIP_LINE.getFieldName());
                errorMessageDetailsList.add(new ErrorMessageDetails(sheet.getSheetName(), new CellReference(sheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.STEAMSHIP_LINE.getFieldName())).getCell(col)).formatAsString(false), (portEntryDetails.getSteamship_line() != null) ? portEntryDetails.getSteamship_line() : ExcelConstants.NA, df.formatCellValue(sheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.STEAMSHIP_LINE.getFieldName())).getCell(col)), ExcelConstants.LogisticPricingFields.STEAMSHIP_LINE.getFieldName(), errorMessage));
            }
        }

        if (Objects.equals(Utils.getTrimmedNumber(df.formatCellValue(sheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.OCEAN_FREIGHT.getFieldName())).getCell(col))), ExcelConstants.NA)) {
            if (portEntryDetails.getOcean_freight() != null && portEntryDetails.getOcean_freight() != 0f) {
                String errorMessage = MessageFormat.format(ErrorMessages.Messages.REPEATED_SUPPLIER_MILL_PORT_ENTRY_HEADER_ERROR.getMessage(), ExcelConstants.LogisticPricingFields.OCEAN_FREIGHT.getFieldName());
                errorMessageDetailsList.add(new ErrorMessageDetails(sheet.getSheetName(), new CellReference(sheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.OCEAN_FREIGHT.getFieldName())).getCell(col)).formatAsString(false), portEntryDetails.getOcean_freight().toString(), Utils.getTrimmedNumber(df.formatCellValue(sheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.OCEAN_FREIGHT.getFieldName())).getCell(col))), ExcelConstants.LogisticPricingFields.OCEAN_FREIGHT.getFieldName(), errorMessage));
            }
        } else {
            if (!Objects.equals(portEntryDetails.getOcean_freight(), Utils.getFloatDoller(df.formatCellValue(sheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.OCEAN_FREIGHT.getFieldName())).getCell(col))))) {
                String errorMessage = MessageFormat.format(ErrorMessages.Messages.REPEATED_SUPPLIER_MILL_PORT_ENTRY_HEADER_ERROR.getMessage(), ExcelConstants.LogisticPricingFields.OCEAN_FREIGHT.getFieldName());
                errorMessageDetailsList.add(new ErrorMessageDetails(sheet.getSheetName(), new CellReference(sheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.OCEAN_FREIGHT.getFieldName())).getCell(col)).formatAsString(false), (portEntryDetails.getOcean_freight() != null) ? portEntryDetails.getOcean_freight().toString() : ExcelConstants.NA, Utils.getTrimmedNumber(df.formatCellValue(sheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.OCEAN_FREIGHT.getFieldName())).getCell(col))), ExcelConstants.LogisticPricingFields.OCEAN_FREIGHT.getFieldName(), errorMessage));
            }
        }

        if (!Objects.equals(portEntryDetails.getSafety_stock_nominated_in_days(), Long.valueOf(Utils.getTrimmedNumber(df.formatCellValue(sheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.NOMINATED_SAFETY_STOCK.getFieldName())).getCell(col)))))) {
            String errorMessage = MessageFormat.format(ErrorMessages.Messages.REPEATED_SUPPLIER_MILL_PORT_ENTRY_HEADER_ERROR.getMessage(), ExcelConstants.LogisticPricingFields.NOMINATED_SAFETY_STOCK.getFieldName());
            errorMessageDetailsList.add(new ErrorMessageDetails(sheet.getSheetName(), new CellReference(sheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.NOMINATED_SAFETY_STOCK.getFieldName())).getCell(col)).formatAsString(false), portEntryDetails.getSafety_stock_nominated_in_days().toString(), Utils.getTrimmedNumber(df.formatCellValue(sheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.NOMINATED_SAFETY_STOCK.getFieldName())).getCell(col))), ExcelConstants.LogisticPricingFields.NOMINATED_SAFETY_STOCK.getFieldName(), errorMessage));
        }


        if (!Objects.equals(portEntryDetails.getSafety_stock_location().getType(), df.formatCellValue(sheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.SAFETY_STOCK_LOCATION_TYPE.getFieldName())).getCell(col)).trim())) {
            String errorMessage = MessageFormat.format(ErrorMessages.Messages.REPEATED_SUPPLIER_MILL_PORT_ENTRY_HEADER_ERROR.getMessage(), ExcelConstants.LogisticPricingFields.SAFETY_STOCK_LOCATION_TYPE.getFieldName());
            errorMessageDetailsList.add(new ErrorMessageDetails(sheet.getSheetName(), new CellReference(sheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.SAFETY_STOCK_LOCATION_TYPE.getFieldName())).getCell(col)).formatAsString(false), portEntryDetails.getSafety_stock_location().getType(), df.formatCellValue(sheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.SAFETY_STOCK_LOCATION_TYPE.getFieldName())).getCell(col)), ExcelConstants.LogisticPricingFields.SAFETY_STOCK_LOCATION_TYPE.getFieldName(), errorMessage));
        }

        if (!Objects.equals(portEntryDetails.getSafety_stock_location().getName(), df.formatCellValue(sheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.SAFETY_STOCK_LOCATION_NAME.getFieldName())).getCell(col)))) {
            String errorMessage = MessageFormat.format(ErrorMessages.Messages.REPEATED_SUPPLIER_MILL_PORT_ENTRY_HEADER_ERROR.getMessage(), ExcelConstants.LogisticPricingFields.SAFETY_STOCK_LOCATION_NAME.getFieldName());
            errorMessageDetailsList.add(new ErrorMessageDetails(sheet.getSheetName(), new CellReference(sheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.SAFETY_STOCK_LOCATION_NAME.getFieldName())).getCell(col)).formatAsString(false), portEntryDetails.getSafety_stock_location().getName(), df.formatCellValue(sheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.SAFETY_STOCK_LOCATION_NAME.getFieldName())).getCell(col)), ExcelConstants.LogisticPricingFields.SAFETY_STOCK_LOCATION_NAME.getFieldName(), errorMessage));
        }

        if (Objects.equals(Utils.getTrimmedNumber(df.formatCellValue(sheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.OCEAN_FREIGHT.getFieldName())).getCell(col))), ExcelConstants.NA)) {
            if (portEntryDetails.getTransit_cost_from_port_entry_to_safety_stock_loc() != null && portEntryDetails.getTransit_cost_from_port_entry_to_safety_stock_loc() != 0f) {
                String errorMessage = MessageFormat.format(ErrorMessages.Messages.REPEATED_SUPPLIER_MILL_PORT_ENTRY_HEADER_ERROR.getMessage(), ExcelConstants.LogisticPricingFields.TRANSIT_COST_US_PORT_TO_SAFETY_STOCK.getFieldName());
                errorMessageDetailsList.add(new ErrorMessageDetails(sheet.getSheetName(), new CellReference(sheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.TRANSIT_COST_US_PORT_TO_SAFETY_STOCK.getFieldName())).getCell(col)).formatAsString(false), portEntryDetails.getTransit_cost_from_port_entry_to_safety_stock_loc().toString(), Utils.getTrimmedNumber(df.formatCellValue(sheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.TRANSIT_COST_US_PORT_TO_SAFETY_STOCK.getFieldName())).getCell(col))), ExcelConstants.LogisticPricingFields.TRANSIT_COST_US_PORT_TO_SAFETY_STOCK.getFieldName(), errorMessage));
            }
        } else {
            if (!Objects.equals(portEntryDetails.getTransit_cost_from_port_entry_to_safety_stock_loc(), Utils.getFloatDoller(df.formatCellValue(sheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.TRANSIT_COST_US_PORT_TO_SAFETY_STOCK.getFieldName())).getCell(col))))) {
                String errorMessage = MessageFormat.format(ErrorMessages.Messages.REPEATED_SUPPLIER_MILL_PORT_ENTRY_HEADER_ERROR.getMessage(), ExcelConstants.LogisticPricingFields.TRANSIT_COST_US_PORT_TO_SAFETY_STOCK.getFieldName());
                errorMessageDetailsList.add(new ErrorMessageDetails(sheet.getSheetName(), new CellReference(sheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.TRANSIT_COST_US_PORT_TO_SAFETY_STOCK.getFieldName())).getCell(col)).formatAsString(false), (portEntryDetails.getTransit_cost_from_port_entry_to_safety_stock_loc() != null) ? portEntryDetails.getTransit_cost_from_port_entry_to_safety_stock_loc().toString() : ExcelConstants.NA, Utils.getTrimmedNumber(df.formatCellValue(sheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.TRANSIT_COST_US_PORT_TO_SAFETY_STOCK.getFieldName())).getCell(col))), ExcelConstants.LogisticPricingFields.TRANSIT_COST_US_PORT_TO_SAFETY_STOCK.getFieldName(), errorMessage));
            }
        }

    }

    private static PortEntryHeaderFields populatePortEntryAndHeaderDetailsForNewTemplate(Sheet logisticSheet, int col, Map<String, Integer> rowMap, String portEntry, String gpMillName) {
        PortEntryHeaderFields portEntryHeaderFields = new PortEntryHeaderFields();

        portEntryHeaderFields.setOrigin_port(df.formatCellValue(logisticSheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.ORIGIN_PORT.getFieldName())).getCell(col)));
        portEntryHeaderFields.setOrigin_cntry(df.formatCellValue(logisticSheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.ORIGIN_COUNTRY.getFieldName())).getCell(col)));
        portEntryHeaderFields.setEnvironmental_certification(df.formatCellValue(logisticSheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.ENVIRONMENTAL_CERTIFICATION.getFieldName())).getCell(col)));
        portEntryHeaderFields.setBale_packaging(df.formatCellValue(logisticSheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.BALE_PACKAGING.getFieldName())).getCell(col)));
        portEntryHeaderFields.setBale_type(df.formatCellValue(logisticSheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.BALE_TYPE.getFieldName())).getCell(col)));

        return portEntryHeaderFields;
    }

    private static Incoterms populateIncotermsForNewTemplate(Sheet logisticSheet, int col, Map<String, Map<String, Incoterms>> usPortEntryToIncotermMap, String portEntry, Map<String, Integer> rowMap, String incotermHeader) {

        Incoterms incoterms = new Incoterms();
        incoterms.setIncoterm(df.formatCellValue(logisticSheet.getRow(rowMap.get(incotermHeader)).getCell(col)));
        incoterms.setCustoms_fee(Utils.getFloatDoller(getCellValueForNAFieldsForNewTemplate(logisticSheet, col, rowMap, ExcelConstants.LogisticPricingFields.IMPORT_CUSTOMS_TARIFF_FEE.getFieldName())));

        incoterms.setHandling_cost(Utils.getFloatDoller(getCellValueForNAFieldsForNewTemplate(logisticSheet, col, rowMap, ExcelConstants.LogisticPricingFields.HANDLING_COST.getFieldName())));
        incoterms.setSecurity_cost(Utils.getFloatDoller(getCellValueForNAFieldsForNewTemplate(logisticSheet, col, rowMap, ExcelConstants.LogisticPricingFields.SECURITY_COST.getFieldName())));
        incoterms.setStevedoring_cost(Utils.getFloatDoller(getCellValueForNAFieldsForNewTemplate(logisticSheet, col, rowMap, ExcelConstants.LogisticPricingFields.STEVEDORING_COST.getFieldName())));
        incoterms.setWharfage_cost(Utils.getFloatDoller(getCellValueForNAFieldsForNewTemplate(logisticSheet, col, rowMap, ExcelConstants.LogisticPricingFields.WHARFAGE_COST.getFieldName())));
        incoterms.setWarehouse_cost_per_month(Utils.getFloatDoller(getCellValueForNAFieldsForNewTemplate(logisticSheet, col, rowMap, ExcelConstants.LogisticPricingFields.WAREHOUSING_FEE_MONTHLY.getFieldName())));

        return validateIncotermForNewTemplate(logisticSheet, incoterms, usPortEntryToIncotermMap, portEntry, col, rowMap);
    }

    private static InlandFreight populateInlandFreightDetailsForNewTemplate(Sheet logisticSheet, int col, String gpMillName, String portEntry, Map<String, Integer> rowMap, Map<String, Integer> rowMismatchMap) {

        InlandFreight inlandFreight = new InlandFreight();
        inlandFreight.setGp_mill(gpMillName);
        inlandFreight.setSource_type(df.formatCellValue(logisticSheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.INLAND_TRANSIT_ORIGIN_TYPE.getFieldName())).getCell(col)));
        inlandFreight.setSource_name(df.formatCellValue(logisticSheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.INLAND_TRANSIT_ORIGIN_NAME.getFieldName())).getCell(col)));
        inlandFreight.setInland_trans_route(df.formatCellValue(logisticSheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.INLAND_TRANSPORTATION_ROUTE.getFieldName())).getCell(col)));
        inlandFreight.setDest_type(df.formatCellValue(logisticSheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.INLAND_TRANSIT_DESTINATION_TYPE.getFieldName())).getCell(col)));
        inlandFreight.setDest_name(df.formatCellValue(logisticSheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.INLAND_TRANSIT_DESTINATION_NAME.getFieldName())).getCell(col)));

        inlandFreight.setDest_location(getLocationFromAddressString(ExcelConstants.ResponseRfpExcelSheetNames.LOGISTIC_PRICING.getSheetName(), logisticSheet.getRow(rowMismatchMap.get(ExcelConstants.LogisticPricingFields.ADDRESS.getFieldName())).getCell(col)));
        inlandFreight.setTransit_mode(df.formatCellValue(logisticSheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.TRANSIT_MODE.getFieldName())).getCell(col)));
        inlandFreight.setTransit_cost(Utils.getFloatDoller( getCellValueForNAFieldsForNewTemplate(logisticSheet, col, rowMap, ExcelConstants.LogisticPricingFields.TRANSIT_COST.getFieldName())));
        inlandFreight.setCost_uom(Constants.COST_UOM);
        inlandFreight.setTransit_leadtime_in_days_port_entry_gp_mill(Long.valueOf(Utils.getTrimmedNumber(df.formatCellValue(logisticSheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.TRANSIT_LEAD_TIME_TO_GP_MILL.getFieldName())).getCell(col)))));
        inlandFreight.setPort_entry(portEntry);
        return inlandFreight;
    }

    private static void validateRepeatedSupplierMetadataForNewTemplate(Sheet logisticSheet, int col, String supplierMillName, Map<String, SupplierMills> supplierMillsToSupplierMillsMetadataMap, Map<String, Integer> logisticPricingFieldNameToRowMap) {

        SupplierMills supplierMills = supplierMillsToSupplierMillsMetadataMap.get(supplierMillName);

        if (!Objects.equals(supplierMills.getOrigin_port(), df.formatCellValue(logisticSheet.getRow(logisticPricingFieldNameToRowMap.get(ExcelConstants.LogisticPricingFields.ORIGIN_PORT.getFieldName())).getCell(col)))) {
            String errorMessage = MessageFormat.format(ErrorMessages.Messages.REPEATED_SUPPLIER_METADATA_ERROR.getMessage(), ExcelConstants.LogisticPricingFields.ORIGIN_PORT.getFieldName());
            errorMessageDetailsList.add(new ErrorMessageDetails(logisticSheet.getSheetName(), new CellReference(logisticPricingFieldNameToRowMap.get(ExcelConstants.LogisticPricingFields.ORIGIN_PORT.getFieldName()), col).formatAsString(false), supplierMills.getOrigin_port(), df.formatCellValue(logisticSheet.getRow(logisticPricingFieldNameToRowMap.get(ExcelConstants.LogisticPricingFields.ORIGIN_PORT.getFieldName())).getCell(col)), ExcelConstants.LogisticPricingFields.ORIGIN_PORT.getFieldName(), errorMessage));
        }

        if (!Objects.equals(supplierMills.getOrigin_cntry(), df.formatCellValue(logisticSheet.getRow(logisticPricingFieldNameToRowMap.get(ExcelConstants.LogisticPricingFields.ORIGIN_COUNTRY.getFieldName())).getCell(col)))) {
            String errorMessage = MessageFormat.format(ErrorMessages.Messages.REPEATED_SUPPLIER_METADATA_ERROR.getMessage(), ExcelConstants.LogisticPricingFields.ORIGIN_COUNTRY.getFieldName());
            errorMessageDetailsList.add(new ErrorMessageDetails(logisticSheet.getSheetName(), new CellReference(logisticPricingFieldNameToRowMap.get(ExcelConstants.LogisticPricingFields.ORIGIN_COUNTRY.getFieldName()), col).formatAsString(false), supplierMills.getOrigin_cntry(), df.formatCellValue(logisticSheet.getRow(logisticPricingFieldNameToRowMap.get(ExcelConstants.LogisticPricingFields.ORIGIN_COUNTRY.getFieldName())).getCell(col)), ExcelConstants.LogisticPricingFields.ORIGIN_COUNTRY.getFieldName(), errorMessage));
        }

        if (!Objects.equals(supplierMills.getEnvironmental_certification(), df.formatCellValue(logisticSheet.getRow(logisticPricingFieldNameToRowMap.get(ExcelConstants.LogisticPricingFields.ENVIRONMENTAL_CERTIFICATION.getFieldName())).getCell(col)))) {
            String errorMessage = MessageFormat.format(ErrorMessages.Messages.REPEATED_SUPPLIER_METADATA_ERROR.getMessage(), ExcelConstants.LogisticPricingFields.ENVIRONMENTAL_CERTIFICATION.getFieldName());
            errorMessageDetailsList.add(new ErrorMessageDetails(logisticSheet.getSheetName(), new CellReference(logisticPricingFieldNameToRowMap.get(ExcelConstants.LogisticPricingFields.ENVIRONMENTAL_CERTIFICATION.getFieldName()), col).formatAsString(false), supplierMills.getEnvironmental_certification(), df.formatCellValue(logisticSheet.getRow(logisticPricingFieldNameToRowMap.get(ExcelConstants.LogisticPricingFields.ENVIRONMENTAL_CERTIFICATION.getFieldName())).getCell(col)), ExcelConstants.LogisticPricingFields.ENVIRONMENTAL_CERTIFICATION.getFieldName(), errorMessage));

        }

        if (!Objects.equals(supplierMills.getBale_packaging(), df.formatCellValue(logisticSheet.getRow(logisticPricingFieldNameToRowMap.get(ExcelConstants.LogisticPricingFields.BALE_PACKAGING.getFieldName())).getCell(col)))) {
            String errorMessage = MessageFormat.format(ErrorMessages.Messages.REPEATED_SUPPLIER_METADATA_ERROR.getMessage(), ExcelConstants.LogisticPricingFields.BALE_PACKAGING.getFieldName());
            errorMessageDetailsList.add(new ErrorMessageDetails(logisticSheet.getSheetName(), new CellReference(logisticPricingFieldNameToRowMap.get(ExcelConstants.LogisticPricingFields.BALE_PACKAGING.getFieldName()), col).formatAsString(false), supplierMills.getBale_packaging(), df.formatCellValue(logisticSheet.getRow(logisticPricingFieldNameToRowMap.get(ExcelConstants.LogisticPricingFields.BALE_PACKAGING.getFieldName())).getCell(col)), ExcelConstants.LogisticPricingFields.BALE_PACKAGING.getFieldName(), errorMessage));

        }

        if (!Objects.equals(supplierMills.getBale_type(), df.formatCellValue(logisticSheet.getRow(logisticPricingFieldNameToRowMap.get(ExcelConstants.LogisticPricingFields.BALE_TYPE.getFieldName())).getCell(col)))) {
            String errorMessage = MessageFormat.format(ErrorMessages.Messages.REPEATED_SUPPLIER_METADATA_ERROR.getMessage(), ExcelConstants.LogisticPricingFields.BALE_TYPE.getFieldName());
            errorMessageDetailsList.add(new ErrorMessageDetails(logisticSheet.getSheetName(), new CellReference(logisticPricingFieldNameToRowMap.get(ExcelConstants.LogisticPricingFields.BALE_TYPE.getFieldName()), col).formatAsString(false), supplierMills.getBale_type(), df.formatCellValue(logisticSheet.getRow(logisticPricingFieldNameToRowMap.get(ExcelConstants.LogisticPricingFields.BALE_TYPE.getFieldName())).getCell(col)), ExcelConstants.LogisticPricingFields.BALE_TYPE.getFieldName(), errorMessage));
        }
    }


    private static void validatePortEntryToPortEntryHeaderFieldsForNewTemplate(Sheet logisticSheet, int col, String portEntry, Map<String, Map<String, PortEntryHeaderFields>> portEntryAndHeaderFieldsMap, String supplierMillName, Map<String, Integer> rowMap) {
        PortEntryHeaderFields portEntryHeaderFields = portEntryAndHeaderFieldsMap.get(supplierMillName).get((portEntry));

        if (!Objects.equals(portEntryHeaderFields.getOrigin_port(), df.formatCellValue(logisticSheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.ORIGIN_PORT.getFieldName())).getCell(col)))) {
            String errorMessage = MessageFormat.format(ErrorMessages.Messages.REPEATED_SUPPLIER_MILL_PORT_ENTRY_HEADER_ERROR.getMessage(), ExcelConstants.LogisticPricingFields.ORIGIN_PORT.getFieldName());
            errorMessageDetailsList.add(new ErrorMessageDetails(logisticSheet.getSheetName(), new CellReference(rowMap.get(ExcelConstants.LogisticPricingFields.ORIGIN_PORT.getFieldName()), col).formatAsString(false), portEntryHeaderFields.getOrigin_port(), df.formatCellValue(logisticSheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.ORIGIN_PORT.getFieldName())).getCell(col)), ExcelConstants.ResponseRfpExcelHeaders.ORIGIN_PORT.getValue(), errorMessage));
        }

        if (!Objects.equals(portEntryHeaderFields.getOrigin_cntry(), df.formatCellValue(logisticSheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.ORIGIN_COUNTRY.getFieldName())).getCell(col)))) {
            String errorMessage = MessageFormat.format(ErrorMessages.Messages.REPEATED_SUPPLIER_MILL_PORT_ENTRY_HEADER_ERROR.getMessage(), ExcelConstants.LogisticPricingFields.ORIGIN_COUNTRY.getFieldName());
            errorMessageDetailsList.add(new ErrorMessageDetails(logisticSheet.getSheetName(), new CellReference(rowMap.get(ExcelConstants.LogisticPricingFields.ORIGIN_COUNTRY.getFieldName()), col).formatAsString(false), portEntryHeaderFields.getOrigin_port(), df.formatCellValue(logisticSheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.ORIGIN_COUNTRY.getFieldName())).getCell(col)), ExcelConstants.ResponseRfpExcelHeaders.ORIGIN_COUNTRY.getValue(), errorMessage));

        }

        if (!Objects.equals(portEntryHeaderFields.getEnvironmental_certification(), df.formatCellValue(logisticSheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.ENVIRONMENTAL_CERTIFICATION.getFieldName())).getCell(col)))) {
            String errorMessage = MessageFormat.format(ErrorMessages.Messages.REPEATED_SUPPLIER_MILL_PORT_ENTRY_HEADER_ERROR.getMessage(), ExcelConstants.LogisticPricingFields.ENVIRONMENTAL_CERTIFICATION.getFieldName());
            errorMessageDetailsList.add(new ErrorMessageDetails(logisticSheet.getSheetName(), new CellReference(rowMap.get(ExcelConstants.LogisticPricingFields.ENVIRONMENTAL_CERTIFICATION.getFieldName()), col).formatAsString(false), portEntryHeaderFields.getOrigin_port(), df.formatCellValue(logisticSheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.ENVIRONMENTAL_CERTIFICATION.getFieldName())).getCell(col)), ExcelConstants.ResponseRfpExcelHeaders.ENVIRONMENTAL_CERTIFICATION.getValue(), errorMessage));

        }

        if (!Objects.equals(portEntryHeaderFields.getBale_packaging(), df.formatCellValue(logisticSheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.BALE_PACKAGING.getFieldName())).getCell(col)))) {
            String errorMessage = MessageFormat.format(ErrorMessages.Messages.REPEATED_SUPPLIER_MILL_PORT_ENTRY_HEADER_ERROR.getMessage(), ExcelConstants.LogisticPricingFields.BALE_PACKAGING.getFieldName());
            errorMessageDetailsList.add(new ErrorMessageDetails(logisticSheet.getSheetName(), new CellReference(rowMap.get(ExcelConstants.LogisticPricingFields.BALE_PACKAGING.getFieldName()), col).formatAsString(false), portEntryHeaderFields.getOrigin_port(), df.formatCellValue(logisticSheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.BALE_PACKAGING.getFieldName())).getCell(col)), ExcelConstants.ResponseRfpExcelHeaders.BALE_PACKAGING.getValue(), errorMessage));
        }

        if (!Objects.equals(portEntryHeaderFields.getBale_type(), df.formatCellValue(logisticSheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.BALE_TYPE.getFieldName())).getCell(col)))) {
            String errorMessage = MessageFormat.format(ErrorMessages.Messages.REPEATED_SUPPLIER_MILL_PORT_ENTRY_HEADER_ERROR.getMessage(), ExcelConstants.LogisticPricingFields.BALE_TYPE.getFieldName());
            errorMessageDetailsList.add(new ErrorMessageDetails(logisticSheet.getSheetName(), new CellReference(rowMap.get(ExcelConstants.LogisticPricingFields.BALE_TYPE.getFieldName()), col).formatAsString(false), portEntryHeaderFields.getOrigin_port(), df.formatCellValue(logisticSheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.BALE_TYPE.getFieldName())).getCell(col)), ExcelConstants.ResponseRfpExcelHeaders.BALE_TYPE.getValue(), errorMessage));
        }
    }

    private static void validateRepeatedInlandFreightMetadataForNewTemplate(Sheet logisticSheet, int col, String supplierMillName, String portEntry, Map<String, Map<String, Map<String, InlandFreight>>> supplierMillPortEntryInlandDetailsMap, String gpMillName, Map<String, Integer> rowMap, Map<String, Integer> rowMapGpMill) {
        InlandFreight inlandFreight = supplierMillPortEntryInlandDetailsMap.get(supplierMillName).get(portEntry).get(gpMillName);

        if (inlandFreight.getGp_mill() != ExcelConstants.GP_MILL_LUMP_SUM) {
            if (!Objects.equals(inlandFreight.getGp_mill(), df.formatCellValue(logisticSheet.getRow(rowMapGpMill.get(ExcelConstants.LogisticPricingFields.GP_MILL.getFieldName())).getCell(col)))) {
                String errorMessage = MessageFormat.format(ErrorMessages.Messages.REPEATED_INLAND_FREIGHT_METADATA_ERROR.getMessage(), ExcelConstants.LogisticPricingFields.GP_MILL.getFieldName());
                errorMessageDetailsList.add(new ErrorMessageDetails(logisticSheet.getSheetName(), new CellReference(logisticSheet.getRow(rowMapGpMill.get(ExcelConstants.LogisticPricingFields.GP_MILL.getFieldName())).getCell(col)).formatAsString(false), inlandFreight.getGp_mill(), df.formatCellValue(logisticSheet.getRow(rowMapGpMill.get(ExcelConstants.LogisticPricingFields.GP_MILL.getFieldName())).getCell(col)), ExcelConstants.LogisticPricingFields.GP_MILL.getFieldName(), errorMessage));
            }
        }

        if (!Objects.equals(inlandFreight.getSource_type(), df.formatCellValue(logisticSheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.INLAND_TRANSIT_ORIGIN_TYPE.getFieldName())).getCell(col)))) {
            String errorMessage = MessageFormat.format(ErrorMessages.Messages.REPEATED_INLAND_FREIGHT_METADATA_ERROR.getMessage(), ExcelConstants.LogisticPricingFields.INLAND_TRANSIT_ORIGIN_TYPE.getFieldName());
            errorMessageDetailsList.add(new ErrorMessageDetails(logisticSheet.getSheetName(), new CellReference(logisticSheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.INLAND_TRANSIT_ORIGIN_TYPE.getFieldName())).getCell(col)).formatAsString(false), inlandFreight.getSource_type(), df.formatCellValue(logisticSheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.INLAND_TRANSIT_ORIGIN_TYPE.getFieldName())).getCell(col)), ExcelConstants.LogisticPricingFields.INLAND_TRANSIT_ORIGIN_TYPE.getFieldName(), errorMessage));
        }

        if (!Objects.equals(inlandFreight.getSource_name(), df.formatCellValue(logisticSheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.INLAND_TRANSIT_ORIGIN_NAME.getFieldName())).getCell(col)))) {
            String errorMessage = MessageFormat.format(ErrorMessages.Messages.REPEATED_INLAND_FREIGHT_METADATA_ERROR.getMessage(), ExcelConstants.LogisticPricingFields.INLAND_TRANSIT_ORIGIN_NAME.getFieldName());
            errorMessageDetailsList.add(new ErrorMessageDetails(logisticSheet.getSheetName(), new CellReference(logisticSheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.INLAND_TRANSIT_ORIGIN_NAME.getFieldName())).getCell(col)).formatAsString(false), inlandFreight.getSource_name(), df.formatCellValue(logisticSheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.INLAND_TRANSIT_ORIGIN_NAME.getFieldName())).getCell(col)), ExcelConstants.LogisticPricingFields.INLAND_TRANSIT_ORIGIN_NAME.getFieldName(), errorMessage));
        }

        if (!Objects.equals(inlandFreight.getInland_trans_route(), df.formatCellValue(logisticSheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.INLAND_TRANSPORTATION_ROUTE.getFieldName())).getCell(col)))) {
            String errorMessage = MessageFormat.format(ErrorMessages.Messages.REPEATED_INLAND_FREIGHT_METADATA_ERROR.getMessage(), ExcelConstants.LogisticPricingFields.INLAND_TRANSPORTATION_ROUTE.getFieldName());
            errorMessageDetailsList.add(new ErrorMessageDetails(logisticSheet.getSheetName(), new CellReference(logisticSheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.INLAND_TRANSPORTATION_ROUTE.getFieldName())).getCell(col)).formatAsString(false), inlandFreight.getInland_trans_route(), df.formatCellValue(logisticSheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.INLAND_TRANSPORTATION_ROUTE.getFieldName())).getCell(col)), ExcelConstants.LogisticPricingFields.INLAND_TRANSPORTATION_ROUTE.getFieldName(), errorMessage));
        }

        if (!Objects.equals(inlandFreight.getDest_type(), df.formatCellValue(logisticSheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.INLAND_TRANSIT_DESTINATION_TYPE.getFieldName())).getCell(col)))) {
            String errorMessage = MessageFormat.format(ErrorMessages.Messages.REPEATED_INLAND_FREIGHT_METADATA_ERROR.getMessage(), ExcelConstants.LogisticPricingFields.INLAND_TRANSIT_DESTINATION_TYPE.getFieldName());
            errorMessageDetailsList.add(new ErrorMessageDetails(logisticSheet.getSheetName(), new CellReference(logisticSheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.INLAND_TRANSIT_DESTINATION_TYPE.getFieldName())).getCell(col)).formatAsString(false), inlandFreight.getDest_type(), df.formatCellValue(logisticSheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.INLAND_TRANSIT_DESTINATION_TYPE.getFieldName())).getCell(col)), ExcelConstants.LogisticPricingFields.INLAND_TRANSIT_DESTINATION_TYPE.getFieldName(), errorMessage));
        }

        if (!Objects.equals(inlandFreight.getDest_name(), df.formatCellValue(logisticSheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.INLAND_TRANSIT_DESTINATION_NAME.getFieldName())).getCell(col)))) {
            String errorMessage = MessageFormat.format(ErrorMessages.Messages.REPEATED_INLAND_FREIGHT_METADATA_ERROR.getMessage(), ExcelConstants.LogisticPricingFields.INLAND_TRANSIT_DESTINATION_NAME.getFieldName());
            errorMessageDetailsList.add(new ErrorMessageDetails(logisticSheet.getSheetName(), new CellReference(logisticSheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.INLAND_TRANSIT_DESTINATION_NAME.getFieldName())).getCell(col)).formatAsString(false), inlandFreight.getDest_name(), df.formatCellValue(logisticSheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.INLAND_TRANSIT_DESTINATION_NAME.getFieldName())).getCell(col)), ExcelConstants.LogisticPricingFields.INLAND_TRANSIT_DESTINATION_NAME.getFieldName(), errorMessage));
        }


        if (!Objects.equals(inlandFreight.getTransit_mode(), df.formatCellValue(logisticSheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.TRANSIT_MODE.getFieldName())).getCell(col)))) {
            String errorMessage = MessageFormat.format(ErrorMessages.Messages.REPEATED_INLAND_FREIGHT_METADATA_ERROR.getMessage(), ExcelConstants.LogisticPricingFields.TRANSIT_MODE.getFieldName());
            errorMessageDetailsList.add(new ErrorMessageDetails(logisticSheet.getSheetName(), new CellReference(logisticSheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.TRANSIT_MODE.getFieldName())).getCell(col)).formatAsString(false), inlandFreight.getTransit_mode(), df.formatCellValue(logisticSheet.getRow(rowMapGpMill.get(ExcelConstants.LogisticPricingFields.TRANSIT_MODE.getFieldName())).getCell(col)), ExcelConstants.LogisticPricingFields.TRANSIT_MODE.getFieldName(), errorMessage));
        }

        if (!Objects.equals(inlandFreight.getTransit_cost(), Utils.getFloatDoller(df.formatCellValue(logisticSheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.TRANSIT_COST.getFieldName())).getCell(col))))) {
            String errorMessage = MessageFormat.format(ErrorMessages.Messages.REPEATED_INLAND_FREIGHT_METADATA_ERROR.getMessage(), ExcelConstants.LogisticPricingFields.TRANSIT_COST.getFieldName());
            errorMessageDetailsList.add(new ErrorMessageDetails(logisticSheet.getSheetName(), new CellReference(logisticSheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.TRANSIT_COST.getFieldName())).getCell(col)).formatAsString(false), inlandFreight.getTransit_cost().toString(), df.formatCellValue(logisticSheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.TRANSIT_COST.getFieldName())).getCell(col)), ExcelConstants.LogisticPricingFields.TRANSIT_COST.getFieldName(), errorMessage));
        }

        if (!Objects.equals(inlandFreight.getTransit_leadtime_in_days_port_entry_gp_mill(), Long.valueOf(Utils.getTrimmedNumber(df.formatCellValue(logisticSheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.TRANSIT_LEAD_TIME_TO_GP_MILL.getFieldName())).getCell(col)))))) {
            String errorMessage = MessageFormat.format(ErrorMessages.Messages.REPEATED_INLAND_FREIGHT_METADATA_ERROR.getMessage(), ExcelConstants.LogisticPricingFields.TRANSIT_LEAD_TIME_TO_GP_MILL.getFieldName());
            errorMessageDetailsList.add(new ErrorMessageDetails(logisticSheet.getSheetName(), new CellReference(logisticSheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.TRANSIT_LEAD_TIME_TO_GP_MILL.getFieldName())).getCell(col)).formatAsString(false), inlandFreight.getTransit_leadtime_in_days_port_entry_gp_mill().toString(), Utils.getTrimmedNumber(df.formatCellValue(logisticSheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.TRANSIT_LEAD_TIME_TO_GP_MILL.getFieldName())).getCell(col))), ExcelConstants.LogisticPricingFields.TRANSIT_LEAD_TIME_TO_GP_MILL.getFieldName(), errorMessage));
        }
    }

    private static void validateInlandFreightSourceNameForNewTemplate(Sheet logisticSheet, int col, Map<String, Integer> rowMap) {

        if (df.formatCellValue(logisticSheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.INLAND_TRANSIT_ORIGIN_NAME.getFieldName())).getCell(col)) == null || df.formatCellValue(logisticSheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.INLAND_TRANSIT_ORIGIN_NAME.getFieldName())).getCell(col)).trim().isEmpty()) {
            return;
        }

        if (df.formatCellValue(logisticSheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.US_PORT_OF_ENTRY.getFieldName())).getCell(col)) == null || df.formatCellValue(logisticSheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.US_PORT_OF_ENTRY.getFieldName())).getCell(col)).trim().isEmpty()) {
            return;
        }

        Cell inlandTransitOriginTypeCell = logisticSheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.INLAND_TRANSIT_ORIGIN_TYPE.getFieldName())).getCell(col);
        Cell inlandTransitOriginNameCell = logisticSheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.INLAND_TRANSIT_ORIGIN_NAME.getFieldName())).getCell(col);
        Cell usPortOfEntryCell = logisticSheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.US_PORT_OF_ENTRY.getFieldName())).getCell(col);

        if (inlandTransitOriginTypeCell != null && Objects.equals(df.formatCellValue(inlandTransitOriginTypeCell), ExcelConstants.InlandTransitOriginType.PORT_OF_ENTRY.getValue()) && (!Objects.equals(df.formatCellValue(usPortOfEntryCell), df.formatCellValue(inlandTransitOriginNameCell)))) {
            String errorMessage = MessageFormat.format(ErrorMessages.Messages.CONDITIONAL_FIELD_ERROR.getMessage(), ResponseRfpExcelHeaders.INLAND_TRANSIT_ORIGIN_TYPE.getValue(), ExcelConstants.InlandTransitOriginType.PORT_OF_ENTRY.getValue(), ResponseRfpExcelHeaders.INLAND_TRANSIT_ORIGIN_NAME.getValue(), df.formatCellValue(usPortOfEntryCell), df.formatCellValue(inlandTransitOriginNameCell));
            errorMessageDetailsList.add(new ErrorMessageDetails(logisticSheet.getSheetName(), new CellReference(inlandTransitOriginNameCell).formatAsString(false), df.formatCellValue(usPortOfEntryCell), df.formatCellValue(inlandTransitOriginNameCell), ResponseRfpExcelHeaders.INLAND_TRANSIT_ORIGIN_NAME.getValue(), errorMessage));
        }
    }

    private static void validatePortEntrySourceNameForNewTemplate(Sheet logisticSheet, int col, Map<String, Integer> rowMap) {

        Cell safetyStockLocationTypeCell = logisticSheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.SAFETY_STOCK_LOCATION_TYPE.getFieldName())).getCell(col);
        Cell safetyStockLocationNameCell = logisticSheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.SAFETY_STOCK_LOCATION_NAME.getFieldName())).getCell(col);
        Cell usPortOfEntryCell = logisticSheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.US_PORT_OF_ENTRY.getFieldName())).getCell(col);

        if (df.formatCellValue(safetyStockLocationNameCell) == null || df.formatCellValue(safetyStockLocationNameCell).trim().isEmpty()) {
            return;
        }

        if (df.formatCellValue(usPortOfEntryCell) == null || df.formatCellValue(usPortOfEntryCell).trim().isEmpty()) {
            return;
        }

        if (Objects.equals(df.formatCellValue(safetyStockLocationTypeCell).trim(), ExcelConstants.InlandTransitOriginType.PORT_OF_ENTRY.getValue()) && (!Objects.equals(df.formatCellValue(usPortOfEntryCell), df.formatCellValue(safetyStockLocationNameCell)))) {
            String errorMessage = MessageFormat.format(ErrorMessages.Messages.CONDITIONAL_FIELD_ERROR.getMessage(), ResponseRfpExcelHeaders.SAFETY_STOCK_LOCATION_TYPE.getValue(), ExcelConstants.InlandTransitOriginType.PORT_OF_ENTRY.getValue(), ResponseRfpExcelHeaders.SAFETY_STOCK_LOCATION_NAME.getValue(), df.formatCellValue(usPortOfEntryCell), df.formatCellValue(safetyStockLocationNameCell));
            errorMessageDetailsList.add(new ErrorMessageDetails(logisticSheet.getSheetName(), new CellReference(safetyStockLocationNameCell).formatAsString(false), df.formatCellValue(usPortOfEntryCell), df.formatCellValue(safetyStockLocationNameCell), ResponseRfpExcelHeaders.SAFETY_STOCK_LOCATION_NAME.getValue(), errorMessage));
        }
    }

    private static void validateDestinationAddressForNewTemplate(Sheet logisticSheet, int col, Map<String, Integer> rowMap, Map<String, Integer> rowMismatchMap, String gpMill) {

        String errorMessage;

        Cell inlandTransitDestTypeCell = logisticSheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.INLAND_TRANSIT_DESTINATION_TYPE.getFieldName())).getCell(col);
        Cell inlandTransitDestNameCell = logisticSheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.INLAND_TRANSIT_DESTINATION_NAME.getFieldName())).getCell(col);
        Cell gpMillCell = logisticSheet.getRow(logisticPricingFieldNameToRowMap.get(ExcelConstants.LogisticPricingFields.GP_MILL.getFieldName())).getCell(col);

        Cell addressCell = logisticSheet.getRow(rowMismatchMap.get(ExcelConstants.LogisticPricingFields.ADDRESS.getFieldName())).getCell(col);

        if (df.formatCellValue(inlandTransitDestTypeCell) == null || df.formatCellValue(inlandTransitDestTypeCell).trim().isEmpty()) {
            return;
        }

        if (!Objects.equals(df.formatCellValue(inlandTransitDestTypeCell), ExcelConstants.InlandTransitDestinationType.GP_MILL.getValue())) {
            String s = df.formatCellValue(addressCell);
            if (s == null || s.trim().isEmpty()) {
                errorMessage = MessageFormat.format(ErrorMessages.Messages.CONDITIONAL_MANDATORY_OTHER_THAN_FILED_ERROR.getMessage(), ResponseRfpExcelHeaders.ADDRESS.getValue(), ResponseRfpExcelHeaders.INLAND_TRANSIT_DESTINATION_TYPE.getValue(), ExcelConstants.InlandTransitDestinationType.GP_MILL.getValue());
                errorMessageDetailsList.add(new ErrorMessageDetails(logisticSheet.getSheetName(), new CellReference(addressCell).formatAsString(false), ExcelConstants.ExpectedValuesList.MANDATORY.getValue(), s, ResponseRfpExcelHeaders.ADDRESS.getValue(), errorMessage));
            } else {
                validateAddress(addressCell, logisticSheet.getSheetName());
            }
        }else{
            if(df.formatCellValue(gpMillCell) != null && !df.formatCellValue(gpMillCell).isEmpty()) {
                if (!Objects.equals(df.formatCellValue(inlandTransitDestNameCell), gpMill)){
                    errorMessage = MessageFormat.format(ErrorMessages.Messages.CONDITIONAL_FIELD_ERROR.getMessage(), ExcelConstants.LogisticPricingFields.INLAND_TRANSIT_DESTINATION_TYPE.getFieldName(), ExcelConstants.InlandTransitDestinationType.GP_MILL.getValue(), ExcelConstants.LogisticPricingFields.INLAND_TRANSIT_DESTINATION_NAME.getFieldName(), gpMill, df.formatCellValue(inlandTransitDestNameCell));
                    errorMessageDetailsList.add(new ErrorMessageDetails(logisticSheet.getSheetName(), new CellReference(inlandTransitDestNameCell).formatAsString(false), gpMill, df.formatCellValue(inlandTransitDestNameCell), ResponseRfpExcelHeaders.INLAND_TRANSIT_DESTINATION_NAME.getValue(), errorMessage));
                }
            }else {
                errorMessage = MessageFormat.format(ErrorMessages.Messages.CONDITIONAL_FIELD_OTHER_THAN_ERROR.getMessage(), ExcelConstants.BID_TYPE, Constants.BidType.LUMP_SUM.getValue(), ExcelConstants.LogisticPricingFields.INLAND_TRANSIT_ORIGIN_TYPE.getFieldName(), ExcelConstants.InlandTransitDestinationType.GP_MILL.getValue() , df.formatCellValue(inlandTransitDestTypeCell));
                errorMessageDetailsList.add(new ErrorMessageDetails(logisticSheet.getSheetName(), new CellReference(inlandTransitDestTypeCell).formatAsString(false), ExcelConstants.OTHER_THAN + Constants.SPACE + ExcelConstants.InlandTransitDestinationType.GP_MILL.getValue(), df.formatCellValue(inlandTransitDestTypeCell), ResponseRfpExcelHeaders.INLAND_TRANSIT_DESTINATION_TYPE.getValue(), errorMessage));
            }
        }
    }


    private static void validateCountryAndUsPortOfEntryForNewTemplate(Sheet logisticSheet, int col, Map<String, Integer> rowMap, Map<String, Integer> rowMapUsPortEntry) {

        Cell originPortCell = logisticSheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.ORIGIN_PORT.getFieldName())).getCell(col);
        Cell originCountryCell = logisticSheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.ORIGIN_COUNTRY.getFieldName())).getCell(col);
        Cell usPortOfEntryCell = logisticSheet.getRow(rowMapUsPortEntry.get(ExcelConstants.LogisticPricingFields.US_PORT_OF_ENTRY.getFieldName())).getCell(col);

        // IF Origin port is set as Domestic, Origin country and Us Port of entry should be Domestic
        if (Objects.equals(df.formatCellValue(originPortCell), ExcelConstants.OriginPort.DOMESTIC.getValue())) {

//            if (!Objects.equals(df.formatCellValue(originCountryCell), ExcelConstants.OriginCountry.USA.getValue())) {
//                String errorMessage = MessageFormat.format(ErrorMessages.Messages.CONDITIONAL_FIELD_ERROR.getMessage(), ResponseRfpExcelHeaders.ORIGIN_PORT.getValue(), ExcelConstants.OriginPort.DOMESTIC.getValue(), ResponseRfpExcelHeaders.ORIGIN_COUNTRY.getValue(), ExcelConstants.OriginCountry.USA.getValue(), df.formatCellValue(originCountryCell));
//                errorMessageDetailsList.add(new ErrorMessageDetails(logisticSheet.getSheetName(), new CellReference(originCountryCell).formatAsString(false), ExcelConstants.OriginCountry.USA.getValue(), df.formatCellValue(originCountryCell), ResponseRfpExcelHeaders.ORIGIN_COUNTRY.getValue(), errorMessage));
//            }

            if (!Objects.equals(df.formatCellValue(usPortOfEntryCell), ExcelConstants.OriginPort.DOMESTIC.getValue())) {
                String errorMessage = MessageFormat.format(ErrorMessages.Messages.CONDITIONAL_FIELD_ERROR.getMessage(), ResponseRfpExcelHeaders.ORIGIN_PORT.getValue(), ExcelConstants.OriginPort.DOMESTIC.getValue(), ResponseRfpExcelHeaders.US_PORT_OF_ENTRY.getValue(), ExcelConstants.OriginPort.DOMESTIC.getValue(), df.formatCellValue(usPortOfEntryCell));
                errorMessageDetailsList.add(new ErrorMessageDetails(logisticSheet.getSheetName(), new CellReference(usPortOfEntryCell).formatAsString(false), ExcelConstants.OriginPort.DOMESTIC.getValue(), df.formatCellValue(usPortOfEntryCell), ResponseRfpExcelHeaders.US_PORT_OF_ENTRY.getValue(), errorMessage));
            }
        }
    }

    private static Incoterms validateIncotermForNewTemplate(Sheet logisticSheet, Incoterms incoterms, Map<String, Map<String, Incoterms>> usPortEntryToIncotermMap, String portEntry, int col, Map<String, Integer> rowMap) {
        Map<String, Incoterms> incotermMap = new HashMap<>();

        String errorMessage;
        String foundValue = null;

        //If portEntry key is not there , add and return
        if (!usPortEntryToIncotermMap.containsKey(portEntry)) {
            incotermMap.put(incoterms.getIncoterm(), incoterms);
            usPortEntryToIncotermMap.put(portEntry, incotermMap);
            return incoterms;
        }
        //If portEntry key is  there , validate for same data
        incotermMap = usPortEntryToIncotermMap.get(portEntry);
        // check if the incoterm is same then incoterm details also be same
        if (incotermMap.containsKey(incoterms.getIncoterm())) {
            Incoterms incotermFromMap = incotermMap.get(incoterms.getIncoterm());

            if (!Objects.equals(incotermFromMap.getStevedoring_cost(), incoterms.getStevedoring_cost())) {
                CellReference cellReference = new CellReference(logisticSheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.STEVEDORING_COST.getFieldName())).getCell(col));
                errorMessage = MessageFormat.format(ErrorMessages.Messages.INVALID_INCOTERMS_ERROR.getMessage(), cellReference.formatAsString(false));
                foundValue = df.formatCellValue(getCellValueFromCellReference(cellReference, logisticSheet));
                errorMessageDetailsList.add(new ErrorMessageDetails(ExcelConstants.ResponseRfpExcelSheetNames.LOGISTIC_PRICING.getSheetName(), cellReference.formatAsString(false), (incotermFromMap.getStevedoring_cost() != null) ? incotermFromMap.getStevedoring_cost().toString() : ExcelConstants.NA, foundValue, ResponseRfpExcelHeaders.STEVEDORING_COST.getValue(), errorMessage));
            }
            if (!Objects.equals(incotermFromMap.getHandling_cost(), incoterms.getHandling_cost())) {
                CellReference cellReference = new CellReference(logisticSheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.HANDLING_COST.getFieldName())).getCell(col));
                errorMessage = MessageFormat.format(ErrorMessages.Messages.INVALID_INCOTERMS_ERROR.getMessage(), cellReference.formatAsString(false));
                foundValue = df.formatCellValue(getCellValueFromCellReference(cellReference, logisticSheet));
                errorMessageDetailsList.add(new ErrorMessageDetails(ExcelConstants.ResponseRfpExcelSheetNames.LOGISTIC_PRICING.getSheetName(), cellReference.formatAsString(false), (incotermFromMap.getHandling_cost() != null) ? incotermFromMap.getHandling_cost().toString() : ExcelConstants.NA, foundValue, ResponseRfpExcelHeaders.HANDLING_COST.getValue(), errorMessage));
            }
            if (!Objects.equals(incotermFromMap.getWharfage_cost(), incoterms.getWharfage_cost())) {
                CellReference cellReference = new CellReference(logisticSheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.WHARFAGE_COST.getFieldName())).getCell(col));
                errorMessage = MessageFormat.format(ErrorMessages.Messages.INVALID_INCOTERMS_ERROR.getMessage(), cellReference.formatAsString(false));
                foundValue = df.formatCellValue(getCellValueFromCellReference(cellReference, logisticSheet));
                errorMessageDetailsList.add(new ErrorMessageDetails(ExcelConstants.ResponseRfpExcelSheetNames.LOGISTIC_PRICING.getSheetName(), cellReference.formatAsString(false), (incotermFromMap.getWharfage_cost() != null) ? incotermFromMap.getWharfage_cost().toString() : ExcelConstants.NA, foundValue, ResponseRfpExcelHeaders.WHARFAGE_COST.getValue(), errorMessage));
            }
            if (!Objects.equals(incotermFromMap.getSecurity_cost(), incoterms.getSecurity_cost())) {
                CellReference cellReference = new CellReference(logisticSheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.SECURITY_COST.getFieldName())).getCell(col));
                errorMessage = MessageFormat.format(ErrorMessages.Messages.INVALID_INCOTERMS_ERROR.getMessage(), cellReference.formatAsString(false));
                foundValue = df.formatCellValue(getCellValueFromCellReference(cellReference, logisticSheet));
                errorMessageDetailsList.add(new ErrorMessageDetails(ExcelConstants.ResponseRfpExcelSheetNames.LOGISTIC_PRICING.getSheetName(), cellReference.formatAsString(false), foundValue, (incotermFromMap.getSecurity_cost() != null) ? incotermFromMap.getSecurity_cost().toString() : ExcelConstants.NA, ResponseRfpExcelHeaders.SECURITY_COST.getValue(), errorMessage));
            }
            if (!Objects.equals(incotermFromMap.getWarehouse_cost_per_month(), incoterms.getWarehouse_cost_per_month())) {
                CellReference cellReference = new CellReference(logisticSheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.WAREHOUSING_FEE_MONTHLY.getFieldName())).getCell(col));
                errorMessage = MessageFormat.format(ErrorMessages.Messages.INVALID_INCOTERMS_ERROR.getMessage(), cellReference.formatAsString(false));
                foundValue = df.formatCellValue(getCellValueFromCellReference(cellReference, logisticSheet));
                errorMessageDetailsList.add(new ErrorMessageDetails(ExcelConstants.ResponseRfpExcelSheetNames.LOGISTIC_PRICING.getSheetName(), cellReference.formatAsString(false), foundValue, (incotermFromMap.getWarehouse_cost_per_month() != null) ? incotermFromMap.getWarehouse_cost_per_month().toString() : ExcelConstants.NA, ResponseRfpExcelHeaders.WAREHOUSING_FEE_MONTHLY.getValue(), errorMessage));
            }
            if (!Objects.equals(incotermFromMap.getCustoms_fee(), incoterms.getCustoms_fee())) {
                CellReference cellReference = new CellReference(logisticSheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.IMPORT_CUSTOMS_TARIFF_FEE.getFieldName())).getCell(col));
                errorMessage = MessageFormat.format(ErrorMessages.Messages.INVALID_INCOTERMS_ERROR.getMessage(), cellReference.formatAsString(false));
                foundValue = df.formatCellValue(getCellValueFromCellReference(cellReference, logisticSheet));
                errorMessageDetailsList.add(new ErrorMessageDetails(ExcelConstants.ResponseRfpExcelSheetNames.LOGISTIC_PRICING.getSheetName(), cellReference.formatAsString(false), foundValue, (incotermFromMap.getCustoms_fee() != null) ? incotermFromMap.getCustoms_fee().toString() : ExcelConstants.NA, ResponseRfpExcelHeaders.IMPORT_CUSTOMS_TARIFF_FEE.getValue(), errorMessage));
            }
            return incoterms;
        } else {
            incotermMap.put(incoterms.getIncoterm(), incoterms);
            usPortEntryToIncotermMap.put(portEntry, incotermMap);
            return incoterms;
        }
    }

    private static void populateAndSetIncotermsForNewTemplate(Sheet logisticSheet, int col, String supplierMillName, Map<String, Map<String, Incoterms>> usPortEntryToIncotermMap, String portEntry, String gpMillName, Map<String, Map<String, Map<String, PortEntryDetails>>> supplierMillPortEntryAndDetailsMap, Map<String, Map<String,List<String>>> supplierMillAndPortEntryToIncotermListMap ,Map<String, Integer> incoterms1Map, Map<String, Integer> incoterms2Map) {
        //Populate and Set Incoterms
        Incoterms incoterms1 = populateIncotermsForNewTemplate(logisticSheet, col, usPortEntryToIncotermMap, portEntry, incoterms1Map, ExcelConstants.LogisticPricingFields.INCOTERMS_1.getFieldName());
        Incoterms incoterms2 = new Incoterms();

        List<String> incotermList = new ArrayList<>();
        incotermList.add(incoterms1.getIncoterm());


        Cell incoterm1Cell = logisticSheet.getRow(incoterms1Map.get(ExcelConstants.LogisticPricingFields.INCOTERMS_1.getFieldName())).getCell(col);
        Cell incoterm2Cell = logisticSheet.getRow(incoterms2Map.get(ExcelConstants.LogisticPricingFields.INCOTERMS_2.getFieldName())).getCell(col);

        if(df.formatCellValue(incoterm2Cell) != null && !df.formatCellValue(incoterm2Cell).isEmpty() && df.formatCellValue(incoterm1Cell) != null && !df.formatCellValue(incoterm1Cell).isEmpty() && Objects.equals(df.formatCellValue(incoterm1Cell), df.formatCellValue(incoterm2Cell))){
            String errorMessage = MessageFormat.format(ErrorMessages.Messages.INCOTERMS_ALREADY_PROVIDED.getMessage(), df.formatCellValue(incoterm2Cell));
            errorMessageDetailsList.add(new ErrorMessageDetails(ExcelConstants.ResponseRfpExcelSheetNames.LOGISTIC_PRICING.getSheetName(), new CellReference(incoterm2Cell.getRowIndex(), incoterm1Cell.getColumnIndex()).formatAsString(false) , null, null, null, errorMessage));
        }

        if (df.formatCellValue(incoterm2Cell) != null && !df.formatCellValue(incoterm2Cell).isEmpty()) {
            incoterms2 = populateIncotermsForNewTemplate(logisticSheet, col, usPortEntryToIncotermMap, portEntry, incoterms2Map, ExcelConstants.LogisticPricingFields.INCOTERMS_2.getFieldName());
            incotermList.add(incoterms2.getIncoterm());
        }


        if(supplierMillAndPortEntryToIncotermListMap.containsKey(supplierMillName) && supplierMillAndPortEntryToIncotermListMap.get(supplierMillName).containsKey(portEntry)){
            List<String> incotermMapList = supplierMillAndPortEntryToIncotermListMap.get(supplierMillName).get(portEntry);
            Collections.sort(incotermMapList);
            Collections.sort(incotermList);

            if(!incotermList.equals(incotermMapList)){

                String expectedvalue = "";
                String foundValue = "";

                for(String incoterm : incotermMapList){
                    expectedvalue  = expectedvalue + Constants.COMMA + Constants.SPACE + incoterm;
                }

                for(String incoterm : incotermList){
                    foundValue  = foundValue + Constants.COMMA + Constants.SPACE + incoterm;
                }

                String errorMessage = ErrorMessages.Messages.REPEATED_INCOTERMS_ERROR.getMessage();
//                errorMessageDetailsList.add(new ErrorMessageDetails(ExcelConstants.ResponseRfpExcelSheetNames.LOGISTIC_PRICING.getSheetName(), new CellReference(incoterm1Cell.getRowIndex(), incoterm1Cell.getColumnIndex()).formatAsString(false)  + Constants.COMMA + Constants.SPACE + new CellReference(incoterm2Cell.getRowIndex(), incoterm2Cell.getColumnIndex()).formatAsString(false) , expectedvalue, foundValue, ExcelConstants.LogisticPricingFields.INCOTERMS_1.getFieldName() + Constants.SPACE + ExcelConstants.LogisticPricingFields.INCOTERMS_2.getFieldName(), errorMessage));
                errorMessageDetailsList.add(new ErrorMessageDetails(ExcelConstants.ResponseRfpExcelSheetNames.LOGISTIC_PRICING.getSheetName(), null, null, null, null, errorMessage));

            }

        }else{
            supplierMillAndPortEntryToIncotermListMap.get(supplierMillName).put(portEntry, incotermList);
        }

        //Adding Incoterms to list (For particular SupplierMill,portEntry and Gp mill combination)
        if (supplierMillPortEntryAndDetailsMap.get(supplierMillName) != null && supplierMillPortEntryAndDetailsMap.get(supplierMillName).get(portEntry) != null && supplierMillPortEntryAndDetailsMap.get(supplierMillName).get(portEntry).get(gpMillName) != null) {
            supplierMillPortEntryAndDetailsMap.get(supplierMillName).get(portEntry).get(gpMillName).getIncoterms().add(incoterms1);

            if (df.formatCellValue(incoterm2Cell) != null && !df.formatCellValue(incoterm2Cell).isEmpty()) {
                supplierMillPortEntryAndDetailsMap.get(supplierMillName).get(portEntry).get(gpMillName).getIncoterms().add(incoterms2);
            }
        }
    }


    private static void populateAndSetFreightDetailsForNewTemplate(Sheet logisticSheet, int col, String supplierMillName, String portEntry, Map<String, Map<String, Map<String, InlandFreight>>> supplierMillPortEntryInlandDetailsMap, String gpMillName, Map<String, Integer> rowMap, Map<String, Integer> rowPortEntryMap, Map<String, Integer> rowPortEntryMismatchMap) {
        //Populate and Set Freight Details
        if (!supplierMillPortEntryInlandDetailsMap.get(supplierMillName).get(portEntry).containsKey(gpMillName)) {
            InlandFreight inlandFreight = populateInlandFreightDetailsForNewTemplate(logisticSheet, col, gpMillName, portEntry, rowPortEntryMap, rowPortEntryMismatchMap);
            supplierMillPortEntryInlandDetailsMap.get(supplierMillName).get(portEntry).put(gpMillName, inlandFreight);
        } else {
            //If SupplierMillName is already there in Map , Data Should Match else throw error
            validateRepeatedInlandFreightMetadataForNewTemplate(logisticSheet, col, supplierMillName, portEntry, supplierMillPortEntryInlandDetailsMap, gpMillName, rowPortEntryMap, rowMap);
        }
    }


    private static void validateRepeatativeFieldsLogisticPricingForNewTemplate(Sheet logisticSheet, int col, String portEntry, String gpMillName, String supplierMillName, Map<String, Map<String, PortEntryDetails>> portEntryAndDetailsMap, Map<String, Map<String, PortEntryHeaderFields>> portEntryAndHeaderFieldsMap, Map<String, Integer> rowMap, Map<String, Integer> rowPortEntryMap) {
        // This mapping is just for some fields validation. It does not have any relation with json parsing
        if (!portEntryAndDetailsMap.get(supplierMillName).containsKey(portEntry)) {
            //Populate Port Entry Details and set in the Map
            PortEntryDetails portEntryDetails = populatePortEntryDetailsForNewTemplate(logisticSheet, col, portEntry, gpMillName, rowPortEntryMap);
            portEntryAndDetailsMap.get(supplierMillName).put(portEntry, portEntryDetails);
        } else {
            //For Same Port of entry Details Should be Same else add in errors list
            validatePortEntryToPortEntryMapDetailsForNewTemplate(supplierMillName, portEntry, col, rowPortEntryMap, logisticSheet, portEntryAndDetailsMap);
        }

//        // This mapping is just for some fields validation. It does not have any relation with json parsing
//        if (!portEntryAndHeaderFieldsMap.get(supplierMillName).containsKey(portEntry)) {
//            PortEntryHeaderFields portEntryHeaderFields = populatePortEntryAndHeaderDetailsForNewTemplate(logisticSheet, col, rowMap, portEntry, gpMillName);
//            portEntryAndHeaderFieldsMap.get(supplierMillName).put(portEntry, portEntryHeaderFields);
//        } else {
//            validatePortEntryToPortEntryHeaderFieldsForNewTemplate(logisticSheet, col, portEntry, portEntryAndHeaderFieldsMap, supplierMillName, logisticPricingFieldNameToRowMap);
//        }
    }

    private static void populateAndSetPortEntryDetailsForNewTemplate(Sheet logisticSheet, int col, String supplierMillName, Map<String, Map<String, Incoterms>> usPortEntryToIncotermMap, String portEntry, String gpMillName, Map<String, Map<String, Map<String, PortEntryDetails>>> supplierMillPortEntryAndDetailsMap, Map<String, Integer> rowPortEntryMap) {

//        if (supplierMillPortEntryAndDetailsMap.get(supplierMillName).containsKey(portEntry)) {
//
//            Map<String, Map<String, PortEntryDetails>> portEntryToGpMillMap = supplierMillPortEntryAndDetailsMap.get(supplierMillName);
//            for (Map.Entry<String, Map<String, PortEntryDetails>> portEntryEntry : portEntryToGpMillMap.entrySet()) {
//                Map<String, PortEntryDetails> gpMillDetailsMap = portEntryEntry.getValue();
//
//                String gpMillSet = null;
//                for (String gpMill : gpMillDetailsMap.keySet()) {
//                    gpMillSet = gpMill;
//                    break;
//                }
//
//                if (gpMillSet != null) {
//                    // Split the gpMillSet into an array of individual GP mills
//                    String[] gpMillsArray = gpMillSet.split(Constants.COMMA_SEPARATED_REGEX); // This splits by comma and trims spaces around each part
//
//                    boolean isPresent = false;
//                    for (String gpMill : gpMillsArray) {
//                        if (gpMill.trim().equals(gpMillName)) {
//                            isPresent = true;
//                            break;
//                        }
//                    }
//
//                    // Append the new GP mill to the existing GP mills in the key
//                    if (!isPresent) {
//                        String newGpMillKey = gpMillSet + Constants.COMMA + gpMillName;
//                        PortEntryDetails oldGpMillDetails = supplierMillPortEntryAndDetailsMap.get(supplierMillName).get(portEntry).get(gpMillSet);
//
//                        if(oldGpMillDetails != null) {
//                            oldGpMillDetails.setGp_mill(newGpMillKey);
//
//                            supplierMillPortEntryAndDetailsMap.get(supplierMillName).get(portEntry).remove(gpMillSet);
//
//                            supplierMillPortEntryAndDetailsMap.get(supplierMillName).get(portEntry).put(newGpMillKey, oldGpMillDetails);
//                        }
//                    }
//                }
//            }
//        }


        if (!supplierMillPortEntryAndDetailsMap.get(supplierMillName).get(portEntry).containsKey(gpMillName)) {

            //Populate Port Entry Details and set in the Map
            PortEntryDetails portEntryDetails = populatePortEntryDetailsForNewTemplate(logisticSheet, col, portEntry, gpMillName, rowPortEntryMap);
            supplierMillPortEntryAndDetailsMap.get(supplierMillName).get(portEntry).put(gpMillName, portEntryDetails);
        }
    }


    //method to Validate Bid type Based Conditions
    private static void validateBidTypeBasedConditions(Sheet sheet,BidQtyDetail bidQtyDetail) {
        LOGGER.info("In ExcelParsingUtils ::  validateBidTypeBasedConditions");
        Integer lumpSumExampleRowNum = getStartOfLumpSumRow(sheet);
        //If LumSum Row Not Found Return
        if (lumpSumExampleRowNum == null)
            return;

        //Validating Mill Specific Conditions (If Mill Specific then Lump Sum Section Should be Null)
        if (bidQtyDetail.getBid_type() != null && Objects.equals(bidQtyDetail.getBid_type(), Constants.BidType.MILL_SPECIFIC.getValue())){
            validateIfLumpSumDetailsEntered(sheet,lumpSumExampleRowNum);

        }

        // Validating Lump Sum(if Lump sum then mill Specific Section should be Null/Empty)
         if (bidQtyDetail.getBid_type() != null && Objects.equals(bidQtyDetail.getBid_type(), Constants.BidType.LUMP_SUM.getValue())){
             validateIfMillSpecificDetailsEntered(sheet);
        }
    }

    //Method to check if Bid Type is Lum Sum but entered any Details in Mill Specific Section
    private static void validateIfMillSpecificDetailsEntered(Sheet sheet) {
        Integer endRowForMillSpecific = endRowOfMills;
        Integer startRowForMillSpecific = bidDetailsMap.get(ResponseRfpExcelHeaders.GP_MILL_START_ROW.getValue());
        if (endRowForMillSpecific == null)
            return;

        for (int rowNumer = startRowForMillSpecific; rowNumer < endRowForMillSpecific ; rowNumer++){
            //Supplier Mill A
            if (checkIsCellEmpty(new CellReference(rowNumer,bidDetailsMap.get(ResponseRfpExcelHeaders.SUPPLIER_MILL_A.getValue())),sheet)){
                String errorMessage = MessageFormat.format(ErrorMessages.Messages.EXPECTED_NULL_VALUE.getMessage(), df.formatCellValue(getCellValueFromCellReference(new CellReference(rowNumer,bidDetailsMap.get(ResponseRfpExcelHeaders.SUPPLIER_MILL_A.getValue())),sheet)),Constants.BidType.LUMP_SUM.getValue());
                errorMessageDetailsList.add(new ErrorMessageDetails(ExcelConstants.ResponseRfpExcelSheetNames.BID_DETAILS.getSheetName(), new CellReference(rowNumer,bidDetailsMap.get(ResponseRfpExcelHeaders.SUPPLIER_MILL_A.getValue())).formatAsString(false), null, df.formatCellValue(getCellValueFromCellReference(new CellReference(rowNumer,bidDetailsMap.get(ResponseRfpExcelHeaders.SUPPLIER_MILL_A.getValue())),sheet)), null, errorMessage));

            }
            //Supplier Mill A Volume
            if (checkIsCellEmpty(new CellReference(rowNumer,bidDetailsMap.get(ResponseRfpExcelHeaders.SUPPLIER_MILL_A_VOLUME.getValue())),sheet)){
                String errorMessage = MessageFormat.format(ErrorMessages.Messages.EXPECTED_NULL_VALUE.getMessage(), df.formatCellValue(getCellValueFromCellReference(new CellReference(rowNumer,bidDetailsMap.get(ResponseRfpExcelHeaders.SUPPLIER_MILL_A_VOLUME.getValue())),sheet)),Constants.BidType.LUMP_SUM.getValue());
                errorMessageDetailsList.add(new ErrorMessageDetails(ExcelConstants.ResponseRfpExcelSheetNames.BID_DETAILS.getSheetName(), new CellReference(rowNumer,bidDetailsMap.get(ResponseRfpExcelHeaders.SUPPLIER_MILL_A_VOLUME.getValue())).formatAsString(false), null, df.formatCellValue(getCellValueFromCellReference(new CellReference(rowNumer,bidDetailsMap.get(ResponseRfpExcelHeaders.SUPPLIER_MILL_A_VOLUME.getValue())),sheet)), null, errorMessage));

            }
            //Supplier Mill B
            if (checkIsCellEmpty(new CellReference(rowNumer,bidDetailsMap.get(ResponseRfpExcelHeaders.SUPPLIER_MILL_B.getValue())),sheet)){
                String errorMessage = MessageFormat.format(ErrorMessages.Messages.EXPECTED_NULL_VALUE.getMessage(), df.formatCellValue(getCellValueFromCellReference(new CellReference(rowNumer,bidDetailsMap.get(ResponseRfpExcelHeaders.SUPPLIER_MILL_B.getValue())),sheet)),Constants.BidType.LUMP_SUM.getValue());
                errorMessageDetailsList.add(new ErrorMessageDetails(ExcelConstants.ResponseRfpExcelSheetNames.BID_DETAILS.getSheetName(), new CellReference(rowNumer,bidDetailsMap.get(ResponseRfpExcelHeaders.SUPPLIER_MILL_B.getValue())).formatAsString(false), null, df.formatCellValue(getCellValueFromCellReference(new CellReference(rowNumer,bidDetailsMap.get(ResponseRfpExcelHeaders.SUPPLIER_MILL_B.getValue())),sheet)), null, errorMessage));

            }
            //Supplier Mill B Volume
            if (checkIsCellEmpty(new CellReference(rowNumer,bidDetailsMap.get(ResponseRfpExcelHeaders.SUPPLIER_MILL_B_VOLUME.getValue())),sheet)){
                String errorMessage = MessageFormat.format(ErrorMessages.Messages.EXPECTED_NULL_VALUE.getMessage(), df.formatCellValue(getCellValueFromCellReference(new CellReference(rowNumer,bidDetailsMap.get(ResponseRfpExcelHeaders.SUPPLIER_MILL_B_VOLUME.getValue())),sheet)),Constants.BidType.LUMP_SUM.getValue());
                errorMessageDetailsList.add(new ErrorMessageDetails(ExcelConstants.ResponseRfpExcelSheetNames.BID_DETAILS.getSheetName(), new CellReference(rowNumer,bidDetailsMap.get(ResponseRfpExcelHeaders.SUPPLIER_MILL_B_VOLUME.getValue())).formatAsString(false), null, df.formatCellValue(getCellValueFromCellReference(new CellReference(rowNumer,bidDetailsMap.get(ResponseRfpExcelHeaders.SUPPLIER_MILL_B_VOLUME.getValue())),sheet)), null, errorMessage));

            }

        }
    }

    //Method to check if Bid Type is Mill Specific but entered any Details in Lump Sum Section
    private static void validateIfLumpSumDetailsEntered(Sheet sheet, Integer lumpSumExampleRowNum) {
        Integer colStart = ExcelConstants.LUMP_SUM_HEADER_TO_COLUMN_NUMBER.get(ResponseRfpExcelHeaders.SUPPLIER_MILL_A.getValue());
        Integer columnEnd = ExcelConstants.LUMP_SUM_HEADER_TO_COLUMN_NUMBER.get(ResponseRfpExcelHeaders.SUPPLIER_MILL_B_VOLUME.getValue());
        for (int colNum = colStart ; colNum <= columnEnd ; colNum++ ){
            if (checkIsCellEmpty(new CellReference(lumpSumExampleRowNum+1,colNum),sheet)){
                // if it is not null then Throw error
                String errorMessage = MessageFormat.format(ErrorMessages.Messages.EXPECTED_NULL_VALUE.getMessage(), df.formatCellValue(getCellValueFromCellReference(new CellReference(lumpSumExampleRowNum+1,colNum),sheet)),Constants.BidType.MILL_SPECIFIC.getValue());
                errorMessageDetailsList.add(new ErrorMessageDetails(ExcelConstants.ResponseRfpExcelSheetNames.BID_DETAILS.getSheetName(), new CellReference(lumpSumExampleRowNum+1,colNum).formatAsString(false), null, df.formatCellValue(getCellValueFromCellReference(new CellReference(lumpSumExampleRowNum+1,colNum),sheet)), null, errorMessage));

            }
        }
    }

    //Method to Fetch Starting Row Number of Lump Sum Section
    private static Integer getStartOfLumpSumRow(Sheet sheet) {
        //Last row will be the Row Where there is LUMP SUM
        Integer startOfLumpSumRow = null;
        for (int i = bidDetailsMap.get(ExcelConstants.ResponseRfpExcelHeaders.GP_MILL_START_ROW.getValue()); i < sheet.getPhysicalNumberOfRows(); i++) {
            Row row = sheet.getRow(i);
            if (row != null && row.getCell(bidDetailsMap.get(ExcelConstants.ResponseRfpExcelHeaders.TOTAL_BID_VOLUME_COL_NUM.getValue()))!=null && df.formatCellValue(row.getCell(bidDetailsMap.get(ExcelConstants.ResponseRfpExcelHeaders.TOTAL_BID_VOLUME_COL_NUM.getValue())))!=null  && Objects.equals(df.formatCellValue(row.getCell(bidDetailsMap.get(ExcelConstants.ResponseRfpExcelHeaders.TOTAL_BID_VOLUME_COL_NUM.getValue()))), Constants.BidType.LUMP_SUM.getValue())) {
                startOfLumpSumRow = i;
                return startOfLumpSumRow;
            }
        }
        String errorMessage = MessageFormat.format(ErrorMessages.Messages.MANDATORY_FIELD_MISSING_ERROR.getMessage(), Constants.BidType.LUMP_SUM.getValue());
        errorMessageDetailsList.add(new ErrorMessageDetails(ExcelConstants.ResponseRfpExcelSheetNames.BID_DETAILS.getSheetName(), null, null, null, Constants.BidType.LUMP_SUM.getValue(), errorMessage));

        return startOfLumpSumRow;
    }


    //method to Validate Mill Conditions For Mill Specific Bid Type

    private static void validateMillSpecificConditions(Sheet sheet, Integer endRowOfMills) {
        //Validate For Mandatory Fields if Mill Specific
        validateMandatoryFieldOfGpMills(sheet, endRowOfMills);

        int firstRowOfGpMills = bidDetailsMap.get(ExcelConstants.ResponseRfpExcelHeaders.GP_MILL_START_ROW.getValue());
        // getting end of row where supplier mills ends
        int endRow = endRowOfMills;
        for (int rowNum = firstRowOfGpMills; rowNum < endRow; rowNum++) {
            //Todo : confirm Supplier Mills Will be Maximum 2
            String supplierMillName1 = null;
            String supplierMillName2 = null;
            Row row = sheet.getRow(rowNum);
            if(isRowEmpty(row)){
                continue;
            }
            MillSpecBid millSpecbid = new MillSpecBid();
            if (checkIsCellEmptyAndValidateRegex(new CellReference(row.getCell(bidDetailsMap.get(ExcelConstants.ResponseRfpExcelHeaders.GP_MILL_COL_NUMBER.getValue()))), sheet, Optional.of(ExcelConstants.ALPHABETS_REGEX))) {
                millSpecbid.setMill(df.formatCellValue(row.getCell(bidDetailsMap.get(ExcelConstants.ResponseRfpExcelHeaders.GP_MILL_COL_NUMBER.getValue()))));
            }

            //TODO :: need to accept supplierMillName will be Null Or N/A
            //validate SupplierMillName
            if (checkIsCellEmptyAndValidateRegex(new CellReference(row.getRowNum(), bidDetailsMap.get(ResponseRfpExcelHeaders.SUPPLIER_MILL_A.getValue())),sheet,Optional.empty())){
                supplierMillName1 = df.formatCellValue(getCellValueFromCellReference(new CellReference(row.getCell(bidDetailsMap.get(ResponseRfpExcelHeaders.SUPPLIER_MILL_A.getValue()))), sheet));
            }

            if (!checkIsCellEmpty(new CellReference(row.getRowNum(), bidDetailsMap.get(ResponseRfpExcelHeaders.SUPPLIER_MILL_A.getValue())), sheet)) {
//                String errorMessage = MessageFormat.format(ErrorMessages.Messages.DATA_FORMAT_MISMATCH.getMessage(), ExcelConstants.ALPHABETS, df.formatCellValue(getCellValueFromCellReference(new CellReference(row.getRowNum(), bidDetailsMap.get(ResponseRfpExcelHeaders.SUPPLIER_MILL_A.getValue())), sheet)));
//                errorMessageDetailsList.add(new ErrorMessageDetails(sheet.getSheetName(), new CellReference(row.getRowNum(), bidDetailsMap.get(ResponseRfpExcelHeaders.SUPPLIER_MILL_A.getValue())).formatAsString(false), ExcelConstants.ExpectedValuesList.MANDATORY.getValue(), df.formatCellValue(getCellValueFromCellReference(new CellReference(row.getRowNum(), bidDetailsMap.get(ResponseRfpExcelHeaders.SUPPLIER_MILL_A.getValue())), sheet)), ResponseRfpExcelHeaders.SUPPLIER_MILL.getValue(), errorMessage));
            }else {
                //Validate Supplier Mill1  and Volume
                validateSupplierMillAndVolume(sheet,supplierMillName1,row,bidDetailsMap.get(ResponseRfpExcelHeaders.SUPPLIER_MILL_A_VOLUME.getValue()));
            }


            // for supplierMill 2
            //validate SupplierMillName
            if (checkIsCellEmptyAndValidateRegex(new CellReference(row.getRowNum(), bidDetailsMap.get(ResponseRfpExcelHeaders.SUPPLIER_MILL_B.getValue())),sheet,Optional.empty())){
                supplierMillName2 = df.formatCellValue(getCellValueFromCellReference(new CellReference(row.getCell(bidDetailsMap.get(ResponseRfpExcelHeaders.SUPPLIER_MILL_B.getValue()))), sheet));
            }

            if (!checkIsCellEmpty(new CellReference(row.getRowNum(), bidDetailsMap.get(ResponseRfpExcelHeaders.SUPPLIER_MILL_B.getValue())), sheet)) {
//                String errorMessage = MessageFormat.format(ErrorMessages.Messages.DATA_FORMAT_MISMATCH.getMessage(), ExcelConstants.ALPHABETS, df.formatCellValue(getCellValueFromCellReference(new CellReference(row.getRowNum(), bidDetailsMap.get(ResponseRfpExcelHeaders.SUPPLIER_MILL_B.getValue())), sheet)));
//                errorMessageDetailsList.add(new ErrorMessageDetails(sheet.getSheetName(), new CellReference(row.getRowNum(), bidDetailsMap.get(ResponseRfpExcelHeaders.SUPPLIER_MILL_B.getValue())).formatAsString(false), ExcelConstants.ExpectedValuesList.MANDATORY.getValue(), df.formatCellValue(getCellValueFromCellReference(new CellReference(row.getRowNum(), bidDetailsMap.get(ResponseRfpExcelHeaders.SUPPLIER_MILL_B.getValue())), sheet)), ResponseRfpExcelHeaders.SUPPLIER_MILL.getValue(), errorMessage));
            }else {
                //Validate Supplier Mill2 and Volume
                validateSupplierMillAndVolume(sheet,supplierMillName2,row,bidDetailsMap.get(ResponseRfpExcelHeaders.SUPPLIER_MILL_B_VOLUME.getValue()));
            }

        }

    }

    //Method to validate Supplier Mill Name abd Volume
    private static void validateSupplierMillAndVolume(Sheet sheet,String supplierMillName, Row row, int colNum ) {
        //If Supplier Mill is Null Supplier Bid Volume should be o or Null else throw error
        //else bid volume Should be 0 or greater than 0
        if (supplierMillName != null && !Objects.equals(supplierMillName.toLowerCase(), ExcelConstants.NA.toLowerCase())) {
            isCellEmptyAndValidateRegex(new CellReference(row.getRowNum(),colNum), sheet, ResponseRfpExcelHeaders.SUPPLIER_BID_VOLUME.getValue(), Optional.of(ExcelConstants.NUMBER_REGEX), ExcelConstants.NUMBER);
        }else{
            String volume = df.formatCellValue(getCellValueFromCellReference(new CellReference(row.getRowNum(), colNum),sheet));
            if (!validateRegex(volume,ExcelConstants.ZERO_OR_NULL_REGEX)){
                String errorMessage = MessageFormat.format(ErrorMessages.Messages.SUPPLIER_MILL_VOLUME_SHOULD_BE_EMPTY_OR_ZERO.getMessage(), volume);
                errorMessageDetailsList.add(new ErrorMessageDetails(sheet.getSheetName(), new CellReference(row.getRowNum(), colNum).formatAsString(false), ExcelConstants.ExpectedValuesList.MANDATORY.getValue(), null,  ResponseRfpExcelHeaders.SUPPLIER_BID_VOLUME.getValue(), errorMessage));
            }
        }
    }



    //Method to Validate Values if Bid Type Is Lump Sum
    private static void validateLumpSumSpecificConditions(Sheet sheet, Integer endRowOfMills) {
        // difference between total row number and lumpsum starting tables (where headers are there)
        Integer lumpSumStartRow = endRowOfMills + ExcelConstants.TOTAL_ROW_NUL_TO_LUMP_SUM_DIFF ;
        // 3 is for adding to lumpSumStartRow and get row where lumpSum SupplierMillDetails are there
        Integer lumpSumSupplierMillRow = ExcelConstants.LUMP_SUM_ABOVE_ROW_TO_SUPPLIER_DETAILS_ROW;



        // for lump sum column numbers
        Integer expectedAnnualVolumeColNum = ExcelConstants.LUMP_SUM_HEADER_TO_COLUMN_NUMBER.get(ResponseRfpExcelHeaders.GP_MILL_EXPECTED_ANNUAL_VOLUME_CELL_NUM.getValue());
        Integer supplierVolumeBidColNum = ExcelConstants.LUMP_SUM_HEADER_TO_COLUMN_NUMBER.get(ResponseRfpExcelHeaders.SUPPLIER_BID_VOLUME.getValue());
        Integer supplierMill1ColNum = ExcelConstants.LUMP_SUM_HEADER_TO_COLUMN_NUMBER.get(ResponseRfpExcelHeaders.SUPPLIER_MILL_A.getValue());
        Integer supplierVolume1ColNum = ExcelConstants.LUMP_SUM_HEADER_TO_COLUMN_NUMBER.get(ResponseRfpExcelHeaders.SUPPLIER_MILL_A_VOLUME.getValue());
        Integer supplierMill2ColNum = ExcelConstants.LUMP_SUM_HEADER_TO_COLUMN_NUMBER.get(ResponseRfpExcelHeaders.SUPPLIER_MILL_B.getValue());
        Integer supplierVolume2ColNum = ExcelConstants.LUMP_SUM_HEADER_TO_COLUMN_NUMBER.get(ResponseRfpExcelHeaders.SUPPLIER_MILL_B_VOLUME.getValue());

        //validating Headers
        validateHeadersForBidQtyDetails(sheet.getRow(lumpSumStartRow),expectedAnnualVolumeColNum,ResponseRfpExcelHeaders.GP_MILL_EXPECTED_ANNUAL_VOLUME_CELL_NUM.getValue());
        validateHeadersForBidQtyDetails(sheet.getRow(lumpSumStartRow),supplierVolumeBidColNum,ResponseRfpExcelHeaders.GP_MILL_SUPPLIER_VOLUME_BID_CELL_NUM.getValue());
        validateHeadersForBidQtyDetails(sheet.getRow(lumpSumStartRow + 1 ),supplierMill1ColNum,ResponseRfpExcelHeaders.SUPPLIER_MILL_A.getValue());
        validateHeadersForBidQtyDetails(sheet.getRow(lumpSumStartRow + 1),supplierVolume1ColNum,ResponseRfpExcelHeaders.SUPPLIER_MILL_A_VOLUME.getValue());
        validateHeadersForBidQtyDetails(sheet.getRow(lumpSumStartRow + 1),supplierMill2ColNum,ResponseRfpExcelHeaders.SUPPLIER_MILL_B.getValue());
        validateHeadersForBidQtyDetails(sheet.getRow(lumpSumStartRow + 1),supplierVolume2ColNum,ResponseRfpExcelHeaders.SUPPLIER_MILL_B_VOLUME.getValue());

        String supplierMillName1 = null;
        String supplierMillName2 = null;

        //validate SupplierMillName
//        isCellEmptyAndValidateRegex(new CellReference(sheet.getRow(lumpSumStartRow + lumpSumSupplierMillRow).getCell(supplierMill1ColNum)),sheet, ResponseRfpExcelHeaders.SUPPLIER_MILL.getValue(), Optional.of(ExcelConstants.ALPHABETS_OR_ZERO_REGEX), ExcelConstants.ALPHABETS);
        if (checkIsCellEmptyAndValidateRegex(new CellReference(sheet.getRow(lumpSumStartRow + lumpSumSupplierMillRow).getCell(supplierMill1ColNum)), sheet, Optional.empty())) {
            supplierMillName1 = df.formatCellValue(getCellValueFromCellReference(new CellReference(sheet.getRow(lumpSumStartRow + lumpSumSupplierMillRow).getCell(supplierMill1ColNum)), sheet));
        }

        if (!checkIsCellEmpty(new CellReference(sheet.getRow(lumpSumStartRow + lumpSumSupplierMillRow).getCell(supplierMill1ColNum)), sheet)) {
//            String errorMessage = MessageFormat.format(ErrorMessages.Messages.DATA_FORMAT_MISMATCH.getMessage(), ExcelConstants.ALPHABETS, df.formatCellValue(getCellValueFromCellReference(new CellReference(sheet.getRow(lumpSumStartRow + lumpSumSupplierMillRow).getCell(supplierMill1ColNum)), sheet)));
//            errorMessageDetailsList.add(new ErrorMessageDetails(sheet.getSheetName(), new CellReference(sheet.getRow(lumpSumStartRow + lumpSumSupplierMillRow).getCell(supplierMill1ColNum)).formatAsString(false), ExcelConstants.ExpectedValuesList.MANDATORY.getValue(), df.formatCellValue(getCellValueFromCellReference(new CellReference(sheet.getRow(lumpSumStartRow + lumpSumSupplierMillRow).getCell(supplierMill1ColNum)), sheet)), ResponseRfpExcelHeaders.SUPPLIER_MILL.getValue(), errorMessage));
        }else {
            //Validate Supplier Mill Name 1 and volume
            validateSupplierMillAndVolume(sheet,supplierMillName1,sheet.getRow(lumpSumStartRow + lumpSumSupplierMillRow),supplierVolume1ColNum);
        }


        //validate SupplierMill 2 Name and volume
//        isCellEmptyAndValidateRegex(new CellReference(sheet.getRow(lumpSumStartRow + lumpSumSupplierMillRow).getCell(supplierMill2ColNum)), sheet, ResponseRfpExcelHeaders.SUPPLIER_MILL.getValue(), Optional.of(ExcelConstants.ALPHABETS_OR_ZERO_REGEX), ExcelConstants.ALPHABETS);
        if (checkIsCellEmptyAndValidateRegex(new CellReference(sheet.getRow(lumpSumStartRow + lumpSumSupplierMillRow).getCell(supplierMill2ColNum)), sheet, Optional.empty())) {
            supplierMillName2 = df.formatCellValue(getCellValueFromCellReference(new CellReference(sheet.getRow(lumpSumStartRow + lumpSumSupplierMillRow).getCell(supplierMill2ColNum)), sheet));
        }
        if (!checkIsCellEmpty(new CellReference(sheet.getRow(lumpSumStartRow + lumpSumSupplierMillRow).getCell(supplierMill2ColNum)), sheet)) {
//            String errorMessage = MessageFormat.format(ErrorMessages.Messages.DATA_FORMAT_MISMATCH.getMessage(), ExcelConstants.ALPHABETS, df.formatCellValue(getCellValueFromCellReference(new CellReference(sheet.getRow(lumpSumStartRow + lumpSumSupplierMillRow).getCell(supplierMill2ColNum)), sheet)));
//            errorMessageDetailsList.add(new ErrorMessageDetails(sheet.getSheetName(), new CellReference(sheet.getRow(lumpSumStartRow + lumpSumSupplierMillRow).getCell(supplierMill2ColNum)).formatAsString(false), ExcelConstants.ExpectedValuesList.MANDATORY.getValue(), df.formatCellValue(getCellValueFromCellReference(new CellReference(sheet.getRow(lumpSumStartRow + lumpSumSupplierMillRow).getCell(supplierMill2ColNum)), sheet)), ResponseRfpExcelHeaders.SUPPLIER_MILL.getValue(), errorMessage));
        }else{
            //Validate Supplier Mill Name 2 and volume
            validateSupplierMillAndVolume(sheet,supplierMillName2,sheet.getRow(lumpSumStartRow + lumpSumSupplierMillRow),supplierVolume2ColNum);
        }
    }


    private static boolean isCellValueNA(Sheet sheet, Cell cell) {
        if (cell == null || cell.getCellType() != CellType.STRING) {
            return false;  // Cell is not a string or is null, so it can't be "N/A"
        }

        String cellValue = cell.getStringCellValue().trim();

        // Case-insensitive check for "N/A"
        return cellValue.equalsIgnoreCase(ExcelConstants.NA);
    }


    //Validation For CeilFloorPeriod is Mandatory if they entered priceCeil or PriceFloor
    private static void validationForCeilFloorPeriod(Long priceCeil, Long priceFloor, String ceilFloorPeriodStart, String ceilFloorPeriodEnd) {

        // if Any One of PriceCeil PriceFloor is there then ceilFloorPeriodStart,ceilFloorPeriodEnd are Mandatory
        if ((priceCeil != null || priceFloor != null) && (ceilFloorPeriodStart == null || ceilFloorPeriodEnd == null)) {
            String errorMessage = ErrorMessages.Messages.PRICE_EFFECTIVE_PERIOD_MANDATORY.getMessage();
            errorMessageDetailsList.add(new ErrorMessageDetails(ExcelConstants.ResponseRfpExcelSheetNames.COMMERCIAL_PRICING.getSheetName(),ExcelConstants.PRICING_DETAILS_CELL_REFERENCE_FOR_COMMERCIAL_PRICING.get(ExcelConstants.CommercialSheetFieldNames.PRICE_CEILING_FLOOR_PERIOD.getFieldName()), ExcelConstants.ExpectedValuesList.MANDATORY.getValue(), null, ResponseRfpExcelHeaders.PRICE_CEILING_EFFECTIVE_PERIOD.getValue(), errorMessage));
        }
    }

    private static void validateDomesticPortEntryDetails(Sheet logisticSheet, int col, Map<String, Integer> rowMap) {

        Cell safetyStockLocationTypeCell = logisticSheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.SAFETY_STOCK_LOCATION_TYPE.getFieldName())).getCell(col);
        Cell usPortEntryCell = logisticSheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.US_PORT_OF_ENTRY.getFieldName())).getCell(col);
        Cell inlandTransitOriginTypeCell = logisticSheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.INLAND_TRANSIT_ORIGIN_TYPE.getFieldName())).getCell(col);

        Cell safetyStockLocationNameCell = logisticSheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.SAFETY_STOCK_LOCATION_NAME.getFieldName())).getCell(col);
        Cell inlandTransitOriginNameCell = logisticSheet.getRow(rowMap.get(ExcelConstants.LogisticPricingFields.INLAND_TRANSIT_ORIGIN_NAME.getFieldName())).getCell(col);
        Cell supplierMillCell = logisticSheet.getRow(logisticPricingFieldNameToRowMap.get(ExcelConstants.LogisticPricingFields.SUPPLIER_MILL.getFieldName())).getCell(col);

        if(Objects.equals(df.formatCellValue(usPortEntryCell), ExcelConstants.USPortOfEntry.DOMESTIC.getValue())){
            if(Objects.equals(df.formatCellValue(safetyStockLocationTypeCell), ExcelConstants.InlandTransitOriginType.PORT_OF_ENTRY.getValue())){
                String errorMessage = MessageFormat.format(ErrorMessages.Messages.CONDITIONAL_FIELD_OTHER_THAN_ERROR.getMessage(), ResponseRfpExcelHeaders.US_PORT_OF_ENTRY.getValue(), ExcelConstants.OriginPort.DOMESTIC.getValue(), ResponseRfpExcelHeaders.SAFETY_STOCK_LOCATION_TYPE.getValue(),  ExcelConstants.InlandTransitOriginType.PORT_OF_ENTRY.getValue(), df.formatCellValue(safetyStockLocationTypeCell));
                errorMessageDetailsList.add(new ErrorMessageDetails(logisticSheet.getSheetName(), new CellReference(safetyStockLocationTypeCell).formatAsString(false), ExcelConstants.OTHER_THAN + Constants.SPACE +  ExcelConstants.InlandTransitOriginType.PORT_OF_ENTRY.getValue() , df.formatCellValue(safetyStockLocationTypeCell), ResponseRfpExcelHeaders.SAFETY_STOCK_LOCATION_TYPE.getValue(), errorMessage));
            }

            if(Objects.equals(df.formatCellValue(inlandTransitOriginTypeCell), ExcelConstants.InlandTransitOriginType.PORT_OF_ENTRY.getValue())){
                String errorMessage = MessageFormat.format(ErrorMessages.Messages.CONDITIONAL_FIELD_OTHER_THAN_ERROR.getMessage(), ResponseRfpExcelHeaders.US_PORT_OF_ENTRY.getValue(), ExcelConstants.OriginPort.DOMESTIC.getValue(), ResponseRfpExcelHeaders.INLAND_TRANSIT_ORIGIN_TYPE.getValue(),  ExcelConstants.InlandTransitOriginType.PORT_OF_ENTRY.getValue(), df.formatCellValue(inlandTransitOriginTypeCell));
                errorMessageDetailsList.add(new ErrorMessageDetails(logisticSheet.getSheetName(), new CellReference(inlandTransitOriginTypeCell).formatAsString(false), ExcelConstants.OTHER_THAN + Constants.SPACE +  ExcelConstants.InlandTransitOriginType.PORT_OF_ENTRY.getValue() , df.formatCellValue(inlandTransitOriginTypeCell), ResponseRfpExcelHeaders.INLAND_TRANSIT_ORIGIN_TYPE.getValue(), errorMessage));
            }

            if(Objects.equals(df.formatCellValue(safetyStockLocationTypeCell), ExcelConstants.InlandTransitOriginType.SUPPLIER_MILL.getValue()) && !Objects.equals(df.formatCellValue(safetyStockLocationNameCell), df.formatCellValue(supplierMillCell))){
                String errorMessage = MessageFormat.format(ErrorMessages.Messages.CONDITIONAL_FIELD_ERROR.getMessage(), ResponseRfpExcelHeaders.SAFETY_STOCK_LOCATION_TYPE.getValue(),ExcelConstants.InlandTransitOriginType.SUPPLIER_MILL.getValue(), ResponseRfpExcelHeaders.SAFETY_STOCK_LOCATION_NAME.getValue(),   df.formatCellValue(supplierMillCell), df.formatCellValue(safetyStockLocationNameCell));
                errorMessageDetailsList.add(new ErrorMessageDetails(logisticSheet.getSheetName(), new CellReference(safetyStockLocationNameCell).formatAsString(false), df.formatCellValue(supplierMillCell).replaceAll(Constants.SPACE,Constants.EMPTY_STRING) , df.formatCellValue(safetyStockLocationNameCell), ResponseRfpExcelHeaders.SAFETY_STOCK_LOCATION_NAME.getValue(), errorMessage));
            }

            if(Objects.equals(df.formatCellValue(inlandTransitOriginTypeCell), ExcelConstants.InlandTransitOriginType.SUPPLIER_MILL.getValue()) && !Objects.equals(df.formatCellValue(inlandTransitOriginNameCell), df.formatCellValue(supplierMillCell))){
                String errorMessage = MessageFormat.format(ErrorMessages.Messages.CONDITIONAL_FIELD_ERROR.getMessage(), ResponseRfpExcelHeaders.SAFETY_STOCK_LOCATION_TYPE.getValue(),ExcelConstants.InlandTransitOriginType.SUPPLIER_MILL.getValue(), ResponseRfpExcelHeaders.SAFETY_STOCK_LOCATION_NAME.getValue(),   df.formatCellValue(supplierMillCell), df.formatCellValue(safetyStockLocationNameCell));
                errorMessageDetailsList.add(new ErrorMessageDetails(logisticSheet.getSheetName(), new CellReference(inlandTransitOriginNameCell).formatAsString(false), df.formatCellValue(supplierMillCell).replaceAll(Constants.SPACE,Constants.EMPTY_STRING) , df.formatCellValue(inlandTransitOriginNameCell), ResponseRfpExcelHeaders.INLAND_TRANSIT_ORIGIN_NAME.getValue(), errorMessage));
            }


        }else{
            if(Objects.equals(df.formatCellValue(safetyStockLocationTypeCell), ExcelConstants.InlandTransitOriginType.SUPPLIER_MILL.getValue())){
                String errorMessage = MessageFormat.format(ErrorMessages.Messages.CONDITIONAL_FIELD_OTHER_THAN_ERROR.getMessage(), ResponseRfpExcelHeaders.US_PORT_OF_ENTRY.getValue(), ExcelConstants.SupplierType.INTERNATIONAL.getValue(), ResponseRfpExcelHeaders.SAFETY_STOCK_LOCATION_TYPE.getValue(),  ExcelConstants.InlandTransitOriginType.SUPPLIER_MILL.getValue(), df.formatCellValue(safetyStockLocationTypeCell));
                errorMessageDetailsList.add(new ErrorMessageDetails(logisticSheet.getSheetName(), new CellReference(safetyStockLocationTypeCell).formatAsString(false), ExcelConstants.OTHER_THAN + Constants.SPACE +  ExcelConstants.InlandTransitOriginType.SUPPLIER_MILL.getValue() , df.formatCellValue(safetyStockLocationTypeCell), ResponseRfpExcelHeaders.SAFETY_STOCK_LOCATION_TYPE.getValue(), errorMessage));
            }

            if(Objects.equals(df.formatCellValue(inlandTransitOriginTypeCell), ExcelConstants.InlandTransitOriginType.SUPPLIER_MILL.getValue())){
                String errorMessage = MessageFormat.format(ErrorMessages.Messages.CONDITIONAL_FIELD_OTHER_THAN_ERROR.getMessage(), ResponseRfpExcelHeaders.US_PORT_OF_ENTRY.getValue(), ExcelConstants.SupplierType.INTERNATIONAL.getValue(), ResponseRfpExcelHeaders.INLAND_TRANSIT_ORIGIN_TYPE.getValue(),  ExcelConstants.InlandTransitOriginType.SUPPLIER_MILL.getValue(), df.formatCellValue(inlandTransitOriginTypeCell));
                errorMessageDetailsList.add(new ErrorMessageDetails(logisticSheet.getSheetName(), new CellReference(inlandTransitOriginTypeCell).formatAsString(false), ExcelConstants.OTHER_THAN + Constants.SPACE +  ExcelConstants.InlandTransitOriginType.SUPPLIER_MILL.getValue() , df.formatCellValue(inlandTransitOriginTypeCell), ResponseRfpExcelHeaders.INLAND_TRANSIT_ORIGIN_TYPE.getValue(), errorMessage));
            }
        }
    }


    private static void validateUsPortEntry(Sheet logisticSheet, String supplierMillName, int col, Map<String, String> supplierMilltoPortEntryMap, String portEntry, Map<String, Integer> rowPortEntryMap) {
        if(supplierMilltoPortEntryMap.containsKey(supplierMillName) == Boolean.TRUE){
            String previousPortEntry = supplierMilltoPortEntryMap.get(supplierMillName);

            if(Objects.equals(previousPortEntry, ExcelConstants.USPortOfEntry.DOMESTIC.getValue())){
                if(!Objects.equals(portEntry, ExcelConstants.USPortOfEntry.DOMESTIC.getValue())){
                    String errorMessage = MessageFormat.format(ErrorMessages.Messages.INVALID_US_PORT.getMessage(), portEntry, ExcelConstants.SupplierType.DOMESTIC.getValue());
                    errorMessageDetailsList.add(new ErrorMessageDetails(logisticSheet.getSheetName(),  new CellReference(logisticSheet.getRow(rowPortEntryMap.get(ExcelConstants.LogisticPricingFields.US_PORT_OF_ENTRY.getFieldName())).getCell(col)).formatAsString(false), ExcelConstants.USPortOfEntry.DOMESTIC.getValue() , (df.formatCellValue(logisticSheet.getRow(rowPortEntryMap.get(ExcelConstants.LogisticPricingFields.US_PORT_OF_ENTRY.getFieldName())).getCell(col))), ExcelConstants.LogisticPricingFields.US_PORT_OF_ENTRY.getFieldName(), errorMessage));
                }
            }else{
                if(Objects.equals(portEntry, ExcelConstants.USPortOfEntry.DOMESTIC.getValue())){
                    String errorMessage = MessageFormat.format(ErrorMessages.Messages.INVALID_US_PORT.getMessage(), portEntry, ExcelConstants.SupplierType.INTERNATIONAL.getValue());
                    errorMessageDetailsList.add(new ErrorMessageDetails(logisticSheet.getSheetName(),  new CellReference(logisticSheet.getRow(rowPortEntryMap.get(ExcelConstants.LogisticPricingFields.US_PORT_OF_ENTRY.getFieldName())).getCell(col)).formatAsString(false), ExcelConstants.OTHER_THAN + Constants.SPACE +  ExcelConstants.USPortOfEntry.DOMESTIC.getValue() , (df.formatCellValue(logisticSheet.getRow(rowPortEntryMap.get(ExcelConstants.LogisticPricingFields.US_PORT_OF_ENTRY.getFieldName())).getCell(col))), ExcelConstants.LogisticPricingFields.US_PORT_OF_ENTRY.getFieldName(), errorMessage));
                }
            }
        }else {
            supplierMilltoPortEntryMap.put(supplierMillName, portEntry);
        }
    }

    private static Boolean isLogisticColumnEmpty(Sheet logisticSheet, int col) {
       if(Boolean.FALSE.equals(checkLogisticSheetEmptyColumn(logisticSheet, col, logisticPricingFieldNameToRowMap, logisticPricingFirstPortEntryFieldNameToRowMap, logisticPricingFirstPortEntryFieldNameToRowMismatchMap, logisticPricingFirstPortEntryIncoterm1FieldNameToRowMap, logisticPricingFirstPortEntryIncoterm2FieldNameToRowMap, logisticPricingSecondPortEntryFieldNameToRowMap, logisticPricingSecondPortEntryFieldNameToRowMismatchMap, logisticPricingSecondPortEntryIncoterm1FieldNameToRowMap,logisticPricingSecondPortEntryIncoterm2FieldNameToRowMap ))){
           return false;
       }
        return true;
    }


    private static Boolean checkLogisticSheetEmptyColumn(Sheet logisticSheet, int col, Map<String, Integer>... rowMapList) {
        for (Map<String, Integer> rowMap : rowMapList) {
            for (Map.Entry<String, Integer> entry : rowMap.entrySet()) {
                if (df.formatCellValue(logisticSheet.getRow(entry.getValue()).getCell(col)) != null && !df.formatCellValue(logisticSheet.getRow(entry.getValue()).getCell(col)).isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }


    // getting Period (Year) from BidQtySheet to CommercialPricing Sheet
    private static void settingPeriodValue(String periodStart) {
        if (periodStart!=null) {
            String[] parts = periodStart.split(Constants.SLASH); // Split the string by '/'
            if (parts.length == 3) {
                String year  = parts[2];
                period = year;
            }
        }
    }

    private static void addUsPortEntryToPortRebateList(String supplierMillName, String portEntry, Map<String, List<Ports>> supplierMillToPorts) {
         if (Objects.equals(portEntry, ExcelConstants.USPortOfEntry.DOMESTIC.getValue())) {
             return;
         }

         // Check if the portEntry already exists in the supplierMillToPorts list for the supplierMillName
         List<Ports> existingPorts = supplierMillToPorts.getOrDefault(supplierMillName, new ArrayList<>());

         // If the portEntry doesn't exist, add it to the portRebateList and supplierMillToPorts
         if (!existingPorts.stream().anyMatch(port -> Objects.equals(port.getPort(), portEntry))) {
             Ports port = new Ports();
             port.setPort(portEntry);
             portRebateList.add(port);
             existingPorts.add(port);
             supplierMillToPorts.put(supplierMillName, existingPorts); // Update the map
         }
    }


    // Method To validate BidVolume From BidSheet to Logistic Sheet
    private static void validateBidVolumeFromBidQtySheetToLogisticSheet(Map<String, BidQtyDetail> millNameToBidQtyDetails, Map<String, Map<String, Long>> mapOfSupplierNameToMapOfGpMillAndVolume, Map<String, Long> mapOfSupplierMillNameToBidVolume, Integer errorCount) {
        LOGGER.info("In excelParsingUtils :: validateBidVolumeFromBidQtySheetToLogisticSheet");
        if (errorCount != errorMessageDetailsList.size()){
            return;
        }

        // if It is Mill_specific, then validating volume based On supplier MillName to GpMillName
        if (mapOfSupplierNameToMapOfGpMillAndVolume != null && !mapOfSupplierNameToMapOfGpMillAndVolume.isEmpty()) {
            validateBidVolumeForMillSpecific(mapOfSupplierNameToMapOfGpMillAndVolume,millNameToBidQtyDetails);
        }

        // if it is LumpSum Then validating Volume based on map key (supplierMilName)
        if (mapOfSupplierMillNameToBidVolume != null && !mapOfSupplierMillNameToBidVolume.isEmpty()) {
            validateBidVolumeForLumpSum(mapOfSupplierMillNameToBidVolume,millNameToBidQtyDetails);
        }
    }


    // Method To validate Bid Volume from BidQty Sheet To Logistic Sheet if it is MILL Specific
    private static void validateBidVolumeForMillSpecific(Map<String, Map<String, Long>> mapOfSupplierNameToMapOfGpMillAndVolume, Map<String, BidQtyDetail> millNameToBidQtyDetails) {

        if (mapOfSupplierNameToMapOfGpMillAndVolume.keySet().equals(millNameToBidQtyDetails.keySet())) {
            for (Map.Entry<String, BidQtyDetail> entry: millNameToBidQtyDetails.entrySet()) {
                String supplierMillName = entry.getKey();
                Map<String, Long> gpMillToVolumeFromLogistic = mapOfSupplierNameToMapOfGpMillAndVolume.get(supplierMillName);

                // Create gpMillToVolumeFromBidQtySheet using enhanced for loop
                Map<String, Long> gpMillToVolumeFromBidQtySheet = new HashMap<>();
                BidQtyDetail bidQtyDetails = millNameToBidQtyDetails.get(supplierMillName);
                for (MillSpecBid mills : bidQtyDetails.getMill_spec_bid()) {
                    gpMillToVolumeFromBidQtySheet.put(mills.getMill(), mills.getBid_vol());
                }

                for (Map.Entry<String, Long> entry1 : gpMillToVolumeFromBidQtySheet.entrySet()) {
                    String gpMill = entry1.getKey();
                    Long volumeFromLogistic = gpMillToVolumeFromLogistic.get(gpMill);
                    Long volumeFromBidQtySheet = gpMillToVolumeFromBidQtySheet.get(gpMill);

                    if (!Objects.equals(volumeFromBidQtySheet, volumeFromLogistic)) {
                        String errorMessage = MessageFormat.format(ErrorMessages.Messages.BID_VOLUME_MISMATCHED_FOR_MILL_SPECIFIC_FOR_TWO_SHEET.getMessage(), supplierMillName, gpMill, volumeFromBidQtySheet, volumeFromLogistic);
                        errorMessageDetailsList.add(new ErrorMessageDetails(ExcelConstants.ResponseRfpExcelSheetNames.LOGISTIC_PRICING.getSheetName(), null, volumeFromBidQtySheet.toString(), volumeFromLogistic.toString(), ResponseRfpExcelHeaders.SUPPLIER_BID_VOLUME.getValue(), errorMessage));
                    }
                }
            }
        }


    }

    // Method To validate Bid Volume from BidQty Sheet To Logistic Sheet if it is LUMP SUM
    private static void validateBidVolumeForLumpSum(Map<String, Long> mapOfSupplierMillNameToBidVolume, Map<String, BidQtyDetail> millNameToBidQtyDetails) {

        if (mapOfSupplierMillNameToBidVolume.keySet().equals(millNameToBidQtyDetails.keySet())) {
            // Both maps have the same keys (SupplierMillsName keys)
            for (Map.Entry<String, BidQtyDetail> entry : millNameToBidQtyDetails.entrySet()) {
                String supplierMillName = entry.getKey();
                // Get the BidQtyDetail for the current SupplierMillsName key
                BidQtyDetail bidQtyDetails = millNameToBidQtyDetails.get(supplierMillName);

                if (!Objects.equals(bidQtyDetails.getBid_vol(), mapOfSupplierMillNameToBidVolume.get(supplierMillName))) {
                    String errorMessage = MessageFormat.format(ErrorMessages.Messages.BID_VOLUME_MISMATCHED_FOR_LUMP_SUM.getMessage(),supplierMillName,bidQtyDetails.getBid_vol(),mapOfSupplierMillNameToBidVolume.get(supplierMillName));
                    errorMessageDetailsList.add(new ErrorMessageDetails(ExcelConstants.ResponseRfpExcelSheetNames.LOGISTIC_PRICING.getSheetName(), null, bidQtyDetails.getBid_vol().toString(), mapOfSupplierMillNameToBidVolume.get(supplierMillName).toString(), ResponseRfpExcelHeaders.SUPPLIER_BID_VOLUME.getValue(),errorMessage));
                }

            }
        }
    }

}
