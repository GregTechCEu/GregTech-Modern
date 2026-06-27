package com.gregtechceu.gtceu.common.computation;

import com.gregtechceu.gtceu.api.computation.ComputationConsumer;
import com.gregtechceu.gtceu.api.computation.ComputationProducer;
import com.gregtechceu.gtceu.api.machine.trait.ComputationPortTrait;

import java.util.*;

public class ComputationSolver {

    public Result solve(List<ComputationPortTrait> nodes) {
        List<ProducerEntry> producers = new ArrayList<>();
        List<ConsumerEntry> consumers = new ArrayList<>();

        for (var node : nodes) {
            node.getComputationProducer().ifPresent(producer -> producers.add(new ProducerEntry(producer,
                    Math.max(0, producer.getOfferedCWUt()))));
            node.getComputationConsumer().ifPresent(consumer -> {
                int minimum = Math.max(0, consumer.getMinimumCWUt());
                int requested = Math.max(minimum, consumer.getRequestedCWUt());
                consumers.add(new ConsumerEntry(node, consumer, minimum, requested));
            });
        }

        consumers.sort(Comparator.comparing(a -> a.node));

        int totalOffered = producers.stream().mapToInt(ProducerEntry::remaining).sum();
        int remainingOffer = totalOffered;
        for (var consumer : consumers) {
            int received = Math.min(remainingOffer, consumer.minimum);
            consumer.received = received;
            remainingOffer -= received;
        }

        if (remainingOffer > 0) {
            for (var consumer : consumers) {
                int extraRequest = consumer.requested - consumer.received;
                if (extraRequest <= 0) continue;
                int extra = Math.min(remainingOffer, extraRequest);
                consumer.received += extra;
                remainingOffer -= extra;
                if (remainingOffer <= 0) break;
            }
        }

        int totalReceived = 0;
        for (var consumer : consumers) {
            consumer.consumer.applyReceivedCWUt(consumer.received);
            totalReceived += consumer.received;
        }

        int remainingToAssign = totalReceived;
        for (var producer : producers) {
            int allocated = Math.min(remainingToAssign, producer.remaining);
            producer.producer.applyProducedCWUt(allocated);
            remainingToAssign -= allocated;
        }

        Map<ComputationConsumer, Integer> result = new HashMap<>();
        for (var consumer : consumers) {
            result.put(consumer.consumer, consumer.received);
        }
        return new Result(totalOffered, totalReceived, Math.max(0, remainingOffer), result);
    }

    public record Result(int totalOfferedCWUt, int allocatedCWUt, int spareCWUt,
                         Map<ComputationConsumer, Integer> allocations) {}

    private record ProducerEntry(ComputationProducer producer, int remaining) {}

    private static class ConsumerEntry {

        private final ComputationPortTrait node;
        private final ComputationConsumer consumer;
        private final int minimum;
        private final int requested;
        private int received;

        private ConsumerEntry(ComputationPortTrait node, ComputationConsumer consumer,
                              int minimum, int requested) {
            this.node = node;
            this.consumer = consumer;
            this.minimum = minimum;
            this.requested = requested;
        }
    }
}
