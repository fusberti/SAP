//============================================================================
// Name        : dpswitches.cpp
// Author      : 
// Version     :
// Copyright   : Your copyright notice
// Description : Hello World in C++, Ansi-style
//============================================================================

#include <iostream>
#include <cstring>
#include <fstream>
#include <sstream>
#include <boost/graph/adjacency_list.hpp>
#include <vector>
#include <chrono>
#include <limits>
#include <iomanip>

#include "exceptions/restexception.h"
#include "instance/node.h"
#include "instance/arc.h"
#include "inputoutput/read.h"
#include "inputoutput/readinstance.h"
#include "instance/instance.h"
#include "network/network.h"
#include "switchesdp/switchesdp.h"

#define MAX_TIME 3600*2 //seconds

using namespace std;


string getFileName(string fileName){
	// Remove directory if present.
	const size_t last_slash_idx = fileName.find_last_of("\\/");
	if (std::string::npos != last_slash_idx) {
		fileName.erase(0, last_slash_idx + 1);
	}
	return fileName;
}

int main(int argc, char *argv[]) {
	long limit_time = MAX_TIME;
	char *file;
	unsigned kmax = 3;
	if (argc > 1) {
		file = argv[1];
//		kmax = atoi(argv[2]);
//		if (argc > 3)
//			limit_time = atoi(argv[3]);
	}
	else{
		cerr << "Wrong number of arguments! Must be switchallocationdp instance.switch kmax or switchallocationdp instance.switch kmax time_limit" << endl;
		return 1;
	}
	try {
		char solFile[300];
		sprintf(solFile, "../solutions/%s%s.%d", getFileName(file).c_str(),
				".sol", kmax);

		Instance inst(file);			//load the instance
		cout << "Running for file " << file << endl;
		Network tree(inst);				//create the graph data structures
		SwitchesDP dp(tree);
//		dp.run(kmax);					//for only for kmax
		dp.run(inst.getArcs().size(), limit_time);		//run the top-down dp algorithm

	} catch (RestException &ex) {
		cerr << "Problem openning: " << file << "!!!" << ex.cause() << endl;
		return 1;
	}
	return 0;
}
