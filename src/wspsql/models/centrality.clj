(ns wspsql.models.centrality
  (:require [clojure.java.jdbc :as sql]
  			[wspsql.models.migration :as migration]))

(defn exp "Funcao que calcula exponencial (x ^ n)."
	[x n]
	(if (zero? n) 1
    	(* x (exp x (dec n)))
    )
)

(defn all "Retorna um vetor com todas as infoormacoes da tabela centralidade ordenados por score decrescente."
	[]
  	(into [] (sql/query migration/spec ["select * from centrality order by closeness desc"]))
)

(defn all-closeness "Retorna um vetor com os nos e o score deles (closeness) ordenados por score decrescente."
	[] 
 	(into [] (sql/query migration/spec ["select no, closeness from centrality order by closeness desc"]))
)

(defn insert-node "Insere um No por um map {:no no, :closeness closeness, :farness farness}."
	[no]
	( if (-> (sql/query migration/spec [(str "select count(*) from centrality where no =" (no :no))]) first :count pos?)
		(sql/update! migration/spec :centrality {:no (no :no), :closeness (no :closeness), :farness (no :farness)} ["no = ?" (no :no)]) 
		(sql/insert! migration/spec :centrality (zipmap [:no :closeness :farness][(no :no)(no :closeness)(no :farness)]))  
	)
)

(defn update-fraud-node "Realize o update de fraude no No relativo a sua distancia DIST do no fraudulento."
	[no dist] 
	(when (-> (sql/query migration/spec [(str "select count(*) from centrality where no =" no)]) first :count pos?)
		(def closeness (-> (sql/query migration/spec [(str "select * from centrality where no =" no)]) first :closeness float))
		(def closeness (* closeness (- 1 (exp 0.5 dist)))) ;(1 - (1/2)^k)
		(if (= closeness 0.0) (def farness 0.0) (def farness (int (/ 1 closeness))))
		(sql/update! migration/spec :centrality {:no no, :closeness closeness, :farness farness} ["no = ?" no]) 
	)
)

(defn delete-content "Apaga todo o conteudo de centralidade do banco."
	[]
	(sql/db-do-commands migration/spec (str "delete from centrality"))
)

(defn node-exist? "Funcao que retorna true se o nó existe"
	[no]
	( if (-> (sql/query migration/spec [(str "select count(*) from centrality where no = " no)]) first :count pos?)
		true
		false
	)
)

(defn node-closeness "Retorna o closeness de um no."
	[no] 
	(if (node-exist? no)
		(-> (sql/query migration/spec [(str "select closeness from centrality where no =" no)]) 
			first 
			:closeness 
			float
		)
	)
)
