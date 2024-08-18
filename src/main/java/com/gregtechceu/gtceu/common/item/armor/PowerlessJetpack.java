package com.gregtechceu.gtceu.common.item.armor;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.*;
import com.gregtechceu.gtceu.api.item.armor.ArmorComponentItem;
import com.gregtechceu.gtceu.api.item.armor.ArmorUtils;
import com.gregtechceu.gtceu.api.item.armor.IArmorLogic;
import com.gregtechceu.gtceu.api.item.component.*;
import com.gregtechceu.gtceu.api.item.component.forge.IComponentCapability;
import com.gregtechceu.gtceu.api.item.datacomponents.GTArmor;
import com.gregtechceu.gtceu.api.misc.FluidRecipeHandler;
import com.gregtechceu.gtceu.api.misc.IgnoreEnergyRecipeHandler;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.data.material.GTMaterials;
import com.gregtechceu.gtceu.data.recipe.GTRecipeTypes;
import com.gregtechceu.gtceu.data.tag.GTDataComponents;
import com.gregtechceu.gtceu.utils.GradientUtil;
import com.gregtechceu.gtceu.utils.input.KeyBind;

import com.lowdragmc.lowdraglib.Platform;
import com.lowdragmc.lowdraglib.side.fluid.FluidTransferHelper;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidHandlerItemStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

public class PowerlessJetpack implements IArmorLogic, IJetpack, IItemHUDProvider {

    public static final int tankCapacity = 16000;

    private RecipeHolder<GTRecipe> previousRecipe = null;
    private RecipeHolder<GTRecipe> currentRecipe = null;
    private int burnTimer = 0;

    @OnlyIn(Dist.CLIENT)
    private ArmorUtils.ModularHUD HUD;

    public PowerlessJetpack() {
        if (Platform.isClient())
            HUD = new ArmorUtils.ModularHUD();
    }

    @Override
    public void onArmorTick(Level world, Player player, @NotNull ItemStack stack) {
        IFluidHandler internalTank = FluidTransferHelper.getFluidTransfer(new CustomItemStackHandler(stack), 0);
        if (internalTank == null)
            return;

        GTArmor data = stack.get(GTDataComponents.ARMOR_DATA);
        if (data == null) {
            return;
        }
        burnTimer = data.burnTimer();
        byte toggleTimer = data.toggleTimer();
        boolean hoverMode = data.hover();
        boolean jetpackEnabled = data.enabled();

        if (toggleTimer == 0 && KeyBind.ARMOR_HOVER.isKeyDown(player)) {
            hoverMode = !hoverMode;
            toggleTimer = 5;
            final boolean finalHover = hoverMode;
            stack.update(GTDataComponents.ARMOR_DATA, new GTArmor(), data1 -> data1.setHover(finalHover));
            if (!world.isClientSide) {
                player.displayClientMessage(
                        Component.translatable("metaarmor.jetpack.hover." + (hoverMode ? "enable" : "disable")), true);
            }
        }

        if (toggleTimer == 0 && KeyBind.JETPACK_ENABLE.isKeyDown(player)) {
            jetpackEnabled = !jetpackEnabled;
            toggleTimer = 5;
            final boolean finalEnabled = jetpackEnabled;
            stack.update(GTDataComponents.ARMOR_DATA, new GTArmor(), data1 -> data1.setEnabled(finalEnabled));
            if (!world.isClientSide) {
                player.displayClientMessage(
                        Component.translatable("metaarmor.jetpack.flight." + (jetpackEnabled ? "enable" : "disable")),
                        true);
            }
        }

        // This causes a caching issue. currentRecipe is only set to null in findNewRecipe, so the fuel is never updated
        // Rewrite in Armor Rework
        if (currentRecipe == null)
            findNewRecipe(stack);

        performFlying(player, jetpackEnabled, hoverMode, stack);

        if (toggleTimer > 0)
            toggleTimer--;

        final byte finalToggleTimer = toggleTimer;
        final boolean finalHover = hoverMode;
        final boolean finalEnabled = jetpackEnabled;
        stack.update(GTDataComponents.ARMOR_DATA, new GTArmor(),
                data1 -> data1.setHover(finalHover)
                        .setBurnTimer((short) burnTimer)
                        .setToggleTimer(finalToggleTimer)
                        .setEnabled(finalEnabled));
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
    public ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot,
                                            ArmorMaterial.Layer layer) {
        return GTCEu.id("textures/armor/liquid_fuel_jetpack.png");
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void drawHUD(@NotNull ItemStack item, GuiGraphics guiGraphics) {
        IFluidHandler tank = FluidTransferHelper.getFluidTransfer(new CustomItemStackHandler(item), 0);
        if (tank != null) {
            if (tank.getFluidInTank(0).getAmount() == 0) return;
            String formated = String.format("%.1f",
                    (tank.getFluidInTank(0).getAmount() * 100.0F / tank.getTankCapacity(0)));
            this.HUD.newString(Component.translatable("metaarmor.hud.fuel_lvl", formated + "%"));
            GTArmor data = item.get(GTDataComponents.ARMOR_DATA);

            if (data != null) {
                Component status = data.enabled() ?
                        Component.translatable("metaarmor.hud.status.enabled") :
                        Component.translatable("metaarmor.hud.status.disabled");
                Component result = Component.translatable("metaarmor.hud.engine_enabled", status);
                this.HUD.newString(result);

                status = data.hover() ?
                        Component.translatable("metaarmor.hud.status.enabled") :
                        Component.translatable("metaarmor.hud.status.disabled");
                result = Component.translatable("metaarmor.hud.hover_mode", status);
                this.HUD.newString(result);
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

        IFluidHandler fluidHandlerItem = getIFluidHandlerItem(stack);
        if (fluidHandlerItem == null)
            return false;

        FluidStack fluidStack = fluidHandlerItem.drain(fuel, IFluidHandler.FluidAction.SIMULATE);
        if (fluidStack.isEmpty())
            return false;

        return fluidStack.getAmount() >= fuel.getAmount();
    }

    @Override
    public void drainEnergy(ItemStack stack, int amount) {
        if (this.burnTimer == 0) {
            FluidStack fuel = getFuel();
            if (fuel == null) return;
            getIFluidHandlerItem(stack).drain(fuel, IFluidHandler.FluidAction.EXECUTE);
            burnTimer = currentRecipe.value().duration;
        }
        this.burnTimer--;
    }

    @Override
    public boolean hasEnergy(ItemStack stack) {
        return burnTimer > 0 || currentRecipe != null;
    }

    private static IFluidHandler getIFluidHandlerItem(@NotNull ItemStack stack) {
        return FluidTransferHelper.getFluidTransfer(new CustomItemStackHandler(stack), 0);
    }

    public void findNewRecipe(@NotNull ItemStack stack) {
        IFluidHandler internalTank = getIFluidHandlerItem(stack);
        if (internalTank != null) {
            FluidStack fluidStack = internalTank.drain(1, IFluidHandler.FluidAction.EXECUTE);
            if (previousRecipe != null && !fluidStack.isEmpty() &&
                    FluidRecipeCapability.CAP
                            .of(previousRecipe.value().getInputContents(FluidRecipeCapability.CAP).get(0))
                            .test(fluidStack) &&
                    fluidStack.getAmount() > 0) {
                currentRecipe = previousRecipe;
                return;
            } else if (!fluidStack.isEmpty()) {
                Table<IO, RecipeCapability<?>, List<IRecipeHandler<?>>> table = Tables
                        .newCustomTable(new EnumMap<>(IO.class), IdentityHashMap::new);
                FluidRecipeHandler handler = new FluidRecipeHandler(IO.IN, 1, Integer.MAX_VALUE);
                handler.getStorages()[0].setFluid(fluidStack);
                table.put(IO.IN, FluidRecipeCapability.CAP, Collections.singletonList(handler));
                table.put(IO.OUT, EURecipeCapability.CAP, Collections.singletonList(new IgnoreEnergyRecipeHandler()));
                IRecipeCapabilityHolder holder = new IRecipeCapabilityHolder() {

                    @Override
                    public @NotNull Table<IO, RecipeCapability<?>, List<IRecipeHandler<?>>> getCapabilitiesProxy() {
                        return table;
                    }
                };
                Iterator<RecipeHolder<GTRecipe>> iterator = GTRecipeTypes.COMBUSTION_GENERATOR_FUELS
                        .searchRecipe(holder);
                if (iterator.hasNext()) {
                    RecipeHolder<GTRecipe> nextRecipe = iterator.next();
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
            var recipeInputs = currentRecipe.value().inputs.get(FluidRecipeCapability.CAP);
            SizedFluidIngredient fluid = FluidRecipeCapability.CAP.of(recipeInputs.getFirst().content);
            return fluid.getFluids()[0];
        }

        return FluidStack.EMPTY;
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
            FluidRecipeHandler handler = new FluidRecipeHandler(IO.IN, 1, Integer.MAX_VALUE);
            handler.getStorages()[0].setFluid(fluidStack);
            table.put(IO.IN, FluidRecipeCapability.CAP, Collections.singletonList(handler));
            table.put(IO.OUT, EURecipeCapability.CAP, Collections.singletonList(new IgnoreEnergyRecipeHandler()));
            IRecipeCapabilityHolder holder = new IRecipeCapabilityHolder() {

                @Override
                public @NotNull Table<IO, RecipeCapability<?>, List<IRecipeHandler<?>>> getCapabilitiesProxy() {
                    return table;
                }
            };
            Iterator<RecipeHolder<GTRecipe>> iterator = GTRecipeTypes.COMBUSTION_GENERATOR_FUELS.searchRecipe(holder);
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
            IFluidHandler fluidHandlerItem = FluidTransferHelper.getFluidTransfer(new CustomItemStackHandler(itemStack),
                    0);
            if (fluidHandlerItem == null) return 0;
            net.neoforged.neoforge.fluids.FluidStack fluidStack = fluidHandlerItem.getFluidInTank(0);
            return fluidStack.isEmpty() ? 0 :
                    (float) fluidStack.getAmount() / (float) fluidHandlerItem.getTankCapacity(0);
        }

        @Nullable
        @Override
        public Pair<Integer, Integer> getDurabilityColorsForDisplay(ItemStack itemStack) {
            return durabilityBarColors;
        }

        @Override
        public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents,
                                    TooltipFlag isAdvanced) {
            GTArmor data = stack.getOrDefault(GTDataComponents.ARMOR_DATA, new GTArmor());

            Component state = data.enabled() ? Component.translatable("metaarmor.hud.status.enabled") :
                    Component.translatable("metaarmor.hud.status.disabled");
            tooltipComponents.add(Component.translatable("metaarmor.hud.engine_enabled", state));

            state = data.hover() ? Component.translatable("metaarmor.hud.status.enabled") :
                    Component.translatable("metaarmor.hud.status.disabled");
            tooltipComponents.add(Component.translatable("metaarmor.hud.hover_mode", state));
        }

        @Override
        public void attachCapabilities(RegisterCapabilitiesEvent event, Item item) {
            event.registerItem(Capabilities.FluidHandler.ITEM,
                    (stack, unused) -> new FluidHandlerItemStack(GTDataComponents.FLUID_CONTENT, stack, maxCapacity) {

                        @Override
                        public boolean canFillFluidType(FluidStack fluid) {
                            return JETPACK_FUEL_FILTER.test(fluid);
                        }
                    }, item);
        }

        @Override
        public void fillItemCategory(Item item, CreativeModeTab category, NonNullList<ItemStack> items) {
            ItemStack copy = item.getDefaultInstance();
            IFluidHandler fluidHandlerItem = FluidTransferHelper.getFluidTransfer(new CustomItemStackHandler(copy), 0);
            if (fluidHandlerItem != null) {
                fluidHandlerItem.fill(GTMaterials.Diesel.getFluid(tankCapacity), IFluidHandler.FluidAction.EXECUTE);
                items.add(copy);
            } else {
                items.add(copy);
            }
        }
    }
}
