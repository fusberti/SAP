#include <algorithm>
#include <iomanip>
#include <stdlib.h>
#include <iostream>
#include <fstream>
#include <sstream>
#include <cmath>
#include <vector>

using namespace std;

#include "../exceptions/restexception.h"
#include "../instance/node.h"
#include "../instance/arc.h"
#include "../instance/instance.h"
#include "write.h"
#include "writeplotfile.h"

WritePlotFile::WritePlotFile(const char *fileName,const bool holdOpened):
							Write(fileName, holdOpened){
}

WritePlotFile::~WritePlotFile(){
}

void  WritePlotFile::save(Instance &inst,  std::vector<std::vector<unsigned short>> &solArcs){
	for (unsigned k = 0; k < solArcs.size(); k++)
		for (unsigned i = 0; i < solArcs[k].size(); i++)
			if (solArcs[k][i]!=0){
				this->arqPoint << inst.getNodes()[inst.getArcs()[solArcs[k][i]].getSource()].getX() << "\t" << inst.getNodes()[inst.getArcs()[solArcs[k][i]].getSource()].getY() << "\t" << k << "\n";
				this->arqPoint << inst.getNodes()[inst.getArcs()[solArcs[k][i]].getTarget()].getX() << "\t" << inst.getNodes()[inst.getArcs()[solArcs[k][i]].getTarget()].getY() << "\t" << k << "\n\n";
			}
	this->closeFile();

}


