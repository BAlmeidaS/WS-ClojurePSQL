(ns wspsql.models.centrality
  (:require [clojure.java.jdbc :as sql]))

(def spec "postgresql://postgres:230789@172.17.0.2:5432/wsclojure")

(defn all []
  (into [] (sql/query spec ["select * from centrality order by closeness desc"])))

(defn all-closeness []
  (into [] (sql/query spec ["select no, closeness from centrality order by closeness desc"])))

(defn insert-node [no]
	( if (-> (sql/query spec [(str "select count(*) from centrality where no =" (no :no))]) first :count pos?)
		(sql/update! spec :centrality {:no (no :no), :closeness (no :closeness), :farness (no :farness)} ["no = ?" (no :no)]) 
		(sql/insert! spec :centrality (zipmap [:no :closeness :farness][(no :no)(no :closeness)(no :farness)]))  
	)
)

(defn node-exist? [no]
	( if (-> (sql/query spec [(str "select count(*) from centrality where no = " no)]) first :count pos?)
		true
		false
	)
)


;	(print "Atualizando UpdateSys...")
;	(updatesys/update-farness 
;		(-> (sql/query spec [(str"select created from edges where noa = " A " and nob = " B "order by created desc")]) first :created)
;	)