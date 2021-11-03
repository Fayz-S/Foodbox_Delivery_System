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

/**
 * Class for testing all Supe
 */

public class SupermarketClientImpTest {
  private final static String clientPropsFilename = "client.cfg";

  private Properties clientProps;
  private SupermarketClient client;
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
    client = new SupermarketClientImp(clientProps.getProperty("endpoint"));
    shieldingIndividualClient = new ShieldingIndividualClientImp(clientProps.getProperty("endpoint"));
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

    assertTrue(client.registerSupermarket(name, validPostcode));
    assertTrue(client.isRegistered());
    assertEquals(client.getName(), name);
    assertEquals(client.getPostCode(), validPostcode);
    assertTrue(client.registerSupermarket(name, validPostcode));


    List<String> invalidPostcodes = MyTestUtils.generateInvalidPostcodes();
    for (String postcode : invalidPostcodes) {
      assertFalse(client.registerSupermarket("Ed", postcode));
    }
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
    assertTrue(client.registerSupermarket("Supermarket", MyTestUtils.generateValidPostcode()));
    assertTrue(shieldingIndividualClient.registerShieldingIndividual(validCHI));
    assertTrue(client.recordSupermarketOrder(validCHI, 2));
    assertFalse(client.recordSupermarketOrder(validCHI, 2));
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
    assertTrue(client.registerSupermarket("Supermarket", MyTestUtils.generateValidPostcode()));
    assertTrue(shieldingIndividualClient.registerShieldingIndividual(validCHI));
    assertTrue(client.recordSupermarketOrder(validCHI, 3));
    assertTrue(client.updateOrderStatus(3, "packed"));
    assertTrue(client.updateOrderStatus(3, "dispatched"));
    assertTrue(client.updateOrderStatus(3, "delivered"));
    assertFalse(client.updateOrderStatus(3, "RandomValue"));
  }
}
