package com.gregtechceu.gtceu.integration.ae2.util;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.GenericStack;
import com.google.common.collect.Lists;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.integration.ae2.gui.widget.AEConfigWidget;
import com.gregtechceu.gtceu.integration.ae2.machine.MEInputHatchPartMachine;
import com.lowdragmc.lowdraglib.gui.ingredient.Target;
import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.gui.util.TextFormattingUtil;
import com.lowdragmc.lowdraglib.side.fluid.FluidActionResult;
import com.lowdragmc.lowdraglib.side.fluid.FluidHelper;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.lowdragmc.lowdraglib.side.fluid.FluidTransferHelper;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

import static com.gregtechceu.gtceu.utils.GTUtil.getFluidFromContainer;
import static com.lowdragmc.lowdraglib.gui.util.DrawerHelper.drawStringFixedCorner;

/**
 * @Author GlodBlock
 * @Description A configurable slot for {@link com.lowdragmc.lowdraglib.side.fluid.FluidStack}
 * @Date 2023/4/21-0:50
 */
public class AEFluidConfigSlot extends AEConfigSlot {
    public static final int LOAD_PHANTOM_FLUID_STACK_FROM_NBT = 13;


    public AEFluidConfigSlot(int x, int y, AEConfigWidget widget, int index) {
        super(new Position(x, y), new Size(18, 18 * 2), widget, index);
    }

    @Override
    public void drawInBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
        Position position = getPosition();
        IConfigurableSlot slot = this.parentWidget.getDisplay(this.index);
        GenericStack config = slot.getConfig();
        GenericStack stock = slot.getStock();
        GuiTextures.FLUID_SLOT.draw(graphics, mouseX, mouseY, position.x, position.y, 18, 18);
        GuiTextures.FLUID_SLOT.draw(graphics, mouseX, mouseY, position.x, position.y + 18, 18, 18);
        GuiTextures.CONFIG_ARROW.draw(graphics, mouseX, mouseY, position.x, position.y, 18, 18);
        if (this.select) {
            GuiTextures.SELECT_BOX.draw(graphics, mouseX, mouseY, position.x, position.y, 18, 18);
        }
        int stackX = position.x + 1;
        int stackY = position.y + 1;
        if (config != null) {
            FluidStack stack = config.what() instanceof AEFluidKey key ? FluidStack.create(key.getFluid(), config.amount(), key.getTag()) : FluidStack.empty();

            DrawerHelper.drawFluidForGui(graphics, stack, config.amount(), stackX, stackY, 17, 17);
            String amountStr = TextFormattingUtil.formatLongToCompactString(config.amount(), 4) + "mB";
            drawStringFixedCorner(graphics, amountStr, stackX + 17, stackY + 17, 16777215, true, 0.5f);
        }
        if (stock != null) {
            FluidStack stack = stock.what() instanceof AEFluidKey key ? FluidStack.create(key.getFluid(), stock.amount(), key.getTag()) : FluidStack.empty();

            DrawerHelper.drawFluidForGui(graphics, stack, stock.amount(), stackX, stackY + 18, 17, 17);
            String amountStr = TextFormattingUtil.formatLongToCompactString(stock.amount(), 4) + "mB";
            drawStringFixedCorner(graphics, amountStr, stackX + 17, stackY + 18 + 17, 16777215, true, 0.5f);
        }
        if (mouseOverConfig(mouseX, mouseY)) {
            drawSelectionOverlay(graphics, stackX, stackY, 16, 16);
        } else if (mouseOverStock(mouseX, mouseY)) {
            drawSelectionOverlay(graphics, stackX, stackY + 18, 16, 16);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseOverConfig(mouseX, mouseY)) {
            if (button == 1) {
                // Right click to clear
                this.parentWidget.disableAmount();
                writeClientAction(REMOVE_ID, buf -> {});
            } else if (button == 0) {
                // Left click to set/select
                ItemStack hold = this.gui.getModularUIContainer().getCarried();
                FluidStack fluid = FluidTransferHelper.getFluidContained(hold);

                if (fluid != null) {
                    writeClientAction(UPDATE_ID, buf -> {
                        buf.writeResourceLocation(BuiltInRegistries.FLUID.getKey(fluid.getFluid()));
                        buf.writeVarLong(fluid.getAmount());
                    });
                }
                this.parentWidget.enableAmount(this.index);
                this.select = true;
            }
            return true;
        } else if (mouseOverStock(mouseX, mouseY)) {
            // Left click to pick up
            if (button == 0) {
                GenericStack stack = this.parentWidget.getDisplay(this.index).getStock();
                if (stack != null) {
                    writeClientAction(PICK_UP_ID, buf -> buf.writeBoolean(isShiftDown()));
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public void handleClientAction(int id, FriendlyByteBuf buffer) {
        super.handleClientAction(id, buffer);
        IConfigurableSlot slot = this.parentWidget.getConfig(this.index);
        if (id == REMOVE_ID) {
            slot.setConfig(null);
            this.parentWidget.disableAmount();
            writeUpdateInfo(REMOVE_ID, buf -> {});
        }
        if (id == UPDATE_ID) {
            FluidStack fluid = FluidStack.create(BuiltInRegistries.FLUID.get(buffer.readResourceLocation()), buffer.readVarLong());
            slot.setConfig(new GenericStack(AEFluidKey.of(fluid.getFluid()), fluid.getAmount()));
            this.parentWidget.enableAmount(this.index);
            if (fluid != FluidStack.empty()) {
                writeUpdateInfo(UPDATE_ID, buf -> {
                    buf.writeResourceLocation(BuiltInRegistries.FLUID.getKey(fluid.getFluid()));
                    buf.writeVarLong(fluid.getAmount());
                });
            }
        }
        if (id == AMOUNT_CHANGE_ID) {
            if (slot.getConfig() != null) {
                int amt = buffer.readInt();
                slot.setConfig(ExportOnlyAESlot.copy(slot.getConfig(), amt));
                writeUpdateInfo(AMOUNT_CHANGE_ID, buf -> buf.writeInt(amt));
            }
        }
        if (id == PICK_UP_ID) {
            if (slot.getStock() != null) {
                boolean isShiftKeyDown = buffer.readBoolean();
                int clickResult = tryClickContainer(isShiftKeyDown);
                if (clickResult >= 0) {
                    writeUpdateInfo(PICK_UP_ID, buf -> buf.writeVarInt(clickResult));
                }
            }
        }
        if (id == LOAD_PHANTOM_FLUID_STACK_FROM_NBT) {
            FluidStack fluid = FluidStack.loadFromTag(buffer.readNbt());
            slot.setConfig(new GenericStack(AEFluidKey.of(fluid.getFluid()), fluid.getAmount()));
            this.parentWidget.enableAmount(this.index);
            if (fluid != FluidStack.empty()) {
                writeUpdateInfo(UPDATE_ID, buf -> {
                    buf.writeResourceLocation(BuiltInRegistries.FLUID.getKey(fluid.getFluid()));
                    buf.writeVarLong(fluid.getAmount());
                });
            }
        }
    }

    @Override
    public void readUpdateInfo(int id, FriendlyByteBuf buffer) {
        super.readUpdateInfo(id, buffer);
        IConfigurableSlot slot = this.parentWidget.getDisplay(this.index);
        if (id == REMOVE_ID) {
            slot.setConfig(null);
        }
        if (id == UPDATE_ID) {
            FluidStack fluid = FluidStack.create(BuiltInRegistries.FLUID.get(buffer.readResourceLocation()), buffer.readVarLong());
            slot.setConfig(new GenericStack(AEFluidKey.of(fluid.getFluid()), fluid.getAmount()));
        }
        if (id == AMOUNT_CHANGE_ID) {
            if (slot.getConfig() != null) {
                int amt = buffer.readInt();
                slot.setConfig(ExportOnlyAESlot.copy(slot.getConfig(), amt));
            }
        }
        if (id == PICK_UP_ID) {
            if (slot.getStock() != null && slot.getStock().what() instanceof AEFluidKey key) {
                ItemStack currentStack = gui.getModularUIContainer().getCarried();
                int newStackSize = buffer.readVarInt();
                currentStack.setCount(newStackSize);
                gui.getModularUIContainer().setCarried(currentStack);

                FluidStack stack = FluidStack.create(key.getFluid(), slot.getStock().amount());
                if (key.hasTag()) {
                    stack.setTag(key.getTag().copy());
                }
                GenericStack stack1 = ExportOnlyAESlot.copy(slot.getStock(), Math.max(0, (slot.getStock().amount() - stack.getAmount())));
                slot.setStock(stack1.amount() == 0 ? null : stack1);
            }
        }
    }

    @Override
    public List<Target> getPhantomTargets(Object ingredient) {
        if (getFluidFromContainer(ingredient) == null) {
            return Collections.emptyList();
        }
        Rect2i rectangle = toRectangleBox();
        rectangle.setHeight(rectangle.getHeight() / 2);
        return Lists.newArrayList(new Target() {

            @Nonnull
            @Override
            public Rect2i getArea() {
                return rectangle;
            }

            @Override
            public void accept(@Nonnull Object ingredient) {
                FluidStack stack = getFluidFromContainer(ingredient);

                if (stack != null) {
                    CompoundTag compound = stack.saveToTag(new CompoundTag());
                    writeClientAction(LOAD_PHANTOM_FLUID_STACK_FROM_NBT, buf -> buf.writeNbt(compound));
                }
            }
        });
    }

    @Override
    public boolean mouseWheelMove(double mouseX, double mouseY, double wheelDelta) {
        IConfigurableSlot slot = this.parentWidget.getDisplay(this.index);
        Rect2i rectangle = toRectangleBox();
        rectangle.setHeight(rectangle.getHeight() / 2);
        if (slot.getConfig() == null || wheelDelta == 0 || !rectangle.contains((int) mouseX, (int) mouseY)) {
            return false;
        }
        FluidStack fluid = slot.getConfig().what() instanceof AEFluidKey fluidKey ? FluidStack.create(fluidKey.getFluid(), slot.getConfig().amount(), fluidKey.getTag()) : FluidStack.empty();
        long amt;
        if (isCtrlDown()) {
            amt = wheelDelta > 0 ? fluid.getAmount() * 2L : fluid.getAmount() / 2L;
        } else {
            amt = wheelDelta > 0 ? fluid.getAmount() + 1L : fluid.getAmount() - 1L;
        }

        if (amt > 0 && amt < Integer.MAX_VALUE + 1L) {
            int finalAmt = (int) amt;
            writeClientAction(AMOUNT_CHANGE_ID, buf -> buf.writeInt(finalAmt));
            return true;
        }
        return false;
    }

    private int tryClickContainer(boolean isShiftKeyDown) {
        MEInputHatchPartMachine.ExportOnlyAEFluid fluidTank = this.parentWidget.getConfig(this.index) instanceof MEInputHatchPartMachine.ExportOnlyAEFluid fluid ? fluid : null;
        if (fluidTank == null) return -1;
        Player player = gui.entityPlayer;
        ItemStack currentStack = gui.getModularUIContainer().getCarried();
        var handler = FluidTransferHelper.getFluidTransfer(gui.entityPlayer, gui.getModularUIContainer());
        if (handler == null) return -1;
        int maxAttempts = isShiftKeyDown ? currentStack.getCount() : 1;

        if (fluidTank.getFluidAmount() > 0) {
            boolean performedFill = false;
            FluidStack initialFluid = fluidTank.getFluid();
            for (int i = 0; i < maxAttempts; i++) {
                FluidActionResult result = FluidTransferHelper.tryFillContainer(currentStack, fluidTank, Integer.MAX_VALUE, null, false);
                if (!result.isSuccess()) break;
                ItemStack remainingStack = FluidTransferHelper.tryFillContainer(currentStack, fluidTank, Integer.MAX_VALUE, null, true).getResult();
                currentStack.shrink(1);
                performedFill = true;
                if (!remainingStack.isEmpty() && !player.addItem(remainingStack)) {
                    Block.popResource(player.level(), player.getOnPos(), remainingStack);
                    break;
                }
            }
            if (performedFill) {
                SoundEvent soundevent = FluidHelper.getFillSound(initialFluid);
                if (soundevent != null) {
                    player.level().playSound(null, player.position().x, player.position().y + 0.5, player.position().z, soundevent, SoundSource.BLOCKS, 1.0F, 1.0F);
                }
                gui.getModularUIContainer().setCarried(currentStack);
                return currentStack.getCount();
            }
        }

        return -1;
    }

}