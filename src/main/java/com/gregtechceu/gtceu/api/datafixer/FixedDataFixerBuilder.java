package com.gregtechceu.gtceu.api.datafixer;

import com.google.common.collect.Lists;
import com.mojang.datafixers.*;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import it.unimi.dsi.fastutil.ints.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/// This class is a full copy of {@link DataFixerBuilder} because all of its fields are private and thus needed to be
/// copied here. There was only one method that didn't need to be copied, so extending it is pointless.
public class FixedDataFixerBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(FixedDataFixerBuilder.class);

    private final int dataVersion;
    private final Int2ObjectSortedMap<Schema> schemas = new Int2ObjectAVLTreeMap<>();
    private final List<DataFix> globalList = Lists.newArrayList();
    private final IntSortedSet fixerVersions = new IntAVLTreeSet();

    public FixedDataFixerBuilder(int dataVersion) {
        this.dataVersion = dataVersion;
    }

    public Schema addSchema(final int version, final BiFunction<Integer, Schema, Schema> factory) {
        return addSchema(version, 0, factory);
    }

    public Schema addSchema(final int version, final int subVersion, final BiFunction<Integer, Schema, Schema> factory) {
        final int key = DataFixUtils.makeKey(version, subVersion);
        final Schema parent = schemas.isEmpty() ? null : schemas.get(FixedDataFixerUpper.getLowestSchemaSameVersion(schemas, key - 1));
        final Schema schema = factory.apply(DataFixUtils.makeKey(version, subVersion), parent);
        addSchema(schema);
        return schema;
    }

    public void addSchema(final Schema schema) {
        schemas.put(schema.getVersionKey(), schema);
    }

    public void addFixer(final DataFix fix) {
        final int version = DataFixUtils.getVersion(fix.getVersionKey());

        if (version > dataVersion) {
            LOGGER.warn("Ignored fix registered for version: {} as the DataVersion of the game is: {}", version, dataVersion);
            return;
        }

        globalList.add(fix);
        fixerVersions.add(fix.getVersionKey());
    }

    public DataFixer buildUnoptimized() {
        return build();
    }

    public DataFixer buildOptimized(final Set<DSL.TypeReference> requiredTypes, final Executor executor) {
        final FixedDataFixerUpper fixerUpper = build();

        final Instant started = Instant.now();
        final List<CompletableFuture<Void>> futures = Lists.newArrayList();

        final Set<String> requiredTypeNames = requiredTypes.stream().map(DSL.TypeReference::typeName).collect(Collectors.toSet());

        final IntBidirectionalIterator iterator = fixerUpper.fixerVersions().iterator();
        while (iterator.hasNext()) {
            final int versionKey = iterator.nextInt();
            final Schema schema = schemas.get(versionKey);
            for (final String typeName : schema.types()) {
                if (!requiredTypeNames.contains(typeName)) {
                    continue;
                }
                final CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    final Type<?> dataType = schema.getType(() -> typeName);
                    final TypeRewriteRule rule = fixerUpper.getRule(DataFixUtils.getVersion(versionKey), dataVersion);
                    dataType.rewrite(rule, FixedDataFixerUpper.OPTIMIZATION_RULE);
                }, executor).exceptionally(e -> {
                    LOGGER.error("Unable to build datafixers", e);
                    Runtime.getRuntime().exit(1);
                    return null;
                });
                futures.add(future);
            }
        }

        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).thenAccept(ignored -> {
            LOGGER.info("{} Datafixer optimizations took {} milliseconds", futures.size(), Duration.between(started, Instant.now()).toMillis());
        });

        return fixerUpper;
    }

    private FixedDataFixerUpper build() {
        return new FixedDataFixerUpper(new Int2ObjectAVLTreeMap<>(schemas), new ArrayList<>(globalList), new IntAVLTreeSet(fixerVersions));
    }
}
