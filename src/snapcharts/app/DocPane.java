/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.app;
import snap.geom.Insets;
import snap.geom.Pos;
import snap.gfx.Color;
import snap.gfx.Font;
import snap.gfx.GFXEnv;
import snap.gfx.Image;
import snap.util.*;
import snap.view.*;
import snap.viewx.*;
import snap.web.WebFile;
import snap.web.WebURL;
import snapcharts.appmisc.OpenInPlotly;
import snapcharts.appmisc.SamplesPane;
import snapcharts.doc.*;
import snapcharts.model.*;
import snapcharts.notebook.DocItemPaneNotebook;
import snapcharts.notebook.Notebook;
import snapcharts.notebook.DocItemNotebook;

import java.io.File;
import java.util.*;

/**
 * A class to manage charts/data in a ChartBook.
 */
public class DocPane extends ViewOwner {

    // The ChartDoc
    private Doc  _doc;

    // The selected DocItem
    private DocItem  _selItem;

    // The DocMenuBar
    private DocPaneMenuBar  _menuBar;

    // The SplitView
    private SplitView  _splitView;

    // The ChartDoc tree
    private TreeView<DocItem>  _treeView;

    // A helper class for Copy/Paste functionality
    private DocPaneCopyPaster  _copyPaster;

    // A map of DocItems to a DocItemPane
    private Map<DocItem,DocItemPane>  _docItemPanes = new HashMap<>();

    // Constants
    public static final String RECENT_FILES_ID = "RecentChartDocs";
    public static final String CHARTS_FILE_EXT = "charts";
    public static final String CHARTS_SIMPLE_FILE_EXT = "simple";

    /**
     * Constructor.
     */
    public DocPane()
    {
        super();
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
     * Returns whether showing sidebar.
     */
    public boolean isShowSidebar()
    {
        View sidebarView = getView("SidebarView");
        return sidebarView != null && sidebarView.isShowing();
    }

    /**
     * Sets whether showing sidebar.
     */
    public void setShowSidebar(boolean aValue)
    {
        // If value already set, just return
        if (aValue == isShowSidebar()) return;

        // Show/hide sidebarView
        View sidebarView = getView("SidebarView");
        SplitView splitView = getView("SplitView", SplitView.class);
        splitView.setItemVisibleWithAnim(sidebarView, aValue);
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
            ViewOwner itemUI = getItemPane(_selItem);
            _splitView.removeItem(itemUI.getUI());
        }

        // Set SelItem
        _selItem = aCP;

        // Install new sel item UI
        if (_selItem !=null) {
            DocItemPane itemPane = getItemPane(_selItem);
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
        return getItemPane(selItem);
    }

    /**
     * Selects the best peer for given docItem (assumes it is going away).
     */
    public void selectBestDocItemPeer(DocItem aDocItem)
    {
        // Get parent item and index of given item
        DocItem parItem = aDocItem.getParent();
        int index = aDocItem.getIndex();

        // Best peer is first available of: 1) Next index, 2) previous index or 3) parent
        DocItem nextItem = parItem;
        if (index + 1 < parItem.getItemCount())
            nextItem = parItem.getItem(index + 1);
        else if (index > 0)
            nextItem = parItem.getItem(index - 1);

        // Select best peer
        if (nextItem != null)
            setSelItem(nextItem);
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
        Doc doc = new Doc();
        doc.setName("Untitled");
        setDoc(doc);
        return this;
    }

    /**
     * Creates a new editor window from an open panel.
     */
    public DocPane showOpenPanel(View aView)
    {
        // Get path from open panel for supported file extensions
        String[] extensions = { DocPane.CHARTS_FILE_EXT, DocPane.CHARTS_SIMPLE_FILE_EXT };
        String path = FilePanel.showOpenPanel(aView, "Snap Charts File", extensions);
        return openDocFromSource(path);
    }

    /**
     * Creates a new editor window by opening the given chart doc.
     */
    public DocPane openDoc(Doc aDoc)
    {
        // Set new doc
        setDoc(aDoc);

        // If source is string, add to recent files menu
        WebURL url = aDoc.getSourceURL();
        String urls = url != null ? url.getString() : null;
        if(urls != null)
            RecentFiles.addPath(RECENT_FILES_ID, urls, 10);

        // Return the editor
        return this;
    }

    /**
     * Creates a new editor window by opening chart doc from given source.
     */
    public DocPane openDocFromSource(Object aSource)
    {
        // Get URL for source
        WebURL url = WebURL.getURL(aSource);

        // Get doc for URL
        Doc doc = Doc.createDocFromSource(url);

        // Return call to openDoc
        return openDoc(doc);
    }

    /**
     * Saves the current editor document, running the save panel.
     */
    public void saveAs()
    {
        // Run save panel, set Document.Source to path and re-save (or just return if cancelled)
        String[] exts = new String[] { CHARTS_FILE_EXT };
        //String path = FilePanel.showSavePanel(getUI(), "Snap Charts File", exts); if (path==null) return;
        WebFile file = FilePanel.showSavePanelWeb(getUI(), "Snap Charts file", exts); if (file == null) return;

        getDoc().setSourceURL(file.getURL());
        save();
    }

    /**
     * Saves the current editor document, running the save panel if needed.
     */
    public void save()
    {
        // If can't save to current source, do SaveAs instead
        WebURL url = getSourceURL(); if (url == null) { saveAs(); return; }

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
        openDocFromSource(getSourceURL());
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
     * Shows samples.
     */
    public void showSamples()
    {
        new SamplesPane().showSamples(this);
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
     * Returns the DocItemPane for given DocItem.
     */
    public DocItemPane getItemPane(DocItem anItem)
    {
        // Get DocItemPane for DocItem from DocItemPanes map (if found, just return)
        DocItemPane pane = _docItemPanes.get(anItem);
        if (pane != null)
            return pane;

        // Create pane, add to DocItemPanes map, return
        pane = createItemPane(anItem);
        _docItemPanes.put(anItem, pane);
        return pane;
    }

    /**
     * Creates a DocItemPane for given DocItem.
     */
    protected DocItemPane createItemPane(DocItem anItem)
    {
        // Handle DocItemChart
        if (anItem instanceof DocItemChart)
            return new ChartPane(anItem);

        // Handle DocItemDataSet
        if (anItem instanceof DocItemDataSet)
            return new ChartPane(anItem);

        // Handle DocItemGroup
        if (anItem instanceof DocItemGroup)
            return new ChartSetPane((DocItemGroup) anItem);

        // Handle DocItemNotebook
        if (anItem instanceof DocItemNotebook)
            return new DocItemPaneNotebook((DocItemNotebook) anItem);

        // Complain (bitterly)
        throw new RuntimeException("DocItemPane.createItemPane: Unknown item: " + anItem);
    }

    /**
     * Resets the DocItemPane for given DocItem.
     */
    protected void resetItemPane(DocItem anItem)
    {
        DocItemPane docItemPane = _docItemPanes.remove(anItem);
        if (docItemPane != null)
            runLater(() -> docItemPane.disposeDocItemPane());
    }

    /**
     * Opens current selection in Plotly.
     */
    public void openInPlotly()
    {
        // Get first non-group item
        DocItem docItem = getSelItem();
        List<Chart> charts = new ArrayList<>();
        if (docItem instanceof DocItemGroup) {
            DocItemGroup groupDocItem = (DocItemGroup) docItem;
            charts = groupDocItem.getCharts();
        }

        // If chart item, open in plotly
        else if (docItem instanceof DocItemChart) {
            DocItemChart chartDocItem = (DocItemChart) docItem;
            charts = Arrays.asList(chartDocItem.getChart());
        }

        new OpenInPlotly().openInPlotly(charts);

        // If first time, Add "Plotly" button in case they want to do it again
        if (getView("PlotlyButton") == null)
            addPlotlyButton();
    }

    /**
     * Adds the plotly button to the toolbar.
     */
    private void addPlotlyButton()
    {
        Button samplesButton = getView("SamplesButton", Button.class);
        Button plotlyButton = new ViewArchiver().copy(samplesButton);
        plotlyButton.setName("PlotlyButton");
        plotlyButton.setText("Plotly");
        samplesButton.setMargin(new Insets(0, 8, 0, 0));
        ViewUtils.addChild(samplesButton.getParent(), plotlyButton, samplesButton.indexInParent()+1);
        plotlyButton.addEventHandler(e -> openInPlotly(), Action);
    }

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
        _splitView.setDividerSpan(6);
        _splitView.removeItem(1);

        // Set Toolbar images
        getView("SaveButton", ButtonBase.class).setImage(Image.get(TextPane.class, "pkg.images/File_Save.png"));
        getView("CutButton", ButtonBase.class).setImage(Image.get(TextPane.class, "pkg.images/Edit_Cut.png"));
        getView("CopyButton", ButtonBase.class).setImage(Image.get(TextPane.class, "pkg.images/Edit_Copy.png"));
        getView("PasteButton", ButtonBase.class).setImage(Image.get(TextPane.class, "pkg.images/Edit_Paste.png"));
        getView("DeleteButton", ButtonBase.class).setImage(Image.get(TextPane.class, "pkg.images/Edit_Delete.png"));
        getView("UndoButton", ButtonBase.class).setImage(Image.get(TextPane.class, "pkg.images/Edit_Undo.png"));
        getView("RedoButton", ButtonBase.class).setImage(Image.get(TextPane.class, "pkg.images/Edit_Redo.png"));

        // Adjust InspectorButton
        Button inspBtn = getView("InspectorButton", Button.class);
        inspBtn.setManaged(false);
        inspBtn.setSize(30, 30);
        inspBtn.setMargin(new Insets(0, 4, 0, 0));
        inspBtn.setLean(Pos.CENTER_RIGHT);

        // Get/configure TreeView
        _treeView = getView("TreeView", TreeView.class);
        _treeView.setResolver(new ChartDocTreeResolver());
        _treeView.setRowHeight(25);
        _treeView.getCol(0).setAltPaint(new Color("#FBFBFC"));

        // Configure window
        WindowView win = getWindow();
        enableEvents(win, WinClose);

        // If TeaVM, go full window
        if (SnapUtils.isTeaVM) {
            getWindow().setMaximized(true);
            getView("WebButton").setVisible(false);
        }

        // Add the plotly button
        addPlotlyButton();

        // Add drag-drop support to open new files
        enableEvents(getUI(), DragEvents);

        // Add key actions
        addKeyActionHandler("DeleteAction", "DELETE");
        addKeyActionHandler("DeleteAction", "BACK_SPACE");
        addKeyActionHandler("EscapeAction", "ESCAPE");
        //addKeyActionHandler("PropArchiverAction", "H");
    }

    /**
     * Reset UI.
     */
    @Override
    protected void resetUI()
    {
        _treeView.setItems(getDoc());
        _treeView.expandItem(getDoc());  //_treeView.expandAll();
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
        // Handle SidebarButton
        if (anEvent.equals("SidebarButton"))
            setShowSidebar(!isShowSidebar());

        // Handle SaveButton
        if (anEvent.equals("SaveButton")) save();

        // Handle CutButton, CopyButton, PasteButton, DeleteButton
        if (anEvent.equals("CutButton")) cut();
        if (anEvent.equals("CopyButton")) copy();
        if (anEvent.equals("PasteButton")) paste();
        if (anEvent.equals("DeleteButton") || anEvent.equals("DeleteAction")) {
            delete();
            anEvent.consume();
        }

        // Handle WebButton
        if (anEvent.equals("WebButton")) {
            boolean isLocal = ViewUtils.isAltDown();
            AppEnv.getEnv().openChartsDocInBrowser(getDoc(), isLocal);
        }

        // Handle InspectorButton
        if (anEvent.equals("InspectorButton")) {
            DocItemPane itemPane = getSelItemPane();
            itemPane.setShowInspector(!itemPane.isShowInspector());
        }

        // Handle SamplesButton
        if (anEvent.equals("SamplesButton"))
            showSamples();

        // Handle TreeView
        if (anEvent.equals(_treeView)) {
            DocItem docItem = _treeView.getSelItem();

            // If event is double-click, reset DocItem's DocItemPane
            if (anEvent.getParentEvent() != null && anEvent.getParentEvent().getClickCount() == 2) {
                DocItem parItem = docItem.getParent();
                if (parItem != null) {
                    setSelItem(parItem);
                    resetItemPane(docItem);
                }
            }

            // Select docItem
            setSelItem(docItem);
        }

        // Handle WinClosing
        if (anEvent.isWinClose()) {
            close(); anEvent.consume(); }

        // Handle NewAction
        if (anEvent.equals("NewButton")) {
            respondToNewAction();
            //getSelItemPane().sendEvent(New_Action);
        }

        // Handle DragEvents
        if (anEvent.isDragEvent())
            handleDragEvent(anEvent);

        // Handle EscapeAction
        if (anEvent.equals("EscapeAction")) {
            DocItem docItem = getSelItem();
            DocItem parItem = docItem != null ? docItem.getParent() : null;
            if (parItem != null)
                setSelItem(parItem);
        }

        if (anEvent.equals("PropArchiverAction"))
            performPropArchiverTest();
    }

    /**
     * Responds to New_Action.
     */
    private void respondToNewAction()
    {
        // Show new doc item panel (if null response, just return)
        Class newDocItemClass = showNewDocItemPanel();
        if (newDocItemClass == null)
            return;

        // Create new DocItem for selected type (class)
        createNewDocItem(newDocItemClass);
    }

    /**
     * Shows the New Document Item panel.
     */
    protected Class showNewDocItemPanel()
    {
        // Get new FormBuilder and configure
        FormBuilder form = new FormBuilder();
        form.setPadding(20, 5, 15, 5);
        form.addLabel("Select new item type:           ").setFont(new Font("Arial", 24));
        form.setSpacing(15);

        // Get options
        Map<String,Class> docItemTypes = getDocItemTypes();
        String[] typeNames = docItemTypes.keySet().toArray(new String[0]);
        Class[] classes = docItemTypes.values().toArray(new Class[0]);

        // Define options, add and configure radio buttons
        for (String option : typeNames)
            form.addRadioButton("ItemType", "New " + option, option == typeNames[0]);

        // Run dialog panel (just return if null), select type and extension
        if (!form.showPanel(getUI(), "New Document Item", DialogBox.infoImage))
            return null;

        // Get item and return
        String selOption = form.getStringValue("ItemType");
        for (int i=0; i<typeNames.length; i++)
            if (selOption.endsWith(typeNames[i]))
                return classes[i];
        return null;
    }

    /**
     * Returns an ordered map of Names to DocItem types.
     */
    protected Map<String,Class> getDocItemTypes()
    {
        Map<String,Class> docItemTypes = new LinkedHashMap<>();
        docItemTypes.put("Notebook", Notebook.class);
        docItemTypes.put("Chart", Chart.class);
        docItemTypes.put("DataSet", Trace.class);
        return docItemTypes;
    }

    /**
     * Creates a new DocItem for given class.
     */
    protected void createNewDocItem(Class aClass)
    {
        // Handle Chart
        if (aClass == Chart.class) {

            // Create new chart
            Chart chart = new Chart();
            chart.setName("New Chart");
            chart.getHeader().setTitle("New Chart");
            chart.getAxisY().setTitle("Y Axis");
            chart.getAxisX().setTitle("X Axis");

            // Add to SelItem
            DocItem selItem = getSelItem();
            DocItem newChartItem = selItem.addChartPart(chart, null);
            setSelItem(newChartItem);
        }

        // Handle DataSet
        else if (aClass == Trace.class) {

            // Create new dataset
            Trace trace = new Trace();
            trace.setName("New Dataset");

            // Add to SelItem
            DocItem selItem = getSelItem();
            DocItem newDataSetItem = selItem.addChartPart(trace, null);
            if (!(selItem instanceof DocItemChart))
                setSelItem(newDataSetItem);
        }

        // Handle Notebook
        else if (aClass == Notebook.class) {

            // Create new Notebook
            Notebook notebook = new Notebook();
            notebook.setName("New Notebook");

            // Get sel index
            DocItem item = getSelItem();
            while (item != null && item != getDoc() && item.getParent() != getDoc()) item = item.getParent();
            int index = item == null || item == getDoc() ? 0 : (item.getIndex() + 1);

            // Add to SelItem
            DocItem notebookDocItem = new DocItemNotebook(notebook);
            Doc doc = getDoc();
            doc.addItem(notebookDocItem, index);
            setSelItem(notebookDocItem);
        }
    }

    /**
     * Called when DragEvent is over DocPane.
     */
    private void handleDragEvent(ViewEvent anEvent)
    {
        anEvent.acceptDrag();
        anEvent.consume();

        // If not Drag-Drop file, just return
        Clipboard clipboard = anEvent.getClipboard();
        if (!clipboard.hasFiles())
            return;

        // If no files, just return
        List<ClipboardData> cbFiles = clipboard.getFiles();
        if (cbFiles.size() == 0)
            return;

        // If file not '.charts' or '.simple', just return
        ClipboardData cbFile = cbFiles.get(0);
        String fileName = cbFile.getName();
        String ext = FilePathUtils.getExtension(fileName);
        if (!ext.equalsIgnoreCase("charts") && !ext.equalsIgnoreCase("simple"))
            return;

        // Handle DragDropEvent: Call handleDragDropChartsFile (with loaded file)
        if(anEvent.isDragDropEvent()) {
            if (cbFile.isLoaded())
                handleDragDropChartsFile(anEvent, cbFile);
            else cbFile.addLoadListener(cbd -> handleDragDropChartsFile(anEvent, cbd));
            anEvent.dropComplete();
        }
    }

    /**
     * Called when DragEvent is over DocPane.
     */
    private void handleDragDropChartsFile(ViewEvent anEvent, ClipboardData aFile)
    {
        byte[] xmlBytes = aFile.getBytes();
        ChartArchiver chartArchiver = new ChartArchiver();
        Doc doc = chartArchiver.getDocFromXMLBytes(xmlBytes);
        if (doc != null)
            setDoc(doc);
    }

    /**
     * Writes the doc to a file and opens in editor.
     */
    private void performPropArchiverTest()
    {
        Doc doc = getDoc();
        PropArchiver propArchiver = new PropArchiver();
        PropNode propNode = propArchiver.propObjectToPropNode(doc);
        XMLElement xml = propNode.getXML("Doc");
        byte[] xmlBytes = xml.getBytes();
        File file = new File("/tmp/PropArchTest.charts");
        try {
            FileUtils.writeBytes(file, xmlBytes);
            GFXEnv.getEnv().openTextFile(file);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Constants for images
    private static Image ICON_PLAIN = Image.get(DocPane.class, "PlainFile.png");
    private static Image ICON_DIR = Image.get(ViewUtils.class, "DirFile.png");
    private static Image ICON_DATA = Image.get(ViewUtils.class, "TableFile.png");
    private static Image ICON_CHART = Image.get(DocPane.class, "Chart.png");
    private static Image ICON_GROUP = Image.get(DocPane.class, "Group2.png");

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
            List<DocItem> docItems = aParent.getItems();
            return docItems.toArray(new DocItem[0]);
        }

        @Override
        public String getText(DocItem anItem)
        {
            return anItem.getName();
        }

        @Override
        public Image getImage(DocItem anItem)
        {
            if (anItem instanceof Doc) return ICON_DIR;
            if (anItem instanceof DocItemChart) return ICON_CHART;
            if (anItem instanceof DocItemDataSet) return ICON_DATA;
            if (anItem instanceof DocItemGroup) return ICON_GROUP;
            return ICON_PLAIN;
        }
    }
}