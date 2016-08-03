
public class HumanAgent implements Agent {

	int move = -1;
	
	@Override
	public int decideMove(GameModel game) {
		// block until button action is received
		synchronized(this) {
		    try {
		        this.wait();
		    } catch (InterruptedException e) {
		        
		    }
		}
		int result = move;
		move = -1;
		
		System.out.println("HumanAgent: returning "+result);
		return result;
	}
	
	// to be called by game system when a button is pressed
	public void buttonAction(int m) {
		
		System.out.println("HumanAgent: I got notified.");
		move = m;
		this.notify();
	}

}
