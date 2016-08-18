package kr.dja;

import javax.swing.JPanel;

public class TaskManager
{
	private LogicCore core;
	TaskManager(LogicCore core)
	{
		this.core = core;
	}
	public JPanel getGraphPanel()
	{
		return new JPanel();
	}

}
