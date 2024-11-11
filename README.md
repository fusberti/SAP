**Dependencies:**

C++ dp algorithm: Boost graph library.

Java Gurobi models: The Jung graph library and Gurobi. The jar file contains both the Jung and Gurobi jar library files, for using Gurobi a license is required.

**Compiling C++ dp code:**

The switchesdp folder contains an Eclipse IDE C++ project. To compile from the shell/terminal, please use makefile in switchesdp/Release directory.

**Compiling Gurobi models code:**

The switchAllocation folder contains an Eclipse IDE Java project with SolverGalias and SolverIflows classes.

**Running C++ dp code:**

./switchallocationdp instancefile.switch to install sectionalizing switches in all arcs of the network or
./switchallocationdp instancefile.switch kmax to install kmax sectionalizing switches

**Running Java models:**

java -jar SolverIflows.jar instancefile.switch to install sectionalizing switches in all arcs of the network or
java -jar SolverIflows.jar instancefile.switch kmax to install kmax sectionalizing switches

java -jar SolverGalias.jar instancefile.switch to install sectionalizing switches in all arcs of the network or
java -jar SolverGalias.jar instancefile.switch kmax to install kmax sectionalizing switches

Also, you can run the models directly using SolverIflows and SolverGalias classes.


