package com.ibagroup.wf.intelia.core;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.freedomoss.crowdcontrol.webharvest.WebHarvestTaskItem;
import com.google.common.collect.Lists;
import groovy.lang.Binding;

public class CommonUtils {
    private static final Logger logger = LoggerFactory.getLogger(CommonUtils.class);

    private CommonUtils() {}

    /**
     * Normalize dates according to acceptable format in 3270 system number (21.07.2017 = JUL2017).
     *
     * @param date_of_incident date of incident
     * @param time_reported time reported
     * @param expectedDateRange
     * @return normalized dates
     */
    public static String[] normalizeDatesForATMX(String date_of_incident, String time_reported, int expectedDateRange) {
        int inputMonth = Integer.parseInt(date_of_incident.substring(3, 5));
        int inputYear = Integer.parseInt(date_of_incident.substring(6, 10));
        int reportedMonth = Integer.parseInt(time_reported.substring(3, 5));

        List<String> dates = new ArrayList<>();
        if (inputMonth != reportedMonth) {
            dates.add(normalizeMonth(reportedMonth) + inputYear);
        }
        for (int i = 0; i < expectedDateRange; i++) {
            if (inputMonth - i > 0) {
                dates.add(normalizeMonth(inputMonth - i) + inputYear);
            } else {
                dates.add(normalizeMonth(12 - (i - inputMonth)) + (inputYear - 1));
            }
        }
        dates = Lists.reverse(dates);

        return dates.toArray(new String[0]);
    }

    /**
     * Normalize dates according to acceptable format in 3270 system for IP logs (21.07.2017 =
     * 2017-07-21).
     *
     * @param time_of_incident time of incident
     * @param time_reported time reported
     * @param expectedDateRange
     * @return normalized dates
     * @throws ParseException
     */
    public static String[] normalizeDatesForIP(String time_of_incident, String time_reported, int expectedDateRange) throws ParseException {
        DateFormat originalFormat = new SimpleDateFormat("dd.MM.yyyy");
        DateFormat targetFormat = new SimpleDateFormat("yyyy-MM-dd");

        Calendar c = Calendar.getInstance();
        c.setTime(originalFormat.parse(time_of_incident));
        c.add(Calendar.MONTH, -expectedDateRange);

        String[] dates = {targetFormat.format(c.getTime()), targetFormat.format(originalFormat.parse(time_reported))};

        return dates;
    }

    /**
     * Normalize Month according to acceptable format in 3270 system (1 = JAN etc).
     *
     * @param month
     * @return normalized month
     */
    public static String normalizeMonth(int month) {
        String currentMonth = "";
        switch (month) {
            case 1:
                currentMonth = "JAN";
                break;
            case 2:
                currentMonth = "FEB";
                break;
            case 3:
                currentMonth = "MAR";
                break;
            case 4:
                currentMonth = "APR";
                break;
            case 5:
                currentMonth = "MAY";
                break;
            case 6:
                currentMonth = "JUN";
                break;
            case 7:
                currentMonth = "JUL";
                break;
            case 8:
                currentMonth = "AUG";
                break;
            case 9:
                currentMonth = "SEP";
                break;
            case 10:
                currentMonth = "OCT";
                break;
            case 11:
                currentMonth = "NOV";
                break;
            case 12:
                currentMonth = "DEC";
                break;
            default:
                break;
        }
        return currentMonth;
    }

    /**
     * Normalize account number. Gets only leading digits from the account passed by parameter. If
     * found account contains less than 8 digits, then append missing "0" at the beginning.
     *
     * Examples: - "210140011" will return the same value "210140011", - "089621XXXXXXB03" will
     * return only "00089621"
     *
     * @param accountNumber - account number for normalization
     * @return normalized account number
     */
    public static String normalizeAccountNumber(String accountNumber) {
        if (StringUtils.isBlank(accountNumber)) {
            return "";
        }
        Matcher matcher = Pattern.compile("\\d+").matcher(accountNumber);
        if (matcher.find()) {
            String resultString = accountNumber.substring(matcher.start(), matcher.end());
            String leadingZeros = StringUtils.repeat("0", 8 - resultString.length());

            return leadingZeros + resultString;
        } else {
            throw new IllegalArgumentException("Wrong account number");
        }
    }
    
    /**
     * This piece of code will check if the product account number has non
     * functional leading zeros and remove them for the transaction only but
     * still maintain the leading zeros for audit trail
     * 
     * e.g 00004754384 -> 004754384 and 004754384 -> 004754384
     */
    public static String normalizeSapAccountNumber(String accountNumber) {
        if (!StringUtils.isBlank(accountNumber) && accountNumber.length() == 11 && accountNumber.startsWith("00")) {
            return accountNumber.replaceFirst("00", "").trim();
        }
        return accountNumber;
    }    

    /**
     * Replace all white spaces with underline character within column name passed by parameter.
     *
     * @param column - value of column to be formatted
     * @return updated column value
     */
    public static String alignColumnNameForExport(String column) {
        if (null != column) {
            String out = column.replaceAll("[\\s]", "_").toLowerCase();
            logger.info("Align column: " + column + " to " + out);
            return out;
        }
        return null;
    }

    /**
     * Normalize customer number. Removes all leading "0" digits in the beginning and returns the
     * rest.
     *
     * Examples: - "210140011" will return the same value "210140011", - "0000989094300" will return
     * only "989094300"
     *
     * @param customerNumber for normalization
     * @return normalized customer number
     */
    public static String normalizeCustomerNumber(String customerNumber) {
        Pattern p = Pattern.compile("^[0]+(\\d+)$");
        Matcher m = p.matcher(customerNumber);

        return m.find() ? m.group(1) : customerNumber;
    }

    
    /**
     * Normalize customer number. Removes all leading and ending "0" digits in the beginning and at the end and returns the rest.
     *
     * Examples: - "210140011" will return the same value "210140011", - "0000989094300" will return only "9890943"
     *
     * @param account for normalization
     * @return normalized customer account
     */
    public static String removeLeadingAndEndingZeros(String account) {
        account = normalizeCustomerNumber(account);
        return StringUtils.stripEnd(account, "0");
    }

    /**
     * Normalize an amount of money from n - unknown decimal points less than 2 to 2 decimal point
     * E.g if the amount is R15.1, the functions changes it to R15.10 But if the amount is Just R15
     * then it leaves it as is
     *
     * @param amount
     * @param scale
     * @return
     */
    public static String normalizeRefundAmount(String amount, int scale) {
        return new BigDecimal(amount).setScale(scale, BigDecimal.ROUND_HALF_UP).toPlainString();
    }

    /**
     * This function simply changes the date format from / to - separation method E.g 12/12/12 to 12-12-12
     */
    public static String formatDate(String date, String actualDatePattern, String expectedDatePattern) {
        SimpleDateFormat actualSdf = new SimpleDateFormat(actualDatePattern);
        SimpleDateFormat expectedSdf = new SimpleDateFormat(expectedDatePattern);

        try {
            return expectedSdf.format(actualSdf.parse(date));
        } catch (ParseException e) {
            return date;
        }
    }

    /**
     * Utility function that returns WF custom attribute value. WF Studio ver 8.1 doesn't support
     * Custom Attributes, therefore to make it work locally please define required Custom Attirbute
     * as input parameter within input CSV file.
     *
     * @param key - key if customer attribute
     * @param binding - Binding implementation, usually from webharvest config
     * @return custom attribute string value
     */
    public static String getCustomAttribute(Binding binding, String key) {
        String tryBinding = binding.hasVariable(key) ? binding.getVariable(key).toString() : null;
        if (tryBinding != null) {
            return tryBinding;
        }

        WebHarvestTaskItem item = BindingUtils.getWebHarvestTaskItem(binding);
        if (item != null) {

            java.util.Map<String, Object> customRunAttrMap = item.getRun().getCustomAttrMap();
            return customRunAttrMap != null ? (customRunAttrMap.containsKey(key) ? customRunAttrMap.get(key).toString() : null) : null;
        }
        return null;
    }
    
    /**
     * Adds leading "0" digits in the beginning up to expected length.
     *
     * @param stringValue
     * @param expectedStringLength
     * @return normalized string value
     */
    public static String addLeadingZeros(String stringValue, int expectedStringLength) {
        return StringUtils.repeat("0", expectedStringLength - stringValue.length()) + stringValue;
    }
}
