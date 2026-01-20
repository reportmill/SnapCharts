/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.view;
import snap.geom.*;
import snap.view.*;
import snapcharts.charts.*;
import java.util.Objects;

/**
 * A view to display chart legend.
 */
public class LegendView extends ChartPartView<Legend> {

    // A ScaleBox to make sure Legend always fits
    private ScaleBox  _scaleBox;

    // The ChildView (ColView or RowView) to hold Legend Entries
    private ChildView  _entryBox;

    // The view to hold title text
    protected StringView  _titleView;

    /**
     * Constructor.
     */
    public LegendView()
    {
        super();

        // Create/configure/add ScaleBox to hold EntryBox
        _scaleBox = new ScaleBox();
        _scaleBox.setKeepAspect(true);
        addChild(_scaleBox);

        // Create/configure TitleView
        _titleView = new StringView();
        _titleView.setMargin(0, 0, 5, 0);

        // Register for click
        addEventHandler(e -> handleLegendMouseReleaseEvent(), MouseRelease);
    }

    /**
     * Returns the legend.
     */
    public Legend getLegend()  { return getChart().getLegend(); }

    /**
     * Returns the ChartPart.
     */
    @Override
    public Legend getChartPart()  { return getLegend(); }

    /**
     * Returns the position of the legend.
     */
    public Pos getPosition()  { return getChartPart().getPosition(); }

    /**
     * Returns whether legend is inside chart content.
     */
    public boolean isInside()  { return getChartPart().isInside(); }

    /**
     * Override to return true when floating.
     */
    @Override
    public boolean isMovable()
    {
        return isFloating();
    }

    /**
     * Override to return true when floating.
     */
    @Override
    public boolean isResizable()
    {
        return isFloating();
    }

    /**
     * Override to move legend via UserXY.
     */
    @Override
    public void processMoveEvent(ViewEvent anEvent, ViewEvent lastEvent)
    {
        // Get change in View X/Y
        double dx = anEvent.getX() - lastEvent.getX();
        double dy = anEvent.getY() - lastEvent.getY();

        // Update new XY
        Point userXY = getUserXY();
        Point newXY = new Point(userXY.x + dx, userXY.y + dy);
        setUserXY(newXY);
    }

    /**
     * Override to resize legend via UserXY and UserSize.
     */
    @Override
    public void processResizeEvent(ViewEvent anEvent, ViewEvent lastEvent, Pos aHandlePos)
    {
        // Get change in View X/Y
        double dx = anEvent.getX() - lastEvent.getX();
        double dy = anEvent.getY() - lastEvent.getY();

        // Calculate change in View Width/Height for handle (and maybe adjust change in X/Y)
        double dw = 0;
        double dh = 0;
        switch (aHandlePos.getHPos()) {
            case LEFT: dw = -dx; break;
            case CENTER: dx = 0; break;
            case RIGHT: dw = dx; dx = 0; break;
        }
        switch (aHandlePos.getVPos()) {
            case TOP: dh = -dy; break;
            case CENTER: dy = 0; break;
            case BOTTOM: dh = dy; dy = 0; break;
        }

        // Update new bounds
        Point userXY = getUserXY();
        Size userSize = getUserSize();
        Point newXY = new Point(userXY.x + dx, userXY.y + dy);
        Size newSize = new Size(userSize.width + dw, userSize.height + dh);
        setUserXY(newXY);
        setUserSize(newSize);
    }

    /**
     * Returns whether legend is floating (position is center but legend not isInside).
     */
    public boolean isFloating()
    {
        return getChartPart().isFloating();
    }

    /**
     * Returns the floating legend bounds.
     */
    public Rect getFloatingBounds()
    {
        Point userXY = getUserXY();
        Size userSize = getUserSize();
        return new Rect(userXY.x, userXY.y, userSize.width, userSize.height);
    }

    /**
     * Returns location of floating legend in ChartView coords.
     */
    public Point getUserXY()
    {
        // Get defined UserXY
        Legend legend = getLegend();
        Point userXY = legend.getUserXY();

        // If defined, convert from ContentView fractional to ChartView coords for current ChartView size
        if (userXY != null) {
            ChartView chartView = getChartView();
            ContentView contentView = chartView.getContentView();
            double dispX = Math.round(userXY.x * contentView.getWidth() + contentView.getX());
            double dispY = Math.round(userXY.y * contentView.getHeight() + contentView.getY());
            return new Point(dispX, dispY);
        }

        // If null, but Floating is set, return XY of centered UserSize (PrefSize probably)
        if (isFloating()) {
            Size userSize = getUserSize();
            double dispX = Math.round((getChartView().getWidth() - userSize.width) / 2);
            double dispY = Math.round((getChartView().getHeight() - userSize.height) / 2);
            return new Point(dispX, dispY);
        }

        // Return
        return userXY;
    }

    /**
     * Sets location of floating legend in ChartView coords.
     */
    public void setUserXY(Point aPoint)
    {
        // Convert from ChartView to ContentView fractional
        if (aPoint != null) {
            ChartView chartView = getChartView();
            View contentView = chartView.getContentView();
            double fractX = (aPoint.x - contentView.getX()) / contentView.getWidth();
            double fractY = (aPoint.y - contentView.getY()) / contentView.getHeight();
            aPoint = new Point(fractX, fractY);
        }
        Legend legend = getLegend();
        legend.setUserXY(aPoint);
    }

    /**
     * Returns size of floating legend in ChartView coords.
     */
    public Size getUserSize()
    {
        // Get Legend.UserSize
        Legend legend = getLegend();
        Size userSize = legend.getUserSize();

        // If not defined, user PrefSize instead
        if (userSize == null)
            userSize = getPrefSize();

        // If Legend.UserXY is defined, make sure UserSize doesn't go outside chart bounds
        if (legend.getUserXY() != null) {
            Point userXY = getUserXY();
            ChartView chartView = getChartView();
            double chartW = chartView.getWidth();
            double chartH = chartView.getHeight();
            if (userXY.x + userSize.width > chartW || userXY.y + userSize.height > chartH) {
                if (userXY.x + userSize.width > chartW)
                    userSize = userSize.withWidth(chartW - userXY.x - 2);
                if (userXY.y + userSize.height > chartH)
                    userSize = userSize.withHeight(chartH - userXY.y - 2);
            }
        }

        // Return
        return userSize;
    }

    /**
     * Sets size of floating legend in ChartView coords.
     */
    public void setUserSize(Size aSize)
    {
        Legend legend = getLegend();
        if (!Objects.equals(aSize, getUserSize()))
            legend.setUserSize(aSize);
    }

    /**
     * Reloads legend contents.
     */
    public void resetView()
    {
        // Do normal version
        super.resetView();

        // Get info
        Legend legend = getLegend();

        // Handle visible
        boolean showLegend = legend.isShowLegend();
        setVisible(showLegend);
        if (!showLegend)
            return;

        // Handle Title.Text
        String titleText = legend.getTitle().getText();
        _titleView.setText(titleText);
        _titleView.setVisible(titleText != null && !titleText.isEmpty());
        _titleView.setLeanX(legend.getAlignX());

        // Handle Inside
        if (legend.isInside() || isFloating()) {
            ChartView chartView = getChartView();
            ViewUtils.moveToFront(chartView, this);
        }

        // Reset EntryBox
        resetEntryBox();
        _entryBox.setSpacing(_entryBox.getSpacing() + legend.getSpacing());

        // Iterate over Traces and add entries
        Content content = getContent();
        Trace[] traces = content.getTraces();
        for (Trace trace : traces) {

            // If not Trace.ShowLegendEntry, just continue
            if (!trace.isShowLegendEntry())
                continue;

            // Create, add LegendEntryView for Trace
            View entryView = new LegendEntryView(legend, trace);
            _entryBox.addChild(entryView);

            // Register row to enable/disable
            entryView.addEventHandler(e -> handleEntryViewMouseReleaseEvent(e, entryView), MouseRelease);
        }
    }

    /**
     * Resets the EntryBox.
     */
    private void resetEntryBox()
    {
        // Get position and update View.Vertical
        Legend legend = getLegend();
        Pos pos = getPosition();
        boolean isVer = !(pos == Pos.TOP_CENTER || pos == Pos.BOTTOM_CENTER);
        setVertical(isVer);

        // Create new EntryBox and add to ScaleBox
        _entryBox = newEntryBox();
        _scaleBox.setContent(_entryBox);

        // Handle Vertical layout
        if (isVer) {
            _scaleBox.setAlignX(legend.getAlignX());
            _scaleBox.setAlignY(pos.getVPos());
        }

        // Handle Horizontal layout
        else {
            _scaleBox.setAlignX(legend.getAlignX());
            _scaleBox.setAlignY(VPos.CENTER);
        }

        // Add TitleView
        _entryBox.addChild(_titleView);
    }

    /**
     * Creates the row or column view that holds LegendEntryViews.
     */
    private ChildView newEntryBox()
    {
        if (isVertical())
            return new ColView();
        ChildView rowView = new RowView();
        rowView.setSpacing(5);
        return rowView;
    }

    /**
     * Called when legend is clicked.
     */
    private void handleLegendMouseReleaseEvent()
    {
        // Enable all Traces
        Content content = getContent();
        Trace[] traces = content.getTraces();
        for (Trace trace : traces)
            trace.setDisabled(false);
    }

    /**
     * Called when legend row is clicked.
     */
    private void handleEntryViewMouseReleaseEvent(ViewEvent anEvent, View anEntryView)
    {
        // If not double-click, just ignore
        if (anEvent.getClickCount() != 2) {
            anEvent.consume();
            return;
        }

        // Get row/trace index
        int index = anEntryView.indexInParent() - 1;

        // Get trace and disable
        Content content = getContent();
        Trace trace = content.getTrace(index);
        trace.setDisabled(!trace.isDisabled());
        anEvent.consume();
    }

    /**
     * Override return pref size for entry box (bypassing ScaleBox).
     */
    @Override
    protected double getPrefWidthImpl(double aH)
    {
        double entryBoxPrefW = _entryBox.getPrefWidth();
        return entryBoxPrefW + getInsetsAll().getWidth();
    }

    /**
     * Override return pref size for entry box (bypassing ScaleBox).
     */
    @Override
    protected double getPrefHeightImpl(double aW)
    {
        double entryBoxPrefH = _entryBox.getPrefHeight();
        return entryBoxPrefH + getInsetsAll().getHeight();
    }

    /**
     * Override to use ColView layout.
     */
    @Override
    protected void layoutImpl()
    {
        Insets ins = getInsetsAll();
        double areaX = ins.left;
        double areaY = ins.top;
        double areaW = getWidth() - ins.getWidth();
        double areaH = getHeight() - ins.getHeight();
        _scaleBox.setBounds(areaX, areaY, areaW, areaH);
    }

    @Override
    public void setWidth(double aValue)
    {
        if (aValue == getWidth()) return;
        super.setWidth(aValue);
        _entryBox.relayoutParent();
    }

    @Override
    public void setHeight(double aValue)
    {
        if (aValue == getHeight()) return;
        super.setHeight(aValue);
        _entryBox.relayoutParent();
    }
}