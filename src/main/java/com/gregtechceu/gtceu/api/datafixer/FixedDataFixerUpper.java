package com.gregtechceu.gtceu.api.datafixer;

import com.google.common.collect.Lists;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.DataFixerUpper;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.functions.PointFreeRule;
import com.mojang.datafixers.schemas.Schema;
import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMap;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import java.util.List;

/// Subclass of {@link DataFixerUpper} to backport a fix from a newer version of DFU.
public class FixedDataFixerUpper extends DataFixerUpper {

    protected static final PointFreeRule OPTIMIZATION_RULE = DataFixerUpper.OPTIMIZATION_RULE;

    private final List<DataFix> globalList;
    private final Long2ObjectMap<TypeRewriteRule> rules = Long2ObjectMaps.synchronize(new Long2ObjectOpenHashMap<>());

    protected FixedDataFixerUpper(Int2ObjectSortedMap<Schema> schemas, List<DataFix> globalList, IntSortedSet fixerVersions) {
        super(schemas, globalList, fixerVersions);
        this.globalList = globalList;
    }

    protected static int getLowestSchemaSameVersion(final Int2ObjectSortedMap<Schema> schemas, final int versionKey) {
        return DataFixerUpper.getLowestSchemaSameVersion(schemas, versionKey);
    }

    private int getLowestFixSameVersion(final int versionKey) {
        if (versionKey < fixerVersions().firstInt()) {
            // can have a version before everything else
            return fixerVersions().firstInt() - 1;
        }
        return fixerVersions().subSet(0, versionKey + 1).lastInt();
    }

    /// backport of DFU commit [`b81c76b`](https://github.com/Mojang/DataFixerUpper/commit/b81c76b)
    /// (Fix: apply all rules from subversions of target version)
    @Override
    protected TypeRewriteRule getRule(final int version, final int newVersion) {
        if (version >= newVersion) {
            return TypeRewriteRule.nop();
        }

        final long key = (long) version << 32 | newVersion;
        return rules.computeIfAbsent(key, k -> {
            final int expandedVersion = getLowestFixSameVersion(DataFixUtils.makeKey(version));

            final List<TypeRewriteRule> rules = Lists.newArrayList();
            for (final DataFix fix : globalList) {
                final int expandedFixVersion = fix.getVersionKey();
                final int fixVersion = DataFixUtils.getVersion(expandedFixVersion);
                if (expandedFixVersion > expandedVersion && fixVersion <= newVersion) {
                    final TypeRewriteRule fixRule = fix.getRule();
                    if (fixRule == TypeRewriteRule.nop()) {
                        continue;
                    }
                    rules.add(fixRule);
                }
            }

            return TypeRewriteRule.seq(rules);
        });
    }

    @Override
    protected IntSortedSet fixerVersions() {
        return super.fixerVersions();
    }
}
