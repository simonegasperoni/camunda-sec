package com.orchestrator.sec.logic;
import java.util.logging.Logger;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

public class CheckConsistency implements JavaDelegate {

	private final static Logger LOGGER = Logger.getLogger("CheckConsistency Task");

	public void execute(DelegateExecution execution) throws Exception {
		String trace_id = execution.getVariable("trace").toString();
		String activity_id = execution.getVariable("activity").toString();
		String postgresJdbc = execution.getVariable("postgresJdbc").toString();
		String postgresUsr = execution.getVariable("postgresUsr").toString();
		String postgresPwd = execution.getVariable("postgresPwd").toString();
		
		//this is an example: number of groups for the current activity must be 1
		//further conditions can be added
		if(SagalogAccess.checkGroup(postgresJdbc, postgresUsr, postgresPwd, trace_id, activity_id)>1) {
			execution.setVariable("outofsync", true);
		}
		else {
			execution.setVariable("outofsync", false);
		}
		LOGGER.info("["+trace_id+"]["+activity_id+"] outofsync="+execution.getVariable("outofsync"));
	}
}