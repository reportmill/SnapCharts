package snapcharts.app;
import rmdraw.app.EditorPane;
import rmdraw.scene.SGDoc;
import snap.view.View;

/**
 *
 */
public class ReportPane extends DocItemPane {

    // The EditorPane
    private EditorPane  _editorPane;

    /**
     * Constructor.
     */
    public ReportPane()
    {
        _editorPane = new EditorPane();
    }

    /**
     * Return Doc.
     */
    public SGDoc getReportDoc()  { return _editorPane.getDoc(); }

    /**
     * Sets the Doc.
     */
    public void setReportDoc(SGDoc aDoc)
    {
        _editorPane.getEditor().setDoc(aDoc);
    }

    /**
     *
     */
    @Override
    protected View createUI()
    {
        return _editorPane.getUI();
    }
}
