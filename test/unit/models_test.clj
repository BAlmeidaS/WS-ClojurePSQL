(ns unit.models-test
  (:import [java.lang.String])
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [wspsql.handler :refer :all]
            [wspsql.models.graph :as graph]
            [wspsql.models.fraud :as fraud]
            [wspsql.models.edges :as edges]
            [wspsql.models.migration :as migration]
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

;;testes de inclusao e mudanca de nos diretamente
(deftest test-graph
  ;testes da funcao exponencial
  (is (= (graph/exp 2 8) 256))
  (is (= (graph/exp 1/2 4) 1/16))
  (is (= (graph/exp 0.5 8) 0.00390625))
  (is (= (graph/exp 1/2 32) 1/4294967296))

  ;insercao de nos com centralidade
  (graph/insert-node {:no 1, :closeness 0.2, :farness 5})
  (graph/insert-node {:no 2, :closeness 0.33333334, :farness 3})
  (graph/insert-node {:no 3, :closeness 0.2, :farness 5})
  (graph/insert-node {:no 4, :closeness 0.2, :farness 5})

  (is (graph/node-exist? 1))
  (is (graph/node-exist? 2))
  (is (graph/node-exist? 3))
  (is (graph/node-exist? 4))
  (is (not (graph/node-exist? 5)))

  (is (= (graph/node-closeness 1) 0.2))
  (is (= (graph/node-closeness 2) 0.33333334))
  (is (= (graph/node-closeness 3) 0.2))
  (is (= (graph/node-closeness 4) 0.2))
  (is (= (graph/node-closeness 5) nil))

  ;visualizacao dos nos e remocao geral
  (is (= (count (graph/all)) 4))
  (is (= (first (graph/all-closeness)) {:no 2, :closeness 0.333333340M})) 
  (is (= (last (graph/all-closeness)) {:no 4, :closeness 0.200000000M})) 

  (graph/remove-all) 
  (is (empty? (graph/all)))
  (is (not (graph/node-exist? 1))))

;;testes de inclusao e mudanca de fraudes
(deftest test-frauds

  ;Inclui Fraudes
  (fraud/set-fraudulent 1)
  (fraud/set-fraudulent 2)
  (fraud/set-fraudulent 3)
  (fraud/set-fraudulent 4)
  (fraud/set-fraudulent 5)

  ;ve se todas as fruades estão de fato não aplicadas
  (is (= (count (fraud/all)) 5))
  (is (= (count (fraud/not-applied)) 5))

  (is (false? (fraud/applied-fraud? 1)))
  (is (false? (fraud/applied-fraud? 2)))
  (is (false? (fraud/applied-fraud? 3)))
  (is (false? (fraud/applied-fraud? 4)))
  (is (false? (fraud/applied-fraud? 5)))
  (is (false? (fraud/applied-fraud? 99)))

  ;considera fraude de um no como aplicada
  (fraud/apply-fraudulent 3)
  (fraud/apply-fraudulent 5)

  (is (true? (fraud/applied-fraud? 3)))
  (is (true? (fraud/applied-fraud? 5)))

  ;Deleta uma fraude aplicada e uma não aplicada
  (fraud/delete-fraudulent 4)
  (fraud/delete-fraudulent 5)

  (is (= (count (fraud/all)) 3))
  (is (= (count (fraud/not-applied)) 2))

  ;verifica se um no esta na lista de fraudes, independente se a fraude foi contabilizada ou nao
  (is (fraud/fraudulent? 1))
  (is (fraud/fraudulent? 2))
  (is (fraud/fraudulent? 3))
  (is (not (fraud/fraudulent? 4)))
  (is (not (fraud/fraudulent? 5)))
  (is (not (fraud/fraudulent? 99)))

  ;desaplica todas as fraudes
  (fraud/unapply-all)

  (is (= (count (fraud/all)) 3))
  (is (= (count (fraud/not-applied)) 3))

  ;remove todas as fraudes
  (fraud/remove-all)

  (is (empty? (fraud/all)))
  (is (empty? (fraud/not-applied))))

;;Criar duas arvores e testar o utilizacao correta do fator que reduz o score de todos os nos quando um no é dito como fraudulento
(deftest test-fraud-complex-tree
  ;; PRIMEIRA ARVORE : 1-2, 1-3, 2-4, 3-5 
  (graph/insert-node {:no 1, :closeness 0.2, :farness 5})
  (graph/insert-node {:no 2, :closeness 0.33333334, :farness 3})
  (graph/insert-node {:no 3, :closeness 0.2, :farness 5})
  (graph/insert-node {:no 4, :closeness 0.2, :farness 5})

  (is (= (graph/node-closeness 1) 0.2))
  (is (= (graph/node-closeness 2) 0.33333334))
  (is (= (graph/node-closeness 3) 0.2))
  (is (= (graph/node-closeness 4) 0.2))

  ;Consideraremos o no 1 como fraudulento. Testaremos então o impacto de,
  ;supondo que ja tenha sido cadastrado na tabela de fraudes, 
  ;como sera o comportamento do score dos nos
  (graph/update-fraud-node 1 0)
  (graph/update-fraud-node 2 1)
  (graph/update-fraud-node 3 2)
  (graph/update-fraud-node 4 2)

  ;(graph/update-fraud-node 1)
  (is (= (graph/node-closeness 1) 0.0))
  (is (= (graph/node-closeness 2) 0.16666667))
  (is (= (graph/node-closeness 3) 0.15))
  (is (= (graph/node-closeness 4) 0.15))

  (graph/remove-all) 
  (is (empty? (graph/all)))

  ; SEGUNDA ARVORE : 1-2, 2-3, 3-4, 4-1 
  (graph/insert-node {:no 1, :closeness 0.25, :farness 4})
  (graph/insert-node {:no 2, :closeness 0.25, :farness 4})
  (graph/insert-node {:no 3, :closeness 0.25, :farness 4})
  (graph/insert-node {:no 4, :closeness 0.25, :farness 4})

  (is (= (graph/node-closeness 1) 0.25))
  (is (= (graph/node-closeness 2) 0.25))
  (is (= (graph/node-closeness 3) 0.25))
  (is (= (graph/node-closeness 4) 0.25))

  ;Fraude no 1
  (graph/update-fraud-node 1 0)
  (graph/update-fraud-node 2 1)
  (graph/update-fraud-node 3 1)
  (graph/update-fraud-node 4 2)

  (is (= (graph/node-closeness 1) 0.0))
  (is (= (graph/node-closeness 2) 0.125))
  (is (= (graph/node-closeness 3) 0.125))
  (is (= (graph/node-closeness 4) 0.1875))

  ;Fraude no 2
  (graph/update-fraud-node 1 1)
  (graph/update-fraud-node 2 0)
  (graph/update-fraud-node 3 2)
  (graph/update-fraud-node 4 1)

  (is (= (graph/node-closeness 1) 0.0))
  (is (= (graph/node-closeness 2) 0.0))
  (is (= (graph/node-closeness 3) 0.09375))
  (is (= (graph/node-closeness 4) 0.09375))

  ;Fraude no 3
  (graph/update-fraud-node 1 1)
  (graph/update-fraud-node 2 2)
  (graph/update-fraud-node 3 0)
  (graph/update-fraud-node 4 1)

  (is (= (graph/node-closeness 1) 0.0))
  (is (= (graph/node-closeness 2) 0.0))
  (is (= (graph/node-closeness 3) 0.0))
  (is (= (graph/node-closeness 4) 0.046875))

  ;Fraude no 4
  (graph/update-fraud-node 1 2)
  (graph/update-fraud-node 2 1)
  (graph/update-fraud-node 3 1)
  (graph/update-fraud-node 4 0)

  (is (= (graph/node-closeness 1) 0.0))
  (is (= (graph/node-closeness 2) 0.0))
  (is (= (graph/node-closeness 3) 0.0))
  (is (= (graph/node-closeness 4) 0.0))

  (graph/remove-all) 
  (is (empty? (graph/all))))
  



