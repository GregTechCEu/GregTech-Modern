/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gregtechceu.gtceu.client.model.quad;

/**
 * Similar in purpose to {@link com.mojang.blaze3d.vertex.BufferBuilder} but simpler and not tied to NIO
 * or any other specific implementation, plus designed to handle both static and dynamic building.
 *
 * <p>
 * Decouples models from the vertex format(s) used by ModelRenderer to allow compatibility across diverse
 * implementations.
 *
 * @implNote Not much to it - mainly it just needs to grow the int[] array as quads are appended and
 *           maintain/provide a properly-configured {@link MutableQuadView} instance.
 *           All the encoding and other work is handled in the quad base classes.
 *           The one interesting bit is in {@link Maker#emit()}.
 */
public class MeshBuilder {

    private int[] data = new int[256];
    private final Maker maker = new Maker();
    private int index = 0;
    private int limit = data.length;

    public static MeshBuilder getInstance() {
        return new MeshBuilder();
    }

    protected void ensureCapacity() {
        if (EncodingFormat.QUAD_STRIDE > limit - index) {
            limit *= 2;
            final int[] bigger = new int[limit];
            System.arraycopy(data, 0, bigger, 0, index);
            data = bigger;
            maker.data = bigger;
        }
    }

    public Mesh build() {
        final int[] packed = new int[index];
        System.arraycopy(data, 0, packed, 0, index);
        index = 0;
        maker.begin(data, index);
        return new Mesh(packed);
    }

    public MutableQuadView getEmitter() {
        ensureCapacity();
        maker.begin(data, index);
        return maker;
    }

    /**
     * Our base classes are used differently so we define final encoding steps in subtypes. This will be a static mesh
     * used at render time so we want to capture all geometry now and apply non-location-dependent lighting.
     */
    private class Maker extends MutableQuadView {

        @Override
        public Maker emit() {
            computeGeometry();
            populateMissingNormals();
            index += EncodingFormat.QUAD_STRIDE;
            ensureCapacity();
            baseIndex = index;
            clear();
            return this;
        }
    }
}
