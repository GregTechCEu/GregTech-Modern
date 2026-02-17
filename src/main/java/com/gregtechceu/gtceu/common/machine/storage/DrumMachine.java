package com.gregtechceu.gtceu.common.machine.storage;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.IDropSaveMachine;
import com.gregtechceu.gtceu.api.machine.trait.AutoOutputTrait;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.utils.ISubscription;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

import com.mojang.blaze3d.MethodsReturnNonnullByDefault;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class DrumMachine extends MetaMachine implements IDropSaveMachine {

    @Getter
    private final int maxStoredFluids;
    @SaveField
    protected final NotifiableFluidTank cache;
    @Nullable
    protected ISubscription exportFluidSubs;
    @SaveField(nbtKey = "Fluid")
    @SyncToClient
    @Getter
    protected FluidStack stored = FluidStack.EMPTY;
    @Getter
    protected final Material material;

    @SaveField
    @SyncToClient
    public final AutoOutputTrait autoOutput;

    public DrumMachine(BlockEntityCreationInfo info, Material material, int maxStoredFluids) {
        super(info);
        this.material = material;
        this.maxStoredFluids = maxStoredFluids;
        this.cache = createCacheFluidHandler();
        this.autoOutput = new AutoOutputTrait(this, List.of(), List.of(cache), false);
        autoOutput.setFluidOutputDirection(Direction.DOWN);
        autoOutput.setFluidOutputDirectionValidator(d -> d == Direction.DOWN);
    }

    //////////////////////////////////////
    // ***** Initialization *****//
    //////////////////////////////////////

    protected NotifiableFluidTank createCacheFluidHandler() {
        return new NotifiableFluidTank(this, 1, maxStoredFluids, IO.BOTH)
                .setFilter(material.getProperty(PropertyKey.FLUID_PIPE));
    }

    @Override
    public void onLoad() {
        super.onLoad();
        updateStoredFluidFromCache();
        this.exportFluidSubs = cache.addChangedListener(this::onFluidChanged);
    }

    private void onFluidChanged() {
        if (!isRemote()) {
            syncDataHolder.markClientSyncFieldDirty("stored");
            updateStoredFluidFromCache();
        }
    }

    private void updateStoredFluidFromCache() {
        FluidStack cachedFluid = cache.getFluidInTank(0);
        this.stored = cachedFluid.isEmpty() ? FluidStack.EMPTY : cachedFluid;
    }

    @Override
    public void onUnload() {
        super.onUnload();
        if (exportFluidSubs != null) {
            exportFluidSubs.unsubscribe();
            exportFluidSubs = null;
        }
    }

    //////////////////////////////////////
    // ****** Fluid Logic *******//
    //////////////////////////////////////

    @Override
    public void saveToItem(CompoundTag tag) {
        tag.put("Fluid", stored.writeToNBT(new CompoundTag()));
    }

    @Override
    public void loadFromItem(CompoundTag tag) {
        if (!tag.contains("Fluid")) {
            stored = FluidStack.EMPTY;
        }
        // "stored" may not be same as cache (due to item's fluid cap). we should update it.
        cache.getStorages()[0].setFluid(stored.copy());
    }

    @Override
    public boolean savePickClone() {
        return false;
    }

    @Override
    public InteractionResult onUse(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand,
                                   BlockHitResult hit) {
        if (!isRemote()) {
            if (FluidUtil.interactWithFluidHandler(player, hand, cache)) {
                return InteractionResult.SUCCESS;
            }
        }
        return super.onUse(state, world, pos, player, hand, hit);
    }

    @Override
    protected InteractionResult onScrewdriverClick(Player player, InteractionHand hand, Direction gridSide,
                                                   BlockHitResult hitResult) {
        autoOutput.setAllowAutoOutputItems(!autoOutput.isAutoOutputItems());
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean saveBreak() {
        return !stored.isEmpty();
    }
}
