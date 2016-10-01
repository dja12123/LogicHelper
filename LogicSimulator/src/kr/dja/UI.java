package kr.dja;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.UUID;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JViewport;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.metal.MetalFileChooserUI;

public class UI
{
	private LogicCore core;
	
	private Size UI_Size;
	
	private FileSavePanel fileSavePanel;
	private FileLoadPanel fileLoadPanel;
	private GridArea gridArea;
	private ToolBar toolBar;
	private UnderBar underBar;
	private TaskOperatorPanel taskOperatorPanel;
	private PalettePanel palettePanel;
	//private InfoPanel infoPanel;
	private BlockControlPanel blockControlPanel;
	private TemplatePanel templatePanel;
	private TaskManagerPanel taskManagerPanel;
	
	private TrackedPane trackedPane;
	
	private JFrame mainFrame;
	private JLayeredPane mainLayeredPane;
	private JPanel glassPane;
	
	private JPanel controlView;
	private JPanel staticControlView;
	private JPanel moveControlView;
	
	final Color innerUIObjectColor = new Color(220, 220, 220);
	
	UI(LogicCore core)
	{
		this.core = core;
		
		this.UI_Size = Size.MIDDLE;
		
		this.fileSavePanel = new FileSavePanel(this.core);
		this.fileLoadPanel = new FileLoadPanel(this.core);
		this.toolBar = new ToolBar(this.core);
		this.underBar = new UnderBar();
		
		this.mainFrame = new JFrame(LogicCore.getResource().getLocal("TITLE") + " v" + LogicCore.VERSION);
		this.mainFrame.setSize(1600, 900);
		this.mainFrame.setMinimumSize(new Dimension(1131, 800));
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		this.mainFrame.setLocation(screenSize.width/2-mainFrame.getSize().width/2, screenSize.height/2-mainFrame.getSize().height/2);
		this.mainLayeredPane = this.mainFrame.getLayeredPane();
		this.mainFrame.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				LogicCore.putConsole("Instance Close");
				LogicCore.removeInstance(core);
			}
		});
		this.mainFrame.addComponentListener(new ComponentAdapter() 
		{
			public void componentResized(ComponentEvent e)
			{
				toolBar.setTabLocation();
			}
		});
		this.gridArea = new GridArea(this);
		this.taskOperatorPanel = new TaskOperatorPanel();
		this.palettePanel = new PalettePanel(this);
		//this.infoPanel = new InfoPanel();
		this.blockControlPanel = new BlockControlPanel();
		this.templatePanel = new TemplatePanel();
		this.taskManagerPanel = new TaskManagerPanel();
		
		this.controlView = new JPanel();
		this.controlView.setPreferredSize(new Dimension(400, 0));
		this.controlView.setLayout(new BorderLayout());
		this.staticControlView = new JPanel();
		this.staticControlView.setPreferredSize(new Dimension(0, 335));
		this.staticControlView.setLayout(null);
		this.moveControlView = new JPanel();
		this.moveControlView.setLayout(new BoxLayout(this.moveControlView, BoxLayout.PAGE_AXIS));
		this.moveControlView.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
		
		this.glassPane = (JPanel)this.mainFrame.getGlassPane();
		this.glassPane.addMouseMotionListener(new MouseAdapter()
		{
			@Override
			public void mouseMoved(MouseEvent e)
			{
				trackedPane.setLocation(e.getX() - (trackedPane.getWidth() / 2), e.getY() - (trackedPane.getHeight() / 2));
			}
		});
		this.glassPane.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				if(e.getButton() == 3)
				{
					removeTrackedPane(trackedPane);
				}
				else if(e.getButton() == 1)
				{
					Grid grid = getGridArea().getGrid();
					if(grid != null)
					{
						Component gridPanel = getGridArea().getGrid().getGridPanel();
						Component gridView = getGridArea().getViewPort();
						Point clickOnGrid = SwingUtilities.convertPoint(glassPane, e.getPoint(), gridPanel);
						Point clickOnViewPort = SwingUtilities.convertPoint(glassPane, e.getPoint(), gridView);
						
						int x = clickOnGrid.x;
						int y = clickOnGrid.y;
						int vX = clickOnViewPort.x;
						int vY = clickOnViewPort.y;
						if(vX > 0 && vY > 0 && vX < gridView.getWidth() && vY < gridView.getHeight())
						if(x > Size.MARGIN && y > Size.MARGIN && x < gridPanel.getWidth() - Size.MARGIN && y < gridPanel.getHeight() - Size.MARGIN)
						{
							trackedPane.addMemberOnGrid(x - Size.MARGIN, y - Size.MARGIN);
							removeTrackedPane(trackedPane);
						}
					}
				}
			}
		});
		this.staticControlView.add(this.blockControlPanel.getComponent());
		//this.staticControlView.add(this.infoPanel.getComponent());
		this.staticControlView.add(this.palettePanel.getComponent());
		this.staticControlView.add(this.taskOperatorPanel.getComponent());
		
		this.moveControlView.add(this.taskManagerPanel.getComponent(), BorderLayout.CENTER);
		this.moveControlView.add(this.templatePanel.getComponent(), BorderLayout.SOUTH);
		
		this.controlView.add(this.moveControlView, BorderLayout.CENTER);
		this.controlView.add(this.staticControlView, BorderLayout.NORTH);
		
		this.mainFrame.add(this.gridArea.getComponent(), BorderLayout.CENTER);
		this.mainFrame.add(this.toolBar.getComponent(), BorderLayout.NORTH);
		this.mainFrame.add(this.underBar.getComponent(), BorderLayout.SOUTH);
		this.mainFrame.add(this.controlView, BorderLayout.EAST);
		
		this.mainFrame.setVisible(true);
	}
	JFrame getFrame()
	{
		return this.mainFrame;
	}
	FileSavePanel getFileSaver()
	{
		return this.fileSavePanel;
	}
	FileLoadPanel getFileLoader()
	{
		return this.fileLoadPanel;
	}
	GridArea getGridArea()
	{
		return this.gridArea;
	}
	TaskOperatorPanel getTaskOperatorPanel()
	{
		return this.taskOperatorPanel;
	}
	TaskManagerPanel getTaskManagerPanel()
	{
		return this.taskManagerPanel;
	}
	TemplatePanel getTemplatePanel()
	{
		return this.templatePanel;
	}
	void setUISize(Size size)
	{
		this.UI_Size = size;
		this.gridArea.sizeUpdate();
	}
	Size getUISize()
	{
		return this.UI_Size;
	}
	LogicCore getCore()
	{
		return this.core;
	}
	ToolBar getToolBar()
	{
		return this.toolBar;
	}
	UnderBar getUnderBar()
	{
		return this.underBar;
	}
	void addTrackedPane(TrackedPane trackedPane)
	{
		this.glassPane.setVisible(true);
		this.trackedPane = trackedPane;
		
		this.trackedPane.setLocation((int)this.glassPane.getMousePosition().getX() - (trackedPane.getWidth() / 2), (int)this.glassPane.getMousePosition().getY() - (trackedPane.getHeight() / 2));
		
		this.mainLayeredPane.add(trackedPane, new Integer(100));
	}
	void removeTrackedPane(TrackedPane trackedPane)
	{
		mainLayeredPane.remove(trackedPane);
		glassPane.setVisible(false);
	}
	BlockControlPanel getBlockControlPanel()
	{
		return this.blockControlPanel;
	}
	PalettePanel getPalettePanel()
	{
		return this.palettePanel;
	}
}
class GridArea implements LogicUIComponent, SizeUpdate
{
	private UI logicUI;
	
	private Grid grid;
	
	private JPanel masterPanel;
	
	private JScrollPane gridScrollPane;
	private ViewPort viewPort;
	private RulerPanel horizonRulerScrollPane;
	private RulerPanel verticalRulerScrollPane;
	private JPanel side;

	private JPanel dftPanel;

	private JLabel dftPanelLabel;

	private boolean showGridOption = true;
	
	GridArea(UI ui)
	{
		this.logicUI = ui;
		
		this.masterPanel = new JPanel(new BorderLayout());
		
		this.dftPanel = new JPanel(new BorderLayout());
		this.dftPanelLabel = new JLabel();
		this.dftPanelLabel.setFont(LogicCore.RES.NORMAL_FONT.deriveFont(32f));
		this.dftPanelLabel.setHorizontalAlignment(SwingConstants.CENTER);
		this.dftPanelLabel.setText(LogicCore.getResource().getLocal("SessionLost"));
		this.dftPanel.add(this.dftPanelLabel, BorderLayout.CENTER);
		this.dftPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
		
		this.gridScrollPane = new JScrollPane();
		this.gridScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		this.gridScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		this.gridScrollPane.getVerticalScrollBar().setUnitIncrement((int)(this.logicUI.getUISize().getWidth() / 2.5));
		this.gridScrollPane.getHorizontalScrollBar().setUnitIncrement((int)(this.logicUI.getUISize().getWidth() / 2.5));
		
		this.horizonRulerScrollPane = new RulerPanel()
		{
			private static final long serialVersionUID = 1L;
			@Override
			public void paintComponent(Graphics g)
			{
				super.paintComponent(g);
				if(grid != null)
				{
					g.setColor(super.lineColor);
					g.drawLine(logicUI.getUISize().getWidth() / 2 - 1, 0, logicUI.getUISize().getWidth() / 2 - 1, grid.getGridSize().getY() * logicUI.getUISize().getWidth() + (Size.MARGIN * 2));
					g.setColor(super.graduationColor);
					for(int i = 0; i <= grid.getGridSize().getY(); i++)
					{
						if((grid.getGridSize().getNY() - i) % 10 == 0 && i != grid.getGridSize().getY())
						{
							g.setColor(super.unitColor);
							g.fillRect(1,(i * logicUI.getUISize().getWidth()) + Size.MARGIN + 1 , (logicUI.getUISize().getWidth() / 2) - 2, logicUI.getUISize().getWidth() - 2);
							g.setColor(super.graduationColor);
						}
						g.fillRect(logicUI.getUISize().getWidth() / 4, (i * logicUI.getUISize().getWidth()) + Size.MARGIN - 1, (i * logicUI.getUISize().getWidth()) + Size.MARGIN, 2);
					}
				}
			}
			@Override
			public void sizeUpdate()
			{
				this.removeAll();
				this.setPreferredSize(new Dimension(logicUI.getUISize().getWidth() / 2, (grid.getGridSize().getY() * logicUI.getUISize().getWidth()) + (Size.MARGIN * 2)));
				for(int i = 0; i < grid.getGridSize().getY(); i++)
				{
					JLabel label = new JLabel(Integer.toString(i - grid.getGridSize().getNY()), SwingConstants.CENTER)
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
					label.setFont(LogicCore.RES.BAR_FONT.deriveFont((float)(logicUI.getUISize().getWidth() / 3.5)));
					label.setBounds(0, (logicUI.getUISize().getWidth() * i) + Size.MARGIN, logicUI.getUISize().getWidth(), logicUI.getUISize().getWidth());
					this.add(label);
					this.repaint();
				}
			}
		};
		this.verticalRulerScrollPane = new RulerPanel()
		{
			private static final long serialVersionUID = 1L;
			@Override
			public void paintComponent(Graphics g)
			{
				super.paintComponent(g);
				if(grid != null)
				{
					g.setColor(super.lineColor);
					g.drawLine(0, logicUI.getUISize().getWidth() / 2 - 1, grid.getGridSize().getX() * logicUI.getUISize().getWidth() + (Size.MARGIN * 2), logicUI.getUISize().getWidth() / 2 - 1);
					g.setColor(super.graduationColor);
					for(int i = 0; i <= grid.getGridSize().getX(); i++)
					{
						if((grid.getGridSize().getNX() - i) % 10 == 0 && i != grid.getGridSize().getX())
						{
							g.setColor(super.unitColor);
							g.fillRect((i * logicUI.getUISize().getWidth()) + Size.MARGIN + 1, 1, logicUI.getUISize().getWidth() - 2, (logicUI.getUISize().getWidth() / 2) - 2);
							g.setColor(super.graduationColor);
						}
						g.fillRect((i * logicUI.getUISize().getWidth()) + Size.MARGIN - 1, logicUI.getUISize().getWidth() / 4, 2, logicUI.getUISize().getWidth() / 2);
					}
				}
			}
			@Override
			public void sizeUpdate()
			{
				this.removeAll();
				this.setPreferredSize(new Dimension((grid.getGridSize().getX() * logicUI.getUISize().getWidth()) + (Size.MARGIN * 2), logicUI.getUISize().getWidth() / 2));
				for(int i = 0; i < grid.getGridSize().getX(); i++)
				{
					JLabel label = new JLabel(Integer.toString(i - grid.getGridSize().getNX()), SwingConstants.CENTER);
					label.setFont(LogicCore.RES.BAR_FONT.deriveFont((float)(logicUI.getUISize().getWidth() / 3.5)));
					label.setBounds((logicUI.getUISize().getWidth() * i) + Size.MARGIN, 0, logicUI.getUISize().getWidth(), logicUI.getUISize().getWidth() / 2);
					this.add(label);
					this.repaint();
				}
			}
		};
		this.side = new JPanel();

		this.viewPort = new ViewPort();
		
		this.gridScrollPane.setViewport(viewPort);
		
		this.gridScrollPane.setRowHeaderView(this.horizonRulerScrollPane);
		this.gridScrollPane.setColumnHeaderView(this.verticalRulerScrollPane);

		this.gridScrollPane.setCorner(JScrollPane.LOWER_LEFT_CORNER, this.side);
		this.gridScrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, this.side);
		this.gridScrollPane.setCorner(JScrollPane.UPPER_RIGHT_CORNER, this.side);
		
		this.masterPanel.add(this.gridScrollPane, BorderLayout.CENTER);
	}
	void setGrid(Grid grid)
	{
		if(this.grid != null)
		{
			this.grid.setViewLoc(this.getViewPosition());
		}
		if(grid != null)
		{
			this.grid = grid;
			this.viewPort.setView(this.grid.getGridPanel());
			this.logicUI.getUnderBar().setGridSizeInfo(this.grid.getGridSize());
			this.sizeUpdate();
			if(!this.showGridOption)
			{
				this.masterPanel.remove(this.dftPanel);
				this.masterPanel.add(this.gridScrollPane, BorderLayout.CENTER);
				this.gridScrollPane.updateUI();
			}
			if(this.grid.getViewLoc() != null)
			{
				this.setViewPosition(this.grid.getViewLoc());
			}
			else
			{
				this.setViewPosition(new Point(0, 0));
			}
			this.showGridOption = true;
		}
		else
		{
			this.grid = null;
			if(this.showGridOption)
			{
				this.masterPanel.remove(this.gridScrollPane);
				this.masterPanel.add(this.dftPanel, BorderLayout.CENTER);
				this.dftPanel.updateUI();
			}
			this.showGridOption = false;
		}
	}
	ViewPort getViewPort()
	{
		return this.viewPort;
	}
	Grid getGrid()
	{
		return this.grid;
	}
	Point getViewPosition()
	{
		return this.viewPort.getViewPosition();
	}
	void setViewPosition(Point p)
	{
		this.viewPort.setViewPosition(p);
	}
	@Override
	public Component getComponent()
	{
		return this.masterPanel;
	}
	JLayeredPane getLayeredPane()
	{
		return this.viewPort.layeredPane;
	}
	private abstract class RulerPanel extends JPanel implements SizeUpdate
	{
		private static final long serialVersionUID = 1L;
		
		protected Color graduationColor = new Color(140, 150, 190);
		protected Color unitColor = new Color(180, 200, 230);
		protected Color lineColor = new Color(122, 138, 153);
		
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
			
			this.eastExpansionPane = new ExpansionPane(Direction.EAST);
			this.westExpansionPane = new ExpansionPane(Direction.WEST);
			this.southExpansionPane = new ExpansionPane(Direction.SOUTH);
			this.northExpansionPane = new ExpansionPane(Direction.NORTH);
			
			this.layeredPane.add(eastExpansionPane, new Integer(3));
			this.layeredPane.add(westExpansionPane, new Integer(3));
			this.layeredPane.add(southExpansionPane, new Integer(3));
			this.layeredPane.add(northExpansionPane, new Integer(3));
			
			this.addMouseListener(new MouseAdapter()
			{
				@Override
				public void mousePressed(MouseEvent e)
				{
					removeSelecter();
					if(e.getButton() == 1)
					{
						selecter = new Selector(120, 180, 255, 50, e.getX(), e.getY(), (int)getViewPosition().getX(), (int)getViewPosition().getY(), "SelecterSelect")
						{
							private static final long serialVersionUID = 1L;
							
							@Override
							void selectAction(GridMember member)
							{
								if(!grid.isSelect(member) || grid.isFocusSelect(member))
								{											
									if(!super.selectMember.contains(member))
									{
										super.selectMember.add(member);
									}
								}
							}
							@Override
							void actionFinal()
							{
								if(super.selectMember.size() > 0)
								{
									grid.select(super.selectMember);
								}
								else if(grid.getFocusSelectMember() != null)
								{
									EditPane editer = logicUI.getPalettePanel().getControl(grid.getFocusSelectMember());
									logicUI.getBlockControlPanel().addControlPanel(editer);
								}
								else if(grid.getSelectMembers().size() <= 0)
								{
									logicUI.getBlockControlPanel().removeControlPane();
								}
							}
						};
					}
					else if(e.getButton() == 3)
					{
						selecter = new Selector(255, 180, 120, 50, e.getX(), e.getY(), (int)getViewPosition().getX(), (int)getViewPosition().getY(), "SelecterDeselect")
						{
							private static final long serialVersionUID = 1L;

							@Override
							void selectAction(GridMember member)
							{
								if(grid.isSelect(member))
								{											
									if(!super.selectMember.contains(member))
									{
										super.selectMember.add(member);
									}
								}
							}
							@Override
							void actionFinal()
							{
								grid.deSelect(super.selectMember);
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
						for(UUID memberID : grid.getMembers().keySet())
						{
							GridMember member = grid.getMembers().get(memberID);
							if((member.getGridViewPane().getX() < e.getX() + (int)getViewPosition().getX() && member.getGridViewPane().getX() + member.getGridViewPane().getWidth() > e.getX() + (int)getViewPosition().getX())
							&& (member.getGridViewPane().getY() < e.getY() + (int)getViewPosition().getY() && member.getGridViewPane().getY() + member.getGridViewPane().getHeight() > e.getY() + (int)getViewPosition().getY()))
							{
								grid.selectFocus(member);
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
			if(this.dftComponent != null)
			{
				this.layeredPane.remove(this.dftComponent);
			}
			this.dftComponent = p;
			this.layeredPane.add(this.dftComponent, new Integer(1));
			this.sizeUpdate();
		}
		@Override
		public void setViewPosition(Point p)
		{
			super.setViewPosition(p);
			if(grid != null)
			{
				eastExpansionPane.setLocation(dftComponent.getWidth() - eastExpansionPane.getWidth() - 3, (this.getHeight() / 2) + p.y - (eastExpansionPane.getHeight() / 2));
				westExpansionPane.setLocation(3, (this.getHeight() / 2) + p.y - (westExpansionPane.getHeight() / 2));
				southExpansionPane.setLocation((this.getWidth() / 2) + p.x - (southExpansionPane.getWidth() / 2), dftComponent.getHeight() - southExpansionPane.getHeight() - 3);
				northExpansionPane.setLocation((this.getWidth() / 2) + p.x - (southExpansionPane.getWidth() / 2), 3);
				if((this.getSize().width - grid.getGridPanel().getSize().width) > 0)
				{
					eastExpansionPane.setLocation(eastExpansionPane.getLocation().x + (this.getSize().width - grid.getGridPanel().getSize().width), eastExpansionPane.getLocation().y);
				}
				if((this.getSize().height - grid.getGridPanel().getSize().height) > 0)
				{
					southExpansionPane.setLocation(southExpansionPane.getLocation().x , southExpansionPane.getLocation().y + (this.getSize().height - grid.getGridPanel().getSize().height));
				}
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
			
			private int r, g, b, a;
			private int startX, startY, startViewX, startViewY;
			private int mouseX, mouseY;
			protected ArrayList<GridMember> selectMember;
			private SelectControlPanel selectControlPanel;
			private String text;

			Selector(int r, int g, int b, int a, int startX, int startY, int startViewX, int startViewY, String text)
			{
				this.r = r;
				this.g = g;
				this.b = b;
				this.a = a;
				this.startX = startX;
				this.startY = startY;
				this.mouseX = startX;
				this.mouseY = startY;
				this.startViewX = startViewX;
				this.startViewY = startViewY;
				this.text = text;
				this.setOpaque(false);
				//this.setBackground(new Color(r, g, b, a));
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
				//layeredPane.repaint();
				this.selectMember = new ArrayList<GridMember>();
				for(UUID memberID : grid.getMembers().keySet())
				{
					GridMember member = grid.getMembers().get(memberID);
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
					this.selectControlPanel = new SelectControlPanel(LogicCore.getResource().getLocal(this.text), logicUI.getBlockControlPanel());
				}
				if(this.selectControlPanel != null)
				{
					this.selectControlPanel.setNumber(this.selectMember.size());
				}
				grid.selectSign(this.selectMember);
			}
			void action(int viewX, int viewY)
			{
				this.action(mouseX, mouseY, viewX, viewY);
			}
			@Override
			public void paint(Graphics g)
			{
				g.setColor(new Color(this.r, this.g, this.b, this.a));
				g.fillRect(0, 0, this.getWidth(), this.getHeight());
				g.setColor(new Color(this.r, this.g, this.b));
				g.drawRect(0, 0, this.getWidth() - 1, this.getHeight() - 1);
				
				super.paint(g);
			}
			abstract void selectAction(GridMember member);
			abstract void actionFinal();
		}
		private class ExpansionPane extends JPanel
		{
			private static final long serialVersionUID = 1L;
			
			private final Direction ext;
			
			ExpansionPane(Direction ext)
			{
				super();
				this.ext = ext;
				this.setLayout(new BoxLayout(this, Math.abs(ext.getWayY())));
				JPanel inRedPanel = new JPanel();
				inRedPanel.setLayout(new BoxLayout(inRedPanel, Math.abs(ext.getWayX())));
				JPanel inExtPanel = new JPanel();
				inExtPanel.setLayout(new BoxLayout(inExtPanel, Math.abs(ext.getWayX())));
				this.setSize((Math.abs(ext.getWayY()) + 1) * 44, (Math.abs(ext.getWayX()) + 1) * 44);
				
				inExtPanel.add(new SizeEditButton("GRID_EXTEND", this.ext, 1));
				inExtPanel.add(new SizeEditButton("GRID_EXTEND_PLUS", this.ext, 10));
				
				inRedPanel.add(new SizeEditButton("GRID_EXTEND", this.ext.getAcross(), -1));
				inRedPanel.add(new SizeEditButton("GRID_EXTEND_PLUS", this.ext.getAcross(), -10));
				this.add(ext.getWayX() + ext.getWayY() >= 0 ? inRedPanel : inExtPanel);
				this.add(ext.getWayX() + ext.getWayY() >= 0 ? inExtPanel : inRedPanel);
			}
			private class SizeEditButton extends ButtonPanel
			{
				private static final long serialVersionUID = 1L;
				private final int editSize;
				private final Direction tagExt;
				
				SizeEditButton(String imgTag, Direction ext, int size)
				{
					this.editSize = size;
					this.tagExt = ext;
					super.setBasicImage(LogicCore.getResource().getImage(imgTag + "_" + tagExt.getTag()));
					super.setOnMouseImage(LogicCore.getResource().getImage(imgTag + "_SELECT_" + tagExt.getTag()));
					super.setBasicPressImage(LogicCore.getResource().getImage(imgTag + "_PUSH_" + tagExt.getTag()));
				}
				@Override
				void pressed(int mouse)
				{
					grid.getSession().getTaskManager().setTask().addCommand(new EditGridSize(grid, ext, editSize));
				}
			}
		}
	}
	public void sizeUpdate()
	{
		if(this.grid != null)
		{
			this.grid.getGridPanel().sizeUpdate();
			this.viewPort.sizeUpdate();
			this.horizonRulerScrollPane.sizeUpdate();
			this.verticalRulerScrollPane.sizeUpdate();
			this.gridScrollPane.revalidate();
		}
	}
}
class ToolBar implements LogicUIComponent
{
	//private LogicCore core;
	private JToolBar toolbar;
	private JPanel leftSidePanel;
	private JPanel toolBarPanel;
	private JPanel rightSidePanel;
	private JLayeredPane sessionTabPanel;
	private ButtonPanel saveButton;
	private ButtonPanel optionSaveButton;
	private ButtonPanel loadButton;
	private ButtonPanel sizeUpButton;
	private ButtonPanel sizeDownButton;
	private ButtonPanel setViewButton;
	private ButtonPanel consolButton;
	private ButtonPanel helpButton;
	private ButtonPanel newInstanceButton;
	private ButtonPanel settingButton;
	
	private ArrayList<ButtonPanel> sessionTabList;
	private ButtonPanel createSessionButton;
	private ButtonPanel focus;
	
	ToolBar(LogicCore core)
	{
		//this.core = core;
		
		this.toolbar = new JToolBar();
		
		this.toolbar.setPreferredSize(new Dimension(0, 30));
		this.toolbar.setFloatable(false);
		
		this.toolBarPanel = new JPanel();
		this.toolBarPanel.setLayout(new BorderLayout());
		this.toolBarPanel.setOpaque(false);
		
		this.rightSidePanel = new JPanel();
		this.rightSidePanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		this.rightSidePanel.setOpaque(false);
		this.rightSidePanel.setPreferredSize(new Dimension(110, 0));
		
		this.leftSidePanel = new JPanel();
		this.leftSidePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		this.leftSidePanel.setOpaque(false);
		this.leftSidePanel.setPreferredSize(new Dimension(230, 0));
		
		this.sessionTabPanel = new JLayeredPane();
		this.sessionTabPanel.setLayout(null);
		
		this.saveButton = new ButtonPanel(20, 20)
		{
			private static final long serialVersionUID = 1L;
			@Override
			void pressed(int button)
			{
				Session session = core.getSession().getFocusSession();
				File file = session.getFileLocation();
				if(file != null && file.canWrite())
				{
					session.saveData(file);
				}
				else
				{
					core.getUI().getFileSaver().active(session);
				}
			}
		};
		this.saveButton.setBasicImage(LogicCore.getResource().getImage("FILE_SAVE"));
		this.saveButton.setOnMouseImage(LogicCore.getResource().getImage("FILE_SAVE_SELECT"));
		this.saveButton.setBasicPressImage(LogicCore.getResource().getImage("FILE_SAVE_PUSH"));
		
		this.optionSaveButton = new ButtonPanel(20, 20)
		{
			private static final long serialVersionUID = 1L;
			@Override
			void pressed(int button)
			{
				core.getUI().getFileSaver().active(core.getSession().getFocusSession());
			}
		};
		this.optionSaveButton.setBasicImage(LogicCore.getResource().getImage("FILE_SAVE_EXT"));
		this.optionSaveButton.setOnMouseImage(LogicCore.getResource().getImage("FILE_SAVE_EXT_SELECT"));
		this.optionSaveButton.setBasicPressImage(LogicCore.getResource().getImage("FILE_SAVE_EXT_PUSH"));
		
		this.loadButton = new ButtonPanel(20, 20)
		{
			private static final long serialVersionUID = 1L;
			@Override
			void pressed(int button)
			{
				core.getUI().getFileLoader().active(core.getSession().getFocusSession());
			}
		};
		this.loadButton.setBasicImage(LogicCore.getResource().getImage("FILE_LOAD"));
		this.loadButton.setOnMouseImage(LogicCore.getResource().getImage("FILE_LOAD_SELECT"));
		this.loadButton.setBasicPressImage(LogicCore.getResource().getImage("FILE_LOAD_PUSH"));
		
		this.sizeUpButton = new ButtonPanel(20, 20)
		{
			private static final long serialVersionUID = 1L;
			@Override
			void pressed(int button)
			{
				Size changeSize = core.getUI().getUISize().getBigSize();
				if(changeSize != core.getUI().getUISize())
				{
					core.getUI().setUISize(changeSize);
				}
			}
		};
		this.sizeUpButton.setBasicImage(LogicCore.getResource().getImage("SIZE_UP"));
		this.sizeUpButton.setOnMouseImage(LogicCore.getResource().getImage("SIZE_UP_SELECT"));
		this.sizeUpButton.setBasicPressImage(LogicCore.getResource().getImage("SIZE_UP_PUSH"));
		
		this.sizeDownButton = new ButtonPanel(20, 20)
		{
			private static final long serialVersionUID = 1L;
			@Override
			void pressed(int button)
			{
				Size changeSize = core.getUI().getUISize().getSmallSize();
				if(changeSize != core.getUI().getUISize())
				{
					core.getUI().setUISize(changeSize);
				}
			}
		};
		this.sizeDownButton.setBasicImage(LogicCore.getResource().getImage("SIZE_DOWN"));
		this.sizeDownButton.setOnMouseImage(LogicCore.getResource().getImage("SIZE_DOWN_SELECT"));
		this.sizeDownButton.setBasicPressImage(LogicCore.getResource().getImage("SIZE_DOWN_PUSH"));
		
		this.setViewButton = new ButtonPanel(20, 20);
		this.setViewButton.setBasicImage(LogicCore.getResource().getImage("VIEW"));
		this.setViewButton.setOnMouseImage(LogicCore.getResource().getImage("VIEW_SELECT"));
		this.setViewButton.setBasicPressImage(LogicCore.getResource().getImage("VIEW_PUSH"));
		
		this.consolButton = new ButtonPanel(20, 20);
		
		this.helpButton = new ButtonPanel(20, 20);
		this.newInstanceButton = new ButtonPanel(20, 20)
		{
			private static final long serialVersionUID = 1L;
			@Override
			void pressed(int button)
			{
				LogicCore.createInstance();
			}
		};
		this.settingButton = new ButtonPanel(20, 20);
		
		this.leftSidePanel.add(this.saveButton);
		this.leftSidePanel.add(this.optionSaveButton);
		this.leftSidePanel.add(this.loadButton);
		this.leftSidePanel.add(Box.createRigidArea(new Dimension(10, 0)));
		this.leftSidePanel.add(this.sizeDownButton);
		this.leftSidePanel.add(this.sizeUpButton);
		this.leftSidePanel.add(Box.createRigidArea(new Dimension(10, 0)));
		this.leftSidePanel.add(this.setViewButton);
		
		this.rightSidePanel.add(this.helpButton);
		this.rightSidePanel.add(this.consolButton);
		this.rightSidePanel.add(this.settingButton);
		this.rightSidePanel.add(this.newInstanceButton);
		
		this.toolBarPanel.add(this.leftSidePanel, BorderLayout.WEST);
		this.toolBarPanel.add(this.sessionTabPanel, BorderLayout.CENTER);
		this.toolBarPanel.add(this.rightSidePanel, BorderLayout.EAST);
		
		this.sessionTabList = new ArrayList<ButtonPanel>();
		this.createSessionButton = new ButtonPanel()
		{
			private static final long serialVersionUID = 1L;
			@Override
			void pressed(int button)
			{
				core.getSession().createSession();
			}
		};
		this.createSessionButton.setSize(16, 16);
		this.createSessionButton.setBasicImage(LogicCore.getResource().getImage("SESSION_CREATE"));
		this.createSessionButton.setBasicPressImage(LogicCore.getResource().getImage("SESSION_CREATE_PUSH"));
		this.sessionTabPanel.add(this.createSessionButton);
		
		this.toolbar.add(toolBarPanel);
	}
	void addSessionTabPanel(ButtonPanel panel)
	{
		this.sessionTabList.add(panel);
		this.sessionTabPanel.add(panel, new Integer(1));
		this.setTabLocation();
	}
	void removeSessionTabPanel(ButtonPanel panel)
	{
		this.sessionTabList.remove(panel);
		this.sessionTabPanel.remove(panel);
		if(this.focus != null && this.focus == panel)
		{
			this.focus = null;
		}
		this.setTabLocation();
	}
	void setTabLocation()
	{
		int width = this.sessionTabPanel.getWidth() - 143 - (this.focus != null ? 120 : 0);
		
		int memberGab = 120 + 3;
		int totalWidth = (this.sessionTabList.size() * memberGab);
		if(width < totalWidth)
		{
			memberGab = (width) / this.sessionTabList.size();
		}
		int memberLocation = 0;
		for(int i = 0; i < this.sessionTabList.size(); i++)
		{
			ButtonPanel btn = this.sessionTabList.get(i);
			this.sessionTabPanel.setLayer(btn, new Integer(i));
			btn.setLocation(memberLocation, 5);
			if(btn == this.focus && this.focus != this.sessionTabList.get(this.sessionTabList.size() - 1))
			{
				memberLocation += 123 - memberGab;
			}
			memberLocation += memberGab;
		}
		memberLocation += 124 - memberGab;
		this.createSessionButton.setLocation(memberLocation, 7);
		this.toolbar.repaint();
	}
	void setFocus(ButtonPanel btn)
	{
		this.focus = btn;
		this.setTabLocation();
	}
	void setSaveButtonStatus(boolean option)
	{
		this.saveButton.setEnable(option);
		this.optionSaveButton.setEnable(option);
	}
	@Override
	public Component getComponent()
	{
		return this.toolbar;
	}
}
class UnderBar implements LogicUIComponent
{
	private JToolBar underbar;
	private JPanel leftSidePanel;
	private JPanel centerSidePanel;
	private JPanel rightSidePanel;
	private JLabel sizeLabel;
	
	UnderBar()
	{
		this.underbar = new JToolBar();
		
		this.underbar.setPreferredSize(new Dimension(0, 25));
		this.underbar.setFloatable(false);
		this.underbar.setLayout(new BorderLayout());
		
		this.leftSidePanel = new JPanel();
		this.leftSidePanel.setPreferredSize(new Dimension(300, 0));
		this.leftSidePanel.setOpaque(false);
		this.leftSidePanel.setLayout(null);
		
		this.centerSidePanel = new JPanel();
		this.centerSidePanel.setOpaque(false);
		
		this.rightSidePanel = new JPanel();
		this.rightSidePanel.setPreferredSize(new Dimension(300, 0));
		this.rightSidePanel.setOpaque(false);
		this.rightSidePanel.setLayout(null);
		
		this.sizeLabel = new JLabel();
		this.sizeLabel.setBounds(5, 0, 120, 22);
		this.sizeLabel.setFont(LogicCore.RES.BAR_FONT.deriveFont(14f));
		
		this.leftSidePanel.add(this.sizeLabel);
		
		this.underbar.add(centerSidePanel, BorderLayout.CENTER);
		this.underbar.add(leftSidePanel, BorderLayout.WEST);
		this.underbar.add(rightSidePanel, BorderLayout.EAST);
	}
	void setGridSizeInfo(SizeInfo info)
	{
		this.sizeLabel.setText("X: " + info.getX() + "  Y: " + info.getY());
		this.underbar.repaint();
	}
	@Override
	public Component getComponent()
	{
		return this.underbar;
	}
}
class TaskOperatorPanel implements LogicUIComponent
{
	private JPanel taskOperatorPanel;
	
	private JPanel graphPanel;
	private ButtonPanel pauseButton;
	private JLabel pauseButtonLabel;
	private TaskOperator operator;
	private TimeSelector timeSelector;
	
	TaskOperatorPanel()
	{
		this.taskOperatorPanel = new JPanel();
		
		this.taskOperatorPanel.setLayout(null);
		this.taskOperatorPanel.setBounds(230, 155, 165, 180);
		this.taskOperatorPanel.setBorder(new PanelBorder(LogicCore.getResource().getLocal("TaskOperatorPanel")));
		
		this.pauseButton = new ButtonPanel(8, 145, 148, 25)
		{
			private static final long serialVersionUID = 1L;

			@Override
			void pressed(int button)
			{
				operator.toggleTask();
			}
		};
		this.pauseButton.setLayout(null);
		this.pauseButtonLabel = new JLabel();
		this.pauseButtonLabel.setFont(LogicCore.RES.NORMAL_FONT.deriveFont(14.0f));
		this.pauseButtonLabel.setBounds(0, 0, 148, 25);
		this.pauseButtonLabel.setHorizontalAlignment(SwingConstants.CENTER);
		this.timeSelector = new TimeSelector(500, 10, 5000, "ms")
		{
			private static final long serialVersionUID = 1L;

			@Override
			void timerEvent(int time)
			{
				if(operator != null)
				{
					operator.setTaskTick(time);
				}
			}
		};
		this.timeSelector.setLocation(8, 120);
		this.pauseButton.add(this.pauseButtonLabel);
		
		this.taskOperatorPanel.add(this.timeSelector);
		this.taskOperatorPanel.add(this.pauseButton);


	}
	void setOperator(TaskOperator operator)
	{
		this.operator = operator;
		this.graphPanel = operator.getGraphPanel();
		this.graphPanel.setLocation(8, 20);
		this.graphPanel.setBorder(new EtchedBorder(EtchedBorder.RAISED));
		this.taskOperatorPanel.add(this.graphPanel);
		this.taskOperatorPanel.repaint();
		this.setPauseButtonStatus(false);
	}
	@Override
	public Component getComponent()
	{
		return this.taskOperatorPanel;
	}
	void setPauseButtonStatus(boolean status)
	{
		if(status)
		{
			this.pauseButtonLabel.setText(LogicCore.getResource().getLocal("TaskWork"));
			this.pauseButton.setBasicImage(LogicCore.getResource().getImage("OPERATOR_RUN"));
			this.pauseButton.setOnMouseImage(LogicCore.getResource().getImage("OPERATOR_RUN_SELECT"));
			this.pauseButton.setBasicPressImage(LogicCore.getResource().getImage("OPERATOR_RUN_PUSH"));
		}
		else
		{
			this.pauseButtonLabel.setText(LogicCore.getResource().getLocal("TaskPause"));
			this.pauseButton.setBasicImage(LogicCore.getResource().getImage("OPERATOR_PAUSE"));
			this.pauseButton.setOnMouseImage(LogicCore.getResource().getImage("OPERATOR_PAUSE_SELECT"));
			this.pauseButton.setBasicPressImage(LogicCore.getResource().getImage("OPERATOR_PAUSE_PUSH"));
		}
	}

}
class BlockControlPanel implements LogicUIComponent
{
	private JPanel blockControlPanel;
	private JPanel control;
	
	private DefaultPane defaultPane;

	BlockControlPanel()
	{
		this.blockControlPanel = new JPanel();
		
		this.defaultPane = new DefaultPane();
		this.blockControlPanel.setLayout(null);
		this.blockControlPanel.setBounds(5, 5, 390, 150);
		this.blockControlPanel.setBorder(new PanelBorder(LogicCore.getResource().getLocal("BlockControlPanel")));
		this.removeControlPane();
	}
	void addControlPanel(JPanel panel)
	{
		this.control = panel;
		this.blockControlPanel.removeAll();
		panel.setBounds(8, 20, 373, 121);
		this.blockControlPanel.add(panel);
		this.blockControlPanel.repaint();
	}
	void removeControlPane()
	{
		this.blockControlPanel.removeAll();
		this.addControlPanel(defaultPane);
	}
	void updateMemberStatus()
	{
		System.out.println("call");
		if(this.control != null && this.control instanceof EditPane)
		{
			((EditPane)this.control).updateMemberStatus();
		}
	}
	@Override
	public Component getComponent()
	{
		return this.blockControlPanel;
	}
}
/*class InfoPanel implements LogicUIComponent
{
	private JPanel infoPanel;

	private JTextArea explanationArea;
	
	InfoPanel()
	{//TODO 설명 기능 구현해야함
		this.infoPanel = new JPanel();
		
		this.infoPanel.setLayout(null);
		this.infoPanel.setBounds(5, 155, 390, 140);
		this.infoPanel.setBorder(new PanelBorder(LogicCore.getResource().getLocal("InfoPanel")));
		
		this.explanationArea = new JTextArea();
		this.explanationArea.setBounds(8, 20, 373, 111);
		this.explanationArea.setEditable(false);
		this.explanationArea.setFont(LogicCore.RES.NORMAL_FONT.deriveFont(14.0f));
		
		this.infoPanel.add(explanationArea);
	}
	public Component getComponent()
	{
		return this.infoPanel;
	}
}*/
class PalettePanel implements LogicUIComponent
{
	private UI logicUI;
	
	private JPanel palettePanel;

	private LinkedHashMap<String, PaletteMember> paletteMembers = new LinkedHashMap<String, PaletteMember>();
	
	PalettePanel(UI logicUI)
	{
		this.logicUI = logicUI;
		
		this.palettePanel = new JPanel();
		
		this.palettePanel.setLayout(null);
		this.palettePanel.setBounds(5, 155, 225, 180);
		this.palettePanel.setBorder(new PanelBorder(LogicCore.getResource().getLocal("PalettePanel")));
		LogicTREditPane DFTLogicControl = new LogicTREditPane();
		
		new PaletteMember(new Wire(this.logicUI.getCore()), new WireEditPane());
		new PaletteMember(new AND(this.logicUI.getCore()), DFTLogicControl);
		new PaletteMember(new OR(this.logicUI.getCore()), DFTLogicControl);
		new PaletteMember(new NOT(this.logicUI.getCore()), DFTLogicControl);
		new PaletteMember(new NAND(this.logicUI.getCore()), DFTLogicControl);
		new PaletteMember(new NOR(this.logicUI.getCore()), DFTLogicControl);
		new PaletteMember(new XOR(this.logicUI.getCore()), DFTLogicControl);
		new PaletteMember(new XNOR(this.logicUI.getCore()), DFTLogicControl);
		new PaletteMember(new Button(this.logicUI.getCore()), new ButtonEditPanel());
		new PaletteMember(new Tag(this.logicUI.getCore()), new TagEditPane());
		
		System.out.println(paletteMembers.keySet().size());
		int i = 0, j = 0;
		for(String str : paletteMembers.keySet())
		{
			PaletteMember p = this.paletteMembers.get(str);
			if(i < 3)
			{
				p.setBounds(j * 42 + 9, i * 42 + 20, 38, 38);
				if(j < 4)
				{
					
					j++;
				}
				else
				{
					j = 0;
					i++;
				}
			}
			this.palettePanel.add(p);
		}
	}
	@Override
	public Component getComponent()
	{
		return this.palettePanel;
	}
	EditPane getControl(GridMember member)
	{
		EditPane edit = this.paletteMembers.get(member.getName()).getControl();
		edit.setInfo(member);
		return edit;
	}
	class PaletteMember extends ButtonPanel
	{
		private static final long serialVersionUID = 1L;
		
		private GridMember putMember;
		private EditPane selectControlPanel;
		PaletteMember(GridMember member, EditPane editPane)
		{
			paletteMembers.put(member.getName(), this);
			this.selectControlPanel = editPane;
			this.putMember = member;
		}
		@Override
		void pressed(int button)
		{
			{
				if(button == 1)
				{
					ArrayList<GridMember> list = new ArrayList<GridMember>();
					list.add(GridMember.Factory(logicUI.getCore(), putMember.getData(new DataBranch("GridMember"))));
					logicUI.addTrackedPane(new TrackedPane(list, logicUI));
				}
				else if(button == 3)
				{
					selectControlPanel.setInfo(putMember);
					if(logicUI.getGridArea().getGrid() != null)
					{
						logicUI.getGridArea().getGrid().deSelectAll();
					}
					logicUI.getBlockControlPanel().addControlPanel(selectControlPanel);
				}
			}
		}
		EditPane getControl()
		{
			return this.selectControlPanel;
		}
	}
}
class TemplatePanel implements LogicUIComponent
{
	private JPanel templatePanel;
	
	private JPanel templateAreaPanel;
	private JPanel buttonAreaPanel;
	private JPanel clipBoardPanel;
	private JScrollPane templateScrollPane;
	private ButtonPanel addTemplateButton;
	private ButtonPanel removeTemplateButton;
	private ButtonPanel checkAllButton;

	private TemplateManager manager;

	private boolean showManagerOption = true;

	private JPanel dftPanel;

	private JLabel dftPanelLabel;

	TemplatePanel()
	{
		this.templatePanel = new JPanel();
		
		this.dftPanel = new JPanel(new BorderLayout());
		this.dftPanelLabel = new JLabel();
		this.dftPanelLabel.setFont(LogicCore.RES.NORMAL_FONT.deriveFont(18f));
		this.dftPanelLabel.setHorizontalAlignment(SwingConstants.CENTER);
		this.dftPanelLabel.setText(LogicCore.getResource().getLocal("SessionLost"));
		this.dftPanel.add(this.dftPanelLabel, BorderLayout.CENTER);
		this.dftPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
		
		this.templatePanel.setLayout(new BorderLayout());
		this.templatePanel.setBorder(new PanelBorder(LogicCore.getResource().getLocal("TemplatePanel")));
		
		this.templateAreaPanel = new JPanel();
		this.templateAreaPanel.setLayout(new BorderLayout());
		
		this.buttonAreaPanel = new JPanel();
		this.buttonAreaPanel.setPreferredSize(new Dimension(30, 0));
		this.buttonAreaPanel.setLayout(null);
		
		this.addTemplateButton = new ButtonPanel(2, 4, 26, 26)
		{
			private static final long serialVersionUID = 1L;
			@Override
			void pressed(int button)
			{
				manager.addTemplate();
			}
		};
		
		if(ClipBoardPanel.clipBoard == null)
		{
			this.addTemplateButton.setEnable(false);
		}
		this.removeTemplateButton = new ButtonPanel(2, 38, 26, 26)
		{
			private static final long serialVersionUID = 1L;

			@Override
			void pressed(int button)
			{
				if(manager != null)
				{
					manager.removeSelectTemplate();
				}
			}
		};
		this.checkAllButton = new ButtonPanel(2, 68, 26, 26)
		{
			private static final long serialVersionUID = 1L;
			@Override
			void pressed(int button)
			{
				if(manager != null)
				{
					if(button == 1)
					{
						manager.setSelectAll(true);
					}
					else
					{
						manager.setSelectAll(false);
					}
				}
			}
		};
		
		this.buttonAreaPanel.add(this.addTemplateButton);
		this.buttonAreaPanel.add(this.removeTemplateButton);
		this.buttonAreaPanel.add(this.checkAllButton);
		
		this.clipBoardPanel = new JPanel();
		this.clipBoardPanel.setLayout(null);
		this.clipBoardPanel.setPreferredSize(new Dimension(0, 35));
		this.clipBoardPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
		
		this.templateScrollPane = new JScrollPane();
		this.templateScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		this.templateScrollPane.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
		this.templateScrollPane.getVerticalScrollBar().setUnitIncrement(8);
		
		this.templateAreaPanel.setPreferredSize(new Dimension(0, 200));
		this.templateAreaPanel.add(this.templateScrollPane, BorderLayout.CENTER);
		this.templateAreaPanel.add(this.clipBoardPanel, BorderLayout.NORTH);
		
		this.templatePanel.add(this.templateAreaPanel, BorderLayout.CENTER);
		this.templatePanel.add(this.buttonAreaPanel, BorderLayout.EAST);
	}
	void setAddButtonActive()
	{
		this.addTemplateButton.setEnable(true);
	}
	void setClipBoard(ClipBoardPanel panel)
	{
		panel.setLocation(2, 2);
		this.clipBoardPanel.add(panel);
		this.clipBoardPanel.repaint();
	}
	void setManager(TemplateManager manager)
	{
		if(manager != null)
		{
			this.manager = manager;
			this.templateScrollPane.setViewportView(manager.getPanel());
			JScrollBar vertical = this.templateScrollPane.getVerticalScrollBar();
			vertical.setValue(manager.getPanel().getHeight());
			if(!this.showManagerOption)
			{
				this.removeTemplateButton.setEnabled(true);
				this.templateAreaPanel.remove(this.dftPanel);
				this.templateAreaPanel.add(this.templateScrollPane, BorderLayout.CENTER);
				this.templateScrollPane.updateUI();
			}
			this.showManagerOption = true;
		}
		else
		{
			this.manager = null;
			if(this.showManagerOption)
			{
				this.removeTemplateButton.setEnabled(false);
				this.templateAreaPanel.remove(this.templateScrollPane);
				this.templateAreaPanel.add(this.dftPanel, BorderLayout.CENTER);
				this.dftPanel.updateUI();
			}
			this.showManagerOption = false;
		}
	}
	void updateUI()
	{
		this.templateScrollPane.revalidate();
	}
	@Override
	public Component getComponent()
	{
		return this.templatePanel;
	}
}
class TaskManagerPanel implements LogicUIComponent
{
	private JPanel taskManagerPanel;
	
	private TaskManager manager;
	private boolean showManagerOption = true;
	
	private JPanel dftPanel;
	private JLabel dftPanelLabel;
	
	private JPanel scrollMasterPanel;
	private JScrollPane taskScrollPane;
	private JPanel buttonAreaPanel;
	private ButtonPanel taskUndoButton;
	private ButtonPanel taskRedoButton;
	
	TaskManagerPanel()
	{
		this.taskManagerPanel = new JPanel();
		
		this.scrollMasterPanel = new JPanel(new BorderLayout());
		this.scrollMasterPanel.setPreferredSize(new Dimension(0, 200));
		
		this.dftPanel = new JPanel(new BorderLayout());
		this.dftPanelLabel = new JLabel();
		this.dftPanelLabel.setFont(LogicCore.RES.NORMAL_FONT.deriveFont(18f));
		this.dftPanelLabel.setHorizontalAlignment(SwingConstants.CENTER);
		this.dftPanelLabel.setText(LogicCore.getResource().getLocal("SessionLost"));
		this.dftPanel.add(this.dftPanelLabel, BorderLayout.CENTER);
		this.dftPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
		
		this.taskManagerPanel.setLayout(new BorderLayout());
		this.taskManagerPanel.setBorder(new PanelBorder(LogicCore.getResource().getLocal("TaskManagerPanel")));
		
		this.buttonAreaPanel = new JPanel();
		this.buttonAreaPanel.setPreferredSize(new Dimension(30, 0));
		this.buttonAreaPanel.setLayout(null);
		
		this.taskUndoButton = new ButtonPanel(2, 4, 26, 26)
		{
			private static final long serialVersionUID = 1L;
			@Override
			void pressed(int button)
			{
				manager.recover(manager.getFocusIndex() - 1);
			}
		};
		this.taskRedoButton = new ButtonPanel(2, 34, 26, 26)
		{
			private static final long serialVersionUID = 1L;
			@Override
			void pressed(int button)
			{
				manager.recover(manager.getFocusIndex() + 1);
			}
		};
		
		this.buttonAreaPanel.add(this.taskUndoButton);
		this.buttonAreaPanel.add(this.taskRedoButton);
		
		this.taskScrollPane = new JScrollPane();
		this.taskScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		this.taskScrollPane.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
		this.taskScrollPane.getVerticalScrollBar().setUnitIncrement(8);
		
		this.scrollMasterPanel.add(this.taskScrollPane, BorderLayout.CENTER);
		
		this.taskManagerPanel.add(this.scrollMasterPanel, BorderLayout.CENTER);
		this.taskManagerPanel.add(this.buttonAreaPanel, BorderLayout.EAST);
	}
	void setManager(TaskManager manager)
	{
		if(manager != null)
		{
			this.manager = manager;
			this.taskScrollPane.setViewportView(manager.getPanel());
			JScrollBar vertical = this.taskScrollPane.getVerticalScrollBar();
			vertical.setValue(manager.getPanel().getHeight());
			if(!this.showManagerOption)
			{
				this.taskRedoButton.setEnabled(true);
				this.taskUndoButton.setEnabled(true);
				this.scrollMasterPanel.remove(this.dftPanel);
				this.scrollMasterPanel.add(this.taskScrollPane, BorderLayout.CENTER);
				this.taskScrollPane.updateUI();
			}
			this.showManagerOption = true;
		}
		else
		{
			this.manager = null;
			if(this.showManagerOption)
			{
				this.taskRedoButton.setEnabled(false);
				this.taskUndoButton.setEnabled(false);
				this.scrollMasterPanel.remove(this.taskScrollPane);
				this.scrollMasterPanel.add(this.dftPanel, BorderLayout.CENTER);
				this.dftPanel.updateUI();
			}
			this.showManagerOption = false;
		}
	}
	@Override
	public Component getComponent()
	{
		return this.taskManagerPanel;
	}
}
class UIButton extends JButton
{
	private static final long serialVersionUID = 1L;
	UIButton(int locationX, int locationY,int sizeX, int sizeY, Image basicImage, Image SelectedIcon)
	{
		super();
		this.setBounds(locationX, locationY, sizeX, sizeY);
		//TODO
		//this.setIcon(new ImageIcon(basicImage));
		//this.setSelectedIcon(new ImageIcon(SelectedIcon));
	}
	UIButton(int sizeX, int sizeY, Image basicImage, Image SelectedIcon)
	{
		super();
		this.setPreferredSize(new Dimension(sizeX, sizeY));
		this.setSize(sizeX, sizeY);
		//TODO
		//this.setIcon(new ImageIcon(basicImage));
		//this.setSelectedIcon(new ImageIcon(SelectedIcon));
	}
}
class PanelBorder extends TitledBorder
{
	private static final long serialVersionUID = 1L;

	public PanelBorder(String title)
	{
		super(new EtchedBorder(EtchedBorder.LOWERED), title);
		super.setTitleFont(LogicCore.RES.NORMAL_FONT.deriveFont(Font.BOLD, 16.0f));
	}
}
class ManySelectEditPanel extends JPanel
{
	private static final long serialVersionUID = 1L;
	private JLabel numberLabel;
	private ButtonPanel copyButton;
	private ButtonPanel cutButton;
	private ButtonPanel createTempButton;
	private ButtonPanel exportTempButton;
	private ButtonPanel restoreButton;
	private ButtonPanel removeButton;
	private ArrayList<GridMember> selectMembers;
	
	ManySelectEditPanel(Grid grid, ArrayList<GridMember> selectMembers, BlockControlPanel blockControl)
	{
		super();
		this.selectMembers = selectMembers;
		this.setLayout(null);
		this.numberLabel = new JLabel();
		this.numberLabel.setHorizontalAlignment(SwingConstants.CENTER);
		this.numberLabel.setFont(LogicCore.RES.NORMAL_FONT.deriveFont(16F));
		this.numberLabel.setBounds(0, 0, 373, 20);
		this.numberLabel.setText(Integer.toString(selectMembers.size()) + LogicCore.getResource().getLocal("SelecterSelected"));
		this.copyButton = new ButtonPanel(66, 25, 40, 40)
		{
			private static final long serialVersionUID = 1L;
			@Override
			void pressed(int button)
			{
				ClipBoardPanel.setClipBoard(getSelectDataBranch());
			}
		};
		this.cutButton = new ButtonPanel(166, 25, 40, 40)
		{
			private static final long serialVersionUID = 1L;
			@SuppressWarnings("unchecked")
			@Override
			void pressed(int button)
			{
				TaskUnit task = grid.getSession().getTaskManager().setTask();
				grid.getSession().getCore().getUI().addTrackedPane(new TrackedPane((ArrayList<GridMember>) selectMembers.clone()
						, grid.getSession().getCore().getUI()));
				ClipBoardPanel.setClipBoard(getSelectDataBranch());
				for(GridMember member : (ArrayList<GridMember>)selectMembers.clone())
				{
					task.addCommand(new RemoveMemberOnGrid(member));
				}
			}
		};
		this.removeButton = new ButtonPanel(266, 25, 40, 40)
		{
			private static final long serialVersionUID = 1L;
			
			@SuppressWarnings("unchecked")
			@Override
			void pressed(int button)
			{
				TaskUnit task = grid.getSession().getTaskManager().setTask();
				for(GridMember member : (ArrayList<GridMember>)selectMembers.clone())
				{
					task.addCommand(new RemoveMemberOnGrid(member));
				}
			}
		};
		this.createTempButton = new ButtonPanel(66, 75, 40, 40)
		{
			private static final long serialVersionUID = 1L;
			@Override
			void pressed(int button)
			{
				grid.getSession().getTemplateManager().addTemplate(getSelectDataBranch());
			}
		};
		this.exportTempButton = new ButtonPanel(166, 75, 40, 40)
		{
			private static final long serialVersionUID = 1L;
			@Override
			void pressed(int button)
			{
				for(GridMember member : selectMembers)
				{
					if(member instanceof LogicBlock)
					{
						((LogicBlock)member).setActive(false);
					}
				}
			}
		};
		this.restoreButton = new ButtonPanel(266, 75, 40, 40)
		{
			private static final long serialVersionUID = 1L;
			@Override
			void pressed(int button)
			{
				for(GridMember member : selectMembers)
				{
					if(member instanceof LogicBlock)
					{
						((LogicBlock)member).setActive(true);
					}
				}
			}
		};
		this.add(this.numberLabel);
		
		this.add(this.copyButton);
		this.add(this.cutButton);
		this.add(this.removeButton);
		this.add(this.createTempButton);
		this.add(this.exportTempButton);
		this.add(this.restoreButton);
		
		
		blockControl.addControlPanel(this);
		
	}
	private DataBranch getSelectDataBranch()
	{
		DataBranch branch = new DataBranch("ClipBoard");
		Tag tag = null;
		for(GridMember member : selectMembers)
		{
			DataBranch memberBranch = new DataBranch("GridMember");
			member.getData(memberBranch);
			branch.addLowerBranch(memberBranch);
			if(member instanceof Tag)
			{
				tag = (Tag)member;
			}
		}
		if(tag != null)
		{
			branch.setData("label", tag.getDescription());
		}
		return branch;
	}
}
class SelectControlPanel extends JPanel
{
	private static final long serialVersionUID = 1L;
	private JLabel numberLabel;
	private String text;
	
	SelectControlPanel(String text, BlockControlPanel blockControl)
	{
		super();
		this.text = text;
		this.setLayout(null);
		this.numberLabel = new JLabel();
		this.numberLabel.setHorizontalAlignment(SwingConstants.CENTER);
		this.numberLabel.setFont(LogicCore.RES.NORMAL_FONT.deriveFont(16.0F));
		this.numberLabel.setBounds(0, 40, 373, 20);
		this.setNumber(0);
		this.add(this.numberLabel);
		blockControl.addControlPanel(this);
		
	}
	void setNumber(int num)
	{
		this.numberLabel.setText(Integer.toString(num) + this.text);
	}
}
class DefaultPane extends JPanel
{
	private static final long serialVersionUID = 1L;
	private JLabel text;
	DefaultPane()
	{
		super();
		this.setLayout(null);
		this.text = new JLabel();
		this.text.setHorizontalAlignment(SwingConstants.CENTER);
		this.text.setFont(LogicCore.RES.NORMAL_FONT.deriveFont(16.0f));
		this.text.setBounds(0, 40, 373, 20);
		this.text.setText(LogicCore.getResource().getLocal("SelecterNone"));
		this.add(this.text);
	}
}
class ColorSelector extends JPanel
{
	private static final long serialVersionUID = 1L;
	
	private JPanel samplePanel = new JPanel();

	private ColorSlider rSlider = new ColorSlider(0, 255);
	private ColorSlider gSlider = new ColorSlider(20, 230);
	private ColorSlider bSlider = new ColorSlider(40, 153);
	
	ColorSelector()
	{
		this.setLayout(null);
		this.setSize(300, 80);
		
		JPanel tempPanel = new JPanel();
		tempPanel.setLayout(null);
		tempPanel.setBorder(new EtchedBorder(EtchedBorder.RAISED));
		this.samplePanel.setBounds(2, 2, 36, 36);
		tempPanel.setBounds(255, 10, 40, 40);
		
		tempPanel.add(this.samplePanel);
		this.add(tempPanel);
		
		this.add(this.rSlider);
		this.add(this.gSlider);
		this.add(this.bSlider);
		
		this.add(new ColorPicker(new Color(255, 0, 0), 5));
		this.add(new ColorPicker(new Color(255, 128, 64), 30));
		this.add(new ColorPicker(new Color(128, 255, 128), 55));
		this.add(new ColorPicker(new Color(9, 181, 247), 80));
		this.add(new ColorPicker(new Color(128, 0, 255), 105));
		this.add(new ColorPicker(new Color(255, 0, 255), 130));
		this.add(new ColorPicker(new Color(255, 255, 0), 155));
		this.add(new ColorPicker(new Color(255, 230, 153), 180));
		this.add(new ColorPicker(new Color(0, 0, 0), 205));
		this.add(new ColorPicker(new Color(255, 255, 255), 230));
		
		this.samplePanel.setBackground(this.getColor());
	}
	Color getColor()
	{
		Color color = this.samplePanel.getBackground();
		try
		{
			color = new Color(this.rSlider.getValue(), this.gSlider.getValue(), this.bSlider.getValue());
		}
		catch(Exception e)
		{
			
		}
		return color;
	}
	void setColor(Color color)
	{
		this.rSlider.setValue(color.getRed());
		this.gSlider.setValue(color.getGreen());
		this.bSlider.setValue(color.getBlue());
	}
	private void setSample()
	{
		this.samplePanel.setBackground(this.getColor());
	}
	private class ColorSlider extends JPanel
	{
		private static final long serialVersionUID = 1L;
		private JSlider slider = new JSlider(JSlider.HORIZONTAL, 0, 255, 0);
		private JTextField textField = new JTextField();
		private boolean scrollCheckFlag = true;
		
		ColorSlider(int y, int color)
		{
			this.setLayout(null);
			this.setBounds(0, y, 250, 20);
			this.slider.setBounds(0, 0, 215, 20);
			this.slider.addChangeListener(new ChangeListener()
			{
				@Override
				public void stateChanged(ChangeEvent arg0)
				{
					if(scrollCheckFlag)
					{
						textField.setText(Integer.toString(slider.getValue()));
					}
					setSample();
				}
			});
			this.textField.setBounds(220, 0, 30, 18);
			this.textField.setBorder(null);
			this.textField.addKeyListener(new KeyAdapter() 
			{
				@Override
				public void keyTyped(KeyEvent e) 
		        {
					int nowInt = 0;
					char c = e.getKeyChar();
					if (!((c >= '0') && (c <= '9') || (c == KeyEvent.VK_BACK_SPACE) || (c == KeyEvent.VK_DELETE)))
					{
						nowInt = Integer.parseInt(textField.getText());
		        	}
					else
					{
						if((c >= '0') && (c <= '9') && !textField.getText().equals(""))
						{
							nowInt = Integer.parseInt(textField.getText() + Character.toString(c));
							if(nowInt > 255)
							{
								nowInt = 255;
							}
						}
					}
					e.consume();
					textField.setText(Integer.toString(nowInt));
					scrollCheckFlag = false;
					System.out.println(nowInt);
					slider.setValue(nowInt);
					scrollCheckFlag = true;
		        }
			});
			this.setValue(color);
			this.add(this.slider);
			this.add(this.textField);
		}
		void setValue(int num)
		{
			this.slider.setValue(num);
		}
		int getValue()
		{
			return this.slider.getValue();
		}
	}
	private class ColorPicker extends ButtonPanel
	{
		private static final long serialVersionUID = 1L;
		
		private final Color color;
		private JPanel colorPanel = new JPanel();
		
		ColorPicker(Color color, int x)
		{
			this.color = color;
			this.setBackground(new Color(200, 200, 200));
			this.setLayout(null);
			this.setBounds(x, 60, 20, 20);
			this.setBorder(new EtchedBorder(EtchedBorder.RAISED));
			this.colorPanel.setBounds(2, 2, 16, 16);
			this.colorPanel.setBackground(color);
			this.add(this.colorPanel);
		}
		@Override
		void pressed(int button)
		{
			setColor(this.color);
		}
	}
}
class TimeSelector extends JPanel
{
	private static final long serialVersionUID = 1L;
	
	private int time;
	private int max;
	private int min;
	
	private TimeSetButton subTime1Button;
	private TimeSetButton subTime10Button;
	private TimeSetButton subTime100Button;
	private TimeSetButton addTime1Button;
	private TimeSetButton addTime10Button;
	private TimeSetButton addTime100Button;
	private JLabel numberLabel;
	private JLabel suffixLabel;
	
	TimeSelector(int defaultTime, int min, int max, String suffix)
	{
		this.time = defaultTime;
		this.max = max;
		this.min = min;
		this.setSize(149, 20);
		this.setLayout(null);
		this.subTime1Button = new TimeSetButton(34, 0, 15, 20, -1);
		this.subTime10Button = new TimeSetButton(17, 0, 15, 20, -10);
		this.subTime100Button = new TimeSetButton(0, 0, 15, 20, -100);
		this.addTime1Button = new TimeSetButton(99, 0, 15, 20, 1);
		this.addTime10Button = new TimeSetButton(116, 0, 15, 20, 10);
		this.addTime100Button = new TimeSetButton(133, 0, 15, 20, 100);
		this.numberLabel = new JLabel();
		this.numberLabel.setHorizontalAlignment(JLabel.RIGHT);
		this.numberLabel.setBounds(48, 0, 30, 20);
		this.suffixLabel = new JLabel(suffix);
		this.suffixLabel.setBounds(79, 0, 30, 20);
		
		this.add(this.suffixLabel);
		this.add(this.subTime1Button);
		this.add(this.subTime10Button);
		this.add(this.subTime100Button);
		this.add(this.addTime1Button);
		this.add(this.addTime10Button);
		this.add(this.addTime100Button);
		this.add(this.numberLabel);
		this.checkAndSetTime(this.time);
	}
	final void setMin(int min)
	{
		this.min = min;
		this.checkAndSetTime(this.time);
	}
	final void setMax(int max)
	{
		this.max = max;
		this.checkAndSetTime(this.time);
	}
	final void checkAndSetTime(int nowTime)
	{
		if(nowTime > this.max)
		{
			nowTime = this.max;
		}
		else if(nowTime < this.min)
		{
			nowTime = this.min;
		}
		this.time = nowTime;
		this.numberLabel.setText(Integer.toString(this.time));
		this.timerEvent(this.time);
	}
	public void setTime(int time)
	{
		this.time = time;
		this.numberLabel.setText(Integer.toString(this.time));
	}
	void timerEvent(int time)
	{
		
	}
	private class TimeSetButton extends ButtonPanel
	{
		private static final long serialVersionUID = 1L;
		
		private final int tick;
		TimeSetButton(int locationX, int locationY,int sizeX, int sizeY, int tick)
		{
			super(locationX, locationY, sizeX, sizeY);
			this.tick = tick;
		}
		@Override
		void pressed(int button)
		{
			checkAndSetTime(time + this.tick);
		}
	}
}
class EditPane extends JPanel
{
	private static final long serialVersionUID = 1L;
	
	private ButtonPanel copyButton = new ButtonPanel(325, 100, 20, 20)
	{
		private static final long serialVersionUID = 1L;
		@Override
		void pressed(int mouse)
		{
			DataBranch temp = new DataBranch("ClipBoard");
			temp.addLowerBranch(member.getData(new DataBranch("GridMember")));
			ClipBoardPanel.setClipBoard(temp);
		}
	};
	private ButtonPanel removeButton = new ButtonPanel(350, 100, 20, 20)
	{
		private static final long serialVersionUID = 1L;
		@Override
		void pressed(int mouse)
		{
			TaskUnit task = member.getGrid().getSession().getTaskManager().setTask();
			task.addCommand(new RemoveMemberOnGrid(member));
		}
	};
	
	protected GridMember member;

	protected JLabel text;
	
	EditPane()
	{
		this.setLayout(null);
		this.text = new JLabel();
		this.text.setHorizontalAlignment(SwingConstants.CENTER);
		this.text.setFont(LogicCore.RES.NORMAL_FONT.deriveFont(16.0f));
		this.text.setBounds(135, 0, 223, 20);
		this.add(this.text);
		this.add(this.copyButton);
		this.add(this.removeButton);
	}
	void updateMemberStatus(){};
	void setInfo(GridMember member)
	{
		if(member.isPlacement())
		{
			this.copyButton.setEnable(true);
			this.removeButton.setEnable(true);
		}
		else
		{
			this.copyButton.setEnable(false);
			this.removeButton.setEnable(false);
		}
		this.member = member;
		this.text.setText(LogicCore.getResource().getLocal(this.member.getName()) + " " + LogicCore.getResource().getLocal("Edit"));
	}
}
class TagEditPane extends EditPane
{
	private static final long serialVersionUID = 1L;
	
	private Tag tag;
	private ColorSelector colorSelector;
	private ButtonPanel textEditButton;
	private ButtonPanel colorSetButton;
	
	TagEditPane()
	{
		super();
		super.text.setLocation(70, 0);
		this.colorSelector = new ColorSelector();
		this.colorSelector.setLocation(10, 30);
		this.textEditButton = new ButtonPanel(340, 10, 30, 30)
		{
			private static final long serialVersionUID = 1L;

			@Override
			void pressed(int button)
			{
				tag.toggleModeActive();
			}
		};
		this.colorSetButton = new ButtonPanel(340, 50, 30, 30)
		{
			private static final long serialVersionUID = 1L;
			
			@Override
			void pressed(int button)
			{
				if(member.isPlacement())
				{
					TaskUnit task = member.getGrid().getSession().getTaskManager().setTask();
					task.addCommand(new SetMemberColor(tag, "BackGround", colorSelector.getColor(), member.getCore().getSession().getFocusSession()));
				}
				else
				{
					((ColorSet)member).setColor("BackGround", colorSelector.getColor());
				}
			}
		};
		this.add(this.colorSelector);
		this.add(this.textEditButton);
		this.add(this.colorSetButton);
	}
	@Override
	void updateMemberStatus()
	{
		System.out.println("check");
		this.colorSelector.setColor(((ColorSet)member).getColor("BackGround"));
	}
	@Override
	void setInfo(GridMember member)
	{
		super.setInfo(member);
		this.tag = (Tag)member;
		this.colorSelector.setColor(this.tag.getColor("BackGround"));
	}
}
class LogicTREditPane extends EditPane
{
	private static final long serialVersionUID = 1L;
	
	private ButtonPanel restoreButton = new ButtonPanel(300, 100, 20, 20)
	{
		private static final long serialVersionUID = 1L;

		@Override
		void pressed(int button)
		{
			TaskUnit task = member.getGrid().getSession().getTaskManager().setTask();
			task.addCommand(new SetBlockActive((LogicBlock)member, !logicMember.getActive()));
		}
	};
	
	private JLabel locationText = new JLabel();
	protected LogicBlock logicMember;
	protected ArrayList<IOControlButton> IOEditButton = new ArrayList<IOControlButton>();

	protected JPanel editViewPanel;
	
	LogicTREditPane()
	{
		this.add(this.restoreButton);
		this.setIOSwitch();
		this.add(this.editViewPanel);
		for(IOControlButton btn : this.IOEditButton)
		{
			this.editViewPanel.add(btn);
		}

		this.locationText.setHorizontalAlignment(SwingConstants.CENTER);
		this.locationText.setFont(LogicCore.RES.NORMAL_FONT.deriveFont(16.0f));
		this.locationText.setBounds(135, 100, 135, 20);
			
		this.add(this.locationText);
		
	}
	void setIOSwitch()
	{
		this.editViewPanel = new JPanel()
		{
			private static final long serialVersionUID = 1L;
			@Override
			public void paint(Graphics g)
			{
				super.paint(g);
				g.drawRect(0, 0, 119, 119);
			}
		};
		this.editViewPanel.setLayout(null);
		this.IOEditButton.add(new IOControlButton(100, 28, 16, 64, Direction.EAST));
		this.IOEditButton.add(new IOControlButton(4, 28, 16, 64, Direction.WEST));
		this.IOEditButton.add(new IOControlButton(28, 100, 64, 16, Direction.SOUTH));
		this.IOEditButton.add(new IOControlButton(28, 4, 64, 16, Direction.NORTH));
		
		this.editViewPanel.setBounds(5, 0, 120, 120);
		this.editViewPanel.setBackground(new Color(200, 200, 200));
	}
	@Override
	void setInfo(GridMember member)
	{
		super.setInfo(member);
		this.logicMember = (LogicBlock)member;
		if(member.isPlacement())
		{
			this.locationText.setText("(X:" + logicMember.getBlockLocationX() + " Y: " + logicMember.getBlockLocationY() + ")");
			//this.disableButton.setEnable(true);
			this.restoreButton.setEnable(true);
		}
		else
		{
			this.locationText.setText(LogicCore.getResource().getLocal("IsNotPlaceBlock"));
			//this.disableButton.setEnable(false);
			this.restoreButton.setEnable(false);
		}
		for(IOControlButton btn : this.IOEditButton)
		{
			btn.setLogicBlock(this.logicMember);
			btn.setImage();
		}
	}
}
class IOControlButton extends ButtonPanel
{
	private static final long serialVersionUID = 1L;
	
	protected LogicBlock logicMember;
	protected Direction ext;
	
	IOControlButton(int locX, int locY, int sizeX, int sizeY, Direction ext)
	{
		super(locX, locY, sizeX, sizeY);
		this.ext = ext;
	}
	void setLogicBlock(LogicBlock logicMember)
	{
		this.logicMember = logicMember;
	}
	@Override
	public void pressed(int mouse)
	{
		if(this.logicMember.getGrid() != null)
		{
			TaskUnit task = this.logicMember.getGrid().getSession().getTaskManager().setTask();
			task.addCommand(new SetLogicBlockIO(this.logicMember, ext, this.logicMember.getGrid().getSession()));
		}
		else
		{
			this.logicMember.toggleIO(ext);
		}
		this.setImage();
	}
	void setImage()
	{
		super.setBasicImage(LogicCore.getResource().getImage("TR_BLOCK_EDIT_" + this.logicMember.getIOStatus(this.ext).getTag() + "_BASIC_" + ext));
		super.setBasicPressImage(LogicCore.getResource().getImage("TR_BLOCK_EDIT_" + this.logicMember.getIOStatus(this.ext).getTag() + "_PUSH_" + ext));
	}
}
class ButtonEditPanel extends LogicTREditPane
{
	private static final long serialVersionUID = 1L;

	private TimeSelector timeSelector;
	ButtonEditPanel()
	{
		timeSelector = new TimeSelector(1, 1, 99, "tik")//tick
		{
			private static final long serialVersionUID = 1L;

			@Override
			void timerEvent(int time)
			{
				if(member != null)
				{
					if(member.isPlacement())
					{
						TaskUnit task = member.getGrid().getSession().getTaskManager().setTask();
						task.addCommand(new SetBlockTimer((TimeSetter)member, "btn", time, member.getGrid().getSession()));
					}
					else
					{
						((TimeSetter)member).setTime("btn", time);
					}
				}
			}
		};
		this.timeSelector.setLocation(222, 70);
		this.add(this.timeSelector);
	}
	@Override
	void setInfo(GridMember member)
	{
		super.setInfo(member);
		this.timeSelector.setTime(((TimeSetter)member).getTime());
	}
	@Override
	void updateMemberStatus()
	{
		this.timeSelector.setTime(((TimeSetter)member).getTime());
	}
}
class WireEditPane extends LogicTREditPane
{
	private Wire wire;
	
	private ModSelectButton standardModButton;
	private ModSelectButton crossAModButton;
	private ModSelectButton crossBModButton;
	private ModSelectButton crossCModButton;
	
	private static final long serialVersionUID = 1L;
	
	WireEditPane()
	{
		super();
		this.standardModButton = new ModSelectButton(170, 40, 30, 30, WireType.Standard);
		this.crossAModButton = new ModSelectButton(215, 40, 30, 30, WireType.Cross_A);
		this.crossBModButton = new ModSelectButton(260, 40, 30, 30, WireType.Cross_B);
		this.crossCModButton = new ModSelectButton(305, 40, 30, 30, WireType.Cross_C);
		this.add(this.standardModButton);
		this.add(this.crossAModButton);
		this.add(this.crossBModButton);
		this.add(this.crossCModButton);
	}
	@Override
	void setIOSwitch()
	{
		super.editViewPanel = new JPanel()
		{
			private static final long serialVersionUID = 1L;
			@Override
			public void paint(Graphics g)
			{
				super.paint(g);
				g.drawImage(LogicCore.getResource().getImage("WIRE_TYPE_" + wire.getType().getImageTag()), 44, 44, this);
				g.drawRect(0, 0, 119, 119);
			}
		};
		super.editViewPanel.setLayout(null);
		super.IOEditButton.add(new WireIOControlButton(72, 52, 44, 16, Direction.EAST));
		super.IOEditButton.add(new WireIOControlButton(4, 52, 44, 16, Direction.WEST));
		super.IOEditButton.add(new WireIOControlButton(52, 72, 16, 44, Direction.SOUTH));
		super.IOEditButton.add(new WireIOControlButton(52, 4, 16, 44, Direction.NORTH));
		
		super.editViewPanel.setBounds(5, 0, 120, 120);
		super.editViewPanel.setBackground(new Color(200, 200, 200));
	}
	@Override
	void setInfo(GridMember member)
	{
		super.setInfo(member);
		this.wire = (Wire)member;
	}
	@Override
	void updateMemberStatus()
	{
		super.editViewPanel.repaint();
	}
	private class ModSelectButton extends ButtonPanel
	{
		private static final long serialVersionUID = 1L;
		
		private final WireType type;
		ModSelectButton(int x, int y, int w, int h, WireType type)
		{
			super(x, y, w, h);
			this.type = type;
		}
		@Override
		void pressed(int mouse)
		{
			if(wire.isPlacement())
			{
				TaskUnit task = wire.getGrid().getSession().getTaskManager().setTask();
				task.addCommand(new WireTypeEdit(wire, this.type, wire.getGrid().getSession()));
			}
			else
			{
				wire.setWireType(type);
			}
			editViewPanel.repaint();
			for(IOControlButton io : IOEditButton)
			{
				io.setImage();
			}
		}
	}
}
class WireIOControlButton extends IOControlButton
{
	private static final long serialVersionUID = 1L;
	
	WireIOControlButton(int locX, int locY, int sizeX, int sizeY, Direction ext)
	{
		super(locX, locY, sizeX, sizeY, ext);
	}
	@Override
	void setImage()
	{
		super.setBasicImage(LogicCore.getResource().getImage("TR_WIRE_EDIT_" + super.logicMember.getIOStatus(super.ext).getTag() + "_BASIC_" + this.ext));
		super.setBasicPressImage(LogicCore.getResource().getImage("TR_WIRE_EDIT_" + super.logicMember.getIOStatus(super.ext).getTag() + "_PUSH_" + this.ext));
	}
}
class TrackedPane extends JPanel implements SizeUpdate
{
	private static final long serialVersionUID = 1L;
	private ArrayList<GridMember> members;
	private HashMap<GridMember, BufferedImage> images;
	private int maxX, maxY, minX, minY;
	private UI logicUI;
	TrackedPane(ArrayList<GridMember> members, UI logicUI)
	{
		this.logicUI = logicUI;
		this.members = members;
		this.images = new HashMap<GridMember, BufferedImage>();
		for(GridMember member : this.members)
		{
			BufferedImage temp = member.getSnapShot();
			for(int x = 0; x < temp.getWidth(); x++)
			{
				for(int y = 0; y < temp.getHeight(); y++)
				{
					Color c = new Color(temp.getRGB(x, y));
					temp.setRGB(x, y, new Color(c.getRed(), c.getGreen(), c.getRed(), 150).getRGB());
				}
			}
			images.put(member, temp);
		}
		
		this.setOpaque(false);
		this.sizeUpdate();
	}
	@Override
	public void paint(Graphics g)
	{
		super.paint(g);
		for(GridMember member : members)
		{
			g.drawImage(images.get(member), (member.getUIabsLocationX() - minX) * logicUI.getUISize().getmultiple()
					, (member.getUIabsLocationY() - minY) * logicUI.getUISize().getmultiple(), this);
		}
	}
	@Override
	public void sizeUpdate()
	{
		this.minX = members.get(0).getUIabsLocationX();
		this.minY = members.get(0).getUIabsLocationY();
		this.maxX = members.get(0).getUIabsLocationX() + members.get(0).getUIabsSizeX();
		this.maxY = members.get(0).getUIabsLocationY() + members.get(0).getUIabsSizeY();
		for(GridMember member : members)
		{
			this.minX = member.getUIabsLocationX() < minX ? member.getUIabsLocationX() : minX;
			this.minY = member.getUIabsLocationY() < minY ? member.getUIabsLocationY() : minY;
			this.maxX = member.getUIabsLocationX() + member.getUIabsSizeX() > maxX ? member.getUIabsLocationX() + member.getUIabsSizeX() : maxX;
			this.maxY = member.getUIabsLocationY() + member.getUIabsSizeY() > maxY ? member.getUIabsLocationY() + member.getUIabsSizeY() : maxY;
		}
		this.setSize((this.maxX - this.minX) * logicUI.getUISize().getmultiple(), (this.maxY - this.minY) * logicUI.getUISize().getmultiple());
		this.repaint();
	}
	void addMemberOnGrid(int x, int y)
	{
		int multiple = this.logicUI.getUISize().getmultiple();
		Session session = this.logicUI.getCore().getSession().getFocusSession();
		SizeInfo gridSize = session.getGrid().getGridSize();
		int stdX = (x / multiple) - (gridSize.getNX() * Size.REGULAR_SIZE);
		int stdY = (y / multiple) - (gridSize.getNY() * Size.REGULAR_SIZE);
		for(GridMember member : members)
		{
			int placeAbsX = stdX + member.getUIabsLocationX() - minX - ((this.getWidth() / 2) / multiple);
			int placeAbsY = stdY + member.getUIabsLocationY() - minY - ((this.getHeight() / 2) / multiple);
			System.out.println(gridSize.getPX() + " " + gridSize.getNX());
			
			if(placeAbsX < gridSize.getPX() * Size.REGULAR_SIZE && placeAbsX > -(gridSize.getNX() + 1) * Size.REGULAR_SIZE
			&& placeAbsY < gridSize.getPY() * Size.REGULAR_SIZE && placeAbsY > -(gridSize.getNY() + 1) * Size.REGULAR_SIZE)
			{
				session.getTaskManager().setTask().addCommand(new PutMemberOnGrid(session.getGrid(), member, placeAbsX, placeAbsY));
			}
		}
		if(this.members.size() == 1)
		{
			GridMember member = this.members.get(0);
			if(member.isPlacement())
			{
				member.getGrid().selectFocus(member);
			}
		}
		this.removeAll();
	}
}
class ButtonPanel extends JPanel implements MouseMotionListener, MouseListener
{
	private static final long serialVersionUID = 1L;
	
	private ButtonStatus status = ButtonStatus.NONE;
	
	private BufferedImage basicImage;
	private BufferedImage onMouseImage;
	private BufferedImage basicPressImage;
	private BufferedImage disableImage;
	private LinkedHashMap<Integer, BufferedImage> pressImages = new LinkedHashMap<Integer, BufferedImage>();
	
	private BufferedImage nowImage;
	private int mouseButton = 0;
	
	private boolean enable = true;
	
	ButtonPanel()
	{
		this.addMouseMotionListener(this);
		this.addMouseListener(this);
		this.setOpaque(false);
	}
	ButtonPanel(int x, int y, int width, int height)
	{
		this();
		this.setBounds(x, y, width, height);
	}
	ButtonPanel(BufferedImage basicImage, BufferedImage onMouseImage, BufferedImage basicPressImage)
	{
		this();
		this.basicImage = basicImage;
		this.onMouseImage = onMouseImage;
		this.basicPressImage = basicPressImage;
	}
	ButtonPanel(int x, int y)
	{
		this();
		this.setSize(x, y);
		this.setPreferredSize(new Dimension(x, y));
	}
	@Override
	public final void mouseClicked(MouseEvent e)
	{
	}
	@Override
	public final void mouseEntered(MouseEvent e)
	{
		if(!(this.status == ButtonStatus.PRESS))
		{
			this.status = ButtonStatus.ONMOUSE;
		}
		this.imageSet();
	}
	@Override
	public final void mouseExited(MouseEvent e)
	{
		if(!(this.status == ButtonStatus.PRESS))
		{
			this.status = ButtonStatus.NONE;
		}
		this.imageSet();
	}
	@Override
	public final void mousePressed(MouseEvent e)
	{
		this.mouseButton = e.getButton();
		this.status = ButtonStatus.PRESS;
		this.imageSet();
	}
	@Override
	public final void mouseReleased(MouseEvent e)
	{
		if((e.getX() >= 0 && e.getY() >= 0) && (e.getX() <= this.getWidth() && e.getY() <= this.getHeight()))
		{
			this.pressed(e.getButton());
			try 
			{//TODO 좌표 꼬이는 버그 해결용 일단두시오
				new Robot().mouseMove((int)MouseInfo.getPointerInfo().getLocation().getX() + 1, (int)MouseInfo.getPointerInfo().getLocation().getY() + 1);
				new Robot().mouseMove((int)MouseInfo.getPointerInfo().getLocation().getX() - 1, (int)MouseInfo.getPointerInfo().getLocation().getY() - 1);
			}
			catch(Exception e1)
			{
				e1.printStackTrace();
			}
			this.status = ButtonStatus.ONMOUSE;
		}
		else
		{
			this.status = ButtonStatus.NONE;
		}
		this.imageSet();
	}
	@Override
	public final void mouseDragged(MouseEvent arg0)
	{
	}
	@Override
	public final void mouseMoved(MouseEvent arg0)
	{
	}
	@Override
	public void paint(Graphics g)
	{
		super.paint(g);
		if(this.nowImage != null)
		{
			g.drawImage(this.nowImage, 0, 0, this);
		}
		else
		{
			g.fillRect(0, 0, this.getWidth(), this.getHeight());
		}
		super.paintChildren(g);
	}
	void pressed(int button)
	{
	}
	void setEnable(boolean option)
	{
		if(this.enable != option)
		{
			this.enable = option;
			if(option)
			{
				this.addMouseListener(this);
				this.addMouseMotionListener(this);
			}
			else
			{
				this.removeMouseListener(this);
				this.removeMouseMotionListener(this);
				this.status = ButtonStatus.NONE;
			}
			this.imageSet();
		}
	}
	void setBasicImage(BufferedImage img)
	{
		this.basicImage = img;
		this.nowImage = this.basicImage;
		this.repaint();
	}
	void setBasicPressImage(BufferedImage img)
	{
		this.basicPressImage = img;
		this.repaint();
	}
	void setBasicDisableImage(BufferedImage img)
	{
		this.disableImage = img;
		this.repaint();
	}
	void setPressImage(int button, BufferedImage img)
	{
		this.pressImages.put(new Integer(button), img);
		this.repaint();
	}
	void setOnMouseImage(BufferedImage image)
	{
		this.onMouseImage = image;
		this.repaint();
		
	}
	void imageSet()
	{
		if(this.enable)
		{
			if(this.status == ButtonStatus.PRESS)
			{
				if(this.pressImages.containsKey(new Integer(this.mouseButton)))
				{
					this.nowImage = this.pressImages.get(new Integer(this.mouseButton));
				}
				else if(this.basicPressImage != null)
				{
					this.nowImage = this.basicPressImage;
				}
				else
				{
					this.nowImage = this.basicImage;
				}
			}
			else if(this.status == ButtonStatus.ONMOUSE)
			{
				if(this.onMouseImage != null)
				{
					this.nowImage = this.onMouseImage;
				}
				else
				{
					this.nowImage = this.basicImage;
				}
			}
			else
			{
				this.nowImage = this.basicImage;
			}
		}
		else
		{
			if(this.disableImage == null)
			{
				this.nowImage = this.basicImage;
			}
			else
			{
				this.nowImage = this.disableImage;
			}
		}
		super.repaint();
	}
	private enum ButtonStatus
	{
		NONE, PRESS, ONMOUSE;
	}
}
class FileSavePanel extends FileManagerWindow
{
	private boolean fileSelectorListenerFlag = true;
	
	private ButtonGroup grp;
	private JRadioButton dftSave;
	private JRadioButton cloudSave;
	private JCheckBox readOnly;
	private JLabel directoryLabel;
	private JLabel fileNameLabel;
	private JTextField fileNameField;
	private boolean fileNameFiledListenerFlag = true;
	private JButton removeButton;
	private JButton saveButton;
	
	private JTextField titleField;
	private JTextArea descriptionArea;
	
	FileSavePanel(LogicCore core)
	{
		super(core);
		
		super.diaLog.setSize(720, 300);
		super.diaLog.setTitle("저장");
		
		this.grp = new ButtonGroup();
		this.dftSave = new JRadioButton("로컬");
		this.dftSave.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				setDFTFileView();
				fileSelecterPanel.setSize(500, 175);
			}
		});
		this.dftSave.setBounds(5, 225, 100, 20);
		this.cloudSave = new JRadioButton("클라우드");
		this.cloudSave.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				setCloudFileView();
				fileSelecterPanel.setSize(500, 195);
			}
		});
		this.cloudSave.setBounds(5, 245, 100, 20);
		this.grp.add(this.dftSave);
		this.grp.add(this.cloudSave);
		this.readOnly = new JCheckBox("읽기 전용");
		this.readOnly.setBounds(110, 225, 100, 20);
		this.dftSave.setSelected(true);
		
		this.directoryLabel = new JLabel();
		this.directoryLabel.setBounds(10, 175, 480, 20);
		this.fileNameLabel = new JLabel("이름");
		this.fileNameLabel.setBounds(10, 200, 50, 20);
		this.fileNameField = new JTextField();
		this.fileNameField.setBounds(65, 200, 420, 20);
		
		this.removeButton = new JButton("삭제");
		this.removeButton.setBounds(310, 230, 80, 30);
		this.removeButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				selectFile.delete();
				fileChooser.rescanCurrentDirectory();
				checkFileReadStatus();
			}
		});
		
		this.saveButton = new JButton("저장");
		this.saveButton.setBounds(400, 230, 80, 30);
		this.saveButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				focusSession.setName(titleField.getText());
				focusSession.setDescription(descriptionArea.getText());
				focusSession.saveData(selectFile);
				if(readOnly.isSelected())
				{
					selectFile.setReadOnly();
				}
				fileChooser.rescanCurrentDirectory();
				checkFileReadStatus();
			}
		});
		
		this.titleField = new JTextField();
		this.titleField.setBounds(505, 5, 205, 20);
		this.titleField.setBorder(new EtchedBorder(EtchedBorder.RAISED));
		this.descriptionArea = new JTextArea();
		this.descriptionArea.setBounds(505, 30, 205, 235);
		this.descriptionArea.setBorder(new EtchedBorder(EtchedBorder.RAISED));
		this.descriptionArea.setLineWrap(true);
		
		super.fileSelecterPanel.setSize(500, 175);
		super.fileChooser.setSize(500, 240);
		super.fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		super.fileChooser.addPropertyChangeListener(new PropertyChangeListener()
		{
			@Override
			public void propertyChange(PropertyChangeEvent arg0)
			{
				if(fileChooser.getSelectedFile() != null && !fileChooser.getSelectedFile().isDirectory() && fileSelectorListenerFlag)
				{
					fileNameFiledListenerFlag = false;
					fileNameField.setText(fileChooser.getSelectedFile().getName().split("[.]LogicSave")[0]);
					fileNameFiledListenerFlag = true;
				}
				checkFileReadStatus();
			}
		});
		super.diaLog.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				focusSession.setName(titleField.getText());
				focusSession.setDescription(descriptionArea.getText());
			}
		});
		super.diaLog.add(this.dftSave);
		super.diaLog.add(this.cloudSave);
		super.diaLog.add(this.readOnly);
		super.diaLog.add(this.directoryLabel);
		super.diaLog.add(this.fileNameLabel);
		super.diaLog.add(this.fileNameField);
		super.diaLog.add(this.removeButton);
		super.diaLog.add(this.saveButton);
		super.diaLog.add(this.titleField);
		super.diaLog.add(this.descriptionArea);
		
		this.fileNameField.getDocument().addDocumentListener(new DocumentListener()
		{
			@Override
			public void changedUpdate(DocumentEvent arg0)
			{
				this.check();
			}
			@Override
			public void insertUpdate(DocumentEvent arg0)
			{
				this.check();
			}
			@Override
			public void removeUpdate(DocumentEvent arg0)
			{
				this.check();
			}
			private void check()
			{
				if(fileNameFiledListenerFlag)
				{
					checkFileReadStatus();
				}
			}
		});
		
		super.setFont(super.diaLog.getComponents());
	}
	private void checkFileReadStatus()
	{
		File file = fileChooser.getSelectedFile();
		File focusDir = fileChooser.getCurrentDirectory();
		this.directoryLabel.setText("경로: " + fileChooser.getCurrentDirectory().toString());
		if(file != null && file.isDirectory())
		{
			focusDir = file;
			this.directoryLabel.setText("경로: " + file.toString());
			
		}
		this.selectFile = new File(focusDir.toString() + "\\" + fileNameField.getText() + ".LogicSave");
		
		if(super.selectFile.isFile())
		{
			this.removeButton.setEnabled(true);
			this.fileSelectorListenerFlag = false;
			super.fileChooser.setSelectedFile(this.selectFile);
			this.fileSelectorListenerFlag = true;
		}
		else
		{
			this.removeButton.setEnabled(false);
		}
		
		if(fileNameField.getText().length() <= 0)
		{
			this.readOnly.setEnabled(true);
			this.readOnly.setSelected(false);
			this.saveButton.setEnabled(false);
		}
		else if(super.selectFile.isFile() && !super.selectFile.canWrite())
		{
			this.readOnly.setEnabled(false);
			this.readOnly.setSelected(true);
			this.saveButton.setEnabled(false);
		}
		else
		{
			this.readOnly.setEnabled(true);
			this.readOnly.setSelected(false);
			this.saveButton.setEnabled(true);
		}
	}
	@Override
	void active(Session session)
	{
		super.active(session);
		if(session.getFileLocation() != null)
		{
			this.fileNameField.setText(session.getFileLocation().getName().split("[.]LogicSave")[0]);
		}
		this.titleField.setText(super.focusSession.getName());
		this.descriptionArea.setText(super.focusSession.getDescription());
	}
}
class FileLoadPanel extends FileManagerWindow
{
	private JPanel buttonPanel;
	private ButtonGroup grp;
	private JRadioButton dftLoad;
	private JRadioButton innerLoad;
	private JRadioButton cloudLoad;
	private JCheckBox tempLoad;
	private JCheckBox newSessionOpen;
	private JButton selectButton;
	
	private JPanel descriptionPane;
	private JLabel descriptionTitle;
	private JTextArea descriptionText;
	
	FileLoadPanel(LogicCore core)
	{
		super(core);
		
		super.diaLog.setTitle("불러오기");
		
		super.fileChooser.addPropertyChangeListener(new PropertyChangeListener()
		{
			@Override
			public void propertyChange(PropertyChangeEvent arg0)
			{
				if(fileChooser.getSelectedFile() != null && fileChooser.getSelectedFile().isFile())
				{
					selectButton.setEnabled(true);
					selectFile = fileChooser.getSelectedFile();
					showFileDescription();
				}
				else
				{
					selectButton.setEnabled(false);
					deShowFileDescription();
				}
			}
		});
		
		this.descriptionPane = new JPanel();
		this.descriptionPane.setLayout(null);
		this.descriptionPane.setBounds(500, 0, 210, 300);
		this.descriptionTitle = new JLabel("제목테스트");
		this.descriptionTitle.setBounds(0, 5, 210, 20);
		this.descriptionTitle.setHorizontalAlignment(SwingConstants.CENTER);
		this.descriptionText = new JTextArea();
		this.descriptionText.setBounds(5, 30, 205, 235);
		this.descriptionText.setBorder(new EtchedBorder(EtchedBorder.RAISED));
		this.descriptionText.setEditable(false);
		this.descriptionText.setLineWrap(true);
		this.descriptionPane.add(this.descriptionTitle);
		this.descriptionPane.add(this.descriptionText);
		
		this.buttonPanel = new JPanel();
		this.buttonPanel.setLayout(null);
		this.buttonPanel.setBounds(0, 220, 500, 80);
		
		this.grp = new ButtonGroup();
		this.dftLoad = new JRadioButton("로컬");
		this.dftLoad.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				setDFTFileView();
			}
		});
		this.dftLoad.setBounds(10, 5, 100, 20);
		this.innerLoad = new JRadioButton("기본 제공");
		this.innerLoad.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				setInnerFileView();
			}
		});
		this.innerLoad.setBounds(10, 25, 100, 20);
		this.cloudLoad = new JRadioButton("클라우드");
		this.cloudLoad.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				setCloudFileView();
			}
		});
		this.cloudLoad.setBounds(110, 5, 100, 20);
		this.grp.add(this.dftLoad);
		this.grp.add(this.innerLoad);
		this.grp.add(this.cloudLoad);
		this.dftLoad.setSelected(true);
		this.tempLoad = new JCheckBox("템플릿만 로드");
		this.tempLoad.setBounds(240, 5, 120, 20);
		this.newSessionOpen = new JCheckBox("새 세션 생성");
		this.newSessionOpen.setSelected(true);
		this.newSessionOpen.setBounds(240, 25, 120, 20);
		this.selectButton = new JButton("선택");
		this.selectButton.setBounds(380, 10, 100, 30);
		this.selectButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				if(selectFile != null)
				{
					if(newSessionOpen.isSelected())
					{
						core.getSession().createSession();
					}
					Session focus = core.getSession().getFocusSession();
					if(tempLoad.isSelected())
					{
						focus.getTemplateManager().addTemplates(selectFile);
					}
					else
					{
						focus.LoadData(selectFile);
					}
				}
			}
		});
		this.buttonPanel.add(this.dftLoad);
		this.buttonPanel.add(this.innerLoad);
		this.buttonPanel.add(this.cloudLoad);
		this.buttonPanel.add(this.tempLoad);
		this.buttonPanel.add(this.newSessionOpen);
		this.buttonPanel.add(this.selectButton);
		
		super.diaLog.add(this.buttonPanel);
		super.diaLog.add(this.descriptionPane);
		super.setDFTFileView();
		super.setFont(super.diaLog.getComponents());
	}
	private void showFileDescription()
	{
		Iterator<String> description = super.getDescription(super.selectFile).iterator();
		if(description.hasNext())
		{
			this.descriptionTitle.setText(description.next());
		}
		String temp = "";
		while(description.hasNext())
		{
			temp += description.next() + "\n";
		}
		this.descriptionText.setText(temp);
		this.diaLog.setSize(720, 300);
	}
	private void deShowFileDescription()
	{
		this.diaLog.setSize(500, 300);
	}
	@Override
	protected void selectFile()
	{
		this.selectButton.setEnabled(true);
	}
	@Override
	protected void active(Session session)
	{
		super.active(session);
		if(super.selectFile != null && super.selectFile.isFile())
		{
			this.showFileDescription();
		}
	}
}
class FileManagerWindow
{
	protected Session focusSession;
	protected JDialog diaLog;
	protected JPanel fileSelecterPanel;
	protected LogicCore core;
	
	protected File selectFile;
	
	protected JFileChooser fileChooser;
	
	private JScrollPane innerSelectScroll;
	private JPanel innerSelectPanel;
	private ArrayList<InnerSelectMember> innerSelects = new ArrayList<InnerSelectMember>();
	private InnerSelectMember focusMember;
	
	private JPanel cloudLoaderPanel;
	
	FileManagerWindow(LogicCore core)
	{
		this.core = core;
		
		this.diaLog = new JDialog();
		this.diaLog.setLayout(null);
		this.diaLog.setResizable(false);
		this.diaLog.setAlwaysOnTop(true);
		this.diaLog.setSize(500, 300);
		
		this.fileSelecterPanel = new JPanel();
		this.fileSelecterPanel.setLayout(null);
		this.fileSelecterPanel.setBounds(0, 0, 500, 220);
		
		this.fileChooser = new JFileChooser();
		this.fileChooser.setFont(LogicCore.RES.NORMAL_FONT.deriveFont(14F));
		this.fileChooser.setBounds(0, 0, 500, 255);
		this.fileChooser.setCurrentDirectory(new File(LogicCore.JARLOC));
		this.fileChooser.setFileFilter(new FileNameExtensionFilter("LogicSave", "LogicSave"));
		this.fileChooser.setControlButtonsAreShown(false);
		this.fileChooser.setAcceptAllFileFilterUsed(false);
		try
		{//http://stackoverflow.com/questions/4167542/how-to-disable-file-input-field-in-jfilechooser
			MetalFileChooserUI ui = (MetalFileChooserUI)fileChooser.getUI();
			Field field;
			field = MetalFileChooserUI.class.getDeclaredField("fileNameTextField");
			field.setAccessible(true);
			JTextField tf = (JTextField) field.get(ui);
			tf.setEditable(false);
		}
		catch(NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e1)
		{
			e1.printStackTrace();
		}
		this.innerSelectPanel = new JPanel();
		this.innerSelectPanel.setLayout(null);
		this.innerSelectScroll = new JScrollPane();
		this.innerSelectScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		this.innerSelectScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		this.innerSelectScroll.setBounds(0, 0, 495, 220);
		this.innerSelectScroll.getVerticalScrollBar().setUnitIncrement(10);
		
		this.cloudLoaderPanel = new JPanel();
		this.cloudLoaderPanel.setBounds(0, 0, 500, 220);
		this.cloudLoaderPanel.setBackground(Color.green);
		
		this.diaLog.add(this.fileSelecterPanel);
		this.setDFTFileView();
	}
	void active(Session session)
	{
		this.focusSession = session;
		JFrame mainFrame = this.core.getUI().getFrame();
		this.diaLog.setLocation(mainFrame.getX() + (mainFrame.getWidth() / 2) - (this.diaLog.getWidth() / 2)
				, mainFrame.getY() + (mainFrame.getHeight() / 2) - (this.diaLog.getHeight() / 2));
		this.fileChooser.rescanCurrentDirectory();
		this.diaLog.setVisible(true);
	}
	void disActive()
	{
		this.diaLog.setVisible(false);
	}
	protected void setDFTFileView()
	{
		this.resetFileSelector();
		this.fileSelecterPanel.add(this.fileChooser);
	}
	protected void setInnerFileView()
	{
		this.resetFileSelector();
		this.fileSelecterPanel.add(this.innerSelectScroll);
		this.innerSelects = new ArrayList<InnerSelectMember>();
		this.innerSelectPanel.removeAll();
		
		ArrayList<String> innerDirList = LogicCore.getResource().getFileList("template");
		for(String dir : innerDirList)
		{
			InnerSelectMember member = new InnerSelectMember(LogicCore.getResource().getFile("/template/" + dir));
			member.setLocation(2, (this.innerSelects.size() - 1) * 53 + 2);
			this.innerSelectPanel.add(member);
		}
		this.innerSelectPanel.setPreferredSize(new Dimension(500, this.innerSelects.size() * 53 + 2));
		this.innerSelectScroll.getViewport().setView(this.innerSelectPanel);
	}
	protected void setCloudFileView()
	{
		this.resetFileSelector();
		this.fileSelecterPanel.add(this.cloudLoaderPanel);
	}
	protected void resetFileSelector()
	{
		this.fileChooser.setSelectedFile(null);
		this.fileSelecterPanel.removeAll();
		this.fileSelecterPanel.repaint();
	}
	protected void setFont(Component[] comp)
	{
		for(Component c : comp)
		{
			if(c instanceof Container)
			{
				this.setFont(((Container)c).getComponents());
				try
				{
					c.setFont(LogicCore.RES.NORMAL_FONT.deriveFont(14F));
				}
				catch(Exception e){}
			}
		}
	}
	protected ArrayList<String> getDescription(File file)
	{
		ArrayList<String> dataList = new ArrayList<String>();
		if(file != null)
		{
			try
			{
				BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
				String line = reader.readLine();
				while(!line.contains("}"))
				{
					switch(line.replace("\t", "").split("=")[0])
					{
					case "name":
						dataList.add(line.split("=")[1]);
						break;
					case "Description":
						line = reader.readLine();
						while(!line.contains("}"))
						{
							if(line.split("=").length > 1)
							{
								dataList.add(line.split("=")[1]);
							}
							
							line = reader.readLine();
						}
						break;
					}
					line = reader.readLine();
				}
				reader.close();
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
		}
		return dataList;
	}
	protected void selectFile()
	{
		
	}
	private class InnerSelectMember extends ButtonPanel
	{
		private static final long serialVersionUID = 1L;
		private File LoadFile;
		private JTextArea descriptionArea;
		
		InnerSelectMember(File file)
		{
			innerSelects.add(this);
			this.LoadFile = file;
			this.setSize(473, 51);
			
			this.descriptionArea = new JTextArea();
			this.descriptionArea.setFont(LogicCore.RES.NORMAL_FONT.deriveFont(13F));
			Iterator<String> description = getDescription(this.LoadFile).iterator();
			String temp = "";
			if(description.hasNext())
			{
				temp = "[" + description.next() + "] ";
			}
			while(description.hasNext())
			{
				temp += description.next() + " ";
			}
			this.descriptionArea.setText(temp);
			this.descriptionArea.setBounds(5, 5, 463, 41);
			this.descriptionArea.setBackground(new Color(0, 0, 0, 0));
			this.descriptionArea.setLineWrap(true);
			this.descriptionArea.updateUI();
			for(MouseListener l : this.descriptionArea.getMouseListeners())
			{
				this.descriptionArea.removeMouseListener(l);
				System.out.println("remove");
			}
			for(MouseMotionListener l : this.descriptionArea.getMouseMotionListeners())
			{
				this.descriptionArea.removeMouseMotionListener(l);
			}
			this.descriptionArea.addMouseListener(this);
			this.descriptionArea.addMouseMotionListener(this);
			
			this.deSelectFocus();
			
			super.setLayout(null);
			super.add(this.descriptionArea);
		}
		void selectFocus()
		{
			this.setBasicImage(LogicCore.getResource().getImage("INNER_LOAD_FOCUS"));
			this.setBasicPressImage(LogicCore.getResource().getImage("INNER_LOAD_FOCUS_PUSH"));
			this.setOnMouseImage(LogicCore.getResource().getImage("INNER_LOAD_FOCUS_SELECT"));
		}
		void deSelectFocus()
		{
			this.setBasicImage(LogicCore.getResource().getImage("INNER_LOAD_DFT"));
			this.setBasicPressImage(LogicCore.getResource().getImage("INNER_LOAD_PUSH"));
			this.setOnMouseImage(LogicCore.getResource().getImage("INNER_LOAD_SELECT"));
		}
		@Override
		void pressed(int button)
		{
			if(focusMember != null)
			{
				focusMember.deSelectFocus();
			}
			selectFile();
			focusMember = this;
			selectFile = this.LoadFile;
			this.selectFocus();
		}
	}
}
enum Size
{
	SMALL(1, "SMALL", "SMALL", "MIDDLE"), MIDDLE(2, "MIDDLE", "SMALL", "BIG"), BIG(4, "BIG", "MIDDLE", "BIG");
	public static final int REGULAR_SIZE = 32;
	public static final int MARGIN = 50;
	
	public final int multiple;
	public final String tag;
	
	private final String smallSize;
	private final String bigSize;
	
	private Size(int multiple, String tag, String smallSize, String bigSize)
	{
		this.smallSize = smallSize;
		this.bigSize = bigSize;
		this.multiple = multiple;
		this.tag = tag;
	}
	public int getmultiple()
	{
		return multiple;
	}
	public int getWidth()
	{
		return REGULAR_SIZE * multiple;
	}
	public String getTag()
	{
		return this.tag;
	}
	public Size getSmallSize()
	{
		return Size.valueOf(this.smallSize);
	}
	public Size getBigSize()
	{
		return Size.valueOf(this.bigSize);
	}
}
interface SizeUpdate
{
	void sizeUpdate();
}
interface LogicUIComponent
{
	Component getComponent();
}