package kr.dja;

import java.awt.Color;
import java.util.ArrayList;

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
		@SuppressWarnings("unchecked")
		ArrayList<LogicBlock> taskTemp = (ArrayList<LogicBlock>)this.reserveTask.clone(); //ConcurrentModificationException ¹æÁö¿ë
		for(LogicBlock member : taskTemp)
		{
			this.reserveTask.remove(member);
			if(member instanceof LogicTimerTask)
			{
				((LogicTimerTask)member).ping();
			}
			else
			{
				member.calculate();
			}
		}
	}
	private void recursiveTask(LogicBlock block)
	{
		
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
interface LogicTimerTask
{
	void ping();
}
interface LogicWire
{
	
}