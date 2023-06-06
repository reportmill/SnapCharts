/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.repl;
import snap.gfx.Color;
import snap.view.ColView;
import snap.view.View;
import snap.view.ViewUtils;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * A TextArea subclass to show code evaluation.
 */
public class EvalView extends ColView {

    // A cache of views for output values
    private Map<Object,View>  _replViewsCache = new HashMap<>();

    /**
     * Constructor.
     */
    public EvalView()
    {
        super();
        setSpacing(6);
        setFill(new Color(.99));

        // Set Padding to match TextArea
        setPadding(5, 5, 5, 5);
    }

    /**
     * Resets the display.
     */
    public void resetDisplay()
    {
        removeChildren();
        _replViewsCache.clear();
    }

    /**
     * Called by shell when there is output.
     */
    public void showObject(Object aValue)
    {
        // synchronized (_outputList) {
        //     if (_outputList.size() + getChildCount() > MAX_OUTPUT_COUNT)
        //         cancelRun();
        //     _outputList.add(aValue);
        //     if (_outputList.size() == 1)
        //         ViewUtils.runLater(() -> showObjectInEventThread());
        // }

        // Add output
        ViewUtils.runLater(() -> showObjectInEventThread(aValue));

        // Yield to show output
        Thread.yield();
    }

    /**
     * Called by shell when there is output.
     */
    private void showObjectInEventThread()
    {
        // synchronized (_outputList) {
        //     for (Object out : _outputList)
        //         processOutputInEventThread(out);
        //     _outputList.clear();
        // }
    }

    /**
     * Called by shell when there is output.
     */
    private void showObjectInEventThread(Object anObj)
    {
        // Get view for output object and add
        View replView = getViewForReplValue(anObj);
        if (!replView.isShowing())
            addChild(replView);
    }

    /**
     * Creates a view for given Repl value.
     */
    protected View getViewForReplValue(Object aValue)
    {
        // Handle simple value: just create/return new view
        if (isSimpleValue(aValue))
            return EvalViewUtils.createBoxViewForValue(aValue);

        // Handle other values: Get cached view and create if not yet cached
        View view = _replViewsCache.get(aValue);
        if (view == null) {
            view = EvalViewUtils.createBoxViewForValue(aValue);
            _replViewsCache.put(aValue, view);
        }

        // Return
        return view;
    }

    /**
     * Returns whether given value is simple (String, Number, Boolean, Character, Date).
     */
    protected boolean isSimpleValue(Object anObj)
    {
        return anObj instanceof Boolean ||
                anObj instanceof Number ||
                anObj instanceof String ||
                anObj instanceof Date;
    }
}
