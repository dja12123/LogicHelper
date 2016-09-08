package kr.dja;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.*;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

public class Grid
{
	static int count = 0;
	
	private UUID id;
	
	private HashMap<UUID, GridMember> members = new HashMap<UUID, GridMember>();
	private HashMap<Integer, HashMap<Integer, LogicBlock>> logicMembers = new HashMap<Integer, HashMap<Integer, LogicBlock>>();
	
	private ArrayList<GridMember> selectMembers = new ArrayList<GridMember>();
	private ArrayList<GridMember> selectSignMembers = new ArrayList<GridMember>();
	
	private Session session;

	private GridPanel gridPanel;
	private SizeInfo gridSize;
	private int MAX_SIZE = 100;
	private int MAX_ABSOLUTE = 150;
	
	private GridMember selectFocusMember;

	private Color selectSignColor = new Color(255, 255, 30, 0);
	private Color selectColor = new Color(120, 180, 255, 50);
	private Color selectFocusColor = new Color(120, 255, 180, 50);
	
	Grid(Session session, SizeInfo size, UUID id)
 	{
		Grid.count++;
		
		this.id = id;
		this.session = session;
		this.gridSize = size;
		this.gridPanel = new GridPanel();
		System.out.println("create " + Grid.count);
		
		JLabel label = new JLabel(Integer.toString(Grid.count) + " 번째 생성된 그리드");
		label.setBounds(0, 0, 200, 30);
		this.gridPanel.add(label);
	}
	HashMap<String, String> getData(HashMap<String, String> dataMap)
	{
		dataMap.put("gridSizeX", Integer.toString(this.gridSize.getX()));
		dataMap.put("gridSizeY", Integer.toString(this.gridSize.getY()));
		dataMap.put("gridSizeNX", Integer.toString(this.gridSize.getNX()));
		dataMap.put("gridSizeNY", Integer.toString(this.gridSize.getNY()));
		dataMap.put("MAX_SIZE", Integer.toString(this.MAX_SIZE));
		dataMap.put("MAX_ABSOLUTE", Integer.toString(this.MAX_ABSOLUTE));
		return dataMap;
	}
	Session getSession()
	{
		return this.session;
	}
	void setData(HashMap<String, String> dataMap)
	{
		SizeInfo size = new SizeInfo(new Integer(dataMap.get("gridSizeX")), new Integer(dataMap.get("gridSizeY"))
		, new Integer(dataMap.get("gridSizeNX")), new Integer(dataMap.get("gridSizeNY")));
		this.gridResize(size, false);
		this.MAX_SIZE = new Integer(dataMap.get("MAX_SIZE"));
		this.MAX_ABSOLUTE = new Integer(dataMap.get("MAX_ABSOLUTE"));
	}
	UUID getUUID()
	{
		return this.id;
	}
	SizeInfo getGridSize()
	{
		return new SizeInfo(this.gridSize);
	}
	void gridResize(Direction ext, int size)
	{//�� �ۼ� �ʿ�
		TaskUnit task = this.session.getTaskManager().setTask();
		task.addEditBefore(this);
		if(ext == Direction.EAST)
		{
			if(gridSize.getX() + size < 1)
			{//������ ����ġ ����
				size -= gridSize.getX() + size - 1;
			}
			if(Math.abs(gridSize.getX() + size - gridSize.getNX()) > MAX_ABSOLUTE + 1)
			{//������ �ִ� ���� �Ѱ� ����
				size -= Math.abs(gridSize.getX() + size - gridSize.getNX()) - MAX_ABSOLUTE - 1;
			}
			if(gridSize.getX() + size > MAX_SIZE)
			{//������ �ִ� ũ�� �Ѱ� ����
				size -= gridSize.getX() + size - MAX_SIZE;
			}
			this.gridSize.setData(new SizeInfo(gridSize.getX() + size, gridSize.getY(), gridSize.getNX(), gridSize.getNY()));
			this.session.getCore().getUI().getGridArea().setViewPosition(new Point(gridPanel.getWidth(), this.session.getCore().getUI().getGridArea().getViewPosition().y));
		}
		else if(ext == Direction.WEST)
		{
			if(gridSize.getX() + size < 1)
			{//������ ����ġ ����
				size -= gridSize.getX() + size - 1;
			}
			if(Math.abs(size + gridSize.getNX()) > MAX_ABSOLUTE)
			{//������ �ִ� ���� �Ѱ� ����
				size -= Math.abs(size + gridSize.getNX()) - MAX_ABSOLUTE;
			}
			if(gridSize.getX() + size > MAX_SIZE)
			{
				size -= gridSize.getX() + size - MAX_SIZE;
			}//������ �ִ� ũ�� �Ѱ� ����
			this.gridSize.setData(new SizeInfo(gridSize.getX() + size, gridSize.getY(), gridSize.getNX() + size, gridSize.getNY()));
			this.session.getCore().getUI().getGridArea().setViewPosition(new Point(0, this.session.getCore().getUI().getGridArea().getViewPosition().y));
		}
		else if(ext == Direction.SOUTH)
		{
			if(gridSize.getY() + size < 1)
			{//������ ����ġ ����
				size -= gridSize.getY() + size - 1;
			}
			if(Math.abs(gridSize.getY() + size - gridSize.getNY()) > MAX_ABSOLUTE + 1)
			{//������ �ִ� ���� �Ѱ� ����
				size -= Math.abs(gridSize.getY() + size - gridSize.getNY()) - MAX_ABSOLUTE - 1;
			}
			if(gridSize.getY() + size > MAX_SIZE)
			{//������ �ִ� ũ�� �Ѱ� ����
				size -= gridSize.getY() + size - MAX_SIZE;
			}
			this.gridSize.setData(new SizeInfo(gridSize.getX(), gridSize.getY() + size, gridSize.getNX(), gridSize.getNY()));
			this.session.getCore().getUI().getGridArea().setViewPosition(new Point(this.session.getCore().getUI().getGridArea().getViewPosition().x, gridPanel.getHeight()));
		}
		else if(ext == Direction.NORTH)
		{
			if(gridSize.getY() + size < 1)
			{//������ ����ġ ����
				size -= gridSize.getY() + size - 1;
			}
			if(Math.abs(size + gridSize.getNY()) > MAX_ABSOLUTE)
			{//������ �ִ� ���� �Ѱ� ����
				size -= Math.abs(size + gridSize.getNY()) - MAX_ABSOLUTE;
			}
			if(gridSize.getY() + size > MAX_SIZE)
			{
				size -= gridSize.getY() + size - MAX_SIZE;
			}//������ �ִ� ũ�� �Ѱ� ����
			this.gridSize.setData(new SizeInfo(gridSize.getX(), gridSize.getY() + size, gridSize.getNX(), gridSize.getNY() + size));
			this.session.getCore().getUI().getGridArea().setViewPosition(new Point(this.session.getCore().getUI().getGridArea().getViewPosition().x, 0));
		}
		this.deSelectAll();
		this.session.getCore().getUI().getGridArea().sizeUpdate();
		List<GridMember> tempMembers = new ArrayList<GridMember>(); //ConcurrentModificationException 방지용
		for(UUID memberID : getMembers().keySet())
		{
			tempMembers.add(getMembers().get(memberID));
		}
		for(GridMember member : tempMembers)
		{
			if((gridPanel.getWidth() - Size.MARGIN < member.getGridViewPane().getX() + member.getGridViewPane().getWidth() || Size.MARGIN > member.getGridViewPane().getX())
			|| (gridPanel.getHeight() - Size.MARGIN < member.getGridViewPane().getY() + member.getGridViewPane().getHeight() || Size.MARGIN > member.getGridViewPane().getY()))
			{
				removeMember(member.getUUID(), true);
			}
		}
		task.addEditAfter(this);
		this.session.getCore().getUI().getUnderBar().setGridSizeInfo(this.gridSize);
	}
	void gridResize(SizeInfo size, boolean record)
	{
		TaskUnit task = null;
		if(record)
		{
			task = this.session.getTaskManager().setTask();
			task.addEditBefore(this);
		}
		this.gridSize.setData(size);
		this.deSelectAll();
		this.session.getCore().getUI().getUnderBar().setGridSizeInfo(this.gridSize);
		this.session.getCore().getUI().getGridArea().sizeUpdate();
		if(record)
		{
			task.addEditAfter(this);
		}
	}
	GridPanel getGridPanel()
	{
		return this.gridPanel;
	}
	void addMember(GridMember member, int absX, int absY, boolean record)
	{
		if(absX < (this.gridSize.getX() - this.gridSize.getNX()) * Size.REGULAR_SIZE && absY < (this.gridSize.getY() - this.gridSize.getNY()) * Size.REGULAR_SIZE
		&& absX > - (this.gridSize.getNX() + 1) * Size.REGULAR_SIZE && absY > - (this.gridSize.getNY() + 1) * Size.REGULAR_SIZE)
		{
			if(record)
			{
				member.setUUID();
			}
			this.getMembers().put(member.getUUID(), member);
			member.put(absX, absY, this);
			if(member instanceof LogicBlock)
			{
				LogicBlock logicMember = (LogicBlock)member;
				if(this.logicMembers.containsKey(new Integer(logicMember.getBlockLocationX()))
						&& this.logicMembers.get(new Integer(logicMember.getBlockLocationX())).containsKey(logicMember.getBlockLocationY()))
				{
					System.out.println("삭제콜");
					this.removeMember(this.logicMembers.get(new Integer(logicMember.getBlockLocationX())).get(logicMember.getBlockLocationY()).getUUID(), record);
				}
				if(!this.logicMembers.containsKey(new Integer(logicMember.getBlockLocationX())))
				{
					this.logicMembers.put(new Integer(logicMember.getBlockLocationX()), new HashMap<Integer, LogicBlock>());
				}
				this.logicMembers.get(new Integer(logicMember.getBlockLocationX())).put(new Integer(logicMember.getBlockLocationY()), logicMember);
				this.session.getCore().getTaskOperator().checkAroundAndReserveTask(logicMember);
			}
			this.getGridPanel().add(member.getGridViewPane());
			member.getGridViewPane().repaint();
			if(record)
			{
				this.selectFocus(member);
				TaskUnit task = this.session.getTaskManager().setTask();
				task.addCreate(member);
				task.setFirstLabel("(" + member.getUIabsLocationX() + ", " + member.getUIabsLocationY() + ")");
			}
		}
	}
	void removeMember(UUID id, boolean record)
	{
		System.out.println("removeMember " + id.toString());

		GridMember removeMember = this.members.get(id);
		removeMember.remove();
		this.members.remove(id);
		this.getGridPanel().remove(removeMember.getGridViewPane());
		this.deSelect(removeMember);
		this.getGridPanel().repaint();
		if(removeMember instanceof LogicBlock)
		{
			LogicBlock removeBlock = (LogicBlock)removeMember;
			this.logicMembers.get(new Integer(removeBlock.getBlockLocationX())).remove(new Integer(removeBlock.getBlockLocationY()));
			if(this.logicMembers.get(new Integer(removeBlock.getBlockLocationX())).size() == 0)
			{
				this.logicMembers.remove(new Integer(removeBlock.getBlockLocationX()));
			}
			this.session.getCore().getTaskOperator().removeReserveTask(removeBlock);
			this.session.getCore().getTaskOperator().checkAroundAndReserveTask(removeBlock);
		}
		if(record)
		{
			TaskUnit task = this.session.getTaskManager().setTask();
			task.addRemove(removeMember);
			task.setFirstLabel("(" + removeMember.getUIabsLocationX() + ", " + removeMember.getUIabsLocationY() + ")");
		}
		
	}
	/*void recover(HashMap<HashMap<String, String>, Boolean> dataMap, SizeInfo sizeInfo, boolean back)
	{
		this.gridResize(sizeInfo);
		for(HashMap<String, String> data : dataMap.keySet())
		{
			boolean placeStatus = dataMap.get(data);
			
			if(placeStatus == back)
			{
				GridMember member = GridMember.Factory(this.session.getCore(), data);
				member.put(member.getUIabsLocationX(), member.getUIabsLocationY(), this);
			}
			else
			{
				this.removeMember(UUID.fromString(data.get("id")));
			}
		}
	}*/
	HashMap<UUID, GridMember> getMembers()
	{
		return this.members;
	}
	LogicBlock getLogicBlock(int absX, int absY)
	{
		if(this.logicMembers.containsKey(absX))
		{
			if(this.logicMembers.get(new Integer(absX)).containsKey(absY))
			{
				return this.logicMembers.get(new Integer(absX)).get(absY);
			}
		}
		return null;
	}
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
			new ManySelectEditPanel(this.selectMembers, this.session.getCore().getUI().getBlockControlPanel());
		}
		else if(this.selectFocusMember == null)
		{
			this.session.getCore().getUI().getBlockControlPanel().removeControlPane();
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
			new ManySelectEditPanel(this.selectMembers, this.session.getCore().getUI().getBlockControlPanel());
		}
		else if(this.selectFocusMember == null)
		{
			this.session.getCore().getUI().getBlockControlPanel().removeControlPane();
		}
	}
	void deSelect(GridMember member)
	{
		ArrayList<GridMember> temp = new ArrayList<GridMember>();
		temp.add(member);
		this.deSelect(temp);
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
	void deSelectAll()
	{
		this.session.getCore().getUI().getBlockControlPanel().removeControlPane();
		for(GridMember member : this.selectMembers)
		{
			member.removeSelectView();
		}
		for(GridMember member : this.selectSignMembers)
		{
			member.removeSelectView();
		}
		this.deSelectFocus();
		this.selectFocusMember = null;
		this.selectMembers = new ArrayList<GridMember>();
		this.selectSignMembers = new ArrayList<GridMember>();
		this.gridPanel.repaint();
	}
	void selectFocus(GridMember member)
	{
		this.deSelectAll();
		this.selectFocusMember = member;
		member.setSelectView(this.selectFocusColor);
		EditPane editer = this.session.getCore().getUI().getPalettePanel().getControl(member);
		this.session.getCore().getUI().getBlockControlPanel().addControlPanel(editer);
	}
	void deSelectFocus()
	{
		if(this.selectFocusMember != null)
		{
			this.selectFocusMember.removeSelectView();
			this.selectFocusMember = null;
		}
	}
	class GridPanel extends JPanel implements SizeUpdate
	{
		private static final long serialVersionUID = 1L;
		GridPanel()
		{
			this.setLayout(null);
			this.setBackground(new Color(210, 210, 215));
			this.sizeUpdate();
		}
		@Override
		public void paintComponent(Graphics g)
		{
			super.paintComponent(g);
			g.setColor(new Color(190, 190, 200));
			for(int x = 0; x <= gridSize.getX(); x++)
			{
				//g.drawLine((x * core.getUI().getUISize().getWidth()) + Size.MARGIN, Size.MARGIN, (x * core.getUI().getUISize().getWidth()) + Size.MARGIN, (gridSize.getY() * core.getUI().getUISize().getWidth()) + Size.MARGIN);
				g.fillRect((x * session.getCore().getUI().getUISize().getWidth()) + Size.MARGIN - 1, Size.MARGIN, 2, (gridSize.getY() * session.getCore().getUI().getUISize().getWidth()));
			}
			for(int y = 0; y <= gridSize.getY(); y++)
			{
				//g.drawLine(Size.MARGIN, (y * core.getUI().getUISize().getWidth()) + Size.MARGIN, (gridSize.getX() * core.getUI().getUISize().getWidth()) + Size.MARGIN, (y * core.getUI().getUISize().getWidth()) + Size.MARGIN);
				g.fillRect(Size.MARGIN, (y * session.getCore().getUI().getUISize().getWidth()) + Size.MARGIN - 1, (gridSize.getX() * session.getCore().getUI().getUISize().getWidth()), 2);
			}
		}
		@Override
		public void sizeUpdate()
		{
			this.setSize((gridSize.getX() * session.getCore().getUI().getUISize().getWidth()) + (Size.MARGIN * 2), (gridSize.getY() * session.getCore().getUI().getUISize().getWidth()) + (Size.MARGIN * 2));
			for(UUID memberID : getMembers().keySet())
			{
				GridMember member = getMembers().get(memberID);
				member.sizeUpdate();
				member.getGridViewPane().setLocation((member.getUIabsLocationX() + (gridSize.getNX() * Size.REGULAR_SIZE)) * session.getCore().getUI().getUISize().getmultiple() + Size.MARGIN
						, (member.getUIabsLocationY() + (gridSize.getNY() * Size.REGULAR_SIZE)) * session.getCore().getUI().getUISize().getmultiple() + Size.MARGIN);
			}
			this.repaint();
		}
	}
}
class SizeInfo
{
	private int sizeX;
	private int sizeY;
	private int negativeExtendsX;
	private int negativeExtendsY;
	SizeInfo(int sizeX, int sizeY, int negativeExtendsX, int negativeExtendsY)
	{
		this.sizeX = sizeX;
		this.sizeY = sizeY;
		this.negativeExtendsX = negativeExtendsX;
		this.negativeExtendsY = negativeExtendsY;
	}
	SizeInfo(SizeInfo info)
	{
		this.sizeX = info.getX();
		this.sizeY = info.getY();
		this.negativeExtendsX = info.getNX();
		this.negativeExtendsY = info.getNY();
	}
	int getX()
	{
		return this.sizeX;
	}
	int getY()
	{
		return this.sizeY;
	}
	int getNX()
	{
		return this.negativeExtendsX;
	}
	int getNY()
	{
		return this.negativeExtendsY;
	}
	void setData(SizeInfo info)
	{
		this.sizeX = info.getX();
		this.sizeY = info.getY();
		this.negativeExtendsX = info.getNX();
		this.negativeExtendsY = info.getNY();
	}
}
enum Direction
{
	EAST("EAST", 1, 0, "WEST"), WEST("WEST", -1, 0, "EAST"), SOUTH("SOUTH", 0, 1, "NORTH"), NORTH("NORTH", 0, -1, "SOUTH");
	
	public final String tag;
	public final int wayX;
	public final int wayY;
	public final String across;
	
	private Direction(String tag, int wayX, int wayY, String across)
	{
		this.tag = tag;
		this.wayX = wayX;
		this.wayY = wayY;
		this.across = across;
	}
	public int getWayX()
	{
		return this.wayX;
	}
	public int getWayY()
	{
		return this.wayY;
	}
	public String getTag()
	{
		return this.tag;
	}
	public Direction getAcross()
	{
		return Direction.valueOf(this.across);
	}
}
