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

    // The EditorPane
    protected ChartPane _epane;
    
    // The Title label
    private Label  _titleLabel;
    
    // The ScrollView that holds UI for child inspectors
    private ScrollView _inspScroll;
    
    // The ColView that holds UI for child inspectors
    private ColView  _inspColView;

    // The child inspector current installed in inspector panel
    private ViewOwner  _childInspector;

    // The ChartTypeTool
    private ChartTypeTool _chartType;
    
    // The BasicPropsTool
    private ChartBasicTool _basicProps;

    // The inspector for view general
    //private ViewTool  _viewTool;

    /**
     * Constructor.
     */
    public ChartPaneInsp(ChartPane anEP)
    {
        _epane = anEP;
    }

    /**
     * Returns the editor pane.
     */
    public ChartPane getEditorPane()  { return _epane; }

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

        // Get ChartTypeTool
        _chartType = new ChartTypeTool(_epane);
        _inspColView.addChild(_chartType.getUI());
        Collapser.createCollapserAndLabel(_chartType.getUI(), "Chart Types").setCollapsed(true);

        // Get ChartBasicTool
        _basicProps = new ChartBasicTool(_epane);
        _inspColView.addChild(_basicProps.getUI());
        Collapser.createCollapserAndLabel(_basicProps.getUI(), "Basic Properties").setCollapsed(true);

        // Get ViewTool
        //_viewTool = _epane._viewTool;
    }

    /**
     * Refreshes the inspector for the current editor selection.
     */
    public void resetUI()
    {
        // Get editor (and just return if null) and tool for selected shapes
        ChartPane epane = getEditorPane();

        // If ViewGeneralButton is selected, instal inspector
        //if (getViewBoolValue("ViewGeneralButton")) setInspector(_viewTool);

        // Get the inspector (owner)
        ViewOwner owner = getInspector();

        // Get inspector title from owner and set
        String title = "Inspector";
        //if (owner==_gallery) title = "Gallery";
        //else if (owner==_viewTool) title = "View Inspector";
        //else if (owner==_stylerPane) title = "Style Inspector";
        //else if (owner instanceof ViewTool) title = selView.getClass().getSimpleName() + " Inspector";
        _titleLabel.setText(title);

        // If owner non-null, tell it to reset
        if (owner!=null)
            owner.resetLater();
    }

    /**
     * Handles changes to the inspector UI controls.
     */
    public void respondUI(ViewEvent anEvent)
    {
        // Handle ViewGeneralButton
        //if (anEvent.equals("ViewGeneralButton")) setInspector(_viewTool);
    }

    /**
     * Returns the inspector (owner) of the inspector pane.
     */
    protected ViewOwner getInspector()  { return _childInspector; }

    /**
     * Sets the inspector in the inspector pane.
     */
    protected void setInspector(ViewOwner anOwner)
    {
        // Set new inspector
        _childInspector = anOwner;

        // Get content and it grows height
        View content = anOwner.getUI();
        boolean contentGrowHeight = content.isGrowHeight();

        // Set content and whether Inspector ScrollView sizes or scrolls content vertically
        _inspScroll.setContent(content);
        _inspScroll.setFillHeight(contentGrowHeight);
    }
}