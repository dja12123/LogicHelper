package kr.dja;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
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
	private int lastCheckTime = 0;
	
	private JPanel taskPanel;
	public static final int HGAP = 1;
	public static final int WGAP = 1;
	public static final int COMPW = 325;
	
	private ArrayList<TaskSnapShot> snapShots = new ArrayList<TaskSnapShot>();
	
	TaskManager(Session session)
	{
		this.session = session;
		
		this.taskPanel = new JPanel();
		this.taskPanel.setLayout(null);

		this.createSnapShot();
	}
	TaskManager(HashMap<String, String> data)
	{
		
	}
	private TaskSnapShot getLastSnapShot()
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
		this.lastCheckTime = (int)System.currentTimeMillis() / 1000;
		TaskSnapShot nowCreateSnapShot = new TaskSnapShot(this, this.getLastSnapShot());
		nowCreateSnapShot.getView().setLocation(WGAP, this.snapShots.size() * (nowCreateSnapShot.getView().getHeight() + HGAP));
		this.taskPanel.add(nowCreateSnapShot.getView());
		if(this.getLastSnapShot() != null)
		{
			this.getLastSnapShot().setAfterLinkedSnapShot(nowCreateSnapShot);
		}
		this.snapShots.add(nowCreateSnapShot);
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
	TaskSnapShot setTask()
	{
		if(this.lastCheckTime + checkTime < (int)System.currentTimeMillis() / 1000 && this.getLastSnapShot().isEdit())
		{
			this.createSnapShot();
		}
		this.lastCheckTime = (int)System.currentTimeMillis() / 1000;
		return this.getLastSnapShot();
	}
	void removeSnapShot(TaskSnapShot snap)
	{
		this.taskPanel.remove(snap.getView());
		this.snapShots.remove(snap);
		this.reSizeTaskPanel();
	}
	void checkSnapShotCount()
	{
		int i = this.snapShots.size() - this.maxSnapShot - 1;
		while(i > 0)
		{
			TaskSnapShot removeSnapShot = this.snapShots.get(i);
			this.taskPanel.remove(removeSnapShot.getView());
			if(removeSnapShot.getAfterLinkedSnapShot() != null)
			{
				removeSnapShot.getAfterLinkedSnapShot().removeBeforeLinkedSnapShot();
			}
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
class TaskSnapShot
{
	private HashMap<UUID, HashMap<String, String>> snapMembers = new HashMap<UUID, HashMap<String, String>>();
	private HashMap<UUID, GridData> snapGrids = new HashMap<UUID, GridData>(); //한 세션에 여러 그리드 대응
	
	private TaskSnapShot beforeLinkedSnapShot;
	private TaskSnapShot afterLinkedSnapShot;
	
	private TaskManager manager;
	
	private TaskButton snapShotView;
	private JLabel stateLabel;
	
	private boolean isEdit = false;
	
	TaskSnapShot(TaskManager manager, TaskSnapShot beforeLinkedSnapShot)
	{
		this.manager = manager;
		this.beforeLinkedSnapShot = beforeLinkedSnapShot;
		this.snapShotView = new TaskButton();
		this.stateLabel = new JLabel();
		this.stateLabel.setFont(LogicCore.RES.NORMAL_FONT.deriveFont(14f));
		this.stateLabel.setText("편집중..");
		this.stateLabel.setBounds(5, 0, 200, 30);
		this.snapShotView.add(this.stateLabel);
	}
	void setAfterLinkedSnapShot(TaskSnapShot snap)
	{
		this.afterLinkedSnapShot = snap;
	}
	void removeBeforeLinkedSnapShot()
	{
		this.beforeLinkedSnapShot = null;
	}
	TaskSnapShot getAfterLinkedSnapShot()
	{
		return this.afterLinkedSnapShot;
	}
	void setData(GridMember member)
	{
		this.snapMembers.put(member.getUUID(), member.getData(new HashMap<String, String>()));
		this.stateLabel.setText(this.snapMembers.size() + " 개의 멤버 편집");
		this.isEdit = true;
	}
	void setData(Grid grid, boolean isActive)
	{
		this.snapGrids.put(grid.getID(), new GridData(grid.getGridSize(), isActive));
		this.stateLabel.setText("그리드 편집");
		this.isEdit = true;
	}
	TaskButton getView()
	{
		return this.snapShotView;
	}
	void reStore()
	{
		if(this.afterLinkedSnapShot != null)
		{
			this.afterLinkedSnapShot.reStore();
		}
		if(this.snapGrids.size() > 0)
		{
			manager.getSession().getGrid().gridResize(this.snapGrids.get(manager.getSession().getGrid().getID()).getSize(), false);
		}
		for(UUID id : this.snapMembers.keySet())
		{
			HashMap<String, String> data = this.snapMembers.get(id);
			if(data.get("placement").equals("true"))
			{
				
				GridMember member = GridMember.Factory(this.manager.getSession().getCore(), data);
				manager.getSession().getGrid().addMember(member, member.getUIabsLocationX(), member.getUIabsLocationY(), false);
			}
			else
			{
				manager.getSession().getGrid().removeMember(UUID.fromString(data.get("id")), false);
			}
		}
	}
	boolean isEdit()
	{
		return this.isEdit;
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
			reStore();
		}
	}
}
class GridData
{
	private final SizeInfo size;
	private final boolean active;
	
	GridData(SizeInfo size, boolean active)
	{
		this.size = size;
		this.active = active;
	}
	SizeInfo getSize()
	{
		return this.size;
	}
	boolean isActive()
	{
		return this.active;
	}
}
