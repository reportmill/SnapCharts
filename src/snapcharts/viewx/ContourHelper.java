package snapcharts.viewx;
import snap.geom.Rect;
import snap.gfx.Color;
import snap.gfx.GradientPaint;
import snap.gfx.Image;
import snap.gfx.Painter;
import snapcharts.data.DataSet;
import snapcharts.model.Chart;
import snapcharts.model.ContourAxis;
import snapcharts.modelx.ContourTrace;
import snapcharts.model.Trace;
import snapcharts.data.MinMax;
import snapcharts.view.ChartHelper;

/**
 * This is a class that is a helper for ContourChartHelper and PolarContourChartHelper (a helper's helper), so the
 * code can be reused (and since there is no multiple inheritence that would let PolarContourChartHelper inherit from
 * both PolarChartHelper and ContourChartHelper).
 */
public class ContourHelper {

    // The ChartHelper
    private ChartHelper  _chartHelper;

    // The ContourTrace
    private ContourTrace  _contourTrace;

    // The number of contour levels
    private int  _levelsCount;

    // The contour range values (min/max) for each contour level
    private MinMax[]  _contourRanges;

    // The array of colors
    private Color[]  _colors;

    // The ColorMap GradientPaint if RenderSmooth
    private GradientPaint  _colorMapPaint;

    // The ColorMap image
    private Image  _colorMapImage;

    /**
     * Constructor.
     */
    public ContourHelper(ChartHelper aChartHelper, ContourTrace aContourTrace)
    {
        _chartHelper = aChartHelper;
        _contourTrace = aContourTrace;
    }

    /**
     * Returns the ContourProps.
     */
    public ContourTrace getContourProps()  { return _contourTrace; }

    /**
     * Returns whether to show contour lines.
     */
    public boolean isShowLines()
    {
        ContourTrace contourTrace = getContourProps();
        return contourTrace.isShowLines();
    }

    /**
     * Returns whether to show mesh.
     */
    public boolean isShowMesh()
    {
        ContourTrace contourTrace = getContourProps();
        return contourTrace.isShowMesh();
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
        Trace trace = _chartHelper.getContent().getTrace(0);
        DataSet dataSet = trace.getProcessedData();
        double zmin = dataSet.getMinZ();
        double zmax = dataSet.getMaxZ();

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
        GradientPaint colorMapPaintV = getColorMapPaint();
        GradientPaint colorMapPaintH = new GradientPaint(0, colorMapPaintV.getStops());

        // Expand to rect
        int count = getContourCount();
        colorMapPaintH = colorMapPaintH.copyForRect(new Rect(0, 0, count, 1));

        // Create image and fill with gradient
        Image img = Image.getImageForSizeAndScale(count, 1, false, 1);
        Painter pntr = img.getPainter();
        pntr.setPaint(colorMapPaintH);
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
    public GradientPaint getColorMapPaint()
    {
        if (_colorMapPaint != null) return _colorMapPaint;
        GradientPaint gradientPaint = createColorMapPaint();
        return _colorMapPaint = gradientPaint;
    }

    /**
     * Returns gradient paint for contours.
     */
    private GradientPaint createColorMapPaint()
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
        return new GradientPaint(-90, stops);
    }

    /**
     * Returns an image for color map.
     */
    public Image getColorMapImage()
    {
        if (_colorMapImage != null) return _colorMapImage;
        Image image = createColorMapImage();
        return _colorMapImage = image;
    }

    /**
     * Creates an image for color map.
     */
    private Image createColorMapImage()
    {
        Image image = Image.getImageForSizeAndScale(1, 200, false, 1);
        Painter pntr = image.getPainter();
        GradientPaint colorMapPaint = getColorMapPaint();
        pntr.setPaint(colorMapPaint);
        pntr.fillRect(0, 0, 1, 200);
        return image;
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
        Trace trace = _chartHelper.getContent().getTrace(0);
        DataSet dataSet = trace.getProcessedData();
        double zmin = dataSet.getMinZ();
        double zmax = dataSet.getMaxZ();
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
        _colorMapPaint = null;
    }
}
