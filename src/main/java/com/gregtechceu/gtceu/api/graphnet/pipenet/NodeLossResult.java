package com.gregtechceu.gtceu.api.graphnet.pipenet;

import com.gregtechceu.gtceu.api.graphnet.traverse.util.CompositeLossOperator;
import com.gregtechceu.gtceu.api.graphnet.traverse.util.ReversibleLossOperator;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public final class NodeLossResult {

    public static final NodeLossResult IDENTITY = new NodeLossResult(null, ReversibleLossOperator.IDENTITY);

    private final @Nullable Consumer<WorldPipeNetNode> postAction;
    private final @NotNull ReversibleLossOperator lossFunction;

    public NodeLossResult(@Nullable Consumer<WorldPipeNetNode> postAction,
                          @NotNull ReversibleLossOperator lossFunction) {
        this.postAction = postAction;
        this.lossFunction = lossFunction;
    }

    @Contract("!null, !null -> new; !null, null -> param1; null, !null -> param2; null, null -> null")
    public static NodeLossResult combine(@Nullable NodeLossResult a, @Nullable NodeLossResult b) {
        if (a == null) return b;
        if (b == null) return a;
        Consumer<WorldPipeNetNode> postAction = a.postAction;
        if (b.postAction != null) {
            if (postAction == null) postAction = b.postAction;
            else postAction = postAction.andThen(b.postAction);
        }
        return new NodeLossResult(postAction, new CompositeLossOperator(a.lossFunction, b.lossFunction));
    }

    public boolean hasPostAction() {
        return postAction != null;
    }

    public Consumer<WorldPipeNetNode> getPostAction() {
        return postAction;
    }

    public void triggerPostAction(WorldPipeNetNode node) {
        if (postAction == null) return;
        this.postAction.accept(node);
    }

    public @NotNull ReversibleLossOperator getLossFunction() {
        return lossFunction;
    }
}
