package kr.dja;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.UUID;

import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

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
		
		private Color rectColor;
		
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
		this.io.put(Direction.EAST, new IOPanel(this, Direction.EAST, "BASIC"));
		this.io.put(Direction.WEST, new IOPanel(this, Direction.WEST, "BASIC"));
		this.io.put(Direction.SOUTH, new IOPanel(this, Direction.SOUTH, "BASIC"));
		this.io.put(Direction.NORTH, new IOPanel(this, Direction.NORTH, "BASIC"));
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
		this.power = Power.valueOf(Power.OFF.toString());
		for(Direction ext : Direction.values())
		{
			this.setTimer(0);
			String data = branch.getData("io_" + ext);
			io.get(ext).setStatus(IOStatus.valueOf(data.split("_")[0]));
			io.get(ext).setOnOffStatus(Power.valueOf(data.split("_")[1]));
			this.calculate();
		}
		if(super.isPlacement())
		{
			super.core.getTaskOperator().checkAroundAndReserveTask(this);
		}
		this.calculate();
		super.setData(branch);
	}
	@Override
	public DataBranch getData(DataBranch branch)
	{
		super.getData(branch);
		branch.setData("blocklocationX", Integer.toString(this.blocklocationX));
		branch.setData("blocklocationY", Integer.toString(this.blocklocationY));
		branch.setData("power", power.toString());
		for(Direction ext : Direction.values())
		{
			branch.setData("io_" + ext, this.io.get(ext).getStatus().toString() + "_" + this.io.get(ext).getOnOffStatus().toString());
		}
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
		this.doToggleIO(ext);
		super.layeredPane.repaint();
		if(super.isPlacement())
		{
			super.core.getTaskOperator().checkAroundAndReserveTask(this);
		}
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
	protected void operatorPing()
	{
		
	}
	final void setTimer(int time)
	{
		this.timer = time;
	}
	int getTimer()
	{
		return this.timer;
	}
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
	void calculate(){}
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
	protected GridViewPane createViewPane()
	{
		return new WireViewPane(this);
	}
	public void setData(DataBranch branch)
	{
		super.setData(branch);
		this.setWireType(WireType.valueOf(branch.getData("WireType")));
	}
	public DataBranch getData(DataBranch branch)
	{
		super.getData(branch);
		branch.setData("WireType", this.wireType.toString());
		return branch;
	}
	@Override
	void setIOResivePower(Direction ext, Power power)
	{
		this.io.get(ext).setOnOffStatus(power);
		for(Direction changeExt : this.wireType.getFromTo(ext))
		{
			super.io.get(changeExt).setOnOffStatus(power);
		}
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
		super.layeredPane.repaint();
	}
	void setWireType(WireType wireType)
	{
		this.wireType = wireType;
		
		if(this.wireType != WireType.Standard)
		{
			for(IOPanel io : super.io.values())
			{
				io.setStatus(IOStatus.TRANCE);
			}
		}
		super.layeredPane.repaint();
	}
	WireType getType()
	{
		return this.wireType;
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
	public void setData(DataBranch branch)
	{
		super.setData(branch);
		this.basicTime = new Integer(branch.getData("basicTime"));
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
		super.endTimer();
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
		System.out.println(member.getCore().getUI());
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