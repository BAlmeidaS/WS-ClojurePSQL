(ns wspsql.models.migration
  (:require [clojure.java.jdbc :as sql :refer :all]))

(def spec "postgresql://postgres:230789@172.17.0.2:5432/wsclojure")

(defn migrated? []
  (-> (sql/query spec
                 [(str "select count(*) from information_schema.tables "
                       "where table_name in ('edges', 'centrality', 'updatesys', 'fraud')")])
      first :count (> 3)))


(defn info-of-tables
        ([info table]
            (if (empty? table) []
                    (conj (info-of-tables info (subvec table 1)) ((table 0) (keyword info)))
            )
        )
)

(defn drop_tables
    ([tables]
        (when-not (empty? tables) 
            (sql/db-do-commands spec (str "drop table " (tables 0)) )
            (drop_tables (subvec tables 1))
        )
    )
)


(defn migrate []
  (when (not (migrated?))
    (print "Realizando Drop de tabelas com o mesmo nome...") (flush)
    (->>
        (sql/query spec
                 [  (str "select table_name from information_schema.tables "
                       "where table_name in ('edges', 'centrality', 'updatesys', 'fraud')")])
        (into [])
        (info-of-tables "table_name")
        (def tables)
    )
    (drop_tables tables)
    (println "feito!")

    (print "Criando tabela Edges...") (flush)
    (sql/db-do-commands spec
                        (sql/create-table-ddl
                         :edges
                         [:created :timestamp "PRIMARY KEY" "DEFAULT CURRENT_TIMESTAMP"]
                         [:noA :integer "NOT NULL"]
                         [:noB :integer "NOT NULL"]))
    (println "feito!")

    (print "Criando tabela Centrality...") (flush)
    (sql/db-do-commands spec
                        (sql/create-table-ddl
                         :centrality
                         [:no :integer "PRIMARY KEY" "NOT NULL"]
                         [:closeness "NUMERIC(8, 8)" "NOT NULL"]
                         [:farness "integer" "NOT NULL"]))
    (println "feito!")

    (print "Criando tabela UpdateSys...") (flush)
    (sql/db-do-commands spec
                        (sql/create-table-ddl
                         :updatesys
                         [:sys "VARCHAR(16)" "PRIMARY KEY" "NOT NULL"]
                         [:update :timestamp "DEFAULT CURRENT_TIMESTAMP"]))
    (println "feito!")

    (print "Criando tabela Fraud...") (flush)
    (sql/db-do-commands spec
                        (sql/create-table-ddl
                         :fraud
                         [:no :integer "PRIMARY KEY"]
                         [:applied :boolean "DEFAULT FALSE"]))
    (println "feito!")




  )
)