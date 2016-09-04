package kr.dja;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.URI;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.text.DefaultCaret;

public class LogicCore
{
	public static final String VERSION = "0.0.1";
	
	private static ArrayList<LogicCore> Task;
	private static ArrayList<Console> Consols;
	
	public static Resource RES;
	
	private SessionManager session;
	private TaskOperator taskOperator;
	private UI logicUI;
	
	public static void main(String[] args)
	{
		Task = new ArrayList<LogicCore>();
		Consols = new ArrayList<Console>();
		LoadingWindow window = new LoadingWindow();
		
		RES = new Resource();
		createInstance();
		window.remove();
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
		{
			System.exit(0);
		}
	}
	static void putConsole(String str)
	{
		System.out.println(str);
		for(Console console : Consols)
		{
			console.put(str);
		}
	}
	static void registerConsole(Console console)
	{
		Consols.add(console);
	}
	static void removeConsole(Console console)
	{
		Consols.remove(console);
	}
	private LogicCore()
	{
		this.logicUI = new UI(this);
		this.session = new SessionManager(this);
		this.taskOperator = new TaskOperator(this);
	}
	SessionManager getSession()
	{
		return this.session;
	}
	TaskOperator getTaskOperator()
	{
		return this.taskOperator;
	}
	UI getUI()
	{
		return this.logicUI;
	}
}
class Resource
{
	private static final String CONFIG_FILE_NAME = "config.properties";
	private static final String IMG_DIR_NAME = "images";
	private static final String LANG_DIR_NAME = "language";
	private static final String LANG_DFT_TAG_FILE_NAME = "_DefaultTag.txt";
	private static final String LANG_SEPARATOR = "\"";
	private static final String FONT_DIR_NAME = "font";
	
	private HashMap<String, BufferedImage> images = new HashMap<String, BufferedImage>();
	private HashMap<String, String> local = new HashMap<String, String>();
	public Font NORMAL_FONT;
	public Font PIXEL_FONT;
	public Font BAR_FONT;
	Properties config = new Properties();
	
	public Resource()
	{
		try
		{
			File configDIR = new File(new File(Resource.class.getProtectionDomain().getCodeSource().getLocation().getPath() + "../../" + CONFIG_FILE_NAME).getCanonicalPath());
			//if(!configDIR.isFile())
			if(true)//TODO
			{//콘픽이 없을경우 기록
				FileOutputStream writer = new FileOutputStream(configDIR, false);
				FileInputStream reader = new FileInputStream(this.getFile("/" + CONFIG_FILE_NAME));
				FileChannel fcw =  writer.getChannel();
				FileChannel fcr = reader.getChannel();
				fcr.transferTo(0, fcr.size(), fcw);
				writer.close();
				reader.close();
			}
			this.config.load(new BufferedReader(new FileReader(configDIR)));
			//config.store(new FileOutputStream(configDIR, false), "LogicSimulator Config");
			if(this.config.getProperty("asd") != null)
			{
				LogicCore.putConsole(config.getProperty("asd"));
			}
			for(String imgFile : this.getFileList(IMG_DIR_NAME))
			{
				BufferedImage img = ImageIO.read(this.getFile("/" + IMG_DIR_NAME + "/" + imgFile));
				if(imgFile.contains("_DIRECTION."))
				{
					images.put(imgFile.split("[.]")[0].replace("DIRECTION", Direction.EAST.getTag()), this.getRotatedImage(img, Math.PI / 2, img.getHeight(), 0));
					images.put(imgFile.split("[.]")[0].replace("DIRECTION", Direction.WEST.getTag()), this.getRotatedImage(img, -Math.PI / 2, 0, img.getWidth()));
					images.put(imgFile.split("[.]")[0].replace("DIRECTION", Direction.SOUTH.getTag()), this.getRotatedImage(img, Math.PI, img.getWidth(), img.getHeight()));
					images.put(imgFile.split("[.]")[0].replace("DIRECTION", Direction.NORTH.getTag()), img);
				}
				else
				{
					images.put(imgFile.split("[.]")[0], img);
				}
				
			}
			BufferedReader in;
			in = new BufferedReader(new FileReader(this.getFile("/" + LANG_DIR_NAME + "/" + LANG_DFT_TAG_FILE_NAME)));
			
			
			String line;
			while((line = in.readLine()) != null)
			{
				if(!line.isEmpty() && line.charAt(0) != '#')
				{
					LogicCore.putConsole(line);
					this.local.put(line, line);
				}
			}
			in.close();
			for(String fileName : this.getFileList(LANG_DIR_NAME))
			{
				if(!fileName.equals(LANG_DFT_TAG_FILE_NAME))
				{
					in = new BufferedReader(new InputStreamReader(new FileInputStream(this.getFile("/" + LANG_DIR_NAME + "/" + fileName)), "UTF-8"));
					line = in.readLine();
					String key = line.split(" = ")[0];
					String value = line.split(LANG_SEPARATOR)[1];
					LogicCore.putConsole(key);
					LogicCore.putConsole(value);
					if(value.equals(this.getConfig("Language")))
					{
						LogicCore.putConsole("LOAD");
						while((line = in.readLine()) != null)
						{
							if(!line.isEmpty() && line.charAt(0) != '#')
							{
								if(line.split(LANG_SEPARATOR).length > 1 && this.local.containsKey(key = line.split(" = ")[0]))
								{
									value = line.split(LANG_SEPARATOR)[1];
									this.local.put(key, value);
									LogicCore.putConsole(value);
								}
							}
						}
					}
				}
			}
			NORMAL_FONT = Font.createFont(Font.TRUETYPE_FONT, new FileInputStream(this.getFile("/" + FONT_DIR_NAME + "/SeoulNamsanM.ttf")));
			BAR_FONT = Font.createFont(Font.TRUETYPE_FONT, new FileInputStream(this.getFile("/" + FONT_DIR_NAME + "/D2Coding.ttc")));
			PIXEL_FONT = Font.createFont(Font.TRUETYPE_FONT, new FileInputStream(this.getFile("/" + FONT_DIR_NAME + "/HOOG0557.ttf")));
		}
		catch (Exception e)
		{
			LogicCore.putConsole(e.toString());
		}
	}
	private BufferedImage getRotatedImage(BufferedImage img, double angle, int startX, int startY)
	{
		AffineTransform transform = AffineTransform.getTranslateInstance(startX, startY);
		transform.rotate(angle);
	    AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
	    return op.filter(img, null);
	}
	String getConfig(String key)
	{
		return (String)this.config.get(key);
	}
	String getLocal(String key)
	{
		return this.local.get(key);
	}
	BufferedImage getImage(String tag)
	{
		return this.images.get(tag);
	}
	ArrayList<String> getFileList(String dir) throws IOException
	{
		ArrayList<String> fileList = new ArrayList<String>();
		boolean isZipRead = false;
		CodeSource src = Resource.class.getProtectionDomain().getCodeSource();
		if(src != null)
		{
			LogicCore.putConsole("JAR MODE");
			URL jar = src.getLocation();
			ZipInputStream zip = new ZipInputStream(jar.openStream());
			ZipEntry ze = null;
			while((ze = zip.getNextEntry()) != null)
			{
				isZipRead = true;
				String entryName = ze.getName();
				if(entryName.startsWith(dir))
				{
					String fileName = entryName.replace(dir + "/", "");
					if(fileName.length() > 0)
					{
						fileList.add(fileName);
					} 
				}
			}
		}
		if(!isZipRead)
		{//이클립스에서만
			LogicCore.putConsole("IDE MODE");
			String dirName = new File(getClass().getResource("../../" + dir).getFile()).toString();
			for(File file : new File(dirName).listFiles())
			{
				fileList.add(file.toString().replace(dirName + "\\", ""));
			}
		}
		return fileList;
	}
	File getFile(String tag) throws IOException
	{//"http://stackoverflow.com/questions/941754/how-to-get-a-path-to-a-resource-in-a-java-jar-file#comment750014_941754"
		File file = null;
		String resource = tag;
		URL res = getClass().getResource(resource);
		if(res.toString().startsWith("jar:"))
		{
			LogicCore.putConsole("JAR MODE ");
			InputStream input = getClass().getResourceAsStream(resource);
			file = File.createTempFile("tempfile", ".tmp");
			OutputStream out = new FileOutputStream(file);
			int read;
			byte[] bytes = new byte[1024];
			while((read = input.read(bytes)) != -1)
			{
				out.write(bytes, 0, read);
			}
			out.close();
			input.close();
			file.deleteOnExit();
		}
		else
		{//this will probably work in your IDE, but not from a JAR
			LogicCore.putConsole("IDE MODE ");
			file = new File(res.getFile());
		}
		if(file != null && !file.exists())
		{
			throw new RuntimeException("Error: File " + file + " not found!");
		}
		else
		{
			LogicCore.putConsole("LOAD: " + file.toString());
		}
		
		return file;
	}
}
class LoadingWindow extends JFrame implements Console
{
	private static final long serialVersionUID = 1L;
	private static Color backgroundColor = new Color(220, 225, 230);
	private JTextArea consoleArea;

	LoadingWindow()
	{
		super("Loading...");
		LogicCore.registerConsole(this);
		this.setUndecorated(true);
		this.setAlwaysOnTop(true);
		this.setSize(400, 300);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(screenSize.width/2-this.getSize().width/2, screenSize.height/2-this.getSize().height/2);
		
		JButton closeButton = new JButton();
		closeButton.setHorizontalAlignment(SwingConstants.CENTER);
		closeButton.setVerticalAlignment(SwingConstants.CENTER);
		closeButton.setBounds(370, 10, 20, 20);
		closeButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				System.exit(0);
			}
		});
		
		JLabel titleLabel = new JLabel("논리회로 시뮬레이터 v" + LogicCore.VERSION);
		titleLabel.setOpaque(true);
		titleLabel.setBackground(backgroundColor);
		titleLabel.setFont(new Font(titleLabel.getName(), Font.BOLD, 26));
		titleLabel.setHorizontalAlignment(JTextField.CENTER);
		titleLabel.setPreferredSize(new Dimension(400, 100));

		this.consoleArea = new JTextArea();
		this.consoleArea.setBackground(backgroundColor);
		this.consoleArea.setEditable(false);
		this.consoleArea.setLineWrap(true);
		
		JScrollPane consolScrollPane = new JScrollPane();
		consolScrollPane.getViewport().setView(this.consoleArea);
		consolScrollPane.setBorder(BorderFactory.createEmptyBorder());
		consolScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		consolScrollPane.getVerticalScrollBar().setUnitIncrement(10);
		
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(consolScrollPane, BorderLayout.CENTER);
		panel.add(titleLabel, BorderLayout.NORTH);
		
		this.add(panel);
		this.getLayeredPane().add(closeButton, new Integer(2));
		this.setVisible(true);
	}
	@Override
	public void put(String str)
	{
		this.consoleArea.append(str + "\n");
		this.consoleArea.setCaretPosition(this.consoleArea.getDocument().getLength());
	}
	void remove()
	{
		LogicCore.removeConsole(this);
		this.dispose();
	}
}
interface Console
{
	void put(String str);
}