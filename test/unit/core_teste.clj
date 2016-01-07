(ns unit.core-test
  (:import [java.lang.String])
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [wspsql.handler :refer :all]
            [wspsql.controllers.core :as core]
            [clojure.data.json :as json]))

;;testes de migracao do BD
(deftest test-migration
  (migration/drop-tables)
  (is (false? (migration/migrated?)))
  (migration/migrate)
  (is (true? (migration/migrated?))))

;;testes de inclusao e mudanca de edges
(deftest test-edges
  ;Apaga todos os dados anteriores
  (migration/remove-all-data)

  ;Criação e deleção de edges
  (edges/create 1 2)
  (is (edges/exist? 1 2))
  (is (edges/exist? 2 1))

  (edges/create 3 2)
  (is (edges/exist? 2 3))
  (is (edges/exist? 3 2))
  (edges/delete 3 2)
  (is (not (edges/exist? 2 3)))
  (is (not (edges/exist? 3 2)))

  (edges/create 3 2)
  (is (edges/exist? 2 3))
  (is (edges/exist? 3 2))
  (edges/delete 2 3)
  (is (not (edges/exist? 2 3)))
  (is (not (edges/exist? 3 2)))

  (edges/delete 1 2)

  (is (= (count (edges/all)) 0))  

  ;Visualizacao das edges
  (edges/create 1 2)
  (edges/create 2 3)
  (edges/create 3 4)
  (edges/create 4 5)
  (is (= (count (edges/all)) 4))

  (is (= (first (edges/all-edges)) {:noa 1, :nob 2})) 
  (is (= (last (edges/all-edges)) {:noa 4, :nob 5})) 

  ;Remocao geral
  (edges/remove-all)
  (is (empty? (edges/all)))
  (is (not (edges/exist? 1 2)))

  (edges/delete 1 2)
  (edges/create 1 2)
  (edges/create 1 2)
  (edges/create 1 2)
  (edges/delete 1 2)
  (is (empty? (edges/all))))

  



