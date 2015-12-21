(ns wspsql.controllers.firstpart
  (:require [clojure.java.io :as io] 
  			[clojure.set :as set]
  			[wspsql.models.centrality :as centrality]
  			[wspsql.models.updatesys :as updatesys]
  )
)


(defn 	not-equal [] (complement =))

(defn 	contem
		"Funcao que retorna se um numero x esta contido dentro de um map"
		([x y]
			(	if 
				(= nil (some #(= x %) (vector (y :noa) (y :nob)) ) ) 
				false 
				true
			)
		)
)

(defn 	search
		"Funcao que retorna um vetor com os maps de ligacao de um no x"
		([x y] (if (empty? y) [] 
					(if (contem x (y 0))  
						(conj (search x (subvec y 1)) (y 0))
						(search x (subvec y 1))	
					)
				)
		)
	)


(defn otherNodes
		"Funcao que retorna os valores diferentes de x 
		pertencentes a um vetor de vetores"
		([x y]
			(if (empty? y) []
					(if (= x ((y 0) :noa)) 
						(conj (otherNodes x (subvec y 1)) ((y 0) :nob))
						(conj (otherNodes x (subvec y 1)) ((y 0) :noa))										
					)
				)
		)
)

(defn includeN
	"Inclui i vezes no vetor o valor x"
	( [x vetor i]
		(into vetor (take i (repeat x)))
	)
)

(defn diffVectors
	"retorna um vetor de A - (A INTERSEC B)"
	([vecA vecB]
		(into [] (remove (into #{} vecB) vecA))
	)
) 

(defn farnessNode
	"retorna o farness de um nó x em um grafo"
	([x base]
		(farnessNode [x 0] base [] #{})
	)

	([restrict]
		;(into #{} restrict)
		(reduce + (map :dist restrict))

	)
	([x base vetor restrict]
		(def vectors (search (x 0) base))

		(def tempBase (diffVectors base vectors))

		
		(def nos 	(clojure.set/difference 
						(into #{} (otherNodes (x 0) vectors)) 
						(into #{} (map :no restrict)))
					)


		(def tempVetor (into vetor nos))

		(def tempRestrict restrict)

		(def tempRestrict
			(into tempRestrict
				(	map 
					#(hash-map :no %1 :dist %2)
					(into [] nos) 
					(into [] (take (count nos) (repeat (+ (x 1) 1))))
				)
			)
		)

		(when-not (empty? tempVetor)

			(def xTemp [])
			(def xTemp 	(assoc-in xTemp [0] (tempVetor 0)))
			(def xTemp 	(assoc-in xTemp [1] 
							((first 
								(filter #(= (get % :no) (tempVetor 0)) tempRestrict)) 
							:dist)	
						)
			)
		)

		(if (empty? tempVetor) 
			(farnessNode tempRestrict)
			(farnessNode xTemp tempBase (into [] (drop 1 tempVetor)) tempRestrict)
		)	
	)
)

(defn farness
	"retorna um set com os maps de cada no e seu farness"
	([base]
		(def x (apply sorted-set (map #(% :noa) base)))
		(def x (into x (apply sorted-set (map #(% :nob) base))))
		(def return #{})
		(print "Calculando centralidade dos nos...")
		(loop [data x, index 0]
	  		(when (seq data)
	  			(def no (into {}
	  						(	map 
								#(hash-map :no %1 :closeness (float (/ 1 %2)) :farness %2)
						  		(vector (first data))
						  		(vector (farnessNode (first data) base))
							)
						)
	  			)
	  			(centrality/insert-node no)
	  			(recur (rest data) (inc index))
	  		)
	  	)
	  	(print "atualizando updatesys...")
	  	(updatesys/set-update)
	  	(println "feito!")
	  	(into #{} return)
	)
)


