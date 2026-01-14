/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.view;
import snap.geom.Insets;
import snap.view.*;
import java.util.Arrays;

/**
 * This class lays out legend entries in columns.
 */
public class LegendViewBoxV extends ChildView {

    // The ChartView height for cached PrefSize + Layout info
    private double  _chartH;

    // The number of cols after layout
    private int  _colCount;

    // The Max X/Y after layout
    private double  _maxX, _maxY;

    // The children from ChartView layout sizing
    private ViewLayout<?>[]  _layoutChildren;

    /**
     * Override getPrefWidth() instead of Impl() to bypass normal view PrefSize caching.
     */
    @Override
    public double getPrefWidth(double aH)
    {
        // Get ChartHeight - EntryBox really uses this for PrefSize+Layout
        ChartView chartView = getParent(ChartView.class);
        double chartH = chartView.getHeight();
        if (chartH == _chartH)
            return _maxX;

        // Set ChartHeight
        _chartH = chartH;

        // If no children, just return
        if (getChildCount() == 0) return 0;

        // Relayout ScaleBox
        getParent().relayout();

        // Get layout
        ColViewLayout<?> viewLayout = getViewLayout();

        // If ChartHeight will definitely result in 5+ cols, bump ChartHeight to at least that
        int entryCount = getChildCount();
        double entryH = getChild(0).getPrefHeight() + getSpacing();
        double minColH = Math.ceil(entryCount / 5d) * entryH;
        if (chartH < minColH)
            chartH = Math.ceil(minColH);

        // Run our first layout
        viewLayout.setSize(-1, chartH);
        layoutProxy(viewLayout);
        _layoutChildren = viewLayout.getChildren();

        // If multi-column, see if scale up to 150% will eliminate a column
        if (_colCount > 1) {
            int colCount = _colCount;
            double maxX = _maxX;
            double maxY = _maxY;

            // Iterate up to 150% by 5% increments
            for (int i = 1; i <= 10; i++) {
                double scaleFactor = 1 + i / 20d;
                double adjustedChartH = Math.round(chartH * scaleFactor);
                viewLayout.setSize(-1, adjustedChartH);
                viewLayout.clearChildren();
                layoutProxy(viewLayout);
                if (_colCount < colCount && (_maxX < _maxY || i + 1 == 10)) {
                    _layoutChildren = viewLayout.getChildren();
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

        // Return MaxX
        return _maxX;
    }

    /**
     * Override getPrefHeight() instead of Impl() to bypass normal view PrefSize caching.
     */
    @Override
    public double getPrefHeight(double aW)
    {
        return _maxY;
    }

    /**
     * Custom layoutImpl() method.
     */
    @Override
    protected void layoutImpl()
    {
        ViewLayout<?> viewLayout = getViewLayout();
        viewLayout.setChildren(_layoutChildren);
        viewLayout.setBoundsInClient();
    }

    /**
     * Real layout method.
     */
    protected void layoutProxy(ColViewLayout<?> viewProxy)
    {
        // Get all children array and declare variable for current column X location
        ViewLayout<?>[] childrenAll = viewProxy.getChildren();
        double childX = 0;

        // Reset col count and max X/Y
        _colCount = 0;
        _maxX = _maxY = 0;

        // If has Title entry, handle that special
        LegendView legendView = getParent(LegendView.class);
        if (legendView._titleView.isVisible()) {

            // Set layout children to TitleView layout, layout and update MaxX, MaxY for used space
            ViewLayout<?> titleViewLayout = childrenAll[0];
            viewProxy.setChildren(new ViewLayout<?>[] {titleViewLayout});
            viewProxy.setPadding(null);
            viewProxy.layoutViewLayout();
            _maxX = titleViewLayout.getMaxX();
            _maxY = titleViewLayout.getMaxY();

            // Set layout Padding to reserve TitleView space and set layout Children to remaining children
            viewProxy.setPadding(new Insets(_maxY + getSpacing(), 0, 0, 0));
            viewProxy.setChildren(Arrays.copyOfRange(childrenAll, 1, childrenAll.length));
        }

        // Do layout till all children set
        while (true) {

            // Layout entries
            viewProxy.layoutViewLayout();

            // Get index of first child below bottom bounds
            int indexOutOfBounds = getIndexOutOfBoundsY(viewProxy);

            // Break children into list of in-bounds and out-of-bounds
            ViewLayout<?>[] children = viewProxy.getChildren();
            ViewLayout<?>[] childrenIn = indexOutOfBounds > 0 ? Arrays.copyOfRange(children, 0, indexOutOfBounds) : children;
            ViewLayout<?>[] childrenOut = indexOutOfBounds > 0 ? Arrays.copyOfRange(children, indexOutOfBounds, children.length) : new ViewLayout<?>[0];

            // Update col count
            _colCount++;

            // Update Max X/Y
            for (ViewLayout<?> child : childrenIn) {
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

        // Restore all children
        viewProxy.setChildren(childrenAll);
    }

    /**
     * Returns layout to layout legend entries.
     */
    protected ColViewLayout<?> getViewLayout()  { return new ColViewLayout<>(this); }

    /**
     * Returns the index of first child below bottom bounds.
     */
    private int getIndexOutOfBoundsY(ViewLayout<?> viewLayout)
    {
        ViewLayout<?>[] children = viewLayout.getChildren();
        for (int i = 0; i < children.length; i++) {
            ViewLayout<?> child = children[i];
            if (child.getMaxY() > viewLayout.getHeight())
                return i;
        }
        return -1;
    }
}
