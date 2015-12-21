(ns wspsql.models.edges
  (:require [clojure.java.jdbc :as sql]
  			[wspsql.models.migration :as migration]))


(def spec "postgresql://postgres:230789@172.17.0.2:5432/wsclojure")

(defn all "Retorna um vetor com as edges ordenadas por data de criacao" 
	[]
  	(into [] (sql/query spec ["select * from edges order by created desc"]))
)

(defn all-edges "Retorna um vetor com as edges cadastradas" 
	[]
  	(into [] (sql/query spec ["select noa, nob from edges"]))
 )

(defn exist? "Verifica se existe a ligacao" 
	[A B]
	(def return false)
	(-> (sql/query spec
                 [(str "select * from edges where noa = " A " and nob = " B)]
        )
      	first :created nil? not (if (def return true))
    )
    (-> (sql/query spec
                 [(str "select * from edges where noa = " B " and nob = " A)]
        )
      	first :created nil? not (if (def return true))
    )
    (if return true false)
)

(defn last-insert "Retorna a data do ultimo edge cadastrado"
	[]
	(-> (into [] (sql/query spec ["select created from edges order by created desc limit 1"]))
		first
		:created
	)
)

(defn create "Cria edge A - B"
	[A B]
	(print (str "Inserindo ligacao " A "-" B"..."))
	(sql/insert! spec :edges (zipmap [:noA :noB][(int A) (int B)]))
	(println "feito!")
)

(defn delete "Deleta edge A - B"
	[A B]
	(print (str "Apagando ligacao " A "-" B"..."))
	(if (-> (sql/query spec [(str "select * from edges where noa = " A " and nob = " B)]) first :created nil? not) ;Verifica se esta cadastrado como noa = A e nob = B
		(sql/delete! spec :edges ["noa=? and nob=?" A B])
		(sql/delete! spec :edges ["noa=? and nob=?" B A])
	)
	(println "feito!")
)

;RETIRAR

;(defn time-created ""
;	[A B]
;	(into [] (sql/query spec [(str "select * from edges where noa = " A " and nob = " B "order by created desc")]))
;)