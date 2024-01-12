package com.gregtechceu.gtceu.client.screen;

import com.gregtechceu.gtceu.api.data.worldgen.strata.StrataGenerationType;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.Locale;
import java.util.function.Consumer;

public class CreateStrataWorldScreen extends Screen {
    protected final CreateWorldScreen parent;
    private final Consumer<StrataGenerationType> applySettings;
    @Getter @Setter
    private StrataGenerationType type;
    private Button selectButton;
    private TypeList list;

    public CreateStrataWorldScreen(CreateWorldScreen parent, Consumer<StrataGenerationType> applySettings) {
        super(Component.translatable("createWorld.customize.strata.title"));
        this.parent = parent;
        this.applySettings = applySettings;
        this.type = StrataGenerationType.BLOB;
    }

    @Override
    protected void init() {
        this.list = new TypeList();
        this.addWidget(list);
        selectButton = this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (button) -> {
            this.applySettings.accept(this.type);
            this.minecraft.setScreen(this.parent);
        }).bounds(this.width / 2 - 155, this.height - 28, 150, 20).build());
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, (button) -> {
            this.minecraft.setScreen(this.parent);
        }).bounds(this.width / 2 + 5, this.height - 28, 150, 20).build());
        this.updateButtonValidity(list.getSelected() != null);
    }

    public void updateButtonValidity(boolean valid) {
        this.selectButton.active = valid;
    }

    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        this.list.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 8, 16777215);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private class TypeList extends ObjectSelectionList<TypeList.Entry> {

        public TypeList() {
            super(CreateStrataWorldScreen.this.minecraft, CreateStrataWorldScreen.this.width, CreateStrataWorldScreen.this.height, 80, CreateStrataWorldScreen.this.height - 37, 24);
            for (StrataGenerationType type : StrataGenerationType.values()) {
                this.addEntry(new Entry(type));
            }
        }

        public void setSelected(@Nullable Entry entry) {
            super.setSelected(entry);
            CreateStrataWorldScreen.this.updateButtonValidity(entry != null);
        }

        @OnlyIn(Dist.CLIENT)
        public class Entry extends ObjectSelectionList.Entry<Entry> {
            private static final ResourceLocation STATS_ICON_LOCATION = new ResourceLocation("textures/gui/container/stats_icons.png");
            private final StrataGenerationType type;
            private final Component name;

            public Entry(StrataGenerationType type) {
                this.type = type;
                this.name = Component.translatable("gtceu.strata_type.%s".formatted(type.name().toLowerCase(Locale.ROOT)));
            }

            public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
                guiGraphics.drawString(CreateStrataWorldScreen.this.font, this.name, left + 18 + 5, top + 6, 16777215, false);
            }

            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                if (button == 0) {
                    this.select();
                }

                return false;
            }

            void select() {
                CreateStrataWorldScreen.TypeList.this.setSelected(this);
                CreateStrataWorldScreen.this.type = this.type;
            }

            private void blitSlot(GuiGraphics guiGraphics, int x, int y, Item item) {
                this.blitSlotBg(guiGraphics, x + 1, y + 1);
                guiGraphics.renderFakeItem(new ItemStack(item), x + 2, y + 2);
            }

            private void blitSlotBg(GuiGraphics guiGraphics, int x, int y) {
                guiGraphics.blit(STATS_ICON_LOCATION, x, y, 0, 0.0F, 0.0F, 18, 18, 128, 128);
            }

            public Component getNarration() {
                return Component.translatable("narrator.select", this.name);
            }
        }
    }
}
