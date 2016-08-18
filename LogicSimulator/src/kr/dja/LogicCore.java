package kr.dja;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

public class LogicCore
{
	public static final Resource RES = new Resource();
	
	private static ArrayList<LogicCore> Task = new ArrayList<LogicCore>();
	
	private TaskManager taskManager;
	private TaskOperator taskOperator;
	private Grid grid;
	private UI logicUI;
	
	public static void main(String[] args)
	{
		createInstance();
	}
	public static Resource getResource()
	{
		return RES;
	}
	static void createInstance()
	{	
		Task.add(new LogicCore());
	}
	static void removeInstance(LogicCore task)
	{
		if(Task.contains(task))
		{
			Task.remove(task);
		}
		if(Task.size() <= 0)
		{//프로그램 종료
			System.exit(0);
		}
	}
	private LogicCore()
	{
		this.logicUI = new UI(this);
		this.taskManager = new TaskManager(this);
		this.taskOperator = new TaskOperator(this);
		this.grid = new Grid(this);
		this.logicUI.doLayout();
	}
	TaskManager getTaskManager()
	{
		return this.taskManager;
	}
	TaskOperator getTaskOperator()
	{
		return this.taskOperator;
	}
	Grid getGrid()
	{
		return this.grid;
	}
	UI getUI()
	{
		return this.logicUI;
	}
}
class Resource
{
	public Font NORMAL_FONT;
	public Font PIXEL_FONT;
	public Font BAR_FONT;
	public int ASD;
	Properties config = new Properties();
	public Resource()
	{
		try
		{
			NORMAL_FONT = Font.createFont(Font.TRUETYPE_FONT, getClass().getClassLoader().getResourceAsStream("font/SeoulNamsanM.ttf"));
			BAR_FONT = Font.createFont(Font.TRUETYPE_FONT,getClass().getClassLoader().getResourceAsStream("font/gulim.ttf"));
			PIXEL_FONT = Font.createFont(Font.TRUETYPE_FONT,getClass().getClassLoader().getResourceAsStream("font/HOOG0557.ttf"));
		}
		catch(FontFormatException | IOException e)
		{
			e.printStackTrace();
		}
		try
		{
			BufferedReader reader = new BufferedReader(new FileReader(getClass().getClassLoader().getResource("config.properties").getFile()));
			config.load(reader);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		config.getOrDefault("SaveLocation", "");
		config.getOrDefault("Cycle", "200");
		config.getOrDefault("OpenFile", "");
	}
}