package com.orchestrator.sec.logic;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.nio.charset.StandardCharsets;

public class StoreNewState  implements JavaDelegate {
	private final static Logger LOGGER = Logger.getLogger("Store new state Task");

	public void execute(DelegateExecution execution) throws Exception {
		//set global variables
		String trace_id = execution.getVariable("trace").toString();
		boolean compensation = Boolean.parseBoolean(execution.getVariable("compensation").toString());
		String group = execution.getVariable("group").toString();
		String sagaState = execution.getVariable("sagaState").toString();
		
		String postgresJdbc = execution.getVariable("postgresJdbc").toString();
		String postgresUsr = execution.getVariable("postgresUsr").toString();
		String postgresPwd = execution.getVariable("postgresPwd").toString();
		String rabbitmqOutput = execution.getVariable("rabbitmqOutput").toString();

		//storing new state
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> messages = (List<Map<String, Object>>) execution.getVariable("messages");

		//generate new activity id
		String newActivityID = UUID.randomUUID().toString();

		if(sagaState.contentEquals("Abort")) {
			SagalogAccess.processAbort(postgresJdbc, postgresUsr, postgresPwd, trace_id);
		}

		for(Map<String,Object> result:messages) {
			String m=(String) result.get("messages");
			if(!m.contentEquals("end")||(!sagaState.equals("Fail")&&(m.contentEquals("end")))){ 
				SagalogAccess.writeRecord(postgresJdbc, postgresUsr, postgresPwd, trace_id, m, compensation, newActivityID, group, "Waiting");
			}
		}

		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(rabbitmqOutput);

		//TODO:sending new messages
		for(Map<String,Object> result:messages) {
			String m=(String) result.get("messages");
			if(!m.contentEquals("end")) {

				try (Connection connection = factory.newConnection();
						Channel channel = connection.createChannel()) {
					channel.queueDeclare(m+"Begin", false, false, false, null);
					String body=trace_id+" "+newActivityID+" "+compensation+" "+group+" start";
					channel.basicPublish("", m+"Begin", null, body.getBytes(StandardCharsets.UTF_8));
					LOGGER.info("["+trace_id+"] SENDING MESSAGE on "+m+"Begin via Rabitmq (message broker: "+rabbitmqOutput+"): "+body);
				}
			}
		}
	}
}