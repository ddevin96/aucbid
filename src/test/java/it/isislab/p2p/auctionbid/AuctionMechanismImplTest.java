package it.isislab.p2p.auctionbid;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.text.SimpleDateFormat;

import org.junit.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

public class AuctionMechanismImplTest {
    
    static AuctionMechanismImpl peer0, peer1, peer2;

    public AuctionMechanismImplTest() {

    }    

    /*
    call before all test happen
    */
    @BeforeAll
    public static void setup() throws Exception{
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
        peer2 = new AuctionMechanismImpl(1, "127.0.0.1", new MessageListenerImpl(1));

    }

    @Test
    void testCreateBid(TestInfo testInfo) {
        assertTrue(peer0.createAuction("cane", new Date(), 100.0, "bel cane"));
    }

    @Test
    void testCheckRunningBid(TestInfo testInfo) {
        try {
            Date newDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2022-12-03 12:53:23");
            assertTrue(peer0.createAuction("canenuovo", newDate, 100.0, "bel cane nuovo"));
            //Thread.sleep(7000);
            assertEquals("THIS AUCTION IS STILL RUNNING\n" + "canenuovo", peer1.checkAuction("canenuovo"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void testCheckExpiredBid(TestInfo testInfo) {
        try {
            Date newDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2015-12-03 12:53:23");
            assertTrue(peer0.createAuction("miao", newDate, 100.0, "bel gatto"));
            //Thread.sleep(5000);
            assertEquals("THIS AUCTION IS EXPIRED\n" + "miao" + "\n" + "no one partecipated", peer1.checkAuction("miao"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void testCheckExpiredBidByOwner(TestInfo testInfo) {
        try {
            Date newDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2015-12-03 12:53:23");
            assertTrue(peer0.createAuction("miaoExp", newDate, 100.0, "bel gatto"));
            //Thread.sleep(5000);
            assertEquals("THIS AUCTION IS EXPIRED\n" + "miaoExp" + "\n" + "You were the owner of the bid", peer0.checkAuction("miaoExp"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void testCreateSameBid(TestInfo testInfo) {
        try {
            peer0.createAuction("cane", new Date(), 100.0, "bel cane");
            //Thread.sleep(7000);
            assertFalse(peer1.createAuction("cane", new Date(), 150.0, "altro cane"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void testCreateBidNegativePrice(TestInfo testInfo) {
        try {
            assertFalse(peer0.createAuction("caneNegativo", new Date(), -100.0, "bel cane"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void testPlaceBidOnExpiredAuc(TestInfo testInfo) {
        try {
            Date newDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2015-12-03 12:53:23");
            assertTrue(peer0.createAuction("miaoExpired", newDate, 100.0, "bel gatto"));
            //Thread.sleep(5000);
            assertEquals("THIS AUCTION IS EXPIRED\n" + "miaoExpired", peer1.placeAbid("miaoExpired", 120));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Test
    void testPlaceCorrectBid(TestInfo testInfo) {
        try {
            Date newDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2022-12-03 12:53:23");
            peer0.createAuction("caneNotExpired", newDate, 100.0, "bel cane");
            //Thread.sleep(5000);
            assertEquals("You placed the bet!", peer1.placeAbid("caneNotExpired", 120));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void testPlaceWrongBid(TestInfo testInfo) {
        try {
            Date newDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2022-12-03 12:53:23");
            peer0.createAuction("caneNotExpired2", newDate, 100.0, "bel cane");
            //Thread.sleep(5000);
            assertEquals("Your bid is too low", peer1.placeAbid("caneNotExpired2", 20));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void testBidOnMyAuc(TestInfo testInfo) {
        try {
            Date newDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2022-12-03 12:53:23");
            peer0.createAuction("caneMio", newDate, 100.0, "bello il mio cane");
            //Thread.sleep(5000);
            assertEquals("You're the owner!", peer0.placeAbid("caneMio", 200));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void testWinningAuc(TestInfo testInfo) {
        try {
            Date newDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2022-12-03 12:53:23");
            peer0.createAuction("caneWin", newDate, 100.0, "bello il mio cane");
            peer1.placeAbid("caneWin", 200);
            assertEquals("You're winning this!", peer1.placeAbid("caneWin", 300));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void testListAllBids(TestInfo testInfo) {
        peer0.createAuction("auto", new Date(), 100.0, "bel cane");
        peer1.createAuction("casa", new Date(), 100.0, "bella casa");
        peer0.createAuction("libro", new Date(), 100.0, "bel libro");
        ArrayList<String> arr = new ArrayList<String>();
        arr.add("auto");
        arr.add("casa");
        arr.add("libro");
        assertEquals(arr, peer0.listAuctions());
    }
    
    @Test
    void testPrintAuction(TestInfo testInfo) {
        try {
            Date newDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2022-12-03 12:53:23");
            peer0.createAuction("caneStampa", newDate, 100.0, "bel cane");
            assertEquals("Auction name: " + "caneStampa" + "\nEnd time: " + newDate + "\nReserved price: " 
                 + "100.0" + "\nDescription: " + "bel cane" + "\nOwner: " + "0" 
                 + "\nMax_bid: " + "0.0" + "\n", peer1.printAuction("caneStampa"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void testLeaveNetwork(){
        assertTrue(peer2.leaveNetwork());
    }

}
