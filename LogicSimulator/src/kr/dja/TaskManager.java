package kr.dja;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
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
	
	private int maxSnapShot = 40;
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
				TaskUnit createTask = new TaskUnit(this, branch);
				createTask.getView().setLocation(WGAP, this.snapShots.size() * (createTask.getView().getHeight() + HGAP));
				this.taskPanel.add(createTask.getView());
				this.snapShots.add(createTask);
				break;
			}
		}
		this.setFocus(this.snapShots.get(new Integer(data.getData("focus"))));
		this.reSizeTaskPanel();
	}
	int getFocusIndex()
	{
		return this.snapShots.indexOf(this.focusUnit);
	}
	void recover(int index)
	{
		if(index >= 0 && index < this.snapShots.size())
		{
			this.recover(this.snapShots.get(index));
		}
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
		this.checkSnapShotCount();
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
			comp.setLocation(0, height);
			height += comp.getHeight() + HGAP;
		}
		this.taskPanel.setPreferredSize(new Dimension(0, height));
		this.session.getCore().getUI().getTaskManagerPanel().setManager(this);
	}
	TaskUnit setTask()
	{
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
		boolean taskEnable = false;
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
			LogicCore.putConsole("Redo " + i);
			task.redo();
			taskEnable = true;
		}
		for(int i = oldFocusIndex; i > nowFocusIndex; i--)
		{
			TaskUnit task = this.snapShots.get(i);
			LogicCore.putConsole("Undo " + i);
			task.undo();
			taskEnable = true;
		}
		if(taskEnable)
		{
			this.session.getCore().getTaskOperator().clearData(this.session.getGrid());
			this.session.getCore().getTaskOperator().checkAroundAndReserveTask(this.session.getGrid());
		}
	}
	private void checkSnapShotCount()
	{
		int i = this.snapShots.size() - this.maxSnapShot - 1;
		while(i >= 0)
		{
			TaskUnit removeSnapShot = this.snapShots.get(i);
			if(this.focusUnit == removeSnapShot)
			{
				this.deFocus();
			}
			this.taskPanel.remove(removeSnapShot.getView());
			this.snapShots.remove(removeSnapShot);
			i--;
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
		this.timeLabel.setBounds(TaskManager.COMPW - 42, 20, 42, 10);
		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
		this.timeLabel.setText(formatter.format(new Date()));
		this.snapShotView.add(this.stateLabel);
		this.snapShotView.add(this.timeLabel);
		this.commandList = new LinkedList<Command>();
	}
	TaskUnit(TaskManager manager, DataBranch data)
	{
		this(manager);
		this.timeLabel.setText(data.getData("editTime"));
		Iterator<DataBranch> itr = data.getLowerBranchIterator();
		while(itr.hasNext())
		{
			DataBranch info = itr.next();
			Command cmd = TaskUnit.Factory(info, manager.getSession());
			this.commandList.add(cmd);
			this.addLabel(cmd);
		}
		this.setLabelText();
	}
	DataBranch getData()
	{
		DataBranch data = new DataBranch("TaskUnit");
		data.setData("editTime", this.timeLabel.getText());
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
			labelText += LogicCore.getResource().getLocal(key) + "(" + this.labels.get(key) + ") ";
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
			LogicCore.putConsole(e.toString());
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
		member.setUUID();
		member.getData(super.masterData);
		if(member instanceof LogicBlock)
		{
			absX = absX > 0 ? (absX + member.getUIabsSizeX() / 2) - 1 : absX - member.getUIabsSizeX() / 2;
			absY = absY > 0 ? (absY + member.getUIabsSizeY() / 2) - 1 : absY - member.getUIabsSizeX() / 2;
		}
		super.masterData.setData("CommandAbsX", Integer.toString(absX));
		super.masterData.setData("CommandAbsY", Integer.toString(absY));
		if(member instanceof LogicBlock)
		{
			LogicBlock block = grid.getLogicBlock(absX / Size.REGULAR_SIZE, absY / Size.REGULAR_SIZE);
			if(block != null)
			{
				grid.getSession().getTaskManager().setTask().addCommand(new RemoveMemberOnGrid(block));
			}
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
		super.session.getGrid().addMember(GridMember.Factory(session.getCore(), super.masterData)
				, new Integer(super.masterData.getData("UIabslocationX")), new Integer(super.masterData.getData("UIabslocationY")));
	}
}
class SetLogicBlockIO extends GridMemberCommand
{
	SetLogicBlockIO(LogicBlock block, Direction ext, Session session)
	{
		super("SetLogicBlockIO", session, block);
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
		((LogicBlock)super.getMember()).setIO(Direction.valueOf(super.masterData.getData("Direction")), IOStatus.valueOf(super.redoData.getData("IOStatus")));
		super.session.getCore().getUI().getBlockControlPanel().updateMemberStatus();
	}
	@Override
	public void undo()
	{
		((LogicBlock)super.getMember()).setIO(Direction.valueOf(super.masterData.getData("Direction")), IOStatus.valueOf(super.undoData.getData("IOStatus")));
		super.session.getCore().getUI().getBlockControlPanel().updateMemberStatus();
	}
}
class SetMemberColor extends GridMemberCommand
{
	SetMemberColor(GridMember member, String tag, Color color, Session session)
	{
		super("SetMemberColor", session, member);
		ColorSet colorMember = ((ColorSet)member);
		super.masterData.setData("tag", tag);
		super.undoData.setData("ColorR", Integer.toString(colorMember.getColor(tag).getRed()));
		super.undoData.setData("ColorG", Integer.toString(colorMember.getColor(tag).getGreen()));
		super.undoData.setData("ColorB", Integer.toString(colorMember.getColor(tag).getBlue()));
		colorMember.setColor(tag, color);
		super.redoData.setData("ColorR", Integer.toString(color.getRed()));
		super.redoData.setData("ColorG", Integer.toString(color.getGreen()));
		super.redoData.setData("ColorB", Integer.toString(color.getBlue()));
	}
	SetMemberColor(DataBranch branch, Session session)
	{
		super(branch, session);
	}
	@Override
	public void redo()
	{
		this.setColor(super.redoData);
		super.getMember().getCore().getUI().getBlockControlPanel().updateMemberStatus();
	}
	@Override
	public void undo()
	{
		this.setColor(super.undoData);
		super.getMember().getCore().getUI().getBlockControlPanel().updateMemberStatus();
	}
	private void setColor(DataBranch branch)
	{
		int r = Integer.parseInt(branch.getData("ColorR"));
		int g = Integer.parseInt(branch.getData("ColorG"));
		int b = Integer.parseInt(branch.getData("ColorB"));
		((ColorSet)super.getMember()).setColor(super.masterData.getData("tag"), new Color(r, g, b));
	}
}
class SetMemberSize extends GridMemberCommand
{
	SetMemberSize(GridMember member, int x, int y, int w, int h, Session session)
	{
		super("SetMemberSize", session, member);
		this.putSizeInBranch(super.undoData, (SizeSet)member);
		((SizeSet)member).setSize(x, y, w, h);
		this.putSizeInBranch(super.redoData, (SizeSet)member);
	}
	SetMemberSize(DataBranch branch, Session session)
	{
		super(branch, session);
	}
	@Override
	void redo()
	{
		this.setSizeInBranch(this.redoData);
	}
	@Override
	void undo()
	{
		this.setSizeInBranch(this.undoData);
	}
	private void putSizeInBranch(DataBranch branch, SizeSet member)
	{
		branch.setData("X", Integer.toString(member.getUIabsLocationX()));
		branch.setData("Y", Integer.toString(member.getUIabsLocationY()));
		branch.setData("W", Integer.toString(member.getUIabsSizeX()));
		branch.setData("H", Integer.toString(member.getUIabsSizeY()));
	}
	private void setSizeInBranch(DataBranch branch)
	{
		int[] arr = new int[4];
		arr[0] = Integer.parseInt(branch.getData("X"));
		arr[1] = Integer.parseInt(branch.getData("Y"));
		arr[2] = Integer.parseInt(branch.getData("W"));
		arr[3] = Integer.parseInt(branch.getData("H"));
		((SizeSet)super.getMember()).setSize(arr[0], arr[1], arr[2], arr[3]);
	}
}
class SetTagDescription extends GridMemberCommand
{
	SetTagDescription(Tag member, String description, Session session)
	{
		super("SetTagDescription", session, member);
		super.undoData.addLowerBranch(this.getDescriptionBranch(member));
		member.setDescription(description);
		super.redoData.addLowerBranch(this.getDescriptionBranch(member));
	} 
	SetTagDescription(DataBranch branch, Session session)
	{
		super(branch, session);
	}
	@Override
	void redo()
	{
		this.setMemberDescription(super.redoData);
	}
	@Override
	void undo()
	{
		this.setMemberDescription(super.undoData);
	}
	private void setMemberDescription(DataBranch branch)
	{
		Iterator<DataBranch> itr = branch.getLowerBranchIterator();
		while(itr.hasNext())
		{
			DataBranch lowerBranch = itr.next();
			switch(lowerBranch.getName())
			{
			case "Description":
				String text = new String();
				Iterator<String> keyItr = lowerBranch.getDataKeySetIterator();
				while(keyItr.hasNext())
				{
					text += lowerBranch.getData(keyItr.next()) + "\n";
				}
				this.getTag().setDescription(text);
				break;
			}
		}
	}
	private DataBranch getDescriptionBranch(Tag member)
	{
		DataBranch description = new DataBranch("Description");
		String[] text = member.getDescription().split("\\r?\\n");
		for(int i = 0; i < text.length; i++)
		{
			description.setData(Integer.toString(i), text[i]);
		}
		return description;
	}
	private Tag getTag()
	{
		return (Tag)super.session.getGrid().getMember(UUID.fromString(super.masterData.getData("GridMember")));
	}
}
class WireTypeEdit extends GridMemberCommand
{
	WireTypeEdit(Wire member, WireType type, Session session)
	{
		super("WireTypeEdit", session, member);
		this.getData(type, super.undoData);
		member.setWireType(type);
		this.getData(type, super.redoData);
	}
	WireTypeEdit(DataBranch arg0, Session arg1)
	{
		super(arg0, arg1);
	}
	@Override
	void redo()
	{
		this.setData(super.redoData);
	}
	@Override
	void undo()
	{
		this.setData(super.undoData);
	}
	private void setData(DataBranch branch)
	{
		Wire wire = ((Wire)super.getMember());
		for(Direction ext : Direction.values())
		{
			wire.setIO(ext, IOStatus.valueOf(branch.getData(ext.toString())));
		}
		wire.setWireType(WireType.valueOf(branch.getData("Type")));
		super.session.getCore().getUI().getBlockControlPanel().updateMemberStatus();
	}
	private void getData(WireType type, DataBranch branch)
	{
		for(Direction ext : Direction.values())
		{
			branch.setData(ext.toString(), ((Wire)super.getMember()).getIOStatus(ext).toString());
		}
		branch.setData("Type", ((Wire)super.getMember()).getType().toString());
	}
}
class SetBlockActive extends GridMemberCommand
{
	SetBlockActive(LogicBlock block, boolean option)
	{
		super("SetBlockActive", block.getGrid().getSession(), block);
		super.undoData.setData("status", Boolean.toString(block.getActive()));
		block.setActive(option);
		super.redoData.setData("status", Boolean.toString(block.getActive()));
	}
	SetBlockActive(DataBranch arg0, Session arg1)
	{
		super(arg0, arg1);
	}
	@Override
	void redo()
	{
		((LogicBlock)super.getMember()).setActive(Boolean.parseBoolean(super.redoData.getData("status")));;
	}
	@Override
	void undo()
	{
		((LogicBlock)super.getMember()).setActive(Boolean.parseBoolean(super.undoData.getData("status")));;
	}
}
class SetBlockTimer extends GridMemberCommand
{
	SetBlockTimer(TimeSetter member, String tag, int time, Session session)
	{
		super("SetBlockTimer", session, (GridMember)member);
		super.masterData.setData("tag", tag);
		super.undoData.setData("time", Integer.toString(member.getTime(tag)));
		member.setTime(tag, time);
		super.redoData.setData("time", Integer.toString(member.getTime(tag)));
	}
	SetBlockTimer(DataBranch branch, Session session)
	{
		super(branch, session);
	}
	@Override
	void redo()
	{
		((TimeSetter)super.getMember()).setTime(super.masterData.getData("tag"), Integer.parseInt(super.redoData.getData("time")));
		super.getMember().getCore().getUI().getBlockControlPanel().updateMemberStatus();
	}
	@Override
	void undo()
	{
		((TimeSetter)super.getMember()).setTime(super.masterData.getData("tag"), Integer.parseInt(super.undoData.getData("time")));
		super.getMember().getCore().getUI().getBlockControlPanel().updateMemberStatus();
	}
}
abstract class GridMemberCommand extends Command
{
	protected final UUID memberID;
	GridMemberCommand(String name, Session session, GridMember member)
	{
		super(name, session);
		this.memberID = member.getUUID();
		super.masterData.setData("GridMember", member.getUUID().toString());
	}
	GridMemberCommand(DataBranch branch, Session session)
	{
		super(branch, session);
		this.memberID = UUID.fromString(super.masterData.getData("GridMember"));
	}
	protected GridMember getMember()
	{
		return (GridMember)super.session.getGrid().getMember(UUID.fromString(super.masterData.getData("GridMember")));
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
		LogicCore.putConsole("Command: " + name);
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