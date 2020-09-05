package snapcharts.apptools;
import snap.view.ViewEvent;
import snapcharts.model.*;
import snapcharts.app.ChartPane;

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
     * Returns the axis.
     */
    public AxisX getAxisX()  { return getChart().getAxisX(); }

    /**
     * Returns the Y axis.
     */
    public AxisY getAxisY()  { return getChart().getAxisY(); }

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
        // Get axis
        Axis axis = getAxis();

        // Reset TitleText
        setViewValue("TitleText", axis.getTitle());

        // Reset ZeroRequiredCheckBox
        setViewValue("ZeroRequiredCheckBox", axis.isZeroRequired());
    }

    /**
     * Respond to UI.
     */
    protected void respondUI(ViewEvent anEvent)
    {
        // Get axis
        Axis axis = getAxis();

        // Handle TitleText
        if (anEvent.equals("TitleText"))
            axis.setTitle(anEvent.getStringValue());

        // Handle ZeroRequiredCheckBox
        if (anEvent.equals("ZeroRequiredCheckBox"))
            axis.setZeroRequired(anEvent.getBoolValue());
    }
}