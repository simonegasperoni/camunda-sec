
# Saga execution controller
## Context
The microservice architectures consist of applications that scale independently of each other. The applications must be (https://microservices.io/patterns/microservices.html):

-   *Highly maintainable and testable - enables rapid and frequent development and deployment*
-   *Loosely coupled with other services - enables a team to work independently the majority of the time on their service(s) without being impacted by changes to other services and without affecting other services*
-   *Independently deployable - enables a team to deploy their service without having to coordinate with other teams*
-   *Capable of being developed by a small team - essential for high productivity by avoiding the high communication head of large teams*

The executions of local transactions in each application can be combined with the execution of other local transactions by other applications to provide the clients with specific business results. 
The purpose of the Saga Execution Controller (SEC) is just to orchestrate all the local transactions of the distributed architecture. 
The combination of the local transactions (business activities) is called Saga Transaction (ST).
The following SEC proposal is in line with the principles of the microservices architectural pattern already listed on the top.
The most important highlights of this SEC proposal are:

 1. The SEC is completely stateless so that it can scale horizontally.
 2. The SEC makes possible compensation actions. In case of failure compensation actions are triggered to guarantee data consistency across different data sources. 
 3. The SEC centralizes all the logic for the orchestration to facilitate the maintainability
 4. The flow components are built-in in a workflow engine, and the most relevant parts of the orchestration logic are declaratively defined and extremely modular. It facilitates the maintainability of the SEC for all the stakeholders. 
 5. **The SEC allows multiple local transactions to run at the same time.**
 6. **The same local transaction can be triggered several times in the context of the same Saga transaction.**
## Communication layer
The communication between business microservices and the SEC is based on an exchange of messages. Messages in the input of the SEC must be processed to establish what is the next action to trigger. The SEC triggers the business microservices via messages too. The messages are exchanged through a message broker (in this proposal the message broker is Rabbitmq).
Each kind of business activity (local transaction) performed by a business microservice needs two queues, one for triggering the start of the business activity and one to send the result to the SEC. For example, if the business activity is `BusinessDataValidation` the queues used by the SEC are:
 1. `BusinessDataValidationBegin` is used to notify that the business service must start the local transaction `BusinessDataValidation`.
 2. `BusinessDataValidationEnd` is used to notify the SEC that the local transaction is completed.
The business microservice must use these queues accordingly.

The structure of the messages is the following:

> <trace, activity, compensation, group, result>
*note: the name of the activity is implicit in the name of the queue.* 

 - **trace**: it is the identifier of the ST.
 - **activity**: it is the identifier of the group of local transactions that are supposed to be performed at the same time.
 - **group**: it is the name of a group of kinds of activities that are supposed to be performed at the same time. In other words, a group is a set of messages to deliver at the same time to trigger different activities.
 - **result**: it is the result of the local transaction. The possible values are "Waiting" (in the case the local transaction has been triggered), "Fail" (in the case the local transaction is failed), and "Success" ("in the case the local transaction has correctly finished").
 - **compensation**: it is a boolean that notifies that the ST ended up in the compensation chain, indeed, after a failure the value for each message of the ST must be `true`

An example follows:
| trace-id | activity-id | compensation | group | result |
|--|--|--|--|--|
| 8f6htyo6-1a5f-4ctt-ar61-52683ef123er | 90b8658a-1a5f-4caa-ae62-52683ef4c6cc | false | Validation | Success |  
  
## Business layer
![enter image description here](https://raw.githubusercontent.com/simonegasperoni/camunda-sec/master/img/sec.png)
The workflow in the figure summarizes the business logic of SEC.
Although the BPMN2 model is self-explanatory, below there is a brief description of the SEC's operations:

 1. **Store input message in the Sagalog**: As soon a message is received in an instance of SEC, it must be saved in the Sagalog (which is a sort of history database of all the relevant events)
 2. **Retrieve the state in the Sagalog**: After writing the input message in the Sagalog, the current state of the ST must be detected. The purpose of this operation is to understand whether all the messages of the group have been received and, in case, SEC must trigger the next group of messages to send.
 3.  **Check consistency in the Sagalog**: Here some checks can be performed to assess the consistency of the Sagalog's records for the current ST. In case the received message introduces some kind of inconsistency an error/exception must be thrown.
 4. **Saga Diagram**: This is a DMN table with the following structure *<input: group, input: saga state, output: new group>*. The values for *saga state* can be: *success*, *fail*, and *abort*. The current state for the group is detected by the operations already described at point 2.
 5. **Messages Detection**: This is a DMN table with the following structure *<input: group, output: messages>* The input is the name of the new group just detected in the Saga Diagram, and the output is the list of messages to send to trigger the new group of activities. 
 6. **Store new state in the Sagalog**: Here SEC stores the new state in the Sagalog and sends the messages of the new group of activities through the *Begin* queues.

## Storage layer
Basically, the messages that arrive at SEC are stored in the Sagalog table along with a time stamp and the name of the input queue.

## Technological stack
### Communication layer: RabbitMQ
In this SEC proposal, the message broker is RabbitMQ. RabbitMQ is easy to deploy on-premises and in the cloud. It supports multiple messaging protocols. RabbitMQ can be deployed in distributed and federated configurations to meet high-scale, high-availability requirements.
### Business layer: Camunda
The model of the SEC is executed in the Camunda workflow engine. In this way, all the logic implemented by the SEC is declaratively defined as well as the Saga diagram. It facilitates the maintainability of the software and the configurability. 
### Storage layer: Postgres
In this SEC proposal the Sagalog is stored in postgres in the following table:

    CREATE TABLE public.sagalog (
        trace_id text NOT NULL,
        state text NOT NULL,
        time_stamp timestamp NOT NULL,
        compensation bool NOT NULL,
        activity_id text NOT NULL,
        group_id text NOT NULL,
        result text NOT NULL,
        CONSTRAINT sagalog_pk 
           PRIMARY KEY (trace_id, activity_id, state, result)
    );

### Artifacts and configuration files 
##### application.yaml
Here there are the Camunda's properties.
##### application.properties
Here there are the rest of the properties.
##### saga_diagram.dmn
This is the DMN table of the Saga Diagram.
##### groups.dmn
This is the DMN table that defines the composition of the groups.
##### sec.bpmn
This is the BPMN2 model of SEC.
