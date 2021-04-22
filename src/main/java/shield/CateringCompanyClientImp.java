package shield;


import com.google.gson.Gson;

public class CateringCompanyClientImp implements CateringCompanyClient {
  final private String endpoint;
  private String name;
  private String postcode;
  private boolean registered = false;

  static final class CatererDetails {
    String business_name;
    String postcode;
  }

  public CateringCompanyClientImp(String endpoint) {
    this.endpoint = endpoint;

  }


  @Override

  public boolean registerCateringCompany(String newName, String newPostcode) {
    boolean success = false;
    String response = "not registered";
    CatererDetails newCompany = new CatererDetails();
    newCompany.business_name = newName;
    newCompany.postcode = newPostcode;
    String data = new Gson().toJson(newCompany);
    System.out.println(data);
    String request = "/registerCateringCompany?business_name=" +
            newName +"&postcode=" +
            newPostcode;
    System.out.println(endpoint+request);


    try {
      response = ClientIO.doPOSTRequest(this.endpoint + request, data);
      System.out.println("Success!");
      if (response.equals("already registered") || response.equals("registered new")) {
        this.name = newName;
        this.postcode = newPostcode;
        this.registered = true;
        success = true;
      }
    } catch (Exception e) {
      e.printStackTrace();
      System.out.println("Failed");
    }
    return success;
  }

  @Override
  public boolean updateOrderStatus(int orderNumber, String newStatus) {
    String request = "/updateOrderStatus?order_id=orderNumber&newStatus=newStatus";
    boolean success = false;
    String response;

    try {
      response = ClientIO.doPOSTRequest(endpoint, request);
      if (response.equals("True")) {
        success = true;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return success;
  }

  @Override //not Sure about parameters
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
