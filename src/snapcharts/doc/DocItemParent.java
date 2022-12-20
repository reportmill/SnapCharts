/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.doc;
import snap.props.Prop;
import snap.props.PropObject;
import snap.props.PropSet;
import snapcharts.model.Chart;
import snapcharts.model.Trace;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * This DocItem subclass manages a list of child DocItems.
 */
public class DocItemParent<T extends PropObject> extends DocItem<T> {

    // The child items
    private List<DocItem<?>>  _docItems = new ArrayList<>();

    // Constants for relation properties
    public static final String DocItems_Prop = "DocItems";
    public static final String Items_Prop = "Items";

    /**
     * Constructor.
     */
    public DocItemParent()
    {
        super(null);
    }

    /**
     * Returns the items.
     */
    public List<DocItem<?>> getDocItems()  { return _docItems; }

    /**
     * Sets the items.
     */
    public void setDocItems(DocItem[] theItems)
    {
        while(getItemCount() > 0)
            removeItem(0);

        for (DocItem item : theItems)
            addItem(item);
    }

    /**
     * Returns the number of items.
     */
    public int getItemCount() { return _docItems.size(); }

    /**
     * Returns the individual item at given index.
     */
    public DocItem getItem(int anIndex)  { return _docItems.get(anIndex); }

    /**
     * Adds an item.
     */
    public void addItem(DocItem anItem)
    {
        addItem(anItem, _docItems.size());
    }

    /**
     * Adds an item at given index.
     */
    public void addItem(DocItem anItem, int anIndex)
    {
        _docItems.add(anIndex, anItem);
        firePropChange(DocItems_Prop, null, anItem, anIndex);
        anItem.setParent(this);
    }

    /**
     * Removes an item at given index.
     */
    public DocItem removeItem(int anIndex)
    {
        DocItem item = _docItems.remove(anIndex);
        firePropChange(DocItems_Prop, item, null, anIndex);
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
     * Returns the items.
     */
    private Object[] getItems()
    {
        Stream<DocItem<?>> docItemStream = _docItems.stream();
        Stream<Object> itemsStream = docItemStream.map(docItem -> docItem.getContent());
        Object[] itemsArray = itemsStream.toArray(size -> new Object[size]);
        return itemsArray;
    }

    /**
     * Sets the items.
     */
    private void setItems(Object[] theItems)
    {
        for (Object item : theItems) {
            DocItem docItem = createDocItemForObject(item);
            addItem(docItem);
        }
    }

    /**
     * Returns a docItem for given object.
     */
    private DocItem<?> createDocItemForObject(Object anObj)
    {
        if (anObj instanceof Chart)
            return new DocItemChart((Chart) anObj);
        if (anObj instanceof Trace)
            return new DocItemTrace((Trace) anObj);

        // Complain
        System.err.println("DocItemParent.createDocItemForObject: Illegal object type: " + anObj.getClass());
        return null;
    }

    /**
     * Override to configure props for this class.
     */
    @Override
    protected void initProps(PropSet aPropSet)
    {
        // Do normal version
        super.initProps(aPropSet);

        // DocItems, Items
        Prop docItemsProp = aPropSet.addPropNamed(DocItems_Prop, DocItem[].class, null);
        docItemsProp.setSkipArchival(true);

        // This property is to allow for archival of content so <Items> has array of <Chart> instead of
        // <DocItemChart><Content><Chart>
        aPropSet.addPropNamed(Items_Prop, PropObject[].class, null).setDefaultPropClass(null);
    }

    /**
     * Override for DocItem properties.
     */
    @Override
    public Object getPropValue(String aPropName)
    {
        switch (aPropName) {

            // DocItems, Items
            case DocItems_Prop: return getDocItems();
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

            // DocItems, Items
            case DocItems_Prop: setDocItems((DocItem[]) aValue); break;
            case Items_Prop: setItems((Object[]) aValue); break;

            // Do normal version
            default: super.setPropValue(aPropName, aValue);
        }
    }
}
