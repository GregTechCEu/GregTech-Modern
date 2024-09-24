package com.gregtechceu.gtceu.data.enumproxy;

import com.gregtechceu.gtceu.data.block.GTBlocks;
import com.gregtechceu.gtceu.data.item.GTItems;

import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;

import java.util.function.Supplier;

@SuppressWarnings({ "Convert2MethodRef", "FunctionalExpressionCanBeFolded" })
public class GTEnumProxies {

    public static final EnumProxy<Boat.Type> RUBBER_BOAT_PROXY = new EnumProxy<>(Boat.Type.class,
            (Supplier<Block>) () -> GTBlocks.RUBBER_PLANK.get(), "gtceu:rubber",
            (Supplier<Item>) () -> GTItems.RUBBER_BOAT.get(),
            (Supplier<Item>) () -> GTItems.RUBBER_CHEST_BOAT.get(),
            (Supplier<Item>) () -> Items.STICK,
            false);

    public static final EnumProxy<Boat.Type> TREATED_WOOD_BOAT_PROXY = new EnumProxy<>(Boat.Type.class,
            (Supplier<Block>) () -> GTBlocks.TREATED_WOOD_PLANK.get(), "gtceu:treated_wood",
            (Supplier<Item>) () -> GTItems.TREATED_WOOD_BOAT.get(),
            (Supplier<Item>) () -> GTItems.TREATED_WOOD_CHEST_BOAT.get(),
            (Supplier<Item>) () -> Items.STICK,
            false);
}
