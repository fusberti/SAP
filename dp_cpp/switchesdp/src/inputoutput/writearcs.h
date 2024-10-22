#ifndef WRITEARCS_H
#define WRITEARCS_H

#include <vector>

class WriteArcs : public Write{

public:

	WriteArcs(const char *fileName,const bool holdOpened);
	~WriteArcs();

	//WRITE THE ARCS ID PER GROUP INTO THE FILE fileName
	void save(std::vector<std::vector<unsigned short>> &solArcs);

};

#endif
