/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.modelx;
import snap.util.PropDefaults;
import snap.util.XMLArchiver;
import snap.util.XMLElement;
import snapcharts.model.DataStyle;

/**
 * A DataStyle subclass for Polar chart properties.
 */
public class PolarStyle extends DataStyle {

    /**
     * Constructor.
     */
    public PolarStyle()
    {
        super();
    }

    /**
     * Override to register props.
     */
    @Override
    protected void initPropDefaults(PropDefaults aPropDefaults)
    {
        // Do normal version
        super.initPropDefaults(aPropDefaults);

        // Add Props
        //aPropDefaults.addProps(ThetaUnit_Prop);
    }

    /**
     * Returns the prop value for given key.
     */
    @Override
    public Object getPropValue(String aPropName)
    {
        // Handle properties
        switch (aPropName) {

            // Handle ThetaUnit
            //case ThetaUnit_Prop: return getThetaUnit();

            // Handle super class properties (or unknown)
            default: return super.getPropValue(aPropName);
        }
    }

    /**
     * Sets the prop value for given key.
     */
    @Override
    public void setPropValue(String aPropName, Object aValue)
    {
        // Handle properties
        switch (aPropName) {

            // Handle ThetaUnit
            //case ThetaUnit_Prop: setThetaUnit((ThetaUnit) aValue); break;

            // Handle super class properties (or unknown)
            default: super.setPropValue(aPropName, aValue);
        }
    }

    /**
     * Override to define DataStyle defaults
     */
    @Override
    public Object getPropDefault(String aPropName)
    {
        switch (aPropName) {

            // Handle ThetaUnit
            //case ThetaUnit_Prop: return DEFAULT_THETA_UNIT;

            // Handle super class properties (or unknown)
            default: return super.getPropDefault(aPropName);
        }
    }

    /**
     * Archival.
     */
    @Override
    public XMLElement toXML(XMLArchiver anArchiver)
    {
        // Archive basic attributes
        XMLElement e = super.toXML(anArchiver);

        // Archive ThetaUnit_Prop
        //if (!isPropDefault(ThetaUnit_Prop))
        //    e.add(ThetaUnit_Prop, getPointJoin());

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

        // Unarchive ThetaUnit
        //if (anElement.hasAttribute(ThetaUnit_Prop))
        //    setThetaUnit(anElement.getAttributeEnumValue(ShowLine_Prop, ThetaUnit.class, null));

        // Return this part
        return this;
    }

    /**
     * Standard toString implementation.
     */
    @Override
    public String toString()
    {
        return "PolarStyle {" +
                //" ThetaUnit=" + _thetaUnit +
                " }";
    }
}
