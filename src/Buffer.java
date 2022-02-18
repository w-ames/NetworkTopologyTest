import java.util.*;

public class Buffer {
    //The maximum quantity of data the Buffer can hold
    private int max;
    //The quantity of data the Buffer is currently holding
    private int current;
    //A list of all the Packets
    private LinkedList<Packet> packets;

    //Constructor initializes the parameter to the max size and initializes all other fields
    public Buffer(int maxSize){
        max= maxSize;
        current= 0;
        packets= new LinkedList<Packet>();
    }

    //Get methods for the Buffer fields
    public int getMax(){
        return max;
    }

    public int getCurrent(){
        return current;
    }

    //Returns a String containing neat information the Buffer's contents
    public String printStatus(){
        String status= "  Buffer: (" + current + "/" + max + ") ";

        for(int i=0; i < packets.size(); i++){
            status += ( "[(" + packets.get(i).getHeader() + ") " );
            //status += packets.get(i).getPayload() + "] ";         //deprecated
            status += "Size: " + packets.get(i).getSize() + "] ";
        }

        status += "\n";

        return status;
    }

    //Insert a packet, returning true if successful and false if not.
    //If the packet size would exceed the size of the buffer, returns
    //false. The packet is dropped
    public boolean insertPacket(Packet packet){
        //Check if packet size exceeds the space left in the buffer
        if(packet.getSize() + current > max){
            return false;
        }

        //Put the packet in the Buffer and change current (size) accordingly
        packets.add(packet);
        current += packet.getSize();

        return true;
    }

    //Checks the buffer for a Packet which can be sent through the OutgoingRouter (in the Router that calls
    //this method) which has the name given by the String outgoing. The RoutingTable table is used to check
    //which OutgoingRouter to send the Packet through.
    //Returns the index of the first Packet in this Buffer that fits this description, or -1 if no Packet does.
    public int checkIndexToSend(RoutingTable table, String outgoing){
        for(int i=0; i < packets.size(); i++){
            String destination= packets.get(i).getHeader();
            String nextRouter= table.getRoute(destination);
            if(outgoing.equals(nextRouter)){
                //The name of the next Router to send the Packet to matches the name given by outgoing, so the index
                //of this Packet is returned
                return i;
            }
        }

        //No Packet in this Buffer can be sent through outgoing, so return -1
        return -1;
    }

    //Removes and returns the Packet with the given index from packets
    public Packet removePacket(int index){
        //Remove the packet at the index from packets
        Packet ret= packets.remove(index);
        //Adjust current (size) accordingly
        current -= ret.getSize();

        return ret;
    }

    //Removes the first Packet in the Buffer which has arrived at its destination, given the name of the destination,
    //and returns it. Returns null if no Packet fits this.
    public Packet arrived(String currRouter){
        for(int i=0; i < packets.size(); i++){
            //Check if the packet at index i has a corresponding null entry in the routing table
            String destination= packets.get(i).getHeader();
            if(destination.equals(currRouter)){
                return removePacket(i);
            }
        }

        //No Packet in this Buffer is at its destination
        return null;
    }

    //Removes the head Packet from packets, and returns it
    //Deprecated. Use removePacket(int index) instead
    public Packet removePacket(){
        //Pop the packet of the head of packets
        Packet ret= packets.remove();
        //Adjust current (size) accordingly
        current -= ret.getSize();

        return ret;
    }


}