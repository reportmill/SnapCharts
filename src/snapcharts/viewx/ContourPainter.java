package snapcharts.viewx;
import snap.geom.*;
import snap.gfx.Color;
import snap.gfx.Painter;
import snap.gfx.Stroke;
import snapcharts.model.Trace;
import snapcharts.data.DataStore;
import snapcharts.util.ContourMaker;
import snapcharts.view.AxisView;
import snapcharts.view.DataArea;

/**
 * This is a class to paint contour for a DataArea and DataSet.
 */
public class ContourPainter {

    // The ContourHelper
    private ContourHelper  _contourHelper;

    // The DataArea
    private DataArea  _dataArea;

    // The ContourMaker
    private ContourMaker _contourMaker;

    // The Contours in display coords
    private Shape[]  _contours;

    // The Contours in data coords
    private Shape[]  _dataContours;

    // The hull path
    private Shape  _hullPath;

    // The mesh path
    private Shape  _meshPath;

    // The contour paint order
    private int[]  _paintOrder;

    /**
     * Constructor.
     */
    public ContourPainter(ContourHelper aContourHelper, DataArea aDataArea)
    {
        _contourHelper = aContourHelper;
        _dataArea = aDataArea;
    }

    /**
     * Paints chart content.
     */
    protected void paintAll(Painter aPntr)
    {
        paintContours(aPntr);

        boolean showMesh = _contourHelper.isShowMesh();
        if (showMesh)
            paintMesh(aPntr);
    }

    /**
     * Paints chart content.
     */
    protected void paintContours(Painter aPntr)
    {
        // Get contour info
        Shape[] contours = getContours();
        int count = contours.length;
        boolean showLines = _contourHelper.isShowLines();
        Color lineColor = new Color(.5, .25);

        // Get paint order and largest color
        int[] paintOrder = getContourPaintOrder();
        int largestContourIndex = paintOrder[0];
        Color largestColor = _contourHelper.getContourColor(largestContourIndex);

        // Get hull path and fill
        Shape hull = getMeshHullPath();
        aPntr.setColor(largestColor);
        aPntr.fill(hull);
        aPntr.setStroke(Stroke.Stroke1);

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
            if (showLines) {
                aPntr.setColor(lineColor);
                aPntr.draw(contour);
            }
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
     * Returns the contour shapes array.
     */
    public Shape[] getContours()
    {
        // If already set, just return
        if (_contours !=null) return _contours;

        // Get contour count and data contours
        int count = _contourHelper.getContourCount();
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
    public Shape[] getDataContours()
    {
        // If already set, just return
        if (_dataContours !=null) return _dataContours;

        // Get number of contours, create shape array and get ContourMaker
        int count = _contourHelper.getContourCount();
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
    public ContourMaker getContourMaker()
    {
        // If already set, just return
        if (_contourMaker != null) return _contourMaker;

        // Get/set DataStore for contours (if Polar, get PolarXYData)
        Trace trace = _dataArea.getTrace();
        DataStore dataStore = trace.getProcessedData();
        if (trace.getChart().getType().isPolarType())
            dataStore = trace.getPolarXYData();

        // Create/set ContourMaker from DataStore
        return _contourMaker = new ContourMaker(dataStore);
    }

    /**
     * Returns a contour in display coords for given contour in data coords.
     */
    private Shape dataContourToView(Shape aDataContour)
    {
        AxisView axisX = _dataArea.getAxisViewX();
        AxisView axisY = _dataArea.getAxisViewY();
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
        // These all depend on the axis ranges
        _contours = null;
        _hullPath = null;
        _meshPath = null;
        _dataArea.repaint();
    }

    /**
     * Clears the Contours.
     */
    public void clearContoursAll()
    {
        clearContours();

        // These all depend on the data only
        _contourMaker = null;
        _dataContours = null;
        _paintOrder = null;
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
        int count = _contourHelper.getContourCount();
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
     * Returns the hull path.
     */
    public Shape getMeshHullPath()
    {
        if (_hullPath != null) return _hullPath;
        Shape dataHull = getContourMaker().getMesh().getHullPath();
        Shape dispHull = dataContourToView(dataHull);
        return _hullPath = dispHull;
    }

    /**
     * Returns the mesh path.
     */
    public Shape getMeshPath()
    {
        if (_meshPath!=null) return _meshPath;
        Shape dataPath = getContourMaker().getMesh().getMeshPath();
        Shape meshPath = dataContourToView(dataPath);
        return _meshPath = meshPath;
    }

}
