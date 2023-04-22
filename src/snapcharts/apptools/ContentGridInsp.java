package snapcharts.apptools;
import snap.geom.Line;
import snap.geom.Shape;
import snap.gfx.Border;
import snap.gfx.Color;
import snap.gfx.Stroke;
import snap.util.Convert;
import snap.view.*;
import snapcharts.app.ChartPane;
import snapcharts.model.Axis;
import snapcharts.model.Chart;
import snapcharts.model.ChartPart;
import snapcharts.model.Content;
import snapcharts.view.AxisView;
import snapcharts.view.ChartHelper;
import snapcharts.view.ChartView;
import java.util.Objects;

/**
 * This class manages UI to edit Chart.Content. Though many of the properties are really for Axis.
 */
public class ContentGridInsp extends ChartPartInsp {

    /**
     * Constructor.
     */
    public ContentGridInsp(ChartPane aChartPane)
    {
        super(aChartPane);
    }

    /**
     * Returns the name.
     */
    @Override
    public String getName()  { return "Content Style"; }

    /**
     * Returns the ChartPart.
     */
    @Override
    public ChartPart getChartPart()
    {
        Chart chart = getChart();
        Content content = chart.getContent();
        return content;
    }

    /**
     * Initialize UI.
     */
    @Override
    protected void initUI()
    {
        // Configure GridDashButton(s)
        for (int i = 0; i < Stroke.DASHES_ALL.length; i++) {
            double[] dashArray = Stroke.DASHES_ALL[i];
            Button lineDashButton = getView("GridDashButton_" + i, Button.class);
            if (lineDashButton != null)
                configureLineDashButton(lineDashButton, dashArray);
        }
    }

    /**
     * Reset UI.
     */
    protected void resetUI()
    {
        // Get Axis, AxisView (just return if null)
        ChartPane chartPane = getChartPane();
        ChartView chartView = chartPane.getChartView();
        ChartHelper chartHelper = chartView.getChartHelper();
        AxisView[] axisViews = chartHelper.getAxisViews();
        Axis[] axes = new Axis[axisViews.length];
        for (int i=0; i<axisViews.length; i++) axes[i] = axisViews[i].getAxis();
        Axis axis = axes.length > 0 ? axes[0] : null;

        // Reset ShowGridCheckBox
        boolean showGrid = axis != null && axis.isShowGrid();
        setViewValue("ShowGridCheckBox", showGrid);

        // Reset GridColorButton, GridColorResetButton
        Color gridColor = axis.getGridColor();
        setViewValue("GridColorButton", gridColor);
        setViewVisible("GridColorResetButton", !Objects.equals(gridColor, Color.BLACK));

        // Reset GridWidthText, GridWidthResetButton
        double gridWidth = axis.getGridWidth();
        double DEFAULT_GRID_WIDTH = 1;
        setViewValue("GridWidthText", gridWidth);
        setViewVisible("GridWidthResetButton", gridWidth != DEFAULT_GRID_WIDTH);

        // Reset GridDashButton
        ToggleButton gridDashButton = getView("GridDashButton", ToggleButton.class);
        configureLineDashButton(gridDashButton, axis.getGridDash());

        // Reset GridDashBox
        View gridDashBox = getView("GridDashBox");
        ViewAnimUtils.setVisible(gridDashBox, gridDashButton.isSelected(), false, true);
    }

    /**
     * Respond to UI.
     */
    protected void respondUI(ViewEvent anEvent)
    {
        // Get Axis, AxisView (just return if null)
        ChartPane chartPane = getChartPane();
        ChartView chartView = chartPane.getChartView();
        ChartHelper chartHelper = chartView.getChartHelper();
        AxisView[] axisViews = chartHelper.getAxisViews();
        Axis[] axes = new Axis[axisViews.length];
        for (int i=0; i<axisViews.length; i++) axes[i] = axisViews[i].getAxis();

        // Handle ShowGridCheckBox
        if (anEvent.equals("ShowGridCheckBox")) {
            boolean showGrid = anEvent.getBoolValue();
            for (Axis axis : axes)
                axis.setShowGrid(showGrid);
        }

        // Handle GridWidthText, GridWidthAdd1Button, GridWidthSub1Button, GridWidthResetButton
        if (anEvent.equals("GridWidthText"))
            for (Axis axis : axes)
                axis.setGridWidth(Math.max(anEvent.getIntValue(), 1));
        if (anEvent.equals("GridWidthAdd1Button"))
            for (Axis axis : axes)
                axis.setGridWidth(axis.getGridWidth() + 1);
        if (anEvent.equals("GridWidthSub1Button"))
            for (Axis axis : axes)
                axis.setGridWidth(Math.max(axis.getGridWidth() - 1, 1));
        if (anEvent.equals("GridWidthResetButton"))
            for (Axis axis : axes)
                axis.setGridWidth(Axis.DEFAULT_GRID_WIDTH);

        // Handle GridColorButton, GridColorResetButton
        if (anEvent.equals("GridColorButton")) {
            Color color = (Color) getViewValue("GridColorButton");
            for (Axis axis : axes)
                axis.setGridColor(color);
        }
        if (anEvent.equals("GridColorResetButton"))
            for (Axis axis : axes)
                axis.setGridColor(Axis.DEFAULT_GRID_COLOR);

        // Handle GridDashButton_X
        String eventName = anEvent.getName();
        if (eventName.startsWith("GridDashButton_")) {
            int id = Convert.intValue(eventName);
            double[] dashArray = Stroke.DASHES_ALL[id];
            for (Axis axis : axes)
                axis.setGridDash(dashArray);
        }
    }

    /**
     * Configures a LineDash button.
     */
    private void configureLineDashButton(ButtonBase aButton, double[] dashArray)
    {
        Stroke stroke = Stroke.Stroke2.copyForCap(Stroke.Cap.Butt).copyForDashes(dashArray);
        Border border = Border.createLineBorder(Color.BLUE.darker(), 2).copyForStroke(stroke);
        Shape shape = new Line(5, 5, 80, 5);
        ShapeView shapeView = new ShapeView(shape);
        shapeView.setBounds(0, 0, 100, 12);
        shapeView.setBorder(border);
        aButton.setGraphic(shapeView);
    }
}