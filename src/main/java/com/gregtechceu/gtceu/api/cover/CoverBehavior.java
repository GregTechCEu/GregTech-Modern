package com.gregtechceu.gtceu.api.cover;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.factory.CoverUIFactory;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfigurator;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.item.tool.IToolGridHighlight;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.transfer.fluid.IFluidHandlerModifiable;
import com.gregtechceu.gtceu.client.renderer.cover.ICoverRenderer;
import com.gregtechceu.gtceu.client.renderer.cover.IDynamicCoverRenderer;

import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.syncdata.IEnhancedManaged;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.FieldManagedStorage;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.items.IItemHandlerModifiable;

import lombok.Getter;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Represents cover instance attached on the specific side of meta tile entity
 * Cover filters out interaction and logic of meta tile entity
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class CoverBehavior implements IEnhancedManaged, IToolGridHighlight {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(CoverBehavior.class);

    @Getter
    private final FieldManagedStorage syncStorage = new FieldManagedStorage(this);
    public final CoverDefinition coverDefinition;
    public final ICoverable coverHolder;
    public final Direction attachedSide;
    @Getter
    @Persisted
    @DescSynced
    protected ItemStack attachItem = ItemStack.EMPTY;
    @Getter
    @Persisted
    protected int redstoneSignalOutput = 0;

    public CoverBehavior(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        this.coverDefinition = definition;
        this.coverHolder = coverHolder;
        this.attachedSide = attachedSide;
    }

    //////////////////////////////////////
    // ***** Initialization ******//
    //////////////////////////////////////
    @Override
    public void scheduleRenderUpdate() {
        coverHolder.scheduleRenderUpdate();
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void onChanged() {
        var level = coverHolder.getLevel();
        if (level != null && !level.isClientSide && level.getServer() != null) {
            level.getServer().execute(coverHolder::markDirty);
        }
    }

    /**
     * Called on server side to check whether cover can be attached to given cover holder.
     * it will be called before {@link CoverBehavior#onAttached(ItemStack, ServerPlayer)}
     *
     * @return true if cover can be attached, false otherwise
     */
    @MustBeInvokedByOverriders
    public boolean canAttach() {
        var machine = MetaMachine.getMachine(coverHolder.getLevel(), coverHolder.getPos());
        return machine == null ||
                (machine.getDefinition().isAllowCoverOnFront() || !machine.hasFrontFacing() ||
                        coverHolder.getFrontFacing() != attachedSide);
    }

    /**
     * Will be called on server side after the cover attachment to the meta tile entity
     * Cover can change it's internal state here and return initial data as nbt.
     *
     * @param itemStack the item cover was attached from
     */
    public void onAttached(ItemStack itemStack, @Nullable ServerPlayer player) {
        attachItem = itemStack.copy();
        attachItem.setCount(1);
    }

    public void onLoad() {}

    public void onUnload() {}

    //////////////////////////////////////
    // ********** Misc ***********//
    //////////////////////////////////////
    public ItemStack getPickItem() {
        return attachItem;
    }

    /**
     * Append additional drops. It doesn't include itself.
     */
    public List<ItemStack> getAdditionalDrops() {
        return new ArrayList<>();
    }

    /**
     * Called prior to cover removing on the server side
     * Will also be called during machine dismantling, as machine loses installed covers after that
     */
    public void onRemoved() {}

    public void onNeighborChanged(Block block, BlockPos fromPos, boolean isMoving) {}

    public void setRedstoneSignalOutput(int redstoneSignalOutput) {
        if (this.redstoneSignalOutput == redstoneSignalOutput) return;
        this.redstoneSignalOutput = redstoneSignalOutput;
        coverHolder.notifyBlockUpdate();
        coverHolder.markDirty();
    }

    public boolean canConnectRedstone() {
        return false;
    }

    //////////////////////////////////////
    // ******* Interaction *******//
    //////////////////////////////////////
    public InteractionResult onScrewdriverClick(Player playerIn, InteractionHand hand, BlockHitResult hitResult) {
        if (this instanceof IUICover) {
            if (playerIn instanceof ServerPlayer serverPlayer) {
                CoverUIFactory.INSTANCE.openUI(this, serverPlayer);
            }
            return InteractionResult.sidedSuccess(playerIn.level().isClientSide);
        }
        return InteractionResult.PASS;
    }

    public InteractionResult onSoftMalletClick(Player playerIn, InteractionHand hand, BlockHitResult hitResult) {
        return InteractionResult.PASS;
    }

    //////////////////////////////////////
    // ******* Rendering ********//
    //////////////////////////////////////
    /**
     * @return If the pipe this is placed on and a pipe on the other side should be able to connect
     */
    public boolean canPipePassThrough() {
        return true;
    }

    public boolean shouldRenderPlate() {
        return true;
    }

    public @Nullable Supplier<ICoverRenderer> getCoverRenderer() {
        return coverDefinition.getCoverRenderer();
    }

    public @Nullable IFancyConfigurator getConfigurator() {
        return null;
    }

    @Override
    public boolean shouldRenderGrid(Player player, BlockPos pos, BlockState state, ItemStack held,
                                    Set<GTToolType> toolTypes) {
        return toolTypes.contains(GTToolType.CROWBAR) ||
                ((toolTypes.isEmpty() || toolTypes.contains(GTToolType.SCREWDRIVER)) && this instanceof IUICover);
    }

    @Override
    public @Nullable ResourceTexture sideTips(Player player, BlockPos pos, BlockState state, Set<GTToolType> toolTypes,
                                              Direction side) {
        if (toolTypes.contains(GTToolType.CROWBAR)) {
            return GuiTextures.TOOL_REMOVE_COVER;
        }
        if ((toolTypes.isEmpty() || toolTypes.contains(GTToolType.SCREWDRIVER)) && this instanceof IUICover) {
            return GuiTextures.TOOL_COVER_SETTINGS;
        }
        return null;
    }

    /**
     * get Appearance. same as IForgeBlock.getAppearance() / IFabricBlock.getAppearance()
     */
    @Nullable
    public BlockState getAppearance(BlockState sourceState, BlockPos sourcePos) {
        return null;
    }

    public Supplier<IDynamicCoverRenderer> getDynamicRenderer() {
        return () -> null;
    }

    //////////////////////////////////////
    // ******* Capabilities *******//
    //////////////////////////////////////

    @Nullable
    public IItemHandlerModifiable getItemHandlerCap(IItemHandlerModifiable defaultValue) {
        return defaultValue;
    }

    @Nullable
    public IFluidHandlerModifiable getFluidHandlerCap(IFluidHandlerModifiable defaultValue) {
        return defaultValue;
    }
}
