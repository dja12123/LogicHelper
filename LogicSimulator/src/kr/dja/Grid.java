package kr.dja;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
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
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import kr.dja.UI.ControlPane;
import kr.dja.UI.UnderBar;

public class Grid
{//패키지로 분리 필요
	private UI logicUI;
	private Size UI_Size;
	private JScrollPane gridScrollPane;
	private ViewPort viewPort;
	private GridPanel gridPanel;
	private int gridSizeX = 30;
	private int gridSizeY = 30;
	private int negativeExtendX = 15;
	private int negativeExtendY = 15;
	private final int MAX_SIZE = 100;
	private final int MAX_ABSOLUTE = 150;
	 
	private RulerPanel horizonRulerScrollPane;
	private RulerPanel verticalRulerScrollPane;
	private JPanel side;
	
	Grid(UI ui)
 	{
		this.logicUI = ui;
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
		logicUI.getUnderBar().setGridSizeInfo(gridSizeX, gridSizeY);
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
		
		private Selector selecter;

		
		ViewPort()
		{
			this.layeredPane = new JLayeredPane();
			
			eastExpansionPane = new ExpansionPane(SizeExt.EAST)
			{
				private static final long serialVersionUID = 1L;
				{
					this.setSize(Size.MARGIN - 6, Size.MARGIN * 2);
					super.setLayout(new GridLayout(4, 1, 0, 2));
					
				}
			};
			westExpansionPane = new ExpansionPane(SizeExt.WEST)
			{
				private static final long serialVersionUID = 1L;
				{
					this.setSize(Size.MARGIN - 6, Size.MARGIN * 2);
					super.setLayout(new GridLayout(4, 1, 0, 2));
				}
			};
			
			southExpansionPane = new ExpansionPane(SizeExt.SOUTH)
			{
				private static final long serialVersionUID = 1L;
				{
					this.setSize(Size.MARGIN * 2, Size.MARGIN - 6);
					super.setLayout(new GridLayout(1, 4, 2, 0));
				}
			};
			
			northExpansionPane = new ExpansionPane(SizeExt.NORTH)
			{
				private static final long serialVersionUID = 1L;
				{
					this.setSize(Size.MARGIN * 2, Size.MARGIN - 6);
					super.setLayout(new GridLayout(1, 4, 2, 0));
				}
			};
			
			this.layeredPane.add(eastExpansionPane, new Integer(1));
			this.layeredPane.add(westExpansionPane, new Integer(1));
			this.layeredPane.add(southExpansionPane, new Integer(1));
			this.layeredPane.add(northExpansionPane, new Integer(1));
			

			
			this.addMouseListener(new MouseAdapter()
			{
				@Override
				public void mousePressed(MouseEvent e)
				{
					removeSelecter();
					if(e.getButton() == 1)
					{
						selecter = new Selector(50, 100, 255, 80, e.getX(), e.getY(), (int)getViewPosition().getX(), (int)getViewPosition().getY(), "개의 블록 추가 선택")
						{
							private static final long serialVersionUID = 1L;
							
							@Override
							void selectAction(GridMember member)
							{
								System.out.println(isSelect(member));
								if(!isSelect(member) || isFocusSelect(member))
								{											
									if(!super.selectMember.contains(member))
									{
										System.out.println("추가");
										super.selectMember.add(member);
									}
								}
							}
							@Override
							void actionFinal()
							{
								select(super.selectMember);
							}
						};
						
					}
					else if(e.getButton() == 3)
					{
						selecter = new Selector(255, 100, 50, 80, e.getX(), e.getY(), (int)getViewPosition().getX(), (int)getViewPosition().getY(), "개의 블록 선택 해제")
						{
							private static final long serialVersionUID = 1L;

							@Override
							void selectAction(GridMember member)
							{
								System.out.println(isSelect(member));
								if(isSelect(member))
								{											
									if(!super.selectMember.contains(member))
									{
										System.out.println("추가");
										super.selectMember.add(member);
									}
								}
							}
							@Override
							void actionFinal()
							{
								deSelect(super.selectMember);
							}
						
						};
					}
					
				}
				@Override
				public void mouseReleased(MouseEvent e)
				{
					removeSelecter();
				}
				@Override
				public void mouseClicked(MouseEvent e)
				{
					if(e.getButton() == 1)
					{
						for(GridMember member : gridPanel.getMembers())
						{
							if((member.getGridViewPane().getX() < e.getX() + (int)getViewPosition().getX() && member.getGridViewPane().getX() + member.getGridViewPane().getWidth() > e.getX() + (int)getViewPosition().getX())
							&& (member.getGridViewPane().getY() < e.getY() + (int)getViewPosition().getY() && member.getGridViewPane().getY() + member.getGridViewPane().getHeight() > e.getY() + (int)getViewPosition().getY()))
							{
								selectFocus(member);
							}
						}
					}
					else if(e.getButton() == 3)
					{
						for(GridMember member : gridPanel.getMembers())
						{
							if((member.getGridViewPane().getX() < e.getX() + (int)getViewPosition().getX() && member.getGridViewPane().getX() + member.getGridViewPane().getWidth() > e.getX() + (int)getViewPosition().getX())
							&& (member.getGridViewPane().getY() < e.getY() + (int)getViewPosition().getY() && member.getGridViewPane().getY() + member.getGridViewPane().getHeight() > e.getY() + (int)getViewPosition().getY()))
							{
								ArrayList<GridMember> temp = new ArrayList<GridMember>();
								temp.add(member);
								deSelect(temp);
							}
						}
					}
				}
			});
			this.addMouseMotionListener(new MouseAdapter()
			{
				@Override
				public void mouseDragged(MouseEvent e)
				{
					if(selecter != null)
					{
						selecter.action(e.getX(), e.getY(), (int)getViewPosition().getX(), (int)getViewPosition().getY());
					}
					
				}
			});
			this.addChangeListener(new ChangeListener(){

				@Override
				public void stateChanged(ChangeEvent arg0)
				{
					if(selecter != null)
					{
						selecter.action((int)getViewPosition().getX(), (int)getViewPosition().getY());
					}

				}
				
			});
			super.setView(layeredPane);
		}
		void removeSelecter()
		{
			if(selecter != null)
			{
				selecter.actionFinal();
				layeredPane.remove(selecter);
				selecter = null;
				layeredPane.repaint();
			}
		}
		@Override
		public void setView(Component p)
		{
			this.dftComponent = p;
			this.layeredPane.add(this.dftComponent, new Integer(0));
			this.sizeUpdate();
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
		private abstract class Selector extends JPanel
		{
			private static final long serialVersionUID = 1L;
			
			private int startX, startY, startViewX, startViewY;
			private int mouseX, mouseY;
			protected ArrayList<GridMember> selectMember;
			private SelectControlPanel selectControlPanel;
			private String text;

			Selector(int r, int g, int b, int a, int startX, int startY, int startViewX, int startViewY, String text)
			{
				this.startX = startX;
				this.startY = startY;
				this.mouseX = startX;
				this.mouseY = startY;
				this.startViewX = startViewX;
				this.startViewY = startViewY;
				this.text = text;
				this.setBackground(new Color(r, g, b, a));
				this.setSize(0, 0);
				this.action(startX, startY, startViewX, startViewY);
				layeredPane.add(this, new Integer(2));
			}
			private void action(int mouseX, int mouseY, int viewX, int viewY)
			{
				this.mouseX = mouseX;
				this.mouseY = mouseY;
				this.setSize(Math.abs(this.mouseX - this.startX + ((int)getViewPosition().getX() - startViewX)), 
					    Math.abs(this.mouseY - this.startY + ((int)getViewPosition().getY() - startViewY)));
				this.setLocation((this.mouseX - this.startX + ((int)getViewPosition().getX() - startViewX)) > 0 ? this.getX() : (int)this.startX + startViewX - Math.abs(this.mouseX - this.startX + ((int)getViewPosition().getX() - startViewX)), 
						    (this.mouseY - this.startY + ((int)getViewPosition().getY() - startViewY)) > 0 ? this.getY() : (int)this.startY + startViewY - Math.abs(this.mouseY - this.startY + ((int)getViewPosition().getY() - startViewY)));
				this.repaint();
				layeredPane.repaint();
				this.selectMember = new ArrayList<GridMember>();
				for(GridMember member : gridPanel.getMembers())
				{
					if((this.getX() < member.getGridViewPane().getX() && this.getX() + this.getWidth() > member.getGridViewPane().getX() + member.getGridViewPane().getWidth())
					 && this.getY() < member.getGridViewPane().getY() && this.getY() + this.getHeight() > member.getGridViewPane().getY() + member.getGridViewPane().getHeight())
					{
						selectAction(member);
					}
					else
					{
						if(this.selectMember.contains(member))
						{
							this.selectMember.remove(member);
						}
					}
				}
				if(this.selectMember.size() > 0 && this.selectControlPanel == null)
				{
					this.selectControlPanel = new SelectControlPanel(text, logicUI.getControlPane().getBlockControlPanel());
				}
				if(this.selectControlPanel != null)
				{
					this.selectControlPanel.setNumber(this.selectMember.size());
				}
				selectSign(this.selectMember);
			}
			void action(int viewX, int viewY)
			{
				this.action(mouseX, mouseY, viewX, viewY);
			}
			abstract void selectAction(GridMember member);
			abstract void actionFinal();
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
		private ArrayList<GridMember> members = new ArrayList<GridMember>();
		private HashMap<Integer, HashMap<Integer, LogicBlock>> logicMembers = new HashMap<Integer, HashMap<Integer, LogicBlock>>();
		private static final long serialVersionUID = 1L;
		GridPanel()
		{
			this.setLayout(null);
			this.setBackground(new Color(200, 200, 200));
			this.sizeUpdate();
		}
		void addMember(GridMember member)
		{
			if(member instanceof LogicBlock)
			{
				LogicBlock block = ((LogicBlock) member);
				if(logicMembers.containsKey(new Integer(block.getBlockLocationX())) && logicMembers.get(new Integer(block.getBlockLocationX())).containsKey(block.getBlockLocationY()))
				{
					this.removeMember(logicMembers.get(new Integer(block.getBlockLocationX())).get(block.getBlockLocationY()));
				}
				if(!logicMembers.containsKey(new Integer(block.getBlockLocationX())))
				{
					logicMembers.put(new Integer(block.getBlockLocationX()), new HashMap<Integer, LogicBlock>());
				}
				logicMembers.get(new Integer(block.getBlockLocationX())).put(new Integer(block.getBlockLocationY()), block);
			}
			this.members.add(member);
			this.add(member.getGridViewPane());
		}
		void removeMember(GridMember member)
		{
			if(member instanceof LogicBlock)
			{
				LogicBlock block = ((LogicBlock) member);
				logicMembers.get(new Integer(block.getBlockLocationX())).remove(new Integer(block.getBlockLocationY()), block);
				if(logicMembers.get(new Integer(block.getBlockLocationX())).size() == 0)
				{
					logicMembers.remove(new Integer(block.getBlockLocationX()));
				}
			}
			this.members.remove(member);
			this.remove(member.getGridViewPane());
		}
		ArrayList<GridMember> getMembers()
		{
			return this.members;
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
			List<GridMember> tempMembers = new ArrayList<GridMember>(); //ConcurrentModificationException방지용
			this.setSize((gridSizeX * UI_Size.getWidth()) + (Size.MARGIN * 2), (gridSizeY * UI_Size.getWidth()) + (Size.MARGIN * 2));
			for(GridMember member : members)
			{
				member.sizeUpdate();
				tempMembers.add(member);
			}
			this.repaint();
			for(GridMember member : tempMembers)
			{
				if((this.getWidth() - Size.MARGIN < member.getGridViewPane().getX() + member.getGridViewPane().getWidth() || Size.MARGIN > member.getGridViewPane().getX()) || (this.getHeight() - Size.MARGIN < member.getGridViewPane().getY() + member.getGridViewPane().getHeight() || Size.MARGIN > member.getGridViewPane().getY()))
				{
					this.removeMember(member);
				}
			}
		}
	}

	private GridMember selectFocusMember;
	private ArrayList<GridMember> selectMembers = new ArrayList<GridMember>();
	private ArrayList<GridMember> selectSignMembers = new ArrayList<GridMember>();
	private int[] selectSignColor = new int[]{0, 0, 0, 0, 255, 255, 30};
	private int[] selectColor = new int[]{50, 100, 255, 60, 100, 150, 255};
	private int[] selectFocusColor = new int[]{50, 255, 100, 60, 50, 255, 100};
	void selectSign(ArrayList<GridMember> selectSignMembers)
	{
		deSelectSign();
		for(GridMember member : selectSignMembers)
		{
			member.setSelectView(this.selectSignColor);
			this.selectSignMembers.add(member);
		}
	}
	void deSelectSign()
	{
		for(GridMember member : this.selectSignMembers)
		{
			member.removeSelectView();
			if(this.selectMembers.contains(member))
			{
				member.setSelectView(this.selectColor);
			}
			if(this.selectFocusMember == member)
			{
				member.setSelectView(this.selectFocusColor);
			}
		}
		this.selectSignMembers = new ArrayList<GridMember>();
	}
	void select(ArrayList<GridMember> selectMembers)
	{
		this.deSelectSign();
		for(GridMember member : selectMembers)
		{
			this.deSelectFocus();
			member.setSelectView(this.selectColor);
			this.selectMembers.add(member);
		}
		if(this.selectMembers.size() == 1)
		{
			this.selectFocus(this.selectMembers.get(0));
		}
		else if(this.selectMembers.size() > 0)
		{
			new ManySelectEditPanel(this.selectMembers, logicUI.getControlPane().getBlockControlPanel());
		}
		else
		{
			this.logicUI.getControlPane().getBlockControlPanel().removeControlPane();
		}
		
	}
	void deSelect(ArrayList<GridMember> deSelectMembers)
	{
		for(GridMember member : deSelectMembers)
		{
			if(this.selectMembers.contains(member))
			{
				member.removeSelectView();
				this.selectMembers.remove(member);
			}
			if(deSelectMembers.contains(this.selectFocusMember))
			{
				this.deSelectFocus();
			}
			if(this.selectSignMembers.contains(member))
			{
				member.setSelectView(this.selectSignColor);
			}
			member.removeSelectView();
		}
		if(this.selectMembers.size() > 0)
		{
			new ManySelectEditPanel(this.selectMembers, logicUI.getControlPane().getBlockControlPanel());
		}
		else
		{
			this.logicUI.getControlPane().getBlockControlPanel().removeControlPane();
		}
	}
	boolean isSelect(GridMember member)
	{
		if((this.selectMembers.contains(member)) || (this.selectFocusMember == member))
		{
			return true;
		}
		return false;	
	}
	boolean isFocusSelect(GridMember member)
	{
		if(this.selectFocusMember == member)
		{
			return true;
		}
		return false;
	}
	void selectFocus(GridMember member)
	{
		this.deSelect(gridPanel.getMembers());
		this.selectFocusMember = member;
		member.setSelectView(this.selectFocusColor);
	}
	void deSelectFocus()
	{
		if(this.selectFocusMember != null)
		{
			this.selectFocusMember.removeSelectView();
			this.selectFocusMember = null;
		}
	}
	
	abstract class GridMember implements SizeUpdate
	{
		protected int UIabslocationX = 0;
		protected int UIabslocationY = 0;
		protected int UIabsSizeX = 1;
		protected int UIabsSizeY = 1;
		private JLayeredPane layeredPane;
		protected GridViewPane gridViewPane;
		
		private SelectShowPanel selectView = null;
		
		protected GridMember()
		{
			this.gridViewPane = new GridViewPane();
			this.gridViewPane.setBackground(new Color(100, 100, 100));
			this.gridViewPane.setLayout(null);
			this.layeredPane = new JLayeredPane();
			this.layeredPane.add(gridViewPane, new Integer(0));
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
		@Override
		public void sizeUpdate()
		{
			this.gridViewPane.sizeUpdate();
			this.layeredPane.setBounds(((UIabslocationX + (negativeExtendX * Size.REGULAR_SIZE)) * UI_Size.getmultiple()) + Size.MARGIN, ((UIabslocationY + (negativeExtendY * Size.REGULAR_SIZE)) * UI_Size.getmultiple()) + Size.MARGIN, UIabsSizeX * UI_Size.getmultiple(), UIabsSizeY * UI_Size.getmultiple());
			if(this.selectView != null)
			{
				this.selectView.sizeUpdate();
			}
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
		JLayeredPane getGridViewPane()
		{
			return this.layeredPane;
		}
		class SelectShowPanel extends JPanel implements SizeUpdate
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
				this.setBounds(UI_Size.getmultiple(), UI_Size.getmultiple(), UIabsSizeX * UI_Size.getmultiple() - (UI_Size.getmultiple() * 2), UIabsSizeY * UI_Size.getmultiple() - (UI_Size.getmultiple() * 2));
			}
			@Override
			public void paint(Graphics g)
			{
				g.setColor(new Color(this.rs, this.gs, this.bs));
				g.drawRect(0, 0, this.getWidth() - 1, this.getHeight() - 1);
				super.paint(g);
			}
		}
		class GridViewPane extends JPanel implements SizeUpdate
		{
			private static final long serialVersionUID = 1L;
			GridViewPane()
			{
				this.sizeUpdate();
			}
			@Override
			public void paint(Graphics g)
			{
				super.paint(g);
			}
			@Override
			public void sizeUpdate()
			{
				this.setBounds(0, 0, UIabsSizeX * UI_Size.getmultiple(), UIabsSizeY * UI_Size.getmultiple());
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
			absX = absX < 0 ? absX - (Size.REGULAR_SIZE) : absX;
			absY = absY < 0 ? absY - (Size.REGULAR_SIZE) : absY;
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
	}
	public class AND extends LogicBlock
	{
		AND()
		{
			JButton b = new JButton("AND");
			b.setBounds(1, 15, 58, 30);
			super.gridViewPane.add(b);
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
