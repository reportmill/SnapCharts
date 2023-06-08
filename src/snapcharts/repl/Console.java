package snapcharts.repl;
import snap.view.View;

/**
 * This class provides an interface to users.
 */
public interface Console {

    /**
     * Shows the given object to user.
     */
    void show(Object anObj);

    /**
     * Resets the console.
     */
    void resetConsole();

    /**
     * Returns the number of items on the console.
     */
    int getItemCount();

    /**
     * Returns the console view.
     */
    View getConsoleView();

    /**
     * Returns the shared console.
     */
    static Console getShared()  { return DefaultConsole.getShared(); }

    /**
     * Sets the shared console.
     */
    static void setShared(Console aConsole)  { DefaultConsole.setShared(aConsole); }
}
