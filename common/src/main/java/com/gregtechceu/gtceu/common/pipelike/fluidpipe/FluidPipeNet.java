package com.gregtechceu.gtceu.common.pipelike.fluidpipe;

import com.gregtechceu.gtceu.api.data.chemical.material.properties.FluidPipeProperties;
import com.lowdragmc.lowdraglib.pipelike.LevelPipeNet;
import com.lowdragmc.lowdraglib.pipelike.PipeNet;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluid;
import org.apache.commons.lang3.mutable.MutableLong;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * @author KilaBash
 * @date 2023/3/11
 * @implNote FluidPipeNet
 */
public class FluidPipeNet extends PipeNet<FluidPipeData> {

    private final Map<BlockPos, List<PipeNetRoutePath>> NET_DATA = new HashMap<>();

    private final Long2ObjectMap<Fluid[]> channelFluidsByBlock = new Long2ObjectOpenHashMap<>();
    private final Long2ObjectMap<ThroughputUsage[]> throughputUsagesByBlock = new Long2ObjectOpenHashMap<>();
    private long lastUpdate;

    public FluidPipeNet(LevelPipeNet<FluidPipeData, ? extends PipeNet> world) {
        super(world);
        lastUpdate = world.getWorld().getGameTime();
    }

    public List<PipeNetRoutePath> getNetData(BlockPos pipePos) {
        List<PipeNetRoutePath> data = NET_DATA.get(pipePos);
        if (data == null) {
            data = FluidPipeNetWalker.createNetData(this, pipePos);
            if (data == null) {
                // walker failed, don't cache so it tries again on next insertion
                return Collections.emptyList();
            }
            data.sort(Comparator.comparingInt(PipeNetRoutePath::getDistance));
            NET_DATA.put(pipePos, data);
        }
        return data;
    }

    @Override
    public void onNeighbourUpdate(BlockPos fromPos) {
        NET_DATA.clear();
    }

    @Override
    public void onPipeConnectionsUpdate() {
        NET_DATA.clear();
    }

    /////////////////////////////////////
    //***********    API    ***********//
    /////////////////////////////////////

    /**
     * Get the throughput of the specified block and channel, summed over the last 20 ticks.
     *
     * @return The throughput over the last second
     */
    public long getLastSecondTotalThroughput(BlockPos blockPos, int channel) {
        MutableLong totalAmount = new MutableLong(0L);

        withLastSecondUsages(blockPos.asLong(), (used) -> {
            totalAmount.add(used.channelAmounts[channel]);
        });

        return totalAmount.longValue();
    }

    /**
     * Get the channel index that is currently used for the specified block and fluid.
     * <p>
     * Note that this may be outside the pipe's max channel number, so you still need to check if the range is valid.
     *
     * @return The channel containing the fluid or -1 if the fluid is not currently being transferred.
     */
    public int getChannel(BlockPos pos, Fluid fluid) {
        updateTick();
        var channelFluids = getChannelFluids(pos.asLong());

        for (int channel = 0; channel < channelFluids.length; channel++) {
            if (channelFluids[channel] != null && channelFluids[channel].equals(fluid))
                return channel;
        }

        return -1;
    }

    /**
     * Use the existing channel for the specified fluid or assign it to the best free channel if none is used
     * for this fluid type yet.
     *
     * @return The channel to be used.
     */
    public int useChannel(BlockPos pos, Fluid fluid) {
        // Note that updateTick() is not called separately here because getChannel() already calls it.

        var channel = getChannel(pos, fluid);
        if (channel != -1)
            return channel;

        var newChannel = findBestFreeChannel(pos.asLong());
        if (newChannel == -1)
            return -1;

        getChannelFluids(pos.asLong())[newChannel] = fluid;
        return newChannel;
    }

    /**
     * Add the specified amount to the current tick's used throughput.
     */
    public void useThroughput(BlockPos pos, int channel, long amount) {
        updateTick();
        getCurrentTickUsage(pos.asLong()).channelAmounts[channel] += amount;
    }

    public Fluid getFluid(BlockPos pos, int channel) {
        updateTick();
        return getChannelFluids(pos.asLong())[channel];
    }

    //////////////////////////////////////
    //*******     Pipe Status    *******//
    //////////////////////////////////////


    private void updateTick() {
        var latestTime = getWorldData().getWorld().getGameTime();
        if (lastUpdate == latestTime)
            return;

        channelFluidsByBlock.forEach((k, v) -> Arrays.fill(v, null));

        lastUpdate = latestTime;
    }


    private static class ThroughputUsage {
        public long tick = 0L;
        public long[] channelAmounts = new long[FluidPipeProperties.MAX_PIPE_CHANNELS];

        public ThroughputUsage() {
            resetAmounts();
        }

        public ThroughputUsage(ThroughputUsage value) {
            this.tick = value.tick;
            this.channelAmounts = Arrays.copyOf(value.channelAmounts, value.channelAmounts.length);
        }

        public void resetAmounts() {
            Arrays.fill(channelAmounts, 0L);
        }
    }

    @NotNull
    private ThroughputUsage[] getThroughputUsages(long blockPos) {
        return throughputUsagesByBlock.computeIfAbsent(blockPos, bp ->
                Stream.generate(ThroughputUsage::new).limit(20).toArray(ThroughputUsage[]::new)
        );
    }

    private ThroughputUsage getCurrentTickUsage(long blockPos) {
        var currentTick = getLevel().getGameTime();
        var lastSecondThroughputs = getThroughputUsages(blockPos);
        var currentThroughput = lastSecondThroughputs[(int) (currentTick % 20)];

        if (currentThroughput.tick != currentTick) {
            currentThroughput.tick = currentTick;
            currentThroughput.resetAmounts();
        }

        return currentThroughput;
    }

    private void withLastSecondUsages(long blockPos, Consumer<ThroughputUsage> consumer) {
        var minTick = getLevel().getGameTime() - 19;
        var lastSecondThroughputs = getThroughputUsages(blockPos);

        for (int i = 0; i < 20; i++) {
            ThroughputUsage throughputUsage = lastSecondThroughputs[i];
            if (throughputUsage.tick < minTick)
                continue;

            consumer.accept(throughputUsage);
        }
    }

    private Fluid[] getChannelFluids(long blockPos) {
        return channelFluidsByBlock.computeIfAbsent(blockPos, bp -> new Fluid[FluidPipeProperties.MAX_PIPE_CHANNELS]);
    }

    private long[] getLastSecondTotalUsagePerChannel(long blockPos) {
        var totalAmounts = new long[FluidPipeProperties.MAX_PIPE_CHANNELS];

        withLastSecondUsages(blockPos, (used) -> {
            for (int channel = 0; channel < FluidPipeProperties.MAX_PIPE_CHANNELS; channel++) {
                totalAmounts[channel] += used.channelAmounts[channel];
            }
        });

        return totalAmounts;
    }

    private int findBestFreeChannel(long pos) {
        Fluid[] channelFluids = getChannelFluids(pos);
        long[] lastSecondUsagePerChannel = getLastSecondTotalUsagePerChannel(pos);

        long leastUsedAmount = Long.MAX_VALUE;
        int bestChannel = -1;

        for (int channel = 0; channel < channelFluids.length; channel++) {
            if (channelFluids[channel] != null)
                continue;

            if (lastSecondUsagePerChannel[channel] < leastUsedAmount) {
                leastUsedAmount = lastSecondUsagePerChannel[channel];
                bestChannel = channel;
            }
        }

        return bestChannel;
    }

    /////////////////////////////////////
    //***********    NBT    ***********//
    /////////////////////////////////////

    @Override
    protected void writeNodeData(FluidPipeData nodeData, CompoundTag tagCompound) {
        tagCompound.putInt("max_temperature", nodeData.properties.getMaxFluidTemperature());
        tagCompound.putLong("throughput", nodeData.properties.getThroughput());
        tagCompound.putBoolean("gas_proof", nodeData.properties.isGasProof());
        tagCompound.putBoolean("acid_proof", nodeData.properties.isAcidProof());
        tagCompound.putBoolean("cryo_proof", nodeData.properties.isCryoProof());
        tagCompound.putBoolean("plasma_proof", nodeData.properties.isPlasmaProof());
        tagCompound.putInt("channels", nodeData.properties.getChannels());
        tagCompound.putByte("connections", nodeData.connections());
    }

    @Override
    protected FluidPipeData readNodeData(CompoundTag tagCompound) {
        int maxTemperature = tagCompound.getInt("max_temperature");
        long throughput = tagCompound.getLong("throughput");
        boolean gasProof = tagCompound.getBoolean("gas_proof");
        boolean acidProof = tagCompound.getBoolean("acid_proof");
        boolean cryoProof = tagCompound.getBoolean("cryo_proof");
        boolean plasmaProof = tagCompound.getBoolean("plasma_proof");
        int channels = tagCompound.getInt("channels");
        return new FluidPipeData(new FluidPipeProperties(maxTemperature, throughput, gasProof, acidProof, cryoProof, plasmaProof, channels), tagCompound.getByte("connections"));
    }


    public record Snapshot(Long2ObjectMap<Fluid[]> channelFluids, Long2ObjectMap<ThroughputUsage[]> throughputUsage) {
    }

    public Snapshot createSnapshot() {
        Long2ObjectMap<Fluid[]> channelUsedCopied = new Long2ObjectOpenHashMap<>();
        channelFluidsByBlock.forEach((k, v) -> channelUsedCopied.put(k.longValue(), Arrays.copyOf(v, v.length)));

        Long2ObjectOpenHashMap<ThroughputUsage[]> throughputUsedCopied = new Long2ObjectOpenHashMap<>();
        throughputUsagesByBlock.forEach((k, v) -> throughputUsedCopied.put(k.longValue(), Arrays.stream(v)
                .map(ThroughputUsage::new)
                .toArray(ThroughputUsage[]::new)
        ));

        return new Snapshot(channelUsedCopied, throughputUsedCopied);
    }

    public void resetData(Snapshot snapshot) {
        channelFluidsByBlock.clear();
        channelFluidsByBlock.putAll(snapshot.channelFluids);

        throughputUsagesByBlock.clear();
        throughputUsagesByBlock.putAll(snapshot.throughputUsage);
    }
}
