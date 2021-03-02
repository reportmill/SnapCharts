package snapcharts.views;

import snap.geom.*;
import snap.gfx.Color;
import snap.gfx.Painter;
import snap.gfx.Stroke;
import snap.util.PropChange;
import snap.view.ViewAnim;
import snapcharts.model.*;

/**
 * A DataArea subclass to display Polar charts.
 */
public class DataAreaPolar extends DataArea {

    // The rect around the polar grid
    private Rect  _polarBounds;

    // The Path2D for painting DataSet
    private Path2D  _dataPath;

    // The TailShape
    private Shape  _tailShape;

    // Constants for defaults
    protected static Stroke Stroke2 = new Stroke(2, Stroke.Cap.Round, Stroke.Join.Round, 0);
    protected static Stroke Stroke3 = new Stroke(3, Stroke.Cap.Round, Stroke.Join.Round, 0);
    protected static Stroke Stroke5 = new Stroke(5, Stroke.Cap.Round, Stroke.Join.Round, 0);

    /**
     * Constructor.
     */
    public DataAreaPolar(ChartHelper aChartHelper, DataSet aDataSet)
    {
        super(aChartHelper, aDataSet);
    }

    /**
     * Returns the polar area bounds.
     */
    public Rect getPolarBounds()
    {
        // If already set, just return
        if (_polarBounds!=null) return _polarBounds;

        // Calc polar rect
        double viewW = getWidth();
        double viewH = getHeight();
        double areaX = 0;
        double areaY = 0;
        double areaW = getWidth();
        double areaH = getHeight();
        if (areaW<areaH) {
            areaH = areaW;
            areaY = areaY + Math.round((viewH - areaH)/2);
        }
        else {
            areaW = areaH;
            areaX = areaX + Math.round((viewW - areaW)/2);
        }

        // Set/return
        return _polarBounds = new Rect(areaX, areaY, areaW, areaH);
    }

    /**
     * Override to suppress.
     */
    @Override
    public void paintBorder(Painter aPntr)  { }

    /**
     * Paints chart axis lines.
     */
    public void paintGridlines(Painter aPntr)
    {
        paintRadialLines(aPntr);
        paintAngleLines(aPntr);
    }

    /**
     * Paints chart radial axis lines.
     */
    protected void paintRadialLines(Painter aPntr)
    {
        // Get info X
        AxisViewX axisViewX = getAxisViewX(); if (axisViewX==null) return;
        AxisX axis = axisViewX.getAxis();
        Color gridColor = new Color("#d0"); //axisViewX.getGridColor().darker();
        Color tickLineColor = AxisView.TICK_LINE_COLOR;
        //double tickLen = axis.getTickLength();
        double reveal = getReveal();

        // Get info Y
        AxisViewY axisViewY = getAxisViewY(); if (axisViewY==null) return;

        // Set Grid Color/Stroke
        aPntr.setColor(gridColor);
        aPntr.setStroke(axisViewX.getGridStroke());

        // Get area bounds
        Rect areaBnds = getPolarBounds();
        double areaX = areaBnds.x;
        double areaY = areaBnds.y;
        double areaW = areaBnds.width;
        double areaH = areaBnds.height;
        double areaMidX = areaX + areaW/2;
        double areaMidY = areaY + areaH/2;

        // A shared arc
        Arc arc = new Arc(areaX, areaY, areaW, areaH, 0, 360);

        // Iterate over intervals and paint lines
        Intervals ivals = axisViewX.getIntervals();
        for (int i = 0, iMax = ivals.getCount(); i < iMax; i++) {

            // Get something
            double dataRad = ivals.getInterval(i);
            double dispX = (int) Math.round(axisViewX.dataToView(dataRad));
            double dispY = (int) Math.round(axisViewY.dataToView(dataRad));
            double radLenX = dispX - areaMidX;
            double radLenY = areaMidY - dispY;
            radLenX *= reveal;
            radLenY *= reveal;

            // Draw radial
            aPntr.setColor(gridColor);
            double radX = areaMidX - radLenX;
            double radY = areaMidY - radLenY;
            arc.setRect(radX, radY, radLenX*2, radLenY*2);
            arc.setStartAngle(0);
            arc.setSweepAngle(-360 * reveal);
            arc.setClosure(Arc.Closure.Open);
            aPntr.draw(arc);

            aPntr.setColor(tickLineColor);
            arc.setStartAngle(-3);
            arc.setSweepAngle(6);
            arc.setClosure(Arc.Closure.Chord);
            aPntr.draw(arc);
        }
    }

    /**
     * Paints chart angle axis lines.
     */
    protected void paintAngleLines(Painter aPntr)
    {
        // Get info X
        AxisViewX axisViewX = getAxisViewX(); if (axisViewX==null) return;
        AxisX axis = axisViewX.getAxis();
        Color gridColor = axisViewX.getGridColor();
        double reveal = getReveal();

        // Get info Y
        AxisViewY axisViewY = getAxisViewY(); if (axisViewY==null) return;

        // Set Grid Color/Stroke
        aPntr.setColor(gridColor);
        aPntr.setStroke(axisViewX.getGridStroke());

        // Get area bounds
        Rect areaBnds = getPolarBounds();
        double areaX = areaBnds.x;
        double areaY = areaBnds.y;
        double areaW = areaBnds.width;
        double areaH = areaBnds.height;
        double areaMidX = areaX + areaW/2;
        double areaMidY = areaY + areaH/2;

        // Get interval max
        Intervals ivals = axisViewX.getIntervals();
        double dataRad = ivals.getMax();
        double dispX = (int) Math.round(axisViewX.dataToView(dataRad));
        double dispY = (int) Math.round(axisViewY.dataToView(dataRad));
        double radLenX = dispX - areaMidX;
        double radLenY = areaMidY - dispY;
        radLenX *= reveal;
        radLenY *= reveal;

        // Iterate over intervals and paint lines
        for (int i = 0, iMax = 360; i < iMax; i+=15) {
            double angleRad = -Math.toRadians((i)) * (-reveal);
            double radX = areaMidX + radLenX * Math.cos(angleRad);
            double radY = areaMidY - radLenY * Math.sin(angleRad);
            aPntr.drawLine(areaMidX, areaMidY, radX, radY);
        }

        // Iterate over intervals and paint lines
        aPntr.setColor(Color.BLACK);
        for (int i = 0, iMax = 91; i < iMax; i+=90) {
            double angleRad = Math.toRadians(i!=90 ? i : -270) * reveal;
            double radX = areaMidX + radLenX * Math.cos(angleRad);
            double radY = areaMidY - radLenY * Math.sin(angleRad);
            aPntr.drawLine(areaMidX, areaMidY, radX, radY);
        }
    }

    /**
     * Returns Path2D for painting dataset.
     */
    public Path2D getDataPath()
    {
        // If already set, just return
        if (_dataPath !=null) return _dataPath;

        // Get AxisView X
        AxisViewX axisViewX = getAxisViewX();
        Intervals intervalsX = axisViewX.getIntervals();
        double minX = intervalsX.getMin();
        double maxX = intervalsX.getMax();

        // Get AxisView Y
        AxisViewY axisViewY = getAxisViewY();
        double minY = _chartHelper.getAxisMinForIntervalCalc(axisViewY);
        double maxY = _chartHelper.getAxisMaxForIntervalCalc(axisViewY);

        // Create/add path for dataset
        DataSet dset = getDataSet();
        int pointCount = dset.getPointCount();
        Path2D path = new Path2D();

        // Iterate over data points
        for (int j = 0; j < pointCount; j++) {

            // Get data X/Y and disp X/Y
            double dataX = dset.getX(j);
            double dataY = dset.getY(j);
            double dataTheta = (dataX - minX) / (maxX - minX) * -360 + 90;
            double dataRad = (dataY - minY) / (maxY - minY) * (maxX - minX) + minX;
            double dataX2 = Math.sin(Math.toRadians(dataTheta)) * dataRad;
            double dataY2 = Math.cos(Math.toRadians(dataTheta)) * dataRad;
            Point dispXY = dataToView(dataX2, dataY2);
            if (j == 0)
                path.moveTo(dispXY.x, dispXY.y);
            else path.lineTo(dispXY.x, dispXY.y);
        }

        // Return path
        return _dataPath = path;
    }

    /**
     * Clears the DataPath.
     */
    private void clearDataPath()
    {
        _dataPath = null;
    }

    /**
     * Paints chart.
     */
    protected void paintChart(Painter aPntr)
    {
        // Get area
        double areaW = getWidth();
        double areaH = getHeight();

        // Get DataSet list
        DataSet dset = getDataSet();
        int dsetIndex = dset.getIndex();

        DataPoint selPoint = getChartView().getTargDataPoint();
        boolean isSelected = selPoint != null && selPoint.getDataSet() == dset;
        double reveal = getReveal();

        // Get path - if Reveal is active, get path spliced
        Shape path = getDataPath();
        if (reveal<1)
            path = new SplicerShape(path, 0, reveal);

        // Set dataset color, stroke and paint
        Color color = getDataColor(dsetIndex);
        aPntr.setColor(color);
        aPntr.setStroke(isSelected ? Stroke3 : Stroke2);

        // If ChartType.LINE, draw path
        aPntr.setColor(color.blend(Color.CLEAR, .98));
        aPntr.draw(path);
        aPntr.setColor(color);
        aPntr.draw(path);

        // If Reveal is active, paint end point
        if (path instanceof SplicerShape) {
            SplicerShape splicer = (SplicerShape) path;
            Point tailPoint = splicer.getTailPoint();
            double tailAngle = splicer.getTailAngle();
            Shape tailShape = getTailShape();
            Rect tailShapeBounds = tailShape.getBounds();
            double tailShapeX = tailPoint.x - tailShapeBounds.getMidX();
            double tailShapeY = tailPoint.y - tailShapeBounds.getMidY();
            aPntr.save();
            aPntr.rotateAround(tailAngle, tailPoint.x, tailPoint.y);
            aPntr.translate(tailShapeX, tailShapeY);
            aPntr.fill(tailShape);
            aPntr.restore();
        }

        // Get DataSets that ShowSymbols
        boolean showSymbols = dset.isShowSymbols();

        // If reveal is not full (1) then clip
        if (reveal < 1) {
            aPntr.save();
            aPntr.clipRect(0, 0, areaW * reveal, areaH);
        }

        // Draw dataset points
        if (showSymbols) {

            // Get dataset index (could be different if DataSetList has disabled sets)
            int pointCount = dset.getPointCount();

            // Iterate over values
            for (int j=0; j<pointCount; j++) {

                // Get data X/Y and disp X/Y
                double dataX = dset.getX(j);
                double dataY = dset.getY(j);
                double dispX = dataToViewX(dataX);
                double dispY = dataToViewY(dataY);

                // Get symbol and color and paint
                Shape symbol = getDataSymbolShape(dsetIndex).copyFor(new Transform(dispX - 4, dispY - 4));
                aPntr.setColor(color);
                aPntr.fill(symbol);
            }
        }

        // Paint selected point
        if (isSelected)
            paintSelPoint(aPntr);

        // If reveal not full, resture gstate
        if (reveal < 1)
            aPntr.restore();
    }

    /**
     * Paints selected point.
     */
    protected void paintSelPoint(Painter aPntr)
    {
        // Get info
        DataSet dset = getDataSet();
        int dsetIndex = dset.getIndex();
        DataPoint selPoint = getChartView().getTargDataPoint();
        int selIndex = selPoint.getIndex();

        // Get data X/Y and disp X/Y
        double dataX = dset.getX(selIndex);
        double dataY = dset.getY(selIndex);
        double dispX = dataToViewX(dataX);
        double dispY = dataToViewY(dataY);

        // Get data color and symbol
        Color dataColor = getDataColor(dsetIndex);
        Shape dataSymbol = getDataSymbolShape(dsetIndex).copyFor(new Transform(dispX - 4, dispY - 4));

        // Set color for glow effect
        aPntr.setColor(dataColor.blend(Color.CLEARWHITE, .5));
        aPntr.fill(new Ellipse(dispX - 10, dispY - 10, 20, 20));

        // Get symbol
        aPntr.setStroke(Stroke5);
        aPntr.setColor(Color.WHITE);
        aPntr.draw(dataSymbol);
        aPntr.setStroke(Stroke3);
        aPntr.setColor(dataColor);
        aPntr.draw(dataSymbol);
    }

    /**
     * Override for Subtype.Scatter.
     */
    @Override
    public Shape getDataSymbolShape(int anIndex)
    {
        // Otherwise get DataSymbol for DataSet index
        return getChart().getSymbolShape(anIndex);
    }

    /**
     * Returns the tail shape.
     */
    public Shape getTailShape()
    {
        // If already set, just return
        if (_tailShape!=null) return _tailShape;

        // Create/configure/set TailShape
        Path2D path = new Path2D();
        path.moveTo(0, 0);
        path.lineTo(16, 6);
        path.lineTo(0, 12);
        path.lineTo(5, 6);
        path.close();
        return _tailShape = path;
    }

    /**
     * Called when a ChartPart changes.
     */
    protected void chartPartDidChange(PropChange aPC)
    {
        Object src = aPC.getSource();
        if (src==getDataSet() || src instanceof Axis) {
            clearDataPath();
        }
    }

    @Override
    public void setWidth(double aValue)
    {
        if (aValue==getWidth()) return;
        super.setWidth(aValue);
        clearDataPath();
        _polarBounds = null;
    }

    @Override
    public void setHeight(double aValue)
    {
        if (aValue==getHeight()) return;
        super.setHeight(aValue);
        clearDataPath();
        _polarBounds = null;
    }
}