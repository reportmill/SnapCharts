package snapcharts.model;

/**
 * Constants for Chart types.
 */
public enum ChartType {

    BAR,
    BAR_3D,
    PIE,
    LINE,
    AREA,
    SCATTER;


    /**
     * Returns whether type is XY type (not bar or pie).
     */
    public boolean isXYType()  { return this!=BAR && this!=BAR_3D && this!=PIE; }

    /**
     * Returns the name in plain camel-case format.
     */
    public String getStringPlain()
    {
        switch (this)
        {
            case BAR: return "Bar";
            case BAR_3D: return "Bar3D";
            case PIE: return "Pie";
            case LINE: return "Line";
            case SCATTER: return "Scatter";
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
        catch(Exception e)  { return BAR; }
    }
}
