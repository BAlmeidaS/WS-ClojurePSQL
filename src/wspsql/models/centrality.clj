(ns wspsql.models.centrality
  (:require [clojure.java.jdbc :as sql]
  			[wspsql.models.migration :as migration]))

(defn exp [x n]
	(if (zero? n) 1
    	(* x (exp x (dec n)))))

(defn all []
  (into [] (sql/query migration/spec ["select * from centrality order by closeness desc"])))

(defn all-closeness []
  (into [] (sql/query migration/spec ["select no, closeness from centrality order by closeness desc"])))

(defn insert-node [no]
	( if (-> (sql/query migration/spec [(str "select count(*) from centrality where no =" (no :no))]) first :count pos?)
		(sql/update! migration/spec :centrality {:no (no :no), :closeness (no :closeness), :farness (no :farness)} ["no = ?" (no :no)]) 
		(sql/insert! migration/spec :centrality (zipmap [:no :closeness :farness][(no :no)(no :closeness)(no :farness)]))  
	)
)

(defn update-fraud-node [no dist]
	(when (-> (sql/query migration/spec [(str "select count(*) from centrality where no =" no)]) first :count pos?)
		(def closeness (-> (sql/query migration/spec [(str "select * from centrality where no =" no)]) first :closeness float))
		(def closeness (* closeness (- 1 (exp 0.5 dist)))) ;(1 - (1/2)^k)
		(if (= closeness 0.0) (def farness 0.0) (def farness (int (/ 1 closeness))))
		(println (str no "-" closeness))
		(sql/update! migration/spec :centrality {:no no, :closeness closeness, :farness farness} ["no = ?" no]) 
	)
)

(defn delete-content []
	(sql/db-do-commands migration/spec (str "delete from centrality"))
)


(defn node-exist? [no]
	( if (-> (sql/query migration/spec [(str "select count(*) from centrality where no = " no)]) first :count pos?)
		true
		false
	)
)


;	(print "Atualizando UpdateSys...")
;	(updatesys/update-farness 
;		(-> (sql/query migration/spec [(str"select created from edges where noa = " A " and nob = " B "order by created desc")]) first :created)
;	)