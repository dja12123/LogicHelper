package kr.dja;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;


public class TaskManager
{
	private Session session;
	
	private int maxSnapShot = 20;
	
	private JPanel taskPanel;
	
	private ArrayList<TaskSnapShot> snapShots = new ArrayList<TaskSnapShot>();
	
	TaskManager(Session session)
	{
		this.session = session;
		
		this.taskPanel = new JPanel();
		this.taskPanel.setLayout(null);
	}
	private TaskSnapShot getLastSnapShot()
	{
		return this.snapShots.get(snapShots.size() - 1);
	}
	void takeSnapShot()
	{
		TaskSnapShot nowCreateSnapShot = new TaskSnapShot(this, this.getLastSnapShot());
		this.getLastSnapShot().setAfterLinkedSnapShot(nowCreateSnapShot);
		this.snapShots.add(nowCreateSnapShot);
		this.checkSnapShotCount();
	}
	void setMaxSnapShot(int max)
	{
		this.maxSnapShot = max;
		this.checkSnapShotCount();
	}
	void checkSnapShotCount()
	{
		int i = this.snapShots.size() - this.maxSnapShot - 1;
		while(i > 0)
		{
			TaskSnapShot removeSnapShot = this.snapShots.get(i);
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
	private ArrayList<HashMap<String, String>> snapMembers = new ArrayList<HashMap<String, String>>();
	private HashMap<UUID, GridData> snapGrids = new HashMap<UUID, GridData>();
	
	private TaskSnapShot beforeLinkedSnapShot;
	private TaskSnapShot afterLinkedSnapShot;
	
	private TaskManager manager;
	
	private JPanel snapShotView;
	
	private int index = 0;
	
	TaskSnapShot(TaskManager manager, TaskSnapShot beforeLinkedSnapShot)
	{
		this.manager = manager;
		this.beforeLinkedSnapShot = beforeLinkedSnapShot;
		this.snapShotView = new JPanel();
		this.snapShotView.setBorder(new BevelBorder(BevelBorder.RAISED));
		this.snapShotView.setSize(200, 50);
		this.snapShotView.setLayout(null); 
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
		
	}
	void setData(Grid grid)
	{
		
	}
	void setIndex(int index)
	{
		this.index = index;
	}
	int getIndex()
	{
		return this.index;
	}
	JPanel getView()
	{
		return this.snapShotView;
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
