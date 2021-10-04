/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.app;
import snap.gfx.Image;
import snap.view.RowView;
import snap.view.ToggleButton;
import snap.view.ViewEvent;

/**
 * This class handles Marker create/edit for ChartPane.
 */
public class ChartPaneTools {

    // The ChartPane
    private ChartPane  _chartPane;

    // Whether currently editing
    private boolean  _editing;

    /**
     * Constructor.
     */
    public ChartPaneTools(ChartPane aChartPane)
    {
        _chartPane = aChartPane;
    }

    /**
     * Returns whether editing.
     */
    public boolean isEditing()  { return _editing; }

    /**
     * Sets whether editing.
     */
    public void setEditing(boolean aValue)
    {
        // If already set, just return
        if (aValue == _editing) return;

        // Set value
        _editing = aValue;
    }

    /**
     * Responds to EditButton pressed.
     */
    public void respondEditButton(ViewEvent anEvent)
    {
        setEditing(anEvent.getBoolValue());
    }

    /**
     * Adds tools.k
     */
    public void addChartPaneTools()
    {
        // Create EditButton
        ToggleButton editButton = new ToggleButton();
        editButton.setName("EditButton");
        editButton.setToolTip("Add Chart Annotations");
        Image editButtonImage = Image.get(getClass(), "SGPencil.png");
        editButton.setImage(editButtonImage);
        editButton.setShowArea(false);
        editButton.setPrefSize(24, 24);

        // Add to toolbar
        RowView toolBar = _chartPane.getView("ToolBar", RowView.class);
        toolBar.addChild(editButton, 0);
    }
}
