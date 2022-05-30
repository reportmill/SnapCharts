package snapcharts.notebook;

/**
 * This class represents an individual entry in a notebook.
 */
public class Entry {

    // The text
    private String  _text;

    // The index in notebook request/response list
    private int  _index;

    /**
     * Constructor.
     */
    public Entry()  { }

    /**
     * Returns the text.
     */
    public String getText()  { return _text; }

    /**
     * Sets the text.
     */
    public void setText(String aValue)
    {
        _text = aValue;
    }

    /**
     * Returns the index in Notebook request/response list.
     */
    public int getIndex()  { return _index; }

    /**
     * Sets the index in Notebook request/response list.
     */
    public void setIndex(int anIndex)
    {
        _index = anIndex;
    }

    /**
     * Standard toString method.
     */
    @Override
    public String toString()
    {
        String className = getClass().getSimpleName();
        String propStrings = toStringProps();
        return className + "{ " + propStrings + " }";
    }

    /**
     * Standard toString method.
     */
    protected String toStringProps()
    {
        String textProp = "Text:" + getText();
        return textProp;
    }
}
