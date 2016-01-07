(ns wspsql.controllers.edges
  (:require [compojure.core :refer [defroutes GET POST DELETE PUT OPTIONS HEAD ANY]]
            [compojure.route :as route]
            [clojure.string :as str]
            [ring.util.response :as ring]
            [wspsql.models.edges :as edges]
            [wspsql.views.layout :as layout]
            [wspsql.views.edges :as layout_edges]
            [wspsql.controllers.core :as core]
            [wspsql.models.graph :as graph]
            [wspsql.models.fraud :as fraud]))

;JSON de resposta de OPTIONS
(def options-descript 
  {:GET {
    :description "Mostra todas as ligacoes existentes",
    :comments "Exibe um Form que se for preenchido corretamente, gera uma ligacao por POST"},
   :POST {
    :description "Cria uma ligacao entre dois nos no grafo",
    :parameters {
     :no_a {
      :type "integer",
      :description "Primeiro no da ligacao."
      :required true},
     :no_b {
      :type "integer",
      :description "Segundo no da ligacao."
      :required true}},
    :comments "Gera a ligacao apenas se ela ainda nao existir. Se ela ja existir devolve uma aviso"},
   :PUT {
    :description "Cria uma ligacao entre dois nos no grafo",
    :parameters {
     :no_a {
      :type "integer",
      :description "Primeiro no da ligacao."
      :required true},
     :no_b {
      :type "integer",
      :description "Segundo no da ligacao."
      :required true}},
    :comments "Se a ligacao ja existir, apaga ela, e cria uma nova. Isso atualiza sua data de criacao"},
   :DELETE {
    :description "Deleta uma ligacao entre dois nos no grafo",
    :parameters {
     :no_a {
      :type "integer",
      :description "Primeiro no da ligacao."
      :required true},
     :no_b {
      :type "integer",
      :description "Segundo no da ligacao."
      :required true}},
    :comments "Deleta a ligacao apenas se ela existir."}})

(defn index []  (layout_edges/index (edges/show-all)))

(defn create-post-edge 
  "Criacao de Edge por POST"
  [A B]
  (when (and (not (or (str/blank? A) (str/blank? B)))
             (and 
              (let [s (drop-while #(Character/isDigit %) A)] (empty? s))
              (let [s (drop-while #(Character/isDigit %) B)] (empty? s)))
             (not (= A B)))      
    (if-not (edges/exist? A B)
      (edges/create (int (read-string A)) (int (read-string B))))) 
  (ring/redirect "/edges/"))


(defn delete-edge
  "Delecao de Edge por DELETE"
  [A B]
  (when-not (and (not (or (str/blank? A) (str/blank? B)))
                 (and 
                  (let [s (drop-while #(Character/isDigit %) A)] (empty? s))
                  (let [s (drop-while #(Character/isDigit %) B)] (empty? s)))
                 (not (= A B))
                 (edges/exist? A B))
    (ring/not-found "erro =/")) 

  (when  (and (not (or (str/blank? A) (str/blank? B)))
              (and 
               (let [s (drop-while #(Character/isDigit %) A)] (empty? s))
               (let [s (drop-while #(Character/isDigit %) B)] (empty? s)))
              (not (= A B))
              (edges/exist? A B))
    (edges/delete (int (read-string A)) (int (read-string B)))
    (core/farness (edges/show-all-edges))
    ;Se o no deixar de existir e ele possuir uma fraude, essa fraude deve ser deletada
    (if (not (graph/node-exist? (int (read-string A)))) 
      (if (fraud/fraudulent? 
        (int (read-string A))) 
        (fraud/delete-fraudulent (int (read-string A)))))
    (if (not (graph/node-exist? (int (read-string B)))) 
      (if (fraud/fraudulent? 
        (int (read-string B))) 
        (fraud/delete-fraudulent (int (read-string B)))))
    (ring/response "done")))

(defn create-put-edge
  "Criacao de Edge por PUT"
  [A B]
  (when-not (and (not (or (str/blank? A) (str/blank? B)))
                 (and 
                  (let [s (drop-while #(Character/isDigit %) A)] (empty? s))
                  (let [s (drop-while #(Character/isDigit %) B)] (empty? s)))
                 (not (= A B)))

    (ring/not-found "erro =/"))
  
  (when (and (not (or (str/blank? A) (str/blank? B)))
             (and 
              (let [s (drop-while #(Character/isDigit %) A)] (empty? s))
              (let [s (drop-while #(Character/isDigit %) B)] (empty? s)))
             (not (= A B)))

    (if (edges/exist? A B)
      (edges/delete (int (read-string A)) (int (read-string B)))) ;PUT ira incluir de qualquer jeito, por isso, deleta (se existir) antes de incluir
    (edges/create (int (read-string A)) (int (read-string B)))
    (ring/response "done")))

(defroutes routes
  (GET "/" [] 
    (index))
  (POST "/" [no_a no_b] 
    (create-post-edge  no_a no_b))
  (DELETE "/:A/:B" [A B] 
    (delete-edge A B))
  (PUT "/:A/:B" [A B] 
    (create-put-edge A B))
  (OPTIONS "/" []
    (layout/options [:options :get :head :put :post :delete] options-descript))
  (HEAD "/" [] 
    (layout/standard nil nil))
  (ANY "/" []
    (layout/method-not-allowed [:options :get :head :put :post :delete]))
  (route/not-found 
    (layout/four-oh-four)))