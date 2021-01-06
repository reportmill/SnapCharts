package snapcharts.views;
import snap.geom.*;
import snap.gfx.*;
import snap.view.ViewAnim;
import snapcharts.model.ChartType;
import snapcharts.model.DataPoint;
import snapcharts.model.DataSet;

/**
 * A DataArea subclass to display common XY ChartTypes: LINE, AREA, SCATTER.
 */
public class DataAreaXY extends DataArea {

    // The ChartType
    private ChartType  _chartType;

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
    public DataAreaXY(ChartHelper aChartHelper, DataSet aDataSet)
    {
        super(aChartHelper, aDataSet);
        _chartType = aChartHelper.getChartType();
    }

    /**
     * Returns Path2D for painting dataset.
     */
    public Path2D getDataPath()
    {
        // If not animating, clear path
        ViewAnim anim = getAnim(-1);
        if (anim==null || !anim.isPlaying())
            _dataPath = null;

        // If already set, just return
        if (_dataPath !=null) return _dataPath;

        // Create/add path for dataset
        DataSet dset = getDataSet();
        int pointCount = dset.getPointCount();
        Path2D path = new Path2D();

        // Iterate over data points
        for (int j = 0; j < pointCount; j++) {

            // Get data X/Y and disp X/Y
            double dataX = dset.getX(j);
            double dataY = dset.getY(j);
            Point dispXY = dataToView(dataX, dataY);
            if (j == 0)
                path.moveTo(dispXY.x, dispXY.y);
            else path.lineTo(dispXY.x, dispXY.y);
        }

        // If area, close path
        if (_chartType ==ChartType.AREA) {
            double areaW = getWidth();
            double areaH = getHeight();
            Point point0 = path.getPoint(0);
            Point pointLast = path.getPoint(pointCount-1);
            path.lineTo(areaW, pointLast.y);
            path.lineTo(areaW, areaH);
            path.lineTo(0, areaH);
            path.lineTo(0, point0.y);
            path.close();
        }

        // Return path
        return _dataPath = path;
    }

    /**
     * Override to return RevealTime based on path length.
     */
    @Override
    protected int getRevealTime()
    {
        if (_chartType !=ChartType.LINE || getDataSet().isDisabled())
            return DataView.DEFAULT_REVEAL_TIME;

        // Calc factor to modify default time
        Path2D path = getDataPath();
        double maxLen =  path.getArcLength();
        double factor = Math.max(1, Math.min(maxLen / 500, 2));

        // Return default time times factor
        return (int) Math.round(factor * DataView.DEFAULT_REVEAL_TIME);
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

        // If reveal is not full (1) then clip
        if (reveal < 1 && _chartType ==ChartType.AREA) {
            aPntr.save();
            aPntr.clipRect(0, 0, areaW * reveal, areaH);
        }

        // Get path - if Reveal is active, get path spliced
        Shape path = getDataPath();
        if (reveal<1 && _chartType ==ChartType.LINE)
            path = new SplicerShape(path, 0, reveal);

        // Set dataset color, stroke and paint
        Color color = getDataColor(dsetIndex);
        if (_chartType ==ChartType.AREA)
            color = color.blend(Color.CLEAR, .3);
        aPntr.setColor(color);
        aPntr.setStroke(isSelected ? Stroke3 : Stroke2);

        // If ChartType.LINE, draw path
        if (_chartType ==ChartType.LINE) {
            aPntr.setColor(color.blend(Color.CLEAR, .98));
            aPntr.draw(path);
            aPntr.setColor(color);
            aPntr.draw(path);
        }

        // If ChartType.AREA, fill path, too
        else if (_chartType ==ChartType.AREA)
            aPntr.fill(path);

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

        // If reveal not full, resture gstate
        if (reveal < 1 && _chartType ==ChartType.AREA) aPntr.restore();

        // Get DataSets that ShowSymbols
        boolean showSymbols = dset.isShowSymbols() || _chartType ==ChartType.SCATTER;

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
                if (_chartType ==ChartType.SCATTER) {
                    aPntr.setStroke(Stroke.Stroke1);
                    aPntr.setColor(color.darker().darker());
                    aPntr.draw(symbol);
                }
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
        if (_chartType ==ChartType.SCATTER)
            return new Ellipse(0,0,9,9);
        return getChart().getSymbolShape(anIndex);
    }

    /**
     * Returns the tail shape.
     */
    public Shape getTailShape()
    {
        if (_tailShape!=null) return _tailShape;

        Path2D path = new Path2D();
        path.moveTo(0, 0);
        path.lineTo(16, 6);
        path.lineTo(0, 12);
        path.lineTo(5, 6);
        path.close();
        return _tailShape = path;
    }

    @Override
    public void setWidth(double aValue)
    {
        if (aValue==getWidth()) return;
        super.setWidth(aValue);
        _dataPath = null;
    }

    @Override
    public void setHeight(double aValue)
    {
        if (aValue==getHeight()) return;
        super.setHeight(aValue);
        _dataPath = null;
    }
}