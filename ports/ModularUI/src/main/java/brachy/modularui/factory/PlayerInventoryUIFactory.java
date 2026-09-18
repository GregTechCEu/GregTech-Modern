package brachy.modularui.factory;

import brachy.modularui.ModularUI;
import brachy.modularui.api.IUIHolder;
import brachy.modularui.api.MCHelper;
import brachy.modularui.factory.inventory.InventoryType;
import brachy.modularui.factory.inventory.InventoryTypes;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class PlayerInventoryUIFactory extends AbstractUIFactory<PlayerInventoryGuiData<?>> {

    public static final PlayerInventoryUIFactory INSTANCE = new PlayerInventoryUIFactory();

    public void openFromPlayerInventory(Player player, int index) {
        GuiManager.open(
                this, PlayerInventoryGuiData.of(player, InventoryTypes.PLAYER, null, index), verifyServerSide(player));
    }

    public void openFromHand(Player player, InteractionHand hand) {
        openFromPlayerInventory(player,
                hand == InteractionHand.OFF_HAND ? Inventory.SLOT_OFFHAND : player.getInventory().selected);
    }

    public void openFromCurios(Player player, String type, int index) {
        if (!ModularUI.Mods.CURIOS.isLoaded()) {
            throw new IllegalArgumentException("Can't open UI for curio item when curios is not loaded!");
        }
        GuiManager.open(this, PlayerInventoryGuiData.of(player, InventoryTypes.CURIOS, type, index), verifyServerSide(player));
    }

    public <T> void open(Player player, InventoryType<T> type, T context, int index) {
        GuiManager.open(this, PlayerInventoryGuiData.of(player, type, context, index), verifyServerSide(player));
    }

    @OnlyIn(Dist.CLIENT)
    public void openFromPlayerInventoryClient(int index) {
        GuiManager.openFromClient(this,
                PlayerInventoryGuiData.of(MCHelper.getPlayer(), InventoryTypes.PLAYER, null, index));
    }

    @OnlyIn(Dist.CLIENT)
    public void openFromHandClient(InteractionHand hand) {
        openFromPlayerInventoryClient(
                hand == InteractionHand.OFF_HAND ? Inventory.SLOT_OFFHAND :
                        MCHelper.getPlayer().getInventory().selected);
    }

    @OnlyIn(Dist.CLIENT)
    public void openFromCuriosClient(String type, int index) {
        if (!ModularUI.Mods.CURIOS.isLoaded()) {
            throw new IllegalArgumentException("Can't open UI for curios item when curios is not loaded!");
        }
        GuiManager.openFromClient(
                this, PlayerInventoryGuiData.of(MCHelper.getPlayer(), InventoryTypes.CURIOS, type, index));
    }

    @OnlyIn(Dist.CLIENT)
    public <T> void openClient(InventoryType<T> type, T context, int index) {
        GuiManager.openFromClient(this, PlayerInventoryGuiData.of(MCHelper.getPlayer(), type, context, index));
    }

    private PlayerInventoryUIFactory() {
        super(ModularUI.id("player_inventory"));
    }

    @Override
    public @NotNull IUIHolder<PlayerInventoryGuiData<?>> getGuiHolder(PlayerInventoryGuiData<?> data) {
        return Objects.requireNonNull(castUIHolder(data.getUsedItemStack().getItem()), "Item was not a gui holder!");
    }

    @Override
    public void writeGuiData(PlayerInventoryGuiData<?> guiData, RegistryFriendlyByteBuf buffer) {
        guiData.getInventoryType().write(buffer);
        writeContext(buffer, guiData.getInventoryType(), guiData.getContext());
        buffer.writeVarInt(guiData.getSlotIndex());
    }

    private static <T> void writeContext(RegistryFriendlyByteBuf buffer, InventoryType<T> type, Object context) {
        type.writeContext(buffer, type.castContext(context));
    }

    @Override
    public @NotNull PlayerInventoryGuiData<?> readGuiData(Player player, RegistryFriendlyByteBuf buffer) {
        return readContext(player, buffer, InventoryType.read(buffer));
    }

    private static <T> PlayerInventoryGuiData<?> readContext(Player player, RegistryFriendlyByteBuf buffer,
                                                             InventoryType<T> inventoryType) {
        return PlayerInventoryGuiData.of(player, inventoryType, inventoryType.readContext(buffer), buffer.readVarInt());
    }
}
