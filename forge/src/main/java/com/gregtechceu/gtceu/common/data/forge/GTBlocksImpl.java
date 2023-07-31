package com.gregtechceu.gtceu.common.data.forge;

import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateItemModelProvider;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

public class GTBlocksImpl {

    public static void rubberTreeModel(DataGenContext<Item, BlockItem> context, RegistrateItemModelProvider provider) {
        provider.generated(context, provider.modLoc("block/" + provider.name(context)));
    }

}
