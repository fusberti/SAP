#ifndef NETWORK_H_
#define NETWORK_H_

class Node;
class Arc;
class Instance;


typedef boost::adjacency_list<boost::listS, boost::vecS, boost::bidirectionalS, Node, Arc> TreeGraph;
typedef TreeGraph::edge_descriptor Edge;
typedef TreeGraph::vertex_descriptor Vertex;
typedef std::set<Edge> EdgeSet;
typedef TreeGraph::edge_iterator Edge_iterator;
typedef TreeGraph::out_edge_iterator Edge_out_iterator;
typedef TreeGraph::in_edge_iterator Edge_in_iterator;

class Network {

	Instance &instance;
	TreeGraph graph;
	Vertex root;
	std::map<unsigned long, Vertex> mapNodes;
	std::unordered_map<Vertex, unsigned long> mapIdxNodes;
	std::map<Edge, unsigned long> mapArcs;
	std::unordered_map<Vertex, std::vector<Edge>> mapIncidents;
	std::unordered_map<Vertex, Edge> mapInEdges;
	std::unordered_map<Vertex, std::vector<Vertex>> mapOutVertices;
	std::vector<Vertex> nextSibling;

	std::vector<Vertex> predecessors;
	std::vector<Vertex> depth;
	std::vector<std::vector<Vertex>> verticesInTree;
	void addVertices(Vertex &current, std::vector<Vertex> &verticesInTree);
	void fillVerticesInTree();


public:
	Network(Instance &inst);
	~Network();

	inline Instance& getInstance(){return instance;}
	inline TreeGraph& getGraph(){return graph;}
	inline Vertex& getDepth(Vertex vert){return depth[vert];}
	inline Vertex& getPredecessor(Vertex vert){return predecessors[vert];}
	inline unsigned short getNumArcs(){return boost::num_edges(graph);}
	inline Vertex getSource(Edge edge){return boost::source(edge, graph);}
	inline Vertex getTarget(Edge edge){return boost::target(edge, graph);}
	inline Vertex& getRoot(){return root;}
	inline unsigned long getArcIndex(Edge edge){return mapArcs[edge];}
	inline Vertex& getVertex(unsigned long idx){return mapNodes[idx];}
	inline std::vector<Edge>& getIncidentArcs(Vertex v){return mapIncidents[v];}
	inline double getArcLength(Edge edge){return graph[edge].getLength();}
	inline Edge& getInEdge(Vertex v){return mapInEdges[v];}
	inline std::vector<Vertex>& getOutVertices(Vertex v){return mapOutVertices[v];}
	inline Vertex& getFirstSuc(Vertex v){return mapOutVertices[v][0];}
	inline Node& getNode(Vertex v){return graph[v];}
	inline unsigned long getNodeIndex(Vertex v){return mapIdxNodes[v];}
	inline Vertex& getNextSibling(Vertex &v){return nextSibling[getNodeIndex(v)];}
	inline unsigned getDegree(Vertex &v){return mapOutVertices[v].size();}
	inline unsigned short getNumNodes(){return boost::num_vertices(graph);}
	inline std::vector<Vertex>& getVerticesInTree(unsigned idx){return verticesInTree[idx];}
};

#endif /* NETWORK_H_ */
