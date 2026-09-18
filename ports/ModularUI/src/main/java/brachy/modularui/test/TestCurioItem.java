package brachy.modularui.test;

import net.minecraft.world.item.ItemStack;

import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

public class TestCurioItem extends TestItem implements ICurioItem {

    public TestCurioItem(Properties properties) {
        super(properties);
        CuriosApi.registerCurio(this, this);
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        return true;
    }
}
