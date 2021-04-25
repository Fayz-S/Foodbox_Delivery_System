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
 *
 */

public class SupermarketClientImpTest {
  private final static String clientPropsFilename = "client.cfg";

  private Properties clientProps;
  private SupermarketClient client;
  private ShieldingIndividualClientImp shieldingIndividualClient;

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

  @BeforeEach
  public void setup() {
    clientProps = loadProperties(clientPropsFilename);
    client = new SupermarketClientImp(clientProps.getProperty("endpoint"));
    shieldingIndividualClient = new ShieldingIndividualClientImp(clientProps.getProperty("endpoint"));
  }


  @Test
  public void testSupermarketNewRegistration() {
    Random rand = new Random();
    String name = String.valueOf(rand.nextInt(10000));
    String postcode = MyTestUtils.generateValidPostcode();

    assertTrue(client.registerSupermarket(name, postcode));
    assertTrue(client.isRegistered());
    assertEquals(client.getName(), name);
    assertEquals(client.getPostCode(), postcode);
    assertTrue(client.registerSupermarket(name, postcode));
  }

  @RepeatedTest(20)
  @DisplayName("registerCC Test: Failure Scenario")
  public void testFailureSupermarketNewRegistration() {

    List<String> invalidPostcodes = MyTestUtils.generateInvalidPostcodes();
    for (String postcode : invalidPostcodes) {
      assertFalse(client.registerSupermarket("Ed", postcode));
    }
  }

  @Test
  @DisplayName("recordSupermarketOrder Test")
  public void testRecordSupermarketOrder() {
    String validCHI = MyTestUtils.generateValidCHI();
    assertTrue(client.registerSupermarket("Supermarket", MyTestUtils.generateValidPostcode()));
    client.recordSupermarketOrder(validCHI, 3);
    assertTrue(shieldingIndividualClient.registerShieldingIndividual(validCHI));
    assertTrue(client.recordSupermarketOrder(validCHI, 2));
    assertFalse(client.recordSupermarketOrder(validCHI, 2));
  }

  @Test
  @DisplayName("updateOrderStatus Test")
  public void testUpdateOrderStatus() {
    String validCHI = MyTestUtils.generateValidCHI();
    assertTrue(client.registerSupermarket("Supermarket", MyTestUtils.generateValidPostcode()));
    assertTrue(shieldingIndividualClient.registerShieldingIndividual(validCHI));
    assertTrue(client.recordSupermarketOrder(validCHI, 3));

    assertTrue(client.updateOrderStatus(3, "packed"));
    assertTrue(client.updateOrderStatus(3, "dispatched"));
    assertTrue(client.updateOrderStatus(3, "delivered"));
  }
}
