# NetworkTopologyTest


This is a project I completed for my Computer Networks class. It creates a simulated
computer network with routers, creates simulated packets within a random router, then
sends those packets to a random destination in the network. The command-line only
program then reports statistics of how many packets reached their destination vs how
many were dropped due to traffic issues. You can create networks of varying sizes and
in a Manhattan (grid), star, or random configuration. At the time, I used these stats
to report on the efficiencies/inefficiencies of the different network shapes.

At the time, I had not yet assimilated software engineering skills regarding code
reusability, and I also had never intended anyone except myself to use the code.

The program is a simple Java program, compiled using javac and run using java LabAMain
on the command line. To change the shape and size of the network in the program, on line
793 in LabAMain.java, change the called function to

createManhattan(numberOfRows, numberOfColumns, sizeOfBufferInRouters)

createStar(numberOfRouters, sizeOfBufferInRouters)

or

createRandom(numberOfRouters, sizeOfBufferInRouters)

If using a star shape, line 795 must also be changed so that isStar is true.

Bear in mind that large network sizes can take prohibitively long to run the program.
The maximum size I used was 64 routers, and I believe it took about 30 min to run.