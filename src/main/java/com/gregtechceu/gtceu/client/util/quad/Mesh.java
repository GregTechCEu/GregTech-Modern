package com.gregtechceu.gtceu.client.util.quad;

import java.util.function.Consumer;

/**
 * A bundle of one or more {@link QuadView} instances encoded by the renderer.
 *
 * <p>
 * Similar in purpose to the {@code List<BakedQuad>} instances returned by BakedModel, but affords the renderer the
 * ability to optimize the format for performance and memory allocation.
 *
 * <p>
 * Only the renderer should implement or extend this interface.
 *
 * @implNote The way we encode meshes makes it very simple.
 */
public class Mesh {
    /** Used to satisfy external calls to {@link #forEach(Consumer)}. */
    ThreadLocal<QuadView> POOL = ThreadLocal.withInitial(QuadView::new);

    final int[] data;

    Mesh(int[] data) {
        this.data = data;
    }

    public int[] data() {
        return data;
    }

    public void forEach(Consumer<QuadView> consumer) {
        forEach(consumer, POOL.get());
    }

    /**
     * The renderer will call this with its own cursor to avoid the performance hit of a thread-local lookup. Also
     * means renderer can hold final references to quad buffers.
     */
    void forEach(Consumer<QuadView> consumer, QuadView cursor) {
        final int limit = data.length;
        int index = 0;

        while (index < limit) {
            cursor.load(data, index);
            consumer.accept(cursor);
            index += EncodingFormat.QUAD_STRIDE;
        }
    }
}
