package com.gregtechceu.gtceu.api.data.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import java.util.List;
@Nonnull
public class Vein { // to be removed, this is useless
    public List<Block> containingBlocks;
   public Vein(List<Block> containingBlocks){
       this.containingBlocks = containingBlocks;
   }
}
