#include <iostream>

#include "arc.h"


Arc::Arc(unsigned long number,int source,int target,double length): // @suppress("Class members should be properly initialized")
number(number), source(source), target(target),length(length), type(0){
}

Arc::Arc(){}

Arc::~Arc(){
}

std::ostream& operator<<(std::ostream &strm, const Arc &arc){
return strm << arc.number <<  "(" << arc.source << ", " << arc.target << ")";
}
