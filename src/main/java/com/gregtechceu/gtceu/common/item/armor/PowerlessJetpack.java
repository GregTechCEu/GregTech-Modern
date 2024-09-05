package com.gregtechceu.gtceu.common.item.armor;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.*;
import com.gregtechceu.gtceu.api.item.armor.ArmorComponentItem;
import com.gregtechceu.gtceu.api.item.armor.ArmorUtils;
import com.gregtechceu.gtceu.api.item.armor.IArmorLogic;
import com.gregtechceu.gtceu.api.item.component.*;
import com.gregtechceu.gtceu.api.item.component.forge.IComponentCapability;
import com.gregtechceu.gtceu.api.misc.FluidRecipeHandler;
import com.gregtechceu.gtceu.api.misc.IgnoreEnergyRecipeHandler;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.utils.GradientUtil;
import com.gregtechceu.gtceu.utils.input.KeyBind;

import com.lowdragmc.lowdraglib.Platform;
import com.lowdragmc.lowdraglib.misc.ItemStackTransfer;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.lowdragmc.lowdraglib.side.fluid.FluidTransferHelper;
import com.lowdragmc.lowdraglib.side.fluid.IFluidTransfer;
import com.lowdragmc.lowdraglib.side.fluid.forge.FluidHelperImpl;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;

import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

public class PowerlessJetpack implements IArmorLogic, IJetpack, IItemHUDProvider {

    public static final int tankCapacity = 16000;

    private GTRecipe previousRecipe = null;
    private GTRecipe currentRecipe = null;
    private int burnTimer = 0;

    @OnlyIn(Dist.CLIENT)
    private ArmorUtils.ModularHUD HUD;

    public PowerlessJetpack() {
        if (Platform.isClient())
            HUD = new ArmorUtils.ModularHUD();
    }

    @Override
    public void onArmorTick(Level world, Player player, @NotNull ItemStack stack) {
        IFluidTransfer internalTank = FluidTransferHelper.getFluidTransfer(new ItemStackTransfer(stack), 0);
        if (internalTank == null)
            return;

        CompoundTag data = stack.getOrCreateTag();

        if (data.contains("burnTimer")) burnTimer = data.getShort("burnTimer");
        if (!data.contains("enabled")) {
            data.putBoolean("enabled", true);
            data.putBoolean("hover", false);
            data.putByte("toggleTimer", (byte) 0);
        }

        boolean jetpackEnabled = data.getBoolean("enabled");
        boolean hoverMode = data.getBoolean("hover");
        byte toggleTimer = data.getByte("toggleTimer");

        String messageKey = null;
        if (toggleTimer == 0) {
            if (KeyBind.JETPACK_ENABLE.isKeyDown(player)) {
                jetpackEnabled = !jetpackEnabled;
                messageKey = "metaarmor.jetpack.flight." + (jetpackEnabled ? "enable" : "disable");
                data.putBoolean("enabled", jetpackEnabled);
            } else if (KeyBind.ARMOR_HOVER.isKeyDown(player)) {
                hoverMode = !hoverMode;
                messageKey = "metaarmor.jetpack.hover." + (hoverMode ? "enable" : "disable");
                data.putBoolean("hover", hoverMode);
            }

            if (messageKey != null) {
                toggleTimer = 5;
                if (!world.isClientSide) player.displayClientMessage(Component.translatable(messageKey), true);
            }
        }

        if (toggleTimer > 0) toggleTimer--;
        data.putByte("toggleTimer", toggleTimer);

        // This causes a caching issue. currentRecipe is only set to null in findNewRecipe, so the fuel is never updated
        // Rewrite in Armor Rework
        if (currentRecipe == null)
            findNewRecipe(stack);

        performFlying(player, jetpackEnabled, hoverMode, stack);
        data.putShort("burnTimer", (short) burnTimer);
    }

    @Override
    public ArmorItem.Type getArmorType() {
        return ArmorItem.Type.CHESTPLATE;
    }

    @Override
    public int getArmorDisplay(Player player, @NotNull ItemStack armor, EquipmentSlot slot) {
        return 0;
    }

    @Override
    public void addToolComponents(@NotNull ArmorComponentItem item) {
        item.attachComponents(new Behaviour(tankCapacity));
    }

    @Override
    public ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
        return GTCEu.id("textures/armor/liquid_fuel_jetpack.png");
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void drawHUD(@NotNull ItemStack item, GuiGraphics guiGraphics) {
        IFluidTransfer tank = FluidTransferHelper.getFluidTransfer(new ItemStackTransfer(item), 0);
        if (tank != null) {
            if (tank.getFluidInTank(0).getAmount() == 0) return;
            String formated = String.format("%.1f",
                    (tank.getFluidInTank(0).getAmount() * 100.0F / tank.getTankCapacity(0)));
            this.HUD.newString(Component.translatable("metaarmor.hud.fuel_lvl", formated + "%"));
            CompoundTag data = item.getTag();

            if (data != null) {
                if (data.contains("enabled")) {
                    Component status = (data.getBoolean("enabled") ?
                            Component.translatable("metaarmor.hud.status.enabled") :
                            Component.translatable("metaarmor.hud.status.disabled"));
                    Component result = Component.translatable("metaarmor.hud.engine_enabled", status);
                    this.HUD.newString(result);
                }
                if (data.contains("hover")) {
                    Component status = (data.getBoolean("hover") ?
                            Component.translatable("metaarmor.hud.status.enabled") :
                            Component.translatable("metaarmor.hud.status.disabled"));
                    Component result = Component.translatable("metaarmor.hud.hover_mode", status);
                    this.HUD.newString(result);
                }
            }
        }
        this.HUD.draw(guiGraphics);
        this.HUD.reset();
    }

    @Override
    public int getEnergyPerUse() {
        return 1;
    }

    @Override
    public boolean canUseEnergy(ItemStack stack, int amount) {
        FluidStack fuel = getFuel();
        if (fuel == null) {
            return false;
        }

        IFluidTransfer fluidHandlerItem = getIFluidHandlerItem(stack);
        if (fluidHandlerItem == null)
            return false;

        com.lowdragmc.lowdraglib.side.fluid.FluidStack fluidStack = fluidHandlerItem.drain(fuel, false);
        if (fluidStack.isEmpty())
            return false;

        return fluidStack.getAmount() >= fuel.getAmount();
    }

    @Override
    public void drainEnergy(ItemStack stack, int amount) {
        if (this.burnTimer == 0) {
            FluidStack fuel = getFuel();
            if (fuel == null) return;
            getIFluidHandlerItem(stack).drain(fuel, true);
            burnTimer = currentRecipe.duration;
        }
        this.burnTimer--;
    }

    @Override
    public boolean hasEnergy(ItemStack stack) {
        return burnTimer > 0 || currentRecipe != null;
    }

    private static IFluidTransfer getIFluidHandlerItem(@NotNull ItemStack stack) {
        return FluidTransferHelper.getFluidTransfer(new ItemStackTransfer(stack), 0);
    }

    public void findNewRecipe(@NotNull ItemStack stack) {
        IFluidTransfer internalTank = getIFluidHandlerItem(stack);
        if (internalTank != null) {
            com.lowdragmc.lowdraglib.side.fluid.FluidStack fluidStack = internalTank.drain(1, false);
            if (previousRecipe != null && !fluidStack.isEmpty() &&
                    FluidRecipeCapability.CAP.of(previousRecipe.getInputContents(FluidRecipeCapability.CAP).get(0))
                            .test(fluidStack) &&
                    fluidStack.getAmount() > 0) {
                currentRecipe = previousRecipe;
                return;
            } else if (!fluidStack.isEmpty()) {
                Table<IO, RecipeCapability<?>, List<IRecipeHandler<?>>> table = Tables
                        .newCustomTable(new EnumMap<>(IO.class), IdentityHashMap::new);
                FluidRecipeHandler handler = new FluidRecipeHandler(IO.IN, 1, Long.MAX_VALUE);
                handler.getStorages()[0].setFluid(fluidStack);
                table.put(IO.IN, FluidRecipeCapability.CAP, Collections.singletonList(handler));
                table.put(IO.OUT, EURecipeCapability.CAP, Collections.singletonList(new IgnoreEnergyRecipeHandler()));
                IRecipeCapabilityHolder holder = new IRecipeCapabilityHolder() {

                    @Override
                    public @NotNull Table<IO, RecipeCapability<?>, List<IRecipeHandler<?>>> getCapabilitiesProxy() {
                        return table;
                    }
                };
                Iterator<GTRecipe> iterator = GTRecipeTypes.COMBUSTION_GENERATOR_FUELS.searchRecipe(holder);
                if (iterator.hasNext()) {
                    GTRecipe nextRecipe = iterator.next();
                    if (nextRecipe == null) {
                        return;
                    }
                    previousRecipe = nextRecipe;
                    currentRecipe = previousRecipe;
                    return;
                }
            }
        }
        currentRecipe = null;
    }

    public void resetRecipe() {
        currentRecipe = null;
        previousRecipe = null;
    }

    public FluidStack getFuel() {
        if (currentRecipe != null) {
            var recipeInputs = currentRecipe.inputs.get(FluidRecipeCapability.CAP);
            FluidIngredient fluid = FluidRecipeCapability.CAP.of(recipeInputs.get(0).content);
            return fluid.getStacks()[0];
        }

        return FluidStack.empty();
    }

    /*
     * @Override
     * public ISpecialArmor.ArmorProperties getProperties(EntityLivingBase player, @NotNull ItemStack armor,
     * 
     * @NotNull DamageSource source, double damage,
     * EntityEquipmentSlot equipmentSlot) {
     * int damageLimit = (int) Math.min(Integer.MAX_VALUE, burnTimer * 1.0 / 32 * 25.0);
     * if (source.isUnblockable()) return new ISpecialArmor.ArmorProperties(0, 0.0, 0);
     * return new ISpecialArmor.ArmorProperties(0, 0, damageLimit);
     * }
     */

    public static class Behaviour implements IDurabilityBar, IItemComponent, ISubItemHandler, IAddInformation,
                                  IInteractionItem, IComponentCapability {

        private static final Predicate<FluidStack> JETPACK_FUEL_FILTER = fluidStack -> {
            Table<IO, RecipeCapability<?>, List<IRecipeHandler<?>>> table = Tables
                    .newCustomTable(new EnumMap<>(IO.class), IdentityHashMap::new);
            FluidRecipeHandler handler = new FluidRecipeHandler(IO.IN, 1, Long.MAX_VALUE);
            handler.getStorages()[0].setFluid(fluidStack);
            table.put(IO.IN, FluidRecipeCapability.CAP, Collections.singletonList(handler));
            table.put(IO.OUT, EURecipeCapability.CAP, Collections.singletonList(new IgnoreEnergyRecipeHandler()));
            IRecipeCapabilityHolder holder = new IRecipeCapabilityHolder() {

                @Override
                public @NotNull Table<IO, RecipeCapability<?>, List<IRecipeHandler<?>>> getCapabilitiesProxy() {
                    return table;
                }
            };
            Iterator<GTRecipe> iterator = GTRecipeTypes.COMBUSTION_GENERATOR_FUELS.searchRecipe(holder);
            return iterator.hasNext() && iterator.next() != null;
        };

        public final int maxCapacity;
        private final Pair<Integer, Integer> durabilityBarColors;

        public Behaviour(int internalCapacity) {
            this.maxCapacity = internalCapacity;
            this.durabilityBarColors = GradientUtil.getGradient(0xB7AF08, 10);
        }

        @Override
        public float getDurabilityForDisplay(@NotNull ItemStack itemStack) {
            IFluidTransfer fluidHandlerItem = FluidTransferHelper.getFluidTransfer(new ItemStackTransfer(itemStack), 0);
            if (fluidHandlerItem == null) return 0;
            FluidStack fluidStack = fluidHandlerItem.getFluidInTank(0);
            return fluidStack.isEmpty() ? 0 :
                    (float) fluidStack.getAmount() / (float) fluidHandlerItem.getTankCapacity(0);
        }

        @Nullable
        @Override
        public Pair<Integer, Integer> getDurabilityColorsForDisplay(ItemStack itemStack) {
            return durabilityBarColors;
        }

        @Override
        public @NotNull <T> LazyOptional<T> getCapability(ItemStack itemStack, @NotNull Capability<T> cap) {
            return ForgeCapabilities.FLUID_HANDLER_ITEM.orEmpty(cap,
                    LazyOptional.of(() -> new FluidHandlerItemStack(itemStack, maxCapacity) {

                        @Override
                        public boolean canFillFluidType(net.minecraftforge.fluids.FluidStack fluid) {
                            return JETPACK_FUEL_FILTER.test(FluidHelperImpl.toFluidStack(fluid));
                        }
                    }));
        }

        @Override
        public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents,
                                    TooltipFlag isAdvanced) {
            CompoundTag data = stack.getOrCreateTag();
            Component state;
            boolean enabled = !data.contains("enabled") || data.getBoolean("enabled");
            state = enabled ? Component.translatable("metaarmor.hud.status.enabled") :
                    Component.translatable("metaarmor.hud.status.disabled");
            tooltipComponents.add(Component.translatable("metaarmor.hud.engine_enabled", state));

            boolean hover = data.contains("hover") && data.getBoolean("hover");
            state = hover ? Component.translatable("metaarmor.hud.status.enabled") :
                    Component.translatable("metaarmor.hud.status.disabled");
            tooltipComponents.add(Component.translatable("metaarmor.hud.hover_mode", state));
        }

        @Override
        public void fillItemCategory(Item item, CreativeModeTab category, NonNullList<ItemStack> items) {
            ItemStack copy = item.getDefaultInstance();
            IFluidTransfer fluidHandlerItem = FluidTransferHelper.getFluidTransfer(new ItemStackTransfer(copy), 0);
            if (fluidHandlerItem != null) {
                fluidHandlerItem.fill(GTMaterials.Diesel.getFluid(tankCapacity), true);
                items.add(copy);
            } else {
                items.add(copy);
            }
        }
    }
}
