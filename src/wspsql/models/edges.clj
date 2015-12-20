(ns wspsql.models.edges
  (:require [clojure.java.jdbc :as sql]))

(def spec (or (System/getenv "DATABASE_URL")
              "postgresql://postgres:230789@172.17.0.2:5432/wsclojure"))

(defn all []
  (into [] (sql/query spec ["select * from edges order by created desc"])))

(defn exist? [A B]
	(-> (sql/query spec
                 [(str "select * from edges where noa = " A " and nob = " B)]
        )
      	first :created nil? not)
)

(defn create [A B]
	(print (str "Inserindo ligacao " A "-" B"..."))
	(sql/insert! spec :edges (zipmap [:noA :noB][(int A) (int B)]))
	(println "feito!")
)

(defn delete [A B]
	(print (str "Apagando ligacao " A "-" B"..."))
	(sql/delete! spec :edges ["noa=? and nob=?" A B])
	(println "feito!")
)


;(defn create2 [A B]
;	(when (non-exist? A B)
;		(print (str "Inserindo ligacao " A "-" B"..."))
;		(sql/insert! spec :edges (zipmap [:noA :noB][A B]))
;		(println "feito!")
;	)
;)