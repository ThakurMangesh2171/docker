package com.vassarlabs.gp.utils;

import com.vassarlabs.gp.constants.Constants;
import com.vassarlabs.gp.constants.ErrorMessages;
import com.vassarlabs.gp.constants.ExcelConstants;
import com.vassarlabs.gp.exception.FileParsingException;
import com.vassarlabs.gp.pojo.Mills;
import com.vassarlabs.gp.pojo.ResponseRfpJson.*;
import com.vassarlabs.gp.pojo.ResponseRfpJson.MetadataPojo.SupplierMetadata;
import com.vassarlabs.gp.pojo.SupplierExcelData;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.springframework.beans.factory.annotation.Value;

import java.io.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;


public class ExcelGenerateUtils {
    public static final DataFormatter df = new DataFormatter();

    private static final Logger LOGGER = LogManager.getLogger(ExcelGenerateUtils.class);

    public static Boolean isMillSpecific = Boolean.FALSE;

    @Value("${responseRfp.excel.path}")
    private String responseRfpExcelPath;

    //Method to generate Response Rfp Excel from the Response RFP Json
    public static String generateExcelFromResponseRfpJson(String responseRfpId, RfpJsonTemplate rfpJsonTemplate, String responseRfpExcelPath, String excelTemplateName, SupplierExcelData supplierExcelMetaData, List<Mills> millsList) throws FileNotFoundException {
        LOGGER.info("in ExcelGenerateUtils :: generateExcelFromResponseRfpJson");
        //Get the Template and Insert Json Details and Save as new File
        String excelTemplatePath = responseRfpExcelPath + excelTemplateName + Constants.UNDER_SCORE + supplierExcelMetaData.getCommodity() + Constants.EXCEL;
        //New File Name
        String generatedExcelPath = responseRfpExcelPath + responseRfpId + Constants.UNDER_SCORE + supplierExcelMetaData.getSupplierName() + Constants.UNDER_SCORE + Constants.UPDATED_VERSION + Constants.EXCEL;


        try (InputStream inputStream = new FileInputStream(new File(excelTemplatePath));
             Workbook workbook = WorkbookFactory.create(inputStream)) {
            //Populates the  given data in Template
            //TODO In Below Function throwing errors so it will be catched or it will be thrown ?
            populateDataInExcelUsingJson(workbook,rfpJsonTemplate,supplierExcelMetaData,millsList);
            // Save the file to outPutPath
            Utils.saveWorkbookToExcel(workbook, generatedExcelPath);
            return generatedExcelPath;
        } catch (IOException e) {
            e.printStackTrace();
            throw new FileNotFoundException(ErrorMessages.FILE_NOT_FOUND);
        } catch (DecoderException e) {
            e.printStackTrace();
            throw new FileParsingException(ErrorMessages.DATA_DECODING_ERROR);
        }
    }

    //Method to Insert Data in Excel from Response RFP Json
    private static void populateDataInExcelUsingJson(Workbook workbook, RfpJsonTemplate rfpJsonTemplate, SupplierExcelData supplierExcelMetaData, List<Mills> millsList) throws DecoderException, FileNotFoundException {

        Sheet logisticSheet = workbook.getSheet(ExcelConstants.ResponseRfpExcelSheetNames.LOGISTIC_PRICING.getSheetName());
        Sheet bidQtySheet = workbook.getSheet(ExcelConstants.ResponseRfpExcelSheetNames.BID_DETAILS.getSheetName());
        Sheet commercialPricingSheet = workbook.getSheet(ExcelConstants.ResponseRfpExcelSheetNames.COMMERCIAL_PRICING.getSheetName());
        //Check if All sheets are present in Excel
        if(bidQtySheet==null){
            throw new FileNotFoundException(MessageFormat.format(ErrorMessages.SHEET_NOT_FOUND_ERROR,ExcelConstants.ResponseRfpExcelSheetNames.BID_DETAILS.getSheetName()));
        }
        if(logisticSheet==null){
            throw new FileNotFoundException(MessageFormat.format(ErrorMessages.SHEET_NOT_FOUND_ERROR,ExcelConstants.ResponseRfpExcelSheetNames.LOGISTIC_PRICING.getSheetName()));
        }
        if(commercialPricingSheet==null){
            throw new FileNotFoundException(MessageFormat.format(ErrorMessages.SHEET_NOT_FOUND_ERROR,ExcelConstants.ResponseRfpExcelSheetNames.COMMERCIAL_PRICING.getSheetName()));
        }


        //Insert Bid Qty Details in sheet
        // map Of SupplierMillName to List Of Mill specific Bid for Mill_specific
        Map<String, List<MillSpecBid>> mapOfSupplierMillsToBidQty = new HashMap<>();
        // map of Supplier Mill Name to Volume for LumpSum
        Map<String, Long> mapOfSupplierMillNameToVolume = new HashMap<>();


        // Need to check for the sheet are there in Excel or Not

        String supplierName = rfpJsonTemplate.getSupplier();
        List<SupplierMills> supplierMillsList = rfpJsonTemplate.getSupplier_mills();
        PricingDetail pricingDetail = new PricingDetail();

        //Iterating Supplier Mills for Preparing Map of MillName to Bid Details
        Map<String, BidQtyDetail> millNameToBidQtyDetails = new HashMap<>();
        for (SupplierMills supplierMill : supplierMillsList) {
            millNameToBidQtyDetails.put(supplierMill.getSupplier_mill(), supplierMill.getBid_qty_detail());
            pricingDetail = supplierMill.getPricing_detail();
        }



        // storing BidQtyDetails in Excel BidQty Sheet
        if (!millNameToBidQtyDetails.isEmpty()) {
            populateDataInBidQtyDetailsSheet(millNameToBidQtyDetails, bidQtySheet, supplierName, workbook, mapOfSupplierMillNameToVolume , mapOfSupplierMillsToBidQty,supplierExcelMetaData,millsList);
            populateDataInLogisticSheet(millNameToBidQtyDetails, rfpJsonTemplate, logisticSheet, workbook, mapOfSupplierMillNameToVolume , mapOfSupplierMillsToBidQty);
        }

        //Inserting Data in Commercial Pricing Sheet
        if (pricingDetail!=null){
            populateDataInCommercialSheet(workbook,commercialPricingSheet,pricingDetail);
        }
    }

    private static void populateDataInBidQtyDetailsSheet(Map<String, BidQtyDetail> millNameToBidQtyDetails, Sheet bidQtySheet, String supplierName, Workbook workbook, Map<String, Long> mapOfSupplierMillNameToVolume, Map<String, List<MillSpecBid>> mapOfSupplierMillToMillSpecBid, SupplierExcelData supplierExcelMetaData, List<Mills> millsList) throws DecoderException {


        //Supplier Name
        Utils.setHeaderValueInBidQtySheet(workbook, bidQtySheet, new CellReference(ExcelConstants.BID_QTY_SHEET_HEADERS_VALUES_CELL_REFERENCE.get(ExcelConstants.ResponseRfpExcelHeaders.SUPPLIER_NAME.getValue())), supplierExcelMetaData.getSupplierName());
        //contact Email
        Utils.setHeaderValueInBidQtySheet(workbook, bidQtySheet, new CellReference(ExcelConstants.BID_QTY_SHEET_HEADERS_VALUES_CELL_REFERENCE.get(ExcelConstants.ResponseRfpExcelHeaders.CONTACT_EMAIL.getValue())), supplierExcelMetaData.getEmail());
        //Rfp Number
        Utils.setHeaderValueInBidQtySheet(workbook, bidQtySheet, new CellReference(ExcelConstants.BID_QTY_SHEET_HEADERS_VALUES_CELL_REFERENCE.get(ExcelConstants.ResponseRfpExcelHeaders.RFP_NUMBER.getValue())), supplierExcelMetaData.getRfpNumber());
        //Due Date
        Utils.setHeaderValueInBidQtySheet(workbook, bidQtySheet, new CellReference(ExcelConstants.BID_QTY_SHEET_HEADERS_VALUES_CELL_REFERENCE.get(ExcelConstants.ResponseRfpExcelHeaders.DUE_DATE.getValue())), supplierExcelMetaData.getDueDate());
        //Commodity
        Utils.setHeaderValueInBidQtySheet(workbook, bidQtySheet, new CellReference(ExcelConstants.BID_QTY_SHEET_HEADERS_VALUES_CELL_REFERENCE.get(ExcelConstants.ResponseRfpExcelHeaders.FIBER_TYPE.getValue())), supplierExcelMetaData.getCommodity());
       //Contract Term
        if(supplierExcelMetaData.getContractTerm()!=null) {
            Utils.setHeaderValueInBidQtySheet(workbook, bidQtySheet, new CellReference(ExcelConstants.BID_QTY_SHEET_HEADERS_VALUES_CELL_REFERENCE.get(ExcelConstants.ResponseRfpExcelHeaders.CONTRACT_TERM.getValue())), supplierExcelMetaData.getContractTerm().toString());
        }



        Collection<BidQtyDetail> bidQtyDetailsCollection = millNameToBidQtyDetails.values();

        //If No Data to insert then return
        if (bidQtyDetailsCollection.isEmpty()) {
            return;
        }

        //Inserting Data which is Not specific to Supplier Mill
        BidQtyDetail bidQtyDetail = bidQtyDetailsCollection.iterator().next();

        //bid type
        Utils.setHeaderValueInBidQtySheet(workbook,bidQtySheet,new CellReference(ExcelConstants.COLUMN_NAME_TO_CELL_REFERENCE.get(ExcelConstants.ResponseRfpExcelHeaders.BID_TYPE.getValue())), bidQtyDetail.getBid_type());
        //Bid volume

        //Period Start
        Utils.setHeaderValueInBidQtySheet(workbook,bidQtySheet,new CellReference(ExcelConstants.COLUMN_NAME_TO_CELL_REFERENCE.get(ExcelConstants.ResponseRfpExcelHeaders.BEGINNING_SUPPLIER_PERIOD.getValue())), bidQtyDetail.getPeriod_start());

        //Period end
        Utils.setHeaderValueInBidQtySheet(workbook,bidQtySheet,new CellReference(ExcelConstants.COLUMN_NAME_TO_CELL_REFERENCE.get(ExcelConstants.ResponseRfpExcelHeaders.ENDING_SUPPLIER_PERIOD.getValue())), bidQtyDetail.getPeriod_end());

        //bid volume variance %
        if (bidQtyDetail.getBid_vol_variance_pct()!=null){
            Utils.setHeaderValueInBidQtySheet(workbook,bidQtySheet,new CellReference(ExcelConstants.COLUMN_NAME_TO_CELL_REFERENCE.get(ExcelConstants.ResponseRfpExcelHeaders.BID_VOLUME_VARIANCE.getValue())), Utils.getPercentageFormat(String.valueOf(bidQtyDetail.getBid_vol_variance_pct())));
        }


        //Data Specific to Supplier Mill

        //Populating mapOfSupplierMillToMillSpecBid and mapOfSupplierMillNameToVolume Maps
        for (Map.Entry<String,BidQtyDetail> millNameToBidDetails: millNameToBidQtyDetails.entrySet()) {
            mapOfSupplierMillToMillSpecBid.put(millNameToBidDetails.getKey(),millNameToBidDetails.getValue().getMill_spec_bid());
            mapOfSupplierMillNameToVolume.put(millNameToBidDetails.getKey(),millNameToBidDetails.getValue().getBid_vol());
        }


        // If it is need to create Map of <String,Mills> from List<Mills>
        Map<String,Mills> mapOfGpMillToGpMillDetails = getGpMillToGpMillsDetails(millsList);


        // If the Bid_type is Mill_specific then set the gpMill and supplier Mill in Mill Specific Section
        if (bidQtyDetail.getBid_type() != null && Objects.equals(bidQtyDetail.getBid_type(), Constants.BidType.MILL_SPECIFIC.getValue())) {
            isMillSpecific = Boolean.TRUE;
            settingBidQtyDetailsForMillSpecific(workbook, bidQtySheet, mapOfSupplierMillToMillSpecBid,mapOfGpMillToGpMillDetails);
        }

        // if Bid_type is lumpSum then Populate Lump Sum Specific Section
        if (bidQtyDetail.getBid_type()!= null && Objects.equals(bidQtyDetail.getBid_type(), Constants.BidType.LUMP_SUM.getValue())) {
            setBidQtyDetailsForLumpSum(workbook,bidQtySheet,mapOfSupplierMillNameToVolume);
        }


    }

    // Methode To Preparing map of GpMillToGpMillsDetails Using GpMillsList
    private static Map<String, Mills> getGpMillToGpMillsDetails(List<Mills> millsList) {
        Map<String,Mills>mapOfGpMillsToGpMillsDetails = new HashMap<>();

        if (millsList == null || millsList.isEmpty()){
            return mapOfGpMillsToGpMillsDetails;
        }

        for (Mills mill:millsList) {
            mapOfGpMillsToGpMillsDetails.put(mill.getMillName(),mill);
        }
        return mapOfGpMillsToGpMillsDetails;
    }


    //Setting cell Value In Excel
    public static void setCellValueInExcel(Object cellValue, Workbook workbook, Sheet sheet, Integer rowIndex, Integer columnIndex, Boolean alignRight, String cellColourCode) throws DecoderException {

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


        // Set horizontal alignment to center
        cellStyle.setAlignment(HorizontalAlignment.CENTER);

        // Set vertical alignment to center
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        if (Boolean.TRUE.equals(alignRight))
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
        if(cellValue!=null){
            cell.setCellValue(cellValue.toString());
        }

    }

    private static Integer getGpMillPresentRowNumber(Sheet bidQtySheet, Integer startRow, Integer endRow, String targetGpMillName, Integer colNum) {
        for (int i = startRow; i < endRow; i++) {
            Row row = bidQtySheet.getRow(i);
            if (row != null) {
                // Assuming the GpMillName cell is in a specific column (e.g., column 0)
                Cell gpMillNameCell = row.getCell(colNum); // Modify the column index as needed

                if (gpMillNameCell != null && df.formatCellValue(gpMillNameCell) != null) {
                    String gpMillName = df.formatCellValue(gpMillNameCell);
                    // Assuming that you want to match GpMillName case-insensitively
                    if (gpMillName.equalsIgnoreCase(targetGpMillName)) {
                        return i; // Return the row number if the GpMillName matches
                    }
                }
            }
        }
        return null; // Return null if the GpMillName is not found
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
                if (regex.isPresent() && !ExcelParsingUtils.validateRegex((df.formatCellValue(cell).replaceAll(Constants.SPACE, Constants.EMPTY_STRING).replaceAll(Constants.COMMA, Constants.EMPTY_STRING)), regex.get())) {
                    return false;
                }
            }
        }
        return true;
    }


    // Setting The GpMill,SupplierMill,SupplierMillVolume for Mill_Specific
    private static void settingBidQtyDetailsForMillSpecific(Workbook workbook, Sheet bidQtySheet, Map<String, List<MillSpecBid>> mapOfSupplierMillNameToMillSpecBid, Map<String, Mills> mapOfGpMillToGpMillDetails) throws DecoderException {
        Long gpMillExpectedAnnualVolume = 0L;
        Map<String,Long> supplierMillToTotalMillVolume = new HashMap<>();

        //Preparing the Set of Gp Mills from Mill Spec bid
        Set<String> setOfGpMills = new HashSet<>();
        for (Map.Entry<String,List<MillSpecBid>> supplierMillNameToMillSpecificBid : mapOfSupplierMillNameToMillSpecBid.entrySet()) {
            if(supplierMillNameToMillSpecificBid.getValue()==null){
                continue;
            }
            //Set of Gp Mill Names
            Set<String> gpMillsFromThisEntry = supplierMillNameToMillSpecificBid.getValue().stream()
                    .map(MillSpecBid::getMill)
                    .collect(Collectors.toSet());

            // Add the extracted Gp Mill Names to the setOfGpMills
            setOfGpMills.addAll(gpMillsFromThisEntry);

        }


        Integer millNameColumnIndex = ExcelConstants.BID_DETAILS_MAP.get(ExcelConstants.ResponseRfpExcelHeaders.GP_MILL_COL_NUMBER.getValue());
        Integer stateColumnIndex = ExcelConstants.BID_DETAILS_MAP.get(ExcelConstants.ResponseRfpExcelHeaders.GP_MILL_STATE_COL.getValue());
        Integer expectedAnnualVolColIndex = ExcelConstants.BID_DETAILS_MAP.get(ExcelConstants.ResponseRfpExcelHeaders.GP_MILL_EXPECTED_ANNUAL_VOLUME_COL_NUM.getValue());

        Integer startingGpMillRowIndex = ExcelConstants.BID_DETAILS_MAP.get(ExcelConstants.ResponseRfpExcelHeaders.GP_MILL_START_ROW.getValue());
        Integer startColumnIndex = ExcelConstants.BID_DETAILS_MAP.get(ExcelConstants.ResponseRfpExcelHeaders.GP_MILL_COL_NUMBER.getValue());
        Integer endColumnIndex = Constants.END_COLUMN_FOR_BORDER;

        //This function will insert specified number of rows in between the rows that specified(i.e it will insert no.of mills +1 rows in between Gp mill start row and 70)
        bidQtySheet.shiftRows(ExcelConstants.BID_DETAILS_MAP.get(ExcelConstants.ResponseRfpExcelHeaders.GP_MILL_START_ROW.getValue()), 70, setOfGpMills.size() + 1, false, true);


        //Inserting Gp Mills and applying Style to cells (border, bg color and Font family)
        for (String gpMillName : setOfGpMills) {
            Utils.setBorderToCell(workbook, startingGpMillRowIndex, startColumnIndex, endColumnIndex, ExcelConstants.CELL_VALUE_COLOUR_CODE);
            //Setting GpMillName
            setCellValueInExcel(gpMillName, workbook, bidQtySheet, startingGpMillRowIndex, millNameColumnIndex, Boolean.FALSE, ExcelConstants.CELL_HEADER_COLOUR_CODE);
            if (mapOfGpMillToGpMillDetails.get(gpMillName) != null && mapOfGpMillToGpMillDetails.get(gpMillName).getState()!=null && mapOfGpMillToGpMillDetails.get(gpMillName).getExpectedAnnualVolume()!=null){
                String state = mapOfGpMillToGpMillDetails.get(gpMillName).getState();
                Long expectedAnnualVolumeOfGpMill = mapOfGpMillToGpMillDetails.get(gpMillName).getExpectedAnnualVolume();
                gpMillExpectedAnnualVolume += expectedAnnualVolumeOfGpMill;
                //setting Mill_stateName
                setCellValueInExcel(state, workbook, bidQtySheet, startingGpMillRowIndex, stateColumnIndex, Boolean.FALSE, ExcelConstants.CELL_HEADER_COLOUR_CODE);
                //setting Mill_volume
                setCellValueInExcel(expectedAnnualVolumeOfGpMill, workbook, bidQtySheet, startingGpMillRowIndex, expectedAnnualVolColIndex, Boolean.FALSE, ExcelConstants.CELL_HEADER_COLOUR_CODE);
            }
            startingGpMillRowIndex++;
        }

        //Adding Total At end of GpMills
        Utils.setBorderToCell(workbook, startingGpMillRowIndex, startColumnIndex, endColumnIndex, ExcelConstants.CELL_HEADER_COLOUR_CODE);
        Utils.setCellValueWithBorderInExcel(workbook, bidQtySheet, new CellReference(bidQtySheet.getRow(startingGpMillRowIndex).getCell(millNameColumnIndex)), ExcelConstants.ResponseRfpExcelHeaders.TOTAL_BID_VOLUME_COL_NUM.getValue(), Constants.EMPTY_STRING, Boolean.FALSE, ExcelConstants.CELL_HEADER_COLOUR_CODE);
        //setting gpMillTotalVolume
        Utils.setCellValueWithBorderInExcel(workbook, bidQtySheet, new CellReference(bidQtySheet.getRow(startingGpMillRowIndex).getCell(millNameColumnIndex+2)), String.valueOf(gpMillExpectedAnnualVolume), Constants.EMPTY_STRING, Boolean.FALSE, ExcelConstants.CELL_HEADER_COLOUR_CODE);



        //Todo need to calculate total volume and set

        setSupplierMillNameAndVolumeForMillSpecific(workbook,bidQtySheet,mapOfSupplierMillNameToMillSpecBid,startingGpMillRowIndex,millNameColumnIndex,supplierMillToTotalMillVolume);
        //Setting SupplierColumn A Total Volume
        Utils.setCellValueWithBorderInExcel(workbook, bidQtySheet, new CellReference(bidQtySheet.getRow(startingGpMillRowIndex).getCell(millNameColumnIndex+4)), String.valueOf(supplierMillToTotalMillVolume.get(ExcelConstants.supplierMillA)), Constants.EMPTY_STRING, Boolean.FALSE, ExcelConstants.CELL_HEADER_COLOUR_CODE);
        //Setting SupplierColumn B Total Volume
        Utils.setCellValueWithBorderInExcel(workbook, bidQtySheet, new CellReference(bidQtySheet.getRow(startingGpMillRowIndex).getCell(millNameColumnIndex+6)), String.valueOf(supplierMillToTotalMillVolume.get(ExcelConstants.supplierMillB)), Constants.EMPTY_STRING, Boolean.FALSE, ExcelConstants.CELL_HEADER_COLOUR_CODE);


    }

    //Setting Supplier Mill Name and bid volume for Mill Specific
    private static void setSupplierMillNameAndVolumeForMillSpecific(Workbook workbook, Sheet bidQtySheet, Map<String, List<MillSpecBid>> mapOfSupplierMillNameToMillSpecBid, Integer startingGpMillRowIndex, Integer millNameColumnIndex, Map<String, Long> supplierMillToTotalMillVolume) throws DecoderException {

        Long supplierMillATotalVolume = 0L;
        Long supplierMillBTotalVolume = 0L;
        supplierMillToTotalMillVolume.put(ExcelConstants.supplierMillA,supplierMillATotalVolume);
        supplierMillToTotalMillVolume.put(ExcelConstants.supplierMillB,supplierMillBTotalVolume);

        //Setting Supplier Mill Name and Volume Data
        for (Map.Entry<String,List<MillSpecBid>> supplierMillNameToMillSpecBid : mapOfSupplierMillNameToMillSpecBid.entrySet()) {
            List<MillSpecBid> millSpecBidList = supplierMillNameToMillSpecBid.getValue();
            if(millSpecBidList==null || millSpecBidList.isEmpty()){
                continue;
            }

            //Iterate the millSpecific List
            for (MillSpecBid millSpecBid : millSpecBidList) {
                String gpMillName = millSpecBid.getMill();
                //Fetching current Gp Mill ROw  Number For Inserting Supplier Mill Name and Volume in the Same row
                Integer rowNumberOfGpMill = getGpMillPresentRowNumber(bidQtySheet, ExcelConstants.BID_DETAILS_MAP.get(ExcelConstants.ResponseRfpExcelHeaders.GP_MILL_START_ROW.getValue()), startingGpMillRowIndex, gpMillName, millNameColumnIndex);

                if (rowNumberOfGpMill != null) {
                    // if Supplier Mill A Column is not filled adding else adding in Supplier Mill B column
                    if (!checkIsCellEmptyAndValidateRegex(new CellReference(rowNumberOfGpMill, ExcelConstants.BID_DETAILS_MAP.get(ExcelConstants.ResponseRfpExcelHeaders.SUPPLIER_MILL_A.getValue())), bidQtySheet, Optional.empty())) {
                        setCellValueInExcel(supplierMillNameToMillSpecBid.getKey(), workbook, bidQtySheet, rowNumberOfGpMill, ExcelConstants.BID_DETAILS_MAP.get(ExcelConstants.ResponseRfpExcelHeaders.SUPPLIER_MILL_A.getValue()), Boolean.FALSE, ExcelConstants.CELL_VALUE_COLOUR_CODE);
                        setCellValueInExcel(millSpecBid.getBid_vol(), workbook, bidQtySheet, rowNumberOfGpMill, ExcelConstants.BID_DETAILS_MAP.get(ExcelConstants.ResponseRfpExcelHeaders.SUPPLIER_MILL_A_VOLUME.getValue()), Boolean.FALSE, ExcelConstants.CELL_VALUE_COLOUR_CODE);
                        supplierMillATotalVolume += millSpecBid.getBid_vol();
                        supplierMillToTotalMillVolume.put(ExcelConstants.supplierMillA,supplierMillATotalVolume);

                    } else {
                        setCellValueInExcel(supplierMillNameToMillSpecBid.getKey(), workbook, bidQtySheet, rowNumberOfGpMill, ExcelConstants.BID_DETAILS_MAP.get(ExcelConstants.ResponseRfpExcelHeaders.SUPPLIER_MILL_B.getValue()), Boolean.FALSE, ExcelConstants.CELL_VALUE_COLOUR_CODE);
                        setCellValueInExcel(millSpecBid.getBid_vol(), workbook, bidQtySheet, rowNumberOfGpMill, ExcelConstants.BID_DETAILS_MAP.get(ExcelConstants.ResponseRfpExcelHeaders.SUPPLIER_MILL_B_VOLUME.getValue()), Boolean.FALSE, ExcelConstants.CELL_VALUE_COLOUR_CODE);
                        supplierMillBTotalVolume += millSpecBid.getBid_vol();
                        supplierMillToTotalMillVolume.put(ExcelConstants.supplierMillB,supplierMillBTotalVolume);
                    }
                }
            }
        }
    }

    //Method to Populate Date LUMP Sum Section of Bid Qty Sheet
    private static void setBidQtyDetailsForLumpSum(Workbook workbook, Sheet bidQtySheet, Map<String, Long> mapOfSupplierMillNameToVolume) throws DecoderException {
        Integer lumpSumDetailsRow = ExcelConstants.LUMP_SUM_DETAILS_ROW_NUMBER;
        for (Map.Entry<String,Long> supplierMillNameToBidVol : mapOfSupplierMillNameToVolume.entrySet()) {
            if (!checkIsCellEmptyAndValidateRegex(new CellReference(lumpSumDetailsRow, ExcelConstants.LUMP_SUM_HEADER_TO_COLUMN_NUMBER.get(ExcelConstants.ResponseRfpExcelHeaders.SUPPLIER_MILL_A.getValue())), bidQtySheet, Optional.empty())) {
                setCellValueInExcel(supplierMillNameToBidVol.getKey(), workbook, bidQtySheet, lumpSumDetailsRow, ExcelConstants.LUMP_SUM_HEADER_TO_COLUMN_NUMBER.get(ExcelConstants.ResponseRfpExcelHeaders.SUPPLIER_MILL_A.getValue()), Boolean.FALSE, ExcelConstants.CELL_VALUE_COLOUR_CODE);
                setCellValueInExcel(supplierMillNameToBidVol.getValue(), workbook, bidQtySheet, lumpSumDetailsRow, ExcelConstants.LUMP_SUM_HEADER_TO_COLUMN_NUMBER.get(ExcelConstants.ResponseRfpExcelHeaders.SUPPLIER_MILL_A_VOLUME.getValue()), Boolean.FALSE, ExcelConstants.CELL_VALUE_COLOUR_CODE);
            } else {
                setCellValueInExcel(supplierMillNameToBidVol.getKey(), workbook, bidQtySheet, lumpSumDetailsRow, ExcelConstants.LUMP_SUM_HEADER_TO_COLUMN_NUMBER.get(ExcelConstants.ResponseRfpExcelHeaders.SUPPLIER_MILL_B.getValue()), Boolean.FALSE, ExcelConstants.CELL_VALUE_COLOUR_CODE);
                setCellValueInExcel(supplierMillNameToBidVol.getValue(), workbook, bidQtySheet, lumpSumDetailsRow, ExcelConstants.LUMP_SUM_HEADER_TO_COLUMN_NUMBER.get(ExcelConstants.ResponseRfpExcelHeaders.SUPPLIER_MILL_B_VOLUME.getValue()), Boolean.FALSE, ExcelConstants.CELL_VALUE_COLOUR_CODE);
            }
        }
        //Todo if Total is Not Populating Automatically need to set Manually
    }

    private static void populateDataInLogisticSheet(Map<String, BidQtyDetail> millNameToBidQtyDetails, RfpJsonTemplate rfpJsonTemplate, Sheet logisticSheet, Workbook workbook,  Map<String, Long> mapOfSupplierMillNameToVolume, Map<String, List<MillSpecBid>> mapOfSupplierMillsToBidQty) throws DecoderException {

        //Starting column of Logistic sheet for maintaining Index of Column Numbers
        Integer columnIndex = ExcelConstants.LOGISTIC_PRICING_SHEET_STARTING_COLUMN;

        //Map of supplier MillName to Supplier Mills Data
        Map<String, SupplierMetadata> supplierMillsToSupplierMillsMetadataMap = new HashMap<>();

        //Map of supplier mill, Gp Mill, Port Entry details
        Map<String, Map<String, Map<String, PortEntryDetails>>> supplierMillGpMillAndPortEntryDetailsMap = new HashMap<>();

        //Map of supplier mill, Gp Mill, Inland Freight details
        Map<String, Map<String, Map<String, InlandFreight>>> supplierMillGpMillAndInlandFreightDetailsMap = new HashMap<>();

        Map<String, Map<String, Long>> supplierMillToGpMillToSupplierBidVolumeMap = new HashMap<>();

        //Populating All Maps
        populateLogisticMapsFromJson(rfpJsonTemplate, mapOfSupplierMillsToBidQty, supplierMillsToSupplierMillsMetadataMap, supplierMillGpMillAndPortEntryDetailsMap, supplierMillGpMillAndInlandFreightDetailsMap, supplierMillToGpMillToSupplierBidVolumeMap, workbook, logisticSheet);

        for (Map.Entry<String, Map<String, Map<String, PortEntryDetails>>> supplierMillEntry : supplierMillGpMillAndPortEntryDetailsMap.entrySet()) {
            String supplierMillName = supplierMillEntry.getKey();

            // Iterate over the nested map
            for (Map.Entry<String, Map<String, PortEntryDetails>> gpMillEntry : supplierMillEntry.getValue().entrySet()) {
                String gpMillName = gpMillEntry.getKey();

                // For filling details of port of entries, If this number is 2 then it is second port of entry (At max there will be only 2)
                int portEntryNumber = 1;

                // Iterate over the last map
                for (Map.Entry<String, PortEntryDetails> portEntryEntry : gpMillEntry.getValue().entrySet()) {
                    String portEntry = portEntryEntry.getKey();

                    if(portEntryNumber == 1){
                        fillLogisticSheet(workbook,logisticSheet, supplierMillName, gpMillName, portEntry, columnIndex, isMillSpecific ,supplierMillsToSupplierMillsMetadataMap, supplierMillGpMillAndPortEntryDetailsMap, supplierMillGpMillAndInlandFreightDetailsMap, ExcelConstants.LOGISTIC_PRICING_FIELDNAME_TO_ROW_MAP, ExcelConstants.LOGISTIC_PRICING_FIRST_PORT_ENTRY_FIELDNAME_TO_ROW_MAP, ExcelConstants.LOGISTIC_PRICING_FIRST_PORT_ENTRY_FIELDNAME_TO_ROW_MISMATCH_MAP, ExcelConstants.LOGISTIC_PRICING_FIRST_PORT_ENTRY_INCOTERM_1_FIELDNAME_TO_ROW_MAP, ExcelConstants.LOGISTIC_PRICING_FIRST_PORT_ENTRY_INCOTERM_2_FIELDNAME_TO_ROW_MAP,  mapOfSupplierMillNameToVolume, supplierMillToGpMillToSupplierBidVolumeMap);
                    }else{
                        fillLogisticSheet(workbook,logisticSheet, supplierMillName, gpMillName, portEntry, columnIndex, isMillSpecific ,supplierMillsToSupplierMillsMetadataMap, supplierMillGpMillAndPortEntryDetailsMap, supplierMillGpMillAndInlandFreightDetailsMap, ExcelConstants.LOGISTIC_PRICING_FIELDNAME_TO_ROW_MAP, ExcelConstants.LOGISTIC_PRICING_SECOND_PORT_ENTRY_FIELDNAME_TO_ROW_MAP, ExcelConstants.LOGISTIC_PRICING_SECOND_PORT_ENTRY_FIELDNAME_TO_ROW_MISMATCH_MAP,  ExcelConstants.LOGISTIC_PRICING_SECOND_PORT_ENTRY_INCOTERM_1_FIELDNAME_TO_ROW_MAP, ExcelConstants.LOGISTIC_PRICING_SECOND_PORT_ENTRY_INCOTERM_2_FIELDNAME_TO_ROW_MAP, mapOfSupplierMillNameToVolume, supplierMillToGpMillToSupplierBidVolumeMap);
                    }
                    portEntryNumber++;
                }
                columnIndex++;
            }
        }
    }



    //Method to Populate Data In commercial Pricing Sheet
    private static void populateDataInCommercialSheet(Workbook workbook, Sheet commercialPricingSheet, PricingDetail pricingDetail) throws DecoderException {
        //Payment term , Monthly Negotiation and Collars Section
        setBasicCommercialDetailsInExcel(workbook,commercialPricingSheet,pricingDetail);
        //Index Details
        //TODO size of Period Details list will always be 1 and index Details list also 1
        List<IndexDetails> indexDetailsList = pricingDetail.getPeriod_detail().get(0).getIndex_details();
        IndexDetails indexDetails = null;
        if(indexDetailsList!=null && !indexDetailsList.isEmpty()){
            indexDetails = indexDetailsList.get(0);
        }
        //1. Index Less
        setMechanismDetailsForIndex(workbook,commercialPricingSheet,pricingDetail,indexDetails);
        //2. Index With Movement
        setMechanismDetailsForMovement(workbook,commercialPricingSheet,pricingDetail,indexDetails);
        //3. Hybrid or Others
        setMechanismDetailsForHybridOrOthers(workbook,commercialPricingSheet,pricingDetail);

        //Discount Allowances (Price Tier and Volume Tier)
        DiscountsAllowances discountsAllowances = pricingDetail.getDiscounts_allowances();
        if(discountsAllowances!=null){
            //Price Tier Discounts
            setPriceTierDetails(workbook,commercialPricingSheet,discountsAllowances.getPrice_tier_discounts());
            //Volume Tier Discounts
            setVolumeTierDetails(workbook,commercialPricingSheet,pricingDetail,discountsAllowances.getVolume_tier_discounts());
        }

    }


    // Setting Commercial Details Basic Details Payment Terms, Monthly negotiation and Collars (Price floor, ceiling and period) Section
    private static void setBasicCommercialDetailsInExcel(Workbook workbook, Sheet commercialPricingSheet, PricingDetail pricingDetail) throws DecoderException {
        // Setting PaymentTerm
        CellReference cellReference = new CellReference(ExcelConstants.PRICING_DETAILS_CELL_REFERENCE_FOR_COMMERCIAL_PRICING.get(ExcelConstants.CommercialSheetFieldNames.PAYMENT_TERM.getFieldName()));
        setCellValueInExcel(pricingDetail.getPayment_term(), workbook, commercialPricingSheet, cellReference.getRow(), (int) cellReference.getCol(), Boolean.FALSE, ExcelConstants.CELL_VALUE_COLOUR_CODE);

        // Setting Monthly negotiation
        cellReference = new CellReference(ExcelConstants.PRICING_DETAILS_CELL_REFERENCE_FOR_COMMERCIAL_PRICING.get(ExcelConstants.CommercialSheetFieldNames.MONTHLY_NEGOTIATION.getFieldName()));
        setCellValueInExcel(pricingDetail.isMonthly_negotiation() ? Constants.YES_STRING : Constants.NO_STRING, workbook, commercialPricingSheet, cellReference.getRow(), (int) cellReference.getCol(), Boolean.FALSE, ExcelConstants.CELL_VALUE_COLOUR_CODE);

        //Setting Price Floor
        if (pricingDetail.getPrice_floor()!=null) {
            cellReference = new CellReference(ExcelConstants.PRICING_DETAILS_CELL_REFERENCE_FOR_COMMERCIAL_PRICING.get(ExcelConstants.CommercialSheetFieldNames.PRICE_FLOOR_ADMT.getFieldName()));
            setCellValueInExcel(Utils.currencyFormat(String.valueOf(pricingDetail.getPrice_floor())), workbook, commercialPricingSheet, cellReference.getRow(), (int) cellReference.getCol(), Boolean.FALSE, ExcelConstants.CELL_VALUE_COLOUR_CODE);
        }

        //Setting Price Ceil
        if (pricingDetail.getPrice_ceil()!=null) {
            cellReference = new CellReference(ExcelConstants.PRICING_DETAILS_CELL_REFERENCE_FOR_COMMERCIAL_PRICING.get(ExcelConstants.CommercialSheetFieldNames.PRICE_CEILING_ADMT.getFieldName()));
            setCellValueInExcel(Utils.currencyFormat(String.valueOf(pricingDetail.getPrice_ceil())), workbook, commercialPricingSheet, cellReference.getRow(), (int) cellReference.getCol(), Boolean.FALSE, ExcelConstants.CELL_VALUE_COLOUR_CODE);
        }

        //Setting Price Floor Period start and End (Price Ceiling/Floor Effective Period)
        cellReference = new CellReference(ExcelConstants.PRICING_DETAILS_CELL_REFERENCE_FOR_COMMERCIAL_PRICING.get(ExcelConstants.CommercialSheetFieldNames.PRICE_CEILING_FLOOR_PERIOD.getFieldName()));
        String priceEffectivePeriod = "";
        if(pricingDetail.getCeil_floor_period_start()!=null){
            priceEffectivePeriod += pricingDetail.getCeil_floor_period_start();
            priceEffectivePeriod += Constants.HYPHEN;
        }
        if(pricingDetail.getCeil_floor_period_end()!=null){
            priceEffectivePeriod += pricingDetail.getCeil_floor_period_end();
        }else {
            //If End Period is not there remove HYPHEN
            priceEffectivePeriod = priceEffectivePeriod.replace(Constants.HYPHEN, Constants.EMPTY_STRING);
        }
        setCellValueInExcel(priceEffectivePeriod, workbook, commercialPricingSheet, cellReference.getRow(), (int) cellReference.getCol(), Boolean.FALSE, ExcelConstants.CELL_VALUE_COLOUR_CODE);

        //Port Rebates
        DiscountsAllowances discountsAllowances = pricingDetail.getDiscounts_allowances();
        if(discountsAllowances!=null && discountsAllowances.getPort_rebates()!=null){
            //Port  rebate
//            if (discountsAllowances.getPort_rebates().getDiscount_val()!=null) {
//                cellReference = new CellReference(ExcelConstants.PORT_REBATE_FIELDS_NAME_VALUES_TO_CELLREFERENCE_MAP.get(ExcelConstants.CommercialSheetFieldNames.PORT_REBATE.getFieldName()));
//                setCellValueInExcel(Utils.currencyFormat(String.valueOf(discountsAllowances.getPort_rebates().getDiscount_val())), workbook, commercialPricingSheet, cellReference.getRow(), (int) cellReference.getCol(), Boolean.FALSE, ExcelConstants.CELL_VALUE_COLOUR_CODE);
//            }

            //Comments
            cellReference = new CellReference(ExcelConstants.PORT_REBATE_FIELDS_NAME_VALUES_TO_CELLREFERENCE_MAP.get(ExcelConstants.CommercialSheetFieldNames.PORT_REBATE_COMMENTS.getFieldName()));
            setCellValueInExcel(discountsAllowances.getPort_rebates().getComments(), workbook, commercialPricingSheet, cellReference.getRow(), (int) cellReference.getCol(), Boolean.FALSE, ExcelConstants.CELL_VALUE_COLOUR_CODE);

        }
    }


    //Method to Populate Index Details if Mechanism Basis is Index
    private static void setMechanismDetailsForIndex(Workbook workbook, Sheet commercialPricingSheet, PricingDetail pricingDetail, IndexDetails indexDetails) throws DecoderException {
        //Pricing Mechanism Index less Discount
        if(indexDetails!=null && pricingDetail.getMechanism_basis().equals(Constants.MECHANISMS.INDEX.getValue()) && Boolean.FALSE.equals(pricingDetail.getIs_movement_based())){
            CellReference cellReference;
            //Discount
            if (indexDetails.getDiscount_pct()!=null) {
                cellReference = new CellReference(ExcelConstants.INDEX_BASED_CELL_REFERENCE_FOR_COMMERCIAL_PRICING.get(ExcelConstants.CommercialSheetFieldNames.DISCOUNT.getFieldName()));
                setCellValueInExcel(Utils.getPercentageFormat(String.valueOf(indexDetails.getDiscount_pct())), workbook, commercialPricingSheet, cellReference.getRow(), (int) cellReference.getCol(), Boolean.FALSE, ExcelConstants.CELL_VALUE_COLOUR_CODE);
            }
            //Index Name
            cellReference = new CellReference(ExcelConstants.INDEX_BASED_CELL_REFERENCE_FOR_COMMERCIAL_PRICING.get(ExcelConstants.CommercialSheetFieldNames.INDEX_NAME.getFieldName()));
            setCellValueInExcel(indexDetails.getIndex(), workbook, commercialPricingSheet, cellReference.getRow(), (int) cellReference.getCol(), Boolean.FALSE, ExcelConstants.CELL_VALUE_COLOUR_CODE);

            //Published Date
            String publishedDate = "";
            if(indexDetails.getRead_week_criteria()!=null){
                publishedDate += indexDetails.getRead_week_criteria()+Constants.SPACE;
            }
            if(indexDetails.getRead_date()!=null){
                publishedDate += indexDetails.getRead_date()+Constants.SPACE;
            }
            if(indexDetails.getRead_day()!=null){
                publishedDate += indexDetails.getRead_day()+Constants.SPACE+ExcelConstants.OF_THE_MONTH ;
            }
            if (Objects.equals(indexDetails.getRead_type(), Constants.GIVEN_DATE)){
                publishedDate = publishedDate.replace(ExcelConstants.OF_THE_MONTH,Constants.EMPTY_STRING);
            }
            if (!publishedDate.isEmpty()) {
                cellReference = new CellReference(ExcelConstants.INDEX_BASED_CELL_REFERENCE_FOR_COMMERCIAL_PRICING.get(ExcelConstants.CommercialSheetFieldNames.INDEX_PUBLISHED_DATE.getFieldName()));
                setCellValueInExcel(publishedDate, workbook, commercialPricingSheet, cellReference.getRow(), (int) cellReference.getCol(), Boolean.FALSE, ExcelConstants.CELL_VALUE_COLOUR_CODE);
            }

            //Additional Adjustment
            if (indexDetails.getAdditional_adjustment()!=null) {
                cellReference = new CellReference(ExcelConstants.INDEX_BASED_CELL_REFERENCE_FOR_COMMERCIAL_PRICING.get(ExcelConstants.CommercialSheetFieldNames.ADDITIONAL_ADJUSTMENT_ADMT.getFieldName()));
                setCellValueInExcel(Utils.currencyFormat(String.valueOf(indexDetails.getAdditional_adjustment())), workbook, commercialPricingSheet, cellReference.getRow(), (int) cellReference.getCol(), Boolean.FALSE, ExcelConstants.CELL_VALUE_COLOUR_CODE);
            }

            //Comments
            cellReference = new CellReference(ExcelConstants.INDEX_BASED_CELL_REFERENCE_FOR_COMMERCIAL_PRICING.get(ExcelConstants.CommercialSheetFieldNames.COMMENTS.getFieldName()));
            setCellValueInExcel(pricingDetail.getComments(), workbook, commercialPricingSheet, cellReference.getRow(), (int) cellReference.getCol(), Boolean.FALSE, ExcelConstants.CELL_VALUE_COLOUR_CODE);
        }
    }

    //Method to Populate Index Details if Mechanism Basis is Movement
    private static void setMechanismDetailsForMovement(Workbook workbook, Sheet commercialPricingSheet, PricingDetail pricingDetail, IndexDetails indexDetails) throws DecoderException {

        //Pricing Mechanism Starting Price With Movement
        if(indexDetails!=null && pricingDetail.getMechanism_basis().equals(Constants.MECHANISMS.INDEX.getValue()) && Boolean.TRUE.equals(pricingDetail.getIs_movement_based())){
            CellReference cellReference;
            //starting price Point
            if (pricingDetail.getInitial_price()!=null) {
                cellReference = new CellReference(ExcelConstants.MOVEMENT_BASED_CELL_REFERENCE_FOR_COMMERCIAL_PRICING.get(ExcelConstants.CommercialSheetFieldNames.STARTING_PRICE_POINT.getFieldName()));
                setCellValueInExcel(Utils.currencyFormat(String.valueOf(pricingDetail.getInitial_price())), workbook, commercialPricingSheet, cellReference.getRow(), (int) cellReference.getCol(), Boolean.FALSE, ExcelConstants.CELL_VALUE_COLOUR_CODE);
            }

            //index
            cellReference = new CellReference(ExcelConstants.MOVEMENT_BASED_CELL_REFERENCE_FOR_COMMERCIAL_PRICING.get(ExcelConstants.CommercialSheetFieldNames.INDEX_NAME.getFieldName()));
            setCellValueInExcel(indexDetails.getIndex(), workbook, commercialPricingSheet, cellReference.getRow(), (int) cellReference.getCol(), Boolean.FALSE, ExcelConstants.CELL_VALUE_COLOUR_CODE);


            //Month over Month Change
            if (pricingDetail.getMovement_change_type()!=null) {
                cellReference = new CellReference(ExcelConstants.MOVEMENT_BASED_CELL_REFERENCE_FOR_COMMERCIAL_PRICING.get(ExcelConstants.CommercialSheetFieldNames.MONTH_OVER_MONTH_CHANGE.getFieldName()));
                setCellValueInExcel(pricingDetail.getMovement_change_type(), workbook, commercialPricingSheet, cellReference.getRow(), (int) cellReference.getCol(), Boolean.FALSE, ExcelConstants.CELL_VALUE_COLOUR_CODE);
            }


            //index published Date
            String indexPublishedDate = "";
            if(pricingDetail.getTime_window()!=null){
                indexPublishedDate += pricingDetail.getTime_window()+Constants.SPACE;
            }
            if(pricingDetail.getTime_window_period()!=null){
                indexPublishedDate += pricingDetail.getTime_window_period()+Constants.SPACE;
            }
            if(indexDetails.getRead_week_criteria()!=null){
                indexPublishedDate += indexDetails.getRead_week_criteria()+Constants.SPACE;
            }
            if(indexDetails.getRead_day()!=null){
                indexPublishedDate += indexDetails.getRead_day()+Constants.SPACE+ExcelConstants.OF_THE_MONTH;
            }
            if(indexDetails.getRead_date()!=null){
                indexPublishedDate += indexDetails.getRead_date();
            }
            if (Objects.equals(indexDetails.getRead_type(), Constants.GIVEN_DATE)){
                indexPublishedDate = indexPublishedDate.replace(ExcelConstants.OF_THE_MONTH,Constants.EMPTY_STRING).replace(Constants.TWO_SPACE,Constants.EMPTY_STRING);
            }
            cellReference = new CellReference(ExcelConstants.MOVEMENT_BASED_CELL_REFERENCE_FOR_COMMERCIAL_PRICING.get(ExcelConstants.CommercialSheetFieldNames.INDEX_PUBLISHED_DATE.getFieldName()));
            setCellValueInExcel(indexPublishedDate, workbook, commercialPricingSheet, cellReference.getRow(), (int) cellReference.getCol(), Boolean.FALSE, ExcelConstants.CELL_VALUE_COLOUR_CODE);


            //Additional Adjustment
            if (indexDetails.getAdditional_adjustment()!=null) {
                cellReference = new CellReference(ExcelConstants.MOVEMENT_BASED_CELL_REFERENCE_FOR_COMMERCIAL_PRICING.get(ExcelConstants.CommercialSheetFieldNames.ADDITIONAL_ADJUSTMENT_ADMT.getFieldName()));
                setCellValueInExcel(Utils.currencyFormat(String.valueOf(indexDetails.getAdditional_adjustment())), workbook, commercialPricingSheet, cellReference.getRow(), (int) cellReference.getCol(), Boolean.FALSE, ExcelConstants.CELL_VALUE_COLOUR_CODE);
            }

            //Comments
            cellReference = new CellReference(ExcelConstants.MOVEMENT_BASED_CELL_REFERENCE_FOR_COMMERCIAL_PRICING.get(ExcelConstants.CommercialSheetFieldNames.COMMENTS.getFieldName()));
            setCellValueInExcel(pricingDetail.getComments(), workbook, commercialPricingSheet, cellReference.getRow(), (int) cellReference.getCol(), Boolean.FALSE, ExcelConstants.CELL_VALUE_COLOUR_CODE);

        }
    }

    //Method to Populate Index Details if Mechanism Basis is Hybrid or Others
    private static void setMechanismDetailsForHybridOrOthers(Workbook workbook, Sheet commercialPricingSheet, PricingDetail pricingDetail) throws DecoderException {
        CellReference cellReference;

        //Hybrid Pricing Mechanism
        if(pricingDetail.getMechanism_basis().equals(Constants.MECHANISMS.HYBRID.getValue()) && pricingDetail.getPricing_alternate_mechanism()!=null){
            String[] values = pricingDetail.getPricing_alternate_mechanism().split(Constants.COMMA);
            if(values.length>=1){
                cellReference = new CellReference(ExcelConstants.HYBRID_BASED_CELL_REFERENCE_FOR_COMMERCIAL_PRICING.get(ExcelConstants.CommercialSheetFieldNames.PART_1.getFieldName()));
                setCellValueInExcel(values[0], workbook, commercialPricingSheet, cellReference.getRow(), (int) cellReference.getCol(), Boolean.FALSE, ExcelConstants.CELL_VALUE_COLOUR_CODE);
            }
            if(values.length>=2){
                cellReference = new CellReference(ExcelConstants.HYBRID_BASED_CELL_REFERENCE_FOR_COMMERCIAL_PRICING.get(ExcelConstants.CommercialSheetFieldNames.PART_2.getFieldName()));
                setCellValueInExcel(values[1], workbook, commercialPricingSheet, cellReference.getRow(), (int) cellReference.getCol(), Boolean.FALSE, ExcelConstants.CELL_VALUE_COLOUR_CODE);
            }
            if(values.length>=3){
                cellReference = new CellReference(ExcelConstants.HYBRID_BASED_CELL_REFERENCE_FOR_COMMERCIAL_PRICING.get(ExcelConstants.CommercialSheetFieldNames.PART_3.getFieldName()));
                setCellValueInExcel(values[2], workbook, commercialPricingSheet, cellReference.getRow(), (int) cellReference.getCol(), Boolean.FALSE, ExcelConstants.CELL_VALUE_COLOUR_CODE);
            }
            //Commets
            cellReference = new CellReference(ExcelConstants.HYBRID_BASED_CELL_REFERENCE_FOR_COMMERCIAL_PRICING.get(ExcelConstants.CommercialSheetFieldNames.COMMENTS.getFieldName()));
            setCellValueInExcel(pricingDetail.getComments(), workbook, commercialPricingSheet, cellReference.getRow(), (int) cellReference.getCol(), Boolean.FALSE, ExcelConstants.CELL_VALUE_COLOUR_CODE);
        }

        //Others Pricing Mechanism
        if(pricingDetail.getMechanism_basis().equals(Constants.MECHANISMS.OTHER.getValue()) && pricingDetail.getPricing_alternate_mechanism()!=null){
            cellReference = new CellReference(ExcelConstants.OTHER_BASED_CELL_REFERENCE_FOR_COMMERCIAL_PRICING.get(ExcelConstants.ResponseRfpExcelHeaders.OTHER.getValue()));
            setCellValueInExcel(pricingDetail.getPricing_alternate_mechanism(), workbook, commercialPricingSheet, cellReference.getRow(), (int) cellReference.getCol(), Boolean.FALSE, ExcelConstants.CELL_VALUE_COLOUR_CODE);
        }
    }


    //Method to Populate price Tier Discounts Data in Commercial Sheet
    private static void setPriceTierDetails(Workbook workbook, Sheet commercialPricingSheet, PriceTierDiscounts priceTierDiscounts) throws DecoderException {
        if (priceTierDiscounts == null) {
            return;
        }
        List<PriceTiers> priceTiersList = priceTierDiscounts.getPrice_tiers();
        CellReference cellReference = new CellReference(ExcelConstants.PRICE_TIER_FIELDS_NAME_VALUES_TO_CELLREFERENCE_MAP.get(ExcelConstants.CommercialSheetFieldNames.TIER_BASED_PRICING_DISCOUNT.getFieldName()));
        setCellValueInExcel(priceTierDiscounts.isIs_tier_based_discount() ? Constants.YES_STRING : Constants.NO_STRING, workbook, commercialPricingSheet, cellReference.getRow(), (int) cellReference.getCol(), Boolean.FALSE, ExcelConstants.CELL_VALUE_COLOUR_CODE);
        if (priceTiersList == null || priceTiersList.isEmpty()) {
            return;
        }
        //comments
        cellReference = new CellReference(ExcelConstants.PRICE_TIER_FIELDS_NAME_VALUES_TO_CELLREFERENCE_MAP.get(ExcelConstants.CommercialSheetFieldNames.TIER_BASED_PRICING_DISCOUNT_COMMENTS.getFieldName()));
        setCellValueInExcel(priceTierDiscounts.getComments(), workbook, commercialPricingSheet, cellReference.getRow(), (int) cellReference.getCol(), Boolean.FALSE, ExcelConstants.CELL_VALUE_COLOUR_CODE);

        //TODO : Price Tiers will be always less than or equal to 3
        for (int i = 0; i < priceTiersList.size(); i++) {
            PriceTiers priceTiers = priceTiersList.get(i);
            if (i == 0) {
                setPriceTierLabelAndRangeAndDisc(workbook, commercialPricingSheet, priceTiers, ExcelConstants.PRICE_TIER_1_FIELDS_NAME_VALUES_TO_CELLREFERENCE_MAP, ExcelConstants.CommercialSheetFieldNames.PRICE_RANGE_ADMT.getFieldName());
            } else if (i == 1) {
                setPriceTierLabelAndRangeAndDisc(workbook, commercialPricingSheet, priceTiers, ExcelConstants.PRICE_TIER_2_FIELDS_NAME_VALUES_TO_CELLREFERENCE_MAP, ExcelConstants.CommercialSheetFieldNames.PRICE_TIER_ADMT.getFieldName());
            } else if (i == 2) {
                setPriceTierLabelAndRangeAndDisc(workbook, commercialPricingSheet, priceTiers, ExcelConstants.PRICE_TIER_3_FIELDS_NAME_VALUES_TO_CELLREFERENCE_MAP, ExcelConstants.CommercialSheetFieldNames.PRICE_TIER_ADMT_2.getFieldName());
            } else {
                break;
            }
        }
    }

    //Method to set Price Tier Label and Range and Discount Percentage
    private static void setPriceTierLabelAndRangeAndDisc(Workbook workbook, Sheet commercialPricingSheet, PriceTiers priceTiers, Map<String, String> priceTierFieldsNameToCellreferenceMap,String priceRangeAdmt) throws DecoderException {
        //Tier category
        CellReference cellReference = new CellReference(priceTierFieldsNameToCellreferenceMap.get(ExcelConstants.CommercialSheetFieldNames.PRICE_TIER_CATEGORY.getFieldName()));
        setCellValueInExcel( priceTiers.getTier_label(), workbook, commercialPricingSheet, cellReference.getRow(), (int) cellReference.getCol(), Boolean.FALSE, ExcelConstants.CELL_VALUE_COLOUR_CODE);


        //TODO :: need to set the Price Range With $(currency)
        //Tier Range
        String priceTierRange = "";
        if(priceTiers.getTier_low()!=null){
            priceTierRange += Utils.currencyFormat(String.valueOf(priceTiers.getTier_low())) +Constants.SPACE + Constants.HYPHEN +Constants.SPACE;
        }
        if(priceTiers.getTier_high()!=null){
            priceTierRange += Utils.currencyFormat(String.valueOf(priceTiers.getTier_high()));
        }else {
            priceTierRange = priceTierRange.replace(Constants.HYPHEN,Constants.EMPTY_STRING).replace(Constants.SPACE,Constants.EMPTY_STRING);
        }
        cellReference = new CellReference(priceTierFieldsNameToCellreferenceMap.get(priceRangeAdmt));
        setCellValueInExcel(priceTierRange.replace(".00",Constants.EMPTY_STRING), workbook, commercialPricingSheet, cellReference.getRow(), (int) cellReference.getCol(), Boolean.FALSE, ExcelConstants.CELL_VALUE_COLOUR_CODE);


        //Discount
        if (priceTiers.getDiscount_type()!=null) {
            cellReference = new CellReference(priceTierFieldsNameToCellreferenceMap.get(ExcelConstants.CommercialSheetFieldNames.DISCOUNT_ADMT.getFieldName()));
            setCellValueInExcel(Objects.equals(priceTiers.getDiscount_type(), Constants.VALUE_STRING) ? Utils.currencyFormat(String.valueOf(priceTiers.getDiscount_val())) : Utils.getPercentageFormat(String.valueOf(priceTiers.getDiscount_pct())) , workbook, commercialPricingSheet, cellReference.getRow(), (int) cellReference.getCol(), Boolean.FALSE, ExcelConstants.CELL_VALUE_COLOUR_CODE);
        }
    }



    //Method to Populate Volume Tier Discounts Data in Commercial Sheet

    private static void setVolumeTierDetails(Workbook workbook, Sheet commercialPricingSheet, PricingDetail pricingDetail, VolumeTierDiscounts volumeTierDiscounts) throws DecoderException {

        //is Volume Tier
        CellReference cellReference = new CellReference(ExcelConstants.VOLUME_TIER_FIELDS_NAME_VALUES_TO_CELLREFERENCE_MAP.get(ExcelConstants.CommercialSheetFieldNames.VOLUME_BASED_PRICING_DISCOUNT.getFieldName()));
        setCellValueInExcel(volumeTierDiscounts.isIs_volume_based_discount() ? Constants.YES_STRING : Constants.NO.toString(), workbook, commercialPricingSheet, cellReference.getRow(), (int) cellReference.getCol(), Boolean.FALSE, ExcelConstants.CELL_VALUE_COLOUR_CODE);

        //Comments
        cellReference = new CellReference(ExcelConstants.VOLUME_TIER_FIELDS_NAME_VALUES_TO_CELLREFERENCE_MAP.get(ExcelConstants.CommercialSheetFieldNames.VOLUME_BASED_REBATE_COMMENTS.getFieldName()));
        setCellValueInExcel(volumeTierDiscounts.getComments(), workbook, commercialPricingSheet, cellReference.getRow(), (int) cellReference.getCol(), Boolean.FALSE, ExcelConstants.CELL_VALUE_COLOUR_CODE);

        List<VolumeTiers> volumeTiersList = volumeTierDiscounts.getVolume_tiers();
        if(volumeTiersList==null || volumeTiersList.isEmpty()) {
            return;
        }

        //TODO : Volume Tiers will be always 1
        VolumeTiers volumeTiers = volumeTiersList.get(0);
        //Volume Based Pricing Method
        cellReference = new CellReference(ExcelConstants.VOLUME_TIER_FIELDS_NAME_VALUES_TO_CELLREFERENCE_MAP.get(ExcelConstants.CommercialSheetFieldNames.VOLUME_BASED_PRICING_PERIOD.getFieldName()));
        setCellValueInExcel(pricingDetail.getVolume_based_period(), workbook, commercialPricingSheet, cellReference.getRow(), (int) cellReference.getCol(), Boolean.FALSE, ExcelConstants.CELL_VALUE_COLOUR_CODE);

        //Volume Tier Category
        cellReference = new CellReference(ExcelConstants.VOLUME_TIER_FIELDS_NAME_VALUES_TO_CELLREFERENCE_MAP.get(ExcelConstants.CommercialSheetFieldNames.VOLUME_TIER_CATEGORY.getFieldName()));
        setCellValueInExcel(volumeTiers.getTier_label(), workbook, commercialPricingSheet, cellReference.getRow(), (int) cellReference.getCol(), Boolean.FALSE, ExcelConstants.CELL_VALUE_COLOUR_CODE);

        //Volume Tier Admt
        //Tier Range
        String volumeTierAdmt = "";
        if(volumeTiers.getTier_low()!=null){
            volumeTierAdmt += volumeTiers.getTier_low() + Constants.HYPHEN;
        }
        if(volumeTiers.getTier_high()!=null){
            volumeTierAdmt += volumeTiers.getTier_low();
        }else {
            volumeTierAdmt = volumeTierAdmt.replace(Constants.HYPHEN,Constants.EMPTY_STRING);
        }
        //Volume Tier Admt
        cellReference = new CellReference(ExcelConstants.VOLUME_TIER_FIELDS_NAME_VALUES_TO_CELLREFERENCE_MAP.get(ExcelConstants.CommercialSheetFieldNames.VOLUME_TIER_ADMT.getFieldName()));
        setCellValueInExcel(volumeTierAdmt, workbook, commercialPricingSheet, cellReference.getRow(), (int) cellReference.getCol(), Boolean.FALSE, ExcelConstants.CELL_VALUE_COLOUR_CODE);

        //Discount
        cellReference = new CellReference(ExcelConstants.VOLUME_TIER_FIELDS_NAME_VALUES_TO_CELLREFERENCE_MAP.get(ExcelConstants.CommercialSheetFieldNames.VOLUME_DISCOUNT_REBATE.getFieldName()));
        setCellValueInExcel(volumeTiers.getDiscount_val()!=null ? Utils.currencyFormat(String.valueOf(volumeTiers.getDiscount_val())) : Utils.getPercentageFormat(String.valueOf(volumeTiers.getDiscount_pct())), workbook, commercialPricingSheet, cellReference.getRow(), (int) cellReference.getCol(), Boolean.FALSE, ExcelConstants.CELL_VALUE_COLOUR_CODE);
     }


     //Method to Prepare the Maps (Supplier Mill to Mill Specific Bid, Supplier Mill to Meta Data , Supplier Mill to Gp Mill to Port Entry Details ,  Supplier Mill to Gp Mill Inland Freight Details, supplierMill To GpMill To SupplierBidVolume
    private static void populateLogisticMapsFromJson(RfpJsonTemplate rfpJsonTemplate, Map<String, List<MillSpecBid>> mapOfSupplierMillsToBidQty, Map<String, SupplierMetadata> supplierMillsToSupplierMillsMetadataMap, Map<String, Map<String, Map<String, PortEntryDetails>>> supplierMillGpMillAndPortEntryDetailsMap, Map<String, Map<String, Map<String, InlandFreight>>> supplierMillGpMillAndInlandFreightDetailsMap, Map<String, Map<String, Long>> supplierMillToGpMillToSupplierBidVolumeMap, Workbook workbook, Sheet logisticSheet) throws DecoderException {
        String supplierMillName;

        fillSupplierMillToGpMillToSupplierBidVolumeMap(mapOfSupplierMillsToBidQty, supplierMillToGpMillToSupplierBidVolumeMap);

        //Iterating Supplier Mills and Populating Maps
        for (SupplierMills supplierMill : rfpJsonTemplate.getSupplier_mills()) {
            supplierMillName = supplierMill.getSupplier_mill();
            //If Supplier Mill Name is Null We cant Populate Maps for This Supplier Mill
            if(supplierMillName==null || supplierMillName.trim().isEmpty()){
                continue;
            }
            FreightDetail freightDetail = supplierMill.getFreight_detail();

            //Adding comments
            CellReference cellReference = new CellReference(ExcelConstants.LOGISTIC_PRICING_COMMENTS_CELL);
            setCellValueInExcel(freightDetail.getComments(), workbook, logisticSheet, cellReference.getRow(), (int)cellReference.getCol(), Boolean.FALSE, ExcelConstants.CELL_VALUE_COLOUR_CODE);

            fillSupplierMillsToSupplierMillsMetadataMap(supplierMill,supplierMillName, supplierMillsToSupplierMillsMetadataMap);
            fillSupplierMillGpMillAndInlandFreightDetailsMap(supplierMillName, supplierMillGpMillAndInlandFreightDetailsMap, freightDetail.getInland_freight());
            fillSupplierMillGpMillAndPortEntryDetailsMap(supplierMillName, supplierMillGpMillAndPortEntryDetailsMap, freightDetail.getPort_entry_details());
        }
    }


    //Method to Populate Map of Supplier Mill to Gp Mill to Bid Volume
    private static void fillSupplierMillToGpMillToSupplierBidVolumeMap(Map<String, List<MillSpecBid>> mapOfSupplierMillsToBidQty, Map<String, Map<String, Long>> supplierMillToGpMillToSupplierBidVolumeMap) {
        //If It is Mill Specific then Populate Map
        if(isMillSpecific == Boolean.TRUE) {
            for (Map.Entry<String, List<MillSpecBid>> entry : mapOfSupplierMillsToBidQty.entrySet()) {
                String supplierMillName = entry.getKey();
                supplierMillToGpMillToSupplierBidVolumeMap.computeIfAbsent(supplierMillName , k -> new HashMap<>());
                //Iterating Mill Specific Bids
                for (MillSpecBid bid : entry.getValue()) {
                    String gpMill = bid.getMill();
                    Long bidVolume = bid.getBid_vol();
                    if(gpMill!=null && bidVolume!=null){
                        supplierMillToGpMillToSupplierBidVolumeMap.get(supplierMillName).put(gpMill, bidVolume);
                    }
                }
            }
        }
    }


    //Method to Insert  Data in Logistic Sheet for Specific Port of Enry
    private static void fillLogisticSheet(Workbook workbook,Sheet logisticSheet, String supplierMill, String gpMill, String portEntry, int colNumber, Boolean isMillSpecific , Map<String, SupplierMetadata> supplierMillsToSupplierMillsMetadataMap, Map<String, Map<String, Map<String, PortEntryDetails>>> supplierMillGpMillAndPortEntryDetailsMap, Map<String, Map<String, Map<String, InlandFreight>>> supplierMillGpMillAndInlandFreightDetailsMap, Map<String, Integer> rowMap, Map<String, Integer> rowMapPortEntry, Map<String, Integer> rowMisMatchMapPortEntry, Map<String, Integer> rowIncoterm1Map, Map<String, Integer> rowIncoterm2Map,  Map<String, Long> mapOfSupplierMillNameToVolume,  Map<String, Map<String, Long>> supplierMillToGpMillToSupplierBidVolumeMap) throws DecoderException {
//        SupplierMetadata supplierMetadata =

        // setting supplier mill metadata (GpMill, supplier mill , Origin Port , origin country , environmental Certification , Bale packaging , bale type)
        if(supplierMillsToSupplierMillsMetadataMap.containsKey(supplierMill)){
            setCellValueInExcelLogisticSheet(workbook,logisticSheet, colNumber, ExcelConstants.LogisticPricingFields.GP_MILL.getFieldName(), rowMap, (Objects.equals(gpMill, ExcelConstants.GP_MILL_LUMP_SUM)) ? Constants.EMPTY_STRING : gpMill, Boolean.FALSE);
            setCellValueInExcelLogisticSheet(workbook,logisticSheet, colNumber, ExcelConstants.LogisticPricingFields.SUPPLIER_MILL.getFieldName(), rowMap, supplierMill, Boolean.FALSE);
            setCellValueInExcelLogisticSheet(workbook,logisticSheet, colNumber, ExcelConstants.LogisticPricingFields.ORIGIN_PORT.getFieldName(), rowMap, supplierMillsToSupplierMillsMetadataMap.get(supplierMill).getOrigin_port(), Boolean.FALSE);
            setCellValueInExcelLogisticSheet(workbook,logisticSheet, colNumber, ExcelConstants.LogisticPricingFields.ORIGIN_COUNTRY.getFieldName(), rowMap, supplierMillsToSupplierMillsMetadataMap.get(supplierMill).getOrigin_cntry(), Boolean.FALSE);
            setCellValueInExcelLogisticSheetWithWrapText(workbook,logisticSheet, colNumber, ExcelConstants.LogisticPricingFields.ENVIRONMENTAL_CERTIFICATION.getFieldName(), rowMap, supplierMillsToSupplierMillsMetadataMap.get(supplierMill).getEnvironmental_certification(), Boolean.FALSE);
            setCellValueInExcelLogisticSheet(workbook,logisticSheet, colNumber, ExcelConstants.LogisticPricingFields.BALE_PACKAGING.getFieldName(), rowMap, supplierMillsToSupplierMillsMetadataMap.get(supplierMill).getBale_packaging(), Boolean.FALSE);
            setCellValueInExcelLogisticSheet(workbook,logisticSheet, colNumber, ExcelConstants.LogisticPricingFields.BALE_TYPE.getFieldName(), rowMap, supplierMillsToSupplierMillsMetadataMap.get(supplierMill).getBale_type(), Boolean.FALSE);
        }

        //Setting Supplier Bid Volume
        if(isMillSpecific == Boolean.FALSE ) {
            if(mapOfSupplierMillNameToVolume.containsKey(supplierMill) == Boolean.TRUE) {
                setCellValueInExcelLogisticSheetLong(workbook, logisticSheet, colNumber, ExcelConstants.LogisticPricingFields.SUPPLIER_BID_VOLUME.getFieldName(), rowMap, mapOfSupplierMillNameToVolume.get(supplierMill), Boolean.FALSE);
            }
        }else{
            if(supplierMillToGpMillToSupplierBidVolumeMap.containsKey(supplierMill) && supplierMillToGpMillToSupplierBidVolumeMap.get(supplierMill).containsKey(gpMill)){
                setCellValueInExcelLogisticSheetLong(workbook,logisticSheet, colNumber, ExcelConstants.LogisticPricingFields.SUPPLIER_BID_VOLUME.getFieldName(), rowMap, supplierMillToGpMillToSupplierBidVolumeMap.get(supplierMill).get(gpMill) , Boolean.FALSE);
            }
        }



        //Port Entry Name
        setCellValueInExcelLogisticSheet(workbook,logisticSheet, colNumber, ExcelConstants.LogisticPricingFields.US_PORT_OF_ENTRY.getFieldName(), rowMapPortEntry, portEntry, Boolean.FALSE);
        //Populate Port Entry Details
        if(supplierMillGpMillAndPortEntryDetailsMap.containsKey(supplierMill) && supplierMillGpMillAndPortEntryDetailsMap.get(supplierMill).containsKey(gpMill) && supplierMillGpMillAndPortEntryDetailsMap.get(supplierMill).get(gpMill).containsKey(portEntry)){
            PortEntryDetails portEntryDetails = supplierMillGpMillAndPortEntryDetailsMap.get(supplierMill).get(gpMill).get(portEntry);
            //Setting incoterms (Only Two Incoterms allowed)
            int incoterm = 1;
            for(Incoterms incoterms : portEntryDetails.getIncoterms()){
                if(incoterm == 1){
                    //Incoterm 1
                    setIncotermsLogisticSheet(workbook,logisticSheet, colNumber, rowIncoterm1Map,ExcelConstants.LogisticPricingFields.INCOTERMS_1.getFieldName(), incoterms);
                }else{
                    //Incoterm 2
                    setIncotermsLogisticSheet(workbook,logisticSheet, colNumber, rowIncoterm2Map, ExcelConstants.LogisticPricingFields.INCOTERMS_2.getFieldName(),incoterms);
                }
                incoterm++;
            }

            //Ocean Freight
            setCellValueInExcelLogisticSheetLong(workbook,logisticSheet, colNumber, ExcelConstants.LogisticPricingFields.PORT_FREE_TIME_IN_DAYS.getFieldName(), rowMapPortEntry, portEntryDetails.getPort_free_time_in_days(), Boolean.TRUE);
            setCellValueInExcelLogisticSheetLong(workbook,logisticSheet, colNumber, ExcelConstants.LogisticPricingFields.TRANSIT_LEAD_TIME_IN_DAYS.getFieldName(), rowMapPortEntry, portEntryDetails.getTransit_leadtime_in_days_origin_port_port_entry(), Boolean.TRUE);
            if(portEntryDetails.getSteamship_line() != null && !Objects.equals(portEntryDetails.getSteamship_line().toLowerCase(), ExcelConstants.NA.toLowerCase())){
                setCellValueInExcelLogisticSheet(workbook,logisticSheet, colNumber, ExcelConstants.LogisticPricingFields.STEAMSHIP_LINE.getFieldName(), rowMapPortEntry, portEntryDetails.getSteamship_line(), Boolean.TRUE);
            }
            setCellValueInExcelLogisticSheetDollar(workbook,logisticSheet, colNumber, ExcelConstants.LogisticPricingFields.OCEAN_FREIGHT.getFieldName(), rowMapPortEntry, portEntryDetails.getOcean_freight(), Boolean.TRUE);

            //Safety Stock Details
            setCellValueInExcelLogisticSheetLong(workbook,logisticSheet, colNumber, ExcelConstants.LogisticPricingFields.NOMINATED_SAFETY_STOCK.getFieldName(), rowMapPortEntry, portEntryDetails.getSafety_stock_nominated_in_days(), Boolean.FALSE);
            setCellValueInExcelLogisticSheet(workbook,logisticSheet, colNumber, ExcelConstants.LogisticPricingFields.SAFETY_STOCK_LOCATION_NAME.getFieldName(), rowMapPortEntry, portEntryDetails.getSafety_stock_location().getName(), Boolean.FALSE);
            setCellValueInExcelLogisticSheet(workbook,logisticSheet, colNumber, ExcelConstants.LogisticPricingFields.SAFETY_STOCK_LOCATION_TYPE.getFieldName(), rowMapPortEntry, portEntryDetails.getSafety_stock_location().getType(), Boolean.FALSE);
            //Setting Address if getSafety_stock_location type is Warehouse
            if(Objects.equals(portEntryDetails.getSafety_stock_location().getType(), ExcelConstants.InlandTransitOriginType.WAREHOUSE.getValue()) &&
                    portEntryDetails.getSafety_stock_location() != null && portEntryDetails.getSafety_stock_location().getLocation() != null) {
                setCellValueInExcelLogisticSheetWithWrapText(workbook,logisticSheet, colNumber, ExcelConstants.LogisticPricingFields.ADDRESS.getFieldName(), rowMapPortEntry, portEntryDetails.getSafety_stock_location().getLocation().toString(), Boolean.FALSE);
            }
            setCellValueInExcelLogisticSheetDollar(workbook,logisticSheet, colNumber, ExcelConstants.LogisticPricingFields.TRANSIT_COST_US_PORT_TO_SAFETY_STOCK.getFieldName(), rowMapPortEntry, portEntryDetails.getTransit_cost_from_port_entry_to_safety_stock_loc(), Boolean.TRUE);



        }


        //Inland Freight Details
        if(supplierMillGpMillAndInlandFreightDetailsMap.containsKey(supplierMill) && supplierMillGpMillAndInlandFreightDetailsMap.get(supplierMill).containsKey(gpMill) && supplierMillGpMillAndInlandFreightDetailsMap.get(supplierMill).get(gpMill).containsKey(portEntry)){
            InlandFreight inlandFreight = supplierMillGpMillAndInlandFreightDetailsMap.get(supplierMill).get(gpMill).get(portEntry);
            setCellValueInExcelLogisticSheet(workbook,logisticSheet, colNumber, ExcelConstants.LogisticPricingFields.INLAND_TRANSIT_ORIGIN_TYPE.getFieldName(), rowMapPortEntry, inlandFreight.getSource_type(), Boolean.FALSE);
            setCellValueInExcelLogisticSheet(workbook,logisticSheet, colNumber, ExcelConstants.LogisticPricingFields.INLAND_TRANSIT_ORIGIN_NAME.getFieldName(), rowMapPortEntry, inlandFreight.getSource_name(), Boolean.FALSE);
            setCellValueInExcelLogisticSheet(workbook,logisticSheet, colNumber, ExcelConstants.LogisticPricingFields.INLAND_TRANSPORTATION_ROUTE.getFieldName(), rowMapPortEntry, inlandFreight.getInland_trans_route(), Boolean.FALSE);
            setCellValueInExcelLogisticSheet(workbook,logisticSheet, colNumber, ExcelConstants.LogisticPricingFields.INLAND_TRANSIT_DESTINATION_TYPE.getFieldName(), rowMapPortEntry, inlandFreight.getDest_type(), Boolean.FALSE);
            setCellValueInExcelLogisticSheet(workbook,logisticSheet, colNumber, ExcelConstants.LogisticPricingFields.INLAND_TRANSIT_DESTINATION_NAME.getFieldName(), rowMapPortEntry, inlandFreight.getDest_name(), Boolean.FALSE);

            //If Inland Transit Destination type is other than GP Mill
            if((!Objects.equals(inlandFreight.getDest_type(), ExcelConstants.InlandTransitDestinationType.GP_MILL.getValue())) && inlandFreight.getDest_location() != null) {
                setCellValueInExcelLogisticSheetWithWrapText(workbook,logisticSheet, colNumber, ExcelConstants.LogisticPricingFields.ADDRESS.getFieldName(), rowMisMatchMapPortEntry, inlandFreight.getDest_location().toString(), Boolean.FALSE);
            }

            setCellValueInExcelLogisticSheet(workbook,logisticSheet, colNumber, ExcelConstants.LogisticPricingFields.TRANSIT_MODE.getFieldName(), rowMapPortEntry, inlandFreight.getTransit_mode(), Boolean.FALSE);
            setCellValueInExcelLogisticSheetLong(workbook,logisticSheet, colNumber, ExcelConstants.LogisticPricingFields.TRANSIT_LEAD_TIME_TO_GP_MILL.getFieldName(), rowMapPortEntry, inlandFreight.getTransit_leadtime_in_days_port_entry_gp_mill(), Boolean.FALSE);
            setCellValueInExcelLogisticSheetDollar(workbook,logisticSheet, colNumber, ExcelConstants.LogisticPricingFields.TRANSIT_COST.getFieldName(), rowMapPortEntry, inlandFreight.getTransit_cost(), Boolean.TRUE);
        }


    }




    //Method to Set Cell Value in a given cell
    private static void setCellValueInExcelLogisticSheet(Workbook workbook,Sheet logisticSheet, int col, String fieldName, Map<String, Integer> rowMap, String value, Boolean isNA) throws DecoderException {
        Row row = logisticSheet.getRow((rowMap.get(fieldName)));
        if(row==null){
            row = logisticSheet.createRow(rowMap.get(fieldName));
        }
        Cell cell = row.getCell(col);
        if(cell==null){
            cell = row.createCell(col);
        }

        // Create a CellStyle with bold font
        XSSFCellStyle cellStyle = (XSSFCellStyle) workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontName(ExcelConstants.FONT_ARIAL);
        cellStyle.setFont(font);


        // Set horizontal alignment to center
        cellStyle.setAlignment(HorizontalAlignment.CENTER);

        // Set vertical alignment to center
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        //Setting color
        String rgbs = ExcelConstants.CELL_VALUE_COLOUR_CODE;
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

        // setting cellValue in Cell
        if(value == null && isNA == Boolean.TRUE){
            cell.setCellValue(ExcelConstants.NA);
        }else {
            if(value!=null){
                cell.setCellValue(value);
            }
        }
    }

    //Method to Set Cell Dollar Value in a given cell
    private static void setCellValueInExcelLogisticSheetDollar(Workbook workbook,Sheet logisticSheet, int col, String fieldName, Map<String, Integer> rowMap, Float value, Boolean isNA) throws DecoderException {
        Row row = logisticSheet.getRow((rowMap.get(fieldName)));
        if(row==null){
            row = logisticSheet.createRow(rowMap.get(fieldName));
        }
        Cell cell = row.getCell(col);
        if(cell==null){
            cell = row.createCell(col);
        }

        // Create a CellStyle with bold font
        XSSFCellStyle cellStyle = (XSSFCellStyle) workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontName(ExcelConstants.FONT_ARIAL);
        cellStyle.setFont(font);


        // Set horizontal alignment to center
        cellStyle.setAlignment(HorizontalAlignment.CENTER);

        // Set vertical alignment to center
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        //Setting color
        String rgbs = ExcelConstants.CELL_VALUE_COLOUR_CODE;
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


        if(Objects.equals(value, null) && isNA == Boolean.TRUE) {
            cell.setCellValue(ExcelConstants.NA);
        }else {
            if(value!=null){
                cell.setCellValue(Utils.currencyFormat().format(value));
            }
        }
    }

    //Method to Set Cell long Value in a given cell
    private static void setCellValueInExcelLogisticSheetLong(Workbook workbook,Sheet logisticSheet, int col, String fieldName, Map<String, Integer> rowMap, Long value, Boolean isNA) throws DecoderException {
        Row row = logisticSheet.getRow((rowMap.get(fieldName)));
        if(row==null){
            row = logisticSheet.createRow(rowMap.get(fieldName));
        }
        Cell cell = row.getCell(col);
        if(cell==null){
            cell = row.createCell(col);
        }

        // Create a CellStyle with bold font
        XSSFCellStyle cellStyle = (XSSFCellStyle) workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontName(ExcelConstants.FONT_ARIAL);
        cellStyle.setFont(font);


        // Set horizontal alignment to center
        cellStyle.setAlignment(HorizontalAlignment.CENTER);

        // Set vertical alignment to center
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        //Setting color
        String rgbs = ExcelConstants.CELL_VALUE_COLOUR_CODE;
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



        if(value == null && isNA == Boolean.TRUE){
            cell.setCellValue(ExcelConstants.NA);
        }else {
            if(value!=null){
                cell.setCellValue(value);
            }
        }
    }

    private static void setCellValueInExcelLogisticSheetFloat(Workbook workbook,Sheet logisticSheet, int col, String fieldName, Map<String, Integer> rowMap, Float value, Boolean isNA) throws DecoderException {
        Row row = logisticSheet.getRow((rowMap.get(fieldName)));
        if(row==null){
            row = logisticSheet.createRow(rowMap.get(fieldName));
        }
        Cell cell = row.getCell(col);
        if(cell==null){
            cell = row.createCell(col);
        }

        // Create a CellStyle with bold font
        XSSFCellStyle cellStyle = (XSSFCellStyle) workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontName(ExcelConstants.FONT_ARIAL);
        cellStyle.setFont(font);


        // Set horizontal alignment to center
        cellStyle.setAlignment(HorizontalAlignment.CENTER);

        // Set vertical alignment to center
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        //Setting color
        String rgbs = ExcelConstants.CELL_VALUE_COLOUR_CODE;
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

        if(value == null && isNA == Boolean.TRUE){
            cell.setCellValue(ExcelConstants.NA);
        }else {
            if(value!=null){
                cell.setCellValue(value);
            }
        }
    }


    //method to Insert Incoterm Data
    private static void setIncotermsLogisticSheet(Workbook workbook,Sheet logisticSheet, int col, Map<String, Integer> rowIncoterm1Map, String incotermFieldName, Incoterms incoterms) throws DecoderException {
        setCellValueInExcelLogisticSheet(workbook,logisticSheet, col, incotermFieldName, rowIncoterm1Map, incoterms.getIncoterm(), Boolean.FALSE);
        setCellValueInExcelLogisticSheetDollar(workbook,logisticSheet, col, ExcelConstants.LogisticPricingFields.STEVEDORING_COST.getFieldName(), rowIncoterm1Map, incoterms.getStevedoring_cost(), Boolean.TRUE);
        setCellValueInExcelLogisticSheetDollar(workbook,logisticSheet, col, ExcelConstants.LogisticPricingFields.HANDLING_COST.getFieldName(), rowIncoterm1Map, incoterms.getHandling_cost(), Boolean.TRUE);
        setCellValueInExcelLogisticSheetDollar(workbook,logisticSheet, col, ExcelConstants.LogisticPricingFields.WHARFAGE_COST.getFieldName(), rowIncoterm1Map, incoterms.getWharfage_cost(), Boolean.TRUE);
        setCellValueInExcelLogisticSheetDollar(workbook,logisticSheet, col, ExcelConstants.LogisticPricingFields.SECURITY_COST.getFieldName(), rowIncoterm1Map, incoterms.getSecurity_cost(), Boolean.TRUE);
        setCellValueInExcelLogisticSheetDollar(workbook,logisticSheet, col, ExcelConstants.LogisticPricingFields.WAREHOUSING_FEE_MONTHLY.getFieldName(), rowIncoterm1Map, incoterms.getWarehouse_cost_per_month(), Boolean.TRUE);
        setCellValueInExcelLogisticSheetDollar(workbook,logisticSheet, col, ExcelConstants.LogisticPricingFields.IMPORT_CUSTOMS_TARIFF_FEE.getFieldName(), rowIncoterm1Map, incoterms.getCustoms_fee(), Boolean.TRUE);
    }

    //Method to Prepare Map of Supplier Mill Name to Supplier Meta Data
    private static void fillSupplierMillsToSupplierMillsMetadataMap(SupplierMills supplierMill, String supplierMillName, Map<String, SupplierMetadata> supplierMillsToSupplierMillsMetadataMap) {
        SupplierMetadata supplierMillMetadata = new SupplierMetadata();
        supplierMillMetadata.setSupplier_mill(supplierMillName);
        if(supplierMill.getOrigin_port()!=null){
            supplierMillMetadata.setOrigin_port(supplierMill.getOrigin_port());
        }
        if(supplierMill.getOrigin_cntry()!=null){
            supplierMillMetadata.setOrigin_cntry(supplierMill.getOrigin_cntry());
        }
        if(supplierMill.getFiber_type()!=null){
            supplierMillMetadata.setFiber_type(supplierMill.getFiber_type());
        }
        if(supplierMill.getEnvironmental_certification()!=null){
            supplierMillMetadata.setEnvironmental_certification(supplierMill.getEnvironmental_certification());
        }
        if(supplierMill.getBale_packaging()!=null){
            supplierMillMetadata.setBale_packaging(supplierMill.getBale_packaging());
        }
        if(supplierMill.getBale_type()!=null){
            supplierMillMetadata.setBale_type(supplierMill.getBale_type());
        }
        supplierMillMetadata.setIs_supplier_mill_domestic(supplierMill.isIs_supplier_mill_domestic());

        //Adding in the Map (Even if supplierMillName exists in the Map , it will override since data will be same)
        supplierMillsToSupplierMillsMetadataMap.put(supplierMillName, supplierMillMetadata);
    }

    //Method to Prepare Map of Supplier Mill to GP mill to Freight Details Map
    private static void fillSupplierMillGpMillAndInlandFreightDetailsMap( String supplierMillName, Map<String, Map<String, Map<String, InlandFreight>>> supplierMillGpMillAndInlandFreightDetailsMap, List<InlandFreight> inlandFreightList) {
        supplierMillGpMillAndInlandFreightDetailsMap.computeIfAbsent(supplierMillName, k -> new HashMap<>());
        //Iterating Freight Details List
        for(InlandFreight inlandFreight : inlandFreightList){
            String gpMill = inlandFreight.getGp_mill();
            String portOfEntry = inlandFreight.getPort_entry();
            //If Gp Mill Name is Null then it will be assumed as LUMP SUm
            if(gpMill == null){
                gpMill = ExcelConstants.GP_MILL_LUMP_SUM;
            }
            //if Gp Mill Name is Null then Port of entry it is Domestic
            if(portOfEntry == null){
                portOfEntry = ExcelConstants.USPortOfEntry.DOMESTIC.getValue();
            }

            supplierMillGpMillAndInlandFreightDetailsMap.get(supplierMillName).computeIfAbsent(gpMill, k -> new HashMap<>());
            supplierMillGpMillAndInlandFreightDetailsMap.get(supplierMillName).get(gpMill).put(portOfEntry, inlandFreight);
        }
    }

    //Method to Prepare Map if Supplier Mill Name to gp Mill name to Port Entry to Port Entry Details
    private static void fillSupplierMillGpMillAndPortEntryDetailsMap(String supplierMillName, Map<String, Map<String, Map<String, PortEntryDetails>>> supplierMillGpMillAndPortEntryDetailsMap, List<PortEntryDetails> portEntryDetailsList)  {
        supplierMillGpMillAndPortEntryDetailsMap.computeIfAbsent(supplierMillName, k -> new HashMap<>());

        for (PortEntryDetails portEntryDetails : portEntryDetailsList) {
            String gpMill = portEntryDetails.getGp_mill();
            String portOfEntry = portEntryDetails.getPort_entry();
            //If Port of Entry is Null then it will be Domestic
            if (portOfEntry == null) {
                portOfEntry = ExcelConstants.USPortOfEntry.DOMESTIC.getValue();
            }

            String[] gpMillsArray; // This splits by comma and trims spaces around each part (Since It will comma separated GP Mills )

            //If Lump Sum then It will be LUMP SUM
            if (gpMill == null) {
                gpMill = ExcelConstants.GP_MILL_LUMP_SUM;
                supplierMillGpMillAndPortEntryDetailsMap.get(supplierMillName).computeIfAbsent(gpMill, k -> new HashMap<>());
                supplierMillGpMillAndPortEntryDetailsMap.get(supplierMillName).get(gpMill).put(portOfEntry, portEntryDetails);

            }else{
                gpMillsArray = gpMill.split(Constants.COMMA_SEPARATED_REGEX);
                for(String gpMillName : gpMillsArray){
                    supplierMillGpMillAndPortEntryDetailsMap.get(supplierMillName).computeIfAbsent(gpMillName, k -> new HashMap<>());
                    supplierMillGpMillAndPortEntryDetailsMap.get(supplierMillName).get(gpMillName).put(portOfEntry, portEntryDetails);

                }
            }
        }
    }

    // Method to Setting the CellValue in sheet With WrapText (Like Address Field)
    private static void setCellValueInExcelLogisticSheetWithWrapText(Workbook workbook, Sheet logisticSheet, int col, String fieldName, Map<String, Integer> rowMap, String value, Boolean isNA) throws DecoderException {
        Row row = logisticSheet.getRow((rowMap.get(fieldName)));
        if(row==null){
            row = logisticSheet.createRow(rowMap.get(fieldName));
        }
        Cell cell = row.getCell(col);
        if(cell==null){
            cell = row.createCell(col);
        }

        // Create a CellStyle with bold font
        XSSFCellStyle cellStyle = (XSSFCellStyle) workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontName(ExcelConstants.FONT_ARIAL);
        cellStyle.setFont(font);

        cellStyle.setWrapText(true);


        // Set horizontal alignment to center
        cellStyle.setAlignment(HorizontalAlignment.CENTER);

        // Set vertical alignment to center
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        //Setting color
        String rgbs = ExcelConstants.CELL_VALUE_COLOUR_CODE;
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


        if(Objects.equals(value, null) && isNA == Boolean.TRUE) {
            cell.setCellValue(ExcelConstants.NA);
        }else {
            if(value!=null){
                cell.setCellValue(value);
            }
        }
    }


}
    





