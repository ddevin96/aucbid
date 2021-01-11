package it.isislab.p2p.auctionbid;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;

import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.p2p.Peer;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.rpc.ObjectDataReply;
import net.tomp2p.storage.Data;

public class AuctionMechanismImpl implements AuctionMechanism{
	final private Peer peer;
	final private PeerDHT dht;
	final private int DEFAULT_MASTER_PORT=4000;
	
	//List of all auctions available
	ArrayList<String> auctions_names = new ArrayList<String>();
	//unique owner of a bid
	int owner;

	public AuctionMechanismImpl( int _id, String _master_peer, final MessageListener _listener) throws Exception
	{
		peer= new PeerBuilder(Number160.createHash(_id)).ports(DEFAULT_MASTER_PORT+_id).start();
		dht = new PeerBuilderDHT(peer).start();	
		
		FutureBootstrap fb = peer.bootstrap().inetAddress(InetAddress.getByName(_master_peer)).ports(DEFAULT_MASTER_PORT).start();
		fb.awaitUninterruptibly();
		if(fb.isSuccess()) {
			peer.discover().peerAddress(fb.bootstrapTo().iterator().next()).start().awaitUninterruptibly();
		}else {
			throw new Exception("Error in master peer bootstrap.");
		}
		owner = _id;

		peer.objectDataReply(new ObjectDataReply() {
			
			public Object reply(PeerAddress sender, Object request) throws Exception {
				return _listener.parseMessage(request);
			}
		});
	}
	
	public boolean createAuction(String _auction_name, Date _end_time, double _reserved_price, String _description) {
		try {

			//check if exist another auction with the same name
			if (checkAuction(_auction_name) == null) {
				
				if (_reserved_price < 0) {
					//negative price not allowed
					return false;
				} 
				Auction auction = new Auction(_auction_name, _end_time, _reserved_price, _description, owner);
				FutureGet futureGet = dht.get(Number160.createHash("auctions")).start();
				futureGet.awaitUninterruptibly();

				//get list of all auction names from dht
				if (futureGet.isSuccess()) 
					auctions_names = (ArrayList<String>) futureGet.dataMap().values().iterator().next().object();
				
				//add my auction to the list
				auctions_names.add(_auction_name);

				//update the dht with the new list and the new auction
				dht.put(Number160.createHash("auctions")).data(new Data(auctions_names)).start().awaitUninterruptibly();
				dht.put(Number160.createHash(_auction_name)).data(new Data(auction)).start().awaitUninterruptibly();
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	
	public String checkAuction(String _auction_name){
		try {
			FutureGet futureGet = dht.get(Number160.createHash("auctions")).start();
			futureGet.awaitUninterruptibly();

			if (futureGet.isSuccess()) {
				//if is empty peer has to create the list first 
				if(futureGet.isEmpty()) {
					dht.put(Number160.createHash("auctions")).data(new Data(auctions_names)).start().awaitUninterruptibly();
					return null;
				}
				auctions_names = (ArrayList<String>) futureGet.dataMap().values().iterator().next().object();

				// control if the list has the auction name in it
				if (auctions_names.contains(_auction_name)) {
					FutureGet futureGet2 = dht.get(Number160.createHash(_auction_name)).start();
					futureGet2.awaitUninterruptibly();

					if (futureGet2.isSuccess()) {
						Auction auction = (Auction) futureGet2.dataMap().values().iterator().next().object();
						
						//compare actual time with end time of the bid
						Date now = new Date();

						if (now.after(auction.get_end_time())) {
							String res = auction.getResult(owner);
							return "THIS AUCTION IS EXPIRED\n" + auction.get_auction_name()
								+ "\n" + res;
						} else {
							return "THIS AUCTION IS STILL RUNNING\n" + auction.get_auction_name();
						}
					} else {
						return null;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public String placeAbid(String _auction_name, double _bid_amount){

		//negative bid not allowed
		if (_bid_amount <= 0) {
			return "Insert a valid number";
		}

		//check if the auction exist
		try {
			if (checkAuction(_auction_name) != null) {
				FutureGet futureGet = dht.get(Number160.createHash(_auction_name)).start();
				futureGet.awaitUninterruptibly();

				if (futureGet.isSuccess()) {
					Auction auction = (Auction) futureGet.dataMap().values().iterator().next().object();

					//can't bid on your own auc
					if (owner == auction.get_owner()) {
						return "You're the owner!";
					} else if (owner == auction.get_max_bid_id()) {
						// your bid is still the best
						return "You're winning this!";
					}
					
					Date now = new Date();

					//compare actual time with end time of the bid
					if (now.after(auction.get_end_time())) {
						return "THIS AUCTION IS EXPIRED\n" + auction.get_auction_name();
					} else {
						//if not expired check if my bid is bigger then the max until now
						if (_bid_amount > auction.get_max_bid() && _bid_amount > auction.get_reserved_price()) {
							auction.set_max_bid(_bid_amount);
							auction.set_max_bid_id(owner);
							if (!auction.addBidder(owner))
								return "error bidder";
							if (!auction.addBid(_bid_amount))
								return "error bid";
							dht.put(Number160.createHash(_auction_name)).data(new Data(auction)).start().awaitUninterruptibly();
							return "You placed the bet!";
						} else {
							return "Your bid is too low";
						}
					}
				} else {
					return "future get error";
				}
			} else {
				//auction not find
				return "not found an auction with that name";
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "error";
	}

	/**
	 * Display all auctions in the system.
	 * @return list of auctions
	 */
	public ArrayList<String> listAuctions(){
		try {
			FutureGet futureGet = dht.get(Number160.createHash("auctions")).start();
			futureGet.awaitUninterruptibly();

			if (futureGet.isSuccess()) {
				//if is empty peer has to create the list first 
				if(futureGet.isEmpty()) {
					dht.put(Number160.createHash("auctions")).data(new Data(auctions_names)).start().awaitUninterruptibly();
					return null;
				}
				auctions_names = (ArrayList<String>) futureGet.dataMap().values().iterator().next().object();

				return auctions_names;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Display informations about an auction.
	 * @param _auction_name a String, the name identify the auction.
	 * @return string of informations if auctions exist, null otherwise.
	 */
	public String printAuction(String _auction_name){
		try {
			FutureGet futureGet = dht.get(Number160.createHash("auctions")).start();
			futureGet.awaitUninterruptibly();

			if (futureGet.isSuccess()) {
				//if is empty peer has to create the list first 
				if(futureGet.isEmpty()) {
					dht.put(Number160.createHash("auctions")).data(new Data(auctions_names)).start().awaitUninterruptibly();
					return null;
				}
				auctions_names = (ArrayList<String>) futureGet.dataMap().values().iterator().next().object();

				// control if the list has the auction name in it
				if (auctions_names.contains(_auction_name)) {
					FutureGet futureGet2 = dht.get(Number160.createHash(_auction_name)).start();
					futureGet2.awaitUninterruptibly();

					if (futureGet2.isSuccess()) {
						Auction auction = (Auction) futureGet2.dataMap().values().iterator().next().object();
						
						return auction.toString();
					} else {
						return null;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Check if you're the owner of the auction.
	 * @param _auction_name a String, the name identify the auction.
	 * @return true if you're the owner, false otherwise.
	 */
	public boolean checkOwner(String _auction_name){
		try {
			FutureGet futureGet = dht.get(Number160.createHash("auctions")).start();
			futureGet.awaitUninterruptibly();

			if (futureGet.isSuccess()) {
				//if is empty peer has to create the list first 
				if(futureGet.isEmpty()) {
					dht.put(Number160.createHash("auctions")).data(new Data(auctions_names)).start().awaitUninterruptibly();
					return false;
				}
				auctions_names = (ArrayList<String>) futureGet.dataMap().values().iterator().next().object();

				// control if the list has the auction name in it
				if (auctions_names.contains(_auction_name)) {
					FutureGet futureGet2 = dht.get(Number160.createHash(_auction_name)).start();
					futureGet2.awaitUninterruptibly();

					if (futureGet2.isSuccess()) {
						Auction auction = (Auction) futureGet2.dataMap().values().iterator().next().object();
						
						if (owner == auction.get_owner())
							return true;
						else
							return false;
					} else {
						return false;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Check if there is at least one bidder on an auction.
	 * @param _auction_name a String, the name identify the auction.
	 * @return true if there is a bidder, false otherwise.
	 */
	public boolean checkNoBidder(String _auction_name){
		try {
			FutureGet futureGet = dht.get(Number160.createHash("auctions")).start();
			futureGet.awaitUninterruptibly();

			if (futureGet.isSuccess()) {
				//if is empty peer has to create the list first 
				if(futureGet.isEmpty()) {
					dht.put(Number160.createHash("auctions")).data(new Data(auctions_names)).start().awaitUninterruptibly();
					return false;
				}
				auctions_names = (ArrayList<String>) futureGet.dataMap().values().iterator().next().object();

				// control if the list has the auction name in it
				if (auctions_names.contains(_auction_name)) {
					FutureGet futureGet2 = dht.get(Number160.createHash(_auction_name)).start();
					futureGet2.awaitUninterruptibly();

					if (futureGet2.isSuccess()) {
						Auction auction = (Auction) futureGet2.dataMap().values().iterator().next().object();
						
						if (auction.get_max_bid_id() == -1)
							return true;
						else
							return false;
					} else {
						return false;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Modify an auction if you're the owner.
	 * @param _auction_name a String, the name identify the auction.
	 * @param _end_time a Date that is the end time of an auction.
	 * @param _reserved_price a double value that is the reserve minimum pricing selling.
	 * @param _description a String describing the selling goods in the auction.
	 * @return true if the auction is correctly modified, false otherwise.
	 */
	public boolean modifyAuction(String _auction_name, Date _end_time, double _reserved_price, String _description){

		Date now = new Date();

		//compare actual time with end time of the bid
		if (now.after(_end_time) || _reserved_price < 0)
			return false;

		//check if the auction exist
		try {
			if (checkAuction(_auction_name) != null) {
				FutureGet futureGet = dht.get(Number160.createHash(_auction_name)).start();
				futureGet.awaitUninterruptibly();

				if (futureGet.isSuccess()) {
					Auction auction = (Auction) futureGet.dataMap().values().iterator().next().object();

					//if i'm the owner i can modify
					if (owner == auction.get_owner()) {
						auction.set_end_time(_end_time);
						auction.set_reserved_price(_reserved_price);
						auction.set_description(_description);
						dht.put(Number160.createHash(_auction_name)).data(new Data(auction)).start().awaitUninterruptibly();
						return true;
					} else {
						return false;
					}
				} else {
					return false;
				}
			} else {
				//auction not find
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean leaveNetwork() {
		dht.peer().announceShutdown().start().awaitUninterruptibly();
		return true;
	}
}
