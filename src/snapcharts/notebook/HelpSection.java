package snapcharts.notebook;
import snap.text.RichText;
import snap.text.TextDoc;
import snap.text.TextStyle;

/**
 * This class represents a section of help from a HelpFile.
 */
public class HelpSection {

    // The HelpFile that holds section
    private HelpFile  _helpFile;

    // The header
    private String  _header;

    // The content
    private String  _content;

    // The TextDoc
    private TextDoc  _textDoc;

    /**
     * Constructor.
     */
    public HelpSection(HelpFile aHelpFile, String sectionText)
    {
        super();

        // Set ivars
        _helpFile = aHelpFile;


        // Assume first line is header
        int headerEnd = sectionText.indexOf('\n');
        if (headerEnd > 0) {
            String header = sectionText.substring(0, headerEnd);
            setHeader(header);
            sectionText = sectionText.substring(headerEnd);
        }

        // Set content
        setContent(sectionText);
    }

    /**
     * Returns the HelpFile.
     */
    public HelpFile getHelpFile()  { return _helpFile; }

    /**
     * Returns the header.
     */
    public String getHeader()  { return _header; }

    /**
     * Sets the header.
     */
    public void setHeader(String aString)
    {
        _header = aString;
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

    /**
     * Returns the TextDoc.
     */
    public TextDoc getTextDoc()
    {
        // If already set, just return
        if (_textDoc != null) return _textDoc;

        // Create, set, return
        TextDoc textDoc = createTextDoc();
        return _textDoc = textDoc;
    }

    /**
     * Creates the TextDoc.
     */
    protected TextDoc createTextDoc()
    {
        // Get HelpFile and create RichText
        HelpFile helpFile = getHelpFile();
        RichText richText = new RichText();

        // Set header
        String headerStr = getHeader();
        TextStyle headerStyle = helpFile.getSectionHeaderStyle();
        richText.addChars(headerStr, headerStyle, richText.length());

        // Set content
        String contentStr = getContent();
        TextStyle contentStyle = helpFile.getSectionContentStyle();
        richText.addChars(contentStr, contentStyle, richText.length());

        // Return
        return richText;
    }
}
