
// * To implement


package shield;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ShieldingIndividualClientImp implements ShieldingIndividualClient {
  final private String endpoint;
  private String CHI;
  private boolean registered;
  final private List <Order> orders = new ArrayList<>();
  final private PersonalInfo personalInfo = new PersonalInfo();
  private String closestCateringName;
  private String closestCateringPostcode;
  private MessagingFoodBox chosenFoodBox;

  static final class Order {
    String orderStatus;
    Integer orderId;
    MessagingFoodBox foodBox;

  }

  static final class MessagingFoodBox {
    List<foodBoxItem> contents;
    String delivered_by;
    String diet;
    int id;
    String name;
  }

  static final class PersonalInfo {
    transient List<String> details;

    String postcode;
    String name;
    String surname;
    String phoneNumber;
  }

   static final class foodBoxItem {
    transient List<String> contents;
    int id;
    String name;
    int quantity;
  }
  static final class CatererDetails {
    int id;
    String name;
    String postcode;
  }


  public ShieldingIndividualClientImp(String endpoint) {
    this.endpoint = endpoint;
    this.registered = false;
  }

  private boolean validateCHI(String CHI) {
    return CHI.length() == 10 &&
            CHI.matches("[0-9]+") &&
            Integer.parseInt(CHI.substring(0,2)) <= 31 &&
            Integer.parseInt(CHI.substring(0,2)) >= 1 &&
            Integer.parseInt(CHI.substring(2,4)) <= 12 &&
            Integer.parseInt(CHI.substring(2,4)) >= 1;
  }
  public String[] convertStringToArray (String strList) {
    strList = strList.replace("]", "");
    strList = strList.replace("[", "");
    strList = strList.replace("\"", "");
    return strList.split(",");
  }

  @Override
  public boolean registerShieldingIndividual(String newCHI) {
    if (newCHI == null || !validateCHI(newCHI)) return false;

    String request = "/registerShieldingIndividual?CHI=" + newCHI;
    boolean success = false;
    try {
      String response = ClientIO.doGETRequest(this.endpoint + request);
      String[] userDetails = convertStringToArray(response);
      if (response.equals("already registered")) success = true;
      else if (userDetails.length == 4) {

        success = true;
        this.registered = true;
        this.CHI = newCHI;
        this.personalInfo.postcode = userDetails[0].replace(' ', '_');
        this.personalInfo.name = userDetails[1];
        this.personalInfo.surname = userDetails[2];
        this.personalInfo.phoneNumber = userDetails[3];
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return success;
  }

  @Override
  public Collection<String> showFoodBoxes(String dietaryPreference) {
    // construct the endpoint request
    String request = "/showFoodBox?" +
            "orderOption=catering" +
            "&dietaryPreference=" + dietaryPreference;

    Collection<String> boxIds = new ArrayList<>();

    try {
      // perform request
      String response = ClientIO.doGETRequest(this.endpoint + request);

      // unmarshal response
      Type listType = new TypeToken<List<MessagingFoodBox>>() {
      }.getType();
      List<MessagingFoodBox> responseBoxes = new Gson().fromJson(response, listType);

      // gather required fields
      for (MessagingFoodBox box : responseBoxes) {
        boxIds.add(String.valueOf(box.id));
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return boxIds;
  }

  // **UPDATE2** REMOVED PARAMETER

  @Override
  public boolean placeOrder() {

    Order newOrder = new Order();
    String closest_company = getClosestCateringCompany();
    String data = new Gson().toJson(this.chosenFoodBox);
    String request = "/placeOrder?" +
            "individual_id=" + getCHI() +
            "&catering_business_name=" + closest_company +
            "&catering_postcode=" + this.closestCateringPostcode;
    try {
      String response = ClientIO.doPOSTRequest(this.endpoint + request, data);
      if (response.equals("must provide individual_id and catering_id. The individual and the catering must be registered before placing an order")){
        return false;
      }
      newOrder.orderId = Integer.parseInt(response);
      newOrder.foodBox = this.chosenFoodBox;
      newOrder.orderStatus = "order has been placed";
      this.orders.add(newOrder);
      return true;

    } catch (Exception e) {
      System.out.println("Post Request Failed");
      e.printStackTrace();
    }
    return false;
  }


  @Override
  public boolean editOrder(int orderNumber) {
    String request = "/editOrder?order_id=" + orderNumber;
    Order editedOrder = getTargetOrder(orderNumber);
    String data = new Gson().toJson(editedOrder.foodBox);
    boolean success = false;

    try {
      String response = ClientIO.doPOSTRequest(this.endpoint + request, data );
      success = response.equals("True");
    } catch (Exception e) {
      e.printStackTrace();
    }

    return success;
  }

  @Override
  public boolean cancelOrder(int orderNumber) {
    String request = "/cancelOrder?order_id=" + orderNumber;
    boolean success = false;
    try {
      // perform request
      String response = ClientIO.doGETRequest(endpoint + request);
      if (response.equals("True")) {
        success = true;
        for (Order order : this.orders) {
          if (order.orderId == orderNumber) {
            order.orderStatus = "order has been cancelled";
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return success;
  }

  @Override
  public boolean requestOrderStatus(int orderNumber) {
    String request = "/requestStatus?order_id=" + orderNumber;
    boolean success = false;

    try {
      // perform request
      String response = ClientIO.doGETRequest(endpoint + request);
      if (!response.equals("-1")) {
        for (Order o : this.orders) {
          if (o.orderId == orderNumber) {
            switch (response) {
              case "0" ->  o.orderStatus = "order has been placed";
              case "1" -> o.orderStatus = "order is packed";
              case "2" -> o.orderStatus = "order has been dispatched";
              case "3" -> o.orderStatus = "order has been delivered";
              case "4" -> o.orderStatus = "order has been cancelled";
              default -> System.out.println("Status " + response + " invalid");
            }

            success = true;
            break;
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return success;
  }


    // **UPDATE**
  @Override
  public Collection<String> getCateringCompanies() {
    String request = "/getCaterers";
    List<String> catererDetailsNames = new ArrayList<>();

    try {
      // perform request
      String response = ClientIO.doGETRequest(endpoint + request);
      // unmarshal response

      List<CatererDetails> caterersList = new ArrayList<>();
      String[] responseList = convertStringToArray(response);
      for (int i = 0; i < responseList.length; i = i + 3) {
        while (responseList[i] == "") i ++;
        CatererDetails currentCaterer = new CatererDetails();
        currentCaterer.id = Integer.parseInt(responseList[i]);
        currentCaterer.name = responseList[i+1];
        currentCaterer.postcode = responseList[i+2];
        caterersList.add(currentCaterer);
      }

      // gather required fields
      for (CatererDetails caterer : caterersList) {
        catererDetailsNames.add(caterer.name);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return catererDetailsNames;
  }

  // **UPDATE**
  @Override
  public float getDistance(String postCode1, String postCode2) {
    String request = "/distance?" +
            "postcode1=" + postCode1 +
            "&postcode2=" + postCode2;
    String response = "0";

    try {
      // perform request
      response = ClientIO.doGETRequest(endpoint + request);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return Float.parseFloat(response);
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
    String request = "/showFoodBox?" +
            "orderOption=catering&" +
            "dietaryPreference=";
    int foodBoxNumber = 0;

    try {
      // perform request
      String response = ClientIO.doGETRequest(this.endpoint + request);
      // unmarshal response
      Type listType = new TypeToken<List<MessagingFoodBox>>() {
      }.getType();
      List<MessagingFoodBox> responseBoxes = new Gson().fromJson(response, listType);

      foodBoxNumber = responseBoxes.size();

    } catch (Exception e) {
      e.printStackTrace();
    }
    return foodBoxNumber;
  }

  @Override
  public String getDietaryPreferenceForFoodBox(int foodBoxId) {
    String request = "/showFoodBox?orderOption=catering&dietaryPreference=";
    String dietaryPreference = "";


    try {
      // perform request
      String response = ClientIO.doGETRequest(endpoint + request);

      // unmarshal response
      Type listType = new TypeToken<List<MessagingFoodBox>>() {
      }.getType();
      List<MessagingFoodBox> responseBoxes = new Gson().fromJson(response, listType);

      // gather required fields
      for (MessagingFoodBox box : responseBoxes) {
        if (foodBoxId == box.id) {
          dietaryPreference = box.diet;
          break;
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return dietaryPreference;
  }

  @Override
  public int getItemsNumberForFoodBox(int foodBoxId) {
    String request = "/showFoodBox?orderOption=catering&dietaryPreference=";
    int itemNumber = 0;

    try {
      // perform request
      String response = ClientIO.doGETRequest(endpoint + request);

      // unmarshal response
      Type listType = new TypeToken<List<MessagingFoodBox>>() {
      }.getType();
      List<MessagingFoodBox> responseBoxes = new Gson().fromJson(response, listType);

      // gather required fields
      for (MessagingFoodBox b : responseBoxes) {
        if (foodBoxId == b.id) {
          itemNumber = (b.contents).size();
          break;
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return itemNumber;
  }


  @Override
  public Collection<Integer> getItemIdsForFoodBox(int foodBoxId) {
    String request = "/showFoodBox?orderOption=catering&dietaryPreference=";
    List<Integer> listID = new ArrayList<>();
    try {
      // perform request
      String response = ClientIO.doGETRequest(endpoint + request);

      // unmarshal response
      Type listType = new TypeToken<List<MessagingFoodBox>>() {
      }.getType();
      List<MessagingFoodBox> responseBoxes = new Gson().fromJson(response, listType);

      // gather required fields
      for (MessagingFoodBox box : responseBoxes) {
        if (foodBoxId == box.id) {
          for (foodBoxItem item : box.contents) {
            listID.add(item.id);
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return listID;
  }

  @Override
  public String getItemNameForFoodBox(int itemId, int foodBoxId) {
    String itemName = "Not Found";
    try {
      String request = "/showFoodBox?orderOption=catering&dietaryPreference=";
      String response = ClientIO.doGETRequest(this.endpoint + request);

      // unmarshal response
      Type listType = new TypeToken<List<MessagingFoodBox>>() {
      }.getType();
      List<MessagingFoodBox> responseBoxes = new Gson().fromJson(response, listType);

      // gather required fields
      for (MessagingFoodBox box : responseBoxes) {
        if (foodBoxId == box.id) {
          for (foodBoxItem item : box.contents)
            if (item.id == itemId) {
              itemName = item.name;
            }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return itemName;
  }

  @Override
  public int getItemQuantityForFoodBox(int itemId, int foodBoxId) {
    String request = "/showFoodBox?orderOption=catering&dietaryPreference=";
    int requestedQuantity = 0;
    try {
      // perform request
      String response = ClientIO.doGETRequest(endpoint + request);

      // unmarshal response
      Type listType = new TypeToken<List<MessagingFoodBox>>() {}.getType();
      List<MessagingFoodBox> responseBoxes = new Gson().fromJson(response, listType);

      // gather required fields
      for (MessagingFoodBox box : responseBoxes) {
        if (foodBoxId == box.id) {
          for (foodBoxItem item : box.contents) {
            if (itemId == item.id) {
              requestedQuantity = item.quantity;
            }
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return requestedQuantity;
  }

  @Override
  public boolean pickFoodBox(int foodBoxId) {
    String request = "/showFoodBox?orderOption=catering&dietaryPreference=";
    boolean success = false;
    try {
      // perform request
      String response = ClientIO.doGETRequest(endpoint + request);

      // unmarshal response
      Type listType = new TypeToken<List<MessagingFoodBox>>() {
      }.getType();
      List<MessagingFoodBox> responseBoxes = new Gson().fromJson(response, listType);

      // gather required fields
      for (MessagingFoodBox box : responseBoxes) {
        if (foodBoxId == box.id) {

          this.chosenFoodBox = box;
          success = true;
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return success;
  }

  @Override
  public boolean changeItemQuantityForPickedFoodBox(int itemId, int newQuantity) {
    if (newQuantity <= 0) {
      return false;
    }
    try {

      for (foodBoxItem item : this.chosenFoodBox.contents) {
        if (item.id == itemId && item.quantity != newQuantity) {
          item.quantity = newQuantity;
          return true;
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return false;
  }

  @Override
  public Collection<Integer> getOrderNumbers() {
    List <Integer> orderNumbers = new ArrayList<>();
    try {
      for (Order order : this.orders) {
        orderNumbers.add(order.orderId);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return orderNumbers;
  }

  public Order getTargetOrder (int orderNumber) {
    Order targetOrder = null;
    try {
      for (Order order : this.orders) {
        if (order.orderId == orderNumber) {
          targetOrder = order;
          break;
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return targetOrder;
  }

  @Override
  public String getStatusForOrder(int orderNumber) {
    return getTargetOrder(orderNumber).orderStatus;
  }

  @Override
  public Collection<Integer> getItemIdsForOrder(int orderNumber) {
    List<Integer> itemIDs = new ArrayList<>();
    Order targetOrder = getTargetOrder(orderNumber);
    try {
      // perform request
      // gather required fields
      for (foodBoxItem item : targetOrder.foodBox.contents) {
        itemIDs.add(item.id);

      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return itemIDs;
  }


  @Override
  public String getItemNameForOrder(int itemId, int orderNumber){
    Order targetOrder = getTargetOrder(orderNumber);
    try {
      for (foodBoxItem item : targetOrder.foodBox.contents) {
        if (item.id == itemId) {
          return item.name;
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  @Override
  public int getItemQuantityForOrder(int itemId, int orderNumber) {
    Order targetOrder = getTargetOrder(orderNumber);
    try {
      for (foodBoxItem item : targetOrder.foodBox.contents) {
        if (item.id == itemId) {
          return item.quantity;
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return 0;
  }

  @Override
  public boolean setItemQuantityForOrder(int itemId, int orderNumber, int newQuantity) {
    if (newQuantity <= 0) {
      return false;
    }
    try {
      for (Order order : this.orders) {
        if (order.orderId == orderNumber) {
          for (foodBoxItem item : order.foodBox.contents) {
            if (item.id == itemId) {
              if (newQuantity < item.quantity) {
                item.quantity = newQuantity;
                return true;

              }
            }
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return false;
  }

  // **UPDATE2** REMOVED METHOD getDeliveryTimeForOrder

  // **UPDATE**
  @Override
  public String getClosestCateringCompany() {
      String request = "/getCaterers";
      try {
        // perform request
        String response = ClientIO.doGETRequest(endpoint + request);
        // unmarshal response
        List<CatererDetails> caterersList = new ArrayList<>();
        String[] responseList = convertStringToArray(response);

        for (int i = 0; i < responseList.length; i = i + 3) {
          CatererDetails currentCaterer = new CatererDetails();

          while (responseList[i].equals("")) i ++;
          currentCaterer.id = Integer.parseInt(responseList[i]);
          currentCaterer.name = responseList[i+1];
          currentCaterer.postcode = responseList[i+2];
          caterersList.add(currentCaterer);

        }

        // gather required fields
        float shortestDistance = Integer.MAX_VALUE;
        this.closestCateringName = "Failed";
        int count = 0;
        for (CatererDetails p : caterersList) {
          if (count == 0) { count ++; continue; }
          if (this.personalInfo.postcode == null || p.postcode == null){
            throw new NullPointerException("Shielding User postcode or caterer postcode was invalid");
          }
            float currentDistance = getDistance(this.personalInfo.postcode, p.postcode);
          if (shortestDistance > currentDistance) {
            shortestDistance = currentDistance;
            this.closestCateringPostcode = p.postcode;
            this.closestCateringName = p.name;
          }
        }
      } catch (Exception e) {
        System.out.println(e.getMessage());
      }

      return this.closestCateringName;
    }
}
