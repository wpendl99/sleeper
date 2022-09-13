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
package sleeper.build.status;

import java.io.PrintStream;
import java.util.Date;
import java.util.Objects;

import static sleeper.build.status.ValidationUtils.ignoreEmpty;

public class ChunkStatus {

    private static final String COMPLETED = "completed";
    private static final String SUCCESS = "success";
    private static final String IN_PROGRESS = "in_progress";

    private final ProjectChunk chunk;
    private final String status;
    private final String conclusion;
    private final Long runId;
    private final String runUrl;
    private final Date runStarted;
    private final String commitSha;
    private final String commitMessage;

    private ChunkStatus(Builder builder) {
        chunk = Objects.requireNonNull(builder.chunk, "chunk must not be null");
        status = ignoreEmpty(builder.status);
        conclusion = ignoreEmpty(builder.conclusion);
        runId = builder.runId;
        runUrl = ignoreEmpty(builder.runUrl);
        runStarted = builder.runStarted;
        commitSha = ignoreEmpty(builder.commitSha);
        commitMessage = ignoreEmpty(builder.commitMessage);
    }

    public static Builder builder() {
        return new Builder();
    }

    public void report(GitHubHead head, PrintStream out) {
        out.println();
        if (conclusion != null) {
            out.println(chunk.getName() + ": " + status + ", " + conclusion);
        } else {
            out.println(chunk.getName() + ": " + status);
        }
        if (runUrl != null) {
            out.println("Run: " + runUrl);
        }
        if (isRunForHead(head)) {
            out.println("Build is for current commit");
        } else {
            if (runStarted != null) {
                out.println("Started at: " + runStarted);
            }
            if (commitMessage != null) {
                out.println("Message: " + commitMessage);
            }
            if (commitSha != null) {
                out.println("Commit: " + commitSha);
            }
        }
    }

    public boolean isFailCheckWithHead(GitHubHead head) {
        return (COMPLETED.equals(status) && !SUCCESS.equals(conclusion))
                || isWaitForOldBuildWithHead(head);
    }

    public boolean isWaitForOldBuildWithHead(GitHubHead head) {
        return IN_PROGRESS.equals(status) && !isRunForHead(head);
    }

    private boolean isRunForHead(GitHubHead head) {
        return head.getSha().equals(commitSha);
    }

    public String getChunkId() {
        return chunk.getId();
    }

    public ProjectChunk getChunk() {
        return chunk;
    }

    public Long getRunId() {
        return runId;
    }

    public String getRunUrl() {
        return runUrl;
    }

    public static Builder chunk(String chunk) {
        return builder().chunk(chunk);
    }

    public static Builder chunk(ProjectChunk chunk) {
        return builder().chunk(chunk);
    }

    public static ChunkStatus success(String chunk) {
        return chunk(chunk).success();
    }

    public static ChunkStatus inProgress(String chunk) {
        return chunk(chunk).inProgress();
    }

    public static ChunkStatus failure(String chunk) {
        return chunk(chunk).failure();
    }

    public static ChunkStatus cancelled(String chunk) {
        return chunk(chunk).cancelled();
    }

    public static ChunkStatus noBuild(String chunk) {
        return chunk(chunk).noBuild();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChunkStatus that = (ChunkStatus) o;
        return chunk.equals(that.chunk) && Objects.equals(status, that.status) && Objects.equals(conclusion, that.conclusion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chunk, status, conclusion);
    }

    @Override
    public String toString() {
        return "ChunkStatus{" +
                "chunk='" + chunk + '\'' +
                ", status='" + status + '\'' +
                ", conclusion='" + conclusion + '\'' +
                '}';
    }

    public static final class Builder {
        private ProjectChunk chunk;
        private String status;
        private String conclusion;
        private Long runId;
        private String runUrl;
        private Date runStarted;
        private String commitSha;
        private String commitMessage;

        private Builder() {
        }

        public Builder chunk(ProjectChunk chunk) {
            this.chunk = chunk;
            return this;
        }

        public Builder chunk(String chunk) {
            return chunk(ProjectChunk.chunk(chunk).name(chunk).workflow(chunk).build());
        }

        public ChunkStatus success() {
            return status(COMPLETED).conclusion(SUCCESS).build();
        }

        public ChunkStatus inProgress() {
            return status(IN_PROGRESS).conclusion(null).build();
        }

        public ChunkStatus failure() {
            return status(COMPLETED).conclusion("failure").build();
        }

        public ChunkStatus cancelled() {
            return status(COMPLETED).conclusion("cancelled").build();
        }

        public ChunkStatus noBuild() {
            return status(null).conclusion(null).build();
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder conclusion(String conclusion) {
            this.conclusion = conclusion;
            return this;
        }

        public Builder runId(Long runId) {
            this.runId = runId;
            return this;
        }

        public Builder runUrl(String runUrl) {
            this.runUrl = runUrl;
            return this;
        }

        public Builder runStarted(Date runStarted) {
            this.runStarted = runStarted;
            return this;
        }

        public Builder commitSha(String commitSha) {
            this.commitSha = commitSha;
            return this;
        }

        public Builder commitMessage(String commitMessage) {
            this.commitMessage = commitMessage;
            return this;
        }

        public ChunkStatus build() {
            return new ChunkStatus(this);
        }
    }

}
