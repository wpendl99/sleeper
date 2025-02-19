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
package sleeper.core.partition;

import sleeper.core.key.Key;
import sleeper.core.range.Region;
import sleeper.core.range.RegionCanonicaliser;
import sleeper.core.schema.Schema;
import sleeper.core.schema.type.PrimitiveType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * A Partition is a Region of key space, with additional information that allows
 * the position of the partition in the tree of partitions to be identified,
 * e.g. it has a unique id, the id of its parent partition, the id of any child
 * partitions, and if it has been split the dimension it was split on.
 * <p>
 * The {@link Region} must be in canonical form, i.e. all the ranges must contain
 * their minimum but not contain their maximum.
 */
public class Partition {
    private List<PrimitiveType> rowKeyTypes;
    private Region region;
    private String id;
    private boolean leafPartition;
    private String parentPartitionId;
    private List<String> childPartitionIds;
    private int dimension = -1; // -1 used to indicate that it has not been split yet; when it has been split, indicates which dimension was used to split on.

    private Partition(Partition.Builder builder) {
        setRowKeyTypes(builder.getRowKeyTypes());
        if (null != builder.getRegion()) {
            setRegion(builder.getRegion());
        }
        setId(builder.getId());
        setLeafPartition(builder.isLeafPartition());
        setParentPartitionId(builder.getParentPartitionId());
        setChildPartitionIds(builder.getChildPartitionIds());
        setDimension(builder.getDimension());
    }

    public static Builder builder() {
        return new Partition.Builder();
    }

    public List<PrimitiveType> getRowKeyTypes() {
        return rowKeyTypes;
    }

    public void setRowKeyTypes(List<PrimitiveType> rowKeyTypes) {
        this.rowKeyTypes = rowKeyTypes;
    }

    public void setRowKeyTypes(PrimitiveType... rowKeyTypes) {
        this.rowKeyTypes = new ArrayList<>();
        for (PrimitiveType type : rowKeyTypes) {
            this.rowKeyTypes.add(type);
        }
    }

    public Region getRegion() {
        return region;
    }

    public void setRegion(Region region) {
        if (!RegionCanonicaliser.isRegionInCanonicalForm(region)) {
            throw new IllegalArgumentException("Region must be in canonical form");
        }
        this.region = region;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isLeafPartition() {
        return leafPartition;
    }

    public void setLeafPartition(boolean leafPartition) {
        this.leafPartition = leafPartition;
    }

    public String getParentPartitionId() {
        return parentPartitionId;
    }

    public void setParentPartitionId(String parentPartitionId) {
        this.parentPartitionId = parentPartitionId;
    }

    public List<String> getChildPartitionIds() {
        if (null == childPartitionIds) {
            childPartitionIds = new ArrayList<>();
        }
        return childPartitionIds;
    }

    public void setChildPartitionIds(List<String> childPartitionIds) {
        this.childPartitionIds = new ArrayList<>();
        if (null != childPartitionIds) {
            this.childPartitionIds.addAll(childPartitionIds);
        }
    }

    public int getDimension() {
        return dimension;
    }

    public void setDimension(int dimension) {
        this.dimension = dimension;
    }

    public boolean isRowKeyInPartition(Schema schema, Key rowKey) {
        return region.isKeyInRegion(schema, rowKey);
    }

    public boolean doesRegionOverlapPartition(Region otherRegion) {
        return region.doesRegionOverlap(otherRegion);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Partition partition = (Partition) o;

        return Objects.equals(rowKeyTypes, partition.rowKeyTypes)
                && leafPartition == partition.leafPartition
                && Objects.equals(region, partition.region)
                && Objects.equals(id, partition.id)
                && Objects.equals(parentPartitionId, partition.parentPartitionId)
                && Objects.equals(getChildPartitionIds(), partition.getChildPartitionIds())
                && dimension == partition.getDimension();
    }

    @Override
    public int hashCode() {
        return Objects.hash(rowKeyTypes, region, id, leafPartition,
                parentPartitionId, getChildPartitionIds(), dimension);
    }

    @Override
    public String toString() {
        return "Partition{"
                + "rowKeyTypes=" + rowKeyTypes
                + ", region=" + region
                + ", id='" + id + '\''
                + ", leafPartition=" + leafPartition
                + ", parentPartitionId='" + parentPartitionId + '\''
                + ", childPartitionIds=" + getChildPartitionIds()
                + ", dimension=" + dimension
                + '}';
    }

    public static final class Builder {
        private List<PrimitiveType> rowKeyTypes;
        private Region region;
        private String id;
        private boolean leafPartition;
        private String parentPartitionId;
        private List<String> childPartitionIds;
        private int dimension;

        public Builder rowKeyTypes(List<PrimitiveType> rowKeyTypes) {
            this.rowKeyTypes = rowKeyTypes;
            return this;
        }

        public Builder rowKeyTypes(PrimitiveType... rowKeyTypes) {
            return this.rowKeyTypes(Arrays.asList(rowKeyTypes));
        }

        public Builder region(Region region) {
            this.region = region;
            return this;
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder leafPartition(boolean leafPartition) {
            this.leafPartition = leafPartition;
            return this;
        }


        public Builder parentPartitionId(String parentPartitionId) {
            this.parentPartitionId = parentPartitionId;
            return this;
        }


        public Builder childPartitionIds(List<String> childPartitionIds) {
            this.childPartitionIds = childPartitionIds;
            return this;
        }

        public Builder childPartitionIds(String... childPartitionIds) {
            return this.childPartitionIds(Arrays.asList(childPartitionIds));
        }

        public Builder dimension(int dimension) {
            this.dimension = dimension;
            return this;
        }

        public List<PrimitiveType> getRowKeyTypes() {
            return rowKeyTypes;
        }

        public Region getRegion() {
            return region;
        }

        public String getId() {
            return id;
        }

        public boolean isLeafPartition() {
            return leafPartition;
        }

        public String getParentPartitionId() {
            return parentPartitionId;
        }

        public List<String> getChildPartitionIds() {
            return childPartitionIds;
        }

        public int getDimension() {
            return dimension;
        }

        public Partition build() {
            return new Partition(this);
        }
    }
}
