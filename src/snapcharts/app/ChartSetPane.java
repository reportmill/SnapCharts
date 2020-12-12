package snapcharts.app;
import snap.geom.*;
import snap.gfx.Color;
import snap.gfx.Image;
import snap.gfx.Painter;
import snap.util.ListUtils;
import snap.util.PropChange;
import snap.view.*;
import snapcharts.model.Chart;
import snapcharts.model.DocItem;
import snapcharts.model.DocItemChart;
import snapcharts.model.DocItemGroup;
import snapcharts.views.PageView;
import java.util.ArrayList;
import java.util.List;

/**
 * A ViewOwner to handle display of whole ChartDoc.
 */
public class ChartSetPane extends DocItemPane {

    // The DocItem that this class displays
    private DocItemGroup  _docItem;

    // The list of charts
    private List<Chart>  _charts = new ArrayList<>();

    // The view to hold PageView
    private BoxView _pageBox;

    // The ScrollView that holds PageBox
    private ScrollView  _pageBoxScrollView;

    // The list of pages
    private List<PageView> _pageViews = new ArrayList<>();

    // The selected page index
    private int _selPageIndex = -1;

    // The Inspector
    private ChartSetPaneInsp _insp;

    // Runnable to reset charts later
    private Runnable  _resetChartsRun, _resetChartsRunShared = () -> resetChartsImpl();

    // Constants
    public static Color BACK_FILL = new Color(226, 232, 246);

    /**
     * Returns the DocItem that this ChartSetPane displays.
     */
    public DocItemGroup getDocItem()  { return _docItem; }

    /**
     * Sets the DocItem that this ChartSetPane displays.
     */
    public void setDocItem(DocItemGroup anItem)
    {
        _docItem = anItem;
        _docItem.addPropChangeListener(pc -> docItemDidPropChange(pc));
    }

    /**
     * Returns the inspector.
     */
    public ViewOwner getInspector()  { return _insp; }

    /**
     * Returns the list of PageView.
     */
    public List<PageView> getPages()  { return _pageViews; }

    /**
     * Returns the number of pages.
     */
    public int getPageCount()  { return _pageViews.size(); }

    /**
     * Returns the individual page at given index.
     */
    public PageView getPage(int anIndex)  { return _pageViews.get(anIndex); }

    /**
     * Returns the selected page index.
     */
    public int getSelPageIndex()  { return _selPageIndex; }

    /**
     * Sets the selected page index.
     */
    public void setSelPageIndex(int anIndex)
    {
        int index = Math.max(0, Math.min(anIndex, getPageCount()-1));
        if (index==getSelPageIndex()) return;

        _selPageIndex = index;

        PageView page = getSelPageView();
        _pageBox.setContent(page);
    }

    /**
     * Returns the selected page.
     */
    public PageView getSelPageView()  { return _selPageIndex>=0 ? getPage(_selPageIndex) : null; }

    /**
     * Override to return selected PageView.
     */
    @Override
    public View getItemView()  { return getSelPageView(); }

    /**
     * Called when DocItem has prop change.
     */
    private void docItemDidPropChange(PropChange aPC)
    {
        // Get PropChange.PropName
        String propName = aPC.getPropName();

        // Handle Items
        if (propName==DocItem.Items_Prop)
            resetCharts();
        if (propName==DocItemGroup.ItemsPerPage_Prop)
            resetCharts();
        if (propName==DocItemGroup.Portrait_Prop)
            resetCharts();
    }

    /**
     * Returns the list of charts.
     */
    public List<Chart> getCharts()  { return _charts; }

    /**
     * Resets the charts.
     */
    private void resetCharts()
    {
        if (_resetChartsRun==null)
            runLater(_resetChartsRun=_resetChartsRunShared);
    }

    /**
     * Resets the charts.
     */
    private void resetChartsImpl()
    {
        _charts.clear();
        _pageBox.setContent(null);
        _pageViews.clear();

        // Get List of DocItemChart
        List<DocItemChart> chartDocItems = ListUtils.getFilteredForClass(_docItem.getItems(), DocItemChart.class);

        // Get number of plots per page
        DocItemGroup docItem = getDocItem();
        int plotsPerPage = docItem.getItemsPerPage();

        // Get the current page
        PageView pageView = new PageView(plotsPerPage);
        pageView.setVertical(docItem.isPortrait());
        _pageViews.add(pageView);

        // Get charts
        for (int i=0; i<chartDocItems.size(); i++) {

            // Get Chart and to Charts list
            DocItemChart item = chartDocItems.get(i);
            Chart chart = item.getChart();
            _charts.add(chart);

            // Create/add PageView if needed
            if (pageView==null) {
                pageView = new PageView(plotsPerPage);
                pageView.setVertical(docItem.isPortrait());
                _pageViews.add(pageView);
            }

            // Add to PageView
            pageView.addChart(chart);

            // If next chart needs new page, clear
            if ((i+1)%plotsPerPage==0)
                pageView = null;
        }

        _selPageIndex = -1;
        setSelPageIndex(0);

        _resetChartsRun = null;
    }

    /**
     * Initialize UI.
     */
    @Override
    protected void initUI()
    {
        RowView topRowView = getUI(RowView.class);

        // Create/add InspectorPane
        _insp = new ChartSetPaneInsp(this);
        topRowView.addChild(_insp.getUI());

        // Get PageBox
        _pageBox = getView("PageBox", BoxView.class);
        _pageBox.setFill(BACK_FILL);

        // Get PageBoxScrollView
        _pageBoxScrollView = getView("ScrollView", ScrollView.class);
        _pageBoxScrollView.setFillWidth(true);
        _pageBoxScrollView.setFillHeight(true);

        resetCharts();

        // Set right arrow in PageForwardButton
        Polygon p1 = new Polygon(4, 5, 10, 11, 4, 17);
        getView("PageForwardButton", Button.class).setImage(getImage(p1));

        // Set left arrow in PageBackButton
        Polygon p2 = new Polygon(10, 5, 4, 11, 10, 17);
        getView("PageBackButton", Button.class).setImage(getImage(p2));

        // Set left arrow plus stop bar in PageBackAllButton
        Path p3 = new Path(); p3.append(p2.getPathIter(new Transform(2, 0)));
        p3.append(new Rect(2, 6, 2, 10));
        getView("PageBackAllButton", Button.class).setImage(getImage(p3));

        // Set right arrow plus stop bar in PageForwardAllButton
        Path p4 = new Path(); p4.append(p1.getPathIter(new Transform(-2, 0)));
        p4.append(new Rect(10, 6, 2, 10));
        getView("PageForwardAllButton", Button.class).setImage(getImage(p4));
    }

    /**
     * ResetUI.
     */
    @Override
    protected void resetUI()
    {
        // Reset ZoomText
        //setViewValue("ZoomText", Math.round(viewer.getZoomFactor()*100) + "%");

        // Reset PageText field
        String pageText = "" + (getSelPageIndex()+1) + " of " + getPageCount();
        setViewValue("PageText", pageText);

        // Reset page button enabled states for page back/forward buttons
        setViewEnabled("PageBackButton", getSelPageIndex()>0);
        setViewEnabled("PageBackAllButton", getSelPageIndex()>0);
        setViewEnabled("PageForwardButton", getSelPageIndex()<getPageCount()-1);
        setViewEnabled("PageForwardAllButton", getSelPageIndex()<getPageCount()-1);

        // Update PageNavBox.Visible
        getView("PageNavBox").setVisible(getPageCount()>1);
    }

    /**
     * RespondUI.
     */
    @Override
    protected void respondUI(ViewEvent anEvent)
    {
        // Handle ZoomComboBox, ZoomMenuButton
        //if(anEvent.equals("ZoomText")) setZoomFactor(anEvent.getFloatValue()/100);
        //if(anEvent.equals("ZoomMenuItem")) setZoomFactor(SnapUtils.floatValue(anEvent.getText())/100);

        // Handle ZoomToActualSizeMenuItem - use screen resolution to figure out zooming for actual size
        //if(anEvent.equals("ZoomToActualSizeMenuItem"))
        //    viewer.setZoomFactor(viewer.getZoomToActualSizeFactor());

        // Handle ZoomToFitMenuItem
        //if(anEvent.equals("ZoomToFitMenuItem")) setZoomMode(RMViewer.ZoomMode.ZoomToFit);

        // Handle ZoomAsNeededMenuItem
        //if(anEvent.equals("ZoomAsNeededMenuItem")) viewer.setZoomMode(RMViewer.ZoomMode.ZoomAsNeeded);

        // Handle PageText
        if(anEvent.equals("PageText"))
            setSelPageIndex(anEvent.getIntValue()-1);

        // Handle PageBackButton
        if(anEvent.equals("PageBackButton"))
            setSelPageIndex(getSelPageIndex()-1);

        // Handle PageBackAllButton
        if(anEvent.equals("PageBackAllButton"))
            setSelPageIndex(0);

        // Handle PageForwardButton
        if(anEvent.equals("PageForwardButton"))
            setSelPageIndex(getSelPageIndex()+1);

        // Handle PageForwardAllButton
        if(anEvent.equals("PageForwardAllButton"))
            setSelPageIndex(getPageCount()-1);
    }

    /**
     * Returns an image for given shape.
     */
    private static Image getImage(Shape aShape)
    {
        Image img = Image.get(14,22,true);
        Painter pntr = img.getPainter();
        pntr.setColor(Color.DARKGRAY);
        pntr.fill(aShape);
        return img;
    }
}