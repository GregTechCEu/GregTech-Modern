package brachy.modularui.widgets.layout;

import brachy.modularui.api.GuiAxis;
import brachy.modularui.api.layout.ILayoutWidget;
import brachy.modularui.api.widget.IParentWidget;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.utils.Alignment;
import brachy.modularui.widget.AbstractScrollWidget;
import brachy.modularui.widget.scroll.HorizontalScrollData;
import brachy.modularui.widget.scroll.ScrollData;
import brachy.modularui.widget.scroll.VerticalScrollData;
import brachy.modularui.widget.sizer.Area;
import brachy.modularui.widget.sizer.Box;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class Grid extends AbstractScrollWidget<IWidget, Grid> implements ILayoutWidget, IParentWidget<IWidget, Grid> {

    private final List<List<IWidget>> matrix = new ArrayList<>();
    @Getter private final Box minElementMargin = new Box();
    @Getter private int minRowHeight = 5, minColWidth = 5;
    @Getter private Alignment alignment = Alignment.Center;
    private boolean dirty = false, unsanitized = false;
    @Getter private boolean collapseDisabledChildren = false;

    public Grid() {
        super(null, null);
    }

    @Override
    public void onInit() {
        super.onInit();
        sanitizeMatrix();
    }

    private int getMarginStart(Area area, GuiAxis axis, int border) {
        int m = 0;
        if (border > -1) {
            m += Math.max(area.getMargin().getStart(axis), this.minElementMargin.getStart(axis));
        } else {
            m += area.getMargin().getStart(axis);
        }
        return m;
    }

    private int getMarginEnd(Area area, GuiAxis axis, int border) {
        int m = 0;
        if (border < 1) {
            m += Math.max(area.getMargin().getEnd(axis), this.minElementMargin.getEnd(axis));
        } else {
            m += area.getMargin().getEnd(axis);
        }
        return m;
    }

    private int getElementWidth(Area area, int border) {
        // border: -1 = start, 0 = none, 1 = end
        return area.width + getMarginStart(area, GuiAxis.X, border) + getMarginEnd(area, GuiAxis.X, border);
    }

    private int getElementHeight(Area area, int border) {
        // border: -1 = start, 0 = none, 1 = end
        return area.height + getMarginStart(area, GuiAxis.Y, border) + getMarginEnd(area, GuiAxis.Y, border);
    }

    private int border(int index, int size) {
        if (index == 0) return -1;
        if (index == size - 1) return 1;
        return 0;
    }

    @Override
    public boolean layoutWidgets() {
        sanitizeMatrix();
        IntList rowSizes = new IntArrayList();
        IntList colSizes = new IntArrayList();

        int i = 0, j;
        for (List<IWidget> row : this.matrix) {
            j = 0;
            rowSizes.add(this.minRowHeight);
            int yBorder = border(i, this.matrix.size());
            for (IWidget child : row) {
                if (i == 0) {
                    colSizes.add(this.minColWidth);
                }
                if (!shouldIgnoreChildSize(child)) {
                    if (!child.resizer().isWidthCalculated() || !child.resizer().isHeightCalculated()) return false;
                    int xBorder = border(j, row.size());
                    rowSizes.set(i, Math.max(rowSizes.getInt(i), getElementHeight(child.getArea(), yBorder)));
                    colSizes.set(j, Math.max(colSizes.getInt(j), getElementWidth(child.getArea(), xBorder)));
                }
                j++;
            }
            i++;
        }

        int x = 0, y = 0;
        for (int r = 0; r < rowSizes.size(); r++) {
            x = 0;
            int height = rowSizes.getInt(r);
            for (int c = 0; c < colSizes.size(); c++) {
                int width = colSizes.getInt(c);
                IWidget child = this.matrix.get(r).get(c);
                if (child != null) {
                    Area area = child.getArea();
                    int xBorder = border(c, colSizes.size());
                    int yBorder = border(r, rowSizes.size());
                    int xs = getMarginStart(area, GuiAxis.X, xBorder);
                    int xe = getMarginEnd(area, GuiAxis.X, xBorder);
                    int ys = getMarginStart(area, GuiAxis.Y, yBorder);
                    int ye = getMarginEnd(area, GuiAxis.Y, yBorder);
                    child.getArea().rx = (int) (x + xs + (width - xs - xe - area.width) * alignment.x);
                    child.getArea().ry = (int) (y + ys + (height - ys - ye - area.height) * alignment.y);
                    child.resizer().setPosResized(true, true);
                }
                x += width;
            }
            y += height;
        }
        if (getScrollArea().getScrollX() != null) {
            getScrollArea().getScrollX().setScrollSize(x);
        }
        if (getScrollArea().getScrollY() != null) {
            getScrollArea().getScrollY().setScrollSize(y);
        }
        return true;
    }

    @Override
    public boolean shouldIgnoreChildSize(IWidget child) {
        return child == null || (this.collapseDisabledChildren && !child.isEnabled());
    }

    @Override
    public @NotNull List<IWidget> getChildren() {
        if (this.dirty) {
            makeFlatList();
            this.dirty = false;
        }
        return super.getChildren();
    }

    private void makeFlatList() {
        super.getChildren().clear();
        super.getChildren().addAll(this.matrix.stream().flatMap(List::stream).filter(Objects::nonNull).toList());
    }

    @Override
    public int getDefaultHeight() {
        sanitizeMatrix();
        int h = 0;
        for (int i = 0; i < this.matrix.size(); i++) {
            List<IWidget> row = this.matrix.get(i);
            int yBorder = border(i, this.matrix.size());
            int rowHeight = 0;
            for (IWidget child : row) {
                if (!shouldIgnoreChildSize(child)) {
                    rowHeight = Math.max(rowHeight, getElementHeight(child.getArea(), yBorder));
                }
            }
            h += Math.max(rowHeight, this.minRowHeight);
        }
        return h;
    }

    @Override
    public int getDefaultWidth() {
        sanitizeMatrix();
        IntList colSizes = new IntArrayList();
        int i = 0, j;
        for (List<? extends IWidget> row : this.matrix) {
            j = 0;
            for (IWidget child : row) {
                if (i == 0) {
                    colSizes.add(this.minColWidth);
                }
                if (!shouldIgnoreChildSize(child)) {
                    int xBorder = border(j, row.size());
                    colSizes.set(j, Math.max(colSizes.getInt(j), getElementWidth(child.getArea(), xBorder)));
                }
                j++;
            }
            i++;
        }
        int w = 0;
        for (int colWidth : colSizes) {
            w += colWidth;
        }
        return w;
    }

    @SuppressWarnings("unchecked")
    public <I extends IWidget> Grid grid(List<List<I>> matrix) {
        this.matrix.clear();
        for (List<I> row : matrix) this.matrix.add((List<IWidget>) row);
        this.dirty = true;
        this.unsanitized = true;
        if (isValid()) {
            foreachGrid(matrix, w -> {
                w.initialise(this, true);
                onChildAdd(w);
            });
        } else {
            foreachGrid(matrix, this::onChildAdd);
        }
        return this;
    }

    public Grid row(List<IWidget> row) {
        this.matrix.add(row);
        this.dirty = true;
        this.unsanitized = true;
        if (isValid()) {
            foreachRow(row, w -> {
                w.initialise(this, true);
                onChildAdd(w);
            });
        } else {
            foreachRow(row, this::onChildAdd);
        }
        return this;
    }

    public Grid row(@NotNull IWidget... row) {
        Objects.requireNonNull(row);
        return row(new ArrayList<>(Arrays.asList(row)));
    }

    @Override
    public boolean addChild(IWidget child, int index) {
        if (child == this || getChildren().contains(child)) {
            return false;
        }
        if (index < 0) {
            index = getChildren().size() + index + 1;
        }
        super.getChildren().add(index, child);
        if (isValid()) {
            child.initialise(this, true);
        }
        onChildAdd(child);
        this.dirty = true;
        this.unsanitized = true;
        return true;
    }

    public Grid child(@Nullable IWidget widget) {
        if (this.matrix.isEmpty()) this.matrix.add(new ArrayList<>());
        this.matrix.get(this.matrix.size() - 1).add(widget);
        this.dirty = true;
        this.unsanitized = true;
        if (widget != null) {
            if (isValid()) {
                widget.initialise(this, true);
            }
            onChildAdd(widget);
        }
        return this;
    }

    public Grid nextRow() {
        this.matrix.add(new ArrayList<>());
        return this;
    }

    public Grid minColWidth(int minColWidth) {
        this.minColWidth = minColWidth;
        return this;
    }

    public Grid minRowHeight(int minRowHeight) {
        this.minRowHeight = minRowHeight;
        return this;
    }

    public Grid alignment(Alignment alignment) {
        this.alignment = alignment;
        return this;
    }

    public Grid scrollable() {
        return scrollable(new VerticalScrollData(), new HorizontalScrollData());
    }

    public Grid scrollable(ScrollData data) {
        getScrollArea().setScrollData(data);
        return this;
    }

    public Grid scrollable(VerticalScrollData data1, HorizontalScrollData data2) {
        getScrollArea().setScrollData(data1);
        getScrollArea().setScrollData(data2);
        return this;
    }

    public Grid minElementMargin(int left, int right, int top, int bottom) {
        this.minElementMargin.all(left, right, top, bottom);
        return getThis();
    }

    public Grid minElementMargin(int horizontal, int vertical) {
        this.minElementMargin.all(horizontal, vertical);
        return getThis();
    }

    public Grid minElementMargin(int all) {
        this.minElementMargin.all(all);
        return getThis();
    }

    public Grid minElementMarginLeft(int val) {
        this.minElementMargin.left(val);
        return getThis();
    }

    public Grid minElementMarginRight(int val) {
        this.minElementMargin.right(val);
        return getThis();
    }

    public Grid minElementMarginTop(int val) {
        this.minElementMargin.top(val);
        return getThis();
    }

    public Grid minElementMarginBottom(int val) {
        this.minElementMargin.bottom(val);
        return getThis();
    }

    /**
     * Configures this widget to collapse row/column if all the child widgets in that axis are disabled.
     */
    public Grid collapseDisabledChildren() {
        this.collapseDisabledChildren = true;
        return getThis();
    }

    /**
     * Creates a grid with a given width and height.
     *
     * @param width         number of elements in each row
     * @param height        number of elements in each column
     * @param widgetCreator A function that maps each pos to a widget. The arguments are (xIndex, yIndex, totalIndex). Can return null.
     * @return this
     */
    public <I extends IWidget> Grid gridOfWidthHeight(int width, int height, GridPosMapper<I> widgetCreator) {
        return grid(createGridOfWidthHeight(width, height, widgetCreator));
    }

    /**
     * Creates a grid with a given total size and height.
     *
     * @param size          total number of elements (width * height)
     * @param height        number of elements in each column
     * @param widgetCreator A function that maps each pos to a widget. The arguments are (xIndex, yIndex, totalIndex). Can return null.
     * @return this
     */
    public <I extends IWidget> Grid gridOfSizeHeight(int size, int height, GridPosMapper<I> widgetCreator) {
        return grid(createGridOfSizeHeight(size, height, widgetCreator));
    }

    /**
     * Creates a grid with a given total size and width.
     *
     * @param size          total number of elements (width * height)
     * @param width         number of elements in each row
     * @param widgetCreator A function that maps each pos to a widget. The arguments are (xIndex, yIndex, totalIndex). Can return null.
     * @return this
     */
    public <I extends IWidget> Grid gridOfSizeWidth(int size, int width, GridPosMapper<I> widgetCreator) {
        return grid(createGridOfSizeWidth(size, width, widgetCreator));
    }

    /**
     * Creates a grid of widgets with a given grid of any type.
     *
     * @param matrix        elements of any type
     * @param widgetCreator A mapper to map each element to a widget. The arguments are (xIndex, yIndex, totalIndex, element). Can return null.
     * @param <T>           type of elements
     * @param <I>           widget type
     * @return this
     */
    public <T, I extends IWidget> Grid gridOfElements(Iterable<? extends Iterable<T>> matrix, GridPosElementMapper<T, I> widgetCreator) {
        return grid(createGridOfElements(matrix, widgetCreator));
    }

    /**
     * Creates a grid of widgets with a given list and row length.
     *
     * @param width number of elements in each row
     * @param list  widgets
     * @param <I>   widget type
     * @return this
     */
    public <I extends IWidget> Grid gridOf(int width, Iterable<I> list) {
        return grid(createGridOfWidthElements(width, list, (x, y, i, e) -> e));
    }

    /**
     * Creates a grid of widgets with a given list and row length.
     *
     * @param width         number of elements in each row
     * @param list          elements of any type
     * @param widgetCreator A mapper to map each element to a widget. The arguments are (xIndex, yIndex, listIndex, element). Can return null.
     * @param <T>           type of elements
     * @param <I>           widget type
     * @return this
     */
    public <T, I extends IWidget> Grid gridOfWidthElements(int width, Iterable<T> list, GridPosElementMapper<T, I> widgetCreator) {
        return grid(createGridOfWidthElements(width, list, widgetCreator));
    }

    public static <T, I extends IWidget> List<List<I>> createGridOfElements(Iterable<? extends Iterable<T>> matrix, GridPosElementMapper<T, I> widgetCreator) {
        List<List<I>> widgetMatrix = new ArrayList<>();
        Iterator<? extends Iterable<T>> colIt = matrix.iterator();
        int r = 0, i = 0;
        while (colIt.hasNext()) {
            List<I> row = new ArrayList<>();
            widgetMatrix.add(row);
            Iterator<T> rowIt = colIt.next().iterator();
            int c = 0;
            while (rowIt.hasNext()) {
                row.add(widgetCreator.apply(c, r, i, rowIt.next()));
                c++;
                i++;
            }
            r++;
        }
        return widgetMatrix;
    }

    public static <T, I extends IWidget> List<List<I>> createGridOfWidthElements(int width, Iterable<T> list, GridPosElementMapper<T, I> widgetCreator) {
        width = Math.max(width, 1);
        List<List<I>> widgetMatrix = new ArrayList<>();
        Iterator<T> it = list.iterator();
        int r = 0, c = 0, i = 0;
        List<I> row = new ArrayList<>();
        widgetMatrix.add(row);
        while (it.hasNext()) {
            row.add(widgetCreator.apply(c, r, i, it.next()));
            i++;
            if (++c == width) {
                r++;
                c = 0;
                row = new ArrayList<>();
                widgetMatrix.add(row);
            }
        }
        return widgetMatrix;
    }

    public static <I extends IWidget> List<List<I>> createGridOfWidthHeight(int width, int height, GridPosMapper<I> widgetCreator) {
        height = Math.max(height, 1);
        width = Math.max(width, 1);
        List<List<I>> matrix = new ArrayList<>();
        for (int i = 0; i < height; i++) {
            List<I> row = new ArrayList<>();
            matrix.add(row);
            for (int j = 0; j < width; j++) {
                row.add(widgetCreator.apply(j, i, i * width + j));
            }
        }
        return matrix;
    }

    public static <I extends IWidget> List<List<I>> createGridOfSizeHeight(int size, int height, GridPosMapper<I> widgetCreator) {
        return createGridOfWidthHeight(calcSize(size, height), height, widgetCreator);
    }

    public static <I extends IWidget> List<List<I>> createGridOfSizeWidth(int size, int width, GridPosMapper<I> widgetCreator) {
        return createGridOfWidthHeight(width, calcSize(size, width), widgetCreator);
    }

    private static int calcSize(int totalSize, int otherSize) {
        return (int) Math.ceil((double) totalSize / otherSize);
    }

    /**
     * Ensures that every row in the matrix has the same size by appending null values to rows with fewer elements than the longest row.
     */
    private void sanitizeMatrix() {
        if (!this.unsanitized) return;
        int maxRowSize = 0;
        for (List<?> row : this.matrix) {
            maxRowSize = Math.max(maxRowSize, row.size());
        }
        for (List<?> row : this.matrix) {
            while (row.size() < maxRowSize) {
                row.add(null);
            }
        }
        this.unsanitized = false;
    }

    private static void foreachRow(Iterable<IWidget> row, Consumer<IWidget> consumer) {
        row.forEach(consumer);
    }

    private static <I extends IWidget> void foreachGrid(Iterable<? extends Iterable<I>> grid, Consumer<IWidget> consumer) {
        for (Iterable<I> row : grid) {
            row.forEach(consumer);
        }
    }

    public interface GridPosMapper<I> {

        @Nullable
        I apply(int xIndex, int yIndex, int index);
    }

    public interface GridPosElementMapper<T, I> {

        @Nullable
        I apply(int xIndex, int yIndex, int index, T element);
    }
}
