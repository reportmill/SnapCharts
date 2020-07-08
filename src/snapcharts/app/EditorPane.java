package snapcharts.app;

import snap.gfx.Font;
import snap.gfx.ShadowEffect;
import snap.view.*;

/**
 * A class to manage charts/data in a ChartBook.
 */
public class EditorPane extends ViewOwner {
    
    // The chartView
    private ChartView  _chartView;
    
    // The ChartBox
    private BoxView  _chartBox;
    
    // The TabView
    private TabView  _tabView;
    
    // The PropsPane to handle chart property editing
    private PropsPane  _propsPane;
    
    // The DataPane to handle chart property editing
    private DataPane  _dataPane;
    
    // The Thr3DPane to handle chart 3d editing
    private Thr3DPane  _3dPane;
    
    /**
     * Create UI.
     */
    protected View createUI()
    {
        // Get ColView
        ColView colView = (ColView)super.createUI();

        // Create ChartView
        _chartView = new ChartView();
        _chartView.setEffect(new ShadowEffect());

        // Create ChartBox
        _chartBox = (BoxView)colView.getChild("ChartBox");
        _chartBox.setContent(_chartView);

        // Create TabView
        _tabView = new TabView(); _tabView.setPrefHeight(300); //_tabView.setGrowHeight(true);
        _tabView.addTab("Chart Props", new Label("PropsPane"));
        _tabView.addTab(" Data Set ", new Label("DataPane"));
        _tabView.addTab("   3D   ", new Label("3D"));
        _tabView.setFont(Font.Arial13);
        loadTabView();

        // Create ColView
        colView.setChildren(_chartBox, _tabView);
        return SplitView.makeSplitView(colView);
    }

    /**
     * Respond to UI.
     */
    protected void respondUI(ViewEvent anEvent)
    {
        // Handle TabView
        if(anEvent.equals(_tabView))
            loadTabView();
    }

    /**
     * Makes sure the tab view is loaded.
     */
    void loadTabView()
    {
        int ind = _tabView.getSelIndex();
        Tab tab = _tabView.getTab(ind);

        // If TabView tab only has label, swap for actual content pane
        if(tab.getContent() instanceof Label)
            _tabView.setTabContent(getTabViewPane(tab.getContent().getText()).getUI(), ind);

        // Update UI
        tab.getContent().getOwner().resetLater();
    }

    /**
     * Returns the tabview pane for given text.
     */
    ViewOwner getTabViewPane(String aName)
    {
        switch(aName) {
            case "PropsPane": return getPropsPane();
            case "DataPane": return getDataPane();
            case "3D": return get3DPane();
            default: System.err.println("ChartPane.getTabViewPane: Pane not found for " + aName); return null;
        }
    }

    /**
     * Returns the PropsPane to handle chart property editing.
     */
    public PropsPane getPropsPane()
    {
        if(_propsPane!=null) return _propsPane;
        _propsPane = new PropsPane(); _propsPane._chartView = _chartView;
        return _propsPane;
    }

    /**
     * Returns the DataPane to handle chart data editing.
     */
    public DataPane getDataPane()
    {
        if(_dataPane!=null) return _dataPane;
        _dataPane = new DataPane(_chartView);
        _dataPane.getUI().setSize(_tabView.getContent().getSize());
        return _dataPane;
    }

    /**
     * Returns the Thr3DPane to handle chart 3d editing.
     */
    public Thr3DPane get3DPane()
    {
        if(_3dPane!=null) return _3dPane;
        _3dPane = new Thr3DPane(_chartView);
        return _3dPane;
    }
}