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
	private int gridSizeX = 10;
	private int gridSizeY = 10;
	private int negativeExtendX = 0;
	private int negativeExtendY = 0;
	private final int MAX_SIZE = 15;
	private final int MAX_ABSOLUTE = 10;
	 
	private RulerPanel horizonRulerScrollPane;
	private RulerPanel verticalRulerScrollPane;
	private JPanel side;
	
	Grid(UI ui)
 	{
		this.ui = ui;
		this.gridScrollPane = new JScrollPane();;
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
			public void sizeUpdate()
			{
				this.removeAll();//수정 필요
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
			public void sizeUpdate()
			{
				this.removeAll();//수정 필요
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
	Size getUISize()
	{//이름 변경 필요
		return this.UI_Size;
	}
	int getNegativeExtendX()
	{
		return this.negativeExtendX;
	}
	int getNegativeExtendY()
	{
		return this.negativeExtendY;
	}
	int getgridSizeX()
	{
		return this.gridSizeX;
	}
	int getgridSizeY()
	{
		return this.gridSizeX;
	}
	void gridExtend(SizeExt ext, int size)
	{//그리드 넓이 확장
		if(ext == SizeExt.EAST)
		{
			if(gridSizeX + size < 1)
			{//사이즈 최저치 예외
				size -= gridSizeX + size - 1;
			}
			if(Math.abs(gridSizeX + size - negativeExtendX) > MAX_ABSOLUTE + 1)
			{//사이즈 최대 설정 한계 예외
				size -= Math.abs(gridSizeX + size - negativeExtendX) - MAX_ABSOLUTE - 1;
			}
			if(gridSizeX + size > MAX_SIZE)
			{//사이즈 최대 크기 한계 예외
				size -= gridSizeX + size - MAX_SIZE;
			}
			gridSizeX += size;
			viewPort.setViewPosition(new Point(gridPanel.getWidth(), viewPort.getViewPosition().x));
		}
		else if(ext == SizeExt.WEST)
		{
			if(gridSizeX + size < 1)
			{//사이즈 최저치 예외
				size -= gridSizeX + size - 1;
			}
			if(Math.abs(size + negativeExtendX) > MAX_ABSOLUTE)
			{//사이즈 최대 설정 한계 예외
				size -= Math.abs(size + negativeExtendX) - MAX_ABSOLUTE;
			}
			if(gridSizeX + size > MAX_SIZE)
			{
				size -= gridSizeX + size - MAX_SIZE;
			}//사이즈 최대 크기 한계 예외
			gridSizeX += size;
			negativeExtendX += size;
			viewPort.setViewPosition(new Point(0, viewPort.getViewPosition().x));
		}
		else if(ext == SizeExt.SOUTH)
		{
			if(gridSizeY + size < 1)
			{//사이즈 최저치 예외
				size -= gridSizeY + size - 1;
			}
			if(Math.abs(gridSizeY + size - negativeExtendY) > MAX_ABSOLUTE + 1)
			{//사이즈 최대 설정 한계 예외
				size -= Math.abs(gridSizeY + size - negativeExtendY) - MAX_ABSOLUTE - 1;
			}
			if(gridSizeY + size > MAX_SIZE)
			{//사이즈 최대 크기 한계 예외
				size -= gridSizeY + size - MAX_SIZE;
			}
			gridSizeY += size;
			viewPort.setViewPosition(new Point(viewPort.getViewPosition().y, gridPanel.getHeight()));
		}
		else if(ext == SizeExt.NORTH)
		{
			if(gridSizeY + size < 1)
			{//사이즈 최저치 예외
				size -= gridSizeY + size - 1;
			}
			if(Math.abs(size + negativeExtendY) > MAX_ABSOLUTE)
			{//사이즈 최대 설정 한계 예외
				size -= Math.abs(size + negativeExtendY) - MAX_ABSOLUTE;
			}
			if(gridSizeY + size > MAX_SIZE)
			{
				size -= gridSizeY + size - MAX_SIZE;
			}//사이즈 최대 크기 한계 예외
			gridSizeY += size;
			negativeExtendY += size;
			viewPort.setViewPosition(new Point(viewPort.getViewPosition().y, 0));
		}
		reSize(this.UI_Size);
		viewPort.sizeUpdate();
		ui.getUnderBar().setGridSizeInfo(gridSizeX, gridSizeY);
	}
	void reSize(Size size)
	{
		this.horizonRulerScrollPane.sizeUpdate();
		this.verticalRulerScrollPane.sizeUpdate();
		this.gridPanel.sizeUpdate();
		this.UI_Size = size;
	}
	JScrollPane getGridScrollPanel()
	{
		return this.gridScrollPane;
	}
	GridPanel getGridPanel()
	{
		return this.gridPanel;
	}
	private class ViewPort extends JViewport implements SizeUpdate
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
		@Override
		public void sizeUpdate()
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
	private abstract class RulerPanel extends JPanel implements SizeUpdate
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
	}
	class GridPanel extends JPanel implements SizeUpdate
	{
		List<GridMember> members = new ArrayList<GridMember>();
		HashMap<Integer, HashMap<Integer, LogicBlock>> logicMember = new HashMap<Integer, HashMap<Integer, LogicBlock>>();
		private static final long serialVersionUID = 1L;
		GridPanel()
		{
			this.setLayout(null);
			this.setBackground(new Color(200, 200, 200));
			this.sizeUpdate();
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
			if(member instanceof LogicBlock)
			{
				LogicBlock block = ((LogicBlock) member);
				if(!logicMember.containsKey(new Integer(block.getBlockLocationX())))
				{
					logicMember.put(new Integer(block.getBlockLocationX()), new HashMap<Integer, LogicBlock>());
				}
				else if(logicMember.get(new Integer(block.getBlockLocationX())).containsKey(block.getBlockLocationY()))
				{
					this.removeMember(logicMember.get(new Integer(block.getBlockLocationX())).get(block.getBlockLocationY()));
				}
				logicMember.get(new Integer(block.getBlockLocationX())).put(new Integer(block.getBlockLocationY()), block);
			}
			this.members.add(member);
			this.add(member.getGridViewPane());
		}
		void removeMember(GridMember member)
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
		@Override
		public void sizeUpdate()
		{
			this.setSize((gridSizeX * UI_Size.getWidth()) + (Size.MARGIN * 2), (gridSizeY * UI_Size.getWidth()) + (Size.MARGIN * 2));
			for(GridMember member : members)
			{
				member.getGridViewPane().sizeUpdate();
			}
			this.repaint();
		}
	}
	abstract class GridMember implements SizeUpdate
	{
		protected int UIabslocationX = 0;
		protected int UIabslocationY = 0;
		protected int UIabsSizeX = 1;
		protected int UIabsSizeY = 1;
		protected GridViewPane gridViewPane;
		
		protected GridMember()
		{
			this.gridViewPane = new GridViewPane();
			this.gridViewPane.setBackground(Color.red);
			this.gridViewPane.setLayout(null);
			this.sizeUpdate();
		}
		protected abstract GridMember clone();
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
		@Override
		public void sizeUpdate()
		{
			this.gridViewPane.sizeUpdate();
		}
		void put(int absX, int absY)
		{
			if(absX >= (0 - (negativeExtendX * Size.REGULAR_SIZE)) && absX <= (gridSizeX - negativeExtendX) * Size.REGULAR_SIZE && absY >= (0 - (negativeExtendY * Size.REGULAR_SIZE)) && absY <= (gridSizeY - negativeExtendY) * Size.REGULAR_SIZE)
			{

				this.UIabslocationX = absX;
				this.UIabslocationY = absY;
				System.out.println("PUT: " + UIabslocationX + " " + UIabslocationY);
				this.sizeUpdate();
				gridPanel.addMember(this);
			}
		}
		BufferedImage getSnapShot()
		{
			BufferedImage img = new BufferedImage(this.gridViewPane.getWidth(), this.gridViewPane.getHeight(), BufferedImage.TYPE_INT_ARGB);
			this.gridViewPane.printAll(img.getGraphics());
			return img;
		}
		GridViewPane getGridViewPane()
		{
			return this.gridViewPane;
		}
		class GridViewPane extends JPanel implements SizeUpdate
		{
			private static final long serialVersionUID = 1L;
			GridViewPane()
			{
				sizeUpdate();
			}
			@Override
			public void paint(Graphics g)
			{
				super.paint(g);
			}
			@Override
			public void sizeUpdate()
			{
				this.setBounds(((UIabslocationX + (negativeExtendX * Size.REGULAR_SIZE)) * UI_Size.getmultiple()) + Size.MARGIN, ((UIabslocationY + (negativeExtendY * Size.REGULAR_SIZE)) * UI_Size.getmultiple()) + Size.MARGIN, UIabsSizeX * UI_Size.getmultiple(), UIabsSizeY * UI_Size.getmultiple());
			}
		}
	}
	public class Partition extends GridMember
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
	public class Tag extends GridMember
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
		protected int blocklocationX = 0;
		protected int blocklocationY = 0;
		protected LogicBlock()
		{
			super.UIabsSizeX = 30;
			super.UIabsSizeY = 30;
			super.sizeUpdate();
		}
		abstract void updateState();
		@Override
		void put(int absX, int absY)
		{
			System.out.println("mouseABS: " + absX + " " + absY);
			absX = absX < 0 ? absX - (Size.REGULAR_SIZE) : absX;
			absY = absY < 0 ? absY - (Size.REGULAR_SIZE) : absY;
			absX = absX / Size.REGULAR_SIZE;
			absY = absY / Size.REGULAR_SIZE;
			this.blocklocationX = absX;
			this.blocklocationY = absY;
			System.out.println("BLOCK: " + blocklocationX + " " + blocklocationY);
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
	}
	public class AND extends LogicBlock
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
}
class SizeExt
{//그리드 넓이 확장 방향을 결정하는 상수 클래스
	public static final SizeExt EAST = new SizeExt();
	public static final SizeExt NORTH = new SizeExt();
	public static final SizeExt WEST = new SizeExt();
	public static final SizeExt SOUTH = new SizeExt();
}
class Size
{
	public static final int REGULAR_SIZE = 30;
	public static final int MARGIN = 50;
	public static final Size small = new Size(1);
	public static final Size middle = new Size(2);
	public static final Size big = new Size(4);
	private int multiple;
	private Size(int multiple)
	{
		this.multiple = multiple;
	}
	public int getmultiple()
	{
		return multiple;
	}
	public int getWidth()
	{
		return REGULAR_SIZE * multiple;
	}
}
