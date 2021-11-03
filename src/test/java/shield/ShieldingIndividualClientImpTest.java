package shield;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.function.Executable;

import java.util.*;
import java.time.LocalDateTime;
import java.io.InputStream;
import java.util.function.BooleanSupplier;
import java.util.stream.IntStream;


import static org.junit.jupiter.api.Assertions.*;

/**
 * All tests for the Shielding Individual Class
 */

public class ShieldingIndividualClientImpTest {
  private final static String clientPropsFilename = "client.cfg";

  private Properties clientProps;
  private ShieldingIndividualClient client;
  private CateringCompanyClientImp cateringCompanyClient;

  /**
   * Gets property object from cfg file so that we can get address for HTTP requests
   * @param propsFilename filename to load server address from
   * @return a properties file that is used to get the address for the HTTP Requests
   */
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

  /**
   * Sets what will be run before each test
   */
  @BeforeEach
  public void setup() {
    clientProps = loadProperties(clientPropsFilename);
    client = new ShieldingIndividualClientImp(clientProps.getProperty("endpoint"));
    cateringCompanyClient = new CateringCompanyClientImp(clientProps.getProperty("endpoint"));

  }

  /**
   * Test for registering a new Shielding Individual
   * The test implements and/or tests the following:
   * 
   * - Registering a new Shielding Individual with a valid CHI, using the method
   *   obtained from <code>generateValidCHI()</code> from the class <code>MyTestUtils</code>
   * - Checking that the private variables <code>CHI, registered</code> have 
   *   been given the correct values.
   * - Checking that a re-registration returns true
   * - Generating invalid CHI's by changing the day, month and year individually
   *   to invalid values and asserting that the <code>shieldingIndividualNewRegistration</code>
   *   method returns false.
   */
  @RepeatedTest(20)
  @DisplayName("registerSI Test: New User Success Scenario")
  public void testShieldingIndividualNewRegistration() {
    String validCHI = MyTestUtils.generateValidCHI();

    assertTrue(client.registerShieldingIndividual(validCHI));
    assertTrue(client.isRegistered());
    assertEquals(client.getCHI(), validCHI);
    assertTrue(client.registerShieldingIndividual(validCHI));

    String invalidMax = String.valueOf(MyTestUtils.generateRandomNumber(32, 99));
    String invalidMin = MyTestUtils.formatForValidCHI(MyTestUtils.generateRandomNumber(-99, 0));
    String invalidCHIMax = invalidMax + "01" + MyTestUtils.generateRandomNumber(100000, 999999);
    String invalidCHIMin = invalidMin + "01" + MyTestUtils.generateRandomNumber(100000, 999999);
    assertFalse(client.registerShieldingIndividual(invalidCHIMax));
    assertFalse(client.registerShieldingIndividual(invalidCHIMin));

    invalidMax = MyTestUtils.formatForValidCHI(MyTestUtils.generateRandomNumber(13, 99));
    invalidMin = MyTestUtils.formatForValidCHI(MyTestUtils.generateRandomNumber(-99, 0));
    invalidCHIMax = "01" + invalidMax + MyTestUtils.generateRandomNumber(100000, 999999);
    invalidCHIMin = "01" + invalidMin + MyTestUtils.generateRandomNumber(100000, 999999);
    assertFalse(client.registerShieldingIndividual(invalidCHIMax));
    assertFalse(client.registerShieldingIndividual(invalidCHIMin));

    String tooShortCHI = MyTestUtils.generateValidCHI().substring(1);
    String tooLongCHI = MyTestUtils.generateValidCHI() + "1";
    assertFalse(client.registerShieldingIndividual(tooShortCHI));
    assertFalse(client.registerShieldingIndividual(tooLongCHI));
  }

  /**
   * Test for getting Food Boxes.
   * It uses the provided Food Box file to check that the correct Ids are returned 
   * for each Food Box using the <code>showFoodBoxes</code> method.
   * to get the number of Food Boxes in the <code>food_box.txt</code> file
   */
  @Test
  @DisplayName("showFoodBoxes Test")
  public void testShowFoodBoxes() {
    List<String> noneBoxIDs = (List<String>) client.showFoodBoxes("none");
    assertEquals(3, noneBoxIDs.size());

    List<String> correctIds = Arrays.asList("1", "3", "4");
    IntStream.range(0, noneBoxIDs.size()).forEach(i -> assertEquals(noneBoxIDs.get(i), correctIds.get(i)));
    
    List<String> pollotarianBoxIDs = (List<String>) client.showFoodBoxes("pollotarian");
    assertEquals("2", pollotarianBoxIDs.get(0));

    List<String> veganBoxIDs = (List<String>) client.showFoodBoxes("vegan");
    assertEquals("5", veganBoxIDs.get(0));

    Collection<String> allBoxIDs = client.showFoodBoxes("");
    assertEquals(client.getFoodBoxNumber(), allBoxIDs.size());
  }

  /**
   * Test for placing an order. This implements and/or tests the following:
   * - Registering a new catering company and a Shielding Individual,
   * - Picking Food Box 1 and placing the order
   * - The number of orders obtained using the <code>getOrderNumbers()</code> method 
   *   has increased  by 1.
   * - The status for the new order is that it has been placed
   */
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

  /**
   * Test for editing an order. This implements and/or tests the following:
   * - Registering a new catering company and a shielding individual.
   * - Picking Food Box 1 and placing an order.
   * - Getting the orderID using <code>getOrderNumbers</code> Only one order has
   *   been placed for this client, so index 0 will contain the order that was placed.
   * - Set a new item quantity using <code>setItemQuantityForOrder</code>
   * - Using the <code>editOrder</code> method to send the edited Food Box to the
   *   server.
   */
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

  /**
   * Test for editing an order. This implements and/or tests the following:
   * - Registering a new catering company and a shielding individual.
   * - Picking Food Box 1 and placing an order.
   * - Getting the orderID using <code>getOrderNumbers</code> Only one order has
   *   been placed for this client, so index 0 will contain the order that was placed
   * - USing <code>cancelOrder()</code> to cancel the order.
   * - Getting the new orderStatus using the methods <code>requestOrderStatus</code>
   *   and <code>getStatusForOrder</code> to check that the status for the order
   *   has been changed, and that the order has indeed been cancelled.
   */
  @Test
  @DisplayName("cancelOrder Test")
  public void testCancelOrder() {
    assertTrue(client.registerShieldingIndividual(MyTestUtils.generateValidCHI()));
    assertTrue(cateringCompanyClient.registerCateringCompany("c", MyTestUtils.generateValidPostcode()));
    assertTrue(client.pickFoodBox(1));
    assertTrue(client.placeOrder());
    int orderId = ((List<Integer>) client.getOrderNumbers()).get(0);
    assertTrue(client.cancelOrder(orderId));
    client.requestOrderStatus(orderId);
    assertEquals("order has been cancelled", client.getStatusForOrder(orderId));
  }

  /**
   * Test for requesting the status of an order. This implements and/or tests the following:
   * - Registering a new Catering Company and a Shielding Individual.
   * - Picking Food Box 1, places an order and checking the status is correct.
   * - Updating t
   * - For the order being placed, packed and dispatched, it uses the updateOrderStatus
   * from the caterer class, requests the status to change it locally and asserts that
   * new status is correct.
   * - For order cancelled, it cancels the order, requests the status and checks it is
   * correct.
   */
  @Test
  @DisplayName("requestOrderStatus Test")
  public void testRequestOrderStatus() {
    assertTrue(client.registerShieldingIndividual(MyTestUtils.generateValidCHI()));
    assertTrue(cateringCompanyClient.registerCateringCompany("c", MyTestUtils.generateValidPostcode()));
    assertTrue(client.pickFoodBox(1));

    assertTrue(client.placeOrder());
    assertTrue(client.placeOrder());
    assertTrue(client.placeOrder());
    assertTrue(client.placeOrder());
    assertTrue(client.placeOrder());

    // this is needed for the integration test with getStatusFromOrder
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
  }

  /**
   * Testing for getting all the catering companies.
   * This implements and/or tests the following:
   * - Getting the current number of catering companies
   * - Using the pre set value x in <code>newRegistrations</code> to register x
   *   new catering companies
   * - Obtating the new number of catering companies, and asserting that this
   *   value has increased by x
   */
  @Test
  @DisplayName("getCateringCompanies Test")
  public void testGetCateringCompanies() {
    int newRegistrations = 10;
    int currentSize = client.getCateringCompanies().size();
    for (int i = 0; i < newRegistrations; i++) {
      cateringCompanyClient.registerCateringCompany("Caterer", MyTestUtils.generateValidPostcode());
    }
    int newSize = client.getCateringCompanies().size();
    assertEquals(10, newSize - currentSize);
  }

  /**
   * Test for getting distance between two postcodes.
   * This implements and/or tests the following:
   * - Generating two valid postcodes via the <code>generateValidPostcode</code>
   *   method from the <code>MyTestUtils</code> class
   * - Asserting that using the method <code>getDistance</code> on the same postcode
   *   returns a value of 0.
   * - Checking that, if two generated postcodes are not the same, that the value
   *   generated by <code>getDistance</code> is greater that 0.
   */
  @RepeatedTest(20)
  @DisplayName("getDistance Test")
  public void testGetDistance() {
    String postcode1 = MyTestUtils.generateValidPostcode();
    String postcode2 = MyTestUtils.generateValidPostcode();
    assertEquals(0.0, client.getDistance(postcode1, postcode1));
    if (postcode1.equals(postcode2)) {
      assertEquals(0.0, client.getDistance(postcode1, postcode2));
    } else {
      assertTrue(client.getDistance(postcode1, postcode2) > 0);
    }
  }

  /**
   * Test for getting the number of available Food Boxes. This checks that the
   * number of Food Boxes obtained via <code>getFoodBoxNumber()</code> is 5, which
   * is the number of Food Boxes in the <code>food_boxes.txt</code> file from the
   * server files.
   */
  @Test
  @DisplayName("getFoodBoxNumber Test")
  public void testGetFoodBoxNumber() {
    assertEquals(5, client.getFoodBoxNumber());

  }

  /**
   * Test for getting the dietary preference of the available Food Boxes.
   * This checks that for each Food Box ID in the <code>food_boxes.txt</code> file,
   * the correct diet type is returned. These correct types are obtained directly
   * from the file <code>food_boxes.txt</code> from the server files.
   */
  @Test
  @DisplayName("getDietaryPreferenceForFoodBox Test")
  public void testGetDietaryPreferenceForFoodBox() {
    assertEquals("none", client.getDietaryPreferenceForFoodBox(1));
    assertEquals("pollotarian", client.getDietaryPreferenceForFoodBox(2));
    assertEquals("none", client.getDietaryPreferenceForFoodBox(3));
    assertEquals("none", client.getDietaryPreferenceForFoodBox(4));
    assertEquals("vegan", client.getDietaryPreferenceForFoodBox(5));

  }

  /**
   * Test for getting the number of items in each of the available Food Boxes.
   * It checks for each Food Box ID in the <code>food_boxes.txt</code> file, the
   * correct number of items are returned. These correct types are obtained directly
   * from the file <code>food_boxes.txt</code> from the server files.
   */
  @Test
  @DisplayName("getItemsNumberForFoodBox Test")
  public void testGetItemsNumberForFoodBox() {
    assertEquals(3, client.getItemsNumberForFoodBox(1));
    assertEquals(3, client.getItemsNumberForFoodBox(2));
    assertEquals(3, client.getItemsNumberForFoodBox(3));
    assertEquals(4, client.getItemsNumberForFoodBox(4));
    assertEquals(3, client.getItemsNumberForFoodBox(5));
  }

  /**
   * Test for getting the item IDs in each of the available Food Boxes.
   * It checks for each Food Box ID in the <code>food_boxes.txt</code> file, the
   * correct item ID for each item is returned. These correct types are obtained
   * directly from the file <code>food_boxes.txt</code> from the server files
   */
  @Test
  @DisplayName("getItemIdsForFoodBox Test")
  public void testGetItemIdsForFoodBox() {
    assertEquals(Arrays.asList(1, 2, 6), client.getItemIdsForFoodBox(1));
    assertEquals(Arrays.asList(1, 3, 7), client.getItemIdsForFoodBox(2));
    assertEquals(Arrays.asList(3, 4, 8), client.getItemIdsForFoodBox(3));
    assertEquals(Arrays.asList(13, 11, 8, 9), client.getItemIdsForFoodBox(4));
    assertEquals(Arrays.asList(9, 11, 12), client.getItemIdsForFoodBox(5));
  }

  /**
   * Test for getting the item names of the available Food Boxes.
   * This implements and/or tests the following:
   * - Checking that using the <code>getItemNameForFoodBox</code> method for an
   *   item not in a Food Fox will return false.
   * - Checking that for every Food Box, the correct name is returned from the
   *   corresponding item, using the <code>getItemNameForFoodBox</code> method.
   */
  @Test
  @DisplayName("getItemNameForFoodBox Test")
  public void testGetItemNameForFoodBox() {
    assertEquals("Not Found", client.getItemNameForFoodBox(3,1));

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

  /**
   * Test for getting the item quantities of the available Food Boxes.
   * It checks for each Food Box ID in the <code>food_boxes.txt</code> file, the
   * correct item quantity for each item is returned. These correct types are obtained
   * directly from the file <code>food_boxes.txt</code> from the server files.
   * It also checks that 0 is returned for every item that is not in the Food Box.
   */
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

  /**
   * Test for picking Food Boxes locally.
   * This implements and/or tests the following:
   *
   * - Registering a new shielding individual
   * - Using the <code>pickFoodBox</code> method to select each the Food Boxes in the
   *   <code>food_boxes.txt</code> file from the server.
   * - Getting a false response when an invalid Food Box ID is used.
   */
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

  /**
   * Test for changing the quantity of an item from the locally picked Food Box.
   * This implements and/or checks the following:
   *
   * - Picking Food Box 1 using the <code>pickFoodBox</code> method
   * - Asserting that the correct error message is thrown when using a
   *   negative quantity.
   * - Asserting that False is returned when using a new quantity that is the
   *   same as the items current quantity.
   * - Asserting that True is returned when using a new quantity that is smaller
   *   than the items current quantity.
   * - Asserting that False is returned when trying to make all quantities 0. This
   *   is done in the last assertion.
   */
  @Test
  @DisplayName("changeItemQuantityForPickedFoodBox Test")
  public void testChangeItemQuantityForPickedFoodBox() {
    client.pickFoodBox(1);
    try {
      client.changeItemQuantityForPickedFoodBox(1, -1);
    } catch (Exception e) {
      assertEquals("Error: Cannot use negative quantity", e.getMessage());
    }
    assertFalse(client.changeItemQuantityForPickedFoodBox(1, 1));
    assertTrue(client.changeItemQuantityForPickedFoodBox(1, 0));
    assertTrue(client.changeItemQuantityForPickedFoodBox(2, 1));
    assertTrue(client.changeItemQuantityForPickedFoodBox(2, 0));
    assertFalse(client.changeItemQuantityForPickedFoodBox(6, 1));
    assertFalse(client.changeItemQuantityForPickedFoodBox(6, 0));

  }

  /**
   * Test for getting all the locally stored order numbers.
   * This implements and/or tests the following:
   * - Registering a new catering company and shielding individual
   * - Picking Food Box 1 from the <code>food_box.txt</code> on the server
   * - Placing three orders, and checking that each time the number of order numbers
   *  obtained via the <code>getOrderNumbers()</code> function increases by 1
   */
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

  /**
   * Test for getting the status of an order stored locally.
   * This implements and/or tests the following:
   *
   * - Registering a new catering company and shielding individual.
   * - Picking Food Box 1 from the <code>food_box.txt</code> on the server and
   *   placing 5 orders.
   * - Getting the 5 different order ID's and using a each of the different status
   *   to update the status of each of the orders via the <code>requestOrderStatus</code>
   *   method from the <code>CateringCompanyClientImp</code> class
   * - Requesting each of the new order status' and asserting that the correct
   *   order status has been given for each order.
   */
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

    // this is needed for the integration test with getStatusFromOrder
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

  /**
   * Test for getting the Item IDs a locally stored order.
   * This implements and/or tests the following:
   * - Registering a new catering company and shielding individual.
   * - Picking Food Box 1 from the <code>food_box.txt</code> on the server and
   *   placing an order
   * - Getting the the order number for placed order, using it to get the
   *   Item IDs, and comparing the returned IDs to the correct IDs. The
   *   correct IDs were obtained via the <code>food_box.txt</code> file.
   * - Repeating these steps from picking the Food Box except using Food Box
   *   4 (has 4 items whereas Food Box 1 has 3)
   */
  @Test
  @DisplayName("getItemIdsForOrder Test")
  public void testGetItemIdsForOrder() {
    assertTrue(client.registerShieldingIndividual(MyTestUtils.generateValidCHI()));
    assertTrue(cateringCompanyClient.registerCateringCompany("c", MyTestUtils.generateValidPostcode()));

    assertTrue(client.pickFoodBox(1));
    assertTrue(client.placeOrder());
    int orderId = ((List<Integer>) client.getOrderNumbers()).get(0);
    List<Integer> itemIds = (List<Integer>) client.getItemIdsForOrder(orderId);
    List<Integer> correctIds = Arrays.asList(1, 2, 6);
    assertEquals(itemIds, correctIds);

    assertTrue(client.pickFoodBox(4));
    assertTrue(client.placeOrder());
    orderId = ((List<Integer>) client.getOrderNumbers()).get(1);
    itemIds = (List<Integer>) client.getItemIdsForOrder(orderId);
    correctIds = Arrays.asList(13, 11, 8, 9);
    assertEquals(itemIds, correctIds);
  }

  /**
   * Test for getting the Item names a locally stored order.
   * This implements and/or tests the following:
   * - Registering a new catering company and shielding individual.
   * - Picking Food Box 1 from the <code>food_box.txt</code> on the server and
   *   placing an order
   * - Getting the the order number for placed order, using it to get the
   *   Item names, and comparing the returned names to the correct names. The
   *   correct names were obtained via the <code>food_box.txt</code> file.
   * - Repeating these steps from picking the Food Box except using Food Box
   *   4 (has 4 items whereas Food Box 1 has 3).
   */
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

  /**
   * Test for getting the Item quantities of a locally stored order.
   * This implements and/or tests the following:
   *
   * - Registering a new catering company and shielding individual.
   * - Picking Food Box 1 from the <code>food_box.txt</code> on the server and
   *   placing an order
   * - Getting the the order number for placed order, using it to get the
   *   Item quantities, and comparing the returned quantities to the correct quantities.
   *   The correct quantities were obtained via the <code>food_box.txt</code> file.
   * - Repeating these steps from picking the Food Box except using Food Box
   *   4 (has 4 items whereas Food Box 1 has 3)
   */
  @Test
  @DisplayName("getItemQuantityForOrder Test")
  public void testGetItemQuantityForOrder() {
    assertTrue(client.registerShieldingIndividual(MyTestUtils.generateValidCHI()));
    assertTrue(cateringCompanyClient.registerCateringCompany("c", MyTestUtils.generateValidPostcode()));

    assertTrue(client.pickFoodBox(1));
    assertTrue(client.placeOrder());
    int orderId = ((List<Integer>) client.getOrderNumbers()).get(0);
    List<Integer> correctIds = Arrays.asList(1, 2, 6);
    List<Integer> correctQuantities = Arrays.asList(1, 2, 1);
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

  /**
   * Test for setting the quantity of an item in a locally stored order.
   * This implements  and/or tests the following:
   *
   * - Registering a new catering company and shielding individual.
   * - Picking Food Box 1 from the <code>food_box.txt</code> on the server and
   *   placing an order
   * - Getting the the order number for the placed order
   * - Checking that the item quantity is the pre set value given by
   *   <code>food_boxes.txt</code>
   * - Setting the new quantity for the item using the <code>setItemQuantityForOrder</code>
   *   method
   * - Checking that the item quantity matches the new quantity.
   */
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

  /**
   * Test for getting the closest catering company.
   * This implements  and/or tests the following:
   *
   * - Registering 5 new catering companies.
   * - Checking that the correct error message is thrown when the function
   *   tries to run without the user having registered(therefore the postcode
   *   of the user is a null value)
   * - Registering a new shielding individual and checking that a string is returned.
   */
  @Test
  @DisplayName("getClosestCateringCompany Test")
  public void testGetClosestCateringCompany() {
    assertTrue(cateringCompanyClient.registerCateringCompany("Portrait_Gallery", "EH4_3DR"));
    assertTrue(cateringCompanyClient.registerCateringCompany("National_Museum", "EH1_1JF"));
    assertTrue(cateringCompanyClient.registerCateringCompany("The_Mound", "EH2_2EL"));
    assertTrue(cateringCompanyClient.registerCateringCompany("Appleton_Tower", "EH4_3DR"));
    assertTrue(cateringCompanyClient.registerCateringCompany("Start_of_Meadow_Walk", "EH9_1LY"));
    try {
      client.getClosestCateringCompany();
    } catch (Exception e) {
      assertEquals(e.getMessage(), "Shielding User postcode or caterer postcode was invalid");
    }
    client.registerShieldingIndividual(MyTestUtils.generateValidCHI());
    assertEquals(client.getClosestCateringCompany().getClass(), String.class);
  }
}