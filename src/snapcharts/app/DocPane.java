package snapcharts.app;
import snap.gfx.Image;
import snap.view.*;
import snap.viewx.TextPane;
import snap.web.WebURL;
import snapcharts.model.Chart;
import snapcharts.model.ChartDoc;
import snapcharts.model.ChartPart;

import java.util.Arrays;

/**
 * A class to manage charts/data in a ChartBook.
 */
public class DocPane extends ViewOwner {

    // The ChartDoc
    private ChartDoc  _doc;

    // The EditorPane
    private EditorPane _editorPane;

    // The DocMenuBar
    private DocPaneMenuBar  _menuBar;

    // The ChartDoc tree
    private TreeView  _treeView;

    /**
     * Constructor.
     */
    public DocPane()
    {
        _editorPane = new EditorPane();
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

        // Set First chart in editor
        Chart chart = _doc.getChartCount()>0 ? _doc.getChart(0) : null;
        if (chart!=null) {
            getEditorPane().setChart(chart);
        }

        resetLater();
    }

    /**
     * Returns the EditorPane.
     */
    public EditorPane getEditorPane()  { return _editorPane; }

    /**
     * Returns the SwingOwner for the menu bar.
     */
    public DocPaneMenuBar getMenuBar()
    {
        if (_menuBar!=null) return _menuBar;
        return _menuBar = new DocPaneMenuBar(this);
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
        SplitView splitView = getView("SplitView", SplitView.class);
        splitView.setDividerSpan(5);
        splitView.addItem(_editorPane.getUI());

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
    }

    /**
     * Reset UI.
     */
    @Override
    protected void resetUI()
    {
        _treeView.setItems(getDoc());
        _treeView.expandAll();
    }

    /**
     * Respond to UI.
     */
    protected void respondUI(ViewEvent anEvent)
    {
        System.out.println("DocPane.respondUI: " + anEvent);
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
    public class ChartDocTreeResolver extends TreeResolver <ChartPart> {

        @Override
        public ChartPart getParent(ChartPart anItem)
        {
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
                return Arrays.asList(aParent.getDataSet()).toArray(new ChartPart[0]);
            return new ChartPart[0];
        }

        @Override
        public String getText(ChartPart anItem)
        {
            return anItem.getName();
        }
    }

    }