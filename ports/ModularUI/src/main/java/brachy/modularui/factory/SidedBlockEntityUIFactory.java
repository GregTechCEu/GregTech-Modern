package brachy.modularui.factory;

import brachy.modularui.ModularUI;
import brachy.modularui.api.IUIHolder;
import brachy.modularui.api.MCHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class SidedBlockEntityUIFactory extends AbstractUIFactory<SidedPosGuiData> {

    public static final SidedBlockEntityUIFactory INSTANCE = new SidedBlockEntityUIFactory();

    private SidedBlockEntityUIFactory() {
        super(ModularUI.id("sided_block_entity"));
    }

    public <T extends BlockEntity & IUIHolder<SidedPosGuiData>> void open(Player player, T blockEntity,
                                                                          Direction facing) {
        Objects.requireNonNull(player);
        Objects.requireNonNull(facing);
        BlockEntityUIFactory.verifyBlockEntity(MCHelper.getPlayer(), blockEntity);
        BlockPos pos = blockEntity.getBlockPos();
        SidedPosGuiData data = new SidedPosGuiData(player, pos, facing);
        GuiManager.open(this, data, (ServerPlayer) player);
    }

    public void open(Player player, BlockPos pos, Direction facing) {
        Objects.requireNonNull(player);
        Objects.requireNonNull(pos);
        Objects.requireNonNull(facing);
        SidedPosGuiData data = new SidedPosGuiData(player, pos, facing);
        GuiManager.open(this, data, (ServerPlayer) player);
    }

    @OnlyIn(Dist.CLIENT)
    public <T extends BlockEntity & IUIHolder<SidedPosGuiData>> void openClient(T blockEntity, Direction facing) {
        Objects.requireNonNull(facing);
        BlockEntityUIFactory.verifyBlockEntity(MCHelper.getPlayer(), blockEntity);
        BlockPos pos = blockEntity.getBlockPos();
        SidedPosGuiData data = new SidedPosGuiData(MCHelper.getPlayer(), pos, facing);
        GuiManager.openFromClient(this, data);
    }

    @OnlyIn(Dist.CLIENT)
    public void openClient(BlockPos pos, Direction facing) {
        Objects.requireNonNull(pos);
        Objects.requireNonNull(facing);
        SidedPosGuiData data = new SidedPosGuiData(MCHelper.getPlayer(), pos, facing);
        GuiManager.openFromClient(this, data);
    }

    @Override
    public @NotNull IUIHolder<SidedPosGuiData> getGuiHolder(SidedPosGuiData data) {
        return Objects.requireNonNull(castUIHolder(data.getBlockEntity()), "Found BlockEntity is not a gui holder!");
    }

    @Override
    public boolean canInteractWith(Player player, SidedPosGuiData guiData) {
        return player == guiData.getPlayer() && guiData.getBlockEntity() != null &&
                guiData.getSquaredDistance(player) <= 64;
    }

    @Override
    public void writeGuiData(SidedPosGuiData guiData, RegistryFriendlyByteBuf buffer) {
        buffer.writeBlockPos(guiData.getBlockPos());
        buffer.writeByte(guiData.getSide().get3DDataValue());
    }

    @Override
    public @NotNull SidedPosGuiData readGuiData(Player player, RegistryFriendlyByteBuf buffer) {
        return new SidedPosGuiData(player, buffer.readBlockPos(), Direction.from3DDataValue(buffer.readByte()));
    }
}
