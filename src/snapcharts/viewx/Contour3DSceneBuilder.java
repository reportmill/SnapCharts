/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.viewx;
import snap.gfx.Color;
import snap.gfx3d.Path3D;
import snap.gfx3d.Scene3D;
import snap.util.MathUtils;
import snapcharts.data.DataSet;
import snapcharts.model.Intervals;
import snapcharts.util.Mesh;
import snapcharts.view.AxisViewX;

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
     * Rebuilds the chart.
     */
    protected void rebuildScene()
    {
        // Do normal version
        super.rebuildScene();

        // Add Line3D shapes
        addContour3D();
    }

    /**
     * Adds the Line3D shapes.
     */
    protected void addContour3D()
    {
        DataSet dataSet = _dataArea.getStagedData();
        double minX = dataSet.getMinX();
        double maxX = dataSet.getMaxX();
        double minY = dataSet.getMinY();
        double maxY = dataSet.getMaxY();
        double minZ = dataSet.getMinZ();
        double maxZ = dataSet.getMaxZ();

        double prefW = getPrefWidth();
        double prefH = getPrefHeight();
        double prefD = getPrefDepth();

        Mesh mesh = new Mesh(dataSet);
        Mesh.Triangle[] triangles = mesh.getTriangles();

        for (Mesh.Triangle triangle : triangles) {
            int v1 = triangle.v1;
            int v2 = triangle.v2;
            int v3 = triangle.v3;
            double p1x = mesh.getX(v1);
            double p1y = mesh.getY(v1);
            double p1z = mesh.getZ(v1);
            double p2x = mesh.getX(v2);
            double p2y = mesh.getY(v2);
            double p2z = mesh.getZ(v2);
            double p3x = mesh.getX(v3);
            double p3y = mesh.getY(v3);
            double p3z = mesh.getZ(v3);

            p1x = MathUtils.mapValueForRanges(p1x, minX, maxX, 0, prefW);
            p1y = MathUtils.mapValueForRanges(p1y, minY, maxY, 0, prefW);
            p1z = MathUtils.mapValueForRanges(p1z, minZ, maxZ, 0, prefD);
            p2x = MathUtils.mapValueForRanges(p2x, minX, maxX, 0, prefW);
            p2y = MathUtils.mapValueForRanges(p2y, minY, maxY, 0, prefW);
            p2z = MathUtils.mapValueForRanges(p2z, minZ, maxZ, 0, prefD);
            p3x = MathUtils.mapValueForRanges(p3x, minX, maxX, 0, prefW);
            p3y = MathUtils.mapValueForRanges(p3y, minY, maxY, 0, prefW);
            p3z = MathUtils.mapValueForRanges(p3z, minZ, maxZ, 0, prefD);

            Path3D path3D = new Path3D();
            path3D.moveTo(p1x, p1z, p1y);
            path3D.lineTo(p2x, p2z, p2y);
            path3D.lineTo(p3x, p3z, p3y);
            path3D.close();

            path3D.setColor(Color.RED);
            _scene.addShape(path3D);

            // Add back
            Path3D path3DBack = path3D.clone();
            path3DBack.reverse();
            _scene.addShape(path3DBack);
        }
    }

    /**
     * Rebuild gridlines.
     */
    protected void rebuildGridLines()
    {
        _grid.clear();
        _gridMinor.clear();
        _gridWithoutSep.clear();

        // Get graph bounds
        double areaX = 0;
        double areaY = 0;
        double areaW = _dataArea.getWidth();
        double areaH = _dataArea.getHeight();

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
        for (int i = 0, iMax = countY; i < iMax - 1; i++) {

            // Get interval ratio and line x & y
            double intervalRatio = i / (iMax - 1f);
            double lineY = areaY + areaH * intervalRatio;

            // DrawMajorAxis
            if (i > 0) {
                addGridLineMajor(areaX, lineY, areaX + areaW, lineY);
            }

            // Draw minor axis
            /*for (int j=0; j<minorTickCountY; j++) {
                double minorLineY = lineY + (j+1) * minorDeltaY;
                addGridLineMinor(areaX, minorLineY, areaX + areaW, minorLineY);
            }*/
        }

        // Get graph min interval and max interval
        AxisViewX axisViewX = _dataArea.getAxisViewX();
        Intervals intervalsX = axisViewX.getIntervals();
        int countX = intervalsX.getCount();

        // Iterate over graph intervals
        for (int i = 0; i < countX; i++) {
            double dataX = intervalsX.getInterval(i);
            double dispX = axisViewX.dataToView(dataX);
            addGridLineSeparator(dispX, areaY, dispX, areaY + areaH);
        }
    }
}
