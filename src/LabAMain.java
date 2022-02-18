import java.util.ArrayList;

public class LabAMain {
    //Creates a series of Routers connected to each other using the Manhattan architecture.
    //Parameters are used to determine the number of columns and rows and the size of the
    //buffer (which will be the same for all Routers). Returns an arraycontaining all Routers
    private static Router[] createManhattan(int numRow, int numCol, int bufferSize){
        Router[] routers= new Router[numRow*numCol];
        //keeps the place in the routers array as we add new Routers to it
        int routersIndex= 0;

        //Set up a new Router for each position in the Manhattan grid
        for(int i=0; i < numRow; i++){
            for(int j=0; j < numCol; j++){
                //Set up all parameters for this Router

                String name= i + "." + j;

                //Names of OutgoingRouters are determined by this Router's position and the number of
                //columns and rows. Maximum 4 OutgoingRouters for left, right, up, and down connections
                String[] connections;

                if(i == 0){
                    //top row, no up
                    if(j == 0){
                        //leftmost column, no left
                        //Determine the other 2 connections
                        connections= new String[2];
                        connections[0]= "0.1";
                        connections[1]= "1.0";
                    }else if(j == numCol - 1){
                        //rightmost column, no right
                        //Determine the other 2 connections
                        connections= new String[2];
                        connections[0]= "0." + (j-1);
                        connections[1]= "1." + j;
                    }else{
                        //Determine the other 3 connections
                        connections= new String[3];
                        connections[0]= "0." + (j-1);
                        connections[1]= "0." + (j+1);
                        connections[2]= "1." + j;
                    }
                }else if(i == numRow - 1){
                    //bottom row, no down
                    if(j == 0){
                        //leftmost column, no left
                        //Determine the other 2 connections
                        connections= new String[2];
                        connections[0]= i + ".1" ;
                        connections[1]= (i-1) + ".0";
                    }else if(j == numCol - 1){
                        //rightmost column, no right
                        //Determine the other 2 connections
                        connections= new String[2];
                        connections[0]= i + "." + (j-1);
                        connections[1]= (i-1) + "." + j;
                    }else{
                        //Determine the other 3 connections
                        connections= new String[3];
                        connections[0]= i + "." + (j-1);
                        connections[1]= i + "." + (j+1);
                        connections[2]= (i-1) + "." + j;
                    }
                }else{
                    if(j == 0){
                        //leftmost column, no left
                        //Determine other 3 connections
                        connections= new String[3];
                        connections[0]= i + ".1";
                        connections[1]= (i-1) + ".0";
                        connections[2]= (i+1) + ".0";
                    }else if(j == numCol - 1){
                        //rightmost column, no right
                        //Determine the other 3 connections
                        connections= new String[3];
                        connections[0]= i + "." + (j-1);
                        connections[1]= (i-1) + "." + j;
                        connections[2]= (i+1) + "." + j;
                    }else{
                        //Determine the 4 connections
                        connections= new String[4];
                        connections[0]= i + "." + (j-1);
                        connections[1]= i + "." + (j+1);
                        connections[2]= (i-1) + "." + j;
                        connections[3]= (i+1) + "." + j;
                    }
                }//end if else

                //Create a new Router and put it in the routers array
                routers[routersIndex]= new Router(name, connections, bufferSize);
                routersIndex++;
            }
        }//end outer for loop

        //Generate routing tables using Dijkstra's algorithm
        RoutingTable[] rt= generateRoutingTables(routers);

        //Insert all routing tables into the Routers
        for(int i=0; i < rt.length; i++){
            routers[i].insertRoutingTable(rt[i]);
        }

        return routers;
    }

    private static Router[] createStar(int numRouters, int bufferSize){
        //Determine how numRouters (dead-end host Routers) affect the number of central switches (transit Routers)
        int exp= 1;
        while(numRouters > Math.pow(4, exp)){
            exp++;
        }

        int tempExp= exp - 1;
        int numSwitches= 1;
        while(tempExp > 0){
            numSwitches += Math.pow(4, tempExp);
            tempExp--;
        }
        //numSwitches now contains the total number of central switches (in 4 point star pattern)
        Router[] routers= new Router[numSwitches + numRouters];

        //The star pattern has numbers, starting at the top, most central switch at number 1.
        //Individual host (dead-end) Routers are at the bottom layer, and are lettered (A, B, C, or D)
        //Thus, a 2 layer network will have central switch 1 connected to switches 2, 3, 4, and 5.
        //Switch 2 will be connected to 1, as well as its dead-end routers: 2.A, 2.B, 2.C, and 2.D
        //Switch Routers at the start of the routers array, dead-end host Routers at the end
        //(Packets may only be created from the dead-end host Routers)

        //Keeps track of how much to subtract first when calculating the switch connection in the layer above
        int counter= 1;
        //Ensures no connections are made to non-existent host Routers in the for loop
        int routersLeft= numRouters;

        //Determine the connections and create the switches
        for(int i=1; i <= numSwitches; i++){
            //will contain all the connections for this new switch
            String[] connections;
            
            if(i == 1){
                //Special case for 1st switch
                if(numSwitches == 1){
                    //only 1 switch in the network
                    connections= new String[numRouters];
                    connections[0]= "1.A";
                    if(numRouters > 1){
                        connections[1]= "1.B";
                    }
                    if(numRouters > 2){
                        connections[2]= "1.C";
                    }
                    if(numRouters > 3){
                        connections[3]= "1.D";
                    }
                }else{
                    //Create 2nd layer of switches
                    connections= new String[4];
                    connections[0]= "2";
                    connections[1]= "3";
                    connections[2]= "4";
                    connections[3]= "5";
                }
            }else{
                //Check to see whether there is another layer under this one
                //the border between the 2nd last and last layer of switches (the actual result is the last switch number that is in the upper layers)
                int borderIndex= numSwitches - (int)Math.pow(4, exp - 1);       

                if(i <= borderIndex){
                    //there is another layer beneath, so all connections are switches
                    connections= new String[5];

                    //0th connection is the switch in the layer above
                    connections[0]= "" + ((i - counter - 1 ) / 4 + 1);
                    counter= (counter + 1) % 5;
                    if(counter == 0){
                        counter++;
                    }

                    //lower connections
                    connections[1]= "" + (4 * (i - 1) + 2);
                    connections[2]= "" + (4 * (i - 1) + 3);
                    connections[3]= "" + (4 * (i - 1) + 4);
                    connections[4]= "" + (4 * (i - 1) + 5);
                }else{
                    //there are no layers beneath
                    if(routersLeft > 3){
                        //make a full complement of connections if there are enough routersLeft
                        connections= new String[5];
                    }else{
                        //only make as many connections as there are routers left to connect to
                        connections= new String[1 + routersLeft];
                    }

                    //0th connection is the switch in the layer above
                    connections[0]= "" + ((i - counter - 1 ) / 4 + 1);
                    counter= (counter + 1) % 5;
                    if(counter == 0){
                        counter++;
                    }

                    //connections to dead-end routers
                    if(routersLeft > 0){
                        connections[1]= "" + i + ".A";
                        routersLeft--;
                    }
                    if(routersLeft > 0){
                        connections[2]= "" + i + ".B";
                        routersLeft--;
                    }
                    if(routersLeft > 0){
                        connections[3]= "" + i + ".C";
                        routersLeft--;
                    }
                    if(routersLeft > 0){
                        connections[4]= "" + i + ".D";
                        routersLeft--;
                    }
                }
            }//end if..else

            routers[i-1]= new Router(Integer.toString(i), connections, bufferSize);
        }//end for

        counter= 1; //reset counter, which will similarly keep track of the parent switch

        //Create dead-end host Routers connected to the bottom layer of switches. These only have 1 connection
        for(int i = numSwitches; i < (numSwitches + numRouters); i++){
            String[] connections= new String[1];

            //determine this host's switch using i
            String parentSwitch= "" + ((i - counter) / 4 + 1);
            connections[0]= parentSwitch;
            
            switch(counter){
                case 1:
                    routers[i]= new Router(parentSwitch + ".A", connections, bufferSize);
                    break;
                case 2:
                    routers[i]= new Router(parentSwitch + ".B", connections, bufferSize);
                    break;
                case 3:
                    routers[i]= new Router(parentSwitch + ".C", connections, bufferSize);
                    break;
                default:
                    //case 4
                    routers[i]= new Router(parentSwitch + ".D", connections, bufferSize);
            }

            counter= (counter + 1) % 5;
            if(counter == 0){
                counter++;
            }
        }

        //Generate routing tables using Dijkstra's algorithm
        RoutingTable[] rt= generateRoutingTables(routers);

        //Insert all routing tables into the Routers
        for(int i=0; i < rt.length; i++){
            routers[i].insertRoutingTable(rt[i]);
        }

        return routers;
    }

    //Minimum size 6
    private static Router[] createRandom(int numRouters, int bufferSize){
        //An ArrayList of ArrayLists, which themselves will contain integers representing the master list of connections in the random topology
        ArrayList<ArrayList<Integer>> mstrConnections= new ArrayList<ArrayList<Integer>>();

        //Initialize all ArrayLists in mstrConnections
        for(int i=0; i < numRouters; i++){
            mstrConnections.add(new ArrayList<Integer>());
        }

        //Generate 1 or 2 random connections (ints) for each index in mstrConnections, depending on whether i is odd or even
        for(int i=0; i < mstrConnections.size(); i++){
            //Get connections already in the master list for this index, so new connections can be compared and won't be duplicated
            ArrayList<Integer> prevConnections= mstrConnections.get(i);
            
            int rand1;
            boolean duplicate;
            do{
                duplicate= false;
                rand1= (int)(Math.random() * numRouters);

                if(rand1 == i){
                    duplicate= true;
                    continue;
                }
                
                for(int j=0; j < prevConnections.size(); j++){
                    if(rand1 == prevConnections.get(j)){
                        duplicate= true;
                        break;
                    }
                }
            }while(duplicate);

            //Add the connection to the master list for both router[i] and router[rand1]
            mstrConnections.get(i).add(rand1);
            mstrConnections.get(rand1).add(i);

            if(i % 2 == 0){
                prevConnections= mstrConnections.get(i);
                int rand2;

                do{
                    duplicate= false;
                    rand2= (int)(Math.random() * numRouters);

                    if(rand2 == i){
                        duplicate= true;
                        continue;
                    }
                    
                    for(int j=0; j < prevConnections.size(); j++){
                        if(rand2 == prevConnections.get(j)){
                            duplicate= true;
                            break;
                        }
                    }
                }while(duplicate);

                //Add the connection to the master list for both router[i] and router[rand2]
                mstrConnections.get(i).add(rand2);
                mstrConnections.get(rand2).add(i);
            }
        }

        //Create all Routers based on mstrConnections
        Router[] routers= new Router[numRouters];

        for(int i=0; i < numRouters; i++){
            //Prepare parameters
            String name= "" + i;

            String[] connections= new String[mstrConnections.get(i).size()];
            for(int j=0; j < mstrConnections.get(i).size(); j++){
                connections[j]= "" + mstrConnections.get(i).get(j);
            }

            //Call constructor for this Router
            routers[i]= new Router(name, connections, bufferSize);
        }

        //Generate routing tables using Dijkstra's algorithm
        RoutingTable[] rt= generateRoutingTables(routers);

        //Insert all routing tables into the Routers
        for(int i=0; i < rt.length; i++){
            routers[i].insertRoutingTable(rt[i]);
        }

        return routers;
    }

    //Given the array of Routers and their connections (OutgoingRouters), determine the best path from each Router to each
    //other Router, written as a RoutingTable. Thus, an array of RoutingTables (the same size as the array of Routers) will
    //be returned, where each index is the RoutingTable for the Router at that index
    private static RoutingTable[] generateRoutingTables(Router[] routers){
        //An array of routing tables- the return value
        RoutingTable[] routingTables= new RoutingTable[routers.length];
        //A master list of connections in the network- the connections are listed as their respective index as Routers in the routers array, NOT as their String names
        ArrayList<ArrayList<Integer>> mstrConnections= new ArrayList<ArrayList<Integer>>();
        //The set of Routers during implementation of Dijkstra, indicating explored or unexplored
        boolean[] explored= new boolean[routers.length];
        //A counter to easily tell if all nodes have been explored
        int numExplored= 0;
        //Indicates the cost of going to any Router from a given starting Router
        int[] distance= new int[routers.length];
        //Indicates the previous Router visited en route to this Router during Dijkstra (listed as the index from routers array, NOT as the String name)
        int[] prev= new int[routers.length];

        //Prepare all of these arrays for use with Dijkstra's algorithm
        for(int i=0; i < routingTables.length; i++){
            //Instantiate all routing table entries in routingTables
            routingTables[i]= new RoutingTable(routers.length);
            //Instantiate the ArrayList of connections for router i
            mstrConnections.add(new ArrayList<Integer>());
            //Add all connections from router i to the ArrayList
            
            for(int j=0; j < routers[i].outgoingRouters.length; j++){
                //Get the name of the outgoingRouter
                String name= routers[i].outgoingRouters[j].getName();
                //Find what index this name corresponds to in the routers array
                for(int k=0; k < routers.length; k++){
                    if(routers[k].getName().equals(name)){
                        //If the name of the kth Router matches the name from the connection, add k to this ArrayList
                        mstrConnections.get(i).add(k);
                        break;
                    }
                }
            }

            //Set the distance of router i to what amounts to infinity
            distance[i]= 9999999;
            //Set the previous router for router i to -1 (indicating there was no previous router)
            prev[i]= -1;
        }

        //Perform Dijkstra's algorithm for each Router
        for(int i=0; i < routers.length; i++){
            //Mark the origin node distance as 0
            distance[i]= 0;

            //Continue checking new routes until all nodes have been explored
            while(numExplored < routers.length){
                //Find the unexplored node with the smallest distance
                int smallest= -1;    //the index of the unexplored node with the smallest distance. -1 is the equivalent to null here
                for(int j=0; j < distance.length; j++){
                    if(smallest == -1){
                        //haven't found an unexplored node yet
                        if(!explored[j]){
                            //the jth node is unexplored, it will be our smallest node, to be compared with all others
                            smallest= j;
                        }
                    }else{
                        //check to see whether the jth node is unexplored and has a smaller distance than the current smallest. If so, make it the smallest
                        if(!explored[j] && distance[j] < distance[smallest]){
                            smallest= j;
                        }
                    }
                }

                //Check all edges in the node corresponding to smallest to see if each lead to a node that is unexplored.
                //If so, check whether the distance from the starting node to the "smallest" node, plus the extra jump to the
                //unexplored node, is less than the distance to the node already in the table
                for(int j=0; j < mstrConnections.get(smallest).size(); j++){
                    //Get the index of the router this edge leads to
                    int edge= mstrConnections.get(smallest).get(j);

                    //Check if the edge is unexplored
                    if(!explored[edge]){

                        //The edge is unexplored. Check the distances as described above
                        if((distance[smallest] + 1) < distance[edge]){
                            //This edge is the new lowest distance path to this router. Adjust distance and prev accordingly
                            distance[edge]= distance[smallest] + 1;
                            prev[edge]= smallest;
                        }
                    }
                }
                
                //the "smallest" node has now been fully explored
                explored[smallest]= true;
                //increment numExplored
                numExplored++;
            }

            //Dijkstra has now been fully performed for router i
            //Use the values in distance and prev to determine the routing table entries for routingTables[i]
            for(int j=0; j < routers.length; j++){
                //the previous router index on the shortest path to router j
                int previous= prev[j];

                if(previous == -1){
                    //There is no previous router, indicating that this is the same router as the origin,
                    //so the routing table entry should be null
                    routingTables[i].insert(j, routers[j].getName(), null);
                }else{
                    //Follow the path backwards to router i. The entry to the routing table is the router just
                    //before we get to i
                    int node= j;
                    while(previous != i){
                        node= previous;
                        previous= prev[node];
                    }
                    routingTables[i].insert(j, routers[j].getName(), routers[node].getName());
                }
            }

            //Reset the arrays and variables for the next iteration of Dijkstra on the next origin Router
            for(int j=0; j < routers.length; j++){
                distance[j]= 9999999;
                prev[j]= -1;
            }
            explored= new boolean[routers.length];
            numExplored= 0;
        }

        //All routing tables have been completed
        return routingTables;
    }

    //Returns a String containing neatly printed data about the array of routers given as the parameter
    private static String printStatus(Router[] routers, int stepNum, ArrayList<String> extraInfo){
        //Initial header information
        String status= "------------------------------\n";
        status += " ==> Simulation Step " + (stepNum + 1) + " <==\n";

        //Add extraInfo if extraInfo is not null
        if(extraInfo.size() != 0){
            for(int i=0; i < extraInfo.size(); i++){
                status += (extraInfo.get(i) + "\n");
            }
        }

        status += "------------------------------\n";

        //Loop through each router, adding its status accordingly
        for(int i=0; i < routers.length; i++){
            status += routers[i].printStatus();
        }

        return status;
    }

    private static ArrayList<Packet> createPackets(Router source, Router[] routers, int packetSize){
        //Generate a random number corresponding to the amount of bytes to be sent from source. This will generate between 1 and 10 full packets
        //worth
        int numBytes= (int)(Math.random() * 14954 + 46);

        //The ArrayList of new Packets to be returned
        ArrayList<Packet> newPackets= new ArrayList<Packet>();

        //Packet destination is random, but it cannot be the same as the source
        int routersIndex= (int)(Math.random() * routers.length);
        while(routers[routersIndex].getName().equals(source.getName())){
            routersIndex= (int)(Math.random() * routers.length);
        }

        String destination= routers[routersIndex].getName();

        //Use switch to determine how much data to put in a single Packet. Generate as many Packets as required to fit all of numBytes,
        //and put the new Packets in the return array
        switch(packetSize){
            case 1:
                //Use the smallest allowable packet payload while guaranteeing the minimum payload of 46 bytes
                //Only the first payload may be more than 46 bytes

                //Calculate the payload size of the first Packet
                int firstSize= numBytes - (46 * (numBytes / 46 - 1));
                //Create the first Packet
                newPackets.add(new Packet(destination, firstSize));
                //Subtract the size of the packet from numBytes
                numBytes -= firstSize;

                //Create all subsequent packets with a payload size of 46 bytes
                while(numBytes > 0){
                    newPackets.add(new Packet(destination, 46));
                    numBytes -= 46;
                }

                break;
            case 2:
                //All but the last 2 Packets are guaranteed to have a payload of 410 bytes

                //Create new Packets with a payload of 410 bytes and add them to newPackets
                while(numBytes > 501){
                    //Packets must have a minimum of 46 bytes, so only generate a full payload if this will not cause the last Packet to have fewer
                    //than 46 bytes
                    newPackets.add(new Packet(destination, 410));
                    numBytes -= 410;
                }

                if(numBytes == 46){
                    //In the rare case that exactly 46 bytes are left, create the last packet and put it in newPackets
                    newPackets.add(new Packet(destination, 46));
                }else{
                    //Create the last 2 packets, which should both have payloads of more than 46 bytes
                    newPackets.add(new Packet(destination, numBytes - 46));
                    newPackets.add(new Packet(destination, 46));
                }

                break;
            case 3:
                //All but the last 2 Packets are guaranteed to have a payload of 775 bytes

                //Create new Packets with a payload of 775 bytes and add them to newPackets
                while(numBytes > 866){
                    //Packets must have a minimum of 46 bytes, so only generate a full payload if this will not cause the last Packet to have fewer
                    //than 46 bytes
                    newPackets.add(new Packet(destination, 775));
                    numBytes -= 775;
                }

                if(numBytes == 46){
                    //In the rare case that exactly 46 bytes are left, create the last packet and put it in newPackets
                    newPackets.add(new Packet(destination, 46));
                }else{
                    //Create the last 2 packets, which should both have payloads of more than 46 bytes
                    newPackets.add(new Packet(destination, numBytes - 46));
                    newPackets.add(new Packet(destination, 46));
                }
                
                break;
            case 4:
                //All but the last 2 Packets are guaranteed to have a payload of 1140 bytes

                //Create new Packets with a payload of 1140 bytes and add them to newPackets
                while(numBytes > 1231){
                    //Packets must have a minimum of 46 bytes, so only generate a full payload if this will not cause the last Packet to have fewer
                    //than 46 bytes
                    newPackets.add(new Packet(destination, 1140));
                    numBytes -= 1140;
                }

                if(numBytes == 46){
                    //In the rare case that exactly 46 bytes are left, create the last packet and put it in newPackets
                    newPackets.add(new Packet(destination, 46));
                }else{
                    //Create the last 2 packets, which should both have payloads of more than 46 bytes
                    newPackets.add(new Packet(destination, numBytes - 46)); //Faulty logic
                    newPackets.add(new Packet(destination, 46));
                }

                break;
            default:
                //case 5. All but the last 2 Packets are guaranteed to have a full payload of 1500 bytes

                //Create new Packets with a full payload and add them to newPackets
                while(numBytes > 1591){
                    //Packets must have a minimum of 46 bytes, so only generate a full payload if this will not cause the last Packet to have fewer
                    //than 46 bytes
                    newPackets.add(new Packet(destination, 1500));
                    numBytes -= 1500;
                }

                if(numBytes == 46){
                    //In the rare case that exactly 46 bytes are left, create the last packet and put it in newPackets
                    newPackets.add(new Packet(destination, 46));
                }else{
                    //Create the last 2 packets, which should both have payloads of more than 46 bytes
                    newPackets.add(new Packet(destination, numBytes - 46));
                    newPackets.add(new Packet(destination, 46));
                }
        }

        return newPackets;
    }

    //used when isStar= true, indicating that only routers including and after the start host index (stHostInd) may be the final destination for packets
    private static ArrayList<Packet> createPackets(Router source, Router[] routers, int stHostInd, int packetSize){
        //Generate a random number corresponding to the amount of bytes to be sent from source. This will generate between 1 and 10 full packets
        //worth
        int numBytes= (int)(Math.random() * 14954 + 46);

        //The ArrayList of new Packets to be returned
        ArrayList<Packet> newPackets= new ArrayList<Packet>();

        //Packet destination is random, but it cannot be the same as the source or before stHostInd
        int routersIndex= (int)(Math.random() * (routers.length - stHostInd) + stHostInd);
        while(routers[routersIndex].getName().equals(source.getName())){
            routersIndex= (int)(Math.random() * (routers.length - stHostInd) + stHostInd);
        }

        String destination= routers[routersIndex].getName();

        //Use switch to determine how much data to put in a single Packet. Generate as many Packets as required to fit all of numBytes,
        //and put the new Packets in the return array
        switch(packetSize){
            case 1:
                //Use the smallest allowable packet payload while guaranteeing the minimum payload of 46 bytes
                //Only the first payload may be more than 46 bytes

                //Calculate the payload size of the first Packet
                int firstSize= numBytes - (46 * (numBytes / 46 - 1));
                //Create the first Packet
                newPackets.add(new Packet(destination, firstSize));
                //Subtract the size of the packet from numBytes
                numBytes -= firstSize;

                //Create all subsequent packets with a payload size of 46 bytes
                while(numBytes > 0){
                    newPackets.add(new Packet(destination, 46));
                    numBytes -= 46;
                }

                break;
            case 2:
                //All but the last 2 Packets are guaranteed to have a payload of 410 bytes

                //Create new Packets with a payload of 410 bytes and add them to newPackets
                while(numBytes > 501){
                    //Packets must have a minimum of 46 bytes, so only generate a full payload if this will not cause the last Packet to have fewer
                    //than 46 bytes
                    newPackets.add(new Packet(destination, 410));
                    numBytes -= 410;
                }

                if(numBytes == 46){
                    //In the rare case that exactly 46 bytes are left, create the last packet and put it in newPackets
                    newPackets.add(new Packet(destination, 46));
                }else{
                    //Create the last 2 packets, which should both have payloads of more than 46 bytes
                    newPackets.add(new Packet(destination, numBytes - 46));
                    newPackets.add(new Packet(destination, 46));
                }

                break;
            case 3:
                //All but the last 2 Packets are guaranteed to have a payload of 775 bytes

                //Create new Packets with a payload of 775 bytes and add them to newPackets
                while(numBytes > 866){
                    //Packets must have a minimum of 46 bytes, so only generate a full payload if this will not cause the last Packet to have fewer
                    //than 46 bytes
                    newPackets.add(new Packet(destination, 775));
                    numBytes -= 775;
                }

                if(numBytes == 46){
                    //In the rare case that exactly 46 bytes are left, create the last packet and put it in newPackets
                    newPackets.add(new Packet(destination, 46));
                }else{
                    //Create the last 2 packets, which should both have payloads of more than 46 bytes
                    newPackets.add(new Packet(destination, numBytes - 46));
                    newPackets.add(new Packet(destination, 46));
                }
                
                break;
            case 4:
                //All but the last 2 Packets are guaranteed to have a payload of 1140 bytes

                //Create new Packets with a payload of 1140 bytes and add them to newPackets
                while(numBytes > 1231){
                    //Packets must have a minimum of 46 bytes, so only generate a full payload if this will not cause the last Packet to have fewer
                    //than 46 bytes
                    newPackets.add(new Packet(destination, 1140));
                    numBytes -= 1140;
                }

                if(numBytes == 46){
                    //In the rare case that exactly 46 bytes are left, create the last packet and put it in newPackets
                    newPackets.add(new Packet(destination, 46));
                }else{
                    //Create the last 2 packets, which should both have payloads of more than 46 bytes
                    newPackets.add(new Packet(destination, numBytes - 46)); //Faulty logic
                    newPackets.add(new Packet(destination, 46));
                }

                break;
            default:
                //case 5. All but the last 2 Packets are guaranteed to have a full payload of 1500 bytes

                //Create new Packets with a full payload and add them to newPackets
                while(numBytes > 1591){
                    //Packets must have a minimum of 46 bytes, so only generate a full payload if this will not cause the last Packet to have fewer
                    //than 46 bytes
                    newPackets.add(new Packet(destination, 1500));
                    numBytes -= 1500;
                }

                if(numBytes == 46){
                    //In the rare case that exactly 46 bytes are left, create the last packet and put it in newPackets
                    newPackets.add(new Packet(destination, 46));
                }else{
                    //Create the last 2 packets, which should both have payloads of more than 46 bytes
                    newPackets.add(new Packet(destination, numBytes - 46));
                    newPackets.add(new Packet(destination, 46));
                }
        }

        return newPackets;
    }

    //Deprecated, use createPackets() instead
    //Create a new Packet to be sent from source to a random router within routers. The packet destination Router will not be the
    //same as the source.
    private static Packet createPacket(Router source, Router[] routers){
        //Packet payload size is uniformly random, between 46 and 1500
        int size= (int)(Math.random() * 1454 + 46);


        //Deprecated
        /*
        String payload= "";
        //The payload is merely the letter A repeated over and over again, up to the random payload size generated above
        for(int i=0; i < size; i++){
            payload += "A";
        }
        */

        //Packet destination is random, but it cannot be the same as the source
        int routersIndex= (int)(Math.random() * routers.length);
        while(routers[routersIndex].getName().equals(source.getName())){
            routersIndex= (int)(Math.random() * routers.length);
        }

        String destination= routers[routersIndex].getName();

        //Packet newPacket= new Packet(destination, payload);   //Deprecated
        Packet newPacket= new Packet(destination, size);
        
        return newPacket;
    }

    public static void main(String[] args){
        try{
            int numHosts= 64;
            //Change the parameters in this call to change the size of the subnet and the router buffers (int numCol, int numRow, int bufferSize)
            Router[] routers= createManhattan(8, 8, 30720); //30720 bytes = 30 MB, half the buffer size suggested for a 10G switch
            //True if the network is a star network
            boolean isStar= false;

            //If the network is a star network, determine which indexes in routers are valid for creating packets in
            int stHostInd= -1;
            if(isStar){
                //stHostInd is the first index which is VALID to create packets in. The range of valid hosts is [stHostInd, routers.length-1]
                stHostInd= routers.length - numHosts;
            }

            //The number of simulated time steps the simulation will run
            final int TOTAL_STEPS= 100000;

            //The size of packets generated by createPackets(), where 5 is full packet size utilization (biggest packets) and 1 is minimal size
            //(smallest packets)
            int packetSize= 5;

            //Counters for data collection purposes
            int packetsCreated= 0;
            int packetsArrived= 0;
            int packetsDropped= 0;
            int dataQuantityCreated= 0;
            int dataQuantityArrived= 0;
            int dataQuantityDropped= 0;

            //Used to handle extra cases: passing Packets from one Router to another, or indicating that a Packet has reached its destination
            ArrayList<OutgoingRouter> handlePackets= new ArrayList<OutgoingRouter>();

            //Used to update the status. This will change content and length at every time step in the program
            ArrayList<String> statusUpdates= new ArrayList<String>();

            //The frequency at which new Packets will be generated. This number corresponds to the number of Routers per time step which will
            //call createPackets(), which generates several Packets to be sent from that Router to another Router
            //e.g. FREQ= 0.2 means that 1 in every 5 Routers will call createPackets() in a time step
            //Note: FREQ may be low enough so createPackets() is not called in every time step
            final double FREQ= 0.0006;      //have been using 0.0006

            //Used to indicate the number of times createPackets() is to be called in a time step. Also used to indicate which time step
            //to call createPackets() in if FREQ is low enough so createPackets() is not called every time step
            double numCreate= numHosts * FREQ;

            //Print the initial state of the subnet, without any packets in it
            //System.out.println(printStatus(routers, -1, statusUpdates));      //optional console message

            //From here, our program follows a predictable cycle:
            //Step 1--> Clear handlePackets and statusUpdates, and iterate 
            //Step 2--> Perform a time step on all Routers, adding all OutgoingRouter return values to handlePackets.
            //Step 3--> Go through all OutgoingRouters in handlePackets. There are 3 possible outcomes for each OutgoingRouter:
            //          (i) Non-null name field: use Router.bufferPacket() to pass the contained Packet to the Router whose name
            //          matches the name of the OutgoingRouter, then create a String containing the Packet's destination and
            //          which Router it was sent to, and add this String to statusUpdates. As in step 4, if the Packet is returned,
            //          it was dropped, so the statusUpdates message should indicate this.
            //          (ii) Null name field: create a String containing the Packet's destination and indicate that the Packet
            //          arrived there. Add this String to statusUpdates.
            //          (iii) Null OutgoingRouter object: do nothing.
            //Step 4--> Create any number of new Packets, and use Router.bufferPacket() to insert them one by one into a Router.
            //          If a Packet is returned, it was dropped, so add a message into statusUpdates for each dropped Packet
            //Step 5--> Print the status for the entire subnet.
            //
            //Of all of these steps, the only code that should change from one iteration to another is in step 4.

            //Continue performing time steps, handling all necessary information. A random number of random packets which are randomly
            //distributed and have random destinations will be generated, according to expected means established above.
            for(int stepNum=0; stepNum < TOTAL_STEPS; stepNum++){
                //Step 1--> Clear handlePackets and statusUpdates
                handlePackets.clear();
                statusUpdates.clear();

                //Step 2--> Perform a time step on all Routers, adding all OutgoingRouter return values to handlePackets.
                for(int i=0; i < routers.length; i++){
                    //Perform a time step on routers[i], and insert the returned ArrayList into handlePackets
                    handlePackets.addAll(routers[i].timeStep());
                }

                //Step 3--> Go through all OutgoingRouters in handlePackets. There are 2 possible outcomes for each OutgoingRouter:
                //          (i) Non-null name field: use Router.bufferPacket() to pass the contained Packet to the Router whose name
                //          matches the name of the OutgoingRouter, then create a String containing the Packet's destination and
                //          which Router it was sent to, and add this String to statusUpdates. As in step 4, if the Packet is returned,
                //          it was dropped, so the statusUpdates message should indicate this.
                //          (ii) Null name field: create a String containing the Packet's destination and indicate that the Packet
                //          arrived there. Add this String to statusUpdates.
                for(int i=0; i < handlePackets.size(); i++){
                    OutgoingRouter temp= handlePackets.get(i);
                    
                    String name= temp.getName();
                    String destination= temp.getPacket().getHeader();
                    //String payload= temp.getPacket().getPayload();    //Deprecated
                    int payloadSize= temp.getPacket().getSize();
                    String msg;
    
                    if(name == null){
                        //Outcome ii, arrived at destination
                        //msg= "Destination is: " + destination + " --> Packet arrived: (" + destination + ") " + payload;  //Deprecated
                        msg= "Destination is: " + destination + " --> Packet arrived: (" + destination + ") Size: " + payloadSize;
                        packetsArrived++;
                        dataQuantityArrived += payloadSize;
                    }else{
                        //Outcome i, pass Packet to next Router
    
                        //Find the correct Router to pass it to
                        int index= 0;
                        Router next= routers[index];
                        while(!name.equals(next.getName())){
                            index++;
                            next= routers[index];
                        }
                        
                        //next is now the correct Router to pass the Packet to. Do so
                        Packet dropped= next.bufferPacket(temp.getPacket());
                        msg= "Destination is: " + destination + ", send to " + next.getName();
    
                        //Add that the packet was dropped if dropped is non-null
                        if(dropped != null){
                            //msg += " Packet dropped! --> (" + destination + ") " + payload;   //Deprecated
                            msg += " Packet dropped! --> (" + destination + ") Size: " + payloadSize;
                            packetsDropped++;
                            dataQuantityDropped += payloadSize;
                        }
                    }
    
                    //Add msg to statusUpdates
                    statusUpdates.add(msg);
                }

                //Step 4--> Create new Packets randomly using the parameters provided at the start of the program, and use Router.bufferPacket()
                //          to insert them into a Router. If a Packet is returned, it was dropped, so add a message into statusUpdates for each
                //          dropped Packet

                if(numCreate > 1){
                    //createPackets() must be called at least once

                    //Call createPackets() and deal with them the number of times indicated by the floor of numCreate
                    for(int i=0; i < Math.floor(numCreate); i++){
                        //Pick random Router to put the new Packets in  //**********change this for star network so only host routers can get packets
                        int rand;
                        ArrayList<Packet> newPackets;

                        if(isStar){
                            rand= (int)(Math.random() * numHosts + stHostInd);
                            newPackets= createPackets(routers[rand], routers, stHostInd, packetSize);
                        }else{
                            rand= (int)(Math.random() * routers.length);
                            newPackets= createPackets(routers[rand], routers, packetSize);
                        }

                        //Deal with each Packet in newPackets
                        for(int j=0; j < newPackets.size(); j++){
                            //Data tracking
                            packetsCreated++;
                            dataQuantityCreated += newPackets.get(j).getSize();

                            //Attempt to insert the new packet into the router
                            Packet dropped= routers[rand].bufferPacket(newPackets.get(j));

                            if(dropped != null){
                                //The Packet was dropped when trying to put it in the buffer
                                //statusUpdates.add("Newly spawned packet dropped! --> (" + dropped.getHeader() + ") " + dropped.getPayload());   //deprecated
                                statusUpdates.add("Newly spawned packet dropped! --> (" + dropped.getHeader() + ") Size: " + dropped.getSize());
                                packetsDropped++;
                                dataQuantityDropped += dropped.getSize(); 
                            }
                        }
                        //Deprecated
                        /*//*Generate the new packet
                        Packet newPacket= createPacket(routers[rand], routers);
                        packetsCreated++;
                        dataQuantityCreated += newPacket.getSize();*/
                    }

                    //Adjust numCreate so its value is only whatever came after the decimal point in its previous value
                    numCreate = numCreate - Math.floor(numCreate);
                }
                
                //Increment numCreate by the number of Routers times FREQ
                numCreate += (numHosts * FREQ);

                //Step 5--> Print the status for the entire subnet.
                //System.out.println(printStatus(routers, stepNum, statusUpdates));   //optional console message

                //Print a message with the current stepNum if stepNum is a multiple of 100. Keeps track of simulation progress, allowing for simulations which will
                //take too long to be stopped
                if(stepNum % 100 == 0){
                    System.out.println("Step number: " + stepNum);
                }
            }

            //Print stats at end of simulation
            System.out.println();
            System.out.println("Number of Routers: " + routers.length);
            System.out.println("Number of Hosts: " + numHosts);
            System.out.println("Packet size (sliding scale 1-5, where 1 is min, 5 is max size): " + packetSize);
            System.out.println("Frequency: " + FREQ);
            System.out.printf("Packets created: %,d\n", packetsCreated);
            System.out.printf("Packets dropped: %,d\n", packetsDropped);
            System.out.printf("Packets arrived: %,d\n", packetsArrived);
            System.out.printf("Packets stranded: %,d\n", (packetsCreated - packetsDropped - packetsArrived));
            System.out.printf("Data created: %,d bytes\n", dataQuantityCreated);
            System.out.printf("Data dropped: %,d bytes\n", dataQuantityDropped);
            System.out.printf("Data arrived: %,d bytes\n", dataQuantityArrived);
            System.out.printf("Data stranded: %,d bytes\n", (dataQuantityCreated - dataQuantityDropped - dataQuantityArrived));
            System.out.println();
            
        }catch(Exception e){
            e.printStackTrace();
        }
        
    }
}