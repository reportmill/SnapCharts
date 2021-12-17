/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.viewx;
import snap.geom.Path;
import snap.geom.Rect;
import snap.gfx.Color;
import snap.gfx3d.Camera3D;
import snap.gfx3d.PathBox3D;
import snap.gfx3d.Scene3D;
import snapcharts.model.Intervals;

/**
 * This class builds the Bar3D scene.
 */
public class Bar3DSceneBuilder extends AxisBoxSceneBuilder {

    // The Bar3DDataArea
    private Bar3DDataArea  _dataArea;

    /**
     * Constructor.
     */
    public Bar3DSceneBuilder(Bar3DDataArea aDataArea, Scene3D aScene)
    {
        super(aScene);
        _dataArea = aDataArea;
    }

    /**
     * Returns the intervals.
     */
    public Intervals getIntervalsY()  { return _dataArea.getIntervalsY(); }

    /**
     * Returns the minor tick count.
     */
    public int getMinorTickCount()  { return _dataArea.getMinorTickCount(); }

    /**
     * Returns the section count.
     */
    public int getSectionCount()  { return _dataArea.getSections().length; }

    /**
     * Rebuilds the chart.
     */
    protected void rebuildScene()
    {
        // Do normal version
        super.rebuildScene();

        // Vertical
        boolean vertical = true;

        // Get whether to draw fast
        boolean fullRender = true; // !isValueAdjusting()

        // Get depth of layers
        //double layerDepth = depth / _layerCount;

        // Calculate bar depth
        //double barDepth = layerDepth/(1 + _graph.getBars().getBarGap());

        // Constrain bar depth to bar width
        //barDepth = Math.min(barDepth, _barWidth);

        // If pseudo3d, depth should be layer depth
        //if (isPseudo3D())
        //    barDepth = layerDepth;

        // Calcuate bar min/max
        //double barMin = (layerDepth-barDepth)/2;
        //double barMax = layerDepth - barMin;

        // Iterate over bars and add each bar shape at bar layer
        //for (int i=0, iMax=_bars.size(); i<iMax; i++) { Bar bar = _bars.get(i);
        //addShapesForRMShape(bar.barShape, barMin + bar.layer*layerDepth, barMax + bar.layer*layerDepth, false); }

        // If no pseudo 3d, add axis and bar labels as 3d shapes
        /*if (!_camera.isPseudo3D()) {

            // Create axis label shapes
            for (int i=0, iMax=_axisLabels.size(); i<iMax && fullRender; i++)
                addShapesForRMShape(_axisLabels.get(i), -.1f, -.1f, false);

            // Create bar label shapes
            for (int i=0, iMax=_barLabels.size(); i<iMax && fullRender; i++) {

                // Get current loop bar label and bar label type
                RMShape barLabel = _barLabels.get(i);

                // Handle outside labels
                if (_barLabelPositions.get(i)==RMGraphPartSeries.LabelPos.Above ||
                    _barLabelPositions.get(i)==RMGraphPartSeries.LabelPos.Below)
                    addShapesForRMShape(barLabel, depth/2, depth/2, false);

                // Handle inside
                else addShapesForRMShape(barLabel, (depth - _barWidth)/2 - 5, (depth - _barWidth)/2 - 5, false);
            }
        }*/

        // If Pseudo3d, add bar labels
        /*if (_camera.isPseudo3D()) {

            // Create axis label shapes
            for (int i=0, iMax=_axisLabels.size(); i<iMax && fullRender; i++)
                addChild(_axisLabels.get(i));

            // Create bar label shapes
            for (int i=0, iMax=_barLabels.size(); i<iMax && fullRender; i++)
                addChild(_barLabels.get(i));
        }*/

        // Add bars
        addBars();
    }

    /**
     * Paints chart.
     */
    protected void addBars()
    {
        // Iterate over sections
        int traceCount = _dataArea._traceCount;
        int pointCount = _dataArea._pointCount;
        BarDataArea.Section[] sections = _dataArea.getSections();
        for (int i=0;i<pointCount;i++) {
            BarDataArea.Section section = sections[i];

            // Iterate over traces and draw bars
            for (int j = 0; j < traceCount; j++) {
                BarDataArea.Bar bar = section.bars[j];
                addBar(bar.x, bar.y, bar.width, bar.height - .5, bar.color);
            }
        }
    }

    /**
     * Adds a 3d bar to scene.
     */
    private void addBar(double aX, double aY, double aW, double aH, Color aColor)
    {
        // If reveal is set, modify Y/H
        double reveal = _dataArea.getReveal();
        if (reveal<1) {
            double nh = aH*reveal;
            aY += aH - nh;
            aH = nh;
        }

        // Get depth, and Z values for back/front
        Camera3D camera = _scene.getCamera();
        double depth = camera.getDepth();
        double z0 = depth/2 - aW / 2;
        double z1 = depth/2 + aW / 2;

        // Create/configure bar path/path3d and add to scene
        Path path = new Path(new Rect(aX, aY, aW, aH));
        PathBox3D bar = new PathBox3D(path, z0, z1);
        bar.setColor(aColor);
        bar.setStroke(Color.BLACK, 1);
        _scene.addShape(bar);
    }
}
