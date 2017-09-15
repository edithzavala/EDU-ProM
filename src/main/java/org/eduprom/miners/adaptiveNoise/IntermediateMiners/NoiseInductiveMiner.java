package org.eduprom.miners.adaptiveNoise.IntermediateMiners;

import org.deckfour.xes.model.XLog;
import org.eduprom.exceptions.ConformanceCheckException;
import org.eduprom.exceptions.LogFileNotFoundException;
import org.eduprom.exceptions.ParsingException;
import org.eduprom.miners.IProcessTreeMiner;
import org.eduprom.miners.InductiveMiner;
import org.processmining.log.algorithms.LowFrequencyFilterAlgorithm;
import org.processmining.log.parameters.LowFrequencyFilterParameters;
import org.processmining.plugins.InductiveMiner.mining.MiningParametersIM;
import org.processmining.plugins.InductiveMiner.plugins.IMProcessTree;
import org.processmining.processtree.ProcessTree;
import org.processmining.ptconversions.pn.ProcessTree2Petrinet;

import java.util.ArrayList;
import java.util.List;

public class NoiseInductiveMiner extends InductiveMiner implements IProcessTreeMiner {

	protected double fitness;
	protected double precision;
	protected ProcessTree processTree;


	public NoiseInductiveMiner(String filename, MiningParametersIM parameters) throws LogFileNotFoundException {
		super(filename, parameters);
	}

	@Override
	public ProcessTree mineProcessTree(XLog log) {
		LowFrequencyFilterParameters params = new LowFrequencyFilterParameters(log);
		params.setThreshold(Math.round(getNoiseThreshold()));
		XLog filteredLog = (new LowFrequencyFilterAlgorithm()).apply(getPromPluginContext(), log, params);
		//org.processmining.plugins.log.logfilters.LogFilter.filter()

		logger.info(String.format("Started mining a petri nets using inductive miner, noise: %f", getNoiseThreshold()));
		processTree = IMProcessTree.mineProcessTree(filteredLog, parameters, getCanceller());
		//petrinetWithMarkings = PetrinetHelper.ConvertToPetrinet(processTree);
		return processTree;
	}

	public static NoiseInductiveMiner WithNoiseThreshold(String filename, float noiseThreshold) throws LogFileNotFoundException {
		MiningParametersIM parametersIM = new MiningParametersIM();
		parametersIM.setNoiseThreshold(noiseThreshold);
		//parametersIM.setRepairLifeCycle(true);
		return new NoiseInductiveMiner(filename, parametersIM);
	}

	public static List<NoiseInductiveMiner> WithNoiseThresholds(String filename, float... noiseThreshold) throws LogFileNotFoundException {
		ArrayList<NoiseInductiveMiner> miners = new ArrayList<>();
		for (float threshold: noiseThreshold){
			miners.add(WithNoiseThreshold(filename, threshold));
		}
		return miners;
	}

	@Override
	public void evaluate() throws ConformanceCheckException {
		logger.info("Checking alignment");
		ProcessTree2Petrinet.PetrinetWithMarkings petrinetWithMarkings = this.getDiscoveredPetriNet();
		alignment = petrinetHelper.getAlignment(log, petrinetWithMarkings.petrinet, petrinetWithMarkings.initialMarking, petrinetWithMarkings.finalMarking);
		petrinetHelper.printResults(alignment);

		this.fitness = Double.parseDouble(alignment.getInfo().get("Move-Model Fitness").toString());

		logger.info("Checking precision");
		this.precision = petrinetHelper.getPrecision(log, petrinetWithMarkings.petrinet, alignment, petrinetWithMarkings.initialMarking, petrinetWithMarkings.finalMarking);

		logger.info(String.format("Precision: %S", precision));

		//AlignmentPrecGenRes conformance = petrinetHelper.getConformance(log, petrinetWithMarkings.petrinet, alignment, petrinetWithMarkings.initialMarking, petrinetWithMarkings.finalMarking);
		//petrinetHelper.printResults(conformance);

		//logger.info("Checking Structuredness");
		//double v = new PetriNetStructurednessMetric().compute(promPluginContext, petrinetWithMarkings.petrinet, petrinetWithMarkings.finalMarking);
		//logger.info(String.format("Structuredness: %s", v));
	}

	public double getFitness(){
		return fitness;

	}

	public double getPrecision(){
		return precision;
	}

	public ProcessTree getProcessTree() {
		return processTree;
	}

	public float getNoiseThreshold() {
		return parameters.getNoiseThreshold();
	}

	@Override
	protected void readLog() throws ParsingException {
		XLog log = logHelper.read(filename);
		LowFrequencyFilterParameters params = new LowFrequencyFilterParameters(log);
		params.setThreshold(20);
		this.log = (new LowFrequencyFilterAlgorithm()).apply(getPromPluginContext(), log, params);
	}
}