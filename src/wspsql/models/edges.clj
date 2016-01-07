(ns wspsql.models.edges
  (:require [clojure.java.jdbc :as sql]
        [wspsql.models.migration :as migration]))
()

(defn all 
  "Retorna um vetor com as edges ordenadas por data de criacao decrescente." 
  []
  (into [] (sql/query migration/spec ["select * from edges order by created desc"])))

(defn all-edges 
  "Retorna um vetor com as edges cadastradas." 
  []
  (into [] (sql/query migration/spec ["select noa, nob from edges"])))

(defn exist? 
  "Verifica se existe a ligacao." 
  [A B]
  (if (or 
       (-> (sql/query migration/spec [(str "select * from edges where noa = " A " and nob = " B)])
           first
           :created 
           nil? 
           not)
       (-> (sql/query migration/spec [(str "select * from edges where noa = " B " and nob = " A)])
           first 
           :created 
           nil? 
           not))
    true
    false))

;;DELETAR
;(defn last-insert 
;  "Retorna a data do ultimo edge cadastrado."
;  []
;  (-> (into [] (sql/query migration/spec ["select created from edges order by created desc limit 1"]))
;      first
;      :created))

(defn create 
  "Cria edge A - B."
  [A B]
  (sql/insert! migration/spec :edges (zipmap [:noA :noB][(int A) (int B)])))

(defn delete 
  "Deleta edge A - B."
  [A B]
  (if (-> (sql/query migration/spec [(str "select * from edges where noa = " A " and nob = " B)]) 
          first 
          :created 
          nil? 
          not) 
    (sql/delete! migration/spec :edges ["noa=? and nob=?" A B])
    (sql/delete! migration/spec :edges ["noa=? and nob=?" B A])))

(defn remove-all 
  "funcao que remove todos os edges do banco"
  []
  (sql/db-do-commands migration/spec (str "delete from edges")))

