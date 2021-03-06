/*
 * Kyle Wong
 * 14.3.6
 * Machine Learning
 * kwong23
 * Assignment 3
 */
package cs475;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

public class Classify {
	static public LinkedList<Option> options = new LinkedList<Option>();
	
	
	public static void main(String[] args) throws IOException {
		// Parse the command line.
		final int UNINITIALIZED = -1;
		String[] manditory_args = { "mode"};
		createCommandLineOptions();
		CommandLineUtilities.initCommandLineParameters(args, Classify.options, manditory_args);
	
		String mode = CommandLineUtilities.getOptionValue("mode");
		String data = CommandLineUtilities.getOptionValue("data");
		String predictions_file = CommandLineUtilities.getOptionValue("predictions_file");
		String algorithm = CommandLineUtilities.getOptionValue("algorithm");
		String model_file = CommandLineUtilities.getOptionValue("model_file");
		int gd_iterations = 20;
		if (CommandLineUtilities.hasArg("gd_iterations"))
			gd_iterations = CommandLineUtilities.getOptionValueAsInt("gd_iterations");
		double gd_eta = .01;
		if (CommandLineUtilities.hasArg("gd_eta"))
			gd_eta = CommandLineUtilities.getOptionValueAsFloat("gd_eta");
		int num_features = UNINITIALIZED;
		if (CommandLineUtilities.hasArg("num_features_to_select"))
			num_features = CommandLineUtilities.getOptionValueAsInt("num_features_to_select");
		double online_learning_rate = 1.0;
		if (CommandLineUtilities.hasArg("online_learning_rate"))
			online_learning_rate = CommandLineUtilities.getOptionValueAsFloat("online_learning_rate");
		double polynomial_kernel_exponent = 2;
		if (CommandLineUtilities.hasArg("polynomial_kernel_exponent"))
			polynomial_kernel_exponent = CommandLineUtilities.getOptionValueAsFloat("polynomial_kernel_exponent");
		int online_training_iterations = 5;
		if (CommandLineUtilities.hasArg("online_training_iterations"))
			online_training_iterations = CommandLineUtilities.getOptionValueAsInt("online_training_iterations");
			
//		for(int i = 0; i < args.length; i++){
//			System.out.print(args[i] + " ");
//		}
//		System.out.println();
		
		if (mode.equalsIgnoreCase("train")) {
			if (data == null || algorithm == null || model_file == null) {
				System.out.println("Train requires the following arguments: data, algorithm, model_file");
				System.exit(0);
			}
			// Load the training data.
			DataReader data_reader = new DataReader(data, true);
			List<Instance> instances = data_reader.readData();
			data_reader.close();
			
			// Train the model.
			TrainParameter params = new TrainParameter(instances, algorithm, gd_eta, num_features, gd_iterations,
					online_learning_rate, online_training_iterations, polynomial_kernel_exponent);
			Predictor predictor = train(params);
//			System.out.println(params);
			saveObject(predictor, model_file);		
			
		} else if (mode.equalsIgnoreCase("test")) {
			if (data == null || predictions_file == null || model_file == null) {
				System.out.println("Train requires the following arguments: data, predictions_file, model_file");
				System.exit(0);
			}
			
			// Load the test data.
			DataReader data_reader = new DataReader(data, true);
			List<Instance> instances = data_reader.readData();
			data_reader.close();
			
			// Load the model.
			Predictor predictor = (Predictor)loadObject(model_file);
			evaluateAndSavePredictions(predictor, instances, predictions_file);
		} else {
			System.out.println("Requires mode argument.");
		}
	}
	
	/**
	 * Nested parameter object class for passing parameters to Classifiers.
	 * @author KT Wong
	 *
	 */
	public static class TrainParameter {
		private List<Instance> instances;
		private String algorithm;
		private double eta;
		private int num_features;
		private int num_iters;
		private double online_learning_rate;
		private int online_training_iterations;
		private double polynomial_kernel_exponent;

		public TrainParameter(List<Instance> instances, String algorithm,
				double eta, int num_features, int num_iters,
				double online_learning_rate, int online_training_iterations,
				double polynomial_kernel_exponent) {
			this.instances = instances;
			this.algorithm = algorithm;
			this.eta = eta;
			this.num_features = num_features;
			this.num_iters = num_iters;
			this.online_learning_rate = online_learning_rate;
			this.online_training_iterations = online_training_iterations;
			this.polynomial_kernel_exponent = polynomial_kernel_exponent;
		}

		public List<Instance> getInstances() {
			return instances;
		}

		public void setInstances(List<Instance> instances) {
			this.instances = instances;
		}

		public String getAlgorithm() {
			return algorithm;
		}

		public void setAlgorithm(String algorithm) {
			this.algorithm = algorithm;
		}

		public double getEta() {
			return eta;
		}

		public void setEta(double eta) {
			this.eta = eta;
		}

		public int getNum_features() {
			return num_features;
		}

		public void setNum_features(int num_features) {
			this.num_features = num_features;
		}

		public int getNum_iters() {
			return num_iters;
		}

		public void setNum_iters(int num_iters) {
			this.num_iters = num_iters;
		}

		public double getOnline_learning_rate() {
			return online_learning_rate;
		}

		public void setOnline_learning_rate(double online_learning_rate) {
			this.online_learning_rate = online_learning_rate;
		}

		public int getOnline_training_iterations() {
			return online_training_iterations;
		}

		public void setOnline_training_iterations(int online_training_iterations) {
			this.online_training_iterations = online_training_iterations;
		}
		
		public double getPolynomial_kernel_exponent(){
			return polynomial_kernel_exponent;
		}
		public void setPolynomial_kernel_exponent(double polynomial_kernel_exponent) {
			this.polynomial_kernel_exponent = polynomial_kernel_exponent;
		}
		
		public String toString(){
//			return instances + " " + 
			return algorithm + " eta: " + eta + " numfeatures: " + num_features + 
					" num_iters: " + num_iters + " online_learning_rate: " + 
					online_learning_rate + "\nonline_training_iterations: " + 
					online_training_iterations + " polynomial_kernel_exponent: " +
					polynomial_kernel_exponent;
		}
	}

	private static Predictor train(TrainParameter params) {
		// TODO Train the model using "algorithm" on "data"
		// TODO Evaluate the model
		Predictor predictor = null;
		AccuracyEvaluator evaluator = new AccuracyEvaluator();
		double accuracy;
		if(params.getAlgorithm().equalsIgnoreCase("majority")){
			predictor = new MajorityClassifier();
		}
		else if(params.getAlgorithm().equalsIgnoreCase("even_odd")){
			predictor = new EvenOddClassifier();
		}
		else if(params.getAlgorithm().equalsIgnoreCase("logistic_regression")){
			predictor = new LogisticClassifier(params.getEta(), params.getNum_features(), params.getNum_iters());
		}
		else if(params.getAlgorithm().equalsIgnoreCase("margin_perceptron")){
			predictor = new MarginPerceptron(params.getOnline_learning_rate(), params.getOnline_training_iterations());
		}
		else if(params.getAlgorithm().equalsIgnoreCase("perceptron_linear_kernel")){
			predictor = new DualMarginPerceptron(params.getOnline_learning_rate(), 
					params.getOnline_training_iterations(), params.getAlgorithm(), 
					params.getPolynomial_kernel_exponent());
		}
		else if(params.getAlgorithm().equalsIgnoreCase("perceptron_polynomial_kernel")){
			predictor = new DualMarginPerceptron(params.getOnline_learning_rate(), 
					params.getOnline_training_iterations(), params.getAlgorithm(), 
					params.getPolynomial_kernel_exponent());
		}
		else if(params.getAlgorithm().equalsIgnoreCase("mira")){
			predictor = new MiraPerceptron(params.getOnline_training_iterations());
		}
		predictor.train(params.getInstances());
//		System.out.printf("Testing %s Accuracy\n", predictor);
		System.out.print("train data: ");
		accuracy = evaluator.evaluateAccuracy(params.getInstances(), predictor);
		return (Predictor) predictor;
	}

	private static void evaluateAndSavePredictions(Predictor predictor,
			List<Instance> instances, String predictions_file) throws IOException {
		PredictionsWriter writer = new PredictionsWriter(predictions_file);
		// TODO Evaluate the model if labels are available. 
		double accuracy;
		if(instances.get(0).getLabel() != null){
			AccuracyEvaluator evaluator = new AccuracyEvaluator();
//			System.out.printf("Testing %s Accuracy\n", predictor);
			System.out.print("dev data: ");
			accuracy = evaluator.evaluateAndPrintAccuracy(instances, predictor, writer);
		}
		else{ //courtesy information message
			System.out.println("Test data therefore accuracy cannot be calculated.");
		}
		
//		for (Instance instance : instances) {
//			Label label = predictor.predict(instance);
//			writer.writePrediction(label);
//		}
		
		writer.close();
		
	}

	public static void saveObject(Object object, String file_name) {
		try {
			ObjectOutputStream oos =
				new ObjectOutputStream(new BufferedOutputStream(
						new FileOutputStream(new File(file_name))));
			oos.writeObject(object);
			oos.close();
		}
		catch (IOException e) {
			System.err.println("Exception writing file " + file_name + ": " + e);
		}
	}

	/**
	 * Load a single object from a filename. 
	 * @param file_name
	 * @return
	 */
	public static Object loadObject(String file_name) {
		ObjectInputStream ois;
		try {
			ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(new File(file_name))));
			Object object = ois.readObject();
			ois.close();
			return object;
		} catch (IOException e) {
			System.err.println("Error loading: " + file_name);
		} catch (ClassNotFoundException e) {
			System.err.println("Error loading: " + file_name);
		}
		return null;
	}
	
	public static void registerOption(String option_name, String arg_name, boolean has_arg, String description) {
		OptionBuilder.withArgName(arg_name);
		OptionBuilder.hasArg(has_arg);
		OptionBuilder.withDescription(description);
		Option option = OptionBuilder.create(option_name);
		
		Classify.options.add(option);		
	}
	
	private static void createCommandLineOptions() {
		registerOption("data", "String", true, "The data to use.");
		registerOption("mode", "String", true, "Operating mode: train or test.");
		registerOption("predictions_file", "String", true, "The predictions file to create.");
		registerOption("algorithm", "String", true, "The name of the algorithm for training.");
		registerOption("model_file", "String", true, "The name of the model file to create/load.");
		registerOption("gd_eta", "int", true, "The step size parameter for GD.");
		registerOption("gd_iterations", "int", true, "The number of GD iterations.");
		registerOption("num_features_to_select", "int", true, "The number of features to select.");
		registerOption("online_learning_rate", "double", true, "The learning rate for perceptron.");
		registerOption("polynomial_kernel_exponent", "double", true, "The exponent of the polynomial kernel.");
		registerOption("online_training_iterations", "int", true, "The number of training iterations for online methods.");
		
		// Other options will be added here.
	}
}
