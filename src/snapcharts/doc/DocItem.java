package snapcharts.doc;
import snap.util.*;
import snapcharts.model.ChartPart;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents an item in a doc that holds other items.
 */
public abstract class DocItem<T extends PropObject> extends PropObject implements XMLArchiver.Archivable {

    // The DocItem that holds this item
    private DocItem  _parent;

    // The name
    private String  _name;

    // The content
    protected T  _content;

    // The child items
    private List<DocItem<?>>  _items = new ArrayList<>();

    // Constants for properties
    public static final String Name_Prop = "Name";

    // Constants for relation properties
    public static final String Content_Rel = "Content";
    public static final String Items_Prop = "Items";

    /**
     * Constructor.
     */
    public DocItem()
    {
        super();
    }

    /**
     * Returns the doc.
     */
    public Doc getDoc()
    {
        return _parent != null ? _parent.getDoc() : null;
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
        if (Objects.equals(aName, _name)) return;
        firePropChange(Name_Prop, _name, _name = aName);
    }

    /**
     * Returns the content of the item (Chart, DataSet, etc.).
     */
    public T getContent()  { return _content; }

    /**
     * Sets the content of the item (Chart, DataSet, etc.).
     */
    public void setContent(T theContent)
    {
        _content = theContent;
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
        List<DocItem> items = getParent() != null ? getParent().getItems() : null;
        return items != null ? items.indexOf(this) : -1;
    }

    /**
     * Returns the items.
     */
    public List<DocItem<?>> getItems()  { return _items; }

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
        if (par != null)
            return par.addChartPart(aChartPart, this);
        return null;
    }

    /**
     * Override to forward to PropSheet - for now.
     */
    @Override
    protected void firePropChange(PropChange aPC)
    {
        super.firePropChange(aPC);

        // Forward to PropSheet because accessors aren't doing this
        getPropSheet().setPropValue(aPC.getPropName(), aPC.getNewValue());
    }

    /**
     * Override to provide prop/relation names.
     */
    @Override
    protected void initPropDefaults(PropDefaults aPropDefaults)
    {
        super.initPropDefaults(aPropDefaults);
        aPropDefaults.addProps(Name_Prop);
        aPropDefaults.addRelations(Content_Rel, Items_Prop);
    }

    /**
     * Override for DocItem properties.
     */
    @Override
    public Object getPropValue(String aPropName)
    {
        switch (aPropName) {

            // Name
            case Name_Prop: return getName();

            // Content, Items
            case Content_Rel: return getContent();
            case Items_Prop: return getItems();

            // Do normal version
            default: return super.getPropValue(aPropName);
        }
    }

    /**
     * Override for DocItem properties.
     */
    @Override
    public void setPropValue(String aPropName, Object aValue)
    {
        switch (aPropName) {

            // Name
            case Name_Prop: setName(SnapUtils.stringValue(aValue)); break;

            // Content, Items
            //case Content_Rel: return getContent();
            //case Items_Prop: return getItems();

            // Do normal version
            default: super.setPropValue(aPropName, aValue);
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
        if(getName() != null && getName().length() > 0)
            e.add(Name_Prop, getName());

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
