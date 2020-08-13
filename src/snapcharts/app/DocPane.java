package snapcharts.app;
import snap.gfx.Image;
import snap.util.SnapUtils;
import snap.view.*;
import snap.viewx.DialogBox;
import snap.viewx.FilePanel;
import snap.viewx.RecentFiles;
import snap.viewx.TextPane;
import snap.web.WebFile;
import snap.web.WebURL;
import snapcharts.model.*;

/**
 * A class to manage charts/data in a ChartBook.
 */
public class DocPane extends ViewOwner {

    // The ChartDoc
    private Doc _doc;

    // The selected DocItem
    private DocItem _selItem;

    // The DocMenuBar
    private DocPaneMenuBar  _menuBar;

    // The SplitView
    private SplitView _splitView;

    // The ChartDoc tree
    private TreeView<DocItem>  _treeView;

    // A helper class for Copy/Paste functionality
    private DocPaneCopyPaster  _copyPaster;

    // Constants
    public static final String RECENT_FILES_ID = "RecentChartDocs";

    // Constants for actions
    //public static final String New_Action = "NewAction";
    //public static final String NewChart_Action = "NewChartAction";

    /**
     * Constructor.
     */
    public DocPane()
    {
        super();
        //_editorPane = new EditorPane();
    }

    /**
     * Returns the source URL.
     */
    public WebURL getSourceURL()
    {
        return getDoc().getSourceURL();
    }

    /**
     * Returns the doc.
     */
    public Doc getDoc()  { return _doc; }

    /**
     * Sets the doc.
     */
    public void setDoc(Doc aDoc)
    {
        // Set Doc
        _doc = aDoc;

        // Load UI
        getUI();

        // Set Doc as SelItem
        setSelItem(_doc);
    }

    /**
     * Returns the selected doc item.
     */
    public DocItem getSelItem()  { return _selItem; }

    /**
     * Sets the selected doc item.
     */
    public void setSelItem(DocItem aCP)
    {
        // If already set, just return
        if (aCP== getSelItem()) return;

        // Remove old SelItem UI
        if (_selItem !=null) {
            ViewOwner itemUI = _selItem.getItemPane();
            _splitView.removeItem(itemUI.getUI());
        }

        // Set SelItem
        _selItem = aCP;

        // Install new sel item UI
        if (_selItem !=null) {
            DocItemPane itemPane = _selItem.getItemPane();
            itemPane.setDocPane(this);
            itemPane.getUI().setGrowWidth(true);
            _splitView.addItem(itemPane.getUI());
        }

        // Reset UI
        resetLater();
    }

    /**
     * Returns the ItemPane for SelItem.
     */
    public DocItemPane getSelItemPane()
    {
        DocItem selItem = getSelItem();
        return selItem.getItemPane();
    }

    /**
     * Adds a chart part.
     */
    public void addChartPart(ChartPart aPart)
    {
        // Get current DocItem
        DocItem selItem = getSelItem();
        if (selItem != null) {
            DocItem item = selItem.addChartPart(aPart, null);
            setSelItem(item);
        }
    }

    /**
     * Returns the SwingOwner for the menu bar.
     */
    public DocPaneMenuBar getMenuBar()
    {
        if (_menuBar!=null) return _menuBar;
        return _menuBar = new DocPaneMenuBar(this);
    }

    /**
     * Creates a new default editor pane.
     */
    public DocPane newDoc()
    {
        // Crummy sample
        if (ViewUtils.isAltDown()) {
            //WebURL url = WebURL.getURL(App.class, "Sample.json");
            WebURL url = WebURL.getURL("/Users/jeff/Samples/Charts/Sample.json");
            Doc doc = Doc.createDocFromSource(url);
            setDoc(doc);
        }

        // Create/set empty doc
        else {
            Doc doc = new Doc();
            doc.setName("Untitled");
            setDoc(doc);
        }

        return this;
    }

    /**
     * Creates a new editor window from an open panel.
     */
    public DocPane showOpenPanel(View aView)
    {
        // Get path from open panel for supported file extensions
        String path = FilePanel.showOpenPanel(aView, "Snap Charts File", "charts");
        return open(path);
    }

    /**
     * Creates a new editor window by opening the document from the given source.
     */
    public DocPane open(Object aSource)
    {
        // If source is already opened, return editor pane
        WebURL url = WebURL.getURL(aSource);

        Doc doc = Doc.createDocFromSource(url);

        setDoc(doc);

        // If source is string, add to recent files menu
        String urls = url!=null ? url.getString() : null;
        if(urls!=null)
            RecentFiles.addPath(RECENT_FILES_ID, urls, 10);

        // Return the editor
        return this;
    }

    /**
     * Saves the current editor document, running the save panel.
     */
    public void saveAs()
    {
        // Run save panel, set Document.Source to path and re-save (or just return if cancelled)
        String exts[] = new String[] { "charts" };
        String path = FilePanel.showSavePanel(getUI(), "Snap Charts File", exts); if (path==null) return;
        getDoc().setSourceURL(WebURL.getURL(path));
        save();
    }

    /**
     * Saves the current editor document, running the save panel if needed.
     */
    public void save()
    {
        // If can't save to current source, do SaveAs instead
        WebURL url = getSourceURL(); if (url==null) { saveAs(); return; }

        // Make sure editor isn't previewing and has focus (to commit any inspector textfield changes)
        //getEditor().requestFocus();

        // Do actual save - if exception, print stack trace and set error string
        try { saveImpl(); }
        catch(Throwable e) {
            e.printStackTrace();
            String msg = "The file " + url.getPath() + " could not be saved (" + e + ").";
            DialogBox dbox = new DialogBox("Error on Save"); dbox.setErrorMessage(msg);
            dbox.showMessageDialog(getUI());
            return;
        }

        // Add URL.String to RecentFilesMenu, clear undoer and reset UI
        String urls = url.getString();
        RecentFiles.addPath(RECENT_FILES_ID, urls, 10);
        //getDoc().getUndoer().reset();
        resetLater();
    }

    /**
     * The real save method.
     */
    protected void saveImpl()
    {
        WebURL url = getSourceURL();
        WebFile file = url.getFile();
        if (file==null) file = url.createFile(false);
        file.setBytes(getDoc().getChartsFileXMLBytes());
        file.save();
    }

    /**
     * Reloads the current editor document from the last saved version.
     */
    public void revert()
    {
        // Get filename (just return if null)
        WebURL surl = getSourceURL(); if (surl==null) return;

        // Run option panel for revert confirmation (just return if denied)
        String msg = "Revert to saved version of " + surl.getPathName() + "?";
        DialogBox dbox = new DialogBox("Revert to Saved");
        dbox.setQuestionMessage(msg);
        if (!dbox.showConfirmDialog(getUI())) return;

        // Re-open filename
        getSourceURL().getFile().reload();
        open(getSourceURL());
    }

    /**
     * Closes this editor pane
     */
    public void close()
    {
        getWindow().hide();
        docPaneClosed();
    }

    /**
     * Called when the app is about to exit to gracefully handle any open documents.
     */
    public void quit()  { App.quitApp(); }

    /**
     * Returns the CopyPaster.
     */
    public DocPaneCopyPaster getCopyPaster()
    {
        if (_copyPaster!=null) return _copyPaster;
        return _copyPaster = new DocPaneCopyPaster(this);
    }

    /**
     * Standard clipboard cut.
     */
    public void cut()  { getCopyPaster().cut(); }

    /**
     * Standard clipboard copy.
     */
    public void copy()  { getCopyPaster().copy(); }

    /**
     * Standard clipboard paste.
     */
    public void paste()  { getCopyPaster().paste(); }

    /**
     * Standard clipboard delete.
     */
    public void delete()  { getCopyPaster().delete(); }

    /**
     * Standard selectAll.
     */
    public void selectAll()  { getCopyPaster().selectAll(); }

    /**
     * Called when a DocItem changes name.
     */
    public void docItemNameChanged()
    {
        _treeView.updateItems(getSelItem());
    }

    /**
     * Called when DocPane is closed.
     */
    protected void docPaneClosed()
    {
        // If another open editor is available focus on it, otherwise run WelcomePanel
        DocPane dpane = WindowView.getOpenWindowOwner(DocPane.class);
        //if (dpane!=null) dpane.getEditor().requestFocus(); else
         if (dpane==null)
             WelcomePanel.getShared().showPanel();
    }

    /**
     * Returns the window title.
     */
    public String getWindowTitle()
    {
        // Get window title: Basic filename + optional "Doc edited asterisk + optional "Doc Scaled"
        String title = getSourceURL()!=null ? getSourceURL().getPath() : null;
        if (title==null) title = "Untitled";

        // If has undos, add asterisk
        //if (getEditor().getUndoer()!=null && getEditor().getUndoer().hasUndos()) title = "* " + title;
        return title;
    }

    @Override
    protected View createUI()
    {
        // Do normal version
        View topView = super.createUI();

        // Create ColView holding MenuBar and EditorPane UI (with key listener so MenuBar catches shortcut keys)
        return MenuBar.createMenuBarView(getMenuBar().getUI(), topView);
    }

    /**
     * InitUI.
     */
    @Override
    protected void initUI()
    {
        // Get/configure SplitView
        _splitView = getView("SplitView", SplitView.class);
        _splitView.setDividerSpan(5);
        _splitView.removeItem(1);

        // Set Toolbar images
        getView("SaveButton", ButtonBase.class).setImage(Image.get(TextPane.class, "pkg.images/File_Save.png"));
        getView("CutButton", ButtonBase.class).setImage(Image.get(TextPane.class, "pkg.images/Edit_Cut.png"));
        getView("CopyButton", ButtonBase.class).setImage(Image.get(TextPane.class, "pkg.images/Edit_Copy.png"));
        getView("PasteButton", ButtonBase.class).setImage(Image.get(TextPane.class, "pkg.images/Edit_Paste.png"));
        getView("DeleteButton", ButtonBase.class).setImage(Image.get(TextPane.class, "pkg.images/Edit_Delete.png"));
        getView("UndoButton", ButtonBase.class).setImage(Image.get(TextPane.class, "pkg.images/Edit_Undo.png"));
        getView("RedoButton", ButtonBase.class).setImage(Image.get(TextPane.class, "pkg.images/Edit_Redo.png"));

        // Get/configure TreeView
        _treeView = getView("TreeView", TreeView.class);
        _treeView.setResolver(new ChartDocTreeResolver());

        // Configure window
        WindowView win = getWindow();
        enableEvents(win, WinClose);
    }

    /**
     * Reset UI.
     */
    @Override
    protected void resetUI()
    {
        _treeView.setItems(getDoc());
        _treeView.expandAll();
        _treeView.setSelItem(getSelItem());

        // If title has changed, update window title
        if(isWindowVisible()) {
            String title = getWindowTitle();
            WindowView win = getWindow();
            if(!SnapUtils.equals(title, win.getTitle())) {
                win.setTitle(title);
                win.setDocURL(getSourceURL());
            }
        }
    }

    /**
     * Respond to UI.
     */
    protected void respondUI(ViewEvent anEvent)
    {
        // Handle TreeView
        if (anEvent.equals(_treeView)) {
            DocItem docItem = _treeView.getSelItem();
            setSelItem(docItem);
        }

        // Handle WinClosing
        if (anEvent.isWinClose()) {
            close(); anEvent.consume(); }

        // Handle NewAction
        if (anEvent.equals("AddButton")) {
            respondToNewAction();
            //getSelItemPane().sendEvent(New_Action);
        }
    }

    /**
     * Responds to New_Action.
     */
    private void respondToNewAction()
    {
        DialogBox dbox = new DialogBox("New Document Item");
        dbox.setMessage("Select new item type:");
        dbox.setOptions("New Chart", "New Dataset");
        int resp = dbox.showOptionDialog(getUI(), "New Chart");

        // Handle New Chart
        if (resp==0) {

            // Create new chart
            Chart chart = new Chart();
            chart.setName("Untitled");

            // Add to SelItem
            DocItem selItem = getSelItem();
            DocItem newChartItem = selItem.addChartPart(chart, null);
            setSelItem(newChartItem);
        }

        // Handle Add new DataSet
        else if (resp==1) {

            // Create new dataset
            DataSet dset = new DataSet();
            dset.setName("New Dataset");

            // Add to SelItem
            DocItem selItem = getSelItem();
            DocItem newDataSetItem = selItem.addChartPart(dset, null);
            setSelItem(newDataSetItem);
        }
    }

    /**
     * A TreeResolver for Document Shapes.
     */
    private static class ChartDocTreeResolver extends TreeResolver <DocItem> {

        @Override
        public DocItem getParent(DocItem anItem)
        {
            return anItem.getParent();
        }

        @Override
        public boolean isParent(DocItem anItem)
        {
            return anItem.isParent();
        }

        @Override
        public DocItem[] getChildren(DocItem aParent)
        {
            return aParent.getItems().toArray(new DocItem[0]);
        }

        @Override
        public String getText(DocItem anItem)
        {
            return anItem.getName();
        }
    }
}