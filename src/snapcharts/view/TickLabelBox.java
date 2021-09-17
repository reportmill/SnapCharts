package snapcharts.view;
import snap.geom.Size;
import snap.gfx.Font;
import snap.gfx.Paint;
import snap.view.ChildView;
import snapcharts.model.Axis;
import snapcharts.model.DataSet;
import snapcharts.model.DataSetList;
import snapcharts.model.Intervals;

import java.util.ArrayList;
import java.util.List;

/**
 * A container view to hold TickLabels.
 */
public class TickLabelBox extends ChildView {

    // The maximum rotated size of tick labels
    private Size  _maxLabelRotatedSize;

    /**
     * Returns the AxisView.
     */
    public AxisView getAxisView()  { return (AxisView) getParent(); }

    /**
     * Override to return TickLabels.MaxWidth for AxisViewY.
     */
    @Override
    protected double getPrefWidthImpl(double aH)
    {
        // If X axis, just return 200 (X axis doesn't have a PrefWidth)
        AxisView axisView = (AxisView) getParent();
        if (axisView instanceof AxisViewX)
            return 200;

        // Get/return max ticks size width
        Size maxTicksSize = axisView.getMaxTickLabelRotatedSize();
        return maxTicksSize.width;
    }

    /**
     * Override to return text height for AxisViewX.
     */
    @Override
    protected double getPrefHeightImpl(double aW)
    {
        // If Y axis, just return 200 (Y axis doesn't have a PrefHeight)
        AxisView axisView = (AxisView) getParent();
        if (axisView instanceof AxisViewY)
            return 200;

        // Get/return max ticks size height
        Size maxTicksSize = axisView.getMaxTickLabelRotatedSize();
        return maxTicksSize.height;
    }
}
