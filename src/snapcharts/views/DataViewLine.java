package snapcharts.views;
import java.util.*;
import snap.geom.*;
import snap.gfx.*;
import snap.view.ViewAnim;
import snapcharts.model.ChartType;
import snapcharts.model.DataPoint;
import snapcharts.model.DataSet;
import snapcharts.model.DataSetList;

/**
 * A ChartArea subclass to display the contents of line chart.
 */
public class DataViewLine extends DataViewPanZoom {

    // The DataSet paths
    List<Path2D>  _dataSetPaths;

    // The TailShape
    private Shape  _tailShape;

    // Constants for defaults
    protected static Stroke Stroke2 = new Stroke(2, Stroke.Cap.Round, Stroke.Join.Round, 0);
    protected static Stroke Stroke3 = new Stroke(3, Stroke.Cap.Round, Stroke.Join.Round, 0);
    protected static Stroke Stroke5 = new Stroke(5, Stroke.Cap.Round, Stroke.Join.Round, 0);

    /**
     * Creates a ChartAreaLine.
     */
    public DataViewLine()
    {
    }

    /**
     * Returns the type.
     */
    public ChartType getChartType()
    {
        return ChartType.LINE;
    }

    /**
     * Returns the list of paths for each dataset.
     */
    public List<Path2D> getDataSetPaths()
    {
        // If already set, just return
        ViewAnim anim = getAnim(-1); if (anim==null || !anim.isPlaying()) _dataSetPaths = null;
        if (_dataSetPaths!=null) return _dataSetPaths;

        // Get info
        DataSetList dataSetList = getDataSetList();
        List<DataSet> dsets = dataSetList.getDataSets();
        List<Path2D> paths = new ArrayList<>();

        // Iterate over datasets
        for (DataSet dset : dsets) {

            // Create/add path for dataset
            int pointCount = dset.getPointCount();
            Path2D path = new Path2D();
            paths.add(path);

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
        }

        // Return paths
        return _dataSetPaths = paths;
    }

    /**
     * Override to return RevealTime based on path length.
     */
    @Override
    protected int getRevealTime()
    {
        // Get all paths
        List<Path2D> paths = getDataSetPaths(); if (paths.size()==0) return REVEAL_TIME;

        // Calc factor to modify default time
        double maxLen = 0; for (Path2D path : paths) maxLen = Math.max(maxLen, path.getArcLength());
        double factor = Math.max(1, Math.min(maxLen / 500, 2.5));

        // Return default time times factor
        return (int) Math.round(factor*REVEAL_TIME);
    }

    /**
     * Paints chart.
     */
    protected void paintChart(Painter aPntr, double aX, double aY, double aW, double aH)
    {
        // Get DataSet list
        DataSetList dataSetList = getDataSetList();
        List<DataSet> dsets = dataSetList.getDataSets();

        DataPoint selPoint = _chartView.getTargDataPoint();
        DataSet selSet = selPoint != null ? selPoint.getDataSet() : null;
        double reveal = getReveal();

        // Draw dataset paths
        List<Path2D> paths = getDataSetPaths();
        for (int i=0; i<paths.size(); i++) {

            // Get path - if Reveal is active, get path spliced
            Shape path = paths.get(i);
            if (reveal<1)
                path = new SplicerShape(path, 0, reveal);

            // Set dataset color, stroke and paint
            DataSet dset = dsets.get(i);
            Color color = getColor(dset.getIndex());
            aPntr.setColor(color);
            aPntr.setStroke(dset==selSet ? Stroke3 : Stroke2);
            aPntr.draw(path);

            // If Reveal is active, paint end point
            if (reveal<1) {
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
        }

        // Get DataSets that ShowSymbols
        List<DataSet> dsetsSymb = dataSetList.getDataSetsFiltered(dset -> dset.isShowSymbols());

        // If reveal is not full (1) then clip
        if (reveal < 1) {
            aPntr.save();
            aPntr.clipRect(0, 0, getWidth() * reveal, getHeight());
        }

        // Draw dataset points
        for (DataSet dset : dsetsSymb) {

            // Get dataset index (could be different if DataSetList has disabled sets)
            int dsetIndex = dset.getIndex();
            int pointCount = dset.getPointCount();

            // Iterate over values
            for (int j=0; j<pointCount; j++) {

                // Get data X/Y and disp X/Y
                double dataX = dset.getX(j);
                double dataY = dset.getY(j);
                double dispX = dataToViewX(dataX);
                double dispY = dataToViewY(dataY);

                // Get symbol and color and paint
                Shape symbol = getSymbolShape(dsetIndex).copyFor(new Transform(dispX - 4, dispY - 4));
                Color color = getColor(dsetIndex);
                aPntr.setColor(color);
                aPntr.fill(symbol);
            }
        }

        // Paint selected point
        paintSelPoint(aPntr);

        // If reveal not full, resture gstate
        if (reveal < 1) aPntr.restore();
    }

    /**
     * Paints selected point.
     */
    protected void paintSelPoint(Painter aPntr)
    {
        // Get DataSet list
        DataPoint selPoint = _chartView.getTargDataPoint(); if (selPoint == null) return;
        DataSet dset = selPoint.getDataSet();
        int dsetIndex = dset.getIndex();
        int selIndex = selPoint.getIndex();

        // Get data X/Y and disp X/Y
        double dataX = dset.getX(selIndex);
        double dataY = dset.getY(selIndex);
        double dispX = dataToViewX(dataX);
        double dispY = dataToViewY(dataY);

        Shape symbol = getSymbolShape(dsetIndex).copyFor(new Transform(dispX - 4, dispY - 4));
        Color color = getColor(dsetIndex);

        aPntr.setColor(color.blend(Color.CLEARWHITE, .5));
        aPntr.fill(new Ellipse(dispX - 10, dispY - 10, 20, 20));
        aPntr.setStroke(Stroke5);
        aPntr.setColor(Color.WHITE);
        aPntr.draw(symbol);
        aPntr.setStroke(Stroke3);
        aPntr.setColor(color);
        aPntr.draw(symbol);
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
        super.setWidth(aValue);
        _dataSetPaths = null;
    }

    @Override
    public void setHeight(double aValue)
    {
        super.setHeight(aValue);
        _dataSetPaths = null;
    }
}