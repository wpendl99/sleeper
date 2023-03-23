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

package sleeper.clients.admin;

import org.junit.jupiter.api.Test;

import sleeper.clients.admin.testutils.AdminClientITBase;
import sleeper.configuration.properties.InstanceProperties;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static sleeper.clients.admin.testutils.ExpectedAdminConsoleValues.PROMPT_RETURN_TO_MAIN;
import static sleeper.clients.admin.testutils.ExpectedAdminConsoleValues.TABLE_SELECT_SCREEN;
import static sleeper.configuration.properties.table.TableProperty.TABLE_NAME;
import static sleeper.console.ConsoleOutput.CLEAR_CONSOLE;

class TableSelectHelperIT extends AdminClientITBase {
    @Test
    void shouldReturnToMenuIfInstanceDoesNotExist() {
        // Given
        in.enterNextPrompts("test-table");

        // When
        String output = runTableSelectHelperGetOutput();

        // Then
        assertThat(output)
                .startsWith(CLEAR_CONSOLE + TABLE_SELECT_SCREEN + "\n" +
                        "Could not load properties for instance test-instance\n" +
                        "Cause: The specified key does not exist")
                .contains("Amazon S3")
                .endsWith(PROMPT_RETURN_TO_MAIN);
    }

    @Test
    void shouldReturnToMenuIfTableDoesNotExist() throws IOException {
        // Given
        InstanceProperties instanceProperties = createValidInstanceProperties();
        instanceProperties.saveToS3(s3);
        in.enterNextPrompts("test-table");

        // When
        String output = runTableSelectHelperGetOutput();

        // Then
        assertThat(output)
                .startsWith(CLEAR_CONSOLE + TABLE_SELECT_SCREEN + "\n" +
                        "Could not load properties for table test-table in instance test-instance\n" +
                        "Cause: The specified key does not exist")
                .contains("Amazon S3")
                .endsWith(PROMPT_RETURN_TO_MAIN);
    }

    private String runTableSelectHelperGetOutput() {
        new TableSelectHelper(out.consoleOut(), in.consoleIn(), store())
                .chooseTableIfExistsThen(INSTANCE_ID, tableProperties ->
                        out.consoleOut().println("\n" +
                                "Found table " + tableProperties.get(TABLE_NAME)));
        return out.toString();
    }
}
