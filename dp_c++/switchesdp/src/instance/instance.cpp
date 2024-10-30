#include <cstring>
#include <fstream>
#include <sstream>
#include <vector>
#include "arc.h"
#include "node.h"
#include "../exceptions/restexception.h"
#include "../inputoutput/read.h"
#include "../inputoutput/readinstance.h"

#include "instance.h"

Instance::Instance(const char* filename):file(filename){
	ReadInstance readInst(filename);
	double speed, frate;
	readInst.read(nodes, arcs); // @suppress("Invalid arguments")
}


Instance::~Instance(){}

