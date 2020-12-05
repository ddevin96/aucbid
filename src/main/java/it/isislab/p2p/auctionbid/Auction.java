package it.isislab.p2p.auctionbid;

import java.util.Date;
import java.io.Serializable;

/**
	 * Creates a new auction for a good.
	 * @param _auction_name a String, the name identify the auction.
	 * @param _end_time a Date that is the end time of an auction.
	 * @param _reserved_price a double value that is the reserve minimum pricing selling.
	 * @param _description a String describing the selling goods in the auction.
*/
public class Auction implements Serializable{
    
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    String _auction_name;
    Date _end_time;
    Double _reserved_price;
    String _description;
    int _owner;
    Double max_bid;
    int max_bid_id;

    public Auction() {
    }

    public Auction(String auction_name, Date end_time, Double reserved_price, String description, int owner) {
        _auction_name = auction_name;
        _end_time = end_time;
        _reserved_price = reserved_price;
        _description = description;
        _owner = owner;
        this.max_bid = 0.0;
        this.max_bid_id = -1;
    }

    public String get_auction_name() {
        return _auction_name;
    }

    public void set_auction_name(String name) {
        this._auction_name = name;
    }

    public Date get_end_time() {
        return _end_time;
    }
    
    public void set_end_time(Date time) {
        this._end_time = time;
    }

    public Double get_reserved_price() {
        return _reserved_price;
    }

    public void set_reserved_price(Double price) {
        this._reserved_price = price;
    }

    public String get_description() {
        return _description;
    }

    public void set_description(String desc) {
        this._description = desc;
    }

    public int get_owner() {
        return _owner;
    }

    public void set_owner(int own) {
        this._owner = own;
    }

    public Double get_max_bid() {
        return max_bid;
    }

    public void set_max_bid(Double max_bid) {
        this.max_bid = max_bid;
    }

    public int get_max_bid_id() {
        return max_bid_id;
    }

    public void set_max_bid_id(int max_bid_id) {
        this.max_bid_id = max_bid_id;
    }
    
    @Override
    public String toString() {
        return "Auction name: " + _auction_name + "\nEnd time: " + _end_time + "\nReserved price: " 
        + _reserved_price + "\nDescription: " + _description + "\nOwner: " + _owner 
        + "\nMax_bid: " + max_bid + "\n";
    }

    
}
