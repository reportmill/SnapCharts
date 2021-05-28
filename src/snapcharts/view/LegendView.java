package snapcharts.view;
import snap.geom.*;
import snap.gfx.*;
import snap.util.ArrayUtils;
import snap.view.*;
import snapcharts.model.*;

/**
 * A view to display chart legend.
 */
public class LegendView<T extends Legend> extends ChartPartView<T> {

    // A ScaleBox to make sure Legend always fits
    private ScaleBox  _scaleBox;

    // The ChildView (ColView or RowView) to hold Legend Entries
    private ChildView  _entryBox;

    // Constants
    private static Insets DEFAULT_PADDING = new Insets(5, 5, 5, 5);
    private static Font DEFAULT_ENTRY_FONT = Font.Arial12.deriveFont(13).getBold();
    private static Color DISABLED_COLOR = Color.LIGHTGRAY;

    /**
     * Constructor.
     */
    public LegendView()
    {
        super();

        setPadding(DEFAULT_PADDING);
        setAlign(Pos.CENTER_LEFT);

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
    public T getChartPart()  { return (T) getLegend(); }

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

        // Handle Orientation
        Pos pos = getPosition();
        boolean isHor = pos==Pos.TOP_CENTER || pos==Pos.BOTTOM_CENTER;
        setVertical(!isHor);
        if (isHor) {
            setAlign(Pos.TOP_CENTER);
            _scaleBox.setContent(_entryBox = new RowView());
            _scaleBox.setAlign(Pos.CENTER);
        }
        else {
            setAlign(Pos.get(HPos.CENTER, pos.getVPos()));
            _scaleBox.setContent(_entryBox = new ColView());
            _scaleBox.setAlign(Pos.get(HPos.CENTER, pos.getVPos()));
        }

        // Handle visible
        boolean showLegend = legend.isShowLegend();
        setVisible(showLegend);
        if (!showLegend)
            return;

        // Handle Inside
        if (legend.isInside()) {
            setPadding(5, 5, 5, 5);
            ChartView chartView = getChartView();
            ViewUtils.moveToFront(chartView, this);
        }

        // Handle outside
        else {
            setPadding(null);
        }

        // Iterate over DataSets and add entries
        DataSetList dsetList = getDataSetList();
        DataSet[] dataSets = dsetList.getDataSets();
        for (int i=0; i<dataSets.length; i++) {
            DataSet dset = dataSets[i];
            View entryView = createLegendEntry(dset, i);
            _entryBox.addChild(entryView);

            // Register row to enable/disable
            entryView.addEventHandler(e -> entryWasClicked(e, entryView), MouseRelease);
        }
    }

    /**
     * Creates a legend entry.
     */
    private View createLegendEntry(DataSet aDataSet, int anIndex)
    {
        // Create Label for entry text
        String text = aDataSet.getName();
        Label label = new Label(text);
        label.setFont(DEFAULT_ENTRY_FONT);
        if (aDataSet.isDisabled()) {
            label.setTextFill(Color.LIGHTGRAY);
        }

        // Create/add EntryGraphicView for entry graphic
        View entryGraphicView = new EntryGraphicView(aDataSet);
        label.setGraphic(entryGraphicView);

        // Return entry label
        return label;
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

    /**
     * A placeholder view to paint entry graphic.
     */
    private class EntryGraphicView extends View {

        // The DataSet
        private DataSet  _dataSet;

        // The DataStyle
        private DataStyle  _dataStyle;

        /**
         * Constructor.
         */
        EntryGraphicView(DataSet aDataSet)
        {
            setPrefSize(24, 20);
            _dataSet = aDataSet;
            _dataStyle = aDataSet.getDataStyle();
        }

        @Override
        protected void paintFront(Painter aPntr)
        {
            // Whether disabled
            boolean disabled = _dataSet.isDisabled();

            // Handle ShowArea
            if (_dataStyle.isShowArea()) {
                Color fillColor = _dataStyle.getFillColor();
                aPntr.fillRectWithPaint(2, 9, 20, 7, fillColor);
            }

            // Handle ShowLine
            if (_dataStyle.isShowLine()) {
                Color lineColor = _dataStyle.getLineColor(); if (disabled) lineColor = DISABLED_COLOR;
                aPntr.fillRectWithPaint(2, 9, 20, 2, lineColor);
            }

            // Handle ShowSymbol
            if (_dataStyle.isShowSymbols()) {

                // Get/paint symbol shape
                Shape symbShape = _dataStyle.getSymbol().copyForSize(8).getShape();
                symbShape = symbShape.copyFor(new Transform(8, 6));
                Color symbColor = _dataStyle.getSymbolColor();
                if (disabled) symbColor = DISABLED_COLOR;
                aPntr.fillWithPaint(symbShape, symbColor);

                // Paint border (if visible)
                int borderWidth = _dataStyle.getSymbolBorderWidth();
                if (borderWidth > 0) {
                    Color borderColor = _dataStyle.getSymbolBorderColor();
                    if (disabled) borderColor = DISABLED_COLOR;
                    aPntr.setStroke(Stroke.getStroke(1));
                    aPntr.drawWithPaint(symbShape, borderColor);
                }
            }
        }
    }
}