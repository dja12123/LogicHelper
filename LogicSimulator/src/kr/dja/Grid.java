package kr.dja;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.*;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;

import kr.dja.UI.UnderBar;

public class Grid
{
	private UI ui;
	private Size UI_Size;
	private JScrollPane gridScrollPane;
	private ViewPort viewPort;
	private GridPanel gridPanel;
	private int gridSizeX = 30;
	private int gridSizeY = 30;
	private int negativeExtendX = 0;
	private int negativeExtendY = 0;
	private final int MAX_SIZE = 2000;
	private final int MAX_NEGATIVE = 2000;
	 
	private RulerPanel horizonRulerScrollPane;
	private RulerPanel verticalRulerScrollPane;
	private JPanel side;
	
	Grid(UI ui, JScrollPane gridScrollPane)
 	{
		this.ui = ui;
		this.gridScrollPane = gridScrollPane;
		this.UI_Size = Size.middle;
		
		ui.getUnderBar().setGridSizeInfo(gridSizeX, gridSizeY);
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
				g.setColor(new Color(122, 138, 153));
				g.drawLine(UI_Size.getWidth() / 2 - 1, 0, UI_Size.getWidth() / 2 - 1, gridSizeY * UI_Size.getWidth() + (Size.MARGIN * 2));
				g.setColor(Color.gray);
				for(int i = 0; i <= gridSizeY; i++)
				{
					if((negativeExtendY - i) % 10 == 0 && i != gridSizeY)
					{
						g.setColor(new Color(180, 200, 230));
						g.fillRect(1,(i * UI_Size.getWidth()) + Size.MARGIN + 2 , (UI_Size.getWidth() / 2) - 2, UI_Size.getWidth() - 3);
						g.setColor(Color.gray);
					}
					g.drawLine(UI_Size.getWidth() / 4, (i * UI_Size.getWidth()) + Size.MARGIN,  UI_Size.getWidth() / 2, (i * UI_Size.getWidth()) + Size.MARGIN);
				}
			}
			@Override
			void resizeUI()
			{
				this.removeAll();
				this.setPreferredSize(new Dimension(UI_Size.getWidth() / 2, (gridSizeY * UI_Size.getWidth()) + (Size.MARGIN * 2)));
				for(int i = 0; i < gridSizeY; i++)
				{
					JLabel label = new JLabel(Integer.toString(i - negativeExtendY), SwingConstants.CENTER)
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
					this.repaint();
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
				g.setColor(new Color(122, 138, 153));
				g.drawLine(0, UI_Size.getWidth() / 2 - 1, gridSizeX * UI_Size.getWidth() + (Size.MARGIN * 2), UI_Size.getWidth() / 2 - 1);
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
				this.removeAll();
				this.setPreferredSize(new Dimension((gridSizeX * UI_Size.getWidth()) + (Size.MARGIN * 2), UI_Size.getWidth() / 2));
				for(int i = 0; i < gridSizeX; i++)
				{
					JLabel label = new JLabel(Integer.toString(i - negativeExtendX), SwingConstants.CENTER);
					label.setFont(LogicCore.RES.NORMAL_FONT.deriveFont((float)(UI_Size.getWidth() / 3.5)));
					label.setBounds((UI_Size.getWidth() * i) + Size.MARGIN, 0, UI_Size.getWidth(), UI_Size.getWidth() / 2);
					this.add(label);
					this.repaint();
				}
			}
		};
		this.side = new JPanel();

		this.viewPort = new ViewPort();
		this.gridPanel = new GridPanel();
		this.viewPort.setView(this.gridPanel);
		
		gridScrollPane.setViewport(viewPort);
		
		
		
		gridScrollPane.setRowHeaderView(horizonRulerScrollPane);
		gridScrollPane.setColumnHeaderView(verticalRulerScrollPane);

		gridScrollPane.setCorner(JScrollPane.LOWER_LEFT_CORNER, side);
		gridScrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, side);
		gridScrollPane.setCorner(JScrollPane.UPPER_RIGHT_CORNER, side);
		

		
		
		this.reSize(UI_Size);
		//UI_Instance.setGridPanel(gridScrollPane);
		
		
	}
	void gridExtend(SizeExt ext, int size)
	{
		if(ext == SizeExt.EAST)
		{
			if(gridSizeX + size < 1)
			{
				gridSizeX = 1;
			}
			else if(gridSizeX + size > MAX_SIZE)
			{
				gridSizeX = MAX_SIZE;
			}
			else
			{
				gridSizeX += size;
			}
			viewPort.setViewPosition(new Point(gridPanel.getWidth(), viewPort.getViewPosition().y));
		}
		else if(ext == SizeExt.WEST)
		{
			if(gridSizeX + size < 1)
			{
				gridSizeX = 1;
			}
			else if(gridSizeX + size > MAX_SIZE)
			{
				gridSizeX = MAX_SIZE;
			}
			else
			{
				gridSizeX += size;
				negativeExtendX += size;
			}
			viewPort.setViewPosition(new Point(0, viewPort.getViewPosition().y));
		}
		else if(ext == SizeExt.SOUTH)
		{
			if(gridSizeY + size < 1)
			{
				gridSizeY = 1;
			}
			else if(gridSizeY + size > MAX_SIZE)
			{
				gridSizeY = MAX_SIZE;
			}
			else
			{
				gridSizeY += size;
			}
			viewPort.setViewPosition(new Point(viewPort.getViewPosition().x, gridPanel.getHeight()));
		}
		else if(ext == SizeExt.NORTH)
		{
			if(gridSizeY + size < 1)
			{
				gridSizeY = 1;
			}
			else if(gridSizeY + size > MAX_SIZE)
			{
				gridSizeY = MAX_SIZE;
			}
			else
			{
				gridSizeY += size;
				negativeExtendY += size;
			}
			viewPort.setViewPosition(new Point(viewPort.getViewPosition().x, 0));
		}
		reSize(this.UI_Size);
		viewPort.reSize();
		ui.getUnderBar().setGridSizeInfo(gridSizeX, gridSizeY);
	}
	void reSize(Size size)
	{
		this.horizonRulerScrollPane.resizeUI();
		this.verticalRulerScrollPane.resizeUI();
		this.gridPanel.resizeUI();
		this.UI_Size = size;
	}
	private class ViewPort extends JViewport
	{
		private static final long serialVersionUID = 1L;
		
		private JLayeredPane layeredPane;
		
		private ExpansionPane eastExpansionPane;
		private ExpansionPane westExpansionPane;
		private ExpansionPane southExpansionPane;
		private ExpansionPane northExpansionPane;
		
		private Component dftComponent;
		
		ViewPort()
		{
			this.layeredPane = new JLayeredPane();
			
			eastExpansionPane = new ExpansionPane(SizeExt.EAST)
			{
				{
					this.setSize(Size.MARGIN - 6, Size.MARGIN * 2);
					super.setLayout(new GridLayout(4, 1, 0, 2));
					
				}
			};
			westExpansionPane = new ExpansionPane(SizeExt.WEST)
			{
				{
					this.setSize(Size.MARGIN - 6, Size.MARGIN * 2);
					super.setLayout(new GridLayout(4, 1, 0, 2));
				}
			};
			
			southExpansionPane = new ExpansionPane(SizeExt.SOUTH)
			{
				{
					this.setSize(Size.MARGIN * 2, Size.MARGIN - 6);
					super.setLayout(new GridLayout(1, 4, 2, 0));
				}
			};
			
			northExpansionPane = new ExpansionPane(SizeExt.NORTH)
			{
				{
					this.setSize(Size.MARGIN * 2, Size.MARGIN - 6);
					super.setLayout(new GridLayout(1, 4, 2, 0));
				}
			};
			
			this.layeredPane.add(eastExpansionPane, new Integer(1));
			this.layeredPane.add(westExpansionPane, new Integer(1));
			this.layeredPane.add(southExpansionPane, new Integer(1));
			this.layeredPane.add(northExpansionPane, new Integer(1));
			super.setView(layeredPane);
			
		}
		@Override
		public void setView(Component p)
		{
			this.dftComponent = p;
			
			this.layeredPane.setPreferredSize(new Dimension(this.dftComponent.getPreferredSize().width, this.dftComponent.getPreferredSize().height));
			p.setBounds(0, 0, this.dftComponent.getPreferredSize().width, this.dftComponent.getPreferredSize().height);
			this.layeredPane.add(this.dftComponent, new Integer(0));
		}
		@Override
		public void setViewPosition(Point p)
		{
			super.setViewPosition(p);
			eastExpansionPane.setLocation(dftComponent.getWidth() - eastExpansionPane.getWidth() - 3, (this.getHeight() / 2) + p.y - (eastExpansionPane.getHeight() / 2));
			westExpansionPane.setLocation(3, (this.getHeight() / 2) + p.y - (westExpansionPane.getHeight() / 2));
			southExpansionPane.setLocation((this.getWidth() / 2) + p.x - (southExpansionPane.getWidth() / 2), dftComponent.getHeight() - southExpansionPane.getHeight() - 3);
			northExpansionPane.setLocation((this.getWidth() / 2) + p.x - (southExpansionPane.getWidth() / 2), 3);
			if((this.getSize().width - gridPanel.getSize().width) > 0)
			{
				eastExpansionPane.setLocation(eastExpansionPane.getLocation().x + (this.getSize().width - gridPanel.getSize().width), eastExpansionPane.getLocation().y);
			}
			if((this.getSize().height - gridPanel.getSize().height) > 0)
			{
				southExpansionPane.setLocation(southExpansionPane.getLocation().x , southExpansionPane.getLocation().y + (this.getSize().height - gridPanel.getSize().height));
			}
		}
		void reSize()
		{
			this.layeredPane.setPreferredSize(new Dimension(this.dftComponent.getWidth(), this.dftComponent.getHeight()));
		}
		private abstract class ExpansionPane extends JPanel
		{
			private static final long serialVersionUID = 1L;
			
			protected UIButton expButton;
			protected UIButton expMButton;
			protected UIButton redButton;
			protected UIButton redMButton;
			
			ExpansionPane(SizeExt ext)
			{
				super();
				
				this.setBackground(new Color(200, 200, 200));
				this.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
				this.setLayout(null);
				
				this.expButton = new UIButton(20, 20, null, null);
				this.expMButton = new UIButton(20, 20, null, null);
				this.redButton = new UIButton(20, 20, null, null);
				this.redMButton = new UIButton(20, 20, null, null);
				
				this.expButton.addActionListener(new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent e)
					{
						gridExtend(ext, 1);
					}
				});
				this.expMButton.addActionListener(new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent e)
					{
						gridExtend(ext, 10);
					}
				});
				this.redButton.addActionListener(new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent e)
					{
						gridExtend(ext, -10);
					}
				});
				this.redMButton.addActionListener(new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent e)
					{
						gridExtend(ext, -1);
					}
				});
				
				this.add(expButton);
				this.add(expMButton);
				this.add(redButton);
				this.add(redMButton);
			}
		}
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
		List<GridMember> member = new ArrayList<GridMember>();
		HashMap<Integer, HashMap<Integer, LogicBlock>> logicMember = new HashMap<Integer, HashMap<Integer, LogicBlock>>();
		private static final long serialVersionUID = 1L;
		GridPanel()
		{
			this.setLayout(null);
			this.setBackground(new Color(200, 200, 200));
			this.resizeUI();
			this.addMouseListener(new MouseListener()
			{
				@Override
				public void mouseClicked(MouseEvent arg0)
				{
					System.out.println("gridCheck");
					
				}
				@Override
				public void mouseEntered(MouseEvent arg0){}
				@Override
				public void mouseExited(MouseEvent arg0){}
				@Override
				public void mousePressed(MouseEvent arg0){}
				@Override
				public void mouseReleased(MouseEvent arg0){}
			});
		}
		void addMember(GridMember member)
		{
			
		}
		void addMember(List<GridMember> members)
		{
			
		}
		void removeMember(GridMember member)
		{
			
		}
		void removeMember(List<GridMember> members)
		{
			
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
			this.setSize((gridSizeX * UI_Size.getWidth()) + (Size.MARGIN * 2), (gridSizeY * UI_Size.getWidth()) + (Size.MARGIN * 2));
			this.repaint();
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
	protected int UISizeX;
	protected int UISizeY;
	protected JPanel gridView;
	
	protected GridMember()
	{
		
	}
	protected abstract GridMember clone();
	int getUILocationX()
	{
		return UIlocationX;
	}
	int getUILocationY()
	{
		return UIlocationY;
	}
	int getSizeX()
	{
		return UISizeX;
	}
	int getSizeY()
	{
		return UISizeY;
	}
	final void sizeUpdate()
	{
		
	}
	void put(int x, int y)
	{
		
	}
	BufferedImage getView()
	{
		BufferedImage img = new BufferedImage(gridView.getSize().width, gridView.getSize().height,BufferedImage.TYPE_INT_RGB);
		gridView.paint(img.getGraphics());
		return img;
	}
}
class Partition extends GridMember
{
	Partition()
	{
		
	}
	Partition(Partition org)
	{
		//TODO 복사 구현
	}
	@Override
	public Partition clone()
	{
		return new Partition(this);
	}
}
class Tag extends GridMember
{
	Tag()
	{
	}
	Tag(Tag org)
	{
		//TODO 복사 구현
	}
	@Override
	public Tag clone()
	{
		return new Tag(this);
	}
}
abstract class LogicBlock extends GridMember
{
	protected int blocklocationX;
	protected int blocklocationY;
	protected LogicBlock()
	{

	}
	abstract void updateState();
	@Override
	void put(int x, int y)
	{
		//TODO 좌표 추상화
		//this.blocklocationX = x;
		//this.blocklocationY = y;
	}
}
class AND extends LogicBlock
{
	AND()
	{
	}
	AND(AND org)
	{
		//TODO 복사 구현
	}
	@Override
	void updateState()
	{
		
	}
	@Override
	public AND clone()
	{
		return new AND(this);
	}
}
class OR extends LogicBlock
{
	OR()
	{
	}
	OR(OR org)
	{
		//TODO 복사 구현
	}
	@Override
	void updateState()
	{
		// TODO Auto-generated method stub
		
	}
	@Override
	public OR clone()
	{
		return new OR(this);
	}
}