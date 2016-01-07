(ns integration.web_service
  (:import [java.lang.String])
  (:require [clojure.test :refer :all]
               [ring.mock.request :as mock]
               [wspsql.handler :refer :all]
               [wspsql.models.graph :as graph]
               [wspsql.models.fraud :as fraud]
               [wspsql.models.edges :as edges]
               [wspsql.models.migration :as migration]
               [clojure.data.json :as json]))

(deftest test-endpoints
  ;Testes /
  (testing "GET main"
    (let [response (app (mock/request :get "/"))]
      (is (= (:status response) 200))
      (is (not (nil? (response :body))))
      (is (.contains (response :body) "Web Service - Analise Grafos"))))

  (testing "OPTIONS main"
    (let [response (app (mock/request :options "/"))]
      (is (= (response :status) 200))
      (is (.contains (response :body) "version"))))

  (testing "HEAD main"
    (let [response (app (mock/request :head "/"))]
      (is (= (response :status) 200))
      (is (nil? (response :body)))))

  (testing "Not Found main"
    (let [response (app (mock/request :get "/invalid"))]
      (is (= (:status response) 404))))

  ;testes /edges
  (testing "GET edges"
    (let [response (app (mock/request :get "/edges"))]
      (is (= (:status response) 200))
      (is (not (nil? (response :body))))
      (is (.contains (response :body) "Web Service - Analise Grafos"))))

  (testing "OPTIONS edges"
    (let [response (app (mock/request :options "/edges"))]
      (is (= (response :status) 200))
      (is (.contains (response :body) "Mostra todas as"))))

  (testing "HEAD edges"
    (let [response (app (mock/request :head "/edges"))]
      (is (= (response :status) 200))
      (is (nil? (response :body)))))

  (testing "Not Found edges"
    (let [response (app (mock/request :get "/edges/invalid"))]
      (is (= (:status response) 404))))

  ;testes /graph
  (testing "GET graph"
    (let [response (app (mock/request :get "/graph"))]
      (is (= (:status response) 200))
      (is (not (nil? (response :body))))
      (is (.contains (response :body) "Web Service - Analise Grafos"))))

  (testing "OPTIONS graph"
    (let [response (app (mock/request :options "/graph"))]
      (is (= (response :status) 200))
      (is (.contains (response :body) "tabela com os nos"))))

  (testing "HEAD graph"
    (let [response (app (mock/request :head "/graph"))]
      (is (= (response :status) 200))
      (is (nil? (response :body)))))

  (testing "Not Found graph"
    (let [response (app (mock/request :get "/graph/invalid"))]
      (is (= (:status response) 404))))

  ;testes /fraud
  (testing "GET fraud"
    (let [response (app (mock/request :get "/fraud"))]
      (is (= (:status response) 200))
      (is (not (nil? (response :body))))
      (is (.contains (response :body) "Web Service - Analise Grafos"))))

  (testing "OPTIONS fraud"
    (let [response (app (mock/request :options "/fraud"))]
      (is (= (response :status) 200))
      (is (.contains (response :body) "possuem fraude"))))

  (testing "HEAD fraud"
    (let [response (app (mock/request :head "/fraud"))]
      (is (= (response :status) 200))
      (is (nil? (response :body)))))

  (testing "Not Found fraud"
    (let [response (app (mock/request :get "/fraud/invalid"))]
      (is (= (:status response) 404)))))

(deftest test-managing-edges
  ;;Insercao de edge por put e deleao
  (testing "Put-Delete edges"
    (migration/remove-all-data)
    (app (mock/request :put "/edges/1/2"))
    (let [response (app (mock/request :get "/graph/1"))]
      (is (= (response :status) 200))
      (def body (json/read-json (response :body)))
      (is (contains? body :no))
      (is (= (body :no) 1))
      (is (contains? body :closeness))
      (is (= (body :closeness) 1.0)))

    (app (mock/request :delete "/edges/1/2"))
    (let [response (app (mock/request :get "/graph/1"))]
      (is (= (response :status) 404))
      (def body (json/read-json (response :body)))
      (is (not (contains? body :no)))
      (is (= body nil))))

  ;;Insercao de edge por post e deleao
  (testing "Post-Delete edges"
    (migration/remove-all-data)
    (app (mock/request :post "/edges/" {:no_a 1, :no_b 2} ))
    (let [response (app (mock/request :get "/graph/1"))]
      (is (= (response :status) 200))
      (def body (json/read-json (response :body)))
      (is (contains? body :no))
      (is (= (body :no) 1))
      (is (contains? body :closeness))
      (is (= (body :closeness) 1.0)))

    (app (mock/request :delete "/edges/1/2"))
    (let [response (app (mock/request :get "/graph/1"))]
      (is (= (response :status) 404))
      (def body (json/read-json (response :body)))
      (is (not (contains? body :no)))
      (is (= body nil)))

    (app (mock/request :post "/edges/?no_a=1&no_b=2"))
      (let [response (app (mock/request :get "/graph/1"))]
        (is (= (response :status) 200))
        (def body (json/read-json (response :body)))
        (is (contains? body :no))
        (is (= (body :no) 1))
        (is (contains? body :closeness))
        (is (= (body :closeness) 1.0)))

    (app (mock/request :delete "/edges/1/2"))
    (let [response (app (mock/request :get "/graph/1"))]
      (is (= (response :status) 404))
      (def body (json/read-json (response :body)))
      (is (not (contains? body :no)))
      (is (= body nil))))

  ;;Teste que verifica de uma forma geral o controle de edges, simula 4 grafos e calcula a centralidade dos nós de cada uma
  (testing "Managing Edges"
    (migration/remove-all-data)

    ;PRIMEIRA grafo - EDGES: 1-2 - FARNESS 1=1 2=1
    (app (mock/request :put "/edges/1/2"))
    ;Confere o valor do closeness do no 1
    (let [response (app (mock/request :get "/graph/1"))]
      (is (= (response :status) 200))
      (def body (json/read-json (response :body)))
      (is (contains? body :no))
      (is (= (body :no) 1))
      (is (contains? body :closeness))
      (is (= (body :closeness) 1.0)))

    ;Confere o valor do closeness do no 1
    (let [response (app (mock/request :get "/graph/2"))]
      (is (= (response :status) 200))
      (def body (json/read-json (response :body)))
      (is (contains? body :no))
      (is (= (body :no) 2))
      (is (contains? body :closeness))
      (is (= (body :closeness) 1.0)))

    ;SEGUNDA grafo - EDGES: 1-2, 2-3, 2-4 - FARNESS 1=5 2=3 3=5 4=5
    (app (mock/request :post "/edges/" {:no_a 2, :no_b 3} ))
    (app (mock/request :post "/edges/?no_a=2&no_b=4" ))
    (def nodesFarness [5 3 5 5])

    ;Confere o valor do closeness do no 1
    (let [response (app (mock/request :get "/graph/1"))]
      (is (= (response :status) 200))
      (def body (json/read-json (response :body)))
      (is (contains? body :no))
      (is (= (body :no) 1))
      (is (contains? body :closeness))
      (is (->> (/ 1 (nodesFarness 0))   ; 5 é o farness do no 1 na segunda grafo, 1/5 é o closeness do no
               double
               (format "%1.8f")
               read-string
                  (= (body :closeness)))))

    ;Confere o valor do closeness do no 2
    (let [response (app (mock/request :get "/graph/2"))]
      (is (= (response :status) 200))
      (def body (json/read-json (response :body)))
      (is (contains? body :no))
      (is (= (body :no) 2))
      (is (contains? body :closeness))
      (is (->> (/ 1 (nodesFarness 1))
               double
               (format "%1.8f")
               read-string
                  (= (body :closeness)))))

    ;Confere o valor do closeness do no 3
    (let [response (app (mock/request :get "/graph/3"))]
      (is (= (response :status) 200))
      (def body (json/read-json (response :body)))
      (is (contains? body :no))
      (is (= (body :no) 3))
      (is (contains? body :closeness))
      (is (->> (/ 1 (nodesFarness 2))
               double
               (format "%1.8f")
               read-string
                  (= (body :closeness)))))

    ;Confere o valor do closeness do no 4
    (let [response (app (mock/request :get "/graph/4"))]
      (is (= (response :status) 200))
      (def body (json/read-json (response :body)))
      (is (contains? body :no))
      (is (= (body :no) 4))
      (is (contains? body :closeness))
      (is (->> (/ 1 (nodesFarness 3)) 
               double
               (format "%1.8f")
               read-string
                  (= (body :closeness)))))

    ;TERCEIRA grafo - EDGES: 1-2, 1-3, 2-4, 3-5, 3-6 - FARNESS 1=8 2=10 3=8 4=14 5=12 6=12
    (app (mock/request :delete "/edges/3/2"))
    (app (mock/request :put "/edges/1/3"))
    (app (mock/request :put "/edges/5/3"))
    (app (mock/request :put "/edges/3/6"))
    (def nodesFarness [8 10 8 14 12 12])

    (let [response (app (mock/request :get "/graph/1"))]
      (is (= (response :status) 200))
      (def body (json/read-json (response :body)))
      (is (contains? body :no))
      (is (= (body :no) 1))
      (is (contains? body :closeness))
      (is (->> (/ 1 (nodesFarness 0))
               double
               (format "%1.8f")
               read-string
               (= (body :closeness)))))

    (let [response (app (mock/request :get "/graph/2"))]
      (is (= (response :status) 200))
      (def body (json/read-json (response :body)))
      (is (contains? body :no))
      (is (= (body :no) 2))
      (is (contains? body :closeness))
      (is (->> (/ 1 (nodesFarness 1))
               double
               (format "%1.8f")
               read-string
               (= (body :closeness)))))

    (let [response (app (mock/request :get "/graph/3"))]
      (is (= (response :status) 200))
      (def body (json/read-json (response :body)))
      (is (contains? body :no))
      (is (= (body :no) 3))
      (is (contains? body :closeness))
      (is (->> (/ 1 (nodesFarness 2)) 
               double
               (format "%1.8f")
               read-string
               (= (body :closeness)))))

    (let [response (app (mock/request :get "/graph/4"))]
      (is (= (response :status) 200))
      (def body (json/read-json (response :body)))
      (is (contains? body :no))
      (is (= (body :no) 4))
      (is (contains? body :closeness))
      (is (->> (/ 1 (nodesFarness 3))
               double
               (format "%1.8f")
               read-string
               (= (body :closeness))))) 

    (let [response (app (mock/request :get "/graph/5"))]
      (is (= (response :status) 200))
      (def body (json/read-json (response :body)))
      (is (contains? body :no))
      (is (= (body :no) 5))
      (is (contains? body :closeness))
      (is (->> (/ 1 (nodesFarness 4))
               double
               (format "%1.8f")
               read-string
               (= (body :closeness))))) 
    (let [response (app (mock/request :get "/graph/6"))]
      (is (= (response :status) 200))
      (def body (json/read-json (response :body)))
      (is (contains? body :no))
      (is (= (body :no) 6))
      (is (contains? body :closeness))
      (is (->> (/ 1 (nodesFarness 5))
               double
               (format "%1.8f")
               read-string
               (= (body :closeness))))) 

    ;QUARTA grafo - EDGES: 1-2, 1-3, 2-4, 3-4, 3-6, 4-6, 4-5, 5-6 - FARNESS 1=9 2=8 3=7 4=6 5=9 6=7
    (app (mock/request :delete "/edges/3/5"))
    (app (mock/request :put "/edges/4/3"))
    (app (mock/request :put "/edges/4/5"))
    (app (mock/request :put "/edges/4/6"))
    (app (mock/request :put "/edges/5/6"))
    (def nodesFarness [9 8 7 6 9 7])

    (let [response (app (mock/request :get "/graph/1"))]
      (is (= (response :status) 200))
      (def body (json/read-json (response :body)))
      (is (contains? body :no))
      (is (= (body :no) 1))
      (is (contains? body :closeness))
      (is (->> (/ 1 (nodesFarness 0))
               double
               (format "%1.8f")
               read-string
               (= (body :closeness)))))

    (let [response (app (mock/request :get "/graph/2"))]
      (is (= (response :status) 200))
      (def body (json/read-json (response :body)))
      (is (contains? body :no))
      (is (= (body :no) 2))
      (is (contains? body :closeness))
      (is (->> (/ 1 (nodesFarness 1))
               double
               (format "%1.8f")
               read-string
               (= (body :closeness)))))

    (let [response (app (mock/request :get "/graph/3"))]
      (is (= (response :status) 200))
      (def body (json/read-json (response :body)))
      (is (contains? body :no))
      (is (= (body :no) 3))
      (is (contains? body :closeness))
      (is (->> (/ 1 (nodesFarness 2)) 
               double
               (format "%1.8f")
               read-string
               (= (body :closeness)))))

    (let [response (app (mock/request :get "/graph/4"))]
      (is (= (response :status) 200))
      (def body (json/read-json (response :body)))
      (is (contains? body :no))
      (is (= (body :no) 4))
      (is (contains? body :closeness))
      (is (->> (/ 1 (nodesFarness 3))
               double
               (format "%1.8f")
               read-string
               (= (body :closeness)))))

    (let [response (app (mock/request :get "/graph/5"))]
      (is (= (response :status) 200))
      (def body (json/read-json (response :body)))
      (is (contains? body :no))
      (is (= (body :no) 5))
      (is (contains? body :closeness))
      (is (->> (/ 1 (nodesFarness 4))
               double
               (format "%1.8f")
               read-string
               (= (body :closeness))))) 

    (let [response (app (mock/request :get "/graph/6"))]
      (is (= (response :status) 200))
      (def body (json/read-json (response :body)))
      (is (contains? body :no))
      (is (= (body :no) 6))
      (is (contains? body :closeness))
      (is (->> (/ 1 (nodesFarness 5))
               double
               (format "%1.8f")
               read-string
               (= (body :closeness)))))))

;;;;Esse teste se consite em testar o sistema de fraudes, a ideia é criar dois grafos. No primeiro fraudar dois nos, e ver se o decaimento de score ocorre corretamente.
;;;;No segundo fraudaremos nó a nó até fraudar todos.
(deftest test-fraud-edges-first-graph
  ;;PRIMEIRO grafo - EDGES: 1-2, 1-3, 2-4, 3-5, 3-6 - FARNESS 1=8 2=10 3=8 4=14 5=12 6=12
  (testing "First Graph"
    (migration/remove-all-data)

    (app (mock/request :put "/edges/1/2"))
    (app (mock/request :put "/edges/1/3"))
    (app (mock/request :put "/edges/2/4"))
    (app (mock/request :put "/edges/5/3"))
    (app (mock/request :put "/edges/6/3"))
    (def nodesFarness [8 10 8 14 12 12])

    ;Fraude no 1
    (app (mock/request :put "/fraud/1"))
    ;testa dados do no 1
    (let [response (app (mock/request :get "/graph/1"))]
      (is (= (response :status) 200))
      (def body (json/read-json (response :body)))
      (is (contains? body :no))
      (is (= (body :no) 1))
      (is (contains? body :closeness))
      (is (= (body :closeness) 0))
      (is (contains? body :farness))
      (is (= (body :farness) 0))
      (is (contains? body :fraudulent))
      (is (= (body :fraudulent) true))) 

    ;testa o closeness dos outros nos
    (let [response (app (mock/request :get "/graph/2"))]
      (is (= (response :status) 200))
      (def body (json/read-json (response :body)))
      (is (contains? body :no))
      (is (= (body :no) 2))
      (is (contains? body :closeness))
      (is (->> (/ 1 (nodesFarness 1))
               (* 1/2) ;fator que diminui o score baseado na distancia 
               double
               (format "%1.8f")
               read-string
               (= (body :closeness)))))

    (let [response (app (mock/request :get "/graph/3"))]
      (is (= (response :status) 200))
      (def body (json/read-json (response :body)))
      (is (contains? body :no))
      (is (= (body :no) 3))
      (is (contains? body :closeness))
      (is (->> (/ 1 (nodesFarness 2))
               (* 1/2) ;fator que diminui o score baseado na distancia 
               double
               (format "%1.8f")
               read-string
               (= (body :closeness)))))

    (let [response (app (mock/request :get "/graph/4"))]
      (is (= (response :status) 200))
      (def body (json/read-json (response :body)))
      (is (contains? body :no))
      (is (= (body :no) 4))
      (is (contains? body :closeness))
      (is (->> (/ 1 (nodesFarness 3))
               (* 3/4) ;fator que diminui o score baseado na distancia 
               double
               (format "%1.8f")
               read-string
               (= (body :closeness)))))

    (let [response (app (mock/request :get "/graph/5"))]
      (is (= (response :status) 200))
      (def body (json/read-json (response :body)))
      (is (contains? body :no))
      (is (= (body :no) 5))
      (is (contains? body :closeness))
      (is (->> (/ 1 (nodesFarness 4))
               (* 3/4) ;fator que diminui o score baseado na distancia 
               double
               (format "%1.8f")
               read-string
               (= (body :closeness)))))

    (let [response (app (mock/request :get "/graph/6"))]
      (is (= (response :status) 200))
      (def body (json/read-json (response :body)))
      (is (contains? body :no))
      (is (= (body :no) 6))
      (is (contains? body :closeness))
      (is (->> (/ 1 (nodesFarness 5))
               (* 3/4) ;fator que diminui o score baseado na distancia 
               double
               (format "%1.8f")
               read-string
               (= (body :closeness)))))


    ;Fraude no 5
    (app (mock/request :put "/fraud/5"))
    ;testa dados do no 1
    (let [response (app (mock/request :get "/graph/1"))]
      (is (= (response :status) 200))
      (def body (json/read-json (response :body)))
      (is (contains? body :no))
      (is (= (body :no) 1))
      (is (contains? body :closeness))
      (is (= (body :closeness) 0))
      (is (contains? body :farness))
      (is (= (body :farness) 0))
      (is (contains? body :fraudulent))
      (is (= (body :fraudulent) true))) 

    ;testa o closeness dos outros nos
    (let [response (app (mock/request :get "/graph/2"))]
      (is (= (response :status) 200))
      (def body (json/read-json (response :body)))
      (is (contains? body :no))
      (is (= (body :no) 2))
      (is (contains? body :closeness))
      (is (->> (/ 1 (nodesFarness 1))
               (* 1/2) ;fator causado pela primeira fraude (no 1)
               (* 7/8) ;fator causado pela segunda fraude (no 5)
               double
               (format "%1.8f")
               read-string
               (= (body :closeness)))))

    (let [response (app (mock/request :get "/graph/3"))]
      (is (= (response :status) 200))
      (def body (json/read-json (response :body)))
      (is (contains? body :no))
      (is (= (body :no) 3))
      (is (contains? body :closeness))
      (is (->> (/ 1 (nodesFarness 2))
               (* 1/2) ;fator causado pela primeira fraude (no 1)
               (* 1/2) ;fator causado pela segunda fraude (no 5) 
               double
               (format "%1.8f")
               read-string
               (= (body :closeness)))))

    (let [response (app (mock/request :get "/graph/4"))]
      (is (= (response :status) 200))
      (def body (json/read-json (response :body)))
      (is (contains? body :no))
      (is (= (body :no) 4))
      (is (contains? body :closeness))
      (is (->> (/ 1 (nodesFarness 3))
               (* 3/4) ;fator causado pela primeira fraude (no 1)
               (* 15/16) ;fator causado pela segunda fraude (no 5) 
               double
               (format "%1.8f")
               read-string
               (= (body :closeness)))))

    (let [response (app (mock/request :get "/graph/5"))]
      (is (= (response :status) 200))
      (def body (json/read-json (response :body)))
      (is (contains? body :no))
      (is (= (body :no) 5))
      (is (contains? body :closeness))
      (is (= (body :closeness) 0))
      (is (contains? body :farness))
      (is (= (body :farness) 0))
      (is (contains? body :fraudulent))
      (is (= (body :fraudulent) true))) 

    (let [response (app (mock/request :get "/graph/6"))]
      (is (= (response :status) 200))
      (def body (json/read-json (response :body)))
      (is (contains? body :no))
      (is (= (body :no) 6))
      (is (contains? body :closeness))
      (is (->> (/ 1 (nodesFarness 5))
               (* 3/4) ;fator causado pela primeira fraude (no 1)
               (* 3/4) ;fator causado pela segunda fraude (no 5) 
               double
               (format "%1.8f")
               read-string
               (= (body :closeness))))))

  ;;SEGUNDO grafo - EDGES: 1-2, 2-3, 3-4, 4-5, 5-1 - FARNESS 1=8 2=10 3=8 4=14 5=12 6=12
  (testing "Second Graph"
    (migration/remove-all-data)

    (app (mock/request :put "/edges/1/2"))
    (app (mock/request :put "/edges/2/3"))
    (app (mock/request :put "/edges/3/4"))
    (app (mock/request :put "/edges/4/5"))
    (app (mock/request :put "/edges/5/1"))
    (def nodesFarness [6 6 6 6 6])

    ;Fraude no 1
    (app (mock/request :put "/fraud/1"))

    (let [response (app (mock/request :get "/graph/1"))]
      (is (= (response :status) 200))
      (def body (json/read-json (response :body)))
      (is (contains? body :no))
      (is (= (body :no) 1))
      (is (contains? body :closeness))
      (is (= (body :closeness) 0))
      (is (contains? body :farness))
      (is (= (body :farness) 0))
      (is (contains? body :fraudulent))
      (is (= (body :fraudulent) true))) 

    (let [response (app (mock/request :get "/graph/2"))]
      (is (= (response :status) 200))
      (def body (json/read-json (response :body)))
      (is (contains? body :no))
      (is (= (body :no) 2))
      (is (contains? body :closeness))
      (is (->> (/ 1 (nodesFarness 1))
               (* 1/2) ;Redução - primeira fraude 
               double
               (format "%1.8f")
               read-string
               (= (body :closeness)))))

    (let [response (app (mock/request :get "/graph/3"))]
      (is (= (response :status) 200))
      (def body (json/read-json (response :body)))
      (is (contains? body :no))
      (is (= (body :no) 3))
      (is (contains? body :closeness))
      (is (->> (/ 1 (nodesFarness 2))
               (* 3/4) ;Redução - primeira fraude 
               double        
               (format "%1.8f")
               read-string
               (= (body :closeness)))))

    (let [response (app (mock/request :get "/graph/4"))]
      (is (= (response :status) 200))
      (def body (json/read-json (response :body)))
      (is (contains? body :no))
      (is (= (body :no) 4))
      (is (contains? body :closeness))
      (is (->> (/ 1 (nodesFarness 3))
               (* 3/4) ;Redução - primeira fraude 
               double
               (format "%1.8f")
               read-string
               (= (body :closeness)))))

    (let [response (app (mock/request :get "/graph/5"))]
      (is (= (response :status) 200))
      (def body (json/read-json (response :body)))
      (is (contains? body :no))
      (is (= (body :no) 5))
      (is (contains? body :closeness))
      (is (->> (/ 1 (nodesFarness 4))
               (* 1/2) ;Redução - primeira fraude 
               double
               (format "%1.8f")
               read-string
               (= (body :closeness)))))

    ;Fraude no 2
    (app (mock/request :put "/fraud/2"))

    (let [response (app (mock/request :get "/graph/1"))]
      (is (= (response :status) 200))
      (def body (json/read-json (response :body)))
      (is (contains? body :no))
      (is (= (body :no) 1))
      (is (contains? body :closeness))
      (is (= (body :closeness) 0))
      (is (contains? body :farness))
      (is (= (body :farness) 0))
      (is (contains? body :fraudulent))
      (is (= (body :fraudulent) true))) 

    (let [response (app (mock/request :get "/graph/2"))]
      (is (= (response :status) 200))
      (def body (json/read-json (response :body)))
      (is (contains? body :no))
      (is (= (body :no) 2))
      (is (contains? body :closeness))
      (is (= (body :closeness) 0))
      (is (contains? body :farness))
      (is (= (body :farness) 0))
      (is (contains? body :fraudulent))
      (is (= (body :fraudulent) true))) 

    (let [response (app (mock/request :get "/graph/3"))]
      (is (= (response :status) 200))
      (def body (json/read-json (response :body)))
      (is (contains? body :no))
      (is (= (body :no) 3))
      (is (contains? body :closeness))
      (is (->> (/ 1 (nodesFarness 2))
               (* 3/4) ;Redução - primeira fraude
               (* 1/2) ;Redução - segunda fraude  
               double        
               (format "%1.8f")
               read-string
               (= (body :closeness)))))

    (let [response (app (mock/request :get "/graph/4"))]
      (is (= (response :status) 200))
      (def body (json/read-json (response :body)))
      (is (contains? body :no))
      (is (= (body :no) 4))
      (is (contains? body :closeness))
      (is (->> (/ 1 (nodesFarness 3))
               (* 3/4) ;Redução - primeira fraude
               (* 3/4) ;Redução - segunda fraude   
               double
               (format "%1.8f")
               read-string
               (= (body :closeness)))))

    (let [response (app (mock/request :get "/graph/5"))]
      (is (= (response :status) 200))
      (def body (json/read-json (response :body)))
      (is (contains? body :no))
      (is (= (body :no) 5))
      (is (contains? body :closeness))
      (is (->> (/ 1 (nodesFarness 4))
               (* 1/2) ;Redução - primeira fraude
               (* 3/4) ;Redução - segunda fraude   
               double
               (format "%1.8f")
               read-string
               (= (body :closeness)))))

    ;Fraude no 3
    (app (mock/request :put "/fraud/3"))

    (let [response (app (mock/request :get "/graph/1"))]
      (is (= (response :status) 200))
      (def body (json/read-json (response :body)))
      (is (contains? body :no))
      (is (= (body :no) 1))
      (is (contains? body :closeness))
      (is (= (body :closeness) 0))
      (is (contains? body :farness))
      (is (= (body :farness) 0))
      (is (contains? body :fraudulent))
      (is (= (body :fraudulent) true))) 

    (let [response (app (mock/request :get "/graph/2"))]
      (is (= (response :status) 200))
      (def body (json/read-json (response :body)))
      (is (contains? body :no))
      (is (= (body :no) 2))
      (is (contains? body :closeness))
      (is (= (body :closeness) 0))
      (is (contains? body :farness))
      (is (= (body :farness) 0))
      (is (contains? body :fraudulent))
      (is (= (body :fraudulent) true))) 

    (let [response (app (mock/request :get "/graph/3"))]
      (is (= (response :status) 200))
      (def body (json/read-json (response :body)))
      (is (contains? body :no))
      (is (= (body :no) 3))
      (is (contains? body :closeness))
      (is (= (body :closeness) 0))
      (is (contains? body :farness))
      (is (= (body :farness) 0))
      (is (contains? body :fraudulent))
      (is (= (body :fraudulent) true))) 

    (let [response (app (mock/request :get "/graph/4"))]
      (is (= (response :status) 200))
      (def body (json/read-json (response :body)))
      (is (contains? body :no))
      (is (= (body :no) 4))
      (is (contains? body :closeness))
      (is (->> (/ 1 (nodesFarness 3))
               (* 3/4) ;Redução - primeira fraude
               (* 3/4) ;Redução - segunda fraude 
               (* 1/2) ;Redução - terceira fraude    
               double
               (format "%1.8f")
               read-string
               (= (body :closeness)))))

    (let [response (app (mock/request :get "/graph/5"))]
      (is (= (response :status) 200))
      (def body (json/read-json (response :body)))
      (is (contains? body :no))
      (is (= (body :no) 5))
      (is (contains? body :closeness))
      (is (->> (/ 1 (nodesFarness 4))
               (* 1/2) ;Redução - primeira fraude
               (* 3/4) ;Redução - segunda fraude
               (* 3/4) ;Redução - terceira fraude    
               double
               (format "%1.8f")
               read-string
               (= (body :closeness)))))

    ;Fraude no 4
    (app (mock/request :put "/fraud/4"))

    (let [response (app (mock/request :get "/graph/1"))]
      (is (= (response :status) 200))
      (def body (json/read-json (response :body)))
      (is (contains? body :no))
      (is (= (body :no) 1))
      (is (contains? body :closeness))
      (is (= (body :closeness) 0))
      (is (contains? body :farness))
      (is (= (body :farness) 0))
      (is (contains? body :fraudulent))
      (is (= (body :fraudulent) true))) 

    (let [response (app (mock/request :get "/graph/2"))]
      (is (= (response :status) 200))
      (def body (json/read-json (response :body)))
      (is (contains? body :no))
      (is (= (body :no) 2))
      (is (contains? body :closeness))
      (is (= (body :closeness) 0))
      (is (contains? body :farness))
      (is (= (body :farness) 0))
      (is (contains? body :fraudulent))
      (is (= (body :fraudulent) true))) 

    (let [response (app (mock/request :get "/graph/3"))]
      (is (= (response :status) 200))
      (def body (json/read-json (response :body)))
      (is (contains? body :no))
      (is (= (body :no) 3))
      (is (contains? body :closeness))
      (is (= (body :closeness) 0))
      (is (contains? body :farness))
      (is (= (body :farness) 0))
      (is (contains? body :fraudulent))
      (is (= (body :fraudulent) true))) 

    (let [response (app (mock/request :get "/graph/4"))]
      (is (= (response :status) 200))
      (def body (json/read-json (response :body)))
      (is (contains? body :no))
      (is (= (body :no) 4))
      (is (contains? body :closeness))
      (is (= (body :closeness) 0))
      (is (contains? body :farness))
      (is (= (body :farness) 0))
      (is (contains? body :fraudulent))
      (is (= (body :fraudulent) true)))

    (let [response (app (mock/request :get "/graph/5"))]
      (is (= (response :status) 200))
      (def body (json/read-json (response :body)))
      (is (contains? body :no))
      (is (= (body :no) 5))
      (is (contains? body :closeness))
      (is (->> (/ 1 (nodesFarness 4))
               (* 1/2) ;Redução - primeira fraude
               (* 3/4) ;Redução - segunda fraude
               (* 3/4) ;Redução - terceira fraude 
               (* 1/2) ;Redução - quarta fraude    
               double
               (format "%1.8f")
               read-string
               (= (body :closeness)))))

    ;Fraude no 5
    (app (mock/request :put "/fraud/5"))

    (let [response (app (mock/request :get "/graph/1"))]
      (is (= (response :status) 200))
      (def body (json/read-json (response :body)))
      (is (contains? body :no))
      (is (= (body :no) 1))
      (is (contains? body :closeness))
      (is (= (body :closeness) 0))
      (is (contains? body :farness))
      (is (= (body :farness) 0))
      (is (contains? body :fraudulent))
      (is (= (body :fraudulent) true))) 

    (let [response (app (mock/request :get "/graph/2"))]
      (is (= (response :status) 200))
      (def body (json/read-json (response :body)))
      (is (contains? body :no))
      (is (= (body :no) 2))
      (is (contains? body :closeness))
      (is (= (body :closeness) 0))
      (is (contains? body :farness))
      (is (= (body :farness) 0))
      (is (contains? body :fraudulent))
      (is (= (body :fraudulent) true))) 

    (let [response (app (mock/request :get "/graph/3"))]
      (is (= (response :status) 200))
      (def body (json/read-json (response :body)))
      (is (contains? body :no))
      (is (= (body :no) 3))
      (is (contains? body :closeness))
      (is (= (body :closeness) 0))
      (is (contains? body :farness))
      (is (= (body :farness) 0))
      (is (contains? body :fraudulent))
      (is (= (body :fraudulent) true))) 

    (let [response (app (mock/request :get "/graph/4"))]
      (is (= (response :status) 200))
      (def body (json/read-json (response :body)))
      (is (contains? body :no))
      (is (= (body :no) 4))
      (is (contains? body :closeness))
      (is (= (body :closeness) 0))
      (is (contains? body :farness))
      (is (= (body :farness) 0))
      (is (contains? body :fraudulent))
      (is (= (body :fraudulent) true)))

    (let [response (app (mock/request :get "/graph/5"))]
      (is (= (response :status) 200))
      (def body (json/read-json (response :body)))
      (is (contains? body :no))
      (is (= (body :no) 5))
      (is (contains? body :closeness))
      (is (= (body :closeness) 0))
      (is (contains? body :farness))
      (is (= (body :farness) 0))
      (is (contains? body :fraudulent))
      (is (= (body :fraudulent) true)))))

(deftest test-special-cases
  ;;Testa um caso especifico de delecao
  ;;Cria um grafo simples: 1-2, 2-3. Considera como fraudulento os nos 1 e 2. Depois deleta a edge 1-2.
  ;;Verificar se:
  ;;No 1 deixou de existir
  ;;No 2 continua existindo
  ;;No 1 nao esta na lista de fraudes, ja que nao existe mais
  ;;No 2 continua na lista de fraudes, ja que ainda existe
  (testing "Special delete"
    (migration/remove-all-data)
    (app (mock/request :put "/edges/1/2"))
    (app (mock/request :put "/edges/2/3"))
    (let [response (app (mock/request :get "/graph/1"))]
      (is (= (response :status) 200))
      (def body (json/read-json (response :body)))
      (is (contains? body :no))
      (is (= (body :no) 1))
      (is (contains? body :closeness))
      (is (= (body :closeness) 0.33333333)))

    (let [response (app (mock/request :get "/graph/2"))]
      (is (= (response :status) 200))
      (def body (json/read-json (response :body)))
      (is (contains? body :no))
      (is (= (body :no) 2))
      (is (contains? body :closeness))
      (is (= (body :closeness) 0.5)))

    (let [response (app (mock/request :get "/graph/3"))]
      (is (= (response :status) 200))
      (def body (json/read-json (response :body)))
      (is (contains? body :no))
      (is (= (body :no) 3))
      (is (contains? body :closeness))
      (is (= (body :closeness) 0.33333333)))

    ;Fraude no 1 e no 2
    (app (mock/request :put "/fraud/1"))
    (app (mock/request :put "/fraud/2"))
    ;testa dados do no 1
    (let [response (app (mock/request :get "/graph/1"))]
      (is (= (response :status) 200))
      (def body (json/read-json (response :body)))
      (is (contains? body :no))
      (is (= (body :no) 1))
      (is (contains? body :closeness))
      (is (= (body :closeness) 0))
      (is (contains? body :farness))
      (is (= (body :farness) 0))
      (is (contains? body :fraudulent))
      (is (= (body :fraudulent) true))) 

    ;testa o closeness dos outros nos
    (let [response (app (mock/request :get "/graph/2"))]
      (is (= (response :status) 200))
      (def body (json/read-json (response :body)))
      (is (contains? body :no))
      (is (= (body :no) 2))
      (is (contains? body :closeness))
      (is (= (body :closeness) 0))
      (is (contains? body :farness))
      (is (= (body :farness) 0))
      (is (contains? body :fraudulent))
      (is (= (body :fraudulent) true))) 

    (let [response (app (mock/request :get "/graph/3"))]
      (is (= (response :status) 200))
      (def body (json/read-json (response :body)))
      (is (contains? body :no))
      (is (= (body :no) 3))
      (is (contains? body :closeness))
      (is (->> (/ 1 3)
               (* 1/2)
               (* 3/4) 
               double
               (format "%1.8f")
               read-string
               (= (body :closeness)))))

    ;;chamadas diretas ao models:
    (is (edges/exist? 1 2))
    (is (edges/exist? 2 3))
    (is (graph/node-exist? 1))
    (is (graph/node-exist? 2))
    (is (graph/node-exist? 3))
    (is (fraud/fraudulent? 1))
    (is (fraud/fraudulent? 2))
    (is (not (fraud/fraudulent? 3)))

    ;Delete da edge 1-2
    (app (mock/request :delete "/edges/1/2"))

    (let [response (app (mock/request :get "/graph/2"))]
      (is (= (response :status) 200))
      (def body (json/read-json (response :body)))
      (is (contains? body :no))
      (is (= (body :no) 2))
      (is (contains? body :closeness))
      (is (= (body :closeness) 0))
      (is (contains? body :farness))
      (is (= (body :farness) 0))
      (is (contains? body :fraudulent))
      (is (= (body :fraudulent) true))) 

    (let [response (app (mock/request :get "/graph/3"))]
      (is (= (response :status) 200))
      (def body (json/read-json (response :body)))
      (is (contains? body :no))
      (is (= (body :no) 3))
      (is (contains? body :closeness))
      (is (->> (/ 1 1)
               (* 1/2)
               double
               (format "%1.8f")
               read-string
               (= (body :closeness)))))

    ;;verificacao pretendida
    (is (not (edges/exist? 1 2)))
    (is (edges/exist? 2 3))
    (is (not (graph/node-exist? 1)))
    (is (graph/node-exist? 2))
    (is (graph/node-exist? 3))
    (is (not (fraud/fraudulent? 1)))
    (is (fraud/fraudulent? 2))
    (is (not (fraud/fraudulent? 3)))))

 




