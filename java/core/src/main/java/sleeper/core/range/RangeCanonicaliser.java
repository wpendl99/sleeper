/*
 * Copyright 2022 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sleeper.core.range;

import sleeper.core.schema.type.ByteArrayType;
import sleeper.core.schema.type.IntType;
import sleeper.core.schema.type.LongType;
import sleeper.core.schema.type.StringType;
import sleeper.core.schema.type.Type;

/**
 * Given a {@link Range}, this class can determine if it is in canonical form,
 * i.e. the minimum is inclusive and the maximum is exclusive, and if not it
 * can convert it into canonical form.
 */
public class RangeCanonicaliser {

    private RangeCanonicaliser() {
    }

    public static boolean isRangeInCanonicalForm(Range range) {
        return range.isMinInclusive() && !range.isMaxInclusive();
    }

    public static Range canonicaliseRange(Range range) {
        if (isRangeInCanonicalForm(range)) {
            return range;
        }

        Object rangeMin = range.isMinInclusive() ? range.getMin() : nextValue(range.getFieldType(), range.getMin());
        Object rangeMax = range.isMaxInclusive() ? nextValue(range.getFieldType(), range.getMax()) : range.getMax();
        return new Range(range.getField(), rangeMin, true, rangeMax, false);
    }

    private static Object nextValue(Type type, Object object) {
        if (null == object) {
            return null;
        }
        if (type instanceof IntType) {
            return nextIntValue((Integer) object);
        }
        if (type instanceof LongType) {
            return nextLongValue((Long) object);
        }
        if (type instanceof StringType) {
            return nextStringValue((String) object);
        }
        if (type instanceof ByteArrayType) {
            return nextByteArrayValue((byte[]) object);
        }
        throw new IllegalArgumentException("Unknown type of " + type);
    }

    private static Integer nextIntValue(Integer value) {
        if (value == Integer.MAX_VALUE) {
            return null;
        }
        return value + 1;
    }

    private static Long nextLongValue(Long value) {
        if (value == Long.MAX_VALUE) {
            return null;
        }
        return value + 1L;
    }

    private static String nextStringValue(String value) {
        return value + '\u0000';
    }

    private static byte[] nextByteArrayValue(byte[] value) {
        byte[] next = new byte[value.length + 1];
        System.arraycopy(value, 0, next, 0, value.length);
        next[value.length] = Byte.MIN_VALUE;
        return next;
    }
}
