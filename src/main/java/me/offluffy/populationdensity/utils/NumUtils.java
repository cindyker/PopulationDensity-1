package me.offluffy.populationdensity.utils;

public class NumUtils {

    /**
     * Check if a number is a valid Integer
     *
     * @param query The query to check
     * @return True if the query is capable of being casted to an int
     */
    public static boolean isInteger(String query) {
        Integer i;
        try {
            i = Integer.parseInt(query);
        } catch (Exception e) {
            i = null;
        }
        return i != null;
    }

    /**
     * Check if a number is a valid Double
     *
     * @param query The query to check
     * @return True if the query is capable of being casted to a double
     */
    public static boolean isDouble(String query) {
        Double d;
        try {
            d = Double.parseDouble(query);
        } catch (Exception e) {
            d = null;
        }
        return d != null;
    }

    /**
     * Check if a number is a valid Long
     *
     * @param query The query to check
     * @return True if the query is capable of being casted to a long
     */
    public static boolean isLong(String query) {
        Long l;
        try {
            l = Long.parseLong(query);
        } catch (Exception e) {
            l = null;
        }
        return l != null;
    }

    /**
     * Generates a random number
     *
     * @param min The lowest number allowed
     * @param max The highest number allowed
     * @return Returns a random int between the specified boundaries
     */
    public static int rand(int min, int max) {
        return min + (int) (Math.random() * ((max - min) + 1));
    }
}
