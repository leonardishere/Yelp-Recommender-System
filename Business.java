import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.*;
import java.io.IOException;


public class Business{
  private String id;
  private String name;
  private String neighborhood;
  private String address;
  private String city;
  private String state;
  private String postal_code;
  private float latitude;
  private float longitude;
  private float stars;
  private int review_count;
  private boolean is_open;
  private ArrayList<String> category;
  private ArrayList<String> attributes;

  private Map<String, Map<String, Boolean>> jsonmap = new HashMap<>(); //can't be right
  private Map<String, String> strmap = new HashMap<>();
  private Map<String, Integer> intmap = new HashMap<>();
  private Map<String, Boolean> boolmap = new HashMap<>();

  private ArrayList<String> jsonval = new ArrayList<String>();

  private ArrayList<String> stringval = new ArrayList<String>();

  private ArrayList<String> boolval = new ArrayList<String>();

  private ArrayList<String> numericval = new ArrayList<String>();

  public Business(String id, String name, String neighborhood, String address, String city, String state, String postal_code, float latitude, float longitude, float stars, int review_count, boolean is_open, ArrayList<String> category){
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
    this.category = (ArrayList<String>) category.clone();

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

  }




  public void AddAttributes(String name, String val){
    if(jsonval.contains(name)){
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
        System.out.println("Key:" + str1 + "\tValue:"+ boolstr2);
        tempmap.put(str1, boolstr2);
        //System.out.println("Key: " + field.getKey() + "\tValue:" + field.getValue());
      }
      jsonmap.put("Ambience", tempmap);
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
    int counter = 0;
    //similarity matrix
    double[] mysimilarity = new double[39];
    //iterate through all attributes, check if both have or both don't have
    for(String att:attributes){

      //current attribute is JSON
      if (jsonval.contains(att)){
        Map<String,Boolean> temp1 = this.jsonmap.get(att);
        Map<String,Boolean> temp2 = other.jsonmap.get(att);
        int larger = 0;
        double count = 0;
        double size = temp1.size();
        if (temp2.size() > temp1.size()){
          larger = 2;
        }else{
          larger = 1;
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

        mysimilarity[counter] = count/size;

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

    return mysimilarity;
  }

}
