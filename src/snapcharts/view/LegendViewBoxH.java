package snapcharts.view;
import snap.geom.Insets;
import snap.view.ChildView;
import snap.view.RowView;
import snap.view.ViewProxy;
import snapcharts.model.Chart;
import snapcharts.model.Legend;

import java.util.Arrays;

/**
 * This class lays out legend entries in rows.
 */
public class LegendViewBoxH extends ChildView {

    // The ChartView width for cached PrefSize + Layout info
    private double  _chartW;

    // The number of rows after layout
    private int  _rowCount;

    // The Max X/Y after layout
    private double  _maxX, _maxY;

    // The children from ChartView layout sizing
    private ViewProxy<?>[]  _layoutChildren;

    /**
     * Override getPrefWidth() instead of Impl() to bypass normal view PrefSize caching.
     */
    @Override
    public double getPrefWidth(double aH)
    {
        // If ChartWidth has changed, recalc PrefSize
        // Probably don't need this - getPrefHeight() should have already been called
        ChartView chartView = getParent(ChartView.class);
        double chartW = chartView.getWidth();
        if (chartW != _chartW) {
            System.out.println("LegendViewBoxH.getPrefW: Surprised that ChartWidth has changed");
            getPrefSizeImpl(chartView, chartW);
        }

        // Return MaxX
        return _maxX;
    }

    /**
     * Override getPrefHeight() instead of Impl() to bypass normal view PrefSize caching.
     */
    @Override
    public double getPrefHeight(double aW)
    {
        // If ChartWidth has changed, recalc PrefSize
        ChartView chartView = getParent(ChartView.class);
        double chartW = chartView.getWidth();
        if (chartW != _chartW)
            getPrefSizeImpl(chartView, chartW);

        // Return MaxY
        return _maxY;
    }

    /**
     * Get PrefSize.
     */
    private void getPrefSizeImpl(ChartView chartView, double chartW)
    {
        // Set ChartWidth
        _chartW = chartW;

        // If no children, just return
        if (getChildCount() == 0) return;

        // Relayout ScaleBox
        getParent().relayout();

        // Get ViewProxy
        ViewProxy<?> viewProxy = getViewProxy();

        // Remove Chart Padding, Legend Margin
        Chart chart = chartView.getChart();
        Legend legend = chart.getLegend();
        Insets chartPadding = chart.getPadding();
        Insets legendMargin = legend.getMargin();
        double insLeft = Math.max(chartPadding.left, legendMargin.left);
        double insRight = Math.max(chartPadding.right, legendMargin.right);
        chartW -= insLeft + insRight;

        // If ChartWidth will definitely result in 5+ rows, bump ChartWidth to at least that
        int entryCount = getChildCount();
        double entryW = getAverageWidth(viewProxy.getChildren()) + getSpacing();
        double minRowW = Math.ceil(entryCount / 5d) * entryW;
        //if (chartW < minRowW)
        //    chartW = Math.ceil(minRowW);

        // Run our first layout
        viewProxy.setSize(chartW, -1);
        layoutProxy(viewProxy);
        _layoutChildren = viewProxy.getChildren();

        // If multi-row, see if scale up to 150% will eliminate a column
        if (_rowCount > 1) {

            //
            int rowCount = _rowCount;
            double maxX = _maxX;
            double maxY = _maxY;

            //
            double scaleFactorMax = 1.6;
            switch (rowCount) {
                case 2: scaleFactorMax = 1.1; break;
                case 3: scaleFactorMax = 1.2; break;
                case 4: scaleFactorMax = 1.3; break;
                case 5: scaleFactorMax = 1.4; break;
                case 6: scaleFactorMax = 1.5; break;
                default: scaleFactorMax = 1.6; break;
            }

            // Iterate up to 150% by 5% increments
            for (double scaleFactor = 1 + .05; scaleFactor <= scaleFactorMax; scaleFactor += .05) {
                double adjustedChartW = Math.round(chartW * scaleFactor);
                viewProxy.setSize(adjustedChartW, -1);
                viewProxy.setChildren(null);
                layoutProxy(viewProxy);
                if (_rowCount < rowCount && (_maxY < _maxX || scaleFactor + .05 > scaleFactorMax)) {
                    _layoutChildren = viewProxy.getChildren();
                    rowCount = _rowCount;
                    maxX = _maxX;
                    maxY = _maxY;
                    break;
                }
            }
            _rowCount = rowCount;
            _maxX = maxX;
            _maxY = maxY;
        }
    }

    /**
     * Custom layoutImpl() method.
     */
    @Override
    protected void layoutImpl()
    {
        ViewProxy<?> viewProxy = getViewProxy();
        viewProxy.setChildren(_layoutChildren);
        viewProxy.setBoundsInClient();
    }

    /**
     * Real layout method.
     */
    protected void layoutProxy(ViewProxy<?> viewProxy)
    {
        // Get all children array and declare variable for current column X location
        ViewProxy<?>[] childrenAll = viewProxy.getChildren();
        double childY = 0;

        // Reset RowCount and MaxX/Y
        _rowCount = 0;
        _maxX = _maxY = 0;

        // Do layout till all children set
        while (true) {

            // Layout entries
            RowView.layoutProxy(viewProxy);

            // Get index of first child below bottom bounds
            int indexOutOfBounds = getIndexOutOfBoundsX(viewProxy);

            // Break children into list of in-bounds and out-of-bounds
            ViewProxy<?>[] children = viewProxy.getChildren();
            ViewProxy<?>[] childrenIn = indexOutOfBounds > 0 ? Arrays.copyOfRange(children, 0, indexOutOfBounds) : children;
            ViewProxy<?>[] childrenOut = indexOutOfBounds > 0 ? Arrays.copyOfRange(children, indexOutOfBounds, children.length) : new ViewProxy<?>[0];

            // Update RowCount
            _rowCount++;

            // Update Max X/Y
            for (ViewProxy<?> child : childrenIn) {
                child.setY(childY);
                _maxX = Math.max(_maxX, child.getMaxX());
                _maxY = Math.max(_maxY, child.getMaxY());
            }

            // If no children outside bounds, break out of loop
            if (childrenOut.length == 0)
                break;

            // Bump running ChildY to next row and set out-of-bounds children to layout next column
            childY = _maxY + viewProxy.getSpacing();
            viewProxy.setChildren(childrenOut);
        }

        // Restore all children to ViewProxy
        viewProxy.setChildren(childrenAll);
    }

    /**
     * Returns ViewProxy to layout legend entries.
     */
    protected ViewProxy<?> getViewProxy()
    {
        ViewProxy<?> viewProxy = new ViewProxy<>(this);
        return viewProxy;
    }

    /**
     * Returns the average width of given ViewProxys in given array.
     */
    private static double getAverageWidth(ViewProxy<?>[] viewProxies)
    {
        double totalW = 0;
        for (ViewProxy<?> viewProxy : viewProxies)
            totalW += viewProxy.getBestWidth(-1);
        double avgW = totalW / viewProxies.length;
        return Math.ceil(avgW);
    }

    /**
     * Returns the index of first child below bottom bounds.
     */
    private static int getIndexOutOfBoundsX(ViewProxy viewProxy)
    {
        double boundsW = viewProxy.getWidth();
        ViewProxy<?>[] children = viewProxy.getChildren();
        for (int i = 0; i < children.length; i++) {
            ViewProxy<?> child = children[i];
            if (child.getMaxX() > boundsW)
                return i;
        }
        return -1;
    }
}
