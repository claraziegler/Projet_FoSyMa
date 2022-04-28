package fundamentals.jadeBehaviours.composite.parallel;

import jade.core.Agent;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.WakerBehaviour;


/**
 * <pre>
 * Be careful, the name "Parallel behaviour" is misleading.
 * 
 * Jade's parallel behaviour does NOT allow to execute different behaviour in different threads.
 * There is still only one thread that control the agent's behaviours.
 * The idea behind what Jade's dev called a parallel behaviour is a 
 * behaviour composed of a set of subbehaviours trigerable at the same time.
 * The main behaviour can be instructed to terminate when :
 * </pre>
 * <ul>
 * <li> ALL of its sub-behaviours have completed or,</li> 
 * <li> ANY sub-behaviour completes</li>
 * </ul>
 * 
 *  In this example, the parallel behaviour is composed of two sub-behaviours.
 *  One is printing integers between 0 to X, and the other is waiting for Y ms.
 *  The first one to finish terminates the parent behaviour.
 *  <p>
 *  For multithreaded behaviours, see {@link AgentMultiThread}
 *  </p>
 * @author hc
 *
 */
public class AgentParallel extends Agent {

	private static final long serialVersionUID = -6252552727773070677L;
	/**
	 *  count target
	 */
	private static final int targetCount=100;
	/**
	 * time available to complete the job
	 */
	private static final int timeoutMS=1;

	protected void setup() {	

		System.out.println("The agent have to count from 0 to "+targetCount+" in less than "+timeoutMS+ "ms");
		//We decide to stop the paraBehav as soon as one of its children is done.
		ParallelBehaviour paraBehav = new ParallelBehaviour(this,ParallelBehaviour.WHEN_ANY);

		//first task, count and print integer	
		paraBehav.addSubBehaviour(new CountBehaviour(targetCount));
		
		//second task, wait for 3s before ending everything
		paraBehav.addSubBehaviour(new WakerBehaviour(this,timeoutMS) {
			private static final long serialVersionUID = 1L;

			public void onWake() {
				System.out.println("Timeout reached before the completion of the task CountBehaviour");
			}
		});
		
		addBehaviour(paraBehav);
	}
}
