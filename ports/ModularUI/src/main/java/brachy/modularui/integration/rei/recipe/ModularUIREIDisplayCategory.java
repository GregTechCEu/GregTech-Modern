package brachy.modularui.integration.rei.recipe;

import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;

@ApiStatus.Experimental
public abstract class ModularUIREIDisplayCategory<D extends ModularUIREIDisplay>
        implements DisplayCategory<D> {

    @Override
    public List<Widget> setupDisplay(D display, Rectangle bounds) {
        return display.createWidgets(bounds);
    }
}
