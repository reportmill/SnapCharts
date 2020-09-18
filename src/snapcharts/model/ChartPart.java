package snapcharts.model;
import snap.util.*;

import java.util.*;

/**
 * Base class for parts of a chart: Axis, Area, Legend, etc.
 */
public class ChartPart implements XMLArchiver.Archivable {

    // The name
    private String  _name;

    // The Chart
    protected Chart  _chart;

    // PropertyChangeSupport
    protected PropChangeSupport _pcs = PropChangeSupport.EMPTY;

    // A map to hold prop keys for unique classes
    private static Map<Class<? extends ChartPart>, String[]>  _classProps = new HashMap<>();

    // Constants for properties
    public static final String Name_Prop = "Name";

    /**
     * Constructor.
     */
    public ChartPart()  { }

    /**
     * Returns the Doc.
     */
    public Doc getDoc()
    {
        Chart chart = getChart();
        return chart!=null ? chart.getDoc() : null;
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
    public DataSetList getDataSetList()  { return _chart.getDataSetList(); }

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

    /**
     * Returns the prop keys.
     */
    public String[] getPropKeysAll()
    {
        return getPropKeysAllForClass(getClass());
    }

    /**
     * Returns the prop keys.
     */
    protected String[] getPropKeysLocal()
    {
        return new String[] {
                Name_Prop
        };
    }

    /**
     * Returns the prop value for given key.
     */
    public Object getPropValue(String aPropName)
    {
        // Handle properties
        switch (aPropName) {
            case Name_Prop: return getName();
            default: System.err.println("ChartPart.getPropValue: Unknown prop: " + aPropName); return null;
        }
    }

    /**
     * Sets the prop value for given key.
     */
    public void setPropValue(String aPropName, Object aValue)
    {
        // Handle properties
        switch (aPropName) {
            case Name_Prop: setName(SnapUtils.stringValue(aValue)); break;
            default: System.err.println("ChartPart.setPropValue: Unknown prop: " + aPropName);
        }
    }

    /**
     * Returns the value for given key.
     */
    public Object getPropDefault(String aPropName)
    {
        // Handle properties
        switch (aPropName) {
            case Name_Prop: return null;
            default: System.err.println("ChartPart.getPropDefault: Unknown prop: " + aPropName); return null;
        }
    }

    /**
     * Archival.
     */
    @Override
    public XMLElement toXML(XMLArchiver anArchiver)
    {
        // Get new element with class name
        String cname = getClass().getSimpleName();
        XMLElement e = new XMLElement(cname);

        // Archive name
        if(getName()!=null && getName().length()>0) e.add(Name_Prop, getName());

        // Return element
        return e;
    }

    /**
     * Unarchival.
     */
    @Override
    public Object fromXML(XMLArchiver anArchiver, XMLElement anElement)
    {
        // Unarchive Name
        if(anElement.hasAttribute(Name_Prop))
            setName(anElement.getAttributeValue(Name_Prop));

        // Return this part
        return this;
    }

    /**
     * Returns the prop keys.
     */
    public static String[] getPropKeysAllForClass(Class<? extends ChartPart> aClass)
    {
        // Get props from cache and just return if found
        String props[] = _classProps.get(aClass);
        if (props!=null)
            return props;

        // Create list and add super props to it
        List<String> propsList = new ArrayList<>();
        Class superClass = aClass.getSuperclass();
        String superProps[] = ChartPart.class.isAssignableFrom(superClass) ? getPropKeysAllForClass(superClass) : null;
        if (superProps!=null)
            Collections.addAll(propsList, superProps);

        // Add props for class
        try {
            ChartPart object = aClass.newInstance();
            String classProps[] = object.getPropKeysLocal();
            Collections.addAll(propsList, classProps);
        }
        catch (Exception e) { throw new RuntimeException("ChartPart.getPropKeysAllForClass failed: " + aClass); }

        // Add props array to Class map and return
        props = propsList.toArray(new String[0]);
        _classProps.put(aClass, props);
        return props;
    }
}
