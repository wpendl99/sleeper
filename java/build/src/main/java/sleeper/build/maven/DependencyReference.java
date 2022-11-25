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
package sleeper.build.maven;

import java.util.Objects;

public class DependencyReference {

    private final String artifactId;
    private final String groupId;
    private final String type;
    private final String scope;
    private final boolean exported;

    private DependencyReference(Builder builder) {
        artifactId = Objects.requireNonNull(builder.artifactId, "artifactId must not be null");
        groupId = Objects.requireNonNull(builder.groupId, "groupId must not be null");
        type = builder.type;
        scope = builder.scope;
        exported = builder.exported;
    }

    public static DependencyReference groupAndArtifact(String groupId, String artifactId) {
        return builder().artifactId(artifactId).groupId(groupId).exported(true).build();
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DependencyReference that = (DependencyReference) o;
        return exported == that.exported && artifactId.equals(that.artifactId) && groupId.equals(that.groupId) && Objects.equals(type, that.type) && Objects.equals(scope, that.scope);
    }

    @Override
    public int hashCode() {
        return Objects.hash(artifactId, groupId, type, scope, exported);
    }

    @Override
    public String toString() {
        return "DependencyReference{" +
                "artifactId='" + artifactId + '\'' +
                ", groupId='" + groupId + '\'' +
                ", type='" + type + '\'' +
                ", scope='" + scope + '\'' +
                ", exported=" + exported +
                '}';
    }

    public static final class Builder {
        private String artifactId;
        private String groupId;
        private String type;
        private String scope;
        private boolean exported;

        private Builder() {
        }

        public static Builder builder() {
            return new Builder();
        }

        public Builder artifactId(String artifactId) {
            this.artifactId = artifactId;
            return this;
        }

        public Builder groupId(String groupId) {
            this.groupId = groupId;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder scope(String scope) {
            this.scope = scope;
            return this;
        }

        public Builder exported(boolean exported) {
            this.exported = exported;
            return this;
        }

        public DependencyReference build() {
            return new DependencyReference(this);
        }
    }
}
