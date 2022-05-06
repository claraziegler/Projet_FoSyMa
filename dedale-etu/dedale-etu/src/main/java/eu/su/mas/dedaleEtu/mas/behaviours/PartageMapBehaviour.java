package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.util.List;

import dataStructures.serializableGraph.SerializableSimpleGraph;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreCoopAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

/**
 * The agent periodically share its map.
 * It blindly tries to send all its graph to its friend(s)  	
 * If it was written properly, this sharing action would NOT be in a ticker behaviour and only a subgraph would be shared.

 * @author hc
 *
 */
public class PartageMapBehaviour extends SimpleBehaviour {
	
	private MapRepresentation myMap;
	private AID receivers;
	private int cpt;
	private List<String> agentNames;
	
	private boolean finished = false;
	private int envoie;
	private int recu;
	private String objectif;
	private long debut_explo;

	/**
	 * The agent periodically share its map.
	 * It blindly tries to send all its graph to its friend(s)  	
	 * If it was written properly, this sharing action would NOT be in a ticker behaviour and only a subgraph would be shared.

	 * @param a the agent
	 * @param period the periodicity of the behaviour (in ms)
	 * @param mymap (the map to share)
	 * @param receivers the list of agents to send the map to
	 */
	public PartageMapBehaviour(ExploreCoopAgent myagent,MapRepresentation mymap, AID receivers,List<String> agentNames,String objectif,long debut_explo) {
		super();
		this.myMap=mymap;
		this.receivers=receivers;	
		this.cpt=0;
		this.agentNames = agentNames;
		this.envoie = 0;
		this.recu = 0;
		this.objectif = objectif;
		this.debut_explo= debut_explo;
		System.out.println("Début de la communication entre "+myagent.getName()+" et "+receivers.getLocalName());
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -568863390879327961L;
	
	
	

	@Override
	public void action() {
		
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setProtocol("SHARE-TOPO");
		msg.setSender(this.myAgent.getAID());
		msg.addReceiver(this.receivers);
			
		SerializableSimpleGraph<String, MapAttribute> sg=this.myMap.getSerializableGraph();
		try {					
			msg.setContentObject(sg);
		} catch (IOException e) {
			e.printStackTrace();
		}
		((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
		System.out.println(this.myAgent.getName() + " envoi map");
		//this.myAgent.doWait(500);
		
		ACLMessage msg3 = new ACLMessage(ACLMessage.INFORM);
		msg3.setProtocol("SHARE-LISTES");
		msg3.setSender(this.myAgent.getAID());
		msg3.addReceiver(this.receivers);
			
		msg3.setContent(((ExploreCoopAgent) myAgent).list_serialisees());
		
		((AbstractDedaleAgent)this.myAgent).sendMessage(msg3);
		System.out.println(this.myAgent.getName() + " envoi liste");
		//this.myAgent.doWait(500);
		
		
		
		
		
		MessageTemplate msgTemplate=MessageTemplate.and(
				MessageTemplate.MatchProtocol("SHARE-TOPO"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		ACLMessage msgReceived=this.myAgent.receive(msgTemplate);
		if (msgReceived!=null) {
			SerializableSimpleGraph<String, MapAttribute> sgreceived=null;
			try {
				sgreceived = (SerializableSimpleGraph<String, MapAttribute>)msgReceived.getContentObject();
			} catch (UnreadableException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.myMap.mergeMap(sgreceived);
			
			ACLMessage msg2 = new ACLMessage(ACLMessage.INFORM);
			msg2.setProtocol("ACK");
			msg2.setSender(this.myAgent.getAID());
			msg2.addReceiver(msgReceived.getSender());
			
			msg2.setContent("Bien recu");
			((AbstractDedaleAgent)this.myAgent).sendMessage(msg2);
			System.out.println(this.myAgent.getName() + " map reçue");
			//this.myAgent.doWait(500);
			
		}
		
		MessageTemplate msgTemplate2=MessageTemplate.and(
				MessageTemplate.MatchProtocol("SHARE-LISTES"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		ACLMessage msgReceived2=this.myAgent.receive(msgTemplate2);
		if (msgReceived2!=null) {
			((ExploreCoopAgent) myAgent).deserialize(msgReceived2.getContent());
			System.out.println(this.myAgent.getName() + " liste reçue");
			//this.myAgent.doWait(500);
		}
		
		
		
		
		MessageTemplate msgTemplate1=MessageTemplate.and(
				MessageTemplate.MatchProtocol("ACK"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		ACLMessage msgReceived1=this.myAgent.receive(msgTemplate1);
		if((msgReceived1 != null)) {
			System.out.println(msgReceived1.getContent());
			this.myAgent.addBehaviour(new ExploCoopBehaviour((ExploreCoopAgent)this.myAgent,this.myMap,this.agentNames,this.objectif,this.debut_explo));
			System.out.println(this.myAgent.getName() + " ACK reçu");
			//this.myAgent.doWait(500);
			while (this.myAgent.receive() != null) {
				;
			}
			finished = true;
			System.out.println("Fin de la communication de "+this.myAgent.getName());
		}
		else {
			if (this.cpt>=5) {
				this.myAgent.addBehaviour(new ExploCoopBehaviour((ExploreCoopAgent)this.myAgent,this.myMap,this.agentNames,this.objectif,this.debut_explo));
				System.out.println("Arret forcé de la communication "+this.myAgent.getName());
				//this.myAgent.doWait(500);
				while (this.myAgent.receive() != null) {
					;
				}
				finished=true;
			}
			else {
				this.cpt+=1;
			}
		}

		
	}

	@Override
	public boolean done() {
		return finished;
	}
}
