package com.gregtechceu.gtceu.api.mui.factory;

import com.gregtechceu.gtceu.api.mui.base.IUIHolder;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Sometimes you don't want to open guis which are bound to a BlockEntity or an Item.
 * For example by a command. You are supposed to create one simple factory per GUI to make sure they are same
 * on client and server.
 * These factories are registered automatically.
 */
public class SimpleUIFactory extends AbstractUIFactory<GuiData> {

    private final Supplier<IUIHolder<GuiData>> guiHolderSupplier;
    private IUIHolder<GuiData> guiHolder;

    /**
     * Creates a simple gui factory.
     *
     * @param name      name of the factory
     * @param guiHolder gui holder
     */
    public SimpleUIFactory(ResourceLocation name, IUIHolder<GuiData> guiHolder) {
        this(name, () -> guiHolder);
    }

    /**
     * Creates a simple gui factory.
     *
     * @param name              name of the factory
     * @param guiHolderSupplier a function which retrieves a gui holder. This is only called once and then cached.
     */
    public SimpleUIFactory(ResourceLocation name, Supplier<IUIHolder<GuiData>> guiHolderSupplier) {
        super(name);
        this.guiHolderSupplier = guiHolderSupplier;
        GuiManager.registerFactory(this);
    }

    public void init() {}

    public void open(ServerPlayer player) {
        GuiManager.open(this, new GuiData(player), player);
    }

    @Override
    public void writeGuiData(GuiData guiData, FriendlyByteBuf buffer) {}

    @Override
    public @NotNull GuiData readGuiData(Player player, FriendlyByteBuf buffer) {
        return new GuiData(player);
    }

    @Override
    public @NotNull IUIHolder<GuiData> getGuiHolder(GuiData data) {
        if (this.guiHolder == null) {
            this.guiHolder = this.guiHolderSupplier.get();
            Objects.requireNonNull(this.guiHolder, "IUIHolder must not be null");
        }
        return this.guiHolder;
    }
}
