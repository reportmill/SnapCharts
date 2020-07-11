package snapcharts.model;
import snap.util.*;

/**
 * Base class for parts of a chart: Axis, Area, Legend, etc.
 */
public class ChartPart {

    // The Chart
    protected Chart  _chart;

    // PropertyChangeSupport
    protected PropChangeSupport _pcs = PropChangeSupport.EMPTY;

    /**
     * Returns the chart.
     */
    public Chart getChart()  { return _chart; }

    /**
     * Returns the dataset.
     */
    public DataSet getDataSet()  { return _chart.getDataSet(); }

    /**
     * Sets the chart.
     */
    protected void setChart(Chart aChart)  { _chart = aChart; }

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
