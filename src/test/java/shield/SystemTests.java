/**
 *
 */

package shield;

import org.junit.jupiter.api.*;

import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.time.LocalDateTime;
import java.io.InputStream;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class SystemTests {

  private SupermarketClientImpTest supermarketTests;
  private CateringCompanyClientImpTest cateringCompanyTests;
  private ShieldingIndividualClientImpTest shieldingIndividualTests;

  private final static String clientPropsFilename = "client.cfg";

  private Properties clientProps;
  private SupermarketClientImp supermarketClient;
  private ShieldingIndividualClientImp shieldingIndividualClient;
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

  /**
   * Sets what will be run before each test
   */

  @BeforeEach
  public void setup() {
    clientProps = loadProperties(clientPropsFilename);
    supermarketClient = new SupermarketClientImp(clientProps.getProperty("endpoint"));
    shieldingIndividualClient = new ShieldingIndividualClientImp(clientProps.getProperty("endpoint"));
    cateringCompanyClient = new CateringCompanyClientImp(clientProps.getProperty("endpoint"));
  }

  /**
   * Test for registering a new Supermarket
   * This implements and/or tests the following:
   * - Registering a new Supermarket
   * - Checking that the name, registered and postcode fields have been updated
   * - Re-registering a Supermarket
   * - Checking that for all invalid postcodes, generated from the
   *   <code>generateInvalidPostcodes</code> from the <code>MyTestUtils</code>
   *   class return false and are therefore not registered
   */
  @Test
  @DisplayName("supermarketNewRegistrationTest")
  public void testSupermarketNewRegistration() {
    Random rand = new Random();
    String name = String.valueOf(rand.nextInt(10000));
    String validPostcode = MyTestUtils.generateValidPostcode();

    assertTrue(supermarketClient.registerSupermarket(name, validPostcode));
    assertTrue(supermarketClient.isRegistered());
    assertEquals(supermarketClient.getName(), name);
    assertEquals(supermarketClient.getPostCode(), validPostcode);
    assertTrue(supermarketClient.registerSupermarket(name, validPostcode));


    List<String> invalidPostcodes = MyTestUtils.generateInvalidPostcodes();
    for (String postcode : invalidPostcodes) {
      assertFalse(supermarketClient.registerSupermarket("Ed", postcode));
    }
  }

  /**
   * Test for registering a new Supermarket
   * This implements and/or tests the following:
   * - Registering a new Catering Company returns True
   * - Checking that the name, registered and postcode fields have been updated
   * - Re-registering a Catering Company returns True
   * - Checking that for all invalid postcodes, generated from the
   *   <code>generateInvalidPostcodes</code> from the <code>MyTestUtils</code>
   *   class return false and are therefore not registered
   */
  @Test
  @DisplayName("registerCateringCompany Test: Success Scenario")
  public void testCateringCompanyNewRegistration() {
    Random rand = new Random();
    String name = String.valueOf(rand.nextInt(10000));
    String validPostcode = MyTestUtils.generateValidPostcode();

    assertTrue(cateringCompanyClient.registerCateringCompany(name, validPostcode));
    assertTrue(cateringCompanyClient.isRegistered());
    assertEquals(cateringCompanyClient.getName(), name);
    assertEquals(cateringCompanyClient.getPostCode(), validPostcode);
    assertTrue(cateringCompanyClient.registerCateringCompany(name, validPostcode));

    List<String> invalidPostcodes = MyTestUtils.generateInvalidPostcodes();
    for (String postcode : invalidPostcodes) {
      System.out.println(postcode);
      assertFalse(cateringCompanyClient.registerCateringCompany("Ed", postcode));
    }
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
  @Test
  @DisplayName("registerShieldingIndividual Test: New User Success Scenario")
  public void testShieldingIndividualNewRegistration() {
    String validCHI = MyTestUtils.generateValidCHI();

    assertTrue(shieldingIndividualClient.registerShieldingIndividual(validCHI));
    assertTrue(shieldingIndividualClient.isRegistered());
    assertEquals(shieldingIndividualClient.getCHI(), validCHI);
    assertTrue(shieldingIndividualClient.registerShieldingIndividual(validCHI));

    String invalidMax = String.valueOf(MyTestUtils.generateRandomNumber(32, 99));
    String invalidMin = MyTestUtils.formatForValidCHI(MyTestUtils.generateRandomNumber(-99, 0));
    String invalidCHIMax = invalidMax + "01" + MyTestUtils.generateRandomNumber(100000, 999999);
    String invalidCHIMin = invalidMin + "01" + MyTestUtils.generateRandomNumber(100000, 999999);
    assertFalse(shieldingIndividualClient.registerShieldingIndividual(invalidCHIMax));
    assertFalse(shieldingIndividualClient.registerShieldingIndividual(invalidCHIMin));

    invalidMax = MyTestUtils.formatForValidCHI(MyTestUtils.generateRandomNumber(13, 99));
    invalidMin = MyTestUtils.formatForValidCHI(MyTestUtils.generateRandomNumber(-99, 0));
    invalidCHIMax = "01" + invalidMax + MyTestUtils.generateRandomNumber(100000, 999999);
    invalidCHIMin = "01" + invalidMin + MyTestUtils.generateRandomNumber(100000, 999999);
    assertFalse(shieldingIndividualClient.registerShieldingIndividual(invalidCHIMax));
    assertFalse(shieldingIndividualClient.registerShieldingIndividual(invalidCHIMin));

    String tooShortCHI = MyTestUtils.generateValidCHI().substring(1);
    String tooLongCHI = MyTestUtils.generateValidCHI() + "1";
    assertFalse(shieldingIndividualClient.registerShieldingIndividual(tooShortCHI));
    assertFalse(shieldingIndividualClient.registerShieldingIndividual(tooLongCHI));
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
    assertTrue(shieldingIndividualClient.registerShieldingIndividual(MyTestUtils.generateValidCHI()));
    assertTrue(cateringCompanyClient.registerCateringCompany("c", MyTestUtils.generateValidPostcode()));
    assertTrue(shieldingIndividualClient.pickFoodBox(1));
    assertTrue(shieldingIndividualClient.placeOrder());
    assertEquals(1, shieldingIndividualClient.getOrderNumbers().size());
    int orderId = ((List<Integer>) shieldingIndividualClient.getOrderNumbers()).get(0);
    shieldingIndividualClient.requestOrderStatus(orderId);
    assertEquals("order has been placed", shieldingIndividualClient.getStatusForOrder(orderId));
  }

  /**
   * Test for recording a new Supermarket order
   * This implements and/or tests the following:
   * - Registering a new Supermarket and a Shielding Individual
   * - Using <code>recordSupermarketOrder</code> to record new order
   * - Checking that the same order ID cannot be used twice to create a new order
   */
  @Test
  @DisplayName("recordSupermarketOrder Test")
  public void testRecordSupermarketOrder() {
    String validCHI = MyTestUtils.generateValidCHI();
    assertTrue(supermarketClient.registerSupermarket("Supermarket", MyTestUtils.generateValidPostcode()));
    assertTrue(shieldingIndividualClient.registerShieldingIndividual(validCHI));
    assertTrue(supermarketClient.recordSupermarketOrder(validCHI, 4));
    assertFalse(supermarketClient.recordSupermarketOrder(validCHI, 4));
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
    assertTrue(shieldingIndividualClient.registerShieldingIndividual(MyTestUtils.generateValidCHI()));
    assertTrue(cateringCompanyClient.registerCateringCompany("c", MyTestUtils.generateValidPostcode()));
    assertTrue(shieldingIndividualClient.pickFoodBox(1));
    assertTrue(shieldingIndividualClient.placeOrder());
    int orderId = ((List<Integer>) shieldingIndividualClient.getOrderNumbers()).get(0);
    assertTrue(shieldingIndividualClient.setItemQuantityForOrder(2, orderId, 1));
    assertTrue(shieldingIndividualClient.editOrder(orderId));

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
    assertTrue(shieldingIndividualClient.registerShieldingIndividual(MyTestUtils.generateValidCHI()));
    assertTrue(cateringCompanyClient.registerCateringCompany("c", MyTestUtils.generateValidPostcode()));
    assertTrue(shieldingIndividualClient.pickFoodBox(1));
    assertTrue(shieldingIndividualClient.placeOrder());
    int orderId = ((List<Integer>) shieldingIndividualClient.getOrderNumbers()).get(0);
    assertTrue(shieldingIndividualClient.cancelOrder(orderId));
    shieldingIndividualClient.requestOrderStatus(orderId);
    assertEquals("order has been cancelled", shieldingIndividualClient.getStatusForOrder(orderId));
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
    assertTrue(shieldingIndividualClient.registerShieldingIndividual(MyTestUtils.generateValidCHI()));
    assertTrue(cateringCompanyClient.registerCateringCompany("c", MyTestUtils.generateValidPostcode()));
    assertTrue(shieldingIndividualClient.pickFoodBox(1));

    assertTrue(shieldingIndividualClient.placeOrder());
    assertTrue(shieldingIndividualClient.placeOrder());
    assertTrue(shieldingIndividualClient.placeOrder());
    assertTrue(shieldingIndividualClient.placeOrder());
    assertTrue(shieldingIndividualClient.placeOrder());

    // this is needed for the integration test with getStatusFromOrder
    int orderIdPlaced = ((List<Integer>) shieldingIndividualClient.getOrderNumbers()).get(0);
    int orderIdPacked = ((List<Integer>) shieldingIndividualClient.getOrderNumbers()).get(1);
    int orderIdDispatched = ((List<Integer>) shieldingIndividualClient.getOrderNumbers()).get(2);
    int orderIdDelivered = ((List<Integer>) shieldingIndividualClient.getOrderNumbers()).get(3);
    int orderIdCancelled = ((List<Integer>) shieldingIndividualClient.getOrderNumbers()).get(4);

    assertTrue(cateringCompanyClient.updateOrderStatus(orderIdPacked, "packed"));
    assertTrue(cateringCompanyClient.updateOrderStatus(orderIdDispatched, "dispatched"));
    assertTrue(cateringCompanyClient.updateOrderStatus(orderIdDelivered, "delivered"));
    assertTrue(shieldingIndividualClient.cancelOrder(orderIdCancelled));

    assertTrue(shieldingIndividualClient.requestOrderStatus(orderIdPacked));
    assertTrue(shieldingIndividualClient.requestOrderStatus(orderIdDispatched));
    assertTrue(shieldingIndividualClient.requestOrderStatus(orderIdDelivered));
    assertTrue(shieldingIndividualClient.requestOrderStatus(orderIdCancelled));
  }

  /**
   * Test for updating the status of a Catering Company order
   * This implements and/or tests the following:
   * - Registering a new Catering Company  and a Shielding Individual
   * - Picking Food Box 1 and placing an order (using methods from the class
   *   <code>ShieldingIndividualClientImp</code>).
   * - Getting the order ID using the method <code>getOrderNumbers</code>
   *   from the class <code>ShieldingIndividualClientImp</code>
   * - Updating the order status to packed, dispatched and then delivered
   *   using the <code>updateOrderStatus</code> method.
   * - Checking that using an invalid status returns false.
   */
  @Test
  public void testUpdateOrderStatus() {
    String validCHI = MyTestUtils.generateValidCHI();
    assertTrue(cateringCompanyClient.registerCateringCompany("Catering_Company", MyTestUtils.generateValidPostcode()));
    assertTrue(shieldingIndividualClient.registerShieldingIndividual(validCHI));
    assertTrue(shieldingIndividualClient.pickFoodBox(1));
    assertTrue(shieldingIndividualClient.placeOrder());
    int orderId = ((List<Integer>) shieldingIndividualClient.getOrderNumbers()).get(0);

    assertTrue(cateringCompanyClient.updateOrderStatus(orderId, "packed"));
    assertTrue(cateringCompanyClient.updateOrderStatus(orderId, "dispatched"));
    assertTrue(cateringCompanyClient.updateOrderStatus(orderId, "delivered"));
    assertFalse(cateringCompanyClient.updateOrderStatus(orderId, "RandomValue"));
  }

    /**
     * Test for updating the status of a Supermarket order
     * This implements and/or tests the following:
     * - Registering a new Catering Company and a Shielding Individual.
     * - Using <code>recordSupermarketOrder</code> to record new order.
     * - Getting the order ID using the method <code>getOrderNumbers</code>
     *   from the class <code>ShieldingIndividualClientImp</code>.
     * - Updating the order status to packed, dispatched and then delivered.
     *   using the <code>updateOrderStatus</code> method.
     *  Checking that using an invalid status returns false.
     */
    @Test
    @DisplayName("updateOrderStatus Test")
    public void testUpdateSupermarketOrderStatus() {
      String validCHI = MyTestUtils.generateValidCHI();
      assertTrue(supermarketClient.registerSupermarket("Supermarket", MyTestUtils.generateValidPostcode()));
      assertTrue(shieldingIndividualClient.registerShieldingIndividual(validCHI));
      assertTrue(supermarketClient.recordSupermarketOrder(validCHI, 5));
      assertTrue(supermarketClient.updateOrderStatus(5, "packed"));
      assertTrue(supermarketClient.updateOrderStatus(5, "dispatched"));
      assertTrue(supermarketClient.updateOrderStatus(5, "delivered"));
      assertFalse(supermarketClient.updateOrderStatus(5, "RandomValue"));
    }
  }
