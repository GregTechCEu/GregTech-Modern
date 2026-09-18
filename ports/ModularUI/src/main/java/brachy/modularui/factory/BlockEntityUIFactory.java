package brachy.modularui.factory;

import brachy.modularui.ModularUI;
import brachy.modularui.api.IUIHolder;
import brachy.modularui.api.MCHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class BlockEntityUIFactory extends AbstractUIFactory<PosGuiData> {

    public static final BlockEntityUIFactory INSTANCE = new BlockEntityUIFactory();

    private BlockEntityUIFactory() {
        super(ModularUI.id("block_entity"));
    }

    public <T extends BlockEntity & IUIHolder<PosGuiData>> void open(Player player, T blockEntity) {
        Objects.requireNonNull(player);
        verifyBlockEntity(MCHelper.getPlayer(), blockEntity);
        BlockPos pos = blockEntity.getBlockPos();
        PosGuiData data = new PosGuiData(player, pos);
        GuiManager.open(this, data, (ServerPlayer) player);
    }

    public void open(Player player, BlockPos pos) {
        Objects.requireNonNull(player);
        Objects.requireNonNull(pos);
        PosGuiData data = new PosGuiData(player, pos);
        GuiManager.open(this, data, (ServerPlayer) player);
    }

    @OnlyIn(Dist.CLIENT)
    public <T extends BlockEntity & IUIHolder<PosGuiData>> void openClient(T blockEntity) {
        verifyBlockEntity(MCHelper.getPlayer(), blockEntity);
        BlockPos pos = blockEntity.getBlockPos();
        GuiManager.openFromClient(this, new PosGuiData(MCHelper.getPlayer(), pos));
    }

    @OnlyIn(Dist.CLIENT)
    public void openClient(BlockPos pos) {
        Objects.requireNonNull(pos);
        GuiManager.openFromClient(this, new PosGuiData(MCHelper.getPlayer(), pos));
    }

    @Override
    public @NotNull IUIHolder<PosGuiData> getGuiHolder(PosGuiData data) {
        return Objects.requireNonNull(castUIHolder(data.getBlockEntity()), "Found BlockEntity is not a gui holder!");
    }

    @Override
    public boolean canInteractWith(Player player, PosGuiData guiData) {
        return player == guiData.getPlayer() && guiData.getBlockEntity() != null &&
                guiData.getSquaredDistance(player) <= 64;
    }

    @Override
    public void writeGuiData(PosGuiData guiData, RegistryFriendlyByteBuf buffer) {
        buffer.writeBlockPos(guiData.getBlockPos());
    }

    @Override
    public @NotNull PosGuiData readGuiData(Player player, RegistryFriendlyByteBuf buffer) {
        return new PosGuiData(player, buffer.readBlockPos());
    }

    public static void verifyBlockEntity(Player player, BlockEntity blockEntity) {
        Objects.requireNonNull(blockEntity);
        if (blockEntity.isRemoved()) {
            throw new IllegalArgumentException("Can't open invalid BlockEntity GUI!");
        }
        if (player.level() != blockEntity.getLevel()) {
            throw new IllegalArgumentException("BlockEntity must be in same dimension as the player!");
        }
    }
}
