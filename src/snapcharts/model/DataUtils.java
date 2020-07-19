package snapcharts.model;
import java.util.Arrays;

/**
 * Utilities for Data.
 */
public class DataUtils {

    /**
     * Returns a String for an array of data values.
     */
    public static String getStringForDoubleArray(double theVals[])
    {
        StringBuffer sb = new StringBuffer();
        for (double val : theVals) {
            sb.append(val);
            sb.append(", ");
        }
        sb.delete(sb.length()-2, sb.length());
        return sb.toString();
    }

    /**
     * Returns an array of double values for given comma separated string.
     */
    public static double[] getDoubleArrayForString(String aStr)
    {
        String valStrs[] = aStr.split("\\s*,\\s*");
        int len = valStrs.length;
        int count = 0;
        double vals[] = new double[len];
        for (String valStr : valStrs) {
            if (valStr.length() > 0) {
                try {
                    double val = Double.valueOf(valStr);
                    vals[count++] = val;
                } catch (Exception e) {
                }
            }
        }
        if (count<len)
            vals = Arrays.copyOf(vals, count);
        return vals;
    }
}
