import java.util.ArrayList;

public class Router {
    private String name;
    private Buffer buffer;
    RoutingTable routingTable;

    //All the routers that this router connects to
    OutgoingRouter[] outgoingRouters;

    //Indicates whether the router is currently sending a packet (false) or not (true)
    //boolean ready;

    //TO-DO: fix this or create another constructor so it can be used for star and random topologies
    //Constructor sets up outgoing routers, the routing table, and the buffer using the given parameters
    /*public Router(String name, String[] outgoing, int numCol, int numRow, int bufferSize){
        this.name= name;

        outgoingRouters= new OutgoingRouter[outgoing.length];
        for(int i=0; i < outgoing.length; i++){
            outgoingRouters[i]= new OutgoingRouter(outgoing[i]);
        }

        routingTable= new RoutingTable(numCol, numRow);

        //The column and row of this Router. Used to determine the entries in the routing table
        int dotIndex= name.indexOf('.');
        int routerCol= Integer.parseInt(name.substring(0, dotIndex));
        int routerRow= Integer.parseInt(name.substring(dotIndex + 1));

        //*******NOTE: this is specifically designed for a Manhattan topology. Must change this to Dijkstra's algorithm*******
        //For our purposes, the routing table will determine the route by first finding the correct row, then
        //the correct column. Fill in all entries in the routing table. The subnet architecture is assumed to be Manhattan
        for(int i=0; i < routingTable.getRows(); i++){
            for(int j=0; j < routingTable.getCols(); j++){
                if(j < routerCol){
                    //Send to lower column
                    routingTable.insert(routerCol - 1, routerRow, j, i);
                }else if(j > routerCol){
                    //Send to higher column
                    routingTable.insert(routerCol + 1, routerRow, j, i);
                }else{
                    //Destination router is in the same column as this router. Move to the correct row
                    if(i < routerRow){
                        //Send to lower row
                        routingTable.insert(routerCol, routerRow - 1, j, i);
                    }else if(i > routerRow){
                        //Send to higher row
                        routingTable.insert(routerCol, routerRow + 1, j, i);
                    }
                    //If the row and column are the same, the destiantion router in the table is this same router, so
                    //no router will send a packet to itself. Do nothing, leaving the entry in the routing table null
                }
            }
        }

        buffer= new Buffer(bufferSize);
    }//end constructor*/

    //Constructor
    public Router(String name, String[] outgoing, int bufferSize){
        this.name= name;
        buffer= new Buffer(bufferSize);

        outgoingRouters= new OutgoingRouter[outgoing.length];
        for(int i=0; i < outgoing.length; i++){
            outgoingRouters[i]= new OutgoingRouter(outgoing[i]);
        }
    }

    public void insertRoutingTable(RoutingTable rt){
        this.routingTable= rt;
    }
    
    public String getName(){
        return name;
    }
    
    //Used to receive a packet from another router or create a new packet to be sent. The packet is put in the
    //buffer if there is room (returns null), or is dropped if there is no room (returns the packet).
    public Packet bufferPacket(Packet packet){
        //Attempt to put the packet in the buffer
        if(buffer.insertPacket(packet) == false){
            return packet;
        }
        
        return null;
    }
    
    /*
    //Deprecated, use Buffer.checkIndexToSend(), Buffer.removePacket(index), and Buffer.arrived() instead.
    //Take the packet at the head of the buffer, remove it, and put it in the appropriate OutgoingRouter. Returns null if
    //an OutgoingRouter took on the packet, or the debuffered packet if not (i.e. the packet has arrived at its destination)
    private Packet debufferPacket(){
        Packet packet= buffer.removePacket();
        
        //Get the header of the packet, which is the name of the router it is meant to go to
        String destination= packet.getHeader();
        //Get the name of the next router to send the packet to
        String nextRouterName= routingTable.getRoute(destination);
        
        if(nextRouterName == null){
            //The packet is at its destination
            return packet;
        }
        
        //Put the packet in the OutgoingRouter corresponding to nextRouter
        OutgoingRouter nextRouter= outgoingRouters[0];
        this.routingTable= rt;
        int index= 0;
        while(!nextRouter.getName().equals(nextRouterName)){
            index++;
            nextRouter= outgoingRouters[index];
        }
        nextRouter.sendPacket(packet);

        return null;
    }*/

    //Simulate a time step. The buffer is checked to see if any contained packets have arrived at their destination. If
    //so, an OutgoingRouter with a null name containing the Packet will be added to the return ArrayList (the number of
    //arrived Packets equals the number of these OutgoingRouters added to the ArrayList in this step).
    //
    //Then, all OutgoingRouters in this Router which contain a Packet will send 10 units of data. If it finishes sending data, a
    //new OutgoingRouter containing the sent Packet and with the same name as the Router to send the Packet to is added to
    //the return ArrayList (the number of OutgoingRouters which finish sending a Packet during this time step is equal to
    //the number of OutoingRouters added to the ArrayList in this step).
    //
    //All OutgoingRouters in this Router which do not contain a Packet will check the buffer to see if any Packets contained
    //in it can be sent via this OutgoingRouter, and if so, the Packet will be moved from the buffer into this OutgoingRouter.
    //No return value is generated for this.
    //
    //Once all this is complete, the ArrayList containing all the generated OutgoingRouters is returned. The simulation must
    //handle all of these returned OutgoingRouters. (For OutgoingRouters with a non-null name, the sim must insert the contained
    //Packet into the appropriate Router and print what has happened. For OutgoingRouters with a null name, the sim must print
    //that the contained Packet has arrived at its destination.)
    public ArrayList<OutgoingRouter> timeStep(){
        OutgoingRouter nextRouter= null;
        ArrayList<OutgoingRouter> ret= new ArrayList<OutgoingRouter>();

        //Deprecated
        /*//Used after the loop to determine whether to move a packet from the buffer to an OutgoingRouter
        boolean notSending= true;*/

        //Check if any Packets in the buffer are destined for this Router (i.e. they have arrived at their destination)
        Packet arrived= null;
        do{
            arrived= buffer.arrived(name);
            if(arrived != null){
                //Create a new OutgoingRouter with a null name, containing arrived. Add it to ret
                nextRouter= new OutgoingRouter(null);
                nextRouter.sendPacket(arrived);
                ret.add(nextRouter);
            }
        }while(arrived != null);

        //Check each OutgoingRouter to see if it contains a packet, and send data appropriately from the packet. If the packet
        //is finished being sent, create a temporary OutgoingRouter object, insert the sent packet into it, and add it to ret
        Packet sent= null;
        for(int i=0; i < outgoingRouters.length; i++){
            if(outgoingRouters[i].containsPacket()){
                //sent will be either the packet that has finished being sent, or null if the packet still has data to send
                sent= outgoingRouters[i].timeStep();

                //If a packet is finished being sent, create an OutgoingRouter object with the same name as outgoingRouters[i]
                //and put sent into it. This OutgoingRouter will be returned
                if(sent != null){
                    nextRouter= new OutgoingRouter(outgoingRouters[i].getName());
                    nextRouter.sendPacket(sent);
                    ret.add(nextRouter);
                }

                //notSending= false;    //Deprecated
                
                //Deprecated. 
                /*//Only one OutgoingRouter at a time can contain a packet, so if one is found, break the loop
                break;*/
            }else{
                //Search the buffer for a Packet that can be sent through outgoingRouters[i]
                int index= buffer.checkIndexToSend(routingTable, outgoingRouters[i].getName());

                if(index != -1){
                    //If there is a suitable Packet, retrieve the Packet from the buffer
                    Packet toSend= buffer.removePacket(index);
                    //Put the Packet into this OutgoingRouter
                    outgoingRouters[i].sendPacket(toSend);
                }
            }
        }

        //Deprecated
        /*
        //If the Router is not sending a packet, move a packet from the buffer into the appropriate OutgoingRouter, if the buffer
        //isn't empty
        if(notSending){
            //If the debuffered packet is at its destination, create an OutgoingRouter object with a null name and put the packet
            //into it. This OutgoingRouter will be returned
            if(buffer.getCurrent() != 0){
                Packet arrived= debufferPacket();
                if(arrived != null){
                    //If arrived is non-null, the packet has arrived
                    nextRouter= new OutgoingRouter(null);
                    nextRouter.sendPacket(arrived);
                }
                //arrived is null, so no packet is at its final destination. Do nothing
            }
        }*/

        return ret;
    }

    //Returns a String representing the status of the Router
    public String printStatus(){
        String status= "Router " + name + " connected to: ";
        //List the routers connected to this router
        for(int i=0; i < outgoingRouters.length; i++){
            if(i != 0){
                //Print a separating comma and space if this isn't the first in the array
                status += ", ";
            }
            status += (outgoingRouters[i].getName());
        }
        status += "\n";

        //List information for each OutgoingRouter
        for(int i=0; i < outgoingRouters.length; i++){
            status += (outgoingRouters[i].printStatus() + "\n");
        }

        //Show the buffer status
        status += (buffer.printStatus() + "\n");

        return status;
    }
}