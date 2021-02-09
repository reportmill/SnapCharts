package snapcharts.views;
import snap.geom.Rect;
import snap.gfx.Color;
import snap.gfx.GradientPaint;
import snap.gfx.Image;
import snap.gfx.Painter;
import snapcharts.model.AxisType;
import snapcharts.model.ChartType;
import snapcharts.model.DataSet;
import snapcharts.model.DataSetList;
import java.text.DecimalFormat;
import java.util.List;

/**
 * A ChartHelper for common Contour types.
 */
public class ChartHelperContour extends ChartHelper {

    // The array of colors
    private Color[]  _colors;

    // Format
    private static DecimalFormat _fmt = new DecimalFormat("#.##");

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
     * Returns the contour level at index.
     */
    public double getContourValue(int anIndex)
    {
        DataSet dset = getDataSetList().getDataSet(0);
        double zmin = dset.getMinZ();
        double zmax = dset.getMaxZ();
        int count = getContourCount();
        double delta = (zmax - zmin) / count;
        return zmin + delta * anIndex;
    }

    /**
     * Returns the contour level label at index.
     */
    public String getContourLabel(int anIndex)
    {
        double val = getContourValue(anIndex);
        return _fmt.format(val);
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
}
