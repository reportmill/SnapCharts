package snapcharts.app;
import snap.gfx.Image;
import snap.view.*;
import snap.viewx.FilePanel;
import snap.viewx.TextPane;
import snap.web.WebURL;
import snapcharts.model.Chart;
import snapcharts.model.ChartDoc;
import snapcharts.model.ChartPart;
import snapcharts.model.DataSet;

import java.util.HashMap;
import java.util.Map;

/**
 * A class to manage charts/data in a ChartBook.
 */
public class DocPane extends ViewOwner {

    // The ChartDoc
    private ChartDoc  _doc;

    // The selected ChartPart
    private ChartPart  _selPart;

    // A map of editors for ChartParts
    private Map<ChartPart,ViewOwner>  _editors = new HashMap<>();

    // The EditorPane
    //private EditorPane _editorPane;

    // The DocMenuBar
    private DocPaneMenuBar  _menuBar;

    // The SplitView
    private SplitView _splitView;

    // The ChartDoc tree
    private TreeView<ChartPart>  _treeView;

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
            ViewOwner partUI = getEditorForChartPart(_selPart);
            _splitView.removeItem(partUI.getUI());
        }

        // Set sel part
        _selPart = aCP;

        // Install new sel part UI
        if (_selPart!=null) {
            ViewOwner partUI = getEditorForChartPart(_selPart);
            partUI.getUI().setGrowWidth(true);
            _splitView.addItem(partUI.getUI());
        }

        // Reset UI
        resetLater();
    }

    /**
     * Returns the editor for a chart part.
     */
    public ViewOwner getEditorForChartPart(ChartPart aChartPart)
    {
        ViewOwner partEditor = _editors.get(aChartPart);
        if (partEditor==null) {
            partEditor = createEditorForChartPart(aChartPart);
            _editors.put(aChartPart, partEditor);
        }
        return partEditor;
    }

    /**
     * Creates an editor for ChartPart.
     */
    protected ViewOwner createEditorForChartPart(ChartPart aChartPart)
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
        //if(url!=null) RecentFilesPanel.addRecentFile(url.getString());

        // Return the editor
        return this;
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