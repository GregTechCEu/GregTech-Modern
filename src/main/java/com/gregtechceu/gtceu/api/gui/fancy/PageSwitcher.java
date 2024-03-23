package com.gregtechceu.gtceu.api.gui.fancy;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.*;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.function.Consumer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class PageSwitcher implements IFancyUIProvider {

    private final Consumer<IFancyUIProvider> onPageSwitched;

    private List<IFancyUIProvider> pages = List.of();
    private IFancyUIProvider currentPage = null;

    public PageSwitcher(Consumer<IFancyUIProvider> onPageSwitched) {
        this.onPageSwitched = onPageSwitched;
    }

    public void setPageList(List<IFancyUIProvider> allPages, IFancyUIProvider currentPage) {
        this.pages = allPages;
        this.currentPage = currentPage;
    }

    @Override
    public Widget createMainPage(FancyMachineUIWidget widget) {
        var container = new WidgetGroup(0, 0, 176, 166);

        var scrollableGroup = new DraggableScrollableWidgetGroup(10, 10, 156, 146);
        scrollableGroup.setYScrollBarWidth(8);
        scrollableGroup.setYBarStyle(GuiTextures.SLIDER_BACKGROUND_VERTICAL, GuiTextures.BUTTON);
        container.addWidget(scrollableGroup);

        int currentY = 0;
        for (IFancyUIProvider page : pages) {
            var pageWidget = new WidgetGroup(0, currentY, 146, 24);

            pageWidget.addWidget(new ButtonWidget(0, 0, 146, 24, GuiTextures.BACKGROUND, clickData -> onPageSwitched.accept(page)));
            pageWidget.addWidget(new ImageWidget(2, 2, 20, 20, page.getTabIcon()));
            pageWidget.addWidget(new ImageWidget(24, 2, 118, 20,
                new TextTexture(ChatFormatting.BLACK.toString() + page.getTitle().getString())
                    .setDropShadow(false)
                    .setWidth(118)
                    .setType(TextTexture.TextType.LEFT_ROLL)
            ));

            scrollableGroup.addWidget(pageWidget);
            currentY += 28;
        }

        return container;
    }

    @Override
    public IGuiTexture getTabIcon() {
        return new TextTexture("+").setDropShadow(false).setColor(ChatFormatting.BLACK.getColor());
    }

    @Override
    public Component getTitle() {
        return Component.translatable("gtceu.gui.title_bar.page_switcher");
    }

    @Override
    public boolean hasPlayerInventory() {
        return false;
    }
}
