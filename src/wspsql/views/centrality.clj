(ns wspsql.views.centrality
  (:require [wspsql.views.layout :as layout]
            [hiccup.core :refer [h]]
            [ring.util.anti-forgery :as anti-forgery]))


(defn display-edges [edges]
  [:div {:class "nodes centrality"}
   (map
    (fn [edges] [:div {:style "position: relative; left: 20px;"} [:h4 {:class "nodes"} (str (:no edges) " - " (:closeness edges) )]])
    edges)])

(defn index [edges]
  (layout/standard "nodes"
                 [:div [:h3 {:class "tabela"} "NO - CLOSENESS"]]
                 (display-edges edges)
  )
)