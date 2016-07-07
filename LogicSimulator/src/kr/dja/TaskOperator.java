package kr.dja;

import java.awt.Color;

import javax.swing.JPanel;

public class TaskOperator
{
	Grid grid;
	JPanel graphPanel;
	TaskOperator(Grid grid, JPanel graphPanel)
	{
		this.grid = grid;
		this.graphPanel = graphPanel;
		this.graphPanel.setLayout(null);
		this.graphPanel.setSize(140, 110);
		this.graphPanel.setBackground(Color.black);
	}
}
