package instances;

import reliability.Reliability;

import instances.networks.Network;
import instances.networks.reductions.Reductions;

public class Instance {
	
	public Network net;
	public InstanceParameters parameters;
	public Reliability reliability;
	public Reductions reduce;
	
	public Instance(String filename) {
		
		//setting instance parameters
		parameters = new InstanceParameters(filename);
		
		//setting instance network
		net = new Network(parameters.getInstanceName());
		
		reliability = new Reliability(this);
		
	}
	

}
