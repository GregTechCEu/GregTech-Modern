package com.gregtechceu.gtceu.api.pipenet;

import net.minecraft.core.Direction;

/**
 * Represents a single node in network of pipes
 * It can have blocked connections and be active or not
 */
public final class Node<NodeDataType> {

    public NodeDataType data;
    /**
     * Specifies bitmask of blocked connections
     * Node will not connect in blocked direction in any case,
     * even if neighbour node mark matches
     */
    public int openConnections;
    public boolean isActive;

    public Node(NodeDataType data, int openConnections, boolean isActive) {
        this.data = data;
        this.openConnections = openConnections;
        this.isActive = isActive;
    }

    public boolean isBlocked(Direction facing) {
        return (openConnections & 1 << facing.ordinal()) == 0;
    }
}
