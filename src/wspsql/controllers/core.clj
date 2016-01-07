(ns wspsql.controllers.core
  (:require [clojure.java.io :as io] 
        [clojure.set :as set]
        [ring.util.response :as ring]
        [wspsql.models.graph :as graph]
        [wspsql.models.fraud :as fraud]
        [wspsql.models.migration :as migration]
        [wspsql.models.edges :as edges]))

;funcao auxiliar para initial-edges
(defn validate-n-insert 
  "valida se a edge ja esta cadastrado e o insere no banco."
  [edge]
  (if-not (or (edges/exist? (edge :noa) (edge :nob)) 
          (= (edge :noa) (edge :nob)))
    (edges/create (edge :noa) (edge :nob))))

(defn initial-edges
  "Função que carrega no sistema os nos gravados em um txt"
  [fileName]
  (migration/remove-all-data)
  (with-open [rdr (io/reader (io/file (io/resource fileName)))]
    (doseq [line (line-seq rdr)]
      (->> (clojure.string/split line #" ")
           (map #(Integer. %))
           vec
           (zipmap [:noa :nob])
           validate-n-insert)))
  (ring/redirect "/edges"))

(defn search
  "Funcao que retorna um vetor com os maps de ligacao de um no x"
  [x y] 
  (if (empty? y) 
    [] 
    (if (some #(= x %) (vector ((y 0) :noa) ((y 0) :nob)))  
      (conj (search x (subvec y 1)) (y 0))
      (search x (subvec y 1)))))

(defn link-nodes
  "Funcao que retorna os valores diferentes de x 
  pertencentes a um vetor de vetores"
  [x y]
    (if (empty? y) 
      []
      (if (= x ((y 0) :noa)) 
        (conj (link-nodes x (subvec y 1)) ((y 0) :nob))
        (conj (link-nodes x (subvec y 1)) ((y 0) :noa)))))

(defn farness-node
  "retorna o farness de um nó x em um grafo"
  ([x base] (farness-node [x 0] base [] #{}))

  ([restrict] (reduce + (map :dist restrict)))

  ([x base vetor restrict]
    (let [vectors (search (x 0) base)
         tempBase (vec (remove (set vectors) base))
         nos (clojure.set/difference 
          (set (link-nodes (x 0) vectors)) 
          (set (map :no restrict)))
         tempVetor (into vetor nos)
         tempRestrict (-> restrict
                          (into (map 
                           #(hash-map :no %1 :dist %2)
                           (vec nos) 
                           (vec (take (count nos) (repeat (+ (x 1) 1)))))))] 
 
     (if-not (empty? tempVetor)
       (let [tempX (-> []
                       (assoc-in [0] (tempVetor 0))
                       (assoc-in [1] 
                        (-> (filter #(= (get % :no) (tempVetor 0)) tempRestrict)
                            first
                            :dist)))]
         (farness-node tempX tempBase (vec (drop 1 tempVetor)) tempRestrict))
       (farness-node tempRestrict)))))

(defn distance-nodes
  "retorna um vetor com os as distances de todos os nos do grafo para o no analisado"
  ([x base] (distance-nodes [x 0] base [] #{}))

  ([restrict] (vec restrict))

  ([x base vetor restrict]
   (let [vectors (search (x 0) base)
         tempBase (vec (remove (set vectors) base))
         nos (clojure.set/difference 
          (set (link-nodes (x 0) vectors)) 
          (set (map :no restrict)))
         tempVetor (into vetor nos)
         tempRestrict (-> restrict
                          (into (map 
                           #(hash-map :no %1 :dist %2)
                           (vec nos) 
                           (vec (take (count nos) (repeat (+ (x 1) 1)))))))] 
 
     (if-not (empty? tempVetor)
       (let [tempX (-> []
                       (assoc-in [0] (tempVetor 0))
                       (assoc-in [1] 
                        (-> (filter #(= (get % :no) (tempVetor 0)) tempRestrict)
                            first
                            :dist)))]
         (distance-nodes tempX tempBase (vec (drop 1 tempVetor)) tempRestrict))
       (distance-nodes tempRestrict)))))

(defn cascade-fraud 
  [no dist] 
  (when-not (empty? dist)
    (graph/update-fraud-node ((dist 0) :no) ((dist 0) :dist))
    (cascade-fraud no (subvec dist 1)))

  (when(empty? dist)
    (graph/update-fraud-node no 0)
    (fraud/set-fraudulent no)
    (fraud/apply-fraudulent no)))

(defn fraud-node 
  [no base]
  (when-not (and (fraud/fraudulent? no) (fraud/applied-fraud? no))
    (let [dist (distance-nodes no base)] (cascade-fraud no dist))))

(defn fraud 
  [base]
  (when-let [x (seq (fraud/not-applied))]
    (loop [data x, index 0]
      (when (seq data)
        (fraud-node ((first data) :no) base)
        (recur (rest data) (inc index))))))

(defn farness
  "calcula a centralidade de todos os nos do banco"
  ([base]
   (let [x (-> (apply sorted-set (map #(% :noa) base))
               (into (apply sorted-set (map #(% :nob) base))))]
  	
     (graph/remove-all)
     (loop [data x index 0]
       (when (seq data)
         (let 
          [no 
           (into {} (map 
                     #(hash-map :no %1 
                                :closeness (->> %2
                                                (/ 1)
                                                (* 1e9)
                                                (Math/round)
                                                (* 1e-9) 
                                                double)
                                :farness %2)
                     (vector (first data))
                     (vector (farness-node (first data) base))))]
           (graph/insert-node no)
           (recur (rest data) (inc index)))))         
     (fraud/unapply-all)
     (fraud base))))


