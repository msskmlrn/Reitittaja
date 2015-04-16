package com.devglyph.reitittaja;

import android.util.Log;

import com.devglyph.reitittaja.models.RouteLeg;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class Util {

    public static final String DATE_FORMAT_FULL = "yyyyMMddHHmm";
    public static final String DATE_FORMAT_TIME = "HHmm";

    private final static String LOG_TAG = Util.class.getSimpleName();

    /**
     * Construct a human readable line code for the leg
     * @param type
     * @param joreCode
     * @return a human readable line code
     */
    public static String parseJoreCode(int type, String joreCode) {
        //line codes for walk, metro, ferry and cycle are just the mode names
        if (type == RouteLeg.WALK) {
            return "walk";
        }
        else if (type == RouteLeg.METRO) {
            return "metro";
        }
        else if (type == RouteLeg.FERRY) {
            return "ferry";
        }
        else if (type == RouteLeg.CYCLE) {
            return "cycle";
        }
        //return the letter for local trains
        else if (type == RouteLeg.TRAIN) {
            return joreCode.substring(4, 5);
        }
        //parse the line code from the string
        else {
            //get the line code (characters 1 - 4) and remove all leading zeroes
            String lineCode = joreCode.substring(1,4).replaceAll("^0+", "");
            //get the letter variant, if any
            String lineLetter = joreCode.substring(4,5);
            return lineCode + lineLetter;
        }
    }

    /**
     * Parse the type string to an integer that matches the RouteLeg constants
     * @param type
     * @return an integer that matches the RouteLeg constants
     */
    public static int parseType(String type) {

        if (type.equals("walk")) {
            return RouteLeg.WALK;
        }
        else {
            return tryParsingStringToInt(type);
        }
    }

    /**
     * Try parsing a string to int
     * @param string
     * @return the string in int if success, else return 0
     */
    public static int tryParsingStringToInt(String string) {
        int number;
        try {
            number = Integer.parseInt(string);
        }
        catch (NumberFormatException ex) {
            number = 0;
        }
        return number;
    }

    /**
     * Round the given distance add append " m" or " km" to the return value
     * @param distance, the distance to be rounded in meters
     * @return a rounded distance with " m" or " km" appended to the value
     */
    public static String roundDistance(double distance) {
        if (distance < 1000) {
            return Math.round(distance) +" m";
        }
        else {
            //kilometers to be shown, so round the number and remove trailing zeroes
            double temp = distance / 1000;
            BigDecimal b = BigDecimal.valueOf(temp).setScale(1, BigDecimal.ROUND_HALF_UP);
            return b.toString() + " km";
        }
    }

    public static Date parseDate(String dateFormat, String time) {

        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, Locale.US);

        try {
            return sdf.parse(time);
        }
        catch (ParseException e) {
            Log.e(LOG_TAG, "Cannot parse time", e);
            return null;
        }
    }

    public static String parseDate(String dateFormat, long time) {

        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, Locale.US);

        try {
            return sdf.format(time);
        }
        catch (IllegalArgumentException e) {
            Log.e(LOG_TAG, "Cannot parse time", e);
            return null;
        }
    }

    public static String convertSecondsToHHmmss(long secs) {
        String time = "";
        long original = secs;

        long hours = TimeUnit.SECONDS.toHours(secs);
        secs -= TimeUnit.HOURS.toSeconds(hours);
        long minutes = TimeUnit.SECONDS.toMinutes(secs);
        secs -= TimeUnit.MINUTES.toSeconds(minutes);
        long seconds = TimeUnit.SECONDS.toSeconds(secs);

        //if the duration was at least one hour long then add the hour info
        if (hours > 0) {
            time = time + hours + " h";
        }
        //same goes for the minutes
        if (minutes > 0) {
            time = time + " " + minutes + " min";
        }

        //only add the seconds info if the duration was < 60 seconds
        if (original < 60) {
            time = time + " " + seconds + " s";
        }

        return time;
    }

    public static boolean isBusMode(int mode) {
        if (mode == RouteLeg.HELSINKI_BUS || mode == RouteLeg.ESPOO_BUS ||
                mode == RouteLeg.VANTAA_BUS || mode == RouteLeg.REGION_BUS ||
                mode == RouteLeg.U_LINE_BUS || mode == RouteLeg.HELSINKI_SERVICE_LINE_BUS ||
                mode == RouteLeg.HELSINKI_NIGHT_BUS || mode == RouteLeg.ESPOO_SERVICE_LINE_BUS ||
                mode == RouteLeg.VANTAA_SERVICE_LINE_BUS || mode == RouteLeg.REGION_NIGHT_BUS ||
                mode == RouteLeg.KIRKKONUMMI_BUS || mode == RouteLeg.SIPOO_INTERNAL ||
                mode == RouteLeg.KERAVA_BUS || mode == RouteLeg.KIRKKONUMMI_BUS) {

            return true;
        }
        return false;
    }
}
