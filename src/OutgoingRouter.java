//Represents a connection from a Router object to another Router
public class OutgoingRouter {
    //Name of the router
    private String name;
    //The packet currently being sent to the router modelled by this OutgoingRouter
    private Packet outPacket;
    //The quantity of data left to send from outPacket
    private int dataLeft;

    //Constructor names the router and initializes dataLeft to 0
    public OutgoingRouter(String name){
        //A null name is allowed, under the special case that this OutgoingRouter is a temporary object used to indicate
        //that its contained packet has arrived at its destination
        this.name= name;
        dataLeft= 0;
    }

    //Get methods for the OutgoingRouter's fields
    public String getName(){
        return name;
    }

    public Packet getPacket(){
        return outPacket;
    }

    public int getDataLeft(){
        return dataLeft;
    }

    //Check if this OutgoingRouter contains a packet
    public boolean containsPacket(){
        if(outPacket == null){
            return false;
        }

        return true;
    }

    //Receive a packet to send, and adjust dataLeft to the size of the packet
    public void sendPacket(Packet send){
        outPacket= send;
        dataLeft= send.getSize();
    }

    //Simulate a time step. If there is no packet, nothing happens and null is returned.
    //If there is a packet, dataLeft is decremented by 10. If this causes dataLeft to be
    //<= 0, the router is emptied and the Packet it contained is returned. If dataLeft
    //is still greater than 0, null is returned.
    public Packet timeStep(){
        if(outPacket != null){
            //dataLeft--;   //deprecated
            dataLeft -= 10;
            if(dataLeft <= 0){
                Packet sent= outPacket;
                outPacket= null;
                return sent;
            }
        }

        return null;
    }

    //Returns a String representing the status of this OutgoingRouter
    public String printStatus(){
        String status;
        if(outPacket == null){
            status= "Outgoing " + name + ": [0]";
        }else{
            //status= "Outgoing " + name + ": (" + outPacket.getHeader() + ") " + outPacket.getPayload() + " [" + dataLeft + "]";   //deprecated
            status= "Outgoing " + name + ": (" + outPacket.getHeader() + ") Size: " + outPacket.getSize() + " [" + dataLeft + "]" ;
        }

        return status;
    }
}
