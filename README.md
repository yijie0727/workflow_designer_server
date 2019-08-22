# INCF Workflow Designer Server

![Google Summer of Code img](https://4.bp.blogspot.com/-AY7eIsmbH0Y/WLRdpe78DJI/AAAAAAAABDU/lsb2XqcmyUsLqYo6yzo9HYMY4vLn3q_OgCLcB/s1600/vertical%2BGSoC%2Blogo.jpg)

## Overview
- This project aims at building an easy to use graphical interface that can streamline the configuration of the parameters
controlling individual processing sub-routines and thus make it easy to design complicated data flows and execute them.  
Workflows are designed using individual component blocks that have completely configurable inputs, outputs and 
properties. The Blocks can be combined and rearranged at runtime without making any modification to code. 

- Efforts are also targeted to make the tool user friendly and enable easy deployment of workflows on distributed 
computing frameworks.

 - This project has been developed as a part of Google Summer of Code 2018 by [Joey Pinto](https://github.com/pintojoey) 
 and Google Summer of Code 2019 by [Yijie Huang](https://github.com/yijie0727) under the mentorship of the International 
 Neuroinformatics Coordinating Facility, Sweden.

 - In GSoC2018 phase, this project was mainly focused on the development of web-based GUI and configurable workflow 
 blocks to deal with the cumulative data, all the blocks in one workflow were executed one by one by checking the dependency 
 among the blocks repeatedly. Worked based on the [GSoC2018 summary](http://www.zedacross.com/gsoc2018/), the 
 [GSoC2019](https://gist.github.com/yijie0727/b2b9d2964d2b81fd682398db330c161f) aims at modifying the existing workflow 
 designer to enable also running of continuous workflows(the continuous stream processed in the blocks in a workflow 
 without waiting the previous blocks finishing execution). 
 
 - Since in GSoC2019, most work is done in the logical part to rearrange and refactor the workflow execution mode to 
 deal with the continuous data and improve the efficiency in the [INCF Workflow Designer](https://github.com/NEUROINFORMATICS-GROUP-FAV-KIV-ZCU/workflow_designer),
 the webServer is only changed by modifying the internal methods connecting with the INCF Workflow Designer, and by 
 adding some functions like templates in navigation, so the webServer configuration and user manual is the same as that in the 
 [GSoC2018](http://www.zedacross.com/gsoc2018/workflow-designer-server-user-manual).

- This repository hosts the webServer which runs the GUI utility to design workflows by combining and arranging blocks.
The webServer also posses the functionality to execute tasks and report errors and logs. 
It also hosts an access control and scheduling system. The project also hosts a remote file upload interface.
For users who login, they can store and fetch their own templates(no results) and workflows(with results) under 
their account directory in the templates navigation.


## Contribution Guidelines

### Code Structure

The project is a Maven project and was developed in IntellijIDEA and has a .iml file that can be used to imported. For 
development purposes you may need to run 'mvn install' to make the library available to other projects needing this. Or
you can simply package the project by running 'mvn package' and adding the generated JAR to your classpath.

#### cz.zcu.kiv.server root package 
It hosts all the Java classes important to the web server:

- 1.The EmbeddedServer contains the JettyServer implementation and hosts the API server and static resources at the 
configured URL

- 2.The Servlet.java hosts the servlet wrapper for Elfinder

- 3.UserAccounts.java hosts the user accounts related APIs

- 4)Workflow.java hosts the APIs relevant to execution, scheduling and tracking of jobs

##### - cz.zcu.kiv.server.scheduler package 
It hosts the classes dealing with the scheduling and management of tasks: 

- Manager.java hosts a singleton that deals with job tracking. The execution methodology currently followed is threads are
created for incoming jobs upto they reach server.maxthreads. After that new threads are allocated only when a thread 
terminates.

##### - cz.zcu.kiv.server.sqlite package 
It hosts the models and database connector for the project:

- SQLiteDB.java is a database instance singleton. Models exist for jobs, users and modules.

##### - cz.zcu.kiv.server.utilities.config package 
It hosts the Properties configuration getter methods.

- The cz.zcu.kiv.server.utilities.elfinder package hosts the Elfinder file system accessors. The Elfinder implementation 
gives a great way to host your own filesystem implementation in the localfs folder. To create your own type of volume
implement all methods of the DefaultFsService as required by you. A browser for HDFS folder has been implmented in 
the current project. Efforts towards enabling file upload and download are more than welcome.
Make sure you mount your filesystem in servlet.ConnectorServlet

- The cz.zcu.kiv.server.utilities.email package contains the Email client and template which you can modify as you wish. 

#### src/main/webapp 
It consists of all the static files available as resources on the webServer.

- The index.html file hosts the GUI as a one page implementation. All popups are implemented as BootStrap modals.
Alerts are shown using Alertify.js library.

- The main.js file contains the primary javascript/jQuery callers. These include API calls, scheduler threads etc. This 
file is a great starting point to make changes to the front end of the workflow designer GUI. This file contains plenty
of methods for opening modals, selecting Files etc.

- Block.js controls are in block_js folder and have been modified appropriately to add authentication utilities. The
blocks.js library hosts a global blocks variable which is sufficient to interact with the workflow.

- csv.html and graph.html help displaying a single table and graph respectively on a web page.

#### src/main/resources directory 
It has the config.properties.template that need to be copied into config.properties.


### Issues
WebServer cannot schedule the Lab Streaming Layer data workflow directly, this kind of workflow can only export its json
and execute in workflow designer locally.

For blocks use native library, due to dynamical classes loading, such kind of blocks can only execute as jar externally.

### Dependencies

org.reflections is a significant dependency of this project. 

An embedded Jetty server has been included in the project to enable hosting the server directly from the JAR.

Hadoop libraries have also been imported to assist the HDFS remote file system.

JDBC for Sqlite has been included for database support.

Others include dependencies for testing and logging.

### References

Grewar's [Block.js](https://github.com/Gregwar/blocks.js) was used to create the block designer.

[Elfinder project](http://elfinder.org/) was used to build the remote file system.
[Elfinder-2.x-servlet repository](https://github.com/bluejoe2008/elfinder-2.x-servlet) was used to create the Java integration.

Other Javascript libraries like Jquery, Bootstrap have their documentation in their headers. 

### Testing

Tests are defined in the [test package](https://github.com/NEUROINFORMATICS-GROUP-FAV-KIV-ZCU/workflow_designer_samples). 
Tests currently include executing an example handling basic arithmetic operations and continuous data.
The repository used for testing common operations is hosted at [workflow_designer_commons](https://github.com/NEUROINFORMATICS-GROUP-FAV-KIV-ZCU/workflow_designer_samples/tree/master/workflow_designer_commons),
testing the EEG data is hosted at [EEGWorkflow](https://github.com/NEUROINFORMATICS-GROUP-FAV-KIV-ZCU/workflow_designer_samples/tree/master/EEGWorkflow).
Mvn package the repository after mvn install the workflow designer, and import them into the web GUI.

### Copyright

 
  This file is part of the Workflow Designer project

  ==========================================
 
  Copyright (C) 2018 by University of West Bohemia (http://www.zcu.cz/en/)
 
 ***********************************************************************************************************************
 
  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
  the License. You may obtain a copy of the License at
 
      http://www.apache.org/licenses/LICENSE-2.0
 
  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
  an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
  specific language governing permissions and limitations under the License.
 
### Contribution Guidelines

Please create a new feature request on the repository before you start working on a new feature so that duplicate efforts
are prevented.

Create a new branch and start development on it.

Make sure you add the relevant tests when you add a feature. 

### Working Demo
http://147.228.63.46:8680
