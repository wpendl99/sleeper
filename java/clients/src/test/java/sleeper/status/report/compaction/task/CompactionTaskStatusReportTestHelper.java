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

package sleeper.status.report.compaction.task;

import sleeper.compaction.task.CompactionTaskFinishedStatus;
import sleeper.compaction.task.CompactionTaskStatus;
import sleeper.compaction.task.CompactionTaskType;
import sleeper.core.record.process.RecordsProcessed;
import sleeper.core.record.process.RecordsProcessedSummary;

import java.time.Instant;

public class CompactionTaskStatusReportTestHelper {
    private CompactionTaskStatusReportTestHelper() {
    }

    public static CompactionTaskStatus startedTask(String taskId, String startTime) {
        return startedTaskBuilder(taskId, startTime).build();
    }

    public static CompactionTaskStatus startedSplittingTask(String taskId, String startTime) {
        return startedTaskBuilder(taskId, startTime).type(CompactionTaskType.SPLITTING)
                .build();
    }

    private static CompactionTaskStatus.Builder startedTaskBuilder(String taskId, String startTime) {
        return CompactionTaskStatus.builder()
                .started(Instant.parse(startTime))
                .taskId(taskId);
    }

    public static CompactionTaskStatus finishedTask(String taskId, String startTime,
                                                    String finishTime, long linesRead, long linesWritten) {
        return finishedTaskBuilder(taskId, startTime, finishTime, linesRead, linesWritten).build();
    }

    public static CompactionTaskStatus finishedTaskWithFourRuns(String taskId, String startTime,
                                                                String finishTime, long linesRead, long linesWritten) {
        return CompactionTaskStatus.builder()
                .started(Instant.parse(startTime))
                .finished(taskFinishedStatusWithFourRuns(startTime, finishTime, linesRead, linesWritten),
                        Instant.parse(finishTime))
                .taskId(taskId).build();
    }

    public static CompactionTaskStatus finishedSplittingTask(String taskId, String startTime,
                                                             String finishTime, long linesRead, long linesWritten) {
        return finishedTaskBuilder(taskId, startTime, finishTime, linesRead, linesWritten)
                .type(CompactionTaskType.SPLITTING).build();
    }

    public static CompactionTaskStatus finishedSplittingTaskWithFourRuns(String taskId, String startTime,
                                                                         String finishTime, long linesRead, long linesWritten) {
        return CompactionTaskStatus.builder()
                .started(Instant.parse(startTime))
                .type(CompactionTaskType.SPLITTING)
                .finished(taskFinishedStatusWithFourRuns(startTime, finishTime, linesRead, linesWritten),
                        Instant.parse(finishTime))
                .taskId(taskId).build();
    }

    private static CompactionTaskStatus.Builder finishedTaskBuilder(String taskId, String startTime,
                                                                    String finishTime, long linesRead, long linesWritten) {
        return CompactionTaskStatus.builder()
                .started(Instant.parse(startTime))
                .finished(taskFinishedStatus(startTime, finishTime, linesRead, linesWritten),
                        Instant.parse(finishTime))
                .taskId(taskId);
    }

    private static CompactionTaskFinishedStatus.Builder taskFinishedStatus(
            String startTime, String finishTime, long linesRead, long linesWritten) {
        return taskFinishedStatusBuilder(startTime, finishTime, linesRead, linesWritten)
                .finish(Instant.parse(startTime), Instant.parse(finishTime));
    }

    private static CompactionTaskFinishedStatus.Builder taskFinishedStatusWithFourRuns(
            String startTime, String finishTime, long linesRead, long linesWritten) {
        return CompactionTaskFinishedStatus.builder()
                .addJobSummary(createSummary(startTime, finishTime, linesRead / 4, linesWritten / 4))
                .addJobSummary(createSummary(startTime, finishTime, linesRead / 4, linesWritten / 4))
                .addJobSummary(createSummary(startTime, finishTime, linesRead / 4, linesWritten / 4))
                .addJobSummary(createSummary(startTime, finishTime, linesRead / 4, linesWritten / 4))
                .finish(Instant.parse(startTime), Instant.parse(finishTime));
    }

    private static CompactionTaskFinishedStatus.Builder taskFinishedStatusBuilder(
            String startTime, String finishTime, long linesRead, long linesWritten) {
        return CompactionTaskFinishedStatus.builder()
                .addJobSummary(createSummary(startTime, finishTime, linesRead, linesWritten));
    }

    private static RecordsProcessedSummary createSummary(
            String startTime, String finishTime, long linesRead, long linesWritten) {
        return new RecordsProcessedSummary(
                new RecordsProcessed(linesRead, linesWritten),
                Instant.parse(startTime), Instant.parse(finishTime));
    }
}
