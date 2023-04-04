package processing.app;

/**
 * This class is a compile time resource that is replaced by the Arduino class at runtime
 */
import javax.swing.JFrame;


@SuppressWarnings("serial")
public class Editor extends JFrame implements RunnerListener {

	@Override
	public void statusError(String message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void statusError(Exception exception) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void statusNotice(String message) {
		// TODO Auto-generated method stub
		
	}

}
