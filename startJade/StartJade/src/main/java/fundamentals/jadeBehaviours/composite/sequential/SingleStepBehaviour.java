package fundamentals.jadeBehaviours.composite.sequential;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;

/**
 * A oneShotbehaviour that print a string given in parameter when executed
 * @author hc
 *
 */
class SingleStepBehaviour extends OneShotBehaviour {
	
	private static final long serialVersionUID = -7450869229672942480L;
	private String myStep;
	   
	/**
	 * OneShot behaviour that print the string step when executed
	 * @param a agent
	 * @param step string to be printed
	 */
	   public SingleStepBehaviour(Agent a, String step) {
	     super(a);
	     myStep = step;
	   }
	   
	   public void action() {
	     System.out.println("Step "+myStep);
	   } 
	}