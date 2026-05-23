package com.gregtechceu.gtceu.api.datafixer.types;

import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.TypeTemplate;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Dynamic;

import static com.mojang.datafixers.DSL.*;

// spotless:off
public interface ExtraDSL {

    static TypeTemplate fields(
            final String name1, final TypeTemplate element1,
            final String name2, final TypeTemplate element2,
            final String name3, final TypeTemplate element3,
            final String name4, final TypeTemplate element4,
            final String name5, final TypeTemplate element5,
            final String name6, final TypeTemplate element6
    ) {
        return and(
                field(name1, element1),
                field(name2, element2),
                field(name3, element3),
                field(name4, element4),
                field(name5, element5),
                field(name6, element6)
        );
    }

    static <F> Type<Pair<F, Dynamic<?>>> fields(final String name, final Type<F> element) {
        return and(
                field(name, element),
                remainderType()
        );
    }

    static <F, G> Type<Pair<F, Pair<G, Dynamic<?>>>> fields(
            final String name1, final Type<F> element1,
            final String name2, final Type<G> element2
    ) {
        return and(
                field(name1, element1),
                field(name2, element2),
                remainderType()
        );
    }

    static <F, G, H> Type<Pair<F, Pair<G, Pair<H, Dynamic<?>>>>> fields(
            final String name1, final Type<F> element1,
            final String name2, final Type<G> element2,
            final String name3, final Type<H> element3
    ) {
        return and(
                field(name1, element1),
                field(name2, element2),
                field(name3, element3),
                remainderType()
        );
    }

    static <F, G, H, I> Type<Pair<F, Pair<G, Pair<H, Pair<I, Dynamic<?>>>>>> fields(
            final String name1, final Type<F> element1,
            final String name2, final Type<G> element2,
            final String name3, final Type<H> element3,
            final String name4, final Type<I> element4
    ) {
        return and(
                field(name1, element1),
                field(name2, element2),
                field(name3, element3),
                fields(
                        name4, element4
                )
        );
    }

    static <F, G, H, I, J> Type<Pair<F, Pair<G, Pair<H, Pair<I, Pair<J, Dynamic<?>>>>>>> fields(
            final String name1, final Type<F> element1,
            final String name2, final Type<G> element2,
            final String name3, final Type<H> element3,
            final String name4, final Type<I> element4,
            final String name5, final Type<J> element5
    ) {
        return and(
                field(name1, element1),
                field(name2, element2),
                field(name3, element3),
                fields(
                        name4, element4,
                        name5, element5
                )
        );
    }

    static <F, G, H, I, J, K> Type<Pair<F, Pair<G, Pair<H, Pair<I, Pair<J, Pair<K, Dynamic<?>>>>>>>> fields(
            final String name1, final Type<F> element1,
            final String name2, final Type<G> element2,
            final String name3, final Type<H> element3,
            final String name4, final Type<I> element4,
            final String name5, final Type<J> element5,
            final String name6, final Type<K> element6
    ) {
        return and(
                field(name1, element1),
                field(name2, element2),
                field(name3, element3),
                fields(
                        name4, element4,
                        name5, element5,
                        name6, element6
                )
        );
    }

    static <F, G> Type<Pair<F, G>> fields(
            final String name, final Type<F> element,
            final Type<G> rest
    ) {
        return and(
                field(name, element),
                rest
        );
    }

    static <F, G, H> Type<Pair<F, Pair<G, H>>> fields(
            final String name1, final Type<F> element1,
            final String name2, final Type<G> element2,
            final Type<H> rest
    ) {
        return and(
                field(name1, element1),
                field(name2, element2),
                rest
        );
    }

    static <F, G, H, I> Type<Pair<F, Pair<G, Pair<H, I>>>> fields(
            final String name1, final Type<F> element1,
            final String name2, final Type<G> element2,
            final String name3, final Type<H> element3,
            final Type<I> rest
    ) {
        return and(
                field(name1, element1),
                field(name2, element2),
                field(name3, element3),
                rest
        );
    }

    static <F, G, H, I, J> Type<Pair<F, Pair<G, Pair<H, Pair<I, J>>>>> fields(
            final String name1, final Type<F> element1,
            final String name2, final Type<G> element2,
            final String name3, final Type<H> element3,
            final String name4, final Type<I> element4,
            final Type<J> rest
    ) {
        return fields(
                name1, element1,
                name2, element2,
                name3, element3,
                fields(
                        name4, element4,
                        rest
                )
        );
    }

    static <F, G, H, I, J, K> Type<Pair<F, Pair<G, Pair<H, Pair<I, Pair<J, K>>>>>> fields(
            final String name1, final Type<F> element1,
            final String name2, final Type<G> element2,
            final String name3, final Type<H> element3,
            final String name4, final Type<I> element4,
            final String name5, final Type<J> element5,
            final Type<K> rest
    ) {
        return fields(
                name1, element1,
                name2, element2,
                name3, element3,
                fields(
                        name4, element4,
                        name5, element5,
                        rest
                )
        );
    }

    static <F, G, H, I, J, K, L> Type<Pair<F, Pair<G, Pair<H, Pair<I, Pair<J, Pair<K, L>>>>>>> fields(
            final String name1, final Type<F> element1,
            final String name2, final Type<G> element2,
            final String name3, final Type<H> element3,
            final String name4, final Type<I> element4,
            final String name5, final Type<J> element5,
            final String name6, final Type<K> element6,
            final Type<L> rest
    ) {
        return fields(
                name1, element1,
                name2, element2,
                name3, element3,
                fields(
                        name4, element4,
                        name5, element5,
                        name6, element6,
                        rest
                )
        );
    }

    static TypeTemplate optionalFields(
            final String name1, final TypeTemplate element1,
            final String name2, final TypeTemplate element2,
            final String name3, final TypeTemplate element3,
            final String name4, final TypeTemplate element4,
            final String name5, final TypeTemplate element5,
            final String name6, final TypeTemplate element6
    ) {
        return allWithRemainder(
                optional(field(name1, element1)),
                optional(field(name2, element2)),
                optional(field(name3, element3)),
                optional(field(name4, element4)),
                optional(field(name5, element5)),
                optional(field(name6, element6))
        );
    }

    static TypeTemplate optionalFields(
            final String name1, final TypeTemplate element1,
            final String name2, final TypeTemplate element2,
            final String name3, final TypeTemplate element3,
            final String name4, final TypeTemplate element4,
            final String name5, final TypeTemplate element5,
            final String name6, final TypeTemplate element6,
            final String name7, final TypeTemplate element7
    ) {
        return allWithRemainder(
                optional(field(name1, element1)),
                optional(field(name2, element2)),
                optional(field(name3, element3)),
                optional(field(name4, element4)),
                optional(field(name5, element5)),
                optional(field(name6, element6)),
                optional(field(name7, element7))
        );
    }

    static TypeTemplate optionalFields(
            final String name1, final TypeTemplate element1,
            final String name2, final TypeTemplate element2,
            final String name3, final TypeTemplate element3,
            final String name4, final TypeTemplate element4,
            final String name5, final TypeTemplate element5,
            final String name6, final TypeTemplate element6,
            final TypeTemplate rest
    ) {
        return and(
                optional(field(name1, element1)),
                optional(field(name2, element2)),
                optional(field(name3, element3)),
                optional(field(name4, element4)),
                optional(field(name5, element5)),
                optional(field(name6, element6)),
                rest
        );
    }

    static <F> Type<Pair<Either<F, Unit>, Dynamic<?>>> optionalFields(final String name, final Type<F> element) {
        return and(
                optional(field(name, element)),
                remainderType()
        );
    }

    static <F, G> Type<Pair<Either<F, Unit>, Pair<Either<G, Unit>, Dynamic<?>>>> optionalFields(
            final String name1, final Type<F> element1,
            final String name2, final Type<G> element2
    ) {
        return and(
                optional(field(name1, element1)),
                optional(field(name2, element2)),
                remainderType()
        );
    }

    static <F, G, H> Type<Pair<Either<F, Unit>, Pair<Either<G, Unit>, Pair<Either<H, Unit>, Dynamic<?>>>>> optionalFields(
            final String name1, final Type<F> element1,
            final String name2, final Type<G> element2,
            final String name3, final Type<H> element3
    ) {
        return and(
                optional(field(name1, element1)),
                optional(field(name2, element2)),
                optional(field(name3, element3)),
                remainderType()
        );
    }

    static <F, G, H, I> Type<Pair<Either<F, Unit>, Pair<Either<G, Unit>, Pair<Either<H, Unit>, Pair<Either<I, Unit>, Dynamic<?>>>>>> optionalFields(
            final String name1, final Type<F> element1,
            final String name2, final Type<G> element2,
            final String name3, final Type<H> element3,
            final String name4, final Type<I> element4
    ) {
        return and(
                optional(field(name1, element1)),
                optional(field(name2, element2)),
                optional(field(name3, element3)),
                optionalFields(
                        name4, element4
                )
        );
    }

    static <F, G, H, I, J> Type<Pair<Either<F, Unit>, Pair<Either<G, Unit>, Pair<Either<H, Unit>, Pair<Either<I, Unit>, Pair<Either<J, Unit>, Dynamic<?>>>>>>> optionalFields(
            final String name1, final Type<F> element1,
            final String name2, final Type<G> element2,
            final String name3, final Type<H> element3,
            final String name4, final Type<I> element4,
            final String name5, final Type<J> element5
    ) {
        return and(
                optional(field(name1, element1)),
                optional(field(name2, element2)),
                optional(field(name3, element3)),
                optionalFields(
                        name4, element4,
                        name5, element5
                )
        );
    }

    static <F, G, H, I, J, K> Type<Pair<Either<F, Unit>, Pair<Either<G, Unit>, Pair<Either<H, Unit>, Pair<Either<I, Unit>, Pair<Either<J, Unit>, Pair<Either<K, Unit>, Dynamic<?>>>>>>>> optionalFields(
            final String name1, final Type<F> element1,
            final String name2, final Type<G> element2,
            final String name3, final Type<H> element3,
            final String name4, final Type<I> element4,
            final String name5, final Type<J> element5,
            final String name6, final Type<K> element6
    ) {
        return and(
                optional(field(name1, element1)),
                optional(field(name2, element2)),
                optional(field(name3, element3)),
                optionalFields(
                        name4, element4,
                        name5, element5,
                        name6, element6
                )
        );
    }

    static <F, G> Type<Pair<Either<F, Unit>, G>> optionalFields(
            final String name, final Type<F> element,
            final Type<G> rest
    ) {
        return and(
                optional(field(name, element)),
                rest
        );
    }

    static <F, G, H> Type<Pair<Either<F, Unit>, Pair<Either<G, Unit>, H>>> optionalFields(
            final String name1, final Type<F> element1,
            final String name2, final Type<G> element2,
            final Type<H> rest
    ) {
        return and(
                optional(field(name1, element1)),
                optional(field(name2, element2)),
                rest
        );
    }

    static <F, G, H, I> Type<Pair<Either<F, Unit>, Pair<Either<G, Unit>, Pair<Either<H, Unit>, I>>>> optionalFields(
            final String name1, final Type<F> element1,
            final String name2, final Type<G> element2,
            final String name3, final Type<H> element3,
            final Type<I> rest
    ) {
        return and(
                optional(field(name1, element1)),
                optional(field(name2, element2)),
                optional(field(name3, element3)),
                rest
        );
    }

    static <F, G, H, I, J> Type<Pair<Either<F, Unit>, Pair<Either<G, Unit>, Pair<Either<H, Unit>, Pair<Either<I, Unit>, J>>>>> optionalFields(
            final String name1, final Type<F> element1,
            final String name2, final Type<G> element2,
            final String name3, final Type<H> element3,
            final String name4, final Type<I> element4,
            final Type<J> rest
    ) {
        return optionalFields(
                name1, element1,
                name2, element2,
                name3, element3,
                optionalFields(
                        name4, element4,
                        rest
                )
        );
    }

    static <F, G, H, I, J, K> Type<Pair<Either<F, Unit>, Pair<Either<G, Unit>, Pair<Either<H, Unit>, Pair<Either<I, Unit>, Pair<Either<J, Unit>, K>>>>>> optionalFields(
            final String name1, final Type<F> element1,
            final String name2, final Type<G> element2,
            final String name3, final Type<H> element3,
            final String name4, final Type<I> element4,
            final String name5, final Type<J> element5,
            final Type<K> rest
    ) {
        return optionalFields(
                name1, element1,
                name2, element2,
                name3, element3,
                optionalFields(
                        name4, element4,
                        name5, element5,
                        rest
                )
        );
    }

    static <F, G, H, I, J, K, L> Type<Pair<Either<F, Unit>, Pair<Either<G, Unit>, Pair<Either<H, Unit>, Pair<Either<I, Unit>, Pair<Either<J, Unit>, Pair<Either<K, Unit>, L>>>>>>> optionalFields(
            final String name1, final Type<F> element1,
            final String name2, final Type<G> element2,
            final String name3, final Type<H> element3,
            final String name4, final Type<I> element4,
            final String name5, final Type<J> element5,
            final String name6, final Type<K> element6,
            final Type<L> rest
    ) {
        return optionalFields(
                name1, element1,
                name2, element2,
                name3, element3,
                optionalFields(
                        name4, element4,
                        name5, element5,
                        name6, element6,
                        rest
                )
        );
    }
}
// spotless:on
