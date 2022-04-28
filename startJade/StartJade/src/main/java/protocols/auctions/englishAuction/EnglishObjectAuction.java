package protocols.auctions.englishAuction;

/** An object shared by behaviors of English Auction **/
public class EnglishObjectAuction {
	
	private String objectId; 
	private int price;
	private String auctioneer;
	private int minIncrement;
	private Boolean stop = false;
	private Boolean finished =false;
	/**
	 * A EnglishObjectAuction is defined by 
	 * @param objectId the object it is related to
	 * @param price the proposed price
	 * @param auctioneer the name of the seller
	 * @param minIncrement an optional parameter 
	 */
	public EnglishObjectAuction ( String objectId ,int price, String auctioneer, int minIncrement){
		this.objectId=objectId;
		this.price=price;
		this.auctioneer=auctioneer;
		this.minIncrement=minIncrement;
	}
	public void setCurrentPrice(int currentPrice) {
		this.price = currentPrice;
	}
	public String getObjectId() {
		return (this.objectId) ;
	}
	public int getCurrentPrice() {
		return (this.price) ;
	}
	public int getMinIncrement() {
		return (this.minIncrement) ;
	}
	public String  getAuctioneer()
	{
		return(this.auctioneer);
	}
	public boolean  getFinished()
	{
		return(this.finished);
	}
	public void setMinIncrement(int minIncrement) {
		this.minIncrement = minIncrement;
	}

	public void setAuctioneer(String auctioneer) {
		this.auctioneer = auctioneer;
	}
	public boolean isStop() {
		return stop;
	}

	public void setStop(boolean stop) {
		this.stop = stop;
	}

	public void setFinished(boolean finished) {
		this.finished= finished;
	}
	public void setObjectId(String idobject) {
		this.objectId = idobject;
		
	}

	

}
