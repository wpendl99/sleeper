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
package sleeper.job.common.action;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.AmazonSQSException;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.DeleteMessageResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * An {@link Action} that deletes the message with the provided receipt handle from SQS.
 */
public class DeleteMessageAction implements Action {
    private static final long[] MILLISECONDS_TO_SLEEP = new long[]{2000L, 8000L, 16000L, 64000L};

    private final AmazonSQS sqsClient;
    private final String sqsJobQueueUrl;
    private final String messageReceiptHandle;
    private final String description;
    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteMessageAction.class);

    public DeleteMessageAction(MessageReference message) {
        sqsClient = message.getSqsClient();
        sqsJobQueueUrl = message.getSqsJobQueueUrl();
        description = message.getJobDescription();
        messageReceiptHandle = message.getReceiptHandle();
    }

    @Override
    public void call() throws ActionException {
        DeleteMessageRequest deleteMessageRequest = new DeleteMessageRequest()
                .withQueueUrl(sqsJobQueueUrl)
                .withReceiptHandle(messageReceiptHandle);
        int count = 0;
        AmazonSQSException exception = null;
        while (count < 3) {
            try {
                DeleteMessageResult result = sqsClient.deleteMessage(deleteMessageRequest);
                LOGGER.info("{}: Deleted message with receipt handle {} with result {}",
                        description, messageReceiptHandle, result);
                return;
            } catch (AmazonSQSException e) {
                count++;
                exception = e;
                String stackTrace = Arrays
                        .stream(exception.getStackTrace())
                        .map(StackTraceElement::toString)
                        .collect(Collectors.joining("\n"));
                LOGGER.info("{}: AmazonSQSException deleting message with receipt handle {} (Exception message {}, stacktrace {})",
                        description, messageReceiptHandle, exception.getMessage(), stackTrace);
                try {
                    Thread.sleep(MILLISECONDS_TO_SLEEP[count - 1]);
                } catch (InterruptedException interruptedException) {
                    // Do nothing
                }
            }
        }
        throw new ActionException(description + ": AmazonSQSException deleting message", exception);
    }
}
