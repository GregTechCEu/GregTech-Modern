package com.gregtechceu.gtceu.api.gui.widget.forge;

import com.lowdragmc.lowdraglib.Platform;
import com.lowdragmc.lowdraglib.gui.editor.annotation.Configurable;
import com.lowdragmc.lowdraglib.gui.editor.annotation.LDLRegister;
import com.lowdragmc.lowdraglib.gui.editor.configurator.ConfiguratorGroup;
import com.lowdragmc.lowdraglib.gui.editor.configurator.IConfigurableWidget;
import com.lowdragmc.lowdraglib.gui.editor.configurator.WrapperConfigurator;
import com.lowdragmc.lowdraglib.gui.editor.runtime.ConfiguratorParser;
import com.lowdragmc.lowdraglib.gui.ingredient.IRecipeIngredientSlot;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ProgressTexture;
import com.lowdragmc.lowdraglib.gui.texture.ProgressTexture.FillDirection;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.gui.util.TextFormattingUtil;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.jei.IngredientIO;
import com.lowdragmc.lowdraglib.side.fluid.FluidHelper;
import com.lowdragmc.lowdraglib.side.fluid.FluidTransferHelper;
import com.lowdragmc.lowdraglib.side.fluid.IFluidTransfer;
import com.lowdragmc.lowdraglib.side.item.ItemTransferHelper;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import lombok.RequiredArgsConstructor;
import mekanism.api.Action;
import mekanism.api.AutomationType;
import mekanism.api.chemical.ChemicalTankBuilder;
import mekanism.api.chemical.gas.GasStack;
import mekanism.api.chemical.gas.IGasHandler;
import mekanism.api.chemical.gas.IGasTank;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.capabilities.Capabilities;
import mekanism.common.registries.MekanismGases;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@LDLRegister(
    name = "gas_slot",
    group = "widget.container"
)
public class GasWidget extends Widget implements IRecipeIngredientSlot, IConfigurableWidget {
    @Nullable
    protected IGasTank gasTank;
    @Configurable
    protected boolean showAmount;
    @Configurable
    protected boolean allowClickFilled;
    @Configurable
    protected boolean allowClickDrained;
    @Configurable
    public boolean drawHoverOverlay;
    @Configurable
    protected boolean drawHoverTips;
    @Configurable
    protected ProgressTexture.FillDirection fillDirection;
    @Configurable
    protected IGuiTexture overlay;
    protected BiConsumer<GasWidget, List<Component>> onAddedTooltips;
    protected IngredientIO ingredientIO;
    protected GasStack lastFluidInTank;
    protected long lastTankCapacity;
    protected Runnable changeListener;
    protected @NotNull List<Consumer<List<Component>>> tooltipCallback;

    public GasWidget() {
        this((IGasTank)null, 0, 0, 18, 18, true, true);
    }

    public void initTemplate() {
        this.setBackground(new ResourceTexture("ldlib:textures/gui/fluid_slot.png"));
        this.setFillDirection(FillDirection.DOWN_TO_UP);
    }

    public GasWidget(IGasTank gasTank, int x, int y, boolean allowClickContainerFilling, boolean allowClickContainerEmptying) {
        this(gasTank, x, y, 18, 18, allowClickContainerFilling, allowClickContainerEmptying);
    }

    public GasWidget(@Nullable IGasTank gasTank, int x, int y, int width, int height, boolean allowClickContainerFilling, boolean allowClickContainerEmptying) {
        super(new Position(x, y), new Size(width, height));
        this.drawHoverOverlay = true;
        this.fillDirection = FillDirection.ALWAYS_FULL;
        this.ingredientIO = IngredientIO.RENDER_ONLY;
        this.tooltipCallback = new ArrayList<>();
        this.gasTank = gasTank;
        this.showAmount = true;
        this.allowClickFilled = allowClickContainerFilling;
        this.allowClickDrained = allowClickContainerEmptying;
        this.drawHoverTips = true;
    }

    public GasWidget setGasTank(IGasTank gasTank) {
        this.gasTank = gasTank;
        if (this.isClientSideWidget) {
            this.setClientSideWidget();
        }

        return this;
    }

    public GasWidget setClientSideWidget() {
        super.setClientSideWidget();
        if (this.gasTank != null) {
            this.gasTank.getStack();
            this.lastFluidInTank = this.gasTank.getStack().copy();
        } else {
            this.lastFluidInTank = null;
        }

        this.lastTankCapacity = this.gasTank != null ? this.gasTank.getCapacity() : 0L;
        return this;
    }

    public GasWidget setBackground(IGuiTexture background) {
        super.setBackground(background);
        return this;
    }

    public Object getJEIIngredient() {
        if (this.lastFluidInTank != null && !this.lastFluidInTank.isEmpty()) {
            return this.lastFluidInTank;
        } else {
            return null;
        }
    }

    public IngredientIO getIngredientIO() {
        return this.ingredientIO;
    }

    private List<Component> getToolTips(List<Component> list) {
        if (this.onAddedTooltips != null) {
            this.onAddedTooltips.accept(this, list);
        }

        Iterator var2 = this.tooltipCallback.iterator();

        while(var2.hasNext()) {
            Consumer<List<Component>> callback = (Consumer)var2.next();
            callback.accept(list);
        }

        return list;
    }

    public void addTooltipCallback(Consumer<List<Component>> callback) {
        this.tooltipCallback.add(callback);
    }

    public void clearTooltipCallback() {
        this.tooltipCallback.clear();
    }

    @OnlyIn(Dist.CLIENT)
    public void drawInBackground(@Nonnull PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.drawInBackground(matrixStack, mouseX, mouseY, partialTicks);
        if (this.isClientSideWidget && this.gasTank != null) {
            GasStack fluidStack = this.gasTank.getStack();
            if (this.gasTank.getCapacity() != this.lastTankCapacity) {
                this.lastTankCapacity = this.gasTank.getCapacity();
            }

            if (!fluidStack.isTypeEqual(this.lastFluidInTank)) {
                this.lastFluidInTank = fluidStack.copy();
            } else if (fluidStack.getAmount() != this.lastFluidInTank.getAmount()) {
                this.lastFluidInTank.setAmount(fluidStack.getAmount());
            }
        }

        Position pos = this.getPosition();
        Size size = this.getSize();
        if (this.lastFluidInTank != null) {
            RenderSystem.disableBlend();
            if (!this.lastFluidInTank.isEmpty()) {
                double progress = (double)this.lastFluidInTank.getAmount() * 1.0 / (double)Math.max(Math.max(this.lastFluidInTank.getAmount(), this.lastTankCapacity), 1L);
                float drawnU = (float)this.fillDirection.getDrawnU(progress);
                float drawnV = (float)this.fillDirection.getDrawnV(progress);
                float drawnWidth = (float)this.fillDirection.getDrawnWidth(progress);
                float drawnHeight = (float)this.fillDirection.getDrawnHeight(progress);
                int width = size.width - 2;
                int height = size.height - 2;
                int x = pos.x + 1;
                int y = pos.y + 1;
                drawGasForGui(matrixStack, this.lastFluidInTank, this.lastFluidInTank.getAmount(), (int)((float)x + drawnU * (float)width), (int)((float)y + drawnV * (float)height), (int)((float)width * drawnWidth), (int)((float)height * drawnHeight));
            }

            if (this.showAmount && !this.lastFluidInTank.isEmpty()) {
                matrixStack.pushPose();
                matrixStack.scale(0.5F, 0.5F, 1.0F);
                long var10000 = this.lastFluidInTank.getAmount();
                String s = TextFormattingUtil.formatLongToCompactStringBuckets(var10000, 3) + "B";
                Font fontRenderer = Minecraft.getInstance().font;
                fontRenderer.drawShadow(matrixStack, s, ((float)pos.x + (float)size.width / 3.0F) * 2.0F - (float)fontRenderer.width(s) + 21.0F, ((float)pos.y + (float)size.height / 3.0F + 6.0F) * 2.0F, 16777215);
                matrixStack.popPose();
            }

            RenderSystem.enableBlend();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        }

        if (this.overlay != null) {
            this.overlay.draw(matrixStack, mouseX, mouseY, (float)pos.x, (float)pos.y, size.width, size.height);
        }

        if (this.drawHoverOverlay && this.isMouseOverElement((double)mouseX, (double)mouseY) && this.getHoverElement((double)mouseX, (double)mouseY) == this) {
            RenderSystem.colorMask(true, true, true, false);
            DrawerHelper.drawSolidRect(matrixStack, this.getPosition().x + 1, this.getPosition().y + 1, this.getSize().width - 2, this.getSize().height - 2, -2130706433);
            RenderSystem.colorMask(true, true, true, true);
        }

    }

    @OnlyIn(Dist.CLIENT)
    public void drawInForeground(@Nonnull PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        if (this.drawHoverTips && this.isMouseOverElement(mouseX, mouseY) && this.getHoverElement(mouseX, mouseY) == this) {
            List<Component> tooltips = new ArrayList();
            if (this.lastFluidInTank != null && !this.lastFluidInTank.isEmpty()) {
                tooltips.add(this.lastFluidInTank.getTextComponent());
                tooltips.add(Component.translatable("ldlib.fluid.amount", this.lastFluidInTank.getAmount(), this.lastTankCapacity).append(" " + FluidHelper.getUnit()));
                if (!Platform.isForge()) {
                    tooltips.add(Component.literal("§6mB:§r %d/%d".formatted(this.lastFluidInTank.getAmount() * 1000L / FluidHelper.getBucket(), this.lastTankCapacity * 1000L / FluidHelper.getBucket())).append(" mB"));
                }
            } else {
                tooltips.add(Component.translatable("ldlib.fluid.empty"));
                tooltips.add(Component.translatable("ldlib.fluid.amount", 0, this.lastTankCapacity).append(" " + FluidHelper.getUnit()));
                if (!Platform.isForge()) {
                    tooltips.add(Component.literal("§6mB:§r %d/%d".formatted(0, this.lastTankCapacity * 1000L / FluidHelper.getBucket())).append(" mB"));
                }
            }

            if (this.gui != null) {
                tooltips.addAll(this.tooltipTexts);
                this.gui.getModularUIGui().setHoverTooltip(this.getToolTips(tooltips), ItemStack.EMPTY, (Font)null, (TooltipComponent)null);
            }

            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        } else {
            super.drawInForeground(matrixStack, mouseX, mouseY, partialTicks);
        }

    }

    public void detectAndSendChanges() {
        if (this.gasTank != null) {
            GasStack gasStack = this.gasTank.getStack();
            if (this.gasTank.getCapacity() != this.lastTankCapacity) {
                this.lastTankCapacity = this.gasTank.getCapacity();
                this.writeUpdateInfo(0, (buffer) -> {
                    buffer.writeVarLong(this.lastTankCapacity);
                });
            }

            if (!gasStack.isTypeEqual(this.lastFluidInTank)) {
                this.lastFluidInTank = gasStack.copy();
                CompoundTag fluidStackTag = gasStack.write(new CompoundTag());
                this.writeUpdateInfo(2, (buffer) -> {
                    buffer.writeNbt(fluidStackTag);
                });
            } else {
                if (gasStack.getAmount() == this.lastFluidInTank.getAmount()) {
                    super.detectAndSendChanges();
                    return;
                }

                this.lastFluidInTank.setAmount(gasStack.getAmount());
                this.writeUpdateInfo(3, (buffer) -> {
                    buffer.writeVarLong(this.lastFluidInTank.getAmount());
                });
            }

            if (this.changeListener != null) {
                this.changeListener.run();
            }
        }

    }

    public void writeInitialData(FriendlyByteBuf buffer) {
        buffer.writeBoolean(this.gasTank != null);
        if (this.gasTank != null) {
            this.lastTankCapacity = this.gasTank.getCapacity();
            buffer.writeVarLong(this.lastTankCapacity);
            GasStack fluidStack = this.gasTank.getStack();
            this.lastFluidInTank = fluidStack.copy();
            buffer.writeNbt(fluidStack.write(new CompoundTag()));
        }

    }

    public void readInitialData(FriendlyByteBuf buffer) {
        if (buffer.readBoolean()) {
            this.lastTankCapacity = buffer.readVarLong();
            this.readUpdateInfo(2, buffer);
        }

    }

    @OnlyIn(Dist.CLIENT)
    public void readUpdateInfo(int id, FriendlyByteBuf buffer) {
        if (id == 0) {
            this.lastTankCapacity = buffer.readVarLong();
        } else if (id == 1) {
            this.lastFluidInTank = null;
        } else if (id == 2) {
            this.lastFluidInTank = GasStack.readFromNBT(buffer.readNbt());
        } else if (id == 3 && this.lastFluidInTank != null) {
            this.lastFluidInTank.setAmount(buffer.readVarLong());
        } else {
            if (id != 4) {
                super.readUpdateInfo(id, buffer);
                return;
            }

            ItemStack currentStack = this.gui.getModularUIContainer().getCarried();
            int newStackSize = buffer.readVarInt();
            currentStack.setCount(newStackSize);
            this.gui.getModularUIContainer().setCarried(currentStack);
        }

        if (this.changeListener != null) {
            this.changeListener.run();
        }

    }

    public void handleClientAction(int id, FriendlyByteBuf buffer) {
        super.handleClientAction(id, buffer);
        if (id == 1) {
            boolean isShiftKeyDown = buffer.readBoolean();
            int clickResult = this.tryClickContainer(isShiftKeyDown);
            if (clickResult >= 0) {
                this.writeUpdateInfo(4, (buf) -> {
                    buf.writeVarInt(clickResult);
                });
            }
        }

    }

    private int tryClickContainer(boolean isShiftKeyDown) {
        if (this.gasTank == null) {
            return -1;
        } else {
            Player player = this.gui.entityPlayer;
            ItemStack currentStack = this.gui.getModularUIContainer().getCarried();
            IFluidTransfer handler = FluidTransferHelper.getFluidTransfer(this.gui.entityPlayer, this.gui.getModularUIContainer());
            if (handler == null) {
                return -1;
            } else {
                int maxAttempts = isShiftKeyDown ? currentStack.getCount() : 1;
                boolean performedEmptying;
                GasStack filledFluid;
                SoundEvent soundevent;
                if (this.allowClickFilled && this.gasTank.getStack().getAmount() > 0L) {
                    performedEmptying = false;
                    filledFluid = this.gasTank.getStack();

                    for(int i = 0; i < maxAttempts; ++i) {
                        boolean result = tryFillContainer(currentStack, this.gasTank, Integer.MAX_VALUE, (Player)null, false);
                        if (!result) {
                            break;
                        }

                        tryFillContainer(currentStack, this.gasTank, Integer.MAX_VALUE, (Player)null, true);
                        currentStack.shrink(1);
                        performedEmptying = true;
                        if (!currentStack.isEmpty() && !player.addItem(currentStack)) {
                            Block.popResource(player.getLevel(), player.getOnPos(), currentStack);
                            break;
                        }
                    }

                    if (performedEmptying) {
                        //soundevent = FluidHelper.getFillSound(filledFluid);
                        //player.level.playSound((Player)null, player.position().x, player.position().y + 0.5, player.position().z, soundevent, SoundSource.BLOCKS, 1.0F, 1.0F);
                        this.gui.getModularUIContainer().setCarried(currentStack);
                        return currentStack.getCount();
                    }
                }

                if (this.allowClickDrained) {
                    performedEmptying = false;

                    for(int i = 0; i < maxAttempts; ++i) {
                        boolean result = tryEmptyContainer(currentStack, new GasTankWrapper(this.gasTank), Integer.MAX_VALUE, (Player)null, false);
                        if (!result) {
                            break;
                        }

                        boolean remainingStack = tryEmptyContainer(currentStack, new GasTankWrapper(this.gasTank), Integer.MAX_VALUE, (Player)null, true);
                        currentStack.shrink(1);
                        performedEmptying = true;
                        if (!currentStack.isEmpty() && !player.getInventory().add(currentStack)) {
                            Block.popResource(player.getLevel(), player.getOnPos(), currentStack);
                            break;
                        }
                    }

                    filledFluid = this.gasTank.getStack();
                    if (performedEmptying) {
                        //soundevent = ChemicalStackHelper.GasStackHelper(filledFluid);
                        //player.level.playSound((Player)null, player.position().x, player.position().y + 0.5, player.position().z, soundevent, SoundSource.BLOCKS, 1.0F, 1.0F);
                        this.gui.getModularUIContainer().setCarried(currentStack);
                        return currentStack.getCount();
                    }
                }

                return -1;
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if ((this.allowClickDrained || this.allowClickFilled) && this.isMouseOverElement(mouseX, mouseY) && button == 0 && FluidTransferHelper.getFluidTransfer(this.gui.entityPlayer, this.gui.getModularUIContainer()) != null) {
            boolean isShiftKeyDown = isShiftDown();
            this.writeClientAction(1, (writer) -> {
                writer.writeBoolean(isShiftKeyDown);
            });
            playButtonClickSound();
            return true;
        } else {
            return false;
        }
    }

    public void buildConfigurator(ConfiguratorGroup father) {
        IGasTank handler = ChemicalTankBuilder.GAS.create(5000L, null);
        handler.insert(new GasStack(MekanismGases.WATER_VAPOR, 3000L), Action.EXECUTE, AutomationType.INTERNAL);
        father.addConfigurators(new WrapperConfigurator("ldlib.gui.editor.group.preview", (new GasWidget() {
            public void updateScreen() {
                super.updateScreen();
                this.setHoverTooltips(GasWidget.this.tooltipTexts);
                this.backgroundTexture = GasWidget.this.backgroundTexture;
                this.hoverTexture = GasWidget.this.hoverTexture;
                this.showAmount = GasWidget.this.showAmount;
                this.drawHoverTips = GasWidget.this.drawHoverTips;
                this.fillDirection = GasWidget.this.fillDirection;
                this.overlay = GasWidget.this.overlay;
            }
        }).setAllowClickDrained(false).setAllowClickFilled(false).setGasTank(handler)));

        ConfiguratorParser.createConfigurators(father, new HashMap<>(), getClass(), this);
    }

    @Nullable
    public IGasTank getGasTank() {
        return this.gasTank;
    }

    public GasWidget setShowAmount(boolean showAmount) {
        this.showAmount = showAmount;
        return this;
    }

    public GasWidget setAllowClickFilled(boolean allowClickFilled) {
        this.allowClickFilled = allowClickFilled;
        return this;
    }

    public GasWidget setAllowClickDrained(boolean allowClickDrained) {
        this.allowClickDrained = allowClickDrained;
        return this;
    }

    public GasWidget setDrawHoverOverlay(boolean drawHoverOverlay) {
        this.drawHoverOverlay = drawHoverOverlay;
        return this;
    }

    public GasWidget setDrawHoverTips(boolean drawHoverTips) {
        this.drawHoverTips = drawHoverTips;
        return this;
    }

    public GasWidget setFillDirection(ProgressTexture.FillDirection fillDirection) {
        this.fillDirection = fillDirection;
        return this;
    }

    public GasWidget setOverlay(IGuiTexture overlay) {
        this.overlay = overlay;
        return this;
    }

    public GasWidget setOnAddedTooltips(BiConsumer<GasWidget, List<Component>> onAddedTooltips) {
        this.onAddedTooltips = onAddedTooltips;
        return this;
    }

    public GasWidget setIngredientIO(IngredientIO ingredientIO) {
        this.ingredientIO = ingredientIO;
        return this;
    }

    public GasWidget setChangeListener(Runnable changeListener) {
        this.changeListener = changeListener;
        return this;
    }

    public static void drawGasForGui(PoseStack poseStack, GasStack contents, long tankCapacity, int startX, int startY, int widthT, int heightT) {
        ResourceLocation LOCATION_BLOCKS_TEXTURE = InventoryMenu.BLOCK_ATLAS;
        TextureAtlasSprite fluidStillSprite = MekanismRenderer.getChemicalTexture(contents.getType());

        int fluidColor = MekanismRenderer.getColorARGB(contents, 1, true) | -16777216;
        int scaledAmount = (int)(contents.getAmount() * (long)heightT / tankCapacity);
        if (contents.getAmount() > 0L && scaledAmount < 1) {
            scaledAmount = 1;
        }

        if (scaledAmount > heightT || contents.getAmount() == tankCapacity) {
            scaledAmount = heightT;
        }

        RenderSystem.enableBlend();
        RenderSystem.setShaderTexture(0, LOCATION_BLOCKS_TEXTURE);
        int xTileCount = widthT / 16;
        int xRemainder = widthT - xTileCount * 16;
        int yTileCount = scaledAmount / 16;
        int yRemainder = scaledAmount - yTileCount * 16;
        int yStart = startY + heightT;

        for(int xTile = 0; xTile <= xTileCount; ++xTile) {
            for(int yTile = 0; yTile <= yTileCount; ++yTile) {
                int width = xTile == xTileCount ? xRemainder : 16;
                int height = yTile == yTileCount ? yRemainder : 16;
                int x = startX + xTile * 16;
                int y = yStart - (yTile + 1) * 16;
                if (width > 0 && height > 0) {
                    int maskTop = 16 - height;
                    int maskRight = 16 - width;
                    DrawerHelper.drawFluidTexture(poseStack, (float)x, (float)y, fluidStillSprite, maskTop, maskRight, 0.0F, fluidColor);
                }
            }
        }

        RenderSystem.enableBlend();
    }

    @Nonnull
    public static GasStack tryGasTransfer(IGasHandler fluidDestination, IGasHandler fluidSource, int maxAmount, boolean doTransfer) {
        GasStack drainable = fluidSource.extractChemical(maxAmount, Action.SIMULATE);
        return !drainable.isEmpty() ? tryGasTransfer_Internal(fluidDestination, fluidSource, drainable, doTransfer) : GasStack.EMPTY;
    }

    @Nonnull
    private static GasStack tryGasTransfer_Internal(IGasHandler fluidDestination, IGasHandler fluidSource, GasStack drainable, boolean doTransfer) {
        GasStack fillableAmount = fluidDestination.insertChemical(drainable, Action.SIMULATE);
        if (fillableAmount.getAmount() > 0L) {
            drainable.setAmount(fillableAmount.getAmount());
            GasStack drained = fluidSource.extractChemical(drainable.getAmount(), doTransfer ? Action.SIMULATE : Action.EXECUTE);
            if (!drained.isEmpty()) {
                drained.setAmount(fluidDestination.insertChemical(drained, doTransfer ? Action.SIMULATE : Action.EXECUTE).getAmount());
                return drained;
            }
        }

        return GasStack.EMPTY;
    }

    public static boolean tryFillContainer(@Nonnull ItemStack container, IGasTank fluidSource, int maxAmount, @Nullable Player player, boolean doFill) {
        IGasHandler handler = container.getCapability(Capabilities.GAS_HANDLER).orElse(null);
        if (handler != null) {
            GasStack simulatedTransfer = tryGasTransfer(handler, new GasTankWrapper(fluidSource), maxAmount, false);
            if (!simulatedTransfer.isEmpty()) {
                if (doFill) {
                    tryGasTransfer(handler, new GasTankWrapper(fluidSource), maxAmount, true);
                    //if (player != null) {
                    //    SoundEvent soundevent = FluidHelper.getFillSound(simulatedTransfer);
                    //    player.level.playSound((Player)null, player.getX(), player.getY() + 0.5, player.getZ(), soundevent, SoundSource.BLOCKS, 1.0F, 1.0F);
                    //}
                } else {
                    handler.insertChemical(simulatedTransfer, Action.SIMULATE);
                }

                return true;
            }
        }

        return false;
    }

    public static boolean tryEmptyContainer(@Nonnull ItemStack container, IGasHandler fluidDestination, int maxAmount, @Nullable Player player, boolean doDrain) {
        ItemStack containerCopy = ItemTransferHelper.copyStackWithSize(container, 1);
        IGasHandler handler = container.getCapability(Capabilities.GAS_HANDLER).orElse(null);
        if (handler != null) {
            GasStack transfer = tryGasTransfer(fluidDestination, handler, maxAmount, doDrain);
            if (transfer.isEmpty()) {
                return false;
            } else {
                //if (doDrain && player != null) {
                //    SoundEvent soundevent = FluidHelper.getEmptySound(transfer);
                //    player.level.playSound((Player)null, player.getX(), player.getY() + 0.5, player.getZ(), soundevent, SoundSource.BLOCKS, 1.0F, 1.0F);
                //}

                return true;
            }
        } else {
            return false;
        }
    }

    @RequiredArgsConstructor
    public static class GasTankWrapper implements IGasHandler {

        private final IGasTank internal;

        @Override
        public int getTanks() {
            return 1;
        }

        @Override
        public GasStack getChemicalInTank(int tank) {
            return internal.getStack();
        }

        @Override
        public void setChemicalInTank(int tank, GasStack stack) {
            internal.setStack(stack);
        }

        @Override
        public long getTankCapacity(int tank) {
            return internal.getCapacity();
        }

        @Override
        public boolean isValid(int tank, GasStack stack) {
            return internal.isValid(stack);
        }

        @Override
        public GasStack insertChemical(int tank, GasStack stack, Action action) {
            return internal.insert(stack, action, AutomationType.INTERNAL);
        }

        @Override
        public GasStack extractChemical(int tank, long amount, Action action) {
            return internal.extract(amount, action, AutomationType.INTERNAL);
        }
    }
}
