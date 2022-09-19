/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.notebook;
import snap.gfx.Image;
import snap.text.TextDoc;
import snap.view.*;
import snap.viewx.TextPane;
import snap.web.WebURL;

/**
 * This class shows a help file for notebooks.
 */
public class HelpPane extends ViewOwner {

    // The NotebookPane
    private NotebookPane  _notebookPane;

    // The HelpFile
    private HelpFile  _helpFile;

    // The selected section
    private HelpSection  _selSection;

    // The section TextArea
    private TextArea  _sectionTextArea;

    /**
     * Constructor.
     */
    public HelpPane(NotebookPane aNP)
    {
        super();
        _notebookPane = aNP;

        // Set HelpFile
        WebURL helpFileURL = WebURL.getURL(getClass(), "HelpFile.md");
        HelpFile helpFile = new HelpFile(helpFileURL);
        setHelpFile(helpFile);
    }

    /**
     * Returns the HelpFile.
     */
    public HelpFile getHelpFile()  { return _helpFile; }

    /**
     * Sets the HelpFile.
     */
    public void setHelpFile(HelpFile aHelpFile)
    {
        _helpFile = aHelpFile;
    }

    /**
     * Returns the selected section.
     */
    public HelpSection getSelSection()  { return _selSection; }

    /**
     * Sets the selected section.
     */
    public void setSelSection(HelpSection aSection)
    {
        // If already set, just return
        if (aSection == getSelSection()) return;

        // Set SelSection
        _selSection = aSection;

        // Update SectionTextArea
        TextDoc sectionText = aSection.getTextDoc();
        _sectionTextArea.setTextDoc(sectionText);
    }

    /**
     * Initialize UI.
     */
    @Override
    protected void initUI()
    {
        // Get/configure SearchText: radius, prompt, image, animation
        TextField searchText = getView("SearchTextField", TextField.class);
        searchText.getLabel().setImage(Image.get(TextPane.class, "Find.png"));
        TextField.setBackLabelAlignAnimatedOnFocused(searchText, true);
        //searchText.addEventFilter(e -> ViewUtils.runLater(() -> textFieldKeyTyped(e)), KeyPress);

        // Get TopicListArea and configure
        ListView<HelpSection> topicListView = getView("TopicListView", ListView.class);
        topicListView.setFocusWhenPressed(false);
        ListArea<HelpSection> topicListArea = topicListView.getListArea();
        topicListArea.setName("TopicListArea");
        topicListArea.setFocusWhenPressed(false);
        topicListArea.setItemTextFunction(helpSect -> helpSect.getHeader());

        // Get SectionTextArea
        TextView sectionTextView = getView("SectionTextView", TextView.class);
        _sectionTextArea = sectionTextView.getTextArea();
        _sectionTextArea.setPadding(8, 8, 8, 8);

        // Get HelpSections and set in TopicListArea
        HelpFile helpFile = getHelpFile();
        HelpSection[] sections = helpFile.getSections();
        topicListArea.setItems(sections);
    }

    /**
     * Override to select first item when showing.
     */
    @Override
    protected void initShowing()
    {
        HelpFile helpFile = getHelpFile();
        HelpSection firstSection = helpFile.getSections()[0];
        setSelSection(firstSection);
    }

    /**
     * Reset UI.
     */
    @Override
    protected void resetUI()
    {
        // Update TopicListArea
        ListArea<HelpSection> topicListArea = getView("TopicListArea", ListArea.class);
        if (topicListArea.getSelItem() != getSelSection())
            topicListArea.setSelItem(getSelSection());
    }

    /**
     * Respond UI.
     */
    @Override
    protected void respondUI(ViewEvent anEvent)
    {
        // Handle TopicListArea
        if (anEvent.equals("TopicListArea")) {
            HelpSection section = (HelpSection) anEvent.getSelItem();
            setSelSection(section);
        }

        // Handle AddCodeButton
        if (anEvent.equals("AddCodeButton"))
            addHelpCodeToNotebook();
    }

    /**
     * Finds help code in current help file and sends to Notebook.
     */
    private void addHelpCodeToNotebook()
    {
        String helpCode = getHelpCode();
        if (helpCode != null)
            _notebookPane.addHelpCode(helpCode);
    }

    /**
     * Returns help code.
     */
    private String getHelpCode()
    {
        HelpSection selSection = getSelSection();
        String content = selSection.getContent();
        int startIndex = content.indexOf(MarkDownDoc.CODE_MARKER);
        if (startIndex < 0)
            return null;

        startIndex += MarkDownDoc.CODE_MARKER.length();
        int endIndex = content.indexOf(MarkDownDoc.CODE_MARKER, startIndex);
        if (endIndex < 0)
            return null;

        String code = content.substring(startIndex, endIndex);
        return code;
    }
}
