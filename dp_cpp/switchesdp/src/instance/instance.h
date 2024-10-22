#ifndef INSTANCE_H
#define INSTANCE_H

class Arc;
class Node;

typedef std::vector<Arc> ArcList;
typedef std::vector<Node> NodeList;

class Instance {

private:

	// NODES
	NodeList nodes;

	// ARCS
	ArcList arcs;

	//FILE NAME
	const char* file;


public:

	static const unsigned short NULLS=65535;

	// CONSTANT FOR CONSIDER COMPARISON BETWEEN REAL NUMBERS
	static constexpr double ZERO=1E-010;


	Instance(const char* filename);
	~Instance();

	void setNodes(NodeList list);
	void setArcs(ArcList list);

	inline NodeList& getNodes(){return nodes;}
	inline ArcList& getArcs(){return arcs;}


	const char* getFileName(){return file;}
};


#endif
