/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.repl;
import snap.view.ViewOwner;
import snap.view.ViewUtils;
import java.util.function.Consumer;

/**
 * This is a REPL base class to provide some convenience methods.
 */
public class ReplObject extends QuickCharts {

    // The consumer to take show calls
    private static Consumer<Object> _showHandler;

    /**
     * Conveniences.
     */
    public static void print(Object anObj)  { System.out.print(anObj); }

    /**
     * Conveniences.
     */
    public static void println(Object anObj)  { System.out.println(anObj); }

    /**
     * Show object.
     */
    public static void show(Object anObj)
    {
        // Creates default ShowHandler
        if (_showHandler == null) {
            EvalView evalView = new EvalView();
            _showHandler = obj -> evalView.showObject(obj);

            ViewUtils.runLater(() -> {
                evalView.setPrefSize(700, 900);
                new ViewOwner(evalView).setWindowVisible(true);
            });
        }

        _showHandler.accept(anObj);
    }

    /**
     * Show object.
     */
    public static void setShowHandler(Consumer<Object> showHandler)
    {
        _showHandler = showHandler;
    }
}
