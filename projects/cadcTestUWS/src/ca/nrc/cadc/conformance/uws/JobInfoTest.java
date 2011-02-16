/*
************************************************************************
*******************  CANADIAN ASTRONOMY DATA CENTRE  *******************
**************  CENTRE CANADIEN DE DONNÉES ASTRONOMIQUES  **************
*
*  (c) 2009.                            (c) 2009.
*  Government of Canada                 Gouvernement du Canada
*  National Research Council            Conseil national de recherches
*  Ottawa, Canada, K1A 0R6              Ottawa, Canada, K1A 0R6
*  All rights reserved                  Tous droits réservés
*
*  NRC disclaims any warranties,        Le CNRC dénie toute garantie
*  expressed, implied, or               énoncée, implicite ou légale,
*  statutory, of any kind with          de quelque nature que ce
*  respect to the software,             soit, concernant le logiciel,
*  including without limitation         y compris sans restriction
*  any warranty of merchantability      toute garantie de valeur
*  or fitness for a particular          marchande ou de pertinence
*  purpose. NRC shall not be            pour un usage particulier.
*  liable in any event for any          Le CNRC ne pourra en aucun cas
*  damages, whether direct or           être tenu responsable de tout
*  indirect, special or general,        dommage, direct ou indirect,
*  consequential or incidental,         particulier ou général,
*  arising from the use of the          accessoire ou fortuit, résultant
*  software.  Neither the name          de l'utilisation du logiciel. Ni
*  of the National Research             le nom du Conseil National de
*  Council of Canada nor the            Recherches du Canada ni les noms
*  names of its contributors may        de ses  participants ne peuvent
*  be used to endorse or promote        être utilisés pour approuver ou
*  products derived from this           promouvoir les produits dérivés
*  software without specific prior      de ce logiciel sans autorisation
*  written permission.                  préalable et particulière
*                                       par écrit.
*
*  This file is part of the             Ce fichier fait partie du projet
*  OpenCADC project.                    OpenCADC.
*
*  OpenCADC is free software:           OpenCADC est un logiciel libre ;
*  you can redistribute it and/or       vous pouvez le redistribuer ou le
*  modify it under the terms of         modifier suivant les termes de
*  the GNU Affero General Public        la “GNU Affero General Public
*  License as published by the          License” telle que publiée
*  Free Software Foundation,            par la Free Software Foundation
*  either version 3 of the              : soit la version 3 de cette
*  License, or (at your option)         licence, soit (à votre gré)
*  any later version.                   toute version ultérieure.
*
*  OpenCADC is distributed in the       OpenCADC est distribué
*  hope that it will be useful,         dans l’espoir qu’il vous
*  but WITHOUT ANY WARRANTY;            sera utile, mais SANS AUCUNE
*  without even the implied             GARANTIE : sans même la garantie
*  warranty of MERCHANTABILITY          implicite de COMMERCIALISABILITÉ
*  or FITNESS FOR A PARTICULAR          ni d’ADÉQUATION À UN OBJECTIF
*  PURPOSE.  See the GNU Affero         PARTICULIER. Consultez la Licence
*  General Public License for           Générale Publique GNU Affero
*  more details.                        pour plus de détails.
*
*  You should have received             Vous devriez avoir reçu une
*  a copy of the GNU Affero             copie de la Licence Générale
*  General Public License along         Publique GNU Affero avec
*  with OpenCADC.  If not, see          OpenCADC ; si ce n’est
*  <http://www.gnu.org/licenses/>.      pas le cas, consultez :
*                                       <http://www.gnu.org/licenses/>.
*
*  $Revision: 4 $
*
************************************************************************
*/

package ca.nrc.cadc.conformance.uws;

import java.io.StringWriter;
import java.io.PrintWriter;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebResponse;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.junit.Test;
import static org.junit.Assert.*;

public class JobInfoTest extends AbstractUWSTest
{
    private static Logger log = Logger.getLogger(JobInfoTest.class);

    public JobInfoTest()
    {
        super();
        setLoggingLevel(log);
    }

    @Test
    public void testCreateJobInfoBadXml()
    {
        // JobInfo XML, wrong format with missing close tag for position
        String xml = "<target><name>name</name><position>position</target>";

        // Create a new Job.
        WebConversation conversation = new WebConversation();
        try
        {
            String jobId = createJob(conversation, xml);
            fail("Bad XML format; expecting exception!");
        }
        catch (Throwable t)
        {
            //expected.
            log.info(this.getClass().getSimpleName() + ".testCreateJobInfoBadXml completed.");
        }
    }

    @Test
    public void testCreateJobInfo()
    {
        try
        {
            // JobInfo XML
            String xml = "<target><name>name</name><position>position</position></target>";
            
            // Create a new Job.
            WebConversation conversation = new WebConversation();
            String jobId = createJob(conversation, xml);

            // GET request to the jobId resource.
            String resourceUrl = serviceUrl + "/" + jobId;
            WebResponse response = get(conversation, resourceUrl);

            // Validate the XML against the schema.
            log.debug("XML:\r\n" + response.getText());
            Document document = buildDocument(response.getText(), true);

            // Get the document root.
            Element root = document.getRootElement();
            assertNotNull("XML returned from GET of " + resourceUrl + " missing root element", root);
            Namespace namespace = root.getNamespace();
            log.debug("namespace: " + namespace);

            // List of jobInfo elements.
            Element jobInfo = root.getChild("jobInfo", namespace);
            Element content = (Element) jobInfo.getChildren().get(0);
            assertNotNull("XML returned from GET of " + resourceUrl + " missing uws:jobInfo element", jobInfo);

            // Validate the jobInfo.        
            XMLOutputter outputter = new XMLOutputter(Format.getCompactFormat());
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            outputter.output(content, pw);            
            log.debug("jobInfo: " + sw.toString());
            assertEquals("Incorrect uws:jobInfo content in XML returned from GET of " + resourceUrl, xml, sw.toString());

            // Delete the job.
            deleteJob(conversation, jobId);

            log.info("JobInfoTest.testCreateJobInfo completed.");
        }
        catch (Throwable t)
        {
            log.error(t);
            fail(t.getMessage());
        }
    }

    @Test
    public void testCreateJobInfoWithNamespace()
    {
        try
        {
            // JobInfo XML
            String xml = "<foo:target xmlns:foo=\"http://localhost/\"><foo:name>name</foo:name><foo:position>position</foo:position></foo:target>";
            
            // Create a new Job.
            WebConversation conversation = new WebConversation();
            String jobId = createJob(conversation, xml);

            // GET request to the jobId resource.
            String resourceUrl = serviceUrl + "/" + jobId;
            WebResponse response = get(conversation, resourceUrl);

            // Validate the XML against the schema.
            log.debug("XML:\r\n" + response.getText());
            Document document = buildDocument(response.getText(), true);

            // Get the document root.
            Element root = document.getRootElement();
            assertNotNull("XML returned from GET of " + resourceUrl + " missing root element", root);
            Namespace namespace = root.getNamespace();
            log.debug("namespace: " + namespace);

            // List of jobInfo elements.
            Element jobInfo = root.getChild("jobInfo", namespace);
            Element content = (Element) jobInfo.getChildren().get(0);
            assertNotNull("XML returned from GET of " + resourceUrl + " missing uws:jobInfo element", jobInfo);

            // Validate the jobInfo.        
            XMLOutputter outputter = new XMLOutputter(Format.getCompactFormat());
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            outputter.output(content, pw);            
            log.debug("jobInfo: " + sw.toString());
            assertEquals("Incorrect uws:jobInfo content in XML returned from GET of " + resourceUrl, xml, sw.toString());

            // Delete the job.
            deleteJob(conversation, jobId);

            log.info("JobTest.testCreateJobInfoWithNamespace completed.");
        }
        catch (Throwable t)
        {
            log.error(t);
            fail(t.getMessage());
        }
    }
}
