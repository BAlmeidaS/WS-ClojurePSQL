(ns wspsql.views.fraud
  (:require [wspsql.views.layout :as layout]
            [hiccup.core :refer [h]]
            [hiccup.form :as form]
            [ring.util.anti-forgery :as anti-forgery]))

(defn nos-fraud-form []
  [:div {:id "nos-fraud-form" :class "sixteen columns alpha omega"}
    (form/form-to [:post "/fraud"]
                 (anti-forgery/anti-forgery-field)
                 (form/label {:style "position: relative; top: -7px;"} "no" "No: ")
                 (form/text-area {:style "border: 2px solid #888; width: 50px; height: 18px;"} "no")
                 (form/submit-button {:style "position: relative; top: -8px; left: 10px;"} "SAVE"))
  ])

(defn display-nos-fraud [nos-fraud]
  [:div {:class "nosfraud sixteen columns alpha omega"}
    (map
      (fn [nos] [:h3 {:class "nosfraud"} (str (:no nos))])
      nos-fraud
    )
  ]
)

(defn index [nos-fraud]
  (layout/standard "edges"
                 (nos-fraud-form)
                 [:div {:class "clear"}]
                 (display-nos-fraud nos-fraud)
  )
)