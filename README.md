# Command watcher extension

This extension works only with the standalone machine agent.

## Use Case
An AppDynamics extension that provide metrics from linux commands or script that generates a numeric output.

## Prerequisites

Before the extension is installed, the prerequisites mentioned [here](https://community.appdynamics.com/t5/Knowledge-Base/Extensions-Prerequisites-Guide/ta-p/35213) need to be met. Please do not proceed with the extension installation if the specified prerequisites are not met

## Installation
1. Run "mvn clean install"
1. Unzip the contents of CommandWatcher-\<version\>.zip file (&lt;CommandWatcherRepo&gt; / targets) and copy the directory to `<your-machine-agent-dir>/monitors`.
2. Edit config.yml file and provide the required configuration (see Configuration section)
3. Restart the Machine Agent.

Please place the extension in the **"monitors"** directory of your **Machine Agent** installation directory. Do not place the extension in the **"extensions"** directory of your **Machine Agent** installation directory.

## Configuration

### Config.yml
**Note**: Please make sure to not use tab (\t) while editing yaml files

#### Configure metric prefix
Please follow section 2.1 of the [Document](https://community.appdynamics.com/t5/Knowledge-Base/How-do-I-troubleshoot-missing-custom-metrics-or-extensions/ta-p/28695) to set up metric prefix.
```
#Metric prefix used when SIM is enabled for your machine agent
#metricPrefix: "Custom Metrics|Command Watcher|"

#This will publish metrics to specific tier
#Instructions on how to retrieve the Component ID can be found in the Metric Prefix section of https://community.appdynamics.com/t5/Knowledge-Base/How-do-I-troubleshoot-missing-custom-metrics-or-extensions/ta-p/28695
metricPrefix: "Server|Component:<COMPONENT OR TIER ID>|Custom Metrics|Command Watcher|"
```

#### Configure commandToProcess section
commandToProcess section should be configured like below:
```
commandToProcess:

  - displayName: "Home Dir File Count"
    command: "ls -ltr ~ | wc -l"
    isScript: false

  - displayName: "Java Processes List"
    command: "ps aux | grep java | wc -l"
    isScript: false

  - displayName: "Random Script"
    command: "/path/to/random_script.sh"
    isScript: true
```
- displayName: Display name for your command which will be displayed in metric path. It should be unique for all commands
- command: Command for which you want to collect metric. It can either be a command or path to some script file. The command or script must return single numerical value only.
- isScript: It is a flag which tells if the command to execute is a path to script file or a command. It can be "true" or "false"

#### Number of threads
Always include one thread per command + 1 (to run main task)

For e.g. if you have configured 4 commands, then number of threads required are 5 (4 to run commands + 1 to run main task).
```
numberOfThreads: 5
```

#### Thread timeout
It represents timeout for a thread in seconds.
```
threadTimeout: 30
```

#### Yml Validation
Please copy all the contents of the config.yml file and go to http://www.yamllint.com/ . On reaching the website, paste the contents and press the “Go” button on the bottom left.

### Metrics Provided
We provide metric related to output of the linux command or the script file that generates single numeric output.

For example: ps –ef | grep java |wc –l = 3

## Workbench
Workbench is an inbuilt feature provided with each extension in order to assist you to fine tune the extension setup before you actually deploy it on the controller. Please review the following document on [How to use the Extensions WorkBench](https://community.appdynamics.com/t5/Knowledge-Base/How-to-use-the-Extensions-WorkBench/ta-p/30130)

## Troubleshooting
Please follow the steps listed in this [troubleshooting-document](https://community.appdynamics.com/t5/Knowledge-Base/How-to-troubleshoot-missing-custom-metrics-or-extensions-metrics/ta-p/28695) in order to troubleshoot your issue. These are a set of common issues that customers might have faced during the installation of the extension. If these don't solve your issue, please follow the last step on the [troubleshooting-document](https://community.appdynamics.com/t5/Knowledge-Base/How-to-troubleshoot-missing-custom-metrics-or-extensions-metrics/ta-p/28695) to contact the support team

## Support Tickets

If after going through the [Troubleshooting Document](https://community.appdynamics.com/t5/Knowledge-Base/How-to-troubleshoot-missing-custom-metrics-or-extensions-metrics/ta-p/28695) you have not been able to get your extension working, please file a ticket and add the following information.

Please provide the following in order for us to assist you better.

1. Stop the running machine agent.
2. Delete all existing logs under <MachineAgent>/logs.
3. Please enable debug logging by editing the file <MachineAgent>/conf/logging/log4j.xml. Change the level value of the following <logger> elements to debug.
   <logger name="com.singularity">
   <logger name="com.appdynamics">
4. Start the machine agent and please let it run for 10 mins. Then zip and upload all the logs in the directory <MachineAgent>/logs/*.
5. Attach the zipped <MachineAgent>/conf/* directory here.
6. Attach the zipped <MachineAgent>/monitors/ExtensionFolderYouAreHavingIssuesWith directory here.
   For any support related questions, you can also contact help@appdynamics.com.

## Contributing

Always feel free to fork and contribute any changes directly via [GitHub](https://github.com/Appdynamics/Command-Watcher-Extension).

## Version

|          Name            |  Version   |
|--------------------------|------------|
|Extension Version         |2.0.0       |
|Controller Compatibility  |4.5 or Later|
|Machine Agent Version     |4.5.13+     |
|Last Update               |05/08/2021  |
