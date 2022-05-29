/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.notebook;

/**
 * This class represents an entry created by the user with an expression, command or code.
 */
public class Request extends Entry {

    /**
     * Returns whether request is empty.
     */
    public boolean isEmpty()
    {
        String text = getText().trim();
        return text.length() == 0;
    }
}
