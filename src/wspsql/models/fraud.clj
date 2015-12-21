(ns wspsql.models.fraud
  (:require [clojure.java.jdbc :as sql]))

(def spec "postgresql://postgres:230789@172.17.0.2:5432/wsclojure")

(defn all []
  (into [] (sql/query spec ["select * from fraud"])))

(defn set-fraudulent [no]
	( if (-> (sql/query spec [(str "select count(*) from fraud where no =" no)]) first :count pos?)
		(sql/update! spec :fraud {:no no, :applied false} ["no = ?" no]) 
		(sql/insert! spec :fraud (zipmap [:no][no]))  
	)
)

(defn delete-fraudulent [no]
	(sql/delete! spec :fraud ["no=? " no])
)


(defn apply-fraudulent [no]
	( if (-> (sql/query spec [(str "select count(*) from fraud where no =" no)]) first :count pos?)
		(sql/update! spec :fraud {:no no, :applied true} ["no = ?" no]) 
		(sql/insert! spec :fraud (zipmap [:no :applied](vector no true)))  
	)
)

(defn not-applied []
	(into [] (sql/query spec ["select no from fraud where applied = FALSE"]))
)

(defn unapply-all []
	(sql/db-do-commands spec (str "update fraud set applied = FALSE"))
)

(defn fraudulent? [no]
	( if (-> (sql/query spec [(str "select count(*) from fraud where no =" no)]) first :count pos?)
		true
		false
	)
)

(defn applied-fraud? [no]
	( if (fraudulent? no)
		( if (-> (sql/query spec [(str "select applied from fraud where no =" no)]) first :applied true?)
			true
			false
		)
		false
	)
)

