/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.notebook;
import snap.gfx.Color;
import snap.view.*;

import java.util.List;

/**
 * This class provides UI and editing for a notebook.
 */
public class NotebookPane extends ViewOwner {

    // The Notebook
    private Notebook  _notebook;

    // The NotebookView
    private NotebookView  _notebookView;

    // The HelpPane
    private HelpPane  _helpPane;

    // Constants
    public static Color BACK_FILL = Color.WHITE;

    /**
     * Returns the Notebook.
     */
    public Notebook getNotebook()
    {
        if (_notebook != null) return _notebook;
        Notebook notebook = new Notebook();
        return _notebook = notebook;
    }

    /**
     * Sets the Notebook.
     */
    public void setNotebook(Notebook aNotebook)
    {
        _notebook = aNotebook;

        if (_notebookView != null) {
            _notebookView.setNotebook(aNotebook);
            _notebookView.resetEntriesLater();
        }
    }

    /**
     * Returns the HelpPane.
     */
    public HelpPane getHelpPane()
    {
        // If already set, just return
        if (_helpPane != null) return _helpPane;

        // Create, set, return
        HelpPane helpPane = new HelpPane(this);
        return _helpPane = helpPane;
    }

    /**
     * Shows the HelpPane.
     */
    public void showHelpPane()
    {
        // Get HelpPane (just return if already showing)
        HelpPane helpPane = getHelpPane();
        if (helpPane.isShowing())
            return;

        // Set UI.PrefSize
        View helpPaneUI = _helpPane.getUI();
        helpPaneUI.setPrefHeight(280);

        // Add to MainSplitView
        SplitView mainSplitView = getView("MainSplitView", SplitView.class);
        mainSplitView.addItemWithAnim(helpPaneUI, 280);
    }

    /**
     * Hides the help pane.
     */
    public void hideHelpPane()
    {
        View helpPaneUI = _helpPane.getUI();
        SplitView mainSplitView = getView("MainSplitView", SplitView.class);
        mainSplitView.removeItemWithAnim(helpPaneUI);
    }

    /**
     * Shows the HelpPane after loaded.
     */
    public void showHelpPaneWhenLoaded()
    {
        Runnable run = () -> {
            HelpPane helpPane = getHelpPane();
            View helpPaneUI = helpPane.getUI();
            helpPaneUI.addPropChangeListener(pc -> resetLater(), View.Showing_Prop);
            ViewUtils.runLater(() -> showHelpPane());
        };
        new Thread(run).start();
    }

    /**
     * Initialize UI.
     */
    @Override
    protected void initUI()
    {
        // Configure TopView
        View topView = getUI();
        topView.setFill(BACK_FILL);

        // Get/configure SplitView
        SplitView mainSplitView = getView("MainSplitView", SplitView.class);
        mainSplitView.setDividerSpan(5);
        mainSplitView.setBorder(null);

        // Get/configure ToolBar
        View toolBar = getView("ToolBar");
        toolBar.setFill(new Color("#FBFBFBF0"));
        toolBar.setBorder(new Color("#E0"), 1);

        // Create NotebookView
        Notebook notebook = getNotebook();
        _notebookView = new NotebookView();
        _notebookView.setGrowHeight(true);
        _notebookView.setNotebook(notebook);

        // Get NotebookScrollView and add NotebookView
        ScrollView notebookScrollView = getView("NotebookScrollView", ScrollView.class);
        notebookScrollView.setBorder(null);
        notebookScrollView.setContent(_notebookView);

        // Add EscapeAction
        addKeyActionFilter("EscapeAction", "ESCAPE");

        // Load HelpPane in background and show
        showHelpPaneWhenLoaded();
    }

    /**
     * Called when first showing.
     */
    @Override
    protected void initShowing()
    {
        _notebookView.resetEntriesLater();
    }

    /**
     * Reset UI.
     */
    @Override
    protected void resetUI()
    {
        // Update ShowHelpButton.Text
        boolean isHelpPaneShowing = _helpPane != null && _helpPane.isShowing();
        String showHelpButtonTitle = isHelpPaneShowing ? "Hide Help" : "Show Help";
        setViewText("ShowHelpButton", showHelpButtonTitle);
    }

    /**
     * Respond to UI.
     */
    @Override
    protected void respondUI(ViewEvent anEvent)
    {
        // Handle ShowHelpButton
        if (anEvent.equals("ShowHelpButton")) {
            if (_helpPane != null && _helpPane.isShowing())
                hideHelpPane();
            else showHelpPaneWhenLoaded();
        }

        // Handle EscapeAction
        if (anEvent.equals("EscapeAction"))
            handleEscapeAction(anEvent);
    }

    /**
     * Escape out of current request editing.
     */
    protected void handleEscapeAction(ViewEvent anEvent)
    {
        // Get active RequestView (just return if none)
        JavaEntryView javaEntryView = _notebookView.getActiveEntryView();
        if (javaEntryView == null) {
            _notebookView.focusLastEntry();
            return;
        }

        // Forward to RequestView - just return if handled
        javaEntryView.handleEscapeAction(anEvent);
        //if (anEvent.isConsumed())
        //    return;

        // If no requests, just return
        //if (_notebook.getEntries().size() == 1) {
        //    beep(); return;
        //}

        // Otherwise remove current request and select previous
        //JavaEntry javaEntry = javaEntryView.getEntry();
        //_notebook.removeEntry(javaEntry);
    }

    public void addHelpCode(String aString)
    {
        // Get last entry/entryView
        List<JavaEntry> javaEntries = _notebook.getEntries();
        JavaEntry lastEntry = javaEntries.get(javaEntries.size() - 1);
        JavaEntryView entryView = _notebookView.getEntryView(lastEntry);

        // Add help code to EntryView.TextArea
        TextArea textArea = entryView.getTextArea();
        textArea.setSel(0, textArea.length());
        textArea.replaceCharsWithContent(aString);

        // Submit entry
        entryView.submitEntry();
    }

    /**
     * Standard main method.
     */
    public static void main(String[] args)
    {
        NotebookPane notebookPane = new NotebookPane();
        notebookPane.getUI().setPrefSize(800, 800);
        notebookPane.setWindowVisible(true);
    }
}
