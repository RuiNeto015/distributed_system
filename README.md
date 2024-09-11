## Railway Network Notification System :station:

Project developed within the scope of the curricular unit of distributed systems.

### About the project :wave:
#### Domain
This project consists on a java application designed to simulate a notification 
system that notifies passengers about schedules changes.

The following events will trigger notifications for the respective clients:

| Event                      | Grade | Incidence         |   
|----------------------------|-------|-------------------|
| Schedule change            | 1     | Railway           |   
| Multiple schedule changes  | 2     | Group of railways |   
| Railway network suspension | 3     | Network           |   

#### Protocol
A communication protocol was designed to achieve the final result of the project.
There are 3 main entities when it comes to the communication:
- there is the **central node** which is the main server that holds a java api 
instance.
- there are **local nodes** that establish a bridge between the **central node**
and **clients**. This entity acts as a "interface" for the **central node** so 
that **clients** can be distributed by all the **local nodes**.
- and finally there are **clients** which is an entity that can connect to a local
node.

To make this protocol generic as possible, for the socket communication between 
client, local node and central node is used a object called "packet" that holds
generic data. 
There are two types of packets:
- **send packet** which is a packet that has a method signature (method that central node
will execute with the api instance) and optionally it can hold data (the method args). 
This packet will be sent from the client to the local node (that he 
is connected) and finally sent to the central node. 

- **response packet** which is the packet that the central node will send back with the
results of the operations executed. This packet can be unicasted, multicasted or 
broadcasted depending on the operation.
 
### Developers :computer:
- Rui Neto
- Simão Santos
- Victor Lopes
