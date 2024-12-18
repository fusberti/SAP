package models.switches.gurobi;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;

import com.gurobi.gurobi.GRB;
import com.gurobi.gurobi.GRBEnv;
import com.gurobi.gurobi.GRBException;
import com.gurobi.gurobi.GRBLinExpr;
import com.gurobi.gurobi.GRBModel;
import com.gurobi.gurobi.GRBVar;

import edu.uci.ics.jung.graph.Graph;
import instances.Instance;
import instances.networks.edges.E;
import instances.networks.edges.E.SwitchType;
import instances.networks.vertices.V;

public class SolverIflow {

	public Instance inst;
	Graph<V, E> g;

	final char tipoInt = GRB.INTEGER;
	final char tipoFloat = GRB.CONTINUOUS;
	final char tipoBinary = GRB.BINARY;
	public static GRBEnv env;
	public static GRBModel model;
	public GRBVar[][] vars;
	public GRBVar[] x, t, tr, f, dec, dt;
	public int numFlowCuts = 0, numDtCuts = 0, numCoverCuts = 0;
	public long userCutsTime;
	public boolean applyUserFlowCuts = false, applyUserDtCuts = false, applyUserCoverCuts = false;

	public SolverIflow(String filename) {
		this.inst = new Instance(filename);
		this.g = this.inst.net.getG();
	}

	public static void main(String[] args) {

		SolverIflow gurobi = null;
		String instanceName = null;
		int numMaxSwitch = 0;
		double executionTime = 0.0;

		try {
			
			if (args.length == 1) {
				instanceName = args[0];
			} else if (args.length == 2) {
				instanceName = args[0];
				numMaxSwitch = Integer.parseInt(args[1]);
			} else {
				System.err.println(
						"Wrong number of arguments!, use SolverGalias.jar file.switch or SolverGalias.jar file.switch kmax");
				System.exit(1);
			}
			gurobi = new SolverIflow(instanceName);

			if (numMaxSwitch == 0)
				numMaxSwitch = gurobi.inst.net.getG().getEdgeCount();
			System.out.println("instaceName: " + instanceName);

			gurobi.inst.parameters.setInstanceName(instanceName.substring(instanceName.lastIndexOf('/') + 1, instanceName.length()));

			// Open the result file
			String outFileName = "results/iFlow/"
					+ args[0].substring(instanceName.lastIndexOf('/') + 1, instanceName.length()) + ".txt";
			Writer outFile = new BufferedWriter(new FileWriter(outFileName));

			// Creating an environment
			env = new GRBEnv("mip1.log");

			int numSwitch = 0;
			executionTime = 0.0;
			while (numSwitch <= numMaxSwitch && executionTime <= 7200) { // 3600
				// Create an empty model
				model = new GRBModel(env);

				// Configure Gurobi solver model parameters
				new GurobiParameters(model);

				// Store the max number of switch to install on the network
				gurobi.inst.parameters.setNumSwitches(numSwitch);

				// Creting the model of switch allocation
				gurobi.populateNewModel(model);

				// Write model to file
				model.write(gurobi.inst.parameters.getInstanceName() + "_solveriflows.lp");

				// long time = System.nanoTime();

				model.optimize();
				// model.tune();

				executionTime += model.get(GRB.DoubleAttr.Runtime);

				//write solution to the file
				model.write(gurobi.inst.parameters.getInstanceName() + "_solveriflows.sol");

				System.out.println("\n=======================");
				System.out.println("Instance: " + gurobi.inst.parameters.getInstanceName());
				System.out.println("Obj " + gurobi.inst.parameters.getOBJ() + ": " + model.get(GRB.DoubleAttr.ObjVal));
				System.out.println("GAP: " + model.get(GRB.DoubleAttr.MIPGap) + " %");
				System.out.println("Execution Time: " + model.get(GRB.DoubleAttr.Runtime) + " (s)");

				System.out.println("maxSwitches " + gurobi.inst.parameters.getNumSwitches());

				outFile.write(String.format("k = %d \t ENS = %.2f :", numSwitch, (model.get(GRB.DoubleAttr.ObjVal)))
						.replace(",", "."));

				// Show the solution
				// System.out.println("Solution:");
				Iterator<E> iterE = gurobi.g.getEdges().iterator();
				while (iterE.hasNext()) {
					E edge = iterE.next();
					if (edge.idNoProt != -1) {
						if (gurobi.x[edge.idNoProt].get(GRB.DoubleAttr.X) > 0.1f) {
							outFile.write(" (" + edge.node1.label + "," + edge.node2.label + "),");
						}
					}

				}
				// System.out.print("\n");

				outFile.write(
						String.format("  time: %.3f secs\n", model.get(GRB.DoubleAttr.Runtime)).replace(",", "."));

				outFile.flush();

				executionTime += model.get(GRB.DoubleAttr.Runtime);

				double DECsol = 0.0f;
				Iterator<E> iterEdges = gurobi.g.getEdges().iterator();
				while (iterEdges.hasNext()) {

					E edge = iterEdges.next();

					if (edge.status != E.SwitchType.PROT) {
						// imprime as posicoes das chaves alocadas
						if (gurobi.x[edge.idNoProt].get(GRB.DoubleAttr.X) > 0.1f) {
							// System.out.println(gurobi.x[edge.idNoProt].get(GRB.StringAttr.VarName)+"
							// "+gurobi.x[edge.idNoProt].get(GRB.DoubleAttr.X));

						}

						// calcula o DEC da solucao
						if (gurobi.f[edge.idNoProt].get(GRB.DoubleAttr.X) > 0.0f) {
							DECsol += gurobi.f[edge.idNoProt].get(GRB.DoubleAttr.X)
									* (double) (edge.node1.clientsSum - edge.node2.clientsSum)
									/ (double) gurobi.inst.net.getRoot().clientsSum;
						}
					}

				}
				DECsol += (gurobi.inst.reliability.getNtheta() + gurobi.inst.reliability.getSumNFl())
						/ gurobi.inst.net.getRoot().clientsSum;
				System.out.println("DEC solucao = " + DECsol);

				System.out.println("Numero de clientes = " + (double) gurobi.inst.net.getRoot().clientsSum);
				System.out.println("Total power load = " + (double) gurobi.inst.net.getRoot().demandSum + " (kW)");

				model.dispose();

				numSwitch++;

				System.out.println("\n=======================\n");
			}

			// outFile.write("Total time: "+executionTime+" secs");
			outFile.write(String.format("Total time: %.3f secs", executionTime).replace(",", "."));

			System.out.println("Total time: " + executionTime + " secs");

			// Dispose of model and environment
			// model.dispose();
			env.dispose();

			outFile.close();			

		} catch (GRBException e) {
			System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
		} catch (IOException e) {
			System.err.println("Erro leitura da instancia");
		}

	}

	// variaveis tipo X -- xij = 1 indica que o arco (i,j) contem uma chave
	private void defineVarX(GRBModel model, int indVar, GRBLinExpr ofexpr) {
		// variaveis tipo X
		vars[indVar] = new GRBVar[inst.net.getNumNoProt()];
		x = vars[indVar];

		try {

			Iterator<E> iterEdges = g.getEdges().iterator();
			while (iterEdges.hasNext()) {
				E edge = iterEdges.next();
				if (edge.status != SwitchType.PROT) {
					double objvalsX = 0.0f;
					switch (inst.parameters.getOBJ()) {
					case COST:
						objvalsX = inst.parameters.getUnitSwitchCost();
						break;
					case ENS:
						objvalsX = 0.0f;
					case SAIDI:
						objvalsX = 0.0f;
					}
					double lbX = 0.0;
					double ubX = 1.0;

					x[edge.idNoProt] = model.addVar(lbX, ubX, 0.0f, tipoBinary,
							"x[" + edge.node1.label + "," + edge.node2.label + "]");
					ofexpr.addTerm(objvalsX, x[edge.idNoProt]);

				}
			}

		} catch (GRBException e) {
			System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
		}

	}

	// variaveis tipo F -- fij indica que o fluxo de tempo de interrupcao no arco
	// (i,j)
	private void defineVarF(GRBModel model, int indVar, GRBLinExpr ofexpr) {

		// variaveis tipo F
		vars[indVar] = new GRBVar[inst.net.getNumNoProt()];
		f = vars[indVar];

		try {

			Iterator<E> iterEdges = g.getEdges().iterator();
			while (iterEdges.hasNext()) {
				E edge = iterEdges.next();
				if (edge.status != SwitchType.PROT) {
					double objvalsF = 0.0f;
					switch (inst.parameters.getOBJ()) {
					case COST:
						objvalsF = inst.parameters.getKwhCost() * (edge.node1.demandSum - edge.node2.demandSum) / 1000;
						break;
					case ENS:
						objvalsF = edge.node1.demandSum - edge.node2.demandSum;
						break;
					case SAIDI:
						objvalsF = ((double) (edge.node1.clientsSum - edge.node2.clientsSum)
								/ (double) inst.net.getRoot().clientsSum);
						break;
					}
					double lbF = 0.0;
					double ubF = Double.MAX_VALUE;
					f[edge.idNoProt] = model.addVar(lbF, ubF, 0.0f, tipoFloat,
							"f[" + edge.node1.label + "," + edge.node2.label + "]");
					ofexpr.addTerm(objvalsF, f[edge.idNoProt]);
				}
			}

		} catch (GRBException e) {
			System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
		}

	}

	// variaveis tipo DT -- dt_ij indica que o tempo de interrupcao que sera evitado
	// por meio de transferencias
	// por manobras de chaves normalmente abertas caso uma chave seccionadora seja
	// alocada no arco (i,j).
	private void defineVarDT(GRBModel model, int indVar, GRBLinExpr ofexpr) {

		// variaveis tipo dt
		vars[indVar] = new GRBVar[inst.net.getTransferSecs().size()];
		dt = vars[indVar];

		try {

			Iterator<E> iterEdges = inst.net.getTransferSecs().iterator();
			while (iterEdges.hasNext()) {
				E edge = iterEdges.next();
				double objvalsDT = 0.0f;
				switch (inst.parameters.getOBJ()) {
				case COST:
					objvalsDT = -inst.parameters.getKwhCost() * edge.node2.demandSum / 1000;
					break;
				case ENS:
					objvalsDT = -edge.node2.demandSum;
					break;
				case SAIDI:
					objvalsDT = ((double) (-edge.node2.clientsSum) / (double) inst.net.getRoot().clientsSum);
					break;
				}
				double lbDT = 0.0;
				double ubDT = Double.MAX_VALUE;
				dt[edge.idGoodSec] = model.addVar(lbDT, ubDT, 0.0f, tipoFloat,
						"dt[" + edge.node1.label + "," + edge.node2.label + "]");
				ofexpr.addTerm(objvalsDT, dt[edge.idGoodSec]);
			}

		} catch (GRBException e) {
			System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
		}

	}

	// RESTRICAO (0): sum X <= numMax
	private void addConstraint0(GRBModel model) {

		try {

			GRBLinExpr constraint = new GRBLinExpr();

			Iterator<E> iterEdges = g.getEdges().iterator();
			while (iterEdges.hasNext()) {
				E edge = iterEdges.next();
				if (edge.status != SwitchType.PROT)
					constraint.addTerm(1, x[edge.idNoProt]);
			}

			model.addConstr(constraint, GRB.LESS_EQUAL, inst.parameters.getNumSwitches(), "c0");

		} catch (GRBException e) {
			System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
		}

	}

	// RESTRICAO (1): fij >= theta_j + sum_{(j,k) \in A}(fjk) - Mxij (all (i,j) in
	// E)
	private void addConstraint1(GRBModel model) {

		try {

			GRBLinExpr constraint = new GRBLinExpr();

			Iterator<E> iterEdges = g.getEdges().iterator();
			while (iterEdges.hasNext()) {

				E edge = iterEdges.next();

				if (edge.status != SwitchType.PROT) {

					constraint.clear();
					constraint.addTerm(1.0, f[edge.idNoProt]);

					constraint.addTerm(edge.node2.M, x[edge.idNoProt]);

					// arestas saindo do no
					Iterator<E> iterEdgesOut = g.getOutEdges(edge.node2).iterator();
					while (iterEdgesOut.hasNext()) {
						E outEdge = iterEdgesOut.next();
						if (outEdge.status != SwitchType.PROT)
							constraint.addTerm(-1.0, f[outEdge.idNoProt]);
					}
					model.addConstr(constraint, GRB.GREATER_EQUAL, edge.node2.thetaR,
							"c1[" + edge.node1.label + "," + edge.node2.label + "]");

				}

			}

		} catch (GRBException e) {
			System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
		}

	}

	// RESTRICAO (2): SAIDI <= SAIDImax
	private void addConstraint2(GRBModel model) {

		try {

			GRBLinExpr constraint = new GRBLinExpr();

			Iterator<E> iterEdges = g.getEdges().iterator();
			while (iterEdges.hasNext()) {
				E edge = iterEdges.next();
				if (edge.status != SwitchType.PROT)
					constraint.addTerm((double) (edge.node1.clientsSum - edge.node2.clientsSum)
							/ (double) inst.net.getRoot().clientsSum, f[edge.idNoProt]);
			}
			model.addConstr(constraint, GRB.LESS_EQUAL,
					inst.parameters.getLimSAIDI() - (inst.reliability.getSumNFl() + inst.reliability.calcNtheta())
							/ (double) inst.net.getRoot().clientsSum,
					"c2");

		} catch (GRBException e) {
			System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
		}

	}

	// RESTRICAO (3): ENS <= ENSmax
	private void addConstraint3(GRBModel model) {

		try {

			GRBLinExpr constraint = new GRBLinExpr();

			Iterator<E> iterEdges = g.getEdges().iterator();
			while (iterEdges.hasNext()) {
				E edge = iterEdges.next();
				if (edge.status != SwitchType.PROT)
					constraint.addTerm((double) (edge.node1.demandSum - edge.node2.demandSum), f[edge.idNoProt]);
			}
			model.addConstr(constraint, GRB.LESS_EQUAL,
					inst.parameters.getLimENS() - (inst.reliability.calcSumPFl() + inst.reliability.calcPtheta()),
					"c3");

		} catch (GRBException e) {
			System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
		}

	}

	// RESTRICAO (4): sum{(u,v) \in path(0,j)}{dtuv} <= ti - fij (all (i,j) in
	// goodSecs)
	// OBS: testar sum{(u,v) \in path(0,j)}{dtuv} <= ti - Fj (errado)
	private void addConstraint4(GRBModel model) {

		try {

			GRBLinExpr constraint = new GRBLinExpr();

			Iterator<E> goodSecs = inst.net.getTransferSecs().iterator();
			while (goodSecs.hasNext()) {

				E goodSec = goodSecs.next();

				constraint.clear();
				constraint.addTerm(1.0f, dt[goodSec.idGoodSec]); // + dtij
				constraint.addTerm(1.0f, f[goodSec.idNoProt]); // + fij

				V predNode1 = goodSec.node1;

				double sumTheta = 0.0f;

				// fij - sum_{(j,k) \in A}{fjk}
				while (predNode1 != inst.net.getRoot()) {

					sumTheta += predNode1.thetaR;

					Iterator<E> sucEdges = g.getOutEdges(predNode1).iterator();

					while (sucEdges.hasNext()) {

						E sucEdge = sucEdges.next();
						if (sucEdge.status != E.SwitchType.PROT)
							constraint.addTerm(-1.0f, f[sucEdge.idNoProt]); // - sum_{(j,k) \in A}{fjk}

					}

					Iterator<E> predEdges = g.getInEdges(predNode1).iterator();
					E predEdge = predEdges.next();
					if ((predEdge.node1 != inst.net.getRoot()) && (predEdge.status != E.SwitchType.PROT))
						constraint.addTerm(1.0f, f[predEdge.idNoProt]); // + fij

					predNode1 = predEdge.node1;

				}

				V predNode2 = goodSec.node1;

				while (predNode2 != inst.net.getRoot()) {

					Iterator<E> predEdges = g.getInEdges(predNode2).iterator();
					E predEdge = predEdges.next();
					if (inst.net.getTransferSecs().contains(predEdge))
						constraint.addTerm(1.0f, dt[predEdge.idGoodSec]); // sum{(u,v) \in path(0,i)}{dtuv}
					predNode2 = predEdge.node1;

				}

				model.addConstr(constraint, GRB.LESS_EQUAL, sumTheta,
						"c4[" + goodSec.node1.label + "," + goodSec.node2.label + "]"); // >= sumTheta

			}

		} catch (GRBException e) {
			System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
		}

	}

	// RESTRICAO (5): dt <= M*xij (all (i,j) in goodSecs)
	private void addConstraint5(GRBModel model) {

		try {

			GRBLinExpr constraint = new GRBLinExpr();

			Iterator<E> goodSecs = inst.net.getTransferSecs().iterator();
			while (goodSecs.hasNext()) {

				E goodSec = goodSecs.next();
				constraint.clear();
				constraint.addTerm(1.0, dt[goodSec.idGoodSec]);
				constraint.addTerm(-(goodSec.node1.subNode.M - goodSec.node2.M), x[goodSec.idNoProt]);
				model.addConstr(constraint, GRB.LESS_EQUAL, 0.0f,
						"c5[" + goodSec.node1.label + "," + goodSec.node2.label + "]");

			}

		} catch (GRBException e) {
			System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
		}

	}

	private void populateNewModel(GRBModel model) {

		try {

			GRBLinExpr ofexpr = new GRBLinExpr();

			// Create variables
			vars = new GRBVar[3][];
			this.defineVarX(model, 0, ofexpr);
			this.defineVarF(model, 1, ofexpr);
			// se considera transferencia por chaves de manobra
			if (this.inst.parameters.isTransfer())
				this.defineVarDT(model, 2, ofexpr);

			// Integrate new variables
			model.update();

			// Create objective function expression
			switch (inst.parameters.getOBJ()) {
			case COST:
				ofexpr.addConstant(
						inst.parameters.getKwhCost() * (inst.reliability.getPtheta() + inst.reliability.getSumPFl()));
				break;
			case ENS:
				ofexpr.addConstant(inst.reliability.getPtheta() + inst.reliability.getSumPFl());
				break;
			case SAIDI:
				ofexpr.addConstant(
						(inst.reliability.getNtheta() + inst.reliability.getSumNFl()) / inst.net.getRoot().clientsSum);
				break;
			}
			model.setObjective(ofexpr);

			// Constraint (0): sum X <= numMax
			this.addConstraint0(model);

			// Constraint (1): fij >= theta_j + sum_{(j,k) \in A}(fjk) - Mxij (all (i,j) in
			// E)
			this.addConstraint1(model);

			// if the SAIDI is bounded
			if (this.inst.parameters.getLimSAIDI() < Double.MAX_VALUE) {

				// Constraint (4): sum{(u,v) \in path(0,j)}{dtuv} <= ti - fij (all (i,j) in
				// goodSecs)
				this.addConstraint2(model);

			}

			// if the ENS is bounded
			if (this.inst.parameters.getLimENS() < Double.MAX_VALUE) {

				// Constraint (4): sum{(u,v) \in path(0,j)}{dtuv} <= ti - fij (all (i,j) in
				// goodSecs)
				this.addConstraint3(model);

			}

			// if there is load transfer through tie lines
			if (this.inst.parameters.isTransfer()) {

				// Constraint (4): sum{(u,v) \in path(0,j)}{dtuv} <= ti - fij (all (i,j) in
				// goodSecs)
				this.addConstraint4(model);

				// Constraint (5): dt <= M*xij (all (i,j) in goodSecs)
				this.addConstraint5(model);

			}

			model.update();

		} catch (GRBException e) {
			System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
		}

	}

}
