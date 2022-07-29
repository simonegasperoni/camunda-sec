package com.orchestrator.sec.app;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.nio.charset.StandardCharsets;

public class Sender {

    private final static String QUEUE_NAME = "SPARQLService/Saga/SEC-Ingestion/SnapshotSPARQLResponseChannel";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            //<msg, trace_id, activity_id, params>
            String message = "a09ab758-001e-4bec-8ff7-f69090d11234 8cb76935-56af-47e8-8dcc-27832a06c5fb False OtherSnapshots Success";
            channel.basicPublish("", QUEUE_NAME, null, message.getBytes(StandardCharsets.UTF_8));
            System.out.println(" [x] Sent '" + message + "'");
        }
    }
}