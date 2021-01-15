package snapcharts.views;

import snap.geom.Rect;

/**
 * A ChartViewLayout for Polar plots.
 */
public class ChartViewLayoutPolar extends ChartViewLayout {

    // The polar chart helper
    private ChartHelperPolar _polarHelper;

    /**
     * Constructor
     */
    public ChartViewLayoutPolar(ChartView aChartView)
    {
        super(aChartView);

        // Set Polar helper
        _polarHelper = (ChartHelperPolar) aChartView.getChartHelper();
    }

    /**
     * Override to handle axis views.
     */
    @Override
    public void layoutChart()
    {
        // Do normal version
        super.layoutChart();

        // Make sure DataViews are up to date
        DataView dataView = _chartView.getDataView();
        double dataW = dataView.getWidth();
        double dataH = dataView.getHeight();
        for (DataArea dataArea : _polarHelper.getDataAreas())
            dataArea.setBounds(0, 0, dataW, dataH);

        // Get polar bounds
        Rect polarInside = _polarHelper.getPolarBounds();
        Rect polarBounds = new Rect(dataView.getX() + polarInside.x, dataView.getY() + polarInside.y, polarInside.width, polarInside.height);

        // Layout X axis
        AxisViewX axisViewX = _polarHelper.getAxisViewX();
        double xaxisX = polarBounds.getMidX();
        double xaxisY = polarBounds.getMidY();
        double xaxisW = polarBounds.getWidth()/2;
        double xaxisH = axisViewX.getPrefHeight();
        axisViewX.setBounds(xaxisX, xaxisY, xaxisW, xaxisH);

        // Layout X axis
        AxisViewY axisViewY = _polarHelper.getAxisViewY();
        double yaxisW = axisViewY.getPrefWidth();
        double yaxisX = polarBounds.getMidX() - yaxisW;
        double yaxisY = polarBounds.y;
        double yaxisH = polarBounds.getHeight()/2;
        axisViewY.setBounds(yaxisX, yaxisY, yaxisW, yaxisH);
    }
}
