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
import java.io.DataInputStream;
import java.io.DataOutputStream;
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
import java.net.InetAddress;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;

public class LogicCore
{
	public static final String VERSION = "0.1.0";
	public static final String JARLOC = System.getProperty("user.dir");
	
	private static ArrayList<LogicCore> Task;
	private static ArrayList<Console> Consols;
	
	public static Resource RES;
	
	private SessionManager session;
	private TaskOperator taskOperator;
	private UI logicUI;
	
	private static boolean Login = false;
	private static String ID;
	private static String PassWord;
	
	public static void main(String[] args)
	{
		Task = new ArrayList<LogicCore>();
		Consols = new ArrayList<Console>();
		LoadingWindow window = new LoadingWindow();
		RES = new Resource();
		LogicCore create = createInstance();
		File loadFile = new File(RES.getConfig("OpenFile"));
		if(loadFile.isFile())
		{
			create.getSession().getFocusSession().loadData(loadFile);
		}
		window.remove();
	}
	public static Resource getResource()
	{
		return RES;
	}
	static LogicCore createInstance()
	{	
		LogicCore core = new LogicCore();
		Task.add(core);
		return core;
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
		String printStr = " ["+ new SimpleDateFormat("HH:mm:ss").format(new Date()) + "] " + str;
		System.out.println(printStr);
		for(Console console : Consols)
		{
			console.put(printStr);
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
	static boolean getLoginStatus()
	{
		return Login;
	}
	static boolean Login(String id, String pass)
	{
		putConsole("Login: " + id);
		Socket socket = null;
		boolean result = false;
		try
		{
			socket = new Socket(InetAddress.getByName(RES.getConfig("Provider")), 9002);
			
			OutputStream out = socket.getOutputStream();
			InputStream in = socket.getInputStream();
			
			DataInputStream in2 = new DataInputStream(in);
			DataOutputStream out2 = new DataOutputStream(out);
			
			out2.writeInt(1);//1번 함수를 실행시키도록 값을 전달.
	
			out2.writeUTF(id);
			out2.flush();
			out2.writeUTF(pass);
			out2.flush();
			
			result = in2.readBoolean();
			
			out2.close();
			in2.close();
		}
		catch(Exception e)
		{
			putConsole(e.toString());
		}
		finally
		{
			try
			{
				socket.close();
			}
			catch(Exception e)
			{
				putConsole(e.toString());
			}
		}
		if(result)
		{
			putConsole("Login Success: " + id);
			ID = id;
			PassWord = pass;
			RES.setConfig("ID", id);
			Login = true;
		}
		else
		{
			putConsole("Bad Login: " + id);
		}
		return result;//로그인 성공결과 리턴.
	}
	private LogicCore()
	{
		this.logicUI = new UI(this);
		this.session = new SessionManager(this);
		
		this.taskOperator = new TaskOperator(this);
		
		ClipBoardPanel.addInstance(this);
		/*누수 테스트용 코드
		while(true)
		{
			session.removeSession(session.createSession());
		}*/
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
	private File configDIR;
	
	private HashMap<String, BufferedImage> images = new HashMap<String, BufferedImage>();
	private HashMap<String, String> local = new HashMap<String, String>();
	private ArrayList<String> localList = new ArrayList<String>();
	public Font NORMAL_FONT;
	public Font PIXEL_FONT;
	public Font BAR_FONT;
	
	private Properties config = new Properties();
	Resource()
	{
		try
		{
			
			this.configDIR = new File(new File(Resource.class.getProtectionDomain().getCodeSource().getLocation().getPath() + "../../" + CONFIG_FILE_NAME).getCanonicalPath());//if(!configDIR.isFile())
			if(!this.configDIR.isFile())//TODO
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
					LogicCore.putConsole("Language: " + value);
					this.localList.add(value);
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
	void setConfig(String key, String value)
	{
		this.config.setProperty(key, value);
		LogicCore.putConsole("SetConfig: " + key + " " + value);
		try
		{
			this.config.store(new FileOutputStream(this.configDIR), "Last: " + key);
		}
		catch (IOException e)
		{
			LogicCore.putConsole(e.toString());
		}
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
	ArrayList<String> getLocalList()
	{
		return this.localList;
	}
	ArrayList<String> getFileList(String dir)
	{
		ArrayList<String> fileList = new ArrayList<String>();
		try
		{
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
		}
		catch(Exception e)
		{
			LogicCore.putConsole(e.toString());
		}
		return fileList;
	}
	File getFile(String tag)
	{//"http://stackoverflow.com/questions/941754/how-to-get-a-path-to-a-resource-in-a-java-jar-file#comment750014_941754"
		File file = null;
		try
		{
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
			
		}
		catch(Exception e)
		{
			LogicCore.putConsole(e.toString());
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
class DialogWindow
{
	protected JDialog dialog;
	protected DialogWindow(int w, int h, String nameTag)
	{
		this.dialog = new JDialog();
		this.dialog.setTitle(LogicCore.getResource().getLocal(nameTag));
		this.dialog.setSize(w, h);
		this.dialog.setResizable(false);
		this.dialog.setLayout(null);
		this.dialog.setAlwaysOnTop(true);
	}
	void toggleShow(int x, int y)
	{
		this.dialog.setVisible(!this.dialog.isVisible());
		if(this.dialog.isVisible())
		{
			this.dialog.setLocation(x, y);
		}
	}
}
class ConsoleWindow extends DialogWindow implements Console
{
	public static final ConsoleWindow Console = new ConsoleWindow();
	private JTextArea consoleArea;
	private ConsoleWindow()
	{
		super(500, 300, "Log");
		super.dialog.setLayout(new BorderLayout());
		
		this.consoleArea = new JTextArea();
		this.consoleArea.setEditable(false);
		this.consoleArea.setLineWrap(true);
		
		JScrollPane consolScrollPane = new JScrollPane();
		consolScrollPane.getViewport().setView(this.consoleArea);
		consolScrollPane.setBorder(BorderFactory.createEmptyBorder());
		consolScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		consolScrollPane.getVerticalScrollBar().setUnitIncrement(10);
		
		this.dialog.add(consolScrollPane, BorderLayout.CENTER);
		LogicCore.registerConsole(this);
	}
	@Override
	public void put(String str)
	{
		this.consoleArea.append(str + "\n");
		if(this.consoleArea.getLineCount() > 300)
		{
			try
			{
				this.consoleArea.replaceRange("", 0, this.consoleArea.getLineEndOffset(0));
			}
			catch(BadLocationException e)
			{
				LogicCore.putConsole(e.toString());
			}
		}
		this.consoleArea.setCaretPosition(this.consoleArea.getDocument().getLength());
	}
}
class SettingWindow extends DialogWindow
{
	public static final SettingWindow Setting = new SettingWindow();
	public static final int Width = 300;
	public static final int Height = 155;
	
	private JTextField providerEdit;
	
	private SettingWindow()
	{
		super(Width, Height, "Setting");
		super.dialog.setLayout(null);
		JLabel langTag = new JLabel("Language:");
		langTag.setFont(LogicCore.RES.NORMAL_FONT.deriveFont(14f));
		langTag.setBounds(20, 20, 120, 20);
		JComboBox<String> langCombo = new JComboBox<String>();
		langCombo.setBounds(Width - 150, 20, 130, 20);
		for(String local : LogicCore.getResource().getLocalList())
		{
			langCombo.addItem(local);
		}
		langCombo.setSelectedItem(LogicCore.getResource().getConfig("Language"));
		langCombo.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				LogicCore.getResource().setConfig("Language", (String)langCombo.getSelectedItem());
			}
		});
		JLabel provider = new JLabel(LogicCore.getResource().getLocal("Provider") + ":");
		provider.setFont(LogicCore.RES.NORMAL_FONT.deriveFont(14f));
		provider.setBounds(20, 60, 120, 20);
		
		this.providerEdit = new JTextField();
		this.providerEdit.setBounds(20, 85, Width - 40, 20);
		
		JButton setProvider = new JButton(LogicCore.getResource().getLocal("OK"));
		setProvider.setFont(LogicCore.RES.NORMAL_FONT.deriveFont(14f));
		setProvider.setBounds(Width - 80, 60, 60, 20);
		setProvider.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				LogicCore.getResource().setConfig("Provider", providerEdit.getText());
			}
		});
		
		super.dialog.add(langTag);
		super.dialog.add(langCombo);
		super.dialog.add(provider);
		super.dialog.add(setProvider);
		super.dialog.add(this.providerEdit);
	}
	@Override
	void toggleShow(int x, int y)
	{
		super.toggleShow(x, y);
		this.providerEdit.setText(LogicCore.getResource().getConfig("Provider"));
	}
}
class HelpWindow extends DialogWindow
{
	public static final HelpWindow Help = new HelpWindow();
	public static final int Width = 420;
	public static final int Height = 500;
	private BufferedImage image;
	private JPanel manualPanel = new JPanel()
	{
		private static final long serialVersionUID = 1L;

		@Override
		public void paint(Graphics g)
		{
			if(image != null)
			{
				g.drawImage(image, 0, 0, this);
			}
		}
	};
	private JScrollPane viewScroll;
	private HelpWindow()
	{
		super(Width, Height, "Manual");
		super.dialog.setLayout(new BorderLayout());
		
		this.viewScroll = new JScrollPane();
		this.viewScroll.getViewport().setView(this.manualPanel);
		this.viewScroll.setBorder(BorderFactory.createEmptyBorder());
		this.viewScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		this.viewScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		this.viewScroll.getVerticalScrollBar().setUnitIncrement(10);
		
		super.dialog.add(viewScroll, BorderLayout.CENTER);
	}
	@Override
	void toggleShow(int x, int y)
	{
		super.toggleShow(x, y);
		this.image = LogicCore.getResource().getImage("Manual_" + LogicCore.getResource().getConfig("Language"));
		this.manualPanel.setPreferredSize(new Dimension(this.image.getWidth(), this.image.getHeight()));
	}
}
class LoginWindow extends DialogWindow
{
	public static final LoginWindow Login = new LoginWindow();
	public static final int Width = 300;
	public static final int Height = 170;
	
	private JTextField idField;
	private JPasswordField passField;
	private JButton loginButton;
	
	private LoginWindow()
	{
		super(Width, Height, "Login");
		
		JLabel idLabel = new JLabel(LogicCore.getResource().getLocal("ID"));
		idLabel.setFont(LogicCore.RES.NORMAL_FONT.deriveFont(14f));
		idLabel.setBounds(20, 20, 80, 25);
		
		this.idField = new JTextField();
		this.idField.setBounds(100, 20, 180, 25);
		
		JLabel passLabel = new JLabel(LogicCore.getResource().getLocal("PW"));
		passLabel.setFont(LogicCore.RES.NORMAL_FONT.deriveFont(14f));
		passLabel.setBounds(20, 50, 80, 25);
		
		this.passField = new JPasswordField();
		this.passField.setBounds(100, 50, 180, 25);
		
		this.loginButton = new JButton(LogicCore.getResource().getLocal("Login"));
		this.loginButton.setFont(LogicCore.RES.NORMAL_FONT.deriveFont(14f));
		this.loginButton.setBounds(100, 90, 100, 30);
		this.loginButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if(LogicCore.Login(idField.getText(), new String(passField.getPassword())))
				{
					dialog.setVisible(false);
				}
			}
		});
		
		super.dialog.add(idLabel);
		super.dialog.add(this.idField);
		super.dialog.add(passLabel);
		super.dialog.add(this.passField);
		super.dialog.add(this.loginButton);
	}
	@Override
	void toggleShow(int x, int y)
	{
		super.toggleShow(x, y);
		this.idField.setText(LogicCore.getResource().getConfig("ID"));
	}
}
class DataBranch
{
	private final String name;
	private LinkedHashMap<String, String> data;
	private ArrayList<DataBranch> lowerBranch;
	DataBranch(String name)
	{
		this.name = name;
		this.data = new LinkedHashMap<String, String>();
		this.lowerBranch = new ArrayList<DataBranch>();
	}
	String getName()
	{
		return this.name;
	}
	void setData(String key, String value)
	{
		this.data.put(key, value);
	}
	String getData(String key)
	{
		return this.data.get(key);
	}
	boolean isData(String key)
	{
		if(this.data.containsKey(key))
		{
			return true;
		}
		return false;
	}
	void addLowerBranch(DataBranch executeData)
	{
		this.lowerBranch.add(executeData);
	}
	Iterator<String> getDataKeySetIterator()
	{
		return this.data.keySet().iterator();
	}
	Iterator<DataBranch> getLowerBranchIterator()
	{
		return this.lowerBranch.iterator();
	}
}