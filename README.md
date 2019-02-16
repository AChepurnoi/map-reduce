## Map reduce implementation
The goal of this implementation is to build a simplified map-reduce implementation
### How to run
For running the example you need to have a jar with `Mapper` and `Reducer` (Either use/modify the provided module or build your own)
### Running the app
* `git clone ` repository
* `./gradlew build`
* `docker-compose up --build`

To start map-reduce job send `USR2` signal to the master container (`docker kill --signal="USR2" $CONTAINER_ID` or `kill -USR2 $PID`)

### Customization
* Custom `Reducer` and `Mapper` could be implemented -- use `example-app` as an example. 
A class name could also be changed in `docker-compose.yml`
* Data customization is possible in `docker-compose.yml` file. 
The format of data is `KEY:VALUE|KEY:VALUE` where `:` is delimiter between key and value and `|` is delimiter between different key-value pairs
* Other env variables can be found in `Configuration.kt` source files

### Scope of the project
* Basic master-slave communication
* Arbitrary code loading and execution at runtime 
* Mapping code delivery and execution
* Collecting and reducing results on the master

### Out of the scope
* Handling cluster splitting and master downtime
* Handling slave outages (Complex cases)
* Handling network delivery issues
* Repartitioning


### Implementation
#### `core` module
This module contains shared code between master and slave 
such as messages classes for communication, code loader class, and interfaces.

Also, this module contains `Mapper` and `Reducer` interfaces that should be implemented by any other map-reduce application that will be executed using this demo.
#### `example-app` module 
This is an example of a map-reduce application that can be executed. 
Any application should implement `Mapper` and `Reducer` 

#### `master` module
Master implemented using kotlin. 
Each slave registered on new connection accept and do the following:
* Sending heartbeat messages to verify slave is connected and listening
* Listening for slave messages
* Mapping

Also, the master is monitoring dead slaves and clean the registry.

Handling complex slave outages are out of the scope, but slaves can easily register/unregister on running master and 
this shouldn't affect the application

#### `slave` module
Connects to the master and listens to messages. Responds to the pings and do mapping over the in-memory map. 
