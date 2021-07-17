/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.appx;
import rmdraw.app.InspectorPanel;
import rmdraw.app.MarkupEditor;
import rmdraw.app.MarkupEditorPane;
import snap.gfx.Image;
import snap.view.*;
import snapcharts.app.ChartPane;
import snapcharts.app.ChartPaneInsp;
import snapcharts.app.ChartSetPane;
import snapcharts.view.ChartView;

/**
 * A class to manage charts/data in a ChartBook.
 */
public class ChartPanePro extends ChartPane {

    // A MarkupEditorPane to hold ChartView and allow markup
    protected MarkupEditorPane  _editorPane;

    /**
     * Constructor.
     */
    public ChartPanePro()
    {
        super();
    }

    /**
     * Respond to UI.
     */
    protected void respondUI(ViewEvent anEvent)
    {
        // Handle EditButton
        if (anEvent.equals("EditButton"))
            runLater(() -> installEditorPane());

        // Do normal version
        else super.respondUI(anEvent);
    }

    /**
     * Override to add Edit button.
     */
    @Override
    protected void initUI()
    {
        // Do normal version
        super.initUI();

        // Create EditButton
        Button editButton = new Button();
        editButton.setName("EditButton");
        editButton.setToolTip("Add Chart Annotations");
        Image editButtonImage = Image.get(InspectorPanel.class, "/rmdraw/apptools/SGPencil.png");
        editButton.setImage(editButtonImage);
        editButton.setShowArea(false);
        editButton.setPrefSize(24, 24);

        // Add to toolbar
        RowView toolBar = getView("ToolBar", RowView.class);
        toolBar.addChild(editButton, 0);
    }

    /**
     * Installs EditorPane.
     */
    private void installEditorPane()
    {
        // Get toolbar
        RowView toolBar = getView("ToolBar", RowView.class);
        toolBar.removeChild(getView("EditButton"));
        toolBar.setClipToBounds(true);

        // Get ChartBox, ChartView
        BoxView chartBox = getChartBox();
        ChartView chartView = getChartView();

        // Get EditorPane, Editor
        _editorPane = new MarkupEditorPane(chartView);
        MarkupEditor editor = _editorPane.getEditor();
        editor.setFill(ChartSetPane.BACK_FILL);
        _editorPane.getScrollView().setBorder(null);

        // Install EditorPane
        SplitView splitView = (SplitView) chartBox.getParent();
        splitView.removeItem(chartBox);
        splitView.addItem(_editorPane.getUI(), 0);

        // WTF
        ViewUtils.setFocused(editor, false);

        // Install ToolBar
        RowView toolsView = _editorPane.getToolsView();
        toolBar.addChild(toolsView, 0);
        for (View child : toolsView.getChildren())
            ((ToggleButton)child).setShowArea(false);

        // Animate in
        toolsView.setTransX(-100);
        toolsView.getAnimCleared(700).setTransX(0).play();
        toolsView.getAnim(0).setOnFinish(() -> installEditorPaneAnimDone());
    }

    /**
     * MarkupEditor: Called after fully installed.
     */
    private void installEditorPaneAnimDone()
    {
        RowView toolsView = _editorPane.getToolsView();
        ((ToggleButton)toolsView.getChildLast()).fire();

        MarkupEditor editor = _editorPane.getEditor();
        editor.setNeedsInspector(false);
        editor.addPropChangeListener(pc -> markupEditorNeedsInspectorChanged(), MarkupEditor.NeedsInspector_Prop);
        editor.setNeedsInspector(true);
    }

    /**
     * MarkupEditor: Called when MarkupEditor.NeedsInspector changes.
     */
    private void markupEditorNeedsInspectorChanged()
    {
        boolean markupInspVisible = _editorPane.getEditor().isNeedsInspector();
        setMarkupInspectorVisible(markupInspVisible);
    }

    /**
     * Returns whether MarkupInspector is visible.
     */
    public boolean isMarkupInspectorVisible()
    {
        InspectorPanel markupInsp = _editorPane != null ? _editorPane.getInspectorPanel() : null;
        return markupInsp != null && markupInsp.isShowing();
    }

    /**
     * Sets the MarkupInspector Visible.
     */
    public void setMarkupInspectorVisible(boolean aValue)
    {
        // If already set, just return
        if (aValue == isMarkupInspectorVisible()) return;

        // Get inspector
        InspectorPanel markupInsp = _editorPane.getInspectorPanel();

        // Make MarkupInspector visible
        ChartPaneInsp insp = getInspector();
        if (aValue) {
            insp.setOverrideInspectorView(markupInsp.getUI());
        }

        // Make ChartPaneInsp visble
        else insp.setOverrideInspectorView(null);
    }

    /**
     * Override to make sure normal Inspector UI is back whenever chart part selection changes.
     */
    @Override
    public void chartPaneSelChanged()
    {
        setMarkupInspectorVisible(false);
        super.chartPaneSelChanged();
    }
}