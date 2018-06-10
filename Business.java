import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.*;
import java.io.IOException;


public class Business{
	public static final int NUM_FEATURES = 41;

	public String id;
	public String name;
	public String neighborhood;
	public String address;
	public String city;
	public String state;
	public String postal_code;
	public double latitude;
	public double longitude;
	public double stars;
	public int review_count;
	public boolean is_open;
	public ArrayList<String> category = null;
	public static ArrayList<String> attributes = null;

	public Map<String, Map<String, Boolean>> jsonmap = new HashMap<>();
	public Map<String, String> strmap = new HashMap<>();
	public Map<String, Integer> intmap = new HashMap<>();
	public Map<String, Boolean> boolmap = new HashMap<>();

	public ArrayList<String> jsonval = new ArrayList<String>();

	public ArrayList<String> stringval = new ArrayList<String>();

	public ArrayList<String> boolval = new ArrayList<String>();

	public ArrayList<String> numericval = new ArrayList<String>();

	//private static TermFrequencyAnalyzer termFrequencyAnalyzer = null; //TODO: re-enable this

	public Business(String id, String name, String neighborhood, String address, String city, String state, String postal_code, double latitude, double longitude, double stars, int review_count, boolean is_open){
		this.id = id;
		this.name = name;
		this.neighborhood = neighborhood;
		this.address = address;
		this.city = city;
		this.state = state;
		this.postal_code = postal_code;
		this.latitude = latitude;
		this.longitude = longitude;
		this.stars = stars;
		this.review_count = review_count;
		this.is_open = is_open;
		this.category = new ArrayList<>();

		jsonval.add("Ambience");
		jsonval.add("BestNights"); //name, boolean
		jsonval.add("BusinessParking");
		jsonval.add("DietaryRestrictions");
		jsonval.add("GoodForMeal");
		jsonval.add("HairSpecializesIn");
		jsonval.add("Music");

		stringval.add("AgesAllowed");
		stringval.add("Alcohol");
		stringval.add("BYOBCorkage");
		stringval.add("NoiseLevel");
		stringval.add("RestaurantsAttire");
		stringval.add("Smoking");
		stringval.add("WiFi");

		boolval.add("AcceptsInsurance");
		boolval.add("BikeParking");
		boolval.add("BusinessAcceptsBitcoin");
		boolval.add("BusinessAcceptsCreditCards");
		boolval.add("ByAppointmentOnly");
		boolval.add("BYOB");
		boolval.add("Caters");
		boolval.add("CoatCheck");
		boolval.add("Corkage");
		boolval.add("DogsAllowed");
		boolval.add("DriveThru");
		boolval.add("GoodForDancing");
		boolval.add("GoodForKids");
		boolval.add("HappyHour");
		boolval.add("HasTV");
		boolval.add("Open24Hours");
		boolval.add("OutdoorSeating");
		boolval.add("RestaurantsCounterService");
		boolval.add("RestaurantsDelivery");
		boolval.add("RestaurantsGoodForGroups");
		boolval.add("RestaurantsReservations");
		boolval.add("RestaurantsTableService");
		boolval.add("RestaurantsTakeOut");
		boolval.add("WheelchairAccessible");

		numericval.add("RestaurantsPriceRange2"); //multiint

		if(attributes == null) {
			attributes = new ArrayList<>();
			attributes.add("AcceptsInsurance");
			attributes.add("AgesAllowed");
			attributes.add("Alcohol");
			attributes.add("Ambience");
			attributes.add("BestNights");
			attributes.add("BikeParking");
			attributes.add("BusinessAcceptsBitcoin");
			attributes.add("BusinessAcceptsCreditCards");
			attributes.add("BusinessParking");
			attributes.add("ByAppointmentOnly");
			attributes.add("BYOB");
			attributes.add("BYOBCorkage");
			attributes.add("Caters");
			attributes.add("CoatCheck");
			attributes.add("Corkage");
			attributes.add("DietaryRestrictions");
			attributes.add("DogsAllowed");
			attributes.add("DriveThru");
			attributes.add("GoodForDancing");
			attributes.add("GoodForKids");
			attributes.add("GoodForMeal");
			attributes.add("HairSpecializesIn");
			attributes.add("HappyHour");
			attributes.add("HasTV");
			attributes.add("Music");
			attributes.add("NoiseLevel");
			attributes.add("Open24Hours");
			attributes.add("OutdoorSeating");
			attributes.add("RestaurantsAttire");
			attributes.add("RestaurantsCounterService");
			attributes.add("RestaurantsDelivery");
			attributes.add("RestaurantsGoodForGroups");
			attributes.add("RestaurantsReservations");
			attributes.add("RestaurantsTableService");
			attributes.add("RestaurantsTakeOut");
			attributes.add("Smoking");
			attributes.add("WheelchairAccessible");
			attributes.add("WiFi");
		}

		//TODO: re-enable this
		/*
    if(termFrequencyAnalyzer == null) {
    	termFrequencyAnalyzer = new TermFrequencyAnalyzer();
    	termFrequencyAnalyzer.initializeDB();
    }
		 */
	}

	public void addAttribute(String name, String val){
		if(name.equalsIgnoreCase("category")) {
			category.add(val);
		}else if(jsonval.contains(name)){
			JsonFactory factory = new JsonFactory();

			ObjectMapper mapper = new ObjectMapper(factory);
			JsonNode rootNode = null;
			try{
				rootNode = mapper.readTree(val);
			}catch(IOException e){
				System.out.println(e);
			}

			Iterator<Map.Entry<String,JsonNode>> fieldsIterator = rootNode.fields();
			String str1;
			String str2;
			boolean boolstr2 = true;

			Map<String,Boolean> tempmap = new HashMap<String, Boolean>();
			while (fieldsIterator.hasNext()) {

				Map.Entry<String,JsonNode> field = fieldsIterator.next();
				str1 = field.getKey();
				str2 = field.getValue().asText();
				if (str2.equals("false")){
					boolstr2 = false;
				}else if (str2.equals("true")){
					boolstr2 = true;
				}
				//System.out.println("Key:" + str1 + "\tValue:"+ boolstr2);
				tempmap.put(str1, boolstr2);
				//System.out.println("Key: " + field.getKey() + "\tValue:" + field.getValue());
			}
			jsonmap.put(name, tempmap);
			//System.out.printf("jsonmap.put(%s, %s)\n", name, val);
		}else if (stringval.contains(name)){

			strmap.put(name, val);

		}else if (boolval.contains(name)){

			boolean tempbool;
			int temp = Integer.parseInt(val);
			if (temp == 1){
				tempbool = true;
			}
			else{
				tempbool = false;
			}
			boolmap.put(name,tempbool);

		}else if (numericval.contains(name)){
			int temp = Integer.parseInt(val);
			intmap.put(name, temp);
		}else{
			System.out.println("Not in the approved attributes");
		}

	}

	public double[] similarity(Business other){
		//int counter = 0;
		//similarity matrix
		//double[] mysimilarity = new double[NUM_FEATURES];
		double[] mysimilarity = Helper.zeros(NUM_FEATURES);
		//iterate through all attributes, check if both have or both don't have
		//for(String att:attributes){
		for(int counter = 0; counter < attributes.size(); ++counter) {
			String att = attributes.get(counter);
			
			//current attribute is JSON
			if (jsonval.contains(att)){
				Map<String,Boolean> temp1 = this.jsonmap.get(att);
				Map<String,Boolean> temp2 = other.jsonmap.get(att);

				/*
				if ((temp1 == null) && (temp2 == null)){
					mysimilarity[counter] = 0;
				} else if ((temp1 == null) || (temp2 == null)){
					mysimilarity[counter] = 1;
				*/
				if(temp1 == null || temp2 == null) {
					mysimilarity[counter] = 0.5;
				}else{
					int larger = 0;
					double count = 0;
					//double size = temp1.size();
					double mysize;
					if (temp2.size() > temp1.size()){
						larger = 2;
						mysize = temp2.size();
					}else{
						larger = 1;
						mysize = temp1.size();
					}
					if (larger == 1){
						for(String key : temp1.keySet()){

							//if temp2 contains it

							if (temp2.get(key) != null){

								//System.out.println("temp1:"+temp1.get(key) + "temp2" + temp2.get(key));


								if (temp1.get(key).equals(temp2.get(key))){
									count++;
								}
							}
						}
					}
					else if (larger == 2){
						for(String key : temp2.keySet()){

							//if temp2 contains it

							if (temp1.get(key) != null){

								//System.out.println("temp1:"+temp1.get(key) + "temp2" + temp2.get(key));


								if (temp1.get(key).equals(temp2.get(key))){
									count++;
								}
							}
						}
					}

					mysimilarity[counter] = count/mysize;
				}

			}

			//current attribute is string
			else if (stringval.contains(att)){
				//check if both are NULL
				int numhas = 0;
				if (this.strmap.containsKey(att)){
					numhas++;
				}
				if (other.strmap.containsKey(att)){
					numhas++;
				}
				if (numhas == 0){
					mysimilarity[counter]++;
				}else if ((numhas == 2) && (this.strmap.get(att).equals(other.strmap.get(att)))){
					mysimilarity[counter]++;
				}

			}

			else if (boolval.contains(att)){
				//check if both are NULL
				int numhas = 0;
				if (this.boolmap.containsKey(att)){
					numhas++;
				}
				if (other.boolmap.containsKey(att)){
					numhas++;
				}
				if (numhas == 0){
					mysimilarity[counter]++;
				}else if ((numhas == 2)&&(this.boolmap.get(att).equals(other.boolmap.get(att)))){
					mysimilarity[counter]++;
				}



			}

			else if (numericval.contains(att)){
				//check if both are NULL
				int numhas = 0;
				if (this.intmap.containsKey(att)){
					numhas++;
				}
				if (other.intmap.containsKey(att)){
					numhas++;
				}
				if (numhas == 0){
					mysimilarity[counter]++;
				}else if ((numhas == 2) && (this.intmap.get(att).equals(other.intmap.get(att)))){
					mysimilarity[counter]++;
				}
			}

			counter++;

		}

		mysimilarity[39] = Helper.jaccardSimilarity(category, other.category);			//category
		//TODO: re-enable this
		//mysimilarity[40] = termFrequencyAnalyzer.getKeyTermsFromDB(this.id, other.id); 	//postagger
		mysimilarity[40] = 0;

		return mysimilarity;
	}

	public boolean equals(Business other) {
		return id.equals(other.id);
	}

}
