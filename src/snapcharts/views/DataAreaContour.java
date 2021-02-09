package snapcharts.views;
import snap.geom.*;
import snap.gfx.*;
import snap.util.PropChange;
import snapcharts.model.Axis;
import snapcharts.model.DataSet;
import snapcharts.util.ContourMaker;

/**
 * A DataArea subclass to display ChartType CONTOUR.
 */
public class DataAreaContour extends DataArea {

    // The Contour helper
    private ChartHelperContour  _contourHelper;

    // The ContourMaker
    private ContourMaker  _contourMaker;

    // The Contours in display coords
    private Shape[]  _contours;

    // The Contours in data coords
    private Shape[]  _dataContours;

    // The mesh path
    private Shape  _meshPath;

    /**
     * Constructor.
     */
    public DataAreaContour(ChartHelper aChartHelper, DataSet aDataSet)
    {
        super(aChartHelper, aDataSet);

        _contourHelper = (ChartHelperContour) aChartHelper;
    }

    /**
     * Returns the number of contours.
     */
    public int getContourCount()
    {
        return _contourHelper.getContourCount();
    }

    /**
     * Returns the contour level at index.
     */
    public double getContourValue(int anIndex)
    {
        return _contourHelper.getContourValue(anIndex);
    }

    /**
     * Returns the contour color at given index.
     */
    public Color getContourColor(int anIndex)
    {
        return _contourHelper.getContourColor(anIndex);
    }

    /**
     * Returns the contour shapes array.
     */
    public Shape[] getContours()
    {
        // If already set, just return
        if (_contours !=null) return _contours;

        // Get contour count and data contours
        int count = getContourCount();
        Shape[] contours = new Shape[count];
        Shape[] dataContours = getDataContours();

        // Iterate over data contours and convert to
        for (int i=0; i<count; i++) {
            Shape dataContour = dataContours[i];
            contours[i] = dataContourToView(dataContour);
        }

        // Set/return
        return _contours = contours;
    }

    /**
     * Returns a shapes array of the data contours (contours in data coords).
     */
    private Shape[] getDataContours()
    {
        // If already set, just return
        if (_dataContours !=null) return _dataContours;

        // Get number of contours, create shape array and get ContourMaker
        int count = getContourCount();
        Shape[] contours = new Shape[count];
        ContourMaker contourMaker = getContourMaker();

        // Iterate for contour count and create/set contour data shape
        for (int i=0; i<count; i++) {
            double valZ = getContourValue(i);
            contours[i] = contourMaker.getContourShape(valZ);
        }

        // Set/return
        return _dataContours = contours;
    }

    /**
     * Returns the contour maker.
     */
    private ContourMaker getContourMaker()
    {
        if (_contourMaker != null) return _contourMaker;
        return _contourMaker = new ContourMaker(getDataSet());
    }

    /**
     * Returns a contour in display coords for given contour in data coords.
     */
    private Shape dataContourToView(Shape aDataContour)
    {
        AxisView axisX = getAxisViewX();
        AxisView axisY = getAxisViewY();
        Path2D path = new Path2D();
        double[] pnts = new double[6];
        PathIter piter = aDataContour.getPathIter(null);
        while (piter.hasNext()) {
            Seg seg = piter.getNext(pnts);
            switch (seg) {
                case MoveTo:
                    double mx = axisX.dataToView(pnts[0]);
                    double my = axisY.dataToView(pnts[1]);
                    path.moveTo(mx, my);
                    break;
                case LineTo:
                    double lx = axisX.dataToView(pnts[0]);
                    double ly = axisY.dataToView(pnts[1]);
                    path.lineTo(lx, ly);
                    break;
                case Close:
                    path.close();
                    break;
                default: System.err.println("Can't happen: " + seg);
            }
        }
        return path;
    }

    /**
     * Clears the Contours.
     */
    public void clearContours()
    {
        _contours = null;
        _meshPath = null;
        repaint();
    }

    /**
     * Returns the mesh path.
     */
    private Shape getMeshPath()
    {
        if (_meshPath!=null) return _meshPath;
        Shape dataPath = getContourMaker().getMesh().getMeshPath();
        Shape meshPath = dataContourToView(dataPath);
        return _meshPath = meshPath;
    }

    /**
     * Paints chart content.
     */
    @Override
    protected void paintChart(Painter aPntr)
    {
        paintContour(aPntr);

        paintMesh(aPntr);
    }

    /**
     * Paints chart content.
     */
    protected void paintContour(Painter aPntr)
    {
        Shape[] contours = getContours();
        int count = contours.length;
        Color lineColor = new Color(.5, .25);
        aPntr.setStroke(Stroke.Stroke1);

        // Iterate over contours and paint
        for (int i=0; i<count; i++) {

            // Get/set color
            Color color = getContourColor(i);
            aPntr.setColor(color);

            // Get/fill contour
            Shape contour = contours[i];
            aPntr.fill(contour);

            // Paint contour line
            aPntr.setColor(lineColor);
            aPntr.draw(contour);
        }
    }

    /**
     * Paints chart content.
     */
    protected void paintMesh(Painter aPntr)
    {
        Color meshColor = new Color(0d, 0, 0, .1);
        aPntr.setColor(meshColor);
        Shape meshPath = getMeshPath();
        aPntr.draw(meshPath);
    }

    /**
     * Called when a ChartPart changes.
     */
    protected void chartPartDidChange(PropChange aPC)
    {
        Object src = aPC.getSource();
        if (src==getDataSet() || src instanceof Axis) {
            clearContours();
            _contourMaker = null;
        }
    }

    @Override
    public void setWidth(double aValue)
    {
        if (aValue==getWidth()) return;
        super.setWidth(aValue);
        clearContours();
    }

    @Override
    public void setHeight(double aValue)
    {
        if (aValue==getHeight()) return;
        super.setHeight(aValue);
        clearContours();
    }
}
