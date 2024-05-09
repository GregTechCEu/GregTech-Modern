package com.gregtechceu.gtceu.api.covers;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.guis.GuiTextures;
import com.gregtechceu.gtceu.api.guis.factory.CoverUIFactory;
import com.gregtechceu.gtceu.api.items.tool.GTToolType;
import com.gregtechceu.gtceu.api.items.tool.IToolGridHighLight;
import com.lowdragmc.lowdraglib.side.fluid.IFluidHandlerModifiable;
import com.gregtechceu.gtceu.client.renderer.cover.ICoverRenderer;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.syncdata.IEnhancedManaged;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.FieldManagedStorage;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import lombok.Getter;
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
import net.neoforged.neoforge.items.IItemHandlerModifiable;

import org.jetbrains.annotations.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Represents cover instance attached on the specific side of meta tile entity
 * Cover filters out interaction and logic of meta tile entity
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class CoverBehavior implements IEnhancedManaged, IToolGridHighLight {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(CoverBehavior.class);

    @Getter
    private final FieldManagedStorage syncStorage = new FieldManagedStorage(this);
    public final CoverDefinition coverDefinition;
    public final ICoverable coverHolder;
    public final Direction attachedSide;
    @Getter @Persisted @DescSynced
    protected ItemStack attachItem = ItemStack.EMPTY;
    @Getter @Persisted
    protected int redstoneSignalOutput = 0;

    public CoverBehavior(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        this.coverDefinition = definition;
        this.coverHolder = coverHolder;
        this.attachedSide = attachedSide;
    }

    //////////////////////////////////////
    //*****     Initialization    ******//
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
    public boolean canAttach() {
        return true;
    }

    /**
     * Will be called on server side after the cover attachment to the meta tile entity
     * Cover can change it's internal state here and return initial data as nbt.
     *
     * @param itemStack the item cover was attached from
     */
    public void onAttached(ItemStack itemStack, ServerPlayer player) {
        attachItem = itemStack.copy();
        attachItem.setCount(1);
    }

    public void onLoad() {

    }

    public void onUnload() {

    }

    //////////////////////////////////////
    //**********     Misc    ***********//
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
    public void onRemoved() {
    }

    public void onNeighborChanged(Block block, BlockPos fromPos, boolean isMoving) {
    }

    public void setRedstoneSignalOutput(int redstoneSignalOutput) {
        this.redstoneSignalOutput = redstoneSignalOutput;
        coverHolder.notifyBlockUpdate();
        coverHolder.markDirty();
    }

    public boolean canConnectRedstone() {
        return false;
    }

    //////////////////////////////////////
    //*******     Interaction    *******//
    //////////////////////////////////////
    public InteractionResult onScrewdriverClick(Player playerIn, InteractionHand hand, BlockHitResult hitResult) {
        if (this instanceof IUICover) {
            if (playerIn instanceof ServerPlayer serverPlayer) {
                CoverUIFactory.INSTANCE.openUI(this, serverPlayer);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    public InteractionResult onSoftMalletClick(Player playerIn, InteractionHand hand, BlockHitResult hitResult) {
        return InteractionResult.PASS;
    }

    //////////////////////////////////////
    //*******     Rendering     ********//
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

    public ICoverRenderer getCoverRenderer() {
        return coverDefinition.getCoverRenderer();
    }

    @Override
    public boolean shouldRenderGrid(Player player, ItemStack held, Set<GTToolType> toolTypes) {
        return toolTypes.contains(GTToolType.CROWBAR) || (toolTypes.contains(GTToolType.SCREWDRIVER ) && this instanceof IUICover);
    }

    @Override
    public ResourceTexture sideTips(Player player, Set<GTToolType> toolTypes, Direction side) {
        if (toolTypes.contains(GTToolType.CROWBAR)) {
            return GuiTextures.TOOL_REMOVE_COVER;
        }
        if (toolTypes.contains(GTToolType.SCREWDRIVER) && this instanceof IUICover) {
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

    //////////////////////////////////////
    //*******    Capabilities    *******//
    //////////////////////////////////////

    // TODO possibly refactor this in the future, using some sort of cross-platform capability-like system

    @org.jetbrains.annotations.Nullable
    public IItemHandlerModifiable getItemTransferCap(IItemHandlerModifiable defaultValue) {
        return defaultValue;
    }

    @org.jetbrains.annotations.Nullable
    public IFluidHandlerModifiable getFluidTransferCap(IFluidHandlerModifiable defaultValue) {
        return defaultValue;
    }
}
