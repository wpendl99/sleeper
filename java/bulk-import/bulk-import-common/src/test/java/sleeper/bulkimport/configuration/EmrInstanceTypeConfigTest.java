/*
 * Copyright 2022-2023 Crown Copyright
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
package sleeper.bulkimport.configuration;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class EmrInstanceTypeConfigTest {

    @Test
    void shouldReadSingleInstanceType() {
        assertThat(EmrInstanceTypeConfig.readInstanceTypesProperty(
                List.of("some-type")))
                .containsExactly(
                        instanceType("some-type"));
    }

    @Test
    void shouldReadSingleInstanceTypeWithWeight() {
        assertThat(EmrInstanceTypeConfig.readInstanceTypesProperty(
                List.of("some-type", "12")))
                .containsExactly(
                        instanceTypeWithWeight("some-type", 12));
    }

    @Test
    void shouldReadMultipleInstanceTypes() {
        assertThat(EmrInstanceTypeConfig.readInstanceTypesProperty(
                List.of("some-type", "other-type", "another-type")))
                .containsExactly(
                        instanceType("some-type"),
                        instanceType("other-type"),
                        instanceType("another-type"));
    }

    @Test
    void shouldReadMultipleInstanceTypesWhereMiddleOneHasWeight() {
        assertThat(EmrInstanceTypeConfig.readInstanceTypesProperty(
                List.of("some-type", "other-type", "42", "another-type")))
                .containsExactly(
                        instanceType("some-type"),
                        instanceTypeWithWeight("other-type", 42),
                        instanceType("another-type"));
    }

    @Test
    void shouldReadMultipleInstanceTypesWhereAllHaveWeight() {
        assertThat(EmrInstanceTypeConfig.readInstanceTypesProperty(
                List.of("type-a", "1", "type-b", "2", "type-c", "3")))
                .containsExactly(
                        instanceTypeWithWeight("type-a", 1),
                        instanceTypeWithWeight("type-b", 2),
                        instanceTypeWithWeight("type-c", 3));
    }

    @Test
    void shouldReadNoInstanceTypes() {
        assertThat(EmrInstanceTypeConfig.readInstanceTypesProperty(List.of()))
                .isEmpty();
    }

    @Test
    void failWhenWeightSpecifiedBeforeType() {
        assertThatThrownBy(() -> EmrInstanceTypeConfig.readInstanceTypesProperty(
                List.of("12", "some-type")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private EmrInstanceTypeConfig instanceType(String instanceType) {
        return EmrInstanceTypeConfig.builder()
                .instanceType(instanceType)
                .build();
    }

    private EmrInstanceTypeConfig instanceTypeWithWeight(String instanceType, int weightedCapacity) {
        return EmrInstanceTypeConfig.builder()
                .instanceType(instanceType)
                .weightedCapacity(weightedCapacity)
                .build();
    }
}
