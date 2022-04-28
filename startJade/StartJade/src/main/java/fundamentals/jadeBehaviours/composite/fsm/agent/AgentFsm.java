package fundamentals.jadeBehaviours.composite.fsm.agent;

import fundamentals.jadeBehaviours.composite.fsm.behaviours.StateBehaviour;
import fundamentals.jadeBehaviours.composite.fsm.behaviours.StateInitEndBehaviour;
import jade.core.Agent;
import jade.core.behaviours.FSMBehaviour;

/**
 * <pre>
 * Example of Finite State Machine behaviour instantiation
 * 
 * 3 states : A,B,C with their associated behaviours.
 *  - From A you unconditionally go to B
 *  - From B you can go to C (final state), stay on B or move to A according to the defined transition-conditions 
 * </pre>
 * @see <a href="https://startjade.gitlab.io/agents-implementation/behaviours/#finite-state-machine-behaviour">starJade's documentation</a>
 *  
 * @author hc
 *
 */
public class AgentFsm extends Agent {

	private static final long serialVersionUID = -4149255636412877945L;

	// State names
	private static final String A = "A";
	private static final String B = "B";
	private static final String C = "C";

	protected void setup() {		
		FSMBehaviour fsm = new FSMBehaviour(this);	

		// Define the different states and behaviours
		fsm.registerFirstState(new StateInitEndBehaviour(), A);
		fsm.registerState(new StateBehaviour(5), B);
		fsm.registerLastState(new StateInitEndBehaviour(), C);

		// Register the transitions
		fsm.registerDefaultTransition(A,B);//Default we unconditionnaly go to B after A
		fsm.registerTransition(B,B, 2,new String[]{B});//Cond 2 - If B.onEnd() returns 2, we go to B
		fsm.registerTransition(B,C, 1);//Cond 1  - If B.onEnd() returns 1, we go to C
		fsm.registerDefaultTransition(B,A,new String[]{B});//Default - otherwise we go to A.
		addBehaviour(fsm);
	}
}