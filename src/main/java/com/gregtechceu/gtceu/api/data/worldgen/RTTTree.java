package com.gregtechceu.gtceu.api.data.worldgen;

import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;

import java.util.ArrayList;
import java.util.List;

/**
 * @author KilaBash
 * @date 2023/3/21
 * @implNote RTTTree
 */
public class RTTTree {

    @Getter
    final List<BlockPos> nodes;
    final BlockPos root;

    public RTTTree(BlockPos root) {
        this.nodes = new ArrayList<>();
        nodes.add(root);
        this.root = root;
    }

    public BlockPos nearestNode(BlockPos random) {
        var minDist = random.distSqr(root);
        var nearestNode = root;
        for (BlockPos node : nodes) {
            var dist = random.distSqr(node);
            if (dist < minDist) {
                nearestNode = node;
                minDist = dist;
            }
        }
        return nearestNode;
    }

    public void addNode(BlockPos node) {
        this.nodes.add(node);
    }

}
