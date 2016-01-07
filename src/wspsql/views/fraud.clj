(ns wspsql.views.fraud
  (:require [wspsql.views.layout :as layout]
            [hiccup.core :refer [h]]
            [hiccup.form :as form]
            [ring.util.anti-forgery :as anti-forgery]))

(defn nodes-fraud-form []
  [:div {:id "nodes-fraud-form" :class "fraudform"}
    (form/form-to [:post "/fraud"]
                 (anti-forgery/anti-forgery-field)
                 (form/label {:style "position: relative; top: -7px;"} "node_label" "No: ")
                 (form/text-area {:style "border: 2px solid #888; width: 50px; height: 18px;"} "no")
                 (form/submit-button {:style "position: relative; top: -8px; left: 10px;"} "SAVE"))])

(defn nodes-fraud-display [nodes-fraud]
  [:div {:class "nodesfraud"}
    (map
      (fn [nos] [:h3 {:style "position: relative; left: 25px; line-height: 40%;"} (str (:no nos))])
      nodes-fraud)])

(defn index [nodes-fraud]
  (layout/standard "edges"
                 (nodes-fraud-form)
                 [:div [:h2 {:class "tabela" :style "line-height: 80%;"} "Fraudes"]]
                 (nodes-fraud-display nodes-fraud)))