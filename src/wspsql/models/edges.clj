(ns wspsql.models.edges
  (:require [clojure.java.jdbc :as sql]
  			[wspsql.models.updatesys :as updatesys]))


(def spec "postgresql://postgres:230789@172.17.0.2:5432/wsclojure")

(defn all []
  (into [] (sql/query spec ["select * from edges order by created desc"])))

(defn all-edges []
  (into [] (sql/query spec ["select noa, nob from edges"])))

(defn exist? [A B]
	(-> (sql/query spec
                 [(str "select * from edges where noa = " A " and nob = " B)]
        )
      	first :created nil? not)
)

(defn time-created [A B]
	(into [] (sql/query spec [(str "select * from edges where noa = " A " and nob = " B "order by created desc")]))
)

(defn last-insert []
	(-> (into [] (sql/query spec ["select created from edges order by created desc limit 1"]))
		first
		:created
	)
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