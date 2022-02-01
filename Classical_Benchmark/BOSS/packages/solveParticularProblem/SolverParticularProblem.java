package solveParticularProblem;

import java.awt.Toolkit;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.Dimension;

public class SolverParticularProblem
{
    boolean packFrame = false;

    
    public SolverParticularProblem()
    {
        MainFrame frame = new MainFrame();
         
        if (packFrame) {
            frame.pack();
        } else {
            frame.validate();
        }

         Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = frame.getSize();
        if (frameSize.height > screenSize.height) {
            frameSize.height = screenSize.height;
        }
        if (frameSize.width > screenSize.width) {
            frameSize.width = screenSize.width;
        }
        frame.setLocation((screenSize.width  - frameSize.width ) / 2,
                          (screenSize.height - frameSize.height) / 2);
        
         frame.getRootPane().setDefaultButton(frame.run_button);

        frame.setVisible(true);
    }

    
    public static void main(String[] args) {
    	SwingUtilities.invokeLater(new Runnable()
    	{
    		public void run() {
    			try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
    			catch (Exception exception) { exception.printStackTrace(); }    			
    			new SolverParticularProblem();
    		}
    	});
    }
}