/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.viewx;
import snap.geom.Point;
import snap.gfx.Color;
import snap.gfx3d.*;
import snapcharts.data.DataSet;
import snapcharts.model.Trace;
import snapcharts.model.TracePoint;
import snapcharts.util.Mesh;
import snapcharts.view.ChartHelper;

/**
 * A DataArea3D subclass to display the contents of Contour3D chart.
 */
public class Contour3DDataArea extends DataArea3D {

    // The Triangle VertexArray
    private VertexArray  _triangleArray;

    /**
     * Constructor.
     */
    public Contour3DDataArea(ChartHelper aChartHelper, Trace aTrace, boolean isVisible)
    {
        super(aChartHelper, aTrace, isVisible);
    }

    /**
     * Override to add contour chart.
     */
    @Override
    protected AxisBoxShape createAxisBoxShape()
    {
        // Do normal version to get AxisBoxShape
        AxisBoxShape axisBoxShape = super.createAxisBoxShape();

        // Get TriangleArray, create/add to VertexArrayShape
        _triangleArray = createTriangleArray();
        VertexArrayShape triangleArrayShape = new VertexArrayShape(_triangleArray);

        // Add TriangleArrayShape to AxisBoxShape, return
        axisBoxShape.addChild(triangleArrayShape);
        return axisBoxShape;
    }

    /**
     * Override to add contour chart.
     */
    protected VertexArray createTriangleArray()
    {
        // Get contour mesh and triangles
        DataSet dataSet = getStagedData();
        Mesh mesh = new Mesh(dataSet);
        Mesh.Triangle[] triangles = mesh.getTriangles();

        // Get ContourHelper and ContourCount
        Contour3DChartHelper chartHelper = (Contour3DChartHelper) getChartHelper();
        ContourHelper contourHelper = chartHelper.getContourHelper();

        // Create VertexArray
        VertexArray vertexArray = new VertexArray();
        vertexArray.setDoubleSided(true);

        // Iterate over triangles and add shape for each
        int pointCount = mesh.getPointCount();
        Point3D meshPoint = new Point3D();
        for (int i = 0; i < pointCount; i++) {

            // Get mesh point in scene coords
            meshPoint.x = mesh.getX(i);
            meshPoint.y = mesh.getY(i);
            double pz = meshPoint.z = mesh.getZ(i);
            convertDataToScene(meshPoint);

            // Get vertex point and add (notice we swap Y and Z)
            vertexArray.addPoint(meshPoint.x, meshPoint.y, meshPoint.z);

            // Add vertex color and add
            Color contourColor1 = contourHelper.getContourColorForZ(pz);
            vertexArray.addColor(contourColor1);
        }

        // Create indexArray for triangles and set
        int indexCount = triangles.length * 3;
        int[] indexArray = new int[indexCount];
        int index = 0;
        for (Mesh.Triangle triangle : triangles) {
            indexArray[index++] = triangle.v1;
            indexArray[index++] = triangle.v2;
            indexArray[index++] = triangle.v3;
        }
        vertexArray.setIndexArray(indexArray);

        // Return
        return vertexArray;
    }

    /**
     * Override for Contour3D.
     */
    @Override
    public TracePoint getDataPointForLocalXY(double aX, double aY)
    {
        // If point above cube view, just return
        if (aX > getWidth() - 100 && aY < 100)
            return null;

        // Get ray from camera origin to View XY
        Camera camera = _camera;
        Point3D rayOrigin = new Point3D();
        Vector3D rayDir = new Vector3D();
        camera.getRayToViewPoint(aX, aY, rayOrigin, rayDir);

        // Do hit detection (just return null if missed)
        HitDetector hitDetector = new HitDetector();
        boolean isHit = hitDetector.isRayHitShape(rayOrigin, rayDir, camera.getScene());
        if (!isHit) {
            System.out.println("Missed");
            return null;
        }

        // Get hit shape
        Shape3D hitShape = hitDetector.getHitShape();
        String hitShapeName = hitShape.getName(); if (hitShapeName == null) hitShapeName = "(no-name)";
        //Point3D hitPoint = hitDetector.getHitPoint();
        //System.out.println("Hit Shape: " + hitShapeName + " at Point: " + hitPoint.x + ", " + hitPoint.y + ", " + hitPoint.z);

        // If hit point isn't mesh, just return null (probably hit axis box)
        VertexArray hitTriangleArray = hitDetector.getHitTriangleArray();
        if (hitTriangleArray != _triangleArray)
            return null;

        // Get closest mesh point and return
        Trace trace = getTrace();
        int pointIndex = hitDetector.getHitVertexIndex();
        if (pointIndex >= trace.getPointCount()) {
            System.err.println("Contour3DDataArea.getDataPointForLocalXY: hit point index outside trace bounds! " + pointIndex);
            return null;
        }

        // Return point
        return trace.getPoint(pointIndex);
    }

    /**
     * Override to handle 3D data point.
     */
    @Override
    public Point getLocalXYForDataPoint(TracePoint aDP)
    {
        // If null, just return
        if (aDP == null) return null;

        // Get point in view coords and return as 2D point
        Point3D point = convertDataToView(aDP.getX(), aDP.getY(), aDP.getZ());
        return new Point(point.x, point.y);
    }
}