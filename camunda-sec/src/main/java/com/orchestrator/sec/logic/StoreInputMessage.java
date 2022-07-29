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
		boolean compensation= Boolean.parseBoolean(execution.getVariable("compensation").toString());
		String group = execution.getVariable("group").toString();
		String postgresJdbc = execution.getVariable("postgresJdbc").toString();
		String postgresUsr = execution.getVariable("postgresUsr").toString();
		String postgresPwd = execution.getVariable("postgresPwd").toString();

		//write record
		if(msg.equals("Abort")) {
			SagalogAccess.writeRecord(postgresJdbc, postgresUsr, postgresPwd, trace_id, msg, compensation, activity_id, group, "Waiting");
		}
		else {
			if(msg.equals("InitializerService/Saga/SEC-Ingestion/Startup")) {
				SagalogAccess.writeRecord(postgresJdbc, postgresUsr, postgresPwd, trace_id, msg, compensation, activity_id, group, "Waiting");
				SagalogAccess.writeRecord(postgresJdbc, postgresUsr, postgresPwd, trace_id, msg, compensation, activity_id, group, "Success");
			}
			else {
				SagalogAccess.writeRecord(postgresJdbc, postgresUsr, postgresPwd, trace_id, msg, compensation, activity_id, group, result);
			}
		}
		//set abort
		boolean abort=new Boolean(false);
		execution.setVariable("abort", abort);
		if(msg.equals("Abort")) {
			abort=new Boolean(true);
		}
		execution.setVariable("abort", abort);

		//logs
		LOGGER.info("["+trace_id+"]["+activity_id+"] msg: "+msg+", compensation: "+compensation+", +abort: "+abort+", result: "+result+", group: "+group);


	}
}