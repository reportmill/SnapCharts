package snapcharts.view;
import snap.view.*;
import snapcharts.model.Chart;

import java.util.Arrays;

/**
 * This class lays out legend entries in columns.
 */
public class LegendViewBoxV extends ChildView {

    // The number of rows/cols after layout
    private int  _rowCount, _colCount;

    // The Max X/Y after layout
    private double  _maxX, _maxY;

    // The children from ChartView layout sizing
    private ViewProxy<?>[]  _layoutChildren;

    /**
     * Custom getPrefWidthImpl() method.
     */
    @Override
    protected double getPrefWidthImpl(double aH)
    {
        // If no children, just return
        if (getChildCount() == 0) return 0;

        // Get ViewProxy
        ViewProxy<?> viewProxy = getViewProxy();

        // Get LegendView
        ParentView legendView = getParent();
        boolean inLegendLayout = legendView.isInLayout();
        boolean firstPass = !inLegendLayout;

        // If firstPass, use ChartView.Height
        if (firstPass) {

            // Start legend layout with ChartView.Height. If it bleeds over LegendView.Height, it will scale back down
            ChartView chartView = getParent(ChartView.class);
            double chartH = chartView.getHeight();

            // If ChartHeight will definitely result in 5+ cols, bump ChartHeight to at least that
            int entryCount = getChildCount();
            double entryH = getChild(0).getPrefHeight() + getSpacing();
            double minColH = Math.ceil(entryCount / 5d) * entryH;
            if (chartH < minColH)
                chartH = Math.ceil(minColH);

            // Run our first layout
            viewProxy.setSize(-1, chartH);
            layoutProxy(viewProxy);
            _layoutChildren = viewProxy.getChildren();

            // If multi-column, see if scale up to 150% will eliminate a column
            if (_colCount > 1) {
                int colCount = _colCount;
                double maxX = _maxX;
                double maxY = _maxY;

                // Iterate up to 150% by 5% increments
                for (int i = 1; i <= 10; i++) {
                    double scaleFactor = 1 + i / 20d;
                    double adjustedChartH = Math.round(chartH * scaleFactor);
                    viewProxy.setSize(-1, adjustedChartH);
                    viewProxy.setChildren(null);
                    layoutProxy(viewProxy);
                    if (_colCount < colCount && (_maxX < _maxY || i + 1 == 10)) {
                        _layoutChildren = viewProxy.getChildren();
                        colCount = _colCount;
                        maxX = _maxX;
                        maxY = _maxY;
                        break;
                    }
                }
                _colCount = colCount;
                _maxX = maxX;
                _maxY = maxY;
            }
        }

        // Return MaxX
        return _maxX;
    }

    /**
     * Custom getPrefWidthImpl() method.
     */
    @Override
    protected double getPrefHeightImpl(double aW)
    {
        return _maxY;
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
        double childX = 0;

        // Reset row/col count and max X/Y
        _rowCount = _colCount = 0;
        _maxX = _maxY = 0;

        // Do layout till all children set
        while (true) {

            // Layout entries
            ColView.layoutProxy(viewProxy);

            // Get index of first child below bottom bounds
            int indexOutOfBounds = getIndexOutOfBoundsY(viewProxy);

            // Break children into list of in-bounds and out-of-bounds
            ViewProxy<?>[] children = viewProxy.getChildren();
            ViewProxy<?>[] childrenIn = indexOutOfBounds > 0 ? Arrays.copyOfRange(children, 0, indexOutOfBounds) : children;
            ViewProxy<?>[] childrenOut = indexOutOfBounds > 0 ? Arrays.copyOfRange(children, indexOutOfBounds, children.length) : new ViewProxy<?>[0];

            // Update Row/col counts
            _rowCount = Math.max(_rowCount, childrenIn.length);
            _colCount++;

            // Update Max X/Y
            for (ViewProxy<?> child : childrenIn) {
                child.setX(childX);
                _maxX = Math.max(_maxX, child.getMaxX());
                _maxY = Math.max(_maxY, child.getMaxY());
            }

            // If no children outside bounds, break out of loop
            if (childrenOut.length == 0)
                break;

            // Bump running ChildX to next column and set out-of-bounds children to layout next column
            childX = _maxX + viewProxy.getSpacing();
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
     * Returns the index of first child below bottom bounds.
     */
    private int getIndexOutOfBoundsY(ViewProxy viewProxy)
    {
        ViewProxy<?>[] children = viewProxy.getChildren();
        for (int i = 0; i < children.length; i++) {
            ViewProxy<?> child = children[i];
            if (child.getMaxY() > viewProxy.getHeight())
                return i;
        }
        return -1;
    }
}
