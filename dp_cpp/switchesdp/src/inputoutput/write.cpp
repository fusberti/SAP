/***************************************************************************
                          write.cpp  -  description
                             -------------------
    begin                : Qui Dez 4 2003
    copyright            : (C) 2003 by Vinicius Jacques Garcia
    email                : jacques@densis.fee.unicamp.br
 ***************************************************************************/

#include <iostream>
#include <fstream>
#include <string>
#include <stdexcept>
#include <cstring>

using namespace std;

#include "write.h"


Write::Write(const char *fileName,const bool holdOpened) :
	holdOpened(holdOpened), arqPoint(fileName,ios::trunc){

	this->fileName=new char[strlen(fileName)+1];
	strcpy(this->fileName,fileName);

	if( !this->holdOpened )
		this->closeFile();
}


Write::~Write(){
	this->closeFile();
}


void Write::write(const char *content){

	if( !this->holdOpened )
		arqPoint.open(this->fileName,ios::app);

	this->arqPoint << content;

	if( !this->holdOpened )
		this->closeFile();
}


void Write::write(string content){

	if( !this->holdOpened )
		arqPoint.open(this->fileName,ios::app);

	this->arqPoint << content;

	if( !this->holdOpened )
		this->closeFile();
}


void Write::write(unsigned long num){

	if( !this->holdOpened )
		arqPoint.open(this->fileName,ios::app);

	this->arqPoint << num;

	if( !this->holdOpened )
		this->closeFile();
}


void Write::write(long num){

	if( !this->holdOpened )
		arqPoint.open(this->fileName,ios::app);

	this->arqPoint << num;

	if( !this->holdOpened )
		this->closeFile();
}


void Write::write(double num){

	if( !this->holdOpened )
		arqPoint.open(this->fileName,ios::app);

	this->arqPoint << num;

	if( !this->holdOpened )
		this->closeFile();
}


void Write::write(unsigned short num){

	if( !this->holdOpened )
		arqPoint.open(this->fileName,ios::app);

	this->arqPoint << num;

	if( !this->holdOpened )
		this->closeFile();
}

