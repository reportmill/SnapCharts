/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.viewx;
import snap.gfx.Color;
import snap.gfx3d.VertexArray;
import snap.gfx3d.VertexArrayShape;
import snap.util.MathUtils;
import snapcharts.data.DataSet;
import snapcharts.model.Trace;
import snapcharts.model.TracePoint;
import snapcharts.util.Mesh;
import snapcharts.view.ChartHelper;

/**
 * A DataArea3D subclass to display the contents of Contour3D chart.
 */
public class Contour3DDataArea extends DataArea3D {

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
        // Do normal version
        AxisBoxShape axisBoxShape = super.createAxisBoxShape();

        // Get axis box size
        double prefW = getAxisBoxPrefWidth();
        double prefH = getAxisBoxPrefHeight();
        double prefD = getAxisBoxPrefDepth();

        // Get dataset info
        DataSet dataSet = getStagedData();
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
        Contour3DChartHelper chartHelper = (Contour3DChartHelper) getChartHelper();
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

    /**
     * Override for Contour3D.
     */
    @Override
    public TracePoint getDataPointForLocalXY(double aX, double aY)
    {
        if (aX > getWidth() - 100 && aY < 100)
            return null;
        return super.getDataPointForLocalXY(aX, aY);
    }
}