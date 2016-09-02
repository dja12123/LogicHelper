package kr.dja;

import java.util.ArrayList;

import javax.swing.JPanel;


public class TaskManager
{
	private LogicCore core;
	
	private int maxSnapShot = 20;
	
	private JPanel taskPanel;
	
	TaskManager(LogicCore core)
	{
		this.core = core;
		this.taskPanel = new JPanel();
	}
	int getMaxSnapShot()
	{
		return this.getMaxSnapShot();
	}
}
class TaskSnapShot extends JPanel
{
	private static final long serialVersionUID = 1L;
	
	private TaskManager manager;
	
	private int index = 0;
	private TaskSnapShot beforeLinkedSnapShot = null, nextLinkedSnapShot = null;
	
	private JPanel SnapShotPanel;
	
	TaskSnapShot(TaskManager manager, TaskSnapShot beforeLinkedSnapShot)
	{
		this.manager = manager;
		
		this.beforeLinkedSnapShot = beforeLinkedSnapShot;
		this.beforeLinkedSnapShot.setNext(this);
		
		TaskSnapShot beforeSnapShot = this.beforeLinkedSnapShot;
		while(beforeSnapShot != null)
		{
			beforeSnapShot.index ++;
			if(beforeSnapShot.index > this.manager.getMaxSnapShot())
			{
				beforeSnapShot.getNext().removeBefore();
			}
		}
	}
	void setNext(TaskSnapShot nextSnapShot)
	{
		this.nextLinkedSnapShot = nextSnapShot;
	}
	TaskSnapShot getBefore()
	{
		return this.beforeLinkedSnapShot;
	}
	TaskSnapShot getNext()
	{
		return this.nextLinkedSnapShot;
	}
	void removeBefore()
	{
		this.beforeLinkedSnapShot = null;
	}
	void removeNext()
	{
		this.nextLinkedSnapShot = null;
	}
	int getIndex()
	{
		return this.index;
	}
}