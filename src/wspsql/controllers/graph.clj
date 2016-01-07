(ns wspsql.controllers.graph
  (:require [compojure.core :refer [defroutes GET POST DELETE PUT OPTIONS HEAD ANY]]
            [compojure.route :as route]
            [clojure.string :as str]
            [ring.util.response :as ring]
            [wspsql.models.edges :as edges]
            [wspsql.views.layout :as layout]
            [wspsql.views.graph :as layout_graph]
            [wspsql.controllers.core :as core]
            [wspsql.controllers.assist :as assist]
            [wspsql.models.graph :as graph]
            [wspsql.models.migration :as migration]
            [wspsql.models.fraud :as fraud]
  )
)

;JSON de resposta de OPTIONS
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
          :required true
        }
      },
      :comments "Caso o no nao exista devolve 'null'"
    }
  }
)

(defn update-centrality "Realiza Update da centralide, retorna um vetor com os Nos e suas Centralidades."
  [base]
  (core/farness base)
  (graph/all-closeness)
)

(defn index [] (layout_graph/index (update-centrality (edges/all-edges))))

(defn nodeCloseness "Retorna o closeness de um no em um vetor de nos."
  [no nodes]
  (if (empty? nodes) []
    (conj (nodeCloseness no (subvec nodes 1))
      (if (= no ((nodes 0) :no)) [] [])
    )
  )
)

(defn nodeInfo "Retorna um map com as informacoes do No."
  [no]
  (def x (graph/node-closeness no))
  (if (> x 0.0)
    (zipmap
      [:no :closeness :farness :fraudulent]
      (vector no x (Math/round (/ 1 x)) (fraud/fraudulent? no) )
    )
    (zipmap
      [:no :closeness :farness :fraudulent]
      (vector no 0 0 (fraud/fraudulent? no) )
    )
  )

)

(defn nodeGet "Retorna as informacoes do no em um request GET com parametro NO"
  [no] 
  (core/farness (edges/all-edges))
  (when (and (not(str/blank? no)) (assist/isnumber? no))
    (if (graph/node-exist? no)
      (ring/response (nodeInfo (assist/cast-int no)))
      (ring/not-found "null") 
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