package brachy.modularui.factory.inventory;

import brachy.modularui.api.MCHelper;
import brachy.modularui.factory.UIFactories;
import brachy.modularui.test.TestItem;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import com.mojang.blaze3d.platform.InputConstants;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.common.NeoForge;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public final class InventoryTypes {

    @ApiStatus.Internal
    public static void init() {
        NeoForge.EVENT_BUS.addListener(InventoryTypes::onKeyInput);
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

    static final Map<String, InventoryType<?>> inventoryTypes = new Object2ObjectOpenHashMap<>();

    public static final Container PLAYER = new Container("player") {

        @Override
        public net.minecraft.world.Container getInventory(Player player) {
            return player.getInventory();
        }
    };

    public static final CuriosHandler CURIOS = CuriosHandler.INSTANCE;

    public static Collection<InventoryType<?>> getAll() {
        return getAllRegistered();
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
    @SuppressWarnings({"unchecked", "rawtypes"})
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
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void visitAll(Player player, InventoryVisitor<?> visitor) {
        for (InventoryType type : getAll()) {
            if (type.visitAll(player, visitor)) {
                return;
            }
        }
    }

    public static Collection<InventoryType<?>> getAllRegistered() {
        return Collections.unmodifiableCollection(inventoryTypes.values());
    }

    public record SlotFindResult<T>(InventoryType<T> type, T context, int slot) {}
}
