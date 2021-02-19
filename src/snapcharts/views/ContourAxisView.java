package snapcharts.views;
import snap.geom.*;
import snap.gfx.*;
import snap.util.ArrayUtils;
import snap.util.StringUtils;
import snap.view.*;
import snapcharts.model.*;

/**
 * A view to display chart contour axis.
 */
public class ContourAxisView<T extends AxisZ> extends ChartPartView<T> {

    // The Contour helper
    private ChartHelperContour  _contourHelper;

    // The view to hold color scale
    private ColorBox  _colorBox;

    // The ColView to hold Contour Entries
    private ColView  _entryBox;

    // Constants
    private static Insets DEFAULT_PADDING = new Insets(5, 5, 5, 10);

    /**
     * Constructor.
     */
    public ContourAxisView()
    {
        super();

        setPadding(DEFAULT_PADDING);
        setAlign(Pos.CENTER_LEFT);

        // Create/add ColorBox
        _colorBox = new ColorBox();
        addChild(_colorBox);

        _entryBox = new ColView();
        _entryBox.setPadding(5,5, 5, 5);
        addChild(_entryBox);
    }

    /**
     * Returns the number of contours.
     */
    public int getContourCount()
    {
        return _contourHelper.getContourCount();
    }

    /**
     * Returns the ChartPart.
     */
    public T getChartPart()  { return (T) getChart().getAxisZ(); }

    /**
     * Reloads legend contents.
     */
    public void resetView()
    {
        // Handle visible
        Chart chart = getChart();
        boolean showView = chart.getType() == ChartType.CONTOUR;
        setVisible(showView);
        if (!showView)
            return;

        ChartView chartView = getChartView();
        ChartHelper chartHelper = chartView.getChartHelper();
        _contourHelper = chartHelper instanceof ChartHelperContour ? (ChartHelperContour) chartHelper : null;

        // Do normal version
        super.resetView();

        // Reset axis labels
        resetAxisLabels();
    }

    /**
     * Rebuilds the axis labels.
     */
    private void resetAxisLabels()
    {
        // Get info
        Chart chart = getChart();
        DataSetList dsetList = chart.getDataSetList();

        // Remove children
        _entryBox.removeChildren();

        // Iterate over ContourRanges and add label for each min val
        int contourCount = getContourCount();
        for (int i=0; i<contourCount; i++) {
            double val = _contourHelper.getContourRange(i).getMin();
            View entryView = createContourEntry(val);
            _entryBox.addChild(entryView);
        }

        // Add final label using final contour max val
        double lastVal = _contourHelper.getContourRange(contourCount-1).getMax();
        View entryView = createContourEntry(lastVal);
        entryView.setGrowHeight(false);
        _entryBox.addChild(entryView);
    }

    /**
     * Creates a Contour entry.
     */
    private View createContourEntry(double aVal)
    {
        String valStr = StringUtils.formatNum("#.##", aVal);
        Label label = new Label(valStr);
        label.setFont(getFont());
        label.setAlign(Pos.TOP_LEFT);
        label.setGrowHeight(true);
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
        return RowView.getPrefWidth(this, aH);
    }

    /**
     * Override to use ColView layout.
     */
    @Override
    protected double getPrefHeightImpl(double aW)
    {
        return RowView.getPrefHeight(this, aW);
    }

    /**
     * Override to use ColView layout.
     */
    @Override
    protected void layoutImpl()
    {
        RowView.layout(this, true);

        if (_entryBox.getChildCount()==0)
            return;

        Insets pad = _entryBox.getPadding();
        Label label = (Label) _entryBox.getChild(0);
        StringView strView = label.getStringView();
        double strH = strView.getTextHeight();
        double halfH = Math.ceil(strH/2);

        _colorBox.setY(_entryBox.getY() + pad.top + halfH);
        _colorBox.setHeight(_entryBox.getHeight() - pad.bottom - strH);
    }

    /**
     * A View subclass to show colors.
     */
    private class ColorBox extends View {

        /**
         * Constructor.
         */
        public ColorBox()
        {
            setPrefWidth(25);
        }

        /**
         * Override to paint box.
         */
        @Override
        protected void paintFront(Painter aPntr)
        {
            double areaX = 0;
            double areaW = getWidth();
            double areaH = getHeight();
            int count = getContourCount();
            double sliceH = areaH / count;

            for (int i=0; i<count; i++) {
                int sliceY = (int) Math.round(sliceH * i);
                int sliceY2 = (int) Math.round(sliceH * (i+1));
                Color color = _contourHelper.getContourColor(i);
                aPntr.setColor(color);
                aPntr.fillRect(areaX, sliceY, areaW, sliceY2 - sliceY);
            }
        }
    }
}