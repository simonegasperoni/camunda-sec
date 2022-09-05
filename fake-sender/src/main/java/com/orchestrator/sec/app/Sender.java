package com.orchestrator.sec.app;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.nio.charset.StandardCharsets;

public class Sender {

    private final static String QUEUE_NAME = "ValidationService/Saga/SEC-Ingestion/StructuralValidationResponseChannel";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            //<msg, trace_id, activity_id, params>
            String message = "a09ab758-001e-4bec-8ff7-f69090d11234 bf4cf604-38ea-4dfa-829b-52f7fb85d603 True StructuralValidation Success";
            channel.basicPublish("", QUEUE_NAME, null, message.getBytes(StandardCharsets.UTF_8));
            System.out.println(" [x] Sent '" + message + "'");
        }
    }
}