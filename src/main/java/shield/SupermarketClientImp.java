/**
 *
 */

package shield;
public class SupermarketClientImp implements SupermarketClient {
  private String endpoint;
  private String name = "not registered";
  private String postcode = "not registered";
  private boolean registered = false;

  public SupermarketClientImp(String endpoint) {
    this.endpoint = endpoint;
  }

  @Override
  public boolean registerSupermarket (String newName, String newPostcode) {
    if (!MyImpUtils.checkValidPostcode(newPostcode)) return false;
    String request = "/registerSupermarket?" +
            "business_name=" + newName + "" +
            "&postcode=" + newPostcode;
    boolean success = false;
    try {
      String response = ClientIO.doGETRequest(endpoint + request);
      if (response.equals("already registered") || response.equals("registered new")) {
        success = true;
        this.name = newName;
        this.postcode = newPostcode;
        this.registered = true;
      }
    }catch (Exception e) {
      e.printStackTrace();
    }
    return success;
  }

  // **UPDATE2** ADDED METHOD
  @Override
  public boolean recordSupermarketOrder(String CHI, int orderNumber) {
    String request = "/recordSupermarketOrder" +
            "?individual_id=" + CHI +
            "&order_number=" + orderNumber +
            "&supermarket_business_name=" + this.name +
            "&supermarket_postcode=" + this.postcode;
    System.out.println(request);
    boolean success = false;
    try {
      String response = ClientIO.doGETRequest(endpoint + request);

      if (response.equals("True")){
        success = true;
      }

    } catch (Exception e){
      e.printStackTrace();
    }
    return success;
  }

  // **UPDATE**
  @Override
  public boolean updateOrderStatus(int orderNumber, String newStatus) {
    String request = "/updateSupermarketOrderStatus?" +
            "order_id=" + orderNumber +
            "&newStatus=" + newStatus;
    System.out.println(request);
    boolean success = false;

    try {
      String response = ClientIO.doGETRequest(endpoint + request);
      if (response.equals("True")) {
        success = true;
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
    return success;
  }

  @Override
  public boolean isRegistered() {
    return this.registered;
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public String getPostCode() {
    return this.postcode;
  }
}
