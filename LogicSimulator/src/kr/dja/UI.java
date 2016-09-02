package kr.dja;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

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
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

public class UI
{
	private LogicCore core;
	
	private Size UI_Size;
	
	private ToolBar toolBar;
	private UnderBar underBar;
	private TaskOperatorPanel taskOperatorPanel;
	private PalettePanel palettePanel;
	private InfoPanel infoPanel;
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
		this.toolBar = new ToolBar();
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
				System.out.println("close");
				LogicCore.removeInstance(core);
			}
		});

		this.taskOperatorPanel = new TaskOperatorPanel();
		this.palettePanel = new PalettePanel(this);
		this.infoPanel = new InfoPanel();
		this.blockControlPanel = new BlockControlPanel();
		this.templatePanel = new TemplatePanel();
		this.taskManagerPanel = new TaskManagerPanel();
		
		this.controlView = new JPanel();
		this.controlView.setPreferredSize(new Dimension(400, 0));
		this.controlView.setLayout(new BorderLayout());
		this.staticControlView = new JPanel();
		this.staticControlView.setPreferredSize(new Dimension(0, 475));
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
					Component pointComp = mainFrame.getContentPane().getComponentAt(e.getX(), e.getY());
					Component pointComp1 = pointComp.getComponentAt((int)(e.getX() - pointComp.getLocation().getX()), (int)(e.getY() - pointComp.getLocation().getY()));
					int compX = (int)(e.getX() - pointComp.getLocation().getX() - pointComp1.getLocation().getX());
					int compY = (int)(e.getY() - pointComp.getLocation().getY() - pointComp1.getLocation().getY());
					Component pointComp2 = pointComp1.getComponentAt(compX, compY);
					Component panelComp = pointComp2.getComponentAt(compX, compY);
					if(panelComp == core.getGrid().getGridPanel())
					{
						System.out.println("push");
						Point loc = core.getGrid().getGridScrollPanel().getViewport().getViewPosition().getLocation();
						trackedPane.addMemberOnGrid((int)((compX + loc.getX()) - Size.MARGIN) / getUISize().getmultiple(), (int)((compY + loc.getY()) - Size.MARGIN) / getUISize().getmultiple());
						removeTrackedPane(trackedPane);
					}
				}
			}
		});
		this.staticControlView.add(this.blockControlPanel.getComponent());
		this.staticControlView.add(this.infoPanel.getComponent());
		this.staticControlView.add(this.palettePanel.getComponent());
		this.staticControlView.add(this.taskOperatorPanel.getComponent());
		
		this.moveControlView.add(this.taskManagerPanel.getComponent(), BorderLayout.CENTER);
		this.moveControlView.add(this.templatePanel.getComponent(), BorderLayout.SOUTH);
		
		this.controlView.add(moveControlView, BorderLayout.CENTER);
		this.controlView.add(staticControlView, BorderLayout.NORTH);
		
		this.mainFrame.add(this.toolBar.getComponent(), BorderLayout.NORTH);
		this.mainFrame.add(this.underBar.getComponent(), BorderLayout.SOUTH);
		this.mainFrame.add(this.controlView, BorderLayout.EAST);

		this.mainFrame.setVisible(true);
	}
	void setGridPanel(Grid grid)
	{
		this.mainFrame.add(grid.getGridScrollPanel(), BorderLayout.CENTER);
		core.getUI().getUnderBar().setGridSizeInfo(grid.getgridSizeX(), grid.getgridSizeY());
	}
	TaskOperatorPanel getTaskOperatorPanel()
	{
		return this.taskOperatorPanel;
	}
	Size getUISize()
	{
		return this.UI_Size;
	}
	LogicCore getCore()
	{
		return this.core;
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
class ToolBar implements LogicUIComponent
{
	private JToolBar toolbar;
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
		this.toolbar = new JToolBar();
		
		this.toolbar.setPreferredSize(new Dimension(0, 30));
		this.toolbar.setFloatable(false);
		
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
		
		this.toolbar.add(toolBarPanel);
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
		
		this.underbar.add(centerSidePanel, BorderLayout.CENTER);
		this.underbar.add(leftSidePanel, BorderLayout.WEST);
		this.underbar.add(rightSidePanel, BorderLayout.EAST);
	}
	void setGridSizeInfo(int x, int y)
	{
		this.sizeLabel.setText("X: " + x + "  Y: " + y);
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
	private PauseButton pauseButton;
	private TimeSetButton subTime1Button;
	private TimeSetButton subTime10Button;
	private TimeSetButton subTime100Button;
	private TimeSetButton addTime1Button;
	private TimeSetButton addTime10Button;
	private TimeSetButton addTime100Button;
	private JLabel taskIntervalLabel;
	private JLabel msLabel;
	private TaskOperator operator;
	
	TaskOperatorPanel()
	{
		this.taskOperatorPanel = new JPanel();
		
		this.taskOperatorPanel.setLayout(null);
		this.taskOperatorPanel.setBounds(230, 295, 165, 180);
		this.taskOperatorPanel.setBorder(new PanelBorder("����"));
		
		this.pauseButton = new PauseButton(8, 145, 148, 25);
		
		this.subTime1Button = new TimeSetButton(42, 120, 15, 20, null, null, -1);
		this.subTime10Button = new TimeSetButton(25, 120, 15, 20, null, null, -10);
		this.subTime100Button = new TimeSetButton(8, 120, 15, 20, null, null, -100);
		this.addTime1Button = new TimeSetButton(107, 120, 15, 20, null, null, 1);
		this.addTime10Button = new TimeSetButton(124, 120, 15, 20, null, null, 10);
		this.addTime100Button = new TimeSetButton(141, 120, 15, 20, null, null, 100);
		
		this.taskIntervalLabel = new JLabel();
		this.taskIntervalLabel.setHorizontalAlignment(JLabel.RIGHT);
		this.taskIntervalLabel.setBounds(56, 120, 30, 20);
		
		this.msLabel = new JLabel("ms");
		this.msLabel.setBounds(87, 120, 30, 20);
		
		this.taskOperatorPanel.add(this.pauseButton);
		this.taskOperatorPanel.add(this.subTime1Button);
		this.taskOperatorPanel.add(this.subTime10Button);
		this.taskOperatorPanel.add(this.subTime100Button);
		this.taskOperatorPanel.add(this.addTime1Button);
		this.taskOperatorPanel.add(this.addTime10Button);
		this.taskOperatorPanel.add(this.addTime100Button);
		this.taskOperatorPanel.add(this.taskIntervalLabel);
		this.taskOperatorPanel.add(this.msLabel);
	}
	void setOperator(TaskOperator operator)
	{
		this.operator = operator;
		this.graphPanel = operator.getGraphPanel();
		this.graphPanel.setBounds(8, 20, 148, 90);
		this.graphPanel.setBorder(new EtchedBorder(EtchedBorder.RAISED));
		this.taskOperatorPanel.add(this.graphPanel);
		this.taskIntervalLabel.setText(Integer.toString(operator.getTaskTick()));
	}
	@Override
	public Component getComponent()
	{
		return this.taskOperatorPanel;
	}
	private class TimeSetButton extends UIButton implements ActionListener
	{
		private static final long serialVersionUID = 1L;
		
		private final int tick;
		TimeSetButton(int locationX, int locationY,int sizeX, int sizeY, Image basicImage, Image selectedIcon, int tick)
		{
			super(locationX, locationY, sizeX, sizeY, basicImage, selectedIcon);
			this.tick = tick;
			this.addActionListener(this);
		}
		@Override
		public void actionPerformed(ActionEvent e)
		{
			int addTick = this.tick;
			if(operator.getTaskTick() + addTick <= operator.MIN_TASK_TICK)
			{
				addTick -= (operator.getTaskTick() + addTick - operator.MIN_TASK_TICK);
			}
			if(operator.getTaskTick() + addTick > operator.MAX_TASK_TICK)
			{
				addTick -= operator.getTaskTick() + addTick - operator.MAX_TASK_TICK;
			}
			operator.setTaskTick(operator.getTaskTick() + addTick);
			taskIntervalLabel.setText(Integer.toString(operator.getTaskTick()));
		}
	}
	private class PauseButton extends JButton implements ActionListener
	{
		private static final long serialVersionUID = 1L;
		
		private boolean pauseStatus = true;
		PauseButton(int locationX, int locationY,int sizeX, int sizeY)
		{
			super();
			this.setBounds(locationX, locationY, sizeX, sizeY);
			this.addActionListener(this);
		}
		@Override
		public void actionPerformed(ActionEvent e)
		{
			if(pauseStatus)
			{//�۾� ����
				pauseStatus = false;
				this.setText("����");
				operator.taskStart();
			}
			else
			{//�۾� ����
				pauseStatus = true;
				this.setText("����");
				operator.taskPause();
			}
			
		}
		
	}
}
class BlockControlPanel implements LogicUIComponent
{
	private JPanel blockControlPanel;
	
	private DefaultPane defaultPane;

	BlockControlPanel()
	{
		this.blockControlPanel = new JPanel();
		
		this.defaultPane = new DefaultPane();
		this.blockControlPanel.setLayout(null);
		this.blockControlPanel.setBounds(5, 5, 390, 150);
		this.blockControlPanel.setBorder(new PanelBorder("��� ���� ����"));
		this.removeControlPane();
	}
	void addControlPanel(JPanel panel)
	{
		this.blockControlPanel.removeAll();
		this.blockControlPanel.repaint();
		panel.setBounds(8, 20, 373, 121);
		this.blockControlPanel.add(panel);
	}
	void removeControlPane()
	{
		this.blockControlPanel.removeAll();
		this.addControlPanel(defaultPane);
	}
	@Override
	public Component getComponent()
	{
		return this.blockControlPanel;
	}
}
class InfoPanel implements LogicUIComponent
{
	private JPanel infoPanel;

	private JTextArea explanationArea;
	
	InfoPanel()
	{
		this.infoPanel = new JPanel();
		
		this.infoPanel.setLayout(null);
		this.infoPanel.setBounds(5, 155, 390, 140);
		this.infoPanel.setBorder(new PanelBorder("����"));
		
		this.explanationArea = new JTextArea();
		this.explanationArea.setBounds(8, 20, 373, 111);
		this.explanationArea.setEditable(false);
		this.explanationArea.setText("���� �г�");
		this.explanationArea.setFont(LogicCore.RES.NORMAL_FONT.deriveFont(14.0f));
		
		this.infoPanel.add(explanationArea);
	}
	public Component getComponent()
	{
		return this.infoPanel;
	}
}
class PalettePanel implements LogicUIComponent
{
	private UI logicUI;
	
	private JPanel palettePanel;

	private HashMap<String, PaletteMember> paletteMembers = new HashMap<String, PaletteMember>();
	
	PalettePanel(UI logicUI)
	{
		this.logicUI = logicUI;
		
		this.palettePanel = new JPanel();
		
		this.palettePanel.setLayout(null);
		this.palettePanel.setBounds(5, 295, 225, 180);
		this.palettePanel.setBorder(new PanelBorder("�ȷ�Ʈ"));
		LogicTREditPane DFTLogicControl = new LogicTREditPane();
		
		new PaletteMember(new AND(this.logicUI.getCore()), DFTLogicControl);
		new PaletteMember(new OR(this.logicUI.getCore()), DFTLogicControl);
		new PaletteMember(new NOT(this.logicUI.getCore()), DFTLogicControl);
		new PaletteMember(new Button(this.logicUI.getCore()), DFTLogicControl);
		new PaletteMember(new XOR(this.logicUI.getCore()), DFTLogicControl);
		System.out.println(paletteMembers.keySet().size());
		int i = 0, j = 0;
		for(String str : paletteMembers.keySet())
		{
			PaletteMember p = this.paletteMembers.get(str);
			if(i < 4)
			{
				if(j < 5)
				{
					p.setBounds(j * 42 + 9, i * 42 + 20, 38, 38);
					System.out.println(i + " " + j);
					j++;
				}
				else
				{
					j = 0;
					i++;
				}
			}
			else
			{
				i = 0;
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
		PaletteMember(GridMember member, LogicTREditPane control)
		{
			paletteMembers.put(member.getName(), this);
			this.selectControlPanel = control;
			this.putMember = member;
		}
		@Override
		void pressed(int button)
		{
			{
				if(button == 1)
				{
					ArrayList<GridMember> list = new ArrayList<GridMember>();
					list.add(GridMember.Factory(logicUI.getCore(), putMember.getData(new HashMap<String, String>())));
					logicUI.addTrackedPane(new TrackedPane(list, logicUI));
				}
				else if(button == 3)
				{
					selectControlPanel.setInfo(putMember);
					logicUI.getCore().getGrid().deSelectAll();
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
	private UIButton addTemplateButton;
	private UIButton putGridButton;
	private UIButton removeTemplateButton;

	TemplatePanel()
	{
		this.templatePanel = new JPanel();
		
		this.templatePanel.setLayout(new BorderLayout());
		this.templatePanel.setBorder(new PanelBorder("���ø�"));
		
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
		
		this.templatePanel.add(this.templateAreaPanel, BorderLayout.CENTER);
		this.templatePanel.add(this.buttonAreaPanel, BorderLayout.EAST);
		
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
	
	private JScrollPane taskScrollPane;
	private JPanel buttonAreaPanel;
	private UIButton TaskUndoButton;
	private UIButton TaskRedoButton;
	
	TaskManagerPanel()
	{
		this.taskManagerPanel = new JPanel();
		
		this.taskManagerPanel.setLayout(new BorderLayout());
		this.taskManagerPanel.setBorder(new PanelBorder("�۾�"));
		
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
		
		this.taskManagerPanel.add(this.taskScrollPane, BorderLayout.CENTER);
		this.taskManagerPanel.add(this.buttonAreaPanel, BorderLayout.EAST);
		
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
	private UIButton copyButton;
	private UIButton cutButton;
	private UIButton createTempButton;
	private UIButton exportTempButton;
	private UIButton restoreButton;
	private UIButton removeButton;
	ManySelectEditPanel(ArrayList<GridMember> selectMembers, BlockControlPanel blockControl)
	{
		super();
		this.setLayout(null);
		this.numberLabel = new JLabel();
		this.numberLabel.setHorizontalAlignment(SwingConstants.CENTER);
		this.numberLabel.setFont(LogicCore.RES.NORMAL_FONT.deriveFont(16F));
		this.numberLabel.setBounds(0, 0, 373, 20);
		this.numberLabel.setText(Integer.toString(selectMembers.size()) + " ���� ��� ����");
		this.copyButton = new UIButton(66, 25, 40, 40, null, null);
		this.cutButton = new UIButton(166, 25, 40, 40, null, null);
		this.removeButton = new UIButton(266, 25, 40, 40, null, null);
		this.createTempButton = new UIButton(66, 75, 40, 40, null, null);
		this.exportTempButton = new UIButton(166, 75, 40, 40, null, null);
		this.restoreButton = new UIButton(266, 75, 40, 40, null, null);
		
		
		this.add(this.numberLabel);
		this.add(this.copyButton);
		this.add(this.cutButton);
		this.add(this.removeButton);
		this.add(this.createTempButton);
		this.add(this.exportTempButton);
		this.add(this.restoreButton);
		
		
		blockControl.addControlPanel(this);
	}
}
class SelectControlPanel extends JPanel
{
	private static final long serialVersionUID = 1L;
	private JLabel numberLabel;
	SelectControlPanel(String text, BlockControlPanel blockControl)
	{
		super();
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
		this.numberLabel.setText(Integer.toString(num) + " ���� ��� ����");
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
		this.text.setText("����� �����Ͻ÷��� Ŭ�� Ȥ�� �巹�� �ϼ���");
		this.add(this.text);
	}
}
class EditPane extends JPanel
{
	private static final long serialVersionUID = 1L;
	
	private ButtonPanel copyButton = new ButtonPanel(275, 100, 20, 20);
	private ButtonPanel removeButton = new ButtonPanel(300, 100, 20, 20)
	{
		private static final long serialVersionUID = 1L;

		@Override
		void pressed(int mouse)
		{
			member.remove();
		}
	};
	private ButtonPanel disableButton = new ButtonPanel(325, 100, 20, 20);
	private ButtonPanel restoreButton = new ButtonPanel(350, 100, 20, 20);
	
	protected GridMember member;
	
	EditPane()
	{
		this.setLayout(null);
		this.add(this.copyButton);
		this.add(this.removeButton);
		this.add(this.disableButton);
		this.add(this.restoreButton);
	}
	void setInfo(GridMember member)
	{
		this.member = member;
	}
}
class LogicTREditPane extends EditPane
{
	private static final long serialVersionUID = 1L;
	
	private JLabel text = new JLabel();
	private JLabel locationText = new JLabel();
	private LogicBlock logicMember;
	private ArrayList<IOControlButton> IOEditButton = new ArrayList<IOControlButton>();
	
	private JPanel editViewPanel = new JPanel()
	{
		private static final long serialVersionUID = 1L;
		@Override
		public void paint(Graphics g)
		{
			super.paint(g);
			g.drawRect(0, 0, 119, 119);
		}
	};
	
	LogicTREditPane()
	{
		this.IOEditButton.add(new IOControlButton(100, 28, 16, 64, Direction.EAST));
		this.IOEditButton.add(new IOControlButton(4, 28, 16, 64, Direction.WEST));
		this.IOEditButton.add(new IOControlButton(28, 100, 64, 16, Direction.SOUTH));
		this.IOEditButton.add(new IOControlButton(28, 4, 64, 16, Direction.NORTH));
		
		this.editViewPanel.setLayout(null);
		for(IOControlButton btn : this.IOEditButton)
		{
			this.editViewPanel.add(btn);
		}
		
		this.text.setHorizontalAlignment(SwingConstants.CENTER);
		this.text.setFont(LogicCore.RES.NORMAL_FONT.deriveFont(16.0f));
		this.text.setBounds(135, 0, 223, 20);

		this.locationText.setHorizontalAlignment(SwingConstants.CENTER);
		this.locationText.setFont(LogicCore.RES.NORMAL_FONT.deriveFont(16.0f));
		this.locationText.setBounds(135, 100, 135, 20);
		
		this.editViewPanel.setBounds(5, 0, 120, 120);
		this.editViewPanel.setBackground(new Color(200, 200, 200));
		
		this.add(this.text);
		this.add(this.locationText);
		this.add(this.editViewPanel);
	}
	@Override
	void setInfo(GridMember member)
	{
		super.setInfo(member);
		this.logicMember = (LogicBlock)member;
		if(member.isPlacement())
		{
			this.locationText.setText("(X:" + logicMember.getBlockLocationX() + " Y: " + logicMember.getBlockLocationY() + ")");
		}
		else
		{
			this.locationText.setText("��ġ�� ����� �ƴ�");
		}
		this.text.setText(super.member.getName() + " ����Ʈ ����");
		for(IOControlButton btn : this.IOEditButton)
		{
			btn.setImage();
		}
	}
	private class IOControlButton extends ButtonPanel
	{
		private static final long serialVersionUID = 1L;
		
		int locX, locY, sizeX, sizeY;
		Direction ext;
		
		IOControlButton(int locX, int locY, int sizeX, int sizeY, Direction ext)
		{
			this.locX = locX; this.locY = locY; this.sizeX = sizeX; this.sizeY = sizeY;
			this.setBounds(locX, locY, sizeX, sizeY);
			this.ext = ext;
		}
		@Override
		public void pressed(int mouse)
		{
			logicMember.toggleIO(this.ext);
			this.setImage();
		}
		void setImage()
		{
			super.setBasicImage(LogicCore.getResource().getImage("TR_BLOCK_EDIT_" + logicMember.getIOStatus(ext).getTag() + "_BASIC_" + ext));
			super.setBasicPressImage(LogicCore.getResource().getImage("TR_BLOCK_EDIT_" + logicMember.getIOStatus(ext).getTag() + "_PUSH_" + ext));
		}
	}
}
class TrackedPane extends JPanel implements SizeUpdate
{
	private static final long serialVersionUID = 1L;
	private List<GridMember> members;
	private int maxX, maxY, minX, minY;
	private UI logicUI;
	TrackedPane(List<GridMember> members, UI logicUI)
	{
		this.logicUI = logicUI;
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
			g.drawImage(img, (member.getUIabsLocationX() - minX) * logicUI.getUISize().getmultiple(), (member.getUIabsLocationY() - minY) * logicUI.getUISize().getmultiple(), this);
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
		this.setSize((this.maxX - this.minX) * logicUI.getUISize().getmultiple(), (this.maxY - this.minY) * logicUI.getUISize().getmultiple());
		this.repaint();

	}
	void addMemberOnGrid(int absX, int absY)
	{
		int stdX = absX - ((this.getWidth() / 2) / this.logicUI.getUISize().getmultiple()) - (this.logicUI.getCore().getGrid().getNegativeExtendX() * Size.REGULAR_SIZE);
		int stdY = absY - ((this.getHeight() / 2) / this.logicUI.getUISize().getmultiple()) - (this.logicUI.getCore().getGrid().getNegativeExtendY() * Size.REGULAR_SIZE);
		stdX = stdX > 0 ? stdX + (Size.REGULAR_SIZE / 2) : stdX - (Size.REGULAR_SIZE / 2);
		stdY = stdY > 0 ? stdY + (Size.REGULAR_SIZE / 2) : stdY - (Size.REGULAR_SIZE / 2);
		stdX = (stdX / Size.REGULAR_SIZE) * Size.REGULAR_SIZE;
		stdY = (stdY / Size.REGULAR_SIZE) * Size.REGULAR_SIZE;
		for(GridMember member : members)
		{
			this.logicUI.getCore().getGrid().addMember(member, stdX, stdY);
			//TODO
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
	private HashMap<Integer, BufferedImage> pressImages = new HashMap<Integer, BufferedImage>();
	
	private BufferedImage nowImage;
	private int mouseButton = 0;
	
	ButtonPanel()
	{
		this.addMouseMotionListener(this);
		this.addMouseListener(this);
		this.setBackground(Color.orange);
	}
	ButtonPanel(int x, int y, int width, int height)
	{
		this.setBounds(x, y, width, height);
		this.addMouseMotionListener(this);
		this.addMouseListener(this);
		this.setBackground(Color.orange);
	}
	ButtonPanel(BufferedImage basicImage, BufferedImage onMouseImage, BufferedImage basicPressImage)
	{
		this.addMouseMotionListener(this);
		this.addMouseListener(this);
		this.basicImage = basicImage;
		this.onMouseImage = onMouseImage;
		this.basicPressImage = basicPressImage;
		this.setBackground(Color.orange);
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
		if((e.getX() <= this.getWidth() && e.getY() <= this.getHeight()))
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
	}
	void pressed(int button)
	{
		System.out.println("��");
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
		if(status == ButtonStatus.PRESS)
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
		else if(status == ButtonStatus.ONMOUSE)
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
		super.repaint();
	}
	private enum ButtonStatus
	{
		NONE, PRESS, ONMOUSE;
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