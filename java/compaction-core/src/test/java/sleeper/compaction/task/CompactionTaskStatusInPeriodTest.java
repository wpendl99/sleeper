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
package sleeper.compaction.task;

import org.junit.Test;
import sleeper.compaction.job.CompactionJobRecordsProcessed;
import sleeper.compaction.job.CompactionJobSummary;

import java.time.Instant;
import java.time.Period;

import static org.assertj.core.api.Assertions.assertThat;

public class CompactionTaskStatusInPeriodTest {
    private static final Instant EPOCH_START = Instant.ofEpochMilli(0);
    private static final Instant FAR_FUTURE = EPOCH_START.plus(Period.ofDays(999999999));

    @Test
    public void shouldBeInPeriodWithStartTimeOnly() {
        // Given
        CompactionTaskStatus task = CompactionTaskStatus.started(
                Instant.parse("2022-10-06T11:19:00.001Z")).build();

        // When / Then
        assertThat(task.isInPeriod(EPOCH_START, FAR_FUTURE)).isTrue();
    }

    @Test
    public void shouldNotBeInPeriodWithStartTimeOnlyWhenEndIsStartTime() {
        // Given
        Instant startTime = Instant.parse("2022-10-06T11:19:00.001Z");
        CompactionTaskStatus task = CompactionTaskStatus.started(startTime).build();

        // When / Then
        assertThat(task.isInPeriod(EPOCH_START, startTime)).isFalse();
    }

    @Test
    public void shouldNotBeInPeriodWithStartTimeOnlyWhenStartIsStartTime() {
        // Given
        Instant startTime = Instant.parse("2022-10-06T11:19:00.001Z");
        CompactionTaskStatus task = CompactionTaskStatus.started(startTime).build();

        // When / Then
        assertThat(task.isInPeriod(startTime, FAR_FUTURE)).isFalse();
    }

    @Test
    public void shouldBeInPeriodWithStartAndFinishTime() {
        // Given
        CompactionTaskStatus task = taskWithStartAndFinishTime(
                Instant.parse("2022-10-06T11:19:00.001Z"),
                Instant.parse("2022-10-06T11:19:30.001Z"));

        // When / Then
        assertThat(task.isInPeriod(EPOCH_START, FAR_FUTURE)).isTrue();
    }

    @Test
    public void shouldNotBeInPeriodWhenEndIsStartTime() {
        // Given
        Instant startTime = Instant.parse("2022-10-06T11:19:00.001Z");
        Instant finishTime = Instant.parse("2022-10-06T11:19:31.001Z");
        CompactionTaskStatus task = taskWithStartAndFinishTime(startTime, finishTime);

        // When / Then
        assertThat(task.isInPeriod(EPOCH_START, startTime)).isFalse();
    }

    @Test
    public void shouldBeInPeriodWhenEndIsFinishTime() {
        // Given
        Instant startTime = Instant.parse("2022-10-06T11:19:00.001Z");
        Instant finishTime = Instant.parse("2022-10-06T11:19:31.001Z");
        CompactionTaskStatus task = taskWithStartAndFinishTime(startTime, finishTime);

        // When / Then
        assertThat(task.isInPeriod(EPOCH_START, finishTime)).isTrue();
    }

    @Test
    public void shouldNotBeInPeriodWhenStartIsFinishTime() {
        // Given
        Instant startTime = Instant.parse("2022-10-06T11:19:00.001Z");
        Instant finishTime = Instant.parse("2022-10-06T11:19:31.001Z");
        CompactionTaskStatus task = taskWithStartAndFinishTime(startTime, finishTime);

        // When / Then
        assertThat(task.isInPeriod(finishTime, FAR_FUTURE)).isFalse();
    }

    @Test
    public void shouldBeInPeriodWhenStartIsStartTime() {
        // Given
        Instant startTime = Instant.parse("2022-10-06T11:19:00.001Z");
        Instant finishTime = Instant.parse("2022-10-06T11:19:31.001Z");
        CompactionTaskStatus task = taskWithStartAndFinishTime(startTime, finishTime);

        // When / Then
        assertThat(task.isInPeriod(startTime, FAR_FUTURE)).isTrue();
    }

    private static CompactionTaskStatus taskWithStartAndFinishTime(Instant startTime, Instant finishTime) {
        return CompactionTaskStatus.started(startTime)
                .finished(CompactionTaskFinishedStatus.builder()
                        .addJobSummary(new CompactionJobSummary(
                                new CompactionJobRecordsProcessed(200, 100),
                                startTime, finishTime
                        )), finishTime)
                .build();
    }
}
