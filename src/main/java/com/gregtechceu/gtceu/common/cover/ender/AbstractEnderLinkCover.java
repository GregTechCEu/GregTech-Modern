package com.gregtechceu.gtceu.common.cover.ender;

import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.cover.IUICover;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.factory.CoverUIFactory;
import com.gregtechceu.gtceu.api.gui.widget.EnumSelectorWidget;
import com.gregtechceu.gtceu.api.gui.widget.ToggleButtonWidget;
import com.gregtechceu.gtceu.utils.virtualregistry.EntryTypes;
import com.gregtechceu.gtceu.utils.virtualregistry.VirtualEnderRegistry;
import com.gregtechceu.gtceu.utils.virtualregistry.VirtualEntry;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

public abstract class AbstractEnderLinkCover<T extends VirtualEntry> extends CoverBehavior implements IUICover {

    protected static final Pattern COLOR_INPUT = Pattern.compile("[0-9a-fA-F]*");

    protected T activeEntry = null;
    @Getter
    protected String color = VirtualEntry.DEFAULT_COLOR;
    protected UUID playerUUID = null;
    @DescSynced
    @Getter
    @Setter
    protected boolean isPrivate = false;
    @Getter
    @Setter
    protected boolean workingEnabled = false;
    @Getter
    private IO io = IO.NONE;

    public AbstractEnderLinkCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);
        updateLink();
    }

    protected void updateLink() {
        this.activeEntry = VirtualEnderRegistry.getOrCreateEntry(getOwner(), getType(), createName());
        this.activeEntry.setColor(this.color);
        markAsDirty();
    }

    protected abstract EntryTypes<T> getType();

    protected final String createName() {
        return identifier() + this.color;
    }

    protected abstract String identifier();

    protected final UUID getOwner() {
        return isPrivate ? playerUUID : null;
    }

    private void setPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
        updateLink();
    }

    private void setIO(IO io) {
        this.io = io;
    }

    @Override
    public InteractionResult onScrewdriverClick(Player playerIn, InteractionHand hand, BlockHitResult hitResult) {
        return super.onScrewdriverClick(playerIn, hand, hitResult);
    }

    @Override
    public void onAttached(ItemStack itemStack, ServerPlayer player) {
        super.onAttached(itemStack, player);
        this.playerUUID = player.getUUID();
    }

    public void updateColor(String c) {
        if(c.length() == 8) {
            this.color = c.toUpperCase();
            updateLink();
        }
    }

    @Override
    public Widget createUIWidget() {
        var group = new WidgetGroup(0, 0, 176, 192);

        List<String> names = new ArrayList<>(VirtualEnderRegistry.getEntryNames(getOwner(), getType()));

        group.addWidget(createPrivateButton());
        group.addWidget(ioSwitch());

        return null;
    }

    protected Widget createColorIcon() {
        return null;
    }

    protected Widget createPrivateButton() {
        return new ToggleButtonWidget(0, 0, 18, 18, GuiTextures.BUTTON_PUBLIC_PRIVATE, this::isPrivate, this::setPrivate).setTooltipText(isPrivate ?
                        "cover.ender_fluid_link.private.tooltip.enabled":
                       "cover.ender_fluid_link.private.tooltip.disabled.0");
    }

    private Widget p;

    protected Widget ioSwitch() {
        p = new SwitchWidget(10, 45, 20, 20,
                (clickData, value) -> {
                    setIO(value ? IO.IN : IO.OUT);
                    p.setHoverTooltips(
                            LocalizationUtils.format("cover.conveyor.mode", LocalizationUtils.format(io.tooltip)));
                })
                .setTexture(
                        new GuiTextureGroup(GuiTextures.VANILLA_BUTTON, IO.OUT.icon),
                        new GuiTextureGroup(GuiTextures.VANILLA_BUTTON, IO.IN.icon))
                .setPressed(io == IO.IN)
                .setHoverTooltips(
                        LocalizationUtils.format("cover.conveyor.mode", LocalizationUtils.format(io.tooltip)));
        return p;
    }

}
