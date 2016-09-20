package kr.dja;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
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
	public static final int WGAP = 0;
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
	TaskManager(Session session, DataBranch data)
	{
		this.session = session;
		this.taskPanel = new JPanel();
		this.taskPanel.setLayout(null);
		this.lastCheckTime = 0;
		Iterator<DataBranch> branchItr = data.getLowerBranchIterator();
		while(branchItr.hasNext())
		{
			
			DataBranch branch = branchItr.next();
			
			switch(branch.getName())
			{
			case "TaskUnit":
				TaskUnit createTask = new TaskUnit(this, session, branch);
				createTask.getView().setLocation(WGAP, this.snapShots.size() * (createTask.getView().getHeight() + HGAP));
				this.taskPanel.add(createTask.getView());
				this.snapShots.add(createTask);
				break;
			}
		}
		this.setFocus(this.snapShots.get(new Integer(data.getData("focus"))));
		this.reSizeTaskPanel();
	}
	DataBranch getData()
	{
		DataBranch data = new DataBranch("TaskManager");
		if(this.focusUnit != null)
		{
			data.setData("focus", Integer.toString(this.snapShots.indexOf(this.focusUnit)));
		}
		else
		{
			data.setData("focus", Integer.toString(0));
		}
		for(TaskUnit unit : this.snapShots)
		{
			data.addLowerBranch(unit.getData());
		}
		return data;
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
		//task.setFirstLabel("초기");
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
		if(this.getLastSnapShot() == null || (this.lastCheckTime + checkTime < System.currentTimeMillis() / 1000 && this.getLastSnapShot().isEdit()))
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
			task.redo();
		}
		for(int i = oldFocusIndex; i > nowFocusIndex; i--)
		{
			TaskUnit task = this.snapShots.get(i);
			System.out.println("UNDO " + this.snapShots.indexOf(task));
			task.undo();
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
	private LinkedList<Command> commandList;
	boolean isEdit = false;
	
	private TaskButton snapShotView;
	private JLabel stateLabel;
	private LinkedHashMap<String, Integer> labels = new LinkedHashMap<String, Integer>();
	private JLabel timeLabel;
	TaskUnit(TaskManager manager)
	{
		this.manager = manager;
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
		this.commandList = new LinkedList<Command>();
	}
	TaskUnit(TaskManager manager, Session session, DataBranch data)
	{
		this(manager);
		Iterator<DataBranch> itr = data.getLowerBranchIterator();
		while(itr.hasNext())
		{
			DataBranch info = itr.next();
			System.out.println(info.getName());
			Command cmd = TaskUnit.Factory(info, session);
			this.commandList.add(cmd);
			this.addLabel(cmd);
		}
		this.setLabelText();
	}
	DataBranch getData()
	{
		DataBranch data = new DataBranch("TaskUnit");
		for(Command cmd : this.commandList)
		{
			data.addLowerBranch(cmd.masterData);
		}
		return data;
	}
	void addCommand(Command cmd)
	{
		this.commandList.add(cmd);
		this.addLabel(cmd);
		this.setLabelText();
	}
	private void addLabel(Command cmd)
	{
		String name = cmd.masterData.getName();
		if(this.labels.containsKey(name))
		{
			this.labels.put(name, new Integer(this.labels.get(name)) + 1);
		}
		else
		{
			this.labels.put(name, new Integer(1));
		}
	}
	private void setLabelText()
	{
		String labelText = new String();
		for(String key : this.labels.keySet())
		{
			labelText += key + "(" + this.labels.get(key) + ") ";
		}
		this.stateLabel.setText(labelText);
	}
	void redo()
	{
		Iterator<Command> itr = commandList.iterator();
		while(itr.hasNext())
		{
			itr.next().redo();
		}
	}
	void undo()
	{
		Iterator<Command> itr = commandList.descendingIterator();
		while(itr.hasNext())
		{
			itr.next().undo();
		}
	}
	TaskButton getView()
	{
		return this.snapShotView;
	}
	void setEdit()
	{
		this.isEdit = true;
	}
	boolean isEdit()
	{
		return this.isEdit;
	}
	static Command Factory(DataBranch info, Session session)
	{
		Command command = null;
		try
		{//리플렉션
			command = (Command)Class.forName(info.getData("CommandPath")).getDeclaredConstructor(new Class[]{DataBranch.class, Session.class}).newInstance(info, session);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return command;
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
class EditGridSize extends Command
{
	EditGridSize(Grid grid, Direction ext, int size)
	{
		super("EditGridSize", grid.getSession());
		super.masterData.setData("Grid", grid.getUUID().toString());
		this.setSizeData(super.undoData, grid.getGridSize());
		grid.gridResize(ext, size);
		this.setSizeData(super.redoData, grid.getGridSize());
	}
	EditGridSize(DataBranch branch, Session session)
	{
		super(branch, session);
	}
	private void setSizeData(DataBranch data, SizeInfo size)
	{
		data.setData("sizeX", Integer.toString(size.getX()));
		data.setData("sizeY", Integer.toString(size.getY()));
		data.setData("sizeNX", Integer.toString(size.getNX()));
		data.setData("sizeNY", Integer.toString(size.getNY()));
	}
	private SizeInfo getSizeData(DataBranch data)
	{
		SizeInfo size = new SizeInfo(new Integer(data.getData("sizeX")), new Integer(data.getData("sizeY"))
									, new Integer(data.getData("sizeNX")), new Integer(data.getData("sizeNY")));
		return size;
	}
	@Override
	public void redo()
	{
		super.session.getGrid().gridResize(this.getSizeData(super.redoData));
	}
	@Override
	public void undo()
	{
		super.session.getGrid().gridResize(this.getSizeData(super.undoData));
	}
}
class PutMemberOnGrid extends Command
{
	PutMemberOnGrid(Grid grid, GridMember member, int absX, int absY)
	{
		super("PutMemberonGrid", grid.getSession());
		super.masterData.setData("CommandAbsX", Integer.toString(absX));
		super.masterData.setData("CommandAbsY", Integer.toString(absY));
		member.setUUID();
		member.getData(super.masterData);
		LogicBlock block = grid.getLogicBlock(absX / Size.REGULAR_SIZE, absY / Size.REGULAR_SIZE);
		if(block != null)
		{
			grid.getSession().getTaskManager().setTask().addCommand(new RemoveMemberOnGrid(block));
		}
		grid.addMember(member, absX, absY);
	}
	PutMemberOnGrid(DataBranch branch, Session session)
	{
		super(branch, session);
	}
	@Override
	public void redo()
	{
		super.session.getGrid().addMember(GridMember.Factory(session.getCore(), super.masterData)
				, new Integer(super.masterData.getData("CommandAbsX")), new Integer(super.masterData.getData("CommandAbsY")));
	}
	@Override
	public void undo()
	{
		super.session.getGrid().removeMember(UUID.fromString(super.masterData.getData("id")));
	}
}
class RemoveMemberOnGrid extends Command
{
	RemoveMemberOnGrid(GridMember member)
	{
		super("RemoveMemberOnGrid", member.getGrid().getSession());
		member.getData(super.masterData);
		member.getGrid().removeMember(member.getUUID());
	}
	RemoveMemberOnGrid(DataBranch branch, Session session)
	{
		super(branch, session);
	}
	@Override
	public void redo()
	{
		super.session.getGrid().removeMember(UUID.fromString(super.masterData.getData("id")));
	}
	@Override
	public void undo()
	{
		System.out.println("UNDOUNDO");
		super.session.getGrid().addMember(GridMember.Factory(session.getCore(), super.masterData)
				, new Integer(super.masterData.getData("UIabslocationX")), new Integer(super.masterData.getData("UIabslocationY")));
	}
}
class SetLogicBlockIO extends Command
{
	SetLogicBlockIO(LogicBlock block, Direction ext, Session session)
	{
		super("SetLogicBlockIO", session);
		super.masterData.setData("Direction", ext.getTag());
		super.masterData.setData("LogicBlock", block.getUUID().toString());
		super.undoData.setData("IOStatus", block.getIOStatus(ext).toString());
		block.toggleIO(ext);
		super.redoData.setData("IOStatus", block.getIOStatus(ext).toString());
	}
	SetLogicBlockIO(DataBranch branch, Session session)
	{
		super(branch, session);
	}
	@Override
	public void redo()
	{
		this.getBlock().setIO(Direction.valueOf(super.masterData.getData("Direction")), IOStatus.valueOf(super.redoData.getData("IOStatus")));
	}
	@Override
	public void undo()
	{
		this.getBlock().setIO(Direction.valueOf(super.masterData.getData("Direction")), IOStatus.valueOf(super.undoData.getData("IOStatus")));
	}
	private LogicBlock getBlock()
	{
		return (LogicBlock)super.session.getGrid().getMember(UUID.fromString(super.masterData.getData("LogicBlock")));
	}
}
abstract class Command
{
	protected Session session;
	protected DataBranch masterData;
	protected DataBranch redoData;
	protected DataBranch undoData;
	Command(String name, Session session)
	{
		this.masterData = new DataBranch(name);
		this.masterData.setData("CommandPath", this.getClass().getName());
		this.session = session;
		this.redoData = new DataBranch("RedoData");
		this.undoData = new DataBranch("UndoData");
		this.masterData.addLowerBranch(this.redoData);
		this.masterData.addLowerBranch(this.undoData);
	}
	Command(DataBranch branch, Session session)
	{
		this.masterData = branch;
		Iterator<DataBranch> dataItr = branch.getLowerBranchIterator();
		while(dataItr.hasNext())
		{
			DataBranch data = dataItr.next();
			switch(data.getName())
			{
			case "RedoData":
				this.redoData = data;
				break;
			case "UndoData":
				this.undoData = data;
				break;
			}
		}
		this.session = session;
	}
	abstract void redo();
	abstract void undo();
}
