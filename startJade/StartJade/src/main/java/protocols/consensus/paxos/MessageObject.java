package protocols.consensus.paxos;



import jade.util.leap.Serializable;



/**
 * Class used to give information of request.
 * it contains the topic, the customer's name and the value of the request
 * 
 * @author Axel Foltyn
 *
 */
public class MessageObject implements Serializable{
	private static final long serialVersionUID = -8221813884219227819L;
	
	private String topic;
	private int id;
	private RequestValue value;
	
	
	/**
	 * 
	 * @param topic : topic of the message.
	 * @param id : id of the message.
	 */
	public MessageObject(String topic, int id) {
		this.topic = topic;
		this.id = id;
	}

	/**
	 * 
	 * @param topic : the topic of the message.
	 * @param id : id of the message.
	 * @param value : the message.
	 */
	public MessageObject(String topic, int id, RequestValue value) {
		this.topic = topic;
		this.id = id;
		this.value = value;
	}

	/**
	 * 
	 * @return : the topic of the message.
	 */
	public String getTopic() {
		return topic;
	}
	
	/**
	 * 
	 * @return : id of the message.
	 */
	public int getId() {
		return id;
	}

	/**
	 * 
	 * @return : the message.
	 */
	public RequestValue getValue() {
		return value;
	}
	
}
