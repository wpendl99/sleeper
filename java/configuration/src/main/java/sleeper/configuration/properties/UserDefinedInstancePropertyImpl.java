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

package sleeper.configuration.properties;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

class UserDefinedInstancePropertyImpl implements UserDefinedInstanceProperty {
    private final String propertyName;
    private final String defaultValue;
    private final Predicate<String> validationPredicate;
    private final String description;
    private final PropertyGroup propertyGroup;
    private final boolean runCDKDeployWhenChanged;

    private UserDefinedInstancePropertyImpl(Builder builder) {
        propertyName = Objects.requireNonNull(builder.propertyName, "propertyName must not be null");
        defaultValue = builder.defaultValue;
        validationPredicate = Objects.requireNonNull(builder.validationPredicate, "validationPredicate must not be null");
        description = Objects.requireNonNull(builder.description, "description must not be null");
        propertyGroup = Objects.requireNonNull(builder.propertyGroup, "propertyGroup must not be null");
        runCDKDeployWhenChanged = builder.runCDKDeployWhenChanged;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder named(String name) {
        return builder().propertyName(name);
    }

    @Override
    public Predicate<String> validationPredicate() {
        return validationPredicate;
    }

    @Override
    public String getPropertyName() {
        return propertyName;
    }

    @Override
    public String getDefaultValue() {
        return defaultValue;
    }

    @Override
    public PropertyGroup getPropertyGroup() {
        return propertyGroup;
    }

    public String toString() {
        return propertyName;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public boolean isRunCDKDeployWhenChanged() {
        return runCDKDeployWhenChanged;
    }

    static final class Builder {
        private String propertyName;
        private String defaultValue;
        private Predicate<String> validationPredicate = s -> true;
        private String description;
        private PropertyGroup propertyGroup;
        private boolean runCDKDeployWhenChanged;
        private Consumer<UserDefinedInstanceProperty> addToIndex;

        private Builder() {
        }

        public Builder propertyName(String propertyName) {
            this.propertyName = propertyName;
            return this;
        }

        public Builder defaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public Builder validationPredicate(Predicate<String> validationPredicate) {
            this.validationPredicate = validationPredicate;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder propertyGroup(PropertyGroup propertyGroup) {
            this.propertyGroup = propertyGroup;
            return this;
        }

        public Builder runCDKDeployWhenChanged(boolean runCDKDeployWhenChanged) {
            this.runCDKDeployWhenChanged = runCDKDeployWhenChanged;
            return this;
        }

        public Builder addToIndex(Consumer<UserDefinedInstanceProperty> addToIndex) {
            this.addToIndex = addToIndex;
            return this;
        }

        public UserDefinedInstanceProperty build() {
            UserDefinedInstanceProperty property = new UserDefinedInstancePropertyImpl(this);
            addToIndex.accept(property);
            return property;
        }
    }
}
