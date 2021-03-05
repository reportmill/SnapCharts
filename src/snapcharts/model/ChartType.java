package snapcharts.model;

/**
 * Constants for Chart types.
 */
public enum ChartType {

    /** Bar chart */
    BAR,

    /** Pie chart */
    PIE,

    /** Line chart */
    LINE,

    /** Area chart */
    AREA,

    /** Scatter chart */
    SCATTER,

    /** Contour chart */
    CONTOUR,

    /** Polar chart */
    POLAR,

    /** Polar Contour chart */
    POLAR_CONTOUR,

    /** 3D Bar chart */
    BAR_3D,

    /** 3D Pie chart */
    PIE_3D,

    /** 2D Line chart */
    LINE_3D;

    /**
     * Returns whether type is XY type (not bar or pie).
     */
    public boolean isXYType()
    {
        return this==LINE || this==AREA || this==SCATTER || this==CONTOUR;
    }

    /**
     * Returns whether type is Bar type (not BAR or BAR_3D).
     */
    public boolean isBarType()  { return this==BAR || this==BAR_3D; }

    /**
     * Returns whether type is Polar type (Polar or PolarContour).
     */
    public boolean isPolarType()
    {
        return this == POLAR || this == POLAR_CONTOUR;
    }

    /**
     * Returns whether type is Contour type (Contour or PolarContour).
     */
    public boolean isContourType()
    {
        return this == CONTOUR || this == POLAR_CONTOUR;
    }

    /**
     * Returns the name in plain camel-case format.
     */
    public String getStringPlain()
    {
        switch (this)
        {
            case BAR: return "Bar";
            case BAR_3D: return "Bar3D";
            case PIE_3D: return "Pie3D";
            case PIE: return "Pie";
            case LINE: return "Line";
            case LINE_3D: return "Line3D";
            case SCATTER: return "Scatter";
            case POLAR_CONTOUR: return "PolarContour";
            default: break;
        }

        // Just return name with first letter capitalized
        String str = toString();
        return str.charAt(0) + str.substring(1).toLowerCase();
    }

    /**
     * Returns the ChartType for string.
     */
    public static ChartType get(String aStr)
    {
        String str = aStr.toUpperCase();
        str = str.replace(" ", "_").replace("-", "_");
        try { return ChartType.valueOf(str); }
        catch(Exception e)
        {
            System.err.println("ChartType.get: Couldn't parse chart type string: " + aStr);
            return LINE;
        }
    }
}
