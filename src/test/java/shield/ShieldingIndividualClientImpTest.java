package shield;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.function.Executable;

import java.util.*;
import java.time.LocalDateTime;
import java.io.InputStream;
import java.util.function.BooleanSupplier;


import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */

public class ShieldingIndividualClientImpTest {
  private final static String clientPropsFilename = "client.cfg";

  private Properties clientProps;
  private ShieldingIndividualClient client;
  private CateringCompanyClientImp cateringCompanyClient;

  private Properties loadProperties(String propsFilename) {
    ClassLoader loader = Thread.currentThread().getContextClassLoader();
    Properties props = new Properties();

    try {
      InputStream propsStream = loader.getResourceAsStream(propsFilename);
      props.load(propsStream);
    } catch (Exception e) {
      e.printStackTrace();
    }

    return props;
  }

  public int generateRandomNumber(int min, int max) {
    Random rand = new Random();
    return rand.nextInt((max - min) + 1) + min;
  }



  @BeforeEach
  public void setup() {
    clientProps = loadProperties(clientPropsFilename);
    client = new ShieldingIndividualClientImp(clientProps.getProperty("endpoint"));
    cateringCompanyClient = new CateringCompanyClientImp(clientProps.getProperty("endpoint"));

  }


  @RepeatedTest(20)
  @DisplayName("registerSI Test: New User Success Scenario")
  public void testSuccessSINewRegistration() {
    String validCHI = MyTestUtils.generateValidCHI();

    assertTrue(client.registerShieldingIndividual(validCHI));
    assertTrue(client.isRegistered());
    assertEquals(client.getCHI(), validCHI);
    assertTrue(client.registerShieldingIndividual(validCHI));
  }

  @RepeatedTest(20)
  @DisplayName("registerSI: Failure Scenarios")
  public void testFailureSINewRegistration() {
    String invalidMax = String.valueOf(generateRandomNumber(32, 99));
    String invalidMin = MyTestUtils.formatForValidCHI(generateRandomNumber(-99, 0));
    String invalidCHIMax = invalidMax + "01" + generateRandomNumber(100000, 999999);
    String invalidCHIMin = invalidMin + "01" + generateRandomNumber(100000, 999999);
    assertFalse(client.registerShieldingIndividual(invalidCHIMax));
    assertFalse(client.registerShieldingIndividual(invalidCHIMin));

    invalidMax = MyTestUtils.formatForValidCHI(generateRandomNumber(13, 99));
    invalidMin = MyTestUtils.formatForValidCHI(generateRandomNumber(-99, 0));
    invalidCHIMax = "01" + invalidMax + generateRandomNumber(100000, 999999);
    invalidCHIMin = "01" + invalidMin + generateRandomNumber(100000, 999999);
    assertFalse(client.registerShieldingIndividual(invalidCHIMax));
    assertFalse(client.registerShieldingIndividual(invalidCHIMin));

    String tooShortCHI = MyTestUtils.generateValidCHI().substring(1);
    String tooLongCHI = MyTestUtils.generateValidCHI() + "1";
    assertFalse(client.registerShieldingIndividual(tooShortCHI));
    assertFalse(client.registerShieldingIndividual(tooLongCHI));
  }

  @Test
  @DisplayName("showFoodBoxes Test")
  public void testShowFoodBoxes() {
    Collection<String> noneBoxIDs = client.showFoodBoxes("none");
    assertEquals(3, noneBoxIDs.size());
    Collection<String> pollotarianBoxIDs = client.showFoodBoxes("pollotarian");
    assertEquals(1, pollotarianBoxIDs.size());
    Collection<String> veganBoxIDs = client.showFoodBoxes("vegan");
    assertEquals(1, veganBoxIDs.size());
    Collection<String> allBoxIDs = client.showFoodBoxes("");
    assertEquals(5, allBoxIDs.size());
  }

  @Test
  @DisplayName("placeOrder Test")
  public void testPlaceOrder() {
    assertTrue(client.registerShieldingIndividual(MyTestUtils.generateValidCHI()));
    assertTrue(cateringCompanyClient.registerCateringCompany("c", MyTestUtils.generateValidPostcode()));
    assertTrue(client.pickFoodBox(1));
    assertTrue(client.placeOrder());
    assertEquals(1, client.getOrderNumbers().size());
    int orderId = ((List<Integer>) client.getOrderNumbers()).get(0);
    client.requestOrderStatus(orderId);
    assertEquals("order has been placed", client.getStatusForOrder(orderId));
  }

  @Test
  @DisplayName("editOrder Test")
  public void testEditOrder() {
    assertTrue(client.registerShieldingIndividual(MyTestUtils.generateValidCHI()));
    assertTrue(cateringCompanyClient.registerCateringCompany("c", MyTestUtils.generateValidPostcode()));
    assertTrue(client.pickFoodBox(1));
    assertTrue(client.placeOrder());
    int orderId = ((List<Integer>) client.getOrderNumbers()).get(0);
    assertTrue(client.setItemQuantityForOrder(2, orderId, 1));
    assertTrue(client.editOrder(orderId));

  }

  @Test
  @DisplayName("cancelOrder Test")
  public void testCancelOrder() {
    assertTrue(client.registerShieldingIndividual(MyTestUtils.generateValidCHI()));
    assertTrue(cateringCompanyClient.registerCateringCompany("c", MyTestUtils.generateValidPostcode()));
    assertTrue(client.pickFoodBox(1));
    assertTrue(client.placeOrder());
    assertEquals(1, client.getOrderNumbers().size());
    int orderId = ((List<Integer>) client.getOrderNumbers()).get(0);
    assertTrue(client.cancelOrder(orderId));
    client.requestOrderStatus(orderId);
    assertEquals("order has been cancelled", client.getStatusForOrder(orderId));
  }

  @Test
  @DisplayName("requestOrderStatus Test")
  public void testRequestOrderStatus() {
    assertTrue(client.registerShieldingIndividual(MyTestUtils.generateValidCHI()));
    assertTrue(cateringCompanyClient.registerCateringCompany("c", MyTestUtils.generateValidPostcode()));
    assertTrue(client.pickFoodBox(1));
    assertTrue(client.placeOrder());

    int orderId = ((List<Integer>) client.getOrderNumbers()).get(0);
    client.requestOrderStatus(orderId);
    assertEquals("order has been placed", client.getStatusForOrder(orderId));

    assertTrue(cateringCompanyClient.updateOrderStatus(orderId, "packed"));
    client.requestOrderStatus(orderId);
    assertEquals("order is packed", client.getStatusForOrder(orderId));

    assertTrue(cateringCompanyClient.updateOrderStatus(orderId, "dispatched"));
    client.requestOrderStatus(orderId);
    assertEquals("order has been dispatched", client.getStatusForOrder(orderId));

    assertTrue(cateringCompanyClient.updateOrderStatus(orderId, "delivered"));
    client.requestOrderStatus(orderId);
    assertEquals("order has been delivered", client.getStatusForOrder(orderId));

    assertTrue(client.placeOrder());
    orderId = ((List<Integer>) client.getOrderNumbers()).get(1);
    assertTrue(client.cancelOrder(orderId));
    client.requestOrderStatus(orderId);
    assertEquals("order has been cancelled", client.getStatusForOrder(orderId));
  }

  @Test
  @DisplayName("getCateringCompanies Test")
  public void testGetCateringCompanies() {
    int newRegistrations = 10;
    int currentSize = client.getCateringCompanies().size();
    for (int i = 0; i < newRegistrations; i++) {
      client.registerShieldingIndividual(MyTestUtils.generateValidCHI());
    }
    int newSize = client.getCateringCompanies().size();
    assertFalse(newSize - currentSize == 10);
  }

  @RepeatedTest(20)
  @DisplayName("getDistance Test")
  public void testGetDistance() {
    String postcode1 = MyTestUtils.generateValidPostcode();
    String postcode2 = MyTestUtils.generateValidPostcode();
    if (postcode1.equals(postcode2)) {
      assertEquals(0.0, client.getDistance(postcode1, postcode2));
    } else {
      assertTrue(client.getDistance(postcode1, postcode2) > 0);
    }
  }

  @Test
  @DisplayName("getFoodBoxNumber Test")
  public void testGetFoodBoxNumber() {
    int response = client.getFoodBoxNumber();
    assertTrue(response == 5);

  }

  @Test
  @DisplayName("getDietaryPreferenceForFoodBox Test")
  public void testGetDietaryPreferenceForFoodBox() {
    assertEquals("none", client.getDietaryPreferenceForFoodBox(1));
    assertEquals("pollotarian", client.getDietaryPreferenceForFoodBox(2));
    assertEquals("none", client.getDietaryPreferenceForFoodBox(3));
    assertEquals("none", client.getDietaryPreferenceForFoodBox(4));
    assertEquals("vegan", client.getDietaryPreferenceForFoodBox(5));

  }

  @Test
  @DisplayName("getItemsNumberForFoodBox Test")
  public void testGetItemsNumberForFoodBox() {
    assertEquals(3, client.getItemsNumberForFoodBox(1));
    assertEquals(3, client.getItemsNumberForFoodBox(2));
    assertEquals(3, client.getItemsNumberForFoodBox(3));
    assertEquals(4, client.getItemsNumberForFoodBox(4));
    assertEquals(3, client.getItemsNumberForFoodBox(5));
  }

  @Test
  @DisplayName("getItemIdsForFoodBox Test")
  public void testGetItemIdsForFoodBox() {
    assertEquals(Arrays.asList(1, 2, 6), client.getItemIdsForFoodBox(1));
    assertEquals(Arrays.asList(1, 3, 7), client.getItemIdsForFoodBox(2));
    assertEquals(Arrays.asList(3, 4, 8), client.getItemIdsForFoodBox(3));
    assertEquals(Arrays.asList(13, 11, 8, 9), client.getItemIdsForFoodBox(4));
    assertEquals(Arrays.asList(9, 11, 12), client.getItemIdsForFoodBox(5));
  }

  @Test
  @DisplayName("getItemNameForFoodBox Test")
  public void testGetItemNameForFoodBox() {
    assertEquals("cucumbers", client.getItemNameForFoodBox(1, 1));
    assertEquals("tomatoes", client.getItemNameForFoodBox(2, 1));
    assertEquals("pork", client.getItemNameForFoodBox(6, 1));

    assertEquals("cucumbers", client.getItemNameForFoodBox(1, 2));
    assertEquals("onions", client.getItemNameForFoodBox(3, 2));
    assertEquals("chicken", client.getItemNameForFoodBox(7, 2));

    assertEquals("onions", client.getItemNameForFoodBox(3, 3));
    assertEquals("carrots", client.getItemNameForFoodBox(4, 3));
    assertEquals("bacon", client.getItemNameForFoodBox(8, 3));

    assertEquals("cabbage", client.getItemNameForFoodBox(13, 4));
    assertEquals("avocado", client.getItemNameForFoodBox(11, 4));
    assertEquals("bacon", client.getItemNameForFoodBox(8, 4));
    assertEquals("oranges", client.getItemNameForFoodBox(9, 4));

    assertEquals("oranges", client.getItemNameForFoodBox(9, 5));
    assertEquals("avocado", client.getItemNameForFoodBox(11, 5));
    assertEquals("mango", client.getItemNameForFoodBox(12, 5));
  }

  @Test
  @DisplayName("getItemQuantityForFoodBox Test")
  public void testGetItemQuantityForFoodBox() {
    List<Integer> boxItemIds = Arrays.asList(1, 2, 6);
    assertEquals(1, client.getItemQuantityForFoodBox(1, 1));
    assertEquals(2, client.getItemQuantityForFoodBox(2, 1));
    assertEquals(1, client.getItemQuantityForFoodBox(6, 1));
    for (int i = 1; i < 14; i++)
      if (!boxItemIds.contains(i)) {
        assertEquals(0, client.getItemQuantityForFoodBox(i, 1));
      }

    boxItemIds = Arrays.asList(1, 3, 7);
    assertEquals(2, client.getItemQuantityForFoodBox(1, 2));
    assertEquals(1, client.getItemQuantityForFoodBox(3, 2));
    assertEquals(1, client.getItemQuantityForFoodBox(7, 2));
    for (int i = 1; i < 14; i++)
      if (!boxItemIds.contains(i)) {
        assertEquals(0, client.getItemQuantityForFoodBox(i, 2));
      }

    boxItemIds = Arrays.asList(3, 4, 8);
    assertEquals(1, client.getItemQuantityForFoodBox(3, 3));
    assertEquals(2, client.getItemQuantityForFoodBox(4, 3));
    assertEquals(1, client.getItemQuantityForFoodBox(8, 3));
    for (int i = 1; i < 14; i++)
      if (!boxItemIds.contains(i)) {
        assertEquals(0, client.getItemQuantityForFoodBox(i, 3));
      }

    boxItemIds = Arrays.asList(13, 11, 8, 9);
    assertEquals(1, client.getItemQuantityForFoodBox(13, 4));
    assertEquals(1, client.getItemQuantityForFoodBox(11, 4));
    assertEquals(1, client.getItemQuantityForFoodBox(8, 4));
    assertEquals(1, client.getItemQuantityForFoodBox(9, 4));
    for (int i = 1; i < 14; i++)
      if (!boxItemIds.contains(i)) {
        assertEquals(0, client.getItemQuantityForFoodBox(i, 4));
      }

    List<Integer> boxOneItemIds = Arrays.asList(9, 11, 12);
    assertEquals(1, client.getItemQuantityForFoodBox(9, 5));
    assertEquals(1, client.getItemQuantityForFoodBox(11, 5));
    assertEquals(1, client.getItemQuantityForFoodBox(12, 5));
    for (int i = 1; i < 14; i++)
      if (!boxOneItemIds.contains(i)) {
        assertEquals(0, client.getItemQuantityForFoodBox(i, 5));
      }

  }

  @Test
  @DisplayName("pickFoodBox Test")
  public void testPickFoodBox() {
    client.registerShieldingIndividual(MyTestUtils.generateValidCHI());
    assertTrue(client.pickFoodBox(1));
    assertTrue(client.pickFoodBox(2));
    assertTrue(client.pickFoodBox(3));
    assertTrue(client.pickFoodBox(4));
    assertTrue(client.pickFoodBox(5));
    assertFalse(client.pickFoodBox(6));
  }

  @Test
  @DisplayName("changeItemQuantityForPickedFoodBox Test")
  public void testChangeItemQuantityForPickedFoodBox() {
    client.pickFoodBox(1);
    assertFalse(client.changeItemQuantityForPickedFoodBox(1, 1));
    assertTrue(client.changeItemQuantityForPickedFoodBox(2, 1));
    assertFalse(client.changeItemQuantityForPickedFoodBox(6, 1));
  }

  @Test
  @DisplayName("getOrderNumbers Test")
  public void testGetOrderNumbers() {
    assertTrue(client.registerShieldingIndividual(MyTestUtils.generateValidCHI()));
    assertTrue(cateringCompanyClient.registerCateringCompany("c", MyTestUtils.generateValidPostcode()));
    assertTrue(client.pickFoodBox(1));
    assertTrue(client.placeOrder());
    assertEquals(1, client.getOrderNumbers().size());
    assertTrue(client.placeOrder());
    assertEquals(2, client.getOrderNumbers().size());
    assertTrue(client.placeOrder());
    assertEquals(3, client.getOrderNumbers().size());
  }

  @Test
  @DisplayName("getStatusForOrder Test")
  public void testGetStatusForOrder() {
    assertTrue(client.registerShieldingIndividual(MyTestUtils.generateValidCHI()));
    assertTrue(cateringCompanyClient.registerCateringCompany("c", MyTestUtils.generateValidPostcode()));
    assertTrue(client.pickFoodBox(1));

    assertTrue(client.placeOrder());
    assertTrue(client.placeOrder());
    assertTrue(client.placeOrder());
    assertTrue(client.placeOrder());
    assertTrue(client.placeOrder());

    int orderIdPlaced = ((List<Integer>) client.getOrderNumbers()).get(0);
    int orderIdPacked = ((List<Integer>) client.getOrderNumbers()).get(1);
    int orderIdDispatched = ((List<Integer>) client.getOrderNumbers()).get(2);
    int orderIdDelivered = ((List<Integer>) client.getOrderNumbers()).get(3);
    int orderIdCancelled = ((List<Integer>) client.getOrderNumbers()).get(4);

    assertTrue(cateringCompanyClient.updateOrderStatus(orderIdPacked, "packed"));
    assertTrue(cateringCompanyClient.updateOrderStatus(orderIdDispatched, "dispatched"));
    assertTrue(cateringCompanyClient.updateOrderStatus(orderIdDelivered, "delivered"));
    assertTrue(client.cancelOrder(orderIdCancelled));

    assertTrue(client.requestOrderStatus(orderIdPacked));
    assertTrue(client.requestOrderStatus(orderIdDispatched));
    assertTrue(client.requestOrderStatus(orderIdDelivered));
    assertTrue(client.requestOrderStatus(orderIdCancelled));


    assertEquals("order has been placed", client.getStatusForOrder(orderIdPlaced));
    assertEquals("order is packed", client.getStatusForOrder(orderIdPacked));
    assertEquals("order has been dispatched", client.getStatusForOrder(orderIdDispatched));
    assertEquals("order has been delivered", client.getStatusForOrder(orderIdDelivered));
    assertEquals("order has been cancelled", client.getStatusForOrder(orderIdCancelled));
  }

  @Test
  @DisplayName("getItemIdsForOrder Test")
  public void testGetItemIdsForOrder() {
    assertTrue(client.registerShieldingIndividual(MyTestUtils.generateValidCHI()));
    assertTrue(cateringCompanyClient.registerCateringCompany("c", MyTestUtils.generateValidPostcode()));
    assertTrue(client.pickFoodBox(1));
    assertTrue(client.placeOrder());
    assertTrue(client.pickFoodBox(4));
    assertTrue(client.placeOrder());

    int orderId = ((List<Integer>) client.getOrderNumbers()).get(0);
    List<Integer> itemIds = (List<Integer>) client.getItemIdsForOrder(orderId);
    List<Integer> correctIds = Arrays.asList(1, 2, 6);
    for (int i = 0; i < itemIds.size(); i++) {
      assertEquals(itemIds.get(i), correctIds.get(i));
    }
    orderId = ((List<Integer>) client.getOrderNumbers()).get(1);
    itemIds = (List<Integer>) client.getItemIdsForOrder(orderId);
    correctIds = Arrays.asList(13, 11, 8, 9);
    for (int i = 0; i < itemIds.size(); i++) {
      assertEquals(itemIds.get(i), correctIds.get(i));
    }
  }

  @Test
  @DisplayName("getItemNameForOrder Test")
  public void testGetItemNameForOrder() {
    assertTrue(client.registerShieldingIndividual(MyTestUtils.generateValidCHI()));
    assertTrue(cateringCompanyClient.registerCateringCompany("c", MyTestUtils.generateValidPostcode()));

    assertTrue(client.pickFoodBox(1));
    assertTrue(client.placeOrder());
    int orderId = ((List<Integer>) client.getOrderNumbers()).get(0);
    List<Integer> correctIds = Arrays.asList(1, 2, 6);
    List<String> correctNames = Arrays.asList("cucumbers", "tomatoes", "pork");
    for (int i = 0; i < correctIds.size(); i++) {
      String itemName = client.getItemNameForOrder(correctIds.get(i), orderId);
      assertEquals(correctNames.get(i), itemName);
    }
    assertTrue(client.pickFoodBox(4));
    assertTrue(client.placeOrder());
    orderId = ((List<Integer>) client.getOrderNumbers()).get(1);
    correctIds = Arrays.asList(13, 11, 8, 9);
    correctNames = Arrays.asList("cabbage", "avocado", "bacon", "oranges");
    for (int i = 0; i < correctIds.size(); i++) {
      String itemName = client.getItemNameForOrder(correctIds.get(i), orderId);
      assertEquals(correctNames.get(i), itemName);
    }
  }

  @Test
  @DisplayName("getItemQuantityForOrder Test")
  public void testGetItemQuantityForOrder() {
    assertTrue(client.registerShieldingIndividual(MyTestUtils.generateValidCHI()));
    assertTrue(cateringCompanyClient.registerCateringCompany("c", MyTestUtils.generateValidPostcode()));

    assertTrue(client.pickFoodBox(1));
    assertTrue(client.placeOrder());
    int orderId = ((List<Integer>) client.getOrderNumbers()).get(0);
    List<Integer> correctIds = Arrays.asList(1, 2, 6);
    List<Integer> correctQuantities = Arrays.asList(1,2,1);
    for (int i = 0; i < correctIds.size(); i++) {
      int itemQuantity = client.getItemQuantityForOrder(correctIds.get(i), orderId);
      assertEquals((int) correctQuantities.get(i), itemQuantity);
    }
    assertTrue(client.pickFoodBox(4));
    assertTrue(client.placeOrder());
    orderId = ((List<Integer>) client.getOrderNumbers()).get(1);
    correctIds = Arrays.asList(13, 11, 8, 9);
    correctQuantities = Arrays.asList(1, 1, 1, 1);
    for (int i = 0; i < correctIds.size(); i++) {
      int itemQuantity = client.getItemQuantityForOrder(correctIds.get(i), orderId);
      assertEquals((int) correctQuantities.get(i), itemQuantity);
    }
  }

  @Test
  @DisplayName("setItemQuantityForOrder Test")
  public void testSetItemQuantityForOrder() {
    assertTrue(client.registerShieldingIndividual(MyTestUtils.generateValidCHI()));
    assertTrue(cateringCompanyClient.registerCateringCompany("c", MyTestUtils.generateValidPostcode()));
    assertTrue(client.pickFoodBox(1));
    assertTrue(client.placeOrder());
    int orderId = ((List<Integer>) client.getOrderNumbers()).get(0);
    assertEquals(2, client.getItemQuantityForOrder(2, orderId));
    assertTrue(client.setItemQuantityForOrder(2, orderId, 1));
    assertEquals(1, client.getItemQuantityForOrder(2, orderId));
  }

  @Test
  @DisplayName("getClosestCateringCompany Test")
  public void testGetClosestCateringCompany() {
    cateringCompanyClient.registerCateringCompany("Portrait_Gallery", "EH4_3DR");
    cateringCompanyClient.registerCateringCompany("National_Museum", "EH1_1JF");
    cateringCompanyClient.registerCateringCompany("The_Mound", "EH2_2EL");
    cateringCompanyClient.registerCateringCompany("Appleton_Tower", "EH4_3DR");
    cateringCompanyClient.registerCateringCompany("Start_of_Meadow_Walk", "EH9_1LY");
    try {
      client.getClosestCateringCompany();
    }
    catch (Exception e) {
      assertEquals(e.getMessage(), "Shielding User postcode or caterer postcode was invalid");
    }
      client.registerShieldingIndividual(MyTestUtils.generateValidCHI());
  }
}