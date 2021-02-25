package snapcharts.views;
import snap.geom.*;
import snap.gfx.*;
import snap.util.PropChange;
import snapcharts.model.Axis;
import snapcharts.model.ChartTypeProps;
import snapcharts.model.DataSet;
import snapcharts.model.Intervals;
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

    // The contour paint order
    private int[]  _paintOrder;

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
            double valZ = _contourHelper.getContourRange(i).getMin();
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
     * Returns the contour paint order.
     */
    public int[] getContourPaintOrder()
    {
        // If already set, just return
        if (_paintOrder != null) return _paintOrder;

        // Find index of largest contour
        double maxArea = 0;
        int maxIndex = 0;
        int count = getContourCount();
        for (int i=0; i<count; i++) {
            Rect bnds = getDataContours()[i].getBounds();
            double area = bnds.width * bnds.height;
            if (area > maxArea) {
                maxIndex = i;
                maxArea = area;
            }
        }

        // Add index from Max contour to low val, then index from max to high val
        int[] paintOrder = new int[count];
        for (int i=0; i<count; i++) {
            int index = i <= maxIndex ? maxIndex - i : i;
            paintOrder[i] = index;
        }

        // Set/return
        return _paintOrder = paintOrder;
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
     * Returns the data to view transform.
     */
    public Transform getDataToView()
    {
        // Get axis bounds
        Intervals ivalsX = getAxisViewX().getIntervals();
        Intervals ivalsY = getAxisViewY().getIntervals();
        double minX = ivalsX.getMin();
        double minY = ivalsY.getMin();
        double maxX = ivalsX.getMax();
        double maxY = ivalsY.getMax();
        Rect dataBnds = new Rect(minX, minY, maxX - minX, maxY - minY);

        // Get view bounds
        double areaX = 0;
        double areaY = 0;
        double areaW = getWidth();
        double areaH = getHeight();
        Rect viewBnds = new Rect(areaX, areaY, areaW, areaH);

        // Return transform from axis bounds to view bounds
        return Transform.getTrans(dataBnds, viewBnds);
    }

    /**
     * Paints chart content.
     */
    @Override
    protected void paintChart(Painter aPntr)
    {
        paintContour(aPntr);

        boolean showMesh = getChart().getTypeHelper().getContourProps().isShowMesh();
        if (showMesh)
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

        // Get paint order and just paint hull for first/largest
        int[] paintOrder = getContourPaintOrder();
        int largestContourIndex = paintOrder[0];
        Color largestColor = _contourHelper.getContourColor(largestContourIndex);
        aPntr.setColor(largestColor);
        Shape hull = getContourMaker().getMesh().getHullPath();
        Shape hull2 = dataContourToView(hull);
        aPntr.fill(hull2);

        // Iterate over contours and paint
        for (int i=1; i<count; i++) {

            // Get index of contour
            int index = paintOrder[i];

            // Get/set color
            Color color = _contourHelper.getContourColor(index);
            aPntr.setColor(color);

            // Get/fill contour
            Shape contour = contours[index];
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
        if (src==getDataSet() || src instanceof Axis || src instanceof ChartTypeProps) {
            clearContours();
            _contourMaker = null;
            _dataContours = null;
            _paintOrder = null;
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
