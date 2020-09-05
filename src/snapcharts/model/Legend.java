package snapcharts.model;
import snap.util.*;

/**
 * A class to represent the Legend of chart.
 */
public class Legend extends ChartPart {

    // Whether legend is showing
    private boolean  _showLegend;

    // Property constants
    public static final String ShowLegend_Prop = "ShowLegend";

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
        firePropChange(ShowLegend_Prop, _showLegend, _showLegend=aValue);
    }

    /**
     * Archival.
     */
    @Override
    public XMLElement toXML(XMLArchiver anArchiver)
    {
        // Archive basic attributes
        XMLElement e = super.toXML(anArchiver);

        // Archive ShowLegend
        if (isShowLegend())
            e.add(ShowLegend_Prop, isShowLegend());

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

        // Unarchive ShowLegend
        if (anElement.hasAttribute(ShowLegend_Prop))
            setShowLegend(anElement.getAttributeBoolValue(ShowLegend_Prop));

        // Return this part
        return this;
    }
}
