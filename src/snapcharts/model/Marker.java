package snapcharts.model;
import snap.util.SnapUtils;

/**
 * This ChartPart subclass is used to highlight or annotate an area on the chart.
 */
public class Marker extends ChartPart {

    // The marker location
    private double  _x, _y;

    // The marker size
    private double  _width, _height;

    // The Coordinate space of x coords
    private CoordSpace  _coordSpaceX;

    // The coordinate space of y coords
    private CoordSpace  _coordSpaceY;

    // Whether X coordinates are provided as a fraction of CoordSpace.Max - CoordSpace.Min
    private boolean  _fractionalX;

    // Whether X coordinates are provided as a fraction of CoordSpace.Max - CoordSpace.Min
    private boolean  _fractionalY;

    // Constant for coordinate space
    public enum CoordSpace  {

        // The constants
        ChartBounds, DataBounds, X, Y, Y2, Y3, Y4;

        // Map to AxisType
        public AxisType getAxisType()  { return getAxisTypeForCoordSpace(this); }
    }

    // Constants for properties
    public static final String X_Prop = "X";
    public static final String Y_Prop = "Y";
    public static final String Width_Prop = "Width";
    public static final String Height_Prop = "Height";
    public static final String CoordSpaceX_Prop = "CoordSpaceX";
    public static final String CoordSpaceY_Prop = "CoordSpaceY";
    public static final String FractionalX_Prop = "FractionalX";
    public static final String FractionalY_Prop = "FractionalY";

    // Constants for defaults
    private static final CoordSpace  DEFAULT_COORD_SPACE = CoordSpace.DataBounds;

    /**
     * Constructor.
     */
    public Marker()
    {
        super();

        // Set default values
        _coordSpaceX = DEFAULT_COORD_SPACE;
        _coordSpaceY = DEFAULT_COORD_SPACE;
    }

    /**
     * Returns the X location of marker.
     */
    public double getX()  { return _x; }

    /**
     * Sets the X location of marker.
     */
    public void setX(double aValue)
    {
        if (aValue == _x) return;
        firePropChange(X_Prop, _x, _x = aValue);
    }

    /**
     * Returns the Y location of marker.
     */
    public double getY()  { return _y; }

    /**
     * Sets the Y location of marker.
     */
    public void setY(double aValue)
    {
        if (aValue == _y) return;
        firePropChange(Y_Prop, _y, _y = aValue);
    }

    /**
     * Returns the width of marker.
     */
    public double getWidth()  { return _width; }

    /**
     * Sets the width of marker.
     */
    public void setWidth(double aValue)
    {
        if (aValue == _width) return;
        firePropChange(Width_Prop, _width, _width = aValue);
    }

    /**
     * Returns the height of marker.
     */
    public double getHeight()  { return _height; }

    /**
     * Sets the height of marker.
     */
    public void setHeight(double aValue)
    {
        if (aValue == _height) return;
        firePropChange(Height_Prop, _height, _height = aValue);
    }

    /**
     * Returns the CoordSpace of X/Width properties.
     */
    public CoordSpace getCoordSpaceX()  { return _coordSpaceX; }

    /**
     * Sets the CoordSpace of X/Width properties.
     */
    public void setCoordSpaceX(CoordSpace aCoordSpace)
    {
        if (aCoordSpace == getCoordSpaceX()) return;
        firePropChange(CoordSpaceX_Prop, _coordSpaceX, _coordSpaceX = aCoordSpace);
    }

    /**
     * Returns the CoordSpace of Y/Height properties.
     */
    public CoordSpace getCoordSpaceY()  { return _coordSpaceY; }

    /**
     * Sets the CoordSpace of Y/Height properties.
     */
    public void setCoordSpaceY(CoordSpace aCoordSpace)
    {
        if (aCoordSpace == getCoordSpaceY()) return;
        firePropChange(CoordSpaceY_Prop, _coordSpaceY, _coordSpaceY = aCoordSpace);
    }

    /**
     * Returns whether X/Width coordinates are provided as fractions of CoordSpace.Max - CoordSpace.Min.
     */
    public boolean isFractionalX()  { return _fractionalX; }

    /**
     * Sets whether X/Width coordinates are provided as fractions of CoordSpace.Max - CoordSpace.Min.
     */
    public void setFractionalX(boolean aValue)
    {
        if (aValue == isFractionalX()) return;
        firePropChange(FractionalX_Prop, _fractionalX, _fractionalX = aValue);
    }

    /**
     * Returns whether Y/Height coordinates are provided as fractions of CoordSpace.Max - CoordSpace.Min.
     */
    public boolean isFractionalY()  { return _fractionalY; }

    /**
     * Sets whether Y/Height coordinates are provided as fractions of CoordSpace.Max - CoordSpace.Min.
     */
    public void setFractionalY(boolean aValue)
    {
        if (aValue == isFractionalY()) return;
        firePropChange(FractionalY_Prop, _fractionalY, _fractionalY = aValue);
    }

    /**
     * Override for subclass properties.
     */
    @Override
    public Object getPropValue(String aPropName)
    {
        switch (aPropName) {

            // X, Y, Width, Height
            case X_Prop: return getX();
            case Y_Prop: return getY();
            case Width_Prop: return getWidth();
            case Height_Prop: return getHeight();

            // CoordSpaceX, CoordSpaceY, FractionalX, FractionalY
            case CoordSpaceX_Prop: return getCoordSpaceX();
            case CoordSpaceY_Prop: return getCoordSpaceY();
            case FractionalX_Prop: return isFractionalX();
            case FractionalY_Prop: return isFractionalY();

            // Do normal version
            default: return super.getPropValue(aPropName);
        }
    }

    /**
     * Override for subclass properties.
     */
    @Override
    public void setPropValue(String aPropName, Object aValue)
    {
        switch (aPropName) {

            // X, Y, Width, Height
            case X_Prop: setX(SnapUtils.doubleValue(aValue)); break;
            case Y_Prop: setY(SnapUtils.doubleValue(aValue)); break;
            case Width_Prop: setWidth(SnapUtils.doubleValue(aValue)); break;
            case Height_Prop: setHeight(SnapUtils.doubleValue(aValue)); break;

            // CoordSpaceX, CoordSpaceY, FractionalX, FractionalY
            case CoordSpaceX_Prop: setCoordSpaceX((CoordSpace) aValue); break;
            case CoordSpaceY_Prop: setCoordSpaceY((CoordSpace) aValue); break;
            case FractionalX_Prop: setFractionalX(SnapUtils.boolValue(aValue)); break;
            case FractionalY_Prop: setFractionalY(SnapUtils.boolValue(aValue)); break;

            // Do normal version
            default: super.setPropValue(aPropName, aValue);
        }
    }

    /**
     * Override for subclass properties.
     */
    @Override
    public Object getPropDefault(String aPropName)
    {
        switch (aPropName) {

            // CoordSpaceX, CoordSpaceY
            case CoordSpaceX_Prop: return DEFAULT_COORD_SPACE;
            case CoordSpaceY_Prop: return DEFAULT_COORD_SPACE;

            // Do normal version
            default: return super.getPropValue(aPropName);
        }
    }

    /**
     * Returns an AxisType for CoordSpace.
     */
    private static AxisType getAxisTypeForCoordSpace(CoordSpace aCoordSpace)
    {
        switch (aCoordSpace) {
            case X: return AxisType.X;
            case Y: return AxisType.Y;
            case Y2: return AxisType.Y2;
            case Y3: return AxisType.Y3;
            case Y4: return AxisType.Y4;
            default: return null;
        }
    }
}
