/*
 * Network.cpp
 *
 *  Created on: 24 de jun de 2020
 *      Author: jfvg
 */
#include <iostream>
#include <boost/graph/adjacency_list.hpp>
#include <boost/graph/breadth_first_search.hpp>

using namespace std;

#include "../instance/node.h"
#include "../instance/arc.h"
#include "../instance/instance.h"
#include "network.h"


Network::Network(Instance &inst) :
		instance(inst) {
	//construct all graph structures: sucessors, predecessors, depht vectors
	NodeList nodes = inst.getNodes();
	ArcList arcs = inst.getArcs();

	for (Node node : nodes) { //inserting nodes
		Vertex v = boost::add_vertex(node, graph);
		mapNodes.insert(make_pair(node.getNumber(), v));
	}
	for (Arc arc : arcs) { //inserting arcs
		mapArcs.insert(
				make_pair(
						boost::add_edge(mapNodes[arc.getSource()],
								mapNodes[arc.getTarget()], arc, graph).first,
						arc.getNumber()));
	}
	unsigned idx = 0;
	for (Vertex vt : boost::make_iterator_range(boost::vertices(graph))) {
		mapIdxNodes.insert(make_pair(vt, idx++));
	}

	root = *vertices(graph).first;		//the root node

	depth.assign(boost::num_vertices(graph), TreeGraph::null_vertex());
	predecessors.assign(boost::num_vertices(graph), TreeGraph::null_vertex());

	boost::breadth_first_search(graph, root,
			boost::visitor(
					boost::make_bfs_visitor(
							boost::record_distances(depth.data(),
									boost::on_tree_edge { }))));

	for (auto d = depth.begin(); d != depth.end(); ++d)
		if (*d == Instance::NULLS)
			*d = 0;
		else
			(*d)++;

	breadth_first_search(graph, root,
			visitor(
					boost::make_bfs_visitor(
							boost::record_predecessors(predecessors.data(),
									boost::on_tree_edge { }))));


	nextSibling.resize(nodes.size(), TreeGraph::null_vertex());	//initializing the sibling structure

	Edge_out_iterator eo, eo_end;
	Edge_in_iterator ein, ein_end;
	for (Vertex vd : boost::make_iterator_range(boost::vertices(graph))) {
		std::vector<Edge> incidents;
		std::vector<Vertex> outVertices;
		Vertex last = TreeGraph::null_vertex();
		for (boost::tie(eo, eo_end) = boost::out_edges(vd, graph); eo != eo_end;
				++eo) {
			incidents.push_back(*eo);
			outVertices.push_back(getTarget(*eo));
			if (last != TreeGraph::null_vertex())
				nextSibling[this->getNodeIndex(last)] = getTarget(*eo);
			last = getTarget(*eo);
		}
		for (boost::tie(ein, ein_end) = boost::in_edges(vd, graph);
				ein != ein_end; ++ein) {
			incidents.push_back(*ein);
			mapInEdges.insert(std::make_pair(vd, *ein));
		}
		mapIncidents.insert(std::make_pair(vd, incidents));
		mapOutVertices.insert(std::make_pair(vd, outVertices));
	}
	fillVerticesInTree();
}

void Network::addVertices(Vertex &current, std::vector<Vertex> &verticesInTree) {
	std::vector<Vertex>::const_iterator itVertices =
			getOutVertices(current).begin();
	while (itVertices != getOutVertices(current).end()) {
		Vertex next = *itVertices;
		verticesInTree.push_back(next);
		addVertices(next, verticesInTree);
		itVertices++;
	}
}

void Network::fillVerticesInTree() {
	verticesInTree.resize(getNumNodes());
	for (Vertex vd : boost::make_iterator_range(boost::vertices(graph))) {
		std::vector<Vertex> vertices;
		addVertices(vd, vertices);
		verticesInTree[getNodeIndex(vd)] = vertices;
	}
}



Network::~Network() {
}

