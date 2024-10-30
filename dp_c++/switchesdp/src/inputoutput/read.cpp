/***************************************************************************
                         read.cpp  -  description
                            -------------------
   begin                : Qui Dez 4 2003
   modified				: Ter Jan 6 2007 by jfvg
   copyright            : (C) 2003 by Vinicius Jacques Garcia
   email                : jacques@densis.fee.unicamp.br
***************************************************************************/

#include <iomanip>
#include <stdlib.h>
#include <iostream>
#include <fstream>
#include <sstream>
#include <cstring>

#include "../exceptions/restexception.h"
#include "read.h"


Read::Read(const char *arqName) :  fileName(arqName),arqPoint( arqName, std::ifstream::in ), str(""), lineBuffer(str,std::istringstream::in) {
       s=new char[Read::S_SIZE];
       if ( (this->arqPoint.rdstate() & std::ifstream::failbit ) != 0 )
    	   throw RestException("\nConstructor Read::Read(): the file can't be opened!!");
}

Read::~Read(){
       delete s;
       this->closeFile();
}

bool Read::fileWasOpened(){
 if ( (this->arqPoint.rdstate() & std::ifstream::failbit ) != 0 )
               return( false );
       else
               return( true );
}

bool Read::isNumber(std::string str){

       bool eNumero=true;
       unsigned int count=0;

       while( eNumero && (count<str.length()) ){
               if( (str.at(count)!='.') && (str.at(count)!='-') && ((str.at(count)<'0') || (str.at(count)>'9')) )
                       eNumero=false;
               count++;
       }

       if( count==0 )
               eNumero=false;

       return( eNumero );
}

bool Read::readFromFileToBuffer(){

       bool isComment=true;

       // READ LINE FROM THE FILE UNTIL THE
       // STRING READ IS NOT COMMENT OR
       // THE END OF FILE IS REACHED
       while( isComment && !this->endOfFile() ){
               this->arqPoint.getline( this->s, S_SIZE );
               isComment=this->isComment(this->s);
       }

       // IF NOT COMMENT AND THE STRING READ HAS SIZE
       // MORE THAN ZERO PUT IT IN THE BUFFER
       if( !isComment && (strlen(this->s)>0) ){
               this->str="";
               this->str.insert(0,this->s);
               this->lineBuffer.str( this->str );
               this->lineBuffer.clear();
       }

       this->findNextValidCaractere(this->arqPoint);

       return( this->endOfFile() );
}

std::string Read::readWordFromBuffer(){

	std::string returnStr("");

       this->lineBuffer >> returnStr;

       if( returnStr=="" ){
               this->readFromFileToBuffer();
               if( !this->emptyBuffer() )
                       this->lineBuffer >> returnStr;
               else
                       throw RestException("\nMethod Read::readWordFromBuffer() : unexpected end of file !!!");
       }

       this->findNextValidCaractere(this->lineBuffer);

       return( returnStr );
}

double Read::readNumberFromBuffer(){
	std::string wordRead;


	   try{
			   wordRead=this->readWordFromBuffer();

	   }
	   catch(RestException &e){
			   e.appendCause("\nMethod Read::readNumberFromBuffer() : unexpected end of file !!!");
			   throw RestException( e.cause() );
	   }
	   if(!this->isNumber(wordRead)){
    	   RestException e("\nMethod Read::readNumberFromBuffer() : not a number !!!");
           throw RestException( e.cause() );
       }
       return( atof(wordRead.c_str()) );
}

void Read::resetArqPointer(){

       //this->arqPoint.seekg (0, ios::beg);
       this->arqPoint.clear();
       this->arqPoint.seekg (0, std::ios::beg);

       this->lineBuffer.str( "" );
       this->lineBuffer.clear();
       this->lineBuffer >> this->str;
}

std::string Read::readLine(){

       bool isComment=true;

       // READ LINE FROM THE FILE UNTIL THE
       // STRING READ IS NOT COMMENT OR
       // THE END OF FILE IS REACHED
       while( isComment && !this->endOfFile() ){
               this->arqPoint.getline( this->s, S_SIZE );
               isComment=this->isComment(this->s);
       }

       // IF NOT COMMENT AND THE STRING READ HAS SIZE
       // MORE THAN ZERO PUT IT IN THE BUFFER
       this->str="";
       if( !isComment && (strlen(this->s)>0) )
               this->str.insert(0,this->s);

       return( this->str );
}
