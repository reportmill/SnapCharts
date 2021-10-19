/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.view;
import snap.geom.*;
import snap.util.ArrayUtils;
import snap.view.*;
import snapcharts.model.*;

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
        addEventHandler(e -> legendWasClicked(e), MouseRelease);
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
     * Returns whether legend is inside data area.
     */
    public boolean isInside()  { return getChartPart().isInside(); }

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
        _titleView.setVisible(titleText != null && titleText.length() > 0);
        _titleView.setLeanX(legend.getAlignX());

        // Handle Inside
        if (legend.isInside()) {
            ChartView chartView = getChartView();
            ViewUtils.moveToFront(chartView, this);
        }

        // Reset EntryBox
        resetEntryBox();
        _entryBox.setSpacing(_entryBox.getSpacing() + legend.getSpacing());

        // Iterate over DataSets and add entries
        DataSetList dataSetList = getDataSetList();
        DataSet[] dataSets = dataSetList.getDataSets();
        for (DataSet dataSet : dataSets) {

            // If not DataSet.ShowLegendEntry, just continue
            if (!dataSet.isShowLegendEntry())
                continue;

            // Create, add LegendEntryView for DataSet
            View entryView = new LegendEntryView(legend, dataSet);
            _entryBox.addChild(entryView);

            // Register row to enable/disable
            entryView.addEventHandler(e -> entryWasClicked(e, entryView), MouseRelease);
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
            _scaleBox.setAlign(Pos.CENTER);
        }

        // Add TitleView
        _entryBox.addChild(_titleView);
    }

    /**
     * Creates the EntryBox that holds LegendEntryViews.
     */
    private ChildView newEntryBox()
    {
        // Handle Vertical: create/return ColView
        if (isVertical()) {
            ChildView colView = new LegendViewBoxV();
            return colView;
        }

        // Handle Horizontal: Create/return RowView
        ChildView rowView = new LegendViewBoxH();
        rowView.setSpacing(5);
        return rowView;
    }

    /**
     * Called when legend is clicked.
     */
    private void legendWasClicked(ViewEvent anEvent)
    {
        // Enable all DataSets
        DataSetList dsetList = getDataSetList();
        DataSet[] dataSets = dsetList.getDataSets();
        for (DataSet dataSet : dataSets)
            dataSet.setDisabled(false);
    }

    /**
     * Called when legend row is clicked.
     */
    private void entryWasClicked(ViewEvent anEvent, View anEntryView)
    {
        // If not double-click, just ignore
        if (anEvent.getClickCount() != 2) {
            anEvent.consume();
            return;
        }

        // Get row/dataset index
        ParentView parentView = anEntryView.getParent();
        int index = ArrayUtils.indexOf(parentView.getChildren(), anEntryView) - 1;

        // Get dataset and disable
        DataSetList dsetList = getDataSetList();
        DataSet dset = dsetList.getDataSet(index);
        dset.setDisabled(!dset.isDisabled());
        anEvent.consume();
    }

    /**
     * Override main version to bypass LegendView, ScaleBox and EntryBox PrefSize caching.
     */
    @Override
    public double getPrefWidth(double aH)
    {
        // Get from EntryBox to bypass ScaleBox PrefSize caching. EntryBox does special PrefSize caching.
        double prefW = _entryBox.getPrefWidth();

        // Return EntryBox.PrefWidth + Insets.Width
        Insets ins = getInsetsAll();
        return prefW + ins.getWidth();
    }

    /**
     * Override main version to bypass LegendView, ScaleBox and EntryBox PrefSize caching.
     */
    @Override
    public double getPrefHeight(double aW)
    {
        // Get from EntryBox to bypass ScaleBox PrefSize caching. EntryBox does special PrefSize caching.
        double prefH = _entryBox.getPrefHeight();

        // Return EntryBox.PrefHeight + Insets.Height
        Insets ins = getInsetsAll();
        return prefH + ins.getHeight();
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