package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.util.List;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
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


public class CollectBehaviour extends SimpleBehaviour {
	
	private MapRepresentation myMap;
	//private AID receivers;
	private int cpt;
	private List<String> agentNames;
	private String objectif;
	
	private boolean finished = false;
	

	
	public CollectBehaviour(ExploreCoopAgent myAgent,MapRepresentation mymap,List<String> agentNames,String objectif) {
		super();
		this.myMap=mymap;
		//this.receivers=receivers;	
		this.cpt=0;
		this.agentNames = agentNames;
		this.objectif = objectif;
		System.out.println("Debut collecte behaviour : "+myAgent.getName());
		
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -568863390879327961L;
	
	
	

	@Override
	public void action() {
		
		if (this.objectif == null) {
			if (((AbstractDedaleAgent) myAgent).getMyTreasureType().equals("Gold")) {
				this.objectif = ((ExploreCoopAgent) myAgent).choisirTresor("or");
			}
			else if (((AbstractDedaleAgent) myAgent).getMyTreasureType().equals("Diamond")){
				this.objectif = ((ExploreCoopAgent) myAgent).choisirTresor("diamant");
			}
			else {
				String type = ((ExploreCoopAgent) myAgent).selectionnerType();
				this.objectif = ((ExploreCoopAgent) myAgent).choisirTresor(type);
			}
			
		}
		System.out.println(this.myAgent.getName()+" objectif : "+this.objectif);
		String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		if(myPosition.equals(this.objectif)) {
			List<Couple<String,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition
			List<Couple<Observation,Integer>> lObservations= lobs.get(0).getRight();
			System.out.println(lObservations);
			((AbstractDedaleAgent) this.myAgent).openLock(lObservations.get(0).getLeft());
			
			/*
			if (((AbstractDedaleAgent) myAgent).getMyTreasureType().equals("Gold")) {
				((AbstractDedaleAgent) this.myAgent).openLock(Observation.GOLD);
			}
			else if (((AbstractDedaleAgent) myAgent).getMyTreasureType().equals("Diamond")){
				((AbstractDedaleAgent) this.myAgent).openLock(Observation.DIAMOND);
			}
			else {
				 
			}*/
			
			int picked = ((AbstractDedaleAgent) this.myAgent).pick();
			System.out.println("PIIIIIIIIIICK !!!!!!!");
			((ExploreCoopAgent) this.myAgent).mise_a_jour(lObservations.get(0).getLeft(),myPosition,picked);
			this.objectif = null;
		}
		else {
			String nextNode=null;
			nextNode=this.myMap.getShortestPath(myPosition,objectif).get(0);
			((AbstractDedaleAgent)this.myAgent).moveTo(nextNode);
		}
		//System.out.println(this.myAgent.getName()+" objectif : "+this.objectif);
		ACLMessage msg1 = new ACLMessage(ACLMessage.INFORM);
		msg1.setProtocol("REPERAGE");
		msg1.setSender(this.myAgent.getAID());
		/*if (this.myAgent.getLocalName().equals("Explo1")) {
			msg1.addReceiver(new AID("Explo2",false));
		}else {
			msg1.addReceiver(new AID("Explo1",false));
		}*/
		for (String agentName : this.agentNames) {
			if (agentName!=this.myAgent.getLocalName()) {
				msg1.addReceiver(new AID(agentName,AID.ISLOCALNAME));
			}
		}
		msg1.setContent("Il y a quelqu'un ?");
		((AbstractDedaleAgent)this.myAgent).sendMessage(msg1);
		this.myAgent.doWait(500);
		
		MessageTemplate msgTemplate1=MessageTemplate.and(
				MessageTemplate.MatchProtocol("REPERAGE"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		ACLMessage msgReceived1=this.myAgent.receive(msgTemplate1);
		if(msgReceived1 != null) {
			ACLMessage msg2 = new ACLMessage(ACLMessage.INFORM);
			msg2.setProtocol("REPERAGE");
			msg2.setSender(this.myAgent.getAID());
			/*if (this.myAgent.getLocalName().equals("Explo1")) {
				msg2.addReceiver(new AID("Explo2",false));
			}else {
				msg2.addReceiver(new AID("Explo1",false));
			}*/
			for (String agentName : this.agentNames) {
				if (agentName!=this.myAgent.getLocalName()) {
					msg2.addReceiver(new AID(agentName,AID.ISLOCALNAME));
				}
			}
			msg2.setContent("Il y a quelqu'un ?");
			((AbstractDedaleAgent)this.myAgent).sendMessage(msg2);
			
			System.out.println(this.myAgent.getLocalName() + ": " + msgReceived1.getContent() + " par " + msgReceived1.getSender().getLocalName());
			this.myAgent.addBehaviour(new PartageMapCollectBehaviour((ExploreCoopAgent) this.myAgent,this.myMap,msgReceived1.getSender(),this.agentNames,this.objectif));
			//this.myAgent.addBehaviour(new ShareMapBehaviour(this.myAgent,500,this.myMap,this.list_agentNames));
			System.out.println("Behaviour ajouté");
			finished = true;
			
		}
	}

	@Override
	public boolean done() {
		return finished;
	}
}
