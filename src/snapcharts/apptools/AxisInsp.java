package snapcharts.apptools;
import snap.view.Label;
import snap.view.ViewEvent;
import snapcharts.model.*;
import snapcharts.app.ChartPane;
import snapcharts.util.MinMax;
import snapcharts.views.AxisView;

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
        ChartPart chartPart = getChartPane().getSel().getSelChartPart();
        Axis axis = chartPart instanceof Axis ? (Axis) chartPart : getChart().getAxisForType(_axisType);
        return axis;
    }

    /**
     * Returns the ChartPart.
     */
    @Override
    public ChartPart getChartPart()  { return getAxis(); }

    /**
     * Reset UI.
     */
    protected void resetUI()
    {
        // Get Axis, AxisView (just return if null)
        Axis axis = getAxis();
        AxisView axisView = (AxisView) getChartPane().getSel().getSelView();
        if (axisView == null)
            return;

        // Reset Collapser.Text
        String title = axis.getType().toString() + " Axis Settings";
        Label label = getCollapser().getLabel();
        label.setText(title);

        // Reset AxisTypeLabel, TitleText
        setViewValue("AxisTypeLabel", axis.getType().toString());
        setViewValue("TitleText", axis.getTitle());

        // Reset ZeroRequiredCheckBox, LogCheckBox
        setViewValue("ZeroRequiredCheckBox", axis.isZeroRequired());
        setViewValue("LogCheckBox", axis.isLog());

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
    }

    /**
     * Respond to UI.
     */
    protected void respondUI(ViewEvent anEvent)
    {
        // Get Axis, AxisView (just return if null)
        Axis axis = getAxis();
        AxisView axisView = (AxisView) getChartPane().getSel().getSelView();
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
            double val = Math.min(anEvent.getFloatValue(), getViewFloatValue("MaxBoundText") - 1);
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
            double val = Math.max(anEvent.getFloatValue(), getViewFloatValue("MinBoundText") + 1);
            axis.setMaxBound(AxisBound.VALUE);
            axis.setMaxValue(val);
        }

        // Handle ZeroRequiredCheckBox, LogCheckBox
        if (anEvent.equals("ZeroRequiredCheckBox"))
            axis.setZeroRequired(anEvent.getBoolValue());
        if (anEvent.equals("LogCheckBox"))
            axis.setLog(anEvent.getBoolValue());
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