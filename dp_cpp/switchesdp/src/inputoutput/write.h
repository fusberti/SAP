/***************************************************************************
                          write.h  -  description
                             -------------------
    begin                : Qui Dez 4 2003
    copyright            : (C) 2003 by Vinicius Jacques Garcia
    email                : jacques@densis.fee.unicamp.br
 ***************************************************************************/

#ifndef WRITE_H
#define WRITE_H


class Write {

protected:

	char *fileName;
	bool holdOpened;
	std::ofstream arqPoint;



public:

	Write(const char *fileName,const bool holdOpened);

	~Write();

	inline void closeFile() {	this->arqPoint.close(); }

	inline std::ofstream& getStream() { return(this->arqPoint); }

	void write(const char *content);
	void write(std::string content);
	void write(unsigned long num);
	void write(long num);
	void write(double num);
	void write(unsigned short num);
};

#endif
