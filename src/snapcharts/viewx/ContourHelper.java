package snapcharts.viewx;
import snap.geom.Rect;
import snap.gfx.Color;
import snap.gfx.GradientPaint;
import snap.gfx.Image;
import snap.gfx.Painter;
import snapcharts.model.Chart;
import snapcharts.model.ContourAxis;
import snapcharts.modelx.ContourStyle;
import snapcharts.model.Trace;
import snapcharts.util.MinMax;
import snapcharts.view.ChartHelper;

/**
 * This is a class that is a helper for ContourChartHelper and PolarContourChartHelper (a helper's helper), so the
 * code can be reused (and since there is no multiple inheritence that would let PolarContourChartHelper inherit from
 * both PolarChartHelper and ContourChartHelper).
 */
public class ContourHelper {

    // The ChartHelper
    private ChartHelper  _chartHelper;

    // The ContourStyle
    private ContourStyle  _contourStyle;

    // The number of contour levels
    private int  _levelsCount;

    // The contour range values (min/max) for each contour level
    private MinMax[]  _contourRanges;

    // The array of colors
    private Color[]  _colors;

    // GradientPaint if RenderSmooth
    private GradientPaint  _gradientPaint;

    /**
     * Constructor.
     */
    public ContourHelper(ChartHelper aChartHelper, ContourStyle aContourStyle)
    {
        _chartHelper = aChartHelper;
        _contourStyle = aContourStyle;
    }

    /**
     * Returns the ContourProps.
     */
    public ContourStyle getContourProps()  { return _contourStyle; }

    /**
     * Returns whether to show contour lines.
     */
    public boolean isShowLines()
    {
        ContourStyle contourStyle = getContourProps();
        return contourStyle.isShowLines();
    }

    /**
     * Returns whether to show mesh.
     */
    public boolean isShowMesh()
    {
        ContourStyle contourStyle = getContourProps();
        return contourStyle.isShowMesh();
    }

    /**
     * Returns the number of contours.
     */
    public int getContourCount()
    {
        if (_levelsCount > 0) return _levelsCount;
        Chart chart = _chartHelper.getChart();
        ContourAxis contourAxis = chart.getContourAxis();
        return _levelsCount = contourAxis.getLevelCount();
    }

    /**
     * Returns the contour range for given contour index.
     */
    public MinMax getContourRange(int anIndex)
    {
        MinMax[] contourRanges = getContourRanges();
        return contourRanges[anIndex];
    }

    /**
     * Returns the contour ranges.
     */
    public MinMax[] getContourRanges()
    {
        // If already set, just return
        if (_contourRanges != null) return _contourRanges;

        // Get contour data min/max
        Trace trace = _chartHelper.getTraceList().getTrace(0);
        double zmin = trace.getMinZ();
        double zmax = trace.getMaxZ();

        // Get contour count and delta
        int count = getContourCount();
        double delta = (zmax - zmin) / count;

        // Iterate over contour levels and create/set MinMax for each
        MinMax[] ranges = new MinMax[count];
        for (int i=0; i<count; i++) {
            double min = zmin + delta * i;
            double max = zmin + delta * (i + 1);
            ranges[i] = new MinMax(min, max);
        }

        // Set/return
        return _contourRanges = ranges;
    }

    /**
     * Returns the contour color at given index.
     */
    public Color getContourColor(int anIndex)
    {
        Color[] colors = getContourColors();
        return colors[anIndex];
    }

    /**
     * Returns the colors.
     */
    public Color[] getContourColors()
    {
        // If colors already set, just return
        if (_colors != null) return _colors;

        // Create Gradient
        GradientPaint gradientVert = getColorMapGradientPaint();
        GradientPaint paint = new GradientPaint(0, gradientVert.getStops());

        // Expand to rect
        int count = getContourCount();
        paint = paint.copyForRect(new Rect(0, 0, count, 1));

        // Create image and fill with gradient
        Image img = Image.getImageForSizeAndScale(count, 1, false, 1);
        Painter pntr = img.getPainter();
        pntr.setPaint(paint);
        pntr.fillRect(0, 0, count, 1);

        // Get colors for each step
        Color[] colors = new Color[count];
        for (int i = 0; i < count; i++)
            colors[i] = new Color(img.getRGB(i, 0));

        // Return colors
        return _colors = colors;
    }

    /**
     * Returns the color map colors.
     */
    public Color[] getColorMapColors()
    {
        // Create Gradient
        String[] colorMapHexStrings = {
                "071E91", "163BA4", "2E6BB9", "469CD0",
                "5DCAE6", "75FBFD", "82FCC3", "A2FC8E",
                "CEFE64", "FFFF54", "F5C142", "ED8732",
                "E85127", "E63222", "AD2317", "75140C"
        };

        // Create ColorMapColors array and fill with colors from ColorMapHexStrings
        Color[] colorMapColors = new Color[colorMapHexStrings.length];
        for (int i = 0; i < colorMapHexStrings.length; i++)
            colorMapColors[i] = new Color("#" + colorMapHexStrings[i]);

        // Return
        return colorMapColors;
    }

    /**
     * Returns gradient paint for contours.
     */
    public GradientPaint getColorMapGradientPaint()
    {
        if (_gradientPaint != null) return _gradientPaint;
        GradientPaint gradientPaint = createColorMapGradientPaint();
        return _gradientPaint = gradientPaint;
    }

    /**
     * Returns gradient paint for contours.
     */
    private GradientPaint createColorMapGradientPaint()
    {
        // Get ColorMapColors color array
        Color[] colorMapColors = getColorMapColors();
        int count = colorMapColors.length;
        double incr = 1d / (count - 1);

        // Create stops array and fill with stops
        GradientPaint.Stop[] stops = new GradientPaint.Stop[count];
        for (int i = 0; i < count; i++) {
            Color color = colorMapColors[i];
            stops[i] = new GradientPaint.Stop(incr * i, color);
        }

        // Create GradientPath with Stops at 90 deg and return
        return new GradientPaint(90, stops);
    }

    /**
     * Returns the contour color for Z value.
     */
    public Color getContourColorForZ(double aValue)
    {
        // Get ColorCount and RangeCount
        int colorCount = getContourCount();
        int rangeCount = colorCount - 1;

        // Get contour data min/max
        Trace trace = _chartHelper.getTraceList().getTrace(0);
        double zmin = trace.getMinZ();
        double zmax = trace.getMaxZ();
        double rangeLength = (zmax - zmin) / rangeCount;

        // Iterate over ranges until we find containing range
        for (int i = 0; i < rangeCount; i++) {

            // If value within range, calculate fraction of range and blend colors
            double rangeMax = zmin + rangeLength * i;
            if (aValue <= rangeMax) {
                double rangeMin = rangeMax - rangeLength;
                double val = Math.max(aValue - rangeMin, 0);
                double fract = val / rangeLength;
                Color c1 = getContourColor(i);
                Color c2 = getContourColor(i + 1);
                return c1.blend(c2, fract);
            }
        }

        // Return last color
        return getContourColor(colorCount - 1);
    }

    /**
     * Resets cached values.
     */
    public void resetCachedValues()
    {
        _levelsCount = 0;
        _contourRanges = null;
        _colors = null;
        _gradientPaint = null;
    }
}
