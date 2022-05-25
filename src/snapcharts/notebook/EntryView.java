/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.notebook;
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

    // The TextArea
    protected TextArea  _textArea;

    /**
     * Constructor.
     */
    public EntryView(NotebookView aNotebookView, T anEntry)
    {
        super();
        _notebookView = aNotebookView;

        setPadding(5, 5, 5, 5);

        _textArea = createTextArea();
        addChild(_textArea);

        setEntry(anEntry);
        String text = anEntry.getText();
        _textArea.setText(text);
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
     * Creates the TextArea.
     */
    protected TextArea createTextArea()
    {
        TextArea textArea = new TextArea();
        textArea.setFill(Color.WHITE);
        textArea.setBorder(Color.BLACK, 1);
        textArea.setFont(Font.Arial14);
        textArea.setPadding(4, 4, 4, 4);
        textArea.setMinSize(30, 30);
        return textArea;
    }

    @Override
    public void requestFocus()
    {
        _textArea.requestFocus();
    }

    @Override
    protected double getPrefWidthImpl(double aH)
    {
        return BoxView.getPrefWidth(this, _textArea, aH);
    }

    @Override
    protected double getPrefHeightImpl(double aW)
    {
        return BoxView.getPrefHeight(this, _textArea, aW);
    }

    @Override
    protected void layoutImpl()
    {
        BoxView.layout(this, _textArea, true, true);
    }
}
