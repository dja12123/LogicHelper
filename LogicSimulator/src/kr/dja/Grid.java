package kr.dja;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.util.*;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;

public class Grid
{
	List<GridMember> member = new ArrayList<GridMember>();
	HashMap<Integer, HashMap<Integer, LogicBlock>> logicMember = new HashMap<Integer, HashMap<Integer, LogicBlock>>();
	private Size UI_Size;
	private JScrollPane gridScrollPane;
	private GridPanel gridPanel;
	private int gridSizeX = 50;
	private int gridSizeY = 30;
	private int negativeExtendX = 0;
	private int negativeExtendY = 0;
	
	private RulerPanel horizonRulerScrollPane;
	private RulerPanel verticalRulerScrollPane;
	private JPanel side;
	
	Grid(JScrollPane gridScrollPane)
 	{
		this.gridScrollPane = gridScrollPane;
		this.UI_Size = Size.middle;
		
		gridScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		gridScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		gridScrollPane.getVerticalScrollBar().setUnitIncrement((int)(this.UI_Size.getWidth() / 2.5));
		gridScrollPane.getHorizontalScrollBar().setUnitIncrement((int)(this.UI_Size.getWidth() / 2.5));
		
		horizonRulerScrollPane = new RulerPanel()
		{
			private static final long serialVersionUID = 1L;
			@Override
			public void paintComponent(Graphics g)
			{
				super.paintComponent(g);
				g.setColor(Color.gray);
				for(int i = 0; i <= gridSizeY; i++)
				{
					if((negativeExtendY - i) % 10 == 0 && i != gridSizeY)
					{
						g.setColor(new Color(180, 200, 230));
						g.fillRect(1,(i * UI_Size.getWidth()) + Size.MARGIN + 2 , (UI_Size.getWidth() / 2) - 2, UI_Size.getWidth() - 3);
						g.setColor(Color.gray);
					}
					g.drawLine( UI_Size.getWidth() / 4, (i * UI_Size.getWidth()) + Size.MARGIN,  UI_Size.getWidth() / 2, (i * UI_Size.getWidth()) + Size.MARGIN);
				}
			}
			@Override
			void resizeUI()
			{
				this.setPreferredSize(new Dimension(UI_Size.getWidth() / 2, (gridSizeY * UI_Size.getWidth()) + (Size.MARGIN * 2)));
				for(int i = 0; i < gridSizeY; i++)
				{
					JLabel label = new JLabel(Integer.toString(i - negativeExtendX), SwingConstants.CENTER)
					{
						private static final long serialVersionUID = 1L;
						@Override
						protected void paintComponent(Graphics g)
						{
							Graphics2D g2d = (Graphics2D) g.create();
							g2d.translate(-getSize().getHeight() / 4, getSize().getWidth());
							g2d.transform(AffineTransform.getQuadrantRotateInstance(-1));
							super.paintComponent(g2d);
						}
					};
					label.setFont(LogicCore.RES.NORMAL_FONT.deriveFont((float)(UI_Size.getWidth() / 3.5)));
					label.setBounds(0, (UI_Size.getWidth() * i) + Size.MARGIN, UI_Size.getWidth(), UI_Size.getWidth());
					
					this.add(label);
				}
			}
		};
		verticalRulerScrollPane = new RulerPanel()
		{
			private static final long serialVersionUID = 1L;
			@Override
			public void paintComponent(Graphics g)
			{
				super.paintComponent(g);
				g.setColor(Color.gray);
				for(int i = 0; i <= gridSizeX; i++)
				{
					
					if((negativeExtendX - i) % 10 == 0 && i != gridSizeX)
					{
						g.setColor(new Color(180, 200, 230));
						g.fillRect((i * UI_Size.getWidth()) + Size.MARGIN + 2,1 , UI_Size.getWidth() - 3, (UI_Size.getWidth() / 2) - 2);
						g.setColor(Color.gray);
					}
					g.drawLine((i * UI_Size.getWidth()) + Size.MARGIN, UI_Size.getWidth() / 4, (i * UI_Size.getWidth()) + Size.MARGIN, UI_Size.getWidth() / 2);
				}
			}
			@Override
			void resizeUI()
			{
				this.setPreferredSize(new Dimension((gridSizeX * UI_Size.getWidth()) + (Size.MARGIN * 2), UI_Size.getWidth() / 2));
				for(int i = 0; i < gridSizeX; i++)
				{
					JLabel label = new JLabel(Integer.toString(i - negativeExtendY), SwingConstants.CENTER);
					label.setFont(LogicCore.RES.NORMAL_FONT.deriveFont((float)(UI_Size.getWidth() / 3.5)));
					label.setBounds((UI_Size.getWidth() * i) + Size.MARGIN, 0, UI_Size.getWidth(), UI_Size.getWidth() / 2);
					this.add(label);
				}
				
				
				
			}
		};

		
		side = new JPanel();
		
		gridScrollPane.setRowHeaderView(horizonRulerScrollPane);
		gridScrollPane.setColumnHeaderView(verticalRulerScrollPane);
		gridScrollPane.setCorner(JScrollPane.LOWER_LEFT_CORNER, side);
		gridScrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, side);
		gridScrollPane.setCorner(JScrollPane.UPPER_RIGHT_CORNER, side);
		
		//JPanel testPanel = new JPanel();
		//testPanel.setBackground(Color.red);
		//JPanel testPanel1 = new JPanel();
		//testPanel1.setPreferredSize(new Dimension(30, 0));
		//gridScrollPane.setCorner(JScrollPane.LOWER_LEFT_CORNER, testPanel);
		//gridScrollPane.setRowHeaderView(testPanel1);
		this.gridPanel = new GridPanel();
		this.reSize(UI_Size);
		//UI_Instance.setGridPanel(gridScrollPane);
	}
	void gridExtend(SizeExt ext, int size)
	{
		if(ext == SizeExt.EAST)
		{
			negativeExtendY += size;
		}
		else if(ext == SizeExt.NORTH)
		{
			negativeExtendX += size;
			gridSizeX += size;
		}
		else if(ext == SizeExt.SOUTH)
		{
			negativeExtendX += size;
		}
		else if(ext == SizeExt.WEST)
		{
			negativeExtendY += size;
			gridSizeY += size;
		}
	}
	void reSize(Size size)
	{
		this.horizonRulerScrollPane.resizeUI();
		this.verticalRulerScrollPane.resizeUI();
		this.gridPanel.resizeUI();
		this.UI_Size = size;
	}
	private class RulerPanel extends JPanel
	{
		private static final long serialVersionUID = 1L;
		RulerPanel()
		{
			setLayout(null);
			setBackground(new Color(200, 220, 250));
		}
		@Override
		public void paintComponent(Graphics g)
		{
			super.paintComponent(g);
		}
		void resizeUI()
		{
		}
	}
	private class GridPanel extends JPanel
	{
		private static final long serialVersionUID = 1L;
		GridPanel()
		{
			this.setLayout(null);
			this.setBackground(new Color(200, 200, 200));
			gridScrollPane.getViewport().setView(this);
			//reSize(Size.middle);
		}
		@Override
		public void paintComponent(Graphics g)
		{//최적화 가능
			super.paintComponent(g);
			g.setColor(new Color(150, 150, 150));
			for(int x = 0; x <= gridSizeX; x++)
			{
				g.drawLine((x * UI_Size.getWidth()) + Size.MARGIN, Size.MARGIN, (x * UI_Size.getWidth()) + Size.MARGIN, (gridSizeY * UI_Size.getWidth()) + Size.MARGIN);
			}
			for(int y = 0; y <= gridSizeY; y++)
			{
				g.drawLine(Size.MARGIN, (y * UI_Size.getWidth()) + Size.MARGIN, (gridSizeX * UI_Size.getWidth()) + Size.MARGIN, (y * UI_Size.getWidth()) + Size.MARGIN);
			}
		}
		void resizeUI()
		{
			this.setPreferredSize(new Dimension((gridSizeX * UI_Size.getWidth()) + (Size.MARGIN * 2), (gridSizeY * UI_Size.getWidth()) + (Size.MARGIN * 2)));
		}
	}
}
class SizeExt
{
	public static final SizeExt EAST = new SizeExt();
	public static final SizeExt NORTH = new SizeExt();
	public static final SizeExt WEST = new SizeExt();
	public static final SizeExt SOUTH = new SizeExt();
}
class Size
{

	public static final int REGULAR_SIZE = 10;
	public static final int MARGIN = 50;
	public static final Size small = new Size(3);
	public static final Size middle = new Size(6);
	public static final Size big = new Size(12);
	private double multiple;
	private Size(int multiple)
	{
		this.multiple = multiple;
	}
	public double getmultiple()
	{
		return multiple;
	}
	public int getWidth()
	{
		return (int)(REGULAR_SIZE * multiple);
	}
}
abstract class GridMember
{
	protected int UIlocationX;
	protected int UIlocationY;
	
	protected GridMember(int x, int y)
	{
		//member.add(this);
	}
	abstract GridMember createInstance(int x, int y);
	abstract void getLocationX();
	abstract void getLocationY();
	final void sizeUpdate()
	{
		
	}
}
class Partition extends GridMember
{
	private Partition(int x, int y)
	{
		super(x, y);
	}
	@Override
	GridMember createInstance(int x, int y)
	{
		return new Partition(x, y);
	}
	@Override
	void getLocationX() {
		// TODO Auto-generated method stub
		
	}
	@Override
	void getLocationY() {
		// TODO Auto-generated method stub
		
	}
}
class Tag extends GridMember
{
	private Tag(int x, int y)
	{
		super(x, y);
	}
	@Override
	GridMember createInstance(int x, int y)
	{
		return new Tag(x, y);
	}
	@Override
	void getLocationX() {
		// TODO Auto-generated method stub
	}
	@Override
	void getLocationY() {
		// TODO Auto-generated method stub
		
	}
}
abstract class LogicBlock extends GridMember
{
	protected int blocklocationX;
	protected int blocklocationY;
	protected LogicBlock(int x, int y)
	{
		super(x, y);
		this.blocklocationX = x;
		this.blocklocationY = y;
	}
	abstract void updateState();
	@Override
	GridMember createInstance(int x, int y)
	{
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	void getLocationX() {
		// TODO Auto-generated method stub
		
	}
	@Override
	void getLocationY() {
		// TODO Auto-generated method stub
		
	}
	
}
class AND extends LogicBlock
{
	private AND(int x, int y)
	{
		super(x, y);
	}
	@Override
	GridMember createInstance(int x, int y)
	{
		return new AND(x, y);
	}
	@Override
	void updateState() {
		// TODO Auto-generated method stub
		
	}
}
class OR extends LogicBlock
{
	private OR(int x, int y)
	{
		super(x, y);
	}
	@Override
	GridMember createInstance(int x, int y)
	{
		return new OR(x, y);
	}
	@Override
	void updateState()
	{
		// TODO Auto-generated method stub
		
	}
}