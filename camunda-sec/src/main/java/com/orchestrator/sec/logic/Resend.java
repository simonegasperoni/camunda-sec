package com.orchestrator.sec.logic;
import java.util.logging.Logger;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.nio.charset.StandardCharsets;

public class Resend  implements JavaDelegate {
	private final static Logger LOGGER = Logger.getLogger("Store new state Task");

	public void execute(DelegateExecution execution) throws Exception {
		//set global variables
		String msg = execution.getVariable("msg").toString();
		String trace_id = execution.getVariable("trace").toString();
		String activity_id = execution.getVariable("activity").toString();
		String group = execution.getVariable("group").toString();
		String rabbitmqOutput = execution.getVariable("rabbitmqOutput").toString();

		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(rabbitmqOutput);

		try (Connection connection = factory.newConnection();
				Channel channel = connection.createChannel()) {
			String body=trace_id+" "+activity_id+" true "+group+" Start";
			channel.basicPublish("", msg+"RequestChannel", null, body.getBytes(StandardCharsets.UTF_8));
			LOGGER.info("["+trace_id+"] SENDING MESSAGE on "+msg+"RequestChannel via Rabitmq (message broker: "+rabbitmqOutput+"): "+body);
		}
	}
}