/*
 * Copyright (c) 2010, ReportMill Software. All rights reserved.
 */
package snapcharts.appx;
import snapcharts.app.App;
import snapcharts.app.DocPane;
import snapcharts.app.WelcomePanel;

/**
 * Subclass of SnapCharts App to enable reporting and annotations.
 */
public class AppX extends App {

    /**
     * Standard main implementation.
     */
    public static void main(String[] args)
    {
        // Create custom WelcomePane so it gets set as WelcomePanel.Shared
        new WelcomePanelPro();

        // Do normal version
        App.main(args);
    }

    /**
     * A WelcomePanel subclass, customized for SnapChartsPro.
     */
    private static class WelcomePanelPro extends WelcomePanel {

        protected WelcomePanelPro()
        {
            super();
        }

        @Override
        protected DocPane newDocPane()
        {
            return new DocPanePro();
        }
    }
}
