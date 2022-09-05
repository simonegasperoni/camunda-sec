package com.orchestrator.sec.logic;
import java.util.logging.Logger;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

public class StoreInputMessage implements JavaDelegate {

	private final static Logger LOGGER = Logger.getLogger("Store Input Message Task");

	public void execute(DelegateExecution execution) throws Exception {
		//set global variables
		String msg = execution.getVariable("msg").toString();
		String trace_id = execution.getVariable("trace").toString();
		String activity_id = execution.getVariable("activity").toString();
		String result = execution.getVariable("result").toString();
		boolean retriable= Boolean.parseBoolean(execution.getVariable("retriable").toString());
		String group = execution.getVariable("group").toString();
		String postgresJdbc = execution.getVariable("postgresJdbc").toString();
		String postgresUsr = execution.getVariable("postgresUsr").toString();
		String postgresPwd = execution.getVariable("postgresPwd").toString();
		execution.setVariable("abort", new Boolean(false));
		execution.setVariable("retry", new Boolean(false));
		
		//write record
		if(msg.equals("Abort")) {
			SagalogAccess.writeRecord(postgresJdbc, postgresUsr, postgresPwd, trace_id, msg, retriable, activity_id, group, "Waiting");
		}
		else {
			if(msg.equals("InitializerService/Saga/SEC-Ingestion/Startup")) {
				SagalogAccess.writeRecord(postgresJdbc, postgresUsr, postgresPwd, trace_id, msg, retriable, activity_id, group, "Waiting");
				SagalogAccess.writeRecord(postgresJdbc, postgresUsr, postgresPwd, trace_id, msg, retriable, activity_id, group, "Success");
			}
			else {
				if(retriable&&result.equals("Fail")) {
					SagalogAccess.writeRecord(postgresJdbc, postgresUsr, postgresPwd, trace_id, msg, retriable, activity_id, group, "Fail + new attempt");
					execution.setVariable("retry", new Boolean(true));
				}
				else SagalogAccess.writeRecord(postgresJdbc, postgresUsr, postgresPwd, trace_id, msg, retriable, activity_id, group, result);
			}
		}
		//set abort
		if(msg.equals("Abort")) {
			execution.setVariable("abort", new Boolean(true));
		}
		

		//logs
		LOGGER.info("["+trace_id+"]["+activity_id+"] msg: "+msg+", retriable: "+retriable+", result: "+result+", group: "+group);


	}
}