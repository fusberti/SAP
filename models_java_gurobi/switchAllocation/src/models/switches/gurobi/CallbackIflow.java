/* Copyright 2012, Gurobi Optimization, Inc. */

/* This example reads an LP or a MIP from a file, sets a callback
   to monitor the optimization progress and to output some progress
   information to the screen and to a log file. If it is a MIP and 10%
   gap is reached, then it aborts */

package models.switches.gurobi;

import instances.networks.edges.E;
import instances.networks.edges.E.SwitchType;
import instances.networks.vertices.V;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

//import graphClasses.V;
import com.gurobi.gurobi.*;

public class CallbackIflow extends GRBCallback {

  private SolverIflow gurobi;
  private ArrayList<E> stackEdges = new ArrayList<E>();
  private double maximalTheta, maximalFracX;
  private boolean foundCut;
  private int countNoFlowCuts = 0, countNoDtCuts = 0;
//  private int countNoCoverCuts = 0;
  private int maxCountNoFlowCuts, maxCountNoDtCuts;
  //private int maxCountNoCoverCuts;
  private double sumTX = 0.0f, sumT = 0.0f;
  private E goodSec;

  public CallbackIflow(SolverIflow gurobi) {
    this.gurobi = gurobi;
    this.gurobi.numFlowCuts = 0;
    this.gurobi.userCutsTime = 0;
    this.maxCountNoFlowCuts = gurobi.inst.net.getNumNoProt();
    this.maxCountNoDtCuts = gurobi.inst.net.getNumNoProt();
    //this.maxCountNoCoverCuts = gurobi.inst.net.getNumNoProt();
  }
   
  protected void callback() {
	  try {
		  	    
		  if (where == GRB.CB_MIPNODE) {

//			//CORTES TRIVIAIS
//			  if (repeatBaseCut) {
//				  repeatBaseCut = false;
//				  Iterator<E> iterEdges;
//
//				  //CUTS : Fij >= thetarj - thetarj*xij (all (i,j) in E)	
//				  GRBLinExpr cut = new GRBLinExpr();
//
//				  iterEdges = gurobi.inst.g.getEdges().iterator();
//
//				  while (iterEdges.hasNext()) {
//
//					  E edge = iterEdges.next();
//					  //if (!edge.prot) {
//					  if (edge.status != SwitchType.PROT) {
//
//						  cut.clear();
//
//						  cut.addTerm(1.0f, gurobi.f[edge.idNoProt]);
//						  cut.addTerm(edge.node2.thetaR, gurobi.x[edge.idNoProt]);
//						  addCut(cut, GRB.GREATER_EQUAL, edge.node2.thetaR);
//
//					  }
//				  }
//			  }

			  //FLOW CUTS
			  if ((getIntInfo(GRB.CB_MIPNODE_STATUS) == GRB.OPTIMAL)&&(this.countNoFlowCuts < maxCountNoFlowCuts)&&(this.gurobi.applyUserFlowCuts)) {
				  
				  long time = System.nanoTime();
				  
				  this.countNoFlowCuts++;

				  Iterator<E> iterEdges = gurobi.inst.net.getG().getEdges().iterator();
				  GRBLinExpr cut = new GRBLinExpr();

				  while (iterEdges.hasNext()) {

					  E edge = iterEdges.next();
					  //if (!edge.prot) {
					  if (edge.status != SwitchType.PROT) {

						  //double xValue = getNodeRel(gurobi.model.getVarByName("X["+edge.node1.label+","+edge.node2.label+"]"));
						  double xValue = getNodeRel(gurobi.x[edge.idNoProt]);

						  if ((xValue > SolverIflow.env.get(GRB.DoubleParam.IntFeasTol))&&(xValue < (1.0f-SolverIflow.env.get(GRB.DoubleParam.IntFeasTol)))) {							

							  this.maximalFracX = this.maximalTheta = 0.0f;
							  this.foundCut = false;
							  probeMaximalFlowCut(edge, 0.0f);
							  //se foi encontrado um corte
							  if (foundCut) {
								  double fValue = getNodeRel(gurobi.f[edge.idNoProt]);
								  //se o corte de fato eliminar a solucao fracionaria
								  if ((fValue + this.maximalFracX) < (this.maximalTheta - SolverIflow.env.get(GRB.DoubleParam.IntFeasTol))) {
									  //System.out.println("\nthis.countNoCuts = "+this.countNoCuts);
									  this.countNoFlowCuts = 0;
									  cut.clear();
									  stackEdges.clear();
									  addMaximalCut(edge,0.0f,cut);
									  cut.addTerm(1.0f, gurobi.f[edge.idNoProt]);
									  
									  //System.out.println("sol antes do corte = "+getDoubleInfo(GRB.CB_MIPNODE_OBJBND));
									  //System.out.println("cortes antes = "+getIntInfo(GRB.CB_MIP_CUTCNT));
									  addCut(cut, GRB.GREATER_EQUAL, this.maximalTheta);
									  //System.out.println("sol depois do corte = "+getDoubleInfo(GRB.CB_MIPNODE_OBJBND));
									  //System.out.println("cortes depois = "+getIntInfo(GRB.CB_MIP_CUTCNT));
									  this.gurobi.numFlowCuts++;
								  }
							  }
						  }
					  }
					  
				  }
				  
				  if (this.countNoFlowCuts == maxCountNoFlowCuts) {
					  System.out.println("\nUser flow cuts off.");
					  this.gurobi.applyUserFlowCuts = false;
					  //gurobi.env.set(GRB.IntParam.Cuts, -1);
					  //System.exit(0);
				  }
				  			  
				  this.gurobi.userCutsTime += (System.nanoTime()-time);
				  
			  }
			  
			  //DT CUTS
			  if ((getIntInfo(GRB.CB_MIPNODE_STATUS) == GRB.OPTIMAL)&&(this.countNoDtCuts < maxCountNoFlowCuts)&&(this.gurobi.applyUserDtCuts)) {

				  long time = System.nanoTime();

				  this.countNoDtCuts++;

				  Iterator<E> goodSecs = gurobi.inst.net.getTransferSecs().iterator();
				  GRBLinExpr dtCut = new GRBLinExpr();

				  while (goodSecs.hasNext()) {

					  dtCut.clear();
					  sumTX = 0.0f;
					  sumT = 0.0f;

					  goodSec = goodSecs.next();
					  if (goodSec.status != SwitchType.PROT) {

						  double dtValue = getNodeRel(gurobi.dt[goodSec.idGoodSec]);
						  double xValue = getNodeRel(gurobi.x[goodSec.idNoProt]);

						  if ((xValue > SolverIflow.env.get(GRB.DoubleParam.IntFeasTol))&&(xValue < (1.0f-SolverIflow.env.get(GRB.DoubleParam.IntFeasTol)))) {
  
						dtCut.addTerm(1.0f, gurobi.dt[goodSec.idGoodSec]);
						dtCut.addTerm(-(goodSec.node1.subNode.M-goodSec.node2.M), gurobi.x[goodSec.idNoProt]);								  
						  double sumTX0 = (goodSec.node1.subNode.M-goodSec.node2.M)*xValue;							  
							  
							  V sourceNode = goodSec.node2;
							  do {

								  V sinkNode = sourceNode;

								  Iterator<V> sourceNodes = gurobi.inst.net.getG().getPredecessors(sinkNode).iterator();
								  sourceNode = sourceNodes.next();

								  Iterator<E> outEdges = gurobi.inst.net.getG().getOutEdges(sourceNode).iterator();

								  while (outEdges.hasNext()) {

									  E outEdge = outEdges.next();
									  if (outEdge.node2 != sinkNode)
										  if (outEdge.status != SwitchType.PROT)
											  probeMaximalDtCut(outEdge,1-xValue,dtCut);
								  }

							  } while (sourceNode != gurobi.inst.net.getRoot());									  
					  

							  if (dtValue > (sumTX0 + (sumT - sumTX) + SolverIflow.env.get(GRB.DoubleParam.IntFeasTol))) {
								  this.countNoDtCuts = 0;
								  addCut(dtCut, GRB.LESS_EQUAL, sumT+goodSec.node1.subNode.M-goodSec.node2.M);
								  this.gurobi.numDtCuts++;								  
							  }

						  }

					  }
				  }

				  if (this.countNoDtCuts == maxCountNoDtCuts) {
					  System.out.println("\nUser dt cuts off ("+this.gurobi.numDtCuts+")");
					  this.gurobi.applyUserDtCuts = false;
				  }

				  this.gurobi.userCutsTime += (System.nanoTime()-time);				  

			  }
			  
//			  //IMPROVED COVER CUTS
//			  //Precisam ser muito aperfeicoados!!
//			  if ((getIntInfo(GRB.CB_MIPNODE_STATUS) == GRB.OPTIMAL)&&(this.countNoFlowCuts > 0)&&(this.countNoCoverCuts < this.maxCountNoCoverCuts)&&(this.gurobi.applyUserCoverCuts)) {
//
//				  double sumFrac = 0.0f; 
//				  
//				  long time = System.nanoTime();
//				  
//				  this.countNoCoverCuts++;
//				  
//				  ArrayList<E> fracEdges = new ArrayList<E>();
//				  ArrayList<E> zeroEdges = new ArrayList<E>();	
//				  GRBLinExpr cut = new GRBLinExpr();
//				  GRBLinExpr cut2 = new GRBLinExpr();
//				  
//				  Iterator<E> iterEdges = gurobi.inst.net.getG().getEdges().iterator();
//				  while (iterEdges.hasNext()) {
//					  E edge = iterEdges.next();
//					  if (edge.status != SwitchType.PROT) {
//						  double xValue = getNodeRel(gurobi.x[edge.idNoProt]);
//						  if (xValue < gurobi.env.get(GRB.DoubleParam.IntFeasTol)) {
//							  zeroEdges.add(edge);
//							  cut2.addTerm(1.0f, gurobi.x[edge.idNoProt]);
//						  } else if ((xValue < (1.0f-gurobi.env.get(GRB.DoubleParam.IntFeasTol)))) {
//							  fracEdges.add(edge);
//							  cut2.addTerm(1.0f, gurobi.x[edge.idNoProt]);
//							  sumFrac += xValue;
//						  }
//					  }
//				  }
//				  
//				  if ((sumFrac > gurobi.env.get(GRB.DoubleParam.IntFeasTol))&&(sumFrac < (1.0f-gurobi.env.get(GRB.DoubleParam.IntFeasTol)))) {			  
//					  this.countNoCoverCuts = 0;
//					  addLiftCoverCut(fracEdges,zeroEdges,cut);
//				  }		  
//			  
//				  if (this.countNoCoverCuts == maxCountNoCoverCuts) {
//					  System.out.println("\nUser improved cover cuts off.");
//				  }
//				  
//				  this.gurobi.userCutsTime += (System.nanoTime()-time);		  
//				  
//			  }
		  }			  

	  } catch (GRBException e) {
		  System.out.println("Error code: " + e.getErrorCode() + ". " +
				  e.getMessage());
		  e.printStackTrace();
	  }
  }  
  
  protected void probeMaximalFlowCut(E edge, double sumX) {

	  try {
			
		  double xValue = getNodeRel(gurobi.x[edge.idNoProt]);
		  double newSumX = sumX+xValue;

		  if (newSumX < (1.0f-SolverIflow.env.get(GRB.DoubleParam.IntFeasTol))) {
	  
			  this.foundCut = true;
			  this.maximalFracX += edge.node2.thetaR*newSumX;
			  this.maximalTheta += edge.node2.thetaR;
			  
			  Iterator<E> iterEdges = gurobi.inst.net.getG().getOutEdges(edge.node2).iterator();
			  while (iterEdges.hasNext()) {
				  E outEdge = iterEdges.next();
				  //if (!outEdge.prot)
				  if (outEdge.status != SwitchType.PROT)
					  probeMaximalFlowCut(outEdge,newSumX);
			  }

		  }

	  } catch (GRBException e) {
		  System.out.println("Error code: " + e.getErrorCode() + ". " +
				  e.getMessage());
		  e.printStackTrace();
	  }

  }  
   
  protected Set<E> probeMaximalDtCut(E edge, double xValuePred, GRBLinExpr dtCut) {

	  Set<E> subTreesUp = null;  

	  try {

		  if (xValuePred < (1.0f-SolverIflow.env.get(GRB.DoubleParam.IntFeasTol))) {
		  
			  double xValue = getNodeRel(gurobi.x[edge.idNoProt]);
	
			  if (xValue > xValuePred) {
				  xValuePred = xValue;
				  subTreesUp = new HashSet<E>();
				  subTreesUp.add(edge);
				  dtCut.addTerm(edge.node2.M, gurobi.x[edge.idNoProt]);
				  dtCut.addTerm(edge.node2.M, gurobi.x[goodSec.idNoProt]);
				  sumTX += xValue*edge.node2.M;
				  sumTX += getNodeRel(gurobi.x[goodSec.idNoProt])*edge.node2.M;
				  sumT += edge.node2.M;
			  }

			  Iterator<E> outEdges = gurobi.inst.net.getG().getOutEdges(edge.node2).iterator();
			  while (outEdges.hasNext()) {
				  E outEdge = outEdges.next();
				  if (outEdge.status != SwitchType.PROT) {
					  Set<E> subTreesDown = probeMaximalDtCut(outEdge,xValuePred,dtCut);
					  if (subTreesDown != null) {
						  if (subTreesUp != null) {
							  Iterator<E> subTreeIter = subTreesDown.iterator();
							  while (subTreeIter.hasNext()) {
								  E subTree = subTreeIter.next();
								  dtCut.addTerm(-subTree.node2.M, gurobi.x[edge.idNoProt]);
								  dtCut.addTerm(-subTree.node2.M, gurobi.x[goodSec.idNoProt]);
								  sumTX -= xValue*subTree.node2.M;
								  sumTX -= getNodeRel(gurobi.x[goodSec.idNoProt])*subTree.node2.M;
								  sumT -= subTree.node2.M;
							  }
						  } else {
							  subTreesUp = subTreesDown;
						  }
					  }
				  }
			  }
		  }

	  } catch (GRBException e) {
		  System.out.println("Error code: " + e.getErrorCode() + ". " +
				  e.getMessage());
		  e.printStackTrace();
	  }

	  return subTreesUp;	  

  }   
  
  protected void addMaximalCut(E edge, double sumX, GRBLinExpr cut) {

	  try {
			
		  double xValue = getNodeRel(gurobi.x[edge.idNoProt]);
		  double newSumX = sumX+xValue;

		  if (newSumX < (1.0f-SolverIflow.env.get(GRB.DoubleParam.IntFeasTol))) {
			  
			  stackEdges.add(edge);
			  
			  Iterator<E> iterStackEdges = stackEdges.iterator();
			  while (iterStackEdges.hasNext()) {		
				  E pathEdge = iterStackEdges.next();
				  cut.addTerm(edge.node2.thetaR, gurobi.x[pathEdge.idNoProt]);				  
			  }
			  
			  Iterator<E> iterEdges = gurobi.inst.net.getG().getOutEdges(edge.node2).iterator();
			  while (iterEdges.hasNext()) {
				  E outEdge = iterEdges.next();
				  //if (!outEdge.prot)
				  if (outEdge.status != SwitchType.PROT)
					  addMaximalCut(outEdge, newSumX, cut);
			  }
			  
			  stackEdges.remove(edge);

		  }

	  } catch (GRBException e) {
		  System.out.println("Error code: " + e.getErrorCode() + ". " +
				  e.getMessage());
		  e.printStackTrace();
	  }

  }     
  
  protected void addEdgesToCut(ArrayList<E> edges, GRBLinExpr cut) {
	  
	  Iterator<E> iterEdges = edges.iterator();
	  
	  while (iterEdges.hasNext()) {
		  E edge = iterEdges.next();
		  cut.addTerm(1.0f, gurobi.x[edge.idNoProt]);
	  }
	  	  
  }
  
//  //Procedimento para a geracao de cortes de cobertura (cover cuts) -- precisa ser aperfeicoado
//	protected void addLiftCoverCut(ArrayList<E> fracEdges, ArrayList<E> zeroEdges, GRBLinExpr cut) {
//	  
//	  int sizeZero = 0, sizeUsed = 0, countCuts = 0;
//	  ArrayList<E> usedEdges = new ArrayList<E>();
//	  ArrayList<E> offCutEdges = new ArrayList<E>();
//	    
//	  try {
//	  	  
//		  this.gurobi.inst.net.decMin(this.gurobi.inst.net.getRoot());
//		  
//		  zeroEdges.addAll(fracEdges);
//		  double decIni = this.gurobi.inst.removeSwitches(zeroEdges);
//		  
//		  do {
//
//			  double dec = decIni; 
//			  
//			  sizeUsed = usedEdges.size();	  
//	  
//			  do {
//				  sizeZero = zeroEdges.size();
//				  			  
//				  for (int i=0;i<zeroEdges.size();i++) {
//					  if ((dec + this.gurobi.inst.putSwitchDeltaDEC(zeroEdges.get(i))) > this.gurobi.maxSwitches) {
//						  dec = this.gurobi.inst.putSwitch(zeroEdges.get(i));
//						  offCutEdges.add(zeroEdges.get(i));
//						  zeroEdges.remove(i--);
////					  } else {
////						  break;
//					  }
//				  }
//				  
//			  } while (zeroEdges.size() < sizeZero);
//			  
//			  if ((countCuts==0)||(offCutEdges.size()>0)) {
//				  cut.clear();
//				  addEdgesToCut(zeroEdges,cut);
//				  addEdgesToCut(usedEdges,cut);
//				  addCut(cut, GRB.GREATER_EQUAL, 1.0f);
//				  countCuts++;
//				  this.gurobi.numCoverCuts++;
//			  }
//			  
//			  usedEdges.addAll(offCutEdges);
//			  this.gurobi.inst.removeSwitches(offCutEdges);
//			  offCutEdges.clear();	
//			  
//		  } while (usedEdges.size() > sizeUsed);
//		  
//	  } catch (GRBException e) {
//		  System.out.println("Error code: " + e.getErrorCode() + ". " +
//				  e.getMessage());
//		  e.printStackTrace();
//	  }
//	  
//	}  
  
}
