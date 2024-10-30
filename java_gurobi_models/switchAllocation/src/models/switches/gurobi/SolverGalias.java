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

public class SolverGalias {

	public Instance inst;
	Graph<V, E> g;

	final char tipoInt = GRB.INTEGER;
	final char tipoFloat = GRB.CONTINUOUS;
	final char tipoBinary = GRB.BINARY;
	public static GRBEnv env;
	public static GRBModel model;
	public GRBVar[] x, z[];

	public SolverGalias(String filename) {
		this.inst = new Instance(filename);
		this.g = this.inst.net.getG();
	}

	public static void main(String[] args) {

		SolverGalias gurobi = null;
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
			gurobi = new SolverGalias(instanceName);// opening network file .switch
			if (numMaxSwitch == 0)
				numMaxSwitch = gurobi.inst.net.getG().getEdgeCount();
						
			System.out.println("instaceName: " + instanceName);

			
			gurobi.inst.parameters.setInstanceName(instanceName.substring(instanceName.lastIndexOf('/') + 1, instanceName.length()));

			// Open the result file
			String outFileName = "results/Galias/"
					+ instanceName.substring(instanceName.lastIndexOf('/') + 1, instanceName.length()) + ".txt";
			System.out.println("outFile: " + outFileName);
			Writer outFile = new BufferedWriter(new FileWriter(outFileName));

			env = new GRBEnv("mip1.log");

			int numSwitch = 0;
			executionTime = 0.0;
			while (numSwitch <= numMaxSwitch && executionTime <= 7200) { // 3600

				model = new GRBModel(env);

				// Configure Gurobi model parameters
				new GurobiParameters(model);

				// Store the max number of switch to install on the network
				gurobi.inst.parameters.setNumSwitches(numSwitch);

				gurobi.populateNewModel(model);

				// Write model to file
				model.write(gurobi.inst.parameters.getInstanceName() + "_solvergalias.lp");
				
				model.optimize();

				executionTime += model.get(GRB.DoubleAttr.Runtime);

				//write solution to the file
				model.write(gurobi.inst.parameters.getInstanceName() + "_solvergalias.sol");

				System.out.println("\n=======================");
				System.out.println("Instance: " + gurobi.inst.parameters.getInstanceName());
				System.out.println("Obj " + gurobi.inst.parameters.getOBJ() + ": " + model.get(GRB.DoubleAttr.ObjVal));
				System.out.println("GAP: " + model.get(GRB.DoubleAttr.MIPGap) + " %");
				System.out.println("Execution Time: " + model.get(GRB.DoubleAttr.Runtime) + " (s)");

				System.out.println("maxSwitches " + gurobi.inst.parameters.getNumSwitches());

				outFile.write(String.format("k = %d \t ENS = %.6f :", numSwitch, (model.get(GRB.DoubleAttr.ObjVal)))
						.replace(",", "."));

				Iterator<E> iterE = gurobi.g.getEdges().iterator();
				while (iterE.hasNext()) {
					E edge = iterE.next();
					if (edge.idNoProt != -1) {
						if (gurobi.x[edge.idNoProt].get(GRB.DoubleAttr.X) > 0.1f) {
							outFile.write(" (" + edge.node1.label + "," + edge.node2.label + "),");
						}
					}

				}
				outFile.write(
						String.format("  time: %.6f secs\n", model.get(GRB.DoubleAttr.Runtime)).replace(",", "."));
				outFile.flush();

				int countSwitches = 0;
				Iterator<E> iterEdges = gurobi.g.getEdges().iterator();
				while (iterEdges.hasNext()) {

					E edge = iterEdges.next();

					if (edge.status != E.SwitchType.PROT) {
						// imprime as posicoes das chaves alocadas
						if (gurobi.x[edge.idNoProt].get(GRB.DoubleAttr.X) > 0.1f) {
							countSwitches++;

						}

					}

				}

				System.out.println("Num. chaves = " + countSwitches);

				// Dispose of model and environment
				model.dispose();
				numSwitch++;

				System.out.println("\n=======================\n");
			}

			// outFile.write("Total time: "+executionTime+" secs");
			outFile.write(String.format("Total time: %.6f secs", executionTime).replace(",", "."));
			System.out.println("Total time: " + executionTime + " secs");

			env.dispose();
			outFile.close();
			
		} catch (GRBException e) {
			System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
		} catch (IOException e) {
			System.err.println("Error in file: " + e.getMessage());
		}

	}

	// variaveis tipo X -- xij = 1 indica que o arco (i,j) contem uma chave
	private void defineVarX(GRBModel model, GRBLinExpr ofexpr) {

		// variaveis tipo X
		x = new GRBVar[inst.net.getNumNoProt()];

		try {

			Iterator<E> iterEdges = g.getEdges().iterator();
			while (iterEdges.hasNext()) {
				E edge = iterEdges.next();
				if (edge.status != SwitchType.PROT) {
					double objvalsX = 0.0f;
					switch (inst.parameters.getOBJ()) {
					case ENS:
						objvalsX = -(inst.net.getRoot().demandSum - edge.node2.demandSum) * edge.node2.thetaTil;
					default:
						break;
					}
					double lbX = 0.0;
					double ubX = 1.0;
					x[edge.idNoProt] = model.addVar(lbX, ubX, 0.0f, tipoBinary, "x[" + edge.id + "]");
					ofexpr.addTerm(objvalsX, x[edge.idNoProt]);

				}
			}

		} catch (GRBException e) {
			System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
		}

	}

	// variaveis tipo Z -- ze1e2 = 1 indica que os dois arcos e1 e2 possuem chaves
	private void defineVarZ(GRBModel model, GRBLinExpr ofexpr) {

		// variaveis tipo X
		z = new GRBVar[inst.net.getNumNoProt()][inst.net.getNumNoProt()];

		try {

			Iterator<E> iterEdges = g.getEdges().iterator();
			while (iterEdges.hasNext()) {
				E edge = iterEdges.next();
				if (edge.status != SwitchType.PROT) {
					Iterator<E> iterEdges2 = g.getEdges().iterator();
					while (iterEdges2.hasNext()) {
						E edge2 = iterEdges2.next();
						if (edge2.status != SwitchType.PROT) {
							// if (edge != edge2 && edge.node2 != edge2.node1 &&
							// inst.net.isAncestor(edge.node2, edge2.node1)) {
							if (edge != edge2 && inst.net.isAncestor(edge.node2, edge2.node1)) {

								double objvalsX = 0.0f;
								switch (inst.parameters.getOBJ()) {
								case ENS:
									objvalsX = inst.net.getRoot().demandSum;
									objvalsX = edge.node2.demandSum;
									objvalsX = edge2.node2.thetaTil;
									objvalsX = (inst.net.getRoot().demandSum - edge.node2.demandSum);
									objvalsX *= edge2.node2.thetaTil;
									objvalsX = (inst.net.getRoot().demandSum - edge.node2.demandSum)
											* edge2.node2.thetaTil;
								default:
									break;
								}
								double lbX = 0.0;
								double ubX = 1.0;
								z[edge.idNoProt][edge2.idNoProt] = model.addVar(lbX, ubX, 0.0f, tipoBinary,
										"z[" + edge.id + "," + edge2.id + "]");
								ofexpr.addTerm(objvalsX, z[edge.idNoProt][edge2.idNoProt]);
							}
						}
					}
				}
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

	// RESTRICAO (1): Z = Xe1 Xe2 Prod (1 - Xpath(e1,e2))
	private void addConstraint1(GRBModel model) {

		Iterator<E> iterEdges = g.getEdges().iterator();
		while (iterEdges.hasNext()) {

			E edge = iterEdges.next();
			if (edge.status != SwitchType.PROT)
				findSuccessorsForConstraint1(edge, edge.node2);

		}

	}

	public void findSuccessorsForConstraint1(E rootEdge, V endNode) {

		try {

			E endEdge = g.getInEdges(endNode).iterator().next();

			// if (rootEdge.node2 != endNode && rootEdge != endEdge) {
			if (rootEdge != endEdge) {

				GRBLinExpr constraint = new GRBLinExpr();
				constraint.addTerm(1, x[rootEdge.idNoProt]);
				constraint.addTerm(1, x[endEdge.idNoProt]);

				// path edges from rootEdge to endEdge
				V pathNode = inst.net.pred(endNode);
				int countPath = 0;
				while (pathNode != rootEdge.node2) {
					E pathEdge = g.getInEdges(pathNode).iterator().next();
					constraint.addTerm(-1, x[pathEdge.idNoProt]);
					pathNode = inst.net.pred(pathNode);
					countPath++;
				}

				// if (countPath > 0) {
				GRBLinExpr constraint1 = new GRBLinExpr(constraint);
				constraint1.addTerm(-(countPath + 2), z[rootEdge.idNoProt][endEdge.idNoProt]);
				model.addConstr(constraint1, GRB.GREATER_EQUAL, -countPath, "c1a" + rootEdge.id + "," + endEdge.id);

				GRBLinExpr constraint2 = new GRBLinExpr(constraint);
				constraint2.addTerm(-1, z[rootEdge.idNoProt][endEdge.idNoProt]);
				model.addConstr(constraint2, GRB.LESS_EQUAL, 1, "c2a" + rootEdge.id + "," + endEdge.id);
				// }
			}

			Iterator<V> iterNodes = g.getSuccessors(endNode).iterator();

			while (iterNodes.hasNext()) {

				V suc = iterNodes.next();
				findSuccessorsForConstraint1(rootEdge, suc);

			}

		} catch (GRBException e) {
			System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
		}

	}

	private void populateNewModel(GRBModel model) {

		try {

			GRBLinExpr ofexpr = new GRBLinExpr();

			// Create variables
			this.defineVarX(model, ofexpr);
			this.defineVarZ(model, ofexpr);

			// Integrate new variables
			model.update();

			// Create objective function expression
			switch (inst.parameters.getOBJ()) {
			case ENS:
				ofexpr.addConstant(inst.net.getRoot().demandSum * inst.net.getRoot().thetaTil);
				break;
			default:
				break;
			}
			model.setObjective(ofexpr);

			// Constraint (0): sum X <= numMax
			this.addConstraint0(model);

			// Constraint (1): Z = Xe1 Xe2 Prod (1 - Xpath(e1,e2))
			this.addConstraint1(model);

			model.update();

		} catch (GRBException e) {
			System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
		}

	}

	public Instance getInst() {
		return inst;
	}

}
