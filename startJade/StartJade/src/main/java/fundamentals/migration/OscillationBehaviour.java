package fundamentals.migration;

import jade.core.Agent;
import jade.core.ContainerID;
import jade.core.behaviours.TickerBehaviour;

/**
 * Illustrating example of intra-platform migration.
 * <p>
 * The agent who uses this behaviour periodically moves between two HARDCODED containers' name.
 * </p>
 * @author hc
 *
 */
public class OscillationBehaviour extends TickerBehaviour {

	private static final long serialVersionUID = 1L;

	private boolean oscillator;
	private ContainerID cID;
	private final static String container1="Mycontainer1";
	private final static String container2="Mycontainer3";
	
	/**
	 * The agent periodically migrates between container Mycontainer1 and Mycontainer3 
	 * 
	 * @param a Ref to the agent we are adding the behaviour 
	 * @param ip IP of the targeted computer
	 * @param port port of the targeted computer
	 * @param period (in ms). Periodicity of the migration between containers
	 */
	public OscillationBehaviour(Agent a, String ip,String port,long period) {
		super(a, period);
		this.oscillator=true;
		
		this.cID= new ContainerID();
		this.cID.setPort(port);
		this.cID.setAddress(ip); //IP of the host of the targeted container		
	}
	
	@Override
	protected void onTick() {
		
		if (oscillator){
			cID.setName(container1);
		}else{
			cID.setName(container2);	
		}
		this.oscillator=!this.oscillator;
		
		this.myAgent.doMove(cID);// LAST method to call in a behaviour, ALWAYS
	}

	
	
}
