package snapcharts.views;

import snap.geom.Path;
import snap.geom.Point;
import snap.geom.Rect;
import snap.gfx.Color;
import snap.gfx.Painter;
import snap.gfx3d.*;
import snapcharts.model.DataSet;
import snapcharts.model.DataSetList;
import snapcharts.model.Intervals;
import java.util.List;

/**
 * A DataArea subclass to display the contents of bar chart.
 */
public class DataAreaLine3D extends DataArea {

    // The Camera
    protected CameraView  _camView;

    // The Camera
    private Camera _camera;

    // The Scene
    private Scene3D _scene;

    // Shapes for grid
    private Path _grid = new Path();

    // Shapes for the minor grid
    private Path  _gridMinor = new Path();

    // The grid path without separators
    private Path  _gridWithoutSep = new Path();

    /**
     * Constructor.
     */
    public DataAreaLine3D(ChartHelper aChartHelper, DataSet aDataSet)
    {
        super(aChartHelper, aDataSet);

        // Create/add CameraView
        _camView = new CameraView() {
            protected void layoutImpl() { rebuildScene(); }
        };
        addChild(_camView);

        // Get/configure camera
        _camera = _camView.getCamera();
        _camera.setYaw(26);
        _camera.setPitch(10);
        _camera.setDepth(300);
        _camera.setFocalLength(8*72);
        _camera.setAdjustZ(true);

        // Get Sceme
        _scene = _camView.getScene();
    }

    /**
     * Returns the number of suggested ticks between the intervals of the RPG'd graph.
     */
    public int getMinorTickCount()
    {
        // Calculate height per tick - if height greater than 1 inch, return 4, greater than 3/4 inch return 3, otherwise 1
        int ivalCount = getIntervalsY().getCount();
        double heightPerTick = getHeight()/(ivalCount - 1);
        return heightPerTick>=72 ? 4 : heightPerTick>=50 ? 3 : 1;
    }

    /**
     * Rebuilds the chart.
     */
    protected void rebuildScene()
    {
        // Rebuild 2D gridlines
        rebuildGridLines();

        // Remove all existing children
        _scene.removeShapes();

        // Get standard width, height, depth
        double width = getWidth();
        double height = getHeight();
        double depth = _camera.getDepth();

        // Calculate whether back plane should be shifted to the front. Back normal = { 0, 0,-1 }.
        boolean shiftBack = _camera.isFacingAway(_scene.localToCameraForVector(0, 0, -1));
        double backZ = shiftBack ? 0 : depth;

        // Create back plane shape
        Path3D back = new Path3D(); back.setOpacity(.8f);
        back.setColor(Color.WHITE); //if (_backFill!=null) back.setColor(_backFill.getColor());
        back.setStroke(Color.BLACK, 1); //if (_backStroke!=null) back.setStroke(_backStroke.getColor(),_backStroke.getWidth());
        back.moveTo(0, 0, backZ);
        back.lineTo(0, height, backZ);
        back.lineTo(width, height, backZ);
        back.lineTo(width, 0, backZ);
        back.close();
        if (!shiftBack)
            back.reverse();
        _scene.addShape(back);

        // Add Grid to back
        Path3D grid = new Path3D(_grid, backZ);
        grid.setStroke(Color.BLACK, 1);
        back.addLayer(grid);

        // Add GridMinor to back
        Path3D gridMinor = new Path3D(_gridMinor, backZ);
        gridMinor.setStrokeColor(Color.LIGHTGRAY);
        back.addLayer(gridMinor);

        // Calculate whether side plane should be shifted to the right. Side normal = { 1, 0, 0 }.
        boolean shiftSide = !_camera.isPseudo3D() && _camera.isFacingAway(_scene.localToCameraForVector(1,0,0));
        double sideX = shiftSide ? width : 0;

        // Create side path shape
        Path3D side = new Path3D();
        side.setColor(Color.LIGHTGRAY);
        side.setStroke(Color.BLACK, 1);
        side.setOpacity(.8f);
        side.moveTo(sideX, 0, 0);
        side.lineTo(sideX, height, 0);
        side.lineTo(sideX, height, depth);
        side.lineTo(sideX, 0, depth);
        side.close();

        // For horizonatal bar charts, make sure the side panel always points towards the camera
        boolean sideFacingAway = _camera.isFacingAway(_scene.localToCamera(side));
        if (sideFacingAway)
            side.reverse();
        _scene.addShape(side);

        // Create floor path shape
        Path3D floor = new Path3D();
        floor.setColor(Color.LIGHTGRAY);
        floor.setStroke(Color.BLACK, 1);
        floor.setOpacity(.8f);
        floor.moveTo(0, height + .5, 0);
        floor.lineTo(width, height + .5, 0);
        floor.lineTo(width, height + .5, depth);
        floor.lineTo(0, height + .5, depth);
        floor.close();
        boolean floorFacingAway = _camera.isFacingAway(_scene.localToCamera(floor));
        if (floorFacingAway)
            floor.reverse();
        _scene.addShape(floor);

        // Determine whether side grid should be added to graph side or floor
        Path3D sideGridBuddy = side;
        Rect gridWithoutSepBnds = _gridWithoutSep.getBounds();
        Rect gridMinorBnds = _gridMinor.getBounds();
        Rect gridRect = new Rect(0, gridWithoutSepBnds.y, depth, gridWithoutSepBnds.height);
        Rect gridMinorRect = new Rect(0, gridMinorBnds.y, depth, gridMinorBnds.height);
        Transform3D gridTrans = new Transform3D().rotateY(-90).translate(sideX, 0, 0);

        // Configure grid
        Path sideGridPath = _gridWithoutSep.copyFor(gridRect);
        Path3D sideGrid = new Path3D(sideGridPath, 0);
        sideGrid.transform(gridTrans);
        sideGrid.setStroke(Color.BLACK, 1);
        sideGridBuddy.addLayer(sideGrid);

        // Add GridMinor to side3d
        Path sideGridPathMinor = _gridMinor.copyFor(gridMinorRect);
        Path3D sideGridMinor = new Path3D(sideGridPathMinor, 0);
        sideGridMinor.transform(gridTrans);
        sideGridMinor.setStroke(Color.LIGHTGRAY,1);
        sideGridBuddy.addLayer(sideGridMinor);
        sideGridBuddy.setColor(Color.WHITE); //if (_backFill!=null) sideGridBuddy.setColor(_backFill.getColor());
        sideGridBuddy.setStroke(Color.BLACK, 1); //if (_backStroke!=null) sideGridBuddy.setStroke(_backStroke.getColor(), _backStroke.getWidth());

        // Add Line3D shapes
        addLine3Ds();
    }

    /**
     * Adds the Line3D shapes.
     */
    protected void addLine3Ds()
    {
        // Get info
        DataSetList dataSetList = getDataSetList();
        List<DataSet> dataSets = dataSetList.getDataSets();
        int dataSetCount = dataSets.size();

        // Iterate over datasets and add Line3D shape for each
        for (int i=0; i<dataSetCount; i++) {
            DataSet dset = dataSets.get(i);
            addLine3D(dset, i, dataSetCount);
        }
    }

    /**
     * Adds the Line3D shape for DataSet.
     */
    protected void addLine3D(DataSet dset, int anIndex, int aCount)
    {
        // Create 2d path
        Path path = createDataPath(dset);
        Color dataStrokeColor = getDataColor(anIndex);
        Color dataFillColor = dataStrokeColor.brighter().brighter();

        // Get depth, and Z values for back/front
        double sectionDepth = _camera.getDepth() / aCount;
        double lineZ = sectionDepth * (anIndex + .5);

        // Create/configure bar path/path3d and add to scene
        PathBox3D bar = new PathBox3D(path, lineZ, lineZ, false);
        bar.setColor(dataFillColor);
        bar.setStroke(dataStrokeColor, 1);
        _scene.addShape(bar);
    }

    /**
     * Returns Path2D for painting dataset.
     */
    protected Path createDataPath(DataSet dset)
    {
        // Create/add path for dataset
        int pointCount = dset.getPointCount();
        Path path = new Path();

        // Get area bounds
        double areaX = .1;
        double areaY = .1;
        double areaW = getWidth();
        double areaH = getHeight();
        double areaMaxX = areaW - .1;
        double areaMaxY = areaH - .1;

        // Iterate over data points, get in display coords and add path line segment
        for (int j=0; j<pointCount; j++) {

            // Get data point in display coords
            double dataX = dset.getX(j);
            double dataY = dset.getY(j);
            Point dispXY = dataToView(dataX, dataY);

            // Clamp to area/axis bounds
            dispXY.x = Math.max(areaX, Math.min(dispXY.x, areaMaxX));
            dispXY.y = Math.max(areaY, Math.min(dispXY.y, areaMaxY));

            // Add to path
            if (j == 0)
                path.moveTo(dispXY.x, dispXY.y);
            else path.lineTo(dispXY.x, dispXY.y);
        }

        // Close path
        Point point0 = path.getPoint(0);
        Point pointLast = path.getPoint(pointCount-1);
        path.lineTo(areaW, pointLast.y);
        path.lineTo(areaW, areaH);
        path.lineTo(0, areaH);
        path.lineTo(0, point0.y);
        path.close();

        // Return path
        return path;
    }

    /**
     * Override to suppress.
     */
    @Override
    protected void paintFront(Painter aPntr) { }

    /**
     * Override to suppress.
     */
    @Override
    protected void paintChart(Painter aPntr)  { }

    /**
     * Override to suppress.
     */
    @Override
    public void paintBorder(Painter aPntr)  { }

    /**
     * Override to suppress.
     */
    @Override
    public void paintGridlines(Painter aPntr)  { }

    /**
     * Rebuild gridlines.
     */
    void rebuildGridLines()
    {
        _grid.clear();
        _gridMinor.clear();
        _gridWithoutSep.clear();

        // Get graph bounds
        double areaX = 0;
        double areaY = 0;
        double areaW = getWidth();
        double areaH = getHeight();

        // Get graph min interval and max interval
        Intervals intervalsY = getIntervalsY();
        int countY = intervalsY.getCount();
        double minY = intervalsY.getMin();
        double maxY = intervalsY.getMax();
        double rangeY = maxY - minY;

        // Get grid max
        double gridMax = areaH;
        int minorTickCountY = getMinorTickCount();
        double majorDeltaY = intervalsY.getInterval(1) - minY;
        double minorDeltaY = gridMax * majorDeltaY / rangeY / (minorTickCountY+1);

        // Iterate over graph intervals
        for (int i=0, iMax=countY; i<iMax - 1; i++) {

            // Get interval ratio and line x & y
            double intervalRatio = i/(iMax - 1f);
            double lineY = areaY + areaH * intervalRatio;

            // DrawMajorAxis
            if (i>0) {
                addGridLineMajor(areaX, lineY, areaX + areaW, lineY);
            }

            // Draw minor axis
            /*for (int j=0; j<minorTickCountY; j++) {
                double minorLineY = lineY + (j+1) * minorDeltaY;
                addGridLineMinor(areaX, minorLineY, areaX + areaW, minorLineY);
            }*/
        }

        // Get graph min interval and max interval
        AxisViewX axisViewX = getAxisViewX();
        Intervals intervalsX = axisViewX.getIntervals();
        int countX = intervalsX.getCount();

        // Iterate over graph intervals
        for (int i=0; i<countX; i++) {
            double dataX = intervalsX.getInterval(i);
            double dispX = axisViewX.dataToView(dataX);
            addGridLineSeparator(dispX, areaY, dispX, areaY + areaH);
        }
    }

    /** Adds a major grid line to the graph view. */
    void addGridLineMajor(double X0, double Y0, double X1, double Y1)
    {
        _grid.moveTo(X0, Y0);
        _grid.lineTo(X1, Y1);
        _gridWithoutSep.moveTo(X0, Y0);
        _gridWithoutSep.lineTo(X1, Y1);
    }

    /** Adds a minor grid line to the graph view. */
    void addGridLineMinor(double X0, double Y0, double X1, double Y1)
    {
        _gridMinor.moveTo(X0, Y0);
        _gridMinor.lineTo(X1, Y1);
    }

    /** Adds a grid line separator to the graph view. */
    void addGridLineSeparator(double X0, double Y0, double X1, double Y1)
    {
        _grid.moveTo(X0, Y0);
        _grid.lineTo(X1, Y1);
    }

    /**
     * Override to rebuild chart.
     */
    public void setReveal(double aValue)
    {
        super.setReveal(aValue);
        _camView.relayout();
        if (aValue==0) {
            _camView.setYaw(90);
            _camView.setPitch(0);
            _camView.setOffsetZ(200);
            _camView.getAnimCleared(1000).setValue(CameraView.Yaw_Prop,26);
            _camView.getAnim(1000).setValue(CameraView.Pitch_Prop,10);
            _camView.getAnim(1000).setValue(CameraView.OffsetZ_Prop,0).setLinear().play();
        }
    }

    /**
     * Override to resize CamView.
     */
    protected void layoutImpl()
    {
        double viewW = getWidth();
        double viewH = getHeight();
        _camView.setSize(viewW, viewH);
    }

    /**
     * Override to properly size hidden Y Axis.
     */
    @Override
    public AxisViewY getAxisViewY()
    {
        AxisViewY axisViewY = super.getAxisViewY();
        axisViewY.setHeight(getHeight());
        return axisViewY;
    }

    /**
     * Override to properly size hidden X Axis.
     */
    @Override
    public AxisViewX getAxisViewX()
    {
        AxisViewX axisViewX = super.getAxisViewX();
        axisViewX.setWidth(getWidth());
        return axisViewX;
    }
}