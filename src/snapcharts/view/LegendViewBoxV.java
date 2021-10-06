package snapcharts.view;
import snap.view.ChildView;
import snap.view.ColView;
import snap.view.ViewProxy;
import java.util.Arrays;

/**
 * This class lays out legend entries in columns.
 */
public class LegendViewBoxV extends ChildView {

    double  _maxX;
    double  _maxY;

    @Override
    protected double getPrefWidthImpl(double aH)
    {
        // Get ViewProxy
        ViewProxy<?> viewProxy = getViewProxy();

        LegendView legendView = getParent(LegendView.class);
        double maxH = legendView.getMaxHeight();
        viewProxy.setSize(-1, maxH);

        // Layout and return
        layoutProxy(viewProxy);
        return _maxX;
    }

    @Override
    protected double getPrefHeightImpl(double aW)
    {
        return _maxY;
    }

    @Override
    protected void layoutImpl()
    {
        ViewProxy<?> viewProxy = getViewProxy();
        layoutProxy(viewProxy);
        viewProxy.setBoundsInClient();
    }

    protected void layoutProxy(ViewProxy<?> viewProxy)
    {

        ViewProxy<?>[] childrenAll = viewProxy.getChildren();
        double childX = 0;

        _maxX = _maxY = 0;

        while (true) {

            ColView.layoutProxy(viewProxy);

            int indexOutOfBounds = getIndexOutOfBoundsY(viewProxy);

            ViewProxy<?>[] children = viewProxy.getChildren();
            ViewProxy<?>[] childrenIn = indexOutOfBounds > 0 ? Arrays.copyOfRange(children, 0, indexOutOfBounds) : children;
            ViewProxy<?>[] childrenOut = indexOutOfBounds > 0 ? Arrays.copyOfRange(children, indexOutOfBounds, children.length) : new ViewProxy<?>[0];

            viewProxy.setChildren(childrenIn);
            double maxW = viewProxy.getChildrenMaxXAllWithInsets();

            for (ViewProxy<?> child : childrenIn) {
                child.setX(childX);
                _maxX = Math.max(_maxX, child.getMaxX());
                _maxY = Math.max(_maxY, child.getMaxY());
            }

            if (childrenOut.length == 0)
                break;

            childX += maxW + viewProxy.getSpacing();
            viewProxy.setChildren(childrenOut);
        }

        viewProxy.setChildren(childrenAll);
    }

    protected ViewProxy<?> getViewProxy()
    {
        ViewProxy<?> viewProxy = new ViewProxy<>(this);
        return viewProxy;
    }

    private int getIndexOutOfBoundsY(ViewProxy viewProxy)
    {
        ViewProxy<?>[] children = viewProxy.getChildren();
        for (int i = 0; i < children.length; i++) {
            ViewProxy<?> child = children[i];
            if (child.getMaxY() > viewProxy.getHeight())
                return i;
        }
        return -1;
    }
}
