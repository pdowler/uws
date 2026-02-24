
package ca.nrc.cadc.uws.server;

import ca.nrc.cadc.db.ConnectionConfig;
import ca.nrc.cadc.db.DBConfig;
import ca.nrc.cadc.db.DBUtil;
import ca.nrc.cadc.util.Log4jInit;
import ca.nrc.cadc.uws.server.impl.InitDatabaseUWS;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.BeforeClass;

/**
 *
 * @author pdowler
 */
public class PostgresJobDAOTest extends AbstractJobDAOTest {
    private static Logger log = Logger.getLogger(PostgresJobDAOTest.class);
    
    static {
        Log4jInit.setLevel("ca.nrc.cadc.db", Level.INFO);
        Log4jInit.setLevel("ca.nrc.cadc.uws", Level.INFO);
    }

    @BeforeClass
    public static void testSetup() {
        Log4jInit.setLevel("ca.nrc.cadc.uws.server", Level.DEBUG);
        try {
            DBConfig conf = new DBConfig();
            ConnectionConfig cc = conf.getConnectionConfig(TestUtil.SERVER, TestUtil.DATABASE);
            dataSource = DBUtil.getDataSource(cc, true, true);
            log.info("configured data source: " + cc.getServer() + "," + cc.getDatabase() + "," + cc.getDriver() + "," + cc.getURL());
            JOB_SCHEMA = new JobDAO.JobSchema(TestUtil.SCHEMA + ".Job", TestUtil.SCHEMA + ".JobDetail", false);
            
            InitDatabaseUWS init = new InitDatabaseUWS(dataSource, null, "uws");
            init.doInit();
            
        } catch (Exception ex) {
            log.error("setup failed", ex);
            throw new IllegalStateException("failed to create DataSource", ex);
        }
    }
}
