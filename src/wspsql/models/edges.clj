(ns wspsql.models.edges
  (:require [clojure.java.jdbc :as sql]
  			[wspsql.models.migration :as migration]))


(defn all "Retorna um vetor com as edges ordenadas por data de criacao." 
	[]
  	(into [] (sql/query migration/spec ["select * from edges order by created desc"]))
)

(defn all-edges "Retorna um vetor com as edges cadastradas." 
	[]
  	(into [] (sql/query migration/spec ["select noa, nob from edges"]))
 )

(defn exist? "Verifica se existe a ligacao." 
	[A B]
	(def return false)
	(-> (sql/query migration/spec
                 [(str "select * from edges where noa = " A " and nob = " B)]
        )
      	first :created nil? not (if (def return true))
    )
    (-> (sql/query migration/spec
                 [(str "select * from edges where noa = " B " and nob = " A)]
        )
      	first :created nil? not (if (def return true))
    )
    (if return true false)
)

(defn last-insert "Retorna a data do ultimo edge cadastrado."
	[]
	(-> (into [] (sql/query migration/spec ["select created from edges order by created desc limit 1"]))
		first
		:created
	)
)

(defn create "Cria edge A - B."
	[A B]
	(print (str "Inserindo ligacao " A "-" B"..."))
	(sql/insert! migration/spec :edges (zipmap [:noA :noB][(int A) (int B)]))
	(println "feito!")
)

(defn delete "Deleta edge A - B."
	[A B]
	(print (str "Apagando ligacao " A "-" B"..."))
	(if (-> (sql/query migration/spec [(str "select * from edges where noa = " A " and nob = " B)]) first :created nil? not) ;Verifica se esta cadastrado como noa = A e nob = B
		(sql/delete! migration/spec :edges ["noa=? and nob=?" A B])
		(sql/delete! migration/spec :edges ["noa=? and nob=?" B A])
	)
	(println "feito!")
)

(defn remove-all "funcao que remove todos os edges do banco"
	[]
	(sql/db-do-commands migration/spec (str "delete from edges"))
)

