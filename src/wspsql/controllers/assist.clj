(ns wspsql.controllers.assist
  (:require [cheshire.core :refer [generate-string]]
            [clojure.string :as str]
            [clojure.core :refer [read-string]]
  )
)

(defn isnumber? [s]
  (if-let [s (seq s)]
    (let  [ ;negativo:
            ;s (if (= (first s) \-) (next s) s)
            s (drop-while #(Character/isDigit %) s)
            ;reconhecimento de ponto
            ;s (if (= (first s) \.) (next s) s)
            ;s (drop-while #(Character/isDigit %) s)
          ]
      (empty? s)
    )
  )
)

(defn cast-int [s] (Integer. (re-find  #"\d+" s )))

