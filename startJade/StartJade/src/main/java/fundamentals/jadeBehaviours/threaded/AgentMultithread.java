package fundamentals.jadeBehaviours.threaded;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.ThreadedBehaviourFactory;

/**
 * This agent illustrates the instantiation of threaded behaviours.
 * <pre>
 * By default, 1 agent = 1 thread. 
 * But you can decide to let your behaviours live in their own threads if they, for example, execute a long and blocking task (gui,computation,..) 
 * </pre>
 * @author hc
 *
 */
public class AgentMultithread extends Agent{

	private static final long serialVersionUID = -8307819785900795961L;

	private ThreadedBehaviourFactory tbf;
	
	protected void setup() {
		//0) Create the thread factory
		tbf= new ThreadedBehaviourFactory();
		
		// 1) You create the behaviour(s) you want
		Behaviour myBehaviour= new OneShotBehaviour(this) {	
			private static final long serialVersionUID = 3688292199950141086L;
			@Override
			public void action() {
				for (int i=1;i<100000;i++) {
					System.out.println(i);
				}
			}
		};
		
		//2) You add it in its own thread. Its works with all behaviours' types.
		this.addBehaviour(tbf.wrap(myBehaviour));
	}
}
