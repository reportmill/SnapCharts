/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.notebook;
import javakit.shell.JavaTextDocBlock;
import snap.gfx.Color;
import snap.text.TextSel;
import snap.view.*;

/**
 * This EntryView subclass supports display/edit of a JavaEntry.
 */
public class JavaEntryView extends EntryView<JavaEntry> {

    /**
     * Constructor.
     */
    public JavaEntryView(NotebookView aNotebookView, JavaEntry aJavaEntry)
    {
        super(aNotebookView, aJavaEntry);
    }

    /**
     * Returns the TextArea.
     */
    public TextArea getTextArea()
    {
        View content = getContent();
        return content instanceof TextArea ? (TextArea) content : null;
    }

    /**
     * Override for custom entry TextArea.
     */
    @Override
    protected TextArea createTextArea()
    {
        JavaEntry javaEntry = getEntry();
        JavaTextDocBlock javaBlock = javaEntry.getJavaBlock();
        TextArea textArea = javaBlock.getTextArea();
        textArea.setBorder(Color.GRAY7, 1);
        textArea.setPadding(5, 5, 2, 5);
        textArea.setGrowWidth(true);
        textArea.setMinSize(30, 30);
        textArea.addEventFilter(e -> textAreaKeyPressed(e), ViewEvent.Type.KeyPress);
        return textArea;
    }

    /**
     * Called to submit an entry.
     */
    public void submitEntry()
    {
        // Get JavaEntry and clear response
        JavaEntry javaEntry = getEntry();
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
        else if (anEvent.isBackSpaceKey() || anEvent.isDeleteKey()) {

            // If empty
            if (getTextArea().length() == 0) {

                // If last entry, just focus previous instead
                JavaEntry javaEntry = getEntry();
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
                Response response = getEntry().getResponse();
                if (response != null) {
                    ResponseView responseView = _notebookView.getResponseView(response);
                    if (responseView != null)
                        responseView.setOpacity(.25);
                }
            }
        }
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
}
