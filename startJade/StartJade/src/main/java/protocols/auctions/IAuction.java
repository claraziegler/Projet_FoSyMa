package protocols.auctions;
import java.util.List;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import protocols.Role;


/** an interface implemented by the different types of 
 * auctions to get the behaviors of each agent according to the role    **/

public interface IAuction {
	public abstract List<Behaviour> getBehaviours(Role role,Agent agent ,List<String> listagents );
	
}

