package kr.dja;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.UUID;

public class SessionManager
{
	private LogicCore core;
	private Session focusSession;
	
	SessionManager(LogicCore core)
	{
		this.core = core;
		this.createSession();
	}
	Session createSession()
	{
		Session session = new Session(this.core);
		this.focusSession = session;
		return session;
	}
	Session getFocusSession()
	{
		return this.focusSession;
	}
}
class Session implements DataIO
{
	private LogicCore core;
	private Grid grid;
	private TaskManager manager;
	private String name = "noname";
	Session(LogicCore core)
	{
		this.core = core;
		this.grid = new Grid(this, new SizeInfo(30, 30, 15, 15), UUID.randomUUID());
		this.manager = new TaskManager(this);
		this.core.getUI().getGridArea().setGrid(this.grid);
		this.core.getUI().getTaskManagerPanel().setManager(this.manager);
	}
	@Override
	public void setData(LinkedHashMap<String, String> dataMap)
	{
		this.name = dataMap.get("name");
	}
	@Override
	public LinkedHashMap<String, String> getData(LinkedHashMap<String, String> dataMap)
	{
		dataMap.put("name", this.name);
		return dataMap;
	}
	void saveData()
	{
		System.out.println(LogicCore.JARLOC + "/" + this.name + ".LogicSave");
		try
		{
			BufferedWriter out = new BufferedWriter(new FileWriter(LogicCore.JARLOC + "/" + this.name + ".LogicSave", false));
			LinkedHashMap<String, String> sessionDataMap = this.getData(new LinkedHashMap<String, String>());
			for(String sessionKey : sessionDataMap.keySet())
			{
				out.write(sessionKey + "=" + sessionDataMap.get(sessionKey) + "\n");
			}
			
				LinkedHashMap<String, String> gridDataMap = this.grid.getData(new LinkedHashMap<String, String>());
				out.write("\nGridData:\n");
				for(String sessionKey : gridDataMap.keySet())
				{
					out.write("\t" + sessionKey + "=" + gridDataMap.get(sessionKey) + "\n");
				}
				for(GridMember member : this.grid.getMembers().values())
				{
					LinkedHashMap<String, String> memberDataMap = new LinkedHashMap<String, String>();
					member.getData(memberDataMap);
					out.write("\n\tGridMemberData:\n");
					for(String memberKey : memberDataMap.keySet())
					{
						out.write("\t\t" + memberKey + "=" + memberDataMap.get(memberKey) + "\n");
					}
				}
			out.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	void Session(String fileLocation)
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
interface DataIO
{
	void setData(LinkedHashMap<String, String> dataMap);
	LinkedHashMap<String, String> getData(LinkedHashMap<String, String> dataMap);
}