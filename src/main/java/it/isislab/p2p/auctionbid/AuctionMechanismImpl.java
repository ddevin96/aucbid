package it.isislab.p2p.auctionbid;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.TimeZone;
import java.util.Date;
import java.text.SimpleDateFormat;

import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.FutureBootstrap;
import net.tomp2p.futures.FutureDirect;
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
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						sdf.setTimeZone(TimeZone.getTimeZone("GMT+1:00"));
						String actualDate = sdf.format(new Date());
						Date now = sdf.parse(actualDate);

						//Date now = new Date();
						if (now.after(auction.get_end_time())) {
							return "THIS AUCTION IS EXPIRED\n" + auction.toString();
						} else {
							return "THIS AUCTION IS STILL RUNNING\n" + auction.toString();
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

		//check if the auction exist
		try {
			if (checkAuction(_auction_name) != null) {
				FutureGet futureGet = dht.get(Number160.createHash(_auction_name)).start();
				futureGet.awaitUninterruptibly();

				if (futureGet.isSuccess()) {
					Auction auction = (Auction) futureGet.dataMap().values().iterator().next().object();
					
					Date now = new Date();

					//compare actual time with end time of the bid
					if (now.after(auction.get_end_time())) {
						return "THIS AUCTION IS EXPIRED\n" + auction.toString();
					} else {
						//if not expired check if my bid is bigger then the max until now
						if (_bid_amount > auction.get_max_bid()) {
							auction.set_max_bid(_bid_amount);
	
							dht.put(Number160.createHash(_auction_name)).data(new Data(auction)).start().awaitUninterruptibly();
							return "You placed the bet!";
						} else {
							return "Your bid it too low";
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
	
	public boolean leaveNetwork() {
		
		//for(String topic: new ArrayList<String>(s_topics)) unsubscribeFromTopic(topic);
		dht.peer().announceShutdown().start().awaitUninterruptibly();
		return true;
	}
}
