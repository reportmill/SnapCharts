package snapcharts.views;
import snap.geom.Path;
import snap.geom.Rect;
import snap.gfx.*;
import snapcharts.app.Intervals;
import snapcharts.gfx3d.*;
import snapcharts.model.DataPoint;

/**
 * A ChartArea subclass to display the contents of bar chart.
 */
public class DataViewBar3D extends DataViewBar {
    
    // The Camera
    protected CameraView _camView;
    
    // The Camera
    private Camera _camera;
    
    // The Scene
    private Scene3D _scene;
    
    // Stuff
    private int  _layerCount = 1;
    
    // Shapes for grid
    private Path _grid = new Path();
    
    // Shapes for the minor grid
    private Path  _gridMinor = new Path();
    
    // The grid path without separators
    private Path  _gridWithoutSep = new Path();
    
    /**
     * Creates a ChartAreaBar.
     */
    public DataViewBar3D()
    {
        _camView = new CameraView() {
            protected void layoutImpl() { rebuildScene(); }
        };
        addChild(_camView);
        _camera = _camView.getCamera();
        _scene = _camView.getScene();

        _camera.setYaw(26); _camera.setPitch(10); _camera.setDepth(100);
        _camera.setFocalLength(8*72); _camera.setAdjustZ(true);
    }

    /**
     * Returns the CameraView.
     */
    public CameraView getCameraView()  { return _camView; }

    /**
     * Returns the number of intervals for this filled graph.
     */
    public int getIntervalCount()  { return getActiveIntervals().getCount(); }

    /**
     * Returns the individual interval at a given index as a float value.
     */
    public Double getInterval(int anIndex)  { return getActiveIntervals().getInterval(anIndex); }

    /**
     * Returns the last interval as a float value.
     */
    public Double getIntervalLast()  { return getInterval(getIntervalCount()-1); }

    /**
     * Returns the number of suggested ticks between the intervals of the RPG'd graph.
     */
    public int getMinorTickCount()
    {
        // Calcuate height per tick - if height greater than 1 inch, return 4, greater than 3/4 inch return 3, otherwise 1
        double heightPerTick = getHeight()/(getIntervalCount() - 1);
        return heightPerTick>=72? 4 : heightPerTick>=50? 3 : 1;
    }


    protected void layoutImpl()
    {
        _camView.setSize(getWidth(), getHeight());
    }

    /**
     * Rebuilds the chart.
     */
    protected void rebuildScene()
    {
        rebuildGridLines();
        boolean vertical = true;

        // Get whether to draw fast
        boolean fullRender = true; // !isValueAdjusting()

        // Remove all existing children
        //_camView.removeChildren();
        _scene.removeShapes();

        // Get standard width, height, depth
        double width = getWidth(), height = getHeight(), depth = _camera.getDepth();

        // Get depth of layers
        double layerDepth = depth/_layerCount;

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

        // Calculate whether back plane should be shifted to the front. Back normal = { 0, 0,-1 }.
        boolean shiftBack = _camera.isFacingAway(_scene.localToCameraForVector(0, 0, -1));
        double backZ = shiftBack? 0 : depth;

        // Create back plane shape
        Path3D back = new Path3D(); back.setOpacity(.8f);
        back.setColor(Color.WHITE); //if (_backFill!=null) back.setColor(_backFill.getColor());
        back.setStroke(Color.BLACK, 1); //if (_backStroke!=null) back.setStroke(_backStroke.getColor(),_backStroke.getWidth());
        back.moveTo(0, 0, backZ); back.lineTo(0, height, backZ);
        back.lineTo(width, height, backZ); back.lineTo(width, 0, backZ); back.close();
        if (!shiftBack) back.reverse();
        _scene.addShape(back);

        // Add Grid to back
        Path3D grid = new Path3D(_grid, backZ); grid.setStroke(Color.BLACK, 1);
        back.addLayer(grid);

        // Add GridMinor to back
        Path3D gridMinor = new Path3D(_gridMinor, backZ); gridMinor.setStrokeColor(Color.LIGHTGRAY);
        back.addLayer(gridMinor);

        // Calculate whether side plane should be shifted to the right. Side normal = { 1, 0, 0 }.
        boolean shiftSide = vertical && !_camera.isPseudo3D() && _camera.isFacingAway(_scene.localToCameraForVector(1,0,0));
        double sideX = shiftSide? width : 0;

        // Create side path shape
        Path3D side = new Path3D(); side.setColor(Color.LIGHTGRAY); side.setStroke(Color.BLACK, 1); side.setOpacity(.8f);
        side.moveTo(sideX, 0, 0); side.lineTo(sideX, height, 0);
        side.lineTo(sideX, height, depth); side.lineTo(sideX, 0, depth); side.close();

        // For horizonatal bar charts, make sure the side panel always points towards the camera
        boolean sideFacingAway = _camera.isFacingAway(_scene.localToCamera(side));
        if (sideFacingAway) side.reverse();
        _scene.addShape(side);

        // Create floor path shape
        Path3D floor = new Path3D(); floor.setColor(Color.LIGHTGRAY); floor.setStroke(Color.BLACK, 1); floor.setOpacity(.8f);
        floor.moveTo(0, height + .5, 0); floor.lineTo(width, height + .5, 0);
        floor.lineTo(width, height + .5, depth); floor.lineTo(0, height + .5, depth); floor.close();
        boolean floorFacingAway = _camera.isFacingAway(_scene.localToCamera(floor));
        if (floorFacingAway) floor.reverse();
        _scene.addShape(floor);

        // Determine whether side grid should be added to graph side or floor
        Path3D sideGridBuddy = vertical? side : floor;
        Rect gridWithoutSepBnds = _gridWithoutSep.getBounds(), gridMinorBnds = _gridMinor.getBounds();
        Rect gridRect = vertical? new Rect(0, gridWithoutSepBnds.y, depth, gridWithoutSepBnds.height) :
            new Rect(gridWithoutSepBnds.x, 0, gridWithoutSepBnds.width, depth);
        Rect gridMinorRect = vertical? new Rect(0, gridMinorBnds.y, depth, gridMinorBnds.height) :
            new Rect(gridMinorBnds.x, 0, gridMinorBnds.width, depth);
        Transform3D gridTrans = vertical? new Transform3D().rotateY(-90).translate(sideX, 0, 0) :
            new Transform3D().rotateX(90).translate(0, height, 0);

        // Configure grid
        Path sideGridPath = _gridWithoutSep.copyFor(gridRect);
        Path3D sideGrid = new Path3D(sideGridPath, 0); sideGrid.transform(gridTrans); sideGrid.setStroke(Color.BLACK, 1);
        sideGridBuddy.addLayer(sideGrid);

        // Add GridMinor to side3d
        Path sideGridPathMinor = _gridMinor.copyFor(gridMinorRect);
        Path3D sideGridMinor = new Path3D(sideGridPathMinor, 0); sideGridMinor.transform(gridTrans);
        sideGridMinor.setStroke(Color.LIGHTGRAY,1);
        sideGridBuddy.addLayer(sideGridMinor);
        sideGridBuddy.setColor(Color.WHITE); //if (_backFill!=null) sideGridBuddy.setColor(_backFill.getColor());
        sideGridBuddy.setStroke(Color.BLACK, 1); //if (_backStroke!=null) sideGridBuddy.setStroke(_backStroke.getColor(), _backStroke.getWidth());

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
        addBars();
    }

    /**
     * Paints chart.
     */
    protected void addBars()
    {
        // Get selected point index (section index)
        DataPoint dataPoint = _chartView.getTargDataPoint();
        int selIndex = dataPoint!=null? dataPoint.getIndex() : -1;

        double cx = 0, cy = 0, cw = getWidth(), ch = getHeight();
        Section sections[] = getSections();

        // If reveal is not full (1) then clip
        //if (getReveal()<1) {
        //    aPntr.save(); aPntr.clipRect(0,getHeight()*(1-getReveal()),getWidth(),getHeight()*getReveal()); }

        // Iterate over sections
        for (int i=0;i<_pointCount;i++) { Section section = sections[i];

            // If selected section, draw background
            //if (i==selIndex) {
            //    aPntr.setColor(Color.get("#4488FF09")); aPntr.fillRect(cx + i*section.width, cy, section.width, ch); }

            // Iterate over series and draw bars
            for (int j=0;j<_seriesCount;j++) { Bar bar = section.bars[j];
                addBar(bar.x, bar.y, bar.width, bar.height - .5, bar.color);
            }
        }

        // If reveal not full, resture gstate
        //if (getReveal()<1) aPntr.restore();
    }

    /**
     * Paints chart.
     */
    protected void addBar(double aX, double aY, double aW, double aH, Color aColor)
    {
        double reveal = getReveal(); if (reveal<1) { double nh = aH*reveal; aY += aH - nh; aH = nh; }
        double depth = _camera.getDepth(), z0 = depth/2 - aW/2, z1 = depth/2 + aW/2;
        Path path = new Path(new Rect(aX, aY, aW, aH));
        PathBox3D bar = new PathBox3D(path, z0, z1, false); bar.setColor(aColor); bar.setStroke(Color.BLACK, 1);
        _scene.addShape(bar);
    }

    /**
     * Paints chart axis lines.
     */
    protected void paintFront(Painter aPntr) { }

    /**
     * Paints chart.
     */
    protected void paintChart(Painter aPntr, double aX, double aY, double aW, double aH)  { }

    void rebuildGridLines()
    {
        Intervals intervals = getActiveIntervals();
        _grid.clear(); _gridMinor.clear(); _gridWithoutSep.clear();

        // Get graph min interval and max interval
        double minInterval = intervals.getMin();
        double maxInterval = intervals.getMax();
        double totalInterval = maxInterval - minInterval;
        boolean vertical = true;

        // Get graph bounds
        Rect bounds = getBoundsLocal();

        // Get grid line width/height
        double lineW = vertical? bounds.width : 0;
        double lineH = vertical? 0 : bounds.height;

        // Get grid max
        double gridMax = vertical? bounds.height : bounds.width;
        double intervalSize = intervals.getInterval(1) - minInterval;
        double minorTickInterval = gridMax*intervalSize/totalInterval/(getMinorTickCount()+1);

        // Iterate over graph intervals
        for (int i=0, iMax=getIntervalCount(); i<iMax - 1; i++) {

            // Get interval ratio and line x & y
            double intervalRatio = i/(iMax - 1f);
            double lineX = vertical? bounds.x : bounds.x + bounds.width*intervalRatio;
            double lineY = vertical? bounds.y + bounds.height*intervalRatio : bounds.y;

            // DrawMajorAxis
            if (i>0) {
                addGridLineMajor(lineX, lineY, lineX + lineW, lineY + lineH); //line.setFrame(lineX, lineY, lineW, lineH);
            }

            // If not drawing minor axis, just continue
            //if (!_graph.getValueAxis().getShowMinorGrid()) continue;

            // Draw minor axis
            for (int j=0; j<getMinorTickCount(); j++) {
                double minorLineX = vertical? bounds.x : lineX + (j+1)*minorTickInterval;
                double minorLineY = vertical? lineY + (j+1)*minorTickInterval : bounds.y;
                addGridLineMinor(minorLineX, minorLineY, minorLineX + lineW, minorLineY + lineH);
            }
        }

        // Get whether zero axis line was added
        /*boolean zeroAxisLineAdded = false;
        if (_graph.getValueAxis().getShowMajorGrid())
            for (int i=0, iMax=getIntervalCount(); i<iMax; i++)
                zeroAxisLineAdded = zeroAxisLineAdded || MathUtils.equalsZero(getInterval(i));*/

        // If zero axis line not added, add it (happens when there are pos & neg values)
        /*if (!zeroAxisLineAdded) {
            RMLineShape line = new RMLineShape();
            double intervalRatio = minInterval/totalInterval;
            double lineX = isVertical()? 0 : -bounds.width*intervalRatio;
            double lineY = isVertical()? bounds.height + bounds.height*intervalRatio : 0;
            line.setFrame(lineX, lineY, lineW, lineH);
            _barShape.addGridLineMajor(line);
        }*/

        // If drawGroupSeparator, add separator line for each section, perpendicular to grid
        boolean getLabelAxis_getShowGridLines = true;
        if (getLabelAxis_getShowGridLines) {

            // Get SeriesCount, SeriesItemCount and SectionCount
            //int seriesCount = getSeriesCount(), seriesItemCount = seriesCount>0? getSeries(0).getPointCount() : 0;
            //int sectionCount = _meshed? seriesItemCount : seriesCount;
            int sectionCount = getSections().length;

            // Iterate over series
            for (int i=1, iMax=sectionCount; i<iMax; i++) {
                double lineX = vertical? bounds.x + bounds.width*i/iMax : bounds.x;
                double lineY = vertical? bounds.y : bounds.y + bounds.height*i/iMax;
                addGridLineSeparator(lineX, lineY, lineX + (vertical? 0:bounds.width), lineY + (vertical? bounds.height:0));
            }
        }
    }

    /** Adds a major grid line to the graph view. */
    void addGridLineMajor(double X0, double Y0, double X1, double Y1)
    {
        _grid.moveTo(X0, Y0); _grid.lineTo(X1, Y1);
        _gridWithoutSep.moveTo(X0, Y0); _gridWithoutSep.lineTo(X1, Y1);
    }

    /** Adds a minor grid line to the graph view. */
    void addGridLineMinor(double X0, double Y0, double X1, double Y1)
    {
        _gridMinor.moveTo(X0, Y0); _gridMinor.lineTo(X1, Y1);
    }

    /** Adds a grid line separator to the graph view. */
    void addGridLineSeparator(double X0, double Y0, double X1, double Y1)
    {
        _grid.moveTo(X0, Y0); _grid.lineTo(X1, Y1);
    }

    /**
     * Override to hide x/y axis and legend.
     */
    public void activate()
    {
        _chartView.getAxisX().setVisible(false);
        _chartView.getAxisY().setVisible(false);
    }

    /**
     * Override to restore x/y axis and legend.
     */
    public void deactivate()
    {
        _chartView.getAxisX().setVisible(true);
        _chartView.getAxisY().setVisible(true);
    }

    /**
     * Override to rebuild chart.
     */
    public void setReveal(double aValue)  { super.setReveal(aValue); _camView.relayout(); }

    /**
     * Registers for animation.
     */
    public void animate()
    {
        super.animate();
        _camView.setYaw(90); _camView.getAnimCleared(1000).setValue(CameraView.Yaw_Prop,26);
        _camView.setPitch(0); _camView.getAnim(1000).setValue(CameraView.Pitch_Prop,10);
        _camView.setOffsetZ(200); _camView.getAnim(1000).setValue(CameraView.OffsetZ_Prop,0).setLinear().play();
    }
}