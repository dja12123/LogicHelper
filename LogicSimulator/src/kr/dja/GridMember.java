package kr.dja;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;

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
	protected GridViewPane gridViewPane;
	protected Size size;
	protected final String name;
	protected GridLayeredPane layeredPane;
	protected TaskOperator operator;
	protected Grid grid;
	private boolean placement = false;

	protected GridMember(Size size, String name)
	{
		this.name = name;
		this.size = size;
		
		this.layeredPane = new GridLayeredPane();
		this.gridViewPane = new GridViewPane(this);
		this.sizeUpdate();
		this.layeredPane.add(this.gridViewPane, new Integer(1));;
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
	Size getSize()
	{
		return this.size;
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
	void put(int absX, int absY, Grid grid, TaskOperator operator)
	{
		this.sizeUpdate();
		this.UIabslocationX = absX;
		this.UIabslocationY = absY;
		this.grid = grid;
		this.operator = operator;
		this.grid.members.add(this);
		System.out.println("PUT: " + UIabslocationX + " " + UIabslocationY);
		System.out.println(this.layeredPane.getSize());
		this.placement = true;
	}
	void remove()
	{
		grid.members.remove(this);
		grid.getGridPanel().remove(this.getGridViewPane());
		grid.deSelect(this);
		grid.getGridPanel().repaint();
		
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
	@Override
	public void sizeUpdate()
	{
		this.gridViewPane.sizeUpdate();
		this.layeredPane.setSize(UIabsSizeX * size.getmultiple(), UIabsSizeY * size.getmultiple());
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
	private int timer = 0;
	
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
	void put(int absX, int absY, Grid grid, TaskOperator operator)
	{
		
		absX = absX / Size.REGULAR_SIZE;
		absY = absY / Size.REGULAR_SIZE;
		this.blocklocationX = absX;
		this.blocklocationY = absY;
		
		if(grid.logicMembers.containsKey(new Integer(this.getBlockLocationX())) && grid.logicMembers.get(new Integer(this.getBlockLocationX())).containsKey(this.getBlockLocationY()))
		{
			grid.removeMember(grid.logicMembers.get(new Integer(this.getBlockLocationX())).get(this.getBlockLocationY()));
		}
		super.put((this.blocklocationX * Size.REGULAR_SIZE), (this.blocklocationY * Size.REGULAR_SIZE),grid,  operator);
		if(!grid.logicMembers.containsKey(new Integer(this.getBlockLocationX())))
		{
			grid.logicMembers.put(new Integer(this.getBlockLocationX()), new HashMap<Integer, LogicBlock>());
		}
		grid.logicMembers.get(new Integer(this.getBlockLocationX())).put(new Integer(this.getBlockLocationY()), this);
		this.operator.checkAroundAndReserveTask(this);
		System.out.println("ADDMEMBER");
		super.getGridViewPane().setLocation((this.getUIabsLocationX() + (grid.getNegativeExtendX() * Size.REGULAR_SIZE)) * super.getSize().getmultiple() + Size.MARGIN, (super.getUIabsLocationY() + (grid.getNegativeExtendY() * Size.REGULAR_SIZE)) * super.getSize().getmultiple() + Size.MARGIN);
		grid.getGridPanel().add(super.getGridViewPane());
		grid.selectFocus(this);
		
	}
	@Override
	protected void remove()
	{
		super.remove();
		grid.logicMembers.get(new Integer(this.getBlockLocationX())).remove(new Integer(this.getBlockLocationY()), this);
		if(grid.logicMembers.get(new Integer(this.getBlockLocationX())).size() == 0)
		{
			grid.logicMembers.remove(new Integer(this.getBlockLocationX()));
		}
		this.operator.removeReserveTask(this);
		System.out.println("REMOVEMEMBER");
		this.operator.checkAroundAndReserveTask(this);
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
			this.getOperator().checkAroundAndReserveTask(this);
		}
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
			this.operator.addReserveTask(this);

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
	TaskOperator getOperator()
	{
		return this.operator;
	}
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
		ArrayList<Power> cal = super.getResiveIOPower();
		int powerCal = 0;
		for(Power power : cal)
		{
			System.out.println(power.getBool());
			if(power.getBool())
			{
				powerCal++;
			}
		}
		if(powerCal == cal.size() && powerCal != 0)
		{
			super.setPowerStatus(Power.ON);
			System.out.println("Power: " + powerCal);
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
	@Override
	protected void operatorPing()
	{
		// TODO Auto-generated method stub
		
	}
}
class XOR extends LogicBlock
{
	XOR(Size size)
	{
		super(size, "XOR");
	}
	@Override
	public XOR clone(Size size)
	{
		XOR cloneMember = new XOR(size);
		super.clone(cloneMember);
		return cloneMember;
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
	protected void doToggleIO(Direction ext)
	{
		if(super.getIOResiveCount() > 0)
		{
			if(this.getIOStatus(ext) == IOStatus.NONE)
			{
				this.io.get(ext).setStatus(IOStatus.TRANCE);
			}
			else if(this.getIOStatus(ext) == IOStatus.TRANCE)
			{
				this.io.get(ext).setStatus(IOStatus.NONE);
			}
			else
			{
				this.io.get(ext).setStatus(IOStatus.NONE);
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
	
	Button(Size size)
	{
		super(size, "BTN");
		this.timeLabel.setVerticalAlignment(JTextField.CENTER);
		this.timeLabel.setHorizontalAlignment(JTextField.CENTER);
		super.layeredPane.add(this.timeLabel, new Integer(20));
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
	protected void doToggleIO(Direction ext)
	{
		if(this.getIOStatus(ext) == IOStatus.NONE)
		{
			this.io.get(ext).setStatus(IOStatus.TRANCE);
		}
		else if(this.getIOStatus(ext) == IOStatus.TRANCE)
		{
			this.io.get(ext).setStatus(IOStatus.NONE);
		}
	}
	void resetTimer()
	{
		super.setTimer(basicTime);
		this.operator.addReserveTask(this);
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
			this.timeLabel.setFont(LogicCore.RES.PIXEL_FONT.deriveFont((float)(12 * super.size.getmultiple())));
			this.timeLabel.setBounds((7 * size.getmultiple()) + super.gridViewPane.getX(), (7 * size.getmultiple()) + super.gridViewPane.getY(), 16 * size.getmultiple(), 16 * size.getmultiple());
		}
		if(this.btn == null)
		{
			this.btn = new TimerButton();
		}
		else
		{
			this.btn.setBounds(7 * size.getmultiple(), 7 * size.getmultiple(), 16 * size.getmultiple(), 16 * size.getmultiple());
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
			super.setBasicImage(LogicCore.getResource().getImage(size.getTag() + "_BUTTON_" + power.getTag()));
			super.setBasicPressImage(LogicCore.getResource().getImage(size.getTag() + "_BUTTON_PRESS_" + power.getTag()));
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
	private IOStatus status;
	private Power power = Power.OFF;
	private final Direction ext;
	private final LogicBlock member;
	private BufferedImage image;
	IOPanel(LogicBlock logicMember, Direction ext)
	{
		this.member = logicMember;
		this.ext = ext;
		this.setStatus(IOStatus.NONE);
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
		}
		this.member.calculate();
		this.image = LogicCore.RES.getImage(member.getSize().getTag() + "_" + this.status.getTag() + "_" + this.power.getTag() + "_" + this.ext.getTag());
	}
	IOStatus getStatus()
	{
		return this.status;
	}
	void setOnOffStatus(Power power)
	{
		this.power = power;
		this.image = LogicCore.RES.getImage(member.getSize().getTag() + "_" + this.status.getTag() + "_" + this.power.getTag() + "_" + this.ext.getTag());
	}
	Power getOnOffStatus()
	{
		System.out.println(this.power);
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
	{
		g.drawImage(LogicCore.getResource().getImage(logicMember.getSize().getTag() + "_BLOCK_BACKGROUND"), 0, 0, this);
		if(logicMember.getIOStatus(Direction.EAST) != IOStatus.NONE)
		{
			g.drawImage(logicMember.getIOImage(Direction.EAST), 25 * member.getSize().getmultiple(), 7 * member.getSize().getmultiple(), this);
		}
		if(logicMember.getIOStatus(Direction.WEST) != IOStatus.NONE)
		{
			g.drawImage(logicMember.getIOImage(Direction.WEST), 1 * member.getSize().getmultiple(), 7 * member.getSize().getmultiple(), this);
		}
		if(logicMember.getIOStatus(Direction.SOUTH) != IOStatus.NONE)
		{
			g.drawImage(logicMember.getIOImage(Direction.SOUTH), 7 * member.getSize().getmultiple(), 25 * member.getSize().getmultiple(), this);
		}
		if(logicMember.getIOStatus(Direction.NORTH) != IOStatus.NONE)
		{
			g.drawImage(logicMember.getIOImage(Direction.NORTH), 7 * member.getSize().getmultiple(), 1 * member.getSize().getmultiple(), this);
		}
		g.drawImage(LogicCore.getResource().getImage(logicMember.getSize().getTag() + "_BLOCK_" + logicMember.getPower().getTag()), 7 * member.getSize().getmultiple(), 7 * member.getSize().getmultiple(), this);
		super.paintChildren(g);
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