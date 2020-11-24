package it.isislab.p2p.auctionbid;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Date;

import org.junit.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

public class AuctionMechanismImplTest {
    
    protected AuctionMechanismImpl peer0, peer1;

    public AuctionMechanismImplTest() throws Exception{
        class MessageListenerImpl implements MessageListener{
			int peerid;
		
			public MessageListenerImpl(int peerid) {
				this.peerid=peerid;
            }
            
			public Object parseMessage(Object obj) {
				System.out.println("\n"+peerid+"] (Direct Message Received) "+obj+"\n\n");
				return "success";
			}
        }
        
        peer0 = new AuctionMechanismImpl(0, "127.0.0.1", new MessageListenerImpl(0));
        peer1 = new AuctionMechanismImpl(1, "127.0.0.1", new MessageListenerImpl(1));
    }

    @Test
    void testCreateBid(TestInfo testInfo) {
        peer0.createAuction("cane", new Date(), 100.0, "bel cane");
        assertNotNull(peer1.checkAuction("cane"));
    }


}
