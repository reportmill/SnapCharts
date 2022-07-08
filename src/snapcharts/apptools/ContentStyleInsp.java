package snapcharts.apptools;
import snap.gfx.Border;
import snap.gfx.Color;
import snap.gfx.Paint;
import snap.view.*;
import snapcharts.app.ChartPane;
import snapcharts.model.Axis;
import snapcharts.model.Chart;
import snapcharts.model.ChartPart;
import snapcharts.model.TraceList;
import snapcharts.view.AxisView;
import snapcharts.view.ChartHelper;
import snapcharts.view.ChartView;
import java.util.Objects;

/**
 * This class manages UI to edit DataView. Though many of the properties are really for Axis.
 */
public class ContentStyleInsp extends ChartPartInsp {

    /**
     * Constructor.
     */
    public ContentStyleInsp(ChartPane aChartPane)
    {
        super(aChartPane);
    }

    /**
     * Returns the name.
     */
    @Override
    public String getName()  { return "Content Style"; }

    /**
     * Returns the ChartPart.
     */
    @Override
    public ChartPart getChartPart()
    {
        Chart chart = getChart();
        TraceList traceList = chart.getTraceList();
        return traceList;
    }

    /**
     * Initialize UI.
     */
    @Override
    protected void initUI()
    {
    }

    /**
     * Reset UI.
     */
    protected void resetUI()
    {
        // Get Axis, AxisView (just return if null)
        Chart chart = getChart();
        TraceList traceList = chart.getTraceList();
        ChartPane chartPane = getChartPane();
        ChartView chartView = chartPane.getChartView();
        ChartHelper chartHelper = chartView.getChartHelper();
        AxisView[] axisViews = chartHelper.getAxisViews();
        Axis[] axes = new Axis[axisViews.length];
        for (int i=0; i<axisViews.length; i++) axes[i] = axisViews[i].getAxis();
        Axis axis = axes.length > 0 ? axes[0] : null;

        // Reset ShowBorderCheckBox
        Border border = traceList.getBorder();
        boolean showBorder = border != null;
        setViewValue("ShowBorderCheckBox", showBorder);

        // Reset BorderColorButton, BorderColorResetButton
        Color borderColor = border != null ? border.getColor() : null;
        setViewValue("BorderColorButton", borderColor);
        setViewVisible("BorderColorResetButton", !traceList.isPropDefault(ChartPart.LineColor_Prop));

        // Reset BorderWidthText, BorderWidthResetButton
        double borderWidth = border != null ? border.getWidth() : 0;
        setViewValue("BorderWidthText", borderWidth);
        setViewVisible("BorderWidthResetButton", !traceList.isPropDefault(ChartPart.LineWidth_Prop));

        // Reset ShowFillCheckBox
        Paint fill = traceList.getFill();
        boolean showFill = fill != null;
        setViewValue("ShowFillCheckBox", showFill);

        // Reset FillColorButton, FillColorResetButton
        Color fillColor = fill != null ? fill.getColor() : null;
        setViewValue("FillColorButton", fillColor);
        setViewVisible("FillColorResetButton", !traceList.isPropDefault(ChartPart.Fill_Prop));
    }

    /**
     * Respond to UI.
     */
    protected void respondUI(ViewEvent anEvent)
    {
        // Get Axis, AxisView (just return if null)
        Chart chart = getChart();
        TraceList traceList = chart.getTraceList();
        ChartPane chartPane = getChartPane();
        ChartView chartView = chartPane.getChartView();
        ChartHelper chartHelper = chartView.getChartHelper();
        AxisView[] axisViews = chartHelper.getAxisViews();
        Axis[] axes = new Axis[axisViews.length];
        for (int i=0; i<axisViews.length; i++) axes[i] = axisViews[i].getAxis();

        // Get Border (and non-null/default version if needed)
        Border border = traceList.getBorder();
        Border borderNonNull = border != null ? border :
                Border.createLineBorder(TraceList.DEFAULT_CONTENT_LINE_COLOR, TraceList.DEFAULT_CONTENT_LINE_WIDTH);

        // Handle ShowBorderCheckBox
        if (anEvent.equals("ShowBorderCheckBox")) {
            boolean showBorder = anEvent.getBoolValue();
            Border border2 = showBorder ? borderNonNull : null;
            traceList.setBorder(border2);
        }

        // Handle BorderWidthText, BorderWidthAdd1Button, BorderWidthSub1Button, BorderWidthResetButton
        if (anEvent.equals("BorderWidthText")) {
            Border border2 = borderNonNull.copyForStrokeWidth(Math.max(anEvent.getIntValue(), 1));
            traceList.setBorder(border2);
        }
        if (anEvent.equals("BorderWidthAdd1Button")) {
            Border border2 = borderNonNull.copyForStrokeWidth(borderNonNull.getWidth() + 1);
            traceList.setBorder(border2);
        }
        if (anEvent.equals("BorderWidthSub1Button")) {
            Border border2 = borderNonNull.copyForStrokeWidth(Math.max(borderNonNull.getWidth() - 1, 1));
            traceList.setBorder(border2);
        }
        if (anEvent.equals("BorderWidthResetButton")) {
            Border border2 = borderNonNull.copyForStrokeWidth(1);
            traceList.setBorder(border2);
        }

        // Handle BorderColorButton, BorderColorResetButton
        if (anEvent.equals("BorderColorButton")) {
            Color color = (Color) getViewValue("BorderColorButton");
            Border border2 = borderNonNull.copyForColor(color);
            traceList.setBorder(border2);
        }
        if (anEvent.equals("BorderColorResetButton")) {
            Border border2 = borderNonNull.copyForColor(TraceList.DEFAULT_LINE_COLOR);
            traceList.setBorder(border2);
        }

        // Handle ShowFillCheckBox
        if (anEvent.equals("ShowFillCheckBox")) {
            boolean showFill = anEvent.getBoolValue();
            Color fill2 = showFill ? Color.WHITE : null;
            traceList.setFill(fill2);
        }

        // Handle FillColorButton, FillColorResetButton
        if (anEvent.equals("FillColorButton")) {
            Color fillColor = (Color) getViewValue("FillColorButton");
            traceList.setFill(fillColor);
        }
        if (anEvent.equals("FillColorResetButton")) {
            Paint fill = ChartPart.DEFAULT_FILL;
            traceList.setFill(fill);
        }

        // Handle ShowGridCheckBox
        if (anEvent.equals("ShowGridCheckBox")) {
            boolean showGrid = anEvent.getBoolValue();
            for (Axis axis : axes)
                axis.setShowGrid(showGrid);
        }
    }
}