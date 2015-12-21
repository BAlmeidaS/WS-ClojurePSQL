(ns wspsql.controllers.fraud
  (:require [compojure.core :refer [defroutes GET POST DELETE PUT OPTIONS HEAD ANY]]
            [compojure.route :as route]
            [cheshire.core :refer [generate-string]]
            [clojure.string :as str]
            [clojure.core :refer [read-string]]
            [ring.util.response :as ring]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [wspsql.models.edges :as edges]
            [wspsql.views.layout :as layout]
            [wspsql.views.fraud :as layout_fraud]
            [wspsql.controllers.firstpart :as firstpart]
            [wspsql.controllers.assist :as assist]
            [wspsql.models.centrality :as centrality]
            [wspsql.models.updatesys :as updatesys]
            [wspsql.models.fraud :as fraud]
            [wspsql.models.migration :as migration]
  )
)



(defn index []   
  (firstpart/fraud (edges/all-edges))
  (layout_fraud/index (fraud/all)) 
)

(defn fraud-node-post [no]
  (when-not (str/blank? no)
    (when (assist/isnumber? no)
      (when (centrality/node-exist? no)
        (when-not (fraud/fraudulent? no)
          (fraud/set-fraudulent (assist/cast-int no))
          (firstpart/fraud (edges/all-edges))
        )  
      )    
    )    
  )
  (ring/redirect "/fraud/")
)

(defn remove-fraud [no]
  (when-not (str/blank? no)
    (when (assist/isnumber? no)
      (when (centrality/node-exist? no)
        (when (fraud/fraudulent? no)
          (fraud/delete-fraudulent (assist/cast-int no)) 
          (firstpart/farness (edges/all-edges))
        )
      ) 
    )    
  )
)

(defn fraud-node-put [no]
  (when-not (str/blank? no)
    (when (assist/isnumber? no)
      (when (centrality/node-exist? no)
        (if (fraud/fraudulent? no)
          (fraud/delete-fraudulent no) 
        )
        (fraud/set-fraudulent (assist/cast-int no))
        (firstpart/fraud (edges/all-edges))
      )
    )    
  )
)

(defroutes routes
  (GET "/" [] 
    (index))
  (POST "/" [no] 
    (fraud-node-post no))
  (DELETE "/:no" [no] 
    (remove-fraud no))
  (PUT "/:no" [no] 
    (fraud-node-put no))
;  (OPTIONS "/" []
;        (layout/options [:options :get :head] options-descript))
  (HEAD "/" [] 
    (layout/standard nil nil))
  (ANY "/" []
       (layout/method-not-allowed [:options :get :head :put :post :delete]))
  (route/not-found (layout/four-oh-four))
)