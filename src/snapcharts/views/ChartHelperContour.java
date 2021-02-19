package snapcharts.views;
import snap.geom.Rect;
import snap.gfx.Color;
import snap.gfx.GradientPaint;
import snap.gfx.Image;
import snap.gfx.Painter;
import snap.util.PropChange;
import snapcharts.model.AxisType;
import snapcharts.model.ChartType;
import snapcharts.model.DataSet;
import snapcharts.model.DataSetList;
import snapcharts.util.MinMax;
import java.util.List;

/**
 * A ChartHelper for common Contour types.
 */
public class ChartHelperContour extends ChartHelper {

    // The contour range values (min/max) for each contour level
    private MinMax[]  _contourRanges;

    // The array of colors
    private Color[]  _colors;

    /**
     * Constructor.
     */
    protected ChartHelperContour(ChartView aChartView)
    {
        super(aChartView);
    }

    /**
     * Returns the ChartType.
     */
    @Override
    public ChartType getChartType()  { return ChartType.CONTOUR; }

    /**
     * Returns the number of contours.
     */
    public int getContourCount()  { return 16; }

    /**
     * Returns the contour range for given contour index.
     */
    public MinMax getContourRange(int anIndex)
    {
        return getContourRanges()[anIndex];
    }

    /**
     * Returns the contour ranges.
     */
    public MinMax[] getContourRanges()
    {
        // If already set, just return
        if (_contourRanges != null) return _contourRanges;

        // Get contour data min/max
        DataSet dset = getDataSetList().getDataSet(0);
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
        double[] offsets = { 0, .05, .25, .45, .65, .90, 1 };
        Color[] gcols = {
                new Color(1, 0, 96),
                Color.BLUE,
                Color.CYAN,
                new Color(98, 213, 63),
                Color.ORANGE,
                new Color(210, 44, 31),
                new Color(183, 37, 25)
        };
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

    @Override
    protected AxisView createAxisView(AxisType anAxisType)
    {
        AxisView axisView = super.createAxisView(anAxisType);

        // Listen to axisView changes: HERE?!? - YOU'VE GOT TO BE JOKING!!!
        axisView.addPropChangeListener(pc -> clearContours());

        return axisView;
    }

    /**
     * Creates the DataAreas.
     */
    @Override
    protected DataArea[] createDataAreas()
    {
        DataSetList dataSetList = getDataSetList();
        List<DataSet> dsets = dataSetList.getDataSets();
        int dsetCount = dsets.size();

        DataArea[] dataAreas = new DataArea[dsetCount]; // *2
        for (int i=0; i<dsetCount; i++) {
            DataSet dset = dsets.get(i);
            //dataAreas[i*2] = new DataAreaXY(this, dset, ChartType.LINE);
            dataAreas[i] = new DataAreaContour(this, dset); // i*2+1
        }

        return dataAreas;
    }

    /**
     * Clears contours.
     */
    private void clearContours()
    {
        for (DataArea dataArea : getDataAreas())
            if (dataArea instanceof DataAreaContour)
                ((DataAreaContour)dataArea).clearContours();
    }

    /**
     * Returns the contour legend.
     */
    public ContourAxisView getContourView()
    {
        return _chartView.getContourView();
    }

    /**
     * Called when a ChartPart changes.
     */
    protected void chartPartDidChange(PropChange aPC)
    {
        // Do normal version
        super.chartPartDidChange(aPC);

        // Handle DataSet/DataSetList change
        Object src = aPC.getSource();
        if (src instanceof DataSet || src instanceof DataSetList) {
            _contourRanges = null;
        }
    }
}
