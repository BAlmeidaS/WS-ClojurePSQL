(ns wspsql.controllers.assist
  (:require [cheshire.core :refer [generate-string]]
            [clojure.string :as str]
            [clojure.core :refer [read-string]]
  )
)

(defn isnumber? [s]
  (if-let [s (seq s)]
    (let [s (drop-while #(Character/isDigit %) s)]
      (empty? s))))

(defn cast-int [s] (Integer. (re-find  #"\d+" s )))

