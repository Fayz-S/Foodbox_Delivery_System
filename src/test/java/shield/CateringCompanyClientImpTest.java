
package shield;

import org.junit.jupiter.api.*;

import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.time.LocalDateTime;
import java.io.InputStream;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */

public class CateringCompanyClientImpTest {
  private final static String clientPropsFilename = "client.cfg";

  private Properties clientProps;
  private CateringCompanyClient client;
  private ShieldingIndividualClientImp shieldingIndividualClient;

  /**
   * Gets property object so that we can get address for HTTP requests
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
    client = new CateringCompanyClientImp(clientProps.getProperty("endpoint"));
    shieldingIndividualClient = new ShieldingIndividualClientImp(clientProps.getProperty("endpoint"));
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
  @RepeatedTest(20)
  @DisplayName("registerCC Test: Success Scenario")
  public void testCateringCompanyNewRegistration() {
    Random rand = new Random();
    String name = String.valueOf(rand.nextInt(10000));
    String validPostcode = MyTestUtils.generateValidPostcode();

    assertTrue(client.registerCateringCompany(name, validPostcode));
    assertTrue(client.isRegistered());
    assertEquals(client.getName(), name);
    assertEquals(client.getPostCode(), validPostcode);
    assertTrue(client.registerCateringCompany(name, validPostcode));

    List<String> invalidPostcodes = MyTestUtils.generateInvalidPostcodes();
    for (String postcode : invalidPostcodes) {
      System.out.println(postcode);
      assertFalse(client.registerCateringCompany("Ed", postcode));
    }
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
    assertTrue(client.registerCateringCompany("Catering_Company", MyTestUtils.generateValidPostcode()));
    assertTrue(shieldingIndividualClient.registerShieldingIndividual(validCHI));
    assertTrue(shieldingIndividualClient.pickFoodBox(1));
    assertTrue(shieldingIndividualClient.placeOrder());
    int orderId = ((List<Integer>) shieldingIndividualClient.getOrderNumbers()).get(0);

    assertTrue(client.updateOrderStatus(orderId, "packed"));
    assertTrue(client.updateOrderStatus(orderId, "dispatched"));
    assertTrue(client.updateOrderStatus(orderId, "delivered"));
    assertFalse(client.updateOrderStatus(orderId, "RandomValue"));

  }
}