package fundamentals.jadeBehaviours.composite.fsm.behaviours;

import jade.core.behaviours.OneShotBehaviour;


/**
 * One shot behaviour used as B state
 * @author hc
 *
 */
public class StateBehaviour extends OneShotBehaviour {

	private static final long serialVersionUID = -3800304354660736222L;
	private int exitValue;
	private int upperbound;
	

	/**
	 * Initiate the state with an exit value upperbound.
	 * At each call it will generate a random value between [0-max], print it and use it as a trigger condition to move to the next step
	 * @param max Upper bound to compute the current value for the generator
	 */
	public StateBehaviour(int max) {
		super();
		upperbound=max;
		exitValue = max; //be careful, even if its a oneshot the behaviour is not cleared when used in an FSM
	}

	public void action() {
		exitValue = (int) (Math.random() * upperbound);
		System.out.println("We are in B, transition value : "+exitValue);
		System.out.print("The next step we will be in: ");
		if (exitValue==2) {
			System.out.println("B again");
		}else {
			System.out.println((exitValue==1)? "terminal state C" : "state A");
		}
	}

	public int onEnd() {
		return exitValue;
	}
	
	
}	
