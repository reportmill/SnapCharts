package snapcharts.views;

/**
 * Some utility methods for ChartView and friends.
 */
public class ChartViewUtils {


    /**
     * Returns the log of given value.
     */
    public static double log10(double aValue)
    {
        if (aValue<=0)
            return 0;
        return Math.log10(aValue);
    }

    /**
     * Returns the inverse of log10.
     */
    public static double invLog10(double aValue)
    {
        return Math.pow(10, aValue);
    }

}
