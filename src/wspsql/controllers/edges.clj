(ns wspsql.controllers.edges
  (:require [compojure.core :refer [defroutes GET POST DELETE PUT OPTIONS HEAD ANY]]
            [compojure.route :as route]
            [cheshire.core :refer [generate-string]]
            [clojure.string :as str]
            [clojure.core :refer [read-string]]
            [ring.util.response :as ring]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [wspsql.models.edges :as edges]
            [wspsql.views.layout :as layout]
            [wspsql.views.edges :as layout_edges]
            [wspsql.controllers.assist :as assist]
  )
)

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
          :required "true"
        },
        :no_b {
          :type "integer",
          :description "Segundo no da ligacao."
          :required "true"
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
          :required "true"
        },
        :no_b {
          :type "integer",
          :description "Segundo no da ligacao."
          :required "true"
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
          :required "true"
        },
        :no_b {
          :type "integer",
          :description "Segundo no da ligacao."
          :required "true"
        }
      },
      :comments "Deleta a ligacao apenas se ela existir."
    }
  }
)

(defn index []   (layout_edges/index (edges/all)))

(defn create_post
  [A B]
  (when-not (or (str/blank? A) (str/blank? B))
    (when (and (assist/isnumber? A) (assist/isnumber? B))
      (when-not (= A B)
        (if (edges/exist? A B)
          (println "no ja cadastrado") ;TEM QUE MUDAR
          (edges/create (assist/cast-int A) (assist/cast-int B))
        )
      )      
    )    
    ;(println (str A "-" B))
    ;(println (isnumber? A))
    ;(println (isnumber? B))
  )
  (ring/redirect "/edges/")
)

(defn delete
  ([A B]
  (when-not (or (str/blank? A) (str/blank? B))
    (when (and (assist/isnumber? A) (assist/isnumber? B))
      (when-not (= A B)
        (if (edges/exist? A B)
          (edges/delete (assist/cast-int A) (assist/cast-int B))
          (println "no não cadastrado2") ;TEM QUE MUDAR
        )
      )      
    )    
  )
  (ring/redirect "/")
  )
)

(defn create_put
  [A B]
  (when-not (or (str/blank? A) (str/blank? B))
    (when (and (assist/isnumber? A) (assist/isnumber? B))
      (when-not (= A B)
        (if (edges/exist? A B)
          (edges/delete (assist/cast-int A) (assist/cast-int B)) ;TEM QUE MUDAR
        )
        (edges/create (assist/cast-int A) (assist/cast-int B))
      )      
    )    
    ;(println (str A "-" B))
    ;(println (isnumber? A))
    ;(println (isnumber? B))
  )
  (ring/redirect "/edges/")
)


(defroutes routes
  (GET "/" [] 
    (index))
  (POST "/" [no_a no_b] 
    (create_post no_a no_b))
  (DELETE "/:A/:B" [A B] 
    (delete A B))
  (PUT "/:A/:B" [A B] 
    (create_put A B))
  (OPTIONS "/" []
        (layout/options [:options :get :head :put :post :delete] options-descript))
  (HEAD "/" [] 
    (layout/standard nil nil))
  (ANY "/" []
        (layout/method-not-allowed [:options :get :head :put :post :delete]))
  (route/not-found (layout/four-oh-four))
)