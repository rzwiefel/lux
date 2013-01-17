# Lux 0.5 Installation #

Lux is distributed as source code and as a compiled library, with some
required dependent libraries, including Saxon-HE and Woodstox.  When
installed in the context of a Solr installation it provides an XQuery REST
service.  With a little extra configuration in the Solr web.xml, it acts as
a web application server for applications written in XQuery and XSLT,
accessing XML indexed and stored using Solr/Lucene.

## Lux app server (complete) ##

FIXME: fix links once we have created the artifacts.

1. Download the complete server bundle as a [zip
   file](http://luxdb.net/download/lux-server-0.5.zip) "Download Lux zip")
   or [tar archive](http://luxdb.net/download/lux-server-0.5.tar.gz
   "Download Lux tar").

   This download includes the following artifacts:

   1. Jetty servlet container
   2. Solr search engine, configured with Lux library and EXPath 
      repo support.
   3. Lux application server
   4. EXPath repository w/modules: HTTP and Zip

2. Start the server using the Windows batch file bin/lux.bat, or the UNIX
   shell script bin/lux.

3. The app server is configured to occupy the root context (/) on port 80,
with the webapp directory as the web app root folder. The Solr service is
set up to on port 8983. The the xrepo folder is configured as an EXPath
repository from which the app server will load EXPath modules. (TODO:
configure app server so it connects to this embedded solr out of the box).

## Lux app server (integrate with existing Solr) ##

1. Download the library bundle as a [zip
   file](http://luxdb.net/download/lux-0.5.zip) "Download Lux zip") or [tar
   archive](http://luxdb.net/download/lux-0.5.tar.gz "Download Lux tar").

2. Unpack the download. Move or copy the contents of the lib folder (jar
   files) into the solr/lib folder so as to add the libraries to your Solr
   classpath.

3. Insert the contents of the conf/luxconfig.xml file into Solr's
   configuration file: solrconfig.xml; just before the closing <config> tag
   is a good place.

4. In solrconfig.xml, insert the Lux update chain in the configuration
   block for each update processor:

        <lst name="defaults">
          <str name="update.chain">lux-update-chain</str>
        </lst>

     For example, the configuration for the XmlUpdateRequestHandler should
     look like:

        <requestHandler name="/update" class="solr.XmlUpdateRequestHandler">
          <lst name="defaults">
            <str name="update.chain">lux-update-chain</str>
          </lst>                  
        </requestHandler>

     Also insert the Lux update chain into the configuration for
     solr.BinaryUpdateRequestHandler, solr.CSVRequestHandler and
     solr.JsonRequestHandler, or whichever update handler you will be
     using.  You can refer to the provided solrconfig.xml file for an
     example of what this should look like.

5. Restart Solr.  Watch the Solr logs to make sure there are no errors.
   You may see ClassNotFoundException.  If you do, that probably means the
   jars are not in the right folder: you may need to read up about Solr
   configuration: see above for a link.

   Now you have a REST server; if you also want to use the app server, it must
   be configured in Solr's web.xml

## Lux library only ##

If you are a Lucene user, but do not use Solr, you can still use Lux as a
means of indexing XML content and querying it using XQuery, since Lux
provides a Lucene-only API and its dependencies on Solr only arise in the
service of Solr request handlers.  Similarly, if you are Saxon user and
want to use Saxon to execute queries against a persistent indexed data
store, you can use Lux to do that without needing Solr.

In these cases, you "install" Lux by placing the Lux jar file (and its
dependencies) on your classpath.  If you use Maven to build your project,
this is easily accomplished by declaring the luxdb.net/luxdb/1.0 dependency
in your pom.xml file.  Otherwise, you can download Lux with all of its
dependencies [here](http://luxdb.net/download/) "Download Lux").

In order to use Lux as a REST service or an application server, follow
these instructions to integrate it with an existing Solr installation.

## EXPath Package Manager

TODO: links

Lux is distributed with a copy of the EXPath package manager, which enables
loading of standardized extensions to the XPath function library.  The app
server is configured to initialize the package manager, and will load any
modules in the EXPath repository.  It comes with two modules installed: an
HTTP client and a Zip file access module.

If you install the Lux library into a pre-existing environment, and you
also want to make use of EXPath modules, you need to add the EXPath (for
Saxon) package manager jars to your classpath, create an EXPath repository,
install the extension jars to that repository, and install the extensions
you want into the repository.  The EXPath jars are available from the
EXPath site, and are also distributed as part of the Lux app server bundle.

To activate the EXPath extensions, you must provide the location of the
repo as the value of the system property "org.expath.pkg.saxon.repo".

## Lux source code ##

The source distribution comes with a Maven project file and an Eclipse
.project file, so it is easiest to build it using those tools.  As usual,
"mvn package" will compile all the source code, run all the unit tests, and
build the various artifacts, including the jar, the application server war,
and the various distribution bundles.

## Maven Artifacts ##

The binaries and source are available via maven using groupId: net.luxdb,
artifactId: luxdb.  The latest version is 0.5