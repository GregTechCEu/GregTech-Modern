package com.gregtechceu.gtceu.api.gui.widget;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.transfer.fluid.IFluidHandlerModifiable;
import com.gregtechceu.gtceu.client.TooltipsHandler;
import com.gregtechceu.gtceu.integration.xei.entry.fluid.FluidEntryList;
import com.gregtechceu.gtceu.integration.xei.entry.fluid.FluidStackList;
import com.gregtechceu.gtceu.integration.xei.entry.fluid.FluidTagList;
import com.gregtechceu.gtceu.integration.xei.handlers.fluid.CycleFluidEntryHandler;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import com.lowdragmc.lowdraglib.gui.editor.annotation.Configurable;
import com.lowdragmc.lowdraglib.gui.editor.annotation.LDLRegister;
import com.lowdragmc.lowdraglib.gui.editor.configurator.ConfiguratorGroup;
import com.lowdragmc.lowdraglib.gui.editor.configurator.IConfigurableWidget;
import com.lowdragmc.lowdraglib.gui.editor.configurator.WrapperConfigurator;
import com.lowdragmc.lowdraglib.gui.ingredient.IRecipeIngredientSlot;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ProgressTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceBorderTexture;
import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.gui.util.TextFormattingUtil;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.jei.ClickableIngredient;
import com.lowdragmc.lowdraglib.jei.IngredientIO;
import com.lowdragmc.lowdraglib.jei.JEIPlugin;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.FluidActionResult;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.emi.emi.api.neoforge.NeoForgeEmiStack;
import dev.emi.emi.api.stack.EmiIngredient;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryStacks;
import mezz.jei.api.helpers.IPlatformFluidHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("unused")
@LDLRegister(name = "gtm_fluid_slot", group = "widget.gtm_container", priority = 50)
@Accessors(chain = true)
public class TankWidget extends Widget implements IRecipeIngredientSlot, IConfigurableWidget {

    public final static ResourceBorderTexture FLUID_SLOT_TEXTURE = new ResourceBorderTexture(
            "ldlib:textures/gui/fluid_slot.png", 18, 18, 1, 1);

    @Nullable
    @Getter
    protected IFluidHandler fluidTank;
    @Getter
    protected int tank;
    @Configurable(name = "ldlib.gui.editor.name.showAmount")
    @Setter
    protected boolean showAmount;
    @Configurable(name = "ldlib.gui.editor.name.allowClickFilled")
    @Setter
    protected boolean allowClickFilled;
    @Configurable(name = "ldlib.gui.editor.name.allowClickDrained")
    @Setter
    protected boolean allowClickDrained;
    @Configurable(name = "ldlib.gui.editor.name.drawHoverOverlay")
    @Setter
    public boolean drawHoverOverlay = true;
    @Configurable(name = "ldlib.gui.editor.name.drawHoverTips")
    @Setter
    protected boolean drawHoverTips;
    @Configurable(name = "ldlib.gui.editor.name.fillDirection")
    @Setter
    protected ProgressTexture.FillDirection fillDirection = ProgressTexture.FillDirection.ALWAYS_FULL;
    @Setter
    protected BiConsumer<TankWidget, List<Component>> onAddedTooltips;
    @Setter
    @Getter
    protected IngredientIO ingredientIO = IngredientIO.RENDER_ONLY;
    @Setter
    @Getter
    protected float XEIChance = 1f;
    protected FluidStack lastFluidInTank;
    protected int lastTankCapacity;
    @Setter
    protected Runnable changeListener;

    public TankWidget() {
        this(null, 0, 0, 18, 18, true, true);
    }

    @Override
    public void initTemplate() {
        setBackground(FLUID_SLOT_TEXTURE);
        setFillDirection(ProgressTexture.FillDirection.DOWN_TO_UP);
    }

    public TankWidget(IFluidHandler fluidTank, int x, int y, boolean allowClickContainerFilling,
                      boolean allowClickContainerEmptying) {
        this(fluidTank, x, y, 18, 18, allowClickContainerFilling, allowClickContainerEmptying);
    }

    public TankWidget(@Nullable IFluidHandler fluidTank, int x, int y, int width, int height,
                      boolean allowClickContainerFilling, boolean allowClickContainerEmptying) {
        super(new Position(x, y), new Size(width, height));
        this.fluidTank = fluidTank;
        this.tank = 0;
        this.showAmount = true;
        this.allowClickFilled = allowClickContainerFilling;
        this.allowClickDrained = allowClickContainerEmptying;
        this.drawHoverTips = true;
    }

    public TankWidget(IFluidHandler fluidHandler, int tank, int x, int y, boolean allowClickContainerFilling,
                      boolean allowClickContainerEmptying) {
        this(fluidHandler, tank, x, y, 18, 18, allowClickContainerFilling, allowClickContainerEmptying);
    }

    public TankWidget(@Nullable IFluidHandler fluidHandler, int tank, int x, int y, int width, int height,
                      boolean allowClickContainerFilling, boolean allowClickContainerEmptying) {
        super(new Position(x, y), new Size(width, height));
        this.fluidTank = fluidHandler;
        this.tank = tank;
        this.showAmount = true;
        this.allowClickFilled = allowClickContainerFilling;
        this.allowClickDrained = allowClickContainerEmptying;
        this.drawHoverTips = true;
    }

    public TankWidget setFluidTank(IFluidHandler fluidTank) {
        this.fluidTank = fluidTank;
        this.tank = 0;
        if (isClientSideWidget) {
            setClientSideWidget();
        }
        return this;
    }

    public TankWidget setFluidTank(IFluidHandler fluidTank, int tank) {
        this.fluidTank = fluidTank;
        this.tank = tank;
        if (isClientSideWidget) {
            setClientSideWidget();
        }
        return this;
    }

    // for kjs
    public FluidStack getFluid() {
        if (isClientSideWidget || isRemote()) {
            return lastFluidInTank == null ? FluidStack.EMPTY : lastFluidInTank;
        }
        return fluidTank != null ? fluidTank.getFluidInTank(tank) : FluidStack.EMPTY;
    }

    public TankWidget setFluid(FluidStack fluidStack) {
        return setFluid(fluidStack, true);
    }

    public TankWidget setFluid(FluidStack fluidStack, boolean notify) {
        if (fluidTank instanceof IFluidHandlerModifiable modifiable) {
            modifiable.setFluidInTank(tank, fluidStack);
            if (notify) {
                detectAndSendChanges();
            }
        }
        return this;
    }

    @Override
    public TankWidget setClientSideWidget() {
        super.setClientSideWidget();
        if (fluidTank != null) {
            this.lastFluidInTank = fluidTank.getFluidInTank(tank).copy();
        } else {
            this.lastFluidInTank = null;
        }
        this.lastTankCapacity = fluidTank != null ? fluidTank.getTankCapacity(tank) : 0;
        return this;
    }

    public TankWidget setBackground(IGuiTexture background) {
        super.setBackground(background);
        return this;
    }

    private Object convertIngredient(FluidStack fluidStack) {
        if (GTCEu.Mods.isEMILoaded()) {
            return EMICallWrapper.getEMIIngredient(fluidStack, getXEIChance());
        } else if (GTCEu.Mods.isREILoaded()) {
            return REICallWrapper.getREIIngredient(fluidStack);
        } else if (GTCEu.Mods.isJEILoaded() && !fluidStack.isEmpty()) {
            return JEICallWrapper.getJEIFluidClickable(fluidStack, getPosition(), getSize());
        }
        return fluidStack;
    }

    @Nullable
    @Override
    public Object getXEIIngredientOverMouse(double mouseX, double mouseY) {
        if (self().isMouseOverElement(mouseX, mouseY)) {
            if (lastFluidInTank == null || lastFluidInTank.isEmpty()) return null;

            if (fluidTank instanceof CycleFluidEntryHandler entryHandler) {
                return getXEIIngredientsClickable(entryHandler, tank).getFirst();
            }

            return convertIngredient(lastFluidInTank);
        }
        return null;
    }

    @Override
    public List<Object> getXEIIngredients() {
        if (lastFluidInTank == null || lastFluidInTank.isEmpty()) return Collections.emptyList();

        if (fluidTank instanceof CycleFluidEntryHandler entryHandler) {
            return getXEIIngredientsClickable(entryHandler, tank);
        }

        return List.of(convertIngredient(lastFluidInTank));
    }

    private List<Object> getXEIIngredientsClickable(CycleFluidEntryHandler handler, int index) {
        FluidEntryList entryList = handler.getEntry(index);
        if (GTCEu.Mods.isEMILoaded()) {
            return EMICallWrapper.getEMIIngredients(entryList, getXEIChance());
        } else if (GTCEu.Mods.isREILoaded()) {
            return REICallWrapper.getREIIngredients(entryList);
        } else if (GTCEu.Mods.isJEILoaded()) {
            return JEICallWrapper.getJEIIngredientsClickable(entryList, getPosition(), getSize());
        }
        return Collections.emptyList();
    }

    @Override
    public List<Component> getTooltipTexts() {
        List<Component> tooltips = getAdditionalTooltips(new ArrayList<>());
        tooltips.addAll(tooltipTexts);
        return tooltips;
    }

    public List<Component> getAdditionalTooltips(List<Component> list) {
        if (this.onAddedTooltips != null) {
            this.onAddedTooltips.accept(this, list);
        }
        return list;
    }

    @Override
    public List<Component> getFullTooltipTexts() {
        List<Component> tooltips = new ArrayList<>();
        boolean isPhantom = this instanceof PhantomFluidWidget;
        var fluidStack = this.lastFluidInTank;
        if (fluidStack != null && !fluidStack.isEmpty()) {
            tooltips.add(fluidStack.getHoverName());
            if (!isPhantom && showAmount) {
                tooltips.add(
                        Component.translatable("gtceu.fluid.amount",
                                FormattingUtil.formatNumbers(fluidStack.getAmount()),
                                FormattingUtil.formatNumbers(lastTankCapacity)));
            }
            TooltipsHandler.appendFluidTooltips(fluidStack, tooltips::add,
                    TooltipFlag.NORMAL, Item.TooltipContext.of(gui.entityPlayer.level()));
        } else {
            tooltips.add(Component.translatable("gtceu.fluid.empty"));
            if (!isPhantom && showAmount) {
                tooltips.add(Component.translatable("gtceu.fluid.amount", 0,
                        FormattingUtil.formatNumbers(lastTankCapacity)));
            }
        }
        tooltips.addAll(getTooltipTexts());
        return tooltips;
    }

    @Override
    public Object getXEICurrentIngredient() {
        if (lastFluidInTank == null || lastFluidInTank.isEmpty()) return null;
        if (GTCEu.Mods.isJEILoaded()) {
            return JEICallWrapper.getJEIFluidClickable(lastFluidInTank, getPosition(), getSize());
        }
        return null;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
        if (isClientSideWidget && fluidTank != null) {
            FluidStack fluidStack = fluidTank.getFluidInTank(tank);
            int capacity = fluidTank.getTankCapacity(tank);
            if (capacity != lastTankCapacity) {
                this.lastTankCapacity = capacity;
            }
            if (!FluidStack.isSameFluidSameComponents(fluidStack, lastFluidInTank)) {
                this.lastFluidInTank = fluidStack.copy();
            } else if (fluidStack.getAmount() != lastFluidInTank.getAmount()) {
                this.lastFluidInTank.setAmount(fluidStack.getAmount());
            }
        }
        Position pos = getPosition();
        Size size = getSize();
        var renderedFluid = lastFluidInTank;
        if (renderedFluid != null) {
            RenderSystem.disableBlend();
            if (!renderedFluid.isEmpty()) {
                double progress = renderedFluid.getAmount() * 1.0 /
                        Math.max(Math.max(renderedFluid.getAmount(), lastTankCapacity), 1);
                float drawnU = (float) fillDirection.getDrawnU(progress);
                float drawnV = (float) fillDirection.getDrawnV(progress);
                float drawnWidth = (float) fillDirection.getDrawnWidth(progress);
                float drawnHeight = (float) fillDirection.getDrawnHeight(progress);
                int width = size.width - 2;
                int height = size.height - 2;
                int x = pos.x + 1;
                int y = pos.y + 1;
                DrawerHelper.drawFluidForGui(graphics, renderedFluid,
                        (int) (x + drawnU * width), (int) (y + drawnV * height),
                        ((int) (width * drawnWidth)), ((int) (height * drawnHeight)));
            }

            if (showAmount && !renderedFluid.isEmpty()) {
                graphics.pose().pushPose();
                graphics.pose().scale(0.5F, 0.5F, 1);
                String s = TextFormattingUtil.formatLongToCompactStringBuckets(renderedFluid.getAmount(), 3) + "B";
                Font fontRenderer = Minecraft.getInstance().font;
                graphics.drawString(fontRenderer, s,
                        (int) ((pos.x + (size.width / 3f)) * 2 - fontRenderer.width(s) + 21),
                        (int) ((pos.y + (size.height / 3f) + 6) * 2), 0xFFFFFF, true);
                graphics.pose().popPose();
            }

            RenderSystem.enableBlend();
            RenderSystem.setShaderColor(1, 1, 1, 1);
        }
        drawOverlay(graphics, mouseX, mouseY, partialTicks);
        if (drawHoverOverlay && isMouseOverElement(mouseX, mouseY) && getHoverElement(mouseX, mouseY) == this) {
            RenderSystem.colorMask(true, true, true, false);
            DrawerHelper.drawSolidRect(graphics, getPosition().x + 1, getPosition().y + 1, getSize().width - 2,
                    getSize().height - 2, 0x80FFFFFF);
            RenderSystem.colorMask(true, true, true, true);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInForeground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (drawHoverTips && isMouseOverElement(mouseX, mouseY) && getHoverElement(mouseX, mouseY) == this) {
            if (gui != null) {
                gui.getModularUIGui().setHoverTooltip(getFullTooltipTexts(), ItemStack.EMPTY, null, null);
            }
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1f);
        } else {
            super.drawInForeground(graphics, mouseX, mouseY, partialTicks);
        }
    }

    @Override
    public void detectAndSendChanges() {
        if (fluidTank != null) {
            FluidStack fluidStack = fluidTank.getFluidInTank(tank);
            int capacity = fluidTank.getTankCapacity(tank);
            if (capacity != lastTankCapacity) {
                this.lastTankCapacity = capacity;
                writeUpdateInfo(0, buffer -> buffer.writeVarInt(lastTankCapacity));
            }
            if (!FluidStack.isSameFluidSameComponents(fluidStack, lastFluidInTank)) {
                this.lastFluidInTank = fluidStack.copy();
                var tag = fluidStack.save(GTRegistries.builtinRegistry());
                writeUpdateInfo(2, buffer -> buffer.writeNbt(tag));
            } else if (fluidStack.getAmount() != lastFluidInTank.getAmount()) {
                this.lastFluidInTank.setAmount(fluidStack.getAmount());
                writeUpdateInfo(3, buffer -> buffer.writeVarInt(lastFluidInTank.getAmount()));
            } else {
                super.detectAndSendChanges();
                return;
            }
            if (changeListener != null) {
                changeListener.run();
            }
        }
    }

    @Override
    public void writeInitialData(RegistryFriendlyByteBuf buffer) {
        buffer.writeBoolean(fluidTank != null);
        if (fluidTank != null) {
            this.lastTankCapacity = fluidTank.getTankCapacity(tank);
            buffer.writeVarInt(lastTankCapacity);
            FluidStack fluidStack = fluidTank.getFluidInTank(tank);
            this.lastFluidInTank = fluidStack.copy();
            var tag = fluidStack.save(buffer.registryAccess());
            buffer.writeNbt(tag);
        }
    }

    @Override
    public void readInitialData(RegistryFriendlyByteBuf buffer) {
        if (buffer.readBoolean()) {
            this.lastTankCapacity = buffer.readVarInt();
            readUpdateInfo(2, buffer);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void readUpdateInfo(int id, RegistryFriendlyByteBuf buffer) {
        if (id == 0) {
            this.lastTankCapacity = buffer.readVarInt();
        } else if (id == 1) {
            this.lastFluidInTank = null;
        } else if (id == 2) {
            this.lastFluidInTank = FluidStack.parseOptional(buffer.registryAccess(), buffer.readNbt());
        } else if (id == 3 && lastFluidInTank != null) {
            this.lastFluidInTank.setAmount(buffer.readVarInt());
        } else if (id == 4) {
            ItemStack currentStack = gui.getModularUIContainer().getCarried();
            int newStackSize = buffer.readVarInt();
            currentStack.setCount(newStackSize);
            gui.getModularUIContainer().setCarried(currentStack);
        } else {
            super.readUpdateInfo(id, buffer);
            return;
        }
        if (changeListener != null) {
            changeListener.run();
        }
    }

    @Override
    public void handleClientAction(int id, RegistryFriendlyByteBuf buffer) {
        super.handleClientAction(id, buffer);
        if (id == 1) {
            boolean isShiftKeyDown = buffer.readBoolean();
            int clickResult = tryClickContainer(isShiftKeyDown);
            if (clickResult >= 0) {
                writeUpdateInfo(4, buf -> buf.writeVarInt(clickResult));
            }
        }
    }

    private int tryClickContainer(boolean isShiftKeyDown) {
        if (fluidTank == null) return -1;
        Player player = gui.entityPlayer;
        ItemStack currentStack = gui.getModularUIContainer().getCarried();
        var handler = FluidUtil.getFluidHandler(currentStack).orElse(null);
        if (handler == null) return -1;
        int maxAttempts = isShiftKeyDown ? currentStack.getCount() : 1;
        FluidStack initialFluid = fluidTank.getFluidInTank(tank).copy();
        if (allowClickFilled && initialFluid.getAmount() > 0) {
            boolean performedFill = false;
            ItemStack filledResult = ItemStack.EMPTY;
            for (int i = 0; i < maxAttempts; i++) {
                FluidActionResult result = FluidUtil.tryFillContainer(currentStack, fluidTank, Integer.MAX_VALUE, null,
                        false);
                if (!result.isSuccess()) break;
                ItemStack remainingStack = FluidUtil
                        .tryFillContainer(currentStack, fluidTank, Integer.MAX_VALUE, null, true).getResult();
                performedFill = true;

                currentStack.shrink(1);

                if (filledResult.isEmpty()) {
                    filledResult = remainingStack.copy();
                } else if (ItemStack.isSameItemSameComponents(filledResult, remainingStack)) {
                    if (filledResult.getCount() < filledResult.getMaxStackSize())
                        filledResult.grow(1);
                    else
                        player.getInventory().placeItemBackInInventory(remainingStack);
                } else {
                    player.getInventory().placeItemBackInInventory(filledResult);
                    filledResult = remainingStack.copy();
                }
            }
            if (performedFill) {
                SoundEvent soundevent = initialFluid.getFluid().getFluidType().getSound(initialFluid,
                        SoundActions.BUCKET_FILL);
                if (soundevent == null)
                    soundevent = SoundEvents.BUCKET_FILL;
                player.level().playSound(null, player.position().x, player.position().y + 0.5, player.position().z,
                        soundevent, SoundSource.BLOCKS, 1.0F, 1.0F);

                if (currentStack.isEmpty()) {
                    gui.getModularUIContainer().setCarried(filledResult);
                } else {
                    gui.getModularUIContainer().setCarried(currentStack);
                    player.getInventory().placeItemBackInInventory(filledResult);
                }
                return gui.getModularUIContainer().getCarried().getCount();
            }
        }

        if (allowClickDrained) {
            boolean performedEmptying = false;
            ItemStack drainedResult = ItemStack.EMPTY;
            for (int i = 0; i < maxAttempts; i++) {
                int remainingCapacity = fluidTank.getTankCapacity(tank) - fluidTank.getFluidInTank(tank).getAmount();
                FluidActionResult result = FluidUtil.tryEmptyContainer(currentStack, fluidTank, remainingCapacity, null,
                        false);
                if (!result.isSuccess()) break;

                ItemStack remainingStack = FluidUtil
                        .tryEmptyContainer(currentStack, fluidTank, remainingCapacity, null, true)
                        .getResult();
                performedEmptying = true;

                currentStack.shrink(1);

                if (drainedResult.isEmpty()) {
                    drainedResult = remainingStack.copy();
                } else if (ItemStack.isSameItemSameComponents(drainedResult, remainingStack)) {
                    if (drainedResult.getCount() < drainedResult.getMaxStackSize())
                        drainedResult.grow(1);
                    else
                        player.getInventory().placeItemBackInInventory(remainingStack);
                } else {
                    player.getInventory().placeItemBackInInventory(drainedResult);
                    drainedResult = remainingStack.copy();
                }
            }
            var filledFluid = fluidTank.getFluidInTank(tank);
            if (performedEmptying) {
                SoundEvent soundevent = filledFluid.getFluid().getFluidType().getSound(filledFluid,
                        SoundActions.BUCKET_EMPTY);
                if (soundevent == null)
                    soundevent = SoundEvents.BUCKET_EMPTY;
                player.level().playSound(null, player.position().x, player.position().y + 0.5, player.position().z,
                        soundevent, SoundSource.BLOCKS, 1.0F, 1.0F);

                if (currentStack.isEmpty()) {
                    gui.getModularUIContainer().setCarried(drainedResult);
                } else {
                    gui.getModularUIContainer().setCarried(currentStack);
                    player.getInventory().placeItemBackInInventory(drainedResult);
                }
                return gui.getModularUIContainer().getCarried().getCount();
            }
        }

        return -1;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if ((allowClickDrained || allowClickFilled) && isMouseOverElement(mouseX, mouseY)) {
            if (button == 0) {
                if (FluidUtil.getFluidHandler(gui.getModularUIContainer().getCarried()).isPresent()) {
                    boolean isShiftKeyDown = isShiftDown();
                    writeClientAction(1, writer -> writer.writeBoolean(isShiftKeyDown));
                    playButtonClickSound();
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void buildConfigurator(ConfiguratorGroup father) {
        var handler = new FluidTank(5000);
        handler.fill(new FluidStack(Fluids.WATER, 3000), IFluidHandler.FluidAction.EXECUTE);
        father.addConfigurators(new WrapperConfigurator("ldlib.gui.editor.group.preview", new TankWidget() {

            @Override
            public void updateScreen() {
                super.updateScreen();
                setHoverTooltips(TankWidget.this.tooltipTexts);
                this.backgroundTexture = TankWidget.this.backgroundTexture;
                this.hoverTexture = TankWidget.this.hoverTexture;
                this.showAmount = TankWidget.this.showAmount;
                this.drawHoverTips = TankWidget.this.drawHoverTips;
                this.fillDirection = TankWidget.this.fillDirection;
                this.overlay = TankWidget.this.overlay;
            }
        }.setAllowClickDrained(false).setAllowClickFilled(false).setFluidTank(handler)));

        IConfigurableWidget.super.buildConfigurator(father);
    }

    /**
     * Wrapper for methods that use JEI classes so that classloading doesn't brick itself.
     */
    public static final class JEICallWrapper {

        public static List<Object> getJEIIngredientsClickable(FluidEntryList list, Position pos, Size size) {
            return list.getStacks()
                    .stream()
                    .filter(stack -> !stack.isEmpty())
                    .map(stack -> getJEIFluidClickable(stack, pos, size))
                    .toList();
        }

        public static Object getJEIFluidClickable(FluidStack fluidStack, Position pos, Size size) {
            return _getJEIFluidClickable(JEIPlugin.jeiHelpers.getPlatformFluidHelper(), fluidStack, pos,
                    size);
        }

        private static <T> Object _getJEIFluidClickable(IPlatformFluidHelper<T> helper,
                                                        FluidStack fluidStack, Position pos, Size size) {
            T ingredient = helper.create(fluidStack.getFluidHolder(), fluidStack.getAmount(),
                    fluidStack.getComponentsPatch());
            return JEIPlugin.jeiHelpers.getIngredientManager().createTypedIngredient(ingredient)
                    .map(typedIngredient -> new ClickableIngredient<>(typedIngredient, pos.x, pos.y, size.width,
                            size.height))
                    .orElse(null);
        }
    }

    public static final class REICallWrapper {

        public static dev.architectury.fluid.FluidStack toREIStack(FluidStack stack) {
            return dev.architectury.fluid.FluidStack.create(stack.getFluid(), stack.getAmount(),
                    stack.getComponentsPatch());
        }

        private static EntryIngredient toREIIngredient(Stream<FluidStack> stream) {
            return EntryIngredient.of(stream
                    .map(REICallWrapper::toREIStack)
                    .map(EntryStacks::of)
                    .toList());
        }

        public static List<Object> getREIIngredients(FluidStackList list) {
            return List.of(toREIIngredient(list.stream()));
        }

        public static List<Object> getREIIngredients(FluidTagList list) {
            return list.getEntries().stream()
                    .map(FluidTagList.FluidTagEntry::stacks)
                    .map(REICallWrapper::toREIIngredient)
                    .collect(Collectors.toList());
        }

        public static List<Object> getREIIngredients(FluidEntryList list) {
            if (list instanceof FluidTagList tagList) return getREIIngredients(tagList);
            if (list instanceof FluidStackList stackList) return getREIIngredients(stackList);
            return Collections.emptyList();
        }

        public static Object getREIIngredient(FluidStack fluidStack) {
            return EntryStacks.of(REICallWrapper.toREIStack(fluidStack));
        }
    }

    public static final class EMICallWrapper {

        private static EmiIngredient toEMIIngredient(Stream<FluidStack> stream) {
            return EmiIngredient.of(stream.map(NeoForgeEmiStack::of).toList());
        }

        public static List<Object> getEMIIngredients(FluidStackList list, float xeiChance) {
            return List.of(toEMIIngredient(list.stream()).setChance(xeiChance));
        }

        public static List<Object> getEMIIngredients(FluidTagList list, float xeiChance) {
            return list.getEntries().stream()
                    .map(FluidTagList.FluidTagEntry::stacks)
                    .map(stream -> toEMIIngredient(stream).setChance(xeiChance))
                    .collect(Collectors.toList());
        }

        public static List<Object> getEMIIngredients(FluidEntryList list, float xeiChance) {
            if (list instanceof FluidTagList tagList) return getEMIIngredients(tagList, xeiChance);
            if (list instanceof FluidStackList stackList) return getEMIIngredients(stackList, xeiChance);
            return Collections.emptyList();
        }

        public static Object getEMIIngredient(FluidStack fluidStack, float xeiChance) {
            return NeoForgeEmiStack.of(fluidStack).setChance(xeiChance);
        }
    }
}
