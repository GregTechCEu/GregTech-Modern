package com.gregtechceu.gtceu.api.machine.fancyconfigurator;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfigurator;
import com.gregtechceu.gtceu.api.gui.widget.PredicatedButtonWidget;
import com.gregtechceu.gtceu.api.machine.feature.IOverclockMachine;
import com.lowdragmc.lowdraglib.gui.editor.Icons;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.ImageWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * @author KilaBash
 * @date 2023/7/1
 * @implNote OverclockFancyConfigurator
 */
public class OverclockFancyConfigurator implements IFancyConfigurator {
    protected IOverclockMachine overclockMachine;
    // runtime
    protected int currentTier;
    @Nullable
    protected WidgetGroup group;
    protected Map<Integer, WidgetGroup> lightGroups;

    public OverclockFancyConfigurator(IOverclockMachine overclockMachine) {
        this.overclockMachine = overclockMachine;
        this.lightGroups = new HashMap<>();
    }

    @Override
    public void detectAndSendChange(BiConsumer<Integer, Consumer<FriendlyByteBuf>> sender) {
        var newTier = overclockMachine.getOverclockTier();
        if (newTier != this.currentTier) {
            this.currentTier = newTier;
            sender.accept(0, buf -> buf.writeVarInt(this.currentTier));
        }
        int min = overclockMachine.getMinOverclockTier();
        int max = overclockMachine.getMaxOverclockTier();
        if (lightGroups.size() != max - min + 1) {
            updateLightButton(min, max);
            sender.accept(1, buf -> {
                buf.writeVarInt(min);
                buf.writeVarInt(max);
            });
        } else {
            for (int i = min; i <= max; i++) {
                if (!lightGroups.containsKey(i)) {
                    updateLightButton(min, max);
                    sender.accept(1, buf -> {
                        buf.writeVarInt(min);
                        buf.writeVarInt(max);
                    });
                    return;
                }
            }
        }
    }

    private void updateLightButton(int min, int max) {
        if  (group != null) {
            for (WidgetGroup light : lightGroups.values()) {
                group.removeWidget(light);
            }
            lightGroups.clear();
            int x = 5;
            for (int tier = min; tier <= max ; tier++) {
                int finalTier = tier;
                var lightGroup = new WidgetGroup(x, 27, 8, 8);
                lightGroup.addWidget(new ButtonWidget(0, 0, 8, 8, null, cd -> {
                    if (!cd.isRemote) {
                        overclockMachine.setOverclockTier(finalTier);
                    }
                }));
                lightGroup.addWidget(new ImageWidget(0, 0, 8, 8, () -> currentTier >= finalTier ? GuiTextures.LIGHT_ON : GuiTextures.LIGHT_OFF));
                lightGroups.put(tier, lightGroup);
                group.addWidget(lightGroup);
                x += 10;
            }
        }
    }

    @Override
    public void readUpdateInfo(int id, FriendlyByteBuf buf) {
        if (id == 0) {
            this.currentTier = buf.readVarInt();
        } else if (id == 1) {
            int min = buf.readVarInt();
            int max = buf.readVarInt();
            updateLightButton(min, max);
        }
    }

    @Override
    public String getTitle() {
        return "gtceu.gui.overclock.title";
    }

    @Override
    public IGuiTexture getIcon() {
        return new TextTexture(GTValues.VNF[this.currentTier]).setDropShadow(false);
    }

    @Override
    public Widget createConfigurator() {
        group = new WidgetGroup(0, 0, 120, 40);
        group.setBackground(GuiTextures.BACKGROUND_INVERSE);
        group.addWidget(new PredicatedButtonWidget(5, 5, 10, 20, new GuiTextureGroup(GuiTextures.BUTTON, Icons.LEFT.copy().scale(0.7f)), cd -> {
            if (!cd.isRemote) {
                overclockMachine.setOverclockTier(currentTier - 1);
            }
        }).setPredicate(() -> currentTier > overclockMachine.getMinOverclockTier()));
        group.addWidget(new ImageWidget(20, 5, 120 - 5 - 10 - 5 - 20, 20, () -> new GuiTextureGroup(GuiTextures.DISPLAY_FRAME, new TextTexture(GTValues.VNF[this.currentTier]))));
        group.addWidget(new PredicatedButtonWidget(120 -5 - 10, 5, 10, 20, new GuiTextureGroup(GuiTextures.BUTTON, Icons.RIGHT.copy().scale(0.7f)), cd -> {
            if (!cd.isRemote) {
                overclockMachine.setOverclockTier(currentTier + 1);
            }
        }).setPredicate(() -> currentTier < overclockMachine.getMaxOverclockTier()));
        return group;
    }



    @Override
    public List<Component> getTooltips() {
        return List.of(Component.translatable(getTitle()), Component.translatable("gtceu.gui.overclock.range", GTValues.VNF[overclockMachine.getMinOverclockTier()], GTValues.VNF[overclockMachine.getMaxOverclockTier()]));
    }
}
