package brachy.modularui.screen;

import net.minecraft.world.inventory.Slot;

public interface IClickableContainerScreen {

    void modularui$setClickedSlot(Slot slot);

    Slot modularui$getClickedSlot();
}
