package snapcharts.notebook;

/**
 * This class represents a section of help from a HelpFile.
 */
public class HelpSection {

    // The heading
    private String  _heading;

    // The content
    private String  _content;

    /**
     * Constructor.
     */
    public HelpSection()
    {
        super();
    }

    /**
     * Returns the heading.
     */
    public String getHeading()  { return _heading; }

    /**
     * Sets the heading.
     */
    public void setHeading(String aString)
    {
        _heading = aString;
    }

    /**
     * Returns the content.
     */
    public String getContent()  { return _content; }

    /**
     * Sets the content.
     */
    public void setContent(String aString)
    {
        _content = aString;
    }
}
