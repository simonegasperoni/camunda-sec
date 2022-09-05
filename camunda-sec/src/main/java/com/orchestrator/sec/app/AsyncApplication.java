package com.orchestrator.sec.app;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngines;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

@SpringBootApplication
public class AsyncApplication {
	private final static Logger LOGGER = Logger.getLogger("SAGA EXECUTION CONTROLLER");
	private static String XPATH = "//*/rule/outputEntry[1]/text";
	private static String DMNTABLE= "src/main/resources/groups.dmn";

	//detection of the names for the queues by means of xpath on the dmn that describes the groups
	private static List<String> detectListOfQueuesByDmn() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException{
		LOGGER.info("Looking at the DMN tables to retrieve the queues to instantiate...");
		List<String> result = new ArrayList<String>();
		FileInputStream fileIS = new FileInputStream(DMNTABLE);
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = builderFactory.newDocumentBuilder();
		Document xmlDocument = builder.parse(fileIS);
		XPath xPath = XPathFactory.newInstance().newXPath();
		String expression = XPATH;
		NodeList nl = (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);
		for(int i=0; i<nl.getLength(); i++) {
			Element elem = (Element)nl.item(i);
			String newQueue = elem.getTextContent().replace("\"", "");
			result.add(newQueue);
		}
		return result;
	} 

	//setting and execution of camunda workflow engine
	public static void startCamundaInstance(ProcessEngine processEngine, Map<String, Object> mapVars) {
		LOGGER.info("["+mapVars.get("trace")+"]["+mapVars.get("activity")+"] Executing the workflow engine fot the transaction: "+mapVars.get("trace"));
		processEngine.getRuntimeService().startProcessInstanceByMessage("start", mapVars);
		LOGGER.info("["+mapVars.get("trace")+"]["+mapVars.get("activity")+"] Finished");
	}

	public static void main(String[] args) throws IOException, TimeoutException, ParserConfigurationException, SAXException, XPathExpressionException {
		SpringApplication.run(AsyncApplication.class);
		ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(Configs.RABBITMQ_INPUT);
		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();

		//Callback Rabbitmq
		LOGGER.info("Defining Rabbitmq deliverCallback...");
		DeliverCallback deliverCallback = (consumerTag, delivery) -> {
			String corpus = new String(delivery.getBody(), "UTF-8");
			Map<String, Object> mapVars=new HashMap<String, Object>();
			String[] corp = corpus.split(" ");
			String queueName = delivery.getEnvelope().getRoutingKey();

			if(queueName.equals("SEC-Ingestion/Task/AbortResponseChannel")) {
				mapVars.put("msg", "Abort");
			}
			else {
				mapVars.put("msg", queueName.replace("ResponseChannel",""));
			}
			
			mapVars.put("trace",corp[0]);
			mapVars.put("activity",corp[1]);
			mapVars.put("retriable",corp[2]);
			mapVars.put("group", corp[3]);
			mapVars.put("result", corp[4]);
			mapVars.put("postgresJdbc", Configs.POSTGRES_JDBC);
			mapVars.put("postgresUsr", Configs.POSTGRES_USR);
			mapVars.put("postgresPwd", Configs.POSTGRES_PWD);
			mapVars.put("rabbitmqInput", Configs.RABBITMQ_INPUT);
			mapVars.put("rabbitmqOutput", Configs.RABBITMQ_OUTPUT);

			LOGGER.info("["+mapVars.get("trace")+"]["+mapVars.get("activity")+"] Rabbitmq: Message received in queue "+queueName);
			LOGGER.info("["+mapVars.get("trace")+"]["+mapVars.get("activity")+"] Rabbitmq: Message received '"+corpus+"'");
			try {
				startCamundaInstance(processEngine, mapVars);
			} catch (Exception e ) { e.printStackTrace(); }
		};

		//Queues declaration
		channel.queueDeclare("Abort", false, false, false, null);
		channel.basicConsume("Abort", true, deliverCallback, consumerTag -> { });
		LOGGER.info("new queue: Abort");
		List<String> queues=detectListOfQueuesByDmn();
		for(String queue:queues) {
			channel.queueDeclare(queue+"RequestChannel", false, false, false, null);
			channel.queueDeclare(queue+"ResponseChannel", false, false, false, null);
			channel.basicConsume(queue+"ResponseChannel", true, deliverCallback, consumerTag -> { });
			LOGGER.info("new queue: "+queue+"RequestChannel");
			LOGGER.info("new queue: "+queue+"ResponseChannel");			

		}
		LOGGER.info("The orchestrator is now ready and it is waiting for messages from the already listed queues");
	}
}

@Component
class Configs {

	public static String POSTGRES_JDBC;
	public static String POSTGRES_PWD;
	public static String POSTGRES_USR;
	public static String RABBITMQ_INPUT;
	public static String RABBITMQ_OUTPUT;

	@Value("${postgres.jdbc}")
	public void setPostgresJdbc(String jdbc) {
		POSTGRES_JDBC= jdbc;
	}

	@Value("${postgres.pwd}")
	public void setPostgresPwd(String pwd) {
		POSTGRES_PWD= pwd;
	}

	@Value("${postgres.usr}")
	public void setPostgresUsr(String usr) {
		POSTGRES_USR= usr;
	}

	@Value("${rabbitmq.input}")
	public void setRabbitmqInput(String rabbitmq) {
		RABBITMQ_INPUT= rabbitmq;
	}

	@Value("${rabbitmq.output}")
	public void setRabbitmqOutput(String rabbitmq) {
		RABBITMQ_OUTPUT= rabbitmq;
	}

}