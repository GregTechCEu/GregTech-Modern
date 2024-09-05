package com.gregtechceu.gtceu.utils;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.api.fluids.store.FluidStorageKeys;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.side.fluid.FluidHelper;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.lowdragmc.lowdraglib.side.fluid.FluidTransferHelper;
import com.lowdragmc.lowdraglib.side.fluid.IFluidTransfer;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Tuple;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.Tags;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.datafixers.util.Pair;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.LongPredicate;

import static com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey.HAZARD;

/**
 * @author KilaBash
 * @date 2023/2/17
 * @implNote GTUtil
 */
public class GTUtil {

    public static final Direction[] DIRECTIONS = Direction.values();

    @Nullable
    public static Direction determineWrenchingSide(Direction facing, float x, float y, float z) {
        Direction opposite = facing.getOpposite();
        switch (facing) {
            case DOWN, UP -> {
                if (x < 0.25) {
                    if (z < 0.25) return opposite;
                    if (z > 0.75) return opposite;
                    return Direction.WEST;
                }
                if (x > 0.75) {
                    if (z < 0.25) return opposite;
                    if (z > 0.75) return opposite;
                    return Direction.EAST;
                }
                if (z < 0.25) return Direction.NORTH;
                if (z > 0.75) return Direction.SOUTH;
                return facing;
            }
            case NORTH, SOUTH -> {
                if (x < 0.25) {
                    if (y < 0.25) return opposite;
                    if (y > 0.75) return opposite;
                    return Direction.WEST;
                }
                if (x > 0.75) {
                    if (y < 0.25) return opposite;
                    if (y > 0.75) return opposite;
                    return Direction.EAST;
                }
                if (y < 0.25) return Direction.DOWN;
                if (y > 0.75) return Direction.UP;
                return facing;
            }
            case WEST, EAST -> {
                if (z < 0.25) {
                    if (y < 0.25) return opposite;
                    if (y > 0.75) return opposite;
                    return Direction.NORTH;
                }
                if (z > 0.75) {
                    if (y < 0.25) return opposite;
                    if (y > 0.75) return opposite;
                    return Direction.SOUTH;
                }
                if (y < 0.25) return Direction.DOWN;
                if (y > 0.75) return Direction.UP;
                return facing;
            }
        }
        return null;
    }

    /**
     * Calculates on which side the neighbor is relative to the main pos.
     *
     * @param main     main pos
     * @param neighbor neighbor pos
     * @return position of neighbor relative to main or null the neighbor pos is not a neighbor
     */
    @Nullable
    public static Direction getFacingToNeighbor(@NotNull BlockPos main, @NotNull BlockPos neighbor) {
        int difX = neighbor.getX() - main.getX();
        int difY = neighbor.getY() - main.getY();
        int difZ = neighbor.getZ() - main.getZ();
        if (difX != 0) {
            if (difY != 0 || difZ != 0 || (difX != 1 && difX != -1)) return null;
            return difX > 0 ? Direction.EAST : Direction.WEST;
        }
        if (difY != 0) {
            if (difZ != 0 || (difY != 1 && difY != -1)) return null;
            return difY > 0 ? Direction.UP : Direction.DOWN;
        }
        if (difZ != 0) {
            if (difZ != 1 && difZ != -1) return null;
            return difZ > 0 ? Direction.SOUTH : Direction.NORTH;
        }
        return null;
    }

    public static float getExplosionPower(long voltage) {
        return getTierByVoltage(voltage) + 1;
    }

    public static int getRedstonePower(Level world, BlockPos blockPos, Direction side) {
        BlockPos offsetPos = blockPos.relative(side);
        int worldPower = world.getDirectSignal(offsetPos, side);
        if (worldPower < 15) {
            BlockState offsetState = world.getBlockState(offsetPos);
            if (offsetState.getBlock() instanceof RedStoneWireBlock) {
                int wirePower = offsetState.getValue(RedStoneWireBlock.POWER);
                return Math.max(worldPower, wirePower);
            }
        }
        return worldPower;
    }

    /**
     * @param array Array sorted with natural order
     * @param value Value to search for
     * @return Index of the nearest value lesser or equal than {@code value},
     *         or {@code -1} if there's no entry matching the condition
     */
    public static int nearestLesserOrEqual(@NotNull long[] array, long value) {
        int low = 0, high = array.length - 1;
        while (true) {
            int median = (low + high) / 2;
            if (array[median] <= value) {
                if (low == high) return low;
                low = median + 1;
            } else {
                if (low == high) return low - 1;
                high = median - 1;
            }
        }
    }

    /**
     * @param array Array sorted with natural order
     * @param value Value to search for
     * @return Index of the nearest value lesser than {@code value},
     *         or {@code -1} if there's no entry matching the condition
     */
    public static int nearestLesser(@NotNull long[] array, long value) {
        int low = 0, high = array.length - 1;
        while (true) {
            int median = (low + high) / 2;
            if (array[median] < value) {
                if (low == high) return low;
                low = median + 1;
            } else {
                if (low == high) return low - 1;
                high = median - 1;
            }
        }
    }

    /**
     * @return Lowest tier of the voltage that can handle {@code voltage}; that is,
     *         a voltage with value greater than equal than {@code voltage}. If there's no
     *         tier that can handle it, {@code MAX} is returned.
     */
    public static byte getTierByVoltage(long voltage) {
        if (voltage > Integer.MAX_VALUE) {
            return GTValues.MAX;
        }
        return getOCTierByVoltage(voltage);
    }

    public static byte getOCTierByVoltage(long voltage) {
        if (voltage <= GTValues.V[GTValues.ULV]) {
            return GTValues.ULV;
        }
        return (byte) ((62 - Long.numberOfLeadingZeros(voltage - 1)) >> 1);
    }

    /**
     * Ex: This method turns both 1024 and 512 into HV.
     *
     * @return the highest voltage tier with value below or equal to {@code voltage}, or
     *         {@code ULV} if there's no tier below
     */
    public static byte getFloorTierByVoltage(long voltage) {
        if (voltage < GTValues.V[GTValues.ULV]) {
            return GTValues.ULV;
        }
        if (voltage == GTValues.VEX[GTValues.MAX_TRUE]) {
            return GTValues.MAX_TRUE;
        }

        return (byte) ((60 - Long.numberOfLeadingZeros(voltage)) >> 1);
    }

    public static ItemStack copy(ItemStack... stacks) {
        for (ItemStack stack : stacks)
            if (!stack.isEmpty()) return stack.copy();
        return ItemStack.EMPTY;
    }

    public static ItemStack copyAmount(int amount, ItemStack... stacks) {
        ItemStack stack = copy(stacks);
        if (stack.isEmpty()) return ItemStack.EMPTY;
        if (amount > 64) amount = 64;
        else if (amount == -1) amount = 111;
        else if (amount < 0) amount = 0;
        stack.setCount(amount);
        return stack;
    }

    public static FluidStack copyAmount(int amount, FluidStack fluidStack) {
        if (fluidStack == null) return null;
        FluidStack stack = fluidStack.copy();
        stack.setAmount(amount);
        return stack;
    }

    public static <M> M selectItemInList(int index, M replacement, List<M> list, Class<M> minClass) {
        if (list.isEmpty())
            return replacement;

        M maybeResult;
        if (list.size() <= index) {
            maybeResult = list.get(list.size() - 1);
        } else if (index < 0) {
            maybeResult = list.get(0);
        } else maybeResult = list.get(index);

        if (maybeResult != null) return maybeResult;
        return replacement;
    }

    public static <M> M getItem(List<? extends M> list, int index, M replacement) {
        if (index >= 0 && index < list.size())
            return list.get(index);
        return replacement;
    }

    public static <T> int getRandomItem(RandomSource random, List<? extends Entry<Integer, T>> randomList, int size) {
        if (randomList.isEmpty())
            return -1;
        int[] baseOffsets = new int[size];
        int currentIndex = 0;
        for (int i = 0; i < size; i++) {
            Entry<Integer, T> entry = randomList.get(i);
            if (entry.getKey() <= 0) {
                throw new IllegalArgumentException("Invalid weight: " + entry.getKey());
            }
            currentIndex += entry.getKey();
            baseOffsets[i] = currentIndex;
        }
        int randomValue = random.nextInt(currentIndex);
        for (int i = 0; i < size; i++) {
            if (randomValue < baseOffsets[i])
                return i;
        }
        throw new IllegalArgumentException("Invalid weight");
    }

    public static <T> int getRandomItem(List<? extends Entry<Integer, T>> randomList, int size) {
        return getRandomItem(GTValues.RNG, randomList, size);
    }

    @SuppressWarnings("unchecked")
    public static <T, R> Class<T> getActualTypeParameter(Class<? extends R> thisClass, int index) {
        Type type = thisClass.getGenericSuperclass();
        return (Class<T>) ((ParameterizedType) type).getActualTypeArguments()[index];
    }

    public static boolean isShiftDown() {
        if (LDLib.isClient()) {
            var id = Minecraft.getInstance().getWindow().getWindow();
            return InputConstants.isKeyDown(id, GLFW.GLFW_KEY_LEFT_SHIFT) ||
                    InputConstants.isKeyDown(id, GLFW.GLFW_KEY_LEFT_SHIFT);
        }
        return false;
    }

    public static boolean isCtrlDown() {
        if (LDLib.isClient()) {
            var id = Minecraft.getInstance().getWindow().getWindow();
            return InputConstants.isKeyDown(id, GLFW.GLFW_KEY_LEFT_CONTROL) ||
                    InputConstants.isKeyDown(id, GLFW.GLFW_KEY_RIGHT_CONTROL);
        }
        return false;
    }

    public static boolean isAltDown() {
        if (LDLib.isClient()) {
            var id = Minecraft.getInstance().getWindow().getWindow();
            return InputConstants.isKeyDown(id, GLFW.GLFW_KEY_LEFT_ALT) ||
                    InputConstants.isKeyDown(id, GLFW.GLFW_KEY_RIGHT_ALT);
        }
        return false;
    }

    public static boolean isFluidStackAmountDivisible(FluidStack fluidStack, int divisor) {
        return fluidStack.getAmount() % divisor == 0 && fluidStack.getAmount() % divisor != fluidStack.getAmount() &&
                fluidStack.getAmount() / divisor != 0;
    }

    public static boolean isItemStackCountDivisible(ItemStack itemStack, int divisor) {
        return itemStack.getCount() % divisor == 0 && itemStack.getCount() % divisor != itemStack.getCount() &&
                itemStack.getCount() / divisor != 0;
    }

    public static int getItemBurnTime(Item item) {
        return ForgeHooks.getBurnTime(item.getDefaultInstance(), RecipeType.SMELTING);
    }

    public static long getPumpBiomeModifier(Holder<Biome> biome) {
        if (biome.is(BiomeTags.IS_NETHER)) {
            return -1;
        }

        if (biome.is(BiomeTags.IS_DEEP_OCEAN) || biome.is(BiomeTags.IS_OCEAN) || biome.is(BiomeTags.IS_BEACH) ||
                biome.is(BiomeTags.IS_RIVER)) {
            return FluidHelper.getBucket();
        } else if (biome.is(Tags.Biomes.IS_SWAMP) || biome.is(Tags.Biomes.IS_WET)) {
            return FluidHelper.getBucket() * 4 / 5;
        } else if (biome.is(BiomeTags.IS_JUNGLE)) {
            return FluidHelper.getBucket() * 35 / 100;
        } else if (biome.is(Tags.Biomes.IS_SNOWY)) {
            return FluidHelper.getBucket() * 3 / 10;
        } else if (biome.is(Tags.Biomes.IS_PLAINS) || biome.is(BiomeTags.IS_FOREST)) {
            return FluidHelper.getBucket() / 4;
        } else if (biome.is(Tags.Biomes.IS_COLD)) {
            return FluidHelper.getBucket() * 175 / 1000;
        } else if (biome.is(CustomTags.IS_SANDY)) {
            return FluidHelper.getBucket() * 170 / 1000;
        }
        return FluidHelper.getBucket() / 10;
    }

    /**
     * Determines dye color nearest to specified RGB color
     */
    public static DyeColor determineDyeColor(int rgbColor) {
        float[] c = GradientUtil.getRGB(rgbColor);

        Map<Double, DyeColor> distances = new HashMap<>();
        for (DyeColor dyeColor : DyeColor.values()) {
            float[] c2 = GradientUtil.getRGB(dyeColor.getTextColor());

            double distance = (c[0] - c2[0]) * (c[0] - c2[0]) + (c[1] - c2[1]) * (c[1] - c2[1]) +
                    (c[2] - c2[2]) * (c[2] - c2[2]);

            distances.put(distance, dyeColor);
        }

        double min = Collections.min(distances.keySet());
        return distances.get(min);
    }

    public static double geometricMean(double first, double... numbers) {
        for (double number : numbers) {
            first *= number;
        }
        return Math.pow(first, 1D / (1 + numbers.length));
    }

    public static long binarySearch(long minValue, long maxValue, LongPredicate test, boolean ascending) {
        while (maxValue - minValue > 1) {
            long middle = (minValue + maxValue) / 2;
            // XOR
            if (test.test(middle) ^ !ascending) {
                maxValue = middle;
            } else {
                minValue = middle;
            }
        }
        return test.test(ascending ? minValue : maxValue) ^ ascending ? maxValue : minValue;
    }

    public static int convertRGBtoARGB(int colorValue) {
        return convertRGBtoARGB(colorValue, 0xFF);
    }

    public static int convertRGBtoARGB(int colorValue, int opacity) {
        // preserve existing opacity if present
        if (((colorValue >> 24) & 0xFF) != 0) return colorValue;
        return opacity << 24 | colorValue;
    }

    public static int[] convertARGBtoArray(int argb) {
        int a = argb >> 24 & 255;
        int r = argb >> 16 & 255;
        int g = argb >> 8 & 255;
        int b = argb & 255;
        return new int[] { a, r, g, b };
    }

    @Contract(pure = true)
    public static boolean evalMask(@NotNull Enum<?> anEnum, byte mask) {
        return (mask & (1 << anEnum.ordinal())) > 0;
    }

    @Contract(pure = true)
    public static boolean evalMask(@NotNull Enum<?> anEnum, @NotNull BitSet mask) {
        return mask.get(anEnum.ordinal());
    }

    @Contract(pure = true)
    @NotNull
    public static <T extends Enum<T>> EnumSet<T> maskToSet(@NotNull Class<T> enumClass, byte mask) {
        EnumSet<T> set = EnumSet.noneOf(enumClass);
        for (T anEnum : enumClass.getEnumConstants()) {
            if (evalMask(anEnum, mask)) set.add(anEnum);
        }
        return set;
    }

    @Contract(pure = true)
    @NotNull
    public static <T extends Enum<T>> EnumSet<T> maskToSet(@NotNull Class<T> enumClass, @NotNull BitSet mask) {
        EnumSet<T> set = EnumSet.noneOf(enumClass);
        for (T anEnum : enumClass.getEnumConstants()) {
            if (evalMask(anEnum, mask)) set.add(anEnum);
        }
        return set;
    }

    @Contract(pure = true)
    @NotNull
    public static BitSet setToMask(@NotNull EnumSet<?> enumSet) {
        BitSet mask = new BitSet();
        for (Enum<?> anEnum : enumSet) {
            mask.set(anEnum.ordinal());
        }
        return mask;
    }

    @Contract(pure = true, value = "-> new")
    @NotNull
    public static <T> Set<T> createWeakHashSet() {
        return Collections.newSetFromMap(new WeakHashMap<>());
    }

    /**
     * @param material the material to use
     * @return the correct "molten" fluid for a material
     */
    @Nullable
    public static Fluid getMoltenFluid(@NotNull Material material) {
        if (material.hasProperty(PropertyKey.ALLOY_BLAST))
            return material.getProperty(PropertyKey.FLUID).getStorage().get(FluidStorageKeys.MOLTEN);
        if (!TagPrefix.ingotHot.doGenerateItem(material) && material.hasProperty(PropertyKey.FLUID))
            return material.getProperty(PropertyKey.FLUID).getStorage().get(FluidStorageKeys.LIQUID);
        return null;
    }

    /**
     * Get fluidstack from a container.
     *
     * @param ingredient the fluidstack or fluid container item
     * @return the fluidstack in container
     */
    @Nullable
    public static FluidStack getFluidFromContainer(Object ingredient) {
        if (ingredient instanceof FluidStack) {
            return (FluidStack) ingredient;
        } else if (ingredient instanceof ItemStack itemStack) {
            IFluidTransfer fluidHandler = FluidTransferHelper.getFluidTransfer(itemStack);
            if (fluidHandler != null)
                return fluidHandler.drain(Integer.MAX_VALUE, false);
        }
        return null;
    }

    public static boolean canSeeSunClearly(Level world, BlockPos blockPos) {
        if (!world.canSeeSky(blockPos.above())) {
            return false;
        }

        Holder<Biome> biomeHolder = world.getBiome(blockPos.above());
        Biome biome = biomeHolder.value();
        if (world.isRaining()) {
            if (biome.warmEnoughToRain(blockPos.above()) || biome.coldEnoughToSnow(blockPos.above())) {
                return false;
            }
        }

        if (world.getBiome(blockPos.above()).is(BiomeTags.IS_END)) {
            return false;
        }

        ResourceLocation javdVoidBiome = new ResourceLocation(GTValues.MODID_JAVD, "void");
        if (GTCEu.isJAVDLoaded() && javdVoidBiome
                .equals(biomeHolder.unwrapKey().map(ResourceKey::location).orElse(null))) {
            return !world.isDay();
        } else return world.isDay();
    }

    public static void appendHazardTooltips(Material material, List<Component> tooltipComponents) {
        if (!ConfigHolder.INSTANCE.gameplay.hazardsEnabled || !material.hasProperty(HAZARD)) return;

        if (GTUtil.isShiftDown()) {
            tooltipComponents.add(Component.translatable("gtceu.medical_condition.description_shift"));
            tooltipComponents.add(Component
                    .translatable("gtceu.medical_condition." + material.getProperty(HAZARD).condition.name));
            tooltipComponents.add(Component.translatable("gtceu.hazard_trigger.description"));
            tooltipComponents.add(Component
                    .translatable("gtceu.hazard_trigger." + material.getProperty(HAZARD).hazardTrigger.name()));
            return;
        }
        tooltipComponents.add(Component.translatable("gtceu.medical_condition.description"));
    }

    public static CompoundTag saveItemStack(ItemStack itemStack, CompoundTag compoundTag) {
        ResourceLocation resourceLocation = BuiltInRegistries.ITEM.getKey(itemStack.getItem());
        compoundTag.putString("id", resourceLocation.toString());
        compoundTag.putInt("Count", itemStack.getCount());
        if (itemStack.getTag() != null) {
            compoundTag.put("tag", itemStack.getTag().copy());
        }

        return compoundTag;
    }

    public static ItemStack loadItemStack(CompoundTag compoundTag) {
        try {
            Item item = BuiltInRegistries.ITEM.get(new ResourceLocation(compoundTag.getString("id")));
            int count = compoundTag.getInt("Count");
            ItemStack stack = new ItemStack(item, count);
            if (compoundTag.contains("tag", Tag.TAG_COMPOUND)) {
                stack.setTag(compoundTag.getCompound("tag"));
                if (stack.getTag() != null) {
                    stack.getItem().verifyTagAfterLoad(stack.getTag());
                }
            }

            if (stack.getItem().canBeDepleted()) {
                stack.setDamageValue(stack.getDamageValue());
            }
            return stack;
        } catch (RuntimeException var2) {
            GTCEu.LOGGER.debug("Tried to load invalid item: {}", compoundTag, var2);
            return ItemStack.EMPTY;
        }
    }

    public static Tuple<ItemStack, MutableComponent> getMaintenanceText(byte flag) {
        return switch (flag) {
            case 0 -> new Tuple<>(ToolItemHelper.getToolItem(GTToolType.WRENCH),
                    Component.translatable("gtceu.top.maintenance.wrench"));
            case 1 -> new Tuple<>(ToolItemHelper.getToolItem(GTToolType.SCREWDRIVER),
                    Component.translatable("gtceu.top.maintenance.screwdriver"));
            case 2 -> new Tuple<>(ToolItemHelper.getToolItem(GTToolType.SOFT_MALLET),
                    Component.translatable("gtceu.top.maintenance.soft_mallet"));
            case 3 -> new Tuple<>(ToolItemHelper.getToolItem(GTToolType.HARD_HAMMER),
                    Component.translatable("gtceu.top.maintenance.hard_hammer"));
            case 4 -> new Tuple<>(ToolItemHelper.getToolItem(GTToolType.WIRE_CUTTER),
                    Component.translatable("gtceu.top.maintenance.wire_cutter"));
            default -> new Tuple<>(ToolItemHelper.getToolItem(GTToolType.CROWBAR),
                    Component.translatable("gtceu.top.maintenance.crowbar"));
        };
    }

    public static void addPotionTooltip(List<Pair<MobEffectInstance, Float>> effects, List<Component> list) {
        list.add(Component.translatable("gtceu.tooltip.potion.header"));
        effects.forEach(pair -> {
            var effect = pair.getFirst();
            float probability = pair.getSecond();
            list.add(Component.translatable("gtceu.tooltip.potion.each",
                    Component.translatable(effect.getDescriptionId()),
                    Component.translatable("enchantment.level." + (effect.getAmplifier() + 1)),
                    effect.getDuration(),
                    100 * probability));
        });
    }

    /**
     * Forces the initialization of a class; this includes things like loading its static fields.
     * This can be useful because a statement like {@code AClass.class} does not initialize a class.
     * <br>
     * <br>
     * Does nothing if the class is already initialized.
     *
     * @param clazz the class object to initialize.
     */
    public static void forceInitialization(Class<?> clazz) {
        try {
            Class.forName(clazz.getName(), true, clazz.getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new AssertionError(e);  // Can't happen
        }
    }
}
