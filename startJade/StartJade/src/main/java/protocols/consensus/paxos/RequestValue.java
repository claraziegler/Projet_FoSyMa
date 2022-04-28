package protocols.consensus.paxos;

import java.util.ArrayList;
import java.util.List;

import jade.util.leap.Serializable;

/**
 * Class used for value request.
 * it contains the topic, the customer's name and the value of the request
 * 
 * @author Axel Foltyn
 *
 */
public class RequestValue implements Serializable{

	private static final long serialVersionUID = 2662448851273743383L;

	private String concensusTopic;

	/*
	 * values use in :
	 * -BasicPaxos
	 */
	private String client;
	private String value;

	/*
	 * values use in :
	 * MultiPaxos
	 */
	private List<String> clients = new ArrayList<String>();
	private List<String> values = new ArrayList<String>();

	public RequestValue(String client, String topic, String value) {
		this.client = client;
		this.concensusTopic = topic;
		this.value = value;
		this.clients.add(client);
		this.values.add(value);
	}

	public RequestValue(List<String> clients, List<String> values) {
		this.clients = clients;
		this.values = values;
	}

	public String getClient() {
		return client;
	}

	public String getValue() {
		return value;
	}

	public List<String> getClients() {
		return clients;
	}

	public List<String> getValues() {
		return values;
	}

	public void addValue(RequestValue value) {
		values.add(value.getValue());
		clients.add(value.getClient());
	}

	public void setClient(String client) {
		this.client = client;
	}

	public String getConcensusTopic() {
		return concensusTopic;
	}



}
