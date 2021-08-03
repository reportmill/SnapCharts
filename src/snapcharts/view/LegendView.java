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

    // Constants
    private static Insets DEFAULT_MARGIN = new Insets(5, 5, 5, 5);

    /**
     * Constructor.
     */
    public LegendView()
    {
        super();
        setMargin(DEFAULT_MARGIN);

        // Create/configure/add ScaleBox to hold EntryBox
        _scaleBox = new ScaleBox();
        _scaleBox.setKeepAspect(true);
        addChild(_scaleBox);

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

        // Reset EntryBox
        resetEntryBox();

        // Handle visible
        boolean showLegend = legend.isShowLegend();
        setVisible(showLegend);
        if (!showLegend)
            return;

        // Handle Inside
        if (legend.isInside()) {
            ChartView chartView = getChartView();
            ViewUtils.moveToFront(chartView, this);
        }

        // Iterate over DataSets and add entries
        DataSetList dataSetList = getDataSetList();
        DataSet[] dataSets = dataSetList.getDataSets();
        for (int i=0; i<dataSets.length; i++) {
            DataSet dataSet = dataSets[i];
            if (!dataSet.isShowLegendEntry())
                continue;
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
        Pos pos = getPosition();
        boolean isVer = !(pos == Pos.TOP_CENTER || pos == Pos.BOTTOM_CENTER);
        setVertical(isVer);

        // Handle Vertical layout
        if (isVer) {
            setAlign(Pos.get(HPos.CENTER, pos.getVPos()));
            _scaleBox.setContent(_entryBox = newEntryBox());
            _scaleBox.setAlign(Pos.get(HPos.CENTER, pos.getVPos()));
        }

        // Handle Horizontal layout
        else {
            setAlign(Pos.TOP_CENTER);
            _scaleBox.setContent(_entryBox = newEntryBox());
            _scaleBox.setAlign(Pos.CENTER);
        }
    }

    /**
     * Creates the EntryBox that holds LegendEntryViews.
     */
    private ChildView newEntryBox()
    {
        // Handle Vertical: create/return ColView
        if (isVertical()) {
            ColView colView = new ColView();
            return colView;
        }

        // Handle Horizontal: Create/return RowView
        RowView rowView = new RowView();
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
        // Get row/dataset index
        ParentView parentView = anEntryView.getParent();
        int index = ArrayUtils.indexOf(parentView.getChildren(), anEntryView);

        // Get dataset and disable
        DataSetList dsetList = getDataSetList();
        DataSet dset = dsetList.getDataSet(index);
        dset.setDisabled(!dset.isDisabled());
        anEvent.consume();
    }

    /**
     * Override to use ColView layout.
     */
    @Override
    protected double getPrefWidthImpl(double aH)
    {
        Insets ins = getInsetsAll();
        return _scaleBox.getPrefWidth() + ins.getWidth();
    }

    /**
     * Override to use ColView layout.
     */
    @Override
    protected double getPrefHeightImpl(double aW)
    {
        Insets ins = getInsetsAll();
        return _scaleBox.getPrefHeight() + ins.getHeight();
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
}