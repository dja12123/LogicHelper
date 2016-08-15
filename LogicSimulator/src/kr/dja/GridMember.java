package kr.dja;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

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
	private JLayeredPane layeredPane;
	private SelectShowPanel selectView = null;
	private EditPanel editPanel;

	protected GridMember(Size size)
	{
		System.out.println("first");
		this.size = size;
		
		this.layeredPane = new JLayeredPane();
		new GridViewPane();
		new EditPanel();
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
	int getUIabsLocationX()
	{//실제 위치는 절대 위치에 배수를 곱해서 사용
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
	EditPanel getEditPanel()
	{
		return this.editPanel;
	}
	@Override
	public void sizeUpdate()
	{
		this.gridViewPane.sizeUpdate();
		this.layeredPane.setSize(UIabsSizeX * size.getmultiple(), UIabsSizeY * size.getmultiple());
		this.editPanel.sizeUpdate();
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
			this.setBackground(new Color(125, 125, 125));
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
	class EditPanel extends JPanel implements SizeUpdate
	{
		private static final long serialVersionUID = 1L;
		
		EditPanel()
		{
			editPanel = this;
			this.setLayout(null);
		}
		@Override
		public void sizeUpdate()
		{
			
		}
	}
}
class Partition extends GridMember
{
	Partition(Size size)
	{
		super(size);
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
		super(size);
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
	protected boolean eastIOStatus = false;
	protected boolean westIOStatus = false;
	protected final String name;
	
	protected LogicBlock(Size size, String name)
	{
		super(size);
		this.name = name;
		System.out.println("second");
		super.UIabsSizeX = 30;
		super.UIabsSizeY = 30;
		new LogicViewPane();
	}
	abstract void updateState();
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
	private class IOPanel
	{
		void on()
		{
			
		}
		boolean getStatus()
		{
			return false;
		}
	}
	class LogicViewPane extends GridViewPane
	{
		private static final long serialVersionUID = 1L;
		LogicViewPane()
		{
			super();
			new LogicEdit();
		}
		@Override
		public void paint(Graphics g)
		{
			super.paint(g);
			g.setColor(new Color(255, 100, 100));
			g.fillRect(7 * size.getmultiple(), size.getmultiple(), 16 * size.getmultiple(), 4 * size.getmultiple());
			g.fillRect(size.getmultiple(), 7 * size.getmultiple(), 4 * size.getmultiple(), 16 * size.getmultiple());
			g.fillRect(7 * size.getmultiple(), 25 * size.getmultiple(), 16 * size.getmultiple(), 4 * size.getmultiple());
			g.fillRect(25 * size.getmultiple(), 7 * size.getmultiple(), 4 * size.getmultiple(), 16 * size.getmultiple());
		}
	}
	class LogicEdit extends EditPanel
	{
		private static final long serialVersionUID = 1L;
		
		private JLabel text = new JLabel();
		private JLabel locationText = new JLabel();
		private UIButton restoreButton = new UIButton(265, 70, 40, 40, null, null);
		private UIButton removeButton = new UIButton(320, 70, 40, 40, null, null);
		
		private JPanel editViewPanel = new JPanel()
		{
			@Override
			public void paint(Graphics g)
			{
				super.paint(g);
				g.setColor(new Color(255, 100, 100));
				g.fillRect(28, 4, 64, 16);
				g.fillRect(4, 28, 16, 64);
				g.fillRect(28, 100, 64, 16);
				g.fillRect(100, 28, 16, 64);
				g.setColor(new Color(50, 200, 250));
				g.drawRect(0, 0, 119, 119);
			}
		};
		
		LogicEdit()
		{
			this.text.setHorizontalAlignment(SwingConstants.CENTER);
			this.text.setFont(LogicCore.RES.NORMAL_FONT.deriveFont(16.0f));
			this.text.setBounds(135, 0, 223, 20);
			this.text.setText(name + " 게이트 편집");
			
			this.locationText.setHorizontalAlignment(SwingConstants.LEADING);
			this.locationText.setFont(LogicCore.RES.NORMAL_FONT.deriveFont(16.0f));
			this.locationText.setBounds(135, 30, 223, 20);
			
			this.editViewPanel.setBounds(5, 0, 120, 120);
			this.editViewPanel.setBackground(new Color(200, 200, 200));
			
			this.add(this.text);
			this.add(this.locationText);
			this.add(this.restoreButton);
			this.add(this.removeButton);
			this.add(this.editViewPanel);
		}
		@Override
		public void sizeUpdate()
		{
			this.locationText.setText("위치: X:" + blocklocationX + " Y: " + blocklocationY);
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
	void updateState()
	{
		
	}
	@Override
	public AND clone()
	{
		return null;
	}
}
class OR extends LogicBlock
{
	OR(Size size)
	{
		super(size, "OR");
	}
	@Override
	void updateState()
	{
		// TODO Auto-generated method stub
		
	}
	@Override
	public OR clone()
	{
		return null;
	}
}
