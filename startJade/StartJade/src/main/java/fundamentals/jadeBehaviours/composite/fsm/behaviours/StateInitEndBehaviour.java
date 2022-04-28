package fundamentals.jadeBehaviours.composite.fsm.behaviours;

import jade.core.behaviours.OneShotBehaviour;

/**
 * Simple state
 * @author hc
 *
 */
public class StateInitEndBehaviour extends OneShotBehaviour {
	
	private static final long serialVersionUID = 3945492807451336559L;

	/**
	 * Initial and ending state
	 */
	public StateInitEndBehaviour() {
		super();
	}
	
	public void action() {
		System.out.println("Start/end and sometime visiting behaviour");
	}
}