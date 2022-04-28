package protocols.consensus.paxos;



/**
 * 
 * @startuml
 * Client->>Proposer: request(topic, val1)
 * Proposer->>Acceptor1:Propose(topic, 1)
 * note over Acceptor1: The first Proposer is always accepted.
 * Proposer->>Acceptor2:Propose(topic, 1)
 * note over Acceptor2: The first Proposer is always accepted.
 * Acceptor2->>Proposer:Promise(topic, 1, null)
 * Acceptor1->>Proposer:Promise(topic, 1, null)
 * Proposer->>Acceptor1:Accept(topic, 1, val)
 * Proposer->>Acceptor2:Accept(topic, 1, val)
 * Acceptor2->>Proposer:Accepted(topic, 1, val)    
 * Acceptor1->>Proposer:Accepted(topic, 1, val)
 * Proposer->>Leaner:Decide(topic, val)
 * Leaner->>Client:res
 * note over Client: res is the answer of the request(topic, val).
 * @enduml
 * 
 * 
 *
 */
/**
 * This class is used to implement the Basic Paxos consensus.
 * 
 * @author axel foltyn
 *
 */
public class BasicPaxos extends Paxos {

	public BasicPaxos() {
		super(PaxosImplemented.BasicPaxos);
	}

}
