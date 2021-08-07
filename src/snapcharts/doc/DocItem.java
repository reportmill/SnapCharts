package snapcharts.doc;
import snap.gfx.Image;
import snap.util.*;
import snap.view.ViewUtils;
import snapcharts.model.ChartPart;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an item in a doc that holds other items.
 */
public abstract class DocItem implements XMLArchiver.Archivable {

    // The DocItem that holds this item
    private DocItem  _parent;

    // The name
    private String  _name;

    // The child items
    private List<DocItem> _items = new ArrayList<>();

    // The image to use as icon
    private Image  _iconImage;

    // PropertyChangeSupport
    protected PropChangeSupport _pcs = PropChangeSupport.EMPTY;

    // Constants for images
    protected static Image ICON_PLAIN = Image.get(ViewUtils.class, "PlainFile.png");
    protected static Image ICON_DATA = Image.get(ViewUtils.class, "TableFile.png");
    protected static Image ICON_DOC = Image.get(DocItem.class, "TableFile.png");

    // Constants for properties
    public static final String Name_Prop = "Name";
    public static final String Items_Prop = "Items";

    /**
     * Constructor.
     */
    public DocItem()
    {

    }

    /**
     * Returns the doc.
     */
    public Doc getDoc()
    {
        return _parent!=null ? _parent.getDoc() : null;
    }

    /**
     * Returns the name.
     */
    public String getName()  { return _name; }

    /**
     * Sets the name.
     */
    public void setName(String aName)
    {
        _name = aName;
    }

    /**
     * Returns whether this item is a parent.
     */
    public boolean isParent()  { return false; }

    /**
     * Returns the parent item.
     */
    public DocItem getParent()  { return _parent; }

    /**
     * Sets the parent item.
     */
    public void setParent(DocItem aPar)
    {
        _parent = aPar;
    }

    /**
     * Returns the index of this item in parent.
     */
    public int getIndex()
    {
        List<DocItem> items = getParent()!=null ? getParent().getItems() : null;
        return items!=null ? items.indexOf(this) : -1;
    }

    /**
     * Returns the items.
     */
    public List<DocItem> getItems()  { return _items; }

    /**
     * Returns the number of items.
     */
    public int getItemCount() { return _items.size(); }

    /**
     * Returns the individual item at given index.
     */
    public DocItem getItem(int anIndex)  { return _items.get(anIndex); }

    /**
     * Adds an item.
     */
    public void addItem(DocItem anItem)
    {
        addItem(anItem, _items.size());
    }

    /**
     * Adds an item at given index.
     */
    public void addItem(DocItem anItem, int anIndex)
    {
        _items.add(anIndex, anItem);
        firePropChange(Items_Prop, null, anItem, anIndex);
        anItem.setParent(this);
    }

    /**
     * Removes an item at given index.
     */
    public DocItem removeItem(int anIndex)
    {
        DocItem item = _items.remove(anIndex);
        firePropChange(Items_Prop, item, null, anIndex);
        return item;
    }

    /**
     * Removes given item.
     */
    public void removeItem(DocItem anItem)
    {
        int index = anItem.getIndex();
        if (index >= 0)
            removeItem(index);
    }

    /**
     * Returns the ChartPart represented by this item.
     */
    public ChartPart getChartPart()  { return null; }

    /**
     * Adds a ChartPart to this DocItem.
     */
    public DocItem addChartPart(ChartPart aChartPart, DocItem anItem)
    {
        DocItem par = getParent();
        if (par!=null)
            return par.addChartPart(aChartPart, this);
        return null;
    }

    protected Image createIconImage()
    {
        return Image.get(ViewUtils.class, "PlainFile.png");
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
}
