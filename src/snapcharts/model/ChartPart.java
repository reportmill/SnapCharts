package snapcharts.model;
import snap.util.*;

import java.util.Objects;

/**
 * Base class for parts of a chart: Axis, Area, Legend, etc.
 */
public class ChartPart {

    // The name
    private String  _name;

    // The Chart
    protected Chart  _chart;

    // PropertyChangeSupport
    protected PropChangeSupport _pcs = PropChangeSupport.EMPTY;

    // Constants for properties
    public static final String Name_Prop = "Name";

    /**
     * Returns the ChartDoc.
     */
    public ChartDoc getDoc()
    {
        ChartPart par = getParent();
        return par!=null ? par.getDoc() : par instanceof ChartDoc ? (ChartDoc)par : null;
    }

    /**
     * Returns the chart.
     */
    public Chart getChart()  { return _chart; }

    /**
     * Sets the chart.
     */
    protected void setChart(Chart aChart)  { _chart = aChart; }

    /**
     * Returns the dataset.
     */
    public DataSet getDataSet()  { return _chart.getDataSet(); }

    /**
     * Returns the parent part.
     */
    public ChartPart getParent()  { return _chart; }

    /**
     * Returns the name.
     */
    public String getName()  { return _name; }

    /**
     * Sets the name.
     */
    public void setName(String aName)
    {
        // If value already set, just return
        if (Objects.equals(aName, _name)) return;

        // Set value and fire prop change
        firePropChange(Name_Prop, _name, _name = aName);
    }

    /**
     * Add listener.
     */
    public void addPropChangeListener(PropChangeListener aPCL)
    {
        if(_pcs== PropChangeSupport.EMPTY) _pcs = new PropChangeSupport(this);
        _pcs.addPropChangeListener(aPCL);
    }

    /**
     * Remove listener.
     */
    public void removePropChangeListener(PropChangeListener aPCL)  { _pcs.removePropChangeListener(aPCL); }

    /**
     * Fires a property change for given property name, old value, new value and index.
     */
    protected void firePropChange(String aProp, Object oldVal, Object newVal)
    {
        if(!_pcs.hasListener(aProp)) return;
        firePropChange(new PropChange(this, aProp, oldVal, newVal));
    }

    /**
     * Fires a property change for given property name, old value, new value and index.
     */
    protected void firePropChange(String aProp, Object oldVal, Object newVal, int anIndex)
    {
        if(!_pcs.hasListener(aProp)) return;
        firePropChange(new PropChange(this, aProp, oldVal, newVal, anIndex));
    }

    /**
     * Fires a given property change.
     */
    protected void firePropChange(PropChange aPC)  { _pcs.firePropChange(aPC); }
}
