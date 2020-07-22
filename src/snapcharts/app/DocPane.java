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
import snapcharts.model.Chart;
import snapcharts.model.ChartDoc;
import snapcharts.model.ChartPart;
import snapcharts.model.DataSet;
import java.util.*;

/**
 * A class to manage charts/data in a ChartBook.
 */
public class DocPane extends ViewOwner {

    // The ChartDoc
    private ChartDoc  _doc;

    // The selected ChartPart
    private ChartPart  _selPart;

    // A map of PartPanes for ChartParts
    private Map<ChartPart,PartPane>  _editors = new HashMap<>();

    // The EditorPane
    //private EditorPane _editorPane;

    // The DocMenuBar
    private DocPaneMenuBar  _menuBar;

    // The SplitView
    private SplitView _splitView;

    // The ChartDoc tree
    private TreeView<ChartPart>  _treeView;

    // A helper class for Copy/Paste functionality
    private DocPaneCopyPaster  _copyPaster;

    // Constants
    public static final String RECENT_FILES_ID = "RecentChartDocs";

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
    public ChartDoc getDoc()  { return _doc; }

    /**
     * Sets the doc.
     */
    public void setDoc(ChartDoc aDoc)
    {
        // Set Doc
        _doc = aDoc;

        // Load UI
        getUI();

        // Set Doc as SelPart
        setSelPart(_doc);
    }

    /**
     * Returns the selected chart part.
     */
    public ChartPart getSelPart()  { return _selPart; }

    /**
     * Sets the selected chart part.
     */
    public void setSelPart(ChartPart aCP)
    {
        // If already set, just return
        if (aCP==getSelPart()) return;

        // Remove old sel part UI
        if (_selPart!=null) {
            ViewOwner partUI = getPartPaneForChartPart(_selPart);
            _splitView.removeItem(partUI.getUI());
        }

        // Set sel part
        _selPart = aCP;

        // Install new sel part UI
        if (_selPart!=null) {
            ViewOwner partUI = getPartPaneForChartPart(_selPart);
            partUI.getUI().setGrowWidth(true);
            _splitView.addItem(partUI.getUI());
        }

        // Reset UI
        resetLater();
    }

    /**
     * Returns the PartPane for SelPart.
     */
    public PartPane getSelPartPane()
    {
        ChartPart selPart = getSelPart();
        return getPartPaneForChartPart(selPart);
    }

    /**
     * Adds a chart part.
     */
    public void addChartPart(ChartPart aPart)
    {
        // Get current SelPart
        ChartPart selPart = getSelPart();

        // Handle Chart
        if (aPart instanceof Chart) { Chart chart = (Chart)aPart;
            ChartDoc doc = selPart.getDoc();
            doc.addChart(chart);
            setSelPart(chart);
        }

        // Handle DataSet
        else if (aPart instanceof DataSet) { DataSet dset = (DataSet) aPart;
            Chart chart = selPart.getChart();
            if (chart!=null) {
                chart.addDataSet(dset);
                setSelPart(dset);
            }
        }
    }

    /**
     * Returns the editor for a chart part.
     */
    public PartPane getPartPaneForChartPart(ChartPart aChartPart)
    {
        PartPane partPane = _editors.get(aChartPart);
        if (partPane==null) {
            partPane = createEditorForChartPart(aChartPart);
            _editors.put(aChartPart, partPane);
        }
        return partPane;
    }

    /**
     * Creates an editor for ChartPart.
     */
    protected PartPane createEditorForChartPart(ChartPart aChartPart)
    {
        // Handle Chart
        if (aChartPart instanceof Chart) {
            ChartPane epane = new ChartPane();
            epane.setChart((Chart) aChartPart);
            return epane;
        }

        // Handle DataSet
        if (aChartPart instanceof DataSet) { DataSet dset = (DataSet)aChartPart;
            DataSetPane epane = new DataSetPane();
            epane.setDataSet(dset);
            return epane;
        }

        // Handle ChartDoc
        if (aChartPart instanceof ChartDoc) { ChartDoc doc = (ChartDoc)aChartPart;
            ChartSetPane dpane = new ChartSetPane();
            dpane.setCharts(doc.getCharts());
            return dpane;
        }

        throw new RuntimeException("FilePane.createEditorForChartPart: Unknown part: " + aChartPart.getClass());
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
        loadSampleDoc();
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

        ChartDoc doc = ChartDoc.createDocFromSource(url);

        setDoc(doc);

        // If source is string, add to recent files menu
        if(url!=null) RecentFiles.addPath(RECENT_FILES_ID, url.getString(), 10);

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
        RecentFiles.addPath(RECENT_FILES_ID, url.getPath(), 10);
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
        _splitView = getView("SplitView", SplitView.class);
        _splitView.setDividerSpan(5);

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
        _treeView.setSelItem(getSelPart());
        _treeView.expandAll();

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
            ChartPart part = _treeView.getSelItem();
            setSelPart(part);
        }

        // Handle WinClosing
        if (anEvent.isWinClose()) {
            close(); anEvent.consume(); }
    }

    /**
     * Loads a sample.
     */
    protected void loadSampleDoc()
    {
        WebURL url = WebURL.getURL(App.class, "Sample.json");
        ChartDoc doc = ChartDoc.createDocFromSource(url);
        setDoc(doc);
    }

    /**
     * A TreeResolver for Document Shapes.
     */
    private static class ChartDocTreeResolver extends TreeResolver <ChartPart> {

        @Override
        public ChartPart getParent(ChartPart anItem)
        {
            if (anItem instanceof DataSet)
                return anItem.getChart();
            return anItem.getParent();
        }

        @Override
        public boolean isParent(ChartPart anItem)
        {
            return anItem instanceof Chart || anItem instanceof ChartDoc;
        }

        @Override
        public ChartPart[] getChildren(ChartPart aParent)
        {
            if (aParent instanceof ChartDoc)
                return ((ChartDoc)aParent).getCharts().toArray(new ChartPart[0]);
            if (aParent instanceof Chart)
                return aParent.getDataSetList().getDataSets().toArray(new ChartPart[0]);
            return new ChartPart[0];
        }

        @Override
        public String getText(ChartPart anItem)
        {
            return anItem.getName();
        }
    }
}