package snapcharts.model;
import snap.geom.Pos;
import snap.util.*;

/**
 * A class to represent the Legend of chart.
 */
public class Legend extends ChartPart {

    // Whether legend is showing
    private boolean  _showLegend;

    // The legend position
    private Pos  _position;

    // Property constants
    public static final String ShowLegend_Prop = "ShowLegend";
    public static final String Position_Prop = "Position";

    /**
     * Constructor.
     */
    public Legend()
    {
        super();
        _position = (Pos) getPropDefault(Position_Prop);
    }

    /**
     * Returns whether to show legend.
     */
    public boolean isShowLegend()  { return _showLegend; }

    /**
     * Sets whether to show legend.
     */
    public void setShowLegend(boolean aValue)
    {
        if (aValue==isShowLegend()) return;
        firePropChange(ShowLegend_Prop, _showLegend, _showLegend = aValue);
    }

    /**
     * Returns the position.
     */
    public Pos getPosition()  { return _position; }

    /**
     * Sets the position.
     */
    public void setPosition(Pos aValue)
    {
        if (aValue==getPosition() || aValue==null) return;
        firePropChange(Position_Prop, _position, _position = aValue);
    }

    /**
     * Override to provide custom defaults for Legend (Position).
     */
    @Override
    public Object getPropDefault(String aPropName)
    {
        if (aPropName==Position_Prop)
            return Pos.CENTER_RIGHT;
        return super.getPropDefault(aPropName);
    }

    /**
     * Archival.
     */
    @Override
    public XMLElement toXML(XMLArchiver anArchiver)
    {
        // Archive basic attributes
        XMLElement e = super.toXML(anArchiver);

        // Archive ShowLegend, Position
        if (isShowLegend())
            e.add(ShowLegend_Prop, isShowLegend());
        if (getPosition()!=getPropDefault(Position_Prop))
            e.add(Position_Prop, getPosition());

        // Return element
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

        // Unarchive ShowLegend, Position
        if (anElement.hasAttribute(ShowLegend_Prop))
            setShowLegend(anElement.getAttributeBoolValue(ShowLegend_Prop));
        if (anElement.hasAttribute(Position_Prop))
            setPosition(Pos.get(anElement.getAttributeValue(Position_Prop)));

        // Return this part
        return this;
    }
}
