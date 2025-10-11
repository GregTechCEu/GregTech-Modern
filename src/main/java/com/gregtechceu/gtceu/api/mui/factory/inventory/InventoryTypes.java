package com.gregtechceu.gtceu.api.mui.factory.inventory;

import com.gregtechceu.gtceu.api.mui.base.MCHelper;
import com.gregtechceu.gtceu.api.mui.factory.UIFactories;
import com.gregtechceu.gtceu.api.mui.test.TestItem;
import com.gregtechceu.gtceu.common.data.GTCreativeModeTabs;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import com.mojang.blaze3d.platform.InputConstants;
import com.tterrag.registrate.util.entry.ItemEntry;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

import static com.gregtechceu.gtceu.common.registry.GTRegistration.REGISTRATE;

public class InventoryTypes {

    public static final ItemEntry<TestItem> TEST_ITEM = REGISTRATE.item("mui_test_item", TestItem::new)
            .tab(GTCreativeModeTabs.ITEM.getKey())
            .register();

    @ApiStatus.Internal
    public static void init() {
        MinecraftForge.EVENT_BUS.addListener(InventoryTypes::onKeyInput);
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (event.getAction() == InputConstants.PRESS && event.getKey() == InputConstants.KEY_NUMPAD2) {
            InventoryTypes.visitAll(MCHelper.getPlayer(), (type, context, index, stackInSlot) -> {
                if (!stackInSlot.isEmpty() && stackInSlot.getItem() instanceof TestItem) {
                    UIFactories.playerInventory().openClient(type, context, index);
                    return true;
                }
                return false;
            });
        }
    }

    public static final Inventory PLAYER = new Inventory("player") {

        @Override
        public Container getInventory(Player player) {
            return player.getInventory();
        }
    };

    public static final CuriosHandler CURIOS = CuriosHandler.INSTANCE;

    public static Collection<InventoryType<?>> getAll() {
        return InventoryType.getAllRegistered();
    }

    public static @Nullable SlotFindResult<?> findFirstStackable(Player player, ItemStack stack, boolean ignoreEmpty) {
        for (InventoryType<?> type : getAll()) {
            if (type instanceof CuriosHandler) continue;
            int i = type.findFirstStackable(player, stack, ignoreEmpty);
            if (i >= 0) return new SlotFindResult<>(type, null, i);
        }
        return null;
    }

    /**
     * Visits all slots where the item can be stacked with the given item of all inventory types, this includes the
     * player inventory, with
     * hotbar, main inventory, armor slots, offhand slot and curios slots if curios is loaded.
     *
     * @param player  the player to visit inventory in
     * @param stack   item stack to check stackability for
     * @param visitor visitor function
     */
    public static void visitAllStackable(Player player, ItemStack stack, InventoryVisitor<?> visitor) {
        for (InventoryType type : getAll()) {
            if (type.visitAllStackable(player, stack, visitor)) {
                return;
            }
        }
    }

    /**
     * Visits all slots of all inventory types, this includes the player inventory, with hotbar, main inventory, armor
     * slots, offhand slot
     * and curios slots if curios is loaded.
     *
     * @param player  the player to visit inventory in
     * @param visitor visitor function
     */
    public static void visitAll(Player player, InventoryVisitor<?> visitor) {
        for (InventoryType type : getAll()) {
            if (type.visitAll(player, visitor)) {
                return;
            }
        }
    }

    public record SlotFindResult<T>(InventoryType<T> type, T context, int slot) {}
}
