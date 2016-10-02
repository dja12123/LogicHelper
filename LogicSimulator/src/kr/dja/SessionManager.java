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

import javax.swing.JLabel;
import javax.swing.JPanel;

public class SessionManager
{
	private LogicCore core;
	private Session focusSession;
	private ArrayList<Session> sessions;
	
	SessionManager(LogicCore core)
	{
		this.core = core;
		this.sessions = new ArrayList<Session>();
		this.createSession();
	}
	Session createSession()
	{
		Session session = new Session(this.core);
		this.core.getUI().getToolBar().addSessionTabPanel(session.getSessionTab());
		this.sessions.add(session);
		this.setFocusSession(session);
		return session;
	}
	void removeSession(Session session)
	{
		int index = this.sessions.indexOf(session);
		this.sessions.remove(session);
		LogicCore.putConsole("RemoveSession");
		if(this.focusSession == session)
		{
			if(this.sessions.size() > index)
			{
				this.setFocusSession(this.sessions.get(index));
			}
			else if(index > 0)
			{
				this.setFocusSession(this.sessions.get(index - 1));
			}
			else
			{
				this.setFocusSession(null);
			}
		}
		this.core.getUI().getToolBar().removeSessionTabPanel(session.getSessionTab());
		session.getGrid().removeGrid();
	}
	void setFocusSession(Session session)
	{
		if(this.focusSession != null)
		{
			this.focusSession.getGrid().deSelectAll();
			this.focusSession.deFocus();
		}
		if(session != null)
		{	
			this.focusSession = session;
			this.focusSession.setFocus();
			this.core.getUI().getToolBar().setFocus(this.focusSession.getSessionTab());
			this.core.getUI().getToolBar().setSaveButtonStatus(true);
		}
		else
		{
			this.focusSession = null;
			this.core.getUI().getGridArea().setGrid(null);
			this.core.getUI().getTaskManagerPanel().setManager(null);
			this.core.getUI().getTemplatePanel().setManager(null);
			this.core.getUI().getToolBar().setSaveButtonStatus(false);
			this.core.getUI().getUnderBar().setGridSizeInfo(new SizeInfo(0, 0, 0, 0));
		}
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
	private String name = LogicCore.getResource().getLocal("Noname");
	private String description = "";
	
	private ButtonPanel sessionTab;
	private JLabel nameLabel;
	private ButtonPanel closeButton;
	
	Session(LogicCore core)
	{
		this.core = core;
		this.grid = new Grid(this, new SizeInfo(30, 30, 15, 15), UUID.randomUUID());
		this.task = new TaskManager(this);
		this.template = new TemplateManager(this);
		
		this.sessionTab = new ButtonPanel()
		{
			private static final long serialVersionUID = 1L;
			@Override
			void pressed(int button)
			{
				core.getSession().setFocusSession(Session.this);
			}
		};
		this.sessionTab.setSize(120, 20);
		this.sessionTab.setLayout(null);
		this.sessionTab.setBasicImage(LogicCore.getResource().getImage("SESSION_TAB"));
		this.sessionTab.setOnMouseImage(LogicCore.getResource().getImage("SESSION_TAB_SELECT"));
		this.sessionTab.setBasicPressImage(LogicCore.getResource().getImage("SESSION_TAB_PUSH"));
		
		this.nameLabel = new JLabel();
		this.nameLabel.setBounds(5, 0, 100, 20);
		this.nameLabel.setFont(LogicCore.RES.NORMAL_FONT.deriveFont(14F));
		this.nameLabel.setText(this.name);
		
		this.closeButton = new ButtonPanel()
		{
			private static final long serialVersionUID = 1L;
			@Override
			void pressed(int button)
			{
				core.getSession().removeSession(Session.this);
			}
		};
		this.closeButton.setBounds(104, 4, 12, 12);
		this.closeButton.setBasicImage(LogicCore.getResource().getImage("SESSION_CLOSE"));
		this.closeButton.setBasicPressImage(LogicCore.getResource().getImage("SESSION_CLOSE_PUSH"));
		
		this.sessionTab.add(this.nameLabel);
		this.sessionTab.add(this.closeButton);
	}
	ButtonPanel getSessionTab()
	{
		return this.sessionTab;
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
	void setFocus()
	{
		this.core.getUI().getGridArea().setGrid(this.grid);
		this.core.getUI().getTaskManagerPanel().setManager(this.task);
		this.core.getUI().getTemplatePanel().setManager(this.template);
		this.sessionTab.setBasicImage(LogicCore.getResource().getImage("SESSION_TAB_FOCUS"));
		this.sessionTab.setOnMouseImage(LogicCore.getResource().getImage("SESSION_TAB_FOCUS_SELECT"));
		this.sessionTab.setBasicPressImage(LogicCore.getResource().getImage("SESSION_TAB_FOCUS_PUSH"));
	}
	void deFocus()
	{
		this.sessionTab.setBasicImage(LogicCore.getResource().getImage("SESSION_TAB"));
		this.sessionTab.setOnMouseImage(LogicCore.getResource().getImage("SESSION_TAB_SELECT"));
		this.sessionTab.setBasicPressImage(LogicCore.getResource().getImage("SESSION_TAB_PUSH"));
	}
	void LoadData(File file)
	{
		LogicCore.putConsole("LoadData: " + file.toString());
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
			LogicCore.putConsole(e.toString());
		}
		Iterator<DataBranch> itr = tree.getLowerBranchIterator();
		while(itr.hasNext())
		{
			DataBranch branch = itr.next();
			LogicCore.putConsole("Load " + branch.getName());
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
			case "TemplateManager":
				this.template = new TemplateManager(this, branch);
				break;
			}
		}
		this.core.getUI().getGridArea().setGrid(this.grid);
		this.core.getUI().getTaskManagerPanel().setManager(this.task);
		this.core.getUI().getTemplatePanel().setManager(this.template);
	}
	void recursiveDataLoad(BufferedReader reader, DataBranch data) throws IOException
	{
		String line;
		while((line = reader.readLine()) != null)
		{
			line = line.replace("\t", "");
			if(line.contains("}"))
			{
				LogicCore.putConsole(data.getName() + " End");
				return;
			}
			else if(line.contains("{"))
			{
				DataBranch branch = new DataBranch(line.split("=")[0]);
				data.addLowerBranch(branch);
				LogicCore.putConsole("Branch " + branch.getName());
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
				LogicCore.putConsole("Data: " + key + " = " + value);
				data.setData(key, value);
			}
		}
	}
	void saveData(File file)
	{
		LogicCore.putConsole("SaveLocation: " + file);
		this.fileLocation = file;
		DataBranch tree = new DataBranch("tree");
		tree.addLowerBranch(this.getData());
		tree.addLowerBranch(this.getGrid().getData());
		tree.addLowerBranch(this.getTaskManager().getData());
		tree.addLowerBranch(this.template.getData());
		try
		{
			BufferedWriter out = new BufferedWriter(new FileWriter(file, false));

			this.recursiveDataSave(out, tree, 0);
			out.close();
		}
		catch (IOException e)
		{
			LogicCore.putConsole(e.toString());
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
			if(data.getData(key) != null)
			{
				String value = data.getData(key).replace("{", "").replace("}", "");
				out.write(levelTab + key + "=" + value + "\n");
			}
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
		this.nameLabel.setText(this.name);
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