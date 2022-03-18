/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.viewx;
import snap.gfx.Color;
import snap.gfx3d.*;
import snap.util.MathUtils;
import snapcharts.data.DataSet;
import snapcharts.model.Intervals;
import snapcharts.util.Mesh;

/**
 * This class builds the Line3D scene.
 */
public class Contour3DSceneBuilder extends AxisBoxSceneBuilder {

    // The Line3DDataArea
    private Contour3DDataArea  _dataArea;

    /**
     * Constructor.
     */
    public Contour3DSceneBuilder(Contour3DDataArea aDataArea, Scene3D aScene)
    {
        super(aDataArea, aScene);
        _dataArea = aDataArea;
    }

    /**
     * Override to return intervals for Z.
     */
    @Override
    public Intervals getIntervalsZ()
    {
        DataSet dataSet = _dataArea.getStagedData();
        double minZ = dataSet.getMinZ();
        double maxZ = dataSet.getMaxZ();
        Intervals intervals= Intervals.getIntervalsForMinMaxLen(minZ, maxZ, 400, 40, false, false);
        return intervals;
    }

    /**
     * Override to add contour chart.
     */
    @Override
    protected ParentShape3D createAxisBoxShape()
    {
        // Do normal version
        ParentShape3D axisBoxShape = super.createAxisBoxShape();

        // Get axis box size
        double prefW = getPrefWidth();
        double prefH = getPrefHeight();
        double prefD = getPrefDepth();

        // Get dataset info
        DataSet dataSet = _dataArea.getStagedData();
        double minX = dataSet.getMinX();
        double maxX = dataSet.getMaxX();
        double minY = dataSet.getMinY();
        double maxY = dataSet.getMaxY();
        double minZ = dataSet.getMinZ();
        double maxZ = dataSet.getMaxZ();

        // Get contour mesh and triangles
        Mesh mesh = new Mesh(dataSet);
        Mesh.Triangle[] triangles = mesh.getTriangles();

        // Get ContourHelper and ContourCount
        Contour3DChartHelper chartHelper = (Contour3DChartHelper) _dataArea.getChartHelper();
        ContourHelper contourHelper = chartHelper.getContourHelper();
        int contourCount = contourHelper.getContourCount();

        // Create VertexArrayShape
        VertexArrayShape vertexArrayShape = new VertexArrayShape();
        vertexArrayShape.setDoubleSided(true);
        VertexArray vertexArray = vertexArrayShape.getVertexArray();
        axisBoxShape.addChild(vertexArrayShape);

        // Iterate over triangles and add shape for each
        for (Mesh.Triangle triangle : triangles) {

            // Get triangle vertex indexes
            int v1 = triangle.v1;
            int v2 = triangle.v2;
            int v3 = triangle.v3;

            // Get triangle points
            double p1x = mesh.getX(v1);
            double p1y = mesh.getY(v1);
            double p1z = mesh.getZ(v1);
            double p2x = mesh.getX(v2);
            double p2y = mesh.getY(v2);
            double p2z = mesh.getZ(v2);
            double p3x = mesh.getX(v3);
            double p3y = mesh.getY(v3);
            double p3z = mesh.getZ(v3);

            // Get vertex colors
            Color contourColor1 = contourHelper.getContourColorForZ(p1z);
            Color contourColor2 = contourHelper.getContourColorForZ(p2z);
            Color contourColor3 = contourHelper.getContourColorForZ(p3z);

            p1x = MathUtils.mapValueForRanges(p1x, minX, maxX, 0, prefW);
            p1y = MathUtils.mapValueForRanges(p1y, minY, maxY, 0, prefD);
            p1z = MathUtils.mapValueForRanges(p1z, minZ, maxZ, 0, prefH);
            p2x = MathUtils.mapValueForRanges(p2x, minX, maxX, 0, prefW);
            p2y = MathUtils.mapValueForRanges(p2y, minY, maxY, 0, prefD);
            p2z = MathUtils.mapValueForRanges(p2z, minZ, maxZ, 0, prefH);
            p3x = MathUtils.mapValueForRanges(p3x, minX, maxX, 0, prefW);
            p3y = MathUtils.mapValueForRanges(p3y, minY, maxY, 0, prefD);
            p3z = MathUtils.mapValueForRanges(p3z, minZ, maxZ, 0, prefH);

            // Create/config path for triangle
            vertexArray.addPoint(p1x, p1z, p1y);
            vertexArray.addPoint(p2x, p2z, p2y);
            vertexArray.addPoint(p3x, p3z, p3y);

            // Add triangle vertex colors
            vertexArray.addColor(contourColor1);
            vertexArray.addColor(contourColor2);
            vertexArray.addColor(contourColor3);
        }

        // Return
        return axisBoxShape;
    }
}
