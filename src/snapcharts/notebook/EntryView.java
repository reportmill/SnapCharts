/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.notebook;
import snap.geom.Pos;
import snap.geom.RoundRect;
import snap.geom.Shape;
import snap.gfx.Color;
import snap.gfx.Font;
import snap.view.*;

/**
 * This View subclass shows snippets.
 */
public class EntryView<T extends Entry> extends ParentView {

    // The NotebookView
    protected NotebookView  _notebookView;

    // The entry
    private T  _entry;

    // The label
    private Label  _label;

    // The content view
    private View  _content;

    /**
     * Constructor.
     */
    public EntryView(NotebookView aNotebookView, T anEntry)
    {
        super();

        // Set notebookView and entry
        _notebookView = aNotebookView;
        setEntry(anEntry);

        // Basic style config
        setSpacing(10);
        setPadding(5, 5, 5, 5);

        // Create/add entry label
        _label = createLabel();
        addChild(_label);

        // Set label text
        String labelText = getLabelPrefix() + "[" + anEntry.getIndex() + "] = ";
        _label.setText(labelText);

        // Create/add entry textArea (content)
        View contentView = createContentViewForEntry(anEntry);
        setContent(contentView);
    }

    /**
     * Returns the Entry.
     */
    public T getEntry()  { return _entry; }

    /**
     * Sets the Entry.
     */
    public void setEntry(T anEntry)
    {
        _entry = anEntry;
    }

    /**
     * Returns the prefix string for label.
     */
    protected String getLabelPrefix()  { return "In"; }

    /**
     * Returns the content.
     */
    public View getContent()  { return _content; }

    /**
     * Sets the content.
     */
    public void setContent(View aView)
    {
        if (_content != null)
            removeChild(_content);
        _content = aView;
        if (_content != null)
            addChild(_content);
    }

    /**
     * Creates the label.
     */
    protected Label createLabel()
    {
        Label label = new Label();
        label.setFont(Font.Arial12.getItalic());
        label.setTextFill(Color.GRAY6);
        label.setAlign(Pos.CENTER_RIGHT);
        label.setPrefWidth(50);
        return label;
    }

    /**
     * Returns a content view for given response.
     */
    protected View createContentViewForEntry(Entry anEntry)
    {
        String entryText = anEntry.getText();
        TextArea textArea = createTextArea();
        if (entryText != null && entryText.length() > 0)
            textArea.setText(entryText);
        return textArea;
    }

    /**
     * Creates the TextArea.
     */
    protected TextArea createTextArea()
    {
        TextArea textArea = new EntryTextArea();
        textArea.setFill(Color.WHITE);
        textArea.setBorder(Color.GRAY7, 1);
        textArea.setFont(Font.Arial14);
        textArea.setPadding(4, 4, 4, 4);
        textArea.setGrowWidth(true);
        textArea.setMinSize(30, 30);
        return textArea;
    }

    @Override
    public void requestFocus()
    {
        if (_content != null)
            _content.requestFocus();
    }

    @Override
    protected double getPrefWidthImpl(double aH)
    {
        return RowView.getPrefWidth(this, aH);
    }

    @Override
    protected double getPrefHeightImpl(double aW)
    {
        return RowView.getPrefHeight(this, aW);
    }

    @Override
    protected void layoutImpl()
    {
        RowView.layout(this, true);
    }

    /**
     * TextArea subclass for round corners.
     */
    private static class EntryTextArea extends TextArea {

        @Override
        public Shape getBoundsShape()
        {
            return new RoundRect(0,0, getWidth(), getHeight(), 4);
        }
    }
}
