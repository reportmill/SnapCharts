package snapcharts.model;

public enum AxisType {

    X,
    Y,
    Y2,
    Y3,
    Y4,
    Z;

    /**
     * Returns whether AxisType is Y type.
     */
    public boolean isAnyY()  { return this==Y || this==Y2 || this==Y3 || this==Y4; }
}
