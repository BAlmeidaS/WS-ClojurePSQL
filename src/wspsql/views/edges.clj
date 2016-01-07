(ns wspsql.views.edges
  (:require [wspsql.views.layout :as layout]
            [hiccup.core :refer [h]]
            [hiccup.form :as form]
            [ring.util.anti-forgery :as anti-forgery]))

(defn edges-form []
  [:div {:id "edges-form" :class "edges_form"}
    (form/form-to [:post "/edges"]
                 (anti-forgery/anti-forgery-field)
                 (form/label {:style "position: relative; top: -7px;"} "no_A" "No A: ")
                 (form/text-area {:style "border: 2px solid #888; width: 50px; height: 18px;"} "no_a")
                 (form/label {:style "position: relative; top: -7px;"} "no_B" " No B: ")
                 (form/text-area {:style "border: 2px solid #888; width: 50px; height: 18px;"} "no_b")
                 (form/submit-button {:style "position: relative; top: -8px; left: 10px;"} "SAVE"))])

(defn display-edges [edges]
  [:div {:class "edges"}
   (map
    (fn [edges] [:h3 {:class "edges" :style "position: relative; left: 25px; line-height: 40%;"}(str (:noa edges) "-" (:nob edges) )])
    edges)])

(defn index [edges]
  (layout/standard "edges"
                 (edges-form)
                 [:div [:h2 {:class "edges_table" :style "line-height: 80%;"} "Ligação"]]
                 (display-edges edges)))