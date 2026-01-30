# Widget Layout and Positioning

This page explains how MUI2 lays out widgets, how to position them, and how spacing works.

## Coordinate system
- Widget coordinates are local to the widget; (0, 0) is the **top-left** corner of the widget.
- Positions are relative to the parent by default.

## Flow layout (Row / Column)
`Flow` is the core layout widget for arranging children along one axis.

- `Flow.row()` lays out children **left-to-right** (X axis).
- `Flow.column()` lays out children **top-to-bottom** (Y axis).
- `mainAxisAlignment(...)` controls how children are distributed on the main axis:
    - `START`, `CENTER`, `END`, `SPACE_BETWEEN`, `SPACE_AROUND`
    - e.g. `Flow.row().mainAxisAlignment(mainAxisAlignment.CENTER)` centers the row's children horizontally, given that it has a set width
- `crossAxisAlignment(...)` controls alignment on the cross axis:
    - `START`, `CENTER`, `END`
    - e.g. `Flow.row().crossAxisAlignment(crossAxisAlignment.CENTER)` centers the row's children vertically, given that it has a set height
- `childPadding(int)` adds fixed spacing *between* children.
- `coverChildren()` / `coverChildrenWidth()` / `coverChildrenHeight()` sizes the flow to fit its children.

Notes:

- Centering (main or cross axis) requires the flow to have a known size on that axis. If you want a row to center its children horizontally, give it a width (e.g. `widthRel(1f)` or `width(120)`).  
- If a flow is set to `coverChildren()` it naturally sizes to its children, so `mainAxisAlignment` behaves like START.  
- By default, a `Flow` is `sizeRel(1, 1)`, which means they take up as much space as their parents size.

## Positioning (left / right / top / bottom)
MUI2 positioning methods come from `IPositioned`:

- Absolute pixel offsets:
    - `left(int px)`, `top(int px)`, `right(int px)`, `bottom(int px)` are offsets from the parent’s edges.
- Relative offsets:
    - `leftRel(float)`, `rightRel(float)`, `topRel(float)`, `bottomRel(float)` is the same as `left(...)` but relative `0.0-1.0` instead of absolute pixels.
- Convenience:
    - `pos(x, y)` is equivalent to `.left(x).top(y)`.
    - `posRel(x, y)` is equivalent to `.leftRel(x).topRel(y)`.

## Sizing
- `width(int)`, `height(int)` set widget size in pixels.
- `widthRel(float)`, `heightRel(float)` set widget size relative to parent (0.0–1.0 of parent).
- `size(w, h)`, `sizeRel(w, h)` are shortcuts for `.width(x).height(y)` and `.widthRel(x).heightRel(y)` respectively
- `fullWidth()`, `fullHeight()`, `full()` are shortcuts for `.widthRel(1)`, `.heightRel(1)` and `.sizeRel(1,1)` respectively.
- `coverChildrenWidth()` / `coverChildrenHeight()` let a parent size itself to cover its children.

## Margin vs padding
Spacing is handled via two different concepts:

- **Margin**: space **outside** a widget. Layouts (like `Flow`) include margins when positioning children.
    - `marginTop(px)`, `marginBottom(px)`, `marginLeft(px)`, `marginRight(px)` set the padding in pixels for the directions
    - `margin(all)`, `margin(horizontal, vertical)`, `margin(left, right, top, bottom)` are shortcuts for the respective methods
- **Padding**: space **inside** a widget. It reduces the content area and affects how children are placed.
    - `paddingTop(px)`, `paddingBottom(px)`, `paddingLeft(px)`, `paddingRight(px)` sets the margin in pixels for the directions
    - `padding(all)`, `padding(horizontal, vertical)`, `padding(left, right, top, bottom)` are shortcuts for the respective methods

## Centering widgets
There are two common ways to center things:

1) **Center a widget within its parent** (positioning)

- `widget.center()` or `widget.align(Alignment.Center)`  
- `widget.horizontalCenter()` / `widget.verticalCenter()`

2) **Center children inside a Row/Column** (layout)  

- `row.mainAxisAlignment(Alignment.MainAxis.CENTER)` to center along the row direction.  
- `row.crossAxisAlignment(Alignment.CrossAxis.CENTER)` to center across the row direction.  
- Remember to give the row/column a size on that axis (e.g. `widthRel(1f)` for a row).  

## Examples

### Simple centered panel with a row of buttons
```java
ModularPanel panel = new ModularPanel("example")
        .size(176, 168);

panel.child(new ParentWidget<>()
        .size(90, 63)
        .align(Alignment.CENTER)
        .child(Flow.row()
                .coverChildren()
                .childPadding(4)
                .child(new ButtonWidget<>().size(16))
                .child(new ButtonWidget<>().size(16))
                .child(new ButtonWidget<>().size(16))));
```

### Column with padding and left-aligned content
```java
Flow column = Flow.column()
        .widthRel(1f)
        .padding(10)
        .crossAxisAlignment(Alignment.CrossAxis.START)
        .child(new TextWidget<>(IKey.str("Title")).marginBottom(4))
        .child(new TextWidget<>(IKey.str("Body")));
```

### Slot grid using absolute positioning
```java
ParentWidget<?> slots = new ParentWidget<>();
for (int y = 0; y < rows; y++) {
    for (int x = 0; x < cols; x++) {
        slots.child(new ItemSlot()
                .left(18 * x)
                .top(18 * y));
    }
}
```
