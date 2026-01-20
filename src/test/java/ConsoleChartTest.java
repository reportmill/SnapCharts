import snap.viewx.Console;
import snapcharts.data.DoubleArray;
import static snapcharts.charts.SnapCharts.*;

/**
 * This test just creates a chart and shows it in the snap console.
 */
public class ConsoleChartTest {

    /**
     * Creates an XYZ dataset, a contour chart and a 3D chart and shows them in the console.
     */
    public void test()
    {
        Console console = Console.getShared();

        // Generate X/Y data array from ranges
        var x = DoubleArray.fromMinMax(-3, 3);
        var y = DoubleArray.fromMinMax(-4, 4);

        // Generate Z data array as function of X/Y
        var z = mapXY(x, y, (a,b) -> Math.sin(a) + Math.cos(b));

        // Generate dataset with X/Y/Z
        var dataSet = dataSet(x, y, z);
        console.show(dataSet);

        // Generate contour chart with dataset
        var chart = chart(dataSet);
        console.show(chart);

        // Generate 3D chart with dataset
        var chart3D = chart3D(dataSet);
        console.show(chart3D);
    }

    public static void main(String[] args)
    {
        ConsoleChartTest consoleChartTest = new ConsoleChartTest();
        consoleChartTest.test();
    }
}
