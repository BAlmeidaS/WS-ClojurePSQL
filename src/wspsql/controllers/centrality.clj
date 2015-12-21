(ns wspsql.controllers.centrality
  (:require [compojure.core :refer [defroutes GET POST DELETE PUT OPTIONS HEAD ANY]]
            [compojure.route :as route]
            [cheshire.core :refer [generate-string]]
            [clojure.string :as str]
            [clojure.core :refer [read-string]]
            [ring.util.response :as ring]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [wspsql.models.edges :as edges]
            [wspsql.views.layout :as layout]
            [wspsql.views.centrality :as layout_centrality]
            [wspsql.controllers.firstpart :as firstpart]
            [wspsql.controllers.assist :as assist]
            [wspsql.models.centrality :as centrality]
            [wspsql.models.updatesys :as updatesys]
  )
)

(def options-descript 
  { 
    :GET {
      :description "Mostra uma tabela com os nos dos grafos e seu closeness.",
      :comments "A tabela é classificada em ordem decrescente do valor do closeness"
    },
    :GET2 {
      :description "Devolve um JSON com os dados de centralidade do No e se ele é fraudulento",
      :parameters {
        :no {
          :type "integer"
          :description "No a ser consultado",
          :required "true"
        }
      },
      :comments "Caso o no nao exista devolve 'null'"
    }
  }
)

(defn update-centrality [base]
  (if (-> (edges/last-insert) (compare (updatesys/get-update)) pos?)
      (firstpart/farness base)
  )
  (centrality/all-closeness)
)


(defn index []   
  (def base (edges/all-edges))
  (layout_centrality/index (update-centrality base))
)

(defn nodeinfo
  "retorna um map com as informacoes do no"
  [no]
  (def closeness (firstpart/farnessNode (assist/cast-int no) (edges/all-edges)))
  (zipmap
    [:no :closeness :farness :fraudulent]
    (vector no closeness (float (/ 1 closeness)) false)
  )
)


(defn nodeGet [no] 
  (when-not (str/blank? no)
    (when (assist/isnumber? no)
      (if (centrality/node-exist? no)
        (ring/response (nodeinfo no)) 
        (ring/response "null") 
      )
    )    
  )  
)



(defroutes routes
  (GET "/" [] 
    (index))
  (GET "/:no" [no] 
    (nodeGet no))
  (OPTIONS "/" []
        (layout/options [:options :get :head] options-descript))
  (HEAD "/" [] 
    (layout/standard nil nil))
  (ANY "/" []
       (layout/method-not-allowed [:options :get :head :put :post :delete]))
  (route/not-found (layout/four-oh-four))
)