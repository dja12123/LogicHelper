package kr.dja;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

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
		this.taskManager = new TaskManager(this);
		this.taskOperator = new TaskOperator(this);
		this.logicUI = new UI(this);
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
	private HashMap<String, BufferedImage> images = new HashMap<String, BufferedImage>();
	public Font NORMAL_FONT;
	public Font PIXEL_FONT;
	public Font BAR_FONT;
	public int ASD;
	Properties config = new Properties();
	public Resource()
	{
		for(File imgFileList : new File(getClass().getClassLoader().getResource("images").getFile()).listFiles())
		{
			try
			{
				System.out.println(imgFileList.getName().split("[.]")[0]);
				images.put(imgFileList.getName().split("[.]")[0], ImageIO.read(imgFileList));
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
		}
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
		/*config.getOrDefault("SaveLocation", "");
		config.getOrDefault("Cycle", "200");
		config.getOrDefault("OpenFile", "");
		JFrame frame = new JFrame();
		frame.setSize(100, 100);
		frame.setVisible(true);
		frame.add(new JPanel(){
			@Override
			public void paint(Graphics g)
			{
				g.drawImage(getImage("MIDDLE_BLOCK_OFF"), 0, 0, this);
			}
		});*/
	}
	BufferedImage getImage(String tag)
	{
		return this.images.get(tag);
	}
}