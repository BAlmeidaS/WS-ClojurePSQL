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
    (h/include-css "")
    (h/include-css "http://fonts.googleapis.com/css?family=Sigmar+One&v1")]
   [:body
    [:div {:id "header"}
     [:h1 {:class "container"} "Web Service - Analise Grafos"]]
    [:div {:id "content" :class "container"} body]]))

(defn home []
  (standard "Home"
          [:div {:id "Home"}
           "Página inicial <br><br> Acesse /edges para mais opções"]))

(defn four-oh-four []
  (standard "Page Not Found"
          [:div {:id "four-oh-four"}
           "Essa página não existe =["]))

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