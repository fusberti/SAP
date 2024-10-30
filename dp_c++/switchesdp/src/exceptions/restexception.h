/***************************************************************************
                          restexception.h  -  description
                             -------------------
    begin                : Qui Dez 4 2003
    copyright            : (C) 2003 by Vinicius Jacques Garcia
    email                : jacques@densis.fee.unicamp.br
 ***************************************************************************/
#ifndef RESTEXCEPTION_H
#define RESTEXCEPTION_H

#include <string>


class RestException {

private:

	std::string reason;


public:

	RestException(std::string reason): reason(reason) {}
	
	~RestException() {}

	std::string cause() { return( this->reason ); }

	void appendCause(std::string str) { this->reason.append( str ); }
};


#endif
