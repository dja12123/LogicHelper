package kr.dja;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.UUID;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

public class TaskManager
{
	private Session session;
	
	private int maxSnapShot = 20;
	private int checkTime = 3;
	private long lastCheckTime;
	
	private int focusUnit;
	
	private JPanel taskPanel;
	public static final int HGAP = 1;
	public static final int WGAP = 1;
	public static final int COMPW = 325;
	
	private ArrayList<TaskUnit> snapShots = new ArrayList<TaskUnit>();
	
	TaskManager(Session session)
	{
		this.session = session;
		this.taskPanel = new JPanel();
		this.taskPanel.setLayout(null);
		this.createSnapShot();
		TaskUnit task = this.getLastSnapShot();
		task.setFirstLabel("초기");
		task.setEdit();
		this.lastCheckTime = 0;
	}
	TaskManager(Session session, HashMap<String, String> data)
	{
		this(session);
	}
	private TaskUnit getLastSnapShot()
	{
		return snapShots.size() > 0 ? this.snapShots.get(snapShots.size() - 1) : null;
	}
	void setMaxSnapShot(int max)
	{
		this.maxSnapShot = max;
		this.checkSnapShotCount();
	}
	void createSnapShot()
	{
		this.lastCheckTime = System.currentTimeMillis() / 1000;
		TaskUnit nowCreateTaskUnit = new TaskUnit(this);

		nowCreateTaskUnit.getView().setLocation(WGAP, this.snapShots.size() * (nowCreateTaskUnit.getView().getHeight() + HGAP));
		this.taskPanel.add(nowCreateTaskUnit.getView());
		this.snapShots.add(nowCreateTaskUnit);
		this.reSizeTaskPanel();
	}
	private void reSizeTaskPanel()
	{
		int height = HGAP;
		for(Component comp : this.taskPanel.getComponents())
		{
			height += comp.getHeight() + HGAP;
		}
		this.taskPanel.setPreferredSize(new Dimension(0, height));
		this.session.getCore().getUI().getTaskManagerPanel().setManager(this);
	}
	TaskUnit setTask()
	{
		if(this.lastCheckTime + checkTime < System.currentTimeMillis() / 1000 && this.getLastSnapShot().isEdit())
		{
			this.createSnapShot();
		}
		TaskUnit returnTaskUnit = this.getLastSnapShot();
		this.lastCheckTime = System.currentTimeMillis() / 1000;
		returnTaskUnit.setEdit();
		return returnTaskUnit;
	}
	void removeSnapShot(TaskUnit snap)
	{
		this.taskPanel.remove(snap.getView());
		this.snapShots.remove(snap);
		this.reSizeTaskPanel();
	}
	void recover(TaskUnit unit)
	{
		int startIndex = this.snapShots.indexOf(unit);
	}
	private void checkSnapShotCount()
	{
		int i = this.snapShots.size() - this.maxSnapShot - 1;
		while(i > 0)
		{
			TaskUnit removeSnapShot = this.snapShots.get(i);
			this.taskPanel.remove(removeSnapShot.getView());
			this.snapShots.remove(removeSnapShot);
		}
	}
	JPanel getPanel()
	{
		return this.taskPanel;
	}
	Session getSession()
	{
		return this.session;
	}
}
class TaskUnit
{
	private TaskManager manager;
	
	private TaskSnapShot beforeSnapShot;
	private TaskSnapShot afterSnapShot;
	
	private TaskButton snapShotView;
	private JLabel stateLabel;
	private JLabel timeLabel;
	private HashMap<String, Integer> labels = new HashMap<String, Integer>();
	private String firstLabel = new String("");
	
	private boolean editFlag = false;
	
	TaskUnit(TaskManager manager)
	{
		this.manager = manager;
		this.beforeSnapShot = new TaskSnapShot();
		this.afterSnapShot = new TaskSnapShot();
		this.snapShotView = new TaskButton();
		this.stateLabel = new JLabel();
		this.stateLabel.setFont(LogicCore.RES.BAR_FONT.deriveFont(12f));
		this.stateLabel.setBounds(5, 0, TaskManager.COMPW - 10, 30);
		this.timeLabel = new JLabel();
		this.timeLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
		this.timeLabel.setFont(LogicCore.RES.BAR_FONT.deriveFont(9f));
		this.timeLabel.setBounds(TaskManager.COMPW - 42, 20, 40, 10);
		this.snapShotView.add(this.stateLabel);
		this.snapShotView.add(this.timeLabel);
	}
	TaskUnit(TaskManager manager, HashMap<String, String> dataMap)
	{
		this(manager);
	}
	HashMap<String, String> getData(HashMap<String, String> dataMap)
	{
		return dataMap;
	}
	boolean isEdit()
	{
		return this.editFlag;
	}
	void setEdit()
	{
		this.editFlag = true;
	}
	void addEditBefore(GridMember member)
	{
		if(!this.beforeSnapShot.editSnapMembers.containsKey(member.getUUID()))
		{
			this.beforeSnapShot.editSnapMembers.put(member.getUUID(), member.getData(new HashMap<String, String>()));
		}
	}
	void addEditAfter(GridMember member)
	{
		this.afterSnapShot.editSnapMembers.put(member.getUUID(), member.getData(new HashMap<String, String>()));
		this.setLabel("편집", this.afterSnapShot.editSnapMembers.size());
	}
	void addCreate(GridMember member)
	{
		if(!this.beforeSnapShot.removeSnapMembers.contains(member.getUUID()))
		{
			this.beforeSnapShot.removeSnapMembers.add(member.getUUID());
		}
		this.afterSnapShot.createSnapMembers.put(member.getUUID(), member.getData(new HashMap<String, String>()));
		this.setLabel("생성", this.afterSnapShot.createSnapMembers.size());
	}
	void addRemove(GridMember member)
	{
		this.beforeSnapShot.createSnapMembers.put(member.getUUID(), member.getData(new HashMap<String, String>()));
		if(!this.afterSnapShot.removeSnapMembers.contains(member.getUUID()))
		{
			this.afterSnapShot.removeSnapMembers.add(member.getUUID());
		}
		this.setLabel("삭제", this.afterSnapShot.removeSnapMembers.size());
	}
	void addEditBefore(Grid grid)
	{
		if(!this.beforeSnapShot.editSnapGrids.containsKey(grid.getUUID()))
		{
			this.beforeSnapShot.editSnapGrids.put(grid.getUUID(), grid.getData(new HashMap<String, String>()));
		}
	}
	void addEditAfter(Grid grid)
	{
		this.afterSnapShot.editSnapGrids.put(grid.getUUID(), grid.getData(new HashMap<String, String>()));
		this.setLabel("그리드 편집", this.afterSnapShot.editSnapGrids.size());
	}
	void addCreate(Grid grid)
	{
		if(!this.beforeSnapShot.removeSnapGrids.contains(grid.getUUID()))
		{
			this.beforeSnapShot.removeSnapGrids.add(grid.getUUID());
		}
		this.afterSnapShot.createSnapGrids.put(grid.getUUID(), grid.getData(new HashMap<String, String>()));
		this.setLabel("그리드 생성", this.afterSnapShot.createSnapGrids.size());
	}
	void addRemove(Grid grid)
	{
		this.beforeSnapShot.createSnapGrids.put(grid.getUUID(), grid.getData(new HashMap<String, String>()));
		if(!this.afterSnapShot.removeSnapGrids.contains(grid.getUUID()))
		{
			this.afterSnapShot.removeSnapGrids.add(grid.getUUID());
		}
		this.setLabel("그리드 삭제", this.afterSnapShot.removeSnapGrids.size());
	}
	void setLabel(String str)
	{
		this.stateLabel.setText(str);
	}
	TaskButton getView()
	{
		return this.snapShotView;
	}
	private void setLabel(String str, int num)
	{
		this.labels.put(str, num);
		this.setLabelText();
	}
	void setFirstLabel(String str)
	{
		this.firstLabel = str;
		this.setLabelText();
	}
	private void setLabelText()
	{
		String set = new String(this.firstLabel);
		for(String arrStr : this.labels.keySet())
		{
			set = new String(set + " " + arrStr + "(" + this.labels.get(arrStr) + ")");
		}
		this.stateLabel.setText(set);
		this.timeLabel.setText(new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()));
	}
	class TaskButton extends ButtonPanel
	{
		private static final long serialVersionUID = 1L;
		
		TaskButton()
		{
			this.setSize(TaskManager.COMPW, 30);
			this.setLayout(null); 
		}
		@Override
		void pressed(int button)
		{
			manager.recover(TaskUnit.this);
		}
	}
}
class TaskSnapShot
{
	HashMap<UUID, HashMap<String, String>> editSnapMembers = new HashMap<UUID, HashMap<String, String>>();
	HashMap<UUID, HashMap<String, String>> createSnapMembers = new HashMap<UUID, HashMap<String, String>>();
	ArrayList<UUID> removeSnapMembers = new ArrayList<UUID>();
	HashMap<UUID, HashMap<String, String>> editSnapGrids = new HashMap<UUID, HashMap<String, String>>();
	HashMap<UUID, HashMap<String, String>> createSnapGrids = new HashMap<UUID, HashMap<String, String>>();
	ArrayList<UUID> removeSnapGrids = new ArrayList<UUID>();
	TaskSnapShot(){};
	TaskSnapShot(HashMap<String, String> dataMap)
	{
		
	}
	HashMap<String, String> getData(HashMap<String, String> dataMap)
	{
		return dataMap;
	}
}
