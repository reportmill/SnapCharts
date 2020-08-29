package snapcharts.app;
import snap.gfx.Color;
import snap.gfx.Image;
import snap.view.*;
import snap.viewx.TextPane;
import snapcharts.apptools.ChartBasicTool;
import snapcharts.apptools.ChartTypeTool;

/**
 * A class to manage inspector.
 */
public class ChartPaneInsp extends ViewOwner {

    // The ChartPane
    protected ChartPane _chartPane;
    
    // The Title label
    private Label  _titleLabel;
    
    // The ScrollView that holds UI for child inspectors
    private ScrollView _inspScroll;
    
    // The ColView that holds UI for child inspectors
    private ColView  _inspColView;

    // The ChartTypeTool
    private ChartTypeTool _chartType;
    
    // The BasicPropsTool
    private ChartBasicTool _basicProps;

    // The DataSet Inspector
    private DataSetInsp  _dsetInsp;

    /**
     * Constructor.
     */
    public ChartPaneInsp(ChartPane anEP)
    {
        _chartPane = anEP;
    }

    /**
     * Returns the ChartPane.
     */
    public ChartPane getEditorPane()  { return _chartPane; }

    /**
     * Initializes UI panel for the inspector.
     */
    public void initUI()
    {
        // Get/configure TitleLabel
        _titleLabel = getView("TitleLabel", Label.class);
        _titleLabel.setTextFill(Color.GRAY);

        // Get/configure SearchText: radius, prompt, image, animation
        TextField searchText = getView("SearchTextField", TextField.class);
        searchText.setPromptText("Search");
        searchText.getLabel().setImage(Image.get(TextPane.class, "Find.png"));
        TextField.setBackLabelAlignAnimatedOnFocused(searchText, true);
        //searchText.addEventFilter(e -> ViewUtils.runLater(() -> textFieldKeyTyped(e)), KeyPress);

        // Get/configure ContentBox
        _inspScroll = getView("ContentBox", ScrollView.class);
        _inspScroll.setBorder(null);
        _inspScroll.setBarSize(12);
        _inspScroll.setFillWidth(true);

        // Get InspColView
        _inspColView = getView("InspColView", ColView.class);

        // Only Add Chart stuff for ChartMode
        boolean chartMode = !_chartPane._dataSetMode;
        if (chartMode) {

            // Get ChartTypeTool
            _chartType = new ChartTypeTool(_chartPane);
            _inspColView.addChild(_chartType.getUI());
            Collapser.createCollapserAndLabel(_chartType.getUI(), "Chart Types");//.setCollapsed(true);

            // Get ChartBasicTool
            _basicProps = new ChartBasicTool(_chartPane);
            _inspColView.addChild(_basicProps.getUI());
            Collapser.createCollapserAndLabel(_basicProps.getUI(), "Chart Settings");//.setCollapsed(true);
        }

        // Add DataSet inspector
        _dsetInsp = new DataSetInsp(_chartPane);
        _inspColView.addChild(_dsetInsp.getUI());
    }

    /**
     * Refreshes the inspector for the current editor selection.
     */
    public void resetUI()
    {
        // Get editor (and just return if null) and tool for selected shapes
        ChartPane chartPane = getEditorPane();

        // Reset DataSetInsp
        boolean isDataSetSelected = chartPane.getDataSet()!=null;
        if (isDataSetSelected) {
            _dsetInsp.getUI().setVisible(true);
            _dsetInsp.resetLater();
        }
        else _dsetInsp.getUI().setVisible(false);
    }

    /**
     * Handles changes to the inspector UI controls.
     */
    public void respondUI(ViewEvent anEvent)  { }
}