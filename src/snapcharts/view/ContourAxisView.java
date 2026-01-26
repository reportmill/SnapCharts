package snapcharts.view;
import snap.geom.*;
import snap.gfx.*;
import snap.util.FormatUtils;
import snap.view.*;
import snapcharts.charts.*;
import snapcharts.data.MinMax;
import snapcharts.viewx.Contour3DChartHelper;
import snapcharts.viewx.ContourChartHelper;
import snapcharts.viewx.ContourHelper;
import snapcharts.viewx.PolarContourChartHelper;

/**
 * A view to display chart contour axis.
 */
public class ContourAxisView extends ChartPartView<ContourAxis> {

    // The Contour helper
    private ContourHelper  _contourHelper;

    // Whether to render as smooth gradient
    private boolean  _renderSmooth;

    // The view to hold color scale
    private ColorBox  _colorBox;

    // The ColView to hold Contour Entries
    private ColView  _entryBox;

    /**
     * Constructor.
     */
    public ContourAxisView()
    {
        super();

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
    public ContourAxis getChartPart()  { return getChart().getContourAxis(); }

    /**
     * Reloads legend contents.
     */
    public void resetView()
    {
        // Handle visible
        Chart chart = getChart();
        boolean showView = chart.getTraceType().isContourType();
        setVisible(showView);
        if (!showView)
            return;

        // Get ChartHelper, ContourHelper
        ChartHelper chartHelper = getChartHelper();
        if (chartHelper instanceof ContourChartHelper)
            _contourHelper = ((ContourChartHelper) chartHelper).getContourHelper();
        else if (chartHelper instanceof PolarContourChartHelper)
            _contourHelper = ((PolarContourChartHelper) chartHelper).getContourHelper();
        else if (chartHelper instanceof Contour3DChartHelper)
            _contourHelper = ((Contour3DChartHelper) chartHelper).getContourHelper();

        // Set RenderSmooth property
        _renderSmooth = chartHelper instanceof Contour3DChartHelper;

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
        for (int i = 0; i < contourCount; i++) {
            MinMax contourRange = _contourHelper.getContourRange(contourCount - i - 1);
            double val = contourRange.getMin();
            View entryView = createContourEntry(val);
            _entryBox.addChild(entryView);
        }

        // Add final label using final contour max val
        MinMax contourRangeLast = _contourHelper.getContourRange(contourCount-1);
        double lastVal = contourRangeLast.getMax();
        View entryView = createContourEntry(lastVal);
        _entryBox.addChild(entryView, 0);

        // Make last entry not grow so it will sit flush on bottom
        View lastEntry = _entryBox.getLastChild();
        lastEntry.setGrowHeight(false);
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
     * Override to layout color box.
     */
    @Override
    protected void layoutImpl()
    {
        super.layoutImpl();
        if (_entryBox.getChildCount() == 0)
            return;

        // Calculate ColorBox Y/H and set
        Insets pad = _entryBox.getPadding();
        Label label = (Label) _entryBox.getChild(0);
        double strH = label.getPrefHeight();
        double halfH = Math.ceil(strH/2);
        double colorBoxY = _entryBox.getY() + pad.top + halfH;
        double colorBoxH = _entryBox.getHeight() - pad.bottom - strH;
        _colorBox.setY(colorBoxY);
        _colorBox.setHeight(colorBoxH);
    }

    /**
     * Override to return row layout.
     */
    @Override
    protected ViewLayout getViewLayoutImpl()  { return new RowViewLayout(this, true); }

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
            // Get info
            double areaX = 0;
            double areaW = getWidth();
            double areaH = getHeight();
            int contourCount = getContourCount();
            double sliceH = areaH / contourCount;

            // Handle RenderSmooth
            if (_renderSmooth) {
                GradientPaint colorMapPaint = _contourHelper.getColorMapPaint();
                aPntr.setPaint(colorMapPaint);
                aPntr.fillRect(areaX, 0, areaW, areaH);
                aPntr.setPaint(Color.BLACK);
                aPntr.setStroke(Stroke.Stroke1);
                aPntr.drawRect(areaX, 0, areaW, areaH);
                return;
            }

            // Handle render blocks (last contour on top)
            for (int i = 0; i < contourCount; i++) {
                int sliceY = (int) Math.round(sliceH * i);
                int sliceY2 = (int) Math.round(sliceH * (i+1));
                Color color = _contourHelper.getContourColor(contourCount - i - 1);
                aPntr.setColor(color);
                aPntr.fillRect(areaX, sliceY, areaW, sliceY2 - sliceY);
            }
        }
    }
}