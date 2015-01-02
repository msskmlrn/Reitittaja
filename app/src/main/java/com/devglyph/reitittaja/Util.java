package com.devglyph.reitittaja;

import com.devglyph.reitittaja.models.RouteLeg;

public class Util {

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
            return lineCode.toString() + lineLetter;
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
}
