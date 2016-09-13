package kr.dja;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.UUID;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

public class TaskManager
{
	private Session session;
	
	private int maxSnapShot = 20;
	private int checkTime = 1;
	private long lastCheckTime;
	
	private TaskUnit focusUnit;
	
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
		this.createNoneUint();
		this.lastCheckTime = 0;
	}
	TaskManager(Session session, LinkedHashMap<String, String> data)
	{
		this(session);
	}
	ArrayList<String> getData(ArrayList<String> dataList)
	{
		if(this.focusUnit != null)
		{
			dataList.add("focus = " + this.snapShots.indexOf(this.focusUnit) + "\n");
		}
		else
		{
			dataList.add("focus = 0\n");
		}
		for(TaskUnit unit : this.snapShots)
		{
			dataList.add("snapShots_" + this.snapShots.indexOf(unit) + "={\n");
			ArrayList<String> dataTemp = new ArrayList<String>();
			for(String key : unit.getData(dataTemp))
			{
				dataList.add("\t" + key);
			}
			dataList.add("}\n");
		}
		return dataList;
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
	private void createSnapShot()
	{
		this.lastCheckTime = System.currentTimeMillis() / 1000;
		TaskUnit nowCreateTaskUnit = new TaskUnit(this);
		nowCreateTaskUnit.getView().setLocation(WGAP, this.snapShots.size() * (nowCreateTaskUnit.getView().getHeight() + HGAP));
		this.taskPanel.add(nowCreateTaskUnit.getView());
		this.snapShots.add(nowCreateTaskUnit);
		this.reSizeTaskPanel();
		this.setFocus(nowCreateTaskUnit);
	}
	private void createNoneUint()
	{
		this.createSnapShot();
		TaskUnit task = this.getLastSnapShot();
		task.setFirstLabel("초기");
		task.setEdit();
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
		System.out.println("record");
		if(this.focusUnit != null && this.focusUnit != this.getLastSnapShot())
		{//작업이 걸려있을경우 삭제함
			int removeIndex = this.snapShots.indexOf(this.focusUnit) + 1;
			while(this.snapShots.size() > removeIndex)
			{
				this.taskPanel.remove(this.snapShots.get(removeIndex).getView());
				this.snapShots.remove(removeIndex);
			}
			this.reSizeTaskPanel();
			this.focusUnit.getView().setFocus(false);
			this.focusUnit = null;
			this.createSnapShot();
		}
		if(this.lastCheckTime + checkTime < System.currentTimeMillis() / 1000 && this.getLastSnapShot().isEdit())
		{
			this.createSnapShot();
		}
		else
		{
			this.lastCheckTime = System.currentTimeMillis() / 1000;
		}
		TaskUnit returnTaskUnit = this.getLastSnapShot();
		returnTaskUnit.setEdit();
		return returnTaskUnit;
	}
	void recover(TaskUnit unit)
	{
		int oldFocusIndex = this.focusUnit != null ? this.snapShots.indexOf(this.focusUnit) : 0;
		this.setFocus(unit);
		int nowFocusIndex = this.snapShots.indexOf(this.focusUnit);
		int startIndex = this.snapShots.indexOf(unit);
		for(TaskUnit task : this.snapShots)
		{
			if(this.snapShots.indexOf(task) != startIndex)
			{
				if(this.snapShots.indexOf(task) > startIndex)
				{
					task.getView().setIsRemove(true);
				}
				else
				{
					task.getView().setIsRemove(false);
				}
			}
		}
		for(int i = oldFocusIndex + 1; i <= nowFocusIndex; i++)
		{
			TaskUnit task = this.snapShots.get(i);
			System.out.println("REDO " + i);
			for(UUID gridID : task.afterSnapShot.removeSnapGrids)
			{//다중 레이어일시 구현하시오
				
			}
			for(UUID gridID : task.afterSnapShot.createSnapGrids.keySet())
			{//다중 레이어일시 구현하시오
				
			}
			for(UUID gridID : task.afterSnapShot.editSnapGrids.keySet())
			{//다중 레이어일시 수정 필요
				Grid grid = this.session.getGrid();
				grid.setData(task.afterSnapShot.editSnapGrids.get(gridID));
			}
			for(UUID memberID : task.afterSnapShot.removeSnapMembers)
			{
				this.session.getGrid().removeMember(memberID, false);
			}
			for(UUID memberID : task.afterSnapShot.createSnapMembers.keySet())
			{
				LinkedHashMap<String, String> memberData = task.afterSnapShot.createSnapMembers.get(memberID);
				GridMember member = GridMember.Factory(session.getCore(), memberData);
				this.session.getGrid().addMember(member, member.getUIabsLocationX(), member.getUIabsLocationY(), false);
			}
			for(UUID memberID : task.afterSnapShot.editSnapMembers.keySet())
			{
				if(this.session.getGrid().getMembers().containsKey(memberID))
				{
					LinkedHashMap<String, String> memberData = task.afterSnapShot.editSnapMembers.get(memberID);
					this.session.getGrid().getMembers().get(memberID).setData(memberData);
				}
			}
		}
		for(int i = oldFocusIndex; i > nowFocusIndex; i--)
		{
			TaskUnit task = this.snapShots.get(i);
			System.out.println("UNDO " + this.snapShots.indexOf(task));
			for(UUID gridID : task.beforeSnapShot.removeSnapGrids)
			{//다중 레이어일시 구현하시오
				
			}
			for(UUID gridID : task.beforeSnapShot.createSnapGrids.keySet())
			{//다중 레이어일시 구현하시오
				
			}
			for(UUID gridID : task.beforeSnapShot.editSnapGrids.keySet())
			{//다중 레이어일시 수정 필요
				Grid grid = this.session.getGrid();
				grid.setData(task.beforeSnapShot.editSnapGrids.get(gridID));
			}
			for(UUID memberID : task.beforeSnapShot.removeSnapMembers)
			{
				if(this.session.getGrid().getMembers().containsKey(memberID))
				{
					this.session.getGrid().removeMember(memberID, false);
				}
			}
			for(UUID memberID : task.beforeSnapShot.createSnapMembers.keySet())
			{
				LinkedHashMap<String, String> memberData = task.beforeSnapShot.createSnapMembers.get(memberID);
				this.session.getGrid().addMember(GridMember.Factory(session.getCore(), memberData)
						, new Integer(memberData.get("UIabslocationX")), new Integer(memberData.get("UIabslocationY")), false);
			}
			for(UUID memberID : task.beforeSnapShot.editSnapMembers.keySet())
			{
				if(this.session.getGrid().getMembers().containsKey(memberID))
				{
					LinkedHashMap<String, String> memberData = task.beforeSnapShot.editSnapMembers.get(memberID);
					this.session.getGrid().getMembers().get(memberID).setData(memberData);
				}
			}
		}
	}
	private void checkSnapShotCount()
	{
		int i = this.snapShots.size() - this.maxSnapShot - 1;
		while(i > 0)
		{
			TaskUnit removeSnapShot = this.snapShots.get(i);
			if(this.focusUnit == removeSnapShot)
			{
				this.deFocus();
			}
			this.taskPanel.remove(removeSnapShot.getView());
			this.snapShots.remove(removeSnapShot);
		}
	}
	private void setFocus(TaskUnit unit)
	{
		if(this.snapShots.contains(unit))
		{
			if(this.focusUnit != null)
			{
				this.focusUnit.getView().setFocus(false);
			}
			this.focusUnit = unit;
			this.focusUnit.getView().setFocus(true);
		}
	}
	private void deFocus()
	{
		if(this.focusUnit != null)
		{
			this.focusUnit.getView().setFocus(false);
			this.focusUnit = null;
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
	
	TaskSnapShot beforeSnapShot;
	TaskSnapShot afterSnapShot;
	
	private TaskButton snapShotView;
	private JLabel stateLabel;
	private JLabel timeLabel;
	private LinkedHashMap<String, Integer> labels = new LinkedHashMap<String, Integer>();
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
	TaskUnit(TaskManager manager, LinkedHashMap<String, String> dataMap)
	{
		this(manager);
	}
	ArrayList<String> getData(ArrayList<String> dataList)
	{
		dataList.add("stateLabel = " + this.stateLabel.getText() + "\n");//다국어 지원 모드로 수정 필요
		dataList.add("timeLabel = " + this.timeLabel.getText() + "\n");
		dataList.add("beforeSnapShot={\n");
		ArrayList<String> dataTemp = new ArrayList<String>();
		for(String data : this.beforeSnapShot.getData(dataTemp))
		{
			dataList.add("\t" + data);
		}
		dataList.add("}\n");
		dataList.add("afterSnapShot={\n");
		dataTemp = new ArrayList<String>();
		for(String data : this.afterSnapShot.getData(dataTemp))
		{
			dataList.add("\t" + data);
		}
		dataList.add("}\n");
		return dataList;
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
			this.beforeSnapShot.editSnapMembers.put(member.getUUID(), member.getData(new LinkedHashMap<String, String>()));
		}
	}
	void addEditAfter(GridMember member)
	{
		this.afterSnapShot.editSnapMembers.put(member.getUUID(), member.getData(new LinkedHashMap<String, String>()));
		this.setLabel("편집", this.afterSnapShot.editSnapMembers.size());
	}
	void addCreate(GridMember member)
	{
		if(!this.beforeSnapShot.removeSnapMembers.equals(member.getUUID()))
		{
			this.beforeSnapShot.removeSnapMembers.add(member.getUUID());
		}
		this.afterSnapShot.createSnapMembers.put(member.getUUID(), member.getData(new LinkedHashMap<String, String>()));
		this.setLabel("생성", this.afterSnapShot.createSnapMembers.size());
	}
	void addRemove(GridMember member)
	{
		if(!this.afterSnapShot.createSnapMembers.containsKey(member.getUUID()))
		{
			this.beforeSnapShot.createSnapMembers.put(member.getUUID(), member.getData(new LinkedHashMap<String, String>()));
			if(!this.afterSnapShot.removeSnapMembers.equals(member.getUUID()))
			{
				this.afterSnapShot.removeSnapMembers.add(member.getUUID());
			}
			this.setLabel("삭제", this.afterSnapShot.removeSnapMembers.size());
		}
		else
		{
			this.afterSnapShot.createSnapMembers.remove(member.getUUID());
			this.beforeSnapShot.removeSnapMembers.remove(member.getUUID());
		}
	}
	void addEditBefore(Grid grid)
	{
		if(!this.beforeSnapShot.editSnapGrids.containsKey(grid.getUUID()))
		{
			this.beforeSnapShot.editSnapGrids.put(grid.getUUID(), grid.getData(new LinkedHashMap<String, String>()));
		}
	}
	void addEditAfter(Grid grid)
	{
		this.afterSnapShot.editSnapGrids.put(grid.getUUID(), grid.getData(new LinkedHashMap<String, String>()));
		this.setLabel("그리드 편집", this.afterSnapShot.editSnapGrids.size());
	}
	void addCreate(Grid grid)
	{
		if(!this.beforeSnapShot.removeSnapGrids.equals(grid.getUUID()))
		{
			this.beforeSnapShot.removeSnapGrids.add(grid.getUUID());
		}
		this.afterSnapShot.createSnapGrids.put(grid.getUUID(), grid.getData(new LinkedHashMap<String, String>()));
		this.setLabel("그리드 생성", this.afterSnapShot.createSnapGrids.size());
	}
	void addRemove(Grid grid)
	{
		if(!this.afterSnapShot.createSnapGrids.containsKey(grid.getUUID()))
		{
			this.beforeSnapShot.createSnapGrids.put(grid.getUUID(), grid.getData(new LinkedHashMap<String, String>()));
			if(!this.afterSnapShot.removeSnapGrids.equals(grid.getUUID()))
			{
				this.afterSnapShot.removeSnapGrids.add(grid.getUUID());
			}
			this.setLabel("그리드 삭제", this.afterSnapShot.removeSnapGrids.size());
		}
		else
		{
			this.afterSnapShot.createSnapGrids.remove(grid.getUUID());
			this.beforeSnapShot.removeSnapGrids.remove(grid.getUUID());
		}
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
			super.setBasicImage(LogicCore.getResource().getImage("MANAGER_DFT"));
			super.setOnMouseImage(LogicCore.getResource().getImage("MANAGER_SELECT"));
			super.setBasicPressImage(LogicCore.getResource().getImage("MANAGER_PUSH"));
		}
		void setFocus(boolean focus)
		{
			if(focus)
			{
				super.setBasicImage(LogicCore.getResource().getImage("MANAGER_FOCUS"));
			}
			else
			{
				super.setBasicImage(LogicCore.getResource().getImage("MANAGER_DFT"));
			}
		}
		void setIsRemove(boolean status)
		{
			if(status)
			{
				super.setBasicImage(LogicCore.getResource().getImage("MANAGER_ISREMOVE"));
			}
			else
			{
				super.setBasicImage(LogicCore.getResource().getImage("MANAGER_DFT"));
			}
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
	LinkedHashMap<UUID, LinkedHashMap<String, String>> editSnapMembers = new LinkedHashMap<UUID, LinkedHashMap<String, String>>();
	LinkedHashMap<UUID, LinkedHashMap<String, String>> createSnapMembers = new LinkedHashMap<UUID, LinkedHashMap<String, String>>();
	ArrayList<UUID> removeSnapMembers = new ArrayList<UUID>();
	LinkedHashMap<UUID, LinkedHashMap<String, String>> editSnapGrids = new LinkedHashMap<UUID, LinkedHashMap<String, String>>();
	LinkedHashMap<UUID, LinkedHashMap<String, String>> createSnapGrids = new LinkedHashMap<UUID, LinkedHashMap<String, String>>();
	ArrayList<UUID> removeSnapGrids = new ArrayList<UUID>();
	TaskSnapShot(){};
	
	/*@SuppressWarnings("unchecked")
	TaskSnapShot(LinkedHashMap<String, String> dataMap)
	{
		Object pushData = null;
		LinkedHashMap<String, String> dataTemp = new LinkedHashMap<String, String>();
		for(String dataKey : dataMap.keySet())
		{
			switch(dataKey)
			{
			case "editSnapMembers = {":
				pushData = this.editSnapMembers;
				break;
			case "createSnapMembers = {":
				pushData = this.createSnapMembers;
				break;
			case "removeSnapMembers = {":
				pushData = this.removeSnapMembers;
				break;
			case "editSnapGrids = {":
				pushData = this.editSnapGrids;
				break;
			case "createSnapGrids = {":
				pushData = this.createSnapGrids;
				break;
			case "removeSnapGrids = {":
				pushData = this.removeSnapGrids;
				break;
			default:
				if(pushData instanceof LinkedHashMap)
				{
					if(dataKey.contains("Data = {"))
					{
						dataTemp = new LinkedHashMap<String, String>();
					}
					else if(dataKey.contains("}"))
					{
						((LinkedHashMap<UUID, LinkedHashMap<String, String>>) pushData).put(UUID.fromString(dataKey), dataTemp);
					}
					else
					{
						dataTemp.put(dataKey, dataMap.get(dataKey));
					}
				}
				else if(pushData instanceof ArrayList)
				{
					((ArrayList<UUID>) pushData).add(UUID.fromString(dataKey));
				}
			}
		}
	}*/
	ArrayList<String> getData(ArrayList<String> dataList)
	{
		dataList.add("createSnapMembers={\n");
		this.inputData(this.createSnapMembers, dataList);
		dataList.add("}\n");
		
		dataList.add("editSnapMembers={\n");
		this.inputData(this.editSnapMembers, dataList);
		dataList.add("}\n");

		dataList.add("removeSnapMembers={\n");
		this.inputData(this.removeSnapMembers, dataList);
		dataList.add("}\n");
		
		dataList.add("createSnapGrids={\n");
		this.inputData(this.createSnapGrids, dataList);
		dataList.add("}\n");
		
		dataList.add("editSnapGrids={\n");
		this.inputData(this.editSnapGrids, dataList);
		dataList.add("}\n");

		dataList.add("removeSnapGrids={\n");
		this.inputData(this.removeSnapGrids, dataList);
		dataList.add("}\n");
		
		return dataList;
	}
	private void inputData(LinkedHashMap<UUID, LinkedHashMap<String, String>> collection, ArrayList<String> dataList)
	{
		for(UUID dataKey : collection.keySet())
		{
			dataList.add("\tData={\n");
			for(String strKey : collection.get(dataKey).keySet())
			{
				dataList.add("\t\t" + strKey + "=" + collection.get(dataKey).get(strKey) + "\n");
			}
			dataList.add("\t}\n");
		}
	}
	private void inputData(ArrayList<UUID> collection, ArrayList<String> dataList)
	{
		for(UUID id : collection)
		{
			dataList.add("\t" + id.toString() + "\n");
		}
	}
}
