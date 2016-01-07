(ns wspsql.views.graph
  (:require [wspsql.views.layout :as layout]
            [hiccup.core :refer [h]]
            [ring.util.anti-forgery :as anti-forgery]))


(defn display-edges [edges]
  [:div {:class "edges"}
   (map
    (fn [edges] [:div {:class "graph" :style "position: relative; left: 25px; line-height: 40%;"} [:h3 {:class "nodes"} (str (:no edges) " - " (format "%1.8f" (:closeness edges)) )]])
    edges)])

(defn index [edges]
  (layout/standard "nodes"
                 [:div [:h2 {:class "graph_table"} "NO - SCORE (CLOSENESS)"]]
                 (display-edges edges)))