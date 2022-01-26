package snapcharts.apptools;
import snap.geom.Line;
import snap.geom.Shape;
import snap.gfx.Border;
import snap.gfx.Color;
import snap.gfx.Paint;
import snap.gfx.Stroke;
import snap.util.SnapUtils;
import snap.view.*;
import snapcharts.app.ChartPane;
import snapcharts.model.*;
import snapcharts.view.AxisView;
import snapcharts.view.ChartHelper;
import snapcharts.view.ChartView;
import java.util.Objects;

/**
 * This class manages UI to edit DataView. Though many of the properties are really for Axis.
 */
public class DataViewInsp extends ChartPartInsp {

    // The Current ExtraInsp
    private ChartPartInsp  _extraInsp;

    // The View that holds the ExtraInsp
    private ColView  _extraInspBox;

    // The 3D inspector
    private Thr3DTool  _3dInsp;

    /**
     * Constructor.
     */
    public DataViewInsp(ChartPane aChartPane)
    {
        super(aChartPane);
    }

    /**
     * Returns the name.
     */
    @Override
    public String getName()  { return "Content Settings"; }

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
        // Hide ShowBorderBox, ShowGridBox, GridDashBox
        setViewVisible("ShowBorderBox", false);
        setViewVisible("ShowGridBox", false);
        setViewVisible("GridDashBox", false);

        // Configure GridDashButton(s)
        for (int i = 0; i < Stroke.DASHES_ALL.length; i++) {
            double[] dashArray = Stroke.DASHES_ALL[i];
            Button lineDashButton = getView("GridDashButton_" + i, Button.class);
            if (lineDashButton != null)
                configureLineDashButton(lineDashButton, dashArray);
        }

        // Configure MoreBG ToggleGroup to allow empty, so clicks on selected button will collapse
        getToggleGroup("MoreBG").setAllowEmpty(true);

        // Get ExtraInspBox
        _extraInspBox = getView("ExtraInspBox", ColView.class);

        // Create 3DInsp
        _3dInsp = new Thr3DTool(_chartPane);
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

        // Reset ShowBorderBox.Visible
        boolean showBorderMore = getViewBoolValue("ShowBorderMoreButton");
        View showBorderBox = getView("ShowBorderBox");
        ViewAnimUtils.setVisible(showBorderBox, showBorderMore, false, true);

        // Reset ShowBorderBox UI
        if (showBorderMore) {

            // Reset BorderColorButton, BorderColorResetButton
            Color borderColor = border != null ? border.getColor() : null;
            setViewValue("BorderColorButton", borderColor);
            setViewVisible("BorderColorResetButton", !Objects.equals(borderColor, TraceList.DEFAULT_BORDER.getColor()));

            // Reset BorderWidthText, BorderWidthResetButton
            double borderWidth = border != null ? border.getWidth() : 0;
            setViewValue("BorderWidthText", borderWidth);
            setViewVisible("BorderWidthResetButton", borderWidth != TraceList.DEFAULT_BORDER.getWidth());
        }

        // Reset ShowFillCheckBox
        Paint fill = traceList.getFill();
        boolean showFill = fill != null;
        setViewValue("ShowFillCheckBox", showFill);

        // Reset ShowFillBox.Visible
        boolean showFillMore = getViewBoolValue("ShowFillMoreButton");
        View showFillBox = getView("ShowFillBox");
        ViewAnimUtils.setVisible(showFillBox, showFillMore, false, true);

        // Reset ShowFillBox UI
        if (showFillMore) {

            // Reset FillColorButton, FillColorResetButton
            Color fillColor = fill != null ? fill.getColor() : null;
            setViewValue("FillColorButton", fillColor);
            setViewVisible("FillColorResetButton", !Objects.equals(fill, ChartPart.DEFAULT_FILL));
        }

        // Reset ShowGridCheckBox
        boolean showGrid = axis != null && axis.isShowGrid();
        setViewValue("ShowGridCheckBox", showGrid);

        // Reset ShowGridBox.Visible
        boolean showGridMore = getViewBoolValue("ShowGridMoreButton");
        View showGridBox = getView("ShowGridBox");
        ViewAnimUtils.setVisible(showGridBox, showGridMore, false, true);

        // Reset ShowGridBox UI
        if (showGridMore) {

            // Reset GridColorButton, GridColorResetButton
            Color gridColor = axis.getGridColor();
            setViewValue("GridColorButton", gridColor);
            setViewVisible("GridColorResetButton", !Objects.equals(gridColor, Color.BLACK));

            // Reset GridWidthText, GridWidthResetButton
            double gridWidth = axis.getGridWidth();
            double DEFAULT_GRID_WIDTH = 1;
            setViewValue("GridWidthText", gridWidth);
            setViewVisible("GridWidthResetButton", gridWidth != DEFAULT_GRID_WIDTH);

            // Reset GridDashButton
            ToggleButton gridDashButton = getView("GridDashButton", ToggleButton.class);
            configureLineDashButton(gridDashButton, axis.getGridDash());

            // Reset GridDashBox
            View gridDashBox = getView("GridDashBox");
            ViewAnimUtils.setVisible(gridDashBox, gridDashButton.isSelected(), false, true);
        }

        // Handle 3DInsp
        if (chart.getType().is3D()) {
            setExtraInsp(_3dInsp);
            _3dInsp.resetUI();
        }
        else setExtraInsp(null);
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
        Border borderNonNull = border != null ? border : TraceList.DEFAULT_BORDER;

        // Handle ShowBorderCheckBox
        if (anEvent.equals("ShowBorderCheckBox")) {
            boolean showBorder = anEvent.getBoolValue();
            Border border2 = showBorder ? borderNonNull : null;
            traceList.setBorder(border2);
            setViewValue("ShowBorderMoreButton", showBorder);
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
            Border border2 = borderNonNull.copyForColor(TraceList.DEFAULT_BORDER.getColor());
            traceList.setBorder(border2);
        }

        // Handle ShowFillCheckBox
        if (anEvent.equals("ShowFillCheckBox")) {
            boolean showFill = anEvent.getBoolValue();
            Color fill2 = showFill ? Color.WHITE : null;
            traceList.setFill(fill2);
            setViewValue("ShowFillMoreButton", showFill);
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

        // Handle GridWidthText, GridWidthAdd1Button, GridWidthSub1Button, GridWidthResetButton
        if (anEvent.equals("GridWidthText"))
            for (Axis axis : axes)
                axis.setGridWidth(Math.max(anEvent.getIntValue(), 1));
        if (anEvent.equals("GridWidthAdd1Button"))
            for (Axis axis : axes)
                axis.setGridWidth(axis.getGridWidth() + 1);
        if (anEvent.equals("GridWidthSub1Button"))
            for (Axis axis : axes)
                axis.setGridWidth(Math.max(axis.getGridWidth() - 1, 1));
        if (anEvent.equals("GridWidthResetButton"))
            for (Axis axis : axes)
                axis.setGridWidth(Axis.DEFAULT_GRID_WIDTH);

        // Handle GridColorButton, GridColorResetButton
        if (anEvent.equals("GridColorButton")) {
            Color color = (Color) getViewValue("GridColorButton");
            for (Axis axis : axes)
                axis.setGridColor(color);
        }
        if (anEvent.equals("GridColorResetButton"))
            for (Axis axis : axes)
                axis.setGridColor(Axis.DEFAULT_GRID_COLOR);

        // Handle GridDashButton_X
        String eventName = anEvent.getName();
        if (eventName.startsWith("GridDashButton_")) {
            int id = SnapUtils.intValue(eventName);
            double[] dashArray = Stroke.DASHES_ALL[id];
            for (Axis axis : axes)
                axis.setGridDash(dashArray);
        }
    }

    /**
     * Returns the Extra inspector.
     */
    private ChartPartInsp getExtraInsp()  { return _extraInsp; }

    /**
     * Sets the Extra inspector.
     */
    private void setExtraInsp(ChartPartInsp anInsp)
    {
        // If already set, just return
        if (anInsp == getExtraInsp()) return;

        // If old, remove it
        if (_extraInsp != null)
            _extraInspBox.removeChild(_extraInsp.getUI());

        // Set new
        _extraInsp = anInsp;

        // If new, add UI
        if(_extraInsp != null)
            _extraInspBox.addChild(_extraInsp.getUI());

        // Update ExtraInspLabelBox.Visible, ExtraInspLabel.Text
        setViewVisible("ExtraInspLabelBox", _extraInsp != null);
        if (_extraInsp != null)
            setViewText("ExtraInspLabel", _extraInsp.getName());
    }

    /**
     * Configures a LineDash button.
     */
    private void configureLineDashButton(ButtonBase aButton, double[] dashArray)
    {
        Stroke stroke = Stroke.Stroke2.copyForCap(Stroke.Cap.Butt).copyForDashes(dashArray);
        Border border = Border.createLineBorder(Color.BLUE.darker(), 2).copyForStroke(stroke);
        Shape shape = new Line(5, 5, 80, 5);
        ShapeView shapeView = new ShapeView(shape);
        shapeView.setBounds(0, 0, 100, 12);
        shapeView.setBorder(border);
        aButton.setGraphic(shapeView);
    }
}