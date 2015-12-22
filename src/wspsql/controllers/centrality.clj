(ns wspsql.controllers.centrality
  (:require [compojure.core :refer [defroutes GET POST DELETE PUT OPTIONS HEAD ANY]]
            [compojure.route :as route]
            [clojure.string :as str]
            [ring.util.response :as ring]
            [wspsql.models.edges :as edges]
            [wspsql.views.layout :as layout]
            [wspsql.views.centrality :as layout_centrality]
            [wspsql.controllers.core :as core]
            [wspsql.controllers.assist :as assist]
            [wspsql.models.centrality :as centrality]
            [wspsql.models.updatesys :as updatesys]
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
  (if (-> (edges/last-insert) (compare (updatesys/get-update)) pos?) ;So recalcula a centralidade se a data da ultimo calculo for anterior a inclusao do ultimo no.
      (core/farness base)
  )
  (centrality/all-closeness)
)


(defn index [] (layout_centrality/index (update-centrality (edges/all-edges))))

(defn nodecloseness "Retorna o closeness de um no em um vetor de nos."
  [no nodes]
  (if (empty? nodes) []
    (conj (nodecloseness no (subvec nodes 1))
      (if (= no ((nodes 0) :no)) [] [])
    )
  )
)

(defn nodeinfo "Retorna um map com as informacoes do No."
  [no]
  (core/farness (edges/all-edges))
  (def x (centrality/node-closeness no))
  (if (> x 0)
    (zipmap
      [:no :closeness :farness :fraudulent]
      (vector no x (float (/ 1 x)) (fraud/fraudulent? no) )
    )
    (zipmap
      [:no :closeness :farness :fraudulent]
      (vector no 0 0.0 (fraud/fraudulent? no) )
    )
  )

)

(defn nodeGet "Retorna as informacoes do no em um request GET com parametro NO"
  [no] 
  (when (and (not(str/blank? no)) (assist/isnumber? no))
    (if (centrality/node-exist? no)
      (ring/response (nodeinfo (assist/cast-int no)))
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