(ns wspsql.controllers.edges
  (:require [compojure.core :refer [defroutes GET POST DELETE PUT OPTIONS HEAD ANY]]
            [compojure.route :as route]
            [clojure.string :as str]
            [ring.util.response :as ring]
            [wspsql.models.edges :as edges]
            [wspsql.views.layout :as layout]
            [wspsql.views.edges :as layout_edges]
            [wspsql.controllers.assist :as assist]
            [wspsql.controllers.core :as core]
            [wspsql.models.centrality :as centrality]
            [wspsql.models.fraud :as fraud]            
  )
)

;JSON de resposta de OPTIONS
(def options-descript 
  { 
    :GET {
      :description "Mostra todas as ligacoes existentes",
      :comments "Exibe um Form que se for preenchido corretamente, gera uma ligacao por POST"
    },
    :POST {
      :description "Cria uma ligacao entre dois nos no grafo",
      :parameters {
        :no_a {
          :type "integer",
          :description "Primeiro no da ligacao."
          :required true
        },
        :no_b {
          :type "integer",
          :description "Segundo no da ligacao."
          :required true
        }
      },
      :comments "Gera a ligacao apenas se ela ainda nao existir. Se ela ja existir devolve uma aviso"
    },
    :PUT {
      :description "Cria uma ligacao entre dois nos no grafo",
      :parameters {
        :no_a {
          :type "integer",
          :description "Primeiro no da ligacao."
          :required true
        },
        :no_b {
          :type "integer",
          :description "Segundo no da ligacao."
          :required true
        }
      },
      :comments "Se a ligacao ja existir, apaga ela, e cria uma nova. Isso atualiza sua data de criacao"
    },
    :DELETE {
      :description "Deleta uma ligacao entre dois nos no grafo",
      :parameters {
        :no_a {
          :type "integer",
          :description "Primeiro no da ligacao."
          :required true
        },
        :no_b {
          :type "integer",
          :description "Segundo no da ligacao."
          :required true
        }
      },
      :comments "Deleta a ligacao apenas se ela existir."
    }
  }
)

(defn index []  (layout_edges/index (edges/all)))

(defn create-post "Criacao de Edge por POST"
  [A B]
  (when-not (or (str/blank? A) (str/blank? B))
    (when (and (assist/isnumber? A) (assist/isnumber? B))
      (when-not (= A B)
        (if (edges/exist? A B)
          (println (str "Ligacao " A "-" B " ja cadastrado"))
          (edges/create (assist/cast-int A) (assist/cast-int B))
        )
      )      
    )    
  )
  (ring/redirect "/edges/")
)

(defn delete "Delecao de Edge por DELETE"
  ([A B]
  (when-not (or (str/blank? A) (str/blank? B))
    (when (and (assist/isnumber? A) (assist/isnumber? B))
      (when-not (= A B)
        (when (edges/exist? A B)
          (edges/delete (assist/cast-int A) (assist/cast-int B))
          (core/farness (edges/all-edges))
          ;Se o no deixar de existir e ele possuir uma fraude, essa fraude deve ser deletada
          (if (not (centrality/node-exist? (assist/cast-int A))) 
            (if (fraud/fraudulent? (assist/cast-int A)) (fraud/delete-fraudulent (assist/cast-int A)))
          )
          (if (not (centrality/node-exist? (assist/cast-int B))) 
            (if (fraud/fraudulent? (assist/cast-int B)) (fraud/delete-fraudulent (assist/cast-int B)))
          )
        )
      )      
    )    
  )
  (ring/redirect "/done")
  )
)

(defn create-put "Criacao de Edge por PUT"
  [A B]
  (when-not (or (str/blank? A) (str/blank? B))
    (when (and (assist/isnumber? A) (assist/isnumber? B))
      (when-not (= A B)
        (if (edges/exist? A B)
          (edges/delete (assist/cast-int A) (assist/cast-int B)) ;PUT ira incluir de qualquer jeito, por isso, deleta (se existir) antes de incluir
        )
        (edges/create (assist/cast-int A) (assist/cast-int B))
      )      
    )    
  )
  (ring/redirect "/done")
)


(defroutes routes
  (GET "/" [] 
    (index))
  (POST "/" [no_a no_b] 
    (create-post no_a no_b))
  (DELETE "/:A/:B" [A B] 
    (delete A B))
  (PUT "/:A/:B" [A B] 
    (create-put A B))
  (OPTIONS "/" []
        (layout/options [:options :get :head :put :post :delete] options-descript))
  (HEAD "/" [] 
    (layout/standard nil nil))
  (ANY "/" []
        (layout/method-not-allowed [:options :get :head :put :post :delete]))
  (route/not-found (layout/four-oh-four))
)