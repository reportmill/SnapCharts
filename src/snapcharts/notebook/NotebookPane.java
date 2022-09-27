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

    // The MainSplitView
    private SplitView  _mainSplitView;

    // The NotebookView
    private NotebookView  _notebookView;

    // The TabView
    private TabView  _tabView;

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
     * Shows the TabView.
     */
    public void showTabView()
    {
        // Get TabView (just return if already showing)
        if (_tabView.isShowing())
            return;

        // Add to MainSplitView
        _mainSplitView.addItemWithAnim(_tabView, 280);
    }

    /**
     * Hides the TabView.
     */
    public void hideTabView()
    {
        _mainSplitView.removeItemWithAnim(_tabView);
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

        // Make sure Help is installed
        View helpPaneUI = helpPane.getUI();
        if (helpPaneUI.getParent() == null)
            _tabView.addTab("Help", helpPaneUI, 0);

        // Show TabView
        _tabView.setSelIndex(0);
        showTabView();
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
        _mainSplitView = getView("MainSplitView", SplitView.class);
        _mainSplitView.setDividerSpan(6);
        _mainSplitView.getDivider().setFill(Color.WHITE);
        _mainSplitView.getDivider().setBorder(null);
        _mainSplitView.setBorder(null);

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
        notebookScrollView.setFillWidth(true);
        notebookScrollView.setBorder(Color.GRAY9, 1);
        notebookScrollView.setContent(_notebookView);

        // Get TabView
        _tabView = getView("TabView", TabView.class);
        _tabView.getShelf().setSpacing(6);
        _tabView.getShelf().setFill(Color.WHITE);
        _tabView.getShelf().setBorder(new Color(.95), 1);
        _tabView.getShelf().setPadding(5,5,0,15);
        _tabView.setTabMinWidth(80);
        View tabViewContextBox = _tabView.getContent().getParent();
        tabViewContextBox.setFill(Color.WHITE);
        //tabViewContextBox.setBorder(null);
        _mainSplitView.removeItem(_tabView);

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
        // Update ShowTabsButton.Text
        boolean isHelpPaneShowing = _helpPane != null && _helpPane.isShowing();
        String showHelpButtonTitle = isHelpPaneShowing ? "Hide Tabs" : "Show Tabs";
        setViewText("ShowTabsButton", showHelpButtonTitle);
    }

    /**
     * Respond to UI.
     */
    @Override
    protected void respondUI(ViewEvent anEvent)
    {
        // Handle ShowTabsButton
        if (anEvent.equals("ShowTabsButton")) {
            if (_helpPane != null && _helpPane.isShowing())
                hideTabView();
            else showTabView();
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
