package it.isislab.p2p.auctionbid;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Date;

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
	final private PeerDHT _dht;
	final private int DEFAULT_MASTER_PORT=4000;
	
	ArrayList<String> auctions_names =new ArrayList<String>();
	int owner;

	public AuctionMechanismImpl( int _id, String _master_peer, final MessageListener _listener) throws Exception
	{
		 peer= new PeerBuilder(Number160.createHash(_id)).ports(DEFAULT_MASTER_PORT+_id).start();
		_dht = new PeerBuilderDHT(peer).start();	
		
		FutureBootstrap fb = peer.bootstrap().inetAddress(InetAddress.getByName(_master_peer)).ports(DEFAULT_MASTER_PORT).start();
		fb.awaitUninterruptibly();
		if(fb.isSuccess()) {
			peer.discover().peerAddress(fb.bootstrapTo().iterator().next()).start().awaitUninterruptibly();
			owner = _id;
		}else {
			throw new Exception("Error in master peer bootstrap.");
		}
		
		peer.objectDataReply(new ObjectDataReply() {
			
			public Object reply(PeerAddress sender, Object request) throws Exception {
				return _listener.parseMessage(request);
			}
		});
	}
	
	public boolean createAuction(String _auction_name, Date _end_time, double _reserved_price, String _description) {
		try {
			Auction auction = new Auction(_auction_name, _end_time, _reserved_price, _description, owner);
			FutureGet futureGet = _dht.get(Number160.createHash("auctions")).start();
			futureGet.awaitUninterruptibly();
			if (futureGet.isSuccess()) 
				auctions_names = (ArrayList<String>) futureGet.dataMap().values().iterator().next().object();
			
			auctions_names.add(_auction_name);

			_dht.put(Number160.createHash("auctions")).data(new Data(auctions_names)).start().awaitUninterruptibly();
			_dht.put(Number160.createHash(_auction_name)).data(new Data(auction)).start().awaitUninterruptibly();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	
	public String checkAuction(String _auction_name){
		try {
			FutureGet futureGet = _dht.get(Number160.createHash("auctions")).start();
			futureGet.awaitUninterruptibly();

			if (futureGet.isSuccess()) {
				auctions_names = (ArrayList<String>) futureGet.dataMap().values().iterator().next().object();

				if (auctions_names.contains(_auction_name)) {
					Auction auction = (Auction) futureGet.dataMap().values().iterator().next().object();
					return auction.get_auction_name();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "a";
	}

	public String placeAbid(String _auction_name, double _bid_amount){
		return "b";
	}
	
	public boolean leaveNetwork() {
		
		//for(String topic: new ArrayList<String>(s_topics)) unsubscribeFromTopic(topic);
		_dht.peer().announceShutdown().start().awaitUninterruptibly();
		return true;
	}
}
