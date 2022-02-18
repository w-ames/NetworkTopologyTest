import java.util.regex.Pattern;

public class Packet {
    //Only contains the name of the router this packet is being sent to, for the purposes of
    //this simulation
    private String header;

    //The information contained in this packet
    //private String payload;   //Deprecated

    //Size of the packet, which is the size of the payload + 1 (assuming the header is of size 1)
    private int size;

    //Deprecated. Uses too much memory
    /*
    //Constructor, intakes parameters and calculates size.
    public Packet(String header, String payload){
        this.header= header;
        this.payload= payload;
        //Add the size of everything an ethernet frame holds (aside from the payload): 26 octets
        size= payload.length() + 26;
    }
    */

    //Constructor intakes parameters
    public Packet(String header, int payloadSize){
        this.header= header;
        size= payloadSize + 26;
    }

    //Series of get methods to return the fields of the packet
    public String getHeader(){
        return header;
    }

    //Deprecated
    /*
    public String getPayload(){
        return payload;
    }
    */

    public int getSize(){
        return size;
    }
}