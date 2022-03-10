/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.model;

/**
 * Constants for Chart types.
 */
public enum ChartType {

    /** Bar chart */
    BAR,

    /** Pie chart */
    PIE,

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
    LINE_3D,

    /** Contour chart */
    CONTOUR_3D;

    /**
     * Returns whether type is XY type (not bar or pie).
     */
    public boolean isXYType()
    {
        return this==SCATTER || this==CONTOUR;
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
        return this == CONTOUR || this == POLAR_CONTOUR || this == CONTOUR_3D;
    }

    /**
     * Returns whether type supports multiple Y Axes.
     */
    public boolean isMultiYAxisType()
    {
        return this == SCATTER;
    }

    /**
     * Returns whether type is 3D.
     */
    public boolean is3D()
    {
        return this == BAR_3D || this == PIE_3D || this == LINE_3D || this == CONTOUR_3D;
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
            case LINE_3D: return "Line3D";
            case SCATTER: return "Scatter";
            case POLAR_CONTOUR: return "PolarContour";
            case CONTOUR_3D: return "Contour3D";
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
        // Normalize string
        String str = aStr.toUpperCase();
        str = str.replace(" ", "_").replace("-", "_");

        // Legacy
        if (str.equals("LINE") || str.equals("AREA"))
            return SCATTER;

        // Return Enum
        try { return ChartType.valueOf(str); }
        catch(Exception e)
        {
            System.err.println("ChartType.get: Couldn't parse chart type string: " + aStr);
            return SCATTER;
        }
    }
}
