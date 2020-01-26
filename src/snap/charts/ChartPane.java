package snap.charts;
import snap.gfx.*;
import snap.util.SnapUtils;
import snap.view.*;

/**
 * A class to manage a ChartView.
 */
public class ChartPane extends ViewOwner {
    
    // Whether to show full app
    boolean       _showFull;
    
    // The chartView
    ChartView     _chartView;
    
    // The ChartBox
    BoxView       _chartBox;
    
    // The TabView
    TabView       _tabView;
    
    // The PropsPane to handle chart property editing
    PropsPane     _propsPane;
    
    // The DataPane to handle chart property editing
    DataPane      _dataPane;
    
    // The Thr3DPane to handle chart 3d editing
    Thr3DPane     _3dPane;
    
    // The TextView
    TextView      _textView;
    
    // The Options button
    Button        _optionButton;

/**
 * Returns whether to show app stuff.
 */
public boolean isShowFull()  { return _showFull; }

/**
 * Sets whether to show full app stuff.
 */
public void setShowFull(boolean aValue)
{
    // If already set, just return
    if(aValue==_showFull) return;
    _showFull = aValue;
    
    // Enable ShowFull
    if(aValue) {
        _tabView.setVisible(true);
        loadTabView();
        if(!SnapUtils.isTeaVM) {
            Size psize = getWindow().getPrefSize();
            Rect screenRect = ViewEnv.getEnv().getScreenBoundsInset();
            Rect maxRect = screenRect.getRectCenteredInside(psize.width, psize.height);
            getWindow().setMaximizedBounds(maxRect);
        }
        _chartBox.setPadding(50,50,50,50); _chartView.setEffect(new ShadowEffect());
        getWindow().setMaximized(true);
    }
    
    // Disable ShowFull
    else {
        _tabView.setVisible(false);
        _chartBox.setPadding(0,0,0,0); _chartView.setEffect(null);
        getWindow().setMaximized(false);
    }
    
    _optionButton.setText(aValue? " Min " : " Max ");
    _optionButton.setSizeToPrefSize();
}

/**
 * Create UI.
 */
protected View createUI()
{
    // Create OptionButton
    _optionButton = new Button(" Max "); _optionButton.setName("OptionButton");
    _optionButton.setManaged(false); _optionButton.setLean(Pos.TOP_RIGHT); _optionButton.setMargin(4,5,0,0);
    _optionButton.setSizeToPrefSize();
    
    // Create ChartView
    _chartView = new ChartView();
    _chartBox = new BoxView(_chartView, true, true); _chartBox.setGrowHeight(true);
    _chartBox.setContent(_optionButton);
    
    // Create TextView
    _textView = new TextView();
    _textView.setDefaultStyle(_textView.getDefaultStyle().copyFor(Font.Arial14));
    BoxView textBox = new BoxView(_textView, true, true); textBox.setFill(new Color(.93)); textBox.setPadding(4,4,4,4);
    textBox.setGrowHeight(true); textBox.setPrefHeight(400);
    
    // Create TabView
    _tabView = new TabView(); _tabView.setPrefHeight(340); //_tabView.setGrowHeight(true);
    _tabView.addTab("Chart Props", new Label("PropsPane"));
    _tabView.addTab(" Data Set ", new Label("DataPane"));
    _tabView.addTab("   3D   ", new Label("3D"));
    _tabView.addTab("JavaScript", textBox);
    _tabView.setFont(Font.Arial13);
    _tabView.setVisible(false);
    
    // Handy line to show DataPane immediately for debugging
    //getEnv().runDelayed(() -> { setShowFull(true); _tabView.setSelIndex(0); }, 100, true);
    
    // Create ColView
    ColView col = new ColView(); col.setFillWidth(true); col.setGrowHeight(true); col.setFill(new Color(.93));
    col.setChildren(_chartBox, _tabView);
    return SplitView.makeSplitView(col);
}

/**
 * Respond to UI.
 */
protected void respondUI(ViewEvent anEvent)
{
    // Handle OptionButton
    if(anEvent.equals("OptionButton")) {
        if(ViewUtils.isAltDown()) getUI(SplitView.class).setItemVisibleWithAnim(_tabView, !_tabView.isVisible());
        else setShowFull(!isShowFull());
    }
    
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
        
    // If JavaScript tab, update text
    if(tab.getTitle().equals("JavaScript")) {
        String str = new ChartWriter(_chartView).getStringWithHTML();
        _textView.setText(str);
    }
    
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

/**
 * Performs an action.
 */
public void doAction(String anAction)
{
    String action = anAction.substring("Action:".length());

    if(action.equals("Playground"))
        setShowFull(true);
}

}