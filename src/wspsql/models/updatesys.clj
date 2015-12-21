(ns wspsql.models.updatesys
  (:require [clojure.java.jdbc :as sql]
  			    [clj-time.format :as time :refer :all]
            [wspsql.models.migration :as migration]))

(defn get-update "Retorna o horario da ultima atualizacao da tabela centrality"
  []
	(-> (into [] (sql/query migration/spec ["select update from updatesys where sys = 'centrality'"]))
		first
		:update
	)
)

(defn set-update "seta o horario de agora como a ultima atualizacao da tabela centrality"
    []
  	( if (-> (sql/query migration/spec ["select count(*) from updatesys where sys = 'centrality'"]) first :count pos?)
  		(sql/delete! migration/spec :updatesys ["sys=?" "centrality"])	
  	)
  	(sql/insert! migration/spec :updatesys (zipmap [:sys]["centrality"]))  
)

