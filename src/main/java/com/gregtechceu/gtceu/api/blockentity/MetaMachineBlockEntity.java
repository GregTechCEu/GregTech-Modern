package com.gregtechceu.gtceu.api.blockentity;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;

import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.syncdata.managed.MultiManagedStorage;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class MetaMachineBlockEntity extends BlockEntity implements IMachineBlockEntity {

    public final MultiManagedStorage managedStorage = new MultiManagedStorage();
    @Getter
    public final MetaMachine metaMachine;
    private final long offset = GTValues.RNG.nextInt(20);

    protected MetaMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
        this.metaMachine = getDefinition().createMetaMachine(this);
    }

    public static MetaMachineBlockEntity createBlockEntity(BlockEntityType<?> type, BlockPos pos,
                                                           BlockState blockState) {
        return new MetaMachineBlockEntity(type, pos, blockState);
    }

    public static void onBlockEntityRegister(BlockEntityType<BlockEntity> type) {}

    @Override
    public MultiManagedStorage getRootStorage() {
        return managedStorage;
    }

    @Override
    public boolean triggerEvent(int id, int para) {
        if (id == 1) { // chunk re render
            if (level != null && level.isClientSide) {
                scheduleRenderUpdate();
            }
            return true;
        }
        return false;
    }

    @Override
    protected void applyImplicitComponents(BlockEntity.DataComponentInput componentInput) {
        super.applyImplicitComponents(componentInput);
        metaMachine.applyImplicitComponents(new ExDataComponentInput() {

            @Override
            public @Nullable <T> T get(DataComponentType<T> component) {
                return componentInput.get(component);
            }

            @Override
            public <T> T getOrDefault(DataComponentType<? extends T> component, T defaultValue) {
                return componentInput.getOrDefault(component, defaultValue);
            }
        });
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        metaMachine.collectImplicitComponents(components);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void removeComponentsFromTag(CompoundTag tag) {
        super.removeComponentsFromTag(tag);
        metaMachine.removeItemComponentsFromTag(tag);
    }

    @Override
    public long getOffset() {
        return offset;
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        metaMachine.onUnload();
    }

    @Override
    public void clearRemoved() {
        super.clearRemoved();
        metaMachine.onLoad();
    }

    @Override
    public boolean shouldRenderGrid(Player player, BlockPos pos, BlockState state, ItemStack held,
                                    Set<GTToolType> toolTypes) {
        return metaMachine.shouldRenderGrid(player, pos, state, held, toolTypes);
    }

    @Override
    public @Nullable ResourceTexture sideTips(Player player, BlockPos pos, BlockState state, Set<GTToolType> toolTypes,
                                              ItemStack held, Direction side) {
        return metaMachine.sideTips(player, pos, state, toolTypes, held, side);
    }

    @Override
    public void setChanged() {
        if (getLevel() != null) {
            getLevel().blockEntityChanged(getBlockPos());
        }
    }

    /**
     * Extending interface to make {@link BlockEntity.DataComponentInput} public as it's protected by default.
     */
    public interface ExDataComponentInput extends BlockEntity.DataComponentInput {}
}
