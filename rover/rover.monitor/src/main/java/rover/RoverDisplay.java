package rover;

import org.iids.aos.service.ServiceBroker;
import rover.MonitorInfo.Rover;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class RoverDisplay extends JFrame implements ActionListener {

	private WorldPanel drawPanel; 

	private JButton btnStart;
	private JButton btnStop;
	private JButton btnSelect;

	private JLabel lblStatus;
	
	private JComboBox scenarioList;
	private JComboBox zoomList;
	private JComboBox speedList;
	
	private JTable roverTable;
	private RoverTableModel roverTableModel;
	
	private RoverService service;	
	private ServiceBroker sb;

	public RoverDisplay(ServiceBroker sb) {
		super("Rover Monitor");

		this.sb = sb;
		
		BorderLayout bl = new BorderLayout();
		this.setLayout(bl);
		
		drawPanel = new WorldPanel();
		drawPanel.setSize(200, 200);
		this.setPreferredSize(new Dimension(200,200));
		
		lblStatus = new JLabel();
		lblStatus.setText("World Stopped");
		
		btnStart = new JButton("Start");
		btnStart.setActionCommand("Start");
		btnStart.addActionListener(this);
		btnStop = new JButton("Stop");
		btnStop.setActionCommand("Stop");
		btnStop.addActionListener(this);
		btnSelect = new JButton("Select Scenario");
		btnSelect.setActionCommand("Select");
		btnSelect.addActionListener(this);
		
		
		btnStop.setEnabled(false);
		
		//create list to select scenario
		String[] scenarios = {"Scenario 0", "Scenario 1", "Scenario 2", "Scenario 3", "Scenario 4", "Scenario 5"};
		scenarioList = new JComboBox(scenarios);

		String[] zooms = { "Zoom 10", "Zoom 9", "Zoom 8", "Zoom 7", "Zoom 6", "Zoom 5", "Zoom 4", "Zoom 3", "Zoom 2", "Zoom 1" };
		zoomList = new JComboBox(zooms);
		
		String[] speeds = {"1x", "2x", "3x", "4x", "5x", "6x", "7x", "8x", "9x", "10x" };
		speedList = new JComboBox(speeds);
		
		roverTableModel = new RoverTableModel();		
		roverTable = new JTable(roverTableModel); 
		
		roverTable.setPreferredSize(new Dimension(0, 200));
		roverTable.setSize(new Dimension(0, 200));
		
		JPanel topPanel = new JPanel();
		topPanel.add(lblStatus);
		topPanel.add(scenarioList);
		topPanel.add(btnSelect);
		topPanel.add(speedList);
		topPanel.add(btnStart);
		topPanel.add(btnStop);
		topPanel.add(zoomList);
		
		add(topPanel, BorderLayout.PAGE_START);
		
		JScrollPane drawScroll = new JScrollPane(drawPanel);
		
		add(drawScroll, BorderLayout.CENTER);
		
		JScrollPane scroll = new JScrollPane(roverTable);
		scroll.setPreferredSize(new Dimension(0, 200));
		
		add(scroll, BorderLayout.PAGE_END);
		

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(800,800);
		setPreferredSize(new Dimension(800,800));
		
		
		
	}

	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().equals("Start")) {
		
			lblStatus.setText("World Running");
			btnStart.setEnabled(false);
			scenarioList.setEnabled(false);
			speedList.setEnabled(false);
			btnSelect.setEnabled(false);
			btnStop.setEnabled(true);
			
			try {
				service = sb.bind(RoverService.class);			
				service.startWorld(speedList.getSelectedIndex() + 1);
			} catch (Exception ex) {
				ex.printStackTrace();				
			}
			
			
		} else if(e.getActionCommand().equals("Stop")) {
			
			lblStatus.setText("World Stopped");
			btnStart.setEnabled(true);
			speedList.setEnabled(true);
			scenarioList.setEnabled(true);
			btnSelect.setEnabled(true);
			btnStop.setEnabled(false);
			
			try {
				service = sb.bind(RoverService.class);			
				service.stopWorld();
			} catch (Exception ex) {
				ex.printStackTrace();				
			}
			
		} else if(e.getActionCommand().equals("Select")) {
			
			try {
				service = sb.bind(RoverService.class);			
				service.resetWorld( scenarioList.getSelectedIndex() );
			} catch (Exception ex) {
				ex.printStackTrace();				
			}
			
		}
	}
	

	public void UpdateDisplay(MonitorInfo info) {
		
				
		//"Rover", "X", "Y", "Task", "% Complete", "Carrying", "Power", "Max Speed", "Max Range", "Max Load" };
		
		ArrayList<Object[]> rovers = roverTableModel.getRowData();
		rovers.clear();
		
		for(Rover ri : info.getRovers()) {
			
			String task = "";
			switch(ri.getTask()) {
			case PollResult.MOVE:
					task = "MOVE";
					break;
			case PollResult.SCAN:
					task = "SCAN";
					break;
			case PollResult.COLLECT:
					task = "COLLECT";
					break;
			case PollResult.DEPOSIT:
					task = "DEPOSIT";
					break;
			}
			
			Object[] rov = { ri.getKey(), ri.getX(), ri.getY(), 
						task, (int) (ri.getTaskCompletion() * 100), 
						ri.getCurrentLoad(), ri.getEnergy(),
						ri.getSpeed(), ri.getScanRange(), ri.getMaxLoad() };
			
			rovers.add(rov);
			
		}
		
		roverTableModel.fireTableDataChanged();
		
		drawPanel.setScale(10 - zoomList.getSelectedIndex());
		drawPanel.setMonitorInfo(info);
		
	}
	
	
	
}