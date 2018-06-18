package cz.zcu.kiv.server.utilities.elfinder.controller.executor;

public interface CommandExecutor
{
	void execute(CommandExecutionContext commandExecutionContext)
			throws Exception;
}
