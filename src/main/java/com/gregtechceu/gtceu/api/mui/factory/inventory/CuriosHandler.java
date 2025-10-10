package com.gregtechceu.gtceu.api.mui.factory.inventory;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.utils.NetworkUtils;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.Map;

public class CuriosHandler extends InventoryType<String> {

    public static final CuriosHandler INSTANCE = new CuriosHandler();

    private CuriosHandler() {
        super("curios");
    }

    public static ICuriosItemHandler getCuriosHandler(Player player) {
        return CuriosApi.getCuriosInventory(player)
                .orElseThrow(() -> new IllegalStateException("Curios handler for player not found"));
    }

    @Override
    public boolean isActive() {
        return GTCEu.Mods.isCuriosLoaded();
    }

    @Override
    public ItemStack getStackInSlot(Player player, String context, int index) {
        return getCuriosHandler(player).getStacksHandler(context)
                .map(stacksHandler -> stacksHandler.getStacks().getStackInSlot(index))
                .orElse(ItemStack.EMPTY);
    }

    @Override
    public void setStackInSlot(Player player, String context, int index, ItemStack stack) {
        getCuriosHandler(player).getStacksHandler(context)
                .ifPresent(stacksHandler -> stacksHandler.getStacks().setStackInSlot(index, stack));
    }

    @Override
    public boolean visitAll(Player player, InventoryVisitor<String> visitor) {
        Map<String, ICurioStacksHandler> handlers = getCuriosHandler(player).getCurios();
        for (ICurioStacksHandler handler : handlers.values()) {
            IItemHandlerModifiable itemHandler = handler.getStacks();
            for (int i = 0; i < itemHandler.getSlots(); i++) {
                if (visitor.visit(this, handler.getIdentifier(), i, itemHandler.getStackInSlot(i))) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void writeContext(FriendlyByteBuf byteBuf, String context) {
        NetworkUtils.writeStringSafe(byteBuf, context);
    }

    @Override
    public String readContext(FriendlyByteBuf byteBuf) {
        return NetworkUtils.readStringSafe(byteBuf);
    }
}
