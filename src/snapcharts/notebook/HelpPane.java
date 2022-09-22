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

    // The TextArea showing the help text
    private TextArea  _helpTextArea;

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
        TextDoc sectionText = aSection.getMarkDownDoc();
        _helpTextArea.setTextDoc(sectionText);
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
        TextView helpTextView = getView("HelpTextView", TextView.class);
        _helpTextArea = helpTextView.getTextArea();
        _helpTextArea.setPadding(8, 8, 8, 8);

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
        // Get current section and MarkDown doc
        HelpSection selSection = getSelSection();
        MarkDownDoc markDown = selSection.getMarkDownDoc();

        // Get selection char index from SectionTextArea
        int selStart = _helpTextArea.getSelStart();
        int selEnd = _helpTextArea.getSelEnd();
        int selCharIndex = (selStart + selEnd) / 2;

        // Get the code for selection char index
        MarkDownDoc.MarkDownRun codeRun = markDown.getCodeRunForCharIndex(selCharIndex);
        if (codeRun == null)
            return null;

        // Get code string and return
        String helpStr = markDown.subSequence(codeRun.startCharIndex, codeRun.endCharIndex).toString();
        return helpStr;
    }
}
