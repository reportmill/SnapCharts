/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.app;
import snap.util.Undoer;
import snap.view.MenuBar;
import snap.view.ViewEvent;
import snap.view.ViewOwner;

/**
 * Menu bar for Editor pane.
 */
public class DocPaneMenuBar extends ViewOwner {

    // The DocPane
    private DocPane _dpane;

    /**
     * Creates a new editor pane menu bar.
     */
    public DocPaneMenuBar(DocPane aDP)  { _dpane = aDP; }

    /**
     * Returns the DocPane.
     */
    public DocPane getEditorPane()  { return _dpane; }

    /**
     * Returns the editor.
     */
    //public Editor getEditor()  { return _epane.getEditor(); }

    /**
     * Override to return node as MenuBar.
     */
    public MenuBar getUI()  { return (MenuBar)super.getUI(); }

    /**
     * Initialize UI panel.
     */
    protected void initUI()  { }

    /**
     * Updates the editor's UI.
     */
    protected void resetUI()
    {
        // Get the editor undoer
        Undoer undoer = null;//getEditor().getUndoer();

        // Update UndoMenuItem
        String uTitle = undoer==null || undoer.getUndoSetLast()==null? "Undo" : undoer.getUndoSetLast().getFullUndoTitle();
        setViewValue("UndoMenuItem", uTitle);
        setViewEnabled("UndoMenuItem", undoer!=null && undoer.getUndoSetLast()!=null);

        // Update RedoMenuItem
        String rTitle = undoer==null || undoer.getRedoSetLast()==null? "Redo" : undoer.getRedoSetLast().getFullRedoTitle();
        setViewValue("RedoMenuItem", rTitle);
        setViewEnabled("RedoMenuItem", undoer!=null && undoer.getRedoSetLast()!=null);
    }

    /**
     * Handles changes to the editor's UI controls.
     */
    protected void respondUI(ViewEvent anEvent)
    {
//        // Get editor pane
//        DocPane epane = getEditorPane();
//        Editor editor = getEditor();
//
//        // Handle NewMenuItem, NewButton: Get new editor pane and make visible
//        if (anEvent.equals("NewMenuItem") || anEvent.equals("NewButton")) {
//            EditorPane editorPane = ClassUtils.newInstance(epane).newDocument();
//            editorPane.setWindowVisible(true);
//        }
//
//        // Handle OpenMenuItem, OpenButton: Get new editor pane from open panel and make visible (if created)
//        if (anEvent.equals("OpenMenuItem") || anEvent.equals("OpenButton")) {
//            EditorPane editorPane = ClassUtils.newInstance(epane).open(epane.getUI());
//            if(editorPane!=null)
//                editorPane.setWindowVisible(true);
//        }
//
//        // Handle OpenRecentMenuItem
//        if (anEvent.equals("OpenRecentMenuItem")) {
//            String path = RecentFiles.showPathsPanel(epane.getUI(), "RecentDocuments"); if(path==null) return;
//            WelcomePanel.getShared().openFile(path); //file.getAbsolutePath());
//        }
//
//        // Handle CloseMenuItem
//        if(anEvent.equals("CloseMenuItem")) epane.close();
//
//        // Handle SaveMenuItem, SaveButton, SaveAsMenuItem, RevertMenuItem
//        if (anEvent.equals("SaveMenuItem") || anEvent.equals("SaveButton"))
//            epane.save();
//        if (anEvent.equals("SaveAsMenuItem"))
//            epane.saveAs();
//        if (anEvent.equals("RevertMenuItem"))
//            epane.revert();
//
//        // Handle QuitMenuItem
//        if (anEvent.equals("QuitMenuItem"))
//            epane.quit();
//
//        // Handle Edit menu items
//        if (anEvent.equals("UndoMenuItem") || anEvent.equals("UndoButton")) editor.undo();
//        if (anEvent.equals("RedoMenuItem") || anEvent.equals("RedoButton")) editor.redo();
//        if (anEvent.equals("CutMenuItem") || anEvent.equals("CutButton")) editor.cut();
//        if (anEvent.equals("CopyMenuItem") || anEvent.equals("CopyButton")) editor.copy();
//        if (anEvent.equals("PasteMenuItem") || anEvent.equals("PasteButton")) editor.paste();
//        if (anEvent.equals("SelectAllMenuItem")) editor.selectAll();
//
//        // Edit -> CheckSpellingAsYouTypeMenuItem
//        if (anEvent.equals("CheckSpellingAsYouTypeMenuItem")) {
//            TextEditor.isSpellChecking = anEvent.getBooleanValue();
//            Prefs.get().setValue("SpellChecking", TextEditor.isSpellChecking);
//            editor.repaint();
//        }
//
//        // Edit -> HyphenateTextMenuItem
//        if (anEvent.equals("HyphenateTextMenuItem")) {
//            TextEditor.setHyphenating(anEvent.getBooleanValue());
//            editor.repaint();
//        }
//
//        // Handle Format menu items (use name because anObj may come from popup menu)
//        if (anEvent.equals("FontPanelMenuItem"))
//            epane.getInspector().setVisibleForName(InspectorPane.STYLE_PANE);
//        if (anEvent.equals("BoldMenuItem") || anEvent.equals("BoldButton"))
//            editor.getStyler().setFontBold(!editor.getStyler().getFont().isBold());
//        if (anEvent.equals("ItalicMenuItem") || anEvent.equals("ItalicButton"))
//            editor.getStyler().setFontItalic(!editor.getStyler().getFont().isItalic());
//        if (anEvent.equals("UnderlineMenuItem"))
//            editor.getStyler().setUnderlined(!editor.getStyler().isUnderlined());
//        if (anEvent.equals("OutlineMenuItem"))
//            editor.getStyler().setTextBorder(Border.blackBorder());
//
//        // Handle Shapes menu items (use name because anObj may come from popup menu)
//        String name = anEvent.getName();
//        if (name.equals("GroupMenuItem")) EditorUtils.groupView(editor);
//        if (name.equals("UngroupMenuItem")) EditorUtils.ungroupView(editor);
//
//        // Handle Tools menu items
//        if (anEvent.equals("GalleryPaneMenuItem"))
//            epane.getInspector().setVisibleForName(InspectorPane.GALLERY_PANE);
//        if (anEvent.equals("ViewPaneMenuItem"))
//            epane.getInspector().setVisibleForName(InspectorPane.VIEW_PANE);
//        if (anEvent.equals("StylePaneMenuItem"))
//            epane.getInspector().setVisibleForName(InspectorPane.STYLE_PANE);
    }
}