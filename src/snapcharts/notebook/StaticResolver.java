package snapcharts.notebook;
import javakit.reflect.*;
import java.io.PrintStream;

/**
 * Provide reflection info for TeaVM.
 */
public class StaticResolver extends javakit.reflect.StaticResolver {

    /**
     * Returns the declared fields for given class.
     */
    public JavaField[] getFieldsForClass(Resolver aResolver, String aClassName)
    {
        fb.init(aResolver, aClassName);

        switch (aClassName) {

            // Handle java.lang.System
            case "java.lang.System":
                fb.name("out").type(PrintStream.class).save();
                return fb.name("err").type(PrintStream.class).buildAll();

            // Handle anything else
            default:
                if (_next != null) return _next.getFieldsForClass(aResolver, aClassName);
                return new JavaField[0];
        }
    }

    /**
     * Returns the declared methods for given class.
     */
    public JavaMethod[] getMethodsForClass(Resolver aResolver, String aClassName)
    {
        mb.init(aResolver, aClassName);

        switch (aClassName) {

            // Handle snapcharts.data.DoubleArray
            case "snapcharts.data.DoubleArray":
                mb.name("doubleArray").returnType(double[].class).save();
                mb.name("fromMinMax").paramTypes(double.class,double.class).returnType(snapcharts.data.DoubleArray.class).save();
                mb.name("fromMinMaxCount").paramTypes(double.class,double.class,int.class).returnType(snapcharts.data.DoubleArray.class).save();
                mb.name("clone").returnType(snapcharts.data.DoubleArray.class).save();
                mb.name("clone").returnType(snapcharts.data.DataArray.class).save();
                mb.name("clone").returnType(snapcharts.data.NumberArray.class).save();
                mb.name("clone").returnType(java.lang.Object.class).save();
                mb.name("toArray").returnType(double[].class).save();
                mb.name("of").paramTypes(java.lang.Object[].class).returnType(snapcharts.data.DoubleArray.class).varArgs().save();
                mb.name("filter").paramTypes(java.util.function.DoublePredicate.class).returnType(snapcharts.data.DoubleArray.class).save();
                return mb.name("map").paramTypes(java.util.function.DoubleUnaryOperator.class).returnType(snapcharts.data.DoubleArray.class).buildAll();

            // Handle snapcharts.data.DataArray
            case "snapcharts.data.DataArray":
                return mb.name("length").returnType(int.class).buildAll();

            // Handle snapcharts.data.DataSet
            case "snapcharts.data.DataSet":
                mb.name("clone").returnType(snap.props.PropObject.class).save();
                mb.name("clone").returnType(java.lang.Object.class).save();
                return mb.name("clone").returnType(snapcharts.data.DataSet.class).buildAll();

            // Handle snapcharts.notebook.ChartsREPL
            case "snapcharts.notebook.ChartsREPL":
                mb.name("dataSet").paramTypes(java.lang.Object[].class).returnType(snapcharts.data.DataSet.class).varArgs().save();
                mb.name("chart").paramTypes(java.lang.Object[].class).returnType(snapcharts.model.Chart.class).varArgs().save();
                mb.name("chart3D").paramTypes(java.lang.Object[].class).returnType(snapcharts.model.Chart.class).varArgs().save();
                mb.name("mapXY").paramTypes(snapcharts.data.DoubleArray.class,snapcharts.data.DoubleArray.class,java.util.function.DoubleBinaryOperator.class).returnType(snapcharts.data.DoubleArray.class).save();
                mb.name("mapXY").paramTypes(double[].class,double[].class,java.util.function.DoubleBinaryOperator.class).returnType(snapcharts.data.DoubleArray.class).save();
                mb.name("doubleArray").paramTypes(java.lang.Object[].class).returnType(snapcharts.data.DoubleArray.class).varArgs().save();
                mb.name("dataArray").paramTypes(java.lang.Object.class).returnType(snapcharts.data.DataArray.class).save();
                mb.name("minMaxArray").paramTypes(double.class,double.class,int.class).returnType(snapcharts.data.DoubleArray.class).save();
                mb.name("minMaxArray").paramTypes(double.class,double.class).returnType(snapcharts.data.DoubleArray.class).save();
                mb.name("getTextForSource").paramTypes(java.lang.Object.class).returnType(java.lang.String.class).save();
                return mb.name("getImageForSource").paramTypes(java.lang.Object.class).returnType(snap.gfx.Image.class).buildAll();

            // Handle anything else
            default:
                if (_next != null) return _next.getMethodsForClass(aResolver, aClassName);
                return new JavaMethod[0];
        }
    }

    /**
     * Invokes methods for given method id, object and args.
     */
    public Object invokeMethod(String anId, Object anObj, Object ... theArgs) throws Exception
    {
        switch (anId) {

            // Handle snapcharts.data.DoubleArray
            case "snapcharts.data.DoubleArray.fromMinMax(double,double)":
                return snapcharts.data.DoubleArray.fromMinMax(doubleVal(theArgs[0]),doubleVal(theArgs[1]));
            case "snapcharts.data.DoubleArray.fromMinMaxCount(double,double,int)":
                return snapcharts.data.DoubleArray.fromMinMaxCount(doubleVal(theArgs[0]),doubleVal(theArgs[1]),intVal(theArgs[2]));
            case "snapcharts.data.DoubleArray.clone()":
                return ((snapcharts.data.DoubleArray) anObj).clone();
            case "snapcharts.data.DoubleArray.toArray()":
                return ((snapcharts.data.DoubleArray) anObj).toArray();
            case "snapcharts.data.DoubleArray.of(java.lang.Object[])":
                return snapcharts.data.DoubleArray.of(theArgs);
            case "snapcharts.data.DoubleArray.filter(java.util.function.DoublePredicate)":
                return ((snapcharts.data.DoubleArray) anObj).filter((java.util.function.DoublePredicate) theArgs[0]);
            case "snapcharts.data.DoubleArray.map(java.util.function.DoubleUnaryOperator)":
                return ((snapcharts.data.DoubleArray) anObj).map((java.util.function.DoubleUnaryOperator) theArgs[0]);

            // Handle snapcharts.data.DataArray
            case "snapcharts.data.DataArray.length()":
                return ((snapcharts.data.DataArray) anObj).length();

            // Handle snapcharts.data.DataSet
            case "snapcharts.data.DataSet.clone()":
                return ((snapcharts.data.DataSet) anObj).clone();

            // Handle snapcharts.notebook.ChartsREPL
            case "snapcharts.notebook.ChartsREPL.dataSet(java.lang.Object[])":
                return snapcharts.notebook.ChartsREPL.dataSet(theArgs);
            case "snapcharts.notebook.ChartsREPL.chart(java.lang.Object[])":
                return snapcharts.notebook.ChartsREPL.chart(theArgs);
            case "snapcharts.notebook.ChartsREPL.chart3D(java.lang.Object[])":
                return snapcharts.notebook.ChartsREPL.chart3D(theArgs);
            case "snapcharts.notebook.ChartsREPL.mapXY(snapcharts.data.DoubleArray,snapcharts.data.DoubleArray,java.util.function.DoubleBinaryOperator)":
                return snapcharts.notebook.ChartsREPL.mapXY((snapcharts.data.DoubleArray) theArgs[0],(snapcharts.data.DoubleArray) theArgs[1],(java.util.function.DoubleBinaryOperator) theArgs[2]);
            case "snapcharts.notebook.ChartsREPL.mapXY(double[],double[],java.util.function.DoubleBinaryOperator)":
                return snapcharts.notebook.ChartsREPL.mapXY((double[]) theArgs[0],(double[]) theArgs[1],(java.util.function.DoubleBinaryOperator) theArgs[2]);
            case "snapcharts.notebook.ChartsREPL.doubleArray(java.lang.Object[])":
                return snapcharts.notebook.ChartsREPL.doubleArray(theArgs);
            case "snapcharts.notebook.ChartsREPL.dataArray(java.lang.Object)":
                return snapcharts.notebook.ChartsREPL.dataArray(theArgs[0]);
            case "snapcharts.notebook.ChartsREPL.minMaxArray(double,double,int)":
                return snapcharts.notebook.ChartsREPL.minMaxArray(doubleVal(theArgs[0]),doubleVal(theArgs[1]),intVal(theArgs[2]));
            case "snapcharts.notebook.ChartsREPL.minMaxArray(double,double)":
                return snapcharts.notebook.ChartsREPL.minMaxArray(doubleVal(theArgs[0]),doubleVal(theArgs[1]));
            case "snapcharts.notebook.ChartsREPL.getTextForSource(java.lang.Object)":
                return snapcharts.notebook.ChartsREPL.getTextForSource(theArgs[0]);
            case "snapcharts.notebook.ChartsREPL.getImageForSource(java.lang.Object)":
                return snapcharts.notebook.ChartsREPL.getImageForSource(theArgs[0]);

            // Handle anything else
            default:
                if (_next != null) return _next.invokeMethod(anId, anObj, theArgs);
                throw new NoSuchMethodException("Unknown method: " + anId);
        }
    }

    /**
     * Returns the declared constructors for given class.
     */
    public JavaConstructor[] getConstructorsForClass(Resolver aResolver, String aClassName)
    {
        cb.init(aResolver, aClassName);

        switch (aClassName) {

            // Handle snapcharts.data.DoubleArray
            case "snapcharts.data.DoubleArray":
                return cb.paramTypes(double[].class).buildAll();

            // Handle anything else
            default:
                if (_next != null) return _next.getConstructorsForClass(aResolver, aClassName);
                return cb.save().buildAll();
        }
    }

    /**
     * Invokes constructors for given constructor id and args.
     */
    public Object invokeConstructor(String anId, Object ... theArgs) throws Exception
    {
        switch (anId) {

            // Handle snapcharts.data.DoubleArray
            case "snapcharts.data.DoubleArray(double[])":
                return new snapcharts.data.DoubleArray((double[]) theArgs[0]);

            // Handle anything else
            default:
                if (_next != null) return _next.invokeConstructor(anId, theArgs);
                throw new NoSuchMethodException("Unknown constructor: " + anId);
        }
    }
}
