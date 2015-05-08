Command watcher extension

An AppDynamics extension to be used with a stand-alone Java machine agent to provide metrics from linux commands or script that generates a numeric output.

Metrics Provided 

We provide metric related to output of the linux command or the script file that generates single numeric output.

For example: ps –ef | grep java |wc –l = 3

Installation

1. Download and unzip CommandWatcher.zip from AppSphere.
2. Copy the CommandWatcher directory to `<MACHINE_AGENT_HOME>/monitors`.


Configuration 

###Note
Please make sure to not use tab (\t) while editing yaml files. You may want to validate the yaml file using a yaml validator http://yamllint.com/

1. Configure the file to be watched by editing the config.yaml file in `<MACHINE_AGENT_HOME>/monitors/ CommandWatcher /`. Below is the format

Important - displayName has to be unique for the command/script file

# List of commands
commandToProcess:

  - displayName: "Java processes list"
    command: "/Users/abhi.pandey/Documents/query1.sql"
    isScript: true

  - displayName: "Java count script"
    command: "/Users/abhi.pandey/Downloads/javaProcessesCount.sh"
    isScript: true

# Make sure the metric prefix ends with a |
metricPrefix: "Custom Metrics|Command Watcher|"


2. Configure the path to the config.yaml file by editing the <task-arguments> in the monitor.xml file. Below is the sample

     <task-arguments>
         <!-- config file-->
             <argument name="config-file" is-required="true" default-value="monitors/ CommandWatcher /config.yml" />
          ....
     </task-arguments>

Contributing

Always feel free to fork and contribute any changes directly via [GitHub][].

Community 

Find out more in the [Community][].

Support

For any questions or feature request, please contact [AppDynamics Center of Excellence].

Version: 1.0
Controller Compatibility: 3.7 or later

[GitHub]: https://github.com/Appdynamics/Command-Watcher-Extension
[Community]: http://community.appdynamics.com/
[AppDynamics Center of Excellence]: mailto:ace-request@appdynamics.com
