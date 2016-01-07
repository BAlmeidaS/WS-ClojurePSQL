(ns wspsql.models.migration
  (:require [clojure.java.jdbc :as sql :refer :all]))


(def spec "postgresql://postgres:senha@172.17.0.2:5432/wsclojure")

(defn migrated? 
  []
  (-> (sql/query spec [(str "select count(*) from information_schema.tables "
                            "where table_name in ('edges', 'centrality', 'fraud')")])
      first 
      :count 
      (> 2)))

(defn info-of-tables
  [table info]
  (if (empty? table) 
    []
    (conj (info-of-tables (subvec table 1) info) ((table 0) (keyword info)))))

(defn drop-tables
  ([]
   (if-let [result (not-empty (sql/query spec [(str "select table_name from information_schema.tables "
                                                    "where table_name in ('edges', 'centrality', 'fraud')")]))]

     (-> result       
         vec
         (info-of-tables "table_name")
         drop-tables)))
  ([tables]
   (sql/db-do-commands spec (str "drop table " (tables 0)))
   (if-not (empty? (subvec tables 1))
     (drop-tables (subvec tables 1)))))
   

(defn remove-all-data 
  [] 
  (sql/db-do-commands spec (str "delete from edges"))
  (sql/db-do-commands spec (str "delete from centrality"))
  (sql/db-do-commands spec (str "delete from fraud")))


(defn migrate 
  []
  (when-not (migrated?)
    
    (drop-tables)

    (sql/db-do-commands 
     spec
     (sql/create-table-ddl
      :edges
      [:created :timestamp "PRIMARY KEY" "DEFAULT CURRENT_TIMESTAMP"]
      [:noA :integer "NOT NULL"]
      [:noB :integer "NOT NULL"]))

    (sql/db-do-commands 
     spec
     (sql/create-table-ddl
      :centrality
      [:no :integer "PRIMARY KEY" "NOT NULL"]
      [:closeness "numeric (10,9)" "NOT NULL"]
      [:farness "integer" "NOT NULL"]))
    
    (sql/db-do-commands 
     spec
     (sql/create-table-ddl
      :fraud
      [:no :integer "PRIMARY KEY"]
      [:applied :boolean "DEFAULT FALSE"]))))