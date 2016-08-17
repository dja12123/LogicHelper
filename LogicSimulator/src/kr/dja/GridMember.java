package kr.dja;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public abstract class GridMember implements SizeUpdate
{
	protected int UIabslocationX = 0;
	protected int UIabslocationY = 0;
	protected int UIabsSizeX = 1;
	protected int UIabsSizeY = 1;
	protected GridViewPane gridViewPane;
	protected Size size;
	protected final String name;
	private JLayeredPane layeredPane;
	private SelectShowPanel selectView = null;
	private boolean placement = false;

	protected GridMember(Size size, String name)
	{
		this.name = name;
		this.size = size;
		
		this.layeredPane = new JLayeredPane();
		new GridViewPane();
		this.sizeUpdate();
	}
	protected abstract GridMember clone();
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
	private class SelectShowPanel extends JPanel implements SizeUpdate
	{
		private static final long serialVersionUID = 1L;
		int r, g, b, s, a, rs, gs, bs;
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
	}
	class GridViewPane extends JPanel implements SizeUpdate
	{
		private static final long serialVersionUID = 1L;
		GridViewPane()
		{
			gridViewPane = this;
			layeredPane.removeAll();
			layeredPane.add(this, new Integer(0));
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
			this.setSize(UIabsSizeX * size.getmultiple(), UIabsSizeY * size.getmultiple());
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
		return null;
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
		return null;
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
		new LogicViewPane();
		new IOPanel(Direction.EAST);
		new IOPanel(Direction.WEST);
		new IOPanel(Direction.SOUTH);
		new IOPanel(Direction.NORTH);
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
			if(this.io.get(ext).getStatus() == IOStatus.TRANCE && this.io.get(ext).getOnOffStatus())
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
	abstract void calculate();
	class IOPanel
	{
		private IOStatus status;
		private boolean onOffStatus;
		private final Direction ext;
		private Color image;
		IOPanel(Direction ext)
		{
			this.ext = ext;
			io.put(ext, this);
			this.setStatus(IOStatus.NONE);
		}
		void setStatus(IOStatus status)
		{
			this.status = status;
			if(status == IOStatus.NONE)
			{
				this.image = gridViewPane.getBackground();
			}
			else if(status == IOStatus.RECEIV)
			{
				this.image = new Color(127, 127, 127);
			}
			else if(status == IOStatus.TRANCE)
			{
				this.image = new Color(99, 37, 35);
			}
			calculate();
			gridViewPane.repaint();
		}
		IOStatus getStatus()
		{
			return this.status;
		}
		void setOnOffStatus(boolean status)
		{
			this.onOffStatus = status;
			calculate();
		}
		private boolean getOnOffStatus()
		{
			return this.onOffStatus;
		}
		Direction getDirection()
		{
			return this.ext;
		}
		Color getImage()
		{
			return this.image;
		}
	}
	class LogicViewPane extends GridViewPane
	{
		private static final long serialVersionUID = 1L;
		LogicViewPane()
		{
			super();
		}
		@Override
		public void paint(Graphics g)
		{//임시 땜빵용
			super.paint(g);
			g.setColor(getIO(Direction.EAST).getImage());
			g.fillRect(25 * size.getmultiple(), 7 * size.getmultiple(), 4 * size.getmultiple(), 16 * size.getmultiple());
			g.setColor(getIO(Direction.WEST).getImage());
			g.fillRect(size.getmultiple(), 7 * size.getmultiple(), 4 * size.getmultiple(), 16 * size.getmultiple());
			g.setColor(getIO(Direction.SOUTH).getImage());
			g.fillRect(7 * size.getmultiple(), 25 * size.getmultiple(), 16 * size.getmultiple(), 4 * size.getmultiple());
			g.setColor(getIO(Direction.NORTH).getImage());
			g.fillRect(7 * size.getmultiple(), size.getmultiple(), 16 * size.getmultiple(), 4 * size.getmultiple());
			if(onOffStatus)
			{
				g.setColor(new Color(247, 150, 70));
			}
			else
			{
				g.setColor(new Color(127, 127, 127));
			}
			g.fillRect(7 * size.getmultiple(), 7 * size.getmultiple(), 16 * size.getmultiple(), 16 * size.getmultiple());
		}
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
		return null;
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
		return null;
	}
	@Override
	void calculate()
	{
		// TODO Auto-generated method stub
		
	}
}
class IOStatus
{
	public final String tag;
	public static final IOStatus NONE = new IOStatus("NONE");
	public static final IOStatus RECEIV = new IOStatus("RECEIV");
	public static final IOStatus TRANCE = new IOStatus("TRANCE");
	private IOStatus(String tag)
	{
		this.tag = tag;
	}
	public String getTag()
	{
		return this.tag;
	}
}