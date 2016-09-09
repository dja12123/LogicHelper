package kr.dja;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.UUID;

import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JTextField;


public abstract class GridMember implements DataIO, SizeUpdate
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
	private TaskUnit taskUnit = null;

	protected GridMember(LogicCore core, String name)
	{
		this.core = core;
		this.id = UUID.randomUUID();
		this.name = name;
		this.layeredPane = new GridLayeredPane();
		this.gridViewPane = new GridViewPane(this);
		this.layeredPane.add(this.gridViewPane, new Integer(1));
	}
	@Override
	public void setData(LinkedHashMap<String, String> dataMap)
	{
		this.id = UUID.fromString(dataMap.get("id"));
		this.name = dataMap.get("name");
		this.UIabslocationX = new Integer(dataMap.get("UIabslocationX"));
		this.UIabslocationY = new Integer(dataMap.get("UIabslocationY"));
		this.UIabsSizeX = new Integer(dataMap.get("UIabsSizeX"));
		this.UIabsSizeY = new Integer(dataMap.get("UIabsSizeY"));
	}
	@Override
	public LinkedHashMap<String, String> getData(LinkedHashMap<String, String> dataMap)
	{
		dataMap.put("ClassName", this.getClass().getName());
		dataMap.put("id", this.id.toString());
		dataMap.put("name", this.name);
		dataMap.put("UIabslocationX", Integer.toString(this.UIabslocationX));
		dataMap.put("UIabslocationY", Integer.toString(this.UIabslocationY));
		dataMap.put("UIabsSizeX", Integer.toString(this.UIabsSizeX));
		dataMap.put("UIabsSizeY", Integer.toString(this.UIabsSizeY));
		dataMap.put("placement", Boolean.toString(this.placement));
		System.out.println(this.placement);
		if(this.grid != null)
		{
			dataMap.put("grid", this.grid.getUUID().toString());
		}
		return dataMap;
	}
	protected void setChangeStateBefore()
	{
		if(this.placement)
		{
			this.taskUnit = this.grid.getSession().getTaskManager().setTask();
			this.taskUnit.addEditBefore(this);
		}
	}
	protected void setChangeStateAfter()
	{
		if(this.placement)
		{
			this.taskUnit.setFirstLabel("(" + this.UIabslocationX + ", " + this.UIabslocationY + ")");
			this.taskUnit.addEditAfter(this);
			this.taskUnit = null;
		}
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
	UUID getUUID()
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
	int getUIabsLocationX()
	{
		return UIabslocationX;
	}
	int getUIabsLocationY()
	{
		return UIabslocationY;
	}
	int getUIabsSizeX()
	{
		return UIabsSizeX;
	}
	int getUIabsSizeY()
	{
		return UIabsSizeY;
	}
	void put(int absX, int absY, Grid grid)
	{
		this.sizeUpdate();
		this.UIabslocationX = absX;
		this.UIabslocationY = absY;
		this.grid = grid;
		System.out.println("PUT: " + UIabslocationX + " " + UIabslocationY);
		System.out.println("UUID: " + this.id.toString());
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
		this.gridViewPane.repaint();
		BufferedImage img = new BufferedImage(this.gridViewPane.getWidth(), this.gridViewPane.getHeight(), BufferedImage.TYPE_INT_ARGB);
		this.gridViewPane.printAll(img.getGraphics());
		return img;
	}
	JLayeredPane getGridViewPane()
	{
		this.sizeUpdate();
		return this.layeredPane;
	}
	Grid getGrid()
	{
		return this.grid;
	}
	@Override
	public void sizeUpdate()
	{
		this.gridViewPane.sizeUpdate();
		this.layeredPane.setSize(UIabsSizeX * core.getUI().getUISize().getmultiple(), UIabsSizeY * core.getUI().getUISize().getmultiple());
	}
	class GridLayeredPane extends JLayeredPane
	{
		private static final long serialVersionUID = 1L;
		
		Color rectColor;
		
		GridLayeredPane()
		{
			MouseAdapter adaptor = new MouseAdapter()
			{
				boolean onMouseFlag;
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
				public final void mouseReleased(MouseEvent e)
				{
					if(this.onMouseFlag)
					{
						if(grid.isSelect(GridMember.this))
						{
							grid.deSelect(GridMember.this);
						}
						else
						{
							grid.selectFocus(GridMember.this);
						}
					}
				}
			};
			this.addMouseListener(adaptor);
			this.addMouseMotionListener(adaptor);
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
	}
	static GridMember Factory(LogicCore core, LinkedHashMap<String, String> info)
	{
		GridMember member = null;
		try
		{//리플렉션
			member = (GridMember)Class.forName(info.get("ClassName")).getDeclaredConstructor(new Class[]{LogicCore.class}).newInstance(core);
			member.setData(info);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return member;
	}
}
class Partition extends GridMember
{
	Partition(LogicCore core)
	{
		super(core, "Partition");
	}
}
class Tag extends GridMember
{
	Tag(LogicCore core)
	{
		super(core, "Partition");
	}
}
abstract class LogicBlock extends GridMember
{
	protected int blocklocationX = 0;
	protected int blocklocationY = 0;
	protected Power power = Power.OFF;
	protected LinkedHashMap<Direction, IOPanel> io = new LinkedHashMap<Direction, IOPanel>();
	private int timer = 0;
	
	protected LogicBlock(LogicCore core, String name)
	{
		super(core, name);
		super.UIabsSizeX = 32;
		super.UIabsSizeY = 32;
		super.layeredPane.removeAll();
		super.gridViewPane = new LogicViewPane(this);
		super.layeredPane.add(super.gridViewPane, new Integer(0));
		this.io.put(Direction.EAST, new IOPanel(this, Direction.EAST));
		this.io.put(Direction.WEST, new IOPanel(this, Direction.WEST));
		this.io.put(Direction.SOUTH, new IOPanel(this, Direction.SOUTH));
		this.io.put(Direction.NORTH, new IOPanel(this, Direction.NORTH));
	}
	@Override
	public void setData(LinkedHashMap<String, String> dataMap)
	{
		super.setData(dataMap);
		this.blocklocationX = new Integer(dataMap.get("blocklocationX"));
		this.blocklocationY = new Integer(dataMap.get("blocklocationY"));
		this.power = Power.valueOf(Power.OFF.toString());
		for(Direction ext : Direction.values())
		{
			String data = dataMap.get("io_" + ext);
			IOPanel ioPanel = new IOPanel(this, ext);
			this.io.put(ext, ioPanel);
			ioPanel.setStatus(IOStatus.valueOf(data.split("_")[0]));
			ioPanel.setOnOffStatus(Power.valueOf(data.split("_")[1]));
			
		}
		if(super.isPlacement())
		{
			super.core.getTaskOperator().checkAroundAndReserveTask(this);
		}
	}
	@Override
	public LinkedHashMap<String, String> getData(LinkedHashMap<String, String> dataMap)
	{
		super.getData(dataMap);
		dataMap.put("blocklocationX", Integer.toString(this.blocklocationX));
		dataMap.put("blocklocationY", Integer.toString(this.blocklocationY));
		dataMap.put("power", power.toString());
		for(Direction ext : Direction.values())
		{
			dataMap.put("io_" + ext, this.io.get(ext).getStatus().toString() + "_" + this.io.get(ext).getOnOffStatus().toString());
		}
		return dataMap;
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
		System.out.println("LogicPut: " + this.getBlockLocationX() + " " + this.getBlockLocationY());
	}
	@Override
	protected void remove()
	{
		super.remove();
	}
	int getBlockLocationX()
	{
		return this.blocklocationX;
	}
	int getBlockLocationY()
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
		super.setChangeStateBefore();
		this.doToggleIO(ext);
		super.layeredPane.repaint();
		if(super.isPlacement())
		{
			super.core.getTaskOperator().checkAroundAndReserveTask(this);
		}
		super.setChangeStateAfter();
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
		}
		this.calculate();
		super.layeredPane.repaint();
	}
	Power getIOPower(Direction ext)
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
	protected abstract void operatorPing();
	
	final void setTimer(int time)
	{
		this.timer = time;
	}
	int getTimer()
	{
		return this.timer;
	}
	protected void activeTimer(){}
	protected void endTimer(){}
	IOStatus getIOStatus(Direction ext)
	{
		return this.io.get(ext).getStatus();
	}
	Power getPower()
	{
		return this.power;
	}
	void calculate(){}
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
	@Override
	protected void operatorPing()
	{
		
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
		boolean powerActive = false;
		for(Power power : cal)
		{
			if(power.getBool())
			{
				powerActive = true;
			}
		}
		if(powerActive)
		{
			super.setPowerStatus(Power.ON);
		}
		else
		{
			super.setPowerStatus(Power.OFF);
		}
	}
	@Override
	protected void operatorPing()
	{
		// TODO Auto-generated method stub
		
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
		// TODO Auto-generated method stub
		
	}
	@Override
	protected void operatorPing()
	{
		// TODO Auto-generated method stub
		
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
	@Override
	protected void operatorPing()
	{
		// TODO Auto-generated method stub
		
	}
}
class Button extends LogicBlock
{
	private TimerButton btn;
	private JLabel timeLabel;
	private int basicTime = 30;
	
	Button(LogicCore core)
	{
		super(core, "BTN");
		this.btn = new TimerButton();
		this.timeLabel = new JLabel();
		this.timeLabel.setVerticalAlignment(JTextField.CENTER);
		this.timeLabel.setHorizontalAlignment(JTextField.CENTER);
		super.layeredPane.add(this.timeLabel, new Integer(20));
		super.gridViewPane.add(this.btn);
		
	}
	@Override
	public void setData(LinkedHashMap<String, String> dataMap)
	{
		super.setData(dataMap);
		this.basicTime = new Integer(dataMap.get("basicTime"));
	}
	@Override
	public LinkedHashMap<String, String> getData(LinkedHashMap<String, String> dataMap)
	{
		super.getData(dataMap);
		dataMap.put("basicTime", Integer.toString(this.basicTime));
		return dataMap;
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
		super.setTimer(basicTime);
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
		this.timeLabel.setText("");
		this.setPowerStatus(Power.OFF);
		this.btn.imageSet();
	}
	@Override
	public void sizeUpdate()
	{
		super.sizeUpdate();
		if(this.timeLabel == null)
		{
			this.timeLabel = new JLabel();
		}
		else
		{
			this.timeLabel.setFont(LogicCore.RES.PIXEL_FONT.deriveFont((float)(12 * super.core.getUI().getUISize().getmultiple())));
			this.timeLabel.setBounds((7 * super.core.getUI().getUISize().getmultiple()) + super.gridViewPane.getX(), (7 * super.core.getUI().getUISize().getmultiple()) + super.gridViewPane.getY()
					, 16 * super.core.getUI().getUISize().getmultiple(), 16 * super.core.getUI().getUISize().getmultiple());
		}
		if(this.btn == null)
		{
			this.btn = new TimerButton();
		}
		else
		{
			this.btn.setBounds(7 * super.core.getUI().getUISize().getmultiple(), 7 * super.core.getUI().getUISize().getmultiple()
					, 16 * super.core.getUI().getUISize().getmultiple(), 16 * super.core.getUI().getUISize().getmultiple());
			this.btn.imageSet();
		}
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
	@Override
	protected void operatorPing()
	{
		// TODO Auto-generated method stub
		
	}
}
class IOPanel
{
	private IOStatus status = IOStatus.NONE;
	private Power power = Power.OFF;
	private final Direction ext;
	private final LogicBlock member;
	private BufferedImage image;
	IOPanel(LogicBlock logicMember, Direction ext)
	{
		this.member = logicMember;
		this.ext = ext;
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
		
		this.image = LogicCore.RES.getImage(member.getCore().getUI().getUISize().getTag() + "_" + this.status.getTag() + "_" + this.power.getTag() + "_" + this.ext.getTag());
	}
	IOStatus getStatus()
	{
		return this.status;
	}
	void setOnOffStatus(Power power)
	{
		this.power = power;
		this.image = LogicCore.RES.getImage(member.getCore().getUI().getUISize().getTag() + "_" + this.status.getTag() + "_" + this.power.getTag() + "_" + this.ext.getTag());
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
	IOPanel clone(LogicBlock logicMember)
	{
		IOPanel cloneIO = new IOPanel(logicMember, this.ext);
		cloneIO.power = this.power;
		cloneIO.setStatus(this.getStatus());
		return cloneIO;
	}
}
class GridViewPane extends JPanel implements SizeUpdate
{
	private static final long serialVersionUID = 1L;
	
	protected final GridMember member;
	GridViewPane(GridMember member)
	{
		this.member = member;
		this.setBackground(new Color(235, 241, 222));
		this.setLayout(null);
	}
	@Override
	public void sizeUpdate()
	{
		this.setSize(this.member.getUIabsSizeX() * this.member.getCore().getUI().getUISize().getmultiple()
				, this.member.getUIabsSizeX() * this.member.getCore().getUI().getUISize().getmultiple());
	}
}
class LogicViewPane extends GridViewPane
{
	private static final long serialVersionUID = 1L;
	
	private final LogicBlock logicMember;
	LogicViewPane(LogicBlock member)
	{
		super(member);
		this.logicMember = member;
	}
	@Override
	public void paint(Graphics g)
	{
		g.drawImage(LogicCore.getResource().getImage(super.member.getCore().getUI().getUISize().getTag() + "_BLOCK_BACKGROUND"), 0, 0, this);
		if(logicMember.getIOStatus(Direction.EAST) != IOStatus.NONE)
		{
			g.drawImage(logicMember.getIOImage(Direction.EAST), 25 * super.member.getCore().getUI().getUISize().getmultiple(), 7 * super.member.getCore().getUI().getUISize().getmultiple(), this);
		}
		if(logicMember.getIOStatus(Direction.WEST) != IOStatus.NONE)
		{
			g.drawImage(logicMember.getIOImage(Direction.WEST), 1 * super.member.getCore().getUI().getUISize().getmultiple(), 7 * super.member.getCore().getUI().getUISize().getmultiple(), this);
		}
		if(logicMember.getIOStatus(Direction.SOUTH) != IOStatus.NONE)
		{
			g.drawImage(logicMember.getIOImage(Direction.SOUTH), 7 * super.member.getCore().getUI().getUISize().getmultiple(), 25 * super.member.getCore().getUI().getUISize().getmultiple(), this);
		}
		if(logicMember.getIOStatus(Direction.NORTH) != IOStatus.NONE)
		{
			g.drawImage(logicMember.getIOImage(Direction.NORTH), 7 *super.member.getCore().getUI().getUISize().getmultiple(), 1 * super.member.getCore().getUI().getUISize().getmultiple(), this);
		}
		g.drawImage(LogicCore.getResource().getImage(super.member.getCore().getUI().getUISize().getTag() + "_BLOCK_" + logicMember.getPower().getTag())
				, 7 * super.member.getCore().getUI().getUISize().getmultiple(), 7 * super.member.getCore().getUI().getUISize().getmultiple(), this);
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