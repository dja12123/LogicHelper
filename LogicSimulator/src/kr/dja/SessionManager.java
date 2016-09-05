package kr.dja;

import java.awt.Color;
import java.util.UUID;

public class SessionManager
{
	private LogicCore core;
	
	SessionManager(LogicCore core)
	{
		this.core = core;
		this.createSession();
		this.createSession();
		this.createSession();
	}
	Session createSession()
	{
		Session session = new Session(this.core);
		return session;
	}
}
class Session
{
	private LogicCore core;
	private Grid grid;
	private TaskManager manager;
	Session(LogicCore core)
	{
		this.core = core;
		this.grid = new Grid(this, new SizeInfo(30, 30, 15, 15), UUID.randomUUID());
		this.manager = new TaskManager(this);
		this.core.getUI().getGridArea().setGrid(this.grid);
		this.core.getUI().getTaskManagerPanel().setManager(this.manager);
	}
	void saveData()
	{
		
	}
	void Session(String location)
	{
		
	}
	void close()
	{
		
	}
	TaskManager getTaskManager()
	{
		return this.manager;
	}
	LogicCore getCore()
	{
		return this.core;
	}
	Grid getGrid()
	{
		return this.grid;
	}
}