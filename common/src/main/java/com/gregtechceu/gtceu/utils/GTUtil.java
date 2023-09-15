package com.gregtechceu.gtceu.utils;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.mojang.blaze3d.platform.InputConstants;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.material.Fluid;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.*;
import java.util.Map.Entry;


/**
 * @author KilaBash
 * @date 2023/2/17
 * @implNote GTUtil
 */
public class GTUtil {
    private static final NavigableMap<Long, Byte> tierByVoltage = new TreeMap<>();

    static {
        for (int i = 0; i < GTValues.V.length; i++) {
            tierByVoltage.put(GTValues.V[i], (byte) i);
        }
    }

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

    public static float getExplosionPower(long voltage) {
        return getTierByVoltage(voltage) + 1;
    }

    /**
     * @return lowest tier that can handle passed voltage
     */
    public static byte getTierByVoltage(long voltage) {
        // TODO do we really need UHV+
        if (voltage > GTValues.V[GTValues.UV]) return GTValues.UV;
        return tierByVoltage.ceilingEntry(voltage).getValue();
    }

    /**
     * Ex: This method turns both 1024 and 512 into HV.
     *
     * @return the highest tier below or equal to the voltage value given
     */
    public static byte getFloorTierByVoltage(long voltage) {
        if (voltage < GTValues.V[GTValues.ULV]) return GTValues.ULV;
        return tierByVoltage.floorEntry(voltage).getValue();
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
            return InputConstants.isKeyDown(id, GLFW.GLFW_KEY_LEFT_SHIFT) || InputConstants.isKeyDown(id, GLFW.GLFW_KEY_LEFT_SHIFT);
        }
        return false;
    }

    public static boolean isCtrlDown() {
        if (LDLib.isClient()) {
            var id = Minecraft.getInstance().getWindow().getWindow();
            return InputConstants.isKeyDown(id, GLFW.GLFW_KEY_LEFT_CONTROL) || InputConstants.isKeyDown(id, GLFW.GLFW_KEY_RIGHT_CONTROL);
        }
        return false;
    }

    public static boolean isAltDown() {
        if (LDLib.isClient()) {
            var id = Minecraft.getInstance().getWindow().getWindow();
            return InputConstants.isKeyDown(id, GLFW.GLFW_KEY_LEFT_ALT) || InputConstants.isKeyDown(id, GLFW.GLFW_KEY_RIGHT_ALT);
        }
        return false;
    }

    public static boolean isFluidStackAmountDivisible(FluidStack fluidStack, int divisor) {
        return fluidStack.getAmount() % divisor == 0 && fluidStack.getAmount() % divisor != fluidStack.getAmount() && fluidStack.getAmount() / divisor != 0;
    }

    public static boolean isItemStackCountDivisible(ItemStack itemStack, int divisor) {
        return itemStack.getCount() % divisor == 0 && itemStack.getCount() % divisor != itemStack.getCount() && itemStack.getCount() / divisor != 0;
    }

    @ExpectPlatform
    public static int getItemBurnTime(Item item) {
        throw new AssertionError();
    }

    /**
     * Determines dye color nearest to specified RGB color
     */
    public static DyeColor determineDyeColor(int rgbColor) {
        Color c = new Color(rgbColor);

        Map<Double, DyeColor> distances = new HashMap<>();
        for (DyeColor dyeColor : DyeColor.values()) {
            Color c2 = new Color(dyeColor.getTextColor());

            double distance = (c.getRed() - c2.getRed()) * (c.getRed() - c2.getRed())
                    + (c.getGreen() - c2.getGreen()) * (c.getGreen() - c2.getGreen())
                    + (c.getBlue() - c2.getBlue()) * (c.getBlue() - c2.getBlue());

            distances.put(distance, dyeColor);
        }

        double min = Collections.min(distances.keySet());
        return distances.get(min);
    }

    @ExpectPlatform
    public static long getPumpBiomeModifier(Holder<Biome> biome) {
        throw new AssertionError();
    }

    /**
     * @param material the material to use
     * @return the correct "molten" fluid for a material
     */
    @Nullable
    public static Fluid getMoltenFluid(@Nonnull Material material) {
        if (material.hasProperty(PropertyKey.ALLOY_BLAST))
            return material.getProperty(PropertyKey.ALLOY_BLAST).getFluid();
        if (!TagPrefix.ingotHot.doGenerateItem(material) && material.hasProperty(PropertyKey.FLUID))
            return material.getProperty(PropertyKey.FLUID).getFluid();
        return null;
    }

}
