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

        // Iterate over DataSets and add entries
        DataSetList dsetList = getDataSetList();
        DataSet[] dataSets = dsetList.getDataSets();
        for (int i=0; i<dataSets.length; i++) {
            DataSet dset = dataSets[i];
            View entryView = createLegendEntry(dset, i);
            _entryBox.addChild(entryView);

            // Register row to enable/disable
            entryView.addEventHandler(e -> entryWasClicked(e, entryView), MouseRelease);
            //shpView.setPickable(false); sview.setPickable(false);
        }
    }

    /**
     * Creates a legend entry.
     */
    private View createLegendEntry(DataSet aDataSet, int anIndex)
    {
        // Get Symbol.Shape
        Chart chart = getChart();
        DataStyle dataStyle = aDataSet.getDataStyle();
        Shape shp = dataStyle.getSymbol().copyForSize(8).getShape();
        shp = shp.copyFor(new Transform(6, 6));

        // If SCATTER, add crossbar
        if (chart.getType() == ChartType.SCATTER) {
            Shape shp1 = new Rect(2,9,16,2);
            shp = Shape.add(shp, shp1);
        }

        // Create marker ShapeView
        ShapeView shpView = new ShapeView(shp);
        shpView.setPrefSize(20,20);

        // Set color
        shpView.setFill(dataStyle.getLineColor());

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