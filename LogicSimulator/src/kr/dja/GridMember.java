package kr.dja;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.UUID;

import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;

public abstract class GridMember implements SizeUpdate
{
	protected int UIabslocationX = 0;
	protected int UIabslocationY = 0;
	protected int UIabsSizeX = 1;
	protected int UIabsSizeY = 1;
	protected LogicCore core;
	protected String name;
	protected GridLayeredPane layeredPane;
	protected GridViewPane gridViewPane;
	protected Grid grid;
	private UUID id;
	private boolean placement = false;

	protected GridMember(LogicCore core, String name)
	{
		this.core = core;
		this.id = UUID.randomUUID();
		this.name = name;
		this.layeredPane = new GridLayeredPane();
		this.gridViewPane = this.createViewPane();
		this.layeredPane.add(this.gridViewPane, new Integer(0));
	}
	protected GridViewPane createViewPane()
	{
		return new GridViewPane(this);
	}
	public void setData(DataBranch branch)
	{
		this.id = UUID.fromString(branch.getData("id"));
		this.name = branch.getData("name");
		this.UIabslocationX = new Integer(branch.getData("UIabslocationX"));
		this.UIabslocationY = new Integer(branch.getData("UIabslocationY"));
		this.UIabsSizeX = new Integer(branch.getData("UIabsSizeX"));
		this.UIabsSizeY = new Integer(branch.getData("UIabsSizeY"));
		this.gridViewPane.repaint();
	}
	public DataBranch getData(DataBranch branch)
	{
		branch.setData("ClassPath", this.getClass().getName());
		branch.setData("id", this.id.toString());
		branch.setData("name", this.name);
		branch.setData("UIabslocationX", Integer.toString(this.UIabslocationX));
		branch.setData("UIabslocationY", Integer.toString(this.UIabslocationY));
		branch.setData("UIabsSizeX", Integer.toString(this.UIabsSizeX));
		branch.setData("UIabsSizeY", Integer.toString(this.UIabsSizeY));
		branch.setData("placement", Boolean.toString(this.placement));
		if(this.grid != null)
		{
			branch.setData("grid", this.grid.getUUID().toString());
		}
		return branch;
	}
	LogicCore getCore()
	{
		return this.core;
	}
	void setCore(LogicCore core)
	{
		this.core = core;
	}
	void setUUID()
	{
		this.id = UUID.randomUUID();
	}
	public UUID getUUID()
	{
		return this.id;
	}
	void setSelectView(Color color)
	{
		this.layeredPane.selectShow(color);
	}
	void removeSelectView()
	{
		this.layeredPane.deSelectShow();
	}
	String getName()
	{
		return this.name;
	}
	public int getUIabsLocationX()
	{
		return UIabslocationX;
	}
	public int getUIabsLocationY()
	{
		return UIabslocationY;
	}
	public int getUIabsSizeX()
	{
		return UIabsSizeX;
	}
	public int getUIabsSizeY()
	{
		return UIabsSizeY;
	}
	void put(int absX, int absY, Grid grid)
	{
		this.sizeUpdate();
		this.UIabslocationX = absX;
		this.UIabslocationY = absY;
		this.grid = grid;
		LogicCore.putConsole("Put id: " + this.id.toString());
		LogicCore.putConsole("Put absLocation: " + UIabslocationX + " " + UIabslocationY);
		this.placement = true;
	}
	void remove()
	{
		this.placement = false;
	}
	boolean isPlacement()
	{
		return this.placement;
	}
	BufferedImage getSnapShot()
	{
		this.sizeUpdate();
		BufferedImage img = new BufferedImage(this.gridViewPane.getWidth(), this.gridViewPane.getHeight(), BufferedImage.TYPE_INT_ARGB);
		this.gridViewPane.printAll(img.getGraphics());
		return img;
	}
	JLayeredPane getGridViewPane()
	{
		this.sizeUpdate();
		return this.layeredPane;
	}
	public Grid getGrid()
	{
		return this.grid;
	}
	@Override
	public void sizeUpdate()
	{
		this.gridViewPane.sizeUpdate();
		this.layeredPane.setSize(UIabsSizeX * core.getUI().getUISize().getmultiple(), UIabsSizeY * core.getUI().getUISize().getmultiple());
	}
	class GridLayeredPane extends JLayeredPane implements MouseListener, MouseMotionListener
	{
		private static final long serialVersionUID = 1L;
		
		private Color rectColor;
		boolean onMouseFlag;
		GridLayeredPane()
		{
			this.addMouseListener(this);
			this.addMouseMotionListener(this);
			this.setLayout(null);
		}
		void selectShow(Color color)
		{
			this.rectColor = color;
			this.repaint();
		}
		void deSelectShow()
		{
			this.rectColor = null;
			this.repaint();
		}
		@Override
		public void paint(Graphics g)
		{
			super.paint(g);
			if(this.rectColor != null)
			{
				g.setColor(this.rectColor);
				g.fillRect(gridViewPane.getX(), gridViewPane.getY(), gridViewPane.getWidth(), gridViewPane.getHeight());
				g.setColor(new Color(this.rectColor.getRed(), this.rectColor.getGreen(), this.rectColor.getBlue()));
				g.drawRect(gridViewPane.getX(), gridViewPane.getY(), gridViewPane.getWidth() - 1, gridViewPane.getHeight() - 1);
			}
		}
		@Override
		public void mouseDragged(MouseEvent arg0){}
		@Override
		public void mouseMoved(MouseEvent arg0){}
		@Override
		public void mouseClicked(MouseEvent arg0){}
		@Override
		public final void mouseEntered(MouseEvent e)
		{
			this.onMouseFlag = true;
		}
		@Override
		public final void mouseExited(MouseEvent e)
		{
			this.onMouseFlag = false;
		}
		@Override
		public void mousePressed(MouseEvent e){}
		@Override
		public final void mouseReleased(MouseEvent e)
		{
			if(this.onMouseFlag)
			{
				if(e.getButton() == 1)
				{
					if(!grid.isFocusSelect(GridMember.this))
					{
						grid.selectFocus(GridMember.this);
					}
				}
				else
				{
					if(grid.isFocusSelect(GridMember.this))
					{
						grid.deSelectFocus();
					}
					else if(grid.isSelect(GridMember.this))
					{
						grid.deSelect(GridMember.this);
					}
					else
					{
						ArrayList<GridMember> temp = new ArrayList<GridMember>();
						temp.add(GridMember.this);
						grid.select(temp);
					}
				}
			}
		}
	}
	static GridMember Factory(LogicCore core, DataBranch info)
	{
		GridMember member = null;
		try
		{//리플렉션
			member = (GridMember)Class.forName(info.getData("ClassPath")).getDeclaredConstructor(new Class[]{LogicCore.class}).newInstance(core);
			member.setData(info);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return member;
	}
}
interface ColorSet
{
	void setColor(String tag, Color color);
	Color getColor(String tag);
	UUID getUUID();
}
interface SizeSet
{
	void setSize(int x, int y, int w, int h);
	int getUIabsLocationX();
	int getUIabsLocationY();
	int getUIabsSizeX();
	int getUIabsSizeY();
	int getMinSize();
	UUID getUUID();
}
class Partition extends GridMember
{
	Partition(LogicCore core)
	{
		super(core, "Partition");
	}
}
class Tag extends GridMember implements ColorSet, SizeSet
{
	private JTextArea viewTextArea;
	private JTextArea editTextArea;
	private ButtonPanel editCompleatButton;
	private Color color;
	
	private String description;
	
	private boolean isShow = false;
	
	Tag(LogicCore core)
	{
		super(core, "Tag");
		super.UIabsSizeX = 60;
		super.UIabsSizeY = 60;
		
		this.viewTextArea = new JTextArea();
		this.viewTextArea.setBackground(super.gridViewPane.getBackground());
		for(MouseListener l : this.viewTextArea.getMouseListeners())
		{
			this.viewTextArea.removeMouseListener(l);
		}
		for(MouseMotionListener l : this.viewTextArea.getMouseMotionListeners())
		{
			this.viewTextArea.removeMouseMotionListener(l);
		}
		this.viewTextArea.addMouseListener(super.layeredPane);
		this.viewTextArea.addMouseMotionListener(super.layeredPane);
		this.viewTextArea.setFocusable(false);
		this.viewTextArea.setLineWrap(true);
		this.viewTextArea.setLocation(5, 5);
		
		this.editTextArea = new JTextArea();
		this.editTextArea.setLineWrap(true);
		this.editTextArea.setLocation(5, 5);
		this.editTextArea.setBorder(new EtchedBorder(EtchedBorder.RAISED));
		
		this.setColor("BackGround", new Color(255, 230, 153));
		this.description = new String();
		
		this.editCompleatButton = new ButtonPanel(16, 16)
		{
			private static final long serialVersionUID = 1L;
			@Override
			void pressed(int button)
			{
				toggleModeActive();
			}
		};
		this.editCompleatButton.setVisible(false);
		
		super.layeredPane.add(this.editCompleatButton, new Integer(3));
		super.gridViewPane.add(this.viewTextArea);
	}
	@Override
	public void setSize(int x, int y, int w, int h)
	{
		Size size = this.getCore().getUI().getUISize();
		SizeInfo gSize = grid.getGridSize();
		super.UIabslocationX = x;
		super.UIabslocationY = y;
		super.UIabsSizeX = w;
		super.UIabsSizeY = h;
		super.layeredPane.setLocation((x + gSize.getNX() * Size.REGULAR_SIZE) * size.getmultiple() + Size.MARGIN
								   , (y + gSize.getNY() * Size.REGULAR_SIZE) * size.getmultiple() + Size.MARGIN);
		super.layeredPane.setSize(UIabsSizeX * core.getUI().getUISize().getmultiple(), UIabsSizeY * core.getUI().getUISize().getmultiple());
		this.sizeUpdate();
	}
	@Override
	public void setData(DataBranch branch)
	{
		super.setData(branch);
		Iterator<DataBranch> itr = branch.getLowerBranchIterator();
		while(itr.hasNext())
		{
			DataBranch lowerBranch = itr.next();
			switch(lowerBranch.getName())
			{
			case "BackGroundColor":
				this.setColor("BackGround", new Color(Integer.parseInt(lowerBranch.getData("red"))
						, Integer.parseInt(lowerBranch.getData("green"))
						, Integer.parseInt(lowerBranch.getData("blue"))));
				break;
			case "Description":
				String text = new String();
				Iterator<String> keyItr = lowerBranch.getDataKeySetIterator();
				while(keyItr.hasNext())
				{
					text += lowerBranch.getData(keyItr.next()) + "\n";
				}
				this.description = text;
				this.viewTextArea.setText(text);
				break;
			}
		}
	}
	@Override
	public DataBranch getData(DataBranch branch)
	{
		DataBranch description = new DataBranch("Description");
		String[] text = this.viewTextArea.getText().split("\\r?\\n");
		for(int i = 0; i < text.length; i++)
		{
			description.setData(Integer.toString(i), text[i]);
		}
		branch.addLowerBranch(description);

		DataBranch colorBranch = new DataBranch("BackGroundColor");
		colorBranch.setData("red", Integer.toString(this.color.getRed()));
		colorBranch.setData("green", Integer.toString(this.color.getGreen()));
		colorBranch.setData("blue", Integer.toString(this.color.getBlue()));
		branch.addLowerBranch(colorBranch);
		return super.getData(branch);
	}
	@Override
	public int getMinSize()
	{
		return 15;
	}
	@Override
	public void setColor(String tag, Color color)
	{
		switch(tag)
		{
		case "BackGround":
			this.color = color;
			super.gridViewPane.setBackground(this.color);
			this.viewTextArea.setBackground(this.color);
			break;
		}
	}
	String getDescription()
	{
		return this.description;
	}
	void setDescription(String description)
	{
		this.description = description;
		this.viewTextArea.setText(description);
	}
	void toggleModeActive()
	{
		if(!this.isShow)
		{
			if(super.isPlacement())
			{
				this.grid.deSelectFocus();
			}
			this.editCompleatButton.setVisible(true);
			this.editTextArea.setText(this.description);
			super.gridViewPane.remove(this.viewTextArea);
			super.gridViewPane.add(this.editTextArea);
			super.layeredPane.repaint();
			this.isShow = true;
		}
		else
		{
			this.editCompleatButton.setVisible(false);
			if(super.isPlacement())
			{
				TaskUnit task = this.getGrid().getSession().getTaskManager().setTask();
				task.addCommand(new SetTagDescription(this, this.editTextArea.getText(), super.getCore().getSession().getFocusSession()));
			}
			this.gridViewPane.remove(this.editTextArea);
			this.gridViewPane.add(this.viewTextArea);
			this.layeredPane.repaint();
			this.isShow = false;
		}
	}
	@Override
	protected GridViewPane createViewPane()
	{
		return new ResizeableViewPane(this);
	}
	@Override
	void put(int x, int y, Grid grid)
	{
		super.put(x, y, grid);
		super.layeredPane.setLocation((super.getUIabsLocationX() + (grid.getGridSize().getNX() * Size.REGULAR_SIZE)) * super.core.getUI().getUISize().getmultiple() + Size.MARGIN
				  , (super.getUIabsLocationY() + (grid.getGridSize().getNY() * Size.REGULAR_SIZE)) * super.core.getUI().getUISize().getmultiple() + Size.MARGIN);
		super.sizeUpdate();
	}
	@Override
	public Color getColor(String tag)
	{
		switch(tag)
		{
		case "BackGround":
			return this.color;
			
		}
		return null;
	}
	@Override
	public void sizeUpdate()
	{
		super.sizeUpdate();
		Font font = LogicCore.RES.NORMAL_FONT.deriveFont(super.core.getUI().getUISize().getmultiple() * 7f);
		this.viewTextArea.setFont(font);
		this.editTextArea.setFont(font);
		this.viewTextArea.setSize(super.layeredPane.getWidth() - 10, super.layeredPane.getHeight() - 10);
		this.editTextArea.setSize(super.layeredPane.getWidth() - 10, super.layeredPane.getHeight() - 10);
		this.editCompleatButton.setLocation(super.layeredPane.getWidth() - 24, super.layeredPane.getHeight() - 24);
	}
}
abstract class LogicBlock extends GridMember
{
	protected int blocklocationX = 0;
	protected int blocklocationY = 0;
	protected Power power = Power.OFF;
	protected LinkedHashMap<Direction, IOPanel> io = new LinkedHashMap<Direction, IOPanel>();
	private int timer = 0;
	private boolean active = true;
	private JPanel disableView;
	private JPanel iconPanel;
	private JLabel textLabel;
	
	protected LogicBlock(LogicCore core, String name)
	{
		super(core, name);
		super.UIabsSizeX = 32;
		super.UIabsSizeY = 32;
		this.io.put(Direction.EAST, new IOPanel(this, Direction.EAST, "BASIC"));
		this.io.put(Direction.WEST, new IOPanel(this, Direction.WEST, "BASIC"));
		this.io.put(Direction.SOUTH, new IOPanel(this, Direction.SOUTH, "BASIC"));
		this.io.put(Direction.NORTH, new IOPanel(this, Direction.NORTH, "BASIC"));
		this.disableView = new JPanel()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void paint(Graphics g)
			{
				super.paint(g);
				g.drawImage(LogicCore.getResource().getImage(core.getUI().getUISize().getTag() + "_BLOCK_DISABLE"), 0, 0, this);
				super.paintChildren(g);
			}
		};
		this.disableView.setOpaque(false);
		
		this.iconPanel = new JPanel()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void paint(Graphics g)
			{
				BufferedImage paintImage = LogicCore.getResource().getImage(core.getUI().getUISize().getTag() + "_" + LogicBlock.this.getName());
				if(paintImage != null)
				{
					g.drawImage(paintImage, 0, 0, this);
				}
			}
		};
		this.iconPanel.setOpaque(false);
		this.iconPanel.setLocation(0, 0);
		
		this.textLabel = new JLabel(this.name);
		this.textLabel.setVerticalAlignment(JTextField.CENTER);
		this.textLabel.setHorizontalAlignment(JTextField.CENTER);
		this.textLabel.setLocation(0, 0);
	}
	void updateViewMode()
	{
		if(this.core.getUI().getView() == ViewMode.SIGN)
		{
			this.gridViewPane.remove(this.textLabel);
			this.gridViewPane.add(this.iconPanel);
		}
		else if(this.core.getUI().getView() == ViewMode.TEXT)
		{
			this.gridViewPane.remove(this.iconPanel);
			this.gridViewPane.add(this.textLabel);
		}
		super.layeredPane.repaint();
	}
	@Override
	protected GridViewPane createViewPane()
	{
		return new StandardLogicViewPane(this);
	}
	@Override
	public void setData(DataBranch branch)
	{
		this.blocklocationX = new Integer(branch.getData("blocklocationX"));
		this.blocklocationY = new Integer(branch.getData("blocklocationY"));
		this.setTimer(new Integer(branch.getData("timer")));
		this.power = Power.valueOf(branch.getData("power"));
		for(Direction ext : Direction.values())
		{
			String data = branch.getData("io_" + ext);
			io.get(ext).setStatus(IOStatus.valueOf(data.split("_")[0]));
			io.get(ext).setOnOffStatus(Power.valueOf(data.split("_")[1]));
		}
		this.calculate();
		if(super.isPlacement())
		{
			super.core.getTaskOperator().checkAroundAndReserveTask(this);
		}
		this.calculate();
		this.core.getTaskOperator().addReserveTask(this);
		this.setActive(Boolean.valueOf(branch.getData("active")));
		super.setData(branch);
	}
	@Override
	public DataBranch getData(DataBranch branch)
	{
		super.getData(branch);
		branch.setData("blocklocationX", Integer.toString(this.blocklocationX));
		branch.setData("blocklocationY", Integer.toString(this.blocklocationY));
		branch.setData("timer", Integer.toString(this.timer));
		branch.setData("power", power.toString());
		for(Direction ext : Direction.values())
		{
			branch.setData("io_" + ext, this.io.get(ext).getStatus().toString() + "_" + this.io.get(ext).getOnOffStatus().toString());
		}
		branch.setData("active", Boolean.toString(this.active));
		return branch;
	}
	@Override
	void put(int absX, int absY, Grid grid)
	{
		absX = absX / Size.REGULAR_SIZE;
		absY = absY / Size.REGULAR_SIZE;
		
		this.blocklocationX = absX;
		this.blocklocationY = absY;
		super.put((this.getBlockLocationX() * Size.REGULAR_SIZE), (this.getBlockLocationY() * Size.REGULAR_SIZE), grid);
		super.getGridViewPane().setLocation((super.getUIabsLocationX() + (grid.getGridSize().getNX() * Size.REGULAR_SIZE)) * super.core.getUI().getUISize().getmultiple() + Size.MARGIN
										  , (super.getUIabsLocationY() + (grid.getGridSize().getNY() * Size.REGULAR_SIZE)) * super.core.getUI().getUISize().getmultiple() + Size.MARGIN);
		LogicCore.putConsole("Put logicLocation: " + this.getBlockLocationX() + " " + this.getBlockLocationY());
	}
	@Override
	protected void remove()
	{
		super.remove();
	}
	public int getBlockLocationX()
	{
		return this.blocklocationX;
	}
	public int getBlockLocationY()
	{
		return this.blocklocationY;
	}
	ArrayList<Direction> getIOTrance()
	{
		ArrayList<Direction> returnInfo = new ArrayList<Direction>();
		for(Direction ext : this.io.keySet())
		{
			if(this.io.get(ext).getStatus() == IOStatus.TRANCE)
			{
				returnInfo.add(this.io.get(ext).getDirection());
			}
		}
		return returnInfo;
	}
	int getIOResiveCount()
	{
		int count = 0;
		for(Direction ext : this.io.keySet())
		{
			if(this.io.get(ext).getStatus() == IOStatus.RECEIV)
			{
				count++;
			}
		}
		return count;
	}
	protected void setPowerStatus(Power power)
	{
		this.power = power;
		for(Direction ext: this.io.keySet())
		{
			if(this.io.get(ext).getStatus() == IOStatus.TRANCE)
			{
				this.io.get(ext).setOnOffStatus(power);
			}
		}
		super.layeredPane.repaint();
	}
	final void toggleIO(Direction ext)
	{
		this.doToggleIO(ext);
		super.layeredPane.repaint();
		if(super.isPlacement())
		{
			super.core.getTaskOperator().checkAroundAndReserveTask(this);
		}
		LogicCore.putConsole("ToggleIO: " + super.getUUID() + " " + ext.getTag() + " " + this.getIOStatus(ext).getTag());
	}
	void setIO(Direction ext, IOStatus io)
	{
		this.io.get(ext).setStatus(io);
		super.layeredPane.repaint();
	}
	protected void doToggleIO(Direction ext)
	{
		if(this.getIOStatus(ext) == IOStatus.NONE)
		{
			this.io.get(ext).setStatus(IOStatus.TRANCE);
		}
		else if(this.getIOStatus(ext) == IOStatus.TRANCE)
		{
			this.io.get(ext).setStatus(IOStatus.RECEIV);
		}
		else if(this.getIOStatus(ext) == IOStatus.RECEIV)
		{
			this.io.get(ext).setStatus(IOStatus.NONE);
		}
	}
	void setIOResivePower(Direction ext, Power power)
	{
		if(this.io.get(ext).getStatus() == IOStatus.RECEIV)
		{
			this.io.get(ext).setOnOffStatus(power);
			this.calculate();
			super.layeredPane.repaint();
		}
	}
	public Power getIOPower(Direction ext)
	{
		return this.io.get(ext).getOnOffStatus();
	}
	ArrayList<Power> getResiveIOPower()
	{
		ArrayList<Power> returnPower = new ArrayList<Power>();
		for(Direction ext : this.io.keySet())
		{
			if(this.io.get(ext).getStatus() == IOStatus.RECEIV)
			{
				returnPower.add(this.io.get(ext).getOnOffStatus());
			}
		}
		return returnPower;
	}
	BufferedImage getIOImage(Direction ext)
	{
		return this.io.get(ext).getImage();
	}
	final void ping()
	{
		if(this.timer > 0)
		{
			this.core.getTaskOperator().addReserveTask(this);

			this.activeTimer();
			this.timer--;
		}
		else
		{
			this.endTimer();
		}
		this.operatorPing();
	}
	final void setTimer(int time)
	{
		this.timer = time;
		LogicCore.putConsole("SetTimer: " + super.getUUID() + " " + this.timer);
	}
	int getTimer()
	{
		return this.timer;
	}
	protected void operatorPing(){}
	protected void activeTimer(){}
	protected void endTimer()
	{
		this.timer = 0;
	}
	IOStatus getIOStatus(Direction ext)
	{
		return this.io.get(ext).getStatus();
	}
	Power getPower()
	{
		return this.power;
	}
	void setActive(boolean option)
	{
		if(this.active != option)
		{
			this.active = option;
			LogicCore.putConsole("SetActive: " + super.getUUID() + " " + option);
			if(option)
			{
				super.layeredPane.remove(this.disableView);
				if(super.isPlacement())
				{
					super.core.getTaskOperator().checkAroundAndReserveTask(this);
				}
			}
			else
			{
				this.disableView.setLocation(super.gridViewPane.getX(), super.gridViewPane.getY());
				this.disableView.setSize(super.gridViewPane.getWidth(), super.gridViewPane.getHeight());
				super.layeredPane.add(this.disableView, new Integer(3));
			}
			super.layeredPane.repaint();
		}
	}
	public boolean getActive()
	{
		return this.active;
	}
	@Override
	public void sizeUpdate()
	{
		super.sizeUpdate();
		this.iconPanel.setSize(super.gridViewPane.getWidth(), super.gridViewPane.getHeight());
		this.textLabel.setFont(LogicCore.RES.BAR_FONT.deriveFont((float)(12 * super.core.getUI().getUISize().getmultiple())));
		this.textLabel.setSize(super.gridViewPane.getWidth(), super.gridViewPane.getHeight());
		this.updateViewMode();
		this.disableView.setLocation(super.gridViewPane.getX(), super.gridViewPane.getY());
		this.disableView.setSize(super.gridViewPane.getWidth(), super.gridViewPane.getHeight());
	}
	void calculate(){}
}
interface LogicWire
{
	ArrayList<LogicWire> getRemoteWire();
	void setRemotePowerStatus(Power power);
	void setIOResivePower(Direction ext, Power power);
	Power getIOPower(Direction ext);
	Grid getGrid();
	int getBlockLocationX();
	int getBlockLocationY();
	boolean isWireValid(Direction ext);
	boolean isLinkedWire(Direction from, Direction to);
	boolean getActive();
}
interface TimeSetter
{
	void setTime(String tag, int time);
	int getTime(String tag);
}
class Wire extends LogicBlock implements LogicWire
{
	private WireType wireType;
	Wire(LogicCore core)
	{
		super(core, "Wire");
		this.wireType = WireType.Standard;
		for(Direction ext : Direction.values())
		{
			super.io.get(ext).setImgTag("WIRE");
		}
	}
	@Override
	void updateViewMode()
	{
	}
	@Override
	protected GridViewPane createViewPane()
	{
		return new WireViewPane(this);
	}
	@Override
	public void setData(DataBranch branch)
	{
		super.setData(branch);
		this.setWireType(WireType.valueOf(branch.getData("WireType")));
	}
	@Override
	public DataBranch getData(DataBranch branch)
	{
		super.getData(branch);
		branch.setData("WireType", this.wireType.toString());
		return branch;
	}
	@Override
	public void setIOResivePower(Direction ext, Power power)
	{
		this.io.get(ext).setOnOffStatus(power);
		for(Direction changeExt : this.wireType.getFromTo(ext))
		{
			super.io.get(changeExt).setOnOffStatus(power);
		}
		super.layeredPane.repaint();
	}
	@Override
	public boolean isWireValid(Direction ext)
	{
		if(super.getIOStatus(ext) != IOStatus.NONE)
		{
			return true;
		}
		return false;
	}
	@Override
	public boolean isLinkedWire(Direction from, Direction to)
	{
		if(this.wireType.getFromTo(from).contains(to))
		{
			return true;
		}
		return false;
	}
	@Override
	protected void doToggleIO(Direction ext)
	{
		if(this.wireType != WireType.Standard)
		{
			this.setWireType(WireType.Standard);
			super.getCore().getUI().getBlockControlPanel().updateMemberStatus();
		}
		if(super.getIOStatus(ext) == IOStatus.NONE)
		{
			super.io.get(ext).setStatus(IOStatus.TRANCE);
		}
		else
		{
			super.io.get(ext).setStatus(IOStatus.NONE);
		}
	}
	void setWireType(WireType wireType)
	{
		this.wireType = wireType;
		LogicCore.putConsole("SetWireType: " + super.getUUID() + " " + this.wireType.getImageTag());
		if(this.wireType != WireType.Standard)
		{
			for(IOPanel io : super.io.values())
			{
				io.setStatus(IOStatus.TRANCE);
			}
		}
		if(super.isPlacement())
		{
			super.core.getTaskOperator().checkAroundAndReserveTask(this);
		}
		super.layeredPane.repaint();
	}
	WireType getType()
	{
		return this.wireType;
	}
	@Override
	public ArrayList<LogicWire> getRemoteWire()
	{
		return null;
	}
	@Override
	public void setRemotePowerStatus(Power power)
	{
		
	}
}
enum WireType
{
	Standard("STANDARD", new Direction[]{Direction.EAST, Direction.WEST, Direction.SOUTH, Direction.NORTH})
	, Cross_A("CROSS_A", new Direction[]{Direction.NORTH, Direction.WEST}, new Direction[]{Direction.EAST, Direction.SOUTH})
	, Cross_B("CROSS_B", new Direction[]{Direction.NORTH, Direction.EAST}, new Direction[]{Direction.WEST, Direction.SOUTH})
	, Cross_C("CROSS_C", new Direction[]{Direction.NORTH, Direction.SOUTH}, new Direction[]{Direction.EAST, Direction.WEST});
	private final String imageTag;
	private ArrayList<ArrayList<Direction>> fromTo = new ArrayList<ArrayList<Direction>>();
	WireType(String tag, Direction[]... fromTo)
	{
		this.imageTag = tag;
	
		for(Direction[] dir : fromTo)
		{
			ArrayList<Direction> temp = new ArrayList<Direction>();
			for(Direction ext : dir)
			{
				temp.add(ext);
			}
			this.fromTo.add(temp);
		}
	}
	String getImageTag()
	{
		return this.imageTag;
	}
	ArrayList<Direction> getFromTo(Direction from)
	{
		for(ArrayList<Direction> dir : this.fromTo)
		{
			if(dir.contains(from))
			{
				return dir;
			}
		}
		return null;
	}
}
class AND extends LogicBlock
{
	AND(LogicCore core)
	{
		super(core, "AND");
	}
	@Override
	void calculate()
	{
		ArrayList<Power> cal = super.getResiveIOPower();
		int powerCal = 0;
		for(Power power : cal)
		{
			if(power.getBool())
			{
				powerCal++;
			}
		}
		if(powerCal == cal.size() && powerCal != 0)
		{
			super.setPowerStatus(Power.ON);
		}
		else
		{
			super.setPowerStatus(Power.OFF);
		}
	}
}
class OR extends LogicBlock
{
	OR(LogicCore core)
	{
		super(core, "OR");
	}
	@Override
	void calculate()
	{
		ArrayList<Power> cal = super.getResiveIOPower();
		Power powerActive = Power.OFF;
		for(Power power : cal)
		{
			if(power.getBool())
			{
				powerActive = Power.ON;
			}
		}
		super.setPowerStatus(powerActive);
	}
}
class NOT extends LogicBlock
{
	NOT(LogicCore core)
	{
		super(core, "NOT");
	}
	@Override
	protected void doToggleIO(Direction ext)
	{
		if(super.getIOResiveCount() > 0)
		{
			if(super.getIOStatus(ext) == IOStatus.NONE)
			{
				super.io.get(ext).setStatus(IOStatus.TRANCE);
			}
			else
			{
				super.io.get(ext).setStatus(IOStatus.NONE);
			}
		}
		else
		{
			super.doToggleIO(ext);
		}
	}
	@Override
	void calculate()
	{
		if(super.getResiveIOPower().size() > 0 && super.getResiveIOPower().get(0).getBool())
		{
			this.setPowerStatus(Power.OFF);
		}
		else
		{
			this.setPowerStatus(Power.ON);
		}
	}
}
class NAND extends LogicBlock
{
	NAND(LogicCore core)
	{
		super(core, "NAND");
	}
	@Override
	void calculate()
	{
		ArrayList<Power> cal = super.getResiveIOPower();
		Power powerActive = Power.OFF;
		for(Power power : cal)
		{
			if(!power.getBool())
			{
				powerActive = Power.ON;
			}
		}
		super.setPowerStatus(powerActive);
	}
}
class NOR extends LogicBlock
{
	NOR(LogicCore core)
	{
		super(core, "NOR");
	}
	@Override
	void calculate()
	{
		ArrayList<Power> cal = super.getResiveIOPower();
		Power powerActive = Power.ON;
		for(Power power : cal)
		{
			if(power.getBool())
			{
				powerActive = Power.OFF;
			}
		}
		super.setPowerStatus(powerActive);
	}
}
class XOR extends LogicBlock
{
	XOR(LogicCore core)
	{
		super(core, "XOR");
	}
	@Override
	void calculate()
	{
		ArrayList<Power> cal = super.getResiveIOPower();
		Power powerStatus = Power.OFF;
		Power powerActive = null;
		for(Power power : cal)
		{
			if(powerActive == null)
			{
				powerActive = power;
			}
			if(power != powerActive)
			{
				powerStatus = Power.ON;
			}
		}
		super.setPowerStatus(powerStatus);
	}
}
class XNOR extends LogicBlock
{
	XNOR(LogicCore core)
	{
		super(core, "XNOR");
	}
	@Override
	void calculate()
	{
		ArrayList<Power> cal = super.getResiveIOPower();
		Power powerStatus = Power.ON;
		Power powerActive = null;
		for(Power power : cal)
		{
			if(powerActive == null)
			{
				powerActive = power;
			}
			if(power != powerActive)
			{
				powerStatus = Power.OFF;
			}
		}
		super.setPowerStatus(powerStatus);
	}
}
class Buffer extends LogicBlock implements TimeSetter
{
	private int waitTime = 0;
	private int maintainTime = 1;
	private boolean charge = false;
	private int chargeCount = 0;
	private int maintainCount = 0;
	private JLabel timeLabel = new JLabel();
	protected Buffer(LogicCore core)
	{
		super(core, "BUF");
		super.layeredPane.add(this.timeLabel, new Integer(2));
		this.timeLabel.setVerticalAlignment(JTextField.CENTER);
		this.timeLabel.setHorizontalAlignment(JTextField.CENTER);
	}
	@Override
	void calculate()
	{
		boolean resiveOn = false;
		for(Direction ext : Direction.values())
		{
			if(super.getIOStatus(ext) == IOStatus.RECEIV && super.getIOPower(ext).getBool())
			{
				resiveOn = true;
			}
		}
		if(resiveOn)
		{
			if(this.charge || this.chargeCount >= this.waitTime)
			{
				this.chargeCount = 0;
				this.charge = true;
				this.maintainCount = this.maintainTime;
				this.timeLabel.setForeground(Color.black);
				this.timeLabel.setText(Integer.toString(this.maintainCount));
				super.setPowerStatus(Power.ON);
			}
			else
			{
				super.core.getTaskOperator().addReserveTask(this);
				this.chargeCount++;
				this.timeLabel.setForeground(Color.red);
				this.timeLabel.setText(Integer.toString(this.chargeCount));
			}
		}
		else
		{
			if(this.maintainCount > 0)
			{
				super.core.getTaskOperator().addReserveTask(this);
				this.timeLabel.setForeground(Color.black);
				this.timeLabel.setText(Integer.toString(this.maintainCount));
				this.maintainCount--;
			}
			else
			{
				this.charge = false;
				this.chargeCount = 0;
				super.setPowerStatus(Power.OFF);
				this.timeLabel.setText("");
			}
		}
	}
	@Override
	protected void operatorPing()
	{
		this.calculate();
	}
	@Override
	public void setData(DataBranch branch)
	{
		super.setData(branch);
		this.waitTime = new Integer(branch.getData("waitTime"));
		this.maintainTime = new Integer(branch.getData("maintainTime"));
		this.charge = new Boolean(branch.getData("charge"));
		this.chargeCount = new Integer(branch.getData("chargeCount"));
		this.maintainCount = new Integer(branch.getData("maintainCount"));
	}
	@Override
	public DataBranch getData(DataBranch branch)
	{
		super.getData(branch);
		branch.setData("waitTime", Integer.toString(this.waitTime));
		branch.setData("maintainTime", Integer.toString(this.maintainTime));
		branch.setData("charge", Boolean.toString(this.charge));
		branch.setData("chargeCount", Integer.toString(this.chargeCount));
		branch.setData("maintainCount", Integer.toString(this.maintainCount));
		return branch;
	}
	@Override
	public void setTime(String tag, int time)
	{
		if(tag.equals("waitTime"))
		{
			this.waitTime = time;
		}
		else if(tag.equals("maintainTime"))
		{
			this.maintainTime = time;
		}
	}
	@Override
	public int getTime(String tag)
	{
		if(tag.equals("waitTime"))
		{
			return this.waitTime;
		}
		else if(tag.equals("maintainTime"))
		{
			return this.maintainTime;
		}
		return 0;
	}
	@Override
	public void sizeUpdate()
	{
		super.sizeUpdate();
		this.timeLabel.setFont(LogicCore.RES.PIXEL_FONT.deriveFont((float)(12 * super.core.getUI().getUISize().getmultiple())));
		this.timeLabel.setBounds((7 * super.core.getUI().getUISize().getmultiple()) + super.gridViewPane.getX(), (7 * super.core.getUI().getUISize().getmultiple()) + super.gridViewPane.getY()
				, 16 * super.core.getUI().getUISize().getmultiple(), 16 * super.core.getUI().getUISize().getmultiple());
	}
}
class Button extends LogicBlock implements TimeSetter
{
	private TimerButton btn = new TimerButton();
	private JLabel timeLabel = new JLabel();
	private int basicTime = 1;
	
	Button(LogicCore core)
	{
		super(core, "BTN");
		this.timeLabel.setVerticalAlignment(JTextField.CENTER);
		this.timeLabel.setHorizontalAlignment(JTextField.CENTER);
		super.layeredPane.add(this.timeLabel, new Integer(2));
		super.gridViewPane.add(this.btn);
	}
	@Override
	void updateViewMode()
	{
	}
	@Override
	public void setData(DataBranch branch)
	{
		super.setData(branch);
		this.basicTime = new Integer(branch.getData("basicTime"));
		this.btn.imageSet();
	}
	@Override
	public DataBranch getData(DataBranch branch)
	{
		super.getData(branch);
		branch.setData("basicTime", Integer.toString(this.basicTime));
		return branch;
	}
	@Override
	protected void doToggleIO(Direction ext)
	{
		if(this.getIOStatus(ext) == IOStatus.NONE)
		{
			this.io.get(ext).setStatus(IOStatus.TRANCE);
		}
		else
		{
			this.io.get(ext).setStatus(IOStatus.NONE);
		}
	}
	void resetTimer()
	{
		super.setTimer(this.basicTime);
		super.core.getTaskOperator().addReserveTask(this);
		super.setPowerStatus(Power.ON);
		this.timeLabel.setText(Integer.toString(super.getTimer()));
		this.btn.imageSet();
	}
	@Override
	protected void activeTimer()
	{
		this.timeLabel.setText(Integer.toString(super.getTimer()));
	}
	@Override
	protected void endTimer()
	{
		super.endTimer();
		this.timeLabel.setText("");
		super.setPowerStatus(Power.OFF);
		this.btn.imageSet();
	}
	@Override
	public void sizeUpdate()
	{
		super.sizeUpdate();
		this.timeLabel.setFont(LogicCore.RES.PIXEL_FONT.deriveFont((float)(12 * super.core.getUI().getUISize().getmultiple())));
		this.timeLabel.setBounds((7 * super.core.getUI().getUISize().getmultiple()) + super.gridViewPane.getX(), (7 * super.core.getUI().getUISize().getmultiple()) + super.gridViewPane.getY()
				, 16 * super.core.getUI().getUISize().getmultiple(), 16 * super.core.getUI().getUISize().getmultiple());
		
		this.btn.setBounds(7 * super.core.getUI().getUISize().getmultiple(), 7 * super.core.getUI().getUISize().getmultiple()
				, 16 * super.core.getUI().getUISize().getmultiple(), 16 * super.core.getUI().getUISize().getmultiple());
		this.btn.imageSet();
	}
	@Override
	public void setTime(String tag, int time)
	{
		 this.basicTime = time;
	}
	@Override
	public int getTime(String tag)
	{
		return this.basicTime;
	}
	private class TimerButton extends ButtonPanel
	{
		private static final long serialVersionUID = 1L;
		@Override
		void pressed(int mouse)
		{
			if(mouse == 1)
			{
				resetTimer();
			}
			else
			{
				setTimer(0);
			}
		}
		@Override
		void imageSet()
		{
			super.setBasicImage(LogicCore.getResource().getImage(core.getUI().getUISize().getTag() + "_BUTTON_" + power.getTag()));
			super.setBasicPressImage(LogicCore.getResource().getImage(core.getUI().getUISize().getTag() + "_BUTTON_PRESS_" + power.getTag()));
			super.imageSet();
		}
	}
}
class LED extends LogicBlock implements ColorSet
{
	private Color onColor = new Color(50, 200, 50);
	private Color offColor = new Color(100, 100, 100);
	protected LED(LogicCore core)
	{
		super(core, "LED");
	}
	@Override
	void put(int absX, int absY, Grid grid)
	{
		super.put(absX, absY, grid);
		for(Direction ext : Direction.values())
		{
			super.setIO(ext, IOStatus.RECEIV);
		}
	}
	@Override
	void calculate()
	{
		boolean resiveOn = false;
		for(Direction ext : Direction.values())
		{
			if(super.getIOStatus(ext) == IOStatus.RECEIV && super.getIOPower(ext).getBool())
			{
				resiveOn = true;
			}
		}
		if(resiveOn)
		{
			super.setPowerStatus(Power.ON);
		}
		else
		{
			super.setPowerStatus(Power.OFF);
		}
	}
	@Override
	public void setColor(String tag, Color color)
	{
		if(tag.equals("onColor"))
		{
			this.onColor = color;
		}
		else if(tag.equals("offColor"))
		{
			this.offColor = color;
		}
		super.layeredPane.repaint();
	}
	@Override
	public Color getColor(String tag)
	{
		if(tag.equals("onColor"))
		{
			return this.onColor;
		}
		else if(tag.equals("offColor"))
		{
			return this.offColor;
		}
		return null;
	}
	@Override
	protected void doToggleIO(Direction ext)
	{
	}
	@Override
	protected GridViewPane createViewPane()
	{
		return new LEDViewPane(this);
	}
}
class IOPanel
{
	private IOStatus status = IOStatus.NONE;
	private Power power = Power.OFF;
	private final Direction ext;
	private final LogicBlock member;
	private BufferedImage image;
	private String imgTag;
	
	IOPanel(LogicBlock logicMember, Direction ext, String imgTag)
	{
		this.member = logicMember;
		this.ext = ext;
		this.imgTag = imgTag;
	}
	void setStatus(IOStatus status)
	{
		this.status = status;
		if(status == IOStatus.TRANCE)
		{
			this.power = member.getPower();
		}
		else
		{
			this.power = Power.OFF;
			this.member.calculate();
		}
		this.image = LogicCore.RES.getImage(member.getCore().getUI().getUISize().getTag() + "_" + this.imgTag
				+ "_" + this.status.getTag() + "_" + this.power.getTag() + "_" + this.ext.getTag());
	}
	void setImgTag(String tag)
	{
		this.imgTag = tag;
	}
	IOStatus getStatus()
	{
		return this.status;
	}
	void setOnOffStatus(Power power)
	{
		this.power = power;
		this.image = LogicCore.RES.getImage(member.getCore().getUI().getUISize().getTag() + "_" + this.imgTag
				+ "_" + this.status.getTag() + "_" + this.power.getTag() + "_" + this.ext.getTag());
	}
	Power getOnOffStatus()
	{
		return this.power;
	}
	Direction getDirection()
	{
		return this.ext;
	}
	BufferedImage getImage()
	{
		return this.image;
	}
}
class GridViewPane extends JPanel implements SizeUpdate
{
	private static final long serialVersionUID = 1L;
	
	protected final GridMember member;
	GridViewPane(GridMember member)
	{
		this.member = member;
		this.setLayout(null);
	}
	@Override
	public void sizeUpdate()
	{
		this.setSize(this.member.getUIabsSizeX() * this.member.getCore().getUI().getUISize().getmultiple()
				, this.member.getUIabsSizeY() * this.member.getCore().getUI().getUISize().getmultiple());
	}
}
abstract class LogicViewPane extends GridViewPane
{
	private static final long serialVersionUID = 1L;
	
	protected final LogicBlock logicMember;
	
	LogicViewPane(LogicBlock member)
	{
		super(member);
		this.logicMember = member;
	}
	@Override
	public void paint(Graphics g)
	{
		super.paint(g);
		g.drawImage(LogicCore.getResource().getImage(super.member.getCore().getUI().getUISize().getTag() + "_BLOCK_BACKGROUND"), 0, 0, this);
		super.paintChildren(g);
	}
	@Override
	public void sizeUpdate()
	{
		this.setBounds(super.member.getCore().getUI().getUISize().getmultiple(), super.member.getCore().getUI().getUISize().getmultiple()
				, member.getUIabsSizeX() * super.member.getCore().getUI().getUISize().getmultiple() - (super.member.getCore().getUI().getUISize().getmultiple() * 2)
				, member.getUIabsSizeX() * super.member.getCore().getUI().getUISize().getmultiple() - (super.member.getCore().getUI().getUISize().getmultiple() * 2));
	}
}
class StandardLogicViewPane extends LogicViewPane
{
	private static final long serialVersionUID = 1L;
	
	StandardLogicViewPane(LogicBlock member)
	{
		super(member);
	}
	@Override
	public void paint(Graphics g)
	{
		super.paint(g);
		for(Direction ext : Direction.values())
		{
			if(super.logicMember.getIOStatus(ext) != IOStatus.NONE)
			{
				g.drawImage(super.logicMember.getIOImage(ext), 0, 0, this);
			}
		}
		g.drawImage(LogicCore.getResource().getImage(super.member.getCore().getUI().getUISize().getTag() + "_BLOCK_" + logicMember.getPower().getTag()), 0, 0, this);
		super.paintChildren(g);
	}
}
class ResizeableViewPane extends GridViewPane
{
	private static final long serialVersionUID = 1L;
	private static final int DetectArea = 3;

	ResizeableViewPane(GridMember member)
	{
		super(member);
		MouseAdapter adapter = new MouseAdapter()
		{
			private Rectangle signR = new Rectangle();
			private boolean xEditFlag = false;
			private boolean yEditFlag = false;
			int multiple;
			private JPanel rectViewPane = new JPanel()
			{
				private static final long serialVersionUID = 1L;
				{
					this.setOpaque(false);
				}
				@Override
				public void paint(Graphics g)
				{
					
					g.setColor(new Color(50, 50, 70, 200));
					g.fillRect(0, 0, this.getWidth(), multiple * DetectArea);
					g.fillRect(0, 0, multiple * DetectArea, this.getHeight());
					g.fillRect(this.getWidth() - multiple * DetectArea, 0, multiple * DetectArea, this.getHeight());
					g.fillRect(0, this.getHeight() - multiple * DetectArea,this.getWidth(), multiple * DetectArea);
					super.paint(g);
				}
			};
			@Override
			public void mousePressed(MouseEvent arg0)
			{
				this.multiple = member.getCore().getUI().getUISize().getmultiple();
				int mouseX = arg0.getX() != 0 ? (arg0.getX() / multiple) : 0;
				int mouseY = arg0.getY() != 0 ? (arg0.getY() / multiple) : 0;
				int absX = member.getUIabsSizeX();
				int absY = member.getUIabsSizeY();
				this.signR = member.getGridViewPane().getBounds();
				this.rectViewPane.setBounds(this.signR);
				member.getCore().getUI().getGridArea().getLayeredPane().add(this.rectViewPane, new Integer(4));
				if(mouseX + DetectArea >= absX || mouseX <= DetectArea)
				{
					this.xEditFlag = true;
				}
				if(mouseY + DetectArea >= absY || mouseY <= DetectArea)
				{
					this.yEditFlag = true;
				}
			}
			@Override
			public void mouseDragged(MouseEvent e)
			{
				Rectangle r = (Rectangle)this.signR.clone();
				if(this.xEditFlag)
				{
					if(e.getX() > this.signR.width / 2)
					{
						r.setSize(this.signR.width + e.getX() - this.signR.width, this.signR.height);
					}
					else
					{
						r.setLocation(this.signR.x + e.getX(), this.signR.y);
						r.setSize(this.signR.width - e.getX(), this.signR.height);
					}
					
				}
				if(this.yEditFlag)
				{
					if(e.getY() > this.signR.height / 2)
					{
						r.setSize(r.width, this.signR.height + e.getY() - this.signR.height);
					}
					else
					{
						r.setLocation(r.x, this.signR.y + e.getY());
						r.setSize(r.width, this.signR.height - e.getY());
					}
				}
				if(r.width >= ((SizeSet)member).getMinSize() *  multiple&& r.height >= ((SizeSet)member).getMinSize() *  multiple)
				{
					this.rectViewPane.setBounds(r);
				}
			}
			@Override
			public void mouseReleased(MouseEvent arg0)
			{
				SizeInfo gridSize = member.getGrid().getGridSize();
				TaskUnit task = member.getGrid().getSession().getTaskManager().setTask();
				task.addCommand(new SetMemberSize(member,
						((this.rectViewPane.getX() - Size.MARGIN) / this.multiple) - (gridSize.getNX() * Size.REGULAR_SIZE)
						, ((this.rectViewPane.getY() - Size.MARGIN) / this.multiple) - (gridSize.getNY() * Size.REGULAR_SIZE)
						, this.rectViewPane.getWidth() / this.multiple
						, this.rectViewPane.getHeight() / this.multiple
						,member.getGrid().getSession()));
				
				this.xEditFlag = false;
				this.yEditFlag = false;
				member.getCore().getUI().getGridArea().getLayeredPane().remove(this.rectViewPane);
				member.getGrid().getGridPanel().repaint();
			}
		};
		super.addMouseListener(adapter);
		super.addMouseMotionListener(adapter);
	}
	
}
class WireViewPane extends LogicViewPane
{
	private static final long serialVersionUID = 1L;
	
	private Wire wire;

	WireViewPane(Wire wire)
	{
		super(wire);
		this.wire = wire;
	}
	@Override
	public void paint(Graphics g)
	{
		super.paint(g);
		switch(this.wire.getType())
		{
		case Standard:
			if(this.wire.getType() == WireType.Standard)
			{
				for(Direction ext : Direction.values())
				{
					if(this.wire.getIOStatus(ext) != IOStatus.NONE)
					{
						g.drawImage(this.wire.getIOImage(ext), 0, 0, this);
					}
				}
			}
			break;
		case Cross_A: case Cross_B:
			g.drawImage(LogicCore.getResource().getImage(wire.getCore().getUI().getUISize().getTag() + "_WIRE_" + this.wire.getType().getImageTag()
					+ "_" + this.wire.getIOPower(Direction.NORTH).getTag() + "_TOP"), 0, 0, this);
			g.drawImage(LogicCore.getResource().getImage(wire.getCore().getUI().getUISize().getTag() + "_WIRE_" + this.wire.getType().getImageTag()
					+ "_" + this.wire.getIOPower(Direction.SOUTH).getTag() + "_BOTTOM"), 0, 0, this);
			break;
		case Cross_C:
			g.drawImage(LogicCore.getResource().getImage(wire.getCore().getUI().getUISize().getTag() + "_WIRE_CROSS_C_"
		+ this.wire.getIOPower(Direction.EAST).getTag() + "_HORIZON"), 0, 0, this);
			g.drawImage(LogicCore.getResource().getImage(wire.getCore().getUI().getUISize().getTag() + "_WIRE_CROSS_C_"
		+ this.wire.getIOPower(Direction.NORTH).getTag() + "_VERTICAL"), 0, 0, this);
			break;
		}
		super.paintChildren(g);
	}
}
class LEDViewPane extends LogicViewPane
{
	private final LED led;
	LEDViewPane(LED member)
	{
		super(member);
		this.led = member;
	}
	@Override
	public void paint(Graphics g)
	{
		super.paint(g);
		Color c;
		if(this.led.getPower().getBool())
		{
			c = this.led.getColor("onColor");
		}
		else
		{
			c = this.led.getColor("offColor");
		}
		int m = this.led.getCore().getUI().getUISize().getmultiple();
		BufferedImage mask = LogicCore.getResource().getImage(this.led.getCore().getUI().getUISize().getTag() + "_LED_MASK");
		BufferedImage buildImage = new BufferedImage(mask.getWidth(),mask.getHeight(), BufferedImage.TYPE_INT_ARGB);
		for(int y = 0; y < mask.getHeight(); y++)
		{
			for(int x = 0; x < mask.getWidth(); x++)
			{
				buildImage.setRGB(x, y, new Color(c.getRed(), c.getGreen(), c.getBlue(), new Color(mask.getRGB(x, y)).getGreen()).getRGB());
			}
		}
		g.drawImage(buildImage, 0, 0, this);
		super.paintChildren(g);
	}
}
enum IOStatus
{
	NONE("NO"), RECEIV("RECEIV"), TRANCE("TRANCE");
	public final String tag;

	private IOStatus(String tag)
	{
		this.tag = tag;
	}
	public String getTag()
	{
		return this.tag;
	}
}
enum Power
{
	ON("ON", true), OFF("OFF", false);
	public final String tag;
	private boolean bool;
	private Power(String tag, boolean bool)
	{
		this.tag = tag;
		this.bool = bool;
	}
	String getTag()
	{
		return this.tag;
	}
	boolean getBool()
	{
		return this.bool;
	}
}