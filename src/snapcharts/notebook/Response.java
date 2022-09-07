/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.notebook;

/**
 * This Entry subclass represents the output of a processed entry.
 */
public class Response extends Entry {

    // The response as a value
    private Object  _value;

    /**
     * Constructor.
     */
    public Response(Notebook aNotebook)
    {
        super(aNotebook);
    }

    /**
     * Returns the response as a value.
     */
    public Object getValue()  { return _value; }

    /**
     * Sets the response as a value.
     */
    public void setValue(Object aValue)
    {
        _value = aValue;
    }

    /**
     * Override to dynamically get text from value.
     */
    @Override
    public String getText()
    {
        // Do normal version - just return if non-null
        String text = super.getText();
        if (text != null) return text;

        // Get text from value
        Object value = getValue();
        text = ProcessorUtils.getStringForValue(value);

        // Set and return
        return _text = text;
    }
}
