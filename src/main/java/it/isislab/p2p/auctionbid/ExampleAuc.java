package it.isislab.p2p.auctionbid;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.beryx.textio.TextIO;
import org.beryx.textio.TextIoFactory;
import org.beryx.textio.TextTerminal;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
/**
 * docker build --no-cache -t test  .
 * docker run -i -e MASTERIP="127.0.0.1" -e ID=0 test
 * use -i for interactive mode
 * use -e to set the environment variables
 * @author carminespagnuolo
 *
 */
public class ExampleAuc {

	@Option(name="-m", aliases="--masterip", usage="the master peer ip address", required=true)
	private static String master;

	@Option(name="-id", aliases="--identifierpeer", usage="the unique identifier for this peer", required=true)
	private static int id;

	public static void main(String[] args) throws Exception {

		class MessageListenerImpl implements MessageListener{
			int peerid;
		
			public MessageListenerImpl(int peerid)
			{
				this.peerid=peerid;

			}
			public Object parseMessage(Object obj) {
				
				TextIO textIO = TextIoFactory.getTextIO();
				TextTerminal terminal = textIO.getTextTerminal();
				terminal.printf("\n"+peerid+"] (Direct Message Received) "+obj+"\n\n");
				return "success";
			}

		}
		ExampleAuc example = new ExampleAuc();
		final CmdLineParser parser = new CmdLineParser(example);  
		try  
		{  
			parser.parseArgument(args);  
			TextIO textIO = TextIoFactory.getTextIO();
			TextTerminal terminal = textIO.getTextTerminal();
			AuctionMechanismImpl peer = 
					new AuctionMechanismImpl(id, master, new MessageListenerImpl(id));
			
			terminal.printf("\nStaring peer id: %d on master node: %s\n",
					id, master);
			
			ArrayList<String> followedAuctions = new ArrayList<String>();

			while(true) {
				
				Date actualDate = new Date();
				terminal.printf("\nActual time: %s\n", actualDate);

				printMenu(terminal);
				
				int option = textIO.newIntInputReader()
						.withMaxVal(8)
						.withMinVal(1)
						.read("\nOption");
				switch (option) {
				case 1:
					terminal.printf("\nENTER AUCTION NAME\n");
					String name = textIO.newStringInputReader()
					        .withDefaultValue("abcd")
							.read("Auction name: ");
					String date = textIO.newStringInputReader()
							.withDefaultValue("yyyy-mm-dd hh:mm:ss")
							.read("Enter expiration date: ");
					Date newDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date);
					Double price = textIO.newDoubleInputReader()
							.withDefaultValue(100.0)
							.read("Enter reserved price: ");
					String description = textIO.newStringInputReader()
							.withDefaultValue("abcdef")
							.read("Description of product: ");

					if(peer.createAuction(name, newDate, price, description))
						terminal.printf("\nAUCTION %s SUCCESSFULLY CREATED\n",name);
					else
						terminal.printf("\nERROR IN AUCTION CREATION\n");
					break;
				case 2:
					terminal.printf("\nENTER AUCTION NAME TO FIND\n");
					String sname = textIO.newStringInputReader()
					        .withDefaultValue("abc")
							.read("Auction name:");
					String checked = peer.checkAuction(sname);
					if(checked!=null) {
						terminal.printf("\n SUCCESSFULLY FIND %s\n",sname);
						terminal.printf("\n INFORMATIONS:\n%s\n",checked);
					} else
						terminal.printf("\nNO AUCTION FIND WITH THAT NAME\n");
					break;
				case 3:
					terminal.printf("\nENTER AUCTION NAME TO FIND TO PLACE A BID: \n");
					String auc_name = textIO.newStringInputReader()
							.withDefaultValue("abc")
							.read("Auction name:");
					Double bid_price = textIO.newDoubleInputReader()
							.withDefaultValue(10.0)
							.read("Enter your bid: ");
					String bid = peer.placeAbid(auc_name, bid_price);
					terminal.printf("\n%s\n", bid);
					if (bid == "You placed the bet!")
						if (!followedAuctions.contains(auc_name))
							followedAuctions.add(auc_name);
					break;
				case 4:
					ArrayList<String> arr = peer.listAuctions();
					if (arr != null) {
						if (arr.size() != 0) {
							for (String elem : arr) {
								terminal.println(elem + "\n");
							}
						} else {
							terminal.println("There are no auctions\n");
						}
					} else {
						terminal.println("There are no auctions\n");
					}
					break;
				case 5:
					terminal.printf("\nENTER AUCTION NAME TO PRINT INFORMATIONS: \n");
					String auc_name_inf = textIO.newStringInputReader()
							.withDefaultValue("abc")
							.read("Auction name:");
					String auc_inf = peer.printAuction(auc_name_inf);
					if (auc_inf != null) {
						terminal.printf("\n%s\n", auc_inf);
					} else {
						terminal.printf("This auction doesn't exist\n", auc_inf);
					}
					break;
				case 6:
					terminal.printf("\nCHECK YOUR BIDS: \n");
					if (!followedAuctions.isEmpty()) {
						for (String auc : followedAuctions) {
							String res = peer.checkAuction(auc);
							String running = "THIS AUCTION IS STILL RUNNING\n" + auc;
							if (res != null) {
								if (!(running == res)) {
									terminal.printf("---------\n");
									terminal.printf(res);
									terminal.printf("\n---------\n");
								}
							}
						}
					}
					break;
				case 7:
				terminal.printf("\nENTER AUCTION TO MODIFY: \n");
				String modname = textIO.newStringInputReader()
						.withDefaultValue("abc")
						.read("Auction name:");
				if(peer.checkOwner(modname)) {
					if (peer.checkNoBidder(modname)) {
						//you're the owner of bet
						date = textIO.newStringInputReader()
							.withDefaultValue("yyyy-mm-dd hh:mm:ss")
							.read("Enter new expiration date: ");
						newDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date);
						price = textIO.newDoubleInputReader()
							.withDefaultValue(100.0)
							.read("Enter new reserved price: ");
						description = textIO.newStringInputReader()
							.withDefaultValue("abcdef")
							.read("Description of product: ");

						if(peer.modifyAuction(modname, newDate, price, description))
							terminal.printf("\nAUCTION SUCCESSFULLY MODIFIED\n");
						else
							terminal.printf("\nERROR IN AUCTION MODIFICATION\n");
					} else {
						terminal.printf("There is already a bidder on this auc!\n");
					}
				} else {
					terminal.printf("You're not the owner\n");
				}
				break;
				case 8:
					terminal.printf("\nARE YOU SURE TO LEAVE THE NETWORK?\n");
					boolean exit = textIO.newBooleanInputReader().withDefaultValue(false).read("exit?");
					if(exit) {
						peer.leaveNetwork();
						System.exit(0);
					}
					break;

				default:
					break;
				}
			}



		}  
		catch (CmdLineException clEx)  
		{  
			System.err.println("ERROR: Unable to parse command-line options: " + clEx);  
		}  


	}
	public static void printMenu(TextTerminal terminal) {
		terminal.printf("\n1 - CREATE AUCTION\n");
		terminal.printf("\n2 - CHECK AUCTION \n");
		terminal.printf("\n3 - PLACE A BID \n");
		terminal.printf("\n4 - LIST ALL AUCTIONS \n");
		terminal.printf("\n5 - PRINT ALL INFORMATIONS OF AN AUCTION \n");
		terminal.printf("\n6 - CHECK YOUR BIDS\n");
		terminal.printf("\n7 - MODIFY YOUR AUCTION\n");
		terminal.printf("\n8 - LEAVE\n");
	}



}
