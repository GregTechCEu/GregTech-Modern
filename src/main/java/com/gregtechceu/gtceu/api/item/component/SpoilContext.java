package com.gregtechceu.gtceu.api.item.component;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.server.ServerLifecycleHooks;

import lombok.With;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * This class represents the environment in which an item spoils, for example,
 * the level and the block's position, or the entity. This info is used to, for example, spawn an entity when an item spoils.<br>
 * It must include a way to set the slot the itemstack is in.
 */
@With
@NotNullByDefault
public record SpoilContext(@Nullable Level level,
                           @Nullable BlockPos pos,
                           @Nullable Entity entity,
                           Consumer<ItemStack> stackSetter) {

    public static final SpoilContext EMPTY = new SpoilContext(null, null, null, null);

    /**
     * @return the {@link Level} used to determine time to calculate spoilage progress (using
     *         {@link Level#getGameTime()}).
     *         This is usually the Overworld. If it is {@code null}, all spoilage updates are ignored.
     */
    public static @Nullable Level getDefaultLevel() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return null;
        return server.overworld();
    }


    public SpoilContext(@Nullable Level level, @Nullable BlockPos pos, IItemHandler handler, int slot) {
        this(level, pos, null, stackSetterForHandler(handler, slot));
    }

    public SpoilContext(Player player, int slot) {
        this(player.level(), player.blockPosition(), player, stackSetterForPlayerInv(player.getInventory(), slot));
    }

    public SpoilContext(BlockEntity be, IItemHandler handler, int slot) {
        this(be.getLevel(), be.getBlockPos(), null, stackSetterForHandler(handler, slot));
    }

    public void setResultStack(ItemStack stack) {
        stackSetter.accept(stack);
    }

    public static Consumer<ItemStack> stackSetterForHandler(IItemHandler handler, int slot) {
        return stack -> handler.extractItem(slot, 64, false);
    }

    public static Consumer<ItemStack> stackSetterForPlayerInv(Inventory inv, int slot) {
        return stack -> inv.setItem(slot, stack);
    }
}
