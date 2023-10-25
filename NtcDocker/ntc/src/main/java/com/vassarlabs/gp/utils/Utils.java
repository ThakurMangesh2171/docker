package com.vassarlabs.gp.utils;

import com.vassarlabs.gp.constants.Constants;
import com.vassarlabs.gp.constants.ErrorMessages;
import com.vassarlabs.gp.constants.ExcelConstants;
import com.vassarlabs.gp.dao.entity.ErrorMessage;
import com.vassarlabs.gp.exception.InvalidValueProvidedException;
import com.vassarlabs.gp.pojo.*;
import com.vassarlabs.gp.pojo.ResponseRfpJson.BidQtyDetail;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Utils {
    private Utils() {
    }

    private static final Logger LOGGER = LogManager.getLogger(Utils.class);

    public static String generateRandomUUID() {
        return UUID.randomUUID().toString();
    }

    public static final DataFormatter df = new DataFormatter();

    //Method to get the Current Time in Long Format
    public static long getCurrentTime() {
        //Create a new date object
        Date date = new Date();

        //Get current time in milliseconds (ms) from date object
        return date.getTime();
    }


    public static Boolean checkIfStringIsNullOrEmpty(String input) {
        return (input == null || input.trim().length() == 0);
    }

    public static boolean validateBidType(String bidType) {
        if (!Stream.of(Constants.BidType.values())
                .map(Constants.BidType::getValue)
                .collect(Collectors.toList()).contains(bidType)) {
            String errorMessage = MessageFormat.format(ErrorMessages.Messages.INVALID_FIELD_VALUE_ERROR.getMessage(),ExcelConstants.ResponseRfpExcelHeaders.BID_TYPE.getValue(),bidType);
            ExcelParsingUtils.errorMessageDetailsList.add(new ErrorMessageDetails(ExcelConstants.ResponseRfpExcelSheetNames.BID_DETAILS.getSheetName(),ExcelConstants.COLUMN_NAME_TO_CELL_REFERENCE.get(ExcelConstants.ResponseRfpExcelHeaders.BID_TYPE.getValue()),ExcelConstants.ResponseRfpExcelHeaders.BID_TYPE.getValue(),bidType,ExcelConstants.ResponseRfpExcelHeaders.BID_TYPE.getValue(),errorMessage));
            return false;
        }
        return true;
    }

    public static Boolean validateDateForExcel(String periodStart, String periodEnd, CellReference startDateCellRef, CellReference endDateCellRef) throws ParseException {
        if(periodEnd==null || periodStart==null){
            return false;
        }
        Boolean startPeriod = isCellInDateFormat(periodStart,startDateCellRef);
        Boolean endPeriod = isCellInDateFormat(periodEnd,endDateCellRef);

        if (startPeriod && endPeriod){
            SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_FOR_EXCEL);
            Date sDate = sdf.parse(periodStart);
            Date eDate = sdf.parse(periodEnd);
            if ((!sDate.before(eDate) || sDate.equals(eDate))) {
                String errorMessage = ErrorMessages.Messages.DATE_MISMATCHED_ERROR.getMessage();
                ExcelParsingUtils.errorMessageDetailsList.add(new ErrorMessageDetails(ExcelConstants.ResponseRfpExcelSheetNames.BID_DETAILS.getSheetName(),null,null,null,null,errorMessage));
                return false;
            }else{
                return true;
            }
        }
        return false;
    }


    //validating Date in format MM-dd-yyyy
    public static boolean isCellInDateFormat(String cellValue, CellReference cellReference) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(Constants.DATE_FORMAT_FOR_EXCEL);
        try {
            LocalDate.parse(cellValue, dateFormatter);
            return true;
        } catch (DateTimeParseException e) {
            String errorMessage = MessageFormat.format(ErrorMessages.Messages.INVALID_FORMAT_ERROR.getMessage(),cellValue,Constants.DATE_FORMAT_FOR_EXCEL);
            ExcelParsingUtils.errorMessageDetailsList.add(new ErrorMessageDetails(ExcelConstants.ResponseRfpExcelSheetNames.BID_DETAILS.getSheetName(),cellReference.formatAsString(false),Constants.DATE_FORMAT_FOR_EXCEL,cellValue,null,errorMessage));
            return false;
        }
    }

    //Returns TRUE if given date is greater than the first day of the current month.
    public static Boolean isDateGreaterThanFirstDayOfMonth(String dateString) throws ParseException {

        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_FOR_EXCEL);

        Date date = sdf.parse(dateString);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1); // Set the day of the month to 1
        Date firstDayOfMonth = calendar.getTime();

        return firstDayOfMonth.before(date);
    }

    public static void saveWorkbookToExcel(Workbook workbook, String filePath) throws IOException {
        try(FileOutputStream out = new FileOutputStream(filePath)){
            workbook.write(out);
        }
    }

    //Setting the Value of Cell with Bold Font
    public static void setHeaderValueInExcelCell(Workbook workbook, Sheet sheet, CellReference reference, String valueInCell, String cellValue) {
        int rowNumber = reference.getRow();
        int columnNumber = reference.getCol();

        Row row = sheet.getRow(rowNumber);
        if (row == null) {
            row = sheet.createRow(rowNumber);
        }

        Cell cell = row.getCell(columnNumber);
        if (cell == null) {
            cell = row.createCell(columnNumber);
        }
        //Setting Header
        cell.setCellValue(valueInCell.trim()+Constants.SPACE);

        // Create a CellStyle with bold font
        CellStyle cellStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontName(ExcelConstants.FONT_ARIAL);
        cellStyle.setFont(font);

        // Apply the CellStyle to the cell
        cell.setCellStyle(cellStyle);


        //setting Value in next column
        columnNumber++;
        cell = row.getCell(columnNumber);
        if (cell == null) {
            cell = row.createCell(columnNumber);
        }
        cell.setCellValue(cellValue.trim());

        // Apply the CellStyle to the cell
        cell.setCellStyle(cellStyle);

    }

    //Set the Value in Excel Cell with Borders
    public static void setCellValueWithBorderInExcel(Workbook workbook, Sheet sheet, CellReference cellReference, String cellHeader, String cellValue,Boolean alignRigh,String cellColourCode) throws DecoderException {
        int rowNumber = cellReference.getRow();
        int columnNumber = cellReference.getCol();

        Row row = sheet.getRow(rowNumber);
        if (row == null) {
            row = sheet.createRow(rowNumber);
        }

        Cell cell = row.getCell(columnNumber);
        if (cell == null) {
            cell = row.createCell(columnNumber);
        }
        cell.setCellValue(cellHeader);

        // Create a CellStyle with bold font
        XSSFCellStyle cellStyle = (XSSFCellStyle) workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontName(ExcelConstants.FONT_ARIAL);
        cellStyle.setFont(font);


        // Set horizontal alignment to center
        cellStyle.setAlignment(HorizontalAlignment.CENTER);

        // Set vertical alignment to center
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        if (alignRigh)
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
    }

    public static void validateMillDetails(List<Mills> millList) throws InvalidValueProvidedException{

        if (millList!=null && !millList.isEmpty()){
            for (Mills mill:millList) {
                if (mill.getMillName() == null || mill.getMillName().trim().isEmpty()){
                    throw new InvalidValueProvidedException(ErrorMessages.MILL_NAME_NULL_ERROR);
                }
                if (mill.getState() == null || mill.getState().trim().isEmpty()){
                    throw new InvalidValueProvidedException(ErrorMessages.STATE_NULL_ERROR);
                }
                if (mill.getExpectedAnnualVolume() == null){
                    throw new InvalidValueProvidedException(ErrorMessages.EXCEPTED_ANNUAL_VOLUME_NULL_ERROR);
                }
            }
        }else {
            throw new InvalidValueProvidedException(ErrorMessages.MILLS_LIST_NULL_ERROR);
        }
    }

    public static ErrorMessage buildErrorMessageEntity(String responseRfpId, List<ErrorMessageDetails> errorMessageDetails, List<ErrorMessageDetails> responseRfpWarning) {
        ErrorMessage errorMessage = new ErrorMessage();

        ResponseRfpErrorMessages responseRfpErrorMessages = new ResponseRfpErrorMessages();


        errorMessage.setMsgUuid(generateRandomUUID());
        errorMessage.setResponseRfpId(responseRfpId);
        errorMessage.setInsertTs(getCurrentTime());
        errorMessage.setUpdatedTs(getCurrentTime());
        errorMessage.setIsInsert(true);
        //TODO : ErrorMessages
        if(errorMessageDetails!=null){
            if (errorMessage.getResponseRfpErrorMessages() != null){
                errorMessage.getResponseRfpErrorMessages().setErrorMessagesJson(errorMessageDetails);
            }else {
                responseRfpErrorMessages.setErrorMessagesJson(errorMessageDetails);
                errorMessage.setResponseRfpErrorMessages(responseRfpErrorMessages);
            }

//            errorMessage.setErrorMessagesJson(errorMessageDetails);
        }
        if (responseRfpWarning != null){
            if (errorMessage.getResponseRfpErrorMessages() != null){
                errorMessage.getResponseRfpErrorMessages().setResponseRfpWarning(responseRfpWarning);
            }else {
                responseRfpErrorMessages.setResponseRfpWarning(responseRfpWarning);
                errorMessage.setResponseRfpErrorMessages(responseRfpErrorMessages);
            }
        }
        return errorMessage;
    }

    //Method to get CurrentDate
    public static String getCurrentDate() {
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return currentDate.format(formatter);
    }

    //Method to get value between the parenthesis
    public static String extractValueBetweenParentheses(String input) {
        // Regular expression pattern to match text between parentheses
        Pattern pattern = Pattern.compile(Constants.valueBeetweenParanthesisRegex);

        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            // Extract the value between parentheses
            return matcher.group(1);
        } else {
            // No match found
            return null;
        }
    }
    //method to get data after COLON(:)
    public static String extractRfpNumberFromString(String inputValue) {

        int index = inputValue.indexOf(ExcelConstants.COLON);
        if (index != -1 && index < inputValue.length() - 1) {
            return inputValue.substring(index + 1).trim();
        } else {
            return "";
        }
    }


    public static boolean validateRegex(String data, String regex) {
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(data).matches();
    }


    //Method to delete a file if exists
    public static void deleteFile(String filePath)  {
        File file = new File(filePath);
        file.delete();
    }

    public static void validateSupplierInfo(List<SupplierInfo> supplierInfoList) throws InvalidValueProvidedException{

        if (supplierInfoList!=null && !supplierInfoList.isEmpty()){
            for (SupplierInfo supplierInfo:supplierInfoList) {
                if (supplierInfo.getName() == null || supplierInfo.getName().trim().isEmpty()){
                    throw new InvalidValueProvidedException(ErrorMessages.SUPPLIER_NAME_NULL_ERROR);
                }
                if (supplierInfo.getEmail() == null || supplierInfo.getEmail().trim().isEmpty()){
                    throw new InvalidValueProvidedException(ErrorMessages.MAIL_ID_NULL_ERROR);
                }
            }
        }else {
            throw new InvalidValueProvidedException(ErrorMessages.SUPPLIER_LIST_NULL_ERROR);
        }

    }

    public static String formatWithCommas(long value) {
        DecimalFormat decimalFormat = new DecimalFormat("#,###");
        return decimalFormat.format(value);
    }

    //Method to fetch header name from bid Qty Details Sheet (Split by : and return first string)
    public static String getHeaderNameFromBidQtySheet(String input) {
        String[] parts = input.split(ExcelConstants.COLON);
        if (parts.length > 0) {
            return parts[0].trim();
        } else {
            // Return the original input if there's no colon in the string
            return input.trim();
        }
    }


    public static String getTrimmedNumber(String cellValue){
        if(cellValue == null) return null;
        return cellValue.replaceAll(Constants.SPACE, Constants.EMPTY_STRING).replaceAll(Constants.COMMA, Constants.EMPTY_STRING);
    }

    public static Float getFloatDoller(String input) {
        if(input == null || input.trim().isEmpty()) return 0f;

        // Remove dollar sign, commas, and spaces
        String cleanedInput = input.replace(Constants.DOLLAR, Constants.EMPTY_STRING).replaceAll(Constants.COMMA, Constants.EMPTY_STRING).replaceAll(Constants.SPACE, Constants.EMPTY_STRING);

        if(Objects.equals(cleanedInput.toLowerCase(), ExcelConstants.NA.toLowerCase())){
            return 0f;
        }

        // Convert to float
        return Float.parseFloat(cleanedInput);
    }

    public static Long getLongDoller(String input) {

        if(input == null || input.trim().isEmpty()) return null;

        // Remove dollar sign, commas, and spaces
        String cleanedInput = input.replace(Constants.DOLLAR, Constants.EMPTY_STRING)
                .replaceAll(Constants.COMMA, Constants.EMPTY_STRING)
                .replaceAll(Constants.SPACE, Constants.EMPTY_STRING);

        // If input is a decimal number, remove the decimal part
        if (cleanedInput.contains(".")) {
            cleanedInput = cleanedInput.split("\\.")[0];
        }

        // Convert to long
        return Long.parseLong(cleanedInput);
    }


    //Method to Fetch rfpNumber/FiberType/DueDate
    public static String getValueFromBidQtySheet(String cellReference, String fieldName, String fieldFormat, String header, String valueRegex, String valueExpected ,String cellValue) {

        //Split string by :
        String[] parts = cellValue.split(ExcelConstants.COLON);
        //Length of String array after splitting must be 2
        if (parts.length != 2) {
            String errorMessage = MessageFormat.format(ErrorMessages.Messages.DATA_FORMAT_MISMATCH.getMessage(), fieldFormat,cellValue);
            ExcelParsingUtils.errorMessageDetailsList.add(new ErrorMessageDetails(ExcelConstants.ResponseRfpExcelSheetNames.BID_DETAILS.getSheetName(),cellReference,ExcelConstants.ExpectedValuesList.MANDATORY.getValue(),cellValue,fieldName,errorMessage));
            return null;
        }

        String headerName = parts[0].trim();
        String headerValue = parts[1].trim();
        //Checking for  Header name
        if (headerName.trim().isEmpty() || !Objects.equals(headerName.trim(), header)) {
            String errorMessage = MessageFormat.format(ErrorMessages.Messages.HEADER_INVALID.getMessage(), headerName,header);
            ExcelParsingUtils.errorMessageDetailsList.add(new ErrorMessageDetails(ExcelConstants.ResponseRfpExcelSheetNames.BID_DETAILS.getSheetName(), cellReference,header,headerName,fieldName,errorMessage));
            return null;
        }
        //Checking for Header Value regex
        if (!headerValue.trim().isEmpty()){
            if (!validateRegex(headerValue,valueRegex)) {
                String errorMessage = MessageFormat.format(ErrorMessages.Messages.DATA_TYPE_MISMATCH.getMessage(),valueExpected,headerValue);
                ExcelParsingUtils.errorMessageDetailsList.add(new ErrorMessageDetails(ExcelConstants.ResponseRfpExcelSheetNames.BID_DETAILS.getSheetName(),cellReference,ExcelConstants.ExpectedValuesList.MANDATORY.getValue(),headerValue, fieldName,errorMessage));
                return null;
            }
        }else {
            String errorMessage = MessageFormat.format(ErrorMessages.Messages.MANDATORY_FIELD_MISSING_ERROR.getMessage(), fieldName);
            ExcelParsingUtils.errorMessageDetailsList.add(new ErrorMessageDetails(ExcelConstants.ResponseRfpExcelSheetNames.BID_DETAILS.getSheetName(),new CellReference(ExcelConstants.BID_QTY_DETAILS_COLUMN_HEADER_TO_COLUMN_NUMBER.get(ExcelConstants.ResponseRfpExcelHeaders.RFP_NUMBER.getValue())).formatAsString(false),ExcelConstants.ExpectedValuesList.MANDATORY.getValue(),null, ExcelConstants.ResponseRfpExcelHeaders.RFP_NUMBER.getValue(),errorMessage));
            return null;
        }
        return headerValue;
    }

    //Method to Check if Cell/Cell value is empty or not
    //returs true if cell is empty
    public static boolean isCellEmpty(Sheet sheet, int rowNum, int colNum) {
        CellReference cellReference = new CellReference(rowNum,colNum);
        if(cellReference == null || sheet.getRow(rowNum).getCell(colNum)==null  || df.formatCellValue(sheet.getRow(rowNum).getCell(colNum))==null || df.formatCellValue(sheet.getRow(rowNum).getCell(colNum)).trim().isEmpty()){
            return true;
        }
        return false;
    }


    public static BidQtyDetail copyBidDetailsToBidQtyDetail(BidQtyDetail source) {
        BidQtyDetail destination = new BidQtyDetail();

        destination.setBid_type(source.getBid_type());
        destination.setBid_vol(source.getBid_vol());
        destination.setBid_vol_variance_pct(source.getBid_vol_variance_pct());
        destination.setQty_uom(source.getQty_uom());
        destination.setPeriod_start(source.getPeriod_start());
        destination.setPeriod_end(source.getPeriod_end());
        destination.setMill_spec_bid(source.getMill_spec_bid());

        return destination;
    }

    public static String intToExcelColumnLabel(int number) {
        StringBuilder columnLabel = new StringBuilder();

        while (number >= 0) {
            int remainder = number % 26;
            char digit = (char) ('A' + remainder);
            columnLabel.insert(0, digit);
            number = (number / 26) - 1;

            if (number < 0) {
                break;
            }
        }

        return columnLabel.toString();
    }

    public static String TrimDecimalValue(float difference){
        DecimalFormat decimalFormat = new DecimalFormat("#.00");
        return decimalFormat.format(difference);
    }

    public static String removeTrailingHyphenAndDollar(String input) {
        // Use regular expression to remove trailing " - $"
        return input.replaceAll(" - \\$$", "");
    }

    public static Float getPositiveValueOf(Float number){
        return (-1) * number;
    }

    public static String getLastMonthName() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -1);

        int lastMonthIndex = calendar.get(Calendar.MONTH);

        String[] monthNames = new DateFormatSymbols().getMonths();
        String lastMonthName = monthNames[lastMonthIndex];

        return lastMonthName;
    }

    public static String getCurrentYear() {
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        String currentYearStr = String.valueOf(currentYear);
        return currentYearStr;
    }

    public static String getPreviousYear() {
        Calendar calendar = Calendar.getInstance();
        int previousYear = calendar.get(Calendar.YEAR) - 1;
        return String.valueOf(previousYear);
    }

    public static String getNextYear() {
        Calendar calendar = Calendar.getInstance();
        int previousYear = calendar.get(Calendar.YEAR) + 1;
        return String.valueOf(previousYear);
    }

    public static String parseAndExtractDateString(String dateString) {

        if (validateRegex(dateString,Constants.VALID_DATE_REGEX)) {
            return dateString;
        }else {
            String errorMessage = MessageFormat.format(ErrorMessages.Messages.INVALID_FORMAT_ERROR.getMessage(),dateString,Constants.DATE_FORMAT_FOR_EXCEL);
            ExcelParsingUtils.errorMessageDetailsList.add(new ErrorMessageDetails(ExcelConstants.ResponseRfpExcelSheetNames.BID_DETAILS.getSheetName(),null,Constants.DATE_FORMAT_FOR_EXCEL,dateString,null,errorMessage));
            return null;
        }
    }

    // Setting The CellValue In Bid Qty Details for Headers
    public static void setHeaderValueInBidQtySheet(Workbook workbook, Sheet sheet, CellReference reference,String cellValue) {

        int rowNumber = reference.getRow();
        int columnNumber = reference.getCol();

        Row row = sheet.getRow(rowNumber);
        //Creating Row if it is empty
        if (row == null) {
            row = sheet.createRow(rowNumber);
        }
        //Creating Cell if it is empty
        Cell cell = row.getCell(columnNumber);
        if (cell == null) {
            cell = row.createCell(columnNumber);
        }
        //Setting Header Value
        if(cellValue!=null){
            cell.setCellValue(cellValue.trim());
        }

    }

    public static void setBorderToCell(Workbook workbook, Integer rowIndex, Integer startColumnIndex, Integer endColumnIndex, String cellColourCode) throws DecoderException {
        Sheet bidQtySheet = workbook.getSheet(ExcelConstants.ResponseRfpExcelSheetNames.BID_DETAILS.getSheetName());
        Row row = bidQtySheet.createRow(rowIndex);
        //Set Row Size Same as Example value
        row.setHeightInPoints(bidQtySheet.getRow(ExcelConstants.BID_DETAILS_MAP.get(ExcelConstants.ResponseRfpExcelHeaders.GP_MILL_EXAMPLE_ROW.getValue())).getHeightInPoints());


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

    public static NumberFormat currencyFormat() {
        // Create a NumberFormat instance for currency formatting with two decimal places
        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.US);
        format.setMinimumFractionDigits(2); // Ensure two decimal places
        return format;
    }


    // Method to convert value to percentageFormat
    public static String getPercentageFormat(String s) {
        if (s == null || s.trim().isEmpty()){
            return null;
        }
            // Convert the input string to a double (assuming it represents a percentage)
            double percentage = Double.parseDouble(s);

            // Create a percentage formatter
            NumberFormat percentageFormatter = NumberFormat.getPercentInstance();

            // Format the percentage
            return percentageFormatter.format(percentage / 100.0);
    }

    //Method to convert String value to currency format
    public static String currencyFormat(String s) {
        if (s == null || s.trim().isEmpty()){
            return null;
        }
        // Parse the input string as a double
        double amount = Double.parseDouble(s);

        // Create a NumberFormat instance for currency formatting with two decimal places
        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.US);
        format.setMinimumFractionDigits(2);

        // Format the double value as currency
        return format.format(amount);
    }

    // Method for checking the start Period should be between contract term year -1 to contract term year

    public static boolean isValidStartPeriod(String startPeriod, int contractTerm) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.DATE_FORMAT_FOR_EXCEL);
        Date parsedStartPeriod = dateFormat.parse(startPeriod);

        Date validStartDate = dateFormat.parse(ExcelConstants.START_MONTH_DATE + (contractTerm-1));
        Date validEndDate = dateFormat.parse(ExcelConstants.END_MONTH_DATE + contractTerm);

        return parsedStartPeriod.compareTo(validStartDate) >= 0 && parsedStartPeriod.compareTo(validEndDate) <= 0;
    }

    // Method for checking the Valid EndDate Based On contract Term
    public static boolean isValidEndPeriod(String endPeriod, int contractTerm) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.DATE_FORMAT_FOR_EXCEL);
        Date parsedEndPeriod = dateFormat.parse(endPeriod);

        Date validStartDate = dateFormat.parse(ExcelConstants.START_MONTH_DATE + contractTerm);
        Date validEndDate = dateFormat.parse(ExcelConstants.END_MONTH_DATE + contractTerm);

        return parsedEndPeriod.compareTo(validStartDate) >= 0 && parsedEndPeriod.compareTo(validEndDate) <= 0;
    }
}
