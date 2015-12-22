(ns wspsql.handler-test
  (:import [java.lang.String])
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [wspsql.handler :refer :all]))


(deftest test-app
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
      (is (= (:status response) 404))))

)
