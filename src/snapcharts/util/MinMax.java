package snapcharts.util;

/**
 * A class to hold a min and max value.
 */
public class MinMax {

    // The min value
    private double  _min;

    // The max value
    private double  _max;

    /**
     * Constructor.
     */
    public MinMax(double aMin, double aMax)
    {
        if (aMax<aMin) {
            System.err.println();
        }
        _min = aMin;
        _max = aMax;
    }

    /**
     * Returns the min value.
     */
    public double getMin()  { return _min; }

    /**
     * Returns the max value.
     */
    public double getMax()  { return _max; }
}
