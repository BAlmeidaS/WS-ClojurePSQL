(ns wspsql.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [compojure.handler :as handler]
            [ring.middleware.json :as middleware]
            [ring.util.response :as ring]
            [wspsql.controllers.edges :as edges]
            [wspsql.controllers.centrality :as centrality]
            [wspsql.controllers.fraud :as fraud]
            [wspsql.views.layout :as layout]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]))

(defroutes app-routes
    (context "/edges" [] 
      edges/routes
    )
    (context "/graph" [] 
      centrality/routes
    )
    (context "/fraud" [] 
      fraud/routes
    )
    (GET "/" [] 
      (layout/home))
    (OPTIONS "/" []
      (layout/options [:options :get] {:version "3.0.0"}))
    (HEAD "/" [] 
      (layout/standard nil nil))
    (ANY "/" []
        (layout/method-not-allowed [:options :get]))
    (route/not-found (layout/four-oh-four))
)

(def app
  (-> (handler/api app-routes)
      (middleware/wrap-json-body)
      (middleware/wrap-json-response)
  )
)
