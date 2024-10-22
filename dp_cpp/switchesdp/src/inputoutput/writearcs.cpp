#include <algorithm>
#include <iomanip>
#include <stdlib.h>
#include <iostream>
#include <fstream>
#include <sstream>
#include <cmath>

using namespace std;

#include "../exceptions/restexception.h"
#include "write.h"
#include "writearcs.h"

WriteArcs::WriteArcs(const char *fileName,const bool holdOpened):
							Write(fileName, holdOpened){
}

WriteArcs::~WriteArcs(){
}

void  WriteArcs::save(std::vector<std::vector<unsigned short>> &solArcs){
	//cout << solArcs.size() << endl;
	for (unsigned k = 0; k < solArcs.size(); k++) {
		for (unsigned i = 0; i < solArcs[k].size()-1; i++)
			this->arqPoint << solArcs[k][i] << " ";
		this->arqPoint << solArcs[k][solArcs[k].size()-1] << endl;
	}

	this->closeFile();

}


