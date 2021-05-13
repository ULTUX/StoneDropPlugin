package me.apisek12.StoneDrop.DataModels;

import java.util.ArrayList;
import java.util.Arrays;

public class ExecuteCommands {
    private final ArrayList<String> commands;
    private final float chance;
    private final boolean requiresPermission;

    public ExecuteCommands(String commandString, float chance, boolean requiresPermission) {
        String[] commands = commandString.split(" & ");
        this.commands = new ArrayList<>();
        this.commands.addAll(Arrays.asList(commands));
        this.chance = chance;
        this.requiresPermission = requiresPermission;
    }

    public ArrayList<String> getCommands() {
        return commands;
    }

    public float getChance() {
        return chance;
    }

    public boolean isRequiredPermission() {
        return requiresPermission;
    }

    @Override
    public String toString() {
        return "ExecuteCommands{" +
                "commands=" + commands +
                ", chance=" + chance +
                ", requiresPermission=" + requiresPermission +
                '}';
    }
}
