# Workflow Designer Server

## Overview
This project aims at building an easy to use graphical interface that can streamline the configuration of the parameters
controlling individual processing sub-routines and thus make it easy to design complicated data flows and execute them.  
Workflows are designed using individual component blocks that have completely configurable inputs, outputs and 
properties. The Blocks can be combined and rearranged at runtime without making any modification to code. 

Efforts are also targeted to make the tool user friendly and enable easy deployment of workflows on distributed 
computing frameworks.

This project has been developed as a part of Google Summer of Code 2018 by Joey Pinto under the mentorship of the 
International Neuroinformatics Coordinating Facility, Sweden.

More details about the project can be found at http://www.zedacross.com/gsoc2018/

This repository hosts the webserver which runs the GUI utility to design workflows by combining and arranging blocks.
The webserver also posses the functionality to execute tasks and report errors and logs. 
It also hosts an access control and scheduling system. The project also hosts a remote file upload interface.

More details about how to use the GUI are available at http://www.zedacross.com/gsoc2018/workflow-designer-server-user-manual

## Contribution Guidelines

### Code Structure

The project is a Maven project and was developed in IntellijIDEA and has a .iml file that can be used to imported. For 
development purposes you may need to run 'mvn install' to make the library available to other projects needing this. Or
you can simply package the project by running 'mvn package' and adding the generated JAR to your classpath.

The cz.zcu.kiv.server root package hosts Java classes important to the web server

1)The EmbeddedServer contains the JettyServer implementation and hosts the API server and static resources at the 
configured URL

2) The Servlet.java hosts the servlet wrapper for Elfinder

3) UserAccounts.java hosts the user accounts related APIs

4) Workflow.java hosts the APIs relevant to execution, scheduling and tracking of jobs

The cz.zcu.kiv.server.scheduler package hosts the classes dealing with the scheduling and management of tasks

Manager.java hosts a singleton that deals with job tracking. The execution methodology currently followed is threads are
created for incoming jobs upto they reach server.maxthreads. After that new threads are allocated only when a thread 
terminates.

The cz.zcu.kiv.server.sqlite package hosts the models and database connector for the project. SQLiteDB.java is a 
database instance singleton. Models exist for jobs, users and modules.

The cz.zcu.kiv.server.utilities.config package hosts the Properties configuration getter methods.

The cz.zcu.kiv.server.utilities.elfinder package hosts the Elfinder file system accessors. The Elfinder implementation 
gives a great way to host your own filesystem implementation in the localfs folder. To create your own type of volume
implement all methods of the DefaultFsService as required by you. A browser for HDFS folder has been implmented in 
the current project. Efforts towards enabling file upload and download are more than welcome.
Make sure you mount your filesystem in servlet.ConnectorServlet

The cz.zcu.kiv.server.utilities.email package contains the Email client and template which you can modify as you wish. 

The src/main/webapp Consists of all the static files available as resources on the webserver.

The index.html file hosts the GUI as a one page implementation. All popups are implemented as BootStrap modals.
Alerts are shown using Alertify.js library.

The main.js file contains the primary javascript/jQuery callers. These include API calls, scheduler threads etc. This 
file is a great starting point to make changes to the front end of the workflow designer GUI. This file contains plenty
of methods for opening modals, selecting Files etc.

Block.js controls are in block_js folder and have been modified appropriately to add authentication utilities. The
blocks.js library hosts a global blocks variable which is sufficient to interact with the workflow.

csv.html and graph.html help displaying a single table and graph respectively on a web page.

The src/main/resources directory has the config.properties.template that need to be copied into config.properties.

### Dependencies

org.reflections is a significant dependency of this project. 

An embedded Jetty server has been included in the project to enable hosting the server directly from the JAR.

Hadoop libraries have also been imported to assist the HDFS remote file system.

JDBC for Sqlite has been included for database support.

Others include dependencies for testing and logging.

### References

Grewar's Block.js was used https://github.com/Gregwar/blocks.js to create the block designer.

Elfinder http://elfinder.org/ project was used to build the remote file system
The https://github.com/bluejoe2008/elfinder-2.x-servlet repository was used to create the Java integration

Other Javascript libraries like Jquery, Bootstrap have their documentation in their headers. 

### Testing

Tests are defined in the test package. Tests currently include executing an example handling basic arithmetic operations.
The JAR used for testing is hosted at 
https://github.com/pintojoey/workflow_designer_samples/tree/master/workflow_test
Package the JAR hosted hosted at this repository and add it to the test_data folder.

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
