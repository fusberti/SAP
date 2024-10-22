#ifndef NODE_H
#define NODE_H


class Node {

private:

	int number;
	unsigned short type;
	unsigned short numclients;
	unsigned short clientsum=0;//customers downstream
	unsigned short numSuc = 0;
	unsigned short numNodesNextBranches = 0;
	double failureTime;
	double failureTimeSum=0;
	double activeLoad; // IN kW
	double activeLoadSum=0;////customers downstream demand
	double reactiveLoad;
	double minENS=0;//minimum ENS if node fails (product of activeLoad and failuteTime)
	double minENSBranches=0;
	double xcoord;
	double ycoord;
	char* label; //ONLY IF THE TYPE OF THE NODE IS "SUBSTATION" OR "SUBSTATION_TRANSF"

public:

	static const unsigned short SUBSTATION=-1;
	static const unsigned short ROOT=0;//artificial node
	static const unsigned short LOAD=1;

	Node(int number, short clients, double activeLoad, double time);
	Node();

	~Node();

	inline int getNumber() { return( this->number ); }
	inline void setNumber(int number) { this->number=number; }
	inline unsigned short getType() { return( this->type ); }
	inline double getActiveLoad() { return( this->activeLoad ); }
	inline double getReactiveLoad() { return( this->reactiveLoad ); }
	inline unsigned short getNumClients() { return(this->numclients);}
	inline void setNumClients(unsigned short number) { this->numclients = number;}

	inline void setActiveLoad(double actLoad){this->activeLoad = actLoad; }
	inline void setReactiveLoad(double reLoad){this->reactiveLoad = reLoad; }

	inline void setX(double x) { this->xcoord=x; }
	inline double getX() { return( this->xcoord ); }
	inline void setY(double y) { this->ycoord=y; }
	inline double getY() { return( this->ycoord ); }

	inline void setType(unsigned short type){this->type = type;}

	inline char* getLabel(){return this->label; }
	static bool comparator(Node* n1,Node* n2) { return( n1->getNumber()<n2->getNumber() ); }

	double inline getFailureTime() const {return failureTime;}
	void inline setFailureTime(double failureTime) {this->failureTime = failureTime;}

	double getActiveLoadSum() const {return activeLoadSum;}
	void setActiveLoadSum(double activeLoadSum) {this->activeLoadSum = activeLoadSum;}

	unsigned short inline getClientsSum() {return clientsum;}
	void inline setClientsSum(unsigned short clientsum) {this->clientsum = clientsum;}

	double inline getFailureTimeSum() const {return failureTimeSum;}
	void inline setFailureTimeSum(double failureTimeSum) {this->failureTimeSum = failureTimeSum;}

	double inline getMinENS() const {return minENS;}
	void inline setMinENS(double minENS = 0) {	this->minENS = minENS;}

	unsigned short inline getNumSuc() const { return numSuc;}
	void inline setNumSuc(unsigned short numSuc = 0) {this->numSuc = numSuc;}

	double inline getMinENSBranches() const {return minENSBranches;}
	void inline setMinENSBranches(double minENS = 0) {	this->minENSBranches = minENS;}

	unsigned short inline getNumNodesNextBranches() {return numNodesNextBranches;}
	void inline setNumNodesNextBranches(unsigned short numNodes) {this->numNodesNextBranches = numNodes;}

};

#endif
