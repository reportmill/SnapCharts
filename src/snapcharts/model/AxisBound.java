package snapcharts.model;

/**
 * A type to describe how Axis min/max are determined.
 */
public enum AxisBound {

    /** Bound type for get pleasing value from data value. */
    AUTO,

    // Bound type for use exact data value. */
    DATA,

    // Bound type for user provided value
    VALUE;

    public static AxisBound get(String aValue)
    {
        try { return AxisBound.valueOf(aValue.toUpperCase()); }
        catch (Exception e) { return AUTO; }
    }
}
