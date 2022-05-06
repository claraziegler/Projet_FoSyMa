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


public class InterblocageCollect extends SimpleBehaviour {
	
	private MapRepresentation myMap;
	//private AID receivers;
	private int cpt;
	private List<String> agentNames;
	private String objectif;
	
	private boolean finished = false;
	

	
	public InterblocageCollect(ExploreCoopAgent myAgent,MapRepresentation mymap,List<String> agentNames,String objectif) {
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
		
		

	}

	@Override
	public boolean done() {
		return finished;
	}
}
