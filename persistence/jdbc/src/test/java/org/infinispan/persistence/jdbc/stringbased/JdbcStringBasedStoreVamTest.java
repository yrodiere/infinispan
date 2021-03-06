package org.infinispan.persistence.jdbc.stringbased;

import static org.infinispan.test.TestingUtil.extractGlobalMarshaller;

import org.infinispan.commons.marshall.StreamingMarshaller;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.persistence.spi.PersistenceException;
import org.infinispan.test.fwk.TestCacheManagerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * JdbcStringBasedStoreTest using production level marshaller.
 *
 * @author Galder Zamarreño
 * @since 4.0
 */
@Test(groups = "functional", testName = "persistence.jdbc.stringbased.JdbcStringBasedStoreVamTest")
public class JdbcStringBasedStoreVamTest extends JdbcStringBasedStoreTest {

   EmbeddedCacheManager cm;
   StreamingMarshaller marshaller;

   @BeforeClass
   public void setUpClass() {
      cm = TestCacheManagerFactory.createCacheManager(false);
      marshaller = extractGlobalMarshaller(cm.getCache().getCacheManager());
   }

   @AfterClass
   public void tearDownClass() throws PersistenceException {
      cm.stop();
   }

   @Override
   protected StreamingMarshaller getMarshaller() {
      return marshaller;
   }

}
