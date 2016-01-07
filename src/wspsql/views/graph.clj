(ns wspsql.views.graph
  (:require [wspsql.views.layout :as layout]
            [hiccup.core :refer [h]]
            [ring.util.anti-forgery :as anti-forgery]))


(defn display-edges [edges]
  [:div {:class "nodes sixteen columns alpha omega"}
   (map
    (fn [edges] [:div {:class "nodes_centrality" :style "position: relative; left: 25px; line-height: 40%;"} [:h3 {:class "nodes"} (str (:no edges) " - " (format "%1.8f" (:closeness edges)) )]])
    edges)])

(defn index [edges]
  (layout/standard "nodes"
                 [:div [:h2 {:class "tabela"} "NO - SCORE (CLOSENESS)"]]
                 (display-edges edges)
  )
)