package kr.dja;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.PrintJob;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;

public abstract class GridMember implements SizeUpdate
{
	protected int UIabslocationX = 0;
	protected int UIabslocationY = 0;
	protected int UIabsSizeX = 1;
	protected int UIabsSizeY = 1;
	protected GridViewPane gridViewPane;
	protected Size size;
	protected final String name;
	protected JLayeredPane layeredPane;
	protected TaskOperator operator;
	private SelectShowPanel selectView = null;
	private boolean placement = false;

	protected GridMember(Size size, String name)
	{
		this.name = name;
		this.size = size;
		
		this.layeredPane = new JLayeredPane();
		this.gridViewPane = new GridViewPane(this);
		this.sizeUpdate();
		this.layeredPane.add(this.gridViewPane, new Integer(0));;
		
	}
	protected GridMember clone(GridMember member)
	{
		member.UIabslocationX = this.UIabslocationX;
		member.UIabslocationY = this.UIabslocationY;
		member.UIabsSizeX = this.UIabsSizeX;
		member.UIabsSizeX = this.UIabsSizeY;
		return member;
	}
	public abstract GridMember clone(Size size);
	void setSelectView(int[] color)
	{//단순 표시용
		if(this.selectView == null)
		{
			this.selectView = new SelectShowPanel(color[0], color[1], color[2], color[3], color[4], color[5], color[6]);
			this.layeredPane.add(this.selectView, new Integer(2));
			this.sizeUpdate();
		}
		else
		{
			this.removeSelectView();
			this.setSelectView(color);
		}
	}
	void removeSelectView()
	{
		if(selectView != null)
		{
			this.layeredPane.remove(this.selectView);
			this.selectView = null;
			this.sizeUpdate();
		}
	}
	String getName()
	{
		return this.name;
	}
	Size getSize()
	{
		return this.size;
	}
	int getUIabsLocationX()
	{//그리드상 실제 위치는 절대 위치에 배수를 곱해서 사용
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
	void put(int absX, int absY, TaskOperator operator)
	{
		this.sizeUpdate();
		this.UIabslocationX = absX;
		this.UIabslocationY = absY;
		this.operator = operator;
		System.out.println("PUT: " + UIabslocationX + " " + UIabslocationY);
		System.out.println(this.layeredPane.getSize());
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
		this.gridViewPane.printAll(img.createGraphics());
		return img;
	}
	JLayeredPane getGridViewPane()
	{
		this.sizeUpdate();
		return this.layeredPane;
	}
	@Override
	public void sizeUpdate()
	{
		this.gridViewPane.sizeUpdate();
		this.layeredPane.setSize(UIabsSizeX * size.getmultiple(), UIabsSizeY * size.getmultiple());
		if(this.selectView != null)
		{
			this.selectView.sizeUpdate();
		}
	}
	class SelectShowPanel extends JPanel implements SizeUpdate
	{
		private static final long serialVersionUID = 1L;
		int r, g, b, a, rs, gs, bs;
		SelectShowPanel(int r, int g, int b, int a, int rs, int gs, int bs)
		{
			this.r = r; this.g = g; this.b = b; this.a = a;this.rs = rs; this.gs = gs; this.bs = bs;
			this.setBackground(new Color(r, g, b, a));
		}
		@Override
		public void sizeUpdate()
		{
			this.setBounds(size.getmultiple() + gridViewPane.getX(), size.getmultiple() + gridViewPane.getY(), gridViewPane.getWidth() - size.getmultiple() - gridViewPane.getX(), gridViewPane.getHeight() - size.getmultiple() - gridViewPane.getY());
		}
		@Override
		public void paint(Graphics g)
		{
			super.paint(g);
			g.setColor(new Color(this.rs, this.gs, this.bs));
			g.drawRect(0, 0, this.getWidth() - 1, this.getHeight() - 1);
		}
		@Override
		public SelectShowPanel clone()
		{
			SelectShowPanel cloneShow = new SelectShowPanel(this.r, this.g, this.b, this.a, this.rs, this.gs, this.bs);
			return cloneShow;
		}
	}
}
class Partition extends GridMember
{
	Partition(Size size)
	{
		super(size, "Partition");
	}
	@Override
	public Partition clone(Size size)
	{
		Partition cloneMember = new Partition(size);
		super.clone(cloneMember);
		return cloneMember;
	}
}
class Tag extends GridMember
{
	Tag(Size size)
	{
		super(size, "Partition");
	}
	@Override
	public Tag clone(Size size)
	{
		Tag cloneMember = new Tag(size);
		super.clone(cloneMember);
		return cloneMember;
	}
}
abstract class LogicBlock extends GridMember
{
	protected int blocklocationX = 0;
	protected int blocklocationY = 0;
	protected Power power = Power.OFF;
	protected HashMap<Direction, IOPanel> io = new HashMap<Direction, IOPanel>();
	
	protected LogicBlock(Size size, String name)
	{
		super(size, name);
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
	protected LogicBlock clone(GridMember member)
	{
		super.clone(member);
		LogicBlock cloneMember = (LogicBlock)member;
		cloneMember.blocklocationX = this.blocklocationX;
		cloneMember.blocklocationY = this.blocklocationY;
		cloneMember.power = this.power;
		cloneMember.io = new HashMap<Direction, IOPanel>();
		cloneMember.io.put(Direction.EAST, this.io.get(Direction.EAST).clone(cloneMember));
		cloneMember.io.put(Direction.WEST, this.io.get(Direction.WEST).clone(cloneMember));
		cloneMember.io.put(Direction.SOUTH, this.io.get(Direction.SOUTH).clone(cloneMember));
		cloneMember.io.put(Direction.NORTH, this.io.get(Direction.NORTH).clone(cloneMember));
		return cloneMember;
	}
	@Override
	void put(int absX, int absY, TaskOperator operator)
	{
		absX = absX / Size.REGULAR_SIZE;
		absY = absY / Size.REGULAR_SIZE;
		this.blocklocationX = absX;
		this.blocklocationY = absY;
		super.put((this.blocklocationX * Size.REGULAR_SIZE), (this.blocklocationY * Size.REGULAR_SIZE), operator);
	}
	int getBlockLocationX()
	{
		return this.blocklocationX;
	}
	int getBlockLocationY() 
	{
		return this.blocklocationY;
	}
	ArrayList<Direction> getOutput()
	{
		ArrayList<Direction> returnInfo = new ArrayList<Direction>();
		for(Direction ext : this.io.keySet())
		{
			returnInfo.add(this.io.get(ext).getDirection());
		}
		return returnInfo;
	}
	void setInput(Direction ext, Power power)
	{
		if(this.io.get(ext).getStatus() == IOStatus.RECEIV)
		{
			this.io.get(ext).setOnOffStatus(power);
		}
		this.calculate();
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
		super.gridViewPane.repaint();
	}
	IOPanel getIO(Direction ext)
	{
		return this.io.get(ext);
	}
	Power getPower()
	{
		return this.power;
	}
	abstract void calculate();
}
class AND extends LogicBlock
{
	AND(Size size)
	{
		super(size, "AND");
	}
	@Override
	public AND clone(Size size)
	{
		AND cloneMember = new AND(size);
		super.clone(cloneMember);
		return cloneMember;
	}
	@Override
	void calculate()
	{
		
	}
}
class OR extends LogicBlock
{
	OR(Size size)
	{
		super(size, "OR");
	}
	@Override
	public OR clone(Size size)
	{
		OR cloneMember = new OR(size);
		super.clone(cloneMember);
		return cloneMember;
	}
	@Override
	void calculate()
	{
		// TODO Auto-generated method stub
		
	}
}
class NOT extends LogicBlock
{
	NOT(Size size)
	{
		super(size, "NOT");
	}
	@Override
	public NOT clone(Size size)
	{
		NOT cloneMember = new NOT(size);
		super.clone(cloneMember);
		return cloneMember;
	}
	@Override
	void calculate()
	{
		
	}
}
class Button extends LogicBlock implements LogicTimerTask
{
	private TimerButton btn;
	private int basicTime = 3;
	private int timer;
	Button(Size size)
	{
		super(size, "BTN");
		this.btn = new TimerButton();
		super.gridViewPane.add(this.btn);
	}
	@Override
	public Button clone(Size size)
	{
		Button cloneMember = new Button(size);
		super.clone(cloneMember);
		return cloneMember;
	}
	@Override
	void calculate()
	{
		
	}
	@Override
	public void ping()
	{
		if(this.timer > 0)
		{
			this.operator.addReserveTask(this);
			this.timer--;
			System.out.println("째깍");
		}
		else
		{
			super.setPowerStatus(Power.OFF);
		}
	}
	void setTimer(int time)
	{
		this.basicTime = time;
	}
	void pushButton()
	{
		timer = basicTime;
		this.operator.addReserveTask(this);
		super.setPowerStatus(Power.ON);
	}
	private class TimerButton extends JButton implements ActionListener
	{
		private static final long serialVersionUID = 1L;
		
		TimerButton()
		{
			this.setBounds(7 * size.getmultiple(), 7 * size.getmultiple(), 16 * size.getmultiple(), 16 * size.getmultiple());
			this.addActionListener(this);
			this.setPressedIcon(new ImageIcon(LogicCore.getResource().getImage("MIDDLE_BUTTON_OFF")));
			this.setIcon(new ImageIcon(LogicCore.getResource().getImage("MIDDLE_BUTTON_OFF")));
		}
		@Override
		public void actionPerformed(ActionEvent arg0)
		{
			pushButton();
		}
	}
}
class IOPanel
{
	private IOStatus status;
	private Power power;
	private final Direction ext;
	private final LogicBlock member;
	private BufferedImage image;
	IOPanel(LogicBlock logicMember, Direction ext)
	{
		this.member = logicMember;
		this.ext = ext;
		this.power = Power.OFF;
		this.setStatus(IOStatus.NONE);
	}
	void setStatus(IOStatus status)
	{
		this.status = status;
		this.image = LogicCore.RES.getImage(member.getSize().getTag() + "_" + this.status.getTag() + "_" + this.power.getTag() + "_" + this.ext.getTag());
		this.member.calculate();
		this.member.getGridViewPane().repaint();
	}
	IOStatus getStatus()
	{
		return this.status;
	}
	void setOnOffStatus(Power power)
	{
		this.power = power;
		if(this.status == IOStatus.RECEIV)
		{
			this.member.calculate();
		}
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
	public void paint(Graphics g)
	{
		super.paint(g);
	}
	@Override
	public void sizeUpdate()
	{
		this.setSize(member.getUIabsSizeX() * member.getSize().getmultiple(), member.getUIabsSizeX() * member.getSize().getmultiple());
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
	{//임시 땜빵용
		super.paint(g);
		
		g.drawImage(LogicCore.getResource().getImage(logicMember.getSize().getTag() + "_BLOCK_BACKGROUND"), 0, 0, this);
		if(logicMember.getIO(Direction.EAST).getStatus() != IOStatus.NONE)
		{
			g.drawImage(logicMember.getIO(Direction.EAST).getImage(), 25 * member.getSize().getmultiple(), 7 * member.getSize().getmultiple(), this);
		}
		if(logicMember.getIO(Direction.WEST).getStatus() != IOStatus.NONE)
		{
			g.drawImage(logicMember.getIO(Direction.WEST).getImage(), 1 * member.getSize().getmultiple(), 7 * member.getSize().getmultiple(), this);
		}
		if(logicMember.getIO(Direction.SOUTH).getStatus() != IOStatus.NONE)
		{
			g.drawImage(logicMember.getIO(Direction.SOUTH).getImage(), 7 * member.getSize().getmultiple(), 25 * member.getSize().getmultiple(), this);
		}
		if(logicMember.getIO(Direction.NORTH).getStatus() != IOStatus.NONE)
		{
			g.drawImage(logicMember.getIO(Direction.NORTH).getImage(), 7 * member.getSize().getmultiple(), 1 * member.getSize().getmultiple(), this);
		}
		g.drawImage(LogicCore.getResource().getImage(logicMember.getSize().getTag() + "_BLOCK_" + logicMember.getPower().getTag()), 7 * member.getSize().getmultiple(), 7 * member.getSize().getmultiple(), this);
		super.paintComponents(g);
	}
	@Override
	public void sizeUpdate()
	{
		this.setBounds(member.getSize().getmultiple(), member.getSize().getmultiple(), member.getUIabsSizeX() * member.getSize().getmultiple() - (member.getSize().getmultiple() * 2), member.getUIabsSizeX() * member.getSize().getmultiple() - (member.getSize().getmultiple() * 2));
	}
}
enum IOStatus
{
	NONE("RECEIV"), RECEIV("RECEIV"), TRANCE("TRANCE");
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
	ON("ON"), OFF("OFF");
	public final String tag;
	private Power(String tag)
	{
		this.tag = tag;
	}
	String getTag()
	{
		return this.tag;
	}
}