package com.gregtechceu.gtceu.common.blockentity;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.PipeBlockEntity;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.capability.forge.GTCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.FluidPipeProperties;
import com.gregtechceu.gtceu.api.fluids.FluidConstants;
import com.gregtechceu.gtceu.api.fluids.FluidState;
import com.gregtechceu.gtceu.api.fluids.GTFluid;
import com.gregtechceu.gtceu.api.fluids.attribute.FluidAttribute;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.feature.IDataInfoProvider;
import com.gregtechceu.gtceu.api.misc.IOFluidHandlerList;
import com.gregtechceu.gtceu.api.transfer.fluid.CustomFluidTank;
import com.gregtechceu.gtceu.api.transfer.fluid.IFluidHandlerModifiable;
import com.gregtechceu.gtceu.common.cover.FluidFilterCover;
import com.gregtechceu.gtceu.common.cover.PumpCover;
import com.gregtechceu.gtceu.common.cover.data.ManualIOMode;
import com.gregtechceu.gtceu.common.item.PortableScannerBehavior;
import com.gregtechceu.gtceu.common.pipelike.fluidpipe.FluidPipeType;
import com.gregtechceu.gtceu.common.pipelike.fluidpipe.PipeTankList;
import com.gregtechceu.gtceu.utils.EntityDamageUtil;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTTransferUtils;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.function.Predicate;

public class FluidPipeBlockEntity extends PipeBlockEntity<FluidPipeType, FluidPipeProperties>
                                  implements IDataInfoProvider {

    public static final int FREQUENCY = 5;

    public byte lastReceivedFrom = 0, oldLastReceivedFrom = 0;
    private PipeTankList pipeTankList;
    private final EnumMap<Direction, PipeTankList> tankLists = new EnumMap<>(Direction.class);
    private CustomFluidTank[] fluidTanks;
    private long timer = 0L;
    private final int offset = GTValues.RNG.nextInt(20);

    private TickableSubscription updateSubs;

    public FluidPipeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    public static void onBlockEntityRegister(BlockEntityType<FluidPipeBlockEntity> fluidPipeBlockEntityBlockEntityType) {}

    public long getOffsetTimer() {
        return timer + offset;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (updateSubs == null) {
            updateSubs = this.subscribeServerTick(this::update);
        }
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        if (updateSubs != null) {
            this.unsubscribe(updateSubs);
            updateSubs = null;
        }
    }

    @Override
    public boolean canAttachTo(Direction side) {
        if (level != null) {
            if (level.getBlockEntity(getBlockPos().relative(side)) instanceof FluidPipeBlockEntity) {
                return false;
            }
            return GTTransferUtils.hasAdjacentFluidHandler(level, getBlockPos(), side);
        }
        return false;
    }

    private Predicate<FluidStack> getFluidCapFilter(@Nullable Direction side, IO io) {
        if (side != null) {
            var cover = getCoverContainer().getCoverAtSide(side);
            if (cover instanceof FluidFilterCover filterCover && filterCover.getFilterMode().filters(io)) {
                return filterCover.getFluidFilter();
            }
        }
        return fluid -> true;
    }

    @NotNull
    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing) {
        if (capability == ForgeCapabilities.FLUID_HANDLER) {
            if (facing != null && isConnected(facing)) {
                PipeTankList tankList = getTankList(facing);
                if (tankList == null)
                    return LazyOptional.empty();

                IOFluidHandlerList list = new IOFluidHandlerList(List.of(tankList), IO.BOTH,
                        getFluidCapFilter(facing, IO.IN), getFluidCapFilter(facing, IO.OUT));
                return ForgeCapabilities.FLUID_HANDLER.orEmpty(capability,
                        LazyOptional.of(() -> list));
            }
        } else if (capability == GTCapability.CAPABILITY_COVERABLE) {
            return GTCapability.CAPABILITY_COVERABLE.orEmpty(capability, LazyOptional.of(this::getCoverContainer));
        } else if (capability == GTCapability.CAPABILITY_TOOLABLE) {
            return GTCapability.CAPABILITY_TOOLABLE.orEmpty(capability, LazyOptional.of(() -> this));
        }
        return super.getCapability(capability, facing);
    }

    public int getCapacityPerTank() {
        return getNodeData().getThroughput() * 20;
    }

    public void update() {
        timer++;
        if (!level.isClientSide && getOffsetTimer() % FREQUENCY == 0) {
            lastReceivedFrom &= 63;
            if (lastReceivedFrom == 63) {
                lastReceivedFrom = 0;
            }

            boolean shouldDistribute = (oldLastReceivedFrom == lastReceivedFrom);
            int tanks = getNodeData().getChannels();
            for (int i = 0, j = GTValues.RNG.nextInt(tanks); i < tanks; i++) {
                int index = (i + j) % tanks;
                CustomFluidTank tank = getFluidTanks()[index];
                FluidStack fluid = tank.getFluid();
                if (fluid.isEmpty() || fluid.getFluid() == Fluids.EMPTY)
                    continue;
                if (fluid.getAmount() <= 0) {
                    tank.setFluid(FluidStack.EMPTY);
                    continue;
                }

                if (shouldDistribute) {
                    distributeFluid(index, tank, fluid);
                    lastReceivedFrom = 0;
                }
            }
            oldLastReceivedFrom = lastReceivedFrom;
        }
    }

    private void distributeFluid(int channel, CustomFluidTank tank, FluidStack fluid) {
        // Tank, From, Amount to receive
        List<FluidTransaction> tanks = new ArrayList<>();
        int amount = fluid.getAmount();

        FluidStack maxFluid = fluid.copy();
        double availableCapacity = 0;

        for (byte i = 0, j = (byte) GTValues.RNG.nextInt(6); i < 6; i++) {
            // Get a list of tanks accepting fluids, and what side they're on
            byte side = (byte) ((i + j) % 6);
            Direction facing = GTUtil.DIRECTIONS[side];

            if (!isConnected(facing) || (lastReceivedFrom & (1 << side)) != 0) {
                continue;
            }

            BlockEntity neighbor = getNeighbor(facing);
            if (neighbor == null) continue;
            IFluidHandler fluidHandler = neighbor.getCapability(ForgeCapabilities.FLUID_HANDLER, facing.getOpposite())
                    .resolve().orElse(null);
            if (fluidHandler == null) continue;

            IFluidHandlerModifiable pipeTank = tank;
            CoverBehavior cover = getCoverContainer().getCoverAtSide(facing);

            // pipeTank should only be determined by the cover attached to the actual pipe
            if (cover != null) {
                pipeTank = cover.getFluidHandlerCap(pipeTank);
                // Shutter covers return null capability when active, so check here to prevent NPE
                if (pipeTank == null || checkForPumpCover(cover)) continue;
            } else {
                ICoverable coverable = neighbor.getCapability(GTCapability.CAPABILITY_COVERABLE, facing.getOpposite())
                        .resolve().orElse(null);
                if (coverable != null) {
                    cover = coverable.getCoverAtSide(facing.getOpposite());
                    if (checkForPumpCover(cover)) continue;
                }
            }

            FluidStack drainable = pipeTank.drain(maxFluid, IFluidHandler.FluidAction.SIMULATE);
            if (drainable.isEmpty() || drainable.getAmount() <= 0) {
                continue;
            }

            int filled = Math.min(fluidHandler.fill(maxFluid, IFluidHandler.FluidAction.SIMULATE),
                    drainable.getAmount());

            if (filled > 0) {
                tanks.add(new FluidTransaction(fluidHandler, pipeTank, filled));
                availableCapacity += filled;
            }
            maxFluid.setAmount(amount); // Because some mods do actually modify input fluid stack
        }

        if (availableCapacity <= 0)
            return;

        // How much of this fluid is available for distribution?
        final double maxAmount = Math.min(getCapacityPerTank() / 2, fluid.getAmount());

        // Now distribute
        for (FluidTransaction transaction : tanks) {
            if (availableCapacity > maxAmount) {
                // Distribute fluids based on percentage available space at destination
                transaction.amount = Mth.floor(transaction.amount * maxAmount / availableCapacity);
            }
            if (transaction.amount == 0) {
                if (tank.getFluidAmount() <= 0) break; // If there is no more stored fluid, stop transferring to prevent
                // dupes
                transaction.amount = 1; // If the percent is not enough to give at least 1L, try to give 1L
            } else if (transaction.amount < 0) {
                continue;
            }

            FluidStack toInsert = fluid.copy();
            if (toInsert.isEmpty() || toInsert.getFluid() == Fluids.EMPTY) continue;
            toInsert.setAmount(transaction.amount);

            int inserted = transaction.target.fill(toInsert, IFluidHandler.FluidAction.EXECUTE);
            if (inserted > 0) {
                transaction.pipeTank.drain(inserted, IFluidHandler.FluidAction.EXECUTE);
            }
        }
    }

    private boolean checkForPumpCover(@Nullable CoverBehavior cover) {
        if (cover instanceof PumpCover coverPump) {
            int pipeThroughput = getNodeData().getThroughput() * 20;
            if (coverPump.getTransferRate() > pipeThroughput) {
                coverPump.setTransferRate(pipeThroughput);
            }
            return coverPump.getManualIOMode() == ManualIOMode.DISABLED;
        }
        return false;
    }

    public void checkAndDestroy(@NotNull FluidStack stack) {
        Fluid fluid = stack.getFluid();
        FluidPipeProperties prop = getNodeData();

        boolean burning = prop.getMaxFluidTemperature() < fluid.getFluidType().getTemperature(stack);
        boolean leaking = !prop.isGasProof() && fluid.getFluidType().getDensity(stack) < 0;
        boolean shattering = !prop.isCryoProof() &&
                fluid.getFluidType().getTemperature(stack) < FluidConstants.CRYOGENIC_FLUID_THRESHOLD;
        boolean corroding = false;
        boolean melting = false;

        if (fluid instanceof GTFluid attributedFluid) {
            FluidState state = attributedFluid.getState();
            if (!prop.canContain(state)) {
                leaking = state == FluidState.GAS;
                melting = state == FluidState.PLASMA;
            }

            // carrying plasmas which are too hot when plasma proof does not burn pipes
            if (burning && state == FluidState.PLASMA && prop.canContain(FluidState.PLASMA)) {
                burning = false;
            }

            for (FluidAttribute attribute : attributedFluid.getAttributes()) {
                if (!prop.canContain(attribute)) {
                    // corrodes if the pipe can't handle the attribute, even if it's not an acid
                    corroding = true;
                }
            }
        }

        if (burning || leaking || corroding || shattering || melting) {
            destroyPipe(stack, burning, leaking, corroding, shattering, melting);
        }
    }

    public void destroyPipe(FluidStack stack, boolean isBurning, boolean isLeaking, boolean isCorroding,
                            boolean isShattering, boolean isMelting) {
        // prevent the sound from spamming when filled from anything not a pipe
        if (getOffsetTimer() % 10 == 0) {
            level.playSound(null, this.getPipePos(), SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 1.0F);
        }

        if (isLeaking) {
            FluidPipeBlockEntity.spawnParticles(level, worldPosition, Direction.UP, ParticleTypes.SMOKE,
                    7 + GTValues.RNG.nextInt(2));

            // voids 10%
            stack.setAmount(Math.max(0, stack.getAmount() * 9 / 10));

            // apply heat damage in area surrounding the pipe
            if (getOffsetTimer() % 20 == 0) {
                List<LivingEntity> entities = getPipeLevel().getEntitiesOfClass(LivingEntity.class,
                        new AABB(getPipePos()).inflate(2));
                for (LivingEntity entityLivingBase : entities) {
                    EntityDamageUtil.applyTemperatureDamage(entityLivingBase,
                            stack.getFluid().getFluidType().getTemperature(stack),
                            2.0F, 10);
                }
            }

            // chance to do a small explosion
            if (GTValues.RNG.nextInt(isBurning ? 3 : 7) == 0) {
                this.doExplosion(1.0f + GTValues.RNG.nextFloat());
            }
        }

        if (isCorroding) {
            FluidPipeBlockEntity.spawnParticles(getPipeLevel(), getPipePos(), Direction.UP, ParticleTypes.CRIT,
                    3 + GTValues.RNG.nextInt(2));

            // voids 25%
            stack.setAmount(Math.max(0, stack.getAmount() * 3 / 4));

            // apply chemical damage in area surrounding the pipe
            if (getOffsetTimer() % 20 == 0) {
                List<LivingEntity> entities = getPipeLevel().getEntitiesOfClass(LivingEntity.class,
                        new AABB(getPipePos()).inflate(1));
                for (LivingEntity entityLivingBase : entities) {
                    EntityDamageUtil.applyChemicalDamage(entityLivingBase, 2);
                }
            }

            // 1/10 chance to void everything and destroy the pipe
            if (GTValues.RNG.nextInt(10) == 0) {
                stack.setAmount(0);
                level.removeBlock(getPipePos(), false);
            }
        }

        if (isBurning || isMelting) {
            FluidPipeBlockEntity.spawnParticles(level, getBlockPos(), Direction.UP, ParticleTypes.FLAME,
                    (isMelting ? 7 : 3) + GTValues.RNG.nextInt(2));

            // voids 75%
            stack.setAmount(Math.max(0, stack.getAmount() / 4));

            // 1/4 chance to burn everything around it
            if (GTValues.RNG.nextInt(4) == 0) {
                FluidPipeBlockEntity.setNeighboursToFire(level, getBlockPos());
            }

            // apply heat damage in area surrounding the pipe
            if (isMelting && getOffsetTimer() % 20 == 0) {
                List<LivingEntity> entities = getPipeLevel().getEntitiesOfClass(LivingEntity.class,
                        new AABB(getPipePos()).inflate(2));
                for (LivingEntity entityLivingBase : entities) {
                    EntityDamageUtil.applyTemperatureDamage(entityLivingBase,
                            stack.getFluid().getFluidType().getTemperature(stack),
                            2.0F, 10);
                }
            }

            // 1/10 chance to void everything and burn the pipe
            if (GTValues.RNG.nextInt(10) == 0) {
                stack.setAmount(0);
                level.setBlockAndUpdate(getBlockPos(), Blocks.FIRE.defaultBlockState());
            }
        }

        if (isShattering) {
            FluidPipeBlockEntity.spawnParticles(level, getBlockPos(), Direction.UP, ParticleTypes.CLOUD,
                    3 + GTValues.RNG.nextInt(2));

            // voids 75%
            stack.setAmount(Math.max(0, stack.getAmount() / 4));

            // apply frost damage in area surrounding the pipe
            if (getOffsetTimer() % 20 == 0) {
                List<LivingEntity> entities = getPipeLevel().getEntitiesOfClass(LivingEntity.class,
                        new AABB(getPipePos()).inflate(2));
                for (LivingEntity entityLivingBase : entities) {
                    EntityDamageUtil.applyTemperatureDamage(entityLivingBase,
                            stack.getFluid().getFluidType().getTemperature(stack),
                            2.0F, 10);
                }
            }

            // 1/10 chance to void everything and freeze the pipe
            if (GTValues.RNG.nextInt(10) == 0) {
                stack.setAmount(0);
                level.removeBlock(getBlockPos(), false);
            }
        }
    }

    public void receivedFrom(Direction facing) {
        if (facing != null) {
            lastReceivedFrom |= (1 << facing.ordinal());
        }
    }

    public FluidStack getContainedFluid(int channel) {
        if (channel < 0 || channel >= getFluidTanks().length) return null;
        return getFluidTanks()[channel].getFluid();
    }

    private void createTanksList() {
        fluidTanks = new CustomFluidTank[getNodeData().getChannels()];
        for (int i = 0; i < getNodeData().getChannels(); i++) {
            fluidTanks[i] = new CustomFluidTank(getCapacityPerTank());
        }
        pipeTankList = new PipeTankList(this, null, fluidTanks);
        for (Direction facing : GTUtil.DIRECTIONS) {
            tankLists.put(facing, new PipeTankList(this, facing, fluidTanks));
        }
    }

    public PipeTankList getTankList() {
        if (pipeTankList == null || fluidTanks == null) {
            createTanksList();
        }
        return pipeTankList;
    }

    public PipeTankList getTankList(Direction facing) {
        if (tankLists.isEmpty() || fluidTanks == null) {
            createTanksList();
        }
        return tankLists.getOrDefault(facing, pipeTankList);
    }

    public CustomFluidTank[] getFluidTanks() {
        if (pipeTankList == null || fluidTanks == null) {
            createTanksList();
        }
        return fluidTanks;
    }

    public FluidStack[] getContainedFluids() {
        FluidStack[] fluids = new FluidStack[getFluidTanks().length];
        for (int i = 0; i < fluids.length; i++) {
            fluids[i] = fluidTanks[i].getFluid();
        }
        return fluids;
    }

    @Override
    public void saveCustomPersistedData(CompoundTag tag, boolean forDrop) {
        super.saveCustomPersistedData(tag, forDrop);
        ListTag list = new ListTag();
        for (int i = 0; i < getFluidTanks().length; i++) {
            FluidStack stack1 = getContainedFluid(i);
            CompoundTag fluidTag = new CompoundTag();
            if (stack1 == null || stack1.getAmount() <= 0)
                fluidTag.putBoolean("isNull", true);
            else
                stack1.writeToNBT(fluidTag);
            list.add(fluidTag);
        }
        tag.put("Fluids", list);
    }

    @Override
    public void loadCustomPersistedData(CompoundTag nbt) {
        super.loadCustomPersistedData(nbt);
        ListTag list = nbt.getList("Fluids", Tag.TAG_COMPOUND);
        createTanksList();
        for (int i = 0; i < list.size(); i++) {
            CompoundTag tag = list.getCompound(i);
            if (!tag.getBoolean("isNull")) {
                fluidTanks[i].setFluid(FluidStack.loadFluidStackFromNBT(tag));
            }
        }
    }

    public static void spawnParticles(Level worldIn, BlockPos pos, Direction direction, ParticleOptions particleType,
                                      int particleCount) {
        if (worldIn instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(particleType,
                    pos.getX() + 0.5,
                    pos.getY() + 0.5,
                    pos.getZ() + 0.5,
                    particleCount,
                    direction.getStepX() * 0.2 + GTValues.RNG.nextDouble() * 0.1,
                    direction.getStepY() * 0.2 + GTValues.RNG.nextDouble() * 0.1,
                    direction.getStepZ() * 0.2 + GTValues.RNG.nextDouble() * 0.1,
                    0.1);
        }
    }

    public static void setNeighboursToFire(Level world, BlockPos selfPos) {
        for (Direction side : GTUtil.DIRECTIONS) {
            if (!GTValues.RNG.nextBoolean()) continue;
            BlockPos blockPos = selfPos.relative(side);
            BlockState blockState = world.getBlockState(blockPos);
            if (world.isEmptyBlock(blockPos) ||
                    blockState.isFlammable(world, blockPos, side.getOpposite())) {
                world.setBlockAndUpdate(blockPos, Blocks.FIRE.defaultBlockState());
            }
        }
    }

    @Override
    public @NotNull List<Component> getDataInfo(PortableScannerBehavior.DisplayMode mode) {
        List<Component> list = new ArrayList<>();

        if (mode == PortableScannerBehavior.DisplayMode.SHOW_ALL ||
                mode == PortableScannerBehavior.DisplayMode.SHOW_MACHINE_INFO) {
            FluidStack[] fluids = getContainedFluids();
            if (fluids != null) {
                boolean allTanksEmpty = true;
                for (int i = 0; i < fluids.length; i++) {
                    if (fluids[i] != null) {
                        if (fluids[i].getFluid() == null || fluids[i].isEmpty()) {
                            continue;
                        }

                        allTanksEmpty = false;
                        list.add(Component.translatable("behavior.portable_scanner.tank", i,
                                Component.translatable(FormattingUtil.formatNumbers(fluids[i].getAmount()))
                                        .withStyle(ChatFormatting.GREEN),
                                Component.translatable(FormattingUtil.formatNumbers(getCapacityPerTank()))
                                        .withStyle(ChatFormatting.YELLOW),
                                fluids[i].getDisplayName().copy()
                                        .withStyle(ChatFormatting.GOLD)));
                    }
                }

                if (allTanksEmpty) {
                    list.add(Component.translatable("behavior.portable_scanner.tanks_empty"));
                }
            }
        }

        return list;
    }

    private static class FluidTransaction {

        public final IFluidHandler target;
        public final IFluidHandler pipeTank;
        public int amount;

        private FluidTransaction(IFluidHandler target, IFluidHandler pipeTank, int amount) {
            this.target = target;
            this.pipeTank = pipeTank;
            this.amount = amount;
        }
    }
}
