package snapcharts.apptools;
import snap.geom.Pos;
import snap.text.NumberFormat;
import snap.view.ButtonBase;
import snap.view.ParentView;
import snap.view.View;
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

    /**
     * Constructor.
     */
    public AxisInsp(ChartPane aChartPane)
    {
        super(aChartPane);
    }

    /**
     * Returns the name.
     */
    @Override
    public String getName()  { return "Axis Settings"; }

    /**
     * Returns the axis.
     */
    public Axis getAxis()
    {
        ChartPart chartPart = getChartPane().getSelChartPart();
        Axis axis = chartPart instanceof Axis ? (Axis) chartPart : null;
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
    public ChartPart getChartPart()
    {
        Axis axis = getAxis();
        if (axis != null)
            return axis;

        Chart chart = getChart();
        return chart.getAxisX();
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
        // Get Axis, AxisType (just return if null)
        Axis axis = getAxis(); if (axis == null) return;
        AxisType axisType = axis.getType();

        // Reset XAxisButton, YAxisButton, Y2AxisButton, Y3AxisButton, Y3AxisButton
        ChartHelper chartHelper = getChartPane().getChartHelper();
        setViewValue("XAxisButton", axisType == AxisType.X);
        setViewValue("YAxisButton", axisType == AxisType.Y);
        setViewValue("Y2AxisButton", axisType == AxisType.Y2);
        setViewValue("Y3AxisButton", axisType == AxisType.Y3);
        setViewValue("Y4AxisButton", axisType == AxisType.Y4);
        setViewValue("ZAxisButton", axisType == AxisType.Z);
        setViewEnabled("XAxisButton", chartHelper.getAxisView(AxisType.X) != null);
        setViewEnabled("YAxisButton", chartHelper.getAxisView(AxisType.Y) != null);
        setViewVisible("Y2AxisButton", chartHelper.getAxisView(AxisType.Y2) != null);
        setViewVisible("Y3AxisButton", chartHelper.getAxisView(AxisType.Y3) != null);
        setViewVisible("Y4AxisButton", chartHelper.getAxisView(AxisType.Y4) != null);
        setViewVisible("ZAxisButton", chartHelper.getAxisView(AxisType.Z) != null);

        // Fix Button.Position for AxisButtons
        ParentView axisButtonParent = getView("XAxisButton").getParent();
        View[] axisButtons = axisButtonParent.getChildrenManaged();
        for (int i = 0, iMax = axisButtons.length; i < iMax; i++) {
            ButtonBase child = (ButtonBase) axisButtons[i];
            child.setPosition(i == 0? Pos.CENTER_LEFT : i + 1 == iMax ? Pos.CENTER_RIGHT : Pos.CENTER);
        }

        // Get AxisView
        AxisView axisView = getAxisView();
        if (axisView == null)
            return;

        // Reset TitleText
        setViewValue("TitleText", axis.getTitle());

        // Reset MinBoundAutoButton, MinBoundDataButton, MinBoundValueButton, MinBoundText
        AxisBound minBound = axis.getMinBound();
        setViewValue("MinBoundAutoButton", minBound == AxisBound.AUTO);
        setViewValue("MinBoundDataButton", minBound == AxisBound.DATA);
        setViewValue("MinBoundValueButton", minBound == AxisBound.VALUE);
        double minVal = axisView.getChartHelper().getAxisMinForIntervalCalc(axisView);
        setViewValue("MinBoundText", minVal);
        setViewVisible("MinBoundText", minBound != AxisBound.AUTO);
        setViewEnabled("MinBoundText", minBound == AxisBound.VALUE);

        // Reset MaxBoundAutoButton, MaxBoundDataButton, MaxBoundValueButton, MaxBoundText
        AxisBound maxBound = axis.getMaxBound();
        setViewValue("MaxBoundAutoButton", maxBound == AxisBound.AUTO);
        setViewValue("MaxBoundDataButton", maxBound == AxisBound.DATA);
        setViewValue("MaxBoundValueButton", maxBound == AxisBound.VALUE);
        double maxVal = axisView.getChartHelper().getAxisMaxForIntervalCalc(axisView);
        setViewValue("MaxBoundText", maxVal);
        setViewVisible("MaxBoundText", maxBound != AxisBound.AUTO);
        setViewEnabled("MaxBoundText", maxBound == AxisBound.VALUE);

        // Reset ZeroRequiredCheckBox, ShowZeroLineCheckBox, LogCheckBox, ShowLogMinorLabelsCheckBox, ShowLegendGraphicCheckBox
        setViewValue("ZeroRequiredCheckBox", axis.isZeroRequired());
        setViewValue("ShowZeroLineCheckBox", axis.isShowZeroLine());
        setViewValue("LogCheckBox", axis.isLog());
        setViewValue("ShowLogMinorLabelsCheckBox", axis.isShowLogMinorLabels());
        setViewVisible("ShowLogMinorLabelsCheckBox", axis.isLog());
        setViewValue("ShowLegendGraphicCheckBox", axis instanceof AxisY && ((AxisY) axis).isShowLegendGraphic());
        setViewVisible("ShowLegendGraphicCheckBox", axis instanceof AxisY);

        // Reset WrapCheckBox, WrapMinMaxBox, WrapMinText, WrapMaxText
        getView("WrapAxisBox").setVisible(axis.getType() == AxisType.X);
        boolean isWrapAxis = axis.isWrapAxis();
        setViewValue("WrapCheckBox", isWrapAxis);
        getView("WrapMinMaxBox").setVisible(isWrapAxis);
        if (isWrapAxis) {
            setViewValue("WrapMinText", axis.getWrapMinMax().getMin());
            setViewValue("WrapMaxText", axis.getWrapMinMax().getMax());
        }

        // Reset GridSpacingText, GridBaseText
        double gridSpacing = axis.getGridSpacing();
        double gridBase = axis.getGridBase();
        setViewValue("GridSpacingText", gridSpacing != 0 ? gridSpacing : null);
        setViewValue("GridBaseText", gridBase != 0 ? gridBase : null);

        // Reset TickLengthText, TickLengthResetButton
        setViewValue("TickLengthText", axis.getTickLength());
        setViewVisible("TickLengthResetButton", axis.getTickLength() != Axis.DEFAULT_TICK_LENGTH);

        // Reset TickPosInsideButton, TickPosOutsideButton, TickPosAcrossButton, TickPosOffButton
        Axis.TickPos tickPos = axis.getTickPos();
        setViewValue("TickPosInsideButton", tickPos == Axis.TickPos.Inside);
        setViewValue("TickPosOutsideButton", tickPos == Axis.TickPos.Outside);
        setViewValue("TickPosAcrossButton", tickPos == Axis.TickPos.Across);
        setViewValue("TickPosOffButton", tickPos == Axis.TickPos.Off);

        // Reset ShowTickLabelsCheckBox
        setViewValue("ShowTickLabelsCheckBox", axis.isShowTickLabels());

        // Reset TickFormatText
        NumberFormat numFormat = NumberFormat.getFormatOrDefault(axis.getTextFormat());
        String numFormatPattern = numFormat.isPatternSet() ? numFormat.getPattern() : null;
        setViewValue("TickFormatText", numFormatPattern);

        // Reset ExpNoneButton, ExpSciButton, ExpFinancialButton
        NumberFormat.ExpStyle expStyle = numFormat.getExpStyle();
        setViewValue("ExpNoneButton", expStyle == NumberFormat.ExpStyle.None);
        setViewValue("ExpSciButton", expStyle == NumberFormat.ExpStyle.Scientific);
        setViewValue("ExpFinancialButton", expStyle == NumberFormat.ExpStyle.Financial);

        // Reset AutoAngleButton, 30AngleButton, 45AngleButton, 60AngleButton, 90AngleButton, TickLabelRotationText
        boolean ticksAutoRotate = axis.isTickLabelAutoRotate();
        double ticksAngle = axis.getTickLabelRotation();
        setViewValue("AutoAngleButton", ticksAutoRotate);
        setViewValue("30AngleButton", ticksAngle == 30);
        setViewValue("45AngleButton", ticksAngle == 45);
        setViewValue("60AngleButton", ticksAngle == 60);
        setViewValue("90AngleButton", ticksAngle == 90);
        setViewValue("TickLabelRotationText", ticksAngle);

        // Reset MinorTickCountText
        setViewValue("MinorTickCountText", axis.getMinorTickCount());
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

        // Handle AxisButtons
        String name = anEvent.getName();
        if (name != null && name.endsWith("AxisButton")) {
            String axisName = name.replace("AxisButton", "");
            AxisType axisType = AxisType.valueOf(axisName);
            ChartHelper chartHelper = axisView.getChartHelper();
            Axis axis2 = chartHelper.getAxisView(axisType).getAxis();
            ChartPane chartPane = getChartPane();
            chartPane.getSel().setSelChartPart(axis2);
            return;
        }

        // Handle TitleText
        if (anEvent.equals("TitleText"))
            axis.setTitle(anEvent.getStringValue());

        // Handle MinBoundAutoButton, MinBoundDataButton, MinBoundValueButton
        if (anEvent.equals("MinBoundAutoButton"))
            axis.setMinBound(AxisBound.AUTO);
        if (anEvent.equals("MinBoundDataButton"))
            axis.setMinBound(AxisBound.DATA);
        if (anEvent.equals("MinBoundValueButton")) {
            double axisMin = axisView.getAxisMin();
            axis.setMinBound(AxisBound.VALUE);
            axis.setMinValue(axisMin);
        }
        if (anEvent.equals("MinBoundText")) {
            double val = anEvent.getFloatValue();
            axis.setMinBound(AxisBound.VALUE);
            axis.setMinValue(val);
        }

        // Handle MaxBoundAutoButton, MaxBoundDataButton, MaxBoundValueButton, MaxBoundText
        if (anEvent.equals("MaxBoundAutoButton"))
            axis.setMaxBound(AxisBound.AUTO);
        if (anEvent.equals("MaxBoundDataButton"))
            axis.setMaxBound(AxisBound.DATA);
        if (anEvent.equals("MaxBoundValueButton")) {
            double axisMax = axisView.getAxisMax();
            axis.setMaxBound(AxisBound.VALUE);
            axis.setMaxValue(axisMax);
        }
        if (anEvent.equals("MaxBoundText")) {
            double val = anEvent.getFloatValue();
            axis.setMaxBound(AxisBound.VALUE);
            axis.setMaxValue(val);
        }

        // Handle ZeroRequiredCheckBox, ShowZeroLineCheckBox, LogCheckBox, ShowLogMinorLabelsCheckBox, ShowLegendGraphic
        if (anEvent.equals("ZeroRequiredCheckBox"))
            axis.setZeroRequired(anEvent.getBoolValue());
        if (anEvent.equals("ShowZeroLineCheckBox"))
            axis.setShowZeroLine(anEvent.getBoolValue());
        if (anEvent.equals("LogCheckBox"))
            axis.setLog(anEvent.getBoolValue());
        if (anEvent.equals("ShowLogMinorLabelsCheckBox"))
            axis.setShowLogMinorLabels(anEvent.getBoolValue());
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

        // Handle ShowTickLabelsCheckBox
        if (anEvent.equals("ShowTickLabelsCheckBox"))
            axis.setShowTickLabels(anEvent.getBoolValue());

        // Handle TickFormatText
        if (anEvent.equals("TickFormatText")) {
            NumberFormat numFormat = NumberFormat.getFormatOrDefault(axis.getTextFormat());
            axis.setTextFormat(numFormat.copyForProps(NumberFormat.Pattern_Prop, anEvent.getStringValue()));
        }

        // Handle ExpNoneButton, ExpSciButton, ExpFinancialButton
        if (anEvent.equals("ExpNoneButton")) {
            NumberFormat numFormat = NumberFormat.getFormatOrDefault(axis.getTextFormat());
            axis.setTextFormat(numFormat.copyForProps(NumberFormat.ExpStyle_Prop, NumberFormat.ExpStyle.None));
        }
        if (anEvent.equals("ExpSciButton")) {
            NumberFormat numFormat = NumberFormat.getFormatOrDefault(axis.getTextFormat());
            axis.setTextFormat(numFormat.copyForProps(NumberFormat.ExpStyle_Prop, NumberFormat.ExpStyle.Scientific));
        }
        if (anEvent.equals("ExpFinancialButton")) {
            NumberFormat numFormat = NumberFormat.getFormatOrDefault(axis.getTextFormat());
            axis.setTextFormat(numFormat.copyForProps(NumberFormat.ExpStyle_Prop, NumberFormat.ExpStyle.Financial));
        }

        // Handle AutoAngleButton, 30AngleButton, 45AngleButton, 60AngleButton, 90AngleButton, TickLabelRotationText
        if (anEvent.equals("AutoAngleButton")) {
            axis.setTickLabelAutoRotate(true);
            axis.setTickLabelRotation(0);
        }
        if (anEvent.equals("30AngleButton")) {
            axis.setTickLabelRotation(30);
            axis.setTickLabelAutoRotate(false);
        }
        if (anEvent.equals("45AngleButton")) {
            axis.setTickLabelRotation(45);
            axis.setTickLabelAutoRotate(false);
        }
        if (anEvent.equals("60AngleButton")) {
            axis.setTickLabelRotation(60);
            axis.setTickLabelAutoRotate(false);
        }
        if (anEvent.equals("90AngleButton")) {
            axis.setTickLabelRotation(90);
            axis.setTickLabelAutoRotate(false);
        }
        if (anEvent.equals("TickLabelRotationText")) {
            axis.setTickLabelRotation(anEvent.getFloatValue());
            axis.setTickLabelAutoRotate(false);
        }

        // Handle MinorTickCountText
        if (anEvent.equals("MinorTickCountText"))
            axis.setMinorTickCount(anEvent.getIntValue());
    }
}