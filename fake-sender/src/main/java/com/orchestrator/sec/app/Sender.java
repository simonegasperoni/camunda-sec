package com.orchestrator.sec.app;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.nio.charset.StandardCharsets;

public class Sender {
	
	
//	APIGateway/Saga/SEC-Ingestion/RollbackResponseChannel           a09ab758-001e-4bec-8ff7-f69090d11234 dbc7737a-45ae-4cd4-9b41-02f030392ff7 true Rollbacks Start
//	RDFService/Saga/SEC-Ingestion/RollbackResponseChannel           a09ab758-001e-4bec-8ff7-f69090d11234 dbc7737a-45ae-4cd4-9b41-02f030392ff7 true Rollbacks Start
//	IndexationService/Saga/SEC-Ingestion/RollbackResponseChannel    a09ab758-001e-4bec-8ff7-f69090d11234 dbc7737a-45ae-4cd4-9b41-02f030392ff7 true Rollbacks Start
//	ContentService/Saga/SEC-Ingestion/RollbackResponseChannel       a09ab758-001e-4bec-8ff7-f69090d11234 dbc7737a-45ae-4cd4-9b41-02f030392ff7 true Rollbacks Start
//	SPARQLService/Saga/SEC-Ingestion/RollbackResponseChannel        a09ab758-001e-4bec-8ff7-f69090d11234 dbc7737a-45ae-4cd4-9b41-02f030392ff7 true Rollbacks Start
//	ArtifactService/Saga/SEC-Ingestion/RollbackResponseChannel      a09ab758-001e-4bec-8ff7-f69090d11234 dbc7737a-45ae-4cd4-9b41-02f030392ff7 true Rollbacks Start

    private final static String QUEUE_NAME = "ArtifactService/Saga/SEC-Ingestion/RollbackResponseChannel";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            //<msg, trace_id, activity_id, params>
            String message = "a09ab758-001e-4bec-8ff7-f69090d11234 dbc7737a-45ae-4cd4-9b41-02f030392ff7 True Rollbacks Success";
            channel.basicPublish("", QUEUE_NAME, null, message.getBytes(StandardCharsets.UTF_8));
            System.out.println(" [x] Sent '" + message + "'");
        }
    }
}