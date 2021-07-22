package snapcharts.model;
import snap.util.XMLArchiver;
import snap.util.XMLElement;

/**
 * A class to represent a Chart Axis.
 */
public class AxisY extends Axis {

    // The axis type
    private AxisType  _axisType;

    // Whether to show legend graphic in axis
    private boolean  _showLegendGraphic;

    // Constants for properties
    public static final String ShowLegendGraphic_Prop = "ShowLegendGraphic";

    /**
     * Constructor.
     */
    public AxisY(Chart aChart, AxisType anAxisType)
    {
        super();
        _chart = aChart;
        _axisType = anAxisType;
    }

    /**
     * Returns the axis type.
     */
    public AxisType getType()  { return _axisType; }

    /**
     * Returns whether to show LegendEntry graphic in axis.
     */
    public boolean isShowLegendGraphic()  { return _showLegendGraphic; }

    /**
     * Sets whether to show LegendEntry graphic in axis.
     */
    public void setShowLegendGraphic(boolean aValue)
    {
        if (aValue == isShowLegendGraphic()) return;
        firePropChange(ShowLegendGraphic_Prop, _showLegendGraphic, _showLegendGraphic = aValue);
    }

    /**
     * Archival.
     */
    @Override
    public XMLElement toXML(XMLArchiver anArchiver)
    {
        // Archive basic attributes
        XMLElement e = super.toXML(anArchiver);

        // Archive ShowLegendGraphic
        if (isShowLegendGraphic())
            e.add(ShowLegendGraphic_Prop, true);

        // Return xml
        return e;
    }

    /**
     * Unarchival.
     */
    @Override
    public Object fromXML(XMLArchiver anArchiver, XMLElement anElement)
    {
        // Unarchive basic attributes
        super.fromXML(anArchiver, anElement);

        // Unuarchive ShowLegendGraphic
        if (anElement.hasAttribute(ShowLegendGraphic_Prop))
            setShowLegendGraphic(anElement.getAttributeBoolValue(ShowLegendGraphic_Prop));

        // Return
        return this;
    }
}