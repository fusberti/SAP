/***************************************************************************
 readinstance.cpp  -  description
 ***************************************************************************/
#include <vector>
#include <algorithm>
#include <fstream>
#include <cmath>
#include <sstream>
#include <string>
#include <iostream>

using namespace std;

#include "../exceptions/restexception.h"
#include "../instance/node.h"
#include "../instance/arc.h"
#include "../instance/instance.h"
#include "read.h"

#include "readinstance.h"

ReadInstance::ReadInstance(const char *arqName) :
		Read(arqName) {
}

ReadInstance::~ReadInstance() {
}

void ReadInstance::read(NodeList &nodes, ArcList &arcs) {

	unsigned long numNodes, numArcs;
	string line, word;

//	this->readWord();
//	word = this->readWord();
//	if (word != "chaves")
//		throw RestException("\nMethod ReadInstance::read() : this file don't represent the problem !!!");
//
//	numNodes = (unsigned long) this->readLong();
//	if (numNodes > MAX_NUMBER_NODES)
//		throw RestException("\nMethod ReadInstance::read() : the maximum number of nodes was exceeded !!!");
//
//	numArcs = (unsigned long) this->readLong();
//
//
//	this->readWord();//discard the last field

	line = this->readLine();

	stringstream strstream(line);

	strstream >> word;//discard "p"
	strstream >> word;//"chaves"
	if (word != "chaves")
			throw RestException("\nMethod ReadInstance::read() : this file don't represent the problem !!!");
	strstream >> word;//numNodes
	numNodes = (unsigned long) atof(word.c_str());
	if (numNodes > MAX_NUMBER_NODES)
			throw RestException("\nMethod ReadInstance::read() : the maximum number of nodes was exceeded !!!");
	strstream >> word;//numArcs
	numArcs = (unsigned long) atof(word.c_str());

	//No artificial -- raiz do alimentador
	Node root(-1, 0, 0, 0);

	nodes.push_back(root); // @suppress("Method cannot be resolved")

	if (numArcs > MAX_NUMBER_ARCS)
		throw RestException("\nMethod ReadInstance::read() : the maximum number of arcs was exceeded !!!");

	for (unsigned long i = 0; i < numNodes; i++) {
		this->readWord();
		unsigned long id = (unsigned long) this->readLong();

		this->readWord();
		double failuretime = this->readDouble();
		double load = this->readDouble();
		short numclients = (short) this->readLong();
		nodes.push_back(Node(id, numclients, load, failuretime)); // @suppress("Method cannot be resolved")
	}

	unsigned long countArcs=0;

	//Aresta artificial -- da raiz para todas as subestacoes
	for(Node node : nodes)
		if (node.getType() == Node::SUBSTATION) {
			//cout << "node: " << node.getNumber() << endl;
			arcs.push_back(Arc(countArcs++, -1, node.getNumber(), 0.0)); // @suppress("Method cannot be resolved")
		}
	//cout << "arcs size: " << arcs.size() << endl;
	for (unsigned long i = 0; i < numArcs; i++) {
		this->readWord();
		unsigned long source = (unsigned long) this->readLong();
		unsigned long target = (unsigned long) this->readLong();
		this->readWord();
		arcs.push_back(Arc(countArcs++, source, target, 0.0));
	}

}
