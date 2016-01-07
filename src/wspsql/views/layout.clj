(ns wspsql.views.layout
  (:require [hiccup.page :as h]
            [ring.util.response :refer :all]
            [clojure.string :refer [upper-case join]]))

(defn standard [title & body]
  (h/html5
   [:head
    [:meta {:charset "utf-8"}]
    [:meta {:http-equiv "X-UA-Compatible" :content "IE=edge,chrome=1"}]
    [:meta {:name "viewport" :content
            "width=device-width, initial-scale=1, maximum-scale=1"}]
    [:title title]
    (h/include-css "http://fonts.googleapis.com/css?family=Sigmar+One&v1")]
   [:body
    [:div {:id "header"}
     [:h1 "Web Service - Analise Grafos"]]
    [:div {:id "content" :class "container"} body]]))

(defn home []
  (standard "Home"
          [:div {:id "Home"}
           "Página inicial <br><br>
            Acesse <a href=\"/edges\">/edges</a> para controle das edges do grafo <br>
            Acesse <a href=\"/graph\">/graph</a> para obtencao das centralidades dos nos do grafo <br>
            Acesse <a href=\"/fraud\">/fraud</a> para controle das fraudes dos nos <br>
            "]))

(defn four-oh-four []
  (h/html5
    [:head
      [:meta {:charset "utf-8"}]
    ]
    [:body
      [:div "Essa página não existe =["]
    ]
  )
)

(defn options
  ([] (options #{:options} nil))
  ([allowed] (options allowed nil))
  ([allowed body]
    (->
      (response body)
      (header "Allow" (join ", " (map (comp upper-case name) allowed))))))

(defn method-not-allowed
  [allowed]
    (->
      (options allowed)
      (status 405)))