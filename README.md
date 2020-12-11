# Auction Mechanism using second-price Auctions on P2P Networks
## Daniele De Vinco 0522500801

Each peer can sell and buy goods using second-price Auctions (eBay). Second-price auction is a non-truthful auction mechanism for multiple items. Each bidder places a bid. The highest bidder gets the first slot, the second-highest, the second slot, and so on, but the highest bidder pays the price bid by the second-highest bidder, the second-highest pays the price bid by the third-highest, and so on. The systems allow the users to create new auction (with an ending time, a reserved selling price, and a description), check the status of an auction, and eventually place a new bid for an auction.

## Project Structure

Usign Maven you can add the dependencies to TomP2P in the pom.xml file. 

```xml
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
  - Succesfully creation of a bid
- _testCheckRunningAuction_ 
  - Check if an auction is correctly running
- _testCheckExpiredAuction_ 
  - Check if an auction is correctly expired	
- _testCheckExpiredAuctionByOwner_ 
  - Check if notify that the auction expired is checked by the owner
- _testCreateSameAuction_
  - Try to create auctions with same name by 2 peers
- _testCreateAuctionNegativePrice_
  - Try to create an auction with negative price
- _testPlaceBidOnExpiredAuc_
  - Try to bid on expired auction
- _testPlaceCorrectBid_
  - Try to correctly bid on an auc
- _testPlaceWrongBid_
  - Try to under bid
- _testPlaceWrongBid2_
  - Try to bid a negative number
- _testBidOnMyAuc_
  - Try to bid on your own bid
- _testWinningAuc_
  - Check if you're winning the auction
- _testListAllBids_
  - Check if all bids listed are correct
- _testPrintAuction_
  - Check if the print of informations of an auction is correct
- _testLeaveNetwork_ 
  - Check if leaving the network is succesfull

The build is tested through CI with github actions:

```yml
name: Java CI with Maven

on:
  push:
    branches: [ master ]
  pull_request:
    branches: [ master ]

jobs:
  build:

    runs-on: ubuntu-latest

    steps:
    - uses: actions/checkout@v2
    - name: Set up JDK 1.8
      uses: actions/setup-java@v1
      with:
        java-version: 1.8
    - name: Build with Maven
      run: mvn -B package --file pom.xml
```

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
