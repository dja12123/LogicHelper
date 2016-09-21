package kr.dja;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class TaskOperator
{
	private Signaller signaller;
	
	private int taskTick = 500;
	public final int MAX_TASK_TICK = 5000;
	public final int MIN_TASK_TICK = 10;
	private GraphViewPanel graphPanel;
	
	private ArrayList<LogicBlock> reserveTask = new ArrayList<LogicBlock>();
	private HashMap<LogicBlock, HashMap<Direction, Power>> check = new HashMap<LogicBlock, HashMap<Direction, Power>>();
	
	TaskOperator(LogicCore core)
	{
		this.graphPanel = new GraphViewPanel(this);
		core.getUI().getTaskOperatorPanel().setOperator(this);
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
	}
	void checkAroundAndReserveTask(LogicBlock block)
	{//최적화 필요	
		if(block.isPlacement())
		{
			for(Direction ext : Direction.values())
			{
				LogicBlock extBlock = block.getGrid().getLogicBlock(block.getBlockLocationX() + ext.getWayX(), block.getBlockLocationY() + ext.getWayY());
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
	
			if(outputExtBlock != null && outputExtBlock.getIOStatus(ext.getAcross()) == IOStatus.RECEIV
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
		this.multipleLabel = new JLabel("배수: 0.0%", SwingConstants.CENTER);
		this.multipleLabel.setForeground(new Color(255, 0, 0, 128));
		this.multipleLabel.setFont(LogicCore.RES.BAR_FONT.deriveFont(14.0F));
		this.multipleLabel.setBounds(0, 30, this.getWidth(), 20);
		this.add(this.multipleLabel);
	}
	@Override
	public void paint(Graphics g)
	{
		super.paint(g);
		g.setColor(Color.gray);
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
		this.multipleLabel.setText("배수: " + Double.parseDouble(String.format("%.3f", this.highest / (double)this.operator.getTaskTick() * 100)) + "%");
		this.repaint();
		System.out.println("걸린시간: " + time);
	}
}
interface LogicWire
{
	
}