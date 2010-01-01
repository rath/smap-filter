=========================================
SMAP filter tool for java web developers.
=========================================

* Quick Start

  try {
    // your logics will be here
  } catch( Exception e ) { 

    SmapFilter smap = new SmapFilter();
    smap.setWorkDirectory(application); // if you are on a jsp page
    smap.setWorkDirectory(getServletContext()); // if you are on a servlet
    smap.filterException(e);

    e.printStackTrace(); 
  }

Expected result on Tomcat is
  at org.apache.jsp.tmp.test_jsp._jspService(tmp/test.jsp:6)

Expected result on Resin is 
  at _jsp._tmp._test__jsp._jspService(tmp/test.jsp:6)


* Note for a user on the Tomcat. 

Default setting of Tomcat doesn't suppress smap files with JSP pages. 
As a result, you must turn on dumpSmap property on $CATALINE_BASE/conf/web.xml.

    <servlet>
        <servlet-name>jsp</servlet-name>
        <servlet-class>org.apache.jasper.servlet.JspServlet</servlet-class>
        <init-param>
            <param-name>fork</param-name>
            <param-value>true</param-value>
        </init-param>
        <init-param>
            <param-name>compiler</param-name>
            <param-value>javac</param-value>
        </init-param>
        <init-param>
            <param-name>dumpSmap</param-name>  
            <param-value>true</param-value>    <!-- Turn on! -->
        </init-param>
        <load-on-startup>3</load-on-startup>
    </servlet>


You should provide your hostname with SmapFilter.setWorkDirectory. 
  SmapFilter smap = new SmapFilter();
  smap.setWorkDirectory(ctx, "mysite.com");
If your hostname is localhost, you don't have to pass your hostname. 
  smap.setWorkDirectory(ctx);
If you want to specify work directory on your own, pass a file like below.
  smap.setWorkDirectory(new File("/usr/local/tomcat/work/Catalina/localhost/_"));


* Note for a user on the Resin.

Create an instance of SmapFilter. and pass your servlet context instance.
Since resin manages its working directory below each webapp's WEB-INF dir, you 
don't have to provide your hostname to SmapFilter instance. 

Behind the scene...
  setWorkDirectory(new File(servletContext.getRealPath("/WEB-INF/work")));


* Performance Issues.

SmapFilter instance will parse .smap files when you invoke 'filterException'. 
An instance of SmapFilter caches parsed smap information on its own hashmap. But
there are no way to turn off its cache. 

When you want to use SmapFilter on a production site, Use singleton SmapFilter. 
If you are on a development stage, create SmapFilter instance each time you want. 
