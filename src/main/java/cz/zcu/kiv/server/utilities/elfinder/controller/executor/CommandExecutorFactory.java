package cz.zcu.kiv.server.utilities.elfinder.controller.executor;

public interface CommandExecutorFactory
{
	CommandExecutor get(String commandName);
}