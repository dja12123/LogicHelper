package kr.dja;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
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
class Session
{
	private LogicCore core;
	private File fileLocation = null;
	private Grid grid;
	private TaskManager task;
	private TemplateManager template;
	private String name = "noname";
	private String description = "";
	
	Session(LogicCore core)
	{
		this.core = core;
		this.grid = new Grid(this, new SizeInfo(30, 30, 15, 15), UUID.randomUUID());
		this.task = new TaskManager(this);
		this.template = new TemplateManager(this);
		this.core.getUI().getGridArea().setGrid(this.grid);
		this.core.getUI().getTaskManagerPanel().setManager(this.task);
	}
	DataBranch getData()
	{
		DataBranch branch = new DataBranch("Session");
		branch.setData("name", this.name);
		DataBranch description = new DataBranch("Description");
		String[] text = this.description.split("\\r?\\n");
		for(int i = 0; i < text.length; i++)
		{
			description.setData(Integer.toString(i), text[i]);
		}
		branch.addLowerBranch(description);
		return branch;
	}
	void setData(DataBranch branch)
	{
		this.name = branch.getData("name");
		Iterator<DataBranch> itr = branch.getLowerBranchIterator();
		while(itr.hasNext())
		{
			DataBranch data = itr.next();
			switch(data.getName())
			{
			case "Description":
				String text = new String();
				Iterator<String> keyItr = data.getDataKeySetIterator();
				while(keyItr.hasNext())
				{
					text += data.getData(keyItr.next()) + "\n";
				}
				this.description = text;
				break;
			}
		}
	}
	void LoadData(File file)
	{
		System.out.println("LOADDATA: " + file.toString());
		DataBranch tree = new DataBranch("tree");
		this.fileLocation = file;
		try
		{
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			this.recursiveDataLoad(reader, tree);
			reader.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		Iterator<DataBranch> itr = tree.getLowerBranchIterator();
		while(itr.hasNext())
		{
			DataBranch branch = itr.next();
			System.out.println(branch.getName());
			switch(branch.getName())
			{
			case "Session":
				this.setData(branch);
				break;
			case "Grid":
				this.grid = new Grid(this, branch);
				break;
			case "TaskManager":
				this.task = new TaskManager(this, branch);
				break;
			}
		}
		this.core.getUI().getGridArea().setGrid(this.grid);
		this.core.getUI().getTaskManagerPanel().setManager(this.task);
	}
	private void recursiveDataLoad(BufferedReader reader, DataBranch data) throws IOException
	{
		String line;
		while((line = reader.readLine()) != null)
		{
			line = line.replace("\t", "");
			if(line.contains("}"))
			{
				System.out.println(data.getName() + " End");
				return;
			}
			else if(line.contains("{"))
			{
				DataBranch branch = new DataBranch(line.split("=")[0]);
				data.addLowerBranch(branch);
				System.out.println("Branch " + branch.getName());
				this.recursiveDataLoad(reader, branch);
			}
			else
			{
				String[] kv = line.split("=");
				String key = kv[0];
				String value = "";
				if(kv.length > 1)
				{
					value = line.split("=")[1];
				}
				System.out.println("data: " + key + " = " + value);
				data.setData(key, value);
			}
		}
	}
	void saveData(File file)
	{
		System.out.println(file);
		this.fileLocation = file;
		DataBranch tree = new DataBranch("tree");
		tree.addLowerBranch(this.getData());
		tree.addLowerBranch(this.getGrid().getData());
		tree.addLowerBranch(this.getTaskManager().getData());
		try
		{
			BufferedWriter out = new BufferedWriter(new FileWriter(file, false));

			this.recursiveDataSave(out, tree, 0);
			out.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	private void recursiveDataSave(BufferedWriter out, DataBranch data, int level) throws IOException
	{
		String levelTab = new String();
		for(int i = 0; i < level; i++)
		{
			levelTab += "\t";
		}
		Iterator<String> dataItr = data.getDataKeySetIterator();
		while(dataItr.hasNext())
		{
			String key = dataItr.next();
			String value = data.getData(key).replace("{", "").replace("}", "");
			out.write(levelTab + key + "=" + value + "\n");
		}
		Iterator<DataBranch> lowerBranchData = data.getLowerBranchIterator();
		while(lowerBranchData.hasNext())
		{
			DataBranch branch = lowerBranchData.next();
			out.write(levelTab + branch.getName() + "={\n");
			this.recursiveDataSave(out, branch, level + 1);
			out.write(levelTab + "}\n");
		}
	}
	void close()
	{
		
	}
	TaskManager getTaskManager()
	{
		return this.task;
	}
	TemplateManager getTemplateManager()
	{
		return this.template;
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