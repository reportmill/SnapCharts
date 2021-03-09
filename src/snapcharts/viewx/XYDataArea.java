package snapcharts.viewx;
import snap.geom.*;
import snap.gfx.*;
import snap.util.PropChange;
import snapcharts.model.Axis;
import snapcharts.model.ChartType;
import snapcharts.model.DataPoint;
import snapcharts.model.DataSet;
import snapcharts.view.ChartHelper;
import snapcharts.view.DataArea;
import snapcharts.view.DataView;

/**
 * A DataArea subclass to display common XY ChartTypes: LINE, AREA, SCATTER.
 */
public class XYDataArea extends DataArea {

    // The ChartType
    private ChartType  _chartType;

    // The XYPainter (an object to provide data line path/shape)
    private XYPainter  _xyPainter = new XYPainter(this);

    // The TailShape
    private Shape  _tailShape;

    // Constants for defaults
    protected static Stroke Stroke1 = new Stroke(1, Stroke.Cap.Round, Stroke.Join.Round, 0);
    protected static Stroke Stroke2 = new Stroke(2, Stroke.Cap.Round, Stroke.Join.Round, 0);
    protected static Stroke Stroke3 = new Stroke(3, Stroke.Cap.Round, Stroke.Join.Round, 0);
    protected static Stroke Stroke5 = new Stroke(5, Stroke.Cap.Round, Stroke.Join.Round, 0);

    /**
     * Constructor.
     */
    public XYDataArea(ChartHelper aChartHelper, DataSet aDataSet)
    {
        this(aChartHelper, aDataSet, aChartHelper.getChartType());
    }

    /**
     * Constructor.
     */
    public XYDataArea(ChartHelper aChartHelper, DataSet aDataSet, ChartType aChartType)
    {
        super(aChartHelper, aDataSet);
        _chartType = aChartType;
    }

    /**
     * Paints the DataArea (ChartType/DataSet specific painting).
     */
    @Override
    protected void paintDataArea(Painter aPntr)
    {
        // Get area bounds
        double areaW = getWidth();
        double areaH = getHeight();

        // Get DataSet and index
        DataSet dset = getDataSet();
        int dsetIndex = dset.getIndex();

        // Get whether DataArea/DataSet is selected
        DataPoint selPoint = getChartView().getTargDataPoint();
        boolean isSelected = selPoint != null && selPoint.getDataSet() == dset;

        // If reveal is not full (1) then clip
        double reveal = getReveal();
        if (reveal < 1 && _chartType == ChartType.AREA) {
            aPntr.save();
            aPntr.clipRect(0, 0, areaW * reveal, areaH);
        }

        // Get dataShape (path) (if Reveal is active, get shape as SplicerShape so we can draw partial/animated)
        Shape dataShape = _xyPainter.getDataShape();
        if (reveal<1 && _chartType == ChartType.LINE)
            dataShape = new SplicerShape(dataShape, 0, reveal);

        // Get dataset color
        Color dataColor = getDataColor(dsetIndex);
        if (_chartType ==ChartType.AREA)
            dataColor = dataColor.blend(Color.CLEAR, .3);

        // Set color, stroke
        aPntr.setColor(dataColor);
        aPntr.setStroke(isSelected ? Stroke2 : Stroke1);

        // If ChartType.LINE, draw path
        if (_chartType == ChartType.LINE) {
            aPntr.setStrokePure(true);
            aPntr.draw(dataShape);
            aPntr.setStrokePure(false);
        }

        // If ChartType.AREA, fill path, too
        else if (_chartType == ChartType.AREA)
            aPntr.fill(dataShape);

        // If Reveal is active, paint TailShape
        if (dataShape instanceof SplicerShape)
            paintTailShape(aPntr, (SplicerShape) dataShape);

        // If reveal not full, restore gstate
        if (reveal < 1 && _chartType == ChartType.AREA)
            aPntr.restore();

        // If reveal is not full (1) then clip
        if (reveal < 1) {
            aPntr.save();
            aPntr.clipRect(0, 0, areaW * reveal, areaH);
        }

        // If ShowSymbols, paint symbols
        boolean showSymbols = dset.isShowSymbols() || _chartType == ChartType.SCATTER;
        if (showSymbols)
            paintSymbols(aPntr);

        // Paint selected point
        if (isSelected)
            paintSelPoint(aPntr);

        // If reveal not full, resture gstate
        if (reveal < 1)
            aPntr.restore();
    }

    /**
     * Paints symbols.
     */
    protected void paintSymbols(Painter aPntr)
    {
        // Get info
        DataSet dset = getDataSet();
        int dsetIndex = dset.getIndex();
        int pointCount = _xyPainter.getDispPointCount();
        Color color = getDataColor(dsetIndex);
        Shape symbolShape = getDataSymbolShape(dsetIndex);

        // Iterate over values
        for (int j=0; j<pointCount; j++) {

            // Get disp X/Y of symbol origin and translate there
            double dispX = _xyPainter.getDispX(j) - 4;
            double dispY = _xyPainter.getDispY(j) - 4;
            aPntr.translate(dispX, dispY);

            // Set color and fill symbol shape
            aPntr.setColor(color);
            aPntr.fill(symbolShape);

            // If Scatter chart, also stroke outline of shape
            if (_chartType == ChartType.SCATTER) {
                aPntr.setStroke(Stroke.Stroke1);
                aPntr.setColor(color.darker().darker());
                aPntr.draw(symbolShape);
            }

            // Translate back
            aPntr.translate(-dispX, -dispY);
        }
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
     * Paints the TailShape.
     */
    private void paintTailShape(Painter aPntr, SplicerShape splicer)
    {
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

    /**
     * Override for Subtype.Scatter.
     */
    @Override
    public Shape getDataSymbolShape(int anIndex)
    {
        // If ChartType SCATTER, return Ellipes
        if (_chartType == ChartType.SCATTER)
            return new Ellipse(0,0,9,9);

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
     * Override to return RevealTime based on path length.
     */
    @Override
    protected int getRevealTime()
    {
        // If not Line chart or DataSet.Disabled, return default
        if (_chartType != ChartType.LINE || getDataSet().isDisabled())
            return DataView.DEFAULT_REVEAL_TIME;

        // Calc factor to modify default time
        double maxLen =  _xyPainter.getArcLength();
        double factor = Math.max(1, Math.min(maxLen / 500, 2));

        // Return default time times factor
        return (int) Math.round(factor * DataView.DEFAULT_REVEAL_TIME);
    }

    /**
     * Clears the DataPath.
     */
    private void clearDataPath()
    {
        _xyPainter = new XYPainter(this);
        repaint();
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

    /**
     * Called when DataView changes size.
     */
    @Override
    protected void dataViewDidChangeSize()
    {
        clearDataPath();
    }

    /**
     * Called when AxisView changes properties.
     */
    @Override
    protected void axisViewDidChange(PropChange aPC)
    {
        clearDataPath();
    }
}