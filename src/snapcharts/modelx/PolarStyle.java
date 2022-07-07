/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.modelx;
import snap.props.PropSet;
import snapcharts.model.TraceStyle;

/**
 * A TraceStyle subclass for Polar chart properties.
 */
public class PolarStyle extends TraceStyle {

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
    protected void initProps(PropSet aPropSet)
    {
        // Do normal version
        super.initProps(aPropSet);

        // Add Props
        //aPropSet.addPropNamed(ThetaUnit_Prop);
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
     * Override to define custom defaults
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
}
