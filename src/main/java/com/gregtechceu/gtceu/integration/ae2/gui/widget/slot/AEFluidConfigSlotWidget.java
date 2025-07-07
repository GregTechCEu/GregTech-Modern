package com.gregtechceu.gtceu.integration.ae2.gui.widget.slot;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.misc.IGhostFluidTarget;
import com.gregtechceu.gtceu.integration.ae2.gui.widget.ConfigWidget;
import com.gregtechceu.gtceu.integration.ae2.slot.ExportOnlyAEFluidSlot;
import com.gregtechceu.gtceu.integration.ae2.slot.ExportOnlyAESlot;
import com.gregtechceu.gtceu.integration.ae2.slot.IConfigurableSlot;
import com.gregtechceu.gtceu.integration.ae2.utils.AEUtil;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTMath;

import com.lowdragmc.lowdraglib.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib.side.fluid.forge.FluidHelperImpl;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.SoundActions;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.GenericStack;
import org.jetbrains.annotations.NotNull;

import static com.lowdragmc.lowdraglib.gui.util.DrawerHelper.drawStringFixedCorner;

public class AEFluidConfigSlotWidget extends AEConfigSlotWidget implements IGhostFluidTarget {

    public AEFluidConfigSlotWidget(int x, int y, ConfigWidget widget, int index) {
        super(new Position(x, y), new Size(18, 18 * 2), widget, index);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void drawInBackground(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);
        Position position = getPosition();
        IConfigurableSlot slot = this.parentWidget.getDisplay(this.index);
        GenericStack config = slot.getConfig();
        GenericStack stock = slot.getStock();
        drawSlots(graphics, mouseX, mouseY, position.x, position.y, parentWidget.isAutoPull());
        if (this.select) {
            GuiTextures.SELECT_BOX.draw(graphics, mouseX, mouseY, position.x, position.y, 18, 18);
        }

        int stackX = position.x + 1;
        int stackY = position.y + 1;
        if (config != null) {
            var stack = AEUtil.toFluidStack(config);
            if (!stack.isEmpty()) {
                DrawerHelper.drawFluidForGui(graphics, FluidHelperImpl.toFluidStack(stack), config.amount(), stackX,
                        stackY, 16, 16);
                if (!parentWidget.isStocking()) {
                    String amountStr = FormattingUtil.formatNumberReadable(config.amount(), true,
                            FormattingUtil.DECIMAL_FORMAT_0F, "B");
                    drawStringFixedCorner(graphics, amountStr, stackX + 17, stackY + 17, 16777215, true, 0.5f);
                }
            }
        }
        if (stock != null) {
            var stack = AEUtil.toFluidStack(stock);
            if (!stack.isEmpty()) {
                DrawerHelper.drawFluidForGui(graphics, FluidHelperImpl.toFluidStack(stack), stock.amount(), stackX,
                        stackY + 18, 16,
                        16);
                String amountStr = FormattingUtil.formatNumberReadable(stock.amount(), true,
                        FormattingUtil.DECIMAL_FORMAT_0F, "B");
                drawStringFixedCorner(graphics, amountStr, stackX + 17, stackY + 18 + 17, 16777215, true, 0.5f);
            }
        }

        if (mouseOverConfig(mouseX, mouseY)) {
            drawSelectionOverlay(graphics, stackX, stackY, 16, 16);
        } else if (mouseOverStock(mouseX, mouseY)) {
            drawSelectionOverlay(graphics, stackX, stackY + 18, 16, 16);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void drawSlots(GuiGraphics graphics, int mouseX, int mouseY, int x, int y, boolean autoPull) {
        if (autoPull) {
            GuiTextures.SLOT_DARK.draw(graphics, mouseX, mouseY, x, y, 18, 18);
            GuiTextures.CONFIG_ARROW_DARK.draw(graphics, mouseX, mouseY, x, y, 18, 18);
        } else {
            GuiTextures.FLUID_SLOT.draw(graphics, mouseX, mouseY, x, y, 18, 18);
            GuiTextures.CONFIG_ARROW.draw(graphics, mouseX, mouseY, x, y, 18, 18);
        }
        GuiTextures.SLOT_DARK.draw(graphics, mouseX, mouseY, x, y + 18, 18, 18);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseOverConfig(mouseX, mouseY)) {
            // don't allow manual interaction with config slots when auto pull is enabled
            if (parentWidget.isAutoPull()) {
                return false;
            }

            if (button == 1) {
                // Right click to clear
                writeClientAction(REMOVE_ID, buf -> {});

                if (!parentWidget.isStocking()) {
                    this.parentWidget.disableAmountClient();
                }
            } else if (button == 0) {
                // Left click to set/select
                ItemStack hold = this.gui.getModularUIContainer().getCarried();
                FluidUtil.getFluidContained(hold).ifPresent(f -> writeClientAction(UPDATE_ID, f::writeToPacket));

                if (!parentWidget.isStocking()) {
                    this.parentWidget.enableAmountClient(this.index);
                    this.select = true;
                }
            }
            return true;
        } else if (mouseOverStock(mouseX, mouseY)) {
            // Left click to pick up
            if (button == 0) {
                if (parentWidget.isStocking()) {
                    return false;
                }
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
            FluidStack fluid = FluidStack.readFromPacket(buffer);
            var stack = AEUtil.fromFluidStack(fluid);
            if (!isStackValidForSlot(stack)) return;
            slot.setConfig(stack);
            this.parentWidget.enableAmount(this.index);
            if (fluid != FluidStack.EMPTY) {
                writeUpdateInfo(UPDATE_ID, fluid::writeToPacket);
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
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void readUpdateInfo(int id, FriendlyByteBuf buffer) {
        super.readUpdateInfo(id, buffer);
        IConfigurableSlot slot = this.parentWidget.getDisplay(this.index);
        if (id == REMOVE_ID) {
            slot.setConfig(null);
        }
        if (id == UPDATE_ID) {
            FluidStack fluid = new FluidStack(BuiltInRegistries.FLUID.get(buffer.readResourceLocation()),
                    buffer.readVarInt());
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

                FluidStack stack = new FluidStack(key.getFluid(), GTMath.saturatedCast(slot.getStock().amount()));
                if (key.hasTag()) {
                    stack.setTag(key.getTag().copy());
                }
                GenericStack stack1 = ExportOnlyAESlot.copy(slot.getStock(),
                        Math.max(0, (slot.getStock().amount() - stack.getAmount())));
                slot.setStock(stack1.amount() == 0 ? null : stack1);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public Rect2i getRectangleBox() {
        Rect2i rectangle = toRectangleBox();
        rectangle.setHeight(rectangle.getHeight() / 2);
        return rectangle;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void acceptFluid(FluidStack fluidStack) {
        if (fluidStack.getRawFluid() != Fluids.EMPTY && fluidStack.getAmount() <= 0L) {
            fluidStack.setAmount(1000);
        }

        if (!fluidStack.isEmpty()) {
            writeClientAction(UPDATE_ID, fluidStack::writeToPacket);
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean mouseWheelMove(double mouseX, double mouseY, double wheelDelta) {
        // Only allow the amount scrolling if not stocking, as amount is useless for stocking
        if (parentWidget.isStocking()) return false;
        IConfigurableSlot slot = this.parentWidget.getDisplay(this.index);
        Rect2i rectangle = toRectangleBox();
        rectangle.setHeight(rectangle.getHeight() / 2);
        if (slot.getConfig() == null || wheelDelta == 0 || !rectangle.contains((int) mouseX, (int) mouseY)) {
            return false;
        }
        FluidStack fluid = slot.getConfig().what() instanceof AEFluidKey fluidKey ?
                new FluidStack(fluidKey.getFluid(), GTMath.saturatedCast(slot.getConfig().amount()),
                        fluidKey.getTag()) :
                FluidStack.EMPTY;
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
        ExportOnlyAEFluidSlot fluidTank = this.parentWidget
                .getConfig(this.index) instanceof ExportOnlyAEFluidSlot fluid ? fluid : null;
        if (fluidTank == null) return -1;
        Player player = gui.entityPlayer;
        ItemStack currentStack = gui.getModularUIContainer().getCarried();
        var handler = FluidUtil.getFluidHandler(currentStack).resolve().orElse(null);
        if (handler == null) return -1;
        int maxAttempts = isShiftKeyDown ? currentStack.getCount() : 1;

        if (fluidTank.getFluidAmount() > 0) {
            boolean performedFill = false;
            FluidStack initialFluid = fluidTank.getFluid();
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
                SoundEvent soundevent = initialFluid.getFluid().getFluidType().getSound(initialFluid,
                        SoundActions.BUCKET_FILL);
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
}
