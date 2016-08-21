# HttpProxyServer

Proxy server written in Java using   [HttpServer] (http://docs.oracle.com/javase/8/docs/jre/api/net/httpserver/spec/com/sun/net/httpserver/HttpServer.html)
 and [HttpUrlConnection] (http://docs.oracle.com/javase/8/docs/api/java/net/HttpURLConnection.html) classes.
 
Besides of simply passing the request and responses it has some additional  functionalities:
  - filtering webpages which are listed in [black_list file] (https://github.com/PaulinaSadowska/HttpProxyServer/blob/master/black_list).
  - collecting statistics about visited sites and storing them in [stats file] (https://github.com/PaulinaSadowska/HttpProxyServer/blob/master/stats).



  
