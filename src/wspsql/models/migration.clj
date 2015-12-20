(ns wspsql.models.migration
  (:require [clojure.java.jdbc :as sql]
            [wspsql.models.edges :as edges]))

(defn migrated? []
  (-> (sql/query edges/spec
                 [(str "select count(*) from information_schema.tables "
                       "where table_name='edges'")])
      first :count pos?))

(defn migrate []
  (when (not (migrated?))
    (print "Criando tabelas...") (flush)
    (sql/db-do-commands edges/spec
                        (sql/create-table-ddl
                         :edges
                         [:created :timestamp "PRIMARY KEY" "DEFAULT CURRENT_TIMESTAMP"]
                         [:noA :integer "NOT NULL"]
                         [:noB :integer "NOT NULL"]))
    (println "feito!")))