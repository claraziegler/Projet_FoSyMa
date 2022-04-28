package exercices.sum1_n.agents;

import exercices.sum1_n.behaviours.*;
import jade.core.Agent;



/*Agent A sends 2 values to AgentSomme*/

public class Agent2 extends Agent{
	
	private static final long serialVersionUID = 1410401284578853709L;
	
	protected void setup(){

		super.setup();

		//get the parameters given into the object[]
		final Object[] args = getArguments();
		if(args.length!=0){
			System.err.println("Malfunction - no parameter expected");
			System.exit(-1);
		}else{
			
		//Add the behaviours
		
		addBehaviour(new ReceiveMessageBehaviour(this));
		//addBehaviour(new SendValuesBehaviour(this,"AgentSomme"));

		System.out.println("The sender agent "+this.getLocalName()+ " is started");
		
		}
	
	}
	
}

