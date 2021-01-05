package snapcharts.apptools;
import snap.view.ViewEvent;
import snapcharts.model.*;
import snapcharts.app.ChartPane;
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
    public Axis getAxis()  { return getChart().getAxisForType(_axisType); }

    /**
     * Returns the ChartPart.
     */
    @Override
    public ChartPart getChartPart()  { return getAxis(); }

    /**
     * Returns the AxisMin value to be shown in
     */

    /**
     * Reset UI.
     */
    protected void resetUI()
    {
        // Get axis
        Axis axis = getAxis();

        // Get AxisView (just return if null)
        AxisView axisView = (AxisView) getChartPane().getSel().getSelView();
        if (axisView == null)
            return;

        // Reset TitleText
        setViewValue("TitleText", axis.getTitle());

        // Reset ZeroRequiredCheckBox
        setViewValue("ZeroRequiredCheckBox", axis.isZeroRequired());

        // Reset MinBoundAutoButton, MinBoundDataButton, MinBoundValueButton
        AxisBound minBound = axis.getMinBound();
        setViewValue("MinBoundAutoButton", minBound == AxisBound.AUTO);
        setViewValue("MinBoundDataButton", minBound == AxisBound.DATA);
        setViewValue("MinBoundValueButton", minBound == AxisBound.VALUE);

        // Reset MinBoundText: Get the value to be shown and set value
        double minVal = axis.getMinValue();
        if (minBound == AxisBound.AUTO) {
            minVal = axisView.getAxisMin();
        }
        else if (minBound == AxisBound.DATA)
            minVal = getChart().getDataSetList().getMinForAxis(axis.getType());
        setViewValue("MinBoundText", minVal);

        // Reset MaxBoundAutoButton, MaxBoundDataButton, MaxBoundValueButton
        AxisBound maxBound = axis.getMaxBound();
        setViewValue("MaxBoundAutoButton", maxBound == AxisBound.AUTO);
        setViewValue("MaxBoundDataButton", maxBound == AxisBound.DATA);
        setViewValue("MaxBoundValueButton", maxBound == AxisBound.VALUE);

        // Reset MaxBoundText: Get the value to be shown and set value
        double maxVal = axis.getMaxValue();
        if (maxBound == AxisBound.AUTO) {
            maxVal = axisView.getAxisMax();
        }
        else if (maxBound == AxisBound.DATA)
            maxVal = getChart().getDataSetList().getMaxForAxis(axis.getType());
        setViewValue("MaxBoundText", maxVal);
    }

    /**
     * Respond to UI.
     */
    protected void respondUI(ViewEvent anEvent)
    {
        // Get axis
        Axis axis = getAxis();

        // Get AxisView (just return if null)
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

        // Handle ZeroRequiredCheckBox
        if (anEvent.equals("ZeroRequiredCheckBox"))
            axis.setZeroRequired(anEvent.getBoolValue());
    }
}