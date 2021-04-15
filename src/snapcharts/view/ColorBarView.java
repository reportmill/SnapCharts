package snapcharts.view;
import snap.geom.*;
import snap.gfx.*;
import snap.util.FormatUtils;
import snap.view.*;
import snapcharts.model.*;
import snapcharts.viewx.ContourChartHelper;
import snapcharts.viewx.ContourHelper;
import snapcharts.viewx.PolarContourChartHelper;

/**
 * A view to display chart contour axis.
 */
public class ColorBarView extends ChartPartView<ColorBar> {

    // The Contour helper
    private ContourHelper  _contourHelper;

    // The view to hold color scale
    private ColorBox  _colorBox;

    // The ColView to hold Contour Entries
    private ColView  _entryBox;

    // Constants
    private static Insets DEFAULT_PADDING = new Insets(5, 5, 5, 10);

    /**
     * Constructor.
     */
    public ColorBarView()
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
    public ColorBar getChartPart()  { return getChart().getColorBar(); }

    /**
     * Reloads legend contents.
     */
    public void resetView()
    {
        // Handle visible
        Chart chart = getChart();
        boolean showView = chart.getType().isContourType();
        setVisible(showView);
        if (!showView)
            return;

        // Get ChartHelper, ContourHelper
        ChartHelper chartHelper = getChartHelper();
        if (chartHelper instanceof ContourChartHelper)
            _contourHelper = ((ContourChartHelper) chartHelper).getContourHelper();
        else if (chartHelper instanceof PolarContourChartHelper)
            _contourHelper = ((PolarContourChartHelper) chartHelper).getContourHelper();

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
        String valStr = FormatUtils.formatNum("#.##", aVal);
        Label label = new Label(valStr);
        label.setFont(getFont());
        label.setAlign(Pos.TOP_LEFT);
        label.setGrowHeight(true);
        return label;
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