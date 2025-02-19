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
package sleeper.splitter;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.apache.hadoop.conf.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sleeper.configuration.properties.instance.InstanceProperties;
import sleeper.configuration.properties.table.TableProperties;
import sleeper.configuration.properties.table.TablePropertiesProvider;
import sleeper.statestore.StateStore;
import sleeper.statestore.StateStoreException;
import sleeper.statestore.StateStoreProvider;
import sleeper.utils.HadoopConfigurationProvider;

import java.io.IOException;

import static sleeper.configuration.properties.instance.SystemDefinedInstanceProperty.CONFIG_BUCKET;

/**
 * Triggered by an SQS event containing a {@link SplitPartitionJobDefinition}
 * job to do.
 */
public class SplitPartitionLambda implements RequestHandler<SQSEvent, Void> {
    private final Configuration conf;
    private static final Logger LOGGER = LoggerFactory.getLogger(SplitPartitionLambda.class);
    private final StateStoreProvider stateStoreProvider;
    private final TablePropertiesProvider tablePropertiesProvider;

    public SplitPartitionLambda() throws IOException {
        String s3Bucket = System.getenv(CONFIG_BUCKET.toEnvironmentVariable());
        if (null == s3Bucket) {
            throw new RuntimeException("Couldn't get S3 bucket from environment variable");
        }
        AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();
        InstanceProperties instanceProperties = new InstanceProperties();
        instanceProperties.loadFromS3(s3Client, s3Bucket);

        AmazonDynamoDB dynamoDBClient = AmazonDynamoDBClientBuilder.defaultClient();
        this.conf = HadoopConfigurationProvider.getConfigurationForLambdas(instanceProperties);
        this.tablePropertiesProvider = new TablePropertiesProvider(s3Client, instanceProperties);
        this.stateStoreProvider = new StateStoreProvider(dynamoDBClient, instanceProperties, conf);
    }

    @Override
    public Void handleRequest(SQSEvent event, Context context) {
        try {
            for (SQSEvent.SQSMessage message : event.getRecords()) {
                String serialisedJob = message.getBody();
                SplitPartitionJobDefinition job = new SplitPartitionJobDefinitionSerDe(tablePropertiesProvider)
                        .fromJson(serialisedJob);
                LOGGER.info("Received partition splitting job {}", job);
                TableProperties tableProperties = tablePropertiesProvider.getTableProperties(job.getTableName());
                StateStore stateStore = stateStoreProvider.getStateStore(tableProperties);
                SplitPartition splitPartition = new SplitPartition(stateStore, tableProperties.getSchema(), conf);
                splitPartition.splitPartition(job.getPartition(), job.getFileNames());
            }
        } catch (IOException | StateStoreException ex) {
            LOGGER.error("Exception handling partition splitting job", ex);
        }
        return null;
    }
}
