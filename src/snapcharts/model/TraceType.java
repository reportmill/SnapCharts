package snapcharts.model;
import snap.props.StringCodec;
import snap.util.ArrayUtils;
import snapcharts.modelx.*;

/**
 * This class defines the type of a trace (XY, Bar, Pie, etc.) and functions as kind of an extendable enum.
 */
public class TraceType implements StringCodec.Codeable {

    // The name
    private String  _name;

    // The Trace class
    private Class<? extends Trace> _traceClass;

    // A list of all known types
    private static TraceType[]  _allTypes = new TraceType[0];

    // Constants for known TraceTypes
    public static final TraceType Scatter = new TraceType("Scatter", ScatterTrace.class);
    public static final TraceType Bar = new TraceType("Bar", BarTrace.class);
    public static final TraceType Pie = new TraceType("Pie", PieTrace.class);
    public static final TraceType Polar = new TraceType("Polar", PolarTrace.class);
    public static final TraceType Contour = new TraceType("Contour", ContourTrace.class);
    public static final TraceType PolarContour = new TraceType("PolarContour", PolarContourTrace.class);
    public static final TraceType Bar3D = new TraceType("Bar3D", Bar3DTrace.class);
    public static final TraceType Pie3D = new TraceType("Pie3D", Pie3DTrace.class);
    public static final TraceType Line3D = new TraceType("Line3D", Line3DTrace.class);
    public static final TraceType Contour3D = new TraceType("Contour3D", Contour3DTrace.class);

    /**
     * Constructor.
     */
    public TraceType()  { _name = "Unknown"; }

    /**
     * Constructor.
     */
    public TraceType(String aName, Class<? extends Trace> aClass)
    {
        _name = aName;
        _traceClass = aClass;


        // Add to AllTypes
        _allTypes = ArrayUtils.add(_allTypes, this);
    }

    /**
     * Returns the name.
     */
    public String getName()  { return _name; }

    /**
     * Returns the Trace subclass for this type.
     */
    public Class<? extends Trace> getTraceClass()  { return _traceClass; }

    /**
     * Returns whether type is XY type (not bar or pie).
     */
    public boolean isXYType()
    {
        return this == Scatter || this == Contour;
    }

    /**
     * Returns whether type is Bar type (not BAR or BAR_3D).
     */
    public boolean isBarType()  { return this == Bar || this == Bar3D; }

    /**
     * Returns whether type is Polar type (Polar or PolarContour).
     */
    public boolean isPolarType()
    {
        return this == Polar || this == PolarContour;
    }

    /**
     * Returns whether type is Contour type (Contour or PolarContour).
     */
    public boolean isContourType()
    {
        return this == Contour || this == PolarContour || this == Contour3D;
    }

    /**
     * Returns whether type supports multiple Y Axes.
     */
    public boolean isMultiYAxisType()
    {
        return this == Scatter;
    }

    /**
     * Returns whether type is 3D.
     */
    public boolean is3D()
    {
        return this == Bar3D || this == Pie3D || this == Line3D || this == Contour3D;
    }

    /**
     * Returns a String representation of this object.
     */
    @Override
    public String codeString()
    {
        return _name;
    }

    /**
     * Configures this object from a String representation.
     */
    @Override
    public StringCodec.Codeable decodeString(String aString)
    {
        return getTypeForName(aString);
    }

    /**
     * Standard toString implementation.
     */
    @Override
    public String toString()
    {
        return "TraceType: " + _name;
    }

    /**
     * Returns all known trace types.
     */
    public static TraceType[] getAllTypes()  { return _allTypes; }

    /**
     * Returns the known type for given type.
     */
    public static TraceType getTypeForName(String aName)
    {
        for (TraceType traceType : _allTypes)
            if (traceType.getName().equals(aName))
                return traceType;
        for (TraceType traceType : _allTypes)
            if (traceType.getName().equalsIgnoreCase(aName))
                return traceType;
        return null;
    }
}
