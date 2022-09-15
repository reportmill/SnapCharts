/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.notebook;
import snap.gfx.Color;
import snap.gfx.Font;
import snap.text.RichText;
import snap.text.TextLine;
import snap.text.TextStyle;

/**
 * This RichText subclass can be created with MarkDown.
 */
public class MarkDownDoc extends RichText {

    // The header 1 style
    private TextStyle  _header1Style;

    // The header 2 style
    private TextStyle  _header2Style;

    // The content style
    private TextStyle  _contentStyle;

    // The code style
    private TextStyle  _codeStyle;

    // Constants
    private static final String CODE_MARKER = "```";

    /**
     * Constructor.
     */
    public MarkDownDoc()
    {
        super();
    }

    /**
     * Sets MarkDown.
     */
    public void setMarkDown(String markDown)
    {
        setDefaultStyle(getContentStyle());

        setString(markDown);

        formatHeader(2);
        formatHeader(1);
        formatCode();
    }

    /**
     * Format headers.
     */
    protected void formatHeader(int aLevel)
    {
        // Get Header marker
        String headerMarker = aLevel == 2 ? "##" : "#";

        // Get index of head
        int markerIndex = indexOf(headerMarker, 0);
        while (markerIndex >= 0) {

            // Get marker end
            int markerEndIndex = markerIndex + 1;
            for (char ch = charAt(markerEndIndex); ch == '#' || Character.isWhitespace(ch); ) {
                markerEndIndex++;
                ch = charAt(markerEndIndex);
            }

            // Remove marker
            removeChars(markerIndex, markerEndIndex);

            // Get Header line start/end
            TextLine textLine = getLineForCharIndex(markerIndex);
            int lineStartCharIndex = textLine.getStart();
            int lineEndCharIndex = textLine.getEnd();

            // Get header style
            TextStyle headerStyle = aLevel == 1 ? getHeader1Style() : getHeader2Style();
            setStyle(headerStyle, lineStartCharIndex, lineEndCharIndex);

            // Get next marker
            markerIndex = indexOf(headerMarker, lineEndCharIndex);
        }
    }

    /**
     * Format code.
     */
    protected void formatCode()
    {
        // Replace code style
        int codeIndex = indexOf(CODE_MARKER, 0);
        while (codeIndex >= 0) {

            // Find end marker
            int codeMarkerLength = CODE_MARKER.length();
            int codeEndIndex = indexOf(CODE_MARKER, codeIndex + codeMarkerLength);
            if (codeEndIndex < 0)
                break;

            // Change font to codeStyle
            TextStyle codeStyle = getCodeStyle();
            setStyle(codeStyle, codeIndex + codeMarkerLength, codeEndIndex);

            // Remove end code marker
            TextLine endLine = getLineForCharIndex(codeEndIndex);
            removeChars(endLine.getStart(), endLine.getEnd());

            // Remove start code marker
            TextLine startLine = getLineForCharIndex(codeIndex);
            removeChars(startLine.getStart(), startLine.getEnd());

            // Look for next block
            codeIndex = indexOf(CODE_MARKER, codeIndex);
        }
    }

    /**
     * Returns the header 1 style.
     */
    public TextStyle getHeader1Style()
    {
        // If already set, just return
        if (_header1Style != null) return _header1Style;

        // Create, configure
        TextStyle textStyle = TextStyle.DEFAULT;
        Font headerFont = Font.Arial16.deriveFont(24).getBold();
        Color headerColor = new Color(.5d, .5d, 1d);
        TextStyle headerStyle = textStyle.copyFor(headerFont, headerColor);

        // Set, return
        return _header1Style = headerStyle;
    }

    /**
     * Returns the header 2 style.
     */
    public TextStyle getHeader2Style()
    {
        // If already set, just return
        if (_header2Style != null) return _header2Style;

        // Create, configure
        TextStyle textStyle = TextStyle.DEFAULT;
        Font headerFont = Font.Arial16.getBold();;
        Color headerColor = Color.BLACK;
        TextStyle headerStyle = textStyle.copyFor(headerFont, headerColor);

        // Set, return
        return _header2Style = headerStyle;
    }

    /**
     * Returns the content style.
     */
    public TextStyle getContentStyle()
    {
        // If already set, just return
        if (_contentStyle != null) return _contentStyle;

        // Create, configure
        TextStyle textStyle = TextStyle.DEFAULT;
        Font contentFont = Font.Arial14;
        Color contentColor = Color.BLACK;
        TextStyle contentStyle = textStyle.copyFor(contentFont, contentColor);

        // Set, return
        return _contentStyle = contentStyle;
    }

    /**
     * Returns the code style.
     */
    public TextStyle getCodeStyle()
    {
        // If already set, just return
        if (_codeStyle != null) return _codeStyle;

        // Get code font
        Font codeFont = null;
        String[] names = { "Monaco", "Consolas", "Courier" };
        for (String name : names) {
            codeFont = new Font(name, 14);
            if (codeFont.getFamily().startsWith(name))
                break;
        }

        // Create, configure
        TextStyle textStyle = TextStyle.DEFAULT;
        Color codeColor = Color.BLACK;
        TextStyle codeStyle = textStyle.copyFor(codeFont, codeColor);

        // Set, return
        return _codeStyle = codeStyle;
    }
}
