package protocols.auctions;

import java.io.Serializable;

/**
 * A common structure of bid
 */
public class Bid implements Serializable {
	
	private static final long serialVersionUID = -5054403761935204553L;
	private String objectId; 
	private int price;
	private String auctioneer;
	private int minIncrement;
	private boolean reverse;
	
	/**
	 * A Bid is defined by 
	 * @param objectId the object it is related to
	 * @param price the proposed price
	 * @param auctioneer the name of the proposer
	 * @param minIncrement an optional parameter 
	 * @param reverse indicate if it's a reverse auction 
	 */
	public Bid ( String objectId ,int price, String auctioneer, int minIncrement, boolean reverse){
		this.objectId=objectId;
		this.price=price;
		this.auctioneer=auctioneer;
		this.minIncrement=minIncrement;
	}
	/**
	 * A Bid is defined by 
	 * @param objectId the object it is related to
	 * @param price the proposed price
	 * @param auctioneer the name of the proposer
	 */
	public Bid (String objectId ,int price, String auctioneer) {
		this.objectId=objectId;
		this.price=price;
		this.auctioneer=auctioneer;
		this.minIncrement=1;
	}
	public void setCurrentPrice(int currentPrice) {
		this.price = currentPrice;
	}
	public String getobjectId() {
		return (this.objectId) ;
	}
	public int getCurrentPrice() {
		return (this.price) ;
	}
	public int getminIncrement() {
		return (this.minIncrement) ;
	}
	public String  getAuctioneer()
	{
		return(this.auctioneer);
	}
	public void setminIncrement(int minIncrement) {
		this.minIncrement = minIncrement;
	}

	public void setauctioneer(String auctioneer) {
		this.auctioneer = auctioneer;
	}

	public String toString() {
		String increment;
		if(this.reverse) increment = "increment"; else increment = "decrement";
		
		return " - Object id: "+this.objectId+"\n - Current auctionner: "+this.auctioneer+
				"\n - Current price : "+this.price+
				"\n - Minimal "+increment+": "+this.minIncrement;
	}

}