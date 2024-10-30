/***************************************************************************
                         read.h  -  description
                            -------------------
   begin                : Qui Dez 4 2003
   modified				: Ter Jan 6 2007 by jfvg
   copyright            : (C) 2003 by Vinicius Jacques Garcia
   email                : jacques@densis.fee.unicamp.br
***************************************************************************/


#ifndef READ_H
#define READ_H

#include <cstring>
//#include <sstream>

class RestException;


class Read {

protected:

       static const int S_SIZE=10000;

	   std::string fileName;
       char *s;
       std::ifstream arqPoint;
       std::string str;
       std::istringstream lineBuffer;

       bool isNumber(std::string str);
       bool readFromFileToBuffer();

       std::string readWordFromBuffer();
       double readNumberFromBuffer();

       inline void findNextValidCaractere(std::istream &inputStream){
               bool valid=false;
               while( !valid && !inputStream.eof() )
                       if( !this->validCaractere(inputStream.peek()) )
                               inputStream.get();
                       else
                               valid=true;
       }

       inline bool validCaractere(char c){
               if( (c!=' ') && (c!='\t') && (c!='\n') && (c!=EOF) )
                       return(true);
               else
                       return(false);
       }

       inline bool isComment(char *s){
               unsigned short count;
               bool valid=false, isComment=true;
               for(count=0; !valid && (count<strlen(s)); count++)
                       if( this->validCaractere(s[count]) )
                               valid=true;
               if( valid )
                       if( (s[count-1]!='#') && (s[count-1]!='/') && (s[count]!='*') )
                               isComment=false;
               return( isComment );
       }

public:

       Read(const char *arqName);

       ~Read();

       bool fileWasOpened();

       void resetArqPointer();

       inline void closeFile() { this->arqPoint.close(); }

       inline std::string readWord() { return( this->readWordFromBuffer() ); }
       inline double readDouble() { return( this->readNumberFromBuffer() ); }
       inline long readLong() { return( (long) this->readNumberFromBuffer() ); }
       inline unsigned short readUnsignedShort() { return( (unsigned short) this->readNumberFromBuffer() );}

       inline bool endOfFile() { return( this->arqPoint.eof() ); }

       inline bool emptyBuffer() { return( this->lineBuffer.eof() ); }

       inline std::string getBuffer() { return( this->lineBuffer.str() ); }

       std::string readLine();

       inline std::string getFileName(){return fileName;}
};

#endif

