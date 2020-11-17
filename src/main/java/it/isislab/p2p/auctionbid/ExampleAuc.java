package it.isislab.p2p.auctionbid;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

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
			
			while(true) {
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
				Date actualDate = new Date();
				terminal.printf("\nActual time: %s\n", actualDate);

				printMenu(terminal);
				
				int option = textIO.newIntInputReader()
						.withMaxVal(3)
						.withMinVal(1)
						.read("Option");
				switch (option) {
				case 1:
					terminal.printf("\nENTER BID NAME\n");
					String name = textIO.newStringInputReader()
					        .withDefaultValue("abcd")
							.read("Bid name: ");
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
						terminal.printf("\nBID %s SUCCESSFULLY CREATED\n",name);
					else
						terminal.printf("\nERROR IN BID CREATION\n");
					break;
				case 2:
					terminal.printf("\nENTER BID NAME TO FIND\n");
					String sname = textIO.newStringInputReader()
					        .withDefaultValue("abc")
							.read("Bid name:");
					String checked = peer.checkAuction(sname);
					if(checked!=null) {
						terminal.printf("\n SUCCESSFULLY FIND %s\n",sname);
						terminal.printf("\n INFORMATIONS:\n%s\n",checked);
					} else
						terminal.printf("\nNO AUCTION FIND WITH THAT NAME\n");
					break;
				case 3:
					terminal.printf("\nENTER BID NAME TO FIND TO PLACE A BID: \n");
					String auc_name = textIO.newStringInputReader()
							.withDefaultValue("abc")
							.read("Bid name:");
					Double bid_price = textIO.newDoubleInputReader()
							.withDefaultValue(10.0)
							.read("Enter your bid: ");
					String bid = peer.placeAbid(auc_name, bid_price);
					terminal.printf("\n%s\n", bid);
					break;
				case 4:
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
		terminal.printf("\n1 - CREATE BID\n");
		terminal.printf("\n2 - CHECK BID \n");
		terminal.printf("\n3 - PLACE A BID \n");
		terminal.printf("\n4 - LEAVE\n");
	}



}
