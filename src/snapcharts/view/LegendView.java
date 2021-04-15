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
    }

    /**
     * Returns the ChartPart.
     */
    public T getChartPart()  { return (T) getChart().getLegend(); }

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
        Chart chart = getChart();
        Legend legend = chart.getLegend();
        DataSetList dsetList = chart.getDataSetList();

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
            if (legend.getFill() == null)
                setFill(Color.WHITE);
            if (legend.getBorder() == null)
                setBorder(Color.BLACK, 1);
            setMargin(8, 8, 8, 8);
            setPadding(5, 5, 5, 5);
            ChartView chartView = getChartView();
            int childCount = chartView.getChildCount();
            if (indexInHost() != childCount - 1) {
                ViewUtils.removeChild(chartView, this);
                ViewUtils.addChild(chartView, this);
            }
        }
        else {
            setBorder(legend.getBorder());
            setMargin(null);
            setPadding(null);
        }
        // Remove children
        //removeChildren();

        for (int i=0; i<dsetList.getDataSetCount(); i++) {
            DataSet dset = dsetList.getDataSet(i);
            View entryView = createLegendEntry(chart, dset, i);
            _entryBox.addChild(entryView);

            // Register row to enable/disable
            entryView.addEventHandler(e -> entryWasClicked(entryView), MouseRelease);
            //shpView.setPickable(false); sview.setPickable(false);
        }
    }

    /**
     * Creates a legend entry.
     */
    private View createLegendEntry(Chart aChart, DataSet aDataSet, int anIndex)
    {
        // Get marker Shape (if LineChart, add crossbar)
        Shape shp = aChart.getSymbolShape(anIndex);
        shp = shp.copyFor(new Transform(6, 6));
        if (aChart.getType() == ChartType.LINE) {
            Shape shp1 = new Rect(2,9,16,2);
            shp = Shape.add(shp, shp1);
        }

        // Create marker ShapeView
        ShapeView shpView = new ShapeView(shp);
        shpView.setPrefSize(20,20);

        // Set color
        shpView.setFill(aChart.getColor(anIndex));

        String text = aDataSet.getName();
        Label label = new Label(text);
        label.setFont(DEFAULT_ENTRY_FONT);
        if (aDataSet.isDisabled()) {
            shpView.setFill(Color.LIGHTGRAY);
            label.setTextFill(Color.LIGHTGRAY);
        }
        label.setGraphic(shpView);
        return label;
    }

    /**
     * Called when legend row is clicked.
     */
    private void entryWasClicked(View anEntryView)
    {
        // Get row/dataset index
        ParentView parentView = anEntryView.getParent();
        int index = ArrayUtils.indexOf(parentView.getChildren(), anEntryView);

        // Get dataset and disable
        Chart chart = getChart();
        DataSetList dsetList = chart.getDataSetList();
        DataSet dset = dsetList.getDataSet(index);
        dset.setDisabled(!dset.isDisabled());

        // Reset ChartView
        ChartView chartView = getChartView();
        chartView.resetLater();
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