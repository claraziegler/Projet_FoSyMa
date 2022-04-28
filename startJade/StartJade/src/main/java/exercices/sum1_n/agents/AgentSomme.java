package exercices.sum1_n.agents;

import exercices.sum1_n.behaviours.*;

import jade.core.Agent;
import java.util.ArrayList;


/**
 * This agent possess the SUM capability.
 * It expects to receive K values from agentA, sum them and reply with the result
 * The number of messages to receive as well as the sender's name are hardcoded bellow
 * 
 * @author hc
 *
 */
public class AgentSomme extends Agent{

	private static final long serialVersionUID = 3499482209671348272L;

	@Override
	protected void setup(){

		super.setup();

		ArrayList<String> agentList = new ArrayList<String>();
		agentList.add("Agent1");
		agentList.add("Agent2");
		
		//get the parameters given into the object[]
		final Object[] args = getArguments();
		if(args.length!=0){
			System.err.println("Error while creating the sum agent");
			System.exit(-1);

		}else{

			//Add the behaviours
			addBehaviour(new SendRequestBehaviour(this,1,agentList));
			addBehaviour(new SumValuesBehaviour(this,1,agentList));
			
			
			System.out.println("The agent possessing the SUM capability "+this.getLocalName()+ " is started");
		}
	}

	/**
	 * This method is automatically called after doDelete()
	 */
	protected void takeDown(){

	}
}
