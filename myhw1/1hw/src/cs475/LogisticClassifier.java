/*
 * Kyle Wong
 * 14.2.11
 * Machine Learning
 * kwong23
 * Assignment 1
 */
package cs475;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class LogisticClassifier extends Predictor{

	private final int UNINITIALIZED = -1;
	private double gd_eta = 0.01; //default
	private int num_features_to_select = UNINITIALIZED;
	private int gd_iterations = 20; //default

	private Double[] infogains;
	private Double[] weights;

	public LogisticClassifier(double input_eta, int input_num_features, int input_num_iters) {
		// TODO Auto-generated constructor stub
		this.gd_eta = input_eta;
		this.num_features_to_select = input_num_features;
		this.gd_iterations = input_num_iters;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Here we go - unit test");
		ArrayList<Instance> instances = new ArrayList<Instance>();
		FeatureVector feature_vector = new FeatureVector();
		Label label = null;
		
		label = new ClassificationLabel(1);
		feature_vector.add(1, -0.112217);
		feature_vector.add(2, 0.077192);
		Instance instance = new Instance(feature_vector, label);
		instances.add(instance);
		
		feature_vector = new FeatureVector();
		label = new ClassificationLabel(0);
		feature_vector.add(1, 0.496442);
		feature_vector.add(2, -0.290597);
		instance = new Instance(feature_vector, label);
		instances.add(instance);
		
		feature_vector = new FeatureVector();
		label = new ClassificationLabel(1);
		feature_vector.add(1, 0.418255);
		feature_vector.add(2, -1.791584);
		instance = new Instance(feature_vector, label);
		instances.add(instance);
		
		feature_vector = new FeatureVector();
		label = new ClassificationLabel(0);
		feature_vector.add(1, 0.237331);
		feature_vector.add(2, 0.9901527);
		instance = new Instance(feature_vector, label);
		instances.add(instance);
		
		feature_vector = new FeatureVector();
		label = new ClassificationLabel(0);
		feature_vector.add(1, 0.537331);
		feature_vector.add(2, 0.9901527);
		instance = new Instance(feature_vector, label);
		instances.add(instance);
		
		for(Instance e : instances){

			System.out.println(e);
			System.out.println(e.getLabel());
			Iterator elements = e.getFeatureVector().getIterator();
			while(elements.hasNext()){
				Map.Entry items = (Map.Entry)elements.next();
				System.out.printf("key %s value %s\n", items.getKey(), items.getValue() );
			}
			System.out.println();
		}
		LogisticClassifier ml = new LogisticClassifier(0.01, -1, 20);
		ml.train(instances);
		System.out.println("Done unit test for logistic classifier.");
	}

	@Override
	public void train(List<Instance> instances) {
		// TODO Auto-generated method stub
		selectFeatures(instances);
		trainWeights(instances);
	}
	
	private void selectFeatures(List<Instance> instances){

		Double thresh_sum;
		Double threshold;
		double px, pyx0, pyx1;
		double pxj, pyixj0, pyixj1;
		
		int maxkey = getMaxFeatureKey(instances);
		infogains = new Double[maxkey];
		weights = new Double[maxkey];
		for(int i = 0; i < maxkey; i++){
			infogains[i] = null;
			weights[i] = 0.0;
		}
		for(int i = 0; i < maxkey; i++){
			thresh_sum = 0.0;
			px = 0;
			pyx0 = 0;
			pyx1 = 0;
			for(Instance inst : instances){
				thresh_sum += inst._feature_vector.get(i+1);
			}
			threshold = thresh_sum / instances.size();
			
			for(Instance inst: instances){
				if(inst._feature_vector.get(i+1) < threshold){
					px++;
					if(inst.getLabel().toString().equals("0")){
						pyx0++;
					}
					else if(inst.getLabel().toString().equals("1")){
						pyx1++;
					}
				}
			}
			pxj = px / instances.size();
			pyixj0 = pyx0 / instances.size();
			pyixj1 = pyx1 / instances.size();//log of 0 is NaN - might be a problem when lacking train data
			infogains[i] = -pyixj0*Math.log(pyixj0 / pxj) - pyixj1*Math.log(pyixj1 / pxj);
			
			double temp = infogains[i]; 
			System.out.println("temp "+temp);
		}
	}
		
	private int getMaxFeatureKey(List<Instance> instances){
		int maxkey = 0;
		int tempkey = 0;
		for (Instance e: instances){
			tempkey = e.getFeatureVector().getMaxKey();
			if ( tempkey > maxkey){
				maxkey = tempkey;
			}
		}
		return maxkey;
	}
	
	private void trainWeights(List<Instance> instances){
		Double[] newWeight = new Double[this.weights.length];
		
		for(int i = 0; i < this.gd_iterations; i++){
			Double[] etaDel = scalarMultiply(this.gd_eta, getDel(instances));
			newWeight = vectorAdd(this.weights, etaDel);
			this.weights = newWeight;
		}
	}
	
	private Double[] getDel(List<Instance> instances){
		Double[] Del = new Double[this.weights.length];
		double yi = 0;
		double xij;
		Double gneg;
		Double gpos;
		Double[] wxneg;
		Double[] wxpos;
		Double[] xi;
		for(Instance e : instances){ //for each instance
			if(e.getLabel().equals("0")) yi = 0;
			else if(e.getLabel().equals("1")) yi = 1;
			xi = e._feature_vector.getDoubles();

			wxneg = vectorMultiply(this.weights, scalarMultiply(-1.0, xi));
			wxpos = vectorMultiply(this.weights, xi);
			for(int i = 0; i < this.weights.length; i++){ //for each feature
				xij = e._feature_vector.get(i+1); //features start at 1 not 0
				gneg = xij*getLinkFunction(wxneg[i]);
				gpos = -xij*getLinkFunction(wxpos[i]);
				Del[i] += yi*gneg + (1-yi)*gpos;
			}
		}
		return Del;
	}
	
	private Double getLinkFunction(Double z){
		return (Double) 1.0 / (1.0+ Math.exp(-z));
	}
	
	private Double[] vectorAdd(Double[] toadd, Double[] added){
		Double[] sum = new Double[toadd.length];
		for(int i = 0; i < toadd.length; i++){
			sum[i] = toadd[i] + added[i];
		}
		return sum;
	}
	
	private Double[] scalarMultiply(double scaler, Double[] ra){
		Double[] product = new Double[ra.length];
		for(int i = 0; i < product.length; i++){
			product[i] = scaler*ra[i];
		}
		return product;
	}
	
	private Double[] vectorMultiply(Double[] tomult, Double[] multiplied){
		Double[] prod = new Double[tomult.length];
		for(int i = 0; i < prod.length; i++){
			prod[i] = tomult[i]*multiplied[i];
		}
		return prod;
	}

	@Override
	public Label predict(Instance instance) {
		// TODO Auto-generated method stub
		return new ClassificationLabel(1);
	}
	
	public String toString() {
		// TODO Auto-generated method stub
		return "LogisticClassifier";
	}

}