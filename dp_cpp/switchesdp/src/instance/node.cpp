#include "node.h"

Node::Node(int number, short clients, double load, double time): // @suppress("Class members should be properly initialized")
number(number), numclients(clients), activeLoad(load), failureTime(time){
	if (number==-1){
		type = ROOT;
		numclients = 0;
		activeLoad = 0;
	}
	else{
		type = clients < 0 ? SUBSTATION : LOAD;
		if (type == SUBSTATION){
			numclients = 0;
			activeLoad = 0;
		}
	}
}

Node::Node(){
}


Node::~Node(){
}
