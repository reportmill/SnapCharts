/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.viewx;
import snap.geom.Shape;
import snap.gfx.Color;
import snap.gfx3d.*;
import snapcharts.model.TracePoint;
import snapcharts.view.ChartView;

/**
 * This class draws highlight lines to show selected data point.
 */
public class AxisBoxHighliner extends FacetShape {

    // The AxisBox
    private AxisBoxShape  _axisBoxShape;

    // The TraceView3D
    private TraceView3D  _traceView;

    // The target point in scene coords
    private Point3D  scenePoint = new Point3D();

    // Points for left/right plane lines
    private Point3D leftP1 = new Point3D(), leftP2 = new Point3D();
    private Point3D leftP3 = new Point3D(), leftP4 = new Point3D();

    // Points for back/front plane lines
    private Point3D backP1 = new Point3D(), backP2 = new Point3D();
    private Point3D backP3 = new Point3D(), backP4 = new Point3D();

    // Points for bottom/top plane lines
    private Point3D bottomP1 = new Point3D(), bottomP2 = new Point3D();
    private Point3D bottomP3 = new Point3D(), bottomP4 = new Point3D();

    // Points for Crossbars
    private Point3D crossBarLR = new Point3D();
    private Point3D crossBarFB = new Point3D();
    private Point3D crossBarTB = new Point3D();

    // Vectors
    private Vector3D leftV = new Vector3D(1,0,0);
    private Vector3D backV = new Vector3D(0,0,1);
    private Vector3D bottomV = new Vector3D(0,1,0);

    // Constant for line width
    private static final int LINE_WIDTH = 3;

    // Constant for line color
    private static final Color LINE_COLOR = Color.BLUE;

    /**
     * Constructor.
     */
    public AxisBoxHighliner(AxisBoxShape axisBoxShape, TraceView3D traceView)
    {
        _axisBoxShape = axisBoxShape;
        _traceView = traceView;
    }

    /**
     * Update.
     */
    public void updateLines()
    {
        // Get ChartView.TargDataPoint. If null, set not visible and return.
        ChartView chartView = _traceView.getChartHelper().getChartView();
        TracePoint tracePoint = chartView.getTargDataPoint();
        if (tracePoint == null) {
            setVisible(false);
            return;
        }

        // Reset visible and triangle array
        setVisible(true);
        _triangleArray = null;

        // Get TargDataPoint in scene space
        scenePoint.setPoint(tracePoint.getX(), tracePoint.getY(), tracePoint.getZ());
        _traceView.convertDataToScene(scenePoint);

        // Get scene size
        double width = _traceView.getAxisBoxPrefWidth();
        double height = _traceView.getAxisBoxPrefHeight();
        double depth = _traceView.getAxisBoxPrefDepth();

        // Get points for left side lines
        boolean leftVisible = _axisBoxShape.isSideVisible(Side3D.LEFT);
        double sideX = leftVisible ? 1 : width - 1;
        leftP1.setPoint(sideX, height, scenePoint.z);
        leftP2.setPoint(sideX, 0, scenePoint.z);
        leftP3.setPoint(sideX, scenePoint.y, depth);
        leftP4.setPoint(sideX, scenePoint.y, 0);

        // Get points for back side lines
        boolean backVisible = _axisBoxShape.isSideVisible(Side3D.BACK);
        double sideZ = backVisible ? 1 : depth - 1;
        backP1.setPoint(0, scenePoint.y, sideZ);
        backP2.setPoint(width, scenePoint.y, sideZ);
        backP3.setPoint(scenePoint.x, 0, sideZ);
        backP4.setPoint(scenePoint.x, height, sideZ);

        // Get points for bottom side lines
        boolean bottomVisible = _axisBoxShape.isSideVisible(Side3D.BOTTOM);
        double sideY = bottomVisible ? 1 : height - 1;
        bottomP1.setPoint(0, sideY, scenePoint.z);
        bottomP2.setPoint(width, sideY, scenePoint.z);
        bottomP3.setPoint(scenePoint.x, sideY, 0);
        bottomP4.setPoint(scenePoint.x, sideY, depth);

        // Get points for crossbar left/right, front/back, top/bottom lines
        crossBarLR.setPoint(sideX, scenePoint.y, scenePoint.z);
        crossBarFB.setPoint(scenePoint.x, scenePoint.y, sideZ);
        crossBarTB.setPoint(scenePoint.x, sideY, scenePoint.z);
    }

    /**
     * Override to create highliner lines in triangle VertexArray.
     */
    @Override
    protected VertexArray createTriangleArray()
    {
        // Create triangles vertex array
        VertexArray vertexArray = new VertexArray();
        vertexArray.setColor(LINE_COLOR);
        vertexArray.setDoubleSided(true);

        // Add left/right plane lines
        VertexArrayUtils.addLineStrokePoints(vertexArray, leftP1, leftP2, leftV, LINE_WIDTH);
        VertexArrayUtils.addLineStrokePoints(vertexArray, leftP3, leftP4, leftV, LINE_WIDTH);

        // Add back/front plane lines
        VertexArrayUtils.addLineStrokePoints(vertexArray, backP1, backP2, backV, LINE_WIDTH);
        VertexArrayUtils.addLineStrokePoints(vertexArray, backP3, backP4, backV, LINE_WIDTH);

        // Add bottom/top plane lines
        VertexArrayUtils.addLineStrokePoints(vertexArray, bottomP1, bottomP2, bottomV, LINE_WIDTH);
        VertexArrayUtils.addLineStrokePoints(vertexArray, bottomP3, bottomP4, bottomV, LINE_WIDTH);

        // Add crossbar left/right lines
        VertexArrayUtils.addLineStrokePoints(vertexArray, crossBarLR, scenePoint, backV, LINE_WIDTH);
        VertexArrayUtils.addLineStrokePoints(vertexArray, crossBarLR, scenePoint, bottomV, LINE_WIDTH);

        // Add crossbar front/back lines
        VertexArrayUtils.addLineStrokePoints(vertexArray, crossBarFB, scenePoint, leftV, LINE_WIDTH);
        VertexArrayUtils.addLineStrokePoints(vertexArray, crossBarFB, scenePoint, bottomV, LINE_WIDTH);

        // Add crossbar top/bottom lines
        VertexArrayUtils.addLineStrokePoints(vertexArray, crossBarTB, scenePoint, leftV, LINE_WIDTH);
        VertexArrayUtils.addLineStrokePoints(vertexArray, crossBarTB, scenePoint, backV, LINE_WIDTH);

        // Return
        return vertexArray;
    }

    @Override
    protected Vector3D createNormal()  { return new Vector3D(0, 0, 1); }

    @Override
    public int getPointCount()  { return 0; }

    @Override
    public Point3D getPoint(int anIndex)  { return null; }

    @Override
    public Shape getShape2D()  { return null; }

    @Override
    public void reverse()  { }

    @Override
    public FacetShape copyForMatrix(Matrix3D aTrans)  { return null; }

    @Override
    protected Bounds3D createBounds3D()
    {
        return new Bounds3D(0, 0, 0, 0, 0, 0);
    }
}
