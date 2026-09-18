package brachy.modularui.widget.sizer;

public class AreaResizer extends StaticResizer {

    private final Area area;

    public AreaResizer(Area area) {
        this.area = area;
    }

    @Override
    public Area getArea() {
        return area;
    }

    @Override
    public String getDebugDisplayName() {
        return "";
    }

    @Override
    public AreaResizer copy() {
        return new AreaResizer(this.area);
    }

    @Override
    public String toString() {
        return "AreaResizer(" + this.area + ")";
    }
}
