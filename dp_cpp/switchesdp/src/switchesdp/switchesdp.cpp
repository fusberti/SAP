/*
 * SwitchesDP.cpp
 *
 *  Created on: 15 de mai. de 2024
 *      Author: jfvg
 */

#include <iostream>
#include <cstring>
#include <fstream>
#include <sstream>
#include <boost/graph/adjacency_list.hpp>
#include <unordered_map>
#include <vector>
#include <limits>
#include <chrono>
#include <iomanip>

#include "../exceptions/restexception.h"
#include "../instance/node.h"
#include "../instance/arc.h"
#include "../inputoutput/read.h"
#include "../inputoutput/readinstance.h"
#include "../instance/instance.h"
#include "../network/network.h"
#include "switchesdp.h"

using namespace std;

SwitchesDP::SwitchesDP(Network &graph) :
		tree(graph) {
	initialize();
}

void SwitchesDP::initialize() {
	calcNumSucessors(tree.getRoot());
	calcClientsSum(tree.getRoot());
	calcDemandSum(tree.getRoot());
	calcFailureTimeSum(tree.getRoot());
	calcENSMin(tree.getRoot());
	findNextBranchesNodes(tree.getRoot());
}

SwitchesDP::~SwitchesDP() {
}

unsigned short SwitchesDP::calcClientsSum(Vertex &current) {

	tree.getNode(current).setClientsSum(tree.getNode(current).getNumClients());
	std::vector<Vertex>::const_iterator itVertices = tree.getOutVertices(
			current).begin();

	while (itVertices != tree.getOutVertices(current).end()) {
		Vertex next = *itVertices;
		tree.getNode(current).setClientsSum(
				tree.getNode(current).getClientsSum() + calcClientsSum(next));
		itVertices++;
	}
	return tree.getNode(current).getClientsSum();
}

double SwitchesDP::calcDemandSum(Vertex &current) {
	tree.getNode(current).setActiveLoadSum(
			tree.getNode(current).getActiveLoad());
	std::vector<Vertex>::const_iterator itVertices = tree.getOutVertices(
			current).begin();

	while (itVertices != tree.getOutVertices(current).end()) {
		Vertex next = *itVertices;
		tree.getNode(current).setActiveLoadSum(
				tree.getNode(current).getActiveLoadSum() + calcDemandSum(next));
		itVertices++;
	}
	return tree.getNode(current).getActiveLoadSum();
}

double SwitchesDP::calcFailureTimeSum(Vertex &current) {
	tree.getNode(current).setFailureTimeSum(
			tree.getNode(current).getFailureTime());
	std::vector<Vertex>::const_iterator itVertices = tree.getOutVertices(
			current).begin();

	while (itVertices != tree.getOutVertices(current).end()) {
		Vertex next = *itVertices;
		tree.getNode(current).setFailureTimeSum(
				tree.getNode(current).getFailureTimeSum()
						+ calcFailureTimeSum(next));
		itVertices++;
	}
	return tree.getNode(current).getFailureTimeSum();
}

unsigned short SwitchesDP::calcNumSucessors(Vertex &current) {
	std::vector<Vertex>::const_iterator itVertices = tree.getOutVertices(
			current).begin();
	while (itVertices != tree.getOutVertices(current).end()) {
		Vertex next = *itVertices;
		tree.getNode(current).setNumSuc(
				1 + tree.getNode(current).getNumSuc() + calcNumSucessors(next));
		itVertices++;
	}
	return tree.getNode(current).getNumSuc();
}

double SwitchesDP::calcENSMin(Vertex &current) {
	tree.getNode(current).setMinENS(
			tree.getNode(current).getFailureTime()
					* tree.getNode(current).getActiveLoadSum());
	std::vector<Vertex>::const_iterator itVertices = tree.getOutVertices(
			current).begin();
	while (itVertices != tree.getOutVertices(current).end()) {
		Vertex next = *itVertices;
		tree.getNode(current).setMinENS(
				tree.getNode(current).getMinENS() + calcENSMin(next));
		itVertices++;
	}
	return tree.getNode(current).getMinENS();
}


void SwitchesDP::findNextBranchesNodes(Vertex &current) {
	tree.getNode(current).setNumNodesNextBranches(1);
	tree.getNode(current).setMinENSBranches(
			tree.getNode(current).getFailureTime()
					* tree.getNode(current).getActiveLoadSum());
	if (tree.getDegree(current) != 0) {
		Vertex &next = tree.getFirstSuc(current);
		findNextBranchesNodes(next);
		tree.getNode(current).setNumNodesNextBranches(
				tree.getNode(current).getNumNodesNextBranches()
						+ tree.getNode(next).getNumNodesNextBranches());
		tree.getNode(current).setMinENSBranches(
				tree.getNode(current).getMinENSBranches()
						+ tree.getNode(next).getMinENSBranches());
	}

	if (tree.getNextSibling(current) != TreeGraph::null_vertex()) {
		Vertex &next = tree.getNextSibling(current);
		findNextBranchesNodes(next);
		tree.getNode(current).setNumNodesNextBranches(
				tree.getNode(current).getNumNodesNextBranches()
						+ tree.getNode(next).getNumNodesNextBranches());
		tree.getNode(current).setMinENSBranches(
				tree.getNode(current).getMinENSBranches()
						+ tree.getNode(next).getMinENSBranches());
	}

}

unsigned long SwitchesDP::hash(unsigned i, unsigned j, unsigned k) {
	return (unsigned long) i * 100000000 + j * 10000 + k;
}

double SwitchesDP::computeENSTilde(Vertex &i, Vertex &j, unsigned k) {
	double sol;
	// first check memoization table
	unsigned long key = hash(tree.getNodeIndex(i), tree.getNodeIndex(j), k);
	std::unordered_map<unsigned long, double>::const_iterator mapIt =
			memoENStilde.find(key);
	if (mapIt == memoENStilde.end()) {
		// no more branches
		if (tree.getNextSibling(j) == TreeGraph::null_vertex()) {
			sol = computeENS(i, j, k);
		} else {
			// Complexity O(k) for the linear search of the best projection
			sol = std::numeric_limits<double>::max();
			for (unsigned q = 0; q <= k; q++) {
				sol = std::min(sol,
						computeENS(i, j, q)
								+ computeENSTilde(i, tree.getNextSibling(j),
										k - q));
			}
		}
		// insert solution value into the memoization table
		memoENStilde[key] = sol;
	} else
		sol = mapIt->second;		//return solution from table
	return sol;

}

double SwitchesDP::computeENS(Vertex &i, Vertex &j, unsigned k) {

	double sol;
	// first check memoization table
	unsigned long key = hash(tree.getNodeIndex(i), tree.getNodeIndex(j), k);
	std::unordered_map<unsigned long, double>::const_iterator mapIt =
			memoENS.find(key);

	if (mapIt == memoENS.end()) {
		unsigned degree = tree.getDegree(j);
		// if current node is the root or it is a substation, then its
		// preceding arc already contains a switch (circuit breaker).
//		if (tree.getNode(j).getType() == Node::SUBSTATION
//				|| tree.getNode(j).getType() == Node::ROOT) {
//
//			if (degree == 1) {
//
//				sol = computeENS(j, tree.getFirstSuc(j), k)
//						+ tree.getNode(j).getFailureTime()
//								* tree.getNode(j).getActiveLoadSum();
//
//			} else {
//
//				sol = computeENSTilde(j, tree.getFirstSuc(j), k)
//						+ tree.getNode(j).getFailureTime()
//								* tree.getNode(j).getActiveLoadSum();
//
//			}
//		} else {
		//{
		// if number of switches is greater than the number of arcs in subtree
		if (k > tree.getNode(j).getNumSuc()) {

			sol = tree.getNode(j).getMinENS();

			// if there are no more available switches
		} else if (k == 0) {

			sol = tree.getNode(j).getFailureTimeSum()
					* tree.getNode(i).getActiveLoadSum();

			// j is not branched (and k > 0)
		} else if (degree == 1) {

			sol = std::min(
					computeENS(j, tree.getFirstSuc(j), k - 1)
							+ tree.getNode(j).getFailureTime()
									* tree.getNode(j).getActiveLoadSum(),
					computeENS(i, tree.getFirstSuc(j), k)
							+ tree.getNode(j).getFailureTime()
									* tree.getNode(i).getActiveLoadSum());

			// j is branched (and k > 0)
		} else {
			sol = std::min(
					computeENSTilde(j, tree.getFirstSuc(j), k - 1)
							+ tree.getNode(j).getFailureTime()
									* tree.getNode(j).getActiveLoadSum(),
					computeENSTilde(i, tree.getFirstSuc(j), k)
							+ tree.getNode(j).getFailureTime()
									* tree.getNode(i).getActiveLoadSum());
		}
		//}
		// insert solution value into the memoization table
		memoENS[key] = sol;
		//cout << "i: "  <<  i << " j: " << j << " sol: " << sol << endl;
	} else
		sol = mapIt->second;		//return solution from table
	return sol;
}


void SwitchesDP::recoverSwitchesTilde(Vertex &i, Vertex &j, unsigned k) {
	double sol, solmin;
	unsigned qmin;
	if (tree.getNextSibling(j) == TreeGraph::null_vertex()) {
		recoverSwitches(i, j, k);
	} else {
		// Complexity O(k) for the linear search of the best projection
		solmin = std::numeric_limits<double>::max();
		qmin = 0;
		for (unsigned q = 0; q <= k; q++) {
			sol = memoENS[hash(i, j, q)]
					+ memoENStilde[hash(i, tree.getNextSibling(j), k - q)];
			if (solmin > sol) {
				solmin = sol;
				qmin = q;
			}
		}
		recoverSwitches(i, j, qmin);
		recoverSwitchesTilde(i, tree.getNextSibling(j), k - qmin);
	}
}

void SwitchesDP::recoverSwitches(Vertex &i, Vertex &j, unsigned k) {
	double sol1, sol2;
	std::pair<double, std::vector<Edge>> compSol, compSol1, compSol2;

	unsigned degree = tree.getDegree(j);
	// if current node is the root or it is a substation, then its
	// preceding arc already contains a switch (circuit breaker).
//		if (tree.getNode(j).getType() == Node::SUBSTATION
//				|| tree.getNode(j).getType() == Node::ROOT) {
//
//			if (degree == 1) {
//
//				sol = computeENS(j, tree.getFirstSuc(j), k)
//						+ tree.getNode(j).getFailureTime()
//								* tree.getNode(j).getActiveLoadSum();
//
//			} else {
//
//				sol = computeENSTilde(j, tree.getFirstSuc(j), k)
//						+ tree.getNode(j).getFailureTime()
//								* tree.getNode(j).getActiveLoadSum();
//
//			}
//		} else {
	//{
	// if number of switches is greater than the number of arcs in subtree
	if (k > tree.getNode(j).getNumSuc()) {

		for (auto itv = tree.getVerticesInTree(j).begin();
				itv != tree.getVerticesInTree(j).end(); itv++)
			switches.push_back(tree.getInEdge(*itv));

		switches.push_back(tree.getInEdge(j));

		// if there are no more available switches
	} else if (k == 0) {

		//do nothing

		// j is not branched (and k > 0)
	} else if (degree == 1) {

		sol1 = memoENS[hash(j, tree.getFirstSuc(j), k - 1)];
		sol1 += tree.getNode(j).getFailureTime()
				* tree.getNode(j).getActiveLoadSum();

		sol2 = memoENS[hash(i, tree.getFirstSuc(j), k)];
		sol2 += tree.getNode(j).getFailureTime()
				* tree.getNode(i).getActiveLoadSum();

		if (sol1 < sol2) {
			switches.push_back(tree.getInEdge(j));
			recoverSwitches(j, tree.getFirstSuc(j), k - 1);
		} else {
			recoverSwitches(i, tree.getFirstSuc(j), k);
		}
		// j is branched (and k > 0)
	} else {
		sol1 = memoENStilde[hash(j, tree.getFirstSuc(j), k - 1)];
		sol1 += tree.getNode(j).getFailureTime()
				* tree.getNode(j).getActiveLoadSum();

		sol2 = memoENStilde[hash(i, tree.getFirstSuc(j), k)];
		sol2 += tree.getNode(j).getFailureTime()
				* tree.getNode(i).getActiveLoadSum();

		if (sol1 < sol2) {
			switches.push_back(tree.getInEdge(j));
			recoverSwitchesTilde(j, tree.getFirstSuc(j), k - 1);
		} else {
			recoverSwitchesTilde(i, tree.getFirstSuc(j), k);
		}
	}

}

void SwitchesDP::run(unsigned k) {
	double ENDmin = computeENS(tree.getRoot(), tree.getRoot(), k);
	std::cout << "k = " << k << " \t END = " << ENDmin << std::endl;
	recoverSwitches(tree.getRoot(), tree.getRoot(), k);
	std::cout << "switches in arcs: " << switches.size() << std::endl;
		std::copy(switches.begin(), switches.end(),
				std::ostream_iterator<Edge>(std::cout, "\n"));
}

void SwitchesDP::run(unsigned kmax, unsigned limit_time) {
	auto start = std::chrono::high_resolution_clock::now();
	auto stop = std::chrono::high_resolution_clock::now();
	auto duration = std::chrono::duration_cast<std::chrono::milliseconds>(
			stop - start);
	for (unsigned k = 0; k < kmax && duration.count() < limit_time * 1000;
			k++) {
		auto partialstart = std::chrono::high_resolution_clock::now();
		double minENS = computeENS(tree.getRoot(), tree.getRoot(), k);
		stop = std::chrono::high_resolution_clock::now();
		auto partialduration = std::chrono::duration_cast<
				std::chrono::milliseconds>(stop - partialstart);
		duration = std::chrono::duration_cast<std::chrono::milliseconds>(
				stop - start);
		std::cout << fixed << std::setprecision(2) << "k = " << k
				<< " \t ENS = " << minENS;
		recoverSwitches(tree.getRoot(), tree.getRoot(), k);
		std::cout << " : ";
		std::copy(switches.begin(), switches.end(),
				std::ostream_iterator<Edge>(std::cout, ", "));
		std::cout << " time: " << partialduration.count() / 1000.0 << " secs"
				<< endl;
		clearSwitches();

	}
	std::cout << "Total time: = " << duration.count() / 1000.0 << " secs"
			<< endl;

}

