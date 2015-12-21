(defproject wspsql "3.0.0"
  :description "Webservice clojure psql"
  :url "https://github.com/BAlmeidaS/WS-ClojurePSQL"
  :min-lein-version "2.0.0"
  :dependencies [ [org.clojure/clojure "1.7.0"]
                  [org.clojure/java.jdbc "0.4.1"]
                  [org.postgresql/postgresql "9.4-1201-jdbc41"]
                  [compojure "1.4.0"]
                  [cheshire "5.5.0"]
                  [hiccup "1.0.5"]
                  [ring/ring-jetty-adapter "1.4.0"]
                  [ring/ring-json "0.1.2"]
                  [ring/ring-defaults "0.1.5"]]
  :plugins [[lein-ring "0.9.7"]]
  :ring { :handler wspsql.handler/app
          :init wspsql.models.migration/migrate
        }
  :profiles
            { :dev  { :dependencies [ [javax.servlet/servlet-api "2.5"]
                                      [ring/ring-mock "0.3.0"]]
                    }
              :uberjar
                    {:aot :all}
            }


)
