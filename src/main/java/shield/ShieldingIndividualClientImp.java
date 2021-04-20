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

  // internal field only used for transmission purposes
  final class MessagingFoodBox {
    // a field marked as transient is skipped in marshalling/unmarshalling
    transient List<String> contents;

    String delivered_by;
    String diet;
    String id;
    String name;
  }

  public ShieldingIndividualClientImp(String endpoint) {
    this.endpoint = endpoint;
  }


  @Override
  public boolean registerShieldingIndividual(String newCHI) {
    String request = "/register_individual?CHI=newCHI";
    boolean success = true;
    String response = null;

    try {
      response = ClientIO.doPOSTRequest(endpoint, request);
    } catch (Exception e) {
      e.printStackTrace();
    }

    if(response == "already registered" || response == "must specify CHI"){
      success = false;
    }

    return success;
  }

  @Override
  public Collection<String> showFoodBoxes(String dietaryPreference) {
    // construct the endpoint request
    String request = "/showFoodBox?orderOption=catering&dietaryPreference=none";

    // setup the response recepient
    List<MessagingFoodBox> responseBoxes = new ArrayList<MessagingFoodBox>();

    List<String> boxIds = new ArrayList<String>();

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
    return false;
  }

  @Override
  public boolean cancelOrder(int orderNumber) {
    return false;
  }

  @Override
  public boolean requestOrderStatus(int orderNumber) {
    return false;
  }

  // **UPDATE**
  @Override
  public Collection<String> getCateringCompanies() {
    return null;
  }

  // **UPDATE**
  @Override
  public float getDistance(String postCode1, String postCode2) {
    return 0;
  }

  @Override
  public boolean isRegistered() {
    return false;
  }

  @Override
  public String getCHI() {
    return null;
  }

  @Override
  public int getFoodBoxNumber() {
    return 0;
  }

  @Override
  public String getDietaryPreferenceForFoodBox(int foodBoxId) {
    return null;
  }

  @Override
  public int getItemsNumberForFoodBox(int foodBoxId) {
    return 0;
  }

  @Override
  public Collection<Integer> getItemIdsForFoodBox(int foodboxId) {
    return null;
  }

  @Override
  public String getItemNameForFoodBox(int itemId, int foodBoxId) {
    return null;
  }

  @Override
  public int getItemQuantityForFoodBox(int itemId, int foodBoxId) {
    return 0;
  }

  @Override
  public boolean pickFoodBox(int foodBoxId) {
    return false;
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
