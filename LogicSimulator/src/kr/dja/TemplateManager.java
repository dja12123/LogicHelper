package kr.dja;

import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class TemplateManager
{
	private Session session;
	private JPanel templatePanel;
	
	private ArrayList<Template> templates;
	
	TemplateManager(Session session)
	{
		this.session = session;
		this.templates = new ArrayList<Template>();
		this.templatePanel = new JPanel();
		this.templatePanel.setLayout(null);
	}
	TemplateManager(DataBranch data)
	{
		
	}
	private void sortManagerPane()
	{
		this.templatePanel.setPreferredSize(new Dimension(325, this.templates.size() * 31));
		for(int i = 0; i < this.templates.size(); i++)
		{
			this.templates.get(i).setLocation(0, i * 31);
		}
		this.templatePanel.repaint();
		this.session.getCore().getUI().getTemplatePanel().updateUI();
	}
	void getData(DataBranch data)
	{
		
	}
	void addTempleat(File file)
	{
		
	}
	void addTempleat()
	{
		System.out.println("call");
		Template temp = new Template(ClipBoardPanel.clipBoard);
		this.templates.add(temp);
		this.templatePanel.add(temp);
		this.sortManagerPane();
	}
	JPanel getPanel()
	{
		return this.templatePanel;
	}
	void setSelectAll(boolean status)
	{
		for(Template t : this.templates)
		{
			t.setSelect(status);
		}
	}
}
class ClipBoardPanel extends ButtonPanel
{
	static DataBranch clipBoard = null;
	private static LinkedHashMap<LogicCore, ClipBoardPanel> sharedClipBoard = new LinkedHashMap<LogicCore, ClipBoardPanel>();
	private static final long serialVersionUID = 1L;
	
	private static String sharedTag = "";
	
	private LogicCore core;
	
	private JTextField nameField;
	private JLabel nameLabel;
	private ButtonPanel editTextButton;
	private boolean editTagFlag;
	
	ClipBoardPanel(LogicCore core)
	{
		this.core = core;
		
		this.setLayout(null);
		this.setSize(342, 30);
		this.setBasicImage(LogicCore.getResource().getImage("TEMP_CLIP"));
		this.setOnMouseImage(LogicCore.getResource().getImage("TEMP_CLIP_SELECT"));
		this.setBasicPressImage(LogicCore.getResource().getImage("TEMP_CLIP_PUSH"));
		this.setBasicDisableImage(LogicCore.getResource().getImage("TEMP_CLIP_DEENABLE"));
		
		this.nameField = new JTextField();
		this.nameField.setBorder(null);
		this.nameField.setBounds(5, 5, 310, 20);
		this.nameLabel = new JLabel(sharedTag);
		this.nameLabel.setBounds(5, 5, 310, 20);
		this.nameLabel.setFont(LogicCore.RES.NORMAL_FONT.deriveFont(14f));
		
		this.editTextButton = new ButtonPanel()
		{
			private static final long serialVersionUID = 1L;

			@Override
			void pressed(int button)
			{
				if(editTagFlag)
				{
					ClipBoardPanel.this.remove(nameField);
					ClipBoardPanel.this.add(nameLabel);
					editTag(nameField.getText());
					ClipBoardPanel.this.setEnable(true);
					editTagFlag = false;
				}
				else
				{
					ClipBoardPanel.this.remove(nameLabel);
					ClipBoardPanel.this.add(nameField);
					ClipBoardPanel.this.setEnable(false);
					editTagFlag = true;
				}
				ClipBoardPanel.this.repaint();
			}
		};
		this.editTextButton.setBounds(319, 7, 16, 16);
		this.editTextButton.setBasicImage(LogicCore.getResource().getImage("TEMP_TEXT"));
		this.editTextButton.setBasicPressImage(LogicCore.getResource().getImage("TEMP_TEXT_PUSH"));
		this.add(nameLabel);
		this.add(editTextButton);
		
		if(clipBoard == null)
		{
			this.setEnable(false);
			this.editTextButton.setEnable(false);
		}
	}
	static void addInstance(LogicCore core)
	{
		ClipBoardPanel c = new ClipBoardPanel(core);
		sharedClipBoard.put(core, c);
		core.getUI().getTemplatePanel().setClipBoard(c);
	}
	static void removeInstance(LogicCore core)
	{
		sharedClipBoard.remove(core);
	}
	static void setClipBoard(DataBranch branch)
	{
		if(clipBoard == null)
		{
			for(ClipBoardPanel p : sharedClipBoard.values())
			{
				p.Active();
			}
		}
		clipBoard = branch;
	}
	private static void editTag(String tag)
	{
		sharedTag = tag;
		for(ClipBoardPanel p : sharedClipBoard.values())
		{
			p.nameField.setText(sharedTag);
			p.nameLabel.setText(sharedTag);
		}
	}
	private void Active()
	{
		this.setEnable(true);
		this.editTextButton.setEnable(true);
		this.core.getUI().getTemplatePanel().setAddButtonActive();
	}
	@Override
	void pressed(int button)
	{
		if(clipBoard != null)
		{
			ArrayList<GridMember> list = new ArrayList<GridMember>();
			Iterator<DataBranch> itr =  clipBoard.getLowerBranchIterator();
			while(itr.hasNext())
			{
				list.add(GridMember.Factory(core, itr.next()));
			}
			core.getUI().addTrackedPane(new TrackedPane(list, core.getUI()));
		}
	}
}
class Template extends ButtonPanel
{
	private static final long serialVersionUID = 1L;

	private DataBranch data;
	
	private JCheckBox checkBox;
	
	Template(DataBranch data)
	{
		this.checkBox = new JCheckBox();
		this.checkBox.setBounds(3, 7, 16, 16);
		this.setLayout(null);
		this.setSize(325, 30);
		this.setBasicImage(LogicCore.getResource().getImage("MANAGER_DFT"));
		this.setOnMouseImage(LogicCore.getResource().getImage("MANAGER_SELECT"));
		this.setBasicPressImage(LogicCore.getResource().getImage("MANAGER_PUSH"));
		this.add(this.checkBox);
	}
	DataBranch getData()
	{
		return this.data;
	}
	void putTempleat()
	{
		
	}
	boolean isSelected()
	{
		return this.checkBox.isSelected();
	}
	void setSelect(boolean status)
	{
		this.checkBox.setSelected(status);
	}
}