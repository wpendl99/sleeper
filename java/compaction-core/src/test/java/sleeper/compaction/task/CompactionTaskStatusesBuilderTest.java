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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CompactionTaskStatusesBuilderTest {

    @Test
    public void shouldCombineStatusUpdatesIntoTaskStatus() {
        // Given
        String taskId = "test-task";
        Instant startTime = Instant.parse("2022-10-12T15:45:00.001Z");
        CompactionTaskFinishedStatus finishedStatus = CompactionTaskFinishedStatus.builder()
                .addJobSummary(new CompactionJobSummary(
                        new CompactionJobRecordsProcessed(300L, 200L),
                        Instant.parse("2022-10-12T15:45:01.001Z"),
                        Instant.parse("2022-10-12T15:46:01.001Z")))
                .finish(startTime,
                        Instant.parse("2022-10-12T15:46:02.001Z"))
                .build();
        Instant expiryDate = Instant.parse("2022-11-12T15:45:00.001Z");

        // When
        List<CompactionTaskStatus> statuses = new CompactionTaskStatusesBuilder()
                .taskStarted(taskId, startTime)
                .taskFinished(taskId, finishedStatus)
                .expiryDate(taskId, expiryDate)
                .build();

        // Then
        assertThat(statuses)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactly(CompactionTaskStatus.builder()
                        .taskId(taskId)
                        .started(startTime)
                        .finishedStatus(finishedStatus)
                        .expiryDate(expiryDate)
                        .build());
    }

    @Test
    public void shouldOrderTasksByStartTimeMostRecentFirst() {
        // Given
        String taskId1 = "test-task-1";
        String taskId2 = "test-task-2";
        String taskId3 = "test-task-3";
        Instant startTime1 = Instant.parse("2022-10-12T15:45:00.001Z");
        Instant startTime2 = Instant.parse("2022-10-12T15:46:00.001Z");
        Instant startTime3 = Instant.parse("2022-10-12T15:47:00.001Z");

        // When
        List<CompactionTaskStatus> statuses = new CompactionTaskStatusesBuilder()
                .taskStarted(taskId3, startTime3)
                .taskStarted(taskId1, startTime1)
                .taskStarted(taskId2, startTime2)
                .build();

        // Then
        assertThat(statuses)
                .usingRecursiveFieldByFieldElementComparator()
                .containsExactly(
                        CompactionTaskStatus.builder().taskId(taskId3).started(startTime3).build(),
                        CompactionTaskStatus.builder().taskId(taskId2).started(startTime2).build(),
                        CompactionTaskStatus.builder().taskId(taskId1).started(startTime1).build());
    }
}
