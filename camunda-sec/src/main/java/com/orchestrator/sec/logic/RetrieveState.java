package com.orchestrator.sec.logic;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

public class RetrieveState implements JavaDelegate {

	private final static Logger LOGGER = Logger.getLogger("Retrieve State Task");

	public void execute(DelegateExecution execution) throws Exception {
		String trace_id = execution.getVariable("trace").toString();
		String activity_id = execution.getVariable("activity").toString();
		String postgresJdbc = execution.getVariable("postgresJdbc").toString();
		String postgresUsr = execution.getVariable("postgresUsr").toString();
		String postgresPwd = execution.getVariable("postgresPwd").toString();
		
		Set<String> lwaiting = (SagalogAccess.retrieveStates(postgresJdbc, postgresUsr, postgresPwd, trace_id, activity_id,"Waiting"));
		Set<String> lsuccess = (SagalogAccess.retrieveStates(postgresJdbc, postgresUsr, postgresPwd, trace_id, activity_id,"Success"));
		Set<String> lfail = (SagalogAccess.retrieveStates(postgresJdbc, postgresUsr, postgresPwd, trace_id, activity_id,"Fail"));
		Set<String> expected = new HashSet<String>();
		Set<String> received = new HashSet<String>();
		expected.addAll(lwaiting);
		received.addAll(lsuccess);
		received.addAll(lfail);

		if(expected.equals(received)) {
			execution.setVariable("complete", true);
			if(SagalogAccess.transactionAborted(postgresJdbc, postgresUsr, postgresPwd, trace_id)) {
				execution.setVariable("sagaState", "Abort");
				execution.setVariable("retriable", true);
			}
			else {
				if(!lfail.isEmpty()) {
					execution.setVariable("sagaState", "Fail");
				}
				else {
					execution.setVariable("sagaState", "Success");
				}
			}
		}
		else {
			execution.setVariable("complete", false);
		}
		LOGGER.info("["+trace_id+"]["+activity_id+"] complete="+execution.getVariable("complete"));
	}
}

