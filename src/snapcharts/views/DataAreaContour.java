package snapcharts.views;
import snap.geom.*;
import snap.gfx.*;
import snap.util.PropChange;
import snapcharts.model.Axis;
import snapcharts.model.DataSet;
import snapcharts.util.Mesh;
import snapcharts.util.Triangulate;
import java.util.Arrays;
import java.util.Comparator;

/**
 * A DataArea subclass to display ChartType CONTOUR.
 */
public class DataAreaContour extends DataArea {

    // The array of colors
    private Color[]  _colors;

    // The Contours
    private Shape[]  _contours;

    // Point indexes in ascending order
    private int[]  _pointIndexesSorted;

    // The Triangle vertex array
    private int[]  _triangleVertexArray;

    /**
     * Constructor.
     */
    public DataAreaContour(ChartHelper aChartHelper, DataSet aDataSet)
    {
        super(aChartHelper, aDataSet);
    }

    /**
     * Returns the number of contours.
     */
    public int getContourCount()  { return 16; }

    /**
     * Returns the contour level at index.
     */
    public double getContourValue(int anIndex)
    {
        DataSet dset = getDataSet();
        double zmin = dset.getMinZ();
        double zmax = dset.getMaxZ();
        int count = getContourCount();
        double delta = (zmax - zmin) / count;
        return zmin + delta * anIndex;
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

        // Listen to axisView changes: HERE?!? - YOU'VE GOT TO BE JOKING!!!
        for (AxisView axisView : _chartHelper.getAxisViews())
            axisView.addPropChangeListener(pc -> clearContours());

        // Create Gradient
        double[] offsets = { 0, .25, .5, .75, 1 };
        Color[] gcols = { Color.BLUE, Color.CYAN, Color.GREEN, Color.YELLOW, Color.RED };
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
     * Returns the contour shapes array.
     */
    public Shape[] getContours()
    {
        if (_contours!=null) return _contours;

        DataSet dset = getDataSet();
        AxisView axisX = getAxisViewX();
        AxisView axisY = getAxisViewY();

        int[] triangleVertextArray = getTriangleVertextArray();

        Mesh mesh = new Mesh(dset, triangleVertextArray);
        int count = getContourCount();
        Shape[] contours = new Shape[count];

        for (int i=0; i<count; i++) {

            double valZ = getContourValue(i);
            Shape contour = mesh.getContourShape(valZ);

            Path2D path = new Path2D();
            double[] pnts = new double[6];
            PathIter piter = contour.getPathIter(null);
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
            contours[i] = path;
        }

        // Set/return
        return _contours = contours;
    }

    /**
     * Clears the Contours.
     */
    private void clearContours()
    {
        _contours = null;
    }

    /**
     * Returns the array of triplets for triangle vertexes.
     * Each entry is an index into the PointIndexesSorted array.
     */
    public int[] getTriangleVertextArray()
    {
        // If already set, just return
        if (_triangleVertexArray!=null) return _triangleVertexArray;

        // Get Points sorted by X
        float[] points = getPointsForTriangulator();

        // Compute triangles, set and return
        int[] tva = new Triangulate().computeTriangles(points);

        // Fix indexes to be for points, not triangulator array indexes
        for (int i=0; i<tva.length; i++)
            tva[i] = tva[i] / 2;

        // Set and return
        return _triangleVertexArray = tva;
    }

    /**
     * Returns the points for the triangulator.
     */
    private float[] getPointsForTriangulator()
    {
        // Get DataSet, pointCount and array of point indexes sorted by X (ascending)
        DataSet dset = getDataSet();
        int pointCount = dset.getPointCount();
        int[] indexes = getPointIndexesSorted();

        // Create points array and load from sorted indexes
        float[] points = new float[pointCount*2];
        for (int i=0; i<pointCount; i++) {
            int index = indexes[i];
            points[i * 2] = (float) dset.getX(index);
            points[i * 2 + 1] = (float) dset.getY(index);
        }

        // Return points
        return points;
    }

    /**
     * Returns the indexes of points in sorted order.
     */
    public int[] getPointIndexesSorted()
    {
        // If already set, just return
        if (_pointIndexesSorted!=null) return _pointIndexesSorted;

        // Get Integer indexes of points sorted by X
        DataSet dset = getDataSet();
        int pointCount = dset.getPointCount();
        Integer[] indexes = new Integer[pointCount];
        for (int i=0; i<pointCount; i++) indexes[i] = i;
        Comparator<Integer> comp = (o1, o2) -> comparePointsX(dset, o1, o2);
        Arrays.sort(indexes, comp);

        // Convert to int[], set and return
        int[] intArray = new int[pointCount];
        for (int i=0; i<pointCount; i++) intArray[i] = indexes[i];
        return _pointIndexesSorted = intArray;
    }

    /**
     * A comparator to compare point X for two given indexes.
     */
    private static int comparePointsX(DataSet dset, Integer ind1, Integer ind2)
    {
        double x1 = dset.getX(ind1);
        double x2 = dset.getX(ind2);
        return Double.compare(x1, x2);
    }

    /**
     * Paints chart content.
     */
    @Override
    protected void paintChart(Painter aPntr)
    {
        paintMesh(aPntr);

        paintContour(aPntr);
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
        DataSet dset = getDataSet();
        int[] triangles = getTriangleVertextArray();

        AxisView axisX = getAxisViewX();
        AxisView axisY = getAxisViewY();

        Ellipse ellipse = new Ellipse(0, 0, 4, 4);

        for (int i=0; i<triangles.length; i+=3) {

            // Get index of triangle vertices
            int index1 = triangles[i];
            int index2 = triangles[i+1];
            int index3 = triangles[i+2];

            // Get points of triangle vertices in data coords
            double dataX1 = dset.getX(index1);
            double dataY1 = dset.getY(index1);
            double dataX2 = dset.getX(index2);
            double dataY2 = dset.getY(index2);
            double dataX3 = dset.getX(index3);
            double dataY3 = dset.getY(index3);

            // Get points of triangle vertices in display coords
            double dispX1 = axisX.dataToView(dataX1);
            double dispY1 = axisY.dataToView(dataY1);
            double dispX2 = axisX.dataToView(dataX2);
            double dispY2 = axisY.dataToView(dataY2);
            double dispX3 = axisX.dataToView(dataX3);
            double dispY3 = axisY.dataToView(dataY3);

            aPntr.setColor(Color.RED);
            aPntr.setStroke(Stroke.Stroke1);

            aPntr.drawLine(dispX1, dispY1, dispX2, dispY2);
            aPntr.drawLine(dispX2, dispY2, dispX3, dispY3);
            aPntr.drawLine(dispX3, dispY3, dispX1, dispY1);

            aPntr.setColor(Color.DARKGRAY);
            ellipse.setXY(dispX1 - 2, dispY1 - 2);
            aPntr.fill(ellipse);
            ellipse.setXY(dispX2 - 2, dispY2 - 2);
            aPntr.fill(ellipse);
            ellipse.setXY(dispX3 - 2, dispY3 - 2);
            aPntr.fill(ellipse);
        }
    }

    /**
     * Called when a ChartPart changes.
     */
    protected void chartPartDidChange(PropChange aPC)
    {
        Object src = aPC.getSource();
        if (src==getDataSet() || src instanceof Axis) {
            clearContours();
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
