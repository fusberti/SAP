#ifndef WRITEPLOTFILE_H
#define WRITEPLOTFILE_H

#include <vector>

class WritePlotFile : public Write{

public:

	WritePlotFile(const char *fileName,const bool holdOpened);
	~WritePlotFile();

	//WRITE THE ARCS COORDINATES WITH GROUP AND THE NODES COORDINATES
	void save(Instance &inst, std::vector<std::vector<unsigned short>> &solArcs);

};

#endif
