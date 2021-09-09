package snapcharts.apptools;
import snap.text.NumberFormat;
import snap.view.Label;
import snap.view.TextField;
import snap.view.ViewEvent;
import snapcharts.model.*;
import snapcharts.app.ChartPane;
import snapcharts.util.MinMax;
import snapcharts.view.AxisView;
import snapcharts.view.ChartHelper;
import snapcharts.view.ChartPartView;

/**
 * A class to manage UI to edit a ChartView.
 */
public class AxisInsp extends ChartPartInsp {

    // The Axis type
    private AxisType  _axisType;

    /**
     * Constructor.
     */
    public AxisInsp(ChartPane aChartPane, AxisType anAxisType)
    {
        super(aChartPane);
        _axisType = anAxisType;
    }

    /**
     * Returns the name.
     */
    @Override
    public String getName()
    {
        switch (_axisType) {
            case X: return "X Axis Settings";
            case Y: return "Y Axis Settings";
            default: return null;
        }
    }

    /**
     * Returns the axis.
     */
    public Axis getAxis()
    {
        ChartPart chartPart = getChartPane().getSelChartPart();
        Axis axis = chartPart instanceof Axis ? (Axis) chartPart : getChart().getAxisForType(_axisType);
        return axis;
    }

    /**
     * Returns the AxisView.
     */
    public AxisView getAxisView()
    {
        ChartPartView chartPartView = getChartPane().getSel().getSelView();
        AxisView axisView = chartPartView instanceof AxisView ? (AxisView) chartPartView : null;
        return axisView;
    }

    /**
     * Returns the ChartPart.
     */
    @Override
    public ChartPart getChartPart()  { return getAxis(); }

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
        Axis axis = getAxis();
        AxisView axisView = getAxisView();
        if (axisView == null)
            return;

        // Reset Collapser.Text
        String title = axis.getType().toString() + " Axis Settings";
        Label label = getCollapser().getLabel();
        label.setText(title);

        // Reset TitleText
        setViewValue("TitleText", axis.getTitle());

        // Reset ZeroRequiredCheckBox, LogCheckBox
        setViewValue("ZeroRequiredCheckBox", axis.isZeroRequired());
        setViewValue("LogCheckBox", axis.isLog());
        setViewValue("ShowLegendGraphicCheckBox", axis instanceof AxisY && ((AxisY) axis).isShowLegendGraphic());
        setViewVisible("ShowLegendGraphicCheckBox", axis instanceof AxisY);

        // Reset MinBoundAutoButton, MinBoundDataButton, MinBoundValueButton, MinBoundText
        AxisBound minBound = axis.getMinBound();
        setViewValue("MinBoundAutoButton", minBound == AxisBound.AUTO);
        setViewValue("MinBoundDataButton", minBound == AxisBound.DATA);
        setViewValue("MinBoundValueButton", minBound == AxisBound.VALUE);
        MinMax minMax = axisView.getAxisMinMax();
        setViewValue("MinBoundText", minMax.getMin());

        // Reset MaxBoundAutoButton, MaxBoundDataButton, MaxBoundValueButton, MaxBoundText
        AxisBound maxBound = axis.getMaxBound();
        setViewValue("MaxBoundAutoButton", maxBound == AxisBound.AUTO);
        setViewValue("MaxBoundDataButton", maxBound == AxisBound.DATA);
        setViewValue("MaxBoundValueButton", maxBound == AxisBound.VALUE);
        setViewValue("MaxBoundText", minMax.getMax());

        // Reset WrapCheckBox, WrapMinMaxBox, WrapMinText, WrapMaxText
        getView("WrapAxisBox").setVisible(axis.getType() == AxisType.X);
        boolean isWrapAxis = axis.isWrapAxis();
        setViewValue("WrapCheckBox", isWrapAxis);
        getView("WrapMinMaxBox").setVisible(isWrapAxis);
        if (isWrapAxis) {
            setViewValue("WrapMinText", axis.getWrapMinMax().getMin());
            setViewValue("WrapMaxText", axis.getWrapMinMax().getMax());
        }

        // Reset TickLengthText, TickLengthResetButton
        setViewValue("TickLengthText", axis.getTickLength());
        setViewVisible("TickLengthResetButton", axis.getTickLength() != Axis.DEFAULT_TICK_LENGTH);

        // Reset TickPosInsideButton, TickPosOutsideButton, TickPosAcrossButton, TickPosOffButton
        Axis.TickPos tickPos = axis.getTickPos();
        setViewValue("TickPosInsideButton", tickPos == Axis.TickPos.Inside);
        setViewValue("TickPosOutsideButton", tickPos == Axis.TickPos.Outside);
        setViewValue("TickPosAcrossButton", tickPos == Axis.TickPos.Across);
        setViewValue("TickPosOffButton", tickPos == Axis.TickPos.Off);

        // Reset GridSpacingText, GridBaseText
        double gridSpacing = axis.getGridSpacing();
        double gridBase = axis.getGridBase();
        setViewValue("GridSpacingText", gridSpacing != 0 ? gridSpacing : null);
        setViewValue("GridBaseText", gridBase != 0 ? gridBase : null);

        // Reset ShowTickLabelsCheckBox, TickFormatText
        setViewValue("ShowTickLabelsCheckBox", axis.isShowTickLabels());
        setViewValue("TickFormatText", axis.getTickLabelFormat());

        // Reset ExpNoneButton, ExpSciButton, ExpFinancialButton
        NumberFormat.ExpStyle expStyle = axis.getTickLabelExpStyle();
        setViewValue("ExpNoneButton", expStyle == NumberFormat.ExpStyle.None);
        setViewValue("ExpSciButton", expStyle == NumberFormat.ExpStyle.Scientific);
        setViewValue("ExpFinancialButton", expStyle == NumberFormat.ExpStyle.Financial);
    }

    /**
     * Respond to UI.
     */
    protected void respondUI(ViewEvent anEvent)
    {
        // Get Axis, AxisView (just return if null)
        Axis axis = getAxis();
        AxisView axisView = getAxisView();
        if (axisView == null)
            return;

        // Handle TitleText
        if (anEvent.equals("TitleText"))
            axis.setTitle(anEvent.getStringValue());

        // Handle MinBoundAutoButton, MinBoundDataButton, MinBoundValueButton
        if (anEvent.equals("MinBoundAutoButton"))
            axis.setMinBound(AxisBound.AUTO);
        if (anEvent.equals("MinBoundDataButton"))
            axis.setMinBound(AxisBound.DATA);
        if (anEvent.equals("MinBoundValueButton")) {
            axis.setMinBound(AxisBound.VALUE);
            axis.setMinValue(getViewFloatValue("MinBoundText"));
        }
        if (anEvent.equals("MinBoundText")) {
            double val = Math.min(anEvent.getFloatValue(), getViewFloatValue("MaxBoundText"));
            axis.setMinBound(AxisBound.VALUE);
            axis.setMinValue(val);
        }

        // Handle MaxBoundAutoButton, MaxBoundDataButton, MaxBoundValueButton, MaxBoundText
        if (anEvent.equals("MaxBoundAutoButton"))
            axis.setMaxBound(AxisBound.AUTO);
        if (anEvent.equals("MaxBoundDataButton"))
            axis.setMaxBound(AxisBound.DATA);
        if (anEvent.equals("MaxBoundValueButton")) {
            axis.setMaxBound(AxisBound.VALUE);
            axis.setMaxValue(getViewFloatValue("MaxBoundText"));
        }
        if (anEvent.equals("MaxBoundText")) {
            double val = Math.max(anEvent.getFloatValue(), getViewFloatValue("MinBoundText"));
            axis.setMaxBound(AxisBound.VALUE);
            axis.setMaxValue(val);
        }

        // Handle ZeroRequiredCheckBox, LogCheckBox, ShowLegendGraphic
        if (anEvent.equals("ZeroRequiredCheckBox"))
            axis.setZeroRequired(anEvent.getBoolValue());
        if (anEvent.equals("LogCheckBox"))
            axis.setLog(anEvent.getBoolValue());
        if (anEvent.equals("ShowLegendGraphicCheckBox"))
            ((AxisY) axis).setShowLegendGraphic(anEvent.getBoolValue());

        // Handle WrapCheckBox WrapMinText, WrapMaxText
        if (anEvent.equals("WrapCheckBox")) {
            axis.setWrapAxis(anEvent.getBoolValue());
            if (axis.getWrapMinMax() == Axis.DEFAULT_WRAP_MINMAX) {
                ChartHelper chartHelper = axisView.getChartHelper();
                double wrapMin = chartHelper.getAxisMinForIntervalCalc(axisView);
                double wrapMax = chartHelper.getAxisMaxForIntervalCalc(axisView);
                axis.setWrapMinMax(new MinMax(wrapMin, wrapMax));
            }
        }
        if (anEvent.equals("WrapMinText"))
            axis.setWrapMinMax(axis.getWrapMinMax().copyForMin(anEvent.getFloatValue()));
        if (anEvent.equals("WrapMaxText"))
            axis.setWrapMinMax(axis.getWrapMinMax().copyForMax(anEvent.getFloatValue()));

        // Handle TickLengthText, TickLengthResetButton
        if (anEvent.equals("TickLengthText"))
            axis.setTickLength(Math.min(Math.max(anEvent.getIntValue(), 1), 100));
        if (anEvent.equals("TickLengthAdd1Button"))
            axis.setTickLength(Math.min(axis.getTickLength() + 1, 100));
        if (anEvent.equals("TickLengthSub1Button"))
            axis.setTickLength(Math.max(axis.getTickLength() - 1, 1));
        if (anEvent.equals("TickLengthResetButton"))
            axis.setTickLength(Axis.DEFAULT_TICK_LENGTH);

        // Handle TickPosInsideButton, TickPosOutsideButton, TickPosAcrossButton, TickPosOffButton
        if (anEvent.equals("TickPosInsideButton"))
            axis.setTickPos(Axis.TickPos.Inside);
        if (anEvent.equals("TickPosOutsideButton"))
            axis.setTickPos(Axis.TickPos.Outside);
        if (anEvent.equals("TickPosAcrossButton"))
            axis.setTickPos(Axis.TickPos.Across);
        if (anEvent.equals("TickPosOffButton"))
            axis.setTickPos(Axis.TickPos.Off);

        // Handle GridSpacingText, GridBaseText
        if (anEvent.equals("GridSpacingText"))
            axis.setGridSpacing(anEvent.getFloatValue());
        if (anEvent.equals("GridBaseText")) {
            String valStr = anEvent.getStringValue();
            double val = anEvent.getFloatValue();
            if (valStr.equalsIgnoreCase("min")) val = Axis.GRID_BASE_DATA_MIN;
            else if (valStr.equalsIgnoreCase("max")) val = Axis.GRID_BASE_DATA_MAX;
            axis.setGridBase(val);
        }

        // Handle ShowTickLabelsCheckBox, TickFormatText
        if (anEvent.equals("ShowTickLabelsCheckBox"))
            axis.setShowTickLabels(anEvent.getBoolValue());
        if (anEvent.equals("TickFormatText"))
            axis.setTickLabelFormat(anEvent.getStringValue());

        // Handle ExpNoneButton, ExpSciButton, ExpFinancialButton
        if (anEvent.equals("ExpNoneButton"))
            axis.setTickLabelExpStyle(NumberFormat.ExpStyle.None);
        if (anEvent.equals("ExpSciButton"))
            axis.setTickLabelExpStyle(NumberFormat.ExpStyle.Scientific);
        if (anEvent.equals("ExpFinancialButton"))
            axis.setTickLabelExpStyle(NumberFormat.ExpStyle.Financial);
    }

    /**
     * Override to reset inspector label to generic "Y Axis Settings"
     */
    @Override
    public void setSelected(boolean aValue)
    {
        if (aValue == isSelected()) return;
        super.setSelected(aValue);
        if (!aValue && _axisType.isAnyY()) {
            Label label = getCollapser().getLabel();
            label.setText("Y Axis Settings");
        }
    }
}