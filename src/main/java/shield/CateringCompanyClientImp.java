/**
 *
 */

package shield;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class CateringCompanyClientImp implements CateringCompanyClient {
  private String endpoint;
  private String name;
  private String postcode;
  private boolean registered = false;


  // internal field only used for transmission purposes
  final class provider {
    // a field marked as transient is skipped in marshalling/unmarshalling
    transient List<String> details;
    String providerID;
    String name;
    String postcode;
  }

  public CateringCompanyClientImp(String endpoint) {
    this.endpoint = endpoint;
    this.name = null;
    this.postcode = null;
  }


  @Override
  public boolean registerCateringCompany(String newName, String newPostcode) {
    String request = "/registerCateringCompany?business_name=newName&postcode=newPostcode";
    boolean success;
    String response = null;

    try {
      response = ClientIO.doPOSTRequest(endpoint, request);
    } catch (Exception e) {
      e.printStackTrace();
    }


    if (response == "already registered" || response == "registered new") {
      success = true;
      this.name = newName;
      this.postcode = newPostcode;
      this.registered = true;

    } else {
      success = false;

    }
    return success;
  }

  @Override
  public boolean updateOrderStatus(int orderNumber, String newStatus) {
    String request = "/updateOrderStatus?order_id=orderNumber&newStatus=newStatus";
    boolean success = true;
    String response = null;

    try {
      response = ClientIO.doPOSTRequest(endpoint, request);
      if (response == "False") {
        success = false;
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
