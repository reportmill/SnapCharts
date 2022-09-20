/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.notebook;
import javakit.shell.JavaTextDocBlock;
import snap.geom.Pos;
import snap.gfx.Color;
import snap.gfx.Font;
import snap.text.TextSel;
import snap.view.*;

/**
 * This View class supports display/edit of a JavaEntry.
 */
public class JavaEntryView extends ParentView {

    // The NotebookView
    protected NotebookView  _notebookView;

    // The entry
    private JavaEntry  _javaEntry;

    // The content view
    private TextArea  _textArea;

    // The label
    private Label  _label;

    /**
     * Constructor.
     */
    public JavaEntryView(NotebookView aNotebookView, JavaEntry aJavaEntry)
    {
        _notebookView = aNotebookView;
        _javaEntry = aJavaEntry;

        // Basic style config
        setSpacing(10);
        setPadding(5, 5, 5, 5);

        // Create/add entry label
        _label = createLabel();
        addChild(_label);

        // Set label text
        String labelText = "In [" + aJavaEntry.getIndex() + "] = ";
        _label.setText(labelText);

        // Get/add JavaEntry TextArea (content)
        _textArea = getJavaBlockTextArea();
        addChild(_textArea);
    }

    /**
     * Returns the JavaEntry.
     */
    public JavaEntry getJavaEntry()  { return _javaEntry; }

    /**
     * Returns the TextArea.
     */
    public TextArea getTextArea()  { return _textArea; }

    /**
     * Override for custom entry TextArea.
     */
    protected TextArea getJavaBlockTextArea()
    {
        // Get JavaEntry.JavaBlock.TextArea
        JavaEntry javaEntry = getJavaEntry();
        JavaTextDocBlock javaBlock = javaEntry.getJavaBlock();
        TextArea textArea = javaBlock.getTextArea();

        // Do additional config
        textArea.setBorder(Color.GRAY7, 1);
        textArea.setPadding(5, 5, 2, 5);
        textArea.setGrowWidth(true);
        textArea.setMinSize(30, 30);
        textArea.addEventFilter(e -> textAreaKeyPressed(e), ViewEvent.Type.KeyPress);

        // Return
        return textArea;
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
     * Called to submit an entry.
     */
    public void submitEntry()
    {
        // Get JavaEntry and clear response
        JavaEntry javaEntry = getJavaEntry();
        javaEntry.setResponse(null);

        // If entry has trailing empty line, remove it
        JavaTextDocBlock javaBlock = javaEntry.getJavaBlock();
        javaBlock.removeTrailingNewline();

        // Forward to Notebook
        Notebook notebook = javaEntry.getNotebook();
        notebook.submitEntry(javaEntry);

        // Select text end
        TextArea textArea = getTextArea();
        textArea.setSel(textArea.length());
    }

    /**
     * Called when TextArea gets KeyPressed.
     */
    private void textAreaKeyPressed(ViewEvent anEvent)
    {
        // Handle Enter
        if (anEvent.getKeyCode() == KeyCode.ENTER)
            handleEnterAction(anEvent);

        // Handle tab key
        else if (anEvent.isTabKey()) {
            _notebookView.handleTabKey(anEvent);
            anEvent.consume();
        }

        // Handle BackSpace/Delete key: If empty entry but not last, remove it
        else if (anEvent.isBackSpaceKey() || anEvent.isDeleteKey())
            handleDeleteAction(anEvent);
    }

    /**
     * Handles Enter key press.
     */
    public void handleEnterAction(ViewEvent anEvent)
    {
        boolean shouldSubmit = isEnterActionSubmitAction(anEvent);
        if (shouldSubmit) {
            submitEntry();
            anEvent.consume();
            return;
        }
    }

    /**
     * Handles Escape key press.
     */
    public void handleEscapeAction(ViewEvent anEvent)
    {
        // If empty text, just return
        TextArea textArea = getTextArea();
        if (textArea.length() == 0)
            return;

        // If all selected, clear all
        TextSel textSel = textArea.getSel();
        if (textSel.getEnd() - textSel.getStart() == textArea.length()) {
            textArea.replaceChars("");
            anEvent.consume();
        }

        // Otherwise, Select all
        else {
            textArea.selectAll();
            anEvent.consume();
        }
    }

    /**
     * Handle delete/backspace action.
     */
    private void handleDeleteAction(ViewEvent anEvent)
    {
        JavaEntry javaEntry = getJavaEntry();

        // If empty
        if (getTextArea().length() == 0) {

            // If last entry, just focus previous instead
            Notebook notebook = javaEntry.getNotebook();
            if (javaEntry == notebook.getLastEntry()) {
                JavaEntry prevEntry = javaEntry.getPrevEntry();
                if (prevEntry != null) {
                    _notebookView.focusEntry(prevEntry, true);
                    anEvent.consume();
                }
            }

            // Otherwise, remove entry
            else {
                notebook.removeEntry(javaEntry);
                anEvent.consume();
            }
        }

        // Otherwise, dim response
        else {
            Response response = javaEntry.getResponse();
            if (response != null) {
                ResponseView responseView = _notebookView.getResponseView(response);
                if (responseView != null)
                    responseView.setOpacity(.25);
            }
        }
    }

    /**
     * Returns whether enter action should cause submit.
     */
    private boolean isEnterActionSubmitAction(ViewEvent anEvent)
    {
        // Shift+Enter always submits
        if (anEvent.isShiftDown())
            return true;

        // Determine whether should submit: If on empty line beyond first
        TextArea textArea = getTextArea();
        TextSel textSel = textArea.getSel();
        int lineLen = textSel.getStartLine().getString().length();
        if (textSel.getStart() > 0 && lineLen == 0)
            return true;

        // Return false
        return false;
    }

    /**
     * Override to focus content instead.
     */
    @Override
    public void requestFocus()
    {
        if (_textArea != null)
            _textArea.requestFocus();
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
}
