/**
 * To implement
 */

package shield;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ShieldingIndividualClientImp implements ShieldingIndividualClient {
  private String endpoint;
  private String CHI;
  private String name;
  private String surname;
  private String postcode;
  private String phoneNumber;
  private String dietaryInfo;
  private boolean registered;
  private String foodboxChoice;
  private String orderStatus;
  private String closestCatering;
  private String closestCateringPostcode;


  // internal field only used for transmission purposes
  final class MessagingFoodBox {
    // a field marked as transient is skipped in marshalling/unmarshalling
    transient List<String> info;
    String delivered_by;
    String diet;
    String id;
    String name;
  }

  final class registeredUser{
    transient List<String> details;

    String postcode;
    String name;
    String surname;
    String phoneNumber;
  }

  final class foodboxItem{
    transient List<String> contents;
    String id;
    String name;
    String quantity;
  }

  final class MessagingFoodBox2 {
    transient List<String> info;
    List<foodboxItem> contents;
    String delivered_by;
    String diet;
    String id;
    String name;
  }

  public ShieldingIndividualClientImp(String endpoint) {
    this.endpoint = endpoint;
    this.name = null;
    this.surname = null;
    this.postcode = null;
    this.phoneNumber = null;
    this.dietaryInfo = null;
    this.CHI = null;
    this.registered = false;
    this.foodboxChoice = null;
    this.orderStatus = null;
    this.closestCatering = null;
    this.closestCateringPostcode = null;
  }


  @Override
  public boolean registerShieldingIndividual(String newCHI) {
    String request = "/registerShieldingIndividual?CHI=newCHI";
    registeredUser userDetails = new registeredUser();
    boolean success;
    String response = "";

    try{
      response = ClientIO.doGETRequest(endpoint + request);
      Type dataType = new TypeToken<registeredUser>() {} .getType();
      userDetails = new Gson().fromJson(response, dataType);
    } catch (Exception e) {
      e.printStackTrace();
    }

    if (response.equals("already registered")){
      success = false;
    }
    else {
      success = true;
      this.registered = true;
      this.CHI = newCHI;
      this.name = userDetails.name;
      this.surname = userDetails.surname;
      this.postcode = userDetails.postcode;
      this.phoneNumber = userDetails.phoneNumber;
    }
  return success;
  }

  @Override
  public Collection<String> showFoodBoxes(String dietaryPreference) {
    // construct the endpoint request
    String request = "/showFoodBox?orderOption=catering&dietaryPreference=dietaryPreference";

    // setup the response recipient
    List<MessagingFoodBox> responseBoxes = new ArrayList<MessagingFoodBox>();

    List<String> boxIds = new ArrayList<>();

    try {
      // perform request
      String response = ClientIO.doGETRequest(endpoint + request);

      // unmarshal response
      Type listType = new TypeToken<List<MessagingFoodBox>>() {} .getType();
      responseBoxes = new Gson().fromJson(response, listType);

      // gather required fields
      for (MessagingFoodBox b : responseBoxes) {
        boxIds.add(b.id);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return boxIds;
  }

  // **UPDATE2** REMOVED PARAMETER
  @Override
  public boolean placeOrder() {
    return false;
  }

  @Override
  public boolean editOrder(int orderNumber) {
    String request = "/placeOrder?order_id=orderNumber";
    return false;
  }

  @Override
  public boolean cancelOrder(int orderNumber) {
    String request = "/cancelOrder?order_id=orderNumber";
    String response = "";

    try {
      // perform request
      response = ClientIO.doPOSTRequest(endpoint+request, null);
    }catch (Exception e) {
      e.printStackTrace();
    }

    if(response == "true"){
      return true;
    }
    else{
      return false;
    }

  }

  @Override
  public boolean requestOrderStatus(int orderNumber) {
    String request = "/requestStatus?order_id=orderNumber";
    String response = "";

    try {
      // perform request
      response = ClientIO.doGETRequest(endpoint + request);
    } catch (Exception e) {
      e.printStackTrace();
    }

    if (response.equals("-1")) {
      return false;
    }
    else{
      this.orderStatus = response;
      return true;
    }
  }

  // **UPDATE**
  @Override
  public Collection<String> getCateringCompanies() {
    String request = "/getCaterers";
    List<CateringCompanyClientImp.provider> providerList;
    List<String> providerNames = new ArrayList<String>();

    try {
      // perform request
      String response = ClientIO.doGETRequest(endpoint + request);

      // unmarshal response
      Type listType = new TypeToken<List<CateringCompanyClientImp.provider>>() {} .getType();
      providerList = new Gson().fromJson(response, listType);

      // gather required fields
      for (CateringCompanyClientImp.provider b : providerList) {
        providerNames.add(b.name);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return providerNames;
  }

  // **UPDATE**
  @Override
  public float getDistance(String postCode1, String postCode2) {
    String request = "/distance?postcode1=postCode1&postcode2=postCode2";
    String response = "";

    try {
      // perform request
      response = ClientIO.doGETRequest(endpoint + request);
    }catch (Exception e) {
      e.printStackTrace();
    }
    int dist = Integer.parseInt(response);

    return dist;
  }

  @Override
  public boolean isRegistered() {
    return this.registered;
  }

  @Override
  public String getCHI() {
    return this.CHI;
  }

  @Override
  public int getFoodBoxNumber() {
    String request = "/showFoodBox";
    List<MessagingFoodBox> responseBoxes = new ArrayList<MessagingFoodBox>();
    int foodBoxNumber = 0;

    try {
      // perform request
      String response = ClientIO.doGETRequest(endpoint + request);

      // unmarshal response
      Type listType = new TypeToken<List<MessagingFoodBox>>() {}.getType();
      responseBoxes = new Gson().fromJson(response, listType);

      foodBoxNumber = responseBoxes.size();

    } catch (Exception e) {
      e.printStackTrace();
    }
    return foodBoxNumber;
  }

  @Override
  public String getDietaryPreferenceForFoodBox(int foodBoxId) {
    String request = "/showFoodBox?orderOption=catering&dietaryPreference=none";
    String dietaryPreference = "";

    List<MessagingFoodBox> responseBoxes = new ArrayList<MessagingFoodBox>();

    try {
      // perform request
      String response = ClientIO.doGETRequest(endpoint + request);

      // unmarshal response
      Type listType = new TypeToken<List<MessagingFoodBox>>() {} .getType();
      responseBoxes = new Gson().fromJson(response, listType);

      // gather required fields
      for (MessagingFoodBox b : responseBoxes) {
        if(foodBoxId == Integer.parseInt(b.id) ){
          dietaryPreference = b.diet;
        };
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return dietaryPreference;
  }

  @Override
  public int getItemsNumberForFoodBox(int foodBoxId) {
    String request = "/showFoodBox?orderOption=catering&dietaryPreference=none";
    int itemNumber = 0;

    List<MessagingFoodBox2> responseBoxes = new ArrayList<MessagingFoodBox2>();

    try {
      // perform request
      String response = ClientIO.doGETRequest(endpoint + request);

      // unmarshal response
      Type listType = new TypeToken<List<MessagingFoodBox2>>() {} .getType();
      responseBoxes = new Gson().fromJson(response, listType);

      // gather required fields
      for (MessagingFoodBox2 b : responseBoxes) {
        if(foodBoxId == Integer.parseInt(b.id) ){
          itemNumber = (b.contents).size();
        };
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return itemNumber;
  }


  @Override
  public Collection<Integer> getItemIdsForFoodBox(int foodBoxId) {
    String request = "/showFoodBox?orderOption=catering&dietaryPreference=none";
    List<Integer> listID = new ArrayList<>();
    List<MessagingFoodBox2> responseBoxes = new ArrayList<MessagingFoodBox2>();

    try {
      // perform request
      String response = ClientIO.doGETRequest(endpoint + request);

      // unmarshal response
      Type listType = new TypeToken<List<MessagingFoodBox2>>() {} .getType();
      responseBoxes = new Gson().fromJson(response, listType);

      // gather required fields
      for (MessagingFoodBox2 b : responseBoxes) {
        if(foodBoxId == Integer.parseInt(b.id) ){
          for (foodboxItem x : b.contents){
            listID.add( Integer.parseInt(x.id) );
          };
        };
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return listID;
  }

  @Override
  public String getItemNameForFoodBox(int itemId, int foodBoxId) {
    String request = "/showFoodBox?orderOption=catering&dietaryPreference=none";
    String itemName = "";
    List<MessagingFoodBox2> responseBoxes = new ArrayList<MessagingFoodBox2>();

    try {
      // perform request
      String response = ClientIO.doGETRequest(endpoint + request);

      // unmarshal response
      Type listType = new TypeToken<List<MessagingFoodBox2>>() {} .getType();
      responseBoxes = new Gson().fromJson(response, listType);

      // gather required fields
      for (MessagingFoodBox2 b : responseBoxes) {
        if(foodBoxId == Integer.parseInt(b.id) ){
          for (foodboxItem x : b.contents){
            if(itemId == Integer.parseInt(x.id)){
              itemName = x.name;
            }
          };
        };
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return itemName;
  }

  @Override
  public int getItemQuantityForFoodBox(int itemId, int foodBoxId) {
    String request = "/showFoodBox?orderOption=catering&dietaryPreference=none";
    List<Integer> foodBoxQuantities = new ArrayList<>();
    int quantity;
    List<MessagingFoodBox2> responseBoxes = new ArrayList<MessagingFoodBox2>();

    try {
      // perform request
      String response = ClientIO.doGETRequest(endpoint + request);

      // unmarshal response
      Type listType = new TypeToken<List<MessagingFoodBox2>>() {
      }.getType();
      responseBoxes = new Gson().fromJson(response, listType);

      // gather required fields
      for (MessagingFoodBox2 b : responseBoxes) {
        if (foodBoxId == Integer.parseInt(b.id)) {
          for (foodboxItem x : b.contents) {
            if (itemId == Integer.parseInt(x.id)) {
              foodBoxQuantities.add(Integer.parseInt(x.quantity));
            }
          }
          ;
        }
        ;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    quantity = foodBoxQuantities.stream().mapToInt(Integer::intValue).sum();

    return quantity;
  }

  @Override
  public boolean pickFoodBox(int foodBoxId) {
    this.foodboxChoice = String.valueOf(foodBoxId);
    return true;
  }

  @Override
  public boolean changeItemQuantityForPickedFoodBox(int itemId, int quantity) {
    return false;
  }

  @Override
  public Collection<Integer> getOrderNumbers() {
    return null;
  }

  @Override
  public String getStatusForOrder(int orderNumber) {
    return null;
  }

  @Override
  public Collection<Integer> getItemIdsForOrder(int orderNumber) {
    return null;
  }

  @Override
  public String getItemNameForOrder(int itemId, int orderNumber) {
    return null;
  }

  @Override
  public int getItemQuantityForOrder(int itemId, int orderNumber) {
    return 0;
  }

  @Override
  public boolean setItemQuantityForOrder(int itemId, int orderNumber, int quantity) {
    return false;
  }

  // **UPDATE2** REMOVED METHOD getDeliveryTimeForOrder

  // **UPDATE**
  @Override
  public String getClosestCateringCompany() {
    return null;
  }
}
