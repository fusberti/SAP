#ifndef ARC_H
#define ARC_H

class Arc {

private:

	unsigned long number;//ARC NUMBER (INDEX)
	int source;//SOURCE NODE INDEX
	int target;//TARGET NODE INDEX
	unsigned short type;
	double length; // IN METERS(m)

	 friend std::ostream& operator<<(std::ostream&, const Arc&);

public:


	Arc(unsigned long number,int source,int target, double length);
	Arc();
	~Arc();

	inline unsigned long getNumber() { return( this->number ); }
	inline int getSource() { return( this->source ); }
	inline void setSource(int source) { this->source=source; }
	inline int getTarget() { return( this->target ); }
	inline void setTarget(int target) { this->target=target; }
	inline unsigned short getType(){ return( this->type ); }
	inline double getLength() { return( this->length ); }
	void setType(unsigned short type){ this->type = type;}

	static bool comparator(Arc* a1,Arc* a2) { return( a1->getNumber()<a2->getNumber() ); }

};

#endif
