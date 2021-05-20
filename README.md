# SnapCharts
A charting package for Java and JavaScript

# Overview
This project is a Java charting package including designer for creating and generating charts and graphs that are animated and interactive.

# Designer
SnapCharts can be run with a reference to a JSON file or JavaScript object to easily generate a chart, but with one click the full
chart desginer can be opened to edit the chart in the browser or on the desktop.

# Java and JavaScript
SnapCharts is written in Java and is compiled to run on the desktop with the JVM and compiled to JavaScript to run in the browser.

# Build and run from source

    prompt> git clone https://github.com/reportmill/SnapCharts
    prompt> cd SnapCharts
    prompt> ./gradlew build
    prompt> ./gradlew run 

# Package Structure

## Overview

SnapCharts is basically a Model-View-Controller (MVC) app, with each part in an individual package:

    - Model: snapcharts.model pkg
    - View: snapcharts.view pkg
    - Controller: snapcharts.app pkg

## SnapCharts 'Model' package

This package holds classes to represent (1) a chart and its component parts, (2) data and datasets and (3) supporting
constants and types.

### SnapCharts.model Chart classes

    - Chart: Type
    - Header: Title, Subtitle
    - Axis: Title, Min/Max Bound, ZeroRequired, isLog, TickLength, ...
    - Legend: Showing, Position, ...
    - ChartPart: Abstract superclass of all parts of a chart
    
### SnapCharts.model Data classes
 
    - DataSet: High-level representation of chart data with optional transforms, filters, sorting
    - DataSetList: Holds a list of DataSets
    - Intervals: Calculates and describes equally spaced divisions for a min/max
    - DataStore: Low-level representation of chart data
    
### SnapCharts.model Supporting Types and Constants

    - ChartType: LINE, AREA, SCATTER, BAR, PIE, POLAR, CONTOUR, LINE_3D, BAR_3D, PIE_3D
    - AxisType: X, Y, Y2, Y3, Y4, Z
    - AxisBound: Auto, Data, Value
    - DataType: XY, XYZ, IY (indexed Y), CY (labeled Y), CXY, TR, TRZ
    - DataChan: X, Y, Z, I, C, T, R
    - PageDisplay: Single, Continuous
    
## SnapCharts 'Views' package

This package holds View subclasses for ChartParts.

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
    
