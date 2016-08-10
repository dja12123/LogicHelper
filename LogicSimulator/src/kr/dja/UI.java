package kr.dja;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import kr.dja.Grid.AND;
import kr.dja.Grid.GridMember;

public class UI
{
	private JFrame mainFrame;
	//JLayeredPane layerPane;
	private ToolBar toolBar;
	private UnderBar underBar;
	private ControlPane controlPane;
	private Grid grid;
	private TaskManager taskManager;
	private TaskOperator taskOperator;
	private JLayeredPane mainLayeredPane;
	private TrackedPane trackedPane;
	private JPanel glassPane;
	final Color innerUIObjectColor = new Color(220, 220, 220);
	UI()
	{
		this.mainFrame = new JFrame("논리회로 시뮬레이터");
		this.mainFrame.setSize(1600, 900);
		this.mainFrame.setMinimumSize(new Dimension(1131, 800));
		this.mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		this.mainLayeredPane = this.mainFrame.getLayeredPane();
		
		this.glassPane = (JPanel)this.mainFrame.getGlassPane();
		this.glassPane.addMouseMotionListener(new MouseMotionListener()
		{
			@Override
			public void mouseDragged(MouseEvent e)
			{
			}
			@Override
			public void mouseMoved(MouseEvent e)
			{
				trackedPane.setLocation(e.getX() - (trackedPane.getWidth() / 2), e.getY() - (trackedPane.getHeight() / 2));
			}
		});
		this.glassPane.addMouseListener(new MouseListener()
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
					Component pointComp = mainFrame.getContentPane().getComponentAt(e.getX(), e.getY());
					Component pointComp1 = pointComp.getComponentAt((int)(e.getX() - pointComp.getLocation().getX()), (int)(e.getY() - pointComp.getLocation().getY()));
					int compX = (int)(e.getX() - pointComp.getLocation().getX() - pointComp1.getLocation().getX());
					int compY = (int)(e.getY() - pointComp.getLocation().getY() - pointComp1.getLocation().getY());
					Component pointComp2 = pointComp1.getComponentAt(compX, compY);
					Component panelComp = pointComp2.getComponentAt(compX, compY);
					if(panelComp == grid.getGridPanel())
					{
						Point loc = grid.getGridScrollPanel().getViewport().getViewPosition().getLocation();
						if(compX + loc.getX() <= grid.getGridPanel().getWidth() - Size.MARGIN && compY + loc.getY() <= grid.getGridPanel().getHeight() - Size.MARGIN && compX + loc.getX() >= Size.MARGIN && compY + loc.getY() >= Size.MARGIN)
						{
							//System.out.println(((int)(() / grid.getUISize().getWidth()) - grid.getNegativeExtendX()) + " " +  ((int)(() / grid.getUISize().getWidth()) - grid.getNegativeExtendY()));
							trackedPane.addMemberOnGrid((int)((compX + loc.getX()) - Size.MARGIN) / grid.getUISize().getmultiple(), (int)((compY + loc.getY()) - Size.MARGIN) / grid.getUISize().getmultiple());
							removeTrackedPane(trackedPane);
						}
					}
				}
			}
			@Override
			public void mouseEntered(MouseEvent e)
			{
			}
			@Override
			public void mouseExited(MouseEvent e)
			{
			}
			@Override
			public void mousePressed(MouseEvent e)
			{
			}
			@Override
			public void mouseReleased(MouseEvent e)
			{
			}
			
		});
		
		this.toolBar = new ToolBar();
		this.underBar = new UnderBar();
		//layerPane = mainFrame.getLayeredPane();
		

		
		this.grid = new Grid(this);
		
		this.taskManager = new TaskManager();
		
		//this.taskOperator = new TaskOperator(this.grid);
		
		this.controlPane = new ControlPane();
		this.controlPane.setPreferredSize(new Dimension(400, 0));
		

		//layerPane.add(controlPane, new Integer(2));
		
		
		
		//mainFrame.addComponentListener(new ResizeListener());
		this.mainFrame.add(this.grid.getGridScrollPanel(), BorderLayout.CENTER);
		this.mainFrame.add(this.toolBar, BorderLayout.NORTH);
		this.mainFrame.add(this.underBar, BorderLayout.SOUTH);
		this.mainFrame.add(this.controlPane, BorderLayout.EAST);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		this.mainFrame.setLocation(screenSize.width/2-mainFrame.getSize().width/2, screenSize.height/2-mainFrame.getSize().height/2);
		/*this.mainFrame.getRootPane().addMouseMotionListener(new MouseMotionListener()
		{
			@Override
			public void mouseDragged(MouseEvent arg0)
			{
				
			}
			@Override
			public void mouseMoved(MouseEvent arg0)
			{
				System.out.println("asd");
				if(trackedPane != null)
					trackedPane.setLocation(arg0.getX() - (trackedPane.getWidth() / 2), arg0.getY() - (trackedPane.getHeight() / 2));
				
			}
		});*/
		this.mainFrame.setVisible(true);
		/*long eventMask = AWTEvent.MOUSE_MOTION_EVENT_MASK + AWTEvent.MOUSE_EVENT_MASK;
		Toolkit.getDefaultToolkit().addAWTEventListener( new AWTEventListener()
		{
		    public void eventDispatched(AWTEvent e)
		    {
		    	
		    	if(e.paramString().contains("MOUSE_MOVED"))
		    	{
		    		//System.out.print(Integer.parseInt(e.paramString().split(",")[1].replace("(", "")));
		    		//System.out.println(Integer.parseInt(e.paramString().split(",")[2].replace(")", "")));
		    		System.out.println(e);
		    	}
		    }
		}, eventMask);*/


	}
	TaskManager getTaskManager()
	{
		return this.taskManager;
	}
	UnderBar getUnderBar()
	{
		return underBar;
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
	class ControlPane extends JPanel
	{
		private static final long serialVersionUID = 1L;
		private JPanel controlAreaPanel;
		
		private TaskOperatorPanel taskOperatorPanel;
		private PalettePanel palettePanel;
		private InfoPanel infoPanel;
		private BlockControlPanel blockControlPanel;
		
		private JPanel resizablePanel;
		private TemplatePanel templatePanel;
		private TaskManagerPanel taskManagerPanel;
		
		ControlPane()
		{
			super();
			
			this.setLayout(new BorderLayout());
			this.controlAreaPanel = new JPanel();
			this.controlAreaPanel.setLayout(null);
			this.controlAreaPanel.setPreferredSize(new Dimension(0, 475));
			
			this.taskOperatorPanel = new TaskOperatorPanel();
			this.palettePanel = new PalettePanel();
			this.infoPanel = new InfoPanel();
			this.blockControlPanel = new BlockControlPanel();

			this.controlAreaPanel.add(taskOperatorPanel);
			this.controlAreaPanel.add(palettePanel);
			this.controlAreaPanel.add(infoPanel);
			this.controlAreaPanel.add(blockControlPanel);
			
			this.resizablePanel = new JPanel();
			this.resizablePanel.setLayout(new BoxLayout(this.resizablePanel, BoxLayout.PAGE_AXIS));
			this.resizablePanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
			
			this.templatePanel = new TemplatePanel();
			
			this.taskManagerPanel = new TaskManagerPanel();
			
			this.resizablePanel.add(templatePanel);
			this.resizablePanel.add(taskManagerPanel);
			
			this.add(resizablePanel, BorderLayout.CENTER);
			this.add(controlAreaPanel, BorderLayout.NORTH);
			
			
		}
		private class BlockControlPanel extends JPanel
		{
			private static final long serialVersionUID = 1L;
			private JPanel detailViewPanel;
			private JPanel detailControlPanel;

			BlockControlPanel()
			{
				super();
				
				this.setLayout(null);
				this.setBounds(5, 5, 390, 150);
				this.setBorder(new PanelBorder("블록 세부 편집"));
				
				this.detailViewPanel = new JPanel();
				this.detailViewPanel.setLayout(null);
				this.detailViewPanel.setBounds(8, 21, 120, 120);
				this.detailViewPanel.setBorder(new EtchedBorder(EtchedBorder.RAISED));
				this.detailViewPanel.setBackground(new Color(200, 220, 250));
				
				this.detailControlPanel = new JPanel();
				this.detailControlPanel.setLayout(null);
				this.detailControlPanel.setBounds(133, 21, 248, 120);
				this.detailControlPanel.setBorder(new EtchedBorder(EtchedBorder.RAISED));
				
				this.add(detailViewPanel);
				this.add(detailControlPanel);
			}
		}
		private class InfoPanel extends JPanel
		{
			private static final long serialVersionUID = 1L;
			private JTextArea explanationArea;
			
			InfoPanel()
			{
				super();
				
				this.setLayout(null);
				this.setBounds(5, 155, 390, 140);
				this.setBorder(new PanelBorder("정보"));
				
				this.explanationArea = new JTextArea();
				this.explanationArea.setBounds(8, 20, 373, 111);
				this.explanationArea.setEditable(false);
				this.explanationArea.setText("정보 패널");
				this.explanationArea.setFont(LogicCore.RES.NORMAL_FONT.deriveFont(14.0f));
				
				
				this.add(explanationArea);
				
			}
		}
		private class PalettePanel extends JPanel
		{
			private static final long serialVersionUID = 1L;
			private List<JButton> paletteMember = new ArrayList<JButton>();
			
			PalettePanel()
			{
				super();
				
				this.setLayout(new GridLayout(4, 5, 3, 3));
				this.setBounds(5, 295, 225, 180);
				this.setBorder(new PanelBorder("팔레트"));
				
				//this.add(new Palette_AND_Gate().getPalettePanel());
				paletteMember.add(new PaletteMember(grid.new AND()));
				paletteMember.add(new JButton());
				paletteMember.add(new JButton());
				paletteMember.add(new JButton());
				paletteMember.add(new JButton());
				paletteMember.add(new JButton());
				paletteMember.add(new JButton());
				paletteMember.add(new JButton());
				paletteMember.add(new JButton());
				paletteMember.add(new JButton());
				paletteMember.add(new JButton());
				paletteMember.add(new JButton());
				paletteMember.add(new JButton());
				paletteMember.add(new JButton());
				paletteMember.add(new JButton());
				paletteMember.add(new JButton());
				paletteMember.add(new JButton());
				Iterator<JButton> itr = paletteMember.iterator();
				while(itr.hasNext())
				{
					this.add(itr.next());
				}
			}
			class PaletteMember extends JButton
			{
				GridMember putMember;
				PaletteMember(GridMember member)
				{
					this.putMember = member;
					this.addActionListener(new ActionListener()
					{
						@Override
						public void actionPerformed(ActionEvent e)
						{
							AND and = grid.new AND();
							ArrayList<GridMember> list = new ArrayList<GridMember>();
							list.add(and);
							addTrackedPane(new TrackedPane(list));
						}
					});
				}

			}
		}
		private class TaskOperatorPanel extends JPanel
		{
			private static final long serialVersionUID = 1L;
			
			private JPanel graphPanel;
			private UIButton pauseButton;
			private UIButton subTime1Button;
			private UIButton subTime10Button;
			private UIButton subTime100Button;
			private UIButton addTime1Button;
			private UIButton addTime10Button;
			private UIButton addTime100Button;
			private JLabel taskIntervalLabel;
			private JLabel msLabel;
			
			TaskOperatorPanel()
			{
				super();
				
				this.setLayout(null);
				this.setBounds(230, 295, 165, 180);
				this.setBorder(new PanelBorder("연산"));
				
				//this.setBackground(innerUIObjectColor);
				this.graphPanel = new JPanel();
				this.graphPanel.setBounds(8, 20, 148, 90);
				this.graphPanel.setBorder(new EtchedBorder(EtchedBorder.RAISED));
				
				this.pauseButton = new UIButton(8, 145, 148, 25, null, null);
				this.subTime1Button = new UIButton(42, 120, 15, 20, null, null);
				this.subTime10Button = new UIButton(25, 120, 15, 20, null, null);
				this.subTime100Button = new UIButton(8, 120, 15, 20, null, null);
				this.addTime1Button = new UIButton(107, 120, 15, 20, null, null);
				this.addTime10Button = new UIButton(124, 120, 15, 20, null, null);
				this.addTime100Button = new UIButton(141, 120, 15, 20, null, null);
				
				this.taskIntervalLabel = new JLabel("200");
				this.taskIntervalLabel.setHorizontalAlignment(JLabel.RIGHT);
				this.taskIntervalLabel.setBounds(56, 120, 30, 20);
				
				this.msLabel = new JLabel("ms");
				this.msLabel.setBounds(87, 120, 30, 20);
				
				this.add(this.graphPanel);
				this.add(this.pauseButton);
				this.add(this.subTime1Button);
				this.add(this.subTime10Button);
				this.add(this.subTime100Button);
				this.add(this.addTime1Button);
				this.add(this.addTime10Button);
				this.add(this.addTime100Button);
				this.add(this.taskIntervalLabel);
				this.add(this.msLabel);
			}
		}
	}
	private class TemplatePanel extends JPanel
	{
		private static final long serialVersionUID = 1L;
		
		private JPanel templateAreaPanel;
		private JPanel buttonAreaPanel;
		private JPanel clipBoardPanel;
		private JScrollPane templateScrollPane;
		private UIButton addTemplateButton;
		private UIButton putGridButton;
		private UIButton removeTemplateButton;

		TemplatePanel()
		{
			super();
			
			this.setLayout(new BorderLayout());
			this.setBorder(new PanelBorder("템플릿"));
			
			this.templateAreaPanel = new JPanel();
			this.templateAreaPanel.setLayout(new BorderLayout());
			
			this.buttonAreaPanel = new JPanel();
			this.buttonAreaPanel.setPreferredSize(new Dimension(30, 0));
			this.buttonAreaPanel.setLayout(null);
			
			this.addTemplateButton = new UIButton(2, 4, 26, 26, null, null);
			this.putGridButton = new UIButton(2, 38, 26, 26, null, null);
			this.removeTemplateButton = new UIButton(2, 68, 26, 26, null, null);
			
			this.buttonAreaPanel.add(this.addTemplateButton);
			this.buttonAreaPanel.add(this.putGridButton);
			this.buttonAreaPanel.add(this.removeTemplateButton);
			
			this.clipBoardPanel = new JPanel();
			this.clipBoardPanel.setPreferredSize(new Dimension(0, 34));
			this.clipBoardPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
			
			this.templateScrollPane = new JScrollPane();
			this.templateScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			this.templateScrollPane.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
			
			this.templateAreaPanel.add(this.templateScrollPane, BorderLayout.CENTER);
			this.templateAreaPanel.add(this.clipBoardPanel, BorderLayout.NORTH);
			
			this.add(this.templateAreaPanel, BorderLayout.CENTER);
			this.add(this.buttonAreaPanel, BorderLayout.EAST);
			
		}
	}
	private class TaskManagerPanel extends JPanel
	{
		private static final long serialVersionUID = 1L;
		
		private JScrollPane taskScrollPane;
		private JPanel buttonAreaPanel;
		private UIButton TaskUndoButton;
		private UIButton TaskRedoButton;
		
		TaskManagerPanel()
		{
			super();
			
			this.setLayout(new BorderLayout());
			this.setBorder(new PanelBorder("작업"));
			
			this.taskScrollPane = new JScrollPane();
			this.taskScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			this.taskScrollPane.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
			
			this.buttonAreaPanel = new JPanel();
			this.buttonAreaPanel.setPreferredSize(new Dimension(30, 0));
			this.buttonAreaPanel.setLayout(null);
			
			this.TaskUndoButton = new UIButton(2, 4, 26, 26, null, null);
			this.TaskRedoButton = new UIButton(2, 34, 26, 26, null, null);
			
			this.buttonAreaPanel.add(this.TaskUndoButton);
			this.buttonAreaPanel.add(this.TaskRedoButton);
			
			this.add(this.taskScrollPane, BorderLayout.CENTER);
			this.add(this.buttonAreaPanel, BorderLayout.EAST);
			
		}
	}
	class ToolBar extends JToolBar
	{
		private static final long serialVersionUID = 1L;
		private JPanel leftSidePanel;
		private JPanel toolBarPanel;
		private JPanel rightSidePanel;
		private JLabel titleLabel;
		private UIButton saveButton;
		private UIButton optionSaveButton;
		private UIButton loadButton;
		private UIButton createNewfileButton;
		private UIButton sizeUpButton;
		private UIButton sizeDownButton;
		private UIButton setViewButton;
		private UIButton consolButton;
		private UIButton helpButton;
		private UIButton newInstanceButton;
		private UIButton controlOpenButton;
		
		ToolBar()
		{
			super();
			
			this.setPreferredSize(new Dimension(0, 30));
			this.setFloatable(false);
			
			this.toolBarPanel = new JPanel();
			this.toolBarPanel.setLayout(new BorderLayout());
			this.toolBarPanel.setBackground(new Color(255, 0, 0, 0));
			
			this.rightSidePanel = new JPanel();
			this.rightSidePanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
			this.rightSidePanel.setBackground(new Color(255, 0, 0, 0));
			this.rightSidePanel.setPreferredSize(new Dimension(300, 0));
			
			this.leftSidePanel = new JPanel();
			this.leftSidePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
			this.leftSidePanel.setBackground(new Color(255, 0, 0, 0));
			this.leftSidePanel.setPreferredSize(new Dimension(300, 0));
			
			this.titleLabel = new JLabel("FileName");
			this.titleLabel.setHorizontalAlignment(JLabel.CENTER);
			
			this.saveButton = new UIButton(20, 20, null, null);
			this.optionSaveButton = new UIButton(20, 20, null, null);
			this.loadButton = new UIButton(20, 20, null, null);
			this.createNewfileButton = new UIButton(20, 20, null, null);
			this.sizeUpButton = new UIButton(20, 20, null, null);
			this.sizeDownButton = new UIButton(20, 20, null, null);
			this.setViewButton = new UIButton(20, 20, null, null);
			
			this.consolButton = new UIButton(20, 20, null, null);
			this.helpButton = new UIButton(20, 20, null, null);
			this.newInstanceButton = new UIButton(20, 20, null, null);
			this.controlOpenButton = new UIButton(20, 20, null, null);
			
			this.leftSidePanel.add(this.saveButton);
			this.leftSidePanel.add(this.optionSaveButton);
			this.leftSidePanel.add(this.loadButton);
			this.leftSidePanel.add(this.createNewfileButton);
			this.leftSidePanel.add(Box.createRigidArea(new Dimension(10, 0)));
			this.leftSidePanel.add(this.sizeDownButton);
			this.leftSidePanel.add(this.sizeUpButton);
			this.leftSidePanel.add(Box.createRigidArea(new Dimension(10, 0)));
			this.leftSidePanel.add(this.setViewButton);
			
			this.rightSidePanel.add(this.helpButton);
			this.rightSidePanel.add(this.consolButton);
			this.rightSidePanel.add(this.controlOpenButton);
			this.rightSidePanel.add(this.newInstanceButton);
			
			this.toolBarPanel.add(this.leftSidePanel, BorderLayout.WEST);
			this.toolBarPanel.add(this.titleLabel, BorderLayout.CENTER);
			this.toolBarPanel.add(this.rightSidePanel, BorderLayout.EAST);
			
			this.add(toolBarPanel);
		}
	}
	class UnderBar extends JToolBar
	{
		private static final long serialVersionUID = 1L;
		private JPanel leftSidePanel;
		private JPanel centerSidePanel;
		private JPanel rightSidePanel;
		private JLabel sizeLabel;
		
		UnderBar()
		{
			super();
			this.setPreferredSize(new Dimension(0, 25));
			this.setFloatable(false);
			this.setLayout(new BorderLayout());
			
			this.leftSidePanel = new JPanel();
			this.leftSidePanel.setPreferredSize(new Dimension(300, 0));
			this.leftSidePanel.setBackground(new Color(255, 0, 0, 0));
			this.leftSidePanel.setLayout(null);
			
			this.centerSidePanel = new JPanel();
			this.centerSidePanel.setBackground(new Color(255, 0, 0, 0));
			
			this.rightSidePanel = new JPanel();
			this.rightSidePanel.setPreferredSize(new Dimension(300, 0));
			this.rightSidePanel.setBackground(new Color(255, 0, 0, 0));
			this.rightSidePanel.setLayout(null);
			
			this.sizeLabel = new JLabel();
			this.sizeLabel.setBounds(5, 0, 120, 22);
			this.sizeLabel.setFont(LogicCore.RES.BAR_FONT.deriveFont(14f));
			
			this.leftSidePanel.add(this.sizeLabel);
			
			this.add(centerSidePanel, BorderLayout.CENTER);
			this.add(leftSidePanel, BorderLayout.WEST);
			this.add(rightSidePanel, BorderLayout.EAST);
		}
		void setGridSizeInfo(int x, int y)
		{
			this.sizeLabel.setText("X: " + x + "  Y: " + y);
			this.repaint();
		}
	}
	class TrackedPane extends JPanel implements SizeUpdate
	{
		private static final long serialVersionUID = 1L;
		private List<GridMember> members;
		private int maxX, maxY, minX, minY;
		TrackedPane(List<GridMember> members)
		{
			this.members = members;
			this.setBackground(new Color(0, 255, 0, 0));
			this.sizeUpdate();
		}
		@Override
		public void paint(Graphics g)
		{
			BufferedImage img;
			for(GridMember member : members)
			{
				img = member.getSnapShot();
				g.drawImage(img, (member.getUIabsLocationX() - minX) * grid.getUISize().getmultiple(), (member.getUIabsLocationY() - minY) * grid.getUISize().getmultiple(), this);
			}
			super.paint(g);
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
			this.setSize((this.maxX - this.minX) * grid.getUISize().getmultiple(), (this.maxY - this.minY) * grid.getUISize().getmultiple());
			this.repaint();

		}
		void addMemberOnGrid(int absX, int absY)
		{
			int stdX = absX - ((this.getWidth() / 2) / grid.getUISize().getmultiple()) - (grid.getNegativeExtendX() * Size.REGULAR_SIZE);
			int stdY = absY - ((this.getHeight() / 2) / grid.getUISize().getmultiple()) - (grid.getNegativeExtendY() * Size.REGULAR_SIZE);
			System.out.println("nSize" + grid.getNegativeExtendX() + " " + grid.getNegativeExtendY());
			
			for(GridMember member : members)
			{
				member.put(stdX - this.minX + member.getUIabsLocationX(), stdY - this.minY + member.getUIabsLocationY());
			}
			this.removeAll();
		}
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


/*class Palette_AND_Gate extends PaletteMember
{

	@Overridere
	void Action()
	{
		System.out.println("Action");
	}
	
}*/


interface SizeUpdate
{
	void sizeUpdate();
}