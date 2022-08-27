/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.notebook;
import javakit.shell.JavaTextDocBlock;
import snap.gfx.Color;
import snap.text.TextSel;
import snap.view.*;

/**
 * This View subclass shows snippets.
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
     * Override for custom request TextArea.
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

//        TextArea textArea = super.createTextArea();
//        textArea.setPadding(5, 5, 2, 5);
//        textArea.setFont(new Font("Courier New", 16));
//        textArea.setEditable(true);
//        textArea.addEventFilter(e -> textAreaKeyPressed(e), ViewEvent.Type.KeyPress);
//        return textArea;
    }

    /**
     * Called to submit a request.
     */
    public void submitRequest()
    {
        // Get TextArea.Text and
        JavaEntry javaEntry = getEntry();
        //String text = textArea.getText().trim();
        //request.setText(text);
        //if (!text.equals(textArea.getText()))
        //    textArea.setText(text);

        // Process request
        _notebookView.processRequest(javaEntry);

        // Select text end
        TextArea textArea = getTextArea();
        textArea.setSel(textArea.length());
    }

    /**
     * Called when TextArea gets Shift+Enter.
     */
    private void textAreaKeyPressed(ViewEvent anEvent)
    {
        // Handle Enter
        if (anEvent.getKeyCode() == KeyCode.ENTER) {

            // Shift+Enter always submits
            if (anEvent.isShiftDown()) {
                submitRequest();
                anEvent.consume();
                return;
            }

            // If on empty line beyond first, submit
            TextArea textArea = getTextArea();
            TextSel textSel = textArea.getSel();
            int lineLen = textSel.getStartLine().getString().length();
            if (textSel.getStart() > 0 && lineLen == 0) {
                submitRequest();
                anEvent.consume();
                return;
            }
        }

        // Handle tab key
        else if (anEvent.isTabKey()) {
            _notebookView.handleTabKey(anEvent);
            anEvent.consume();
        }
    }

    /**
     * Handles Escape key in RequestView
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
}
