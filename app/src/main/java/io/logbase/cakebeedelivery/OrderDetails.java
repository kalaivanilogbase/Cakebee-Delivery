package io.logbase.cakebeedelivery;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * Created by logbase on 20/11/15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderDetails implements Comparable {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public String Id;
    @JsonIgnoreProperties(ignoreUnknown = true)
    public String Address;
    @JsonIgnoreProperties(ignoreUnknown = true)
    public String Name;
    @JsonIgnoreProperties(ignoreUnknown = true)
    public Double TimeSort;
    @JsonIgnoreProperties(ignoreUnknown = true)
    public String Time;
    @JsonIgnoreProperties(ignoreUnknown = true)
    public Integer Amount;
    @JsonIgnoreProperties(ignoreUnknown = true)
    public String Mobile;
    @JsonIgnoreProperties(ignoreUnknown = true)
    public String Acceptedon;
    @JsonIgnoreProperties(ignoreUnknown = true)
    public String Pickedon;
    @JsonIgnoreProperties(ignoreUnknown = true)
    public String Pickedat;
    @JsonIgnoreProperties(ignoreUnknown = true)
    public String Startedon;
    @JsonIgnoreProperties(ignoreUnknown = true)
    public String Deliveredon;
    @JsonIgnoreProperties(ignoreUnknown = true)
    public String Deliveredat;
    @JsonIgnoreProperties(ignoreUnknown = true)
    public String Cancelledon;
    @JsonIgnoreProperties(ignoreUnknown = true)
    public String Status;
    @JsonIgnoreProperties(ignoreUnknown = true)
    public double lat;
    @JsonIgnoreProperties(ignoreUnknown = true)
    public double lng;
    @JsonIgnoreProperties(ignoreUnknown = true)
    public String Notes;
    @JsonIgnoreProperties(ignoreUnknown = true)
    public List<ItemDetails> Items;

    @Override
    public int compareTo(Object another) {
        return this.TimeSort > ((OrderDetails)another).TimeSort ? 1 : -1;
    }

}


