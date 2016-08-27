package kr.dja;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JPanel;

public class TaskOperator
{
	private LogicCore core;
	private Signaller signaller;
	
	private int taskTick = 500;
	public final int MAX_TASK_TICK = 5000;
	public final int MIN_TASK_TICK = 3;
	private JPanel graphPanel;
	
	private volatile ArrayList<LogicBlock> reserveTask = new ArrayList<LogicBlock>();
	private volatile HashMap<LogicBlock, HashMap<Direction, Power>> check = new HashMap<LogicBlock, HashMap<Direction, Power>>();
	
	TaskOperator(LogicCore core)
	{
		this.core = core;
		
		this.graphPanel = new JPanel();
	}
	void addReserveTask(LogicBlock member)
	{
		if(!this.reserveTask.contains(member))
		{
			this.reserveTask.add(member);
		}
	}
	void checkAroundAndReserveTask(LogicBlock block)
	{//최적화 필요
		System.out.println(this.core.getGrid().getMembers().contains(block));
		System.out.println("Check");		
		if(this.core.getGrid().getMembers().contains(block))
		{
			for(Direction ext : Direction.values())
			{
				LogicBlock extBlock = this.core.getGrid().getLogicBlock(block.getBlockLocationX() + ext.getWayX(), block.getBlockLocationY() + ext.getWayY());
				if(extBlock != null)
				{
					if(block.getIOStatus(ext) == IOStatus.RECEIV)
					{
						if(extBlock.getIOStatus(ext.getAcross()) == IOStatus.TRANCE && block.getIOPower(ext) != extBlock.getIOPower(ext.getAcross()))
						{
							block.setIOResivePower(ext, extBlock.getIOPower(ext.getAcross()));
						}
						else if(extBlock.getIOStatus(ext.getAcross()) != IOStatus.TRANCE)
						{
							System.out.println("Po!!!werOFF "+ extBlock.getBlockLocationX() +" " + extBlock.getBlockLocationY()+" "+ ext.getAcross());
							block.setIOResivePower(ext, Power.OFF);
						}
					}
				}
			}
		}
		for(Direction ext : Direction.values())
		{
			LogicBlock extBlock = this.core.getGrid().getLogicBlock(block.getBlockLocationX() + ext.getWayX(), block.getBlockLocationY() + ext.getWayY());
			if(extBlock != null)
			{
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
				if(!this.core.getGrid().getMembers().contains(block))
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
		System.out.println("doTask");
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
		for(LogicBlock member : taskTemp)
		{
			this.reserveTask.remove(member);
			member.ping();
			recursiveTask(member);
		}
	}
	private void recursiveTask(LogicBlock block)
	{
		for(Direction ext : block.getIOTrance())
		{
			LogicBlock outputExtBlock = this.core.getGrid().getLogicBlock(block.getBlockLocationX() + ext.getWayX(), block.getBlockLocationY() + ext.getWayY());
			if(outputExtBlock != null)
			{
				if(outputExtBlock.getIOStatus(ext.getAcross()) == IOStatus.RECEIV)
				{
					if(block.getPower() != outputExtBlock.getIOPower(ext.getAcross()))
					{
						Power power = outputExtBlock.getPower();
						System.out.println("isResive" + power);
						outputExtBlock.setIOResivePower(ext.getAcross(), block.getPower());
						if(power != outputExtBlock.getPower())
						{
							System.out.println("Task");
							recursiveTask(outputExtBlock);
						}
					}
				}
			}
		}
	}
	void taskStart()
	{
		if(this.signaller == null)
		{
			this.signaller = new Signaller();
			this.signaller.start();
		}
	}
	void taskPause()
	{
		if(this.signaller != null)
		{
			this.signaller.taskStop();
		}
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
			signaller = null;
		}
		void taskStop()
		{
			this.taskFlag = false;
		}
	}
}
class GraphPanel extends JPanel
{
	
}
interface LogicWire
{
	
}