#ifndef READINSTANCE_H
#define READINSTANCE_H


//class Arc;
//class Node;
//class Instance;
//class Read;

typedef std::vector<Arc> ArcList;
typedef std::vector<Node> NodeList;

// CLASS TO READ THE INSTANCE
class ReadInstance : public Read  {

protected:

	static const unsigned short MAX_NUMBER_NODES=65000;
	static const unsigned short MAX_NUMBER_ARCS=65000;

public:

	ReadInstance(const char *arqName);
	~ReadInstance();

	void read(NodeList &nodes, ArcList &arcs);
};

#endif
