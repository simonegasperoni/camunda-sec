# camunda-sec
## Context
The microservice architectures consist of applications that scale independently of each other. The most important highlights are:

-   Highly maintainable and testable - enables rapid and frequent development and deployment
-   Loosely coupled with other services - enables a team to work independently the majority of the time on their service(s) without being impacted by changes to other services and without affecting other services
-   Independently deployable - enables a team to deploy their service without having to coordinate with other teams
-   Capable of being developed by a small team - essential for high productivity by avoiding the high communication head of large teams

The executions of local transactions in each application can be combined with the execution of other local transactions by other applications to provide the final result to the clients. 
The purpose of the Saga Execution Controller (SEC) is just to orchestrate all the local transactions of the distributed architecture. 
The combination of the local transactions is called Saga Transaction (ST) in the following.

With this SEC proposal the implementation must be in line with the highlights already listed before about the microservices:

 - The SEC is completely stateless so that it can scale horizontally 
 - The SEC makes possible compensation actions so that in case of failure compensation actions are triggered to guarantee data consistency across different data sources. 
 - The SEC centralizes all the logic for the orchestration to facilitate the maintainability
 - The orchestration logic is defined declaratively, to facilitate the understanding of its behavior.
 - The SEC allows two or more local transactions to run at the same time.
 - The same local transaction can be triggered several times in the context of the same Saga transaction.


