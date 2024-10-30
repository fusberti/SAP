/*
 * DpSwitches.h
 *
 *  Created on: 15 de mai. de 2024
 *      Author: jfvg
 */

#ifndef DPSWITCHES_H
#define DPSWITCHES_H

class Network;

class SwitchesDP {

	Network &tree;

	std::unordered_map<unsigned long, double> memoENS;
	std::unordered_map<unsigned long, double> memoENStilde;
	std::vector<Edge> switches;

	void initialize();
	unsigned short calcClientsSum(Vertex &v);
	unsigned short calcNumSucessors(Vertex &v);
	double calcFailureTimeSum(Vertex &v);
	double calcDemandSum(Vertex &v);
	double calcENSMin(Vertex &v);
	void findNextBranchesNodes(Vertex &v);
	void print(Vertex& current);
	unsigned long hash(unsigned i, unsigned j, unsigned k) ;
	double computeENS(Vertex &i, Vertex &j, unsigned k);
	double computeENSTilde(Vertex &i, Vertex &j, unsigned k);
	void recoverSwitches(Vertex &i, Vertex &j, unsigned k);
	void recoverSwitchesTilde(Vertex &i, Vertex &j, unsigned k);
	void clearSwitches(){switches.clear();};

public:
	SwitchesDP(Network &graph);
	virtual ~SwitchesDP();
	void run(unsigned kmax);
	void run(unsigned kmax, unsigned limit_time);


};

#endif /*DPSWITCHES_H*/
