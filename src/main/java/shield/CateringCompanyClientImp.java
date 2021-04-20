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


    if (response == "already registered" || response == "registered new"){
      success = true;
      this.name = newName;
      this.postcode = newPostcode;

    }
    else{
      success = false;

    }
    return success;
  }

  @Override
  public boolean updateOrderStatus (int orderNumber, String newStatus) {
    String request = "/updateOrderStatus?order_id=orderNumber&newStatus=newStatus";
    boolean success = true;
    String response = null;

    try{
      response = ClientIO.doPOSTRequest(endpoint, request);
    }catch (Exception e) {
      e.printStackTrace();
    }

    if (response == "False"){
      success = false;
    }
  return success;
  }

  @Override //not Sure about parameters
  public boolean isRegistered () {
    String request = "/getCaterers";
    Boolean success = true;
    List<provider> providerList = new ArrayList<provider>();
    List<String> providerNames = new ArrayList<String>();

    try {
      // perform request
      String response = ClientIO.doGETRequest(endpoint + request);

      // unmarshal response
      Type listType = new TypeToken<List<provider>>() {} .getType();
      providerNames = new Gson().fromJson(response, listType);

      // gather required fields
      for (provider b : providerList) {
        providerNames.add(b.name);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    if (providerNames.contains(this.name) == false){
      success = false;
    }

    return success;
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
