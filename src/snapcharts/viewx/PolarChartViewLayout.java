package snapcharts.viewx;

import snap.geom.Rect;
import snapcharts.view.*;

/**
 * A ChartViewLayout for Polar plots.
 */
public class PolarChartViewLayout extends ChartViewLayout {

    // The polar chart helper
    private PolarChartHelper _polarHelper;

    /**
     * Constructor
     */
    public PolarChartViewLayout(ChartView aChartView)
    {
        super(aChartView);

        // Set Polar helper
        _polarHelper = (PolarChartHelper) aChartView.getChartHelper();
    }

    /**
     * Override to handle axis views.
     */
    @Override
    public void layoutChart()
    {
        // Do normal version
        super.layoutChart();

        // Make sure ContentViews are up to date
        ContentView contentView = _chartView.getContentView();
        double dataW = contentView.getWidth();
        double dataH = contentView.getHeight();
        for (TraceView traceView : _polarHelper.getTraceViews())
            traceView.setBounds(0, 0, dataW, dataH);

        // Get polar bounds
        Rect polarInside = _polarHelper.getPolarBounds();
        Rect polarBounds = new Rect(contentView.getX() + polarInside.x, contentView.getY() + polarInside.y, polarInside.width, polarInside.height);

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
