package fundamentals.jadeBehaviours.composite.sequential;

import jade.core.Agent;
import jade.core.behaviours.SequentialBehaviour;

/**
 * Illustration of sequential behaviours.<br/> In a given sequence, the n-th behaviour is only trigerred when the (n-1)-th  is done.
 * <p>
 * Behaviour 1 is composed of two oneshot sub behaviours :
 * <ul>
 *  <li>1.1 and 1.2 that should be executed in this order</li>
 *  </ul>
 * 
 * Behaviour 2 is composed of 3 sub-behaviours :
 * <ul>
 * <li> 2.1 is a sequential (2.1.1 and 2.1.2 are oneShot)</li>
 * <li> 2.2 (oneShot)</li>
 * <li> 2.3 (oneShot)</li>
 * </ul>
 * </p>
 * Behaviours 1 and 2 can thus intertwine themselves, but their respectives components will execute in the choosen sequence.
 * @author hc
 *
 */
public class AgentSequential extends Agent {
	
	private static final long serialVersionUID = 7494115848353017052L;

	protected void setup() {

		SequentialBehaviour myBehaviour1 = new SequentialBehaviour(this);

		myBehaviour1.addSubBehaviour(new SingleStepBehaviour(this, "1.1"));
		myBehaviour1.addSubBehaviour(new SingleStepBehaviour(this, "1.2"));

		SequentialBehaviour myBehaviour2 = new SequentialBehaviour(this);
		
		SequentialBehaviour myBehaviour2_1 = new SequentialBehaviour(this);

		myBehaviour2_1.addSubBehaviour(new SingleStepBehaviour(this, "2.1.1"));
		myBehaviour2_1.addSubBehaviour(new SingleStepBehaviour(this, "2.1.2"));

		myBehaviour2.addSubBehaviour(myBehaviour2_1);

		myBehaviour2.addSubBehaviour(new SingleStepBehaviour(this, "2.2"));
		myBehaviour2.addSubBehaviour(new SingleStepBehaviour(this, "2.3"));

		addBehaviour(myBehaviour1);
		addBehaviour(myBehaviour2);
	} 
}


