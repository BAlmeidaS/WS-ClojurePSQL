(ns wspsql.models.updatesys
  (:require [clojure.java.jdbc :as sql]
  			[clj-time.format :as time :refer :all]))

(def spec "postgresql://postgres:230789@172.17.0.2:5432/wsclojure")

(defn get-update []
	(-> (into [] (sql/query spec ["select update from updatesys where sys = 'centrality'"]))
		first
		:update
	)
)

(defn set-update []
  	( if (-> (sql/query spec ["select count(*) from updatesys where sys = 'centrality'"]) first :count pos?)
  		(sql/delete! spec :updatesys ["sys=?" "centrality"])	
  	)
  	(sql/insert! spec :updatesys (zipmap [:sys]["centrality"]))  
  
)
