package protocols.auctions.japaneseAuction;
/** An object shared by behaviors of Japanese Auction **/
public class JapaneseObjectAuction {
	private boolean stop;
	
	public JapaneseObjectAuction  (boolean stop )
	{
		this.stop=stop;
	}

	public void setStop(boolean stop) {
		this.stop = stop;
	}
	public boolean isStop() {
		return stop;
	}

}
