package solveParticularProblem;
import dpSolver.DPSolver;
import general.General;
import inputOutput.Input;
import inputOutput.SolverNames;
import inputOutput.ValueDistribution;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import static javax.swing.JFrame.EXIT_ON_CLOSE;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import mainSolver.MainSolver;
import mainSolver.Result;
public class MainFrame extends JFrame
{
	public MainFrame() {
		try { jbInit();	} catch (Exception ex) {ex.printStackTrace(); }
	}
	JButton run_button = new JButton();
	ButtonGroup radioButtonGroup2 = new ButtonGroup();
	JTextField numOfAgents_textField = new JTextField();
	JTextField inputAndOutputPathAndFolderName_textField = new JTextField();
	JTextField inputFileName_textField = new JTextField();
	JTextField ipAcceptableRatio_textField = new JTextField();
	JPanel contentPane;
	JPanel jPanel1 = new JPanel();
	JPanel jPanel5 = new JPanel();
	JPanel jPanel7 = new JPanel();
	JPanel jPanel9 = new JPanel();
	JRadioButton runCplex_radioButton = new JRadioButton();
	JRadioButton runInclusionExclusion_radioButton = new JRadioButton();
	JRadioButton runDP_radioButton = new JRadioButton();
	JRadioButton runIDP_radioButton = new JRadioButton();
	JRadioButton runODP_radioButton = new JRadioButton();
	JRadioButton runIP_radioButton = new JRadioButton();
	JRadioButton runODPIP_radioButton = new JRadioButton();
	JLabel jLabel1 = new JLabel();
	JLabel jLabel2 = new JLabel();
	JLabel jLabel6 = new JLabel();
	JLabel jLabel7 = new JLabel();
	JLabel jLabel10 = new JLabel();
	JLabel jLabel11 = new JLabel();
	JLabel jLabel14 = new JLabel();
	JLabel jLabel15 = new JLabel();
	JLabel jLabel16 = new JLabel();
	JCheckBox ipOrdersIntegerPartitionsAscendingly_checkBox = new JCheckBox();
	JCheckBox printDetailedResultsOfIPToFiles_checkBox = new JCheckBox();
	JCheckBox printTimeTakenByIPForEachSubspace_checkBox = new JCheckBox();
	public JTextArea textArea = new JTextArea();	
	TitledBorder titledBorder1 = new TitledBorder("");
	JScrollPane jScrollPane1 = new JScrollPane();	
	private void main()
	{
		//System.out.println("Debug Point -------------1");
		
		Input input = readInputFromGUI();  
		//Input input = new Input();
		//input.initInput();
		//input.printInterimResultsOfIPToFiles = false;
		//input.printTimeTakenByIPForEachSubspace = false;
		//input.orderIntegerPartitionsAscendingly = false;
		//input.acceptableRatio = 100.0;
		//Calendar calendar = Calendar.getInstance();
		//SimpleDateFormat simpleDateFormat = new SimpleDateFormat( "yyyy-MM-dd (HH-mm-ss)" );
		//input.outputFolder = input.folderInWhichCoalitionValuesAreStored+"/"+SolverNames.toString(input.solverName)+" "+simpleDateFormat.format(calendar.getTime());;					
		//input.readCoalitionValuesFromFile = true;
		
		
		//if( input == null ) return;
		//System.out.println("Debug Point -------------2 ");
		
		try {
			//List<String> allLines = Files.readAllLines(Paths.get("D:\\UDS_LECTURES\\qai\\AAAI21_BOSS\\Data set generation\\BOSS_runtimes.txt"));
			List<String> allLines = Files.readAllLines(Paths.get(inputAndOutputPathAndFolderName_textField.getText()));
			
			System.out.println(inputAndOutputPathAndFolderName_textField.getText());
			String path  = inputAndOutputPathAndFolderName_textField.getText();
			String output_file_path = path.substring(0,path.lastIndexOf(".")) + new String("_results.txt");
			System.out.println(output_file_path);
			PrintWriter writer = new PrintWriter(output_file_path,"UTF-8");
			//writer.println("Distribution"+new String(" ")+"Number_of_Agents"+new String(" ")+result.ipTime+new String(" ")+General.convertArrayToString(result.get_ipBestCSFound()));
			
			for (String line : allLines) {
				Instant start = Instant.now();
				String[] tokens=line.split(" ");
				List<String> new_tokens = new LinkedList<String>(Arrays.asList(tokens));
				
				String distribution_name = new_tokens.remove(0);
				//System.out.println("distribution_name = "+distribution_name);
				
				String number_of_agents = new_tokens.remove(0);
				//System.out.println("number_of_agents = "+Integer.parseInt(number_of_agents));
				
				//System.out.println("Printing coalition values as strings");
				//for (String token: new_tokens) {
				//	System.out.println(token);
				//}
				
				double[] coalition_values = new double[new_tokens.size()+1];
				coalition_values[0] = 0;
				for (int i = 0; i < new_tokens.size(); ++i) {
					coalition_values[i+1] = Double.parseDouble(new_tokens.get(i));
				}
				//System.out.println("Printing coalition values as doubles");
				//for (double coalition_value: coalition_values) {
				//	System.out.println(coalition_value);
				//}
				
				//System.out.println(Arrays.toString(coalition_values));
				
				
				input.numOfAgents = Integer.parseInt(number_of_agents);
				input.coalitionValues = coalition_values;
				Result result = (new MainSolver()).solve( input );
				//System.out.println(General.convertArrayToString(result.get_ipBestCSFound()));
				//System.out.println(result.ipTime);
				printResultOnGUI(input, result);
				
				Instant end = Instant.now();
				Duration timeElapsed = Duration.between(start, end);
				
				writer.println(distribution_name+new String(" ")+number_of_agents+new String(" ")+timeElapsed.toMillis()+new String(" ")+General.convertArrayToString(result.get_ipBestCSFound()));
				System.out.println(distribution_name+new String(" ")+number_of_agents+new String(" ")+timeElapsed.toMillis()+new String(" ")+General.convertArrayToString(result.get_ipBestCSFound()));
				//System.out.println("Complete line is -----------"+line);
				
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		//System.out.println("Debug Point -------------3 ");
		
		//System.out.println(input.numOfAgents);
		//System.out.println(Arrays.toString(input.coalitionValues));
		
 		//Result result = (new MainSolver()).solve( input );
 		
 		
 		//System.out.println(General.convertArrayToString(result.get_ipBestCSFound()));
 		//System.out.println(result.ipTime);
 		//printResultOnGUI(input, result);
	}
	public Input readInputFromGUI()
	{
		Input input = new Input();
		input.initInput();
 		//input.numOfAgents = (new Integer(numOfAgents_textField.getText())).intValue();
		input.numOfAgents = 1;
		if( input.numOfAgents > 27){
			JOptionPane.showMessageDialog(null,	"The number of coalition structures cannot be handled by java for more than 25 agents!","Alert", JOptionPane.ERROR_MESSAGE);
			return(null); 
 		}
		 if( runODPIP_radioButton.isSelected()) input.solverName = SolverNames.ODPIP;
		if (printDetailedResultsOfIPToFiles_checkBox.isSelected()) input.printInterimResultsOfIPToFiles = true; else input.printInterimResultsOfIPToFiles = false;
		if (printTimeTakenByIPForEachSubspace_checkBox.isSelected()) input.printTimeTakenByIPForEachSubspace = true; else input.printTimeTakenByIPForEachSubspace = false;
 		if (ipOrdersIntegerPartitionsAscendingly_checkBox.isSelected()) input.orderIntegerPartitionsAscendingly = true; else input.orderIntegerPartitionsAscendingly = false;
		input.acceptableRatio = 100.0;
 		input.folderInWhichCoalitionValuesAreStored = inputAndOutputPathAndFolderName_textField.getText();
 		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat( "yyyy-MM-dd (HH-mm-ss)" );
		input.outputFolder = input.folderInWhichCoalitionValuesAreStored+"/"+SolverNames.toString(input.solverName)+" "+simpleDateFormat.format(calendar.getTime());;					
		input.readCoalitionValuesFromFile = true;
		//input.readCoalitionValuesFromFile()
		input.readCoalitionValuesFromFile( inputFileName_textField.getText() );
		return( input );
	}
	public void printResultOnGUI(Input input, Result result)
	{
		if ( (input.solverName == SolverNames.ODPIP) ||( input.solverName == SolverNames.ODPinParallelWithIP ) ){
			textArea.append("----------------------------------------------------\n    "+SolverNames.toString(input.solverName)+" ("+input.numOfAgents+" agents)\n----------------------------------------------------\n");
                        if( input.numOfRunningTimes == 1 ){
				textArea.append("\nThe time for IP to scan the input (in milliseconds):\n"+result.ipTimeForScanningTheInput+"\n");			
				textArea.append("\nThe total run time of "+SolverNames.toString(input.solverName)+" (in milliseconds):\n"+result.ipTime+"\n");
				textArea.append("\nThe best coalition structure found by "+SolverNames.toString(input.solverName)+" is:\n"+General.convertArrayToString(result.get_ipBestCSFound())+"\n");
				textArea.append("\nThe value of this coalition structure is:\n"+result.get_ipValueOfBestCSFound()+"\n");
				if( input.solverName == SolverNames.IP ){
					textArea.append("\nThe number of expansions made by IP:\n"+result.ipNumOfExpansions+"\n");
					textArea.append("\nBased on this, the percentage of search-space that was searched by "+SolverNames.toString(input.solverName)+" is:\n"+(double)(result.ipNumOfExpansions*100)/result.totalNumOfExpansions+"%\n");
				}
			}else{
				textArea.append("\nThe average time for "+SolverNames.toString(input.solverName)+" to scan the input (in milliseconds):\n"+result.ipTimeForScanningTheInput+" +/- "+result.ipTimeForScanningTheInput_confidenceInterval+"\n");			
				textArea.append("\nThe average run time of "+SolverNames.toString(input.solverName)+" (in milliseconds):\n"+result.ipTime+" +/- "+result.ipTime_confidenceInterval+"\n");
				if( input.solverName == SolverNames.IP ){
					textArea.append("\nThe average number of expansions made by "+SolverNames.toString(input.solverName)+":\n"+result.ipNumOfExpansions+" +/- "+result.ipNumOfExpansions_confidenceInterval+"\n");
					textArea.append("\nBased on this, the average percentage of search-space that was searched by "+SolverNames.toString(input.solverName)+" is:\n"+(double)(result.ipNumOfExpansions*100)/result.totalNumOfExpansions+"% +/- "+(double)(result.ipNumOfExpansions_confidenceInterval*100)/result.totalNumOfExpansions+"%\n");
				}
			}
		}
	}
	private void actionPerformed(ActionEvent e)
	{
 		if (e.getActionCommand() == run_button.getActionCommand()) { main(); }
	}
	private void jbInit() throws Exception
	{
		this.setResizable(false);
		setSize(new Dimension(990, 750));
		setTitle("The hybrid BOSS algorithm for optimal CSG problem.");
		textArea.setBorder(BorderFactory.createEtchedBorder());
		textArea.setText("");
		contentPane = (JPanel) getContentPane();
		contentPane.setLayout(null);
		contentPane.setMinimumSize(new Dimension(1, 1));
		contentPane.add(jLabel6);
		contentPane.add(jLabel7);
		contentPane.add(jLabel10);
		contentPane.add(jLabel11);
		contentPane.add(jLabel14);
		contentPane.add(jScrollPane1);
		jScrollPane1.getViewport().add(textArea);
		jScrollPane1.getViewport().setBackground(Color.black);
		jScrollPane1.setBounds(new Rectangle(15, 15, 616, 690));
		contentPane.add(run_button);
		run_button.setBounds(new Rectangle(650, 14, 61, 26));
		run_button.setMnemonic('R');
		run_button.setText("Run");
		run_button.addActionListener(new applicationGUI_actionAdapter(this));
		//contentPane.add(jLabel1);
		//jLabel1.setBounds(new Rectangle(740, 15, 95, 21));
		//jLabel1.setText("Number of agents");
		//contentPane.add(numOfAgents_textField);
		//numOfAgents_textField.setBounds(new Rectangle(840, 15, 38, 22));
		//numOfAgents_textField.setText("");
		contentPane.add(jPanel1);
		contentPane.add(jPanel5);
		contentPane.add(jPanel7);
		contentPane.add(jPanel9);
		jPanel1.setBounds(new Rectangle(650, 55, 315, 155));
		jPanel1.setBorder(BorderFactory.createEtchedBorder());
		jPanel1.setLayout(null);
		jLabel14.setText("    Input file (and Input/output folder)   ");
		jLabel14.setBounds(new Rectangle(670, 45, 210, 18));
		jLabel14.setOpaque(true);	
		jPanel1.add(jLabel15);
		jLabel15.setBounds(new Rectangle(10, 7, 290, 50));
		jLabel15.setText("<html>The complete path of the file(with .txt file extension) containing the distributions, number of agents and coalition values (any output file(s) will be placed in the same folder)</html>");
		jPanel1.add(inputAndOutputPathAndFolderName_textField);
		inputAndOutputPathAndFolderName_textField.setBounds(new Rectangle(10, 55, 290, 22));
		//inputAndOutputPathAndFolderName_textField.setText("D:\\UDS_LECTURES\\qai\\New_boss");
		//inputAndOutputPathAndFolderName_textField.setText("D:\\UDS_LECTURES\\qai\\AAAI21_BOSS\\Data set generation");
		//jPanel1.add(jLabel16);
		//jLabel16.setBounds(new Rectangle(10, 75, 290, 50));
		//jLabel16.setText("<html>Name (with extension) of file containing coalition values</html>");
		//jPanel1.add(inputFileName_textField);
		//inputFileName_textField.setBounds(new Rectangle(10, 120, 290, 22));
		//nputFileName_textField.setText("coalition_values.txt");
		//inputFileName_textField.setText("BOSS_runtimes.txt");
		jPanel7.setBounds(new Rectangle(650, 230, 315, 190));
		jPanel7.setBorder(BorderFactory.createEtchedBorder());
		jPanel7.setLayout(null);
		jLabel10.setText("    Select  algorithm");
		jLabel10.setBounds(new Rectangle(670, 220, 135, 18));
		jLabel10.setOpaque(true);
		jPanel7.add(runODPIP_radioButton);
		radioButtonGroup2.add(runODPIP_radioButton);
		runODPIP_radioButton.setBounds(new Rectangle(6, 110, 270,22));
		runODPIP_radioButton.setText("Run BOSS");
		runODPIP_radioButton.setSelected(true);
        }
	class applicationGUI_actionAdapter implements ActionListener {
		private MainFrame adaptee;
		applicationGUI_actionAdapter(MainFrame adaptee) {
			this.adaptee = adaptee;
		}
		public void actionPerformed(ActionEvent e) {
			adaptee.actionPerformed(e);
		}
	}
	public MainFrame(String title) {
		super(title);
		try { setDefaultCloseOperation(EXIT_ON_CLOSE);	jbInit(); }
		catch (Exception exception) {	exception.printStackTrace(); }
	}
}