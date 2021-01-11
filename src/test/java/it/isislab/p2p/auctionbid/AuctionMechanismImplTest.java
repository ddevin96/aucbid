package it.isislab.p2p.auctionbid;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.text.SimpleDateFormat;

import org.junit.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

@TestMethodOrder(OrderAnnotation.class)
public class AuctionMechanismImplTest {
    
    static AuctionMechanismImpl peer0, peer1, peer2, peer3;

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
        peer2 = new AuctionMechanismImpl(2, "127.0.0.1", new MessageListenerImpl(2));
        peer3 = new AuctionMechanismImpl(3, "127.0.0.1", new MessageListenerImpl(3));
    }

    @Test
    @Order(1)
    void testListAllAuctionsVoid(TestInfo testInfo) {
        assertEquals(null, peer0.listAuctions());
        ArrayList<String> arr = new ArrayList<String>();
        assertEquals(arr, peer0.listAuctions());
    }

    @Test
    @Order(2)
    void testListAllAuctions(TestInfo testInfo) {
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
    void testCreateAuction(TestInfo testInfo) {
        assertTrue(peer0.createAuction("cane", new Date(), 100.0, "bel cane"));
    }

    @Test
    void testCheckRunningAuction(TestInfo testInfo) {
        try {
            Date newDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2022-12-03 12:53:23");
            assertTrue(peer0.createAuction("canenuovo", newDate, 100.0, "bel cane nuovo"));
            assertEquals("THIS AUCTION IS STILL RUNNING\n" + "canenuovo", peer1.checkAuction("canenuovo"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void testCheckExpiredAuction(TestInfo testInfo) {
        try {
            Date newDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2015-12-03 12:53:23");
            assertTrue(peer0.createAuction("miao", newDate, 100.0, "bel gatto"));
            assertEquals("THIS AUCTION IS EXPIRED\n" + "miao" + "\n" + "no one partecipated", peer1.checkAuction("miao"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void testCheckExpiredAuctionByOwner(TestInfo testInfo) {
        try {
            Date newDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2015-12-03 12:53:23");
            assertTrue(peer0.createAuction("miaoExp", newDate, 100.0, "bel gatto"));
            assertEquals("THIS AUCTION IS EXPIRED\n" + "miaoExp" + "\n" + "You were the owner of the bid", peer0.checkAuction("miaoExp"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void testCreateSameAuction(TestInfo testInfo) {
        try {
            peer0.createAuction("cane", new Date(), 100.0, "bel cane");
            assertFalse(peer1.createAuction("cane", new Date(), 150.0, "altro cane"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void testCreateAuctionNegativePrice(TestInfo testInfo) {
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
            assertEquals("Your bid is too low", peer1.placeAbid("caneNotExpired2", 20));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void testPlaceWrongBid2(TestInfo testInfo) {
        try {
            Date newDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2022-12-03 12:53:23");
            peer0.createAuction("caneNotExpired2", newDate, 100.0, "bel cane");
            assertEquals("Insert a valid number", peer1.placeAbid("caneNotExpired2", -20));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void testBidOnMyAuc(TestInfo testInfo) {
        try {
            Date newDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2022-12-03 12:53:23");
            peer0.createAuction("caneMio", newDate, 100.0, "bello il mio cane");
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
    void testWinnerAuc(TestInfo testInfo) {
        try {
            Date newDate = new Date();
            int sec = newDate.getSeconds();
            int min = newDate.getMinutes();
            if (sec < 50)
                newDate.setSeconds(sec+10);
            else {
                newDate.setMinutes(min+1);
                newDate.setSeconds(5);
            }
            assertTrue(peer0.createAuction("winning", newDate, 100.0, "wiiiiin"));
            assertEquals("You placed the bet!", peer1.placeAbid("winning", 200));

            Thread.sleep(15000);
            assertEquals("THIS AUCTION IS EXPIRED\n" + "winning"
            + "\n" + "You win. You pay: 100.0", peer1.checkAuction("winning"));
        } catch (Exception e) {
            e.printStackTrace();
        }
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
    void testPrintAuctionNotInList(TestInfo testInfo) {
        try {
            assertEquals(null, peer1.printAuction("auctionInvented"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void testRightOwner(TestInfo testInfo) {
        try {
            peer0.createAuction("elsaowner", new Date(), 100.0, "bel cane");
            assertTrue(peer0.checkOwner("elsaowner"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void testWrongOwner(TestInfo testInfo) {
        try {
            peer0.createAuction("elsaowner2", new Date(), 100.0, "bel cane");
            assertFalse(peer1.checkOwner("elsaowner2"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void testIfThereIsBidder(TestInfo testInfo) {
        try {
            Date newDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2022-12-03 12:53:23");
            peer0.createAuction("elsabidder", newDate, 100.0, "bel cane");
            peer1.placeAbid("elsabidder", 200);
            assertFalse(peer0.checkNoBidder("elsabidder"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void testNoBidder(TestInfo testInfo) {
        try {
            Date newDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2022-12-03 12:53:23");
            peer0.createAuction("elsanobid", newDate, 100.0, "bel cane");
            assertTrue(peer0.checkNoBidder("elsanobid"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void testModifyAuctionCorrect(TestInfo testInfo) {
        try {
            Date newDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2022-12-03 12:53:23");
            peer0.createAuction("auctomodify", newDate, 100.0, "bella auc");
            Date modifiedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2023-12-03 12:53:23");

            assertTrue(peer0.modifyAuction("auctomodify", modifiedDate, 200.0, "bella modified auc"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void testModifyAuctionIncorrectDate(TestInfo testInfo) {
        try {
            Date newDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2022-12-03 12:53:23");
            peer0.createAuction("auctomodify2", newDate, 100.0, "bella auc");
            Date modifiedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2015-12-03 12:53:23");

            assertFalse(peer0.modifyAuction("auctomodify2", modifiedDate, 200.0, "bella modified auc"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void testModifyAuctionIncorrectPrice(TestInfo testInfo) {
        try {
            Date newDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2022-12-03 12:53:23");
            peer0.createAuction("auctomodify3", newDate, 100.0, "bella auc");

            assertFalse(peer0.modifyAuction("auctomodify3", newDate, -200.0, "bella modified auc"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void testModifyAuctionIncorrectOwner(TestInfo testInfo) {
        try {
            Date newDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2022-12-03 12:53:23");
            peer0.createAuction("auctomodify4", newDate, 100.0, "bella auc");
            Date modifiedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2023-12-03 12:53:23");
            assertFalse(peer1.modifyAuction("auctomodify4", modifiedDate, 200.0, "bella modified auc"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    @Test
    void testLeaveNetwork(){
        assertTrue(peer2.leaveNetwork());
        assertTrue(peer3.leaveNetwork());
    }

}
