package kr.dja;

import java.io.File;
import java.util.ArrayList;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

public class TemplateManager
{
	private static Template clipBoard;
	
	private Session session;
	private JPanel templatePanel;
	
	private ArrayList<Template> templates;
	
	TemplateManager(Session session)
	{
		this.session = session;
		this.templatePanel = new JPanel();
		this.templatePanel.setLayout(null);
	}
	TemplateManager(DataBranch data)
	{
		
	}
	private void sortManagerPane()
	{
		
	}
	void getData(DataBranch data)
	{
		
	}
	void addTempleat(File file)
	{
		
	}
	void addTempleat()
	{
		Template temp = new Template(clipBoard.getData());
		this.templates.add(temp);
		this.templatePanel.add(temp.getView());
		this.sortManagerPane();
	}
}
class Template
{
	private DataBranch data;
	
	private TemplatePanel showPanel;
	private JCheckBox checkBox;
	
	Template(DataBranch data)
	{
		this.showPanel = new TemplatePanel();
		this.checkBox = new JCheckBox();
		this.checkBox.setBounds(3, 7, 16, 16);
		this.showPanel.add(this.checkBox);
	}
	DataBranch getData()
	{
		return this.data;
	}
	void putTempleatOnGrid()
	{
		
	}
	ButtonPanel getView()
	{
		return this.showPanel;
	}
	class TemplatePanel extends ButtonPanel
	{
		private static final long serialVersionUID = 1L;

		TemplatePanel()
		{
			this.setLayout(null);
			this.setSize(325, 30);
		}
		@Override
		void pressed(int button)
		{
			
		}
	}
}