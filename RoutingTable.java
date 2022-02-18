public class RoutingTable {
    //The routing table. Each index contains the String name of the router to send the packet to
    //if trying to send the packet to this logical destination
    private String [] table;
    private String [] destNames;
    //private int cols, rows;
    private int numRouters;

    /*
    //Constructor intializes the table according to the number of columns and rows given as parameters
    public RoutingTable(int numCol, int numRow){
        table= new String[numCol][numRow];
        cols= numCol;
        rows= numRow;
    }*/

    //Constructor initializes the table according to the number of Routers give as a parameter
    public RoutingTable(int numRouters){
        table= new String[numRouters];
        destNames= new String[numRouters];
        this.numRouters= numRouters;
    }

    /*
    //Get methods for the number of columns and rows in the table
    public int getCols(){
        return cols;
    }

    public int getRows(){
        return rows;
    }*/

    //Get method
    public int getNumRouters(){
        return numRouters;
    }

    /*
    //Insert an entry into the table. nextCol and nextRow correspond to the value
    //inserted into the array, while finalCol and finalRow correspond to the actual
    //index of the array where we are putting this information
    public void insert(int nextCol, int nextRow, int finalCol, int finalRow){
        String data= Integer.toString(nextCol) + "." + Integer.toString(nextRow);
        table[finalCol][finalRow]= data;
    }*/

    //Insert an entry into the table
    public void insert(int destIndex, String destName, String entry){
        destNames[destIndex]= destName;
        table[destIndex]= entry;
    }

    /*
    //Returns the name of the next router to send the packet to if its ultimate destination is the router
    //at the given destination
    public String getRoute(String destination){
        //Get the column and row of the destination
        int dotIndex= destination.indexOf('.');
        int col= Integer.parseInt(destination.substring(0, dotIndex));
        int row= Integer.parseInt(destination.substring(dotIndex + 1));

        //Get the entry at the table corresponding to col and row, and return it
        return table[col][row];
    }*/

    //Returns the name of the next router to send the packet to if its ultimate destination is the router
    //at the given destination
    public String getRoute(String destination){
        for(int i=0; i < destNames.length; i++){
            if(destination.equals(destNames[i])){
                return table[i];
            }
        }

        return null;    //this code should be unreachable
    }

    public String printTable(){
        String ret= "";

        for(int i=0; i < table.length - 1; i++){
            ret += "Dest: " + destNames[i];
            ret += " Next: " + table[i] + " --- ";
        }
        ret += "Dest: " + destNames[table.length-1];
        ret += " Next: " + table[table.length-1] + "\n";

        return ret;
    }
}