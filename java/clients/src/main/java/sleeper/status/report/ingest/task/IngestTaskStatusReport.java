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
package sleeper.status.report.ingest.task;

import sleeper.status.report.table.TableField;
import sleeper.status.report.table.TableWriterFactory;

import java.io.PrintStream;

public class IngestTaskStatusReport {

    private static final TableWriterFactory.Builder TABLE_FACTORY_BUILDER = TableWriterFactory.builder();

    private static final TableField STATE = TABLE_FACTORY_BUILDER.addField("STATE");
    private static final TableField START_TIME = TABLE_FACTORY_BUILDER.addField("START_TIME");
    private static final TableField FINISH_TIME = TABLE_FACTORY_BUILDER.addField("FINISH_TIME");
    private static final TableField TASK_ID = TABLE_FACTORY_BUILDER.addField("TASK_ID");

    private static final TableWriterFactory TABLE_FACTORY = TABLE_FACTORY_BUILDER.build();

    private final PrintStream out;

    public IngestTaskStatusReport(PrintStream out) {
        this.out = out;
    }

    public void run(IngestTaskQuery query) {
        out.println();
        out.println("Ingest Task Status Report");
        out.println("-------------------------");
        out.printf("Total tasks: %s%n", 0);
        out.printf("Total unfinished tasks: %s%n", 0);
        out.printf("Total finished tasks: %s%n", 0);

        TABLE_FACTORY.tableBuilder()
                .build().write(out);
    }
}
