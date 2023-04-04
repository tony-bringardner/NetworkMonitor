package us.bringardner.neworkmonitor;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.bringardner.simplechart.ChartControlPanel;
import com.bringardner.simplechart.ChartPanel;
import com.bringardner.simplechart.Dataset;
import com.bringardner.simplechart.ISeries;
import com.bringardner.simplechart.Series;
import com.bringardner.simplechart.TimeXLabelRenderer;

public class PlotterPanel extends JPanel implements NetworkMonitorFrame.DebugListner{
	//(((\w+\s*:\s*)?[-+0123456789.]+)+([ ,\t]+)*)+
	private static final Pattern ID_PLOT_DATA =  Pattern.compile("(((\\w+\\s*:\\s*)?[-+0123456789.]+)+([ ,\\t]+)*)+");// validate
	private static final Pattern PARSE_PLOT_DATA = Pattern.compile("(\\w+\\s*:\\s*)?[-+0123456789.]+[ ,\\t]*");// parse


	private static class NameValue {
		String name;
		double value;

		NameValue(String group) {
			int idx=group.indexOf(":");
			if( idx>0) {				
				name = group.substring(0,idx);
				group = group.substring(idx+1);
			}
			value = Double.parseDouble(group);
		}
	}

	public static List<NameValue> parsePlotData(String line) {

		List<NameValue> ret = new ArrayList<NameValue>();


		//Pattern p = Pattern.compile("(((\\w+\\s*:\\s*)?[-+0123456789.]+)+([ ,\\t]+)*)+");// validate

		//System.out.println("'"+line+"'");
		Matcher m = ID_PLOT_DATA.matcher(line);
		boolean is = m.matches();
		//System.out.println("line="+line+" is="+is);
		if( is ) {

			//p = Pattern.compile("(\\w+\\s*:\\s*)?[-+0123456789.]+[ ,\\t]*");// parse
			m = PARSE_PLOT_DATA.matcher(line);
			while (m.find()) {
				String group = m.group().replace(',', ' ').trim();
				try {
					ret.add(new NameValue(group));	
				} catch (Exception e) {
					// TODO: handle exception
				}

			}
		}

		return ret;
	}


	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		List<NameValue> tmp = parsePlotData("AI Thinker ESP32-CAM Monitored by 192.168.1.212 6000");
		System.out.println(tmp.size());

		/*
		parsePlotData("123 one:456.0, aaa:432 76.0,234.9\t344");
		parsePlotData("123 456.0 aaa:432 76.0,234.9\t344");
		parsePlotData("some stuff 123 456.0 432 76.0,234.9\t344");
		parsePlotData("124\t1324.0");
		//expr("val1=124,1324.0");
		parsePlotData("124");
		parsePlotData("124.0");
		parsePlotData("-124.0");
		parsePlotData("+124.0");
		 */

		System.exit(0);
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					JFrame frame = new JFrame();
					frame.getContentPane().add(new PlotterPanel());
					frame.setLocationRelativeTo(null);
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	ChartControlPanel chart;
	ChartPanel lineChartPanel;
	private Dataset dataSet;
	private JButton startButton;
	private boolean running;
	private JPanel panel;

	/**
	 * Create the panel.
	 */
	public PlotterPanel() {
		setLayout(new BorderLayout(0, 0));

		JPanel controlPanel = new JPanel();
		add(controlPanel, BorderLayout.CENTER);
		controlPanel.setLayout(new BorderLayout(0, 0));
		dataSet = new Dataset();
		dataSet.setMaxSampleSize(100);

		lineChartPanel = new ChartPanel();
		lineChartPanel.setShowLedger(true);
		lineChartPanel.setDataset(dataSet);
		lineChartPanel.setXLabelRenderer(new TimeXLabelRenderer());
		lineChartPanel.setShowSampleValue(false);
		lineChartPanel.setShowIcon(false);
		//System.out.println("shopwLedger="+lineChartPanel.isShowLedger());


		chart = new ChartControlPanel(lineChartPanel);	
		chart.setDetaSet(dataSet);


		controlPanel.add(chart);

		panel = new JPanel();
		controlPanel.add(panel, BorderLayout.NORTH);
		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		JButton clearButton = new JButton("Clear");
		panel.add(clearButton);
		startButton = new JButton("Start");
		panel.add(startButton);
		startButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actionStart();
			}
		});
		clearButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				actionClear();
			}
		});


	}

	protected void actionStart() {
		if( startButton.getText().equals("Start")) {
			running = true;
			dataSet.setMaxSampleSize(100);
			startButton.setText("Stop");						
		} else {
			// stop
			running = false;
			startButton.setText("Start");			
		}

	}

	protected void actionClear() {
		dataSet = new Dataset();
		chart.setDetaSet(dataSet);

	}


	private ISeries getSeriesByName(String name) {
		ISeries ret = null;
		for(ISeries s : dataSet.getSeries()) {
			if( name.equals(s.getName())) {
				ret = s;
				break;
			}
		}

		return ret;
	}
	StringBuilder messageBuffer = new StringBuilder();

	//@Override
	public void receive(final String msg) {

		if(running ) {
			messageBuffer.append(msg);
			int linebreak=messageBuffer.indexOf("\n");

			if( linebreak<0) {
				return;
			}
			String line = messageBuffer.substring(0, linebreak);
			messageBuffer.delete(0, linebreak + 1);


			long time = System.currentTimeMillis();


			List<NameValue> tmp = parsePlotData(line.trim());
			if(tmp.size()>0) {
				for (int idx = 0,sz=tmp.size(); idx < sz; idx++) {
					final NameValue item = tmp.get(idx);
					if( item.name == null) {
						item.name = "Variable"+idx;
					}
					ISeries s = getSeriesByName(item.name);
					if( s == null ) {
						s = new Series(item.name);
						dataSet.addSeries(s);
					}
					final ISeries s2 = s;
					SwingUtilities.invokeLater(()->{
						s2.addSample(time,item.value);
						lineChartPanel.updateUI();
					});
					Thread.yield();
				}
			}										
		}
	}
}
