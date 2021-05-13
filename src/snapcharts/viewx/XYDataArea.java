package snapcharts.viewx;
import snap.geom.*;
import snap.gfx.*;
import snap.util.PropChange;
import snapcharts.model.*;
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

        // Get whether DataArea/DataSet is selected
        DataSet dset = getDataSet();
        DataPoint selPoint = getChartView().getTargDataPoint();
        boolean isSelected = selPoint != null && selPoint.getDataSet() == dset;

        // Get style info
        DataStyle dataStyle = dset.getDataStyle();
        boolean showLine = dataStyle.isShowLine();
        Stroke dataStroke = dataStyle.getLineStroke();
        boolean showSymbols = dataStyle.isShowSymbols() || _chartType == ChartType.SCATTER;

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
        Color dataColor = getDataColor();

        // If ChartType.AREA, fill path, too
        if (_chartType == ChartType.AREA) {
            Color dataColorArea = dataColor.blend(Color.CLEAR, .3);
            aPntr.setColor(dataColorArea);
            aPntr.fill(dataShape);
        }

        // If ChartType.LINE, draw path
        if (isSelected && showLine) {
            aPntr.setStrokePure(true);

            // If selected, draw path
            Color selColor = dataColor.blend(Color.CLEARWHITE, .75);
            Stroke selStroke = dataStroke.copyForWidth(dataStroke.getWidth() * 3 + 8).copyForDashes(null);
            aPntr.setColor(selColor);
            aPntr.setStroke(selStroke);
            aPntr.draw(dataShape);

            // If selected, draw path
            Color selColor2 = dataColor.blend(Color.WHITE, 1);
            Stroke selStroke2 = dataStroke.copyForWidth(dataStroke.getWidth() + 2);
            aPntr.setColor(selColor2);
            aPntr.setStroke(selStroke2);
            aPntr.draw(dataShape);

            aPntr.setStrokePure(false);
            aPntr.setColor(dataColor);
            aPntr.setStroke(dataStroke);
        }

        // Set color, stroke
        aPntr.setColor(dataColor);
        aPntr.setStroke(dataStroke);

        // If ChartType.LINE, draw path
        if (showLine) {
            aPntr.setStrokePure(true);
            aPntr.draw(dataShape);
            aPntr.setStrokePure(false);
        }

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
        int pointCount = _xyPainter.getDispPointCount();
        Color color = getDataColor();
        Shape symbolShape = getDataSymbolShape();
        double symbolShift = getDataSymbol().getSize() / 2d;

        // Iterate over values
        for (int j=0; j<pointCount; j++) {

            // Get disp X/Y of symbol origin and translate there
            double dispX = _xyPainter.getDispX(j) - symbolShift;
            double dispY = _xyPainter.getDispY(j) - symbolShift;
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
        DataSet dataSet = getDataSet();
        DataPoint selPoint = getChartView().getTargDataPoint();
        int selIndex = selPoint.getIndex();

        // Get data X/Y and disp X/Y
        double dataX = dataSet.getX(selIndex);
        double dataY = dataSet.getY(selIndex);
        double dispX = dataToViewX(dataX);
        double dispY = dataToViewY(dataY);

        // Get data color and symbol
        Color dataColor = getDataColor();
        Symbol dataSymbol = getDataSymbol();
        double symbolOffset = dataSymbol.getSize() / 2d;
        Shape dataSymbolShape = dataSymbol.getShape().copyFor(new Transform(dispX - symbolOffset, dispY - symbolOffset));

        // Set color for glow effect
        aPntr.setColor(dataColor.blend(Color.CLEARWHITE, .5));
        double haloSize = dataSymbol.getSize() * 2 + 4;
        double haloOffset = haloSize / 2d;
        aPntr.fill(new Ellipse(dispX - haloOffset, dispY - haloOffset, haloSize, haloSize));

        // Paint selected symbol
        aPntr.setStroke(Stroke5);
        aPntr.setColor(Color.WHITE);
        aPntr.draw(dataSymbolShape);
        aPntr.setStroke(Stroke3);
        aPntr.setColor(dataColor);
        aPntr.draw(dataSymbolShape);
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