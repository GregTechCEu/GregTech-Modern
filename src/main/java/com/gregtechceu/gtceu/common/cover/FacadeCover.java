package com.gregtechceu.gtceu.common.cover;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.pipenet.ITickablePipeNet;
import com.gregtechceu.gtceu.api.syncdata.RequireRerender;
import com.gregtechceu.gtceu.common.item.FacadeItemBehaviour;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FacadeCover extends CoverBehavior {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(FacadeCover.class, CoverBehavior.MANAGED_FIELD_HOLDER);
    @Setter @Getter @DescSynced @Persisted @RequireRerender
    private BlockState facadeState = Blocks.STONE.defaultBlockState();

    public FacadeCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void onAttached(ItemStack itemStack, ServerPlayer player) {
        super.onAttached(itemStack, player);
        var facadeStack = FacadeItemBehaviour.getFacadeStack(itemStack);
        if (facadeStack.getItem() instanceof BlockItem blockItem) {
            facadeState = blockItem.getBlock().defaultBlockState();
        }
    }

    @Override
    public boolean shouldRenderPlate() {
        return facadeState.canOcclude();
    }

    /**
     * @return If the pipe this is placed on and a pipe on the other side should be able to connect
     */
    public boolean blockPipePassThrough() {
        return false;
    }

    @Nullable
    public BlockState getAppearance(BlockState sourceState, BlockPos sourcePos) {
        return facadeState;
    }
}
