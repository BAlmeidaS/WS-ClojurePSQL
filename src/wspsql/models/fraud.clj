(ns wspsql.models.fraud
  (:require [clojure.java.jdbc :as sql]
  			[wspsql.models.migration :as migration]))


(defn all "Retorna um vetor com todas os nos fraudelentos e se a fraude já foi aplicada." 
	[]
  	(into [] (sql/query migration/spec ["select * from fraud"]))
)

(defn set-fraudulent "Seta um No como fraudulento e deixa sua fraude como não aplicada."
	[no]
	( if (-> (sql/query migration/spec [(str "select count(*) from fraud where no =" no)]) first :count pos?)
		(sql/update! migration/spec :fraud {:no no, :applied false} ["no = ?" no]) 
		(sql/insert! migration/spec :fraud (zipmap [:no][no]))  
	)
)

(defn delete-fraudulent "Deleta a fraude de um no."
	[no]
	(sql/delete! migration/spec :fraud ["no=? " no])
)


(defn apply-fraudulent "Seta fraude de um no como aplicada."
	[no]
	( if (-> (sql/query migration/spec [(str "select count(*) from fraud where no =" no)]) first :count pos?)
		(sql/update! migration/spec :fraud {:no no, :applied true} ["no = ?" no]) 
		(sql/insert! migration/spec :fraud (zipmap [:no :applied](vector no true)))  
	)
)

(defn not-applied "Retorna um vetor com os Nos que possuem fraudes não aplicadas."
	[]
	(into [] (sql/query migration/spec ["select no from fraud where applied = FALSE"]))
)

(defn unapply-all "Desaplicar todos as fraudes de todos os nos que possuem fraudes."
	[]
	(sql/db-do-commands migration/spec (str "update fraud set applied = FALSE"))
)

(defn fraudulent? "Retorna true se o No está cadastrado como fraudulento."
	[no]
	( if (-> (sql/query migration/spec [(str "select count(*) from fraud where no =" no)]) first :count pos?)
		true
		false
	)
)

(defn applied-fraud? "Retorna true se o No além de estar cadastrado como fraudulento, também já tiver sua fraude aplicada."
	[no]
	( if (fraudulent? no)
		( if (-> (sql/query migration/spec [(str "select applied from fraud where no =" no)]) first :applied true?)
			true
			false
		)
		false
	)
)

(defn remove-all "funcao que remove todos as fraudes do banco"
	[]
	(sql/db-do-commands migration/spec (str "delete from fraud"))
)


