package kr.dja;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class TaskOperator
{
	private Signaller signaller;
	private LogicCore core;
	
	private int taskTick = 500;
	public final int MAX_TASK_TICK = 5000;
	public final int MIN_TASK_TICK = 10;
	private GraphViewPanel graphPanel;
	
	private ArrayList<LogicBlock> reserveTask = new ArrayList<LogicBlock>();
	private HashMap<LogicBlock, HashMap<Direction, Power>> check = new HashMap<LogicBlock, HashMap<Direction, Power>>();
	
	private boolean task;
	
	TaskOperator(LogicCore core)
	{
		this.core = core;
		this.graphPanel = new GraphViewPanel(this);
		core.getUI().getTaskOperatorPanel().setOperator(this);
	}
	void toggleTask()
	{
		if(!this.task)
		{
			if(this.signaller != null)
			{
				this.signaller.taskStop();
			}
			this.signaller = new Signaller();
			this.signaller.start();
			this.task = true;
		}
		else
		{
			if(this.signaller != null)
			{
				this.signaller.taskStop();
				this.signaller = null;
			}
			this.task = false;
		}
		this.core.getUI().getTaskOperatorPanel().setPauseButtonStatus(this.task);
	}
	void addReserveTask(LogicBlock member)
	{
		if(!this.reserveTask.contains(member))
		{
			this.reserveTask.add(member);
		}
	}
	@SuppressWarnings("unchecked")
	void clearData(Grid grid)
	{
		ArrayList<LogicBlock> temp = (ArrayList<LogicBlock>) this.reserveTask.clone();
		for(LogicBlock block : temp)
		{
			if(block.getGrid() == grid)
			{
				this.reserveTask.remove(block);
				block.endTimer();
			}
		}
		HashMap<LogicBlock, HashMap<Direction, Power>> temp1 = (HashMap<LogicBlock, HashMap<Direction, Power>>) this.check.clone();
		for(LogicBlock block : temp1.keySet())
		{
			if(block.getGrid() == grid)
			{
				this.check.remove(block);
			}
		}
		if(this.task)
		{
			this.toggleTask();
		}
	}
	void checkAroundAndReserveTask(Grid grid)
	{
		for(GridMember member : grid.getMembers().values())
		{
			if(member instanceof LogicBlock)
			{
				this.checkAroundAndReserveTask((LogicBlock)member);
			}
		}
	}
	void checkAroundAndReserveTask(LogicBlock block)
	{//최적화 필요	
		if(block.isPlacement())
		{
			for(Direction ext : Direction.values())
			{
				if(block instanceof LogicWire)
				{
					this.wireTask((LogicWire)block, ext, false);
				}
				LogicBlock extBlock = block.getGrid().getLogicBlock(block.getBlockLocationX() + ext.getWayX(), block.getBlockLocationY() + ext.getWayY());
				if(extBlock != null)
				{
					if(extBlock instanceof LogicWire)
					{
						LogicWire wire = (LogicWire)extBlock;
						if(wire.isWireValid(ext.getAcross()))
						{
							this.wireTask(wire, ext.getAcross(), false);
						}
					}
					if(block.getIOStatus(ext) == IOStatus.RECEIV)
					{
						if(extBlock.getIOStatus(ext.getAcross()) == IOStatus.TRANCE && block.getIOPower(ext) != extBlock.getIOPower(ext.getAcross()))
						{
							block.setIOResivePower(ext, extBlock.getIOPower(ext.getAcross()));
						}
						else if(extBlock.getIOStatus(ext.getAcross()) != IOStatus.TRANCE)
						{
							block.setIOResivePower(ext, Power.OFF);
						}
					}
				}
			}
		}
		for(Direction ext : Direction.values())
		{
			LogicBlock extBlock = block.getGrid() != null ? block.getGrid().getLogicBlock(block.getBlockLocationX() + ext.getWayX()
					, block.getBlockLocationY() + ext.getWayY()) : null;
			if(extBlock != null)
			{
				if(extBlock instanceof LogicWire)
				{
					LogicWire wire = (LogicWire)extBlock;
					if(wire.isWireValid(ext.getAcross()))
					{
						if(extBlock.getIOPower(ext.getAcross()) !=  block.getIOPower(ext))
						{
							this.checkHashPut(extBlock, ext.getAcross(), block.getIOPower(ext));
						}
						else
						{
							this.checkHashRemove(extBlock, ext.getAcross());
						}
					}
				}
				if(block.getIOStatus(ext) == IOStatus.TRANCE)
				{
					if(extBlock.getIOStatus(ext.getAcross()) == IOStatus.RECEIV)
					{
						if(extBlock.getIOPower(ext.getAcross()) !=  block.getIOPower(ext))
						{
							this.checkHashPut(extBlock, ext.getAcross(), block.getIOPower(ext));
						}
						else
						{
							this.checkHashRemove(extBlock, ext.getAcross());
						}
					}
				}
				else
				{
					this.checkHashPut(extBlock, ext.getAcross(), Power.OFF);
				}
				if(!block.isPlacement())
				{
					this.checkHashPut(extBlock, ext.getAcross(), Power.OFF);
				}
			}
		}
	}
	private void checkHashPut(LogicBlock block, Direction ext, Power power)
	{
		if(!check.containsKey(block))
		{
			this.check.put(block, new HashMap<Direction, Power>());
		}
		this.check.get(block).put(ext, power);
	}
	private void checkHashRemove(LogicBlock block, Direction ext)
	{
		if(check.containsKey(block))
		{
			if(this.check.get(block).containsKey(ext))
			{
				this.check.get(block).remove(ext);
			}
			if(this.check.get(block).size() < 1)
			{
				this.check.remove(block);
			}	
		}
	}
	void removeReserveTask(GridMember member)
	{
		if(this.reserveTask.contains(member))
		{
			this.reserveTask.remove(member);
		}
	}
	private void doTask()
	{
		boolean graphFlag = false;
		long taskStartTime = System.currentTimeMillis();
		for(LogicBlock member : this.check.keySet())
		{
			for(Direction ext : this.check.get(member).keySet())
			{
				member.setIOResivePower(ext, this.check.get(member).get(ext));
			}
			this.addReserveTask(member);
		}
		this.check = new HashMap<LogicBlock, HashMap<Direction, Power>>();
		@SuppressWarnings("unchecked")
		ArrayList<LogicBlock> taskTemp = (ArrayList<LogicBlock>)this.reserveTask.clone(); //ConcurrentModificationException 방지
		if(this.reserveTask.size() != 0)
		{
			graphFlag = true;
		}
		for(LogicBlock member : taskTemp)
		{
			this.reserveTask.remove(member);
			member.ping();
			recursiveTask(member, new ArrayList<LogicBlock>());
		}
		if(graphFlag)
		{
			this.graphPanel.setGraph(this.taskTick, System.currentTimeMillis() - taskStartTime);
		}
	}
	@SuppressWarnings("unchecked")
	private void recursiveTask(LogicBlock block, ArrayList<LogicBlock> taskList)
	{
		boolean copyTaskFlag = false;
		for(Direction ext : block.getIOTrance())
		{
			LogicBlock outputExtBlock = block.getGrid().getLogicBlock(block.getBlockLocationX() + ext.getWayX()
					, block.getBlockLocationY() + ext.getWayY());
	
			if(outputExtBlock != null)
			{
				if(outputExtBlock instanceof LogicWire)
				{
					LogicWire wire = (LogicWire)outputExtBlock;
					if(wire.isWireValid(ext.getAcross()) && wire.getIOPower(ext.getAcross()) != block.getIOPower(ext))
					{
						this.wireTask(wire, ext.getAcross(), true);
					}
				}
				else if(outputExtBlock.getIOStatus(ext.getAcross()) == IOStatus.RECEIV
					&& block.getPower() != outputExtBlock.getIOPower(ext.getAcross()))
				{
					Power power = outputExtBlock.getPower();
					System.out.println("isResive" + power);
					outputExtBlock.setIOResivePower(ext.getAcross(), block.getPower());
					if(power != outputExtBlock.getPower())
					{
						System.out.println("Task");
						if(!taskList.contains(outputExtBlock))
						{
							taskList.add(outputExtBlock);
							recursiveTask(outputExtBlock, copyTaskFlag ? (ArrayList<LogicBlock>) taskList.clone() : taskList);
							copyTaskFlag = true;
						}
						else
						{
							System.out.println("회로가 통구이가 되었습니다" + outputExtBlock.getBlockLocationX() + " " + outputExtBlock.getBlockLocationY());
						}
					}
				}
			}
		}
	}
	private void wireTask(LogicWire wire, Direction ext, boolean memberTask)
	{//와이어는 다른 연산방식 필요
		LinkedHashMap<LogicWire, ArrayList<Direction>> linkedWire = new LinkedHashMap<LogicWire, ArrayList<Direction>>();
		LinkedHashMap<LogicBlock, ArrayList<Direction>> linkedBlock = new LinkedHashMap<LogicBlock, ArrayList<Direction>>();
		ArrayList<Direction> temp = new ArrayList<Direction>();
		temp.add(ext);
		linkedWire.put(wire, temp);
		Power power = this.recursiveWireTask(wire, linkedWire, linkedBlock, ext, Power.OFF);
		System.out.println("블록상태" + power);
		for(LogicWire editWire : linkedWire.keySet())
		{
			for(Direction editExt : linkedWire.get(editWire))
			{
				editWire.setIOResivePower(editExt, power);
				System.out.println("wireTask연산");
			}
		}
		if(memberTask)
		{
			for(LogicBlock editBlock : linkedBlock.keySet())
			{
				for(Direction editExt : linkedBlock.get(editBlock))
				{
					editBlock.setIOResivePower(editExt, power);
				}
				this.recursiveTask(editBlock, new ArrayList<LogicBlock>());
			}
		}
		else
		{
			for(LogicBlock editBlock : linkedBlock.keySet())
			{
				for(Direction editExt : linkedBlock.get(editBlock))
				{
					this.checkHashPut(editBlock, editExt, power);
				}
			}
		}
	}
	private Power recursiveWireTask(LogicWire wire, LinkedHashMap<LogicWire, ArrayList<Direction>> linkedWire
			, LinkedHashMap<LogicBlock, ArrayList<Direction>> linkedBlock, Direction from, Power power)
	{
		for(Direction ext : Direction.values())
		{
			if(wire.isLinkedWire(from, ext) && wire.isWireValid(ext))
			{
				//System.out.println("확인" + wire.isWireValid(ext));
				LogicBlock extBlock = wire.getGrid().getLogicBlock(wire.getBlockLocationX() + ext.getWayX(), wire.getBlockLocationY() + ext.getWayY());
				if(extBlock != null)
				{
					if(extBlock instanceof LogicWire)
					{
						//System.out.println("이프통과" + wire.isWireValid(ext));
						LogicWire extWire = (LogicWire)extBlock;
						if(extWire.isWireValid(ext.getAcross()))
						{
							if(linkedWire.containsKey(extWire))
							{
								if(!linkedWire.get(extWire).contains(ext.getAcross()))
								{
									linkedWire.get(extWire).add(ext.getAcross());
									power = this.recursiveWireTask(extWire, linkedWire, linkedBlock, ext.getAcross(), power);
									
								}
							}
							else
							{
								ArrayList<Direction> temp = new ArrayList<Direction>();
								temp.add(ext.getAcross());
								linkedWire.put(extWire, temp);
								power = this.recursiveWireTask(extWire, linkedWire, linkedBlock, ext.getAcross(), power);
							}
						}
					}
					else if(!power.getBool() && extBlock.getIOStatus(ext.getAcross()) == IOStatus.TRANCE && extBlock.getIOPower(ext.getAcross()).getBool())
					{
						//System.out.println("공급자 감지" + wire.isWireValid(ext));
					
						power = Power.ON;
						System.out.println(power);
						
					}
					if(extBlock.getIOStatus(ext.getAcross()) == IOStatus.RECEIV)
					{
						//System.out.println("공급함" + ext.toString() + from.toString());
						
						if(linkedBlock.containsKey(extBlock))
						{
							linkedBlock.get(extBlock).add(ext.getAcross());
						}
						else
						{
							ArrayList<Direction> temp = new ArrayList<Direction>();
							temp.add(ext.getAcross());
							linkedBlock.put(extBlock, temp);
						}
						
					}
				}
			}
		}
		return power;
	}
	void setTaskTick(int tick)
	{
		this.taskTick = tick;
	}
	int getTaskTick()
	{
		return this.taskTick;
	}
	JPanel getGraphPanel()
	{
		return this.graphPanel;
	}
	private class Signaller extends Thread
	{
		private boolean taskFlag = true;
		@Override
		public void run()
		{
			while(this.taskFlag)
			{
				doTask();
				try
				{
					Thread.sleep(taskTick);
				}
				catch(InterruptedException e)
				{
					e.printStackTrace();
				}
			}
		}
		void taskStop()
		{
			this.taskFlag = false;
		}
	}
}
class GraphViewPanel extends JPanel
{
	private static final long serialVersionUID = 1L;
	
	private TaskOperator operator;
	
	private ArrayList<Integer> graphBar = new ArrayList<Integer>();
	private int graphLocX = 4, graphLocY = 5, graphSizeX = 141, graphSizeY = 82;
	private int highest = 1;
	private JLabel multipleLabel;
	GraphViewPanel(TaskOperator operator)
	{
		this.operator = operator;
		this.setLayout(null);
		this.setSize(148, 90);
		this.multipleLabel = new JLabel("지연: 0.0%", SwingConstants.CENTER);
		this.multipleLabel.setForeground(new Color(255, 0, 0, 70));
		this.multipleLabel.setFont(LogicCore.RES.BAR_FONT.deriveFont(Font.BOLD, 14.0F));
		this.multipleLabel.setBounds(0, 30, this.getWidth(), 20);
		this.add(this.multipleLabel);
	}
	@Override
	public void paint(Graphics g)
	{
		super.paint(g);
		g.setColor(new Color(185, 205, 229));
		for(int i = 0; i < this.graphBar.size(); i++)
		{
			g.drawLine(i + graphLocX, graphLocY + graphSizeY - 1, i + graphLocX, graphLocY + graphSizeY - 1
					- (int)((double)this.graphBar.get(i) / this.highest * this.graphSizeY));
		}
		this.paintChildren(g);
	}
	void setGraph(int stdTime, long time)
	{
		this.highest = 1;
		this.graphBar.add((int)time);
		if(this.graphBar.size() > this.graphSizeX)
		{
			this.graphBar.remove(0);
		}
		for(int height : this.graphBar)
		{
			if(this.highest < height)
			{
				this.highest = height;
			}
		}
		this.multipleLabel.setText("지연: " + Double.parseDouble(String.format("%.3f", this.highest / (double)this.operator.getTaskTick() * 100)) + "%");
		this.repaint();
	}
}