package com.appdynamics.extensions.commandwatcher.config;

/**
 * Created by abhi.pandey on 4/24/15.
 */
public class CommandToProcess {
    private String displayName;
    private String command;
    private Boolean isScript;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public Boolean getIsScript() {
        return isScript;
    }

    public void setIsScript(Boolean isScript) {
        this.isScript = isScript;
    }

    @Override
    public String toString() {
        return "CommandToProcess{" +
                "displayName='" + displayName + '\'' +
                ", command='" + command + '\'' +
                ", isScript=" + isScript +
                '}';
    }
}
