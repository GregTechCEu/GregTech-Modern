package brachy.modularui.factory;

import brachy.modularui.utils.NetworkUtils;
import brachy.modularui.value.sync.PanelSyncManager;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * This class and subclasses are holding necessary data to find the exact same GUI on client and server.
 * For example, if the GUI was opened by right-clicking a BlockEntity, then this data needs a world and a block pos.
 * <p>
 * Also see {@link PosGuiData} (useful for BlockEntities) and {@link SidedPosGuiData} (useful for covers from GregTech) and
 * {@link PlayerInventoryGuiData} (useful for GUIs opened by interacting with an item in the players inventory)
 * for default implementations.
 * </p>
 */
public class GuiData {

    @Getter
    private final @NotNull Player player;

    public GuiData(@NotNull Player player) {
        this.player = Objects.requireNonNull(player);
    }

    public Level getLevel() {
        return getPlayer().level();
    }

    public boolean isClient() {
        return NetworkUtils.isClient(this.player);
    }

    public ItemStack getMainHandItem() {
        return this.player.getMainHandItem();
    }

    public ItemStack getOffHandItem() {
        return this.player.getOffhandItem();
    }

    public void addSyncHandlers(PanelSyncManager mainPSM) {}
}
