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
                mb.name("toArray").returnType(double[].class).save();
                mb.name("of").paramTypes(java.lang.Object[].class).returnType(snapcharts.data.DoubleArray.class).varArgs().save();
                mb.name("filter").paramTypes(java.util.function.DoublePredicate.class).returnType(snapcharts.data.DoubleArray.class).save();
                return mb.name("map").paramTypes(java.util.function.DoubleUnaryOperator.class).returnType(snapcharts.data.DoubleArray.class).buildAll();

            // Handle snapcharts.notebook.ChartsREPL
            case "snapcharts.notebook.ChartsREPL":
                mb.name("doubleArray").paramTypes(java.lang.Object[].class).returnType(snapcharts.data.DoubleArray.class).varArgs().save();
                mb.name("minMaxArray").paramTypes(double.class,double.class,int.class).returnType(snapcharts.data.DoubleArray.class).save();
                mb.name("minMaxArray").paramTypes(double.class,double.class).returnType(snapcharts.data.DoubleArray.class).save();
                mb.name("dataArray").paramTypes(java.lang.Object.class).returnType(snapcharts.data.DataArray.class).save();
                mb.name("dataSet").paramTypes(java.lang.Object[].class).returnType(snapcharts.data.DataSet.class).varArgs().save();
                mb.name("chart").paramTypes(java.lang.Object[].class).returnType(snapcharts.model.Chart.class).varArgs().save();
                return mb.name("chart3D").paramTypes(java.lang.Object[].class).returnType(snapcharts.model.Chart.class).varArgs().buildAll();

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
            case "snapcharts.data.DoubleArray.toArray()":
                return ((snapcharts.data.DoubleArray) anObj).toArray();
            case "snapcharts.data.DoubleArray.of(java.lang.Object[])":
                return snapcharts.data.DoubleArray.of(theArgs);
            case "snapcharts.data.DoubleArray.filter(java.util.function.DoublePredicate)":
                return ((snapcharts.data.DoubleArray) anObj).filter((java.util.function.DoublePredicate) theArgs[0]);
            case "snapcharts.data.DoubleArray.map(java.util.function.DoubleUnaryOperator)":
                return ((snapcharts.data.DoubleArray) anObj).map((java.util.function.DoubleUnaryOperator) theArgs[0]);

            // Handle snapcharts.data.DataArray

            // Handle snapcharts.data.DataSet

            // Handle snapcharts.notebook.ChartsREPL
            case "snapcharts.notebook.ChartsREPL.doubleArray(java.lang.Object[])":
                return snapcharts.notebook.ChartsREPL.doubleArray(theArgs);
            case "snapcharts.notebook.ChartsREPL.minMaxArray(double,double,int)":
                return snapcharts.notebook.ChartsREPL.minMaxArray(doubleVal(theArgs[0]),doubleVal(theArgs[1]),intVal(theArgs[2]));
            case "snapcharts.notebook.ChartsREPL.minMaxArray(double,double)":
                return snapcharts.notebook.ChartsREPL.minMaxArray(doubleVal(theArgs[0]),doubleVal(theArgs[1]));
            case "snapcharts.notebook.ChartsREPL.dataArray(java.lang.Object)":
                return snapcharts.notebook.ChartsREPL.dataArray(theArgs[0]);
            case "snapcharts.notebook.ChartsREPL.dataSet(java.lang.Object[])":
                return snapcharts.notebook.ChartsREPL.dataSet(theArgs);
            case "snapcharts.notebook.ChartsREPL.chart(java.lang.Object[])":
                return snapcharts.notebook.ChartsREPL.chart(theArgs);
            case "snapcharts.notebook.ChartsREPL.chart3D(java.lang.Object[])":
                return snapcharts.notebook.ChartsREPL.chart3D(theArgs);

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
