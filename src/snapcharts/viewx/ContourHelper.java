package snapcharts.viewx;

import snap.geom.Rect;
import snap.gfx.Color;
import snap.gfx.GradientPaint;
import snap.gfx.Image;
import snap.gfx.Painter;
import snapcharts.model.Chart;
import snapcharts.modelx.ContourProps;
import snapcharts.model.DataSet;
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

    // The number of contour levels
    private int  _levelsCount;

    // The contour range values (min/max) for each contour level
    private MinMax[]  _contourRanges;

    // The array of colors
    private Color[]  _colors;

    /**
     * Constructor.
     */
    public ContourHelper(ChartHelper aChartHelper)
    {
        _chartHelper = aChartHelper;
    }

    /**
     * Returns the ContourProps.
     */
    public ContourProps getContourProps()
    {
        Chart chart = _chartHelper.getChart();
        return chart.getTypeHelper().getContourProps();
    }

    /**
     * Returns whether to show contour lines.
     */
    public boolean isShowLines()
    {
        ContourProps contourProps = getContourProps();
        return contourProps.isShowLines();
    }

    /**
     * Returns whether to show mesh.
     */
    public boolean isShowMesh()
    {
        ContourProps contourProps = getContourProps();
        return contourProps.isShowMesh();
    }

    /**
     * Returns the number of contours.
     */
    public int getContourCount()
    {
        if (_levelsCount > 0) return _levelsCount;
        ContourProps contourProps = getContourProps();
        return _levelsCount = contourProps.getLevelCount();
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
        DataSet dset = _chartHelper.getDataSetList().getDataSet(0);
        double zmin = dset.getMinZ();
        double zmax = dset.getMaxZ();

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
        if (_colors!=null) return _colors;

        // Create Gradient
        String[] chex = {
                "071E91", "163BA4", "2E6BB9", "469CD0",
                "5DCAE6", "75FBFD", "82FCC3", "A2FC8E",
                "CEFE64", "FFFF54", "F5C142", "ED8732",
                "E85127", "E63222", "AD2317", "75140C"
        };
        Color[] gcols = new Color[chex.length];
        for (int i=0; i<chex.length; i++) gcols[i] = new Color("#" + chex[i]);
        double[] offsets = new double[chex.length];
        for (int i=0; i<chex.length; i++) offsets[i] = 1d/(chex.length-1) * i;
        offsets[chex.length-1] = 1;

        GradientPaint paint = new GradientPaint(0, GradientPaint.getStops(offsets, gcols));

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
        for (int i=0; i<count; i++)
            colors[i] = new Color(img.getRGB(i, 0));

        // Return colors
        return _colors = colors;
    }

    /**
     * Resets cached values.
     */
    public void resetCachedValues()
    {
        _levelsCount = 0;
        _contourRanges = null;
        _colors = null;
    }
}
