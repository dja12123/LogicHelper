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
{//연산부분 재설계 필요
	private Signaller signaller;
	private LogicCore core;
	
	private int taskTick = 500;
	public final int MAX_TASK_TICK = 5000;
	public final int MIN_TASK_TICK = 10;
	private GraphViewPanel graphPanel;
	
	private ArrayList<LogicBlock> reserveTask = new ArrayList<LogicBlock>();
	
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
		ArrayList<LogicBlock> taskList = new ArrayList<LogicBlock>();
		if(block instanceof LogicWire)
		{
			this.recursiveTask(block, new ArrayList<LogicBlock>());
		}
		for(Direction ext : Direction.values())
		{
			LogicBlock extBlock = block.getGrid() != null ? block.getGrid().getLogicBlock(block.getBlockLocationX() + ext.getWayX()
					, block.getBlockLocationY() + ext.getWayY()) : null;
			if(block instanceof LogicWire)
			{
				this.wireTask((LogicWire)block, ext, taskList);
			}
			if(extBlock != null)
			{
				if(extBlock instanceof LogicWire)
				{
					this.wireTask((LogicWire)extBlock, ext.getAcross(), taskList);
				}
				else if(extBlock.getIOStatus(ext.getAcross()) == IOStatus.RECEIV)
				{//주변 블록이 수신기가 있는 상태
					if(!block.isPlacement() || !(block.getIOStatus(ext) == IOStatus.TRANCE && block.getIOPower(ext).getBool()))
					{//블록이 설치되있지 않거나 신호 발송중이 아닐경우 
						extBlock.setIOResivePower(ext.getAcross(), Power.OFF);
						//System.out.println("끔1");
					}
					else
					{
						extBlock.setIOResivePower(ext.getAcross(), Power.ON);
					}
					this.recursiveTask(extBlock, new ArrayList<LogicBlock>());
				}
			}
			if(block.isPlacement() && block.getIOStatus(ext) == IOStatus.RECEIV)
			{//블록이 설치되었고 블록의 해당방향 수신자가 있을경우
				if(extBlock == null || !(extBlock.getIOStatus(ext.getAcross()) == IOStatus.TRANCE && extBlock.getIOPower(ext.getAcross()).getBool()))
				{//해당 방향 블록이 설치되있지 않거나 신호 발송중이 아닐경우 
					block.setIOResivePower(ext, Power.OFF);
					//System.out.println("끔2 " + block.getBlockLocationX() + " " + block.getBlockLocationY());
				}
				else
				{
					block.setIOResivePower(ext, Power.ON);
				}
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
	{//재귀함수
		boolean copyTaskFlag = false;
		if(block.isPlacement() && block.getActive())
		{//블록이 설치된 상태이고, 활성화 상태일경우
			for(Direction ext : block.getIOTrance())
			{//동서남북 돌아가면서 신호를 받는 블록이 있는지 체크
				LogicBlock extBlock = block.getGrid().getLogicBlock(block.getBlockLocationX() + ext.getWayX(), block.getBlockLocationY() + ext.getWayY());
				if(extBlock != null && extBlock.getActive())
				{//해당 방향 블록이 존재하고, 활성화 상태일 경우
					if(extBlock instanceof LogicWire)
					{
						this.wireTask((LogicWire)extBlock, ext.getAcross(), copyTaskFlag ? (ArrayList<LogicBlock>)taskList.clone() : taskList);
						copyTaskFlag = true;
					}
					if(extBlock.getIOPower(ext.getAcross()) != block.getIOPower(ext))
					{//쏘는 방향과 받는 방향의 신호 상태가 불일치 할경우

						if(extBlock.getIOStatus(ext.getAcross()) == IOStatus.RECEIV)
						{//해당 방향 블록이 신호 수신이 가능한 경우
							Power beforePower = extBlock.getPower();
							extBlock.setIOResivePower(ext.getAcross(), block.getIOPower(ext));
							//블록에 신호 발송!
							if(beforePower != extBlock.getPower())
							{//상태가 변경되었을경우 재귀 호출
								if(!taskList.contains(extBlock))
								{//작업 목록에 블록이 없는지 확인
									taskList.add(extBlock);
									this.recursiveTask(extBlock, copyTaskFlag ? (ArrayList<LogicBlock>)taskList.clone() : taskList);
									//이전에 한번 작업 목록을 발송했으면 작업 목록 복사해서 재귀호출
									copyTaskFlag = true;
								}
								else
								{//만약 작업 목록에 블록이 있으면 맴돌이 신호라는 뜻이므로 블록 비활성화
									extBlock.setActive(false);
								}
							}
						}
					}
				}
			}
		}
	}
	@SuppressWarnings("unchecked")
	private void wireTask(LogicWire wire, Direction ext, ArrayList<LogicBlock> taskList)
	{
		LinkedHashMap<LogicWire, ArrayList<Direction>> linkedWire = new LinkedHashMap<LogicWire, ArrayList<Direction>>();
		ArrayList<Direction> temp = new ArrayList<Direction>();
		temp.add(ext);
		linkedWire.put(wire, temp);
		LinkedHashMap<LogicBlock, ArrayList<Direction>> linkedBlock = new LinkedHashMap<LogicBlock, ArrayList<Direction>>();
		//System.out.println("작업시작");
		Power blockresivePower = this.recursiveWireTask(wire, ext,  linkedWire, linkedBlock, Power.OFF);
		for(LogicWire taskWire : linkedWire.keySet())
		{
			//System.out.println("연산최종");
			for(Direction taskExt : linkedWire.get(taskWire))
			{
				taskWire.setIOResivePower(taskExt, blockresivePower);
			}
		}
		for(LogicBlock taskBlock : linkedBlock.keySet())
		{
			//System.out.println("연산최종");
			if(taskBlock.getActive())
			{
				for(Direction taskExt : linkedBlock.get(taskBlock))
				{
					Power beforePower = taskBlock.getPower();
					taskBlock.setIOResivePower(taskExt, blockresivePower);
					if(beforePower != taskBlock.getPower())
					{
						if(!taskList.contains(taskBlock))
						{
							taskList.add(taskBlock);
							this.recursiveTask(taskBlock, (ArrayList<LogicBlock>)taskList.clone());
						}
						else
						{
							taskBlock.setActive(false);
						}
					}
				}
			}
		}
	}
	private Power recursiveWireTask(LogicWire wire, Direction from, LinkedHashMap<LogicWire, ArrayList<Direction>> linkedWire
			, LinkedHashMap<LogicBlock, ArrayList<Direction>> linkedBlock, Power power)
	{
		if(wire.getActive())
		{//와이어가 활성화 상태일 경우
			//System.out.println("발견!! " + wire.getBlockLocationX() + " " + wire.getBlockLocationY());
			for(Direction ext : Direction.values())
			{
				if(wire.isLinkedWire(from, ext) && wire.isWireValid(ext))
				{//해당 방향이 신호 준 방향과 링크일경우
					LogicBlock extBlock = wire.getGrid().getLogicBlock(wire.getBlockLocationX() + ext.getWayX(), wire.getBlockLocationY() + ext.getWayY());
					if(extBlock != null && extBlock.getActive())
					{//해당 방향 블록이 존재하고, 활성화 상태일 경우
						if(extBlock instanceof LogicWire)
						{//와이어가 감지되면
							LogicWire extWire = (LogicWire)extBlock;
							if(extWire.isWireValid(ext.getAcross()))
							{//와이어 수신기 있으면
								if(!linkedWire.containsKey(extWire) || !linkedWire.get(extWire).contains(ext.getAcross()))
								{
									if(linkedWire.containsKey(extWire))
									{
										linkedWire.get(extWire).add(ext.getAcross());
									}
									else
									{
										ArrayList<Direction> temp = new ArrayList<Direction>();
										temp.add(ext.getAcross());
										linkedWire.put(extWire, temp);
									}
									power = this.recursiveWireTask(extWire, ext.getAcross(), linkedWire, linkedBlock, power);
								}
							}
						}
						else
						{//블록이 감지되면
							if(extBlock.getIOStatus(ext.getAcross()) == IOStatus.TRANCE)
							{
								if(extBlock.getIOPower(ext.getAcross()).getBool())
								{
									power = Power.ON;
								}
								//System.out.println("전송감지 " + extBlock.getBlockLocationX() + " " + extBlock.getBlockLocationY() + " " + ext.getAcross());
							}
							else if(extBlock.getIOStatus(ext.getAcross()) == IOStatus.RECEIV)
							{
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
					LogicCore.putConsole(e.toString());
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
	private int graphLocX = 4, graphLocY = 5, graphSizeX = 141, graphSizeY = 57;
	private int highest = 1;
	private JLabel multipleLabel;
	GraphViewPanel(TaskOperator operator)
	{
		this.operator = operator;
		this.setLayout(null);
		this.setSize(148, 65);
		this.multipleLabel = new JLabel(LogicCore.getResource().getLocal("delay") + ": 0.0%", SwingConstants.CENTER);
		this.multipleLabel.setForeground(new Color(255, 0, 0, 70));
		this.multipleLabel.setFont(LogicCore.RES.BAR_FONT.deriveFont(Font.BOLD, 14.0F));
		this.multipleLabel.setBounds(0, 20, this.getWidth(), 20);
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
		this.multipleLabel.setText(LogicCore.getResource().getLocal("delay") + ": "
		+ Double.parseDouble(String.format("%.3f", this.highest / (double)this.operator.getTaskTick() * 100)) + "%");
		this.repaint();
	}
}