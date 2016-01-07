(ns unit.core-test
  (:import [java.lang.String])
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [wspsql.handler :refer :all]
            [wspsql.models.graph :as graph]
            [wspsql.models.fraud :as fraud]
            [wspsql.models.edges :as edges]
            [wspsql.controllers.core :as core]
            [wspsql.models.migration :as migration]
            [clojure.data.json :as json]))

;;Testa inclusão de edges no banco atraves do txt
(deftest test-initial-edges
  (migration/remove-all-data)
  (core/initial-edges "test.txt")
  (is (= (count (edges/show-all)) 8)))

(deftest test-validate-n-insert
  (migration/remove-all-data)
  ;criacao da grafo: 1-2, 1-3, 2-4, 3-4, 3-6, 4-6, 4-5, 5-6
  (core/validate-n-insert {:noa 1, :nob 2})
  (core/validate-n-insert {:noa 1, :nob 3})
  (core/validate-n-insert {:noa 2, :nob 4})
  (core/validate-n-insert {:noa 3, :nob 4})
  (core/validate-n-insert {:noa 3, :nob 6})
  (core/validate-n-insert {:noa 4, :nob 6})
  (core/validate-n-insert {:noa 4, :nob 5})
  (core/validate-n-insert {:noa 5, :nob 6})

  (is (= (count (edges/show-all)) 8))

  ;insercoes erradas ou repetidas
  (core/validate-n-insert {:noa 5, :nob 6})
  (core/validate-n-insert {:noa 6, :nob 5})
  (core/validate-n-insert {:noa 4, :nob 2})
  (core/validate-n-insert {:noa 3, :nob 1})
  (core/validate-n-insert {:noa 1, :nob 1})
  (core/validate-n-insert {:noa 2, :nob 2})

  (is (= (count (edges/show-all)) 8))

  (is (edges/exist? 1 2))
  (is (edges/exist? 2 1))
  (is (edges/exist? 1 3))
  (is (edges/exist? 3 1))
  (is (edges/exist? 2 4))
  (is (edges/exist? 4 2))
  (is (edges/exist? 3 4))
  (is (edges/exist? 3 6))
  (is (edges/exist? 4 6))
  (is (edges/exist? 4 5))
  (is (edges/exist? 5 6)))

;;Testa a busca de edges, dentro de um vetor de edges, que possuam o no procurado
(deftest test-search
  (migration/remove-all-data)
  (is ( = (set (core/search 1 [{:noa 1, :nob 2}, {:noa 2, :nob 4}, {:noa 2, :nob 5}, {:noa 4, :nob 1}, {:noa 4, :nob 5}, {:noa 1, :nob 3}]))
          #{{:noa 1, :nob 2}, {:noa 1, :nob 3}, {:noa 4, :nob 1}})))

;;Testa a a busca pelos nos que estão ligados ao no referência
(deftest test-link-nodes
  (migration/remove-all-data)
  (is (-> (core/link-nodes 1 [{:noa 6, :nob 1}, {:noa 1, :nob 2}, {:noa 1, :nob 3}, {:noa 4, :nob 1}])
          set
          (= #{6, 2, 3, 4}))))

;;Testa a função que calcula o farness de um nó
(deftest test-farness-node
  (test-initial-edges)
  (is (= (core/farness-node 1 (edges/show-all-edges)) 9))
  (is (= (core/farness-node 2 (edges/show-all-edges)) 8))
  (is (= (core/farness-node 3 (edges/show-all-edges)) 7))
  (is (= (core/farness-node 4 (edges/show-all-edges)) 6))
  (is (= (core/farness-node 5 (edges/show-all-edges)) 9))
  (is (= (core/farness-node 6 (edges/show-all-edges)) 7)))

;;Testa a função que devolve um vetor com as ditâncias dos outros nos do grafo ao nó referência
(deftest test-distance-nodes
  (test-initial-edges)
  (is (= (set (core/distance-nodes 1 (edges/show-all-edges)))
         (set [{:dist 1, :no 2} {:dist 1, :no 3} {:dist 2, :no 4} {:dist 3, :no 5} {:dist 2, :no 6}])))

  (is (= (set (core/distance-nodes 2 (edges/show-all-edges)))
         (set [{:dist 1, :no 1} {:dist 2, :no 3} {:dist 1, :no 4} {:dist 2, :no 5} {:dist 2, :no 6}])))

  (is (= (set (core/distance-nodes 3 (edges/show-all-edges)))
         (set [{:dist 1, :no 1} {:dist 2, :no 2} {:dist 1, :no 4} {:dist 2, :no 5} {:dist 1, :no 6}])))

  (is (= (set (core/distance-nodes 4 (edges/show-all-edges)))
         (set [{:dist 2, :no 1} {:dist 1, :no 2} {:dist 1, :no 3} {:dist 1, :no 5} {:dist 1, :no 6}])))

  (is (= (set (core/distance-nodes 5 (edges/show-all-edges)))
         (set [{:dist 3, :no 1} {:dist 2, :no 2} {:dist 2, :no 3} {:dist 1, :no 4} {:dist 1, :no 6}])))

  (is (= (set (core/distance-nodes 6 (edges/show-all-edges)))
         (set [{:dist 2, :no 1} {:dist 2, :no 2} {:dist 1, :no 3} {:dist 1, :no 4} {:dist 1, :no 5}]))))
  
(deftest test-farness
  (test-farness-node)
  (test-initial-edges)  

  (is (not (graph/node-exist? 1)))
  (is (not (graph/node-exist? 2)))
  (is (not (graph/node-exist? 3)))
  (is (not (graph/node-exist? 4)))
  (is (not (graph/node-exist? 5)))
  (is (not (graph/node-exist? 6)))

  (core/farness (edges/show-all-edges))

  (is (graph/node-exist? 1))
  (is (graph/node-exist? 2))
  (is (graph/node-exist? 3))
  (is (graph/node-exist? 4))
  (is (graph/node-exist? 5))
  (is (graph/node-exist? 6))

  (is (= (graph/node-closeness 1) 0.11111111))
  (is (= (graph/node-closeness 2) 0.125))
  (is (= (graph/node-closeness 3) 0.14285714))
  (is (= (graph/node-closeness 4) 0.16666667))
  (is (= (graph/node-closeness 5) 0.11111111))
  (is (= (graph/node-closeness 6) 0.14285714)))

(deftest test-cascade-fraud
  (test-distance-nodes)
  (test-farness)

  (fraud/set-fraudulent 1)

  (is (= (graph/node-closeness 1) 0.11111111))
  (is (= (graph/node-closeness 2) 0.125))
  (is (= (graph/node-closeness 3) 0.14285714))
  (is (= (graph/node-closeness 4) 0.16666667))
  (is (= (graph/node-closeness 5) 0.11111111))
  (is (= (graph/node-closeness 6) 0.14285714))

  (core/cascade-fraud 1 [{:dist 1, :no 2} {:dist 1, :no 3} {:dist 2, :no 4} {:dist 3, :no 5} {:dist 2, :no 6}])

  (is (= (graph/node-closeness 1) 0.0))
  (is (= (graph/node-closeness 2) 0.0625))
  (is (= (graph/node-closeness 3) 0.07142857))
  (is (= (graph/node-closeness 4) 0.125))
  (is (= (graph/node-closeness 5) 0.09722222))
  (is (= (graph/node-closeness 6) 0.10714286)))

(deftest test-fraud-node
  (test-distance-nodes)
  (test-farness)

  (fraud/set-fraudulent 1)  

  (is (= (graph/node-closeness 1) 0.11111111))
  (is (= (graph/node-closeness 2) 0.125))
  (is (= (graph/node-closeness 3) 0.14285714))
  (is (= (graph/node-closeness 4) 0.16666667))
  (is (= (graph/node-closeness 5) 0.11111111))
  (is (= (graph/node-closeness 6) 0.14285714))

  (core/fraud (edges/show-all-edges))

  (is (= (graph/node-closeness 1) 0.0))
  (is (= (graph/node-closeness 2) 0.0625))
  (is (= (graph/node-closeness 3) 0.07142857))
  (is (= (graph/node-closeness 4) 0.125))
  (is (= (graph/node-closeness 5) 0.09722222))
  (is (= (graph/node-closeness 6) 0.10714286)))

(deftest test-fraud
  (test-distance-nodes)
  (test-farness)

  (fraud/set-fraudulent 1)  

  (is (= (graph/node-closeness 1) 0.11111111))
  (is (= (graph/node-closeness 2) 0.125))
  (is (= (graph/node-closeness 3) 0.14285714))
  (is (= (graph/node-closeness 4) 0.16666667))
  (is (= (graph/node-closeness 5) 0.11111111))
  (is (= (graph/node-closeness 6) 0.14285714))

  (core/fraud-node 1 (edges/show-all-edges))

  (is (= (graph/node-closeness 1) 0.0))
  (is (= (graph/node-closeness 2) 0.0625))
  (is (= (graph/node-closeness 3) 0.07142857))
  (is (= (graph/node-closeness 4) 0.125))
  (is (= (graph/node-closeness 5) 0.09722222))
  (is (= (graph/node-closeness 6) 0.10714286)))


