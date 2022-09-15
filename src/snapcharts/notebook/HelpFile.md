

# Define data array

## Simple

```
    x = new double[] { 1, 2, 3, 4 }
```

## From Range

```
    x = DoubleArray.fromMinMax(-3, 3)
```

## From Range with count

```
    x = DoubleArray.fromMinMaxCount(-3, 3, 100)
```

## From other array via function

```
    y = DoubleArray.of(x).map(d -> func)
```

# Define dataset

## From data arrays

```
    dataSet = DataSet.of(x, y)
```

# Create Chart

## From data arrays

```
    chart = chart(x,y)
```

## From data set

```
    chart = chart(dataSet)
```

# Create 3D chart

## From data arrays

```
    chart = chart3D(x,y)
```

## From data set

```
    chart = chart3D(dataSet)
```

# Create UI

## Create Button

```
    button = new Button("Hello World")
    button.setPrefSize(100, 25)
```

## Create Slider

```
    slider = new Slider()
```

# Animate UI

## Animate Button

```
    button = new Button("Hello World")
    anim = button.getAnim(0)
    anim.getAnim(1000).setScale(3).getAnim(2000).setScale(1)
    anim.getAnim(2000).setRotate(360)
    anim.setLoopCount(4)
    anim.play()
```

