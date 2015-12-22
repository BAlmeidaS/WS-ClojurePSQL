(ns wspsql.controllers.fraud
  (:require [compojure.core :refer [defroutes GET POST DELETE PUT OPTIONS HEAD ANY]]
            [compojure.route :as route]
            [clojure.string :as str]
            [ring.util.response :as ring]
            [wspsql.models.edges :as edges]
            [wspsql.views.layout :as layout]
            [wspsql.views.fraud :as layout_fraud]
            [wspsql.controllers.core :as core]
            [wspsql.controllers.assist :as assist]
            [wspsql.models.centrality :as centrality]
            [wspsql.models.fraud :as fraud]
  )
)

;JSON de resposta de OPTIONS
(def options-descript 
  { 
    :GET {
      :description "Mostra todas os nós que possuem fraude.",
      :comments "Não descrimina se a fraude foi ou não aplicada" 
    },
    :POST {
      :description "Aplica fraude sobre um Nó",
      :parameters {
        :no {
          :type "integer",
          :description "Nó Fraudulento"
          :required true
        }
      },
      :comments "O Score do Nó fraudulento é reduzido a zero. Todos os outros nós do grafo tem seu score multiplicado pela função (1 - (1/2)^k), onde k é a distancia entre o nó fraudulento e o nó não fraudulento."
    },
    :PUT {
      :description "Aplica fraude sobre um nó",
      :parameters {
        :no {
          :type "integer",
          :description "No Fraudulento"
          :required true
        }
      },
      :comments "O Score do Nó fraudulento é reduzido a zero. Todos os outros nós do grafo tem seu score multiplicado pela função (1 - (1/2)^k), onde k é a distancia entre o nó fraudulento e o nó não fraudulento."
    },
    :DELETE {
      :description "Apaga a fraude de um nó.",
      :parameters {
        :no {
          :type "integer",
          :description "No Fraudulento"
          :required true
        }
      }
      :comments "Deleta, se existir, a fraude de um nó."
    }
  }
)

(defn index []   
  (core/farness (edges/all-edges))
  (layout_fraud/index (fraud/all)) 
)

(defn fraud-node-post "Aplica Fraude no No por POST."
  [no]
  (when     (and  (not (str/blank? no))
                  (assist/isnumber? no)
                  (centrality/node-exist? no)
                  (not (fraud/fraudulent? no)))

    (fraud/set-fraudulent (assist/cast-int no))
    (core/fraud (edges/all-edges))
  )
  (ring/redirect "/fraud/")
)

(defn remove-fraud "Remove Fraude do No por DELETE."
  [no]
  (when-not (and  (not (str/blank? no))
                  (assist/isnumber? no)
                  (centrality/node-exist? no)
                  (fraud/fraudulent? no))
  
    (ring/not-found "erro =/")
  )
  (when     (and  (not (str/blank? no))
                  (assist/isnumber? no)
                  (centrality/node-exist? no)
                  (fraud/fraudulent? no))
  
    (fraud/delete-fraudulent (assist/cast-int no)) 
    (core/farness (edges/all-edges)) 
    (ring/response "done")
  )
)

(defn fraud-node-put "Aplica Fraude no No por PUT."
  [no]
  (when-not (and  (not (str/blank? no))
                  (assist/isnumber? no)
                  (centrality/node-exist? no))
  
    (ring/not-found "erro =/")
  )

  (when     (and  (not (str/blank? no))
                  (assist/isnumber? no)
                  (centrality/node-exist? no))
  
    (if (fraud/fraudulent? no)
      (fraud/delete-fraudulent (assist/cast-int no)) 
    )
    (core/farness (edges/all-edges))
    (fraud/set-fraudulent (assist/cast-int no))
    (core/fraud (edges/all-edges))
    (ring/response "done")
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
  (OPTIONS "/" []
        (layout/options [:options :get :head] options-descript))
  (HEAD "/" [] 
    (layout/standard nil nil))
  (ANY "/" []
       (layout/method-not-allowed [:options :get :head :put :post :delete]))
  (route/not-found (layout/four-oh-four))
)