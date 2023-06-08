package snapcharts.repl;
import snap.view.View;
import snap.view.ViewOwner;
import snap.view.ViewUtils;

/**
 * This class is a real implementation of Console.
 */
public class DefaultConsole extends ViewOwner implements Console {

    // The Console view
    private ConsoleView _consoleView;

    // The shared console
    private static Console _shared = null;

    /**
     * Constructor.
     */
    public DefaultConsole()
    {
        super();
        _consoleView = new ConsoleView();

        // Set shared
        if (_shared == null)
            _shared = this;
    }

    /**
     * Shows the given object to user.
     */
    public void show(Object anObj)
    {
        _consoleView.showObject(anObj);
    }

    /**
     * Resets the console.
     */
    public void resetConsole()
    {
        _consoleView.resetDisplay();
    }

    /**
     * Returns the number of items on the console.
     */
    public int getItemCount()
    {
        return _consoleView.getChildCount();
    }

    /**
     * Returns the console view.
     */
    @Override
    public ConsoleView getConsoleView()  { return _consoleView; }

    /**
     * Create UI.
     */
    @Override
    protected View createUI()  { return _consoleView; }

    /**
     * Returns the shared console.
     */
    protected static Console getShared()
    {
        if (_shared != null) return _shared;

        // Create default console
        DefaultConsole defaultConsole = new DefaultConsole();

        // Auto-show console
        ViewUtils.runLater(() -> {
            View consoleView = defaultConsole.getConsoleView();
            consoleView.setPrefSize(700, 900);
            defaultConsole.setWindowVisible(true);
        });

        // Set and return
        return _shared = defaultConsole;
    }

    /**
     * Sets the shared console.
     */
    protected static void setShared(Console aConsole)  { _shared = aConsole; }
}
