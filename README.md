# SnapCharts
A charting package for Java and JavaScript

# Overview
This project is a Java charting package including designer for creating and generating charts and graphs that are animated and interactive.

# Designer
SnapCharts can be run with a reference to a JSON file or JavaScript object to easily generate a chart, but with one click the full
chart desginer can be opened to edit the chart in the browser or on the desktop.

# Java and JavaScript
SnapCharts is written in Java and is compiled to run on the desktop with the JVM and compiled to JavaScript to run in the browser.

# Package Structure

## SnapCharts 'Model' package

This package holds classes to:

    - Describe a chart and its components
    - Describe and represent datasets
    - Describe chart types and constants

### Chart Components (and properties)

    - Chart: Type
    - Header: Title, Subtitle
    - Axis: Title, Min/Max Bound, ZeroRequired, isLog, TickLength, ...
    - Legend: Showing, Position, ...
    - ChartPart: Abstract superclass of all parts of a chart
    
### Data
 
    - DataSet: Has type
    - DataSetList: Holds a list of DataSets
    - Intervals: Calculates and describes equally spaced divisions for a min/max
    - RawData: Simple representation of raw data
    
### Types and Constants

    - ChartType: LINE, AREA, SCATTER, BAR, PIE, POLAR, CONTOUR, LINE_3D, BAR_3D, PIE_3D
    - AxisType: X, Y, Y2, Y3, Y4, Z
    - AxisBound: Auto, Data, Value
    - DataType: XY, XYZ, IY (indexed Y), CY (labeled Y), CXY, TR, TRZ
    - DataChan: X, Y, Z, I, C, T, R
    - PageDisplay: Single, Continuous
    
## SnapCharts 'Views' package

This package holds View subclasses for ChartParts

    - ChartView: Top level chart view, encapsulates Chart
    - HeaderView: Display chart header, encapsulates Header
    - AxisView: Display chart axes, encapsulates Axis
    - LegendView: Display chart legend, encapsulates Legend
    - ChartPartView: Abstract superclass of all chart part views
    - ChartHelper: The base class for customizations
    
## SnapCharts 'app' package
    
This package holds the main UI classes for the app:

    - DocPane: Top level controller to show whole doc
    - ChartSetPane: Controller to show charts in a page
    - ChartPane: Controller to show/edit an individual chart
    - DataSetPane: Controller to show/edit datasets
    
## SnapCharts 'apptools' package
    
This package holds inspector controllers.

    - ChartInsp: Inspector for basic chart attributes
    - HeaderInsp: Inspector for header
    - AxisInsp: Inspector for axis
    - LegendInsp: Inspector for legend

## SnapCharts 'appmisc' package
    
This package holds less fundamental app components.

    - SamplesPane: Show collection of charts from the website
    - OpenInPlotly: Convert/open selected chart as Plotly chart in browser
    
