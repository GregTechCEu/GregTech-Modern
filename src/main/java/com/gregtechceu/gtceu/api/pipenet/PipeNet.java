package com.gregtechceu.gtceu.api.pipenet;

import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import lombok.Getter;

import java.util.*;

public abstract class PipeNet<NodeDataType> {

    @Getter
    protected final LevelPipeNet<NodeDataType, PipeNet<NodeDataType>> worldData;
    private final Map<BlockPos, Node<NodeDataType>> nodeByBlockPos = new HashMap<>();
    private final Map<BlockPos, Node<NodeDataType>> unmodifiableNodeByBlockPos = Collections
            .unmodifiableMap(nodeByBlockPos);
    private final Object2IntOpenHashMap<ChunkPos> ownedChunks = new Object2IntOpenHashMap<>();
    @Getter
    private long lastUpdate;
    @Getter
    boolean isValid = false;

    public PipeNet(LevelPipeNet<NodeDataType, ? extends PipeNet<NodeDataType>> Level) {
        // noinspection unchecked
        this.worldData = (LevelPipeNet<NodeDataType, PipeNet<NodeDataType>>) Level;
    }

    public Set<ChunkPos> getContainedChunks() {
        return Collections.unmodifiableSet(ownedChunks.keySet());
    }

    public ServerLevel getLevel() {
        return worldData.getWorld();
    }

    /**
     * Is only called when connections changed of nodes. Nodes can ONLY connect to other nodes.
     */
    protected void onNodeConnectionsUpdate() {
        this.lastUpdate = System.currentTimeMillis();
    }

    /**
     * Is only called when Data changed of nodes.
     */
    protected void onNodeDataUpdate() {}

    /**
     * Is called when any connection of any pipe in the net changes
     */
    public void onPipeConnectionsUpdate() {}

    public void onNeighbourUpdate(BlockPos fromPos) {}

    public Map<BlockPos, Node<NodeDataType>> getAllNodes() {
        return unmodifiableNodeByBlockPos;
    }

    public Node<NodeDataType> getNodeAt(BlockPos blockPos) {
        return nodeByBlockPos.get(blockPos);
    }

    public boolean containsNode(BlockPos blockPos) {
        return nodeByBlockPos.containsKey(blockPos);
    }

    public boolean isNodeConnectedTo(BlockPos pos, Direction side) {
        var nodeFirst = getNodeAt(pos);
        if (nodeFirst == null) return false;
        var nodeSecond = getNodeAt(pos.relative(side));
        if (nodeSecond == null) return false;
        return canNodesConnect(nodeFirst, side, nodeSecond, this);
    }

    protected void addNodeSilently(BlockPos nodePos, Node<NodeDataType> node) {
        this.nodeByBlockPos.put(nodePos, node);
        checkAddedInChunk(nodePos);
    }

    protected void addNode(BlockPos nodePos, Node<NodeDataType> node) {
        addNodeSilently(nodePos, node);
        onNodeConnectionsUpdate();
        worldData.setDirty();
    }

    protected Node<NodeDataType> removeNodeWithoutRebuilding(BlockPos nodePos) {
        Node<NodeDataType> removedNode = this.nodeByBlockPos.remove(nodePos);
        ensureRemovedFromChunk(nodePos);
        worldData.setDirty();
        return removedNode;
    }

    public void removeNode(BlockPos nodePos) {
        if (nodeByBlockPos.containsKey(nodePos)) {
            Node<NodeDataType> selfNode = removeNodeWithoutRebuilding(nodePos);
            rebuildNetworkOnNodeRemoval(nodePos, selfNode);
        }
    }

    protected void checkAddedInChunk(BlockPos nodePos) {
        ChunkPos chunkPos = new ChunkPos(nodePos);
        int oldValue = this.ownedChunks.addTo(chunkPos, 1);
        if (oldValue == 0 && isValid()) {
            this.worldData.addPipeNetToChunk(chunkPos, this);
        }
    }

    protected void ensureRemovedFromChunk(BlockPos nodePos) {
        ChunkPos chunkPos = new ChunkPos(nodePos);
        int oldValue = this.ownedChunks.containsKey(chunkPos) ? ownedChunks.addTo(chunkPos, -1) : 0;
        if (oldValue == 1) {
            this.ownedChunks.removeInt(chunkPos);
            if (isValid()) {
                this.worldData.removePipeNetFromChunk(chunkPos, this);
            }
        }
    }

    public void updateBlockedConnections(BlockPos nodePos, Direction facing, boolean isBlocked) {
        if (!containsNode(nodePos)) {
            return;
        }
        Node<NodeDataType> selfNode = getNodeAt(nodePos);
        if (selfNode.isBlocked(facing) == isBlocked) {
            return;
        }

        setBlocked(selfNode, facing, isBlocked);
        BlockPos offsetPos = nodePos.relative(facing);
        PipeNet<NodeDataType> pipeNetAtOffset = worldData.getNetFromPos(offsetPos);
        if (pipeNetAtOffset == null) {
            return;
        }
        // if we are on that side of node too
        // and it is blocked now
        if (pipeNetAtOffset == this) {
            // if side was unblocked, well, there is really nothing changed in this e-net
            // if it is blocked now, but was able to connect with neighbour node before, try split networks
            if (isBlocked) {
                // need to unblock node before doing canNodesConnectCheck
                setBlocked(selfNode, facing, false);
                if (canNodesConnect(selfNode, facing, getNodeAt(offsetPos), this)) {
                    // now block again to call findAllConnectedBlocks
                    setBlocked(selfNode, facing, true);
                    HashMap<BlockPos, Node<NodeDataType>> thisENet = findAllConnectedBlocks(nodePos);
                    if (!getAllNodes().equals(thisENet)) {
                        // node visibility has changed, split network into 2
                        // node that code below is similar to removeNodeInternal, but only for 2 networks, and without
                        // node removal
                        PipeNet<NodeDataType> newPipeNet = worldData.createNetInstance();
                        thisENet.keySet().forEach(this::removeNodeWithoutRebuilding);
                        newPipeNet.transferNodeData(thisENet, this);
                        worldData.addPipeNet(newPipeNet);
                    }
                }
            }
            // there is another network on that side
            // if this is an unblock, and we can connect with their node, merge them

        } else if (!isBlocked) {
            Node<NodeDataType> neighbourNode = pipeNetAtOffset.getNodeAt(offsetPos);
            // check connection availability from both networks
            if (canNodesConnect(selfNode, facing, neighbourNode, pipeNetAtOffset) &&
                    pipeNetAtOffset.canNodesConnect(neighbourNode, facing.getOpposite(), selfNode, this)) {
                // so, side is unblocked now, and nodes can connect, merge two networks
                // our network consumes other one
                uniteNetworks(pipeNetAtOffset);
            }
        }
        onNodeConnectionsUpdate();
        worldData.setDirty();
    }

    public void updateNodeData(BlockPos nodePos, NodeDataType data) {
        if (containsNode(nodePos)) {
            Node<NodeDataType> selfNode = getNodeAt(nodePos);
            selfNode.data = data;
            onNodeDataUpdate();
            worldData.setDirty();
        }
    }

    private void setBlocked(Node<NodeDataType> selfNode, Direction facing, boolean isBlocked) {
        if (!isBlocked) {
            selfNode.openConnections |= 1 << facing.ordinal();
        } else {
            selfNode.openConnections &= ~(1 << facing.ordinal());
        }
    }

    public boolean markNodeAsActive(BlockPos nodePos, boolean isActive) {
        if (containsNode(nodePos) && getNodeAt(nodePos).isActive != isActive) {
            getNodeAt(nodePos).isActive = isActive;
            worldData.setDirty();
            onNodeConnectionsUpdate();
            return true;
        }
        return false;
    }

    protected final void uniteNetworks(PipeNet<NodeDataType> unitedPipeNet) {
        Map<BlockPos, Node<NodeDataType>> allNodes = new HashMap<>(unitedPipeNet.getAllNodes());
        worldData.removePipeNet(unitedPipeNet);
        allNodes.keySet().forEach(unitedPipeNet::removeNodeWithoutRebuilding);
        transferNodeData(allNodes, unitedPipeNet);
    }

    private boolean areNodeBlockedConnectionsCompatible(Node<NodeDataType> first, Direction firstFacing,
                                                        Node<NodeDataType> second) {
        return !first.isBlocked(firstFacing) && !second.isBlocked(firstFacing.getOpposite());
    }

    private boolean areMarksCompatible(int mark1, int mark2) {
        return mark1 == mark2 || mark1 == Node.DEFAULT_MARK || mark2 == Node.DEFAULT_MARK;
    }

    /**
     * Checks if given nodes can connect
     * Note that this logic should equal with block connection logic
     * for proper work of network
     */
    protected final boolean canNodesConnect(Node<NodeDataType> first, Direction firstFacing, Node<NodeDataType> second,
                                            PipeNet<NodeDataType> secondPipeNet) {
        return areNodeBlockedConnectionsCompatible(first, firstFacing, second) &&
                areMarksCompatible(first.mark, second.mark) &&
                areNodesCustomContactable(first.data, second.data, secondPipeNet);
    }

    // we need to search only this network
    protected HashMap<BlockPos, Node<NodeDataType>> findAllConnectedBlocks(BlockPos startPos) {
        HashMap<BlockPos, Node<NodeDataType>> observedSet = new HashMap<>();
        observedSet.put(startPos, getNodeAt(startPos));
        Node<NodeDataType> firstNode = getNodeAt(startPos);
        BlockPos.MutableBlockPos currentPos = startPos.mutable();
        Deque<Direction> moveStack = new ArrayDeque<>();
        main:
        while (true) {
            for (Direction facing : GTUtil.DIRECTIONS) {
                currentPos.move(facing);
                Node<NodeDataType> secondNode = getNodeAt(currentPos);
                // if there is node, and it can connect with previous node, add it to list, and set previous node as
                // current
                if (secondNode != null && canNodesConnect(firstNode, facing, secondNode, this) &&
                        !observedSet.containsKey(currentPos)) {
                    observedSet.put(currentPos.immutable(), getNodeAt(currentPos));
                    firstNode = secondNode;
                    moveStack.push(facing.getOpposite());
                    continue main;
                } else currentPos.move(facing.getOpposite());
            }
            if (!moveStack.isEmpty()) {
                currentPos.move(moveStack.pop());
                firstNode = getNodeAt(currentPos);
            } else break;
        }
        return observedSet;
    }

    // called when node is removed to rebuild network
    protected void rebuildNetworkOnNodeRemoval(BlockPos nodePos, Node<NodeDataType> selfNode) {
        int amountOfConnectedSides = 0;
        for (Direction facing : GTUtil.DIRECTIONS) {
            BlockPos offsetPos = nodePos.relative(facing);
            if (containsNode(offsetPos))
                amountOfConnectedSides++;
        }
        // if we are connected only on one side or not connected at all, we don't need to find connected blocks
        // because they are only on on side or doesn't exist at all
        // this saves a lot of performance in big networks, which are quite big to depth-first them fastly
        if (amountOfConnectedSides >= 2) {
            for (Direction facing : GTUtil.DIRECTIONS) {
                BlockPos offsetPos = nodePos.relative(facing);
                Node<NodeDataType> secondNode = getNodeAt(offsetPos);
                if (secondNode == null || !canNodesConnect(selfNode, facing, secondNode, this)) {
                    // if there isn't any neighbour node, or it wasn't connected with us, just skip it
                    continue;
                }
                HashMap<BlockPos, Node<NodeDataType>> thisENet = findAllConnectedBlocks(offsetPos);
                if (getAllNodes().equals(thisENet)) {
                    // if cable on some direction contains all nodes of this network
                    // the network didn't change so keep it as is
                    break;
                } else {
                    // and use them to create new network with caching active nodes set
                    PipeNet<NodeDataType> energyNet = worldData.createNetInstance();
                    // remove blocks that aren't connected with this network
                    thisENet.keySet().forEach(this::removeNodeWithoutRebuilding);
                    energyNet.transferNodeData(thisENet, this);
                    worldData.addPipeNet(energyNet);
                }
            }
        }
        if (getAllNodes().isEmpty()) {
            // if this energy net is empty now, remove it
            worldData.removePipeNet(this);
        }
        onNodeConnectionsUpdate();
        worldData.setDirty();
    }

    protected boolean areNodesCustomContactable(NodeDataType first, NodeDataType second,
                                                PipeNet<NodeDataType> secondNodePipeNet) {
        return true;
    }

    protected boolean canAttachNode(NodeDataType nodeData) {
        return true;
    }

    /**
     * Called during network split when one net needs to transfer some of it's nodes to another one
     * Use this for diving old net contents according to node amount of new network
     * For example, for fluid pipes it would remove amount of fluid contained in old nodes
     * from parent network and add it to it's own tank, keeping network contents when old network is split
     * Note that it should be called when parent net doesn't have transferredNodes in allNodes already
     */
    protected void transferNodeData(Map<BlockPos, Node<NodeDataType>> transferredNodes,
                                    PipeNet<NodeDataType> parentNet) {
        transferredNodes.forEach(this::addNodeSilently);
        onNodeConnectionsUpdate();
        worldData.setDirty();
    }
}
