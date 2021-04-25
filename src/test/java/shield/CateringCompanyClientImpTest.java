
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
    client = new CateringCompanyClientImp(clientProps.getProperty("endpoint"));
  }


  @RepeatedTest(20)
  @DisplayName("registerCC Test: Success Scenario")
  public void testCateringCompanyNewRegistration() {
    Random rand = new Random();
    String name = String.valueOf(rand.nextInt(10000));
    String postcode = MyTestUtils.generateValidPostcode();
//    System.out.println(postcode);

    assertTrue(client.registerCateringCompany(name, postcode));
    assertTrue(client.isRegistered());
    assertEquals(client.getName(), name);
    assertEquals(client.getPostCode(), postcode);
    assertTrue(client.registerCateringCompany(name, postcode));
  }

  @RepeatedTest(20)
  @DisplayName("registerCC Test: Failure Scenario")
  public void testFailureCateringCompanyNewRegistration() {
    List<String> invalidPostcodes = MyTestUtils.generateInvalidPostcodes();
    for (String postcode : invalidPostcodes) {
      System.out.println(postcode);
      assertFalse(client.registerCateringCompany("Ed", postcode));
    }
  }

    @Test
    public void testUpdateOrderStatus () {


    }

}