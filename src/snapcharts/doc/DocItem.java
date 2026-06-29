/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.doc;
import snap.props.PropSet;
import snap.props.PropObject;
import snap.util.*;
import snapcharts.charts.ChartPart;
import java.util.List;
import java.util.Objects;

/**
 * Represents an item in a doc that holds other items.
 */
public abstract class DocItem<T extends PropObject> extends PropObject {

    // The DocItem that holds this item
    private DocItemParent  _parent;

    // The name
    private String  _name;

    // The content
    protected T  _content;

    // Constants for properties
    public static final String Name_Prop = "Name";
    public static final String Content_Prop = "Content";

    /**
     * Constructor.
     */
    public DocItem(T aContent)
    {
        super();

        if (aContent != null)
            setContent(aContent);
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
     * Returns the parent item.
     */
    public DocItemParent getParent()  { return _parent; }

    /**
     * Sets the parent item.
     */
    public void setParent(DocItemParent aPar)
    {
        _parent = aPar;
    }

    /**
     * Returns the index of this item in parent.
     */
    public int getIndex()
    {
        DocItemParent parent = getParent();
        List<DocItem> items = parent != null ? parent.getDocItems() : null;
        return items != null ? items.indexOf(this) : -1;
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
        DocItemParent parent = getParent();
        if (parent != null)
            return parent.addChartPart(aChartPart, this);
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

        // Name, Content
        aPropSet.addPropNamed(Name_Prop, String.class, null);
        aPropSet.addPropNamed(Content_Prop, PropObject.class, null);
    }

    /**
     * Override for DocItem properties.
     */
    @Override
    public Object getPropValue(String aPropName)
    {
        return switch (aPropName) {

            // Name, Content
            case Name_Prop -> getName();
            case Content_Prop -> getContent();

            // Do normal version
            default -> super.getPropValue(aPropName);
        };
    }

    /**
     * Override for DocItem properties.
     */
    @Override
    public void setPropValue(String aPropName, Object aValue)
    {
        switch (aPropName) {

            // Name, Content
            case Name_Prop -> setName(Convert.stringValue(aValue));
            case Content_Prop -> setContent((T) aValue);

            // Do normal version
            default -> super.setPropValue(aPropName, aValue);
        }
    }
}
