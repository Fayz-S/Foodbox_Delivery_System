
// * To implement


package shield;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ShieldingIndividualClientImp implements ShieldingIndividualClient {
  private String endpoint;
  private String CHI;
  private String dietaryInfo;
  private boolean registered;
  private List <Order> orders;
  private String foodboxChoice;
  private PersonalInfo personalInfo;
  private String closestCateringName;
  private String closestCateringPostcode;
  private MessagingFoodBox chosenFoodBox;
    
  final class Order {
    String orderStatus;
    Integer orderId;
    MessagingFoodBox foodBox;
  }

  // internal field only used for transmission purposes
//  final class MessagingFoodBox {
//    // a field marked as transient is skipped in marshalling/unmarshalling
//    transient List<foodboxItem> info;
//    String delivered_by;
//    String diet;
//    String id;
//    String name;
//  }
  final class MessagingFoodBox {
    List<foodboxItem> contents;
    String delivered_by;
    String diet;
    int id;
    String name;
  }

  final class PersonalInfo {
    transient List<String> details;

    String postcode;
    String name;
    String surname;
    String phoneNumber;
  }

  final class foodboxItem {
    transient List<String> contents;
    int id;
    String name;
    int quantity;
  }


  public ShieldingIndividualClientImp(String endpoint) {
    this.endpoint = endpoint;
    this.registered = false;
  }


  @Override
  public boolean registerShieldingIndividual(String newCHI) {
    String request = "/registerShieldingIndividual?CHI=newCHI";
    PersonalInfo userDetails = new PersonalInfo();
    boolean success = false;
    String response = "";

    try {
      response = ClientIO.doGETRequest(endpoint + request);
      Type dataType = new TypeToken<PersonalInfo>() {
      }.getType();
      userDetails = new Gson().fromJson(response, dataType);


      if (!response.equals("already registered")) {
        success = true;
        this.registered = true;
        this.CHI = newCHI;
        this.personalInfo = userDetails;
      }
    } catch (Exception e) {
      e.printStackTrace();
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
      Type listType = new TypeToken<List<MessagingFoodBox>>() {
      }.getType();
      responseBoxes = new Gson().fromJson(response, listType);

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
  /**
  {"contents": [{"id":1,
          "name":"cucumbers",
          "quantity":1},
    {"id":2,
            "name":"tomatoes",
            "quantity":2}]}

   -----

   Car car = new Car();
   car.brand = "Rover";
   car.doors = 5;

   Gson gson = new Gson();

   String json = gson.toJson(car);

   */
  @Override
  //placeOrder?individual_id=x
  // &catering business name=caterer
  // &catering postcode=catererPostcode
  public boolean placeOrder() {
//    String individualID = this.CHI();
//    String diet = this.dietaryInfo;
//    String caterer = getClosestCateringCompany();
//    String catererPostcode = this.closestCateringPostcode;
//    String data = new Gson().toJson(this.chosenFoodBox);
//    Order newOrder = new Order();
//    String request = "/showFoodBox?orderOption=catering&dietaryPreference=diet";
//    List<MessagingFoodBox> responseBoxes = new ArrayList<MessagingFoodBox>();
//
//    try {
//      // perform request
//      String response = ClientIO.doGETRequest(endpoint + request);
//
//      // unmarshal response
//      Type listType = new TypeToken<List<MessagingFoodBox>>() {}.getType();
//      responseBoxes = new Gson().fromJson(response, listType);
//
//      for(MessagingFoodBox x : responseBoxes){
//        data = new Gson().toJson(x.contents); //THIS ONE <--------------------------------------
//        break;
//      }
//    } catch (Exception e) {
//      e.printStackTrace();
//    }
//
//    request = "/placeOrder?individual_id=individualID&catering_business_name=caterer&catering_postcode=catererPostcode";
//
//    try {
//      // perform request
//      String response = ClientIO.doPOSTRequest(endpoint + request, data);
//      this.order.orderId = response;
//      return true;
//    } catch (Exception e) {
//      e.printStackTrace();
//      return false;
//    }
    String individualID = getCHI();
    String diet = this.dietaryInfo;
    String caterer = getClosestCateringCompany();
    Order newOrder = null;
    Integer newOrderId;
    String catererPostcode = this.closestCateringPostcode;
    String data = new Gson().toJson(this.chosenFoodBox);
    String request =    "/placeOrder?individual_id=x" +
            "&catering business name=caterer" +
            "&catering postcode=catererPostcode";
    try {
      newOrderId = Integer.parseInt(ClientIO.doPOSTRequest(request, data));
      newOrder.orderId = newOrderId;
      newOrder.foodBox = this.chosenFoodBox;
      newOrder.orderStatus = "order has been placed";
      this.orders.add(newOrder);
      return true;

    } catch (Exception e) {
      e.printStackTrace();
    }
    return false;
  }


  @Override
  public boolean editOrder(int orderNumber) {
    String request = "/editOrder?order_id=orderNumber";
    Order editedOrder = getTargetOrder(orderNumber);
    String data = new Gson().toJson(editedOrder.foodBox);
    try {
      String response = ClientIO.doPOSTRequest(request, data);
      return true;
    } catch (Exception e) {
      e.printStackTrace();
    }

    return false;
  }

  @Override
  public boolean cancelOrder(int orderNumber) {
    String request = "/cancelOrder?order_id=orderNumber";
    String response = null;

    try {
      // perform request
      response = ClientIO.doPOSTRequest(endpoint + request, null);
    } catch (Exception e) {
      e.printStackTrace();
    }

    if (response == "True") {
      return true;
    } else {
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
    } else {
      for (Order o : this.orders) {
        if (o.orderId == orderNumber) {
          o.orderStatus = response;
        }
      }
      return true;
    }
  }

  // **UPDATE**
  @Override
  public Collection<String> getCateringCompanies() {
    String request = "/getCaterers";
    List<CateringCompanyClientImp.CatererDetails> CatererDetailsList;
    List<String> CatererDetailsNames = new ArrayList<String>();

    try {
      // perform request
      String response = ClientIO.doGETRequest(endpoint + request);

      // unmarshal response
      Type listType = new TypeToken<List<CateringCompanyClientImp.CatererDetails>>() {
      }.getType();
      CatererDetailsList = new Gson().fromJson(response, listType);

      // gather required fields
      for (CateringCompanyClientImp.CatererDetails b : CatererDetailsList) {
        CatererDetailsNames.add(b.business_name);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return CatererDetailsNames;
  }

  // **UPDATE**
  @Override
  public float getDistance(String postCode1, String postCode2) {
    String request = "/distance?postcode1=postCode1&postcode2=postCode2";
    String response = "0";

    try {
      // perform request
      response = ClientIO.doGETRequest(endpoint + request);
    } catch (Exception e) {
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
    String request = "/showFoodBox?orderOption=catering&dietaryPreference=";
    List<MessagingFoodBox> responseBoxes = new ArrayList<MessagingFoodBox>();
    int foodBoxNumber = 0;

    try {
      // perform request
      String response = ClientIO.doGETRequest(this.endpoint + request);

      // unmarshal response
      Type listType = new TypeToken<List<MessagingFoodBox>>() {
      }.getType();
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
      Type listType = new TypeToken<List<MessagingFoodBox>>() {
      }.getType();
      responseBoxes = new Gson().fromJson(response, listType);

      // gather required fields
      for (MessagingFoodBox box : responseBoxes) {
        if (foodBoxId == box.id) {
          dietaryPreference = box.diet;
          break;
        }
        ;
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

    List<MessagingFoodBox> responseBoxes = new ArrayList<MessagingFoodBox>();

    try {
      // perform request
      String response = ClientIO.doGETRequest(endpoint + request);

      // unmarshal response
      Type listType = new TypeToken<List<MessagingFoodBox>>() {
      }.getType();
      responseBoxes = new Gson().fromJson(response, listType);

      // gather required fields
      for (MessagingFoodBox b : responseBoxes) {
        if (foodBoxId == b.id) {
          itemNumber = (b.contents).size();
          break;
        }
        ;
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
    List<MessagingFoodBox> responseBoxes = new ArrayList<MessagingFoodBox>();
    try {
      // perform request
      String response = ClientIO.doGETRequest(endpoint + request);

      // unmarshal response
      Type listType = new TypeToken<List<MessagingFoodBox>>() {
      }.getType();
      responseBoxes = new Gson().fromJson(response, listType);

      // gather required fields
      for (MessagingFoodBox box : responseBoxes) {
        if (foodBoxId == box.id) {
          for (foodboxItem item : box.contents) {
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
    String request = "/showFoodBox?orderOption=catering&dietaryPreference=";
    List<Integer> itemIDs = new ArrayList<Integer>();
    String itemName = null;
    List<MessagingFoodBox> responseBoxes = new ArrayList<MessagingFoodBox>();
    try {
      // perform request
      String response = ClientIO.doGETRequest(this.endpoint + request);

      // unmarshal response
      Type listType = new TypeToken<List<MessagingFoodBox>>() {
      }.getType();
      responseBoxes = new Gson().fromJson(response, listType);

      // gather required fields
      for (MessagingFoodBox box : responseBoxes) {
        if (foodBoxId == box.id) {
          for (foodboxItem item : box.contents)
            if (item.id == itemId) {
              itemName = item.name;
              break;
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
    List<Integer> foodBoxQuantities = new ArrayList<>();
    int quantity;
    List<MessagingFoodBox> responseBoxes = new ArrayList<MessagingFoodBox>();

    try {
      // perform request
      String response = ClientIO.doGETRequest(endpoint + request);

      // unmarshal response
      Type listType = new TypeToken<List<MessagingFoodBox>>() {}.getType();
      responseBoxes = new Gson().fromJson(response, listType);

      // gather required fields
      for (MessagingFoodBox box : responseBoxes) {
        if (foodBoxId == box.id) {
          for (foodboxItem item : box.contents) {
            if (itemId == item.id) {
              foodBoxQuantities.add(item.quantity);
            }
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    quantity = foodBoxQuantities.stream().mapToInt(Integer::intValue).sum();
    return quantity;
  }

  @Override
  public boolean pickFoodBox(int foodBoxId) {
    String request = "/showFoodBox?orderOption=catering&dietaryPreference=";
    List<MessagingFoodBox> responseBoxes = new ArrayList<MessagingFoodBox>();

    try {
      // perform request
      String response = ClientIO.doGETRequest(endpoint + request);

      // unmarshal response
      Type listType = new TypeToken<List<MessagingFoodBox>>() {
      }.getType();
      responseBoxes = new Gson().fromJson(response, listType);

      // gather required fields
      for (MessagingFoodBox box : responseBoxes) {
        if (foodBoxId == box.id) {
          this.chosenFoodBox = box;
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
    return true;
  }

  @Override
  public boolean changeItemQuantityForPickedFoodBox(int itemId, int newQuantity) {
    try {
      for (foodboxItem item : this.chosenFoodBox.contents) {
        if (item.id == itemId) {
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
    String status = null;
    try {
      Order targetOrder = getTargetOrder(orderNumber);
      if (targetOrder.orderStatus.equals("0")) {
        status = "order has been placed";
      } else if (targetOrder.orderStatus.equals("1")) {
        status = "order is packed";
      } else if (targetOrder.orderStatus.equals("2")) {
        status = "order has been dispatched";
      } else if (targetOrder.orderStatus.equals("3")) {
        status = "order has been delivered";
      } else if (targetOrder.orderStatus.equals("4")) {
        status = "order has been cancelled";
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return status;
  }

  @Override
  public Collection<Integer> getItemIdsForOrder(int orderNumber) {
    List<Integer> itemIDs = new ArrayList<Integer>();
    Order targetOrder = getTargetOrder(orderNumber);
    try {
      // perform request
      // gather required fields
      for (foodboxItem item : targetOrder.foodBox.contents) {
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
      for (foodboxItem item : targetOrder.foodBox.contents) {
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
      for (foodboxItem item : targetOrder.foodBox.contents) {
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
    try {
      for (Order order : this.orders) {
        if (order.orderId == orderNumber) {
          for (foodboxItem item : order.foodBox.contents) {
            if (item.id == itemId) {
              if (newQuantity <= item.quantity) {
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
      List<CateringCompanyClientImp.CatererDetails> CatererDetailsList;
      String closestCompany = null;
      float shortestDistance = Integer.MAX_VALUE;
      try {
        // perform request
        String response = ClientIO.doGETRequest(endpoint + request);

        // unmarshal response
        Type listType = new TypeToken<List<CateringCompanyClientImp.CatererDetails>>() {
        }.getType();
        CatererDetailsList = new Gson().fromJson(response, listType);

        // gather required fields
        for (CateringCompanyClientImp.CatererDetails p : CatererDetailsList) {
          float currentDistance = getDistance(this.personalInfo.postcode, p.postcode);
          if (shortestDistance > currentDistance) {
            currentDistance = shortestDistance;
            this.closestCateringPostcode = p.postcode;
            this.closestCateringName = p.business_name;
          }
        }
      } catch (Exception e) {
        e.printStackTrace();
      }

      return this.closestCateringName;
    }
}
