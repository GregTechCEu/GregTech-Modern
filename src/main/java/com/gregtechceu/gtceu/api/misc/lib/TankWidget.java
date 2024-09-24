package com.gregtechceu.gtceu.api.misc.lib;

import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.Platform;
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
import com.lowdragmc.lowdraglib.side.fluid.FluidHelper;
import com.lowdragmc.lowdraglib.side.fluid.FluidTransferHelper;
import com.lowdragmc.lowdraglib.side.fluid.forge.FluidHelperImpl;
import com.lowdragmc.lowdraglib.utils.CycleFluidTransfer;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import mezz.jei.api.helpers.IPlatformFluidHelper;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@SuppressWarnings("unused")
@LDLRegister(name = "fluid_slot", group = "widget.container")
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
    @NotNull
    protected List<Consumer<List<Component>>> tooltipCallback = new ArrayList<>();

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

    public TankWidget(IFluidHandler fluidTank, int tank, int x, int y, boolean allowClickContainerFilling,
                      boolean allowClickContainerEmptying) {
        this(fluidTank, tank, x, y, 18, 18, allowClickContainerFilling, allowClickContainerEmptying);
    }

    public TankWidget(@Nullable IFluidHandler fluidTank, int tank, int x, int y, int width, int height,
                      boolean allowClickContainerFilling, boolean allowClickContainerEmptying) {
        super(new Position(x, y), new Size(width, height));
        this.fluidTank = fluidTank;
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

    @Nullable
    @Override
    public Object getXEIIngredientOverMouse(double mouseX, double mouseY) {
        if (self().isMouseOverElement(mouseX, mouseY)) {
            if (lastFluidInTank == null || lastFluidInTank.isEmpty()) return null;

            if (this.fluidTank instanceof CycleFluidTransfer cycleItemStackHandler) {
                return getXEIIngredientsFromCycleTransferClickable(cycleItemStackHandler, tank).get(0);
            } else if (this.fluidTank instanceof TagOrCycleFluidTransfer transfer) {
                return getXEIIngredientsFromTagOrCycleTransferClickable(transfer, tank).get(0);
            }

            if (LDLib.isJeiLoaded()) {
                return JEICallWrapper.getPlatformFluidTypeForJEIClickable(lastFluidInTank, getPosition(), getSize());
            }
            if (LDLib.isReiLoaded()) {
                return EntryStacks.of(dev.architectury.fluid.FluidStack.create(lastFluidInTank.getFluid(),
                        lastFluidInTank.getAmount(), lastFluidInTank.getOrCreateTag()));
            }
            if (LDLib.isEmiLoaded()) {
                return EmiStack
                        .of(lastFluidInTank.getFluid(), lastFluidInTank.getOrCreateTag(), lastFluidInTank.getAmount())
                        .setChance(XEIChance);
            }
        }
        return null;
    }

    @Override
    public List<Object> getXEIIngredients() {
        if (lastFluidInTank == null || lastFluidInTank.isEmpty()) return Collections.emptyList();

        if (this.fluidTank instanceof CycleFluidTransfer cycleItemStackHandler) {
            return getXEIIngredientsFromCycleTransfer(cycleItemStackHandler, tank);
        } else if (this.fluidTank instanceof TagOrCycleFluidTransfer transfer) {
            return getXEIIngredientsFromTagOrCycleTransfer(transfer, tank);
        }

        if (LDLib.isJeiLoaded()) {
            return List.of(JEICallWrapper.getPlatformFluidTypeForJEI(lastFluidInTank));
        }
        if (LDLib.isReiLoaded()) {
            return List.of(EntryStacks.of(dev.architectury.fluid.FluidStack.create(lastFluidInTank.getFluid(),
                    lastFluidInTank.getAmount(), lastFluidInTank.getOrCreateTag())));
        }
        if (LDLib.isEmiLoaded()) {
            return List.of(EmiStack
                    .of(lastFluidInTank.getFluid(), lastFluidInTank.getOrCreateTag(), lastFluidInTank.getAmount())
                    .setChance(XEIChance));
        }
        return List.of(lastFluidInTank);
    }

    private List<Object> getXEIIngredientsFromCycleTransfer(CycleFluidTransfer transfer, int index) {
        var stream = transfer.getStackList(index).stream().map(FluidHelper::toRealFluidStack)
                .map(FluidStack.class::cast);
        if (LDLib.isJeiLoaded()) {
            return stream.filter(fluid -> !fluid.isEmpty()).map(JEICallWrapper::getPlatformFluidTypeForJEI).toList();
        } else if (LDLib.isReiLoaded()) {
            return REICallWrapper.getReiIngredients(stream);
        } else if (LDLib.isEmiLoaded()) {
            return EMICallWrapper.getEmiIngredients(stream, getXEIChance());
        }
        return Collections.emptyList();
    }

    private List<Object> getXEIIngredientsFromCycleTransferClickable(CycleFluidTransfer transfer, int index) {
        var stream = transfer.getStackList(index).stream().map(FluidHelper::toRealFluidStack)
                .map(FluidStack.class::cast);
        if (LDLib.isJeiLoaded()) {
            return stream
                    .filter(fluid -> !fluid.isEmpty())
                    .map(fluid -> JEICallWrapper.getPlatformFluidTypeForJEIClickable(fluid, getPosition(), getSize()))
                    .toList();
        } else if (LDLib.isReiLoaded()) {
            return REICallWrapper.getReiIngredients(stream);
        } else if (LDLib.isEmiLoaded()) {
            return EMICallWrapper.getEmiIngredients(stream, getXEIChance());
        }
        return Collections.emptyList();
    }

    private List<Object> getXEIIngredientsFromTagOrCycleTransfer(TagOrCycleFluidTransfer transfer, int index) {
        Either<List<Pair<TagKey<Fluid>, Integer>>, List<FluidStack>> either = transfer
                .getStacks()
                .get(index);
        var ref = new Object() {

            List<Object> returnValue = Collections.emptyList();
        };
        either.ifLeft(list -> {
            if (LDLib.isJeiLoaded()) {
                ref.returnValue = list.stream()
                        .flatMap(pair -> BuiltInRegistries.FLUID
                                .getTag(pair.getFirst())
                                .stream()
                                .flatMap(HolderSet.ListBacked::stream)
                                .map(fluid -> JEICallWrapper
                                        .getPlatformFluidTypeForJEI(new FluidStack(fluid.value(), pair.getSecond()))))
                        .collect(Collectors.toList());
            } else if (LDLib.isReiLoaded()) {
                ref.returnValue = REICallWrapper.getReiIngredients(list);
            } else if (LDLib.isEmiLoaded()) {
                ref.returnValue = EMICallWrapper.getEmiIngredients(list, getXEIChance());
            }
        }).ifRight(fluids -> {
            var stream = fluids.stream();
            if (LDLib.isJeiLoaded()) {
                ref.returnValue = stream.filter(fluid -> !fluid.isEmpty())
                        .map(JEICallWrapper::getPlatformFluidTypeForJEI).toList();
            } else if (LDLib.isReiLoaded()) {
                ref.returnValue = REICallWrapper.getReiIngredients(stream);
            } else if (LDLib.isEmiLoaded()) {
                ref.returnValue = EMICallWrapper.getEmiIngredients(stream, getXEIChance());
            }
        });
        return ref.returnValue;
    }

    private List<Object> getXEIIngredientsFromTagOrCycleTransferClickable(TagOrCycleFluidTransfer transfer, int index) {
        Either<List<Pair<TagKey<Fluid>, Integer>>, List<FluidStack>> either = transfer
                .getStacks()
                .get(index);
        var ref = new Object() {

            List<Object> returnValue = Collections.emptyList();
        };
        either.ifLeft(list -> {
            if (LDLib.isJeiLoaded()) {
                ref.returnValue = list.stream()
                        .flatMap(pair -> BuiltInRegistries.FLUID
                                .getTag(pair.getFirst())
                                .stream()
                                .flatMap(HolderSet.ListBacked::stream)
                                .map(fluid -> JEICallWrapper.getPlatformFluidTypeForJEIClickable(
                                        new FluidStack(fluid.value(), pair.getSecond()), getPosition(), getSize())))
                        .collect(Collectors.toList());
            } else if (LDLib.isReiLoaded()) {
                ref.returnValue = REICallWrapper.getReiIngredients(list);
            } else if (LDLib.isEmiLoaded()) {
                ref.returnValue = EMICallWrapper.getEmiIngredients(list, getXEIChance());
            }
        }).ifRight(fluids -> {
            var stream = fluids.stream();
            if (LDLib.isJeiLoaded()) {
                ref.returnValue = stream
                        .filter(fluid -> !fluid.isEmpty())
                        .map(fluid -> JEICallWrapper.getPlatformFluidTypeForJEIClickable(fluid, getPosition(),
                                getSize()))
                        .toList();
            } else if (LDLib.isReiLoaded()) {
                ref.returnValue = REICallWrapper.getReiIngredients(stream);
            } else if (LDLib.isEmiLoaded()) {
                ref.returnValue = EMICallWrapper.getEmiIngredients(stream, getXEIChance());
            }
        });
        return ref.returnValue;
    }

    @Override
    public List<Component> getTooltipTexts() {
        List<Component> tooltips = getToolTips(new ArrayList<>());
        tooltips.addAll(tooltipTexts);
        return tooltips;
    }

    private List<Component> getToolTips(List<Component> list) {
        if (this.onAddedTooltips != null) {
            this.onAddedTooltips.accept(this, list);
        }
        for (Consumer<List<Component>> callback : this.tooltipCallback) {
            callback.accept(list);
        }

        return list;
    }

    @Override
    public void addTooltipCallback(Consumer<List<Component>> callback) {
        this.tooltipCallback.add(callback);
    }

    @Override
    public void clearTooltipCallback() {
        this.tooltipCallback.clear();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
        if (isClientSideWidget && fluidTank != null) {
            FluidStack fluidStack = fluidTank.getFluidInTank(tank);
            int capacity = fluidTank.getTankCapacity(tank);
            if (capacity != lastTankCapacity) {
                this.lastTankCapacity = capacity;
            }
            if (!fluidStack.isFluidEqual(lastFluidInTank)) {
                this.lastFluidInTank = fluidStack.copy();
            } else if (fluidStack.getAmount() != lastFluidInTank.getAmount()) {
                this.lastFluidInTank.setAmount(fluidStack.getAmount());
            }
        }
        Position pos = getPosition();
        Size size = getSize();
        if (lastFluidInTank != null) {
            RenderSystem.disableBlend();
            if (!lastFluidInTank.isEmpty()) {
                double progress = lastFluidInTank.getAmount() * 1.0 /
                        Math.max(Math.max(lastFluidInTank.getAmount(), lastTankCapacity), 1);
                float drawnU = (float) fillDirection.getDrawnU(progress);
                float drawnV = (float) fillDirection.getDrawnV(progress);
                float drawnWidth = (float) fillDirection.getDrawnWidth(progress);
                float drawnHeight = (float) fillDirection.getDrawnHeight(progress);
                int width = size.width - 2;
                int height = size.height - 2;
                int x = pos.x + 1;
                int y = pos.y + 1;
                DrawerHelper.drawFluidForGui(graphics, FluidHelperImpl.toFluidStack(lastFluidInTank),
                        lastFluidInTank.getAmount(), (int) (x + drawnU * width), (int) (y + drawnV * height),
                        ((int) (width * drawnWidth)), ((int) (height * drawnHeight)));
            }

            if (showAmount && !lastFluidInTank.isEmpty()) {
                graphics.pose().pushPose();
                graphics.pose().scale(0.5F, 0.5F, 1);
                String s = TextFormattingUtil.formatLongToCompactStringBuckets(lastFluidInTank.getAmount(), 3) + "B";
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
    public void drawInForeground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        if (drawHoverTips && isMouseOverElement(mouseX, mouseY) && getHoverElement(mouseX, mouseY) == this) {
            List<Component> tooltips = new ArrayList<>();
            if (lastFluidInTank != null && !lastFluidInTank.isEmpty()) {
                tooltips.add(lastFluidInTank.getDisplayName());
                tooltips.add(Component.translatable("ldlib.fluid.amount", lastFluidInTank.getAmount(), lastTankCapacity)
                        .append(" " + FluidHelper.getUnit()));
                if (!Platform.isForge()) {
                    tooltips.add(Component.literal(
                            "§6mB:§r %d/%d".formatted(lastFluidInTank.getAmount() * 1000 / FluidType.BUCKET_VOLUME,
                                    lastTankCapacity * 1000 / FluidType.BUCKET_VOLUME))
                            .append(" " + "mB"));
                }
                tooltips.add(Component.translatable("ldlib.fluid.temperature",
                        lastFluidInTank.getFluid().getFluidType().getTemperature()));
                tooltips.add(Component.translatable(lastFluidInTank.getFluid().getFluidType().isLighterThanAir() ?
                        "ldlib.fluid.state_gas" : "ldlib.fluid.state_liquid"));
            } else {
                tooltips.add(Component.translatable("ldlib.fluid.empty"));
                tooltips.add(Component.translatable("ldlib.fluid.amount", 0, lastTankCapacity)
                        .append(" " + FluidHelper.getUnit()));
                if (!Platform.isForge()) {
                    tooltips.add(Component
                            .literal("§6mB:§r %d/%d".formatted(0, lastTankCapacity * 1000 / FluidType.BUCKET_VOLUME))
                            .append(" " + "mB"));
                }
            }
            if (gui != null) {
                tooltips.addAll(getTooltipTexts());
                gui.getModularUIGui().setHoverTooltip(tooltips, ItemStack.EMPTY, null, null);
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
            if (!fluidStack.isFluidEqual(lastFluidInTank)) {
                this.lastFluidInTank = fluidStack.copy();
                var tag = fluidStack.writeToNBT(new CompoundTag());
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
    public void writeInitialData(FriendlyByteBuf buffer) {
        buffer.writeBoolean(fluidTank != null);
        if (fluidTank != null) {
            this.lastTankCapacity = fluidTank.getTankCapacity(tank);
            buffer.writeVarInt(lastTankCapacity);
            FluidStack fluidStack = fluidTank.getFluidInTank(tank);
            this.lastFluidInTank = fluidStack.copy();
            var tag = fluidStack.writeToNBT(new CompoundTag());
            buffer.writeNbt(tag);
        }
    }

    @Override
    public void readInitialData(FriendlyByteBuf buffer) {
        if (buffer.readBoolean()) {
            this.lastTankCapacity = buffer.readVarInt();
            readUpdateInfo(2, buffer);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void readUpdateInfo(int id, FriendlyByteBuf buffer) {
        if (id == 0) {
            this.lastTankCapacity = buffer.readVarInt();
        } else if (id == 1) {
            this.lastFluidInTank = null;
        } else if (id == 2) {
            this.lastFluidInTank = FluidStack.loadFluidStackFromNBT(buffer.readNbt());
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
    public void handleClientAction(int id, FriendlyByteBuf buffer) {
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
        var handler = FluidTransferHelper.getFluidTransfer(gui.entityPlayer, gui.getModularUIContainer());
        if (handler == null) return -1;
        int maxAttempts = isShiftKeyDown ? currentStack.getCount() : 1;
        FluidStack initialFluid = fluidTank.getFluidInTank(tank);
        if (allowClickFilled && initialFluid.getAmount() > 0) {
            boolean performedFill = false;
            for (int i = 0; i < maxAttempts; i++) {
                FluidActionResult result = FluidUtil.tryFillContainer(currentStack, fluidTank, Integer.MAX_VALUE, null,
                        false);
                if (!result.isSuccess()) break;
                ItemStack remainingStack = FluidUtil
                        .tryFillContainer(currentStack, fluidTank, Integer.MAX_VALUE, null, true).getResult();
                currentStack.shrink(1);
                performedFill = true;
                if (!remainingStack.isEmpty() && !player.addItem(remainingStack)) {
                    Block.popResource(player.level(), player.getOnPos(), remainingStack);
                    break;
                }
            }
            if (performedFill) {
                SoundEvent soundevent = initialFluid.getFluid().getFluidType().getSound(SoundActions.BUCKET_FILL);
                if (soundevent != null) {
                    player.level().playSound(null, player.position().x, player.position().y + 0.5, player.position().z,
                            soundevent, SoundSource.BLOCKS, 1.0F, 1.0F);
                }
                gui.getModularUIContainer().setCarried(currentStack);
                return currentStack.getCount();
            }
        }

        if (allowClickDrained) {
            boolean performedEmptying = false;
            for (int i = 0; i < maxAttempts; i++) {
                FluidActionResult result = FluidUtil.tryEmptyContainer(currentStack, fluidTank, Integer.MAX_VALUE, null,
                        false);
                if (!result.isSuccess()) break;
                ItemStack remainingStack = FluidUtil
                        .tryEmptyContainer(currentStack, fluidTank, Integer.MAX_VALUE, null, true).getResult();
                currentStack.shrink(1);
                performedEmptying = true;
                if (!remainingStack.isEmpty() && !player.getInventory().add(remainingStack)) {
                    Block.popResource(player.level(), player.getOnPos(), remainingStack);
                    break;
                }
            }
            var filledFluid = fluidTank.getFluidInTank(tank);
            if (performedEmptying) {
                SoundEvent soundevent = filledFluid.getFluid().getFluidType().getSound(SoundActions.BUCKET_EMPTY);
                if (soundevent != null) {
                    player.level().playSound(null, player.position().x, player.position().y + 0.5, player.position().z,
                            soundevent, SoundSource.BLOCKS, 1.0F, 1.0F);
                }
                gui.getModularUIContainer().setCarried(currentStack);
                return currentStack.getCount();
            }
        }

        return -1;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if ((allowClickDrained || allowClickFilled) && isMouseOverElement(mouseX, mouseY)) {
            if (button == 0) {
                if (FluidTransferHelper.getFluidTransfer(gui.entityPlayer, gui.getModularUIContainer()) != null) {
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

        public static Object getPlatformFluidTypeForJEI(FluidStack fluidStack) {
            return _getPlatformFluidTypeForJEI(JEIPlugin.jeiHelpers.getPlatformFluidHelper(), fluidStack);
        }

        private static <T> Object _getPlatformFluidTypeForJEI(IPlatformFluidHelper<T> helper, FluidStack fluidStack) {
            return helper.create(fluidStack.getFluid(), fluidStack.getAmount(), fluidStack.getOrCreateTag());
        }

        public static Object getPlatformFluidTypeForJEIClickable(FluidStack fluidStack, Position pos, Size size) {
            return _getPlatformFluidTypeForJEIClickable(JEIPlugin.jeiHelpers.getPlatformFluidHelper(), fluidStack, pos,
                    size);
        }

        private static <T> Object _getPlatformFluidTypeForJEIClickable(IPlatformFluidHelper<T> helper,
                                                                       FluidStack fluidStack, Position pos, Size size) {
            T ingredient = helper.create(fluidStack.getFluid(), fluidStack.getAmount(), fluidStack.getOrCreateTag());
            return JEIPlugin.jeiHelpers.getIngredientManager().createTypedIngredient(ingredient)
                    .map(typedIngredient -> new ClickableIngredient<>(typedIngredient, pos.x, pos.y, size.width,
                            size.height))
                    .orElse(null);
        }
    }

    public static final class REICallWrapper {

        public static List<Object> getReiIngredients(Stream<FluidStack> stream) {
            return List.of(EntryIngredient.of(stream
                    .map(fluidStack -> dev.architectury.fluid.FluidStack.create(fluidStack.getFluid(),
                            fluidStack.getAmount(), fluidStack.getOrCreateTag()))
                    .map(EntryStacks::of)
                    .toList()));
        }

        public static List<Object> getReiIngredients(List<Pair<TagKey<Fluid>, Integer>> list) {
            return list.stream()
                    .map(pair -> EntryIngredients.ofTag(pair.getFirst(),
                            holder -> EntryStacks
                                    .of(dev.architectury.fluid.FluidStack.create(holder.value(), pair.getSecond()))))
                    .collect(Collectors.toList());
        }
    }

    public static final class EMICallWrapper {

        public static List<Object> getEmiIngredients(Stream<FluidStack> stream, float xeiChance) {
            return List.of(EmiIngredient.of(stream.map(fluidStack -> EmiStack.of(fluidStack.getFluid(),
                    fluidStack.getOrCreateTag(), fluidStack.getAmount())).toList()).setChance(xeiChance));
        }

        public static List<Object> getEmiIngredients(List<Pair<TagKey<Fluid>, Integer>> list, float xeiChance) {
            return list.stream()
                    .map(pair -> EmiIngredient.of(pair.getFirst()).setAmount(pair.getSecond()).setChance(xeiChance))
                    .collect(Collectors.toList());
        }
    }
}
