/**
 * Copyright (C) 2011 Angelo Zerr <angelo.zerr@gmail.com> and Pascal Leclercq <pascal.leclercq@gmail.com>
 *
 * All rights reserved.
 *
 * Permission is hereby granted, free  of charge, to any person obtaining
 * a  copy  of this  software  and  associated  documentation files  (the
 * "Software"), to  deal in  the Software without  restriction, including
 * without limitation  the rights to  use, copy, modify,  merge, publish,
 * distribute,  sublicense, and/or sell  copies of  the Software,  and to
 * permit persons to whom the Software  is furnished to do so, subject to
 * the following conditions:
 *
 * The  above  copyright  notice  and  this permission  notice  shall  be
 * included in all copies or substantial portions of the Software.
 *
 * THE  SOFTWARE IS  PROVIDED  "AS  IS", WITHOUT  WARRANTY  OF ANY  KIND,
 * EXPRESS OR  IMPLIED, INCLUDING  BUT NOT LIMITED  TO THE  WARRANTIES OF
 * MERCHANTABILITY,    FITNESS    FOR    A   PARTICULAR    PURPOSE    AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE,  ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package fr.opensagres.xdocreport.service.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.jaxrs.servlet.CXFNonSpringJaxrsServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import fr.opensagres.xdocreport.converter.ConverterTypeTo;
import fr.opensagres.xdocreport.converter.ConverterTypeVia;
import fr.opensagres.xdocreport.core.document.DocumentKind;
import fr.opensagres.xdocreport.document.ProcessState;
import fr.opensagres.xdocreport.document.service.DataContext;
import fr.opensagres.xdocreport.document.service.ReportAndDataRepresentation;
import fr.opensagres.xdocreport.document.service.ReportId;
import fr.opensagres.xdocreport.document.service.ReportRepresentation;
import fr.opensagres.xdocreport.document.service.WSOptions;

public class RESTXDocReportServiceTest
{

    private static final int PORT = 9999;

    private static Server server;

    private static final String BASE_ADDRESS = "http://localhost:" + PORT;


    public File tempFolder = new File("target");

    @BeforeClass
    public static void startServer()
        throws Exception
    {

        ServletHolder servlet = new ServletHolder( CXFNonSpringJaxrsServlet.class );

        servlet.setInitParameter( Application.class.getName(),
                                  fr.opensagres.xdocreport.service.rest.XDocreportApplication.class.getName() );
        servlet.setInitParameter( "jaxrs.serviceClasses", RESTXDocReportService.class.getName() );

        servlet.setInitParameter( "timeout", "60000" );
        server = new Server( PORT );

        ServletContextHandler context = new ServletContextHandler( server, "/", ServletContextHandler.SESSIONS );

        context.addServlet( servlet, "/*" );
        server.start();

    }

    @Test
    public void upload()
        throws IOException
    {

        uploadAFile( "reportID1" );

    }

    private static int uploaded=0;
    private void uploadAFile(String reportID1)
        throws IOException
    {
        WebClient client = WebClient.create( BASE_ADDRESS );
        client.path( "upload" );
        ReportRepresentation report = new ReportRepresentation();

        InputStream in = RESTXDocReportServiceTest.class.getClassLoader().getResourceAsStream( "bo.docx" );
        report.setReportID( reportID1);
        report.setDocument( fr.opensagres.xdocreport.core.io.IOUtils.toByteArray( in ) );
        report.setTemplateEngine( "Velocity" );
        report.getFieldsMetaData().add( "test" );
        report.setTemplateEngine( "Velocity" );
        uploaded=uploaded+1;
        client.post( report );
    }


    @Ignore
    @Test
    public void download()
        throws IOException
    {
        //first upload a file...
        String reportID = "download";
        uploadAFile( reportID );

        WebClient client = WebClient.create( BASE_ADDRESS );
        client.path( "download/"+reportID+"/"+ProcessState.ORIGINAL.name() );
        client.accept( MediaType.APPLICATION_XML );
System.out.println(client.getCurrentURI());
      Response resp=  client.get();
      System.out.println(resp.getStatus());
//        byte[] flux= client.get( byte[].class );
//        assertNotNull(flux);
//        createFile( flux,"result.docx" );

    }


    @Test
    public void listReports()
        throws Exception
    {

        WebClient client = WebClient.create( BASE_ADDRESS );
        client.path( "listReports" );

        @SuppressWarnings( "unchecked" )
        Collection<ReportId> reports = (Collection<ReportId>) client.getCollection( ReportId.class );
        //System.out.println( reports );


        assertEquals( uploaded, reports.size() );


    }


    @Test
    public void processReportWithoutOptions()
        throws IOException
    {

        WebClient client = WebClient.create( BASE_ADDRESS );
        client.path( "processReport" );
        client.accept( MediaType.APPLICATION_XML );

        ReportAndDataRepresentation report = new ReportAndDataRepresentation();

        InputStream in = RESTXDocReportServiceTest.class.getClassLoader().getResourceAsStream( "bo.docx" );
        report.setReportID( "reportID1" );
        report.setDocument( fr.opensagres.xdocreport.core.io.IOUtils.toByteArray( in ) );
        report.setTemplateEngine( "Velocity" );
        report.getFieldsMetaData().add( "test" );
        report.setTemplateEngine( "Velocity" );

        report.setDataContext( new ArrayList<DataContext>() );

        report.setOptions( null );

        byte[] flux= client.post( report,byte[].class );
        assertNotNull(flux);
        createFile( flux,"result.docx" );

    }


    @Test
    public void processReportWithOptions()
        throws IOException
    {

        WebClient client = WebClient.create( BASE_ADDRESS );
        client.path( "processReport" );
        client.accept( MediaType.APPLICATION_XML );

        ReportAndDataRepresentation report = new ReportAndDataRepresentation();

        InputStream in = RESTXDocReportServiceTest.class.getClassLoader().getResourceAsStream( "bo.docx" );
        report.setReportID( "reportID1" );
        report.setDocument( fr.opensagres.xdocreport.core.io.IOUtils.toByteArray( in ) );
        report.setTemplateEngine( "Velocity" );
        report.getFieldsMetaData().add( "test" );
        report.setTemplateEngine( "Velocity" );

        report.setDataContext( new ArrayList<DataContext>() );

        WSOptions options = new WSOptions();
        options.setFrom( DocumentKind.DOCX.name() );
        options.setTo( ConverterTypeTo.PDF.name() );
        options.setVia( ConverterTypeVia.ITEXT.name() );

        report.setOptions( options );
        //client.post( report);
        byte[] flux= client.post( report,byte[].class );
        assertNotNull(flux);

        createFile( flux,"result.pdf" );
    }

    private void createFile( byte[] flux ,String filename)
        throws FileNotFoundException, IOException
    {
        File aFile= new File(tempFolder,filename );
        FileOutputStream fos= new FileOutputStream( aFile );
        fos.write( flux );
        fos.close();
    }

    @AfterClass
    public static void stopServer()
        throws Exception
    {
        server.stop();
    }
}
