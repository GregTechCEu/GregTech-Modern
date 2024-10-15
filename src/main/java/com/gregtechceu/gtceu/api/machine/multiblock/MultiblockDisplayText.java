package com.gregtechceu.gtceu.api.machine.multiblock;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;
import java.util.function.Consumer;

public class MultiblockDisplayText {

    private static final Component EMPTY_COMPONENT = Component.empty();

    /**
     * Construct a new Multiblock Display Text builder.
     * <br>
     * Automatically adds the "Invalid Structure" line if the structure is not formed.
     */
    public static Builder builder(List<Component> textList, boolean isStructureFormed) {
        return builder(textList, isStructureFormed, true);
    }

    public static Builder builder(List<Component> textList, boolean isStructureFormed,
                                  boolean showIncompleteStructureWarning) {
        return new Builder(textList, isStructureFormed, showIncompleteStructureWarning);
    }

    public static class Builder {

        private final List<Component> textList;
        private final boolean isStructureFormed;

        private boolean isWorkingEnabled, isActive;

        // Keys for the three-state working system, can be set custom by multiblocks.
        private String idlingKey = "gtceu.multiblock.idling";
        private String pausedKey = "gtceu.multiblock.work_paused";
        private String runningKey = "gtceu.multiblock.running";

        private Builder(List<Component> textList, boolean isStructureFormed,
                        boolean showIncompleteStructureWarning) {
            this.textList = textList;
            this.isStructureFormed = isStructureFormed;

            if (!isStructureFormed && showIncompleteStructureWarning) {
                MutableComponent base = Component.translatable("gtceu.multiblock.invalid_structure")
                        .withStyle(ChatFormatting.RED);
                Component hover = Component.translatable("gtceu.multiblock.invalid_structure.tooltip")
                        .withStyle(ChatFormatting.GRAY);
                textList.add(base
                        .withStyle(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover))));
            }
        }

        /**
         * Set the current working enabled and active status of this multiblock, used by many line addition calls.
         */
        public Builder setWorkingStatus(boolean isWorkingEnabled, boolean isActive) {
            this.isWorkingEnabled = isWorkingEnabled;
            this.isActive = isActive;
            return this;
        }

        /**
         * Set custom translation keys for the three-state "Idling", "Paused", "Running" display text.
         * <strong>You still must call {@link Builder#addWorkingStatusLine()} for these to appear!</strong>
         * <br>
         * Pass any key as null for it to continue to use the default key.
         *
         * @param idlingKey  The translation key for the Idle state, or "!isActive && isWorkingEnabled".
         * @param pausedKey  The translation key for the Paused state, or "!isWorkingEnabled".
         * @param runningKey The translation key for the Running state, or "isActive".
         */
        public Builder setWorkingStatusKeys(String idlingKey, String pausedKey, String runningKey) {
            if (idlingKey != null)
                this.idlingKey = idlingKey;
            if (pausedKey != null)
                this.pausedKey = pausedKey;
            if (runningKey != null)
                this.runningKey = runningKey;
            return this;
        }

        /**
         * Adds the max EU/t that this multiblock can use.
         * <br>
         * Added if the structure is formed and if the passed energy container has greater than zero capacity.
         */
        public Builder addEnergyUsageLine(IEnergyContainer energyContainer) {
            if (!isStructureFormed)
                return this;
            if (energyContainer != null && energyContainer.getEnergyCapacity() > 0) {
                long maxVoltage = Math.max(energyContainer.getInputVoltage(), energyContainer.getOutputVoltage());

                String energyFormatted = FormattingUtil.formatNumbers(maxVoltage);
                // wrap in text component to keep it from being formatted
                Component voltageName = Component.literal(
                        GTValues.VNF[GTUtil.getFloorTierByVoltage(maxVoltage)]);

                MutableComponent bodyText = Component.translatable("gtceu.multiblock.max_energy_per_tick",
                        energyFormatted, voltageName).withStyle(ChatFormatting.GRAY);
                Component hoverText = Component.translatable("gtceu.multiblock.max_energy_per_tick_hover")
                        .withStyle(ChatFormatting.GRAY);
                textList.add(bodyText.withStyle(
                        style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText))));
            }
            return this;
        }

        /**
         * Adds the max Recipe Tier that this multiblock can use for recipe lookup.
         * <br>
         * Added if the structure is formed and if the passed tier is a valid energy tier index for
         * {@link GTValues#VNF}.
         */
        public Builder addEnergyTierLine(int tier) {
            if (!isStructureFormed)
                return this;
            if (tier < GTValues.ULV || tier > GTValues.MAX)
                return this;

            Component voltageName = Component.literal(GTValues.VNF[tier]);
            MutableComponent bodyText = Component.translatable(
                    "gtceu.multiblock.max_recipe_tier",
                    voltageName).withStyle(ChatFormatting.GRAY);
            Component hoverText = Component.translatable("gtceu.multiblock.max_recipe_tier_hover")
                    .withStyle(ChatFormatting.GRAY);
            textList.add(bodyText
                    .withStyle(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText))));
            return this;
        }

        /**
         * Adds the exact EU/t that this multiblock needs to run.
         * <br>
         * Added if the structure is formed and if the passed value is greater than zero.
         */
        public Builder addEnergyUsageExactLine(long energyUsage) {
            if (!isStructureFormed)
                return this;
            if (energyUsage > 0) {
                String energyFormatted = FormattingUtil.formatNumbers(energyUsage);
                // wrap in text component to keep it from being formatted
                Component voltageName = Component.literal(
                        GTValues.VNF[GTUtil.getTierByVoltage(energyUsage)]);

                textList.add(Component.translatable("gtceu.multiblock.energy_consumption",
                        energyFormatted, voltageName).withStyle(ChatFormatting.GRAY));
            }
            return this;
        }

        /**
         * Adds the max EU/t that this multiblock can produce.
         * <br>
         * Added if the structure is formed and if the max voltage is greater than zero and the recipe EU/t.
         */
        public Builder addEnergyProductionLine(long maxVoltage, long recipeEUt) {
            if (!isStructureFormed)
                return this;
            if (maxVoltage != 0 && maxVoltage >= -recipeEUt) {
                String energyFormatted = FormattingUtil.formatNumbers(maxVoltage);
                // wrap in text component to keep it from being formatted
                Component voltageName = Component.literal(
                        GTValues.VNF[GTUtil.getFloorTierByVoltage(maxVoltage)]);

                textList.add(Component.translatable("gtceu.multiblock.max_energy_per_tick",
                        energyFormatted, voltageName).withStyle(ChatFormatting.GRAY));
            }
            return this;
        }

        /**
         * Adds the max EU/t that this multiblock can produce, including how many amps. Recommended for multi-amp
         * outputting multis.
         * <br>
         * Added if the structure is formed, if the amperage is greater than zero and if the max voltage is greater than
         * zero.
         */
        public Builder addEnergyProductionAmpsLine(long maxVoltage, int amperage) {
            if (!isStructureFormed)
                return this;
            if (maxVoltage != 0 && amperage != 0) {
                String energyFormatted = FormattingUtil.formatNumbers(maxVoltage);
                // wrap in text component to keep it from being formatted
                Component voltageName = Component.literal(
                        GTValues.VNF[GTUtil.getFloorTierByVoltage(maxVoltage)]);

                textList.add(Component.translatable("gtceu.multiblock.max_energy_per_tick_amps",
                        energyFormatted, amperage, voltageName).withStyle(ChatFormatting.GRAY));
            }
            return this;
        }

        /**
         * Adds the max CWU/t that this multiblock can use.
         * <br>
         * Added if the structure is formed and if the max CWU/t is greater than zero.
         */
        public Builder addComputationUsageLine(int maxCWUt) {
            if (!isStructureFormed)
                return this;
            if (maxCWUt > 0) {
                Component computation = Component.literal(FormattingUtil.formatNumbers(maxCWUt))
                        .withStyle(ChatFormatting.AQUA);
                textList.add(Component.translatable("gtceu.multiblock.computation.max",
                        computation).withStyle(ChatFormatting.GRAY));
            }
            return this;
        }

        /**
         * Adds a currently used CWU/t line.
         * <br>
         * Added if the structure is formed, the machine is active, and the current CWU/t is greater than zero.
         */
        public Builder addComputationUsageExactLine(int currentCWUt) {
            if (!isStructureFormed)
                return this;
            if (isActive && currentCWUt > 0) {
                Component computation = Component.literal(FormattingUtil.formatNumbers(currentCWUt) + " CWU/t")
                        .withStyle(ChatFormatting.AQUA);
                textList.add(Component.translatable(
                        "gtceu.multiblock.computation.usage",
                        computation).withStyle(ChatFormatting.GRAY));
            }
            return this;
        }

        /**
         * Adds a three-state indicator line, showing if the machine is running, paused, or idling.
         * <br>
         * Added if the structure is formed.
         */
        public Builder addWorkingStatusLine() {
            if (!isStructureFormed)
                return this;

            if (!isWorkingEnabled) {
                return addWorkPausedLine(false);
            } else if (isActive) {
                return addRunningPerfectlyLine(false);
            } else {
                return addIdlingLine(false);
            }
        }

        /**
         * Adds the "Work Paused." line.
         * <br>
         * Added if working is not enabled, or if the checkState passed parameter is false.
         * Also added only if formed.
         */
        public Builder addWorkPausedLine(boolean checkState) {
            if (!isStructureFormed)
                return this;
            if (!checkState || !isWorkingEnabled) {
                textList.add(Component.translatable(pausedKey).withStyle(ChatFormatting.GOLD));
            }
            return this;
        }

        /**
         * Adds the "Running Perfectly." line.
         * <br>
         * Added if machine is active, or if the checkState passed parameter is false.
         * Also added only if formed.
         */
        public Builder addRunningPerfectlyLine(boolean checkState) {
            if (!isStructureFormed)
                return this;
            if (!checkState || isActive) {
                textList.add(Component.translatable(runningKey).withStyle(ChatFormatting.GREEN));
            }
            return this;
        }

        /**
         * Adds the "Idling." line.
         * <br>
         * Added if the machine is not active and working is enabled, or if the checkState passed parameter is false.
         * Also added only if formed.
         */
        public Builder addIdlingLine(boolean checkState) {
            if (!isStructureFormed)
                return this;
            if (!checkState || (isWorkingEnabled && !isActive)) {
                textList.add(Component.translatable(idlingKey).withStyle(ChatFormatting.GRAY));
            }
            return this;
        }

        /**
         * Adds a simple progress line that displays the current time of a recipe and its progress as a percentage.
         * <br>
         * Added if structure is formed and the machine is active.
         *
         * @param currentDuration The current duration of the recipe in ticks
         * @param maxDuration     The max duration of the recipe in ticks
         * @param progressPercent Progress formatted as a range of [0,1] representing the progress of the recipe.
         */
        public Builder addProgressLine(double currentDuration, double maxDuration, double progressPercent) {
            if (!isStructureFormed || !isActive)
                return this;
            int currentProgress = (int) (progressPercent * 100);
            double currentInSec = currentDuration / 20.0;
            double maxInSec = maxDuration / 20.0;
            textList.add(Component.translatable("gtceu.multiblock.progress",
                    String.format("%.2f", (float) currentInSec),
                    String.format("%.2f", (float) maxInSec), currentProgress));
            return this;
        }

        public Builder addOutputLines(GTRecipe recipe, int chanceTier) {
            if (!isStructureFormed || !isActive)
                return this;
            if (recipe != null) {
                var function = recipe.getType().getChanceFunction();
                double maxDurationSec = (double) recipe.duration / 20.0;
                var itemOutputs = recipe.getOutputContents(ItemRecipeCapability.CAP);
                var fluidOutputs = recipe.getOutputContents(FluidRecipeCapability.CAP);

                for (var item : itemOutputs) {
                    var stack = (ItemRecipeCapability.CAP.of(item.content).getItems()[0]);
                    if (stack.getCount() < maxDurationSec) {
                        if (item.chance < item.maxChance) {
                            double averageDurationforRoll = (double) item.maxChance / (double) function
                                    .getBoostedChance(item, RecipeHelper.getPreOCRecipeEuTier(recipe), chanceTier);
                            textList.add(Component.translatable("gtceu.multiblock.output_line.2", stack.getHoverName(),
                                    stack.getCount(),
                                    FormattingUtil.formatNumber2Places(averageDurationforRoll * maxDurationSec)));
                        } else {
                            textList.add(Component.translatable("gtceu.multiblock.output_line.0", stack.getHoverName(),
                                    stack.getCount(), maxDurationSec));
                        }
                    } else {
                        double countPerSec = (double) stack.getCount() / maxDurationSec;
                        if (item.chance < item.maxChance) {
                            double averageDurationforRoll = (double) item.maxChance / (double) function
                                    .getBoostedChance(item, RecipeHelper.getPreOCRecipeEuTier(recipe), chanceTier);
                            textList.add(Component.translatable("gtceu.multiblock.output_line.3",
                                    stack.getHoverName(), stack.getCount(),
                                    FormattingUtil.formatNumber2Places(averageDurationforRoll * countPerSec)));
                        } else {
                            textList.add(Component.translatable("gtceu.multiblock.output_line.1",
                                    stack.getHoverName(), stack.getCount(),
                                    FormattingUtil.formatNumber2Places(countPerSec)));
                        }
                    }
                }
                for (var fluid : fluidOutputs) {
                    var stack = (FluidRecipeCapability.CAP.of(fluid.content).getStacks()[0]);
                    if (stack.getAmount() < maxDurationSec) {
                        if (fluid.chance < fluid.maxChance) {
                            double averageDurationforRoll = (double) fluid.maxChance / (double) function
                                    .getBoostedChance(fluid, RecipeHelper.getPreOCRecipeEuTier(recipe), chanceTier);
                            textList.add(Component.translatable("gtceu.multiblock.output_line.2",
                                    stack.getDisplayName(), stack.getAmount(),
                                    FormattingUtil.formatNumber2Places(averageDurationforRoll * maxDurationSec)));
                        } else {
                            textList.add(Component.translatable("gtceu.multiblock.output_line.0",
                                    stack.getDisplayName(), stack.getAmount(),
                                    FormattingUtil.formatNumber2Places(maxDurationSec)));
                        }
                    } else {
                        double countPerSec = (double) stack.getAmount() / maxDurationSec;
                        if (fluid.chance < fluid.maxChance) {
                            double averageDurationforRoll = (double) fluid.maxChance / (double) function
                                    .getBoostedChance(fluid, RecipeHelper.getPreOCRecipeEuTier(recipe), chanceTier);
                            textList.add(Component.translatable("gtceu.multiblock.output_line.3",
                                    stack.getDisplayName(), stack.getAmount(),
                                    FormattingUtil.formatNumber2Places(averageDurationforRoll * countPerSec)));
                        } else {
                            textList.add(Component.translatable("gtceu.multiblock.output_line.1",
                                    stack.getDisplayName(), stack.getAmount(),
                                    FormattingUtil.formatNumber2Places(countPerSec)));
                        }
                    }
                }
            }
            return this;
        }

        /**
         * Adds a line indicating the current mode of the multi
         */
        public Builder addMachineModeLine(GTRecipeType recipeType, boolean hasMultipleModes) {
            if (!isStructureFormed || !hasMultipleModes)
                return this;
            textList.add(Component
                    .translatable("gtceu.gui.machinemode",
                            Component.translatable(recipeType.registryName.toLanguageKey()))
                    .withStyle(ChatFormatting.AQUA));
            return this;
        }

        /**
         * Adds a line indicating how many parallels this multi can potentially perform.
         * <br>
         * Added if structure is formed and the number of parallels is greater than one.
         */
        public Builder addParallelsLine(int numParallels) {
            if (!isStructureFormed)
                return this;
            if (numParallels > 1) {
                Component parallels = Component.literal(FormattingUtil.formatNumbers(numParallels))
                        .withStyle(ChatFormatting.DARK_PURPLE);

                textList.add(Component.translatable(
                        "gtceu.multiblock.parallel",
                        parallels)
                        .withStyle(ChatFormatting.GRAY));
            }
            return this;
        }

        /**
         * Adds a warning line when the machine is low on power.
         * <br>
         * Added if the structure is formed and if the passed parameter is true.
         */
        public Builder addLowPowerLine(boolean isLowPower) {
            if (!isStructureFormed)
                return this;
            if (isLowPower) {
                textList.add(
                        Component.translatable("gtceu.multiblock.not_enough_energy").withStyle(ChatFormatting.YELLOW));
            }
            return this;
        }

        /**
         * Adds a warning line when the machine is low on computation.
         * <br>
         * Added if the structure is formed and if the passed parameter is true.
         */
        public Builder addLowComputationLine(boolean isLowComputation) {
            if (!isStructureFormed)
                return this;
            if (isLowComputation) {
                textList.add(Component.translatable("gtceu.multiblock.computation.not_enough_computation")
                        .withStyle(ChatFormatting.YELLOW));
            }
            return this;
        }

        /**
         * Adds a warning line when the machine's dynamo tier is too low for current conditions.
         * <br>
         * Added if the structure is formed and if the passed parameter is true.
         */
        public Builder addLowDynamoTierLine(boolean isTooLow) {
            if (!isStructureFormed)
                return this;
            if (isTooLow) {
                textList.add(Component.translatable("gtceu.multiblock.not_enough_energy_output")
                        .withStyle(ChatFormatting.YELLOW));
            }
            return this;
        }

        /**
         * Adds warning line(s) when the machine has maintenance problems.
         * <br>
         * Added if there are any maintenance problems, one line per problem as well as a header. <br>
         * Will check the config setting for if maintenance is enabled automatically.
         */
        public Builder addMaintenanceProblemLines(byte maintenanceProblems) {
            if (!isStructureFormed || !ConfigHolder.INSTANCE.machines.enableMaintenance)
                return this;
            if (maintenanceProblems <= 0b111111 && maintenanceProblems > 0) {
                addMaintenanceProblemHeader();

                // Wrench
                if ((maintenanceProblems & 1) == 0) {
                    textList.add(Component.translatable("gtceu.multiblock.universal.problem.wrench")
                            .withStyle(ChatFormatting.GRAY));
                }

                // Screwdriver
                if (((maintenanceProblems >> 1) & 1) == 0) {
                    textList.add(Component.translatable("gtceu.multiblock.universal.problem.screwdriver")
                            .withStyle(ChatFormatting.GRAY));
                }

                // Soft Mallet
                if (((maintenanceProblems >> 2) & 1) == 0) {
                    textList.add(Component.translatable("gtceu.multiblock.universal.problem.soft_mallet")
                            .withStyle(ChatFormatting.GRAY));
                }

                // Hammer
                if (((maintenanceProblems >> 3) & 1) == 0) {
                    textList.add(Component.translatable("gtceu.multiblock.universal.problem.hard_hammer")
                            .withStyle(ChatFormatting.GRAY));
                }

                // Wire Cutters
                if (((maintenanceProblems >> 4) & 1) == 0) {
                    textList.add(Component.translatable("gtceu.multiblock.universal.problem.wire_cutter")
                            .withStyle(ChatFormatting.GRAY));
                }

                // Crowbar
                if (((maintenanceProblems >> 5) & 1) == 0) {
                    textList.add(Component.translatable("gtceu.multiblock.universal.problem.crowbar")
                            .withStyle(ChatFormatting.GRAY));
                }
            }
            return this;
        }

        private void addMaintenanceProblemHeader() {
            textList.add(
                    Component.translatable("gtceu.multiblock.universal.has_problems").withStyle(ChatFormatting.YELLOW));
        }

        /**
         * Adds two error lines when the machine's muffler hatch is obstructed.
         * <br>
         * Added if the structure is formed and if the passed parameter is true.
         */
        public Builder addMufflerObstructedLine(boolean isObstructed) {
            if (!isStructureFormed)
                return this;
            if (isObstructed) {
                textList.add(Component.translatable("gtceu.multiblock.universal.muffler_obstructed")
                        .withStyle(ChatFormatting.RED));
                textList.add(Component.translatable("gtceu.multiblock.universal.muffler_obstructed.tooltip")
                        .withStyle(ChatFormatting.GRAY));
            }
            return this;
        }

        /**
         * Adds a fuel consumption line showing the fuel name and the number of ticks per recipe run.
         * <br>
         * Added if structure is formed, the machine is active, and the passed fuelName parameter is not null.
         */
        public Builder addFuelNeededLine(String fuelName, int previousRecipeDuration) {
            if (!isStructureFormed || !isActive)
                return this;
            Component fuelNeeded = Component.literal(fuelName).withStyle(ChatFormatting.RED);
            Component numTicks = Component.literal(FormattingUtil.formatNumbers(previousRecipeDuration))
                    .withStyle(ChatFormatting.AQUA);
            textList.add(Component.translatable(
                    "gtceu.multiblock.turbine.fuel_needed",
                    fuelNeeded, numTicks).withStyle(ChatFormatting.GRAY));
            return this;
        }

        /**
         * Insert an empty line into the text list.
         */
        public Builder addEmptyLine() {
            textList.add(EMPTY_COMPONENT);
            return this;
        }

        /**
         * Add custom text dynamically, allowing for custom application logic.
         */
        public Builder addCustom(Consumer<List<Component>> customConsumer) {
            customConsumer.accept(textList);
            return this;
        }
    }
}
