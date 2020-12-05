# Auction Mechanism using second-price Auctions on P2P Networks

## Project Structure

Usign Maven you can add the dependencies to TomP2P in the pom.xml file. 

```
<repositories>
    <repository>
        <id>tomp2p.net</id>
         <url>http://tomp2p.net/dev/mvn/</url>
     </repository>
</repositories>
<dependencies>
   <dependency>
     <groupId>net.tomp2p</groupId>
     <artifactId>tomp2p-all</artifactId>
      <version>5.0-Beta8</version>
   </dependency>
</dependencies>
```

The package ```src/main/java/it/isislab/p2p/auctionbid/``` provides three Java classes:

- _Auction_ a class to model an Auction object.
- _MessageListener_ a interface for listener of messages received by a peer.
- _AuctionMechanism_ a interface that defines the AuctionBid communication paradigm.	
- _AuctionMechanismImpl_ an implementation of the AuctionBid interface that exploits the TomP2P library.
- _Example_ an example REPL application of a peers network able to manage Auctions and bid through communication.

The package ```src/test/java/it/isislab/p2p/auctionbid/``` provides one Java class:
- _AuctionMechanismImplTest_  a class to test all methods of AuctionMechanismImpl.

Here are all the test passing:

- _testCreateBid_ 
- _testCheckRunningBid_ 
- _testCheckExpiredBid_ 	
- _testCheckExpiredBidByOwner_ 
- _testCreateSameBid_
- _testCreateBidNegativePrice_ 	
- _testPlaceBidOnExpiredAuc_ 
- _testPlaceCorrectBid_ 
- _testPlaceWrongBid_ 	
- _testBidOnMyAuc_ 
- _testWinningAuc_
- _testListAllBids_ 
- _testPrintAuction_	
- _testLeaveNetwork_ 

## Docker start

An example application is provided using Docker container, running on a local machine. See the Dockerfile, for the builing details.

First of all you can build your docker container:

```docker build --no-cache -t p2paucbid .```

#### Start the master peer

After that you can start the master peer, in interactive mode (-i) and with two (-e) environment variables:

```docker run -i --name MASTER-PEER -e MASTERIP="127.0.0.1" -e ID=0 p2p-pp-client```

,the MASTERIP envirnoment variable is the master peer ip address and the ID environment variable is the unique id of your peer. Rember you have to run the master peer using the ID=0.

**Note that**: after the first launch, you can launch the master node using the following command: 
```docker start -i MASTER-PEER```.

#### Start a generic peer

When master is started you have to check the ip address of your container:

- Check the docker <container ID>: ```docker ps```
- Check the IP address: ```docker inspect <container ID>```

Now you can start your peers varying the unique peer id:

```docker run -i --name CLIENT-PEER-1 -e MASTERIP="172.17.0.2" -e ID=1 p2paucbid```

**Note that**: after the first launch, you can launch this peer node using the following command: 
```docker start -i PEER-1```.
