package snapcharts.notebook;
import snap.gfx.Color;
import snap.gfx.Font;
import snap.text.TextStyle;
import snap.web.WebURL;
import java.util.ArrayList;
import java.util.List;

/**
 * This class manages a help file for HelpPane.
 */
public class HelpFile {

    // The source URL
    private WebURL  _sourceURL;

    // The sections
    private HelpSection[]  _sections;

    // The section style
    private TextStyle  _sectionHeaderStyle;

    // The content style
    private TextStyle  _sectionContentStyle;

    /**
     * Constructor.
     */
    public HelpFile(WebURL aURL)
    {
        super();

        setSourceURL(aURL);
    }

    /**
     * Returns the source URL.
     */
    public WebURL getSourceURL()  { return _sourceURL; }

    /**
     * Sets the source URL.
     */
    protected void setSourceURL(WebURL aSourceURL)
    {
        _sourceURL = aSourceURL;

        readFileFromURL(_sourceURL);
    }

    /**
     * Returns the sections.
     */
    public HelpSection[] getSections()  { return _sections; }

    /**
     * Sets the sections.
     */
    public void setSections(HelpSection[] theSections)
    {
        _sections = theSections;
    }

    /**
     * Reads the file.
     */
    protected void readFileFromURL(WebURL aURL)
    {
        // Get full text
        String text = aURL.getText();
        text = text.replace("##", "<H2>");

        // Get text for sections
        String[] sectionTexts = text.split("#");
        List<HelpSection> sections = new ArrayList<>();

        // Iterate over sectionTexts and create/add sections
        for (String sectionText : sectionTexts) {

            // Get trimmed - just skip if empty
            sectionText = sectionText.trim();
            if (sectionText.length() == 0) continue;
            sectionText = sectionText.replace("<H2>", "##");

            // Create/add section
            HelpSection section = new HelpSection(this, sectionText);
            sections.add(section);
        }

        // Set sections and return
        setSections(sections.toArray(new HelpSection[0]));
    }

    /**
     * Returns the section header style.
     */
    public TextStyle getSectionHeaderStyle()
    {
        // If already set, just return
        if (_sectionHeaderStyle != null) return _sectionHeaderStyle;

        // Create, configure
        TextStyle textStyle = TextStyle.DEFAULT;
        Font headerFont = Font.Arial16.getBold();
        Color headerColor = new Color(.5d, .5d, 1d);
        TextStyle headerStyle = textStyle.copyFor(headerFont, headerColor);

        // Set, return
        return _sectionHeaderStyle = headerStyle;
    }

    /**
     * Returns the section content style.
     */
    public TextStyle getSectionContentStyle()
    {
        // If already set, just return
        if (_sectionContentStyle != null) return _sectionContentStyle;

        // Create, configure
        TextStyle textStyle = TextStyle.DEFAULT;
        Font contentFont = Font.Arial12.getBold();
        Color contentColor = Color.BLACK;
        TextStyle contentStyle = textStyle.copyFor(contentFont, contentColor);

        // Set, return
        return _sectionContentStyle = contentStyle;
    }
}
