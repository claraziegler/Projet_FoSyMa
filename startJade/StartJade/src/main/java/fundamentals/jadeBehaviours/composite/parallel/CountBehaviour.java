package fundamentals.jadeBehaviours.composite.parallel;


import jade.core.behaviours.SimpleBehaviour;

/**
 * Uninteresting behaviour that count until it reached its target value
 * 
 * @author hc
 *
 */
public class CountBehaviour extends SimpleBehaviour {

	private static final long serialVersionUID = -7424152157850361631L;
	private int targetVal;
	private int currentVal;
	private boolean finished=false;
	
	/**
	 * Print integer from 0 to i
	 * @param i
	 */
	public CountBehaviour(Integer i) {
		super();
		targetVal=i;
		currentVal=0;

	}
	public void action() {
		if (currentVal<targetVal) {
			System.out.println(currentVal);
			currentVal++;
		}else {
			finished=true;
		}

	}
	@Override
	public boolean done() {
		if (finished) {
			System.out.println("Finished before the deadline :)");
		}
		return finished;
	}


}
