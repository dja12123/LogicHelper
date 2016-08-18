package kr.dja;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
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
	private SelectShowPanel selectView = null;
	private boolean placement = false;

	protected GridMember(Size size, String name)
	{
		this.name = name;
		this.size = size;
		
		this.layeredPane = new JLayeredPane();
		this.gridViewPane = new GridViewPane(this);
		this.layeredPane.add(this.gridViewPane, new Integer(0));;
		this.sizeUpdate();
	}
	protected GridMember clone(GridMember member)
	{
		member.UIabslocationX = this.UIabslocationX;
		member.UIabslocationY = this.UIabslocationY;
		member.UIabsSizeX = this.UIabsSizeX;
		member.UIabsSizeX = this.UIabsSizeY;
		return member;
	}
	public abstract GridMember clone();
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
	void put(int absX, int absY)
	{
		this.sizeUpdate();
		this.UIabslocationX = absX;
		this.UIabslocationY = absY;
		System.out.println("PUT: " + UIabslocationX + " " + UIabslocationY);
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
			this.setBounds(size.getmultiple(), size.getmultiple(), UIabsSizeX * size.getmultiple() - (size.getmultiple() * 2), UIabsSizeY * size.getmultiple() - (size.getmultiple() * 2));
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
	public Partition clone()
	{
		Partition cloneMember = new Partition(this.size);
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
	public Tag clone()
	{
		Tag cloneMember = new Tag(this.size);
		super.clone(cloneMember);
		return cloneMember;
	}
}
abstract class LogicBlock extends GridMember
{
	protected int blocklocationX = 0;
	protected int blocklocationY = 0;
	protected boolean onOffStatus = false;
	protected HashMap<Direction, IOPanel> io = new HashMap<Direction, IOPanel>();
	
	protected LogicBlock(Size size, String name)
	{
		super(size, name);
		super.UIabsSizeX = 30;
		super.UIabsSizeY = 30;
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
		cloneMember.onOffStatus = this.onOffStatus;
		cloneMember.io = new HashMap<Direction, IOPanel>();
		cloneMember.io.put(Direction.EAST, this.io.get(Direction.EAST).clone(cloneMember));
		cloneMember.io.put(Direction.WEST, this.io.get(Direction.WEST).clone(cloneMember));
		cloneMember.io.put(Direction.SOUTH, this.io.get(Direction.SOUTH).clone(cloneMember));
		cloneMember.io.put(Direction.NORTH, this.io.get(Direction.NORTH).clone(cloneMember));
		return cloneMember;
	}
	@Override
	void put(int absX, int absY)
	{
		absX = absX / Size.REGULAR_SIZE;
		absY = absY / Size.REGULAR_SIZE;
		this.blocklocationX = absX;
		this.blocklocationY = absY;
		super.put((this.blocklocationX * Size.REGULAR_SIZE), (this.blocklocationY * Size.REGULAR_SIZE));
	}
	int getBlockLocationX()
	{
		return this.blocklocationX;
	}
	int getBlockLocationY() 
	{
		return this.blocklocationY;
	}
	ArrayList<Direction> getOnlineOutput()
	{
		ArrayList<Direction> returnInfo = new ArrayList<Direction>();
		for(Direction ext : this.io.keySet())
		{
			if(this.io.get(ext).getStatus() == IOStatus.TRANCE && this.io.get(ext).getOnOffStatus() == Power.ON)
			{
				returnInfo.add(this.io.get(ext).getDirection());
			}
		}
		return returnInfo;
	}
	IOPanel getIO(Direction ext)
	{
		return this.io.get(ext);
	}
	boolean getOnOffStatus()
	{
		return this.onOffStatus;
	}
	void calculate()
	{
		
	}

}
class AND extends LogicBlock
{
	AND(Size size)
	{
		super(size, "AND");
	}
	@Override
	public AND clone()
	{
		AND cloneMember = new AND(this.size);
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
	public OR clone()
	{
		OR cloneMember = new OR(this.size);
		super.clone(cloneMember);
		return cloneMember;
	}
	@Override
	void calculate()
	{
		// TODO Auto-generated method stub
		
	}
}
class IOPanel
{
	private IOStatus status;
	private Power power;
	private final Direction ext;
	private final LogicBlock member;
	private Color image;
	IOPanel(LogicBlock logicMember, Direction ext)
	{
		this.member = logicMember;
		this.ext = ext;
		
		this.setStatus(IOStatus.NONE);
	}
	void setStatus(IOStatus status)
	{
		this.status = status;
		if(status == IOStatus.NONE)
		{
			this.image = new Color(235, 241, 222);
		}
		else if(status == IOStatus.RECEIV)
		{
			this.image = new Color(127, 127, 127);
		}
		else if(status == IOStatus.TRANCE)
		{
			this.image = new Color(99, 37, 35);
		}
		this.member.calculate();
		this.member.getGridViewPane().repaint();
	}
	IOStatus getStatus()
	{
		return this.status;
	}
	void setOnOffStatus(Power status)
	{
		this.power = status;
		this.member.calculate();
	}
	Power getOnOffStatus()
	{
		return this.power;
	}
	Direction getDirection()
	{
		return this.ext;
	}
	Color getImage()
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
		g.setColor(logicMember.getIO(Direction.EAST).getImage());
		g.fillRect(25 * member.getSize().getmultiple(), 7 * member.getSize().getmultiple(), 4 * member.getSize().getmultiple(), 16 * member.getSize().getmultiple());
		g.setColor(logicMember.getIO(Direction.WEST).getImage());
		g.fillRect(member.getSize().getmultiple(), 7 * member.getSize().getmultiple(), 4 * member.getSize().getmultiple(), 16 * member.getSize().getmultiple());
		g.setColor(logicMember.getIO(Direction.SOUTH).getImage());
		g.fillRect(7 * member.getSize().getmultiple(), 25 * member.getSize().getmultiple(), 16 * member.getSize().getmultiple(), 4 * member.getSize().getmultiple());
		g.setColor(logicMember.getIO(Direction.NORTH).getImage());
		g.fillRect(7 * member.getSize().getmultiple(), member.getSize().getmultiple(), 16 * member.getSize().getmultiple(), 4 * member.getSize().getmultiple());
		if(logicMember.getOnOffStatus())
		{
			g.setColor(new Color(247, 150, 70));
		}
		else
		{
			g.setColor(new Color(127, 127, 127));
		}
		g.fillRect(7 * member.getSize().getmultiple(), 7 * member.getSize().getmultiple(), 16 * member.getSize().getmultiple(), 16 * member.getSize().getmultiple());
	}
}
enum IOStatus
{
	NONE("NONE"), RECEIV("RECEIV"), TRANCE("TRANCE");
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
	public final String power;
	private Power(String tag)
	{
		this.power = tag;
	}
}
interface LogicTimeTask
{
	void setSleepTime(int tick);
	void ping();
}