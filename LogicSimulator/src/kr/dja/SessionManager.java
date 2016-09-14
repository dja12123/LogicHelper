package kr.dja;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
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
	void LoadFile(File file)
	{
		
	}
}
class Session
{
	private LogicCore core;
	private File fileLocation = null;
	private Grid grid;
	private TaskManager manager;
	private String name = "noname";
	private String description = "";
	Session(LogicCore core)
	{
		this.core = core;
		this.grid = new Grid(this, new SizeInfo(30, 30, 15, 15), UUID.randomUUID());
		this.manager = new TaskManager(this);
		this.core.getUI().getGridArea().setGrid(this.grid);
		this.core.getUI().getTaskManagerPanel().setManager(this.manager);
	}
	void setData(LinkedHashMap<String, String> dataMap)
	{
		//this.name = dataMap.get("name");
		//this.description = dataMap.get("description");
	}
	void saveData(File file)
	{
		System.out.println(file);
		this.fileLocation = file;
		try
		{
			BufferedWriter out = new BufferedWriter(new FileWriter(file, false));
			out.write("name=" + this.name + "\n");
			out.write("description={\n");
			for(String str : this.description.split("\\r?\\n"))
			{
				out.write("\t" + str + "\n");
			}
			out.write("}\n");
				LinkedHashMap<String, String> gridDataMap = this.grid.getData(new LinkedHashMap<String, String>());
				out.write("GridData={\n");
				for(String sessionKey : gridDataMap.keySet())
				{
					out.write("\t" + sessionKey + "=" + gridDataMap.get(sessionKey) + "\n");
				}
				ArrayList<String> memberDataList = new ArrayList<String>();
				this.grid.getMemberData(memberDataList);
				for(String memberData : memberDataList)
				{
					out.write("\t" + memberData);
				}
				out.write("}\n");
			ArrayList<String> arrDataTemp = new ArrayList<String>();
			this.getTaskManager().getData(arrDataTemp);
			out.write("TaskManager={\n");
			for(String data : arrDataTemp)
			{
				out.write("\t" + data);
			}
			out.write("}\n");
			out.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	void Session(String fileLocation)
	{
		LogicCore.getResource().getFile(fileLocation);

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
	String getName()
	{
		return this.name;
	}
	void setName(String name)
	{
		this.name = name;
	}
	String getDescription()
	{
		return this.description;
	}
	void setDescription(String str)
	{
		this.description = str;
	}
	File getFileLocation()
	{
		return this.fileLocation;
	}
}
interface DataIO
{
	void setData(LinkedHashMap<String, String> dataMap);
	LinkedHashMap<String, String> getData(LinkedHashMap<String, String> dataMap);
}