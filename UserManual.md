# Workflow Designer Server (User Manual)
## Introduction
The Workflow Designer Server project is a Jetty/Tomcat based web server that provides a visual interface to run and execute workflows created using the Workflow Designer Project.

The Server hosts the following features:
1. Drag and Drop Workflow Designer
2. Dynamic property configuration
3. JAR import
4. Workflow import/export
5. Job scheduler
6. Job tracker
7. Multiple User accounts and Access Control
8. Remote File browser (OnServer/HDFS)

## Setup and Installation
1. Build the <https://github.com/pintojoey/workflow_designer_server> project
2. Create a copy of config.properties.tempate as config.properties in the src/main/resources folder.
3. Fill in the configuration of an SMTP mail server</li>
4. If you wish to avoid SPAM and are using the project for personal usage, consider using a fake SMTP server like <http://mailtrap.io">mailtrap.io>
5. Build the project **mvn package**. This creates both the jar file as well as the WAR file for tomcat.
6. Run **mvn jetty:run**
7. The default port for jetty is 8680 and can be accessed locally at **localhost:8680**. You should see a similar window.

[![N|Solid](http://www.zedacross.com/wp-content/uploads/2018/08/Screen-Shot-2018-08-09-at-10.40.35-AM-300x110.png)](http://zedacross.com/gsoc2018)
### Usage
#### Login/Registration:
1. Create an account using the registration popup and login to the system. Note email will arrive at the configured SMTP server.
2. You can reset your password anytime using the reset option and forgotten passwords can also be recovered.
3. Username must be a valid email ID, else verification links will not be received
#### Importing Jars:
1. To draw workflows, we need blocks. These blocks are defined using the <https://github.com/pintojoey/workflow_designer>workflow designer project. The project needs to be exported to a JAR file **with all its dependencies**
2. Import the JAR by going to File-&gt;Import Library(.jar)

[![N|Solid](http://www.zedacross.com/wp-content/uploads/2018/08/Screen-Shot-2018-08-09-at-10.44.48-AM-290x300.png)](http://zedacross.com/gsoc2018)
3. Make sure you enter the correct package name. If there are multiple packages you want to scan in a single jar separate them with commas.
4. Making a JAR public makes the JAR file accessible to all users on the same server. (Note: This cannot be undone)
5. On Clicking submit, the file will be uploaded to the server (Note: This may take some time with large JAR files)
6. Once done you will receive an alert indicating the number of blocks registered. If it says 0, then it means there is something wrong with your JAR file or the packageName and you can retry.
7. Once the upload is complete and blocks are registered, expand the left tree, you should see something similar

[![N|Solid](http://www.zedacross.com/wp-content/uploads/2018/08/Screen-Shot-2018-08-09-at-10.49.12-AM-212x300.png)](http://zedacross.com/gsoc2018)
8. This indicates the JAR is now ready for use.

#### Creating Workflows:
1. To create a workflow simply drag and drop blocks from the library onto the canvas. There is no restriction on the number of blocks. To delete a block, select and press delete or use the 'X' icon on the top right. Block descriptions are available on clicking the 'i' icon.
2. Connecting blocks can simply be done by clicking and dragging lines between an input and an output parameter. To delete, select an edge and press delete. Undo/Redo actions are also supported.

[![N|Solid](http://www.zedacross.com/wp-content/uploads/2018/06/Screen-Shot-2018-06-04-at-12.51.14-PM-300x157.png)](http://zedacross.com/gsoc2018)
3. To set the value of the property, double click on the block OR click the settings icon on the block.

[![N|Solid](http://www.zedacross.com/wp-content/uploads/2018/06/Screen-Shot-2018-06-04-at-12.55.40-PM-300x228.png)](http://zedacross.com/gsoc2018)
4. Blocks can be dragged and rearranged.
5. Workflows can be saved and reloaded using the file menu. Workflows are saved as a .json file.
6. Array Properties can be configured using the Add/Remove buttons. File selection blocks appear as a ChooseFile button. Click on it to open the file browser.

[![N|Solid](http://www.zedacross.com/wp-content/uploads/2018/08/Screen-Shot-2018-08-09-at-11.02.46-AM-300x193.png)](http://zedacross.com/gsoc2018)

7. The file browser also provides Hadoop Integration if the HDFS mounted.

[![N|Solid](http://www.zedacross.com/wp-content/uploads/2018/08/Screen-Shot-2018-08-09-at-11.05.27-AM-300x151.png)](http://zedacross.com/gsoc2018)
#### Executing a Workflow:
1. To execute a workflow click on the Schedule button</li>
2. The status of the job is indicated above the canvas</li>
3. When the job is running, the colour of the block indicates the status of the block execution
* Blue Indicates execution is complete
* Red Indicates error in the execution of a block, eventually leads to a failed workflow execution
* White indicates yet to be processed

4. The show log button on a block displays console output of the block with separated STDout and Error Stream contents.

[![N|Solid](http://www.zedacross.com/wp-content/uploads/2018/08/Screen-Shot-2018-08-09-at-12.22.00-PM-300x122.png)](http://zedacross.com/gsoc2018)

[![N|Solid](http://www.zedacross.com/wp-content/uploads/2018/08/Screen-Shot-2018-08-09-at-12.27.42-PM-300x85.png)](http://zedacross.com/gsoc2018)
5. You can schedule multiple jobs. You can also exit the browser after scheduling a job.
6. Scheduled Jobs are executed in a sequential pattern on a fixed number of threads configured on the server.
7. The list of jobs scheduled by you can be visualized from Scheduler-&gt;Jobs List

[![N|Solid](http://www.zedacross.com/wp-content/uploads/2018/08/Screen-Shot-2018-08-09-at-12.30.35-PM-300x205.png)](http://zedacross.com/gsoc2018)
8. Previously executed jobs can be visualized using the Preview button
9. Complex output types are shown as download/open options on the block

[![N|Solid](http://www.zedacross.com/wp-content/uploads/2018/08/Screen-Shot-2018-08-09-at-12.32.09-PM-247x300.png)](http://zedacross.com/gsoc2018)
[![N|Solid](http://www.zedacross.com/wp-content/uploads/2018/08/Screen-Shot-2018-08-09-at-12.32.23-PM-300x88.png)](http://zedacross.com/gsoc2018)
[![N|Solid](http://www.zedacross.com/wp-content/uploads/2018/08/Screen-Shot-2018-08-09-at-12.32.33-PM-160x300.png)](http://zedacross.com/gsoc2018)
