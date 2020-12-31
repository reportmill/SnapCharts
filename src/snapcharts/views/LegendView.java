package snapcharts.views;

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
        }
        else {
            setAlign(Pos.get(HPos.CENTER, pos.getVPos()));
            _scaleBox.setContent(_entryBox = new ColView());
        }

        // Handle visible
        boolean showLegend = legend.isShowLegend();
        setVisible(showLegend);
        if (!showLegend)
            return;

        // Remove children
        //removeChildren();

        for (int i=0; i<dsetList.getDataSetCount(); i++) { DataSet dset = dsetList.getDataSet(i);
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
        int index = ArrayUtils.indexOf(getChildren(), anEntryView);

        // Get dataset and disable
        ChartView chart = getChartView();
        DataSetList dsetList = chart.getDataSetList();
        DataSet dset = dsetList.getDataSet(index);
        dset.setDisabled(!dset.isDisabled());

        // Redraw chart and reload legend
        chart.resetLater();
    }

    /**
     * Override to use ColView layout.
     */
    @Override
    protected double getPrefWidthImpl(double aH)
    {
        // If Position is Top/Bottom, layout as RowView
        /*if (isHorizontal())
            return RowView.getPrefWidth(this, aH);

        // Otherwise Layout as ColView
        return ColView.getPrefWidth(this, aH);*/
        return _scaleBox.getPrefWidth();
    }

    /**
     * Override to use ColView layout.
     */
    @Override
    protected double getPrefHeightImpl(double aW)
    {
        // If Position is Top/Bottom, layout as RowView
        /*if (isHorizontal()) {
            double prefH = RowView.getPrefHeight(this, -1);
            double prefW = RowView.getPrefWidth(this, -1);
            if (aW > 0 && prefW > aW)
                prefH *= 2;
            return prefH;
        }

        // Otherwise Layout as ColView
        return ColView.getPrefHeight(this, aW);*/
        return _scaleBox.getPrefHeight();
    }

    /**
     * Override to use ColView layout.
     */
    @Override
    protected void layoutImpl()
    {
        double areaW = getWidth();
        double areaH = getHeight();
        _scaleBox.setSize(areaW, areaH);

        // If Position is Top/Bottom, layout as RowView
        //if (isHorizontal())
        //    RowView.layout(this, false);

        // Otherwise Layout as ColView
        //else ColView.layout(this, true);
    }
}